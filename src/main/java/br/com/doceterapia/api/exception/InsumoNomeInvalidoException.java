package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised by get-or-create for a blank name or a name longer than 45 characters.
 * Mapped to 422 instead of the 400 that bean validation produces, because
 * get-or-create name violations are reported as {@code Unprocessable Entity}.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class InsumoNomeInvalidoException extends RuntimeException {

    public InsumoNomeInvalidoException(String message) {
        super(message);
    }
}
