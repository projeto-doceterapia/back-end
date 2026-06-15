package br.com.doceterapia.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CategoriaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoriaInsumo;

    @Column(length = 100)
    private String nome;

    private String descricao;

    public Integer getIdCategoriaInsumo() {
        return idCategoriaInsumo;
    }

    public void setIdCategoriaInsumo(Integer idCategoriaInsumo) {
        this.idCategoriaInsumo = idCategoriaInsumo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
