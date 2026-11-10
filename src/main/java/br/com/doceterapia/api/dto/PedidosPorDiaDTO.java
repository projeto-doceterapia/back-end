package br.com.doceterapia.api.dto;

import java.time.LocalDate;

public record PedidosPorDiaDTO(LocalDate data, long quantidade) {
}
