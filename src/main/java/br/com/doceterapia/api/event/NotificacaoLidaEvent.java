package br.com.doceterapia.api.event;

/**
 * Published when a notification is marked as read. Carries only the id, which is
 * all the stream contract sends for a {@code lida} event.
 */
public record NotificacaoLidaEvent(Integer id) {
}
