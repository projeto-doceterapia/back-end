package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import br.com.doceterapia.api.entity.CancelamentoPedido;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoCancelamento;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.enums.TipoRetorno;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ClienteRepository;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the indicator service against real repositories and a fixed sample:
 * November 2026, clock on 2026-11-18 (a Wednesday), red limit 3.
 */
@DataJpaTest
@DisplayName("Indicadores sobre a massa fictícia de novembro de 2026")
class IndicadorConsultasDataJpaTest {

    private static final PeriodoConsulta NOVEMBRO =
            PeriodoConsulta.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ItemPedidoRepository itemPedidoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private CancelamentoPedidoRepository cancelamentoRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private ConfiguracaoFarolAgendaRepository farolRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private CategoriaProdutoRepository categoriaProdutoRepository;
    @Autowired private CategoriaInsumoRepository categoriaInsumoRepository;
    @Autowired private ProdutoRepository produtoRepository;

    private IndicadorService service;
    private Cliente cliente;
    private Produto boloDePote;
    private Produto brigadeiro;
    private Produto torta;
    private CategoriaInsumo categoriaInsumo;
    private int sequencia;

    @BeforeEach
    void montarMassa() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new IndicadorService(pedidoRepository, itemPedidoRepository, pagamentoRepository,
                cancelamentoRepository, movimentacaoRepository, insumoRepository, farolRepository, clock);

        cliente = new Cliente();
        cliente.setNome("Cliente Fictício");
        cliente.setTelefone("+5511900000001");
        cliente = clienteRepository.save(cliente);

        CategoriaProduto categoriaProduto = new CategoriaProduto();
        categoriaProduto.setNome("Doces");
        categoriaProduto = categoriaProdutoRepository.save(categoriaProduto);
        boloDePote = produto(categoriaProduto, "Bolo de pote");
        brigadeiro = produto(categoriaProduto, "Brigadeiro gourmet");
        torta = produto(categoriaProduto, "Torta de limão");

        ConfiguracaoFarolAgenda farol = new ConfiguracaoFarolAgenda();
        farol.setLimiteVerde(1);
        farol.setLimiteAmarelo(2);
        farol.setLimiteVermelho(3);
        farolRepository.save(farol);

        categoriaInsumo = new CategoriaInsumo();
        categoriaInsumo.setNome("Cobertura");
        categoriaInsumo = categoriaInsumoRepository.save(categoriaInsumo);

        Pedido p1 = pedido(StatusPedido.ENTREGUE, LocalDate.of(2026, 11, 5));
        item(p1, boloDePote, "10", "120.00");
        pagamento(p1, "60.00", true, "60.00", true);
        Pedido p2 = pedido(StatusPedido.ENTREGUE, LocalDate.of(2026, 11, 20));
        item(p2, brigadeiro, "50", "125.00");
        pagamento(p2, "62.50", true, "62.50", true);
        Pedido p3 = pedido(StatusPedido.EM_PRODUCAO, LocalDate.of(2026, 11, 20));
        item(p3, torta, "1", "90.00");
        pagamento(p3, "45.00", true, "45.00", false);
        Pedido p4 = pedido(StatusPedido.AGUARDANDO_SINAL, LocalDate.of(2026, 11, 20));
        item(p4, boloDePote, "5", "60.00");
        pagamento(p4, "30.00", false, "30.00", false);
        Pedido p5 = pedido(StatusPedido.CANCELADO, LocalDate.of(2026, 11, 20));
        item(p5, torta, "1", "80.00");
        pagamento(p5, "40.00", true, "40.00", false);
        cancelar(p5, TipoRetorno.DEVOLUCAO, "20.00");
        Pedido p6 = pedido(StatusPedido.ENTREGUE, LocalDate.of(2026, 10, 31));
        item(p6, boloDePote, "3", "36.00");
        pagamento(p6, "18.00", true, "18.00", true);

