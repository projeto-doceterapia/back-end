package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.CategoriaInsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.exception.CategoriaInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.CategoriaInsumoHasInsumosException;
import br.com.doceterapia.api.exception.CategoriaInsumoNomeAlreadyExistsException;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CategoriaInsumoService")
class CategoriaInsumoServiceTest {

    @Mock
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Mock
    private InsumoRepository insumoRepository;

    @InjectMocks
    private CategoriaInsumoService categoriaInsumoService;

    private CategoriaInsumo categoria;
    private CategoriaInsumoRequestDTO categoriaRequestDTO;

    @BeforeEach
    void setUp() {
        categoria = new CategoriaInsumo();
        categoria.setIdCategoriaInsumo(1);
        categoria.setNome("Farináceos");
        categoria.setDescricao("Farinhas, féculas e amidos");

        categoriaRequestDTO = new CategoriaInsumoRequestDTO();
        categoriaRequestDTO.setNome("Farináceos");
        categoriaRequestDTO.setDescricao("Farinhas, féculas e amidos");
    }

    @Test
    @DisplayName("Deve listar todas as categorias de insumo")
    void testListarTodos() {
        // Arrange
        CategoriaInsumo categoria2 = new CategoriaInsumo();
        categoria2.setIdCategoriaInsumo(2);
        categoria2.setNome("Laticínios");
        categoria2.setDescricao("Leite, creme de leite e manteiga");

        when(categoriaInsumoRepository.findAll()).thenReturn(List.of(categoria, categoria2));

        // Act
        List<CategoriaInsumo> resultado = categoriaInsumoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Farináceos", resultado.get(0).getNome());
        assertEquals("Laticínios", resultado.get(1).getNome());
        verify(categoriaInsumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver categorias")
    void testListarTodosVazio() {
        // Arrange
        when(categoriaInsumoRepository.findAll()).thenReturn(List.of());

        // Act
        List<CategoriaInsumo> resultado = categoriaInsumoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(categoriaInsumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar uma nova categoria com sucesso")
    void testCadastrar() {
        // Arrange
        when(categoriaInsumoRepository.existsByNome(categoriaRequestDTO.getNome())).thenReturn(false);
        when(categoriaInsumoRepository.save(any(CategoriaInsumo.class))).thenReturn(categoria);

        // Act
        CategoriaInsumo resultado = categoriaInsumoService.cadastrar(categoriaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Farináceos", resultado.getNome());
        assertEquals("Farinhas, féculas e amidos", resultado.getDescricao());
        verify(categoriaInsumoRepository).existsByNome(categoriaRequestDTO.getNome());
        verify(categoriaInsumoRepository).save(any(CategoriaInsumo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar categoria com nome duplicado")
    void testCadastrarNomeDuplicado() {
        // Arrange
        when(categoriaInsumoRepository.existsByNome(categoriaRequestDTO.getNome())).thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaInsumoNomeAlreadyExistsException.class, () -> {
            categoriaInsumoService.cadastrar(categoriaRequestDTO);
        });

        verify(categoriaInsumoRepository).existsByNome(categoriaRequestDTO.getNome());
        verify(categoriaInsumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void testAtualizar() {
        // Arrange
        Integer id = 1;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(true);
        when(categoriaInsumoRepository.existsByNomeAndIdCategoriaInsumoNot(categoriaRequestDTO.getNome(), id))
                .thenReturn(false);
        when(categoriaInsumoRepository.save(any(CategoriaInsumo.class))).thenReturn(categoria);

        // Act
        CategoriaInsumo resultado = categoriaInsumoService.atualizar(id, categoriaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getIdCategoriaInsumo());
        verify(categoriaInsumoRepository).existsById(id);
        verify(categoriaInsumoRepository).existsByNomeAndIdCategoriaInsumoNot(categoriaRequestDTO.getNome(), id);
        verify(categoriaInsumoRepository).save(any(CategoriaInsumo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer id = 999;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaInsumoIdDontExistsException.class, () -> {
            categoriaInsumoService.atualizar(id, categoriaRequestDTO);
        });

        verify(categoriaInsumoRepository).existsById(id);
        verify(categoriaInsumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria com nome duplicado")
    void testAtualizarNomeDuplicado() {
        // Arrange
        Integer id = 1;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(true);
        when(categoriaInsumoRepository.existsByNomeAndIdCategoriaInsumoNot(categoriaRequestDTO.getNome(), id))
                .thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaInsumoNomeAlreadyExistsException.class, () -> {
            categoriaInsumoService.atualizar(id, categoriaRequestDTO);
        });

        verify(categoriaInsumoRepository).existsById(id);
        verify(categoriaInsumoRepository).existsByNomeAndIdCategoriaInsumoNot(categoriaRequestDTO.getNome(), id);
        verify(categoriaInsumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar categoria com sucesso")
    void testDeletar() {
        // Arrange
        Integer id = 1;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(true);
        when(insumoRepository.existsByCategoriaIdCategoriaInsumo(id)).thenReturn(false);

        // Act
        categoriaInsumoService.deletar(id);

        // Assert
        verify(categoriaInsumoRepository).existsById(id);
        verify(insumoRepository).existsByCategoriaIdCategoriaInsumo(id);
        verify(categoriaInsumoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar categoria inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer id = 999;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaInsumoIdDontExistsException.class, () -> {
            categoriaInsumoService.deletar(id);
        });

        verify(categoriaInsumoRepository).existsById(id);
        verify(categoriaInsumoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar categoria com insumos associados")
    void testDeletarComInsumos() {
        // Arrange
        Integer id = 1;
        when(categoriaInsumoRepository.existsById(id)).thenReturn(true);
        when(insumoRepository.existsByCategoriaIdCategoriaInsumo(id)).thenReturn(true);

        // Act & Assert
        assertThrows(CategoriaInsumoHasInsumosException.class, () -> {
            categoriaInsumoService.deletar(id);
        });

        verify(categoriaInsumoRepository).existsById(id);
        verify(insumoRepository).existsByCategoriaIdCategoriaInsumo(id);
        verify(categoriaInsumoRepository, never()).deleteById(any());
    }
}
