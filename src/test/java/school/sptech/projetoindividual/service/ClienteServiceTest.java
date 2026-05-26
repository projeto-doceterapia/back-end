package school.sptech.projetoindividual.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import school.sptech.projetoindividual.dto.ClienteRequestDTO;
import school.sptech.projetoindividual.entity.Cliente;
import school.sptech.projetoindividual.exception.ClienteIdDontExistsException;
import school.sptech.projetoindividual.exception.TelefoneAlreadyExistsException;
import school.sptech.projetoindividual.repository.ClienteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private ClienteRequestDTO clienteRequestDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNomeCompleto("João Silva");
        cliente.setTelefone("(11) 98765-4321");
        cliente.setEndereco("Rua das Flores, 123");

        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNomeCompleto("João Silva");
        clienteRequestDTO.setTelefone("(11) 98765-4321");
        clienteRequestDTO.setEndereco("Rua das Flores, 123");
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void testListarTodos() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2);
        cliente2.setNomeCompleto("Maria Silva");
        cliente2.setTelefone("(11) 87654-3210");
        cliente2.setEndereco("Rua das Plantas, 456");

        when(clienteRepository.findAll()).thenReturn(List.of(cliente, cliente2));

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNomeCompleto());
        assertEquals("Maria Silva", resultado.get(1).getNomeCompleto());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver clientes")
    void testListarTodosVazio() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of());

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar um novo cliente com sucesso")
    void testCadastrarCliente() {
        // Arrange
        when(clienteRepository.existsByTelefone(clienteRequestDTO.getTelefone())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.cadastrar(clienteRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNomeCompleto());
        assertEquals("(11) 98765-4321", resultado.getTelefone());
        verify(clienteRepository).existsByTelefone(clienteRequestDTO.getTelefone());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar cliente com telefone duplicado")
    void testCadastrarClienteTelefoneDuplicado() {
        // Arrange
        when(clienteRepository.existsByTelefone(clienteRequestDTO.getTelefone())).thenReturn(true);

        // Act & Assert
        assertThrows(TelefoneAlreadyExistsException.class, () -> {
            clienteService.cadastrar(clienteRequestDTO);
        });

        verify(clienteRepository).existsByTelefone(clienteRequestDTO.getTelefone());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void testAtualizarCliente() {
        // Arrange
        Integer idCliente = 1;
        when(clienteRepository.existsById(idCliente)).thenReturn(true);
        when(clienteRepository.existsByTelefoneAndIdClienteNot(clienteRequestDTO.getTelefone(), idCliente))
                .thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.atualizar(idCliente, clienteRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idCliente, resultado.getIdCliente());
        verify(clienteRepository).existsById(idCliente);
        verify(clienteRepository).existsByTelefoneAndIdClienteNot(clienteRequestDTO.getTelefone(), idCliente);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
    void testAtualizarClienteInexistente() {
        // Arrange
        Integer idCliente = 999;
        when(clienteRepository.existsById(idCliente)).thenReturn(false);

        // Act & Assert
        assertThrows(ClienteIdDontExistsException.class, () -> {
            clienteService.atualizar(idCliente, clienteRequestDTO);
        });

        verify(clienteRepository).existsById(idCliente);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente com telefone duplicado")
    void testAtualizarClienteTelefoneDuplicado() {
        // Arrange
        Integer idCliente = 1;
        when(clienteRepository.existsById(idCliente)).thenReturn(true);
        when(clienteRepository.existsByTelefoneAndIdClienteNot(clienteRequestDTO.getTelefone(), idCliente))
                .thenReturn(true);

        // Act & Assert
        assertThrows(TelefoneAlreadyExistsException.class, () -> {
            clienteService.atualizar(idCliente, clienteRequestDTO);
        });

        verify(clienteRepository).existsById(idCliente);
        verify(clienteRepository).existsByTelefoneAndIdClienteNot(clienteRequestDTO.getTelefone(), idCliente);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void testDeletarCliente() {
        // Arrange
        Integer idCliente = 1;
        when(clienteRepository.existsById(idCliente)).thenReturn(true);

        // Act
        clienteService.deletar(idCliente);

        // Assert
        verify(clienteRepository).existsById(idCliente);
        verify(clienteRepository).deleteById(idCliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
    void testDeletarClienteInexistente() {
        // Arrange
        Integer idCliente = 999;
        when(clienteRepository.existsById(idCliente)).thenReturn(false);

        // Act & Assert
        assertThrows(ClienteIdDontExistsException.class, () -> {
            clienteService.deletar(idCliente);
        });

        verify(clienteRepository).existsById(idCliente);
        verify(clienteRepository, never()).deleteById(any());
    }
}
