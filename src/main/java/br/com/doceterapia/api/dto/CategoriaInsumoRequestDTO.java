package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para requisição de cadastro ou atualização de categoria de insumo")
public class CategoriaInsumoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 45, message = "Nome não pode exceder 45 caracteres")
    @Schema(description = "Nome da categoria de insumo", example = "Farináceos", maxLength = 45)
    private String nome;

    @Size(max = 245, message = "Descrição não pode exceder 245 caracteres")
    @Schema(description = "Descrição da categoria de insumo", example = "Farinhas, féculas e amidos", maxLength = 245)
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
