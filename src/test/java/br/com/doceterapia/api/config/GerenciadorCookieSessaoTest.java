package br.com.doceterapia.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes para GerenciadorCookieSessao")
class GerenciadorCookieSessaoTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(GerenciadorCookieSessao.class)
            .withPropertyValues("jwt.validity=3600");

    @Test
    @DisplayName("Sem a propriedade, login e logout não marcam o cookie como Secure")
    void secureDesligadoPorPadrao() {
        contexto.run(ctx -> {
            GerenciadorCookieSessao gerenciador = ctx.getBean(GerenciadorCookieSessao.class);

            assertThat(gerenciador.criar("token-ficticio").toString()).doesNotContain("Secure");
            assertThat(gerenciador.remover().toString()).doesNotContain("Secure");
        });
    }

    @Test
    @DisplayName("Com a propriedade ligada, login e logout marcam o cookie como Secure")
    void secureLigadoPorPropriedade() {
        contexto.withPropertyValues("sessao.cookie.secure=true").run(ctx -> {
            GerenciadorCookieSessao gerenciador = ctx.getBean(GerenciadorCookieSessao.class);

            assertThat(gerenciador.criar("token-ficticio").toString()).contains("Secure");
            assertThat(gerenciador.remover().toString()).contains("Secure");
        });
    }

    @Test
    @DisplayName("O cookie de login carrega o token e dura o tempo do JWT")
    void criarUsaTokenEValidade() {
        String cookie = new GerenciadorCookieSessao(3600, false).criar("token-ficticio").toString();

        assertThat(cookie)
                .startsWith("authToken=token-ficticio")
                .contains("Max-Age=3600", "Path=/", "HttpOnly", "SameSite=Strict");
    }

    @Test
    @DisplayName("O cookie de logout expira na hora e mantém os mesmos atributos")
    void removerExpiraCookie() {
        String cookie = new GerenciadorCookieSessao(3600, false).remover().toString();

        assertThat(cookie)
                .startsWith("authToken=;")
                .contains("Max-Age=0", "Path=/", "HttpOnly", "SameSite=Strict");
    }
}
