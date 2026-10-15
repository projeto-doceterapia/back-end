package br.com.doceterapia.api.exception;

import br.com.doceterapia.api.enums.UnidadeMedida;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnidadeIncompativelException extends RuntimeException {

    public UnidadeIncompativelException(UnidadeMedida informada, UnidadeMedida insumo) {
        super("Unidade " + informada + " é incompatível com a unidade " + insumo + " do insumo");
    }
}
