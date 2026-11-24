package br.com.doceterapia.api.dto;

import java.time.LocalDate;

public record ClienteContagemDTO(Integer idCliente, long totalPedidos, LocalDate ultimoPedido) {
}
