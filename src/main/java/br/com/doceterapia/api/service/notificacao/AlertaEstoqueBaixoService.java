package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Decides whether a balance change must raise an ESTOQUE_BAIXO alert. Called by
 * {@code MovimentacaoEstoqueService} inside the same transaction that mutates
 * the balance, so the alert commits together with the movement. The alert fires
 * only on a downward crossing of the minimum (above to at-or-below), never while
 * the balance was already at or below it; the daily dedup key then keeps a single
 * alert per supply per day.
 */
@Service
public class AlertaEstoqueBaixoService {

    private final CriacaoNotificacaoService criacaoService;
    private final ConfiguracaoNotificacaoService configuracaoService;
    private final Clock clock;

    public AlertaEstoqueBaixoService(CriacaoNotificacaoService criacaoService,
                                     ConfiguracaoNotificacaoService configuracaoService,
                                     Clock clock) {
        this.criacaoService = criacaoService;
        this.configuracaoService = configuracaoService;
        this.clock = clock;
    }

    public void avaliar(Insumo insumo, BigDecimal saldoAnterior, BigDecimal saldoNovo) {
        BigDecimal minimo = insumo.getQuantidadeMinima();
        if (minimo == null) {
            return;
        }
        boolean cruzouParaBaixo = saldoAnterior.compareTo(minimo) > 0
                && saldoNovo.compareTo(minimo) <= 0;
        if (!cruzouParaBaixo) {
            return;
        }

        ConfiguracaoNotificacao config = configuracaoService.obterOuCriar();
        if (!Boolean.TRUE.equals(config.getAlertaEstoqueBaixoAtivo())) {
            return;
        }

        LocalDate hoje = LocalDate.now(clock);
        String chave = "ESTOQUE_BAIXO:insumo:" + insumo.getIdInsumo() + ":" + hoje;
        String mensagem = "O insumo " + insumo.getNome() + " está em "
                + saldoNovo.stripTrailingZeros().toPlainString() + " "
                + insumo.getUnidade() + " (mínimo "
                + minimo.stripTrailingZeros().toPlainString() + ").";

        ComandoNotificacao comando = ComandoNotificacao.imediata(
                TipoNotificacao.ESTOQUE_BAIXO,
                "Estoque baixo",
                mensagem,
                chave,
                "estoque_baixo",
                new ParametrosEstoqueBaixo(insumo.getNome(), saldoNovo, minimo),
                null,
                insumo);
        criacaoService.criar(comando);
    }
}
