package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Item da ficha técnica não encontrado")
public class ProdutoInsumoIdDontExistsException extends RuntimeException {
}
