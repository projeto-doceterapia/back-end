package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Página do catálogo de insumos com paginação por cursor")
public class InsumoCatalogoResponseDTO {

    @Schema(description = "Itens da página, ordenados por nome normalizado")
    private List<InsumoCatalogoItemDTO> itens;

    @Schema(description = "Total de insumos que atendem aos filtros", example = "80")
    private long total;

    @Schema(description = "Cursor para a próxima página; nulo quando não há mais itens", example = "ZmFyaW5oYSBkZSB0cmlnbwA1")
    private String proximoCursor;

    public InsumoCatalogoResponseDTO() {
    }

    public InsumoCatalogoResponseDTO(List<InsumoCatalogoItemDTO> itens, long total, String proximoCursor) {
        this.itens = itens;
        this.total = total;
        this.proximoCursor = proximoCursor;
    }

    public List<InsumoCatalogoItemDTO> getItens() {
        return itens;
    }

    public void setItens(List<InsumoCatalogoItemDTO> itens) {
        this.itens = itens;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getProximoCursor() {
        return proximoCursor;
    }

    public void setProximoCursor(String proximoCursor) {
        this.proximoCursor = proximoCursor;
    }
}
