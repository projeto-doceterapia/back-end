package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoResponseDTO;
import br.com.doceterapia.api.exception.ConfiguracaoNotificacaoInvalidaException;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.service.ConfiguracaoNotificacaoService;
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ConfiguracaoNotificacaoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
@DisplayName("Testes para ConfiguracaoNotificacaoController")
class ConfiguracaoNotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfiguracaoNotificacaoService service;

    private static final String BODY_VALIDO = """
            {
              "inicioExpediente": "08:00",
              "fimExpediente": "18:00",
              "diasExpediente": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"],
              "horarioResumoInicio": "08:00",
              "horarioResumoFim": "17:30",
              "diasAntecedencia": 3,
              "resumoInicioDiaAtivo": true,
              "resumoFimDiaAtivo": true,
              "estoqueBaixoAtivo": true,
              "diaLotadoAtivo": true
            }
            """;

    private ConfiguracaoNotificacaoResponseDTO respostaPadrao() {
        ConfiguracaoNotificacaoResponseDTO dto = new ConfiguracaoNotificacaoResponseDTO();
        dto.setInicioExpediente(LocalTime.of(8, 0));
        dto.setFimExpediente(LocalTime.of(18, 0));
        dto.setDiasExpediente(List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
        dto.setHorarioResumoInicio(LocalTime.of(8, 0));
        dto.setHorarioResumoFim(LocalTime.of(17, 30));
        dto.setDiasAntecedencia(3);
        dto.setResumoInicioDiaAtivo(true);
        dto.setResumoFimDiaAtivo(true);
        dto.setEstoqueBaixoAtivo(true);
        dto.setDiaLotadoAtivo(true);
        return dto;
    }

    @Test
    @DisplayName("GET deve retornar a configuração no formato do contrato")
    void deveBuscar() throws Exception {
        when(service.buscar()).thenReturn(respostaPadrao());

        mockMvc.perform(get("/configuracao-notificacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicioExpediente").value("08:00"))
                .andExpect(jsonPath("$.fimExpediente").value("18:00"))
                .andExpect(jsonPath("$.diasExpediente[0]").value("MONDAY"))
                .andExpect(jsonPath("$.diasAntecedencia").value(3))
                .andExpect(jsonPath("$.estoqueBaixoAtivo").value(true))
                .andExpect(jsonPath("$.diaLotadoAtivo").value(true));
    }

    @Test
    @DisplayName("PUT válido deve retornar 200")
    void deveAtualizarValido() throws Exception {
        when(service.atualizar(any())).thenReturn(respostaPadrao());

        mockMvc.perform(put("/configuracao-notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarioResumoFim").value("17:30"));
    }

    @Test
    @DisplayName("PUT inválido deve retornar 422 com mensagem em PT-BR")
    void deveRejeitarComUnprocessable() throws Exception {
        when(service.atualizar(any())).thenThrow(
                new ConfiguracaoNotificacaoInvalidaException(
                        "O fim do expediente deve ser depois do início"));

        mockMvc.perform(put("/configuracao-notificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("expediente")));
    }
}
