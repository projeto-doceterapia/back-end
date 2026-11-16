package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.repository.ConsumoPrevistoInsumo;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BaixaProducaoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final InsumoRepository insumoRepository;
    private final ConversaoUnidadeService conversaoUnidadeService;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public BaixaProducaoService(ItemPedidoRepository itemPedidoRepository, InsumoRepository insumoRepository,
                                ConversaoUnidadeService conversaoUnidadeService,
                                MovimentacaoEstoqueService movimentacaoEstoqueService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.insumoRepository = insumoRepository;
        this.conversaoUnidadeService = conversaoUnidadeService;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    public void baixarInsumos(Pedido pedido) {
        List<ConsumoPrevistoInsumo> consumosPrevistos = itemPedidoRepository.somarConsumoPorPedido(pedido.getIdPedido());
        Map<Integer, Insumo> insumos = insumoRepository.findAllById(consumosPrevistos.stream()
                        .map(ConsumoPrevistoInsumo::insumoId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Insumo::getIdInsumo, Function.identity()));
        Map<Integer, BigDecimal> consumos = consumosPrevistos.stream()
                .collect(Collectors.groupingBy(ConsumoPrevistoInsumo::insumoId, Collectors.reducing(BigDecimal.ZERO,
                        consumo -> converter(consumo, insumos), BigDecimal::add)));
        consumos.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> movimentacaoEstoqueService.registrarSaidaProducao(
                        insumos.get(entry.getKey()), entry.getValue().setScale(3, RoundingMode.HALF_EVEN), pedido.getIdPedido()));
    }

    private BigDecimal converter(ConsumoPrevistoInsumo consumo, Map<Integer, Insumo> insumos) {
        Insumo insumo = insumos.get(consumo.insumoId());
        if (insumo == null) {
            throw new IllegalStateException("Insumo da ficha técnica não foi encontrado");
        }
        return conversaoUnidadeService.converter(consumo.quantidade(), consumo.unidade(), insumo.getUnidade());
    }
}
