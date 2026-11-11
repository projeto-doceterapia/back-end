package br.com.doceterapia.api.exception;

public class SinalJaRegistradoException extends ConflitoEstadoException {
    public SinalJaRegistradoException() {
        super("O sinal deste pedido já foi registrado.");
    }
}
