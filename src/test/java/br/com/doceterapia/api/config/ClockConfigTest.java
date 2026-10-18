package br.com.doceterapia.api.config;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import br.com.doceterapia.api.service.notificacao.ComandoNotificacao;
import br.com.doceterapia.api.service.notificacao.CriacaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ClockConfig")
class ClockConfigTest {

    @Mock
    private NotificacaoRepository repository;

    @Mock
    private ConfiguracaoNotificacaoService configuracaoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ConfiguracaoNotificacao config;

    @BeforeEach
    void setUp() {
        config = new ConfiguracaoNotificacao();
        config.setInicioExpediente(LocalTime.of(8, 0));
        config.setFimExpediente(LocalTime.of(18, 0));
        config.setDiasExpediente("1,2,3,4,5,6");
    }

    @Test
    @DisplayName("Sem configuração explícita o relógio usa o fuso de São Paulo")
    void relogioPadraoSaoPaulo() {
        Clock clock = new ClockConfig("America/Sao_Paulo").clock();

        assertEquals(ZoneId.of("America/Sao_Paulo"), clock.getZone());
    }

    @Test
    @DisplayName("O fuso configurado define o fuso do relógio")
    void fusoConfiguradoDefineZona() {
        Clock clock = new ClockConfig("UTC").clock();

        assertEquals(ZoneId.of("UTC"), clock.getZone());
    }

    @Test
    @DisplayName("Mesmo instante decide o resumo em dias diferentes conforme o fuso")
    void resumoDecideDiaConformeFuso() {
        // 2026-10-21T02:00:00Z is still Tuesday 23:00 in São Paulo (UTC-3)
        // but already Wednesday 02:00 in UTC.
        Instant instante = Instant.parse("2026-10-21T02:00:00Z");

        LocalDateTime janelaSaoPaulo = janelaResumoNoFuso(instante, "America/Sao_Paulo");
        LocalDateTime janelaUtc = janelaResumoNoFuso(instante, "UTC");

        assertEquals(LocalDateTime.of(2026, 10, 20, 8, 0), janelaSaoPaulo);
        assertEquals(LocalDateTime.of(2026, 10, 21, 8, 0), janelaUtc);
    }

    private LocalDateTime janelaResumoNoFuso(Instant instante, String fuso) {
        Clock clock = Clock.fixed(instante, new ClockConfig(fuso).clock().getZone());
        CriacaoNotificacaoService servico = new CriacaoNotificacaoService(
                repository, configuracaoService, clock, objectMapper, 10, 2);

        when(configuracaoService.obterOuCriar()).thenReturn(config);
        when(repository.findByChaveUnicidade(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComandoNotificacao comando = ComandoNotificacao.resumo(
                TipoNotificacao.RESUMO_INICIO_DIA, "Resumo", "Bom dia",
                "RESUMO_INICIO_DIA:" + fuso, "resumo_inicio_dia",
                null, LocalTime.of(8, 0));

        return servico.criar(comando).getJanelaInicio();
    }
}
