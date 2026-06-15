package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StatusTransitionInvalidException extends RuntimeException {

    public StatusTransitionInvalidException(String message) {
        super(message);
    }
}
