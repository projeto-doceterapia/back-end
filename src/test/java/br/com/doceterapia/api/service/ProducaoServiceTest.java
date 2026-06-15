package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.ProducaoRequestDTO;
import br.com.doceterapia.api.dto.ProducaoResponseDTO;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Producao;
import br.com.doceterapia.api.enums.StatusProducao;
import br.com.doceterapia.api.exception.ProducaoNotFoundException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoAlreadyHasProducaoException;
import br.com.doceterapia.api.mapper.ProducaoMapper;
import br.com.doceterapia.api.repository.ProducaoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProducaoService")
class ProducaoServiceTest {

    @Mock
    private ProducaoRepository producaoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProducaoMapper producaoMapper;

    @InjectMocks
    private ProducaoService producaoService;

    private Producao producao;
    private ProducaoRequestDTO producaoRequestDTO;
    private ProducaoResponseDTO producaoResponseDTO;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setIdPedido(1);

        producao = new Producao();
        producao.setIdProducao(1);
        producao.setPedido(pedido);
        producao.setDataInicio(LocalDate.of(2026, 6, 15));
        producao.setDataPrevista(LocalDate.of(2026, 6, 20));
        producao.setStatusProducao(StatusProducao.EM_PRODUCAO);
        producao.setObservacao("Produção com urgência");

        producaoResponseDTO = new ProducaoResponseDTO();
        producaoResponseDTO.setIdProducao(1);

        producaoRequestDTO = new ProducaoRequestDTO();
        producaoRequestDTO.setPedidoId(1);
        producaoRequestDTO.setDataInicio(LocalDate.of(2026, 6, 15));
        producaoRequestDTO.setDataPrevista(LocalDate.of(2026, 6, 20));
        producaoRequestDTO.setObservacao("Produção com urgência");
    }

    @Test
    @DisplayName("Deve listar todas as produções")
    void testListarTodos() {
        // Arrange
        Producao producao2 = new Producao();
        producao2.setIdProducao(2);
        producao2.setPedido(pedido);
        producao2.setStatusProducao(StatusProducao.FINALIZADO);

        ProducaoResponseDTO dto2 = new ProducaoResponseDTO();
        dto2.setIdProducao(2);

        when(producaoRepository.findAll()).thenReturn(List.of(producao, producao2));
        when(producaoMapper.toResponseDTO(producao)).thenReturn(producaoResponseDTO);
        when(producaoMapper.toResponseDTO(producao2)).thenReturn(dto2);

        // Act
        List<ProducaoResponseDTO> resultado = producaoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(producaoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar uma nova produção com sucesso")
    void testCadastrar() {
        // Arrange
        when(pedidoRepository.existsById(producaoRequestDTO.getPedidoId())).thenReturn(true);
        when(producaoRepository.existsByPedidoIdPedido(producaoRequestDTO.getPedidoId())).thenReturn(false);
        when(producaoMapper.toEntity(producaoRequestDTO)).thenReturn(producao);
        when(producaoRepository.save(any(Producao.class))).thenReturn(producao);
        when(producaoMapper.toResponseDTO(producao)).thenReturn(producaoResponseDTO);

        // Act
        ProducaoResponseDTO resultado = producaoService.cadastrar(producaoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdProducao());
        verify(pedidoRepository).existsById(producaoRequestDTO.getPedidoId());
        verify(producaoRepository).existsByPedidoIdPedido(producaoRequestDTO.getPedidoId());
        verify(producaoMapper).toEntity(producaoRequestDTO);
        verify(producaoRepository).save(any(Producao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar produção com pedido inexistente")
    void testCadastrarPedidoInexistente() {
        // Arrange
        when(pedidoRepository.existsById(producaoRequestDTO.getPedidoId())).thenReturn(false);

        // Act & Assert
        assertThrows(PedidoIdDontExistsException.class, () -> {
            producaoService.cadastrar(producaoRequestDTO);
        });

        verify(pedidoRepository).existsById(producaoRequestDTO.getPedidoId());
        verify(producaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar produção duplicada para o mesmo pedido")
    void testCadastrarItemPedidoInexistente() {
        // Arrange
        when(pedidoRepository.existsById(producaoRequestDTO.getPedidoId())).thenReturn(true);
        when(producaoRepository.existsByPedidoIdPedido(producaoRequestDTO.getPedidoId())).thenReturn(true);

        // Act & Assert
        assertThrows(PedidoAlreadyHasProducaoException.class, () -> {
            producaoService.cadastrar(producaoRequestDTO);
        });

        verify(pedidoRepository).existsById(producaoRequestDTO.getPedidoId());
        verify(producaoRepository).existsByPedidoIdPedido(producaoRequestDTO.getPedidoId());
        verify(producaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar produção com sucesso")
    void testAtualizar() {
        // Arrange
        Integer id = 1;
        when(producaoRepository.findById(id)).thenReturn(Optional.of(producao));
        when(pedidoRepository.existsById(producaoRequestDTO.getPedidoId())).thenReturn(true);
        when(pedidoRepository.findById(producaoRequestDTO.getPedidoId())).thenReturn(Optional.of(pedido));
        when(producaoRepository.save(any(Producao.class))).thenReturn(producao);
        when(producaoMapper.toResponseDTO(producao)).thenReturn(producaoResponseDTO);

        // Act
        ProducaoResponseDTO resultado = producaoService.atualizar(id, producaoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getIdProducao());
        verify(producaoRepository).findById(id);
        verify(pedidoRepository).existsById(producaoRequestDTO.getPedidoId());
        verify(producaoRepository).save(any(Producao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produção inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer id = 999;
        when(producaoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProducaoNotFoundException.class, () -> {
            producaoService.atualizar(id, producaoRequestDTO);
        });

        verify(producaoRepository).findById(id);
        verify(producaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar produção com sucesso")
    void testDeletar() {
        // Arrange
        Integer id = 1;
        when(producaoRepository.existsById(id)).thenReturn(true);

        // Act
        producaoService.deletar(id);

        // Assert
        verify(producaoRepository).existsById(id);
        verify(producaoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produção inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer id = 999;
        when(producaoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ProducaoNotFoundException.class, () -> {
            producaoService.deletar(id);
        });

        verify(producaoRepository).existsById(id);
        verify(producaoRepository, never()).deleteById(any());
    }
}
