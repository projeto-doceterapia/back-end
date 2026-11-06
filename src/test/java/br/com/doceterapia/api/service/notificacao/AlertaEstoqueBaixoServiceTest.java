package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AlertaEstoqueBaixoService")
class AlertaEstoqueBaixoServiceTest {

    private static final ZoneId ZONA = ZoneOffset.UTC;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 21, 10, 0);

    @Mock
    private CriacaoNotificacaoService criacaoService;

    @Mock
    private ConfiguracaoNotificacaoService configuracaoService;

    private AlertaEstoqueBaixoService service;

    private Insumo insumo;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);
        service = new AlertaEstoqueBaixoService(criacaoService, configuracaoService, clock);

        insumo = new Insumo();
        insumo.setIdInsumo(7);
        insumo.setNome("Farinha");
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setQuantidadeMinima(BigDecimal.valueOf(2));
    }

    private ConfiguracaoNotificacao configComEstoqueBaixo(boolean ativo) {
        ConfiguracaoNotificacao config = new ConfiguracaoNotificacao();
        config.setAlertaEstoqueBaixoAtivo(ativo);
        return config;
    }

    @Test
    @DisplayName("Cruzamento para baixo do mínimo cria o alerta uma vez")
    void cruzamentoParaBaixoCriaAlerta() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(true));

        // 3 (acima do mínimo 2) -> 1 (abaixo do mínimo)
        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1));

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService).criar(captor.capture());
        assertEquals("ESTOQUE_BAIXO:insumo:7:2026-10-21", captor.getValue().chaveUnicidade());
        assertEquals(insumo, captor.getValue().insumo());
    }

    @Test
    @DisplayName("Saldo já abaixo do mínimo não cria novo alerta")
    void jaAbaixoNaoCriaAlerta() {
        // 1 (já abaixo do mínimo 2) -> 0.5 (continua abaixo)
        service.avaliar(insumo, BigDecimal.valueOf(1), BigDecimal.valueOf(2), new BigDecimal("0.5"));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Chegar exatamente ao mínimo conta como cruzamento para baixo")
    void chegarAoMinimoCriaAlerta() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(true));

        // 3 (acima) -> 2 (igual ao mínimo)
        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(2));

        verify(criacaoService).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Alerta desativado não cria notificação mesmo no cruzamento")
    void alertaDesativadoNaoCria() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(false));

        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }

    private ComandoNotificacao comandoCriado() {
        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService, times(1)).criar(captor.capture());
        return captor.getValue();
    }

    private void assertUsaMinimoNovo(ComandoNotificacao comando, String saldo) {
        ParametrosEstoqueBaixo parametros = (ParametrosEstoqueBaixo) comando.parametros();
        assertEquals(0, new BigDecimal(saldo).compareTo(parametros.saldo()));
        assertEquals(0, BigDecimal.valueOf(4).compareTo(parametros.minimo()));
        assertTrue(comando.mensagem().contains("mínimo 4"));
    }

    @Test
    @DisplayName("Subir o mínimo acima do saldo, sem mudar o saldo, cria o alerta com o mínimo novo")
    void minimoElevadoAcimaDoSaldoCriaAlerta() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(true));
        insumo.setQuantidadeMinima(BigDecimal.valueOf(4));

        // saldo 3 / mínimo 2 -> saldo 3 / mínimo 4
        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(3));

        assertUsaMinimoNovo(comandoCriado(), "3");
    }

    @Test
    @DisplayName("Baixar o mínimo não cria alerta, com saldo acima ou já abaixo do mínimo anterior")
    void minimoReduzidoNaoCriaAlerta() {
        // saldo 3 / mínimo 2 -> saldo 3 / mínimo 1
        insumo.setQuantidadeMinima(BigDecimal.valueOf(1));
        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(3));

        // saldo 1 / mínimo 2 -> saldo 1 / mínimo 0,5
        insumo.setQuantidadeMinima(new BigDecimal("0.5"));
        service.avaliar(insumo, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(1));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Baixar o saldo e subir o mínimo na mesma edição cria um alerta com o mínimo novo")
    void ajusteComMinimoElevadoCriaAlerta() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(true));
        insumo.setQuantidadeMinima(BigDecimal.valueOf(4));

        // saldo 5 / mínimo 2 -> saldo 3 / mínimo 4
        service.avaliar(insumo, BigDecimal.valueOf(5), BigDecimal.valueOf(2), BigDecimal.valueOf(3));

        assertUsaMinimoNovo(comandoCriado(), "3");
    }

    @Test
    @DisplayName("Baixar o saldo e o mínimo na mesma edição não cria alerta falso")
    void ajusteComMinimoReduzidoNaoCriaAlertaFalso() {
        insumo.setQuantidadeMinima(BigDecimal.valueOf(2));

        // saldo 5 / mínimo 4 -> saldo 3 / mínimo 2
        service.avaliar(insumo, BigDecimal.valueOf(5), BigDecimal.valueOf(4), BigDecimal.valueOf(3));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Subir o mínimo com o saldo já abaixo não cria novo alerta")
    void jaAbaixoComMinimoElevadoNaoCriaAlerta() {
        insumo.setQuantidadeMinima(BigDecimal.valueOf(3));

        // saldo 1 / mínimo 2 -> saldo 1 / mínimo 3
        service.avaliar(insumo, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(1));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Mínimo anterior nulo conta como não estar baixo")
    void minimoAnteriorNuloContaComoNaoBaixo() {
        when(configuracaoService.obterOuCriar()).thenReturn(configComEstoqueBaixo(true));

        // saldo 1 / sem mínimo -> saldo 1 / mínimo 2
        service.avaliar(insumo, BigDecimal.valueOf(1), null, BigDecimal.valueOf(1));

        verify(criacaoService).criar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Mínimo novo nulo não cria alerta")
    void minimoNovoNuloNaoCriaAlerta() {
        insumo.setQuantidadeMinima(null);

        service.avaliar(insumo, BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1));

        verify(criacaoService, never()).criar(org.mockito.ArgumentMatchers.any());
    }
}
