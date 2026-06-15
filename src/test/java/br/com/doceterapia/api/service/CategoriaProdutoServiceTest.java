package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.CategoriaProdutoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.exception.CategoriaProdutoHasProdutosException;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.CategoriaProdutoNomeAlreadyExistsException;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CategoriaProdutoService")
class CategoriaProdutoServiceTest {

    @Mock
    private CategoriaProdutoRepository categoriaProdutoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CategoriaProdutoService categoriaProdutoService;

    private CategoriaProduto categoria;
    private CategoriaProdutoRequestDTO categoriaRequestDTO;

    @BeforeEach
    void setUp() {
        categoria = new CategoriaProduto();
        categoria.setIdCategoriaProduto(1);
        categoria.setNome("Bolos");
        categoria.setDescricao("Categoria para todos os tipos de bolos");

        categoriaRequestDTO = new CategoriaProdutoRequestDTO();
        categoriaRequestDTO.setNome("Bolos");
        categoriaRequestDTO.setDescricao("Categoria para todos os tipos de bolos");
    }

    @Test
    @DisplayName("Deve listar todas as categorias de produto")
    void testListarTodos() {
        // Arrange
        CategoriaProduto categoria2 = new CategoriaProduto();
        categoria2.setIdCategoriaProduto(2);
        categoria2.setNome("Tortas");
        categoria2.setDescricao("Categoria para tortas");

        when(categoriaProdutoRepository.findAll()).thenReturn(List.of(categoria, categoria2));

        // Act
        List<CategoriaProduto> resultado = categoriaProdutoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Bolos", resultado.get(0).getNome());
        assertEquals("Tortas", resultado.get(1).getNome());
        verify(categoriaProdutoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver categorias")
    void testListarTodosVazio() {
        // Arrange
        when(categoriaProdutoRepository.findAll()).thenReturn(List.of());

        // Act
        List<CategoriaProduto> resultado = categoriaProdutoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(categoriaProdutoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar uma nova categoria com sucesso")
    void testCadastrar() {
        // Arrange
        when(categoriaProdutoRepository.existsByNome(categoriaRequestDTO.getNome())).thenReturn(false);
        when(categoriaProdutoRepository.save(any(CategoriaProduto.class))).thenReturn(categoria);

        // Act
        CategoriaProduto resultado = categoriaProdutoService.cadastrar(categoriaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Bolos", resultado.getNome());
        assertEquals("Categoria para todos os tipos de bolos", resultado.getDescricao());
        verify(categoriaProdutoRepository).existsByNome(categoriaRequestDTO.getNome());
        verify(categoriaProdutoRepository).save(any(CategoriaProduto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar categoria com nome duplicado")
    void testCadastrarNomeDuplicado() {
        // Arrange
        when(categoriaProdutoRepository.existsByNome(categoriaRequestDTO.getNome())).thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaProdutoNomeAlreadyExistsException.class, () -> {
            categoriaProdutoService.cadastrar(categoriaRequestDTO);
        });

        verify(categoriaProdutoRepository).existsByNome(categoriaRequestDTO.getNome());
        verify(categoriaProdutoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void testAtualizar() {
        // Arrange
        Integer idCategoria = 1;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(true);
        when(categoriaProdutoRepository.existsByNomeAndIdCategoriaProdutoNot(categoriaRequestDTO.getNome(), idCategoria))
                .thenReturn(false);
        when(categoriaProdutoRepository.save(any(CategoriaProduto.class))).thenReturn(categoria);

        // Act
        CategoriaProduto resultado = categoriaProdutoService.atualizar(idCategoria, categoriaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(idCategoria, resultado.getIdCategoriaProduto());
        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(categoriaProdutoRepository).existsByNomeAndIdCategoriaProdutoNot(categoriaRequestDTO.getNome(), idCategoria);
        verify(categoriaProdutoRepository).save(any(CategoriaProduto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer idCategoria = 999;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaProdutoIdDontExistsException.class, () -> {
            categoriaProdutoService.atualizar(idCategoria, categoriaRequestDTO);
        });

        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(categoriaProdutoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria com nome duplicado")
    void testAtualizarNomeDuplicado() {
        // Arrange
        Integer idCategoria = 1;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(true);
        when(categoriaProdutoRepository.existsByNomeAndIdCategoriaProdutoNot(categoriaRequestDTO.getNome(), idCategoria))
                .thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaProdutoNomeAlreadyExistsException.class, () -> {
            categoriaProdutoService.atualizar(idCategoria, categoriaRequestDTO);
        });

        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(categoriaProdutoRepository).existsByNomeAndIdCategoriaProdutoNot(categoriaRequestDTO.getNome(), idCategoria);
        verify(categoriaProdutoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar categoria com sucesso")
    void testDeletar() {
        // Arrange
        Integer idCategoria = 1;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(true);
        when(produtoRepository.existsByCategoriaProdutoIdCategoriaProduto(idCategoria)).thenReturn(false);

        // Act
        categoriaProdutoService.deletar(idCategoria);

        // Assert
        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(produtoRepository).existsByCategoriaProdutoIdCategoriaProduto(idCategoria);
        verify(categoriaProdutoRepository).deleteById(idCategoria);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar categoria inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer idCategoria = 999;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaProdutoIdDontExistsException.class, () -> {
            categoriaProdutoService.deletar(idCategoria);
        });

        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(categoriaProdutoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar categoria com produtos associados")
    void testDeletarComProdutos() {
        // Arrange
        Integer idCategoria = 1;
        when(categoriaProdutoRepository.existsById(idCategoria)).thenReturn(true);
        when(produtoRepository.existsByCategoriaProdutoIdCategoriaProduto(idCategoria)).thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaProdutoHasProdutosException.class, () -> {
            categoriaProdutoService.deletar(idCategoria);
        });

        verify(categoriaProdutoRepository).existsById(idCategoria);
        verify(produtoRepository).existsByCategoriaProdutoIdCategoriaProduto(idCategoria);
        verify(categoriaProdutoRepository, never()).deleteById(any());
    }
}
