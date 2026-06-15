package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.CancelamentoPedidoRequestDTO;
import br.com.doceterapia.api.dto.CancelamentoPedidoResponseDTO;
import br.com.doceterapia.api.entity.CancelamentoPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoCancelamento;
import br.com.doceterapia.api.enums.TipoRetorno;
import br.com.doceterapia.api.exception.CancelamentoAlreadyExistsException;
import br.com.doceterapia.api.exception.CancelamentoPedidoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.CancelamentoPedidoMapper;
import br.com.doceterapia.api.repository.CancelamentoPedidoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CancelamentoPedidoService")
class CancelamentoPedidoServiceTest {

    @Mock
    private CancelamentoPedidoRepository cancelamentoPedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CancelamentoPedidoMapper cancelamentoPedidoMapper;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private CancelamentoPedidoService cancelamentoPedidoService;

    private CancelamentoPedido cancelamento;
    private CancelamentoPedidoRequestDTO requestDTO;
    private CancelamentoPedidoResponseDTO responseDTO;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setIdPedido(1);

        cancelamento = new CancelamentoPedido();
        cancelamento.setIdCancelamento(1);
        cancelamento.setPedido(pedido);
        cancelamento.setTipoCancelamento(TipoCancelamento.CLIENTE_DESISTIU);
        cancelamento.setTipoRetorno(TipoRetorno.DEVOLUCAO);
        cancelamento.setValorRetorno(BigDecimal.valueOf(50.0));
        cancelamento.setDataCancelamento(LocalDateTime.of(2026, 6, 14, 10, 0));
        cancelamento.setObservacao("Cliente desistiu do pedido");

        responseDTO = new CancelamentoPedidoResponseDTO();
        responseDTO.setIdCancelamento(1);
        responseDTO.setPedidoId(1);

