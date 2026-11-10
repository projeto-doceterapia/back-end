package br.com.doceterapia.api.exception;

import br.com.doceterapia.api.dto.CooldownErroResponseDTO;
import br.com.doceterapia.api.dto.ErroResponseDTO;
import tools.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates request parsing and bean validation failures, which Spring already
 * maps to 400, and the domain errors listed below into the project's standard
 * error body with a PT-BR {@code message}.
 */
@RestControllerAdvice
public class RequestValidationExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Corpo da requisição inválido ou mal formatado";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalid
                && invalid.getTargetType() != null
                && invalid.getTargetType().isEnum()) {
            message = mensagemValorEnumInvalido(invalid);
        }

        return construir(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Dados inválidos fornecidos");
        return construir(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(FiltroInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleFiltroInvalido(
            FiltroInvalidoException ex, HttpServletRequest request) {
        return erro(ex.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> handleParametroAusente(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String nome = ex.getParameterName();
        String message = "de".equals(nome) || "ate".equals(nome)
                ? "Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."
                : "O parâmetro '" + nome + "' é obrigatório.";
        return erro(message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> handleParametroInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return erro("Valor inválido para o parâmetro '" + ex.getName() + "': '" + ex.getValue() + "'.", request);
    }

    @ExceptionHandler(ConfiguracaoNotificacaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleConfiguracaoInvalida(
            ConfiguracaoNotificacaoInvalidaException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(UnidadeIncompativelException.class)
    public ResponseEntity<Map<String, Object>> handleUnidadeIncompativel(
            UnidadeIncompativelException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(UsuarioEmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioEmailDuplicado(
            UsuarioEmailAlreadyExistsException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(NotificacaoTesteCooldownException.class)
    public ResponseEntity<CooldownErroResponseDTO> handleNotificacaoTesteCooldown(
            NotificacaoTesteCooldownException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(new CooldownErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getSegundosRestantes()));
    }

    private ResponseEntity<ErroResponseDTO> erro(String message, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ErroResponseDTO.de(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
    }

    private String mensagemValorEnumInvalido(InvalidFormatException invalid) {
        String valor = String.valueOf(invalid.getValue());
        String aceitos = Arrays.stream(invalid.getTargetType().getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return "Tipo inválido: '" + valor + "'. Valores aceitos: " + aceitos;
    }

    private ResponseEntity<Map<String, Object>> construir(
            HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
