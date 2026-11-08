package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.CanceladosDTO;
import br.com.doceterapia.api.dto.EmOrcamentoDTO;
import br.com.doceterapia.api.dto.FaturamentoDTO;
import br.com.doceterapia.api.dto.FaturamentoMesDTO;
import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import br.com.doceterapia.api.dto.InsumoAbaixoMinimoDTO;
import br.com.doceterapia.api.dto.PedidosPorDiaDTO;
import br.com.doceterapia.api.dto.PeriodoDTO;
import br.com.doceterapia.api.dto.ProdutoQuantidadeDTO;
import br.com.doceterapia.api.dto.ProdutoValorDTO;
import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.projecao.ContagemPorDia;
import br.com.doceterapia.api.repository.projecao.TotalPorMes;
import br.com.doceterapia.api.repository.projecao.TotalPorProduto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndicadorService {

    private static final int POSICOES_RANKING = 5;
    private static final int MESES_SERIE = 6;
    private static final int ESCALA_MONETARIA = 2;

    // Budgets are not sales yet and cancelled orders are reported apart.
    private static final List<StatusPedido> VENDAS = List.of(
            StatusPedido.AGUARDANDO_SINAL, StatusPedido.ESPERANDO_FAZER, StatusPedido.EM_PRODUCAO,
            StatusPedido.AGUARDANDO_ENTREGA, StatusPedido.ENTREGUE);
    private static final List<StatusPedido> CANCELADOS = List.of(StatusPedido.CANCELADO);
    private static final List<StatusPedido> ORCAMENTOS = List.of(StatusPedido.ORCAMENTO);

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CancelamentoPedidoRepository cancelamentoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final InsumoRepository insumoRepository;
    private final ConfiguracaoFarolAgendaRepository farolRepository;
    private final Clock clock;

    public IndicadorService(PedidoRepository pedidoRepository,
                            ItemPedidoRepository itemPedidoRepository,
                            PagamentoRepository pagamentoRepository,
                            CancelamentoPedidoRepository cancelamentoRepository,
                            MovimentacaoEstoqueRepository movimentacaoRepository,
                            InsumoRepository insumoRepository,
                            ConfiguracaoFarolAgendaRepository farolRepository,
                            Clock clock) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.cancelamentoRepository = cancelamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.insumoRepository = insumoRepository;
        this.farolRepository = farolRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public IndicadoresOperacionaisResponseDTO operacionais(PeriodoConsulta periodo) {
        LocalDate de = periodo.de();
        LocalDate ate = periodo.ate();
        LocalDate hoje = LocalDate.now(clock);
        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return new IndicadoresOperacionaisResponseDTO(
                new PeriodoDTO(de, ate),
                pedidosPorStatus(de, ate),
                pedidoRepository.contarAtivosEntre(hoje, hoje),
                pedidoRepository.contarAtivosEntre(segunda, segunda.plusDays(6)),
                pedidoRepository.contarAtivosPorDia(de, ate).stream().map(this::paraDia).toList(),
                diasLotados(de, ate),
                itemPedidoRepository.somarQuantidadePorProduto(de, ate, VENDAS, ranking()).stream()
                        .map(this::paraProdutoQuantidade).toList(),
                insumoRepository.buscarNoMinimoOuAbaixo().stream()
                        .map(insumo -> new InsumoAbaixoMinimoDTO(insumo.getIdInsumo(), insumo.getNome(),
                                insumo.getQuantidadeAtual(), insumo.getQuantidadeMinima(), insumo.getUnidade()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public IndicadoresFinanceirosResponseDTO financeiros(PeriodoConsulta periodo) {
        LocalDate de = periodo.de();
        LocalDate ate = periodo.ate();

        BigDecimal total = dinheiro(itemPedidoRepository.somarValor(de, ate, VENDAS));
        BigDecimal recebido = dinheiro(pagamentoRepository.somarPago(de, ate, VENDAS));
        BigDecimal gastos = dinheiro(movimentacaoRepository.somarGastosCompra(periodo.inicio(), periodo.fimExclusivo()));

        return new IndicadoresFinanceirosResponseDTO(
                new PeriodoDTO(de, ate),
                new FaturamentoDTO(total, recebido, total.subtract(recebido)),
                ticketMedio(total, pedidoRepository.contarPorStatusIn(de, ate, VENDAS)),
                gastos,
                total.subtract(gastos),
                itemPedidoRepository.somarValorPorProduto(de, ate, VENDAS, ranking()).stream()
                        .map(this::paraProdutoValor).toList(),
                cancelados(de, ate),
                new EmOrcamentoDTO(pedidoRepository.contarPorStatusIn(de, ate, ORCAMENTOS),
                        dinheiro(itemPedidoRepository.somarValor(de, ate, ORCAMENTOS))),
                faturamentoPorMes(ate));
    }

    private Map<StatusPedido, Long> pedidosPorStatus(LocalDate de, LocalDate ate) {
        Map<StatusPedido, Long> contagem = new EnumMap<>(StatusPedido.class);
        for (StatusPedido status : StatusPedido.values()) {
            contagem.put(status, 0L);
        }
        pedidoRepository.contarPorStatus(de, ate)
                .forEach(linha -> contagem.put(linha.status(), linha.quantidade()));
        return contagem;
    }

    private List<PedidosPorDiaDTO> diasLotados(LocalDate de, LocalDate ate) {
        Integer limiteVermelho = farolRepository.findAll().stream()
                .findFirst()
                .map(ConfiguracaoFarolAgenda::getLimiteVermelho)
                .orElse(null);
        if (limiteVermelho == null) {
            return List.of();
        }
        return pedidoRepository.buscarDiasLotados(de, ate, limiteVermelho).stream().map(this::paraDia).toList();
    }

    private CanceladosDTO cancelados(LocalDate de, LocalDate ate) {
        BigDecimal pago = dinheiro(pagamentoRepository.somarPago(de, ate, CANCELADOS));
        BigDecimal devolvido = dinheiro(cancelamentoRepository.somarDevolvido(de, ate));
        return new CanceladosDTO(pedidoRepository.contarPorStatusIn(de, ate, CANCELADOS),
                pago, devolvido, pago.subtract(devolvido));
    }

    // Months without sales are filled with zero so the chart always has the same number of points.
    private List<FaturamentoMesDTO> faturamentoPorMes(LocalDate ate) {
        YearMonth ultimo = YearMonth.from(ate);
        YearMonth primeiro = ultimo.minusMonths(MESES_SERIE - 1L);
        Map<YearMonth, BigDecimal> totais = new HashMap<>();
        for (TotalPorMes linha : itemPedidoRepository.somarValorPorMes(primeiro.atDay(1), ultimo.atEndOfMonth(), VENDAS)) {
            totais.put(YearMonth.of(linha.ano(), linha.mes()), linha.total());
        }
        List<FaturamentoMesDTO> serie = new ArrayList<>();
        for (YearMonth mes = primeiro; !mes.isAfter(ultimo); mes = mes.plusMonths(1)) {
            serie.add(new FaturamentoMesDTO(mes.toString(), dinheiro(totais.get(mes))));
        }
        return serie;
    }

    private BigDecimal ticketMedio(BigDecimal total, long pedidos) {
        if (pedidos == 0) {
            return dinheiro(null);
        }
        return total.divide(BigDecimal.valueOf(pedidos), ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private Pageable ranking() {
        return PageRequest.of(0, POSICOES_RANKING);
    }

    private BigDecimal dinheiro(BigDecimal valor) {
        BigDecimal base = valor == null ? BigDecimal.ZERO : valor;
        return base.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private PedidosPorDiaDTO paraDia(ContagemPorDia linha) {
        return new PedidosPorDiaDTO(linha.data(), linha.quantidade());
    }

    private ProdutoQuantidadeDTO paraProdutoQuantidade(TotalPorProduto linha) {
        return new ProdutoQuantidadeDTO(linha.produtoId(), linha.nome(), linha.total());
    }

    private ProdutoValorDTO paraProdutoValor(TotalPorProduto linha) {
        return new ProdutoValorDTO(linha.produtoId(), linha.nome(), dinheiro(linha.total()));
    }
}
