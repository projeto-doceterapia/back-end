package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.FormaPagamento;
import br.com.doceterapia.api.enums.StatusPagamento;
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
import java.time.LocalDate;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPagamento;

    @OneToOne
    @JoinColumn(name = "fk_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorSinal;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorRestante;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusPagamento statusPagamento;

    private LocalDate dataPagamentoSinal;

    private LocalDate dataPagamentoRestante;

    public Integer getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Integer idPagamento) {
        this.idPagamento = idPagamento;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorSinal() {
        return valorSinal;
    }

    public void setValorSinal(BigDecimal valorSinal) {
        this.valorSinal = valorSinal;
    }

    public BigDecimal getValorRestante() {
        return valorRestante;
    }

    public void setValorRestante(BigDecimal valorRestante) {
        this.valorRestante = valorRestante;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public LocalDate getDataPagamentoSinal() {
        return dataPagamentoSinal;
    }

    public void setDataPagamentoSinal(LocalDate dataPagamentoSinal) {
        this.dataPagamentoSinal = dataPagamentoSinal;
    }

    public LocalDate getDataPagamentoRestante() {
        return dataPagamentoRestante;
    }

    public void setDataPagamentoRestante(LocalDate dataPagamentoRestante) {
        this.dataPagamentoRestante = dataPagamentoRestante;
    }
}
