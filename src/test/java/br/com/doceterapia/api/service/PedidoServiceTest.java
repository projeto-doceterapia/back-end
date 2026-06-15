package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoResponseDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.FormaEntrega;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPedido;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.PedidoMapper;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PedidoService")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;
    private PedidoResponseDTO pedidoResponseDTO;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1);

        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setCliente(cliente);
        pedido.setTipoPedido(TipoPedido.ORCAMENTO);
        pedido.setStatusPedido(StatusPedido.ORCAMENTO);
        pedido.setDataEntrega(LocalDate.of(2026, 5, 30));
        pedido.setAnotacao("Bolo de chocolate");

        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setIdPedido(1);
        pedidoResponseDTO.setClienteId(1);

        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setClienteId(1);
        pedidoRequestDTO.setTipoPedido(TipoPedido.ORCAMENTO);
        pedidoRequestDTO.setStatusPedido(StatusPedido.ORCAMENTO);
        pedidoRequestDTO.setDataEntrega(LocalDate.of(2026, 5, 30));
        pedidoRequestDTO.setAnotacao("Bolo de chocolate");
    }

    @Test
    @DisplayName("Deve listar todos os pedidos com cliente")
    void testListarTodos() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2);

        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2);
        pedido2.setCliente(cliente2);
        pedido2.setTipoPedido(TipoPedido.ORCAMENTO);
        pedido2.setStatusPedido(StatusPedido.ORCAMENTO);
        pedido2.setDataEntrega(LocalDate.of(2026, 6, 1));

        PedidoResponseDTO dto2 = new PedidoResponseDTO();
        dto2.setIdPedido(2);
        dto2.setClienteId(2);

        List<Pedido> pedidos = List.of(pedido, pedido2);
        when(pedidoRepository.findAllWithClienteEager()).thenReturn(pedidos);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(pedidoResponseDTO);
        when(pedidoMapper.toPedidoResponse(pedido2)).thenReturn(dto2);

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findAllWithClienteEager();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver pedidos")
    void testListarTodosVazio() {
        // Arrange
        when(pedidoRepository.findAllWithClienteEager()).thenReturn(List.of());

        // Act
        List<PedidoResponseDTO> resultado = pedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository).findAllWithClienteEager();
    }

    @Test
    @DisplayName("Deve cadastrar um novo pedido com sucesso")
    void testCadastrarPedido() {
        // Arrange
        when(pedidoMapper.toPedido(pedidoRequestDTO)).thenReturn(pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(pedidoResponseDTO);

        // Act
        PedidoResponseDTO resultado = pedidoService.cadastrar(pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdPedido());
        verify(pedidoMapper).toPedido(pedidoRequestDTO);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(pedidoMapper).toPedidoResponse(pedido);
    }

    @Test
    @DisplayName("Deve atualizar um pedido com sucesso")
    void testAtualizarPedido() {
        // Arrange
        Integer idPedido = 1;
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toPedido(pedidoRequestDTO)).thenReturn(pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(pedidoResponseDTO);

        // Act
        PedidoResponseDTO resultado = pedidoService.atualizar(idPedido, pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idPedido, resultado.getIdPedido());
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve deletar um pedido com sucesso")
    void testDeletarPedido() {
        // Arrange
        Integer idPedido = 1;
        when(pedidoRepository.existsById(idPedido)).thenReturn(true);

        // Act
        pedidoService.deletar(idPedido);

        // Assert
        verify(pedidoRepository).existsById(idPedido);
        verify(pedidoRepository).deleteById(idPedido);
    }

    @Test
    @DisplayName("Deve atualizar status do pedido com sucesso")
    void testAtualizarStatusPedido() {
        // Arrange
        Integer idPedido = 1;
        StatusPedido novoStatus = StatusPedido.AGUARDANDO_SINAL;

        pedido.setStatusPedido(StatusPedido.ORCAMENTO);
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(pedidoResponseDTO);

        // Act
        PedidoResponseDTO resultado = pedidoService.atualizarStatus(idPedido, novoStatus);

        // Assert
        assertNotNull(resultado);
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar status de pedido inexistente")
    void testAtualizarStatusPedidoInexistente() {
        // Arrange
        Integer idPedido = 999;
        StatusPedido novoStatus = StatusPedido.AGUARDANDO_SINAL;

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PedidoIdDontExistsException.class, () -> {
            pedidoService.atualizarStatus(idPedido, novoStatus);
        });

        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar status para AGUARDANDO_SINAL")
    void testAtualizarStatusParaConcluido() {
        // Arrange
        Integer idPedido = 1;
        StatusPedido novoStatus = StatusPedido.AGUARDANDO_SINAL;

        pedido.setStatusPedido(StatusPedido.ORCAMENTO);
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toPedidoResponse(pedido)).thenReturn(pedidoResponseDTO);

        // Act
        PedidoResponseDTO resultado = pedidoService.atualizarStatus(idPedido, novoStatus);

        // Assert
        assertNotNull(resultado);
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
        assertEquals(StatusPedido.AGUARDANDO_SINAL, pedido.getStatusPedido());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar status com transição inválida")
    void testAtualizarStatusParaNaoConcluido() {
        // Arrange
        Integer idPedido = 1;
        StatusPedido transicaoInvalida = StatusPedido.ENTREGUE;

        pedido.setStatusPedido(StatusPedido.ORCAMENTO);
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(br.com.doceterapia.api.exception.StatusTransitionInvalidException.class, () -> {
            pedidoService.atualizarStatus(idPedido, transicaoInvalida);
        });

        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository, never()).save(any());
    }
}
