package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Nome de categoria já está em uso")
public class CategoriaProdutoNomeAlreadyExistsException extends RuntimeException {
}
