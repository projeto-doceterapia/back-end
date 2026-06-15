package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "DTO para requisição de cadastro ou atualização de insumo")
public class InsumoRequestDTO {

    @NotNull(message = "Categoria de insumo é obrigatória")
    @Schema(description = "ID da categoria de insumo", example = "1")
    private Integer fkCategoriaInsumo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 45, message = "Nome não pode exceder 45 caracteres")
    @Schema(description = "Nome do insumo", example = "Farinha de trigo", maxLength = 45)
    private String nome;

    @NotNull(message = "Quantidade atual é obrigatória")
    @PositiveOrZero(message = "Quantidade atual deve ser maior ou igual a zero")
    @Schema(description = "Quantidade atual em estoque do insumo", example = "10.500")
    private BigDecimal quantidadeAtual;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @PositiveOrZero(message = "Quantidade mínima deve ser maior ou igual a zero")
    @Schema(description = "Quantidade mínima permitida em estoque", example = "2.000")
    private BigDecimal quantidadeMinima;

    @NotNull(message = "Unidade de medida é obrigatória")
    @Schema(description = "Unidade de medida do insumo", example = "KG")
    private UnidadeMedida unidade;

    @Schema(description = "Status do insumo", example = "ATIVO")
    private StatusAtivo status;

    @Size(max = 45, message = "Marca não pode exceder 45 caracteres")
    @Schema(description = "Marca do insumo", example = "Dona Benta", maxLength = 45)
    private String marca;

    @PositiveOrZero(message = "Custo unitário deve ser maior ou igual a zero")
    @Schema(description = "Custo unitário do insumo", example = "15.5000")
    private BigDecimal custoUnitario;

    public Integer getFkCategoriaInsumo() {
        return fkCategoriaInsumo;
    }

    public void setFkCategoriaInsumo(Integer fkCategoriaInsumo) {
        this.fkCategoriaInsumo = fkCategoriaInsumo;
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
