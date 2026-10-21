package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ChaveNotaFiscalInvalidaException extends RuntimeException {

    public ChaveNotaFiscalInvalidaException() {
        super("A chave da nota fiscal deve ter 44 dígitos com dígito verificador válido");
    }
}
