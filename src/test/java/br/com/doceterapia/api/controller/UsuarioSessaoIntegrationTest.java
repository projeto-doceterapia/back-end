package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Sessão real: login, cookie e usuário logado")
class UsuarioSessaoIntegrationTest {

    private static final String EMAIL = "dona@exemplo.com";
    private static final String SENHA = "SenhaFicticia1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("O cookie emitido no login autentica GET /usuarios/me")
    void cookieDoLoginAutenticaMe() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Dona Exemplo");
        usuario.setEmail(EMAIL);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setPerfil(Perfil.DONA);
        Long id = usuarioRepository.save(usuario).getId();

        Cookie cookie = mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(UsuarioController.COOKIE_NOME);
        assertNotNull(cookie);

        mockMvc.perform(get("/usuarios/me").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Dona Exemplo"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }
}
