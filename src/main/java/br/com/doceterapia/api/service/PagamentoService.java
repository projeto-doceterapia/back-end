package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.dto.RegistroPagamentoRequestDTO;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoService {
    private final PagamentoRepository pagamentoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PagamentoMapper mapper;
    private final MaquinaStatusPedido maquinaStatusPedido;
    private final BloqueioPedidoService bloqueioPedidoService;
    private final EntityManager entityManager;
    private final Clock clock;

    public PagamentoService(PagamentoRepository pagamentoRepository, ItemPedidoRepository itemPedidoRepository,
                            PagamentoMapper mapper, MaquinaStatusPedido maquinaStatusPedido,
                            BloqueioPedidoService bloqueioPedidoService, EntityManager entityManager, Clock clock) {
        this.pagamentoRepository = pagamentoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.mapper = mapper;
        this.maquinaStatusPedido = maquinaStatusPedido;
        this.bloqueioPedidoService = bloqueioPedidoService;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    public PagamentoResponseDTO buscarPorId(Integer id) { return mapper.toResponseDTO(pagamentoRepository.findById(id).orElseThrow(PagamentoIdDontExistsException::new)); }
    public PagamentoResponseDTO buscarPorPedidoId(Integer pedidoId) { return mapper.toResponseDTO(pagamentoRepository.findByPedidoIdPedido(pedidoId).orElseThrow(PagamentoIdDontExistsException::new)); }

    @Transactional
    public Pagamento criar(Pedido pedido) {
        if (pagamentoRepository.existsByPedidoIdPedido(pedido.getIdPedido())) return pagamentoRepository.findByPedidoIdPedido(pedido.getIdPedido()).orElseThrow();
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        atualizarValores(pagamento, somarItens(pedido.getIdPedido()));
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public void recalcular(Integer pedidoId) {
        bloqueioPedidoService.buscar(pedidoId);
        Pagamento pagamento = pagamentoRepository.findByPedidoIdPedido(pedidoId).orElse(null);
        if (pagamento == null) return;
        entityManager.lock(pagamento, LockModeType.PESSIMISTIC_WRITE);
        atualizarValores(pagamento, somarItens(pedidoId));
        pagamentoRepository.save(pagamento);
    }

    @Transactional
    public PagamentoResponseDTO registrarSinal(Integer id, RegistroPagamentoRequestDTO request) {
        bloquearPedidoDoPagamento(id);
        Pagamento pagamento = pagamentoRepository.findById(id).orElseThrow(PagamentoIdDontExistsException::new);
        entityManager.lock(pagamento, LockModeType.PESSIMISTIC_WRITE);
        validarRegistro(pagamento, request);
        if (pagamento.getDataPagamentoSinal() != null) throw new SinalJaRegistradoException();
        maquinaStatusPedido.validar(pagamento.getPedido().getStatusPedido(), StatusPedido.ESPERANDO_FAZER);
        pagamento.setFormaPagamentoSinal(request.getFormaPagamento());
        pagamento.setDataPagamentoSinal(dataRegistro(request));
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_PAGAMENTO);
        pagamento.getPedido().setStatusPedido(StatusPedido.ESPERANDO_FAZER);
        pagamentoRepository.save(pagamento);
        return mapper.toResponseDTO(pagamento);
    }

    @Transactional
    public PagamentoResponseDTO registrarRestante(Integer id, RegistroPagamentoRequestDTO request) {
        bloquearPedidoDoPagamento(id);
        Pagamento pagamento = pagamentoRepository.findById(id).orElseThrow(PagamentoIdDontExistsException::new);
        entityManager.lock(pagamento, LockModeType.PESSIMISTIC_WRITE);
        validarRegistro(pagamento, request);
        if (pagamento.getDataPagamentoSinal() == null) throw new SinalNaoRegistradoException();
        if (pagamento.getDataPagamentoRestante() != null) throw new RestanteJaRegistradoException();
        pagamento.setFormaPagamentoRestante(request.getFormaPagamento());
        pagamento.setDataPagamentoRestante(dataRegistro(request));
        pagamento.setStatusPagamento(StatusPagamento.PAGO_INTEGRAL);
        pagamentoRepository.save(pagamento);
        return mapper.toResponseDTO(pagamento);
    }

    private void atualizarValores(Pagamento pagamento, BigDecimal total) {
        BigDecimal totalNormalizado = total.setScale(2, RoundingMode.HALF_UP);
        if (pagamento.getDataPagamentoSinal() != null && totalNormalizado.compareTo(pagamento.getValorSinal()) < 0) throw new TotalAbaixoDoSinalPagoException(totalNormalizado, pagamento.getValorSinal());
        pagamento.setValorTotal(totalNormalizado);
        if (pagamento.getDataPagamentoSinal() == null) {
            BigDecimal sinal = totalNormalizado.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
            pagamento.setValorSinal(sinal);
            pagamento.setValorRestante(totalNormalizado.subtract(sinal));
        } else pagamento.setValorRestante(totalNormalizado.subtract(pagamento.getValorSinal()));
    }

    private BigDecimal somarItens(Integer pedidoId) { return itemPedidoRepository.findByPedidoIdPedido(pedidoId).stream().map(item -> item.getValorTotal()).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private void bloquearPedidoDoPagamento(Integer pagamentoId) {
        Integer pedidoId = pagamentoRepository.buscarPedidoId(pagamentoId)
                .orElseThrow(PagamentoIdDontExistsException::new);
        bloqueioPedidoService.buscar(pedidoId);
    }

    private void validarRegistro(Pagamento pagamento, RegistroPagamentoRequestDTO request) {
        if (pagamento.getPedido().getStatusPedido() == StatusPedido.CANCELADO) throw new PagamentoPedidoCanceladoException();
        if (pagamento.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) throw new PagamentoSemItensException();
        if (dataRegistro(request).isAfter(LocalDate.now(clock))) throw new DataPagamentoFuturaException();
    }
    private LocalDate dataRegistro(RegistroPagamentoRequestDTO request) { return request.getDataPagamento() == null ? LocalDate.now(clock) : request.getDataPagamento(); }
}
