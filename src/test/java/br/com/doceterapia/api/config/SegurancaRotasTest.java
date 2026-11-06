package br.com.doceterapia.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Segurança por grupo de rota")
class SegurancaRotasTest {

    static final List<RotasOrquestrador.Rota> ROTAS_DO_ORQUESTRADOR = RotasOrquestrador.ROTAS;

    @Autowired
    private MockMvc mockMvc;

    @Value("${seguranca.orquestrador.token}")
    private String token;

    private static MockHttpServletRequestBuilder requisicao(RotasOrquestrador.Rota rota) {
        String uri = rota.caminho().replaceAll("\\{[^}]+}", "1");
        return request(rota.metodo(), uri).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    private void assertNaoBarrada(MockHttpServletRequestBuilder requisicao) throws Exception {
        int status = mockMvc.perform(requisicao).andReturn().getResponse().getStatus();
        assertFalse(status == 401 || status == 403, "unexpected status " + status);
    }

    @ParameterizedTest
    @FieldSource("ROTAS_DO_ORQUESTRADOR")
    @DisplayName("Transição: as rotas do orquestrador seguem abertas com ou sem token")
    void transicao_rotasContinuamAbertas(RotasOrquestrador.Rota rota) throws Exception {
        assertNaoBarrada(requisicao(rota));
        assertNaoBarrada(requisicao(rota).header(OrquestradorTokenFilter.HEADER, token));
        assertNaoBarrada(requisicao(rota).header(OrquestradorTokenFilter.HEADER, "token-errado"));
    }

    @Test
    @DisplayName("Token válido em rota de usuário devolve 403 no formato de erro")
    void tokenValido_emRotaDeUsuario_retorna403() throws Exception {
        mockMvc.perform(get("/usuarios").header(OrquestradorTokenFilter.HEADER, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acesso negado para esta credencial."))
                .andExpect(jsonPath("$.path").value("/usuarios"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Sem credencial em rota de usuário devolve 401 no formato de erro")
    void semCredencial_emRotaDeUsuario_retorna401() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Autenticação necessária. Faça login para continuar."))
                .andExpect(jsonPath("$.path").value("/usuarios"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
