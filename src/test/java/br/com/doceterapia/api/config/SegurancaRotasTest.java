package br.com.doceterapia.api.config;

import br.com.doceterapia.api.controller.UsuarioController;
import br.com.doceterapia.api.dto.UsuarioDetalhesDto;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Segurança por grupo de rota")
class SegurancaRotasTest {

    static final List<RotasOrquestrador.Rota> ROTAS_DO_ORQUESTRADOR = RotasOrquestrador.ROTAS;

    private static final String EMAIL = "dona@exemplo.com";
    private static final String SENHA = "SenhaFicticia1";
    private static final String MENSAGEM_401 = "Autenticação necessária. Faça login para continuar.";
    private static final String MENSAGEM_401_SESSAO = "Sessão ausente ou expirada. Entre novamente.";
    private static final String MENSAGEM_403 = "Acesso negado para esta credencial.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${seguranca.orquestrador.token}")
    private String token;

    private static MockHttpServletRequestBuilder requisicao(HttpMethod metodo, String caminho) {
        String uri = caminho.replaceAll("\\{[^}]+}", "1");
        return request(metodo, uri).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    private static MockHttpServletRequestBuilder requisicao(RotasOrquestrador.Rota rota) {
        return requisicao(rota.metodo(), rota.caminho());
    }

    private static Usuario usuarioFicticio() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Dona Exemplo");
        usuario.setEmail(EMAIL);
        usuario.setSenha("hash-ficticio");
        usuario.setPerfil(Perfil.DONA);
        return usuario;
    }

    private void salvarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Dona Exemplo");
        usuario.setEmail(EMAIL);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setPerfil(Perfil.DONA);
        usuarioRepository.save(usuario);
    }

    private String corpoDeLogin() {
        return "{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}";
    }

    private void assertNaoBarrada(MockHttpServletRequestBuilder requisicao) throws Exception {
        int status = mockMvc.perform(requisicao).andReturn().getResponse().getStatus();
        assertFalse(status == 401 || status == 403, "unexpected status " + status);
    }

    @ParameterizedTest(name = "{0} {1} sem credencial devolve 401")
    @DisplayName("Sem credencial, cada grupo de rota de negócio devolve 401 no formato de erro")
    @CsvSource({
            "GET, /clientes", "GET, /pedidos", "GET, /pagamentos", "GET, /cancelamentos/pedido/1",
            "GET, /categorias-produto", "GET, /produtos", "GET, /produtos/1/insumos",
            "GET, /insumos", "GET, /itens-pedido", "GET, /producoes",
            "GET, /notificacoes", "GET, /notificacoes/stream", "GET, /configuracao-farol",
            "PUT, /configuracao-notificacao", "GET, /estoque/movimentos", "GET, /usuarios",
            "GET, /usuarios/me", "POST, /usuarios"
    })
    void semCredencial_retorna401(HttpMethod metodo, String caminho) throws Exception {
        mockMvc.perform(requisicao(metodo, caminho))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(
                        caminho.equals("/usuarios/me") ? MENSAGEM_401_SESSAO : MENSAGEM_401))
                .andExpect(jsonPath("$.path").value(caminho))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest
    @FieldSource("ROTAS_DO_ORQUESTRADOR")
    @DisplayName("Sem credencial, as rotas do orquestrador também devolvem 401")
    void rotasDoOrquestrador_semCredencial_retorna401(RotasOrquestrador.Rota rota) throws Exception {
        mockMvc.perform(requisicao(rota)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("O cookie emitido no login libera rotas de negócio e as do orquestrador")
    void cookieDeUsuario_liberaRotas() throws Exception {
        salvarUsuario();

        Cookie cookie = mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoDeLogin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie(UsuarioController.COOKIE_NOME);
        assertNotNull(cookie);

        assertNaoBarrada(get("/clientes").cookie(cookie));
        assertNaoBarrada(get("/usuarios").cookie(cookie));
        assertNaoBarrada(requisicao(HttpMethod.PUT, "/configuracao-notificacao").cookie(cookie));
        assertNaoBarrada(requisicao(RotasOrquestrador.ROTAS.get(0)).cookie(cookie));
    }

    @ParameterizedTest
    @FieldSource("ROTAS_DO_ORQUESTRADOR")
    @DisplayName("Usuário logado acessa as 9 rotas do orquestrador")
    void usuarioLogado_liberaRotasDoOrquestrador(RotasOrquestrador.Rota rota) throws Exception {
        assertNaoBarrada(requisicao(rota)
                .with(SecurityMockMvcRequestPostProcessors.user(new UsuarioDetalhesDto(usuarioFicticio()))));
    }

    @ParameterizedTest
    @FieldSource("ROTAS_DO_ORQUESTRADOR")
    @DisplayName("Token válido libera cada uma das 9 rotas do orquestrador")
    void tokenValido_liberaRotasDoOrquestrador(RotasOrquestrador.Rota rota) throws Exception {
        assertNaoBarrada(requisicao(rota).header(OrquestradorTokenFilter.HEADER, token));
    }

    @ParameterizedTest(name = "{0} {1} com token devolve 403")
    @DisplayName("Token válido fora das rotas do orquestrador devolve 403 no formato de erro")
    @CsvSource({
            "GET, /clientes", "PUT, /configuracao-notificacao", "DELETE, /insumos/1",
            "GET, /estoque/movimentos", "GET, /notificacoes/stream", "GET, /usuarios",
            "GET, /usuarios/me", "POST, /usuarios", "GET, /pedidos", "PUT, /notificacoes/1"
    })
    void tokenValido_foraDasRotas_retorna403(HttpMethod metodo, String caminho) throws Exception {
        mockMvc.perform(requisicao(metodo, caminho).header(OrquestradorTokenFilter.HEADER, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(MENSAGEM_403))
                .andExpect(jsonPath("$.path").value(caminho));
    }

    @ParameterizedTest
    @FieldSource("ROTAS_DO_ORQUESTRADOR")
    @DisplayName("Token inválido sem cookie devolve 401 e o valor enviado não vai ao log")
    void tokenInvalido_retorna401(RotasOrquestrador.Rota rota, CapturedOutput saida) throws Exception {
        String enviado = "valor-enviado-que-nao-deve-aparecer";

        mockMvc.perform(requisicao(rota).header(OrquestradorTokenFilter.HEADER, enviado))
                .andExpect(status().isUnauthorized());

        assertTrue(saida.getAll().contains("orquestrador_token_invalido"));
        assertFalse(saida.getAll().contains(enviado));
        assertFalse(saida.getAll().contains(token));
    }

    @Test
    @DisplayName("Rotas públicas respondem sem credencial")
    void rotasPublicas_semCredencial() throws Exception {
        salvarUsuario();

        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(corpoDeLogin()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/usuarios/logout")).andExpect(status().isNoContent());
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Logout com cookie vencido ou inválido segue devolvendo 204")
    void logoutComCookieInvalido_retorna204() throws Exception {
        mockMvc.perform(post("/usuarios/logout").cookie(new Cookie(UsuarioController.COOKIE_NOME, "vencido")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Rotas do actuator fora do health seguem protegidas")
    void actuatorForaDoHealth_exigeLogin() throws Exception {
        mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }
}
