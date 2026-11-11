package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.service.PedidoService;
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

@WebMvcTest(controllers = PedidoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
class PedidoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService service;

    @Test
    void deveResponder409ParaTransicaoInvalida() throws Exception {
        when(service.atualizarStatus(1, br.com.doceterapia.api.enums.StatusPedido.ENTREGUE))
                .thenThrow(new StatusTransitionInvalidException("Transição de ORCAMENTO para ENTREGUE não é permitida"));
        mockMvc.perform(patch("/pedidos/1/status").param("statusPedido", "ENTREGUE"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Transição de ORCAMENTO para ENTREGUE não é permitida"))
                .andExpect(jsonPath("$.path").value("/pedidos/1/status"));
    }

    @Test
    void deveResponder422ParaNovaDataPassada() throws Exception {
        when(service.atualizar(eq(1), any())).thenThrow(new DataEntregaNoPassadoException());
        mockMvc.perform(put("/pedidos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"dataEntrega\":\"2026-01-01\"}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("A nova data de entrega não pode estar no passado."))
                .andExpect(jsonPath("$.path").value("/pedidos/1"));
    }

    @Test
    void deveResponder400ParaStatusMalformado() throws Exception {
        mockMvc.perform(patch("/pedidos/1/status").param("statusPedido", "INVALIDO"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/pedidos/1/status"));
    }
}

