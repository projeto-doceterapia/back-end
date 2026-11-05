package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Sessão ausente ou expirada. Entre novamente.")
public class UsuarioLogadoNotFoundException extends RuntimeException {
}
