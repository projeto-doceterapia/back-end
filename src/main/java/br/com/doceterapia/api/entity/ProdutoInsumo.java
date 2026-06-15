package br.com.doceterapia.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import br.com.doceterapia.api.enums.UnidadeMedida;

@Entity
@Table(name = "produto_insumo", uniqueConstraints = @UniqueConstraint(columnNames = {"fk_produto", "fk_insumo"}))
public class ProdutoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProdutoInsumo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_produto", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_insumo", nullable = false)
    private Insumo insumo;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal quantidadeUtilizada;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UnidadeMedida unidade;

    public Integer getIdProdutoInsumo() {
        return idProdutoInsumo;
    }

    public void setIdProdutoInsumo(Integer idProdutoInsumo) {
        this.idProdutoInsumo = idProdutoInsumo;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
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
