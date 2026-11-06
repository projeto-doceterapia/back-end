package br.com.doceterapia.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AcessoNegadoHandler implements AccessDeniedHandler {

    private static final String MENSAGEM_ACESSO_NEGADO = "Acesso negado para esta credencial.";

    private final EscritorErroSeguranca escritorErro;

    public AcessoNegadoHandler(EscritorErroSeguranca escritorErro) {
        this.escritorErro = escritorErro;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escritorErro.escrever(request, response, HttpStatus.FORBIDDEN, MENSAGEM_ACESSO_NEGADO);
    }
}
