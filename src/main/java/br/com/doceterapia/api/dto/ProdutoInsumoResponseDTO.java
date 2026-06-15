package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import br.com.doceterapia.api.enums.UnidadeMedida;

@Schema(description = "DTO para resposta de insumo de produto cadastrado ou atualizado")
public class ProdutoInsumoResponseDTO {

    @Schema(description = "ID único do vínculo produto-insumo", example = "1")
    private Integer idProdutoInsumo;

    @Schema(description = "ID do produto", example = "1")
    private Integer fkProduto;

    @Schema(description = "ID do insumo", example = "1")
    private Integer fkInsumo;

    @Schema(description = "Nome do insumo", example = "Farinha de Trigo")
    private String nomeInsumo;

    @Schema(description = "Quantidade utilizada do insumo", example = "0.500")
    private BigDecimal quantidadeUtilizada;

    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    public Integer getIdProdutoInsumo() {
        return idProdutoInsumo;
    }

    public void setIdProdutoInsumo(Integer idProdutoInsumo) {
        this.idProdutoInsumo = idProdutoInsumo;
    }

    public Integer getFkProduto() {
        return fkProduto;
    }

    public void setFkProduto(Integer fkProduto) {
        this.fkProduto = fkProduto;
    }

    public Integer getFkInsumo() {
        return fkInsumo;
    }

    public void setFkInsumo(Integer fkInsumo) {
        this.fkInsumo = fkInsumo;
    }

    public String getNomeInsumo() {
        return nomeInsumo;
    }

    public void setNomeInsumo(String nomeInsumo) {
        this.nomeInsumo = nomeInsumo;
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
