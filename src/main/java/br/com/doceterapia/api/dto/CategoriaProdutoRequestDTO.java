package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para requisição de cadastro ou atualização de categoria de produto")
public class CategoriaProdutoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode exceder 100 caracteres")
    @Schema(description = "Nome da categoria de produto", example = "Bolos", maxLength = 100)
    private String nome;

    @Size(max = 245, message = "Descrição não pode exceder 245 caracteres")
    @Schema(description = "Descrição da categoria de produto", example = "Categoria para todos os tipos de bolos", maxLength = 245)
    private String descricao;

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
