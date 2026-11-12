package br.com.doceterapia.api.exception;

public abstract class ConflitoEstadoException extends RuntimeException {

    protected ConflitoEstadoException(String message) {
        super(message);
    }
}
