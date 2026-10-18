package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;

@Schema(description = "DTO para confirmação de envio de uma notificação")
public class ConfirmacaoEnvioRequestDTO {

    @NotNull(message = "Status de envio é obrigatório")
    @Schema(description = "Resultado do envio (ENVIADA ou FALHA)", example = "ENVIADA")
    private StatusEnvioNotificacao statusEnvio;

    @Schema(description = "ID da mensagem no canal de envio", example = "wamid.ABC123")
    private String canalMessageId;

    public StatusEnvioNotificacao getStatusEnvio() {
        return statusEnvio;
    }

    public void setStatusEnvio(StatusEnvioNotificacao statusEnvio) {
        this.statusEnvio = statusEnvio;
    }

    public String getCanalMessageId() {
        return canalMessageId;
    }

    public void setCanalMessageId(String canalMessageId) {
        this.canalMessageId = canalMessageId;
    }
}
