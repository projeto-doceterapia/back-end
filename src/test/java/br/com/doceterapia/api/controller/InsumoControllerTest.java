package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.AutenticacaoFilter;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.dto.InsumoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.InsumoHasMovimentacoesException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.service.InsumoCatalogoService;
import br.com.doceterapia.api.service.InsumoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InsumoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfiguracao.class, AutenticacaoFilter.class, AutenticacaoEntryPoint.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Contrato HTTP de /insumos")
class InsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsumoService service;

    @MockitoBean
    private InsumoCatalogoService catalogoService;

    @MockitoBean
    private InsumoMapper insumoMapper;

    private InsumoResponseDTO respostaFarinha() {
        InsumoResponseDTO dto = new InsumoResponseDTO();
        dto.setIdInsumo(1);
        dto.setCategoriaId(1);
        dto.setCategoriaNome("Farináceos");
        dto.setNome("Farinha de trigo");
        dto.setQuantidadeAtual(new BigDecimal("10.500"));
        dto.setQuantidadeMinima(new BigDecimal("2.000"));
        dto.setUnidade(UnidadeMedida.KG);
        dto.setStatus(StatusAtivo.ATIVO);
        dto.setMarca("Dona Benta");
        dto.setCustoUnitario(new BigDecimal("15.5000"));
        return dto;
    }

    @Test
    @DisplayName("GET /insumos mantém o formato JSON esperado")
    void listaMantemContrato() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(new Insumo()));
        when(insumoMapper.toResponseDTO(any())).thenReturn(respostaFarinha());

        mockMvc.perform(get("/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idInsumo").value(1))
                .andExpect(jsonPath("$[0].categoriaId").value(1))
                .andExpect(jsonPath("$[0].categoriaNome").value("Farináceos"))
                .andExpect(jsonPath("$[0].nome").value("Farinha de trigo"))
                .andExpect(jsonPath("$[0].quantidadeAtual").value(10.500))
                .andExpect(jsonPath("$[0].quantidadeMinima").value(2.000))
                .andExpect(jsonPath("$[0].unidade").value("KG"))
                .andExpect(jsonPath("$[0].status").value("ATIVO"))
                .andExpect(jsonPath("$[0].marca").value("Dona Benta"))
                .andExpect(jsonPath("$[0].custoUnitario").value(15.5000));
    }

    @Test
    @DisplayName("POST /insumos aceita e devolve quantidadeAtual com status 201")
    void cadastraMantemContrato() throws Exception {
        when(service.cadastrar(any())).thenReturn(new Insumo());
        when(insumoMapper.toResponseDTO(any())).thenReturn(respostaFarinha());

        String corpo = """
                {
                  "fkCategoriaInsumo": 1,
                  "nome": "Farinha de trigo",
                  "quantidadeAtual": 10.5,
                  "quantidadeMinima": 2.0,
                  "unidade": "KG",
                  "status": "ATIVO",
                  "marca": "Dona Benta"
                }
                """;

        mockMvc.perform(post("/insumos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInsumo").value(1))
                .andExpect(jsonPath("$.quantidadeAtual").value(10.500));
    }

    @Test
    @DisplayName("DELETE /insumos de insumo com movimentações responde 409")
    void deleteComMovimentacoesResponde409() throws Exception {
        doThrow(new InsumoHasMovimentacoesException()).when(service).deletar(1);

        mockMvc.perform(delete("/insumos/1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /insumos sem movimentações responde 204")
    void deleteSemMovimentacoesResponde204() throws Exception {
        mockMvc.perform(delete("/insumos/1"))
                .andExpect(status().isNoContent());
    }
}
