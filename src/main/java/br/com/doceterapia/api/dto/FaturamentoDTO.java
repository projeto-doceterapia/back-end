package br.com.doceterapia.api.dto;

import java.math.BigDecimal;

public record FaturamentoDTO(BigDecimal total, BigDecimal recebido, BigDecimal aReceber) {
}
