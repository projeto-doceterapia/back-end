package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "DTO para registro de uma parcela de pagamento")
public class RegistroPagamentoRequestDTO {

    @NotNull(message = "Forma de pagamento é obrigatória")
    @Schema(description = "Forma de pagamento da parcela", example = "PIX")
    private FormaPagamento formaPagamento;

    @Schema(description = "Data de pagamento da parcela", example = "2026-11-10", type = "string", format = "date")
    private LocalDate dataPagamento;

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
