package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.doceterapia.api.enums.StatusProducao;

import java.time.LocalDate;

@Schema(description = "DTO para resposta de produção cadastrada ou atualizada")
public class ProducaoResponseDTO {

    @Schema(description = "ID único da produção", example = "1")
    private Integer idProducao;

    @Schema(description = "ID do pedido", example = "1")
    private Integer pedidoId;

    @Schema(description = "Data de início da produção", example = "15/06/2026")
    private LocalDate dataInicio;

    @Schema(description = "Data prevista para conclusão", example = "20/06/2026")
    private LocalDate dataPrevista;

    @Schema(description = "Data de finalização da produção", example = "25/06/2026")
    private LocalDate dataFinalizacao;

    @Schema(description = "Status da produção", example = "ESPERANDO_FAZER")
    private StatusProducao statusProducao;

    @Schema(description = "Observação sobre a produção", example = "Produção com urgência")
    private String observacao;

    public Integer getIdProducao() {
        return idProducao;
    }

    public void setIdProducao(Integer idProducao) {
        this.idProducao = idProducao;
    }

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

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public StatusProducao getStatusProducao() {
        return statusProducao;
    }

    public void setStatusProducao(StatusProducao statusProducao) {
        this.statusProducao = statusProducao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
