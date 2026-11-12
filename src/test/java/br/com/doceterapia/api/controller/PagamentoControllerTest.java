package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.service.PagamentoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PagamentoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
class PagamentoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoService service;

    @Test
    void deveResponder400SemFormaPagamento() throws Exception {
        mockMvc.perform(patch("/pagamentos/1/registrar-sinal").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Forma de pagamento é obrigatória"))
                .andExpect(jsonPath("$.path").value("/pagamentos/1/registrar-sinal"));
    }

    @Test
    void deveResponder409QuandoSinalRepetido() throws Exception {
        when(service.registrarSinal(eq(1), any())).thenThrow(new SinalJaRegistradoException());
        mockMvc.perform(patch("/pagamentos/1/registrar-sinal").contentType(MediaType.APPLICATION_JSON).content("{\"formaPagamento\":\"PIX\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("O sinal deste pedido já foi registrado."))
                .andExpect(jsonPath("$.path").value("/pagamentos/1/registrar-sinal"));
    }

    @Test
    void deveResponder409QuandoRestanteSemSinal() throws Exception {
        when(service.registrarRestante(eq(1), any())).thenThrow(new SinalNaoRegistradoException());
        mockMvc.perform(patch("/pagamentos/1/registrar-restante").contentType(MediaType.APPLICATION_JSON).content("{\"formaPagamento\":\"PIX\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Registre o sinal antes do restante."))
                .andExpect(jsonPath("$.path").value("/pagamentos/1/registrar-restante"));
    }

    @Test
    void deveExigirCorpoDeRegistro() throws Exception {
        mockMvc.perform(patch("/pagamentos/1/registrar-sinal"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void naoDeveExporRemocaoDePagamento() throws Exception {
        mockMvc.perform(delete("/pagamentos/1"))
                .andExpect(status().isMethodNotAllowed());
    }
}
