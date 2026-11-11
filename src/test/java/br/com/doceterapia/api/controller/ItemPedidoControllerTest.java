package br.com.doceterapia.api.controller;

import br.com.doceterapia.api.exception.*;
import br.com.doceterapia.api.service.ItemPedidoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemPedidoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = br.com.doceterapia.api.config.SecurityConfiguracao.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(RequestValidationExceptionHandler.class)
class ItemPedidoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemPedidoService service;

    @MockitoBean
    private br.com.doceterapia.api.mapper.ItemPedidoMapper mapper;

    @Test
    void deveResponder422QuandoTotalAbaixoDoSinal() throws Exception {
        when(service.atualizar(eq(31), any())).thenThrow(
                new TotalAbaixoDoSinalPagoException(new java.math.BigDecimal("80.00"), new java.math.BigDecimal("92.95")));
        mockMvc.perform(put("/itens-pedido/31").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fkPedido\":1,\"fkProduto\":1,\"quantidade\":1,\"valorUnitario\":80.00}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("O total do pedido (R$ 80,00) não pode ficar abaixo do sinal já pago (R$ 92,95)."))
                .andExpect(jsonPath("$.path").value("/itens-pedido/31"));
    }
}
