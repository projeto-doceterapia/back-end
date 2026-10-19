package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.entity.ConfiguracaoFarolAgenda;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AlertaDiaLotadoService")
class AlertaDiaLotadoServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 21);

    @Mock
    private ConfiguracaoFarolAgendaRepository farolRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ConfiguracaoNotificacaoService configuracaoService;

    @Mock
    private CriacaoNotificacaoService criacaoService;

    private AlertaDiaLotadoService service;

    @BeforeEach
    void setUp() {
        service = new AlertaDiaLotadoService(farolRepository, pedidoRepository, configuracaoService, criacaoService);
    }

    private ConfiguracaoFarolAgenda farolComVermelho(int vermelho) {
        ConfiguracaoFarolAgenda farol = new ConfiguracaoFarolAgenda();
        farol.setLimiteVerde(1);
        farol.setLimiteAmarelo(3);
        farol.setLimiteVermelho(vermelho);
        return farol;
    }

    private ConfiguracaoNotificacao configComDiaLotado(boolean ativo) {
        ConfiguracaoNotificacao config = new ConfiguracaoNotificacao();
        config.setAlertaDiaLotadoAtivo(ativo);
        return config;
    }

    @Test
    @DisplayName("Atingir o limite vermelho cria o alerta de dia lotado")
    void atingeLimiteCriaAlerta() {
        when(farolRepository.findAll()).thenReturn(List.of(farolComVermelho(5)));
        when(configuracaoService.obterOuCriar()).thenReturn(configComDiaLotado(true));
        when(pedidoRepository.contarAtivosPorDataEntrega(DATA)).thenReturn(5L);

        service.avaliar(DATA);

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService).criar(captor.capture());
        assertEquals("DIA_LOTADO:2026-10-21", captor.getValue().chaveUnicidade());
    }

    @Test
    @DisplayName("Ultrapassar o limite vermelho de uma vez cria o alerta")
    void ultrapassarLimiteCriaAlerta() {
        when(farolRepository.findAll()).thenReturn(List.of(farolComVermelho(5)));
        when(configuracaoService.obterOuCriar()).thenReturn(configComDiaLotado(true));
        when(pedidoRepository.contarAtivosPorDataEntrega(DATA)).thenReturn(8L);

        service.avaliar(DATA);

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService).criar(captor.capture());
        assertEquals("DIA_LOTADO:2026-10-21", captor.getValue().chaveUnicidade());
    }

    @Test
    @DisplayName("Chamadas repetidas acima do limite usam a mesma chave por dia")
    void repeticaoMantemChavePorDia() {
        when(farolRepository.findAll()).thenReturn(List.of(farolComVermelho(5)));
        when(configuracaoService.obterOuCriar()).thenReturn(configComDiaLotado(true));
        when(pedidoRepository.contarAtivosPorDataEntrega(DATA)).thenReturn(8L);

        service.avaliar(DATA);
        service.avaliar(DATA);

        ArgumentCaptor<ComandoNotificacao> captor = ArgumentCaptor.forClass(ComandoNotificacao.class);
        verify(criacaoService, times(2)).criar(captor.capture());
        assertEquals("DIA_LOTADO:2026-10-21", captor.getAllValues().get(0).chaveUnicidade());
        assertEquals("DIA_LOTADO:2026-10-21", captor.getAllValues().get(1).chaveUnicidade());
    }

    @Test
    @DisplayName("Abaixo do limite vermelho não cria alerta")
    void abaixoDoLimiteNaoCria() {
        when(farolRepository.findAll()).thenReturn(List.of(farolComVermelho(5)));
        when(configuracaoService.obterOuCriar()).thenReturn(configComDiaLotado(true));
        when(pedidoRepository.contarAtivosPorDataEntrega(DATA)).thenReturn(4L);

        service.avaliar(DATA);

        verify(criacaoService, never()).criar(any());
    }

    @Test
    @DisplayName("Alerta desativado não cria notificação mesmo no limite")
    void alertaDesativadoNaoCria() {
        when(farolRepository.findAll()).thenReturn(List.of(farolComVermelho(5)));
        when(configuracaoService.obterOuCriar()).thenReturn(configComDiaLotado(false));

        service.avaliar(DATA);

        verify(criacaoService, never()).criar(any());
    }

    @Test
    @DisplayName("Sem configuração de farol não cria alerta")
    void semFarolNaoCria() {
        when(farolRepository.findAll()).thenReturn(List.of());

        service.avaliar(DATA);

        verify(criacaoService, never()).criar(any());
    }
}
