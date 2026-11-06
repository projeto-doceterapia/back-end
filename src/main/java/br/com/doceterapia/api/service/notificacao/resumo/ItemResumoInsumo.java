package br.com.doceterapia.api.service.notificacao.resumo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;

/**
 * Compact supply line used in the end-of-day summary: supply name, current
 * balance, minimum and unit. It is serialized as the readable line the owner
 * sees, because the orchestrator joins list items as plain text.
 */
public record ItemResumoInsumo(
        String insumo,
        BigDecimal saldo,
        BigDecimal minimo,
        String unidade) {

    @JsonValue
    public String texto() {
        return insumo + ": " + quantidade(saldo) + " (mínimo " + quantidade(minimo) + ")";
    }

    private String quantidade(BigDecimal valor) {
        String numero = valor.stripTrailingZeros().toPlainString();
        return unidade == null ? numero : numero + " " + unidade;
    }
}
