package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;

import java.time.LocalDateTime;

@Schema(description = "DTO para resposta de notificação cadastrada ou atualizada")
public class NotificacaoResponseDTO {

    @Schema(description = "ID único da notificação", example = "1")
    private Integer idNotificacao;

    @Schema(description = "Tipo da notificação", example = "ESTOQUE_BAIXO")
    private TipoNotificacao tipo;

    @Schema(description = "Título da notificação", example = "Estoque baixo de farinha")
    private String titulo;

    @Schema(description = "Mensagem da notificação", example = "O insumo farinha de trigo está com quantidade abaixo do mínimo")
    private String mensagem;

    @Schema(description = "Data de criação da notificação", example = "2026-06-14T10:30:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Indica se a notificação foi lida", example = "false")
    private Boolean lida;

    @Schema(description = "ID do pedido relacionado (opcional)", example = "1")
    private Integer fkPedido;

    @Schema(description = "ID do insumo relacionado (opcional)", example = "1")
    private Integer fkInsumo;

    @Schema(description = "Status de envio da notificação ao WhatsApp", example = "NAO_ENVIAR")
    private StatusEnvioNotificacao statusEnvio;

    @Schema(description = "Início da janela de entrega", example = "2026-06-14T08:00:00")
    private LocalDateTime janelaInicio;

    @Schema(description = "Fim da janela de entrega", example = "2026-06-14T08:10:00")
    private LocalDateTime janelaFim;

    @Schema(description = "Chave do template de mensagem", example = "estoque_baixo")
    private String templateKey;

    @Schema(description = "Parâmetros do template em JSON", example = "{\"insumo\":\"Farinha\"}")
    private String parametros;

    @Schema(description = "Momento em que a notificação foi enviada", example = "2026-06-14T08:05:00")
    private LocalDateTime enviadoEm;

    @Schema(description = "ID da mensagem no canal de envio", example = "wamid.ABC123")
    private String canalMessageId;

    public Integer getIdNotificacao() {
        return idNotificacao;
    }

    public void setIdNotificacao(Integer idNotificacao) {
        this.idNotificacao = idNotificacao;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public Integer getFkPedido() {
        return fkPedido;
    }

    public void setFkPedido(Integer fkPedido) {
        this.fkPedido = fkPedido;
    }

    public Integer getFkInsumo() {
        return fkInsumo;
    }

    public void setFkInsumo(Integer fkInsumo) {
        this.fkInsumo = fkInsumo;
    }

    public StatusEnvioNotificacao getStatusEnvio() {
        return statusEnvio;
    }

    public void setStatusEnvio(StatusEnvioNotificacao statusEnvio) {
        this.statusEnvio = statusEnvio;
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

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public String getCanalMessageId() {
        return canalMessageId;
    }

    public void setCanalMessageId(String canalMessageId) {
        this.canalMessageId = canalMessageId;
    }
}
