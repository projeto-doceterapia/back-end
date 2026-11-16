package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.ConsumoPrevistoInsumo;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaixaProducaoServiceTest {

    @Mock
    private ItemPedidoRepository itemPedidoRepository;
    @Mock
    private InsumoRepository insumoRepository;
    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Test
    void deveGerarUmaSaidaPorInsumoAgregandoItensEmOrdemCrescente() {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(42);
        Insumo insumo7 = insumo(7);
        Insumo insumo9 = insumo(9);
        when(itemPedidoRepository.somarConsumoPorPedido(42)).thenReturn(List.of(
                new ConsumoPrevistoInsumo(9, UnidadeMedida.KG, new BigDecimal("0.250")),
                new ConsumoPrevistoInsumo(7, UnidadeMedida.G, new BigDecimal("700")),
                new ConsumoPrevistoInsumo(7, UnidadeMedida.KG, new BigDecimal("1.000"))));
        when(insumoRepository.findAllById(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(insumo7, insumo9));

        new BaixaProducaoService(itemPedidoRepository, insumoRepository, new ConversaoUnidadeService(),
                movimentacaoEstoqueService).baixarInsumos(pedido);

        InOrder ordem = inOrder(movimentacaoEstoqueService);
        ordem.verify(movimentacaoEstoqueService).registrarSaidaProducao(insumo7, new BigDecimal("1.700"), 42);
        ordem.verify(movimentacaoEstoqueService).registrarSaidaProducao(insumo9, new BigDecimal("0.250"), 42);
    }

    private Insumo insumo(Integer id) {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(id);
        insumo.setUnidade(UnidadeMedida.KG);
        return insumo;
    }
}
