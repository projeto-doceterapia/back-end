package br.com.doceterapia.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProdutoPedido;

    @ManyToOne
    @JoinColumn(name = "fk_produto", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "fk_pedido", nullable = false)
    private Pedido pedido;

    @Column(precision = 10, scale = 3)
    private BigDecimal quantidade;

    @Column(precision = 10, scale = 2)
    private BigDecimal precoOriginalProduto;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal custoEstimadoItem;

    @Column(precision = 10, scale = 2)
    private BigDecimal margemLucroItem;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    public Integer getIdProdutoPedido() {
        return idProdutoPedido;
    }

    public void setIdProdutoPedido(Integer idProdutoPedido) {
        this.idProdutoPedido = idProdutoPedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
