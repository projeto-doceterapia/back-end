package br.com.doceterapia.api.service.notificacao;

import java.time.LocalDate;

/**
 * Template parameters for a DIA_LOTADO alert: the overbooked delivery date and
 * the number of active orders on it.
 */
public record ParametrosDiaLotado(
        LocalDate data,
        long quantidadePedidos) implements ParametrosTemplate {
}
