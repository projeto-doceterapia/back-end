package br.com.doceterapia.api.exception;

public class RestanteJaRegistradoException extends ConflitoEstadoException {
    public RestanteJaRegistradoException() {
        super("O restante deste pedido já foi registrado.");
    }
}
