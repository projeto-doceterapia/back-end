package br.com.doceterapia.api.config;

import br.com.doceterapia.api.entity.Usuario;
import br.com.doceterapia.api.enums.Perfil;
import br.com.doceterapia.api.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "bootstrap.usuario-inicial.email=dona@exemplo.com",
        "bootstrap.usuario-inicial.senha=SenhaFicticia1"
})
@AutoConfigureMockMvc
@DisplayName("Bootstrap do usuário inicial em H2")
class UsuarioInicialBootstrapIntegrationTest {

    @Autowired
    private UsuarioInicialBootstrap bootstrap;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rodar de novo não duplica e a credencial funciona no login")
    void bootstrapIdempotenteECredencialFunciona() throws Exception {
        // The runner already ran once during the context startup.
        bootstrap.run(null);

        List<Usuario> usuarios = usuarioRepository.findAll();
        assertEquals(1, usuarios.size());
        assertEquals("Dona", usuarios.get(0).getNome());
        assertEquals(Perfil.DONA, usuarios.get(0).getPerfil());
        assertTrue(usuarios.get(0).getSenha().startsWith("$2a$"));

        mockMvc.perform(post("/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "dona@exemplo.com", "senha": "SenhaFicticia1" }
                                """))
                .andExpect(status().isOk());
    }
}
