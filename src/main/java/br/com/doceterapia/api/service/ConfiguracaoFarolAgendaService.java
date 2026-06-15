package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoFarolRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoFarolResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.exception.ConfiguracaoFarolAlreadyExistsException;
import br.com.doceterapia.api.exception.NotificacaoNotFoundException;
import br.com.doceterapia.api.mapper.ConfiguracaoFarolMapper;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracaoFarolAgendaService {

    private final ConfiguracaoFarolAgendaRepository repository;

    public ConfiguracaoFarolAgendaService(ConfiguracaoFarolAgendaRepository repository) {
        this.repository = repository;
    }

    public ConfiguracaoFarolResponseDTO criar(ConfiguracaoFarolRequestDTO request) {
        if (repository.count() > 0) {
            throw new ConfiguracaoFarolAlreadyExistsException();
        }

        validarLimites(request);

        ConfiguracaoFarolAgenda entity = ConfiguracaoFarolMapper.toEntity(request);
        repository.save(entity);
        return ConfiguracaoFarolMapper.toResponseDTO(entity);
    }

    public ConfiguracaoFarolResponseDTO buscar() {
        ConfiguracaoFarolAgenda entity = repository.findAll().stream()
                .findFirst()
                .orElseThrow(NotificacaoNotFoundException::new);
        return ConfiguracaoFarolMapper.toResponseDTO(entity);
    }

    public ConfiguracaoFarolResponseDTO atualizar(ConfiguracaoFarolRequestDTO request) {
        ConfiguracaoFarolAgenda entity = repository.findAll().stream()
                .findFirst()
                .orElseThrow(NotificacaoNotFoundException::new);

        validarLimites(request);

        entity.setLimiteVerde(request.getLimiteVerde());
        entity.setLimiteAmarelo(request.getLimiteAmarelo());
        entity.setLimiteVermelho(request.getLimiteVermelho());
        repository.save(entity);
        return ConfiguracaoFarolMapper.toResponseDTO(entity);
    }

    private void validarLimites(ConfiguracaoFarolRequestDTO request) {
        if (request.getLimiteAmarelo() <= request.getLimiteVerde()) {
            throw new IllegalArgumentException("Limite amarelo deve ser maior que limite verde");
        }
        if (request.getLimiteVermelho() <= request.getLimiteAmarelo()) {
            throw new IllegalArgumentException("Limite vermelho deve ser maior que limite amarelo");
        }
    }
}
