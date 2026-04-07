package school.sptech.projetoindividual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "DTO para resposta de pedido com as informações completas do cliente")
public class PedidoWithClienteResponseDTO {

    @Schema(description = "ID único do pedido", example = "1")
    private Integer idPedido;
    @Schema(description = "ID do cliente associado ao pedido", example = "1")
    private Integer fkCliente;
    @Schema(description = "Descrição do pedido", example = "Bolo de chocolate com 3 andares")
    private String descricao;
    @Schema(description = "Data de entrega do pedido", example = "2026-04-15", type = "string", format = "date")
    private LocalDate dataEntrega;
    @Schema(description = "Valor do pedido em reais", example = "150.50")
    private Double valor;
    @Schema(description = "Status de conclusão do pedido", example = "false")
    private Boolean statusConcluido;
    @Schema(description = "ID único do cliente", example = "1")
    private Integer idCliente;
    @Schema(description = "Nome completo do cliente", example = "João Silva")
    private String nomeCompleto;
    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321")
    private String telefone;
    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;

    public PedidoWithClienteResponseDTO(Integer idPedido, Integer fkCliente, String descricao, LocalDate dataEntrega, Double valor, Boolean statusConcluido, Integer idCliente, String nomeCompleto, String telefone, String endereco) {
        this.idPedido = idPedido;
        this.fkCliente = fkCliente;
        this.descricao = descricao;
        this.dataEntrega = dataEntrega;
        this.valor = valor;
        this.statusConcluido = statusConcluido;
        this.idCliente = idCliente;
        this.nomeCompleto = nomeCompleto;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getFkCliente() {
        return fkCliente;
    }

    public void setFkCliente(Integer fkCliente) {
        this.fkCliente = fkCliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Boolean getStatusConcluido() {
        return statusConcluido;
    }

    public void setStatusConcluido(Boolean statusConcluido) {
        this.statusConcluido = statusConcluido;
    }
}
