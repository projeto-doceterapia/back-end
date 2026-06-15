package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Já existe um cancelamento cadastrado para este pedido")
public class CancelamentoAlreadyExistsException extends RuntimeException {
}
