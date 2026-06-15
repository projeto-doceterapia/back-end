package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.PagamentoRequestDTO;
import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.exception.PagamentoAlreadyExistsException;
import br.com.doceterapia.api.exception.PagamentoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagamentoMapper mapper;

    public PagamentoService(PagamentoRepository pagamentoRepository, PedidoRepository pedidoRepository, PagamentoMapper mapper) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.mapper = mapper;
    }

    public List<PagamentoResponseDTO> listarTodos() {
        return pagamentoRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public PagamentoResponseDTO buscarPorId(Integer id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(PagamentoIdDontExistsException::new);
        return mapper.toResponseDTO(pagamento);
    }

    public PagamentoResponseDTO buscarPorPedidoId(Integer pedidoId) {
        Pagamento pagamento = pagamentoRepository.findByPedidoIdPedido(pedidoId)
                .orElseThrow(PagamentoIdDontExistsException::new);
        return mapper.toResponseDTO(pagamento);
    }

    public Pagamento criar(Pedido pedido, BigDecimal valorTotal) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValorTotal(valorTotal);
        BigDecimal metade = valorTotal.multiply(BigDecimal.valueOf(0.5));
        pagamento.setValorSinal(metade);
        pagamento.setValorRestante(metade);
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        return pagamentoRepository.save(pagamento);
    }

    public PagamentoResponseDTO atualizar(Integer id, PagamentoRequestDTO dto) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(PagamentoIdDontExistsException::new);
        if (dto.getFormaPagamento() != null) {
            pagamento.setFormaPagamento(dto.getFormaPagamento());
        }
        if (dto.getDataPagamentoSinal() != null) {
            pagamento.setDataPagamentoSinal(dto.getDataPagamentoSinal());
        }
        if (dto.getDataPagamentoRestante() != null) {
            pagamento.setDataPagamentoRestante(dto.getDataPagamentoRestante());
        }
        pagamentoRepository.save(pagamento);
        return mapper.toResponseDTO(pagamento);
    }

    public PagamentoResponseDTO registrarSinal(Integer id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(PagamentoIdDontExistsException::new);
        if (pagamento.getDataPagamentoSinal() != null) {
            throw new IllegalStateException("Sinal já foi pago para este pagamento");
        }
        pagamento.setDataPagamentoSinal(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_PAGAMENTO);
        pagamentoRepository.save(pagamento);
        return mapper.toResponseDTO(pagamento);
    }

    public PagamentoResponseDTO registrarRestante(Integer id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(PagamentoIdDontExistsException::new);
        if (pagamento.getDataPagamentoSinal() == null) {
            throw new IllegalStateException("Sinal deve ser pago antes do restante");
        }
        pagamento.setDataPagamentoRestante(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.PAGO_INTEGRAL);
        pagamentoRepository.save(pagamento);
        return mapper.toResponseDTO(pagamento);
    }

    public void deletar(Integer id) {
        if (!pagamentoRepository.existsById(id)) {
            throw new PagamentoIdDontExistsException();
        }
        pagamentoRepository.deleteById(id);
    }
}
