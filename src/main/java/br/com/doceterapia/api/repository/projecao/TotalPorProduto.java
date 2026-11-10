package br.com.doceterapia.api.repository.projecao;

import java.math.BigDecimal;

public record TotalPorProduto(Integer produtoId, String nome, BigDecimal total) {
}
