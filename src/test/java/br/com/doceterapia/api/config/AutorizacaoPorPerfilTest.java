package br.com.doceterapia.api.config;

import br.com.doceterapia.api.controller.UsuarioController;
import br.com.doceterapia.api.dto.UsuarioDetalhesDto;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Autorização por perfil")
class AutorizacaoPorPerfilTest {

    private static final String MENSAGEM_403 = "Esta ação é permitida somente para o perfil DONA.";

    // Each row is a route only the owner may call. The routes of the last block do not exist
    // yet: the rule is checked before routing, so it already answers 403 for an employee.
    private static final String ROTAS_SO_DA_DONA = """
            GET,    /usuarios,
            POST,   /usuarios,
            POST,   /produtos,
            PUT,    /produtos/1,
            DELETE, /produtos/1,
            POST,   /produtos/1/insumos,
            PUT,    /produtos/1/insumos/1,
            DELETE, /produtos/1/insumos/1,
            POST,   /cancelamentos/pedido/1,
            PUT,    /cancelamentos/1,
            DELETE, /cancelamentos/1,
            PATCH,  /pedidos/1/status?statusPedido=CANCELADO,
            PATCH,  /pedidos/1/status?statusPedido=cancelado,
            PATCH,  /pedidos/1/status?statusPedido=EM_PRODUCAO&statusPedido=CANCELADO,
            POST,   /itens-pedido?confirmarComFalta=true,
            PATCH,  /pedidos/1/status?statusPedido=EM_PRODUCAO&confirmarComFalta=TRUE,
            PUT,    /pedidos/1?confirmarComFalta=true,
            DELETE, /clientes/1,
            DELETE, /pedidos/1,
            PUT,    /pagamentos/1,
            DELETE, /pagamentos/1,
            PUT,    /configuracao-notificacao,
            POST,   /configuracao-farol,
            PUT,    /configuracao-farol,
            GET,    /indicadores/financeiros,
            GET,    /configuracao-precificacao,
            PUT,    /configuracao-precificacao,
            PATCH,  /clientes/1/reativar
            """;

