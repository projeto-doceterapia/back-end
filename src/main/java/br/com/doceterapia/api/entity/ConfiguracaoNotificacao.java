package br.com.doceterapia.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalTime;

@Entity
public class ConfiguracaoNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfiguracaoNotificacao;

    @Column(nullable = false)
    private LocalTime inicioExpediente = LocalTime.of(8, 0);

    @Column(nullable = false)
    private LocalTime fimExpediente = LocalTime.of(18, 0);

    // ISO-8601 weekday numbers (1=Monday .. 7=Sunday) separated by commas.
    // Default Mon-Sat means the owner does not work on Sundays.
    @Column(length = 20, nullable = false)
    private String diasExpediente = "1,2,3,4,5,6";

    @Column(nullable = false)
    private LocalTime horarioResumoInicio = LocalTime.of(8, 0);

    @Column(nullable = false)
    private LocalTime horarioResumoFim = LocalTime.of(17, 30);

    @Column(nullable = false)
    private Integer diasAntecedencia = 3;

    @Column(nullable = false)
    private Boolean resumoInicioDiaAtivo = true;

    @Column(nullable = false)
    private Boolean resumoFimDiaAtivo = true;

    @Column(nullable = false)
    private Boolean alertaEstoqueBaixoAtivo = true;

    @Column(nullable = false)
    private Boolean alertaDiaLotadoAtivo = true;

    public Integer getIdConfiguracaoNotificacao() {
        return idConfiguracaoNotificacao;
    }

    public void setIdConfiguracaoNotificacao(Integer idConfiguracaoNotificacao) {
        this.idConfiguracaoNotificacao = idConfiguracaoNotificacao;
    }

    public LocalTime getInicioExpediente() {
        return inicioExpediente;
    }

    public void setInicioExpediente(LocalTime inicioExpediente) {
        this.inicioExpediente = inicioExpediente;
    }

    public LocalTime getFimExpediente() {
        return fimExpediente;
    }

    public void setFimExpediente(LocalTime fimExpediente) {
        this.fimExpediente = fimExpediente;
    }

    public String getDiasExpediente() {
        return diasExpediente;
    }

    public void setDiasExpediente(String diasExpediente) {
        this.diasExpediente = diasExpediente;
    }

    public LocalTime getHorarioResumoInicio() {
        return horarioResumoInicio;
    }

    public void setHorarioResumoInicio(LocalTime horarioResumoInicio) {
        this.horarioResumoInicio = horarioResumoInicio;
    }

    public LocalTime getHorarioResumoFim() {
        return horarioResumoFim;
    }

    public void setHorarioResumoFim(LocalTime horarioResumoFim) {
        this.horarioResumoFim = horarioResumoFim;
    }

    public Integer getDiasAntecedencia() {
        return diasAntecedencia;
    }

    public void setDiasAntecedencia(Integer diasAntecedencia) {
        this.diasAntecedencia = diasAntecedencia;
    }

    public Boolean getResumoInicioDiaAtivo() {
        return resumoInicioDiaAtivo;
    }

    public void setResumoInicioDiaAtivo(Boolean resumoInicioDiaAtivo) {
        this.resumoInicioDiaAtivo = resumoInicioDiaAtivo;
    }

    public Boolean getResumoFimDiaAtivo() {
        return resumoFimDiaAtivo;
    }

    public void setResumoFimDiaAtivo(Boolean resumoFimDiaAtivo) {
        this.resumoFimDiaAtivo = resumoFimDiaAtivo;
    }

    public Boolean getAlertaEstoqueBaixoAtivo() {
        return alertaEstoqueBaixoAtivo;
    }

    public void setAlertaEstoqueBaixoAtivo(Boolean alertaEstoqueBaixoAtivo) {
        this.alertaEstoqueBaixoAtivo = alertaEstoqueBaixoAtivo;
    }

    public Boolean getAlertaDiaLotadoAtivo() {
        return alertaDiaLotadoAtivo;
    }

    public void setAlertaDiaLotadoAtivo(Boolean alertaDiaLotadoAtivo) {
        this.alertaDiaLotadoAtivo = alertaDiaLotadoAtivo;
    }
}
