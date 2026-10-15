package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "movimentacao_estoque",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_movimentacao_chave_idempotencia",
        columnNames = "chave_idempotencia"
    ),
    indexes = @Index(name = "idx_movimentacao_chave_nota", columnList = "chave_nota")
)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimentacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_insumo", nullable = false)
    private Insumo insumo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoMovimentacao tipo;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal quantidade;

    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal quantidadeInformada;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UnidadeMedida unidadeInformada;

    @Column(precision = 10, scale = 4)
    private BigDecimal custoUnitario;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrigemMovimentacao origem;

    @Column(length = 44)
    private String chaveNota;

    private String fluxoId;

    @Column(length = 120)
    private String chaveIdempotencia;

    @Column(length = 64, nullable = false)
    private String hashConteudo;

    @Column(nullable = false)
    private LocalDateTime ocorridoEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public Integer getIdMovimentacao() {
        return idMovimentacao;
    }

    public void setIdMovimentacao(Integer idMovimentacao) {
        this.idMovimentacao = idMovimentacao;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getQuantidadeInformada() {
        return quantidadeInformada;
    }

    public void setQuantidadeInformada(BigDecimal quantidadeInformada) {
        this.quantidadeInformada = quantidadeInformada;
    }

    public UnidadeMedida getUnidadeInformada() {
        return unidadeInformada;
    }

    public void setUnidadeInformada(UnidadeMedida unidadeInformada) {
        this.unidadeInformada = unidadeInformada;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public OrigemMovimentacao getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemMovimentacao origem) {
        this.origem = origem;
    }

    public String getChaveNota() {
        return chaveNota;
    }

    public void setChaveNota(String chaveNota) {
        this.chaveNota = chaveNota;
    }

    public String getFluxoId() {
        return fluxoId;
    }

    public void setFluxoId(String fluxoId) {
        this.fluxoId = fluxoId;
    }

    public String getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public void setChaveIdempotencia(String chaveIdempotencia) {
        this.chaveIdempotencia = chaveIdempotencia;
    }

    public String getHashConteudo() {
        return hashConteudo;
    }

    public void setHashConteudo(String hashConteudo) {
        this.hashConteudo = hashConteudo;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public void setOcorridoEm(LocalDateTime ocorridoEm) {
        this.ocorridoEm = ocorridoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
