package br.com.doceterapia.api.config;

import br.com.doceterapia.api.controller.UsuarioController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class GerenciadorCookieSessao {

    private final long validadeEmSegundos;
    private final boolean secure;

    public GerenciadorCookieSessao(@Value("${jwt.validity}") long validadeEmSegundos,
                                   @Value("${sessao.cookie.secure:false}") boolean secure) {
        this.validadeEmSegundos = validadeEmSegundos;
        this.secure = secure;
    }

    public ResponseCookie criar(String token) {
        return montar(token, Duration.ofSeconds(validadeEmSegundos));
    }

    public ResponseCookie remover() {
        return montar("", Duration.ZERO);
    }

    private ResponseCookie montar(String valor, Duration maxAge) {
        return ResponseCookie.from(UsuarioController.COOKIE_NOME, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
