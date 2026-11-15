package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.FiltroInvalidoException;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.service.PedidoConsultaService;
import br.com.doceterapia.api.service.ProducaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProducaoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(br.com.doceterapia.api.exception.RequestValidationExceptionHandler.class)
class ProducaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProducaoService service;

    @MockitoBean
    private ProducaoMapper mapper;

    @MockitoBean
    private PedidoConsultaService consultaService;

    @Test
    void deveExigirPeriodoERecusarStatusInvalido() throws Exception {
        doThrow(new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."))
                .when(consultaService).listarProducoesComParametros(null, null, null);
        doReturn(List.of()).when(consultaService).listarProducoesComParametros(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null);
        doThrow(new FiltroInvalidoException("Status inválido para consulta.")).when(consultaService)
                .listarProducoesComParametros(eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30)), aryEq(new String[] {"INVALIDO"}));

        mockMvc.perform(get("/producoes")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/producoes").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/producoes").param("de", "2026-11-01").param("ate", "2026-11-30")
                        .param("status", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }
}
