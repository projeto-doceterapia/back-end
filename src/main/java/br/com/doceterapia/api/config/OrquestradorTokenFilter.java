package br.com.doceterapia.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Authenticates the orchestrator by its static service token. This filter only
 * identifies the caller: a missing or wrong token continues unauthenticated and
 * the authorization rules decide the response.
 */
public class OrquestradorTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Orquestrador-Token";
    public static final String PRINCIPAL = "orquestrador";
    public static final String AUTORIDADE = "ROLE_ORQUESTRADOR";

    private static final Logger LOGGER = LoggerFactory.getLogger(OrquestradorTokenFilter.class);

    private final String tokenConfigurado;

    public OrquestradorTokenFilter(SegurancaProperties propriedades) {
        this.tokenConfigurado = propriedades.getOrquestrador().getToken();
        if (credencialDesligada()) {
            LOGGER.warn("orquestrador_token_em_branco service credential disabled");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenRecebido = request.getHeader(HEADER);

        if (tokenRecebido != null && !tokenRecebido.isEmpty() && !credencialDesligada()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (iguaisEmTempoConstante(tokenRecebido, tokenConfigurado)) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        PRINCIPAL, null, List.of(new SimpleGrantedAuthority(AUTORIDADE))));
            } else {
                LOGGER.warn("orquestrador_token_invalido path={}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean credencialDesligada() {
        return tokenConfigurado == null || tokenConfigurado.isBlank();
    }

    // Comparing digests keeps the token length from leaking through the comparison time.
    private static boolean iguaisEmTempoConstante(String recebido, String configurado) {
        return MessageDigest.isEqual(sha256(recebido), sha256(configurado));
    }

    private static byte[] sha256(String valor) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", e);
        }
    }
}
