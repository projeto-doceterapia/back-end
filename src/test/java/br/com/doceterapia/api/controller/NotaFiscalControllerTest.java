package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.AutenticacaoFilter;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.dto.NotaFiscalStatusResponseDTO;
import br.com.doceterapia.api.enums.StatusNotaFiscal;
import br.com.doceterapia.api.exception.ChaveNotaFiscalInvalidaException;
import br.com.doceterapia.api.service.NotaFiscalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotaFiscalController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfiguracao.class, AutenticacaoFilter.class, AutenticacaoEntryPoint.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Contrato HTTP de /notas-fiscais/{chave}/status")
class NotaFiscalControllerTest {

    private static final String CHAVE = "35200114200166000187650010000000181000000012";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotaFiscalService service;

    @Test
    @DisplayName("Nota registrada retorna 200 com REGISTRADA e ids")
    void registrada() throws Exception {
        when(service.consultarStatus(CHAVE))
                .thenReturn(new NotaFiscalStatusResponseDTO(StatusNotaFiscal.REGISTRADA, List.of(1, 2)));

        mockMvc.perform(get("/notas-fiscais/{chave}/status", CHAVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTRADA"))
                .andExpect(jsonPath("$.insumoIds[0]").value(1))
                .andExpect(jsonPath("$.insumoIds[1]").value(2));
    }

    @Test
    @DisplayName("Chave inválida retorna 422")
    void invalida() throws Exception {
        when(service.consultarStatus(any())).thenThrow(new ChaveNotaFiscalInvalidaException());

        mockMvc.perform(get("/notas-fiscais/{chave}/status", "123"))
                .andExpect(status().isUnprocessableEntity());
    }
}
