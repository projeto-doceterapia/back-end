package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.mapper.ProdutoMapper;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProdutoService")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaProdutoRepository categoriaProdutoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private ProdutoRequestDTO produtoRequestDTO;
    private CategoriaProduto categoriaProduto;

    @BeforeEach
    void setUp() {
        categoriaProduto = new CategoriaProduto();
        categoriaProduto.setIdCategoriaProduto(1);

        produto = new Produto();
        produto.setIdProduto(1);
        produto.setCategoriaProduto(categoriaProduto);
        produto.setNome("Bolo de Chocolate");
        produto.setCustoEstimado(BigDecimal.valueOf(25.50));
        produto.setPrecoAtual(BigDecimal.valueOf(89.90));
        produto.setPrecoSugerido(BigDecimal.valueOf(99.90));
        produto.setMargemLucro(BigDecimal.valueOf(64.40));
        produto.setUnidadeProducao(UnidadeMedida.UNIDADE);
        produto.setStatus(StatusAtivo.ATIVO);
        produto.setDescricao("Bolo de chocolate com recheio de brigadeiro");

        produtoRequestDTO = new ProdutoRequestDTO();
        produtoRequestDTO.setFkCategoriaProduto(1);
        produtoRequestDTO.setNome("Bolo de Chocolate");
        produtoRequestDTO.setCustoEstimado(BigDecimal.valueOf(25.50));
        produtoRequestDTO.setPrecoAtual(BigDecimal.valueOf(89.90));
        produtoRequestDTO.setPrecoSugerido(BigDecimal.valueOf(99.90));
        produtoRequestDTO.setMargemLucro(BigDecimal.valueOf(64.40));
        produtoRequestDTO.setUnidadeProducao(UnidadeMedida.UNIDADE);
        produtoRequestDTO.setStatus(StatusAtivo.ATIVO);
        produtoRequestDTO.setDescricao("Bolo de chocolate com recheio de brigadeiro");
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void testListarTodos() {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setIdProduto(2);
        produto2.setCategoriaProduto(categoriaProduto);
        produto2.setNome("Torta de Morango");
        produto2.setStatus(StatusAtivo.ATIVO);

        when(produtoRepository.findAll()).thenReturn(List.of(produto, produto2));

        // Act
        List<Produto> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Bolo de Chocolate", resultado.get(0).getNome());
        assertEquals("Torta de Morango", resultado.get(1).getNome());
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver produtos")
    void testListarTodosVazio() {
        // Arrange
        when(produtoRepository.findAll()).thenReturn(List.of());

        // Act
        List<Produto> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar um novo produto com sucesso")
    void testCadastrar() {
        // Arrange
        when(categoriaProdutoRepository.existsById(produtoRequestDTO.getFkCategoriaProduto())).thenReturn(true);
        when(produtoMapper.toEntity(produtoRequestDTO)).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.cadastrar(produtoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Bolo de Chocolate", resultado.getNome());
        assertEquals(1, resultado.getCategoriaProduto().getIdCategoriaProduto());
        assertEquals(BigDecimal.valueOf(89.90), resultado.getPrecoAtual());
        assertEquals(StatusAtivo.ATIVO, resultado.getStatus());
        verify(categoriaProdutoRepository).existsById(produtoRequestDTO.getFkCategoriaProduto());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar produto com categoria inexistente")
    void testCadastrarCategoriaInexistente() {
        // Arrange
        when(categoriaProdutoRepository.existsById(produtoRequestDTO.getFkCategoriaProduto())).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaProdutoIdDontExistsException.class, () -> {
            produtoService.cadastrar(produtoRequestDTO);
        });

        verify(categoriaProdutoRepository).existsById(produtoRequestDTO.getFkCategoriaProduto());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void testAtualizar() {
        // Arrange
        Integer idProduto = 1;
        when(produtoRepository.existsById(idProduto)).thenReturn(true);
        when(categoriaProdutoRepository.existsById(produtoRequestDTO.getFkCategoriaProduto())).thenReturn(true);
        when(produtoMapper.toEntity(produtoRequestDTO)).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.atualizar(idProduto, produtoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idProduto, resultado.getIdProduto());
        verify(produtoRepository).existsById(idProduto);
        verify(categoriaProdutoRepository).existsById(produtoRequestDTO.getFkCategoriaProduto());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer idProduto = 999;
        when(produtoRepository.existsById(idProduto)).thenReturn(false);

        // Act & Assert
        assertThrows(ProdutoIdDontExistsException.class, () -> {
            produtoService.atualizar(idProduto, produtoRequestDTO);
        });

        verify(produtoRepository).existsById(idProduto);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto com categoria inexistente")
    void testAtualizarCategoriaInexistente() {
        // Arrange
        Integer idProduto = 1;
        when(produtoRepository.existsById(idProduto)).thenReturn(true);
        when(categoriaProdutoRepository.existsById(produtoRequestDTO.getFkCategoriaProduto())).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaProdutoIdDontExistsException.class, () -> {
            produtoService.atualizar(idProduto, produtoRequestDTO);
        });

        verify(produtoRepository).existsById(idProduto);
        verify(categoriaProdutoRepository).existsById(produtoRequestDTO.getFkCategoriaProduto());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void testDeletar() {
        // Arrange
        Integer idProduto = 1;
        when(produtoRepository.existsById(idProduto)).thenReturn(true);

        // Act
        produtoService.deletar(idProduto);

        // Assert
        verify(produtoRepository).existsById(idProduto);
        verify(produtoRepository).deleteById(idProduto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer idProduto = 999;
        when(produtoRepository.existsById(idProduto)).thenReturn(false);

        // Act & Assert
        assertThrows(ProdutoIdDontExistsException.class, () -> {
            produtoService.deletar(idProduto);
        });

        verify(produtoRepository).existsById(idProduto);
        verify(produtoRepository, never()).deleteById(any());
    }
}
