package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item compacto do catálogo de insumos para o assistente")
public class InsumoCatalogoItemDTO {

    @Schema(description = "ID único do insumo", example = "1")
    private Integer idInsumo;

    @Schema(description = "Nome do insumo", example = "Farinha de trigo")
    private String nome;

    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    public InsumoCatalogoItemDTO() {
    }

    public InsumoCatalogoItemDTO(Integer idInsumo, String nome, UnidadeMedida unidade) {
        this.idInsumo = idInsumo;
        this.nome = nome;
        this.unidade = unidade;
    }

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

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
}
