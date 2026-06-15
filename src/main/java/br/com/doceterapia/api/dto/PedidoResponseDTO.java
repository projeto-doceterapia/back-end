package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.FormaEntrega;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO para resposta de pedido cadastrado ou atualizado")
public class PedidoResponseDTO {

    @Schema(description = "ID único do pedido", example = "1")
    private Integer idPedido;

    @Schema(description = "ID do cliente associado ao pedido", example = "1")
    private Integer clienteId;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String nomeCliente;

    @Schema(description = "Tipo do pedido", example = "ORCAMENTO")
    private TipoPedido tipoPedido;

    @Schema(description = "Status do pedido", example = "ORCAMENTO")
    private StatusPedido statusPedido;

    @Schema(description = "Forma de entrega do pedido", example = "ENTREGA")
    private FormaEntrega formaEntrega;

    @Schema(description = "Endereço de entrega do pedido", example = "Rua das Flores, 123")
    private String enderecoEntrega;

    @Schema(description = "Data de entrega do pedido", example = "2026-04-15", type = "string", format = "date")
    private LocalDate dataEntrega;

    @Schema(description = "Anotação sobre o pedido", example = "Bolo sem glúten")
    private String anotacao;

    @Schema(description = "Data de criação do pedido", example = "2026-04-10T14:30:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data de confirmação do pedido", example = "2026-04-11T10:00:00")
    private LocalDateTime dataConfirmacao;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }
}
