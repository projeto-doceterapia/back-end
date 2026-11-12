package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.*;
import br.com.doceterapia.api.enums.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@DisplayName("Consultas filtradas de pedidos")
class PedidoConsultaRepositoriesDataJpaTest {

    private static final LocalDate DE = LocalDate.of(2026, 11, 1);
    private static final LocalDate ATE = LocalDate.of(2026, 11, 30);

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ItemPedidoRepository itemPedidoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private ProducaoRepository producaoRepository;
    @Autowired private CancelamentoPedidoRepository cancelamentoRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @PersistenceContext private EntityManager entityManager;

    private Statistics statistics;
    private Pedido primeiroPedido;
    private Pedido segundoPedido;
    private Pedido pedidoCancelado;
    private Pedido pedidoForaDoPeriodo;

    @BeforeEach
    void criarMassaReal() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        Cliente cliente = persistirCliente("Cliente consultado");
        Cliente outroCliente = persistirCliente("Outro cliente");
        Produto produto = persistirProduto();

        primeiroPedido = persistirPedido(cliente, LocalDate.of(2026, 11, 12), StatusPedido.AGUARDANDO_ENTREGA);
        segundoPedido = persistirPedido(cliente, LocalDate.of(2026, 11, 10), StatusPedido.AGUARDANDO_ENTREGA);
        pedidoCancelado = persistirPedido(cliente, LocalDate.of(2026, 11, 20), StatusPedido.CANCELADO);
        pedidoForaDoPeriodo = persistirPedido(outroCliente, LocalDate.of(2026, 12, 1), StatusPedido.EM_PRODUCAO);

        persistirItem(primeiroPedido, produto);
        persistirItem(segundoPedido, produto);
        persistirPagamento(primeiroPedido);
        persistirProducao(segundoPedido);
        persistirCancelamento(pedidoCancelado, LocalDateTime.of(2026, 11, 30, 23, 59));
        persistirCancelamento(pedidoForaDoPeriodo, LocalDateTime.of(2026, 12, 1, 0, 0));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void filtraEOrdenaAsCincoConsultasSemNMaisUm() {
        statistics.clear();
        List<Pedido> pedidos = pedidoRepository.buscarPorPeriodo(DE, ATE, EnumSet.allOf(StatusPedido.class));
        pedidos.forEach(pedido -> pedido.getCliente().getNome());
        assertEquals(List.of(segundoPedido.getIdPedido(), primeiroPedido.getIdPedido(), pedidoCancelado.getIdPedido()),
                pedidos.stream().map(Pedido::getIdPedido).toList());
        assertEquals(1, statistics.getPrepareStatementCount());

        entityManager.clear();
        statistics.clear();
        List<ItemPedido> itens = itemPedidoRepository.buscarPorPedidos(List.of(primeiroPedido.getIdPedido(), segundoPedido.getIdPedido()));
        itens.forEach(item -> { item.getPedido().getCliente().getNome(); item.getProduto().getCategoriaProduto().getNome(); });
        assertEquals(List.of(primeiroPedido.getIdPedido(), segundoPedido.getIdPedido()),
                itens.stream().map(item -> item.getPedido().getIdPedido()).toList());
        assertEquals(1, statistics.getPrepareStatementCount());

        entityManager.clear();
        statistics.clear();
        List<Pagamento> pagamentos = pagamentoRepository.buscarPorPedidos(List.of(primeiroPedido.getIdPedido()));
        pagamentos.forEach(pagamento -> pagamento.getPedido().getCliente().getNome());
        assertEquals(List.of(primeiroPedido.getIdPedido()), pagamentos.stream().map(pagamento -> pagamento.getPedido().getIdPedido()).toList());
        assertEquals(1, statistics.getPrepareStatementCount());

        entityManager.clear();
        statistics.clear();
        List<Producao> producoes = producaoRepository.buscarPorPeriodo(DE, ATE, EnumSet.allOf(StatusProducao.class));
        producoes.forEach(producao -> producao.getPedido().getCliente().getNome());
        assertEquals(List.of(segundoPedido.getIdPedido()), producoes.stream().map(producao -> producao.getPedido().getIdPedido()).toList());
        assertEquals(1, statistics.getPrepareStatementCount());

        entityManager.clear();
        statistics.clear();
        List<CancelamentoPedido> cancelamentos = cancelamentoRepository.buscarPorPeriodo(DE.atStartOfDay(), ATE.plusDays(1).atStartOfDay());
        cancelamentos.forEach(cancelamento -> cancelamento.getPedido().getCliente().getNome());
        assertEquals(List.of(pedidoCancelado.getIdPedido()), cancelamentos.stream().map(cancelamento -> cancelamento.getPedido().getIdPedido()).toList());
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    @Test
    void retornaVazioParaClienteInexistente() {
        assertTrue(pedidoRepository.buscarPorCliente(999_999, EnumSet.allOf(StatusPedido.class)).isEmpty());
    }

    private Cliente persistirCliente(String nome) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        entityManager.persist(cliente);
        return cliente;
    }

    private Produto persistirProduto() {
        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setNome("Bolos");
        entityManager.persist(categoria);
        Produto produto = new Produto();
        produto.setCategoriaProduto(categoria);
        produto.setNome("Bolo de teste");
        entityManager.persist(produto);
        return produto;
    }

    private Pedido persistirPedido(Cliente cliente, LocalDate dataEntrega, StatusPedido status) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        pedido.setStatusPedido(status);
        pedido.setDataEntrega(dataEntrega);
        entityManager.persist(pedido);
        return pedido;
    }

    private void persistirItem(Pedido pedido, Produto produto) {
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(BigDecimal.ONE);
        item.setValorUnitario(BigDecimal.TEN);
        item.setValorTotal(BigDecimal.TEN);
        entityManager.persist(item);
    }

    private void persistirPagamento(Pedido pedido) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        entityManager.persist(pagamento);
    }

    private void persistirProducao(Pedido pedido) {
        Producao producao = new Producao();
        producao.setPedido(pedido);
        producao.setStatusProducao(StatusProducao.EM_PRODUCAO);
        entityManager.persist(producao);
    }

    private void persistirCancelamento(Pedido pedido, LocalDateTime dataCancelamento) {
        CancelamentoPedido cancelamento = new CancelamentoPedido();
        cancelamento.setPedido(pedido);
        cancelamento.setTipoCancelamento(TipoCancelamento.CLIENTE_DESISTIU);
        cancelamento.setTipoRetorno(TipoRetorno.SEM_DEVOLUCAO);
        cancelamento.setDataCancelamento(dataCancelamento);
        entityManager.persist(cancelamento);
    }
}
