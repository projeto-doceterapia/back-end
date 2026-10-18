package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "DTO para resposta da configuração de notificações")
public class ConfiguracaoNotificacaoResponseDTO {

    @Schema(description = "Horário de início do expediente (HH:mm)", example = "08:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime inicioExpediente;

    @Schema(description = "Horário de fim do expediente (HH:mm)", example = "18:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime fimExpediente;

    @Schema(description = "Dias de expediente (nomes de DayOfWeek)",
            example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\", \"SATURDAY\"]")
    private List<DayOfWeek> diasExpediente;

    @Schema(description = "Horário do resumo de início do dia (HH:mm)", example = "08:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioResumoInicio;

    @Schema(description = "Horário do resumo de fim do dia (HH:mm)", example = "17:30", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioResumoFim;

    @Schema(description = "Dias de antecedência do resumo de fim do dia", example = "3")
    private Integer diasAntecedencia;

    @Schema(description = "Resumo de início do dia ativo", example = "true")
    private Boolean resumoInicioDiaAtivo;

    @Schema(description = "Resumo de fim do dia ativo", example = "true")
    private Boolean resumoFimDiaAtivo;

    @Schema(description = "Alerta de estoque baixo ativo", example = "true")
    private Boolean estoqueBaixoAtivo;

    @Schema(description = "Alerta de dia lotado ativo", example = "true")
    private Boolean diaLotadoAtivo;

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

    public List<DayOfWeek> getDiasExpediente() {
        return diasExpediente;
    }

    public void setDiasExpediente(List<DayOfWeek> diasExpediente) {
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

    public Boolean getEstoqueBaixoAtivo() {
        return estoqueBaixoAtivo;
    }

    public void setEstoqueBaixoAtivo(Boolean estoqueBaixoAtivo) {
        this.estoqueBaixoAtivo = estoqueBaixoAtivo;
    }

    public Boolean getDiaLotadoAtivo() {
        return diaLotadoAtivo;
    }

    public void setDiaLotadoAtivo(Boolean diaLotadoAtivo) {
        this.diaLotadoAtivo = diaLotadoAtivo;
    }
}
