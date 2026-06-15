package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.ItemPedidoRequestDTO;
import br.com.doceterapia.api.entity.ItemPedido;
import br.com.doceterapia.api.entity.Pedido;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.exception.ItemPedidoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.PedidoIdDontExistsException;
import br.com.doceterapia.api.mapper.ItemPedidoMapper;
import br.com.doceterapia.api.repository.ItemPedidoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.repository.PedidoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ItemPedidoService")
class ItemPedidoServiceTest {

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoMapper itemPedidoMapper;

    @InjectMocks
    private ItemPedidoService itemPedidoService;

    private ItemPedido itemPedido;
    private ItemPedidoRequestDTO itemPedidoRequestDTO;
    private Produto produto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setIdProduto(1);
        pedido = new Pedido();
        pedido.setIdPedido(1);

        itemPedido = new ItemPedido();
        itemPedido.setIdProdutoPedido(1);
        itemPedido.setProduto(produto);
        itemPedido.setPedido(pedido);
        itemPedido.setQuantidade(BigDecimal.valueOf(2));
        itemPedido.setValorUnitario(BigDecimal.valueOf(45.90));
        itemPedido.setValorTotal(BigDecimal.valueOf(91.80));
        itemPedido.setCustoEstimadoItem(BigDecimal.valueOf(50.00));
        itemPedido.setObservacao("Sem glúten");

        itemPedidoRequestDTO = new ItemPedidoRequestDTO();
        itemPedidoRequestDTO.setFkProduto(1);
        itemPedidoRequestDTO.setFkPedido(1);
        itemPedidoRequestDTO.setQuantidade(BigDecimal.valueOf(2));
        itemPedidoRequestDTO.setValorUnitario(BigDecimal.valueOf(45.90));
        itemPedidoRequestDTO.setObservacao("Sem glúten");
    }

    @Test
    @DisplayName("Deve listar todos os itens de pedido")
    void testListarTodos() {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setIdProduto(2);

        ItemPedido itemPedido2 = new ItemPedido();
        itemPedido2.setIdProdutoPedido(2);
        itemPedido2.setProduto(produto2);
        itemPedido2.setPedido(pedido);
        itemPedido2.setQuantidade(BigDecimal.valueOf(1));
        itemPedido2.setValorUnitario(BigDecimal.valueOf(30.00));
        itemPedido2.setValorTotal(BigDecimal.valueOf(30.00));

        when(itemPedidoRepository.findAll()).thenReturn(List.of(itemPedido, itemPedido2));

        // Act
        List<ItemPedido> resultado = itemPedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdProdutoPedido());
        assertEquals(2, resultado.get(1).getIdProdutoPedido());
        verify(itemPedidoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar um novo item de pedido com sucesso")
    void testCadastrar() {
        // Arrange
        when(produtoRepository.existsById(itemPedidoRequestDTO.getFkProduto())).thenReturn(true);
        when(pedidoRepository.existsById(itemPedidoRequestDTO.getFkPedido())).thenReturn(true);
        when(itemPedidoMapper.toEntity(itemPedidoRequestDTO)).thenReturn(itemPedido);
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenReturn(itemPedido);

        // Act
        ItemPedido resultado = itemPedidoService.cadastrar(itemPedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getProduto().getIdProduto());
        assertEquals(1, resultado.getPedido().getIdPedido());
        assertEquals(BigDecimal.valueOf(2), resultado.getQuantidade());
        assertEquals(BigDecimal.valueOf(45.90), resultado.getValorUnitario());
        assertEquals(BigDecimal.valueOf(91.80), resultado.getValorTotal());
        verify(produtoRepository).existsById(itemPedidoRequestDTO.getFkProduto());
        verify(pedidoRepository).existsById(itemPedidoRequestDTO.getFkPedido());
        verify(itemPedidoRepository).save(any(ItemPedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar item de pedido com produto inexistente")
    void testCadastrarProdutoInexistente() {
        // Arrange
        when(produtoRepository.existsById(itemPedidoRequestDTO.getFkProduto())).thenReturn(false);

        // Act & Assert
        assertThrows(ProdutoIdDontExistsException.class, () -> {
            itemPedidoService.cadastrar(itemPedidoRequestDTO);
        });

        verify(produtoRepository).existsById(itemPedidoRequestDTO.getFkProduto());
        verify(itemPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar item de pedido com pedido inexistente")
    void testCadastrarPedidoInexistente() {
        // Arrange
        when(produtoRepository.existsById(itemPedidoRequestDTO.getFkProduto())).thenReturn(true);
        when(pedidoRepository.existsById(itemPedidoRequestDTO.getFkPedido())).thenReturn(false);

        // Act & Assert
        assertThrows(PedidoIdDontExistsException.class, () -> {
            itemPedidoService.cadastrar(itemPedidoRequestDTO);
        });

        verify(produtoRepository).existsById(itemPedidoRequestDTO.getFkProduto());
        verify(pedidoRepository).existsById(itemPedidoRequestDTO.getFkPedido());
        verify(itemPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar item de pedido com sucesso")
    void testAtualizar() {
        // Arrange
        Integer id = 1;
        when(itemPedidoRepository.findById(id)).thenReturn(Optional.of(itemPedido));
        when(itemPedidoRepository.existsById(id)).thenReturn(true);
        when(produtoRepository.existsById(itemPedidoRequestDTO.getFkProduto())).thenReturn(true);
        when(pedidoRepository.existsById(itemPedidoRequestDTO.getFkPedido())).thenReturn(true);
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenReturn(itemPedido);

        // Act
        ItemPedido resultado = itemPedidoService.atualizar(id, itemPedidoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getIdProdutoPedido());
        verify(itemPedidoRepository).existsById(id);
        verify(produtoRepository).existsById(itemPedidoRequestDTO.getFkProduto());
        verify(pedidoRepository).existsById(itemPedidoRequestDTO.getFkPedido());
        verify(itemPedidoRepository).save(any(ItemPedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar item de pedido inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer id = 999;
        when(itemPedidoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ItemPedidoIdDontExistsException.class, () -> {
            itemPedidoService.atualizar(id, itemPedidoRequestDTO);
        });

        verify(itemPedidoRepository).existsById(id);
        verify(itemPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar item de pedido com sucesso")
    void testDeletar() {
        // Arrange
        Integer id = 1;
        when(itemPedidoRepository.existsById(id)).thenReturn(true);

        // Act
        itemPedidoService.deletar(id);

        // Assert
        verify(itemPedidoRepository).existsById(id);
        verify(itemPedidoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar item de pedido inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer id = 999;
        when(itemPedidoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ItemPedidoIdDontExistsException.class, () -> {
            itemPedidoService.deletar(id);
        });

        verify(itemPedidoRepository).existsById(id);
        verify(itemPedidoRepository, never()).deleteById(any());
    }
}
