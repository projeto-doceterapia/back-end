package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.exception.DataEntregaNoPassadoException;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.service.notificacao.AlertaDiaLotadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PedidoService")
class PedidoServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private AlertaDiaLotadoService alertaDiaLotadoService;

    @Mock
    private BloqueioPedidoService bloqueioPedidoService;

    private PedidoService pedidoService;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepository, pedidoMapper, pagamentoService,
                alertaDiaLotadoService, new MaquinaStatusPedido(), bloqueioPedidoService, CLOCK);
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setTipoPedido(TipoPedido.ORCAMENTO);
        pedido.setStatusPedido(StatusPedido.ORCAMENTO);
        pedido.setDataEntrega(LocalDate.of(2026, 11, 20));
    }

    @Test
    @DisplayName("Confirma orçamento, registra a data e cria o pagamento calculado")
    void confirmaOrcamentoECriaPagamento() {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setIdPedido(1);
        when(bloqueioPedidoService.buscar(1)).thenReturn(pedido);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(response);

        PedidoResponseDTO result = pedidoService.atualizarStatus(1, StatusPedido.AGUARDANDO_SINAL);

        assertEquals(1, result.getIdPedido());
        assertEquals(TipoPedido.PEDIDO_CONFIRMADO, pedido.getTipoPedido());
        assertEquals(StatusPedido.AGUARDANDO_SINAL, pedido.getStatusPedido());
        assertEquals(Instant.now(CLOCK), pedido.getDataConfirmacao().toInstant(ZoneOffset.UTC));
        verify(pagamentoService).criar(pedido);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("Rejeita cadastro com data de entrega no passado")
    void rejeitaCadastroComDataDeEntregaNoPassado() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setStatusPedido(StatusPedido.ORCAMENTO);
        Pedido pedidoPassado = new Pedido();
        pedidoPassado.setDataEntrega(LocalDate.of(2026, 11, 10));
        when(pedidoMapper.toPedido(request)).thenReturn(pedidoPassado);

        assertThrows(DataEntregaNoPassadoException.class, () -> pedidoService.cadastrar(request));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Rejeita transição que pula etapas do fluxo")
    void rejeitaTransicaoQuePulaEtapas() {
        when(bloqueioPedidoService.buscar(1)).thenReturn(pedido);

        assertThrows(StatusTransitionInvalidException.class,
                () -> pedidoService.atualizarStatus(1, StatusPedido.ENTREGUE));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Rejeita avanço manual enquanto o sinal não foi registrado")
    void rejeitaAvancoManualSemSinalRegistrado() {
        pedido.setTipoPedido(TipoPedido.PEDIDO_CONFIRMADO);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_SINAL);
        when(bloqueioPedidoService.buscar(1)).thenReturn(pedido);

        assertThrows(StatusTransitionInvalidException.class,
                () -> pedidoService.atualizarStatus(1, StatusPedido.ESPERANDO_FAZER));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}
