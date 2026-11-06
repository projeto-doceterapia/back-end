package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.exception.ConfiguracaoNotificacaoInvalidaException;
import br.com.doceterapia.api.mapper.ConfiguracaoNotificacaoMapper;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
public class ConfiguracaoNotificacaoService {

    private final ConfiguracaoNotificacaoRepository repository;
    private final NotificacaoRepository notificacaoRepository;

    public ConfiguracaoNotificacaoService(ConfiguracaoNotificacaoRepository repository,
                                          NotificacaoRepository notificacaoRepository) {
        this.repository = repository;
        this.notificacaoRepository = notificacaoRepository;
    }

    /**
     * WhatsApp delivery needs both global switches on. With either off a
     * notification stays in the app only.
     */
    public static boolean enviaPorWhatsapp(ConfiguracaoNotificacao config) {
        return Boolean.TRUE.equals(config.getAvisosAutomaticosAtivo())
                && Boolean.TRUE.equals(config.getAssistenteAtivo());
    }

    public ConfiguracaoNotificacaoResponseDTO buscar() {
        return ConfiguracaoNotificacaoMapper.toResponseDTO(obterOuCriar());
    }

    /**
     * Pending notifications are discarded only on the transition to off: there
     * is no queue, so turning a switch back on never reactivates them.
     */
    @Transactional
    public ConfiguracaoNotificacaoResponseDTO atualizar(ConfiguracaoNotificacaoRequestDTO request) {
        validar(request);
        ConfiguracaoNotificacao entity = obterOuCriar();
        boolean enviavaAntes = enviaPorWhatsapp(entity);
        ConfiguracaoNotificacaoMapper.aplicar(entity, request);
        repository.save(entity);
        if (enviavaAntes && !enviaPorWhatsapp(entity)) {
            notificacaoRepository.descartarPendentes();
        }
        return ConfiguracaoNotificacaoMapper.toResponseDTO(entity);
    }

    /**
     * Returns the single settings row, creating it with the owner-confirmed
     * defaults when it does not exist, so callers (API and scheduled checks)
     * always find a usable configuration. If two rows ever exist, the oldest wins.
     */
    public ConfiguracaoNotificacao obterOuCriar() {
        return repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()
                .orElseGet(this::criarLinhaUnica);
    }

    // Serializes the first creation; enough for the single back-end instance.
    private synchronized ConfiguracaoNotificacao criarLinhaUnica() {
        return repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()
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
