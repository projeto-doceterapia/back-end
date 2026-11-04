package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.exception.ConfiguracaoNotificacaoInvalidaException;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ConfiguracaoNotificacaoService")
class ConfiguracaoNotificacaoServiceTest {

    @Mock
    private ConfiguracaoNotificacaoRepository repository;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private ConfiguracaoNotificacaoService service;

    private ConfiguracaoNotificacaoRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new ConfiguracaoNotificacaoRequestDTO();
        request.setInicioExpediente(LocalTime.of(8, 0));
        request.setFimExpediente(LocalTime.of(18, 0));
        request.setDiasExpediente(List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
        request.setHorarioResumoInicio(LocalTime.of(8, 0));
        request.setHorarioResumoFim(LocalTime.of(17, 30));
        request.setDiasAntecedencia(3);
        request.setResumoInicioDiaAtivo(true);
        request.setResumoFimDiaAtivo(true);
        request.setEstoqueBaixoAtivo(true);
        request.setDiaLotadoAtivo(true);
        request.setAvisosAutomaticosAtivo(true);
        request.setAssistenteAtivo(true);
    }

    @Test
    @DisplayName("Deve criar a configuração padrão quando nenhuma existe")
    void deveBuscarCriandoPadrao() {
        // Arrange
        when(repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracaoNotificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConfiguracaoNotificacaoResponseDTO resultado = service.buscar();

        // Assert
        assertNotNull(resultado);
        assertEquals(LocalTime.of(8, 0), resultado.getInicioExpediente());
        assertEquals(6, resultado.getDiasExpediente().size());
        verify(repository).save(any(ConfiguracaoNotificacao.class));
    }

    @Test
    @DisplayName("Deve atualizar configuração válida")
    void deveAtualizarValida() {
        // Arrange
        ConfiguracaoNotificacao existente = new ConfiguracaoNotificacao();
        existente.setIdConfiguracaoNotificacao(1);
        when(repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()).thenReturn(Optional.of(existente));
        when(repository.save(any(ConfiguracaoNotificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConfiguracaoNotificacaoResponseDTO resultado = service.atualizar(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(LocalTime.of(17, 30), resultado.getHorarioResumoFim());
        assertEquals("1,2,3,4,5,6", existente.getDiasExpediente());
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("A configuração padrão traz os dois interruptores ligados")
    void padraoComInterruptoresLigados() {
        // Arrange
        when(repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracaoNotificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConfiguracaoNotificacaoResponseDTO resultado = service.buscar();

        // Assert
        assertTrue(resultado.getAvisosAutomaticosAtivo());
        assertTrue(resultado.getAssistenteAtivo());
    }

    private ConfiguracaoNotificacao existenteCom(boolean avisos, boolean assistente) {
        ConfiguracaoNotificacao existente = new ConfiguracaoNotificacao();
        existente.setIdConfiguracaoNotificacao(1);
        existente.setAvisosAutomaticosAtivo(avisos);
        existente.setAssistenteAtivo(assistente);
        when(repository.findFirstByOrderByIdConfiguracaoNotificacaoAsc()).thenReturn(Optional.of(existente));
        when(repository.save(any(ConfiguracaoNotificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return existente;
    }

    @Test
    @DisplayName("Desligar os avisos automáticos descarta as pendentes")
    void desligarDescartaPendentes() {
        // Arrange
        existenteCom(true, true);
        request.setAvisosAutomaticosAtivo(false);

        // Act
        service.atualizar(request);

        // Assert
        verify(notificacaoRepository).descartarPendentes();
    }

    @Test
    @DisplayName("Desligar o assistente descarta as pendentes")
    void desligarAssistenteDescartaPendentes() {
        // Arrange
        existenteCom(true, true);
        request.setAssistenteAtivo(false);

        // Act
        service.atualizar(request);

        // Assert
        verify(notificacaoRepository).descartarPendentes();
    }

    @Test
    @DisplayName("Sem transição para desligado não há descarte")
    void semDesligarNaoDescarta() {
        // Arrange: already off, stays off; the other PUT changes nothing
        existenteCom(false, true);
        request.setAvisosAutomaticosAtivo(false);

        // Act
        service.atualizar(request);

        // Assert
        verify(notificacaoRepository, never()).descartarPendentes();
    }

    @Test
    @DisplayName("Um PUT com os dois ligados não descarta")
    void putComDoisLigadosNaoDescarta() {
        // Arrange
        existenteCom(true, true);

        // Act
        service.atualizar(request);

        // Assert
        verify(notificacaoRepository, never()).descartarPendentes();
    }

    @Test
    @DisplayName("Religar não descarta nem reativa")
    void religarNaoReativa() {
        // Arrange
        existenteCom(false, false);

        // Act
        service.atualizar(request);

        // Assert
        verify(notificacaoRepository, never()).descartarPendentes();
    }

    @Test
    @DisplayName("Deve rejeitar quando o fim não é depois do início")
    void deveRejeitarFimAntesDoInicio() {
        // Arrange
        request.setFimExpediente(LocalTime.of(7, 0));

        // Act & Assert
        assertThrows(ConfiguracaoNotificacaoInvalidaException.class,
                () -> service.atualizar(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar resumo fora do expediente")
    void deveRejeitarResumoForaDoExpediente() {
        // Arrange
        request.setHorarioResumoFim(LocalTime.of(19, 0));

        // Act & Assert
        assertThrows(ConfiguracaoNotificacaoInvalidaException.class,
                () -> service.atualizar(request));
        verify(repository, never()).save(any());
    }
}
