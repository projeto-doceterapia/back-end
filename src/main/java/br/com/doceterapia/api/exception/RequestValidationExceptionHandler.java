package br.com.doceterapia.api.exception;

import tools.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates malformed-request errors into the project's standard error body
 * with a PT-BR {@code message}. It only handles request parsing and bean
 * validation failures, which Spring already maps to 400, so existing endpoints
 * keep their status codes while the front-end gets a readable message.
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
