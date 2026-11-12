package br.com.doceterapia.api.exception;

public class PedidoFinanceiramenteFechadoException extends ConflitoEstadoException {
    public PedidoFinanceiramenteFechadoException() {
        super("Não é possível alterar os itens deste pedido no estado atual.");
    }
}
