package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.enums.UnidadeMedida;

import java.math.BigDecimal;

public record ConsumoPrevistoInsumo(Integer insumoId, UnidadeMedida unidade, BigDecimal quantidade) {
}
