package br.com.doceterapia.api.service.notificacao;

/**
 * Template parameters for the {@code alerta_operacional} template. The Cloud API
 * body continues after the parameter, so {@code mensagem} must not bring its own
 * final period.
 */
public record ParametrosAlertaOperacional(String mensagem) implements ParametrosTemplate {
}
