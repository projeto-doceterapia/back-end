package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.ProdutoInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.RequestValidationExceptionHandler;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
import br.com.doceterapia.api.mapper.ProdutoInsumoMapper;
import br.com.doceterapia.api.service.ProdutoInsumoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProdutoInsumoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
@DisplayName("Testes para ProdutoInsumoController")
class ProdutoInsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoInsumoService service;

    @MockitoBean
    private ProdutoInsumoMapper mapper;

    @Test
    @DisplayName("Deve devolver 422 no formato padrão para unidade incompatível")
    void unidadeIncompativelDevolve422NoFormatoPadrao() throws Exception {
        when(service.cadastrar(eq(1), any(ProdutoInsumoRequestDTO.class)))
                .thenThrow(new UnidadeIncompativelException(UnidadeMedida.G, UnidadeMedida.UNIDADE));

        mockMvc.perform(post("/produtos/1/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fkInsumo\": 3, \"quantidadeUtilizada\": 60.000, \"unidade\": \"G\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Unidade G é incompatível com a unidade UNIDADE do insumo"))
                .andExpect(jsonPath("$.path").value("/produtos/1/insumos"));
    }

    @Test
    @DisplayName("Deve devolver 404 ao remover item da ficha de outro produto")
    void deletarItemDeOutroProdutoDevolve404() throws Exception {
        doThrow(new ProdutoInsumoIdDontExistsException()).when(service).deletar(1, 9);

        mockMvc.perform(delete("/produtos/1/insumos/9"))
                .andExpect(status().isNotFound());
    }
}
