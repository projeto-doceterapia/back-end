package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.exception.TotalAbaixoDoSinalPagoException;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ClienteRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("Transações reais de itens e pagamentos")
class PagamentoTransacaoIntegrationTest {

    @Autowired
    private ItemPedidoService itemPedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaProdutoRepository categoriaProdutoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Test
    @DisplayName("Rollback da alteração de item preserva total quando ficaria abaixo do sinal pago")
    void rollbackDaAlteracaoPreservaTotalQuandoFicaAbaixoDoSinal() {
        Contexto contexto = criarContexto(new BigDecimal("185.90"));
        Pagamento pagamento = pagamentoRepository.findByPedidoIdPedido(contexto.pedidoId()).orElseThrow();
        pagamento.setDataPagamentoSinal(LocalDate.now());
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_PAGAMENTO);
        pagamentoRepository.save(pagamento);

        assertThrows(TotalAbaixoDoSinalPagoException.class,
                () -> itemPedidoService.atualizar(contexto.itemIds().getFirst(),
                        request(contexto.produtoId(), contexto.pedidoId(), BigDecimal.ONE, new BigDecimal("80.00"))));

        ItemPedido itemPersistido = itemPedidoRepository.findById(contexto.itemIds().getFirst()).orElseThrow();
        Pagamento pagamentoPersistido = pagamentoRepository.findByPedidoIdPedido(contexto.pedidoId()).orElseThrow();
        assertEquals(0, new BigDecimal("185.90").compareTo(itemPersistido.getValorTotal()));
        assertEquals(0, new BigDecimal("185.90").compareTo(pagamentoPersistido.getValorTotal()));
        assertEquals(0, new BigDecimal("92.95").compareTo(pagamentoPersistido.getValorSinal()));
        assertEquals(0, new BigDecimal("92.95").compareTo(pagamentoPersistido.getValorRestante()));
    }

    @Test
    @DisplayName("Alterações concorrentes recalculam o total persistido sem perder valores")
    void alteracoesConcorrentesRecalculamTotalSemPerderValores() throws Exception {
        Contexto contexto = criarContexto(new BigDecimal("10.00"), new BigDecimal("10.00"));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        futuros.add(pool.submit(() -> {
            largada.await();
            itemPedidoService.atualizar(contexto.itemIds().get(0),
                    request(contexto.produtoId(), contexto.pedidoId(), BigDecimal.ONE, new BigDecimal("20.00")));
            return null;
        }));
        futuros.add(pool.submit(() -> {
            largada.await();
            itemPedidoService.atualizar(contexto.itemIds().get(1),
                    request(contexto.produtoId(), contexto.pedidoId(), BigDecimal.ONE, new BigDecimal("30.00")));
            return null;
        }));

        largada.countDown();
        for (Future<?> futuro : futuros) {
            futuro.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        Pagamento pagamento = pagamentoRepository.findByPedidoIdPedido(contexto.pedidoId()).orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(pagamento.getValorTotal()));
        assertEquals(0, new BigDecimal("25.00").compareTo(pagamento.getValorSinal()));
        assertEquals(0, new BigDecimal("25.00").compareTo(pagamento.getValorRestante()));
    }

    private Contexto criarContexto(BigDecimal... valores) {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente de teste");
        cliente.setTelefone("+55 11 90000-1000");
        cliente = clienteRepository.save(cliente);

        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setNome("Categoria de teste");
        categoria = categoriaProdutoRepository.save(categoria);

        Produto produto = new Produto();
        produto.setCategoriaProduto(categoria);
        produto.setNome("Produto de teste");
        produto = produtoRepository.save(produto);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_SINAL);
        pedido.setDataEntrega(LocalDate.now().plusDays(10));
        pedido = pedidoRepository.save(pedido);

        List<Integer> itemIds = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(BigDecimal.ONE);
            item.setValorUnitario(valor);
            item.setValorTotal(valor);
            itemIds.add(itemPedidoRepository.save(item).getIdProdutoPedido());
            total = total.add(valor);
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValorTotal(total);
        pagamento.setValorSinal(total.divide(new BigDecimal("2.00")));
        pagamento.setValorRestante(total.subtract(pagamento.getValorSinal()));
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        pagamentoRepository.save(pagamento);
        return new Contexto(pedido.getIdPedido(), produto.getIdProduto(), itemIds);
    }

    private ItemPedidoRequestDTO request(Integer produtoId, Integer pedidoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        ItemPedidoRequestDTO request = new ItemPedidoRequestDTO();
        request.setFkProduto(produtoId);
        request.setFkPedido(pedidoId);
        request.setQuantidade(quantidade);
        request.setValorUnitario(valorUnitario);
        return request;
    }

    private record Contexto(Integer pedidoId, Integer produtoId, List<Integer> itemIds) {
    }
}
