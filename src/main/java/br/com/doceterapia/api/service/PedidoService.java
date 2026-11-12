package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.FormaEntrega;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.exception.DataEntregaNoPassadoException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.service.notificacao.AlertaDiaLotadoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final PagamentoService pagamentoService;
    private final AlertaDiaLotadoService alertaDiaLotadoService;
    private final MaquinaStatusPedido maquinaStatusPedido;
    private final BloqueioPedidoService bloqueioPedidoService;
    private final Clock clock;

    public PedidoService(PedidoRepository repository, PedidoMapper mapper, PagamentoService pagamentoService,
                         AlertaDiaLotadoService alertaDiaLotadoService, MaquinaStatusPedido maquinaStatusPedido,
                         BloqueioPedidoService bloqueioPedidoService, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.pagamentoService = pagamentoService;
        this.alertaDiaLotadoService = alertaDiaLotadoService;
        this.maquinaStatusPedido = maquinaStatusPedido;
        this.bloqueioPedidoService = bloqueioPedidoService;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponseDTO cadastrar(PedidoRequestDTO request) {
        Pedido pedido = mapper.toPedido(request);
        configurarEstadoInicial(pedido, request.getStatusPedido());
        if (pedido.getDataEntrega().isBefore(LocalDate.now(clock))) {
            throw new DataEntregaNoPassadoException();
        }
        pedido.setDataCriacao(LocalDateTime.now(clock));
        validarEntrega(pedido);
        repository.save(pedido);
        if (pedido.getTipoPedido() == TipoPedido.PEDIDO_CONFIRMADO) {
            pagamentoService.criar(pedido);
        }
        alertaDiaLotadoService.avaliar(pedido.getDataEntrega());
        return mapper.toPedidoResponse(pedido);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponseDTO atualizar(Integer id, PedidoRequestDTO request) {
        Pedido existente = bloqueioPedidoService.buscar(id);
        Pedido pedido = mapper.toPedido(request);
        pedido.setIdPedido(id);
        pedido.setDataCriacao(existente.getDataCriacao());
        pedido.setDataConfirmacao(existente.getDataConfirmacao());
        pedido.setTipoPedido(existente.getTipoPedido());
        pedido.setStatusPedido(existente.getStatusPedido());
        if (!pedido.getDataEntrega().equals(existente.getDataEntrega()) && pedido.getDataEntrega().isBefore(LocalDate.now(clock))) {
            throw new DataEntregaNoPassadoException();
        }
        validarEntrega(pedido);
        existente.setCliente(pedido.getCliente());
        existente.setFormaEntrega(pedido.getFormaEntrega());
        existente.setEnderecoEntrega(pedido.getEnderecoEntrega());
        existente.setCep(pedido.getCep());
        existente.setDataEntrega(pedido.getDataEntrega());
        existente.setAnotacao(pedido.getAnotacao());
        repository.save(existente);
        alertaDiaLotadoService.avaliar(existente.getDataEntrega());
        return mapper.toPedidoResponse(existente);
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new PedidoIdDontExistsException();
        }
        repository.deleteById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PedidoResponseDTO atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = bloqueioPedidoService.buscar(id);
        if (novoStatus == StatusPedido.CANCELADO) {
            throw new StatusTransitionInvalidException("Use o registro de cancelamento para cancelar um pedido.");
        }
        if (pedido.getStatusPedido() == StatusPedido.AGUARDANDO_SINAL && novoStatus == StatusPedido.ESPERANDO_FAZER) {
            throw new StatusTransitionInvalidException("O pedido só pode avançar após o registro do sinal.");
        }
        maquinaStatusPedido.validar(pedido.getStatusPedido(), novoStatus);
        pedido.setStatusPedido(novoStatus);
        if (novoStatus == StatusPedido.AGUARDANDO_SINAL) {
            pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
            pedido.setDataConfirmacao(LocalDateTime.now(clock));
            pagamentoService.criar(pedido);
        }
        repository.save(pedido);
        return mapper.toPedidoResponse(pedido);
    }

    private void configurarEstadoInicial(Pedido pedido, StatusPedido statusSolicitado) {
        if (statusSolicitado == null || statusSolicitado == StatusPedido.ORCAMENTO) {
            pedido.setTipoPedido(TipoPedido.ORCAMENTO);
            pedido.setStatusPedido(StatusPedido.ORCAMENTO);
            return;
        }
        pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_SINAL);
        pedido.setDataConfirmacao(LocalDateTime.now(clock));
    }

    private void validarEntrega(Pedido pedido) {
        if (pedido.getFormaEntrega() == FormaEntrega.ENTREGA
                && (pedido.getEnderecoEntrega() == null || pedido.getEnderecoEntrega().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Endereço de entrega é obrigatório quando a forma de entrega é ENTREGA");
        }
    }
}
