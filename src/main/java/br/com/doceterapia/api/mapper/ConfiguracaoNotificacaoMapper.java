package br.com.doceterapia.api.mapper;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfiguracaoNotificacaoMapper {

    private ConfiguracaoNotificacaoMapper() {
    }

    public static void aplicar(ConfiguracaoNotificacao entity, ConfiguracaoNotificacaoRequestDTO request) {
        entity.setInicioExpediente(request.getInicioExpediente());
        entity.setFimExpediente(request.getFimExpediente());
        entity.setDiasExpediente(diasParaCsv(request.getDiasExpediente()));
        entity.setHorarioResumoInicio(request.getHorarioResumoInicio());
        entity.setHorarioResumoFim(request.getHorarioResumoFim());
        entity.setDiasAntecedencia(request.getDiasAntecedencia());
        entity.setResumoInicioDiaAtivo(request.getResumoInicioDiaAtivo());
        entity.setResumoFimDiaAtivo(request.getResumoFimDiaAtivo());
        entity.setAlertaEstoqueBaixoAtivo(request.getEstoqueBaixoAtivo());
        entity.setAlertaDiaLotadoAtivo(request.getDiaLotadoAtivo());
    }

    public static ConfiguracaoNotificacaoResponseDTO toResponseDTO(ConfiguracaoNotificacao entity) {
        ConfiguracaoNotificacaoResponseDTO dto = new ConfiguracaoNotificacaoResponseDTO();
        dto.setInicioExpediente(entity.getInicioExpediente());
        dto.setFimExpediente(entity.getFimExpediente());
        dto.setDiasExpediente(csvParaDias(entity.getDiasExpediente()));
        dto.setHorarioResumoInicio(entity.getHorarioResumoInicio());
        dto.setHorarioResumoFim(entity.getHorarioResumoFim());
        dto.setDiasAntecedencia(entity.getDiasAntecedencia());
        dto.setResumoInicioDiaAtivo(entity.getResumoInicioDiaAtivo());
        dto.setResumoFimDiaAtivo(entity.getResumoFimDiaAtivo());
        dto.setEstoqueBaixoAtivo(entity.getAlertaEstoqueBaixoAtivo());
        dto.setDiaLotadoAtivo(entity.getAlertaDiaLotadoAtivo());
        return dto;
    }

    // The entity stores working days as ISO-8601 weekday numbers (Monday=1..Sunday=7)
    // in a comma-separated string, while the API exposes DayOfWeek names.
    public static String diasParaCsv(List<DayOfWeek> dias) {
        return dias.stream()
                .sorted()
                .map(dia -> String.valueOf(dia.getValue()))
                .collect(Collectors.joining(","));
    }

    public static List<DayOfWeek> csvParaDias(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .map(valor -> DayOfWeek.of(Integer.parseInt(valor)))
                .collect(Collectors.toList());
    }
}
