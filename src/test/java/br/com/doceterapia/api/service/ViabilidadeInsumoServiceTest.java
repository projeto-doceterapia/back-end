package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.InsumosInsuficientesException;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ViabilidadeInsumoServiceTest {
    @Mock private ProdutoInsumoRepository fichas;
    @Mock private DisponibilidadeInsumoService disponibilidade;

    @Test
    void converteGramasParaQuilosESomaItensDoMesmoInsumo() {
        Insumo chocolate = new Insumo();
        chocolate.setIdInsumo(7);
        chocolate.setNome("Chocolate");
        chocolate.setUnidade(UnidadeMedida.KG);
        Produto bolo = new Produto();
        bolo.setIdProduto(2);
        ProdutoInsumo ficha = new ProdutoInsumo();
        ficha.setProduto(bolo);
        ficha.setInsumo(chocolate);
        ficha.setQuantidadeUtilizada(new BigDecimal("200"));
        ficha.setUnidade(UnidadeMedida.G);
        ItemPedido item = new ItemPedido();
        item.setProduto(bolo);
        item.setQuantidade(new BigDecimal("6"));
        when(fichas.findComInsumoByProdutoIds(java.util.Set.of(2))).thenReturn(List.of(ficha));
        when(disponibilidade.disponivel(eq(chocolate), any(Pedido.class))).thenReturn(new BigDecimal("0.800"));

        ViabilidadeInsumoService service = new ViabilidadeInsumoService(fichas, new ConversaoUnidadeService(), disponibilidade);
        InsumosInsuficientesException erro = assertThrows(InsumosInsuficientesException.class,
                () -> service.exigirViabilidade(new Pedido(), List.of(item), false, "Faltam insumos."));

        assertEquals(new BigDecimal("1.200"), erro.getFaltas().getFirst().necessario());
        assertEquals(UnidadeMedida.KG, erro.getFaltas().getFirst().unidade());
    }
}
