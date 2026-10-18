package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.dto.InsumoCatalogoItemDTO;
import br.com.doceterapia.api.dto.InsumoCatalogoResponseDTO;
import br.com.doceterapia.api.dto.InsumoResponseDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.CursorInvalidoException;
import br.com.doceterapia.api.exception.InsumoNomeInvalidoException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.service.InsumoCatalogoService;
import br.com.doceterapia.api.service.InsumoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsumoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes de contrato HTTP do catálogo de insumos (@WebMvcTest)")
class InsumoControllerCatalogoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsumoService service;

    @MockitoBean
    private InsumoCatalogoService catalogoService;

    @MockitoBean
    private InsumoMapper insumoMapper;

    @Test
    @DisplayName("GET /insumos/catalogo retorna a forma compacta com total e cursor")
    void catalogoRetornaFormaCompacta() throws Exception {
        InsumoCatalogoItemDTO item = new InsumoCatalogoItemDTO(1, "Farinha de trigo", UnidadeMedida.KG);
        when(catalogoService.listarCatalogo(any(), any(), any(), any()))
                .thenReturn(new InsumoCatalogoResponseDTO(List.of(item), 1, "abc"));

        mockMvc.perform(get("/insumos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.proximoCursor").value("abc"))
                .andExpect(jsonPath("$.itens[0].idInsumo").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Farinha de trigo"))
                .andExpect(jsonPath("$.itens[0].unidade").value("KG"));
    }

    @Test
    @DisplayName("GET /insumos/catalogo com cursor inválido retorna 400")
    void catalogoCursorInvalido() throws Exception {
        when(catalogoService.listarCatalogo(any(), any(), any(), eq("ruim")))
                .thenThrow(new CursorInvalidoException());

        mockMvc.perform(get("/insumos/catalogo").param("cursor", "ruim"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /insumos/obter-ou-criar retorna 200 para insumo existente")
    void obterOuCriarExistente() throws Exception {
        Insumo insumo = new Insumo();
        when(catalogoService.obterOuCriar(any()))
                .thenReturn(new InsumoCatalogoService.ObterOuCriarResultado(insumo, false));
        when(insumoMapper.toResponseDTO(insumo)).thenReturn(respostaDe(5, "Farinha de trigo"));

        mockMvc.perform(post("/insumos/obter-ou-criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Farinha de trigo\",\"unidade\":\"KG\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInsumo").value(5));
    }

    @Test
    @DisplayName("POST /insumos/obter-ou-criar retorna 201 para insumo criado")
    void obterOuCriarCriado() throws Exception {
        Insumo insumo = new Insumo();
        when(catalogoService.obterOuCriar(any()))
                .thenReturn(new InsumoCatalogoService.ObterOuCriarResultado(insumo, true));
        when(insumoMapper.toResponseDTO(insumo)).thenReturn(respostaDe(9, "Farinha de mandioca"));

        mockMvc.perform(post("/insumos/obter-ou-criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Farinha de mandioca\",\"unidade\":\"KG\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInsumo").value(9));
    }

    @Test
    @DisplayName("POST /insumos/obter-ou-criar retorna 422 para nome longo")
    void obterOuCriarNomeLongo() throws Exception {
        when(catalogoService.obterOuCriar(any()))
                .thenThrow(new InsumoNomeInvalidoException("Nome não pode exceder 45 caracteres"));

        mockMvc.perform(post("/insumos/obter-ou-criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + "a".repeat(60) + "\",\"unidade\":\"KG\"}"))
                .andExpect(status().is(422));
    }

    private InsumoResponseDTO respostaDe(Integer id, String nome) {
        InsumoResponseDTO dto = new InsumoResponseDTO();
        dto.setIdInsumo(id);
        dto.setNome(nome);
        dto.setUnidade(UnidadeMedida.KG);
        dto.setStatus(StatusAtivo.ATIVO);
        return dto;
    }
}
