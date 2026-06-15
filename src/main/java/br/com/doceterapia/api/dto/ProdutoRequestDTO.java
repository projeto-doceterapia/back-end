package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;

@Schema(description = "DTO para requisição de cadastro ou atualização de produto")
public class ProdutoRequestDTO {

    @NotNull(message = "Categoria do produto é obrigatória")
    @Schema(description = "ID da categoria do produto", example = "1")
    private Integer fkCategoriaProduto;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 45, message = "Nome não pode exceder 45 caracteres")
    @Schema(description = "Nome do produto", example = "Bolo de Chocolate", maxLength = 45)
    private String nome;

    @PositiveOrZero(message = "Custo estimado deve ser maior ou igual a zero")
    @Schema(description = "Custo estimado do produto em reais", example = "25.50")
    private BigDecimal custoEstimado;

    @PositiveOrZero(message = "Preço atual deve ser maior ou igual a zero")
    @Schema(description = "Preço atual do produto em reais", example = "89.90")
    private BigDecimal precoAtual;

    @PositiveOrZero(message = "Preço sugerido deve ser maior ou igual a zero")
    @Schema(description = "Preço sugerido do produto em reais", example = "99.90")
    private BigDecimal precoSugerido;

    @PositiveOrZero(message = "Margem de lucro deve ser maior ou igual a zero")
    @Schema(description = "Margem de lucro do produto em reais", example = "64.40")
    private BigDecimal margemLucro;

    @Schema(description = "Unidade de produção do produto", example = "UNIDADE")
    private UnidadeMedida unidadeProducao;

    @Schema(description = "Status do produto", example = "ATIVO")
    private StatusAtivo status;

    @Schema(description = "Quantidade produzida", example = "10")
    private Integer quantidadeProduzida;

    @Size(max = 245, message = "Descrição não pode exceder 245 caracteres")
    @Schema(description = "Descrição do produto", example = "Bolo de chocolate com recheio de brigadeiro", maxLength = 245)
    private String descricao;

    public Integer getFkCategoriaProduto() {
        return fkCategoriaProduto;
    }

    public void setFkCategoriaProduto(Integer fkCategoriaProduto) {
        this.fkCategoriaProduto = fkCategoriaProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getCustoEstimado() {
        return custoEstimado;
    }

    public void setCustoEstimado(BigDecimal custoEstimado) {
        this.custoEstimado = custoEstimado;
    }

    public BigDecimal getPrecoAtual() {
        return precoAtual;
    }

    public void setPrecoAtual(BigDecimal precoAtual) {
        this.precoAtual = precoAtual;
    }

    public BigDecimal getPrecoSugerido() {
        return precoSugerido;
    }

    public void setPrecoSugerido(BigDecimal precoSugerido) {
        this.precoSugerido = precoSugerido;
    }

    public BigDecimal getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    public UnidadeMedida getUnidadeProducao() {
        return unidadeProducao;
    }

    public void setUnidadeProducao(UnidadeMedida unidadeProducao) {
        this.unidadeProducao = unidadeProducao;
    }

    public StatusAtivo getStatus() {
        return status;
    }

    public void setStatus(StatusAtivo status) {
        this.status = status;
    }

    public Integer getQuantidadeProduzida() {
        return quantidadeProduzida;
    }

    public void setQuantidadeProduzida(Integer quantidadeProduzida) {
        this.quantidadeProduzida = quantidadeProduzida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
