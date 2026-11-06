package br.com.doceterapia.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;

import java.util.Arrays;

/**
 * Every rule that only the owner (DONA) passes lives here. These rules must be
 * registered before the generic "authenticated" rule, which would otherwise match first.
 * Routes not listed stay open to any signed-in user.
 */
public final class AutorizacaoPorPerfil {

    static final String PAPEL_DONA = "DONA";

    private static final String PARAMETRO_CONFIRMAR_COM_FALTA = "confirmarComFalta";
    private static final String PARAMETRO_STATUS_PEDIDO = "statusPedido";
    private static final String STATUS_CANCELADO = "CANCELADO";

    private AutorizacaoPorPerfil() {
    }

    public static void registrar(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                .requestMatchers(parametroIgual(PARAMETRO_CONFIRMAR_COM_FALTA, "true")).hasRole(PAPEL_DONA)
                .requestMatchers(RequestMatchers.allOf(
                        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.PATCH, "/pedidos/*/status"),
                        parametroIgual(PARAMETRO_STATUS_PEDIDO, STATUS_CANCELADO))).hasRole(PAPEL_DONA)
                .requestMatchers(HttpMethod.GET, "/indicadores/financeiros", "/usuarios").hasRole(PAPEL_DONA)
                .requestMatchers(HttpMethod.POST,
                        "/produtos", "/produtos/*/insumos",
                        "/cancelamentos/pedido/*",
                        "/configuracao-farol",
                        "/usuarios").hasRole(PAPEL_DONA)
                .requestMatchers(HttpMethod.PUT,
                        "/produtos/*", "/produtos/*/insumos/*",
                        "/cancelamentos/*",
                        "/configuracao-notificacao", "/configuracao-farol",
                        "/pagamentos/*").hasRole(PAPEL_DONA)
                .requestMatchers(HttpMethod.DELETE,
                        "/produtos/*", "/produtos/*/insumos/*",
                        "/cancelamentos/*",
                        "/clientes/*",
                        "/pedidos/*",
                        "/pagamentos/*").hasRole(PAPEL_DONA)
                .requestMatchers(HttpMethod.PATCH, "/clientes/*/reativar").hasRole(PAPEL_DONA)
                .requestMatchers("/configuracao-precificacao", "/configuracao-precificacao/**").hasRole(PAPEL_DONA);
    }

    // Looks at every repeated value and ignores case: Spring binds enums and booleans leniently,
    // so an exact comparison of the first value alone would let a variant slip through.
    private static RequestMatcher parametroIgual(String nome, String valor) {
        return (HttpServletRequest request) -> {
            String[] valores = request.getParameterValues(nome);
            return valores != null && Arrays.stream(valores).anyMatch(v -> valor.equalsIgnoreCase(v.trim()));
        };
    }
}
