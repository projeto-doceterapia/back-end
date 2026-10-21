package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfirmacaoEnvioRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoAgendaResponseDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para NotificacaoService agenda e confirmação")
class NotificacaoServiceAgendaTest {

    private static final LocalDateTime PRIMEIRO_ENVIO =
            LocalDateTime.of(2026, 10, 21, 10, 0);

    private final NotificacaoRepository repository = mock(NotificacaoRepository.class);
    private final NotificacaoMapper mapper = new NotificacaoMapper(mock(
            br.com.doceterapia.api.repository.PedidoRepository.class),
            mock(br.com.doceterapia.api.repository.InsumoRepository.class));
    private final Clock clock = Clock.fixed(
            PRIMEIRO_ENVIO.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private final ApplicationEventPublisher eventPublisher =
            mock(ApplicationEventPublisher.class);

    private final NotificacaoService service =
            new NotificacaoService(repository, mapper, eventPublisher, clock);

    private Notificacao pendente(Integer id) {
        Notificacao n = new Notificacao();
        n.setIdNotificacao(id);
        n.setTipo(TipoNotificacao.ESTOQUE_BAIXO);
        n.setTitulo("Estoque baixo");
        n.setMensagem("Farinha baixa");
        n.setLida(false);
        n.setStatusEnvio(StatusEnvioNotificacao.PENDENTE);
        n.setTemplateKey("estoque_baixo");
        n.setParametros("{\"insumo\":\"Farinha\"}");
        n.setJanelaInicio(LocalDateTime.of(2026, 10, 21, 8, 0));
        n.setJanelaFim(LocalDateTime.of(2026, 10, 21, 8, 2));
        return n;
    }

    @Test
    @DisplayName("Agenda retorna pendentes no intervalo pela query de sobreposição")
    void agendaRetornaPendentesNoIntervalo() {
        // Arrange
        LocalDateTime de = LocalDateTime.of(2026, 10, 21, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 10, 21, 23, 59);
        when(repository.findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
                eq(StatusEnvioNotificacao.PENDENTE), eq(ate), eq(de)))
                .thenReturn(List.of(pendente(1)));

        // Act
        List<NotificacaoAgendaResponseDTO> agenda = service.listarAgenda(de, ate);

        // Assert
        assertEquals(1, agenda.size());
        assertEquals(1, agenda.get(0).getIdNotificacao());
        assertEquals("estoque_baixo", agenda.get(0).getTemplateKey());
    }

    @Test
    @DisplayName("Agenda não retorna enviadas nem internas pois filtra por PENDENTE")
    void agendaFiltraPorPendente() {
        // Arrange: repository only returns PENDENTE; ENVIADA and NAO_ENVIAR never match
        LocalDateTime de = LocalDateTime.of(2026, 10, 21, 0, 0);
        LocalDateTime ate = LocalDateTime.of(2026, 10, 21, 23, 59);
        when(repository.findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
                eq(StatusEnvioNotificacao.PENDENTE), any(), any()))
                .thenReturn(List.of());

        // Act
        List<NotificacaoAgendaResponseDTO> agenda = service.listarAgenda(de, ate);

        // Assert
        assertEquals(0, agenda.size());
        verify(repository).findByStatusEnvioAndJanelaInicioLessThanEqualAndJanelaFimGreaterThanEqual(
                eq(StatusEnvioNotificacao.PENDENTE), any(), any());
    }

    @Test
    @DisplayName("Confirmação de envio grava status, horário e message id na primeira chamada")
    void confirmaPrimeiraVez() {
        // Arrange
        Notificacao n = pendente(5);
        when(repository.findById(5)).thenReturn(Optional.of(n));
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfirmacaoEnvioRequestDTO request = new ConfirmacaoEnvioRequestDTO();
        request.setStatusEnvio(StatusEnvioNotificacao.ENVIADA);
        request.setCanalMessageId("wamid.1");

        // Act
        NotificacaoResponseDTO resultado = service.confirmarEnvio(5, request);

        // Assert
        assertEquals(StatusEnvioNotificacao.ENVIADA, resultado.getStatusEnvio());
        assertEquals(PRIMEIRO_ENVIO, resultado.getEnviadoEm());
        assertEquals("wamid.1", resultado.getCanalMessageId());
    }

    @Test
    @DisplayName("Confirmação repetida mantém a primeira confirmação (idempotente)")
    void confirmaRepetidaMantemPrimeira() {
        // Arrange: notification already sent at a prior time
        Notificacao n = pendente(5);
        n.setStatusEnvio(StatusEnvioNotificacao.ENVIADA);
        LocalDateTime enviadoAntes = LocalDateTime.of(2026, 10, 21, 9, 0);
        n.setEnviadoEm(enviadoAntes);
        n.setCanalMessageId("wamid.original");
        when(repository.findById(5)).thenReturn(Optional.of(n));

        ConfirmacaoEnvioRequestDTO request = new ConfirmacaoEnvioRequestDTO();
        request.setStatusEnvio(StatusEnvioNotificacao.ENVIADA);
        request.setCanalMessageId("wamid.novo");

        // Act
        NotificacaoResponseDTO resultado = service.confirmarEnvio(5, request);

        // Assert
        assertEquals(enviadoAntes, resultado.getEnviadoEm());
        assertEquals("wamid.original", resultado.getCanalMessageId());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Confirmação com status inválido é rejeitada")
    void confirmaStatusInvalido() {
        // Arrange
        Notificacao n = pendente(5);
        when(repository.findById(5)).thenReturn(Optional.of(n));

        ConfirmacaoEnvioRequestDTO request = new ConfirmacaoEnvioRequestDTO();
        request.setStatusEnvio(StatusEnvioNotificacao.PENDENTE);

        // Act & Assert
        assertThrows(StatusTransitionInvalidException.class,
                () -> service.confirmarEnvio(5, request));
        verify(repository, never()).save(any());
    }
}
