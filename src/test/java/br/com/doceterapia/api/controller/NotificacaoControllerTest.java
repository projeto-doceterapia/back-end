package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
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
}
