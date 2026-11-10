package br.com.doceterapia.api.dto;

import java.math.BigDecimal;

public record ProdutoQuantidadeDTO(Integer produtoId, String nome, BigDecimal quantidade) {
}
