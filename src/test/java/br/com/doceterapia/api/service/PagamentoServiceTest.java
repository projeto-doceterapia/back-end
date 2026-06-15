package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.PagamentoRequestDTO;
import br.com.doceterapia.api.dto.PagamentoResponseDTO;
import br.com.doceterapia.api.entity.Pagamento;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.enums.StatusPagamento;
import br.com.doceterapia.api.exception.PagamentoIdDontExistsException;
import br.com.doceterapia.api.mapper.PagamentoMapper;
import br.com.doceterapia.api.repository.PagamentoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PagamentoService")
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PagamentoMapper pagamentoMapper;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pagamento pagamento;
    private PagamentoRequestDTO pagamentoRequestDTO;
    private PagamentoResponseDTO pagamentoResponseDTO;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setIdPedido(1);

        pagamento = new Pagamento();
        pagamento.setIdPagamento(1);
        pagamento.setPedido(pedido);
        pagamento.setValorTotal(BigDecimal.valueOf(250.0));
        pagamento.setValorSinal(BigDecimal.valueOf(125.0));
        pagamento.setValorRestante(BigDecimal.valueOf(125.0));
        pagamento.setStatusPagamento(StatusPagamento.AGUARDANDO_SINAL);

        pagamentoResponseDTO = new PagamentoResponseDTO();
        pagamentoResponseDTO.setIdPagamento(1);
        pagamentoResponseDTO.setPedidoId(1);

        pagamentoRequestDTO = new PagamentoRequestDTO();
    }

    @Test
    @DisplayName("Deve listar todos os pagamentos")
    void testListarTodos() {
        // Arrange
        Pedido pedido2 = new Pedido();
        pedido2.setIdPedido(2);

        Pagamento pagamento2 = new Pagamento();
        pagamento2.setIdPagamento(2);
        pagamento2.setPedido(pedido2);
        pagamento2.setValorTotal(BigDecimal.valueOf(300.0));
        pagamento2.setValorSinal(BigDecimal.valueOf(150.0));
        pagamento2.setValorRestante(BigDecimal.valueOf(150.0));
        pagamento2.setStatusPagamento(StatusPagamento.PAGO_INTEGRAL);

        PagamentoResponseDTO dto2 = new PagamentoResponseDTO();
        dto2.setIdPagamento(2);
        dto2.setPedidoId(2);

        when(pagamentoRepository.findAll()).thenReturn(List.of(pagamento, pagamento2));
        when(pagamentoMapper.toResponseDTO(pagamento)).thenReturn(pagamentoResponseDTO);
        when(pagamentoMapper.toResponseDTO(pagamento2)).thenReturn(dto2);

        // Act
        List<PagamentoResponseDTO> resultado = pagamentoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pagamentoRepository).findAll();
    }

    @Test
    @DisplayName("Deve criar um novo pagamento via criar()")
    void testCriarPagamento() {
        // Arrange
        BigDecimal valorTotal = BigDecimal.valueOf(250.0);
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);

        // Act
        Pagamento resultado = pagamentoService.criar(pedido, valorTotal);

        // Assert
        assertNotNull(resultado);
        assertEquals(pedido, resultado.getPedido());
        assertEquals(valorTotal, resultado.getValorTotal());
        assertEquals(StatusPagamento.AGUARDANDO_SINAL, resultado.getStatusPagamento());
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve atualizar um pagamento com sucesso")
    void testAtualizarPagamento() {
        // Arrange
        Integer idPagamento = 1;
        when(pagamentoRepository.findById(idPagamento)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        when(pagamentoMapper.toResponseDTO(pagamento)).thenReturn(pagamentoResponseDTO);

        // Act
        PagamentoResponseDTO resultado = pagamentoService.atualizar(idPagamento, pagamentoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idPagamento, resultado.getIdPagamento());
        verify(pagamentoRepository).findById(idPagamento);
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar pagamento inexistente")
    void testAtualizarPagamentoInexistente() {
        // Arrange
        Integer idPagamento = 999;
        when(pagamentoRepository.findById(idPagamento)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PagamentoIdDontExistsException.class, () -> {
            pagamentoService.atualizar(idPagamento, pagamentoRequestDTO);
        });

        verify(pagamentoRepository).findById(idPagamento);
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar um pagamento com sucesso")
    void testDeletarPagamento() {
        // Arrange
        Integer idPagamento = 1;
        when(pagamentoRepository.existsById(idPagamento)).thenReturn(true);

        // Act
        pagamentoService.deletar(idPagamento);

        // Assert
        verify(pagamentoRepository).existsById(idPagamento);
        verify(pagamentoRepository).deleteById(idPagamento);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar pagamento inexistente")
    void testDeletarPagamentoInexistente() {
        // Arrange
        Integer idPagamento = 999;
        when(pagamentoRepository.existsById(idPagamento)).thenReturn(false);

        // Act & Assert
        assertThrows(PagamentoIdDontExistsException.class, () -> {
            pagamentoService.deletar(idPagamento);
        });

        verify(pagamentoRepository).existsById(idPagamento);
        verify(pagamentoRepository, never()).deleteById(any());
    }
}
