package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.ClienteMetricasDTO;
import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.dto.ClienteResumoResponseDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoPessoa;
import br.com.doceterapia.api.exception.ClienteIdDontExistsException;
import br.com.doceterapia.api.exception.TelefoneAlreadyExistsException;
import br.com.doceterapia.api.repository.ClienteRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private ClienteRequestDTO clienteRequestDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNome("João Silva");
        cliente.setTelefone("(11) 98765-4321");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setClassificacaoCliente(ClassificacaoCliente.PADRAO);
        cliente.setStatus(StatusAtivo.ATIVO);

        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNome("João Silva");
        clienteRequestDTO.setTelefone("(11) 98765-4321");
        clienteRequestDTO.setEndereco("Rua das Flores, 123");
        clienteRequestDTO.setTipoPessoa(TipoPessoa.FISICA);
        clienteRequestDTO.setClassificacaoCliente(ClassificacaoCliente.PADRAO);
        clienteRequestDTO.setStatus(StatusAtivo.ATIVO);
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void testListarTodos() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2);
        cliente2.setNome("Maria Silva");
        cliente2.setTelefone("(11) 87654-3210");
        cliente2.setEndereco("Rua das Plantas, 456");

        when(clienteRepository.findAll()).thenReturn(List.of(cliente, cliente2));

        // Act
        List<Cliente> resultado = clienteService.listar(null);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertEquals("Maria Silva", resultado.get(1).getNome());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver clientes")
    void testListarTodosVazio() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of());

        // Act
        List<Cliente> resultado = clienteService.listar(null);

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
        assertEquals("João Silva", resultado.getNome());
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
    @DisplayName("Deletar inativa o cliente sem apagar o registro")
    void deletarInativaSemApagar() {
        // Arrange
        Integer idCliente = 1;
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setStatus(StatusAtivo.ATIVO);
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

        // Act
        clienteService.deletar(idCliente);

        // Assert
        assertEquals(StatusAtivo.INATIVO, cliente.getStatus());
        verify(clienteRepository).save(cliente);
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Listar com busca parcial consulta pelo texto e pelos dígitos")
    void listarComNomeBuscaParcial() {
        // Arrange
        when(clienteRepository.buscar("exem", "")).thenReturn(List.of(cliente));

        // Act
        List<Cliente> resultado = clienteService.listar(" exem ");

        // Assert
        assertEquals(List.of(cliente), resultado);
        verify(clienteRepository, never()).findAll();
    }

    @Test
    @DisplayName("Listar com telefone repassa só os dígitos")
    void listarComTelefonePassaDigitos() {
        // Arrange
        when(clienteRepository.buscar("90000-0001", "900000001")).thenReturn(List.of(cliente));

        // Act
        List<Cliente> resultado = clienteService.listar("90000-0001");

        // Assert
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Listar sem busca ou em branco retorna todos")
    void listarSemNomeRetornaTodos() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        // Act
        List<Cliente> semBusca = clienteService.listar(null);
        List<Cliente> emBranco = clienteService.listar("   ");

        // Assert
        assertEquals(1, semBusca.size());
        assertEquals(1, emBranco.size());
        verify(clienteRepository, never()).buscar(any(), any());
    }

    @Test
    @DisplayName("Resumo combina o cliente com as métricas e zera quem não tem pedidos")
    void resumirCombinaMetricas() {
        // Arrange
        Cliente semPedidos = new Cliente();
        semPedidos.setIdCliente(2);
        semPedidos.setNome("Outra Pessoa");
        semPedidos.setStatus(StatusAtivo.ATIVO);
        when(clienteRepository.findAll(Sort.by("nome"))).thenReturn(List.of(cliente, semPedidos));
        when(pedidoRepository.resumirPorCliente()).thenReturn(List.of(
                new ClienteMetricasDTO(1, 2, new BigDecimal("150.50"), LocalDate.of(2026, 10, 28))));

        // Act
        List<ClienteResumoResponseDTO> resumo = clienteService.resumir(null, false, true);

        // Assert
        assertEquals(2, resumo.size());
        assertEquals(2, resumo.get(0).totalPedidos());
        assertEquals(new BigDecimal("150.50"), resumo.get(0).gastoTotal());
        assertEquals(LocalDate.of(2026, 10, 28), resumo.get(0).ultimoPedido());
        assertEquals(0, resumo.get(1).totalPedidos());
        assertEquals(BigDecimal.ZERO, resumo.get(1).gastoTotal());
        assertNull(resumo.get(1).ultimoPedido());
    }

    @Test
    @DisplayName("Resumo oculta o gasto total para quem não é a dona")
    void resumoOcultaGastoParaFuncionaria() {
        // Arrange
        when(clienteRepository.findAll(Sort.by("nome"))).thenReturn(List.of(cliente));
        when(pedidoRepository.resumirPorCliente()).thenReturn(List.of(
                new ClienteMetricasDTO(1, 1, new BigDecimal("80.00"), LocalDate.of(2026, 10, 1))));

        // Act
        List<ClienteResumoResponseDTO> resumo = clienteService.resumir(null, false, false);

        // Assert
        assertNull(resumo.get(0).gastoTotal());
        assertEquals(1, resumo.get(0).totalPedidos());
    }

    @Test
    @DisplayName("Resumo esconde inativos por padrão e os mostra quando pedido")
    void resumoEscondeInativosPorPadrao() {
        // Arrange
        Cliente inativo = new Cliente();
        inativo.setIdCliente(3);
        inativo.setNome("Cliente Inativo");
        inativo.setStatus(StatusAtivo.INATIVO);
        when(clienteRepository.findAll(Sort.by("nome"))).thenReturn(List.of(cliente, inativo));
        when(pedidoRepository.resumirPorCliente()).thenReturn(List.of());

        // Act
        List<ClienteResumoResponseDTO> padrao = clienteService.resumir(null, false, true);
        List<ClienteResumoResponseDTO> todos = clienteService.resumir(null, true, true);

        // Assert
        assertEquals(1, padrao.size());
        assertEquals(2, todos.size());
    }

    @Test
    @DisplayName("Resumo com busca usa a consulta de busca")
    void resumoComBuscaUsaConsultaDeBusca() {
        // Arrange
        when(clienteRepository.buscar("joão", "")).thenReturn(List.of(cliente));
        when(pedidoRepository.resumirPorCliente()).thenReturn(List.of());

        // Act
        List<ClienteResumoResponseDTO> resumo = clienteService.resumir("joão", false, true);

        // Assert
        assertEquals(1, resumo.size());
        verify(clienteRepository, never()).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Reativar volta o cliente para ATIVO")
    void reativarVoltaParaAtivo() {
        // Arrange
        cliente.setStatus(StatusAtivo.INATIVO);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.reativar(1);

        // Assert
        assertEquals(StatusAtivo.ATIVO, resultado.getStatus());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Reativar cliente inexistente lança exceção")
    void reativarInexistente() {
        // Arrange
        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteIdDontExistsException.class, () -> clienteService.reativar(999));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
    void testDeletarClienteInexistente() {
        // Arrange
        Integer idCliente = 999;
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteIdDontExistsException.class, () -> {
            clienteService.deletar(idCliente);
        });

        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).deleteById(any());
    }
}
