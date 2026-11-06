package br.com.doceterapia.api.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Filtro do token de serviço do orquestrador")
class OrquestradorTokenFilterTest {

    private static final String TOKEN = "token-ficticio-de-teste-0000000000000000";

    private MockHttpServletRequest request;
    private MockFilterChain chain;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest("GET", "/insumos/catalogo");
        chain = new MockFilterChain();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private static OrquestradorTokenFilter filtroCom(String tokenConfigurado) {
        SegurancaProperties propriedades = new SegurancaProperties();
        propriedades.getOrquestrador().setToken(tokenConfigurado);
        return new OrquestradorTokenFilter(propriedades);
    }

    private void executar(OrquestradorTokenFilter filtro) throws Exception {
        filtro.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "the chain must always continue");
    }

    @Test
    @DisplayName("Token correto autentica o principal de serviço")
    void tokenCorreto_autenticaPrincipalDeServico() throws Exception {
        request.addHeader(OrquestradorTokenFilter.HEADER, TOKEN);

        executar(filtroCom(TOKEN));

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacao);
        assertTrue(autenticacao.isAuthenticated());
        assertEquals("orquestrador", autenticacao.getName());
        assertEquals("ROLE_ORQUESTRADOR",
                autenticacao.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"token-errado", "token-ficticio-de-teste-000000000000000", ""})
    @DisplayName("Token errado, de tamanho diferente ou vazio não autentica")
    void tokenErrado_naoAutentica(String enviado) throws Exception {
        request.addHeader(OrquestradorTokenFilter.HEADER, enviado);

        executar(filtroCom(TOKEN));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Sem o header não autentica")
    void semHeader_naoAutentica() throws Exception {
        executar(filtroCom(TOKEN));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Token configurado em branco desliga a credencial, mesmo com header igual")
    void tokenConfiguradoEmBranco_naoAutentica(String configurado) throws Exception {
        request.addHeader(OrquestradorTokenFilter.HEADER, configurado == null ? "   " : configurado);

        executar(filtroCom(configurado));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Contexto já autenticado não é substituído")
    void contextoJaAutenticado_naoSubstitui() throws Exception {
        Authentication existente = new TestingAuthenticationToken("usuario", null, "ROLE_QUALQUER");
        SecurityContextHolder.getContext().setAuthentication(existente);
        request.addHeader(OrquestradorTokenFilter.HEADER, TOKEN);

        executar(filtroCom(TOKEN));

        assertSame(existente, SecurityContextHolder.getContext().getAuthentication());
    }
}
