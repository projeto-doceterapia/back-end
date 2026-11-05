package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.GerenciadorCookieSessao;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.dto.UsuarioDetalhesDto;
import br.com.doceterapia.api.dto.UsuarioLogadoResponseDTO;
import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.exception.UsuarioEmailAlreadyExistsException;
import br.com.doceterapia.api.service.AutenticacaoService;
import br.com.doceterapia.api.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The real security chain is imported on purpose: excluding it, as other controller
// tests do, would make the anonymous-access cases pass without exercising the rules.
@WebMvcTest(controllers = UsuarioController.class)
@Import({
        SecurityConfiguracao.class,
        AutenticacaoEntryPoint.class,
        GerenciadorCookieSessao.class,
        RequestValidationExceptionHandler.class
})
@DisplayName("Testes para UsuarioController com segurança")
class UsuarioControllerTest {

    private static final String EMAIL = "dona@exemplo.com";
    private static final String MENSAGEM_401 = "Sessão ausente ou expirada. Entre novamente.";
    private static final String CADASTRO_VALIDO = """
            { "nome": "Funcionária Exemplo", "email": "funcionaria@exemplo.com", "senha": "SenhaFicticia1" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    private static RequestPostProcessor sessaoDaDona() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Dona Exemplo");
        usuario.setEmail(EMAIL);
        usuario.setSenha("hash-ficticio");
        return SecurityMockMvcRequestPostProcessors.user(new UsuarioDetalhesDto(usuario));
    }

    @Test
    @DisplayName("GET /usuarios/me com sessão devolve 200 sem a senha")
    void meComSessaoRetorna200() throws Exception {
        UsuarioLogadoResponseDTO dto = new UsuarioLogadoResponseDTO();
        dto.setId(1L);
        dto.setNome("Dona Exemplo");
        dto.setEmail(EMAIL);
        when(usuarioService.buscarUsuarioLogado(EMAIL)).thenReturn(dto);

        mockMvc.perform(get("/usuarios/me").with(sessaoDaDona()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Dona Exemplo"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("GET /usuarios/me sem sessão devolve 401 com mensagem em português")
    void meSemSessaoRetorna401() throws Exception {
        var resposta = mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();

        assertEquals(MENSAGEM_401, resposta.getErrorMessage());
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
    @DisplayName("POST /usuarios autenticado com e-mail novo devolve 201")
    void criarAutenticadoRetorna201() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(sessaoDaDona())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO_VALIDO))
                .andExpect(status().isCreated());

        verify(usuarioService).criar(any(Usuario.class));
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
}
