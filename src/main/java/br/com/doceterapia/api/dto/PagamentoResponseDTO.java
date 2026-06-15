package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.FormaPagamento;
import br.com.doceterapia.api.enums.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO para resposta de pagamento")
public class PagamentoResponseDTO {

    @Schema(description = "ID único do pagamento", example = "1")
    private Integer idPagamento;

    @Schema(description = "ID do pedido associado ao pagamento", example = "1")
    private Integer pedidoId;

    @Schema(description = "Valor total do pedido em reais", example = "250.00")
    private BigDecimal valorTotal;

    @Schema(description = "Valor do sinal pago em reais", example = "125.00")
    private BigDecimal valorSinal;

    @Schema(description = "Valor restante a pagar em reais", example = "125.00")
    private BigDecimal valorRestante;

    @Schema(description = "Forma de pagamento", example = "PIX")
    private FormaPagamento formaPagamento;

    @Schema(description = "Status do pagamento", example = "AGUARDANDO_SINAL")
    private StatusPagamento statusPagamento;

    @Schema(description = "Data do pagamento do sinal", example = "2026-06-14", type = "string", format = "date")
    private LocalDate dataPagamentoSinal;

    @Schema(description = "Data do pagamento do restante", example = "2026-06-20", type = "string", format = "date")
    private LocalDate dataPagamentoRestante;

    public Integer getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Integer idPagamento) {
        this.idPagamento = idPagamento;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
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