        Insumo chocolate = insumo("Chocolate meio amargo", StatusAtivo.ATIVO, "0.800", "2.000", UnidadeMedida.KG);
        Insumo leite = insumo("Leite condensado", StatusAtivo.INATIVO, "0.100", "1.000", UnidadeMedida.UNIDADE);
        movimentacao(chocolate, TipoMovimentacao.ENTRADA_COMPRA, "2.000", "45.9000", LocalDateTime.of(2026, 11, 3, 9, 12));
        movimentacao(leite, TipoMovimentacao.ENTRADA_COMPRA, "5.000", "3.5000", LocalDateTime.of(2026, 11, 30, 23, 59));
        movimentacao(chocolate, TipoMovimentacao.AJUSTE, "1.000", "10.0000", LocalDateTime.of(2026, 11, 12, 10, 0));
        movimentacao(chocolate, TipoMovimentacao.ENTRADA_COMPRA, "1.000", "10.0000", LocalDateTime.of(2026, 10, 31, 23, 59));
        movimentacao(chocolate, TipoMovimentacao.ENTRADA_COMPRA, "1.000", "10.0000", LocalDateTime.of(2026, 12, 1, 0, 0));
    }

    @Test
    @DisplayName("Operacionais do período: contagens, dias lotados, ranking e insumos")
    void operacionaisDoPeriodo() {
        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(7, resposta.pedidosPorStatus().size());
        assertEquals(2L, resposta.pedidosPorStatus().get(StatusPedido.ENTREGUE));
        assertEquals(1L, resposta.pedidosPorStatus().get(StatusPedido.EM_PRODUCAO));
        assertEquals(1L, resposta.pedidosPorStatus().get(StatusPedido.AGUARDANDO_SINAL));
        assertEquals(1L, resposta.pedidosPorStatus().get(StatusPedido.CANCELADO));
        assertEquals(0L, resposta.pedidosPorStatus().get(StatusPedido.ORCAMENTO));
        assertEquals(0, resposta.pedidosHoje());
        assertEquals(3, resposta.pedidosSemana());
        assertEquals(2, resposta.pedidosPorDia().size());
        assertEquals(3, resposta.pedidosPorDia().get(1).quantidade());
    }

    @Test
    @DisplayName("Dias lotados ignoram pedidos cancelados")
    void diasLotadosIgnoramCancelados() {
        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(1, resposta.diasLotados().size());
        assertEquals(LocalDate.of(2026, 11, 20), resposta.diasLotados().get(0).data());
        assertEquals(3, resposta.diasLotados().get(0).quantidade());
    }

    @Test
    @DisplayName("Sem farol configurado não há dias lotados")
    void semFarolNaoHaDiasLotados() {
        farolRepository.deleteAll();

        assertTrue(service.operacionais(NOVEMBRO).diasLotados().isEmpty());
    }

    @Test
    @DisplayName("Top 5 por quantidade sem cancelados nem pedidos de fora do período")
    void topCincoPorQuantidade() {
        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(3, resposta.produtosMaisVendidos().size());
        assertEquals("Brigadeiro gourmet", resposta.produtosMaisVendidos().get(0).nome());
        assertEquals(0, new BigDecimal("50").compareTo(resposta.produtosMaisVendidos().get(0).quantidade()));
        assertEquals("Bolo de pote", resposta.produtosMaisVendidos().get(1).nome());
        assertEquals(0, new BigDecimal("15").compareTo(resposta.produtosMaisVendidos().get(1).quantidade()));
        assertEquals("Torta de limão", resposta.produtosMaisVendidos().get(2).nome());
        assertEquals(0, BigDecimal.ONE.compareTo(resposta.produtosMaisVendidos().get(2).quantidade()));
    }

    @Test
    @DisplayName("Só insumos ativos abaixo do mínimo aparecem")
    void insumosAbaixoDoMinimoSoAtivos() {
        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(1, resposta.insumosAbaixoDoMinimo().size());
        assertEquals("Chocolate meio amargo", resposta.insumosAbaixoDoMinimo().get(0).nome());
    }

    @Test
    @DisplayName("Faturamento exclui cancelados e separa recebido de a receber")
    void faturamentoSemCancelados() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("395.00"), resposta.faturamento().total());
        assertEquals(new BigDecimal("290.00"), resposta.faturamento().recebido());
        assertEquals(new BigDecimal("105.00"), resposta.faturamento().aReceber());
        assertEquals(new BigDecimal("98.75"), resposta.ticketMedio());
    }

    @Test
    @DisplayName("Gastos somam só compras dentro do período, com fim exclusivo")
    void gastosSoComprasDoPeriodo() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("109.30"), resposta.gastosInsumos());
        assertEquals(new BigDecimal("285.70"), resposta.lucroEstimado());
        assertEquals(new BigDecimal("72.33"), resposta.margem());
    }

    @Test
    @DisplayName("Top 5 por faturamento")
    void topCincoPorFaturamento() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals("Bolo de pote", resposta.produtosMaisVendidosPorFaturamento().get(0).nome());
        assertEquals(new BigDecimal("180.00"), resposta.produtosMaisVendidosPorFaturamento().get(0).valor());
        assertEquals(new BigDecimal("125.00"), resposta.produtosMaisVendidosPorFaturamento().get(1).valor());
        assertEquals(new BigDecimal("90.00"), resposta.produtosMaisVendidosPorFaturamento().get(2).valor());
    }

    @Test
    @DisplayName("Cancelados à parte: pago, devolvido e retido")
    void canceladosAParte() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(1, resposta.cancelados().quantidade());
        assertEquals(new BigDecimal("40.00"), resposta.cancelados().pago());
        assertEquals(new BigDecimal("20.00"), resposta.cancelados().devolvido());
        assertEquals(new BigDecimal("20.00"), resposta.cancelados().retido());
    }

    @Test
    @DisplayName("Orçamentos ficam fora do faturamento e aparecem em emOrcamento")
    void orcamentoForaDoFaturamento() {
        Pedido orcamento = pedido(StatusPedido.ORCAMENTO, LocalDate.of(2026, 11, 25));
        item(orcamento, boloDePote, "10", "150.00");

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("395.00"), resposta.faturamento().total());
        assertEquals(new BigDecimal("98.75"), resposta.ticketMedio());
        assertEquals(1, resposta.emOrcamento().quantidade());
        assertEquals(new BigDecimal("150.00"), resposta.emOrcamento().valor());
    }

    @Test
    @DisplayName("Faturamento por mês cobre seis meses terminando no mês de 'ate', com zeros")
    void faturamentoPorMesComSeisPontos() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(6, resposta.faturamentoPorMes().size());
        assertEquals("2026-06", resposta.faturamentoPorMes().get(0).mes());
        assertEquals(new BigDecimal("0.00"), resposta.faturamentoPorMes().get(0).total());
        assertEquals("2026-10", resposta.faturamentoPorMes().get(4).mes());
        assertEquals(new BigDecimal("36.00"), resposta.faturamentoPorMes().get(4).total());
        assertEquals("2026-11", resposta.faturamentoPorMes().get(5).mes());
        assertEquals(new BigDecimal("395.00"), resposta.faturamentoPorMes().get(5).total());
        assertEquals(new BigDecimal("109.30"), resposta.faturamentoPorMes().get(5).gastos());
        assertEquals(new BigDecimal("285.70"), resposta.faturamentoPorMes().get(5).lucro());
        assertEquals(new BigDecimal("10.00"), resposta.faturamentoPorMes().get(4).gastos());
        assertEquals(new BigDecimal("26.00"), resposta.faturamentoPorMes().get(4).lucro());
    }

    @Test
    @DisplayName("Período sem dados devolve zeros e listas vazias")
    void periodoSemDados() {
        PeriodoConsulta vazio = PeriodoConsulta.de(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        IndicadoresFinanceirosResponseDTO financeiros = service.financeiros(vazio);

        assertEquals(new BigDecimal("0.00"), financeiros.faturamento().total());
        assertEquals(new BigDecimal("0.00"), financeiros.ticketMedio());
        assertEquals(new BigDecimal("0.00"), financeiros.lucroEstimado());
        assertTrue(financeiros.produtosMaisVendidosPorFaturamento().isEmpty());
        assertTrue(service.operacionais(vazio).produtosMaisVendidos().isEmpty());
    }

    private Produto produto(CategoriaProduto categoria, String nome) {
        Produto produto = new Produto();
        produto.setCategoriaProduto(categoria);
        produto.setNome(nome);
        produto.setStatus(StatusAtivo.ATIVO);
        return produtoRepository.save(produto);
    }

    private Pedido pedido(StatusPedido status, LocalDate entrega) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        pedido.setStatusPedido(status);
        pedido.setDataEntrega(entrega);
        return pedidoRepository.save(pedido);
    }

    private void item(Pedido pedido, Produto produto, String quantidade, String valorTotal) {
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(new BigDecimal(quantidade));
        item.setValorTotal(new BigDecimal(valorTotal));
        itemPedidoRepository.save(item);
    }

    private void pagamento(Pedido pedido, String sinal, boolean sinalPago, String restante, boolean restantePago) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setValorSinal(new BigDecimal(sinal));
        pagamento.setValorRestante(new BigDecimal(restante));
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        pagamento.setDataPagamentoSinal(sinalPago ? LocalDate.of(2026, 11, 1) : null);
        pagamento.setDataPagamentoRestante(restantePago ? LocalDate.of(2026, 11, 2) : null);
        pagamentoRepository.save(pagamento);
    }

    private void cancelar(Pedido pedido, TipoRetorno retorno, String valor) {
        CancelamentoPedido cancelamento = new CancelamentoPedido();
        cancelamento.setPedido(pedido);
        cancelamento.setTipoCancelamento(TipoCancelamento.CLIENTE_DESISTIU);
        cancelamento.setTipoRetorno(retorno);
        cancelamento.setValorRetorno(new BigDecimal(valor));
        cancelamento.setDataCancelamento(LocalDateTime.of(2026, 11, 10, 10, 0));
        cancelamentoRepository.save(cancelamento);
    }

    private Insumo insumo(String nome, StatusAtivo status, String saldo, String minimo, UnidadeMedida unidade) {
        Insumo insumo = new Insumo();
        insumo.setCategoria(categoriaInsumo);
        insumo.setNome(nome);
        insumo.setQuantidadeAtual(new BigDecimal(saldo));
        insumo.setQuantidadeMinima(new BigDecimal(minimo));
        insumo.setUnidade(unidade);
        insumo.setStatus(status);
        return insumoRepository.save(insumo);
    }

    private void movimentacao(Insumo insumo, TipoMovimentacao tipo, String quantidade, String custo,
                              LocalDateTime ocorridoEm) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setInsumo(insumo);
        m.setTipo(tipo);
        m.setQuantidade(new BigDecimal(quantidade));
        m.setQuantidadeInformada(new BigDecimal(quantidade));
        m.setUnidadeInformada(insumo.getUnidade());
        m.setCustoUnitario(new BigDecimal(custo));
        m.setOrigem(OrigemMovimentacao.MANUAL);
        m.setChaveIdempotencia("indicador:" + sequencia++);
        m.setHashConteudo("hash");
        m.setOcorridoEm(ocorridoEm);
        m.setCriadoEm(ocorridoEm);
        movimentacaoRepository.save(m);
    }
}
