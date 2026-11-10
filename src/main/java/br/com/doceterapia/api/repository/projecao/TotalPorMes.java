package br.com.doceterapia.api.repository.projecao;

import java.math.BigDecimal;

public record TotalPorMes(int ano, int mes, BigDecimal total) {
}
