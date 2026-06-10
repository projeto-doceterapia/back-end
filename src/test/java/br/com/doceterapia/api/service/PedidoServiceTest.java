package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.PedidoRequestDTO;
import br.com.doceterapia.api.dto.PedidoWithClienteResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
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

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setFkCliente(1);
        pedido.setDescricao("Bolo de chocolate");
        pedido.setDataEntrega(LocalDate.of(2026, 5, 30));
        pedido.setValor(150.0);
        pedido.setStatusConcluido(false);

        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setFkCliente(1);
        pedidoRequestDTO.setDescricao("Bolo de chocolate");
        pedidoRequestDTO.setDataEntrega(LocalDate.of(2026, 5, 30));
        pedidoRequestDTO.setValor(150.0);
    }

    @Test
    @DisplayName("Deve listar todos os pedidos com cliente")
    void testListarTodos() {
        // Arrange
        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2);
        pedido2.setFkCliente(2);
        pedido2.setDescricao("Torta de morango");
        pedido2.setDataEntrega(LocalDate.of(2026, 6, 1));
        pedido2.setValor(200.0);
        pedido2.setStatusConcluido(false);

        List<PedidoWithClienteResponseDTO> pedidosResponse = List.of(
                new PedidoWithClienteResponseDTO(1, 1, "Bolo de chocolate", LocalDate.of(2026, 5, 30), 150.0, false, 1, "João Silva", "(11) 98765-4321", "Rua das Flores, 123"),
                new PedidoWithClienteResponseDTO(2, 2, "Torta de morango", LocalDate.of(2026, 6, 1), 200.0, false, 2, "Maria Silva", "(11) 87654-3210", "Rua das Plantas, 456")
        );

        when(pedidoRepository.findAllWithCliente()).thenReturn(pedidosResponse);

        // Act
        List<PedidoWithClienteResponseDTO> resultado = pedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoRepository).findAllWithCliente();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver pedidos")
    void testListarTodosVazio() {
        // Arrange
        when(pedidoRepository.findAllWithCliente()).thenReturn(List.of());

        // Act
        List<PedidoWithClienteResponseDTO> resultado = pedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository).findAllWithCliente();
    }

    @Test
    @DisplayName("Deve cadastrar um novo pedido com sucesso")
    void testCadastrarPedido() {
        // Arrange
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.cadastrar(pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Bolo de chocolate", resultado.getDescricao());
        assertEquals(1, resultado.getFkCliente());
        assertEquals(150.0, resultado.getValor());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve atualizar um pedido com sucesso")
    void testAtualizarPedido() {
        // Arrange
        Integer idPedido = 1;
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.atualizar(idPedido, pedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idPedido, resultado.getIdPedido());
        assertEquals("Bolo de chocolate", resultado.getDescricao());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve deletar um pedido com sucesso")
    void testDeletarPedido() {
        // Arrange
        Integer idPedido = 1;

        // Act
        pedidoService.deletar(idPedido);

        // Assert
        verify(pedidoRepository).deleteById(idPedido);
    }

    @Test
    @DisplayName("Deve atualizar status do pedido com sucesso")
    void testAtualizarStatusPedido() {
        // Arrange
        Integer idPedido = 1;
        Boolean novoStatus = true;

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.atualizarStatus(idPedido, novoStatus);

        // Assert
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar status de pedido inexistente")
    void testAtualizarStatusPedidoInexistente() {
        // Arrange
        Integer idPedido = 999;
        Boolean novoStatus = true;

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PedidoIdDontExistsException.class, () -> {
            pedidoService.atualizarStatus(idPedido, novoStatus);
        });

        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar status para true (pedido concluído)")
    void testAtualizarStatusParaConcluido() {
        // Arrange
        Integer idPedido = 1;
        Boolean statusConcluido = true;
        Pedido pedidoAtualizado = new Pedido();
        pedidoAtualizado.setIdPedido(idPedido);
        pedidoAtualizado.setStatusConcluido(statusConcluido);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtualizado);

        // Act
        pedidoService.atualizarStatus(idPedido, statusConcluido);

        // Assert
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
        assertTrue(pedidoAtualizado.getStatusConcluido());
    }

    @Test
    @DisplayName("Deve atualizar status para false (pedido não concluído)")
    void testAtualizarStatusParaNaoConcluido() {
        // Arrange
        Integer idPedido = 1;
        pedido.setStatusConcluido(true);
        Boolean statusNaoConcluido = false;
        Pedido pedidoAtualizado = new Pedido();
        pedidoAtualizado.setIdPedido(idPedido);
        pedidoAtualizado.setStatusConcluido(statusNaoConcluido);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtualizado);

        // Act
        pedidoService.atualizarStatus(idPedido, statusNaoConcluido);

        // Assert
        verify(pedidoRepository).findById(idPedido);
        verify(pedidoRepository).save(any(Pedido.class));
        assertFalse(pedidoAtualizado.getStatusConcluido());
    }
}
