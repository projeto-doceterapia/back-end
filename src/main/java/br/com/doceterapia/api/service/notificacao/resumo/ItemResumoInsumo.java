package br.com.doceterapia.api.service.notificacao.resumo;

import java.math.BigDecimal;

/**
 * Compact supply line used in the end-of-day summary: supply name, current
 * balance, minimum and unit.
 */
public record ItemResumoInsumo(
        String insumo,
        BigDecimal saldo,
        BigDecimal minimo,
        String unidade) {
}
