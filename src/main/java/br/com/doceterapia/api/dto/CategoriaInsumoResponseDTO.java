package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de categoria de insumo cadastrada")
public class CategoriaInsumoResponseDTO {

    @Schema(description = "ID único da categoria de insumo", example = "1")
    private Integer idCategoriaInsumo;

    @Schema(description = "Nome da categoria de insumo", example = "Farináceos")
    private String nome;

    @Schema(description = "Descrição da categoria de insumo", example = "Farinhas, féculas e amidos")
    private String descricao;

    public Integer getIdCategoriaInsumo() {
        return idCategoriaInsumo;
    }

    public void setIdCategoriaInsumo(Integer idCategoriaInsumo) {
        this.idCategoriaInsumo = idCategoriaInsumo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
