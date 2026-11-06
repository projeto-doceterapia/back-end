package br.com.doceterapia.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Console do H2 só no perfil local")
class H2ConsoleSegurancaTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("local")
    @DisplayName("Perfil local")
    class PerfilLocal {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApplicationContext contexto;

        @Test
        @DisplayName("A cadeia do console existe e o caminho não exige credencial")
        void consoleAberto() throws Exception {
            assertTrue(contexto.containsBean("h2ConsoleFilterChain"));
            int status = mockMvc.perform(get("/h2-console/")).andReturn().getResponse().getStatus();
            assertFalse(status == 401 || status == 403, "unexpected status " + status);
            mockMvc.perform(get("/h2-console/")).andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @DisplayName("Sem o perfil local")
    class SemPerfilLocal {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApplicationContext contexto;

        @Test
        @DisplayName("A cadeia do console não existe e o caminho exige autenticação")
        void consoleFechado() throws Exception {
            assertFalse(contexto.containsBean("h2ConsoleFilterChain"));
            mockMvc.perform(get("/h2-console/")).andExpect(status().isUnauthorized());
        }
    }
}
