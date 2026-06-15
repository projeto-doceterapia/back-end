package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;

@Schema(description = "DTO para resposta de produto cadastrado ou atualizado")
public class ProdutoResponseDTO {

    @Schema(description = "ID único do produto", example = "1")
    private Integer idProduto;

    @Schema(description = "ID da categoria do produto", example = "1")
    private Integer categoriaId;

    @Schema(description = "Nome da categoria do produto", example = "Bolos")
    private String categoriaNome;

    @Schema(description = "Nome do produto", example = "Bolo de Chocolate")
    private String nome;

    @Schema(description = "Custo estimado do produto em reais", example = "25.50")
    private BigDecimal custoEstimado;

    @Schema(description = "Preço atual do produto em reais", example = "89.90")
    private BigDecimal precoAtual;

    @Schema(description = "Preço sugerido do produto em reais", example = "99.90")
    private BigDecimal precoSugerido;

    @Schema(description = "Margem de lucro do produto em reais", example = "64.40")
    private BigDecimal margemLucro;

    @Schema(description = "Unidade de produção do produto", example = "UNIDADE")
    private UnidadeMedida unidadeProducao;

    @Schema(description = "Status do produto", example = "ATIVO")
    private StatusAtivo status;

    @Schema(description = "Quantidade produzida", example = "10")
    private Integer quantidadeProduzida;

    @Schema(description = "Descrição do produto", example = "Bolo de chocolate com recheio de brigadeiro")
    private String descricao;

    public Integer getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
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
