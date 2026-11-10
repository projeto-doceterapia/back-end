package br.com.doceterapia.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class ConfiguracaoPrecificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfiguracaoPrecificacao;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal margemPadrao = BigDecimal.ZERO.setScale(2);

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal percentualIndiretos = BigDecimal.ZERO.setScale(2);

    public Integer getIdConfiguracaoPrecificacao() {
        return idConfiguracaoPrecificacao;
    }

    public void setIdConfiguracaoPrecificacao(Integer idConfiguracaoPrecificacao) {
        this.idConfiguracaoPrecificacao = idConfiguracaoPrecificacao;
    }

    public BigDecimal getMargemPadrao() {
        return margemPadrao;
    }

    public void setMargemPadrao(BigDecimal margemPadrao) {
        this.margemPadrao = margemPadrao;
    }

    public BigDecimal getPercentualIndiretos() {
        return percentualIndiretos;
    }

    public void setPercentualIndiretos(BigDecimal percentualIndiretos) {
        this.percentualIndiretos = percentualIndiretos;
    }
}
