package br.com.doceterapia.api.service.notificacao.resumo;

import java.time.LocalDate;

/**
 * Compact order line used in daily summary sections: the order id, its client
 * name and its delivery date.
 */
public record ItemResumoPedido(
        Integer pedidoId,
        String cliente,
        LocalDate dataEntrega) {
}
