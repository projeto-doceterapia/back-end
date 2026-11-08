package br.com.doceterapia.api.dto;

import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.ClienteRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validação e mapeamento do CEP")
class CepValidacaoTest {

    private static final String MENSAGEM = "CEP deve ter exatamente 8 dígitos";

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Cliente aceita CEP nulo ou de 8 dígitos e recusa os demais")
    void cepComOitoDigitosValidoNoCliente() {
        ClienteRequestDTO dto = new ClienteRequestDTO();

        for (String valido : new String[] {null, "01001000"}) {
            dto.setCep(valido);
            assertTrue(validator.validateProperty(dto, "cep").isEmpty());
        }
        for (String invalido : new String[] {"", "0100100", "01001-000", "010010001", "0100100a"}) {
            dto.setCep(invalido);
            assertEquals(MENSAGEM, validator.validateProperty(dto, "cep").iterator().next().getMessage());
        }
    }

    @Test
    @DisplayName("Pedido aceita CEP nulo ou de 8 dígitos e recusa os demais")
    void cepComOitoDigitosValidoNoPedido() {
        PedidoRequestDTO dto = new PedidoRequestDTO();

        for (String valido : new String[] {null, "01001000"}) {
            dto.setCep(valido);
            assertTrue(validator.validateProperty(dto, "cep").isEmpty());
        }
        for (String invalido : new String[] {"", "0100100", "01001-000"}) {
            dto.setCep(invalido);
            assertEquals(MENSAGEM, validator.validateProperty(dto, "cep").iterator().next().getMessage());
        }
    }

    @Test
    @DisplayName("O mapper do pedido copia o CEP nos dois sentidos")
    void mapperDoPedidoCopiaCep() {
        ClienteRepository clienteRepository = Mockito.mock(ClienteRepository.class);
        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setNome("Cliente Exemplo");
        Mockito.when(clienteRepository.findById(3)).thenReturn(Optional.of(cliente));
        PedidoMapper mapper = new PedidoMapper(clienteRepository);
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setClienteId(3);
        dto.setCep("01001000");

        Pedido pedido = mapper.toPedido(dto);
        PedidoResponseDTO resposta = mapper.toPedidoResponse(pedido);

        assertEquals("01001000", pedido.getCep());
        assertEquals("01001000", resposta.getCep());

        dto.setCep(null);
        assertNull(mapper.toPedido(dto).getCep());
    }
}
