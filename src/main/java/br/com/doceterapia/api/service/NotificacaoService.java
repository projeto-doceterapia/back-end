package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.event.NotificacaoCriadaEvent;
import br.com.doceterapia.api.event.NotificacaoLidaEvent;
import br.com.doceterapia.api.exception.NotificacaoNotFoundException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final NotificacaoMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificacaoService(NotificacaoRepository repository, NotificacaoMapper mapper,
                              ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public NotificacaoResponseDTO cadastrar(NotificacaoRequestDTO request) {
        Notificacao entity = mapper.toEntity(request);
        repository.save(entity);
        NotificacaoResponseDTO response = mapper.toResponseDTO(entity);
        eventPublisher.publishEvent(new NotificacaoCriadaEvent(response));
        return response;
    }

    public List<NotificacaoResponseDTO> listarTodas() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
    }

    public NotificacaoResponseDTO buscarPorId(Integer id) {
        Notificacao entity = repository.findById(id)
                .orElseThrow(NotificacaoNotFoundException::new);
        return mapper.toResponseDTO(entity);
    }

    public NotificacaoResponseDTO atualizar(Integer id, NotificacaoRequestDTO request) {
        Notificacao entity = repository.findById(id)
                .orElseThrow(NotificacaoNotFoundException::new);

        Notificacao updated = mapper.toEntity(request);
        updated.setIdNotificacao(entity.getIdNotificacao());

        repository.save(updated);
        return mapper.toResponseDTO(updated);
    }

    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotificacaoNotFoundException();
        }
        repository.deleteById(id);
    }

    @Transactional
    public NotificacaoResponseDTO marcarLida(Integer id) {
        Notificacao entity = repository.findById(id)
                .orElseThrow(NotificacaoNotFoundException::new);
        entity.setLida(true);
        repository.save(entity);
        eventPublisher.publishEvent(new NotificacaoLidaEvent(entity.getIdNotificacao()));
        return mapper.toResponseDTO(entity);
    }

    public List<NotificacaoResponseDTO> listarNaoLidas() {
        return repository.findByLidaFalseOrderByDataCriacaoDesc()
                .stream().map(mapper::toResponseDTO).toList();
    }
}
