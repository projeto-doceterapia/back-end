package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInsumo;

    @ManyToOne
    @JoinColumn(name = "fk_categoria_insumo")
    private CategoriaInsumo categoria;

    private String nome;

    @Column(precision = 10, scale = 3)
    private BigDecimal quantidadeAtual;

    @Column(precision = 10, scale = 3)
    private BigDecimal quantidadeMinima;

    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidade;

    @Enumerated(EnumType.STRING)
    private StatusAtivo status;

    private String marca;

    @Column(precision = 10, scale = 4)
    private BigDecimal custoUnitario;

    public Integer getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Integer idInsumo) {
        this.idInsumo = idInsumo;
    }

    public CategoriaInsumo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaInsumo categoria) {
        this.categoria = categoria;
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
