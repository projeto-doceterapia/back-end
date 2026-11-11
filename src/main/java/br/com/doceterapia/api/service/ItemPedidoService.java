package br.com.doceterapia.api.service;

import org.springframework.stereotype.Service;
import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.exception.ItemPedidoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoFinanceiramenteFechadoException;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProducaoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.entity.Pedido;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ItemPedidoService {

    private final ItemPedidoRepository repository;
    private final ItemPedidoMapper mapper;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProducaoRepository producaoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoService pagamentoService;
    private final BloqueioPedidoService bloqueioPedidoService;

    public ItemPedidoService(ItemPedidoRepository repository, ItemPedidoMapper mapper,
                             ProdutoRepository produtoRepository, PedidoRepository pedidoRepository,
                             ProducaoRepository producaoRepository, PagamentoRepository pagamentoRepository,
                             PagamentoService pagamentoService, BloqueioPedidoService bloqueioPedidoService) {
        this.repository = repository;
        this.mapper = mapper;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.producaoRepository = producaoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoService = pagamentoService;
        this.bloqueioPedidoService = bloqueioPedidoService;
    }

    public List<ItemPedido> listarTodos() {
        return repository.findAll();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemPedido cadastrar(ItemPedidoRequestDTO request) {
        if (!produtoRepository.existsById(request.getFkProduto())) {
            throw new ProdutoIdDontExistsException();
        }

        if (!pedidoRepository.existsById(request.getFkPedido())) {
            throw new PedidoIdDontExistsException();
        }

        permitirAlteracao(bloqueioPedidoService.buscar(request.getFkPedido()));

        ItemPedido entity = mapper.toEntity(request);
        repository.save(entity);
        pagamentoService.recalcular(entity.getPedido().getIdPedido());
        return entity;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemPedido atualizar(Integer id, ItemPedidoRequestDTO request) {
        if (!repository.existsById(id)) {
            throw new ItemPedidoIdDontExistsException();
        }
        Integer pedidoId = repository.buscarPedidoId(id).orElseThrow(ItemPedidoIdDontExistsException::new);
        Pedido pedido = bloqueioPedidoService.buscar(pedidoId);
        permitirAlteracao(pedido);

        if (!produtoRepository.existsById(request.getFkProduto())) {
            throw new ProdutoIdDontExistsException();
        }

        if (!pedidoId.equals(request.getFkPedido())) {
            throw new PedidoFinanceiramenteFechadoException();
        }

        ItemPedido entity = repository.findById(id).orElseThrow(ItemPedidoIdDontExistsException::new);
        entity.setQuantidade(request.getQuantidade());
        entity.setValorUnitario(request.getValorUnitario());
        entity.setObservacao(request.getObservacao());

        BigDecimal valorTotal = request.getValorUnitario().multiply(request.getQuantidade())
                .setScale(2, RoundingMode.HALF_UP);
        entity.setValorTotal(valorTotal);
        BigDecimal custoEstimadoItem = entity.getCustoEstimadoItem();
        entity.setMargemLucroItem(custoEstimadoItem == null ? null : valorTotal.subtract(custoEstimadoItem));

        repository.save(entity);
        pagamentoService.recalcular(pedidoId);
        return entity;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ItemPedidoIdDontExistsException();
        }
        Integer pedidoId = repository.buscarPedidoId(id).orElseThrow(ItemPedidoIdDontExistsException::new);
        permitirAlteracao(bloqueioPedidoService.buscar(pedidoId));
        repository.deleteById(id);
        pagamentoService.recalcular(pedidoId);
    }

    private void permitirAlteracao(Pedido pedido) {
        if (pedido.getStatusPedido() == StatusPedido.CANCELADO || pedido.getStatusPedido() == StatusPedido.ENTREGUE
                || pedido.getStatusPedido() == StatusPedido.EM_PRODUCAO
                || pedido.getStatusPedido() == StatusPedido.AGUARDANDO_ENTREGA
                || producaoRepository.findByPedidoIdPedido(pedido.getIdPedido())
                .map(producao -> producao.getDataInicio() != null).orElse(false)
                || pagamentoRepository.findByPedidoIdPedido(pedido.getIdPedido())
                .map(pagamento -> pagamento.getStatusPagamento() == StatusPagamento.PAGO_INTEGRAL).orElse(false)) {
            throw new PedidoFinanceiramenteFechadoException();
        }
    }
}
