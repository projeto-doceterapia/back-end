package br.com.doceterapia.api.exception;

public class DataEntregaNoPassadoException extends RegraNegocioException {
    public DataEntregaNoPassadoException() {
        super("A nova data de entrega não pode estar no passado.");
    }
}
