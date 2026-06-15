package br.com.doceterapia.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "DTO para requisição de cadastro ou atualização de produção")
public class ProducaoRequestDTO {

    @NotNull(message = "Pedido é obrigatório")
    @Schema(description = "ID do pedido", example = "1")
    private Integer pedidoId;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Data de início da produção", example = "15/06/2026")
    private LocalDate dataInicio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Data prevista para conclusão", example = "20/06/2026")
    private LocalDate dataPrevista;

    @Schema(description = "Observação sobre a produção", example = "Produção com urgência")
    private String observacao;

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
