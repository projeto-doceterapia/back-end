package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.exception.NotificacaoTesteCooldownException;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para NotificacaoTesteService")
class NotificacaoTesteServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 18, 10, 0, 30);

    @Mock
    private NotificacaoRepository repository;

    @Mock
    private CriacaoNotificacaoService criacaoService;

    @Mock
    private NotificacaoMapper mapper;

    private NotificacaoTesteService servico() {
        Clock clock = Clock.fixed(AGORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        return new NotificacaoTesteService(repository, criacaoService, mapper, clock, 300);
    }

    private void ultimoTesteEm(LocalDateTime momento) {
        Notificacao ultimo = new Notificacao();
        ultimo.setDataCriacao(momento);
        when(repository.findFirstByChaveUnicidadeStartingWithOrderByDataCriacaoDesc("TESTE:"))
                .thenReturn(Optional.of(ultimo));
    }

    @Test
    @DisplayName("Primeiro teste cria a notificação com o template de alerta operacional e janela imediata")
    void primeiroTesteCria() {
        // Arrange
        Notificacao criada = new Notificacao();
        NotificacaoResponseDTO resposta = new NotificacaoResponseDTO();
        when(criacaoService.criar(any())).thenReturn(criada);
        when(mapper.toResponseDTO(criada)).thenReturn(resposta);

        // Act
        NotificacaoResponseDTO resultado = servico().enviar();

        // Assert
        assertSame(resposta, resultado);
        ArgumentCaptor<ComandoNotificacao> comando = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService).criar(comando.capture());
        assertEquals(TipoNotificacao.ALERTA_OPERACIONAL, comando.getValue().tipo());
        assertEquals("alerta_operacional", comando.getValue().templateKey());
        assertEquals(ComandoNotificacao.Janela.AGORA, comando.getValue().janela());
        assertTrue(comando.getValue().chaveUnicidade().startsWith("TESTE:"));
    }

    @Test
    @DisplayName("O parâmetro mensagem é preenchido e sem ponto final, que já está no corpo do template")
    void testePreencheParametroMensagem() {
        // Arrange
        when(criacaoService.criar(any())).thenReturn(new Notificacao());

        // Act
        servico().enviar();

        // Assert
        ArgumentCaptor<ComandoNotificacao> comando = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService).criar(comando.capture());
        ParametrosAlertaOperacional parametros =
                assertInstanceOf(ParametrosAlertaOperacional.class, comando.getValue().parametros());
        assertFalse(parametros.mensagem().isBlank());
        assertFalse(parametros.mensagem().endsWith("."));
        assertEquals(parametros.mensagem() + ".", comando.getValue().mensagem());
    }

    @Test
    @DisplayName("Dentro do intervalo mínimo recusa com os segundos restantes e não cria")
    void dentroDoCooldownRecusa() {
        // Arrange
        ultimoTesteEm(AGORA.minusSeconds(30));
        NotificacaoTesteService servico = servico();

        // Act
        NotificacaoTesteCooldownException erro =
                assertThrows(NotificacaoTesteCooldownException.class, servico::enviar);

        // Assert
        assertEquals(270, erro.getSegundosRestantes());
        verify(criacaoService, never()).criar(any());
    }

    @Test
    @DisplayName("Ao completar o intervalo mínimo volta a criar")
    void foraDoCooldownCria() {
        // Arrange
        ultimoTesteEm(AGORA.minusSeconds(300));
        when(criacaoService.criar(any())).thenReturn(new Notificacao());

        // Act
        servico().enviar();

        // Assert
        verify(criacaoService).criar(any());
    }
}
