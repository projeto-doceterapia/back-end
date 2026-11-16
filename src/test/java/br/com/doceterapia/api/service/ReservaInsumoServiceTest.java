package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.ConsumoPrevistoInsumo;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaInsumoServiceTest {

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Test
    void deveDescontarConsumoDosConfirmadosNaoProduzidosConvertendoUnidade() {
        Insumo chocolate = insumo(7, "2.000");
        when(itemPedidoRepository.somarConsumoReservado(any(), any(), any())).thenReturn(List.of(
                new ConsumoPrevistoInsumo(7, UnidadeMedida.KG, new BigDecimal("1.000")),
                new ConsumoPrevistoInsumo(7, UnidadeMedida.G, new BigDecimal("700"))));

        Map<Integer, BigDecimal> disponivel = new ReservaInsumoService(itemPedidoRepository,
                new ConversaoUnidadeService()).disponivelPorInsumo(List.of(chocolate), null);

        assertEquals(0, new BigDecimal("0.300").compareTo(disponivel.get(7)));
    }

    @Test
    void deveIgnorarOProprioPedido() {
        Insumo chocolate = insumo(7, "2.000");
        when(itemPedidoRepository.somarConsumoReservado(any(), any(), any())).thenReturn(List.of());

        new ReservaInsumoService(itemPedidoRepository, new ConversaoUnidadeService())
                .disponivelPorInsumo(List.of(chocolate), 42);

        ArgumentCaptor<Integer> pedidoIgnorado = ArgumentCaptor.forClass(Integer.class);
        verify(itemPedidoRepository).somarConsumoReservado(any(), any(), pedidoIgnorado.capture());
        assertEquals(42, pedidoIgnorado.getValue());
    }

    private Insumo insumo(Integer id, String saldo) {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(id);
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setQuantidadeAtual(new BigDecimal(saldo));
        return insumo;
    }
}
