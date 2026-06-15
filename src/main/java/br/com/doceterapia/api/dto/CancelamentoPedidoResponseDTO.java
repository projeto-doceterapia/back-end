package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.TipoCancelamento;
import br.com.doceterapia.api.enums.TipoRetorno;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO para resposta de cancelamento de pedido")
public class CancelamentoPedidoResponseDTO {

    @Schema(description = "ID único do cancelamento", example = "1")
    private Integer idCancelamento;

    @Schema(description = "ID do pedido cancelado", example = "1")
    private Integer pedidoId;

    @Schema(description = "Tipo do cancelamento", example = "CLIENTE_DESISTIU")
    private TipoCancelamento tipoCancelamento;

    @Schema(description = "Tipo de retorno/reembolso", example = "DEVOLUCAO")
    private TipoRetorno tipoRetorno;

    @Schema(description = "Valor de retorno/reembolso em reais", example = "50.00")
    private BigDecimal valorRetorno;

    @Schema(description = "Data e hora do cancelamento", example = "2026-06-14T10:30:00")
    private LocalDateTime dataCancelamento;

    @Schema(description = "Observação sobre o cancelamento", example = "Cliente desistiu do pedido")
    private String observacao;

    public Integer getIdCancelamento() {
        return idCancelamento;
    }

    public void setIdCancelamento(Integer idCancelamento) {
        this.idCancelamento = idCancelamento;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
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
