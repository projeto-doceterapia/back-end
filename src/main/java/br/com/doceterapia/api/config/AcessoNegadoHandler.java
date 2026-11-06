package br.com.doceterapia.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import br.com.doceterapia.api.enums.Perfil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AcessoNegadoHandler implements AccessDeniedHandler {

    private static final String MENSAGEM_ACESSO_NEGADO = "Acesso negado para esta credencial.";
    private static final String MENSAGEM_SOMENTE_DONA = "Esta ação é permitida somente para o perfil DONA.";
    private static final String AUTORIDADE_FUNCIONARIA = "ROLE_" + Perfil.FUNCIONARIA.name();

    private final EscritorErroSeguranca escritorErro;

    public AcessoNegadoHandler(EscritorErroSeguranca escritorErro) {
        this.escritorErro = escritorErro;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escritorErro.escrever(request, response, HttpStatus.FORBIDDEN, mensagemPara(SecurityContextHolder.getContext().getAuthentication()));
    }

    // The service principal keeps its own wording; only a signed-in employee is told which profile the action needs.
    private static String mensagemPara(Authentication autenticacao) {
        boolean funcionaria = autenticacao != null && autenticacao.getAuthorities().stream()
                .anyMatch(autoridade -> AUTORIDADE_FUNCIONARIA.equals(autoridade.getAuthority()));
        return funcionaria ? MENSAGEM_SOMENTE_DONA : MENSAGEM_ACESSO_NEGADO;
    }
}
