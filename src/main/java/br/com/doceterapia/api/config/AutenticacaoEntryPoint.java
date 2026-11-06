package br.com.doceterapia.api.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    private static final String ROTA_USUARIO_LOGADO = "/usuarios/me";
    private static final String MENSAGEM_SESSAO_AUSENTE = "Sessão ausente ou expirada. Entre novamente.";
    private static final String MENSAGEM_AUTENTICACAO_NECESSARIA = "Autenticação necessária. Faça login para continuar.";
    private static final String MENSAGEM_ACESSO_NEGADO = "Acesso negado para esta credencial.";

    private final EscritorErroSeguranca escritorErro;

    public AutenticacaoEntryPoint(EscritorErroSeguranca escritorErro) {
        this.escritorErro = escritorErro;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        if (authException instanceof BadCredentialsException
                || authException instanceof InsufficientAuthenticationException) {
            escritorErro.escrever(request, response, HttpStatus.UNAUTHORIZED, mensagemNaoAutenticado(request));
        } else {
            escritorErro.escrever(request, response, HttpStatus.FORBIDDEN, MENSAGEM_ACESSO_NEGADO);
        }
    }

    // GET /usuarios/me is the session check, so its contract speaks of the session itself.
    private static String mensagemNaoAutenticado(HttpServletRequest request) {
        return ROTA_USUARIO_LOGADO.equals(request.getRequestURI())
                ? MENSAGEM_SESSAO_AUSENTE
                : MENSAGEM_AUTENTICACAO_NECESSARIA;
    }
}
