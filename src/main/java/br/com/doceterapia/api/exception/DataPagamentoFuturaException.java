package br.com.doceterapia.api.exception;

public class DataPagamentoFuturaException extends RegraNegocioException {
    public DataPagamentoFuturaException() {
        super("A data de pagamento não pode estar no futuro.");
    }
}
