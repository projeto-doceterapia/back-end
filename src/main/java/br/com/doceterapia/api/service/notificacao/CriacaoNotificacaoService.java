package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Single entry point that creates automatic notifications. It guarantees
 * deduplication by business key, computes the delivery window (summary jitter
 * or a short immediate window) inside business hours, serializes the typed
 * template parameters to JSON and marks the notification as PENDENTE for
 * delivery. Immediate-alert and daily-summary tasks call {@link #criar}.
 */
@Service
public class CriacaoNotificacaoService {

    private final NotificacaoRepository repository;
    private final ConfiguracaoNotificacaoService configuracaoService;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final int jitterResumoMin;
    private final int janelaImediataMin;

    public CriacaoNotificacaoService(
            NotificacaoRepository repository,
            ConfiguracaoNotificacaoService configuracaoService,
            Clock clock,
            ObjectMapper objectMapper,
            @Value("${assistente.notificacao.jitter-resumo-min:10}") int jitterResumoMin,
            @Value("${assistente.notificacao.janela-imediata-min:2}") int janelaImediataMin) {
        this.repository = repository;
        this.configuracaoService = configuracaoService;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.jitterResumoMin = jitterResumoMin;
        this.janelaImediataMin = janelaImediataMin;
    }

    /**
     * Creates the notification unless one with the same business key already
     * exists. Returns the created notification, or the existing one when the
     * key is already present, so reruns and restarts never duplicate.
     */
    public Notificacao criar(ComandoNotificacao comando) {
        Optional<Notificacao> existente =
                repository.findByChaveUnicidade(comando.chaveUnicidade());
        if (existente.isPresent()) {
            return existente.get();
        }

        ConfiguracaoNotificacao config = configuracaoService.obterOuCriar();

        LocalDateTime base = baseDaJanela(comando);
        LocalDateTime inicio = moverParaExpediente(base, config);
        LocalDateTime fim = inicio.plus(duracaoDaJanela(comando));

        Notificacao entity = new Notificacao();
        entity.setTipo(comando.tipo());
        entity.setTitulo(comando.titulo());
        entity.setMensagem(comando.mensagem());
        entity.setDataCriacao(LocalDateTime.now(clock));
        entity.setLida(false);
        entity.setChaveUnicidade(comando.chaveUnicidade());
        entity.setTemplateKey(comando.templateKey());
        entity.setParametros(serializar(comando.parametros()));
        entity.setJanelaInicio(inicio);
        entity.setJanelaFim(fim);
        entity.setStatusEnvio(StatusEnvioNotificacao.PENDENTE);
        entity.setPedido(comando.pedido());
        entity.setInsumo(comando.insumo());

        return repository.save(entity);
    }

    private LocalDateTime baseDaJanela(ComandoNotificacao comando) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (comando.janela() == ComandoNotificacao.Janela.RESUMO) {
            return agora.toLocalDate().atTime(comando.horarioResumo());
        }
        return agora;
    }

    private Duration duracaoDaJanela(ComandoNotificacao comando) {
        if (comando.janela() == ComandoNotificacao.Janela.RESUMO) {
            return Duration.ofMinutes(jitterResumoMin);
        }
        return Duration.ofMinutes(janelaImediataMin);
    }

    private String serializar(ParametrosTemplate parametros) {
        if (parametros == null) {
            return null;
        }
        return objectMapper.writeValueAsString(parametros);
    }

    /**
     * Moves a window start into the next business period: to the start of the
     * expediente when the moment is before it on a working day, or to the start
     * of the next working day when it is after the expediente or on a day off.
     */
    private LocalDateTime moverParaExpediente(LocalDateTime momento, ConfiguracaoNotificacao config) {
        LocalTime inicioExpediente = config.getInicioExpediente();
        LocalTime fimExpediente = config.getFimExpediente();

        if (diaDeTrabalho(momento.toLocalDate(), config)
                && !momento.toLocalTime().isBefore(inicioExpediente)
                && !momento.toLocalTime().isAfter(fimExpediente)) {
            return momento;
        }

        if (diaDeTrabalho(momento.toLocalDate(), config)
                && momento.toLocalTime().isBefore(inicioExpediente)) {
            return momento.toLocalDate().atTime(inicioExpediente);
        }

        LocalDate proximoDia = momento.toLocalDate().plusDays(1);
        while (!diaDeTrabalho(proximoDia, config)) {
            proximoDia = proximoDia.plusDays(1);
        }
        return proximoDia.atTime(inicioExpediente);
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
