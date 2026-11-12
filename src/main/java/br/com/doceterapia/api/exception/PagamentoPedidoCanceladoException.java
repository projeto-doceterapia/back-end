package br.com.doceterapia.api.exception;

public class PagamentoPedidoCanceladoException extends ConflitoEstadoException {
    public PagamentoPedidoCanceladoException() {
        super("Não é possível registrar pagamento de um pedido cancelado.");
    }
}
