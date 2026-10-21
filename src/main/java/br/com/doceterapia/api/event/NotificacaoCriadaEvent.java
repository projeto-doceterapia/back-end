package br.com.doceterapia.api.event;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;

/**
 * Published when a notification is persisted. The payload is the same body the
 * REST API returns, so the stream listener can forward it without reloading.
 */
public record NotificacaoCriadaEvent(NotificacaoResponseDTO notificacao) {
}
