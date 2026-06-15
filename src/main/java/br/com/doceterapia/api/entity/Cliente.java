package br.com.doceterapia.api.entity;

import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Integer idCliente;

    private String nome;

    @Column(length = 20)
    private String telefone;

    private String endereco;

    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    @Enumerated(EnumType.STRING)
    private ClassificacaoCliente classificacaoCliente;

    @Enumerated(EnumType.STRING)
    private StatusAtivo status;

    @Column(columnDefinition = "TEXT")
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
