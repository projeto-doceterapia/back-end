package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.enums.StatusPedido;

import java.util.List;
import java.util.Map;

public record IndicadoresOperacionaisResponseDTO(
        PeriodoDTO periodo,
        Map<StatusPedido, Long> pedidosPorStatus,
        long pedidosHoje,
        long pedidosSemana,
        List<PedidosPorDiaDTO> pedidosPorDia,
        List<PedidosPorDiaDTO> diasLotados,
        List<ProdutoQuantidadeDTO> produtosMaisVendidos,
        List<InsumoAbaixoMinimoDTO> insumosAbaixoDoMinimo) {
}
