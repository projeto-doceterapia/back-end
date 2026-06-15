package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "DTO para resposta de item de pedido cadastrado ou atualizado")
public class ItemPedidoResponseDTO {

    @Schema(description = "ID único do item do pedido", example = "1")
    private Integer idProdutoPedido;

    @Schema(description = "ID do produto", example = "1")
    private Integer fkProduto;

    @Schema(description = "ID do pedido", example = "1")
    private Integer fkPedido;

    @Schema(description = "Quantidade do produto no pedido", example = "2")
    private BigDecimal quantidade;

    @Schema(description = "Preço original do produto no momento da venda", example = "45.90")
    private BigDecimal precoOriginalProduto;

    @Schema(description = "Valor unitário do produto em reais", example = "45.90")
    private BigDecimal valorUnitario;

    @Schema(description = "Valor total do item em reais", example = "91.80")
    private BigDecimal valorTotal;

    @Schema(description = "Custo estimado do item em reais", example = "30.00")
    private BigDecimal custoEstimadoItem;

    @Schema(description = "Margem de lucro do item em reais", example = "61.80")
    private BigDecimal margemLucroItem;

    @Schema(description = "Observação sobre o item", example = "Sem glúten")
    private String observacao;

    public Integer getIdProdutoPedido() {
        return idProdutoPedido;
    }

    public void setIdProdutoPedido(Integer idProdutoPedido) {
        this.idProdutoPedido = idProdutoPedido;
    }

    public Integer getFkProduto() {
        return fkProduto;
    }

    public void setFkProduto(Integer fkProduto) {
        this.fkProduto = fkProduto;
    }

    public Integer getFkPedido() {
        return fkPedido;
    }

    public void setFkPedido(Integer fkPedido) {
        this.fkPedido = fkPedido;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoOriginalProduto() {
        return precoOriginalProduto;
    }

    public void setPrecoOriginalProduto(BigDecimal precoOriginalProduto) {
        this.precoOriginalProduto = precoOriginalProduto;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getCustoEstimadoItem() {
        return custoEstimadoItem;
    }

    public void setCustoEstimadoItem(BigDecimal custoEstimadoItem) {
        this.custoEstimadoItem = custoEstimadoItem;
    }

    public BigDecimal getMargemLucroItem() {
        return margemLucroItem;
    }

    public void setMargemLucroItem(BigDecimal margemLucroItem) {
        this.margemLucroItem = margemLucroItem;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
