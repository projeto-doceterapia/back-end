package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.exception.ConfiguracaoNotificacaoInvalidaException;
import br.com.doceterapia.api.mapper.ConfiguracaoNotificacaoMapper;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class ConfiguracaoNotificacaoService {

    private final ConfiguracaoNotificacaoRepository repository;

    public ConfiguracaoNotificacaoService(ConfiguracaoNotificacaoRepository repository) {
        this.repository = repository;
    }

    public ConfiguracaoNotificacaoResponseDTO buscar() {
        return ConfiguracaoNotificacaoMapper.toResponseDTO(obterOuCriar());
    }

    public ConfiguracaoNotificacaoResponseDTO atualizar(ConfiguracaoNotificacaoRequestDTO request) {
        validar(request);
        ConfiguracaoNotificacao entity = obterOuCriar();
        ConfiguracaoNotificacaoMapper.aplicar(entity, request);
        repository.save(entity);
        return ConfiguracaoNotificacaoMapper.toResponseDTO(entity);
    }

    /**
     * Returns the single settings row, creating it with the owner-confirmed
     * defaults when it does not exist, so callers (API and scheduled checks)
     * always find a usable configuration.
     */
    public ConfiguracaoNotificacao obterOuCriar() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> repository.save(new ConfiguracaoNotificacao()));
    }

    private void validar(ConfiguracaoNotificacaoRequestDTO request) {
        LocalTime inicio = request.getInicioExpediente();
        LocalTime fim = request.getFimExpediente();

        if (!fim.isAfter(inicio)) {
            throw new ConfiguracaoNotificacaoInvalidaException(
                    "O fim do expediente deve ser depois do início");
        }

        if (!dentroDoExpediente(request.getHorarioResumoInicio(), inicio, fim)) {
            throw new ConfiguracaoNotificacaoInvalidaException(
                    "O horário do resumo de início deve estar dentro do expediente");
        }

        if (!dentroDoExpediente(request.getHorarioResumoFim(), inicio, fim)) {
            throw new ConfiguracaoNotificacaoInvalidaException(
                    "O horário do resumo de fim deve estar dentro do expediente");
        }
    }

    private boolean dentroDoExpediente(LocalTime horario, LocalTime inicio, LocalTime fim) {
        return !horario.isBefore(inicio) && !horario.isAfter(fim);
    }
}
