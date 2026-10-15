package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ChaveIdempotenciaConflitanteException extends RuntimeException {

    public ChaveIdempotenciaConflitanteException() {
        super("A chave de idempotência já foi usada com um conteúdo diferente");
    }
}
