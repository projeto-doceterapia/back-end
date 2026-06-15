package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de cliente cadastrado")
public class ClienteResponseDTO {

    @Schema(description = "ID único do cliente", example = "1")
    private Integer idCliente;
    @Schema(description = "Nome do cliente", example = "João Silva")
    private String nome;
    @Schema(description = "Telefone do cliente", example = "(11) 98765-4321")
    private String telefone;
    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;
    @Schema(description = "Tipo de pessoa", example = "FISICA")
    private TipoPessoa tipoPessoa;
    @Schema(description = "Classificação do cliente", example = "PADRAO")
    private ClassificacaoCliente classificacaoCliente;
    @Schema(description = "Status do cliente", example = "ATIVO")
    private StatusAtivo status;
    @Schema(description = "Observação sobre o cliente", example = "Cliente prefere contato por WhatsApp")
    private String observacao;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

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
