package br.com.doceterapia.api.exception;

public abstract class RegraNegocioException extends RuntimeException {

    protected RegraNegocioException(String message) {
        super(message);
    }
}
