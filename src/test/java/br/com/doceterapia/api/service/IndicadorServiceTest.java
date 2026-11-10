package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.projecao.ContagemPorDia;
import br.com.doceterapia.api.repository.projecao.ContagemPorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IndicadorService")
class IndicadorServiceTest {

    private static final PeriodoConsulta NOVEMBRO =
            PeriodoConsulta.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private CancelamentoPedidoRepository cancelamentoRepository;
    @Mock private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock private InsumoRepository insumoRepository;
    @Mock private ConfiguracaoFarolAgendaRepository farolRepository;

    private IndicadorService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new IndicadorService(pedidoRepository, itemPedidoRepository, pagamentoRepository,
                cancelamentoRepository, movimentacaoRepository, insumoRepository, farolRepository, clock);
    }

    private ConfiguracaoFarolAgenda farol(Integer vermelho) {
        ConfiguracaoFarolAgenda farol = new ConfiguracaoFarolAgenda();
        farol.setLimiteVermelho(vermelho);
        return farol;
    }

    @Test
    @DisplayName("Ticket médio divide o faturamento pelos pedidos que entram nele")
    void ticketMedio() {
        when(itemPedidoRepository.somarValor(any(), any(), anyCollection())).thenReturn(new BigDecimal("395.00"));
        when(pedidoRepository.contarPorStatusIn(any(), any(), anyCollection())).thenReturn(4L);

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("98.75"), resposta.ticketMedio());
    }

    @Test
    @DisplayName("Ticket médio é 0,00 sem pedidos")
    void ticketMedioSemPedidos() {
        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("0.00"), resposta.ticketMedio());
        assertEquals(new BigDecimal("0.00"), resposta.faturamento().total());
    }

    @Test
    @DisplayName("Ticket médio arredonda para cima na metade")
    void ticketMedioArredonda() {
        when(itemPedidoRepository.somarValor(any(), any(), anyCollection())).thenReturn(new BigDecimal("100.00"));
        when(pedidoRepository.contarPorStatusIn(any(), any(), anyCollection())).thenReturn(3L);

        assertEquals(new BigDecimal("33.33"), service.financeiros(NOVEMBRO).ticketMedio());
    }

    @Test
    @DisplayName("Retido é o pago menos o devolvido")
    void retidoEhPagoMenosDevolvido() {
        when(pagamentoRepository.somarPago(any(), any(), eq(List.of(StatusPedido.CANCELADO))))
                .thenReturn(new BigDecimal("40.00"));
        when(cancelamentoRepository.somarDevolvido(any(), any())).thenReturn(new BigDecimal("15.00"));
        when(pedidoRepository.contarPorStatusIn(any(), any(), eq(List.of(StatusPedido.CANCELADO)))).thenReturn(1L);

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("25.00"), resposta.cancelados().retido());
        assertEquals(1, resposta.cancelados().quantidade());
    }

    @Test
    @DisplayName("Lucro estimado é o faturamento menos as compras e pode ser negativo")
    void lucroEstimadoPodeSerNegativo() {
        when(itemPedidoRepository.somarValor(any(), any(), anyCollection())).thenReturn(new BigDecimal("100.00"));
        when(movimentacaoRepository.somarGastosCompra(any(), any())).thenReturn(new BigDecimal("130.5000"));

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("130.50"), resposta.gastosInsumos());
        assertEquals(new BigDecimal("-30.50"), resposta.lucroEstimado());
    }

    @Test
    @DisplayName("Margem é o lucro estimado sobre o faturamento, e 0,00 sem faturamento")
    void margem() {
        assertEquals(new BigDecimal("0.00"), service.financeiros(NOVEMBRO).margem());

        when(itemPedidoRepository.somarValor(any(), any(), anyCollection())).thenReturn(new BigDecimal("200.00"));
        when(movimentacaoRepository.somarGastosCompra(any(), any())).thenReturn(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("75.00"), service.financeiros(NOVEMBRO).margem());
    }

    @Test
    @DisplayName("A receber é o total menos o recebido")
    void aReceber() {
        when(itemPedidoRepository.somarValor(any(), any(), eq(List.of(
                StatusPedido.AGUARDANDO_SINAL, StatusPedido.ESPERANDO_FAZER, StatusPedido.EM_PRODUCAO,
                StatusPedido.AGUARDANDO_ENTREGA, StatusPedido.ENTREGUE)))).thenReturn(new BigDecimal("395.00"));
        when(pagamentoRepository.somarPago(any(), any(), anyCollection())).thenReturn(new BigDecimal("290.00"));

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(new BigDecimal("105.00"), resposta.faturamento().aReceber());
    }

    @Test
    @DisplayName("Orçamento fora do faturamento: soma e quantidade vêm à parte")
    void somaEmOrcamento() {
        List<StatusPedido> orcamento = List.of(StatusPedido.ORCAMENTO);
        when(pedidoRepository.contarPorStatusIn(any(), any(), eq(orcamento))).thenReturn(3L);
        when(itemPedidoRepository.somarValor(any(), any(), eq(orcamento))).thenReturn(new BigDecimal("450.00"));

        IndicadoresFinanceirosResponseDTO resposta = service.financeiros(NOVEMBRO);

        assertEquals(3, resposta.emOrcamento().quantidade());
        assertEquals(new BigDecimal("450.00"), resposta.emOrcamento().valor());
        assertEquals(new BigDecimal("0.00"), resposta.faturamento().total());
    }

    @Test
    @DisplayName("Sem farol configurado os dias lotados vêm vazios")
    void semFarolDiasLotadosVazio() {
        when(farolRepository.findAll()).thenReturn(List.of());

        assertTrue(service.operacionais(NOVEMBRO).diasLotados().isEmpty());
        verify(pedidoRepository, never()).buscarDiasLotados(any(), any(), anyLong());
    }

    @Test
    @DisplayName("Dias lotados usam o limite vermelho do farol")
    void diasLotadosUsamLimiteVermelho() {
        when(farolRepository.findAll()).thenReturn(List.of(farol(3)));
        when(pedidoRepository.buscarDiasLotados(NOVEMBRO.de(), NOVEMBRO.ate(), 3L))
                .thenReturn(List.of(new ContagemPorDia(LocalDate.of(2026, 11, 20), 3)));

        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(1, resposta.diasLotados().size());
        assertEquals(LocalDate.of(2026, 11, 20), resposta.diasLotados().get(0).data());
    }

    @Test
    @DisplayName("Pedidos por status traz todas as chaves, com zero quando ausentes")
    void pedidosPorStatusCompleto() {
        when(pedidoRepository.contarPorStatus(any(), any()))
                .thenReturn(List.of(new ContagemPorStatus(StatusPedido.ENTREGUE, 2)));

        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(StatusPedido.values().length, resposta.pedidosPorStatus().size());
        assertEquals(2L, resposta.pedidosPorStatus().get(StatusPedido.ENTREGUE));
        assertEquals(0L, resposta.pedidosPorStatus().get(StatusPedido.ORCAMENTO));
    }

    @Test
    @DisplayName("Hoje e semana usam o relógio: quarta 18/11 vai de segunda 16/11 a domingo 22/11")
    void hojeESemanaPeloRelogio() {
        when(pedidoRepository.contarAtivosEntre(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 18)))
                .thenReturn(2L);
        when(pedidoRepository.contarAtivosEntre(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22)))
                .thenReturn(9L);

        IndicadoresOperacionaisResponseDTO resposta = service.operacionais(NOVEMBRO);

        assertEquals(2, resposta.pedidosHoje());
        assertEquals(9, resposta.pedidosSemana());
    }

    @Test
    @DisplayName("Insumos abaixo do mínimo trazem saldo, mínimo e unidade")
    void insumosAbaixoDoMinimo() {
        Insumo chocolate = new Insumo();
        chocolate.setIdInsumo(7);
        chocolate.setNome("Chocolate meio amargo");
        chocolate.setQuantidadeAtual(new BigDecimal("0.800"));
        chocolate.setQuantidadeMinima(new BigDecimal("2.000"));
        chocolate.setUnidade(UnidadeMedida.KG);
        chocolate.setStatus(StatusAtivo.ATIVO);
        when(insumoRepository.buscarNoMinimoOuAbaixo()).thenReturn(List.of(chocolate));

        var lista = service.operacionais(NOVEMBRO).insumosAbaixoDoMinimo();

        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).insumoId());
        assertEquals(new BigDecimal("0.800"), lista.get(0).saldo());
        assertEquals(UnidadeMedida.KG, lista.get(0).unidade());
    }

    @Test
    @DisplayName("Nenhuma consulta carrega listas de entidades do período")
    void naoCarregaListasDeEntidades() {
        service.operacionais(NOVEMBRO);
        service.financeiros(NOVEMBRO);

        verify(pedidoRepository, never()).findAll();
        verify(pedidoRepository, never()).buscarEntreDatas(any(), any());
        verify(itemPedidoRepository, never()).findAll();
        verify(pagamentoRepository, never()).findAll();
        verify(cancelamentoRepository, never()).findAll();
        verify(movimentacaoRepository, never()).findAll();
    }
}
