package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProducaoStatusTransitionException extends RuntimeException {

    public ProducaoStatusTransitionException(String message) {
        super(message);
    }
}
