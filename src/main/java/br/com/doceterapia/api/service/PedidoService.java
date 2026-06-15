package br.com.doceterapia.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.FormaEntrega;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final PagamentoService pagamentoService;

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = Map.ofEntries(
            Map.entry(StatusPedido.ORCAMENTO, Set.of(StatusPedido.AGUARDANDO_SINAL, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.AGUARDANDO_SINAL, Set.of(StatusPedido.ESPERANDO_FAZER, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.ESPERANDO_FAZER, Set.of(StatusPedido.EM_PRODUCAO, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.EM_PRODUCAO, Set.of(StatusPedido.AGUARDANDO_ENTREGA, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.AGUARDANDO_ENTREGA, Set.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO)),
            Map.entry(StatusPedido.ENTREGUE, Set.of())
    );

    public PedidoService(PedidoRepository repository, PedidoMapper mapper, PagamentoService pagamentoService) {
        this.repository = repository;
        this.mapper = mapper;
        this.pagamentoService = pagamentoService;
    }

    public List<PedidoResponseDTO> listarTodos() {
        return repository.findAllWithClienteEager()
                .stream()
                .map(mapper::toPedidoResponse)
                .toList();
    }

    public PedidoResponseDTO cadastrar(PedidoRequestDTO request) {
        Pedido pedido = mapper.toPedido(request);

        if (pedido.getFormaEntrega() == FormaEntrega.ENTREGA
                && (pedido.getEnderecoEntrega() == null || pedido.getEnderecoEntrega().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço de entrega é obrigatório quando a forma de entrega é ENTREGA");
        }

        repository.save(pedido);
        return mapper.toPedidoResponse(pedido);
    }

    public PedidoResponseDTO atualizar(Integer id, PedidoRequestDTO request) {
        Pedido existing = repository.findById(id)
                .orElseThrow(PedidoIdDontExistsException::new);

        TipoPedido tipoAnterior = existing.getTipoPedido();
        LocalDateTime dataCriacaoExistente = existing.getDataCriacao();
        Pedido pedido = mapper.toPedido(request);
        pedido.setIdPedido(id);
        pedido.setDataCriacao(dataCriacaoExistente);

        if (tipoAnterior == TipoPedido.ORCAMENTO && pedido.getTipoPedido() == TipoPedido.PEDIDO_CONFIRMADO) {
            pedido.setDataConfirmacao(LocalDateTime.now());
            pagamentoService.criar(pedido, BigDecimal.ZERO);
        }

        if (pedido.getFormaEntrega() == FormaEntrega.ENTREGA
                && (pedido.getEnderecoEntrega() == null || pedido.getEnderecoEntrega().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço de entrega é obrigatório quando a forma de entrega é ENTREGA");
        }

        repository.save(pedido);
        return mapper.toPedidoResponse(pedido);
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new PedidoIdDontExistsException();
        }
        repository.deleteById(id);
    }

    public PedidoResponseDTO atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(PedidoIdDontExistsException::new);

        Set<StatusPedido> transicoesPermitidas = TRANSICOES_VALIDAS.get(pedido.getStatusPedido());
        if (transicoesPermitidas == null || !transicoesPermitidas.contains(novoStatus)) {
            throw new StatusTransitionInvalidException(
                    "Transição de " + pedido.getStatusPedido() + " para " + novoStatus + " não é permitida");
        }

        pedido.setStatusPedido(novoStatus);
        repository.save(pedido);
        return mapper.toPedidoResponse(pedido);
    }
}
