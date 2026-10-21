package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.NotificacaoAgendaResponseDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.service.NotificacaoEmitterRegistry;
import br.com.doceterapia.api.service.NotificacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificacaoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
@DisplayName("Testes para NotificacaoController")
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacaoService service;

    @MockitoBean
    private NotificacaoEmitterRegistry emitterRegistry;

    @Test
    @DisplayName("Deve cadastrar notificação com tipo permitido e retornar 201")
    void deveCadastrarComTipoPermitido() throws Exception {
        NotificacaoResponseDTO response = new NotificacaoResponseDTO();
        response.setIdNotificacao(1);
        response.setTipo(TipoNotificacao.ESTOQUE_BAIXO);
        response.setStatusEnvio(StatusEnvioNotificacao.NAO_ENVIAR);
        when(service.cadastrar(any())).thenReturn(response);

        String body = """
                {"tipo":"ESTOQUE_BAIXO","titulo":"Estoque baixo","mensagem":"Farinha abaixo do minimo"}
                """;

        mockMvc.perform(post("/notificacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ESTOQUE_BAIXO"))
                .andExpect(jsonPath("$.statusEnvio").value("NAO_ENVIAR"));
    }

    @Test
    @DisplayName("Deve rejeitar tipo removido com 400 e mensagem em PT-BR")
    void deveRejeitarTipoRemovido() throws Exception {
        String body = """
                {"tipo":"PAGAMENTO_PENDENTE","titulo":"Teste","mensagem":"Teste"}
                """;

        mockMvc.perform(post("/notificacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Valores aceitos")))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("PAGAMENTO_PENDENTE")));
    }

    @Test
    @DisplayName("Deve listar notificações não lidas")
    void deveListarNaoLidas() throws Exception {
        NotificacaoResponseDTO n = new NotificacaoResponseDTO();
        n.setIdNotificacao(7);
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        when(service.listarNaoLidas()).thenReturn(List.of(n));

        mockMvc.perform(get("/notificacoes/nao-lidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idNotificacao").value(7));
    }

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void deveMarcarLida() throws Exception {
        NotificacaoResponseDTO n = new NotificacaoResponseDTO();
        n.setIdNotificacao(3);
        n.setLida(true);
        when(service.marcarLida(3)).thenReturn(n);

        mockMvc.perform(patch("/notificacoes/3/marcar-lida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lida").value(true));
    }

    @Test
    @DisplayName("GET /agenda deve retornar itens pendentes no intervalo")
    void deveListarAgenda() throws Exception {
        NotificacaoAgendaResponseDTO item = new NotificacaoAgendaResponseDTO();
        item.setIdNotificacao(10);
        item.setJanelaInicio(LocalDateTime.of(2026, 10, 21, 8, 0));
        item.setJanelaFim(LocalDateTime.of(2026, 10, 21, 8, 2));
        item.setTemplateKey("estoque_baixo");
        item.setParametros("{\"insumo\":\"Farinha\"}");
        when(service.listarAgenda(any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/notificacoes/agenda")
                        .param("de", "2026-10-21T00:00:00")
                        .param("ate", "2026-10-21T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idNotificacao").value(10))
                .andExpect(jsonPath("$[0].templateKey").value("estoque_baixo"))
                .andExpect(jsonPath("$[0].janelaInicio").value("2026-10-21T08:00:00"));
    }

    @Test
    @DisplayName("POST /confirmacao-envio deve registrar o envio")
    void deveConfirmarEnvio() throws Exception {
        NotificacaoResponseDTO response = new NotificacaoResponseDTO();
        response.setIdNotificacao(10);
        response.setStatusEnvio(StatusEnvioNotificacao.ENVIADA);
        response.setCanalMessageId("wamid.1");
        when(service.confirmarEnvio(eq(10), any())).thenReturn(response);

        String body = """
                {"statusEnvio":"ENVIADA","canalMessageId":"wamid.1"}
                """;

        mockMvc.perform(post("/notificacoes/10/confirmacao-envio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusEnvio").value("ENVIADA"))
                .andExpect(jsonPath("$.canalMessageId").value("wamid.1"));
    }
}
