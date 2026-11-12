package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.FormaEntrega;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "DTO para requisição de cadastro ou atualização de pedido")
public class PedidoRequestDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente associado ao pedido", example = "1")
    private Integer clienteId;

    @Schema(description = "Campo ignorado; o tipo é derivado do status", example = "ORCAMENTO")
    private TipoPedido tipoPedido;

    @Schema(description = "Status inicial do pedido", example = "ORCAMENTO")
    private StatusPedido statusPedido;

    @Schema(description = "Forma de entrega do pedido", example = "ENTREGA")
    private FormaEntrega formaEntrega;

    @Size(max = 255, message = "Endereço de entrega deve ter no máximo 255 caracteres")
    @Schema(description = "Endereço de entrega do pedido", example = "Rua das Flores, 123")
    private String enderecoEntrega;

    @Pattern(regexp = "\\d{8}", message = "CEP deve ter exatamente 8 dígitos")
    @Schema(description = "CEP com 8 dígitos, sem traço", example = "01001000", nullable = true)
    private String cep;

    @NotNull(message = "Data de entrega é obrigatória")
    @Schema(description = "Data de entrega do pedido", example = "2026-04-15", type = "string", format = "date")
    private LocalDate dataEntrega;

    @Schema(description = "Anotação sobre o pedido", example = "Bolo sem glúten")
    private String anotacao;

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public TipoPedido getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(TipoPedido tipoPedido) {
        this.tipoPedido = tipoPedido;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public FormaEntrega getFormaEntrega() {
        return formaEntrega;
    }

    public void setFormaEntrega(FormaEntrega formaEntrega) {
        this.formaEntrega = formaEntrega;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getAnotacao() {
        return anotacao;
    }

    public void setAnotacao(String anotacao) {
        this.anotacao = anotacao;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }
}
