package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.AutenticacaoFilter;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.ChaveIdempotenciaConflitanteException;
import br.com.doceterapia.api.exception.InsumoInativoException;
import br.com.doceterapia.api.exception.MovimentacaoInvalidaException;
import br.com.doceterapia.api.mapper.MovimentacaoEstoqueMapper;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService.ResultadoMovimentacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EstoqueMovimentoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfiguracao.class, AutenticacaoFilter.class, AutenticacaoEntryPoint.class}))
@AutoConfigureMockMvc(addFilters = false)
@Import(MovimentacaoEstoqueMapper.class)
@DisplayName("Contrato HTTP de /estoque/movimentos")
class EstoqueMovimentoControllerTest {

    private static final String CORPO = """
            {
              "insumoId": 1,
              "quantidadeInformada": 10.0,
              "unidadeInformada": "KG",
              "origem": "NOTA_IMAGEM"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimentacaoEstoqueService service;

    private MovimentacaoEstoque movimentacao(BigDecimal saldo) {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(1);
        insumo.setQuantidadeAtual(saldo);

        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setIdMovimentacao(1);
        m.setInsumo(insumo);
        m.setTipo(TipoMovimentacao.ENTRADA_COMPRA);
        m.setQuantidade(new BigDecimal("10.000"));
        m.setQuantidadeInformada(new BigDecimal("10.000"));
        m.setUnidadeInformada(UnidadeMedida.KG);
        m.setOrigem(OrigemMovimentacao.NOTA_IMAGEM);
        m.setOcorridoEm(LocalDateTime.now());
        return m;
    }

    @Test
    @DisplayName("Primeira requisição retorna 201 com saldo atualizado")
    void primeiraRequisicao201() throws Exception {
        when(service.registrarEntradaCompra(any()))
                .thenReturn(new ResultadoMovimentacao(movimentacao(new BigDecimal("10.000")), false));

        mockMvc.perform(post("/estoque/movimentos")
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimentacao").value(1))
                .andExpect(jsonPath("$.saldoAtual").value(10.000))
                .andExpect(jsonPath("$.duplicado").value(false));
    }

    @Test
    @DisplayName("Reenvio com mesma chave e conteúdo retorna 200 duplicado")
    void duplicado200() throws Exception {
        when(service.registrarEntradaCompra(any()))
                .thenReturn(new ResultadoMovimentacao(movimentacao(new BigDecimal("10.000")), true));

        mockMvc.perform(post("/estoque/movimentos")
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicado").value(true));
    }

    @Test
    @DisplayName("Chave ausente retorna 400")
    void chaveAusente400() throws Exception {
        mockMvc.perform(post("/estoque/movimentos")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Chave com conteúdo diferente retorna 409")
    void conflito409() throws Exception {
        when(service.registrarEntradaCompra(any())).thenThrow(new ChaveIdempotenciaConflitanteException());

        mockMvc.perform(post("/estoque/movimentos")
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Conteúdo inválido retorna 422")
    void invalido422() throws Exception {
        when(service.registrarEntradaCompra(any()))
                .thenThrow(new MovimentacaoInvalidaException("A quantidade informada deve ser maior que zero"));

        mockMvc.perform(post("/estoque/movimentos")
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Insumo inexistente ou inativo retorna 404")
    void insumoInativo404() throws Exception {
        when(service.registrarEntradaCompra(any())).thenThrow(new InsumoInativoException());

        mockMvc.perform(post("/estoque/movimentos")
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET lista o histórico de um insumo")
    void listaHistorico() throws Exception {
        when(service.listarHistorico(eq(1), any(), any()))
                .thenReturn(List.of(movimentacao(new BigDecimal("10.000"))));

        mockMvc.perform(get("/estoque/movimentos").param("insumoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMovimentacao").value(1))
                .andExpect(jsonPath("$[0].insumoId").value(1));
    }
}
