package school.sptech.projetoindividual.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Telefone já está associado a outro cliente")
public class TelefoneAlreadyExistsException extends RuntimeException {
}
