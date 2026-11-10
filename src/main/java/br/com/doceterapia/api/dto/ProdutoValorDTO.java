package br.com.doceterapia.api.dto;

import java.math.BigDecimal;

public record ProdutoValorDTO(Integer produtoId, String nome, BigDecimal valor) {
}
