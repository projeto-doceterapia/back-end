package school.sptech.projetoindividual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de cliente cadastrado")
public class ClienteResponseDTO {

    @Schema(description = "ID único do cliente", example = "1")
    private Integer idCliente;
    @Schema(description = "Nome completo do cliente", example = "João Silva")
    private String nomeCompleto;
    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321")
    private String telefone;
    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;

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
}
