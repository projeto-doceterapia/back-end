package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import br.com.doceterapia.api.enums.UnidadeMedida;

@Schema(description = "DTO para requisição de cadastro ou atualização de insumo de produto")
public class ProdutoInsumoRequestDTO {

    @NotNull(message = "Insumo é obrigatório")
    @Schema(description = "ID do insumo", example = "1")
    private Integer fkInsumo;

    @NotNull(message = "Quantidade utilizada é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade utilizada deve ser maior que zero")
    @Schema(description = "Quantidade utilizada do insumo", example = "0.500")
    private BigDecimal quantidadeUtilizada;

    @NotNull(message = "Unidade de medida é obrigatória")
    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    public Integer getFkInsumo() {
        return fkInsumo;
    }

    public void setFkInsumo(Integer fkInsumo) {
        this.fkInsumo = fkInsumo;
    }

    public BigDecimal getQuantidadeUtilizada() {
        return quantidadeUtilizada;
    }

    public void setQuantidadeUtilizada(BigDecimal quantidadeUtilizada) {
        this.quantidadeUtilizada = quantidadeUtilizada;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
    }
}
