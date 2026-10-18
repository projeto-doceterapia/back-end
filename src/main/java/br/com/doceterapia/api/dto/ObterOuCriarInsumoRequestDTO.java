package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para obter um insumo existente pelo nome normalizado ou criá-lo")
public class ObterOuCriarInsumoRequestDTO {

    // The name is not bean-validated here on purpose: blank and over-45-char
    // names map to 422, which the service enforces, not the 400 that
    // jakarta validation would produce.
    @Schema(description = "Nome do insumo", example = "Farinha de trigo", maxLength = 45)
    private String nome;

    @NotNull(message = "Unidade de medida é obrigatória")
    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    @Schema(description = "ID da categoria de insumo; opcional, cai na categoria padrão quando ausente ou inválido", example = "1")
    private Integer fkCategoriaInsumo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
    }

    public Integer getFkCategoriaInsumo() {
        return fkCategoriaInsumo;
    }

    public void setFkCategoriaInsumo(Integer fkCategoriaInsumo) {
        this.fkCategoriaInsumo = fkCategoriaInsumo;
    }
}
