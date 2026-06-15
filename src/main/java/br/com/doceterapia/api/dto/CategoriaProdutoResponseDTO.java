package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de categoria de produto cadastrada ou atualizada")
public class CategoriaProdutoResponseDTO {

    @Schema(description = "ID único da categoria de produto", example = "1")
    private Integer idCategoriaProduto;

    @Schema(description = "Nome da categoria de produto", example = "Bolos")
    private String nome;

    @Schema(description = "Descrição da categoria de produto", example = "Categoria para todos os tipos de bolos")
    private String descricao;

    public Integer getIdCategoriaProduto() {
        return idCategoriaProduto;
    }

    public void setIdCategoriaProduto(Integer idCategoriaProduto) {
        this.idCategoriaProduto = idCategoriaProduto;
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
