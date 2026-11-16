package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.UnidadeMedida;

import java.math.BigDecimal;

public record FaltaInsumoDTO(Integer insumoId, String nome, BigDecimal necessario,
                             BigDecimal disponivel, UnidadeMedida unidade) {
}
