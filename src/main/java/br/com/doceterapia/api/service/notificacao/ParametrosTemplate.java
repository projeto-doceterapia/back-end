package br.com.doceterapia.api.service.notificacao;

/**
 * Marker for typed notification template parameters. Each notification type
 * provides its own record implementing this interface, so the creation service
 * serializes a well-typed object to JSON instead of a {@code Map<String, Object>}.
 */
public interface ParametrosTemplate {
}
