package br.com.doceterapia.api.service.notificacao.resumo;

import java.time.LocalDate;
import java.util.List;

/**
 * Orders of a single delivery date in the end-of-day summary, split into those
 * ready to produce and those still awaiting the down payment.
 */
public record GrupoDataResumo(
        LocalDate data,
        List<ItemResumoPedido> prontosParaProduzir,
        List<ItemResumoPedido> aguardandoSinal) {
}
