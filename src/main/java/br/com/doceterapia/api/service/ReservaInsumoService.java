package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.repository.ConsumoPrevistoInsumo;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReservaInsumoService {

    private static final List<StatusPedido> STATUS_COM_RESERVA = List.of(
            StatusPedido.AGUARDANDO_SINAL, StatusPedido.ESPERANDO_FAZER);

    private final ItemPedidoRepository itemPedidoRepository;
    private final ConversaoUnidadeService conversaoUnidadeService;

    public ReservaInsumoService(ItemPedidoRepository itemPedidoRepository,
                                ConversaoUnidadeService conversaoUnidadeService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.conversaoUnidadeService = conversaoUnidadeService;
    }

    public Map<Integer, BigDecimal> disponivelPorInsumo(Collection<Insumo> insumos, Integer pedidoIgnorado) {
        Map<Integer, Insumo> porId = insumos.stream()
                .collect(Collectors.toMap(Insumo::getIdInsumo, Function.identity()));
        Map<Integer, BigDecimal> reservado = itemPedidoRepository.somarConsumoReservado(
                        STATUS_COM_RESERVA, porId.keySet(), pedidoIgnorado)
                .stream()
                .collect(Collectors.groupingBy(ConsumoPrevistoInsumo::insumoId, Collectors.reducing(BigDecimal.ZERO,
                        consumo -> conversaoUnidadeService.converter(consumo.quantidade(), consumo.unidade(),
                                porId.get(consumo.insumoId()).getUnidade()), BigDecimal::add)));
        return porId.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> saldo(entry.getValue()).subtract(reservado.getOrDefault(entry.getKey(), BigDecimal.ZERO))));
    }

    private BigDecimal saldo(Insumo insumo) {
        return insumo.getQuantidadeAtual() != null ? insumo.getQuantidadeAtual() : BigDecimal.ZERO;
    }
}
