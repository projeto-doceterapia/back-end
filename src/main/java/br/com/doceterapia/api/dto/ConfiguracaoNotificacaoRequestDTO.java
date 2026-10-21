package br.com.doceterapia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "DTO para requisição de atualização da configuração de notificações")
public class ConfiguracaoNotificacaoRequestDTO {

    @NotNull(message = "Início do expediente é obrigatório")
    @Schema(description = "Horário de início do expediente (HH:mm)", example = "08:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime inicioExpediente;

    @NotNull(message = "Fim do expediente é obrigatório")
    @Schema(description = "Horário de fim do expediente (HH:mm)", example = "18:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime fimExpediente;

    @NotEmpty(message = "Informe ao menos um dia de expediente")
    @Schema(description = "Dias de expediente (nomes de DayOfWeek)",
            example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\", \"SATURDAY\"]")
    private List<DayOfWeek> diasExpediente;

    @NotNull(message = "Horário do resumo de início do dia é obrigatório")
    @Schema(description = "Horário do resumo de início do dia (HH:mm)", example = "08:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioResumoInicio;

    @NotNull(message = "Horário do resumo de fim do dia é obrigatório")
    @Schema(description = "Horário do resumo de fim do dia (HH:mm)", example = "17:30", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioResumoFim;

    @NotNull(message = "Dias de antecedência é obrigatório")
    @Min(value = 1, message = "Dias de antecedência deve estar entre 1 e 14")
    @Max(value = 14, message = "Dias de antecedência deve estar entre 1 e 14")
    @Schema(description = "Dias de antecedência do resumo de fim do dia", example = "3")
    private Integer diasAntecedencia;

    @NotNull(message = "Flag do resumo de início do dia é obrigatória")
    @Schema(description = "Resumo de início do dia ativo", example = "true")
    private Boolean resumoInicioDiaAtivo;

    @NotNull(message = "Flag do resumo de fim do dia é obrigatória")
    @Schema(description = "Resumo de fim do dia ativo", example = "true")
    private Boolean resumoFimDiaAtivo;

    @NotNull(message = "Flag de estoque baixo é obrigatória")
    @Schema(description = "Alerta de estoque baixo ativo", example = "true")
    private Boolean estoqueBaixoAtivo;

    @NotNull(message = "Flag de dia lotado é obrigatória")
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
