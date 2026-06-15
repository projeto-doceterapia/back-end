package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Nome de categoria de insumo já cadastrado")
public class CategoriaInsumoNomeAlreadyExistsException extends RuntimeException {
}
