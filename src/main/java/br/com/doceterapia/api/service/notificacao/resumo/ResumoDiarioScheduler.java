package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import br.com.doceterapia.api.service.notificacao.CriacaoNotificacaoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Runs every few minutes, reads the settings and the clock, and creates each
 * daily summary once its configured time has passed on a working day. The
 * creation service deduplicates by {@code RESUMO_*:{data}}, so a time changed
 * during the day applies on the same day and reruns never duplicate.
 */
@Component
public class ResumoDiarioScheduler {

    private final ConfiguracaoNotificacaoService configuracaoService;
    private final ResumoDiarioQueries queries;
    private final ResumoDiarioBuilder builder;
    private final CriacaoNotificacaoService criacaoService;
    private final Clock clock;

    public ResumoDiarioScheduler(ConfiguracaoNotificacaoService configuracaoService,
                                 ResumoDiarioQueries queries,
                                 ResumoDiarioBuilder builder,
                                 CriacaoNotificacaoService criacaoService,
                                 Clock clock) {
        this.configuracaoService = configuracaoService;
        this.queries = queries;
        this.builder = builder;
        this.criacaoService = criacaoService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${assistente.notificacao.intervalo-verificacao-ms:300000}")
    @Transactional
    public void verificar() {
        ConfiguracaoNotificacao config = configuracaoService.obterOuCriar();
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDate hoje = agora.toLocalDate();

        if (!diaDeTrabalho(hoje, config)) {
            return;
        }

        if (Boolean.TRUE.equals(config.getResumoInicioDiaAtivo())
                && horaPassou(agora, config.getHorarioResumoInicio())) {
            criarInicioDia(hoje, config.getHorarioResumoInicio());
        }

        if (Boolean.TRUE.equals(config.getResumoFimDiaAtivo())
                && horaPassou(agora, config.getHorarioResumoFim())) {
            criarFimDia(hoje, config.getHorarioResumoFim(), config.getDiasAntecedencia());
        }
    }

    private void criarInicioDia(LocalDate hoje, LocalTime horario) {
        ParametrosResumoInicioDia parametros = new ParametrosResumoInicioDia(
                queries.sinaisParaConferir(),
                queries.pagamentosFinaisParaConferir(),
                queries.entregasHoje(hoje),
                queries.pedidosAtrasados(hoje));
        criacaoService.criar(builder.inicioDia(hoje, horario, parametros));
    }

    private void criarFimDia(LocalDate hoje, LocalTime horario, int diasAntecedencia) {
        LocalDate amanha = hoje.plusDays(1);
        LocalDate ate = hoje.plusDays(diasAntecedencia);
        ParametrosResumoFimDia parametros = new ParametrosResumoFimDia(
                queries.proximosDias(amanha, ate),
                queries.insumosParaComprar());
        criacaoService.criar(builder.fimDia(hoje, horario, parametros));
    }

    private boolean horaPassou(LocalDateTime agora, LocalTime horario) {
        return !agora.toLocalTime().isBefore(horario);
    }

    private boolean diaDeTrabalho(LocalDate data, ConfiguracaoNotificacao config) {
        DayOfWeek dia = data.getDayOfWeek();
        for (String valor : config.getDiasExpediente().split(",")) {
            if (!valor.isBlank() && DayOfWeek.of(Integer.parseInt(valor.trim())) == dia) {
                return true;
            }
        }
        return false;
    }
}