        requestDTO = new CancelamentoPedidoRequestDTO();
        requestDTO.setTipoCancelamento(TipoCancelamento.CLIENTE_DESISTIU);
        requestDTO.setTipoRetorno(TipoRetorno.DEVOLUCAO);
        requestDTO.setValorRetorno(BigDecimal.valueOf(50.0));
        requestDTO.setObservacao("Cliente desistiu do pedido");
    }

    @Test
    @DisplayName("Deve listar todos os cancelamentos")
    void testListarTodos() {
        // Arrange
        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2);

        CancelamentoPedido cancelamento2 = new CancelamentoPedido();
        cancelamento2.setIdCancelamento(2);
        cancelamento2.setPedido(pedido2);
        cancelamento2.setTipoCancelamento(TipoCancelamento.ERRO_PEDIDO);
        cancelamento2.setTipoRetorno(TipoRetorno.DEVOLUCAO);
        cancelamento2.setValorRetorno(BigDecimal.valueOf(100.0));
        cancelamento2.setObservacao("Cliente trocou de produto");

        CancelamentoPedidoResponseDTO dto2 = new CancelamentoPedidoResponseDTO();
        dto2.setIdCancelamento(2);
        dto2.setPedidoId(2);

        when(cancelamentoPedidoRepository.findAll()).thenReturn(List.of(cancelamento, cancelamento2));
        when(cancelamentoPedidoMapper.toResponseDTO(cancelamento)).thenReturn(responseDTO);
        when(cancelamentoPedidoMapper.toResponseDTO(cancelamento2)).thenReturn(dto2);

        // Act
        List<CancelamentoPedidoResponseDTO> resultado = cancelamentoPedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(cancelamentoPedidoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver cancelamentos")
    void testListarTodosVazio() {
        // Arrange
        when(cancelamentoPedidoRepository.findAll()).thenReturn(List.of());

        // Act
        List<CancelamentoPedidoResponseDTO> resultado = cancelamentoPedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(cancelamentoPedidoRepository).findAll();
    }

    @Test
    @DisplayName("Deve criar um novo cancelamento com sucesso")
    void testCadastrarCancelamento() {
        // Arrange
        Integer pedidoId = 1;
        when(pedidoRepository.existsById(pedidoId)).thenReturn(true);
        when(cancelamentoPedidoRepository.existsByPedidoIdPedido(pedidoId)).thenReturn(false);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(cancelamentoPedidoMapper.toEntity(requestDTO)).thenReturn(cancelamento);
        when(cancelamentoPedidoRepository.save(any(CancelamentoPedido.class))).thenReturn(cancelamento);
        when(cancelamentoPedidoMapper.toResponseDTO(cancelamento)).thenReturn(responseDTO);

        // Act
        CancelamentoPedidoResponseDTO resultado = cancelamentoPedidoService.criar(pedidoId, requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdCancelamento());
        verify(pedidoRepository).existsById(pedidoId);
        verify(cancelamentoPedidoRepository).existsByPedidoIdPedido(pedidoId);
        verify(cancelamentoPedidoMapper).toEntity(requestDTO);
        verify(cancelamentoPedidoRepository).save(any(CancelamentoPedido.class));
        verify(pedidoService).atualizarStatus(pedidoId, StatusPedido.CANCELADO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cancelamento para pedido inexistente")
    void testCadastrarCancelamentoPedidoInexistente() {
        // Arrange
        Integer pedidoId = 999;
        when(pedidoRepository.existsById(pedidoId)).thenReturn(false);

        // Act & Assert
        assertThrows(PedidoIdDontExistsException.class, () -> {
            cancelamentoPedidoService.criar(pedidoId, requestDTO);
        });

        verify(pedidoRepository).existsById(pedidoId);
        verify(cancelamentoPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cancelamento duplicado para mesmo pedido")
    void testCadastrarCancelamentoDuplicado() {
        // Arrange
        Integer pedidoId = 1;
        when(pedidoRepository.existsById(pedidoId)).thenReturn(true);
        when(cancelamentoPedidoRepository.existsByPedidoIdPedido(pedidoId)).thenReturn(true);

        // Act & Assert
        assertThrows(CancelamentoAlreadyExistsException.class, () -> {
            cancelamentoPedidoService.criar(pedidoId, requestDTO);
        });

        verify(pedidoRepository).existsById(pedidoId);
        verify(cancelamentoPedidoRepository).existsByPedidoIdPedido(pedidoId);
        verify(cancelamentoPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar um cancelamento com sucesso")
    void testAtualizarCancelamento() {
        // Arrange
        Integer idCancelamento = 1;
        when(cancelamentoPedidoRepository.findById(idCancelamento)).thenReturn(Optional.of(cancelamento));
        when(cancelamentoPedidoRepository.save(any(CancelamentoPedido.class))).thenReturn(cancelamento);
        when(cancelamentoPedidoMapper.toResponseDTO(cancelamento)).thenReturn(responseDTO);

        // Act
        CancelamentoPedidoResponseDTO resultado = cancelamentoPedidoService.atualizar(idCancelamento, requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idCancelamento, resultado.getIdCancelamento());
        verify(cancelamentoPedidoRepository).findById(idCancelamento);
        verify(cancelamentoPedidoRepository).save(any(CancelamentoPedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cancelamento inexistente")
    void testAtualizarCancelamentoInexistente() {
        // Arrange
        Integer idCancelamento = 999;
        when(cancelamentoPedidoRepository.findById(idCancelamento)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CancelamentoPedidoIdDontExistsException.class, () -> {
            cancelamentoPedidoService.atualizar(idCancelamento, requestDTO);
        });

        verify(cancelamentoPedidoRepository).findById(idCancelamento);
        verify(cancelamentoPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar um cancelamento com sucesso")
    void testDeletarCancelamento() {
        // Arrange
        Integer idCancelamento = 1;
        when(cancelamentoPedidoRepository.existsById(idCancelamento)).thenReturn(true);

        // Act
        cancelamentoPedidoService.deletar(idCancelamento);

        // Assert
        verify(cancelamentoPedidoRepository).existsById(idCancelamento);
        verify(cancelamentoPedidoRepository).deleteById(idCancelamento);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cancelamento inexistente")
    void testDeletarCancelamentoInexistente() {
        // Arrange
        Integer idCancelamento = 999;
        when(cancelamentoPedidoRepository.existsById(idCancelamento)).thenReturn(false);

        // Act & Assert
        assertThrows(CancelamentoPedidoIdDontExistsException.class, () -> {
            cancelamentoPedidoService.deletar(idCancelamento);
        });

        verify(cancelamentoPedidoRepository).existsById(idCancelamento);
        verify(cancelamentoPedidoRepository, never()).deleteById(any());
    }
}
