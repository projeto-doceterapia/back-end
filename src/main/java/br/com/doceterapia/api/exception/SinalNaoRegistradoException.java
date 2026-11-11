package br.com.doceterapia.api.exception;

public class SinalNaoRegistradoException extends ConflitoEstadoException {
    public SinalNaoRegistradoException() {
        super("Registre o sinal antes do restante.");
    }
}
