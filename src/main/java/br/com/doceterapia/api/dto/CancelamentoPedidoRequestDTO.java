package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.TipoCancelamento;
import br.com.doceterapia.api.enums.TipoRetorno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "DTO para requisição de cadastro ou atualização de cancelamento de pedido")
public class CancelamentoPedidoRequestDTO {

    @NotNull(message = "Tipo de cancelamento é obrigatório")
    @Schema(description = "Tipo do cancelamento", example = "CLIENTE_DESISTIU")
    private TipoCancelamento tipoCancelamento;

    @NotNull(message = "Tipo de retorno é obrigatório")
    @Schema(description = "Tipo de retorno/reembolso", example = "DEVOLUCAO")
    private TipoRetorno tipoRetorno;

    @Schema(description = "Valor de retorno/reembolso em reais", example = "50.00")
    private BigDecimal valorRetorno;

    @Schema(description = "Observação sobre o cancelamento", example = "Cliente desistiu do pedido")
    private String observacao;

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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
