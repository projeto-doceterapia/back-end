package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.RegistroPagamentoRequestDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.FormaPagamento;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Spy private PagamentoMapper mapper = new PagamentoMapper();
    @Spy private MaquinaStatusPedido maquina = new MaquinaStatusPedido();
    @Mock private EntityManager entityManager;
    @Mock private BloqueioPedidoService bloqueioPedidoService;
    @Spy private Clock clock = Clock.fixed(Instant.parse("2026-11-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    @InjectMocks private PagamentoService service;

    private Pedido pedido;
    private Pagamento pagamento;
    private RegistroPagamentoRequestDTO request;

    @BeforeEach
    void preparar() {
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_SINAL);
        pagamento = new Pagamento();
        pagamento.setIdPagamento(2);
        pagamento.setPedido(pedido);
        pagamento.setValorTotal(new BigDecimal("185.90"));
        pagamento.setValorSinal(new BigDecimal("92.95"));
        pagamento.setValorRestante(new BigDecimal("92.95"));
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);
        request = new RegistroPagamentoRequestDTO();
        request.setFormaPagamento(FormaPagamento.PIX);
        lenient().when(bloqueioPedidoService.buscar(1)).thenReturn(pedido);
        lenient().when(pagamentoRepository.buscarPedidoId(2)).thenReturn(Optional.of(1));
    }

    private void pagamentoExistente() {
        when(pagamentoRepository.findById(2)).thenReturn(Optional.of(pagamento));
    }

    private void totalItens(String total) {
        ItemPedido item = new ItemPedido();
        item.setValorTotal(new BigDecimal(total));
        when(itemPedidoRepository.findByPedidoIdPedido(1)).thenReturn(List.of(item));
    }

    @Test
    void deveCriarPagamentoPelaSomaDosItens() {
        totalItens("185.90");
        when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Pagamento resultado = service.criar(pedido);
        assertEquals(new BigDecimal("185.90"), resultado.getValorTotal());
        assertEquals(new BigDecimal("92.95"), resultado.getValorSinal());
        assertEquals(new BigDecimal("92.95"), resultado.getValorRestante());
        assertEquals(StatusPagamento.AGUARDANDO_SINAL, resultado.getStatusPagamento());
    }

    @Test
    void deveRecalcularSinalERestanteAntesDoSinalPago() {
        when(pagamentoRepository.findByPedidoIdPedido(1)).thenReturn(Optional.of(pagamento));
        totalItens("45.85");
        service.recalcular(1);
        assertEquals(new BigDecimal("45.85"), pagamento.getValorTotal());
        assertEquals(new BigDecimal("22.93"), pagamento.getValorSinal());
        assertEquals(new BigDecimal("22.92"), pagamento.getValorRestante());
        verify(bloqueioPedidoService).buscar(1);
    }

    @Test
    void deveManterSinalCongeladoDepoisDePago() {
        pagamento.setDataPagamentoSinal(LocalDate.of(2026, 11, 9));
        when(pagamentoRepository.findByPedidoIdPedido(1)).thenReturn(Optional.of(pagamento));
        totalItens("120.00");
        service.recalcular(1);
        assertEquals(new BigDecimal("92.95"), pagamento.getValorSinal());
        assertEquals(new BigDecimal("27.05"), pagamento.getValorRestante());
    }

    @Test
    void deveRecusarTotalAbaixoDoSinalPago() {
        pagamento.setDataPagamentoSinal(LocalDate.of(2026, 11, 9));
        when(pagamentoRepository.findByPedidoIdPedido(1)).thenReturn(Optional.of(pagamento));
        totalItens("80.00");
        assertThrows(TotalAbaixoDoSinalPagoException.class, () -> service.recalcular(1));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveIgnorarRecalculoDeOrcamentoSemPagamento() {
        service.recalcular(1);
        verifyNoInteractions(itemPedidoRepository);
    }

    @Test
    void deveAvancarPedidoAoRegistrarSinal() {
        pagamentoExistente();
        var resultado = service.registrarSinal(2, request);
        assertEquals(FormaPagamento.PIX, resultado.getFormaPagamentoSinal());
        assertEquals(LocalDate.of(2026, 11, 10), resultado.getDataPagamentoSinal());
        assertEquals(StatusPagamento.AGUARDANDO_PAGAMENTO, resultado.getStatusPagamento());
        assertEquals(StatusPedido.ESPERANDO_FAZER, pedido.getStatusPedido());
        verify(bloqueioPedidoService).buscar(1);
    }

    @Test
    void deveRegistrarRestanteSemAlterarFaseOperacional() {
        pagamentoExistente();
        pedido.setStatusPedido(StatusPedido.EM_PRODUCAO);
        pagamento.setDataPagamentoSinal(LocalDate.of(2026, 11, 9));
        pagamento.setFormaPagamentoSinal(FormaPagamento.PIX);
        request.setFormaPagamento(FormaPagamento.DINHEIRO);
        var resultado = service.registrarRestante(2, request);
        assertEquals(FormaPagamento.PIX, resultado.getFormaPagamentoSinal());
        assertEquals(FormaPagamento.DINHEIRO, resultado.getFormaPagamentoRestante());
        assertEquals(StatusPedido.EM_PRODUCAO, pedido.getStatusPedido());
        assertEquals(StatusPagamento.PAGO_INTEGRAL, resultado.getStatusPagamento());
        verify(bloqueioPedidoService).buscar(1);
    }

    @Test
    void deveRecusarSinalRepetido() {
        pagamentoExistente();
        pagamento.setDataPagamentoSinal(LocalDate.of(2026, 11, 9));
        assertThrows(SinalJaRegistradoException.class, () -> service.registrarSinal(2, request));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveRecusarRestanteSemSinal() {
        pagamentoExistente();
        assertThrows(SinalNaoRegistradoException.class, () -> service.registrarRestante(2, request));
    }

    @Test
    void deveRecusarRestanteRepetido() {
        pagamentoExistente();
        pagamento.setDataPagamentoSinal(LocalDate.of(2026, 11, 9));
        pagamento.setDataPagamentoRestante(LocalDate.of(2026, 11, 10));
        assertThrows(RestanteJaRegistradoException.class, () -> service.registrarRestante(2, request));
    }

    @Test
    void deveRecusarPagamentoDePedidoCancelado() {
        pagamentoExistente();
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        assertThrows(PagamentoPedidoCanceladoException.class, () -> service.registrarSinal(2, request));
        assertThrows(PagamentoPedidoCanceladoException.class, () -> service.registrarRestante(2, request));
    }

    @Test
    void deveRecusarDataFutura() {
        pagamentoExistente();
        request.setDataPagamento(LocalDate.of(2026, 11, 11));
        assertThrows(DataPagamentoFuturaException.class, () -> service.registrarSinal(2, request));
    }

    @Test
    void deveRecusarSinalDeTotalZero() {
        pagamentoExistente();
        pagamento.setValorTotal(BigDecimal.ZERO);
        assertThrows(PagamentoSemItensException.class, () -> service.registrarSinal(2, request));
    }

    @Test
    void naoDeveRegistrarSinalQuandoTransicaoDoPedidoFalha() {
        pagamentoExistente();
        pedido.setStatusPedido(StatusPedido.ENTREGUE);
        assertThrows(StatusTransitionInvalidException.class, () -> service.registrarSinal(2, request));
        assertNull(pagamento.getDataPagamentoSinal());
        assertNull(pagamento.getFormaPagamentoSinal());
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveBuscarPagamentoPorIdEPedido() {
        pagamentoExistente();
        when(pagamentoRepository.findByPedidoIdPedido(1)).thenReturn(Optional.of(pagamento));
        assertEquals(2, service.buscarPorId(2).getIdPagamento());
        assertEquals(2, service.buscarPorPedidoId(1).getIdPagamento());
    }

    @Test
    void deveRecusarConsultaInexistente() {
        assertThrows(PagamentoIdDontExistsException.class, () -> service.buscarPorId(999));
        assertThrows(PagamentoIdDontExistsException.class, () -> service.buscarPorPedidoId(999));
    }
}
