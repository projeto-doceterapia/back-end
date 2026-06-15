package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para requisição de cadastro de cliente")
public class ClienteRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome não pode exceder 150 caracteres")
    @Schema(description = "Nome do cliente", example = "João Silva", maxLength = 150)
    private String nome;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321", maxLength = 20)
    private String telefone;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 255, message = "Endereço não pode exceder 255 caracteres")
    @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123, Apto 45, São Paulo, SP", maxLength = 255)
    private String endereco;

    @NotNull(message = "Tipo de pessoa é obrigatório")
    @Schema(description = "Tipo de pessoa", example = "FISICA")
    private TipoPessoa tipoPessoa;

    @NotNull(message = "Classificação do cliente é obrigatório")
    @Schema(description = "Classificação do cliente", example = "PADRAO")
    private ClassificacaoCliente classificacaoCliente;

    @NotNull(message = "Status é obrigatório")
    @Schema(description = "Status do cliente", example = "ATIVO")
    private StatusAtivo status;

    @Schema(description = "Observação sobre o cliente", example = "Cliente prefere contato por WhatsApp")
    private String observacao;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public ClassificacaoCliente getClassificacaoCliente() {
        return classificacaoCliente;
    }

    public void setClassificacaoCliente(ClassificacaoCliente classificacaoCliente) {
        this.classificacaoCliente = classificacaoCliente;
    }

    public StatusAtivo getStatus() {
        return status;
    }

    public void setStatus(StatusAtivo status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
