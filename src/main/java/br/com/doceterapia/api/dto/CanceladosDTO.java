package br.com.doceterapia.api.dto;

import java.math.BigDecimal;

public record CanceladosDTO(long quantidade, BigDecimal pago, BigDecimal devolvido, BigDecimal retido) {
}
