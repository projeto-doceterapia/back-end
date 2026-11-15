package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.FiltroInvalidoException;
import br.com.doceterapia.api.service.CancelamentoPedidoService;
import br.com.doceterapia.api.service.PedidoConsultaService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CancelamentoPedidoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(br.com.doceterapia.api.exception.RequestValidationExceptionHandler.class)
class CancelamentoPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CancelamentoPedidoService service;

    @MockitoBean
    private PedidoConsultaService consultaService;

    @Test
    void deveExigirPeriodo() throws Exception {
        when(consultaService.listarCancelamentos(null, null))
                .thenThrow(new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."));
        when(consultaService.listarCancelamentos(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of());

        mockMvc.perform(get("/cancelamentos")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/cancelamentos").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk());
    }
}
