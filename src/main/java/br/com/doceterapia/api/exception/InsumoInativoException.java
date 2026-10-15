package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InsumoInativoException extends RuntimeException {

    public InsumoInativoException() {
        super("Insumo não encontrado ou inativo");
    }
}
