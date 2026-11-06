package br.com.doceterapia.api.service.notificacao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Template parameters for a DIA_LOTADO alert: the overbooked delivery date and
 * the number of active orders on it. The JSON names and the date format are read
 * by the {@code dia_lotado} template of the orchestrator.
 */
public record ParametrosDiaLotado(
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate data,
        @JsonProperty("pedidos") long quantidadePedidos) implements ParametrosTemplate {
}
