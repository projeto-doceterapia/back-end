package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.ClienteRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ResumoDiarioQueries.class)
@DisplayName("Testes das consultas dos resumos diários (@DataJpaTest)")
class ResumoDiarioQueriesTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 21);

    @Autowired
    private ResumoDiarioQueries queries;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNome("Maria");
        cliente = clienteRepository.save(cliente);
    }

    private Pedido pedido(StatusPedido status, LocalDate dataEntrega) {
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        p.setStatusPedido(status);
        p.setDataEntrega(dataEntrega);
        return pedidoRepository.save(p);
    }

    private void pagamento(Pedido pedido, StatusPagamento status) {
        Pagamento pg = new Pagamento();
        pg.setPedido(pedido);
        pg.setStatusPagamento(status);
        pagamentoRepository.save(pg);
    }

    private void insumo(String nome, BigDecimal atual, BigDecimal minimo, StatusAtivo status) {
        Insumo i = new Insumo();
        i.setNome(nome);
        i.setUnidade(UnidadeMedida.KG);
        i.setStatus(status);
        i.setQuantidadeAtual(atual);
        i.setQuantidadeMinima(minimo);
        insumoRepository.save(i);
    }

    @Test
    @DisplayName("Sinais a conferir lista apenas pedidos AGUARDANDO_SINAL")
    void sinaisParaConferir() {
        pedido(StatusPedido.AGUARDANDO_SINAL, HOJE.plusDays(2));
        pedido(StatusPedido.EM_PRODUCAO, HOJE.plusDays(2));

        List<ItemResumoPedido> itens = queries.sinaisParaConferir();

        assertEquals(1, itens.size());
        assertEquals("Maria", itens.get(0).cliente());
    }

    @Test
    @DisplayName("Pagamentos finais lista entregues cujo pagamento não é PAGO_INTEGRAL")
    void pagamentosFinaisParaConferir() {
        Pedido entregueAberto = pedido(StatusPedido.ENTREGUE, HOJE.minusDays(1));
        pagamento(entregueAberto, StatusPagamento.AGUARDANDO_PAGAMENTO);

        Pedido entreguePago = pedido(StatusPedido.ENTREGUE, HOJE.minusDays(1));
        pagamento(entreguePago, StatusPagamento.PAGO_INTEGRAL);

        List<ItemResumoPedido> itens = queries.pagamentosFinaisParaConferir();

        assertEquals(1, itens.size());
        assertEquals(entregueAberto.getIdPedido(), itens.get(0).pedidoId());
    }

    @Test
    @DisplayName("Pedido antes da entrega não aparece na seção de pagamentos finais")
    void semAlertaDePagamentoAntesDaEntrega() {
        Pedido emProducao = pedido(StatusPedido.EM_PRODUCAO, HOJE.plusDays(1));
        pagamento(emProducao, StatusPagamento.AGUARDANDO_PAGAMENTO);

        assertTrue(queries.pagamentosFinaisParaConferir().isEmpty());
    }

    @Test
    @DisplayName("Entregas de hoje lista pedidos ativos com entrega na data")
    void entregasHoje() {
        pedido(StatusPedido.EM_PRODUCAO, HOJE);
        pedido(StatusPedido.ENTREGUE, HOJE);
        pedido(StatusPedido.CANCELADO, HOJE);

        List<ItemResumoPedido> itens = queries.entregasHoje(HOJE);

        assertEquals(1, itens.size());
    }

    @Test
    @DisplayName("Atrasados lista pedidos ativos com entrega anterior a hoje")
    void pedidosAtrasados() {
        pedido(StatusPedido.EM_PRODUCAO, HOJE.minusDays(1));
        pedido(StatusPedido.ENTREGUE, HOJE.minusDays(1));

        List<ItemResumoPedido> itens = queries.pedidosAtrasados(HOJE);

        assertEquals(1, itens.size());
    }

    @Test
    @DisplayName("Próximos dias agrupa por data e separa AGUARDANDO_SINAL dos prontos")
    void proximosDiasSeparaSinal() {
        LocalDate amanha = HOJE.plusDays(1);
        pedido(StatusPedido.AGUARDANDO_SINAL, amanha);
        pedido(StatusPedido.EM_PRODUCAO, amanha);
        pedido(StatusPedido.ESPERANDO_FAZER, HOJE.plusDays(3));
        pedido(StatusPedido.CANCELADO, amanha);

        List<GrupoDataResumo> grupos = queries.proximosDias(amanha, HOJE.plusDays(3));

        assertEquals(2, grupos.size());
        GrupoDataResumo primeiro = grupos.get(0);
        assertEquals(amanha, primeiro.data());
        assertEquals(1, primeiro.aguardandoSinal().size());
        assertEquals(1, primeiro.prontosParaProduzir().size());
    }

    @Test
    @DisplayName("Insumos para comprar lista ativos no mínimo ou abaixo")
    void insumosParaComprar() {
        insumo("Farinha", BigDecimal.valueOf(1), BigDecimal.valueOf(2), StatusAtivo.ATIVO);
        insumo("Açúcar", BigDecimal.valueOf(2), BigDecimal.valueOf(2), StatusAtivo.ATIVO);
        insumo("Fermento", BigDecimal.valueOf(5), BigDecimal.valueOf(2), StatusAtivo.ATIVO);
        insumo("Inativo", BigDecimal.valueOf(0), BigDecimal.valueOf(2), StatusAtivo.INATIVO);

        List<ItemResumoInsumo> itens = queries.insumosParaComprar();

        assertEquals(2, itens.size());
    }
}
