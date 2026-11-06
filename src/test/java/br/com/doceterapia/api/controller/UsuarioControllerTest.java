package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AcessoNegadoHandler;
import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.EscritorErroSeguranca;
import br.com.doceterapia.api.config.GerenciadorCookieSessao;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.config.SegurancaProperties;
import br.com.doceterapia.api.dto.UsuarioDetalhesDto;
import br.com.doceterapia.api.dto.UsuarioListarDto;
import br.com.doceterapia.api.dto.UsuarioLogadoResponseDTO;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.exception.UsuarioEmailAlreadyExistsException;
import br.com.doceterapia.api.service.AutenticacaoService;
import br.com.doceterapia.api.service.UsuarioService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The real security chain is imported on purpose: excluding it, as other controller
// tests do, would make the anonymous-access cases pass without exercising the rules.
@WebMvcTest(controllers = UsuarioController.class)
@Import({
        SecurityConfiguracao.class,
        SegurancaProperties.class,
        AcessoNegadoHandler.class,
        AutenticacaoEntryPoint.class,
        EscritorErroSeguranca.class,
        GerenciadorCookieSessao.class,
        RequestValidationExceptionHandler.class
})
@DisplayName("Testes para UsuarioController com segurança")
class UsuarioControllerTest {

    private static final String EMAIL = "dona@exemplo.com";
    private static final String MENSAGEM_401 = "Sessão ausente ou expirada. Entre novamente.";
    private static final String CADASTRO_VALIDO = """
            { "nome": "Funcionária Exemplo", "email": "funcionaria@exemplo.com", "senha": "SenhaFicticia1",
              "perfil": "FUNCIONARIA" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static RequestPostProcessor sessaoDa(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuária Exemplo");
        usuario.setEmail(EMAIL);
        usuario.setSenha("hash-ficticio");
        usuario.setPerfil(perfil);
        return SecurityMockMvcRequestPostProcessors.user(new UsuarioDetalhesDto(usuario));
    }

    private static RequestPostProcessor sessaoDaDona() {
        return sessaoDa(Perfil.DONA);
    }

    @Test
    @DisplayName("GET /usuarios/me com sessão devolve 200 com o perfil e sem a senha")
    void meComSessaoRetorna200() throws Exception {
        UsuarioLogadoResponseDTO dto = new UsuarioLogadoResponseDTO();
        dto.setId(1L);
        dto.setNome("Dona Exemplo");
        dto.setEmail(EMAIL);
        dto.setPerfil(Perfil.DONA);
        when(usuarioService.buscarUsuarioLogado(EMAIL)).thenReturn(dto);

        mockMvc.perform(get("/usuarios/me").with(sessaoDaDona()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Dona Exemplo"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.perfil").value("DONA"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios/me de uma FUNCIONARIA devolve o perfil dela")
    void meDevolveOPerfil() throws Exception {
        UsuarioLogadoResponseDTO dto = new UsuarioLogadoResponseDTO();
        dto.setId(2L);
        dto.setNome("Ajudante Exemplo");
        dto.setEmail(EMAIL);
        dto.setPerfil(Perfil.FUNCIONARIA);
        when(usuarioService.buscarUsuarioLogado(EMAIL)).thenReturn(dto);

        mockMvc.perform(get("/usuarios/me").with(sessaoDa(Perfil.FUNCIONARIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIA"));
    }

    @Test
    @DisplayName("GET /usuarios/me sem sessão devolve 401 com mensagem em português")
    void meSemSessaoRetorna401() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(MENSAGEM_401))
                .andExpect(jsonPath("$.path").value("/usuarios/me"));
    }

    @Test
    @DisplayName("POST /usuarios sem sessão devolve 401 e não grava")
    void criarSemSessaoRetorna401() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO_VALIDO))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).criar(any(Usuario.class));
    }

    @Test
    @DisplayName("DONA cria FUNCIONARIA e recebe 201 sem corpo")
    void donaCriaFuncionaria() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        ArgumentCaptor<Usuario> criado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).criar(criado.capture());
        assertEquals(Perfil.FUNCIONARIA, criado.getValue().getPerfil());
        assertEquals("funcionaria@exemplo.com", criado.getValue().getEmail());
    }

    @Test
    @DisplayName("POST /usuarios com e-mail duplicado devolve 409 no formato compartilhado")
    void criarEmailDuplicadoRetorna409() throws Exception {
        doThrow(new UsuarioEmailAlreadyExistsException()).when(usuarioService).criar(any(Usuario.class));

        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Já existe um usuário com este e-mail."))
                .andExpect(jsonPath("$.path").value("/usuarios"));
    }

    @Test
    @DisplayName("Perfil ausente ou inválido devolve 400 em português")
    void perfilAusenteOuInvalidoDevolve400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Funcionária Exemplo", "email": "funcionaria@exemplo.com", "senha": "SenhaFicticia1" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Perfil é obrigatório"));

        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Funcionária Exemplo", "email": "funcionaria@exemplo.com", "senha": "SenhaFicticia1",
                                  "perfil": "GERENTE" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Tipo inválido: 'GERENTE'. Valores aceitos: DONA, FUNCIONARIA"));

        verify(usuarioService, never()).criar(any(Usuario.class));
    }

    @Test
    @DisplayName("GET /usuarios da DONA lista com o perfil de cada um")
    void listaComPerfil() throws Exception {
        UsuarioListarDto dona = new UsuarioListarDto();
        dona.setId(1L);
        dona.setNome("Dona Exemplo");
        dona.setEmail("dona@exemplo.com");
        dona.setPerfil(Perfil.DONA);
        UsuarioListarDto ajudante = new UsuarioListarDto();
        ajudante.setId(2L);
        ajudante.setNome("Ajudante Exemplo");
        ajudante.setEmail("ajudante@exemplo.com");
        ajudante.setPerfil(Perfil.FUNCIONARIA);
        when(usuarioService.listarTodos()).thenReturn(List.of(dona, ajudante));

        mockMvc.perform(get("/usuarios").with(sessaoDaDona()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].perfil").value("DONA"))
                .andExpect(jsonPath("$[1].perfil").value("FUNCIONARIA"))
                .andExpect(jsonPath("$[1].senha").doesNotExist());
    }

    @Test
    @DisplayName("POST /usuarios com nome vazio devolve 400 com mensagem em português")
    void criarComDadosInvalidosRetorna400EmPortugues() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "", "email": "funcionaria@exemplo.com", "senha": "SenhaFicticia1",
                                  "perfil": "FUNCIONARIA" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(anyOf(
                        is("Informe o nome."),
                        is("O nome deve ter de 3 a 50 caracteres."))));

        verify(usuarioService, never()).criar(any(Usuario.class));
    }

    @Test
    @DisplayName("POST /usuarios sem e-mail ou com senha curta devolve 400 com mensagem em português")
    void criarSemEmailOuComSenhaCurtaRetorna400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Funcionária Exemplo", "senha": "SenhaFicticia1", "perfil": "FUNCIONARIA" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe o e-mail."));

        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Funcionária Exemplo", "email": "funcionaria@exemplo.com", "senha": "123",
                                  "perfil": "FUNCIONARIA" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha deve ter de 6 a 20 caracteres."));
    }

    @Test
    @DisplayName("POST /usuarios/logout sem sessão ou com token expirado devolve 204 e limpa o cookie")
    void logoutSemSessaoLimpaCookie() throws Exception {
        mockMvc.perform(post("/usuarios/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        String tokenExpirado = Jwts.builder()
                .subject(EMAIL)
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .compact();

        mockMvc.perform(post("/usuarios/logout").cookie(new Cookie(UsuarioController.COOKIE_NOME, tokenExpirado)))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
