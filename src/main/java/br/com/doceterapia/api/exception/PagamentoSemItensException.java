package br.com.doceterapia.api.exception;

public class PagamentoSemItensException extends RegraNegocioException {
    public PagamentoSemItensException() {
        super("Não é possível registrar sinal para um pedido sem itens.");
    }
}
