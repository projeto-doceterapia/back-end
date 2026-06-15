package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.exception.NotificacaoNotFoundException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final NotificacaoMapper mapper;

    public NotificacaoService(NotificacaoRepository repository, NotificacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public NotificacaoResponseDTO cadastrar(NotificacaoRequestDTO request) {
        Notificacao entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponseDTO(entity);
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

    public NotificacaoResponseDTO marcarLida(Integer id) {
        Notificacao entity = repository.findById(id)
                .orElseThrow(NotificacaoNotFoundException::new);
        entity.setLida(true);
        repository.save(entity);
        return mapper.toResponseDTO(entity);
    }

    public List<NotificacaoResponseDTO> listarNaoLidas() {
        return repository.findByLidaFalseOrderByDataCriacaoDesc()
                .stream().map(mapper::toResponseDTO).toList();
    }
}