    private static final String ROTAS_OPERACIONAIS = """
            POST,   /pedidos,
            PUT,    /pedidos/1,
            PATCH,  /pedidos/1/status?statusPedido=EM_PRODUCAO,
            PATCH,  /pagamentos/1/registrar-sinal,
            PATCH,  /pagamentos/1/registrar-restante,
            GET,    /pagamentos,
            POST,   /estoque/movimentos,
            GET,    /estoque/movimentos,
            GET,    /produtos,
            GET,    /produtos/1/insumos,
            GET,    /indicadores/operacionais,
            GET,    /configuracao-farol,
            GET,    /configuracao-notificacao,
            GET,    /cancelamentos,
            GET,    /clientes,
            POST,   /clientes,
            PUT,    /clientes/1,
            GET,    /categorias-produto,
            POST,   /categorias-produto,
            PUT,    /categorias-produto/1,
            DELETE, /categorias-produto/1,
            POST,   /itens-pedido,
            POST,   /producoes
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static RequestPostProcessor sessaoDa(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuária Exemplo");
        usuario.setEmail("usuaria@exemplo.com");
        usuario.setSenha("hash-ficticio");
        usuario.setPerfil(perfil);
        return SecurityMockMvcRequestPostProcessors.user(new UsuarioDetalhesDto(usuario));
    }

    private static MockHttpServletRequestBuilder requisicao(HttpMethod metodo, String caminhoComQuery) {
        return request(metodo, caminhoComQuery.strip()).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    private static String caminho(String caminhoComQuery) {
        int interrogacao = caminhoComQuery.indexOf('?');
        return interrogacao < 0 ? caminhoComQuery : caminhoComQuery.substring(0, interrogacao);
    }

    static Stream<Arguments> rotasSoDaDona() {
        return linhas(ROTAS_SO_DA_DONA);
    }

    static Stream<Arguments> rotasOperacionais() {
        return linhas(ROTAS_OPERACIONAIS);
    }

    private static Stream<Arguments> linhas(String bloco) {
        return bloco.lines()
                .map(linha -> linha.strip().replaceAll(",$", "").split(",\\s*", 2))
                .map(partes -> Arguments.of(
                        HttpMethod.valueOf(partes[0]), partes[1].strip()));
    }

    // An exception thrown from the controller means the request got past the security chain.
    private void assertNaoBarrada(MockHttpServletRequestBuilder requisicao) throws Exception {
        int status;
        try {
            status = mockMvc.perform(requisicao).andReturn().getResponse().getStatus();
        } catch (ServletException e) {
            return;
        }
        assertFalse(status == 401 || status == 403, "unexpected status " + status);
    }

    @ParameterizedTest(name = "FUNCIONARIA {0} {1} devolve 403")
    @MethodSource("rotasSoDaDona")
    @DisplayName("FUNCIONARIA recebe 403 nas rotas só da DONA")
    void funcionariaRecebe403NasRotasSoDaDona(HttpMethod metodo, String caminhoComQuery) throws Exception {
        mockMvc.perform(requisicao(metodo, caminhoComQuery).with(sessaoDa(Perfil.FUNCIONARIA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(MENSAGEM_403))
                .andExpect(jsonPath("$.path").value(caminho(caminhoComQuery)));
    }

    @ParameterizedTest(name = "DONA {0} {1} passa")
    @MethodSource("rotasSoDaDona")
    @DisplayName("DONA passa pelas rotas só da DONA")
    void donaPassaPelasRotasSoDaDona(HttpMethod metodo, String caminhoComQuery) throws Exception {
        assertNaoBarrada(requisicao(metodo, caminhoComQuery).with(sessaoDa(Perfil.DONA)));
    }

    @ParameterizedTest(name = "FUNCIONARIA {0} {1} passa")
    @MethodSource("rotasOperacionais")
    @DisplayName("FUNCIONARIA usa as rotas operacionais")
    void funcionariaUsaAsRotasOperacionais(HttpMethod metodo, String caminhoComQuery) throws Exception {
        assertNaoBarrada(requisicao(metodo, caminhoComQuery).with(sessaoDa(Perfil.FUNCIONARIA)));
    }

    @ParameterizedTest(name = "{0} {1} sem credencial devolve 401")
    @MethodSource("rotasSoDaDona")
    @DisplayName("Sem credencial, as rotas só da DONA continuam devolvendo 401")
    void semCredencialDevolve401(HttpMethod metodo, String caminhoComQuery) throws Exception {
        mockMvc.perform(requisicao(metodo, caminhoComQuery))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "PATCH, /pedidos/1/status?statusPedido=ESPERANDO_FAZER",
            "PATCH, /pedidos/1/status?statusPedido=EM_PRODUCAO&confirmarComFalta=false",
            "GET, /pedidos?statusPedido=CANCELADO"
    })
    @DisplayName("Os matchers por parâmetro só pegam o valor perigoso")
    void parametroInofensivoNaoEBarrado(HttpMethod metodo, String caminhoComQuery) throws Exception {
        assertNaoBarrada(requisicao(metodo, caminhoComQuery).with(sessaoDa(Perfil.FUNCIONARIA)));
    }

    @Test
    @DisplayName("Perfil vem do banco a cada requisição, com o mesmo cookie")
    void perfilVemDoBancoACadaRequisicao() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Ajudante Exemplo");
        usuario.setEmail("ajudante@exemplo.com");
        usuario.setSenha(passwordEncoder.encode("SenhaFicticia1"));
        usuario.setPerfil(Perfil.FUNCIONARIA);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Cookie cookie = mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ajudante@exemplo.com\",\"senha\":\"SenhaFicticia1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie(UsuarioController.COOKIE_NOME);
        assertNotNull(cookie);

        mockMvc.perform(requisicao(HttpMethod.DELETE, "/clientes/1").cookie(cookie))
                .andExpect(status().isForbidden());

        usuario.setPerfil(Perfil.DONA);
        usuarioRepository.saveAndFlush(usuario);

        assertNaoBarrada(requisicao(HttpMethod.DELETE, "/clientes/1").cookie(cookie));

        usuario.setPerfil(Perfil.FUNCIONARIA);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(requisicao(HttpMethod.DELETE, "/clientes/1").cookie(cookie))
                .andExpect(status().isForbidden());
    }
}
