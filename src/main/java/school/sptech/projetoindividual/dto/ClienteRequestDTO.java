package school.sptech.projetoindividual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para requisição de cadastro de cliente")
public class ClienteRequestDTO {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 45, message = "Nome completo não pode exceder 45 caracteres")
    @Schema(description = "Nome completo do cliente", example = "João Silva", maxLength = 45)
    private String nomeCompleto;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 45, message = "Telefone não pode exceder 45 caracteres")
    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321", maxLength = 45)
    private String telefone;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 255, message = "Endereço não pode exceder 255 caracteres")
    @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123, Apto 45, São Paulo, SP", maxLength = 255)
    private String endereco;

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
