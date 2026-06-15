package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO para atualização de pagamento")
public class PagamentoRequestDTO {

    @Schema(description = "Forma de pagamento", example = "PIX")
    private FormaPagamento formaPagamento;

    @Schema(description = "Data do pagamento do sinal", example = "2026-06-14", type = "string", format = "date")
    private LocalDate dataPagamentoSinal;

    @Schema(description = "Data do pagamento do restante", example = "2026-06-20", type = "string", format = "date")
    private LocalDate dataPagamentoRestante;

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
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
