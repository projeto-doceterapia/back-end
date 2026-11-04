package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.service.notificacao.ParametrosTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Template parameters for the RESUMO_INICIO_DIA notification: the summary date
 * and the four sections of the start-of-day routine. Every list is always present
 * (possibly empty); an empty list renders as "nada pendente" in the template. The
 * JSON names are read by the {@code resumo_inicio_dia} template of the orchestrator.
 */
public record ParametrosResumoInicioDia(
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate data,
        @JsonProperty("sinais_pendentes") List<ItemResumoPedido> sinaisParaConferir,
        @JsonProperty("pagamentos_finais") List<ItemResumoPedido> pagamentosFinaisParaConferir,
        @JsonProperty("entregas_hoje") List<ItemResumoPedido> entregasHoje,
        @JsonProperty("pedidos_atrasados") List<ItemResumoPedido> pedidosAtrasados) implements ParametrosTemplate {
}
