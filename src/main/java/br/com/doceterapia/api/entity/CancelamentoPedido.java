package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.TipoCancelamento;
import br.com.doceterapia.api.enums.TipoRetorno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class CancelamentoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCancelamento;

    @OneToOne
    @JoinColumn(name = "fk_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoCancelamento tipoCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TipoRetorno tipoRetorno;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorRetorno;

    private LocalDateTime dataCancelamento;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    public Integer getIdCancelamento() {
        return idCancelamento;
    }

    public void setIdCancelamento(Integer idCancelamento) {
        this.idCancelamento = idCancelamento;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public TipoCancelamento getTipoCancelamento() {
        return tipoCancelamento;
    }

    public void setTipoCancelamento(TipoCancelamento tipoCancelamento) {
        this.tipoCancelamento = tipoCancelamento;
    }

    public TipoRetorno getTipoRetorno() {
        return tipoRetorno;
    }

    public void setTipoRetorno(TipoRetorno tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
    }

    public BigDecimal getValorRetorno() {
        return valorRetorno;
    }

    public void setValorRetorno(BigDecimal valorRetorno) {
        this.valorRetorno = valorRetorno;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
