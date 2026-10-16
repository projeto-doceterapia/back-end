package br.com.doceterapia.api.service;

import br.com.doceterapia.api.event.NotificacaoCriadaEvent;
import br.com.doceterapia.api.event.NotificacaoLidaEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Pushes stream events only after the surrounding transaction commits, so a
 * rolled-back change never reaches a connected client.
 */
@Component
public class NotificacaoStreamListener {

    private final NotificacaoEmitterRegistry registry;

    public NotificacaoStreamListener(NotificacaoEmitterRegistry registry) {
        this.registry = registry;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriar(NotificacaoCriadaEvent event) {
        registry.enviar("notificacao", event.notificacao());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoMarcarLida(NotificacaoLidaEvent event) {
        registry.enviar("lida", Map.of("id", event.id()));
    }
}
