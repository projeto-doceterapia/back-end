package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Categoria possui insumos associados e não pode ser removida")
public class CategoriaInsumoHasInsumosException extends RuntimeException {
}
