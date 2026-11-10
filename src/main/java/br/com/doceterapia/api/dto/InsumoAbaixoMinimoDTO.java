package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.UnidadeMedida;

import java.math.BigDecimal;

public record InsumoAbaixoMinimoDTO(Integer insumoId, String nome, BigDecimal saldo, BigDecimal minimo, UnidadeMedida unidade) {
}
