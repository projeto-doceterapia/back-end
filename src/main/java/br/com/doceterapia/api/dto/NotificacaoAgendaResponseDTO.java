package br.com.doceterapia.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO para item da agenda de entrega de notificações")
public class NotificacaoAgendaResponseDTO {

    @Schema(description = "ID da notificação", example = "1")
    private Integer idNotificacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Início da janela de entrega", example = "2026-06-14T08:00:00")
    private LocalDateTime janelaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fim da janela de entrega", example = "2026-06-14T08:10:00")
    private LocalDateTime janelaFim;

    @Schema(description = "Chave do template de mensagem", example = "estoque_baixo")
    private String templateKey;

    @Schema(description = "Parâmetros do template em JSON", example = "{\"insumo\":\"Farinha\"}")
    private String parametros;

    public Integer getIdNotificacao() {
        return idNotificacao;
    }

    public void setIdNotificacao(Integer idNotificacao) {
        this.idNotificacao = idNotificacao;
    }

    public LocalDateTime getJanelaInicio() {
        return janelaInicio;
    }

    public void setJanelaInicio(LocalDateTime janelaInicio) {
        this.janelaInicio = janelaInicio;
    }

    public LocalDateTime getJanelaFim() {
        return janelaFim;
    }

    public void setJanelaFim(LocalDateTime janelaFim) {
        this.janelaFim = janelaFim;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getParametros() {
        return parametros;
    }

    public void setParametros(String parametros) {
        this.parametros = parametros;
    }
}
