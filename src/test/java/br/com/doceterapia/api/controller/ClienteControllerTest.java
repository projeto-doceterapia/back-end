package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.config.AutenticacaoEntryPoint;
import br.com.doceterapia.api.config.AutenticacaoFilter;
import br.com.doceterapia.api.config.SecurityConfiguracao;
import br.com.doceterapia.api.dto.ClienteResumoResponseDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import br.com.doceterapia.api.exception.ClienteIdDontExistsException;
import br.com.doceterapia.api.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfiguracao.class, AutenticacaoFilter.class, AutenticacaoEntryPoint.class}))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Contrato HTTP de /clientes")
class ClienteControllerTest {

    private static final String CORPO_BASE = """
            {
              "nome": "Cliente Exemplo",
              "telefone": "+55 11 90000-0001",
              "endereco": "Praça da Sé, 100, Sé, São Paulo - SP",
              %s
              "tipoPessoa": "FISICA",
              "classificacaoCliente": "PADRAO",
              "status": "ATIVO"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    private Cliente cliente(String cep) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setNome("Cliente Exemplo");
        cliente.setTelefone("+55 11 90000-0001");
        cliente.setEndereco("Praça da Sé, 100, Sé, São Paulo - SP");
        cliente.setCep(cep);
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setClassificacaoCliente(ClassificacaoCliente.PADRAO);
        cliente.setStatus(StatusAtivo.ATIVO);
        return cliente;
    }

    private UsernamePasswordAuthenticationToken usuario(String papel) {
        return new UsernamePasswordAuthenticationToken("usuario@exemplo.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
    }

    @Test
    @DisplayName("Listar repassa a busca ao serviço")
    void listarRepassaBusca() throws Exception {
        when(service.listar("exem")).thenReturn(List.of(cliente("01001000")));

        mockMvc.perform(get("/clientes").param("busca", "exem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Cliente Exemplo"))
                .andExpect(jsonPath("$[0].cep").value("01001000"));
    }

    @Test
    @DisplayName("Listar sem busca chama o serviço com nulo")
    void listarSemBusca() throws Exception {
        when(service.listar(null)).thenReturn(List.of());

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Resumo filtra por busca e informa o gasto para a dona")
    void resumoFiltraPorNome() throws Exception {
        ClienteResumoResponseDTO resumo = new ClienteResumoResponseDTO(3, "Cliente Exemplo", "+55 11 90000-0001",
                "Praça da Sé, 100", null, TipoPessoa.FISICA, ClassificacaoCliente.PADRAO, StatusAtivo.ATIVO, null,
                7, new BigDecimal("845.50"), LocalDate.of(2026, 10, 28));
        when(service.resumir("exem", false, true)).thenReturn(List.of(resumo));

        mockMvc.perform(get("/clientes/resumo").param("busca", "exem").principal(usuario("DONA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalPedidos").value(7))
                .andExpect(jsonPath("$[0].gastoTotal").value(845.50))
                .andExpect(jsonPath("$[0].ultimoPedido").value("2026-10-28"));
    }

    @Test
    @DisplayName("Resumo para a funcionária pede o serviço sem o gasto")
    void resumoParaFuncionariaSemGasto() throws Exception {
        when(service.resumir(null, true, false)).thenReturn(List.of());

        mockMvc.perform(get("/clientes/resumo").param("incluirInativos", "true").principal(usuario("FUNCIONARIA")))
                .andExpect(status().isOk());

        verify(service).resumir(null, true, false);
    }

    @Test
    @DisplayName("CEP inválido retorna 400 com a mensagem em português")
    void cepInvalidoRetorna400() throws Exception {
        for (String cep : List.of("\"0100100\"", "\"\"", "\"01001-000\"")) {
            mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_BASE.formatted("\"cep\": " + cep + ",")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                            "CEP deve ter exatamente 8 dígitos")));
        }
    }

    @Test
    @DisplayName("CEP com 8 dígitos ou nulo é aceito e devolvido")
    void cepValidoOuNuloEhAceito() throws Exception {
        when(service.cadastrar(any())).thenReturn(cliente("01001000"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_BASE.formatted("\"cep\": \"01001000\",")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cep").value("01001000"));

        when(service.cadastrar(any())).thenReturn(cliente(null));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_BASE.formatted("\"cep\": null,")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cep").doesNotExist());
    }

    @Test
    @DisplayName("Reativar devolve o cliente ativo")
    void reativarDevolveCliente() throws Exception {
        when(service.reativar(eq(3))).thenReturn(cliente(null));

        mockMvc.perform(patch("/clientes/3/reativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    @DisplayName("Reativar cliente inexistente retorna 404")
    void reativarInexistente404() throws Exception {
        when(service.reativar(eq(99))).thenThrow(new ClienteIdDontExistsException());

        mockMvc.perform(patch("/clientes/99/reativar"))
                .andExpect(status().isNotFound());
    }
}
