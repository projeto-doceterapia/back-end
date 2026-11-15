package br.com.doceterapia.api.service;

import br.com.doceterapia.api.exception.FiltroInvalidoException;
import br.com.doceterapia.api.mapper.CancelamentoPedidoMapper;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import br.com.doceterapia.api.repository.ProducaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoConsultaServiceTest {
    @Mock private PedidoRepository pedidos;
    @Mock private ItemPedidoRepository itens;
    @Mock private PagamentoRepository pagamentos;
    @Mock private ProducaoRepository producoes;
    @Mock private CancelamentoPedidoRepository cancelamentos;
    @Mock private PedidoMapper pedidoMapper;
    @Mock private ItemPedidoMapper itemMapper;
    @Mock private PagamentoMapper pagamentoMapper;
    @Mock private ProducaoMapper producaoMapper;
    @Mock private CancelamentoPedidoMapper cancelamentoMapper;

    @Test
    void deduplicaIdsAntesDaConsultaELimitaAposDeduplicar() {
        PedidoConsultaService service = service();
        when(itens.buscarPorPedidos(List.of(1, 2))).thenReturn(List.of());
        service.listarItens(List.of(1, 1, 2));
        verify(itens).buscarPorPedidos(List.of(1, 2));
        assertEquals("Informe ao menos um pedidoId.", assertThrows(FiltroInvalidoException.class,
                () -> service.listarPagamentos(List.of())).getMessage());
    }

    @Test
    void aceitaQuinhentosIdsDistintosERecusaQuinhentosEUm() {
        PedidoConsultaService service = service();
        List<Integer> quinhentosIds = IntStream.rangeClosed(1, 500).boxed().toList();
        when(pagamentos.buscarPorPedidos(quinhentosIds)).thenReturn(List.of());

        service.listarPagamentos(quinhentosIds);
        verify(pagamentos).buscarPorPedidos(quinhentosIds);

        List<Integer> quinhentosEUmIds = IntStream.rangeClosed(1, 501).boxed().toList();
        FiltroInvalidoException exception = assertThrows(FiltroInvalidoException.class,
                () -> service.listarItens(quinhentosEUmIds));
        assertTrue(exception.getMessage().contains("500 pedidoId"));
    }

    @Test
    void converteParametrosCsvERepetidosAntesDeConsultar() {
        PedidoConsultaService service = service();
        when(itens.buscarPorPedidos(List.of(1, 2, 3))).thenReturn(List.of());

        service.listarItensComParametros(new String[] {"1,2", "3"});

        verify(itens).buscarPorPedidos(List.of(1, 2, 3));
    }

    private PedidoConsultaService service() {
        return new PedidoConsultaService(pedidos, itens, pagamentos, producoes, cancelamentos,
                pedidoMapper, itemMapper, pagamentoMapper, producaoMapper, cancelamentoMapper);
    }
}
