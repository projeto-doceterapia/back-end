package br.com.doceterapia.api.service.notificacao.resumo;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import br.com.doceterapia.api.service.notificacao.ComandoNotificacao;
import br.com.doceterapia.api.service.notificacao.CriacaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ResumoDiarioScheduler")
class ResumoDiarioSchedulerTest {

    private static final ZoneId ZONA = ZoneOffset.UTC;

    @Mock
    private ConfiguracaoNotificacaoService configuracaoService;

    @Mock
    private ResumoDiarioQueries queries;

    @Mock
    private CriacaoNotificacaoService criacaoService;

    private final ResumoDiarioBuilder builder = new ResumoDiarioBuilder();

    private ConfiguracaoNotificacao config;

    @BeforeEach
    void setUp() {
        config = new ConfiguracaoNotificacao();
        config.setDiasExpediente("1,2,3,4,5,6"); // Mon..Sat
        config.setHorarioResumoInicio(LocalTime.of(8, 0));
        config.setHorarioResumoFim(LocalTime.of(17, 30));
        config.setDiasAntecedencia(3);
    }

    private ResumoDiarioScheduler schedulerEm(LocalDateTime agora) {
        Clock clock = Clock.fixed(agora.atZone(ZONA).toInstant(), ZONA);
        return new ResumoDiarioScheduler(configuracaoService, queries, builder, criacaoService, clock);
    }

    private void comConsultasVazias() {
        lenient().when(queries.sinaisParaConferir()).thenReturn(List.of());
        lenient().when(queries.pagamentosFinaisParaConferir()).thenReturn(List.of());
        lenient().when(queries.entregasHoje(any())).thenReturn(List.of());
        lenient().when(queries.pedidosAtrasados(any())).thenReturn(List.of());
        lenient().when(queries.proximosDias(any(), any())).thenReturn(List.of());
        lenient().when(queries.insumosParaComprar()).thenReturn(List.of());
    }

    @Test
    @DisplayName("Dia vazio ainda cria o resumo com seções marcadas como 'nada pendente'")
    void diaVazioCriaResumo() {
        // Wednesday 09:00: start-of-day time (08:00) has passed, end-of-day (17:30) not yet
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        comConsultasVazias();

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        when(criacaoService.criar(captor.capture())).thenReturn(new Notificacao());

        schedulerEm(LocalDateTime.of(2026, 10, 21, 9, 0)).verificar();

        verify(criacaoService, times(1)).criar(any());
        ComandoNotificacao comando = captor.getValue();
        assertEquals(TipoNotificacao.RESUMO_INICIO_DIA, comando.tipo());
        assertTrue(comando.mensagem().contains("nada pendente"));
    }

    @Test
    @DisplayName("Após o horário de fim do dia cria ambos os resumos")
    void aposFimDoDiaCriaAmbos() {
        // Wednesday 18:00: both summary times have passed
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        comConsultasVazias();
        when(criacaoService.criar(any())).thenReturn(new Notificacao());

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        schedulerEm(LocalDateTime.of(2026, 10, 21, 18, 0)).verificar();

        verify(criacaoService, times(2)).criar(captor.capture());
        Set<TipoNotificacao> tipos = new HashSet<>();
        captor.getAllValues().forEach(c -> tipos.add(c.tipo()));
        assertTrue(tipos.contains(TipoNotificacao.RESUMO_INICIO_DIA));
        assertTrue(tipos.contains(TipoNotificacao.RESUMO_FIM_DIA));
    }

    @Test
    @DisplayName("Horário de fim alterado para antes de agora passa a valer no mesmo dia")
    void horarioAlteradoNoMesmoDia() {
        // Owner moved end-of-day to 16:00 at 16:30 the same day
        config.setHorarioResumoFim(LocalTime.of(16, 0));
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        comConsultasVazias();

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        when(criacaoService.criar(captor.capture())).thenReturn(new Notificacao());

        schedulerEm(LocalDateTime.of(2026, 10, 21, 16, 30)).verificar();

        Set<TipoNotificacao> tipos = new HashSet<>();
        captor.getAllValues().forEach(c -> tipos.add(c.tipo()));
        assertTrue(tipos.contains(TipoNotificacao.RESUMO_FIM_DIA));
    }

    @Test
    @DisplayName("Reexecuções dependem da dedup do serviço de criação (chaves estáveis por dia)")
    void reexecucoesUsamChaveEstavel() {
        when(configuracaoService.obterOuCriar()).thenReturn(config);
        comConsultasVazias();

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        when(criacaoService.criar(captor.capture())).thenReturn(new Notificacao());

        ResumoDiarioScheduler scheduler = schedulerEm(LocalDateTime.of(2026, 10, 21, 9, 0));
        scheduler.verificar();
        scheduler.verificar();

        // Same business key on every run, so the creation service deduplicates.
        captor.getAllValues().forEach(c ->
                assertEquals("RESUMO_INICIO_DIA:2026-10-21", c.chaveUnicidade()));
    }

    @Test
    @DisplayName("Dia não útil não cria resumo")
    void diaNaoUtilNaoCria() {
        // Sunday
        when(configuracaoService.obterOuCriar()).thenReturn(config);

        schedulerEm(LocalDateTime.of(2026, 10, 25, 9, 0)).verificar();

        verify(criacaoService, never()).criar(any());
    }

    @Test
    @DisplayName("Resumo desativado não é criado")
    void resumoDesativadoNaoCria() {
        config.setResumoInicioDiaAtivo(false);
        config.setResumoFimDiaAtivo(false);
        when(configuracaoService.obterOuCriar()).thenReturn(config);

        schedulerEm(LocalDateTime.of(2026, 10, 21, 18, 0)).verificar();

        verify(criacaoService, never()).criar(any());
    }
}
