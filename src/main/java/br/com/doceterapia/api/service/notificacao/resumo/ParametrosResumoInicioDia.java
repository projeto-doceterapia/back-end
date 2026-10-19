package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.service.notificacao.ParametrosTemplate;

import java.util.List;

/**
 * Template parameters for the RESUMO_INICIO_DIA notification: the four sections
 * of the start-of-day routine. Every list is always present (possibly empty);
 * an empty list renders as "nada pendente" in the template.
 */
public record ParametrosResumoInicioDia(
        List<ItemResumoPedido> sinaisParaConferir,
        List<ItemResumoPedido> pagamentosFinaisParaConferir,
        List<ItemResumoPedido> entregasHoje,
        List<ItemResumoPedido> pedidosAtrasados) implements ParametrosTemplate {
}
