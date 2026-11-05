package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsuarioEmailAlreadyExistsException extends RuntimeException {

    public UsuarioEmailAlreadyExistsException() {
        super("Já existe um usuário com este e-mail.");
    }
}
