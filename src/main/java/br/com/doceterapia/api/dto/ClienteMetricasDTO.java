package br.com.doceterapia.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteMetricasDTO(Integer idCliente, long totalPedidos, BigDecimal gastoTotal, LocalDate ultimoPedido) {

    public ClienteMetricasDTO {
        if (gastoTotal == null) {
            gastoTotal = BigDecimal.ZERO;
        }
    }
}
