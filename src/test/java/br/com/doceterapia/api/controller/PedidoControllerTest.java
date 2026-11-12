package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.service.PedidoService;
import br.com.doceterapia.api.service.PedidoConsultaService;
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
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;
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

    @MockitoBean
    private PedidoConsultaService consultaService;

    @Test
    void deveRecusarPeriodoAusente() throws Exception {
        doThrow(new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."))
                .when(consultaService).listarPedidosComParametros(null, null, null, null);
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."));
    }

    @Test
    void deveAceitarPeriodoStatusCsvEStatusRepetido() throws Exception {
        doReturn(java.util.List.of()).when(consultaService).listarPedidosComParametros(
                eq(java.time.LocalDate.of(2026, 11, 1)), eq(java.time.LocalDate.of(2026, 11, 30)), any(String[].class), isNull());

        mockMvc.perform(get("/pedidos")
                        .param("de", "2026-11-01")
                        .param("ate", "2026-11-30")
                        .param("status", "EM_PRODUCAO,EM_PRODUCAO")
                        .param("status", "ENTREGUE"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarClienteSemPeriodo() throws Exception {
        doReturn(java.util.List.of()).when(consultaService).listarPedidosComParametros(isNull(), isNull(), isNull(), eq(7));

        mockMvc.perform(get("/pedidos").param("clienteId", "7"))
                .andExpect(status().isOk());
        verify(consultaService).listarPedidosComParametros(null, null, null, 7);
    }

    @Test
    void deveRecusarPeriodoParcialEnumEDataInvalidos() throws Exception {
        doThrow(new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd.")).when(consultaService)
                .listarPedidosComParametros(java.time.LocalDate.of(2026, 11, 1), null, null, 7);
        doThrow(new FiltroInvalidoException("Status inválido para consulta.")).when(consultaService).listarPedidosComParametros(
                eq(java.time.LocalDate.of(2026, 11, 1)), eq(java.time.LocalDate.of(2026, 11, 30)),
                aryEq(new String[] {"INEXISTENTE"}), isNull());

        mockMvc.perform(get("/pedidos").param("clienteId", "7").param("de", "2026-11-01"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/pedidos").param("de", "2026-11-01").param("ate", "2026-11-30")
                        .param("status", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/pedidos").param("de", "2026-11-31").param("ate", "2026-11-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarPedidoPorIdOuRetornar404() throws Exception {
        when(consultaService.buscarPedido(1)).thenReturn(new br.com.doceterapia.api.dto.PedidoResponseDTO());
        when(consultaService.buscarPedido(404)).thenThrow(new PedidoIdDontExistsException());

        mockMvc.perform(get("/pedidos/1")).andExpect(status().isOk());
        mockMvc.perform(get("/pedidos/404")).andExpect(status().isNotFound());
    }

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
