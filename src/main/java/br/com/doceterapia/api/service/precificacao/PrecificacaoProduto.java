package br.com.doceterapia.api.service.precificacao;

import java.math.BigDecimal;

/**
 * Cost and suggested price of one product. Cost and price are null when the recipe
 * does not allow a calculation.
 */
public record PrecificacaoProduto(BigDecimal custoEstimado, BigDecimal precoSugerido, BigDecimal margemAplicada) {

    public static PrecificacaoProduto indisponivel(BigDecimal margemAplicada) {
        return new PrecificacaoProduto(null, null, margemAplicada);
    }
}
