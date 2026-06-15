package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "DTO para resposta de insumo cadastrado")
public class InsumoResponseDTO {

    @Schema(description = "ID único do insumo", example = "1")
    private Integer idInsumo;

    @Schema(description = "ID da categoria de insumo", example = "1")
    private Integer categoriaId;

    @Schema(description = "Nome da categoria de insumo", example = "Farináceos")
    private String categoriaNome;

    @Schema(description = "Nome do insumo", example = "Farinha de trigo")
    private String nome;

    @Schema(description = "Quantidade atual em estoque", example = "10.500")
    private BigDecimal quantidadeAtual;

    @Schema(description = "Quantidade mínima permitida", example = "2.000")
    private BigDecimal quantidadeMinima;

    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    @Schema(description = "Status do insumo", example = "ATIVO")
    private StatusAtivo status;

    @Schema(description = "Marca do insumo", example = "Dona Benta")
    private String marca;

    @Schema(description = "Custo unitário do insumo", example = "15.5000")
    private BigDecimal custoUnitario;

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        this.categoriaNome = categoriaNome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public BigDecimal getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(BigDecimal quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
    }

    public StatusAtivo getStatus() {
        return status;
    }

    public void setStatus(StatusAtivo status) {
        this.status = status;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }
}
