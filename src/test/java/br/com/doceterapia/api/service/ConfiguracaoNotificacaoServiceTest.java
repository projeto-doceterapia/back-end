package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.exception.ConfiguracaoNotificacaoInvalidaException;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ConfiguracaoNotificacaoService")
class ConfiguracaoNotificacaoServiceTest {

    @Mock
    private ConfiguracaoNotificacaoRepository repository;

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
    }

    @Test
    @DisplayName("Deve criar a configuração padrão quando nenhuma existe")
    void deveBuscarCriandoPadrao() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());
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
        when(repository.findAll()).thenReturn(List.of(existente));
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
