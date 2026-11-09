package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.AutenticacaoFilter;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.dto.CanceladosDTO;
import br.com.doceterapia.api.dto.EmOrcamentoDTO;
import br.com.doceterapia.api.dto.FaturamentoDTO;
import br.com.doceterapia.api.dto.IndicadoresFinanceirosResponseDTO;
import br.com.doceterapia.api.dto.IndicadoresOperacionaisResponseDTO;
import br.com.doceterapia.api.dto.PeriodoDTO;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.service.IndicadorService;
import br.com.doceterapia.api.service.PeriodoConsulta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IndicadorController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfiguracao.class, AutenticacaoFilter.class, AutenticacaoEntryPoint.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Contrato HTTP de /indicadores")
class IndicadorControllerTest {

    private static final String[] ROTAS = {"/indicadores/operacionais", "/indicadores/financeiros"};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndicadorService service;

    private void respostasVazias() {
        PeriodoDTO periodo = new PeriodoDTO(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        when(service.operacionais(any(PeriodoConsulta.class))).thenReturn(new IndicadoresOperacionaisResponseDTO(
                periodo, new EnumMap<>(StatusPedido.class), 0, 0, List.of(), List.of(), List.of(), List.of()));
        BigDecimal zero = new BigDecimal("0.00");
        when(service.financeiros(any(PeriodoConsulta.class))).thenReturn(new IndicadoresFinanceirosResponseDTO(
                periodo, new FaturamentoDTO(zero, zero, zero), zero, zero, zero, zero, List.of(),
                new CanceladosDTO(0, zero, zero, zero), new EmOrcamentoDTO(0, zero), List.of()));
    }

    @Test
    @DisplayName("Operacionais devolve os campos do contrato")
    void operacionaisDevolveContrato() throws Exception {
        respostasVazias();

        mockMvc.perform(get("/indicadores/operacionais").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo.de").value("2026-11-01"))
                .andExpect(jsonPath("$.periodo.ate").value("2026-11-30"))
                .andExpect(jsonPath("$.diasLotados").isArray())
                .andExpect(jsonPath("$.insumosAbaixoDoMinimo").isArray());
    }

    @Test
    @DisplayName("Financeiros devolve os campos do contrato")
    void financeirosDevolveContrato() throws Exception {
        respostasVazias();

        mockMvc.perform(get("/indicadores/financeiros").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamento.aReceber").value(0.0))
                .andExpect(jsonPath("$.cancelados.retido").value(0.0))
                .andExpect(jsonPath("$.emOrcamento.quantidade").value(0))
                .andExpect(jsonPath("$.faturamentoPorMes").isArray());
    }

    @Test
    @DisplayName("Período acima de 366 dias retorna 400 com a mensagem padrão")
    void periodoAcimaDe366Retorna400() throws Exception {
        for (String rota : ROTAS) {
            mockMvc.perform(get(rota).param("de", "2026-01-01").param("ate", "2027-01-03"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("O período máximo por consulta é de 366 dias."))
                    .andExpect(jsonPath("$.path").value(rota));
        }
    }

    @Test
    @DisplayName("Período de 366 dias exatos é aceito")
    void periodoDe366DiasAceito() throws Exception {
        respostasVazias();

        for (String rota : ROTAS) {
            mockMvc.perform(get(rota).param("de", "2026-01-01").param("ate", "2027-01-02"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Sem 'de' ou sem 'ate' retorna 400")
    void semDataRetorna400() throws Exception {
        for (String rota : ROTAS) {
            mockMvc.perform(get(rota).param("ate", "2026-11-30"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."));
            mockMvc.perform(get(rota).param("de", "2026-11-01"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd."));
        }
    }

    @Test
    @DisplayName("'de' depois de 'ate' retorna 400")
    void deDepoisDeAteRetorna400() throws Exception {
        for (String rota : ROTAS) {
            mockMvc.perform(get(rota).param("de", "2026-11-30").param("ate", "2026-11-01"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("A data 'de' deve ser anterior ou igual à data 'ate'."));
        }
    }

    @Test
    @DisplayName("Data mal formada retorna 400")
    void dataMalFormadaRetorna400() throws Exception {
        for (String rota : ROTAS) {
            mockMvc.perform(get(rota).param("de", "2026-13-01").param("ate", "2026-11-30"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'de': '2026-13-01'."));
        }
    }
}
