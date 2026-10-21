package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CriacaoNotificacaoService")
class CriacaoNotificacaoServiceTest {

    private static final ZoneId ZONA = ZoneOffset.UTC;

    @Mock
    private NotificacaoRepository repository;

    @Mock
    private ConfiguracaoNotificacaoService configuracaoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ConfiguracaoNotificacao config;

    private record ParametrosTeste(String insumo, int saldo) implements ParametrosTemplate {
    }

    @BeforeEach
    void setUp() {
        config = new ConfiguracaoNotificacao();
        config.setInicioExpediente(LocalTime.of(8, 0));
        config.setFimExpediente(LocalTime.of(18, 0));
        // Monday..Saturday (no Sunday).
        config.setDiasExpediente("1,2,3,4,5,6");
    }

    private CriacaoNotificacaoService servicoCom(LocalDateTime agora) {
        Clock clock = Clock.fixed(agora.atZone(ZONA).toInstant(), ZONA);
        return new CriacaoNotificacaoService(
                repository, configuracaoService, clock, objectMapper, 10, 2);
    }

    @Test
    @DisplayName("Alerta imediato dentro do expediente usa janela de 2 minutos")
    void janelaImediataDentroDoExpediente() {
        // Arrange: Wednesday 10:00
        LocalDateTime agora = LocalDateTime.of(2026, 10, 21, 10, 0);
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComandoNotificacao comando = ComandoNotificacao.imediata(
                TipoNotificacao.ESTOQUE_BAIXO, "Estoque baixo", "Farinha baixa",
                "ESTOQUE_BAIXO:insumo:1:2026-10-21", "estoque_baixo",
                new ParametrosTeste("Farinha", 1), null, null);

        // Act
        Notificacao criada = servicoCom(agora).criar(comando);

        // Assert
        assertEquals(LocalDateTime.of(2026, 10, 21, 10, 0), criada.getJanelaInicio());
        assertEquals(LocalDateTime.of(2026, 10, 21, 10, 2), criada.getJanelaFim());
        assertEquals(StatusEnvioNotificacao.PENDENTE, criada.getStatusEnvio());
        assertTrue(criada.getParametros().contains("Farinha"));
    }

    @Test
    @DisplayName("Alerta após o expediente move a janela para o próximo início de expediente")
    void alertaAposExpedienteMoveParaProximoDia() {
        // Arrange: Wednesday 23:00 -> Thursday 08:00
        LocalDateTime agora = LocalDateTime.of(2026, 10, 21, 23, 0);
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComandoNotificacao comando = ComandoNotificacao.imediata(
                TipoNotificacao.ESTOQUE_BAIXO, "Estoque baixo", "Farinha baixa",
                "ESTOQUE_BAIXO:insumo:1:2026-10-21", "estoque_baixo",
                new ParametrosTeste("Farinha", 1), null, null);

        // Act
        Notificacao criada = servicoCom(agora).criar(comando);

        // Assert
        assertEquals(LocalDateTime.of(2026, 10, 22, 8, 0), criada.getJanelaInicio());
        assertEquals(LocalDateTime.of(2026, 10, 22, 8, 2), criada.getJanelaFim());
    }

    @Test
    @DisplayName("Resumo em dia não útil move a janela para o próximo dia útil")
    void resumoEmDiaNaoUtilMoveParaProximoDiaUtil() {
        // Arrange: Sunday 2026-10-25, summary at 08:00 -> Monday 2026-10-26 08:00
        LocalDateTime agora = LocalDateTime.of(2026, 10, 25, 8, 0);
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComandoNotificacao comando = ComandoNotificacao.resumo(
                TipoNotificacao.RESUMO_INICIO_DIA, "Resumo", "Bom dia",
                "RESUMO_INICIO_DIA:2026-10-25", "resumo_inicio_dia",
                null, LocalTime.of(8, 0));

        // Act
        Notificacao criada = servicoCom(agora).criar(comando);

        // Assert
        assertEquals(LocalDateTime.of(2026, 10, 26, 8, 0), criada.getJanelaInicio());
        assertEquals(LocalDateTime.of(2026, 10, 26, 8, 10), criada.getJanelaFim());
    }

    @Test
    @DisplayName("Chave de unicidade existente não cria uma nova notificação")
    void dedupNaoCriaNovamente() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 10, 21, 10, 0);
        Notificacao existente = new Notificacao();
        existente.setIdNotificacao(99);
        when(repository.findByChaveUnicidade("ESTOQUE_BAIXO:insumo:1:2026-10-21"))
                .thenReturn(Optional.of(existente));

        ComandoNotificacao comando = ComandoNotificacao.imediata(
                TipoNotificacao.ESTOQUE_BAIXO, "Estoque baixo", "Farinha baixa",
                "ESTOQUE_BAIXO:insumo:1:2026-10-21", "estoque_baixo",
                new ParametrosTeste("Farinha", 1), null, null);

        // Act
        Notificacao resultado = servicoCom(agora).criar(comando);

        // Assert
        assertSame(existente, resultado);
        verify(repository, never()).save(any());
    }
}
