package br.com.doceterapia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT,
        reason = "O insumo possui movimentações de estoque e não pode ser excluído; inative-o em vez disso")
public class InsumoHasMovimentacoesException extends RuntimeException {
}
