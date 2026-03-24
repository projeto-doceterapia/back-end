package school.sptech.projetoindividual.dto;

import java.time.LocalDate;

public class PedidoWithClienteResponseDTO {

    private Integer idPedido;
    private Integer fkCliente;
    private String descricao;
    private LocalDate dataEntrega;
    private Double valor;
    private Boolean statusConcluido;
    private Integer idCliente;
    private String nomeCompleto;
    private String telefone;
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
