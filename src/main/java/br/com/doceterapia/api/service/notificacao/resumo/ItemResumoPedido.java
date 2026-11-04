package br.com.doceterapia.api.service.notificacao.resumo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Compact order line used in daily summary sections: the order id, its client
 * name and its delivery date. It is serialized as the readable line the owner
 * sees, because the orchestrator joins list items as plain text.
 */
public record ItemResumoPedido(
        Integer pedidoId,
        String cliente,
        LocalDate dataEntrega) {

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    @JsonValue
    public String texto() {
        String pedido = "Pedido " + pedidoId;
        return cliente == null ? pedido : pedido + " - " + cliente;
    }

    public String textoComData() {
        return dataEntrega.format(DIA_MES) + ": " + texto();
    }
}
