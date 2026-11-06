package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.event.NotificacaoCriadaEvent;
import br.com.doceterapia.api.mapper.NotificacaoMapper;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

    @Mock
    private NotificacaoMapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
                repository, configuracaoService, clock, objectMapper, 10, 2, mapper, eventPublisher);
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

    private ComandoNotificacao comandoEstoqueBaixo() {
        return ComandoNotificacao.imediata(
                TipoNotificacao.ESTOQUE_BAIXO, "Estoque baixo", "Farinha baixa",
                "ESTOQUE_BAIXO:insumo:1:2026-10-21", "estoque_baixo",
                new ParametrosTeste("Farinha", 1), null, null);
    }

    private Notificacao criarComConfig() {
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return servicoCom(LocalDateTime.of(2026, 10, 21, 10, 0)).criar(comandoEstoqueBaixo());
    }

    @Test
    @DisplayName("Avisos automáticos desligados gravam NAO_ENVIAR")
    void avisosDesligadosGravaNaoEnviar() {
        // Arrange
        config.setAvisosAutomaticosAtivo(false);

        // Act
        Notificacao criada = criarComConfig();

        // Assert
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, criada.getStatusEnvio());
    }

    @Test
    @DisplayName("Assistente desligado grava NAO_ENVIAR")
    void assistenteDesligadoGravaNaoEnviar() {
        // Arrange
        config.setAssistenteAtivo(false);

        // Act
        Notificacao criada = criarComConfig();

        // Assert
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, criada.getStatusEnvio());
    }

    @Test
    @DisplayName("Interruptores ligados gravam PENDENTE")
    void interruptoresLigadosGravaPendente() {
        // Act
        Notificacao criada = criarComConfig();

        // Assert
        assertEquals(StatusEnvioNotificacao.PENDENTE, criada.getStatusEnvio());
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

    @Test
    @DisplayName("Chave nova publica o evento com o mesmo DTO da API, já com id, tipo e status salvos")
    void criacaoPublicaEventoComRespostaDaApi() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 10, 21, 10, 0);
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao salva = invocation.getArgument(0);
            salva.setIdNotificacao(31);
            return salva;
        });
        when(mapper.toResponseDTO(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao salva = invocation.getArgument(0);
            NotificacaoResponseDTO dto = new NotificacaoResponseDTO();
            dto.setIdNotificacao(salva.getIdNotificacao());
            dto.setTipo(salva.getTipo());
            dto.setStatusEnvio(salva.getStatusEnvio());
            return dto;
        });

        // Act
        servicoCom(agora).criar(comandoEstoqueBaixo());

        // Assert
        ArgumentCaptor<NotificacaoCriadaEvent> captor = ArgumentCaptor.forClass(NotificacaoCriadaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        NotificacaoResponseDTO publicado = captor.getValue().notificacao();
        assertEquals(31, publicado.getIdNotificacao());
        assertEquals(TipoNotificacao.ESTOQUE_BAIXO, publicado.getTipo());
        assertEquals(StatusEnvioNotificacao.PENDENTE, publicado.getStatusEnvio());
    }

    @Test
    @DisplayName("Chave existente devolve a notificação salva e não publica evento")
    void chaveExistenteNaoPublicaEvento() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 10, 21, 10, 0);
        Notificacao existente = new Notificacao();
        existente.setIdNotificacao(99);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.of(existente));

        // Act
        Notificacao resultado = servicoCom(agora).criar(comandoEstoqueBaixo());

        // Assert
        assertSame(existente, resultado);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }
}
