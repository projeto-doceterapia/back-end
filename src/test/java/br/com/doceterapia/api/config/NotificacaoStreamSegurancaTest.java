package br.com.doceterapia.api.config;

import br.com.doceterapia.api.service.NotificacaoEmitterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Stream de notificações com segurança")
class NotificacaoStreamSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacaoEmitterRegistry registry;

    @Test
    @DisplayName("Com usuário logado o stream abre como event-stream e recebe eventos")
    void comUsuario_abreStream() throws Exception {
        int antes = registry.conexoesAtivas();

        MvcResult resultado = mockMvc.perform(get("/notificacoes/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .with(SecurityMockMvcRequestPostProcessors.user("dona@exemplo.com")))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertEquals(antes + 1, registry.conexoesAtivas());
        // The SSE headers are only written with the first event.
        registry.enviar("teste", "{}");
        assertEquals(MediaType.TEXT_EVENT_STREAM_VALUE, resultado.getResponse().getContentType());
        resultado.getRequest().getAsyncContext().complete();
    }

    @Test
    @DisplayName("Sem credencial o stream devolve 401 e não abre conexão")
    void semCredencial_devolve401() throws Exception {
        int antes = registry.conexoesAtivas();

        mockMvc.perform(get("/notificacoes/stream").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isUnauthorized());

        assertEquals(antes, registry.conexoesAtivas());
    }
}
