package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import br.com.doceterapia.api.enums.TipoNotificacao;

@Schema(description = "DTO para requisição de cadastro ou atualização de notificação")
public class NotificacaoRequestDTO {

    @NotNull(message = "Tipo é obrigatório")
    @Schema(description = "Tipo da notificação", example = "ESTOQUE_BAIXO")
    private TipoNotificacao tipo;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    @Schema(description = "Título da notificação", example = "Estoque baixo de farinha")
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    @Schema(description = "Mensagem da notificação", example = "O insumo farinha de trigo está com quantidade abaixo do mínimo")
    private String mensagem;

    @Schema(description = "ID do pedido relacionado (opcional)", example = "1")
    private Integer fkPedido;

    @Schema(description = "ID do insumo relacionado (opcional)", example = "1")
    private Integer fkInsumo;

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
}
