package br.com.doceterapia.api.exception;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class TotalAbaixoDoSinalPagoException extends RegraNegocioException {
    public TotalAbaixoDoSinalPagoException(BigDecimal total, BigDecimal sinal) {
        super("O total do pedido (R$ " + formatar(total) + ") não pode ficar abaixo do sinal já pago (R$ " + formatar(sinal) + ").");
    }

    private static String formatar(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }
}
