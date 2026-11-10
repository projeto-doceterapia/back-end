package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO para atualização da configuração de precificação")
public record ConfiguracaoPrecificacaoRequestDTO(

        @NotNull(message = "Margem padrão é obrigatória")
        @DecimalMin(value = "0.00", message = "Margem padrão deve estar entre 0 e 999,99%")
        @DecimalMax(value = "999.99", message = "Margem padrão deve estar entre 0 e 999,99%")
        @Digits(integer = 3, fraction = 2, message = "Margem padrão aceita no máximo 2 casas decimais")
        @Schema(description = "Margem padrão em percentual sobre o custo, usada quando o produto não tem margem própria",
                example = "100.00")
        BigDecimal margemPadrao,

        @NotNull(message = "Percentual de custos indiretos é obrigatório")
        @DecimalMin(value = "0.00", message = "Percentual de custos indiretos deve estar entre 0 e 100%")
        @DecimalMax(value = "100.00", message = "Percentual de custos indiretos deve estar entre 0 e 100%")
        @Digits(integer = 3, fraction = 2, message = "Percentual de custos indiretos aceita no máximo 2 casas decimais")
        @Schema(description = "Percentual de gás, energia e desgaste somado ao custo dos insumos", example = "10.00")
        BigDecimal percentualIndiretos) {
}
