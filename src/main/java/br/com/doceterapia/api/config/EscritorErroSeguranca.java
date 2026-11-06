package br.com.doceterapia.api.config;

import br.com.doceterapia.api.dto.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes security rejections directly, instead of {@code sendError}, so they carry
 * the standard error body without going through the {@code /error} dispatch.
 */
@Component
public class EscritorErroSeguranca {

    private final ObjectMapper objectMapper;

    public EscritorErroSeguranca(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void escrever(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String mensagem)
            throws IOException {
        // An open SSE stream has already sent its headers and cannot change status anymore.
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.getWriter().write(objectMapper.writeValueAsString(
                ErroResponseDTO.de(status, mensagem, request.getRequestURI())));
    }
}
