package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoRequestDTO;
import br.com.doceterapia.api.dto.ConfiguracaoPrecificacaoResponseDTO;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.service.ConfiguracaoPrecificacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ConfiguracaoPrecificacaoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
@DisplayName("Testes para ConfiguracaoPrecificacaoController")
class ConfiguracaoPrecificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfiguracaoPrecificacaoService service;

    @Test
    @DisplayName("Deve devolver a configuração gravada")
    void buscarDevolveConfiguracao() throws Exception {
        when(service.buscar()).thenReturn(new ConfiguracaoPrecificacaoResponseDTO(
                new BigDecimal("100.00"), new BigDecimal("10.00")));

        mockMvc.perform(get("/configuracao-precificacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.margemPadrao").value(100.00))
                .andExpect(jsonPath("$.percentualIndiretos").value(10.00));
    }

    @Test
    @DisplayName("Deve devolver os valores salvos no PUT")
    void atualizarDevolveValoresSalvos() throws Exception {
        when(service.atualizar(any(ConfiguracaoPrecificacaoRequestDTO.class))).thenReturn(
                new ConfiguracaoPrecificacaoResponseDTO(new BigDecimal("100.00"), new BigDecimal("10.00")));

        mockMvc.perform(put("/configuracao-precificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"margemPadrao\": 100.00, \"percentualIndiretos\": 10.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.margemPadrao").value(100.00))
                .andExpect(jsonPath("$.percentualIndiretos").value(10.00));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "indiretos acima de 100|{\"margemPadrao\": 50, \"percentualIndiretos\": 100.01}|Percentual de custos indiretos deve estar entre 0 e 100%",
            "margem negativa|{\"margemPadrao\": -1, \"percentualIndiretos\": 10}|Margem padrão deve estar entre 0 e 999,99%",
            "margem nula|{\"percentualIndiretos\": 10}|Margem padrão é obrigatória",
            "indiretos nulo|{\"margemPadrao\": 50}|Percentual de custos indiretos é obrigatório",
            "margem com 3 casas|{\"margemPadrao\": 10.123, \"percentualIndiretos\": 10}|Margem padrão aceita no máximo 2 casas decimais",
            "indiretos com 3 casas|{\"margemPadrao\": 10, \"percentualIndiretos\": 10.123}|Percentual de custos indiretos aceita no máximo 2 casas decimais"
    })
    @DisplayName("Deve devolver 400 com mensagem em português fora dos limites")
    void foraDosLimitesDevolve400ComMensagem(String cenario, String corpo, String mensagem) throws Exception {
        mockMvc.perform(put("/configuracao-precificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(mensagem))
                .andExpect(jsonPath("$.path").value("/configuracao-precificacao"));

        verifyNoInteractions(service);
    }
}
