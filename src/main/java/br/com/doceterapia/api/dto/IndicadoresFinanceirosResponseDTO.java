package br.com.doceterapia.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresFinanceirosResponseDTO(
        PeriodoDTO periodo,
        FaturamentoDTO faturamento,
        BigDecimal ticketMedio,
        BigDecimal gastosInsumos,
        BigDecimal lucroEstimado,
        List<ProdutoValorDTO> produtosMaisVendidosPorFaturamento,
        CanceladosDTO cancelados,
        EmOrcamentoDTO emOrcamento,
        List<FaturamentoMesDTO> faturamentoPorMes) {
}
