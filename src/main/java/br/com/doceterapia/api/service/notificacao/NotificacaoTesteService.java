package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.exception.NotificacaoTesteCooldownException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Creates the test notification the owner sends from the settings window. The
 * cooldown is measured from the latest test row, so it survives a restart and
 * does not depend on the browser clock. {@link #enviar} is synchronized so two
 * clicks cannot both pass the check, which is enough for the single back-end
 * instance.
 */
@Service
public class NotificacaoTesteService {

    static final String PREFIXO_CHAVE = "TESTE:";
    static final String TEXTO_PARAMETRO =
            "Teste de notificação do Doceterapia: se você recebeu esta mensagem, os avisos estão funcionando";

    private static final String TITULO = "Notificação de teste";
    private static final String TEMPLATE = "alerta_operacional";

    private final NotificacaoRepository repository;
    private final CriacaoNotificacaoService criacaoService;
    private final NotificacaoMapper mapper;
    private final Clock clock;
    private final long cooldownSegundos;

    public NotificacaoTesteService(
            NotificacaoRepository repository,
            CriacaoNotificacaoService criacaoService,
            NotificacaoMapper mapper,
            Clock clock,
            @Value("${assistente.notificacao.teste-cooldown-s:300}") long cooldownSegundos) {
        this.repository = repository;
        this.criacaoService = criacaoService;
        this.mapper = mapper;
        this.clock = clock;
        this.cooldownSegundos = cooldownSegundos;
    }

    public synchronized NotificacaoResponseDTO enviar() {
        LocalDateTime agora = LocalDateTime.now(clock);
        repository.findFirstByChaveUnicidadeStartingWithOrderByDataCriacaoDesc(PREFIXO_CHAVE)
                .ifPresent(ultimo -> validarCooldown(ultimo.getDataCriacao(), agora));

        ComandoNotificacao comando = ComandoNotificacao.agora(
                TipoNotificacao.ALERTA_OPERACIONAL,
                TITULO,
                TEXTO_PARAMETRO + ".",
                PREFIXO_CHAVE + agora,
                TEMPLATE,
                new ParametrosAlertaOperacional(TEXTO_PARAMETRO));
        return mapper.toResponseDTO(criacaoService.criar(comando));
    }

    private void validarCooldown(LocalDateTime ultimoTeste, LocalDateTime agora) {
        long decorridos = Duration.between(ultimoTeste, agora).toSeconds();
        if (decorridos < cooldownSegundos) {
            throw new NotificacaoTesteCooldownException(cooldownSegundos - decorridos);
        }
    }
}
