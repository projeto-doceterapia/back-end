package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO de resposta da configuração de precificação")
public record ConfiguracaoPrecificacaoResponseDTO(

        @Schema(description = "Margem padrão em percentual sobre o custo", example = "100.00")
        BigDecimal margemPadrao,

        @Schema(description = "Percentual de custos indiretos sobre o custo dos insumos", example = "10.00")
        BigDecimal percentualIndiretos) {
}
