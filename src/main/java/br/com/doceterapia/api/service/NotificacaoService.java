package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfirmacaoEnvioRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoAgendaResponseDTO;
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.event.NotificacaoCriadaEvent;
import br.com.doceterapia.api.event.NotificacaoLidaEvent;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.exception.NotificacaoNotFoundException;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final NotificacaoMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public NotificacaoService(NotificacaoRepository repository, NotificacaoMapper mapper,
                              ApplicationEventPublisher eventPublisher, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
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

    /**
     * Returns pending notifications whose delivery window overlaps the range.
     * Filtering by PENDENTE excludes in-app only notifications (NAO_ENVIAR) and
     * notifications already confirmed as sent.
     */
    public List<NotificacaoAgendaResponseDTO> listarAgenda(LocalDateTime de, LocalDateTime ate) {
        return repository
                .findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
                        StatusEnvioNotificacao.PENDENTE, ate, de)
                .stream()
                .map(mapper::toAgendaResponseDTO)
                .toList();
    }

    /**
     * Records the delivery outcome. It is idempotent: once a notification has
     * left PENDENTE it keeps its first confirmation, including the send time.
     */
    public NotificacaoResponseDTO confirmarEnvio(Integer id, ConfirmacaoEnvioRequestDTO request) {
        Notificacao entity = repository.findById(id)
                .orElseThrow(NotificacaoNotFoundException::new);

        StatusEnvioNotificacao novoStatus = request.getStatusEnvio();
        if (novoStatus != StatusEnvioNotificacao.ENVIADA
                && novoStatus != StatusEnvioNotificacao.FALHA) {
            throw new StatusTransitionInvalidException(
                    "A confirmação de envio só aceita os status ENVIADA ou FALHA");
        }

        if (entity.getStatusEnvio() == StatusEnvioNotificacao.PENDENTE) {
            entity.setStatusEnvio(novoStatus);
            entity.setEnviadoEm(LocalDateTime.now(clock));
            entity.setCanalMessageId(request.getCanalMessageId());
            repository.save(entity);
        }

        return mapper.toResponseDTO(entity);
    }
}
