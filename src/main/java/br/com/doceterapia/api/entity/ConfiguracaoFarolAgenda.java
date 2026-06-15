package br.com.doceterapia.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ConfiguracaoFarolAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfiguracaoFarol;

    @Column(nullable = false)
    private Integer limiteVerde;

    @Column(nullable = false)
    private Integer limiteAmarelo;

    @Column(nullable = false)
    private Integer limiteVermelho;

    public Integer getIdConfiguracaoFarol() {
        return idConfiguracaoFarol;
    }

    public void setIdConfiguracaoFarol(Integer idConfiguracaoFarol) {
        this.idConfiguracaoFarol = idConfiguracaoFarol;
    }

    public Integer getLimiteVerde() {
        return limiteVerde;
    }

    public void setLimiteVerde(Integer limiteVerde) {
        this.limiteVerde = limiteVerde;
    }

    public Integer getLimiteAmarelo() {
        return limiteAmarelo;
    }

    public void setLimiteAmarelo(Integer limiteAmarelo) {
        this.limiteAmarelo = limiteAmarelo;
    }

    public Integer getLimiteVermelho() {
        return limiteVermelho;
    }

    public void setLimiteVermelho(Integer limiteVermelho) {
        this.limiteVermelho = limiteVermelho;
    }
}
