package br.com.doceterapia.api.service.notificacao;

import java.math.BigDecimal;

/**
 * Template parameters for an ESTOQUE_BAIXO alert: the supply name, its current
 * balance after the movement and its configured minimum.
 */
public record ParametrosEstoqueBaixo(
        String insumo,
        BigDecimal saldo,
        BigDecimal minimo) implements ParametrosTemplate {
}
