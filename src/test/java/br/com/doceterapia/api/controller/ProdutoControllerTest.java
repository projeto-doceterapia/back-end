package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.service.ProdutoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProdutoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
@DisplayName("Testes para ProdutoController")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService service;

    private ProdutoResponseDTO produtoCalculado() {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setIdProduto(1);
        dto.setNome("Bolo de pote");
        dto.setCustoEstimado(new BigDecimal("5.21"));
        dto.setPrecoAtual(new BigDecimal("10.00"));
        dto.setPrecoSugerido(new BigDecimal("10.50"));
        return dto;
    }

    @Test
    @DisplayName("Deve devolver o produto com custo e preço sugerido calculados")
    void buscarPorIdDevolveCustoCalculado() throws Exception {
        when(service.buscarPorId(1)).thenReturn(produtoCalculado());

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProduto").value(1))
                .andExpect(jsonPath("$.custoEstimado").value(5.21))
                .andExpect(jsonPath("$.precoSugerido").value(10.50));
    }

    @Test
    @DisplayName("Deve devolver 404 para produto inexistente")
    void buscarPorIdInexistenteDevolve404() throws Exception {
        when(service.buscarPorId(999)).thenThrow(new ProdutoIdDontExistsException());

        mockMvc.perform(get("/produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar produtos com os valores calculados")
    void listarDevolveValoresCalculados() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(produtoCalculado()));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].custoEstimado").value(5.21));
    }

    @Test
    @DisplayName("Deve ignorar custo e preço sugerido enviados no corpo")
    void camposCalculadosNoCorpoSaoIgnorados() throws Exception {
        when(service.atualizar(eq(1), any(ProdutoRequestDTO.class))).thenReturn(produtoCalculado());

        mockMvc.perform(put("/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fkCategoriaProduto": 1, "nome": "Bolo de pote", "precoAtual": 10.00,
                                 "custoEstimado": 999.99, "precoSugerido": 888.88}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoEstimado").value(5.21))
                .andExpect(jsonPath("$.precoSugerido").value(10.50));
    }
}
