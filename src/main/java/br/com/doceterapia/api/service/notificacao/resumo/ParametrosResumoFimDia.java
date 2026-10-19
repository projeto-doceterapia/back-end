package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.service.notificacao.ParametrosTemplate;

import java.util.List;

/**
 * Template parameters for the RESUMO_FIM_DIA notification: the orders to produce
 * in the next days grouped by date and the supplies to buy. Both lists are always
 * present (possibly empty); an empty list renders as "nada pendente".
 */
public record ParametrosResumoFimDia(
        List<GrupoDataResumo> proximosDias,
        List<ItemResumoInsumo> insumosParaComprar) implements ParametrosTemplate {
}
