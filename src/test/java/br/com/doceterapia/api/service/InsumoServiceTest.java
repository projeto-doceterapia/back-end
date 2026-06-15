package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.CategoriaInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para InsumoService")
class InsumoServiceTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Mock
    private InsumoMapper insumoMapper;

    @InjectMocks
    private InsumoService insumoService;

    private Insumo insumo;
    private InsumoRequestDTO insumoRequestDTO;
    private CategoriaInsumo categoriaInsumo;

    @BeforeEach
    void setUp() {
        categoriaInsumo = new CategoriaInsumo();
        categoriaInsumo.setIdCategoriaInsumo(1);

        insumo = new Insumo();
        insumo.setIdInsumo(1);
        insumo.setCategoria(categoriaInsumo);
        insumo.setNome("Farinha de trigo");
        insumo.setQuantidadeAtual(BigDecimal.valueOf(10.5));
        insumo.setQuantidadeMinima(BigDecimal.valueOf(2.0));
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setStatus(StatusAtivo.ATIVO);
        insumo.setMarca("Dona Benta");

        insumoRequestDTO = new InsumoRequestDTO();
        insumoRequestDTO.setFkCategoriaInsumo(1);
        insumoRequestDTO.setNome("Farinha de trigo");
        insumoRequestDTO.setQuantidadeAtual(BigDecimal.valueOf(10.5));
        insumoRequestDTO.setQuantidadeMinima(BigDecimal.valueOf(2.0));
        insumoRequestDTO.setUnidade(UnidadeMedida.KG);
        insumoRequestDTO.setStatus(StatusAtivo.ATIVO);
        insumoRequestDTO.setMarca("Dona Benta");
    }

    @Test
    @DisplayName("Deve listar todos os insumos")
    void testListarTodos() {
        // Arrange
        Insumo insumo2 = new Insumo();
        insumo2.setIdInsumo(2);
        insumo2.setCategoria(categoriaInsumo);
        insumo2.setNome("Açúcar refinado");
        insumo2.setQuantidadeAtual(BigDecimal.valueOf(20.0));
        insumo2.setQuantidadeMinima(BigDecimal.valueOf(5.0));
        insumo2.setUnidade(UnidadeMedida.KG);
        insumo2.setStatus(StatusAtivo.ATIVO);
        insumo2.setMarca("União");

        when(insumoRepository.findAll()).thenReturn(List.of(insumo, insumo2));

        // Act
        List<Insumo> resultado = insumoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Farinha de trigo", resultado.get(0).getNome());
        assertEquals("Açúcar refinado", resultado.get(1).getNome());
        verify(insumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver insumos")
    void testListarTodosVazio() {
        // Arrange
        when(insumoRepository.findAll()).thenReturn(List.of());

        // Act
        List<Insumo> resultado = insumoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(insumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar um novo insumo com sucesso")
    void testCadastrar() {
        // Arrange
        when(categoriaInsumoRepository.existsById(insumoRequestDTO.getFkCategoriaInsumo())).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(insumo);
        when(insumoRepository.save(any(Insumo.class))).thenReturn(insumo);

        // Act
        Insumo resultado = insumoService.cadastrar(insumoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Farinha de trigo", resultado.getNome());
        assertEquals(BigDecimal.valueOf(10.5), resultado.getQuantidadeAtual());
        assertEquals(BigDecimal.valueOf(2.0), resultado.getQuantidadeMinima());
        assertEquals(UnidadeMedida.KG, resultado.getUnidade());
        assertEquals(StatusAtivo.ATIVO, resultado.getStatus());
        assertEquals("Dona Benta", resultado.getMarca());
        verify(categoriaInsumoRepository).existsById(insumoRequestDTO.getFkCategoriaInsumo());
        verify(insumoRepository).save(any(Insumo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar insumo com categoria inexistente")
    void testCadastrarCategoriaInexistente() {
        // Arrange
        when(categoriaInsumoRepository.existsById(insumoRequestDTO.getFkCategoriaInsumo())).thenReturn(false);

        // Act & Assert
        assertThrows(CategoriaInsumoIdDontExistsException.class, () -> {
            insumoService.cadastrar(insumoRequestDTO);
        });

        verify(categoriaInsumoRepository).existsById(insumoRequestDTO.getFkCategoriaInsumo());
        verify(insumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar insumo com sucesso")
    void testAtualizar() {
        // Arrange
        Integer id = 1;
        when(insumoRepository.existsById(id)).thenReturn(true);
        when(categoriaInsumoRepository.existsById(insumoRequestDTO.getFkCategoriaInsumo())).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(insumo);
        when(insumoRepository.save(any(Insumo.class))).thenReturn(insumo);

        // Act
        Insumo resultado = insumoService.atualizar(id, insumoRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getIdInsumo());
        verify(insumoRepository).existsById(id);
        verify(categoriaInsumoRepository).existsById(insumoRequestDTO.getFkCategoriaInsumo());
        verify(insumoRepository).save(any(Insumo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar insumo inexistente")
    void testAtualizarInexistente() {
        // Arrange
        Integer id = 999;
        when(insumoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(InsumoIdDontExistsException.class, () -> {
            insumoService.atualizar(id, insumoRequestDTO);
        });

        verify(insumoRepository).existsById(id);
        verify(insumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar insumo com sucesso")
    void testDeletar() {
        // Arrange
        Integer id = 1;
        when(insumoRepository.existsById(id)).thenReturn(true);

        // Act
        insumoService.deletar(id);

        // Assert
        verify(insumoRepository).existsById(id);
        verify(insumoRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar insumo inexistente")
    void testDeletarInexistente() {
        // Arrange
        Integer id = 999;
        when(insumoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(InsumoIdDontExistsException.class, () -> {
            insumoService.deletar(id);
        });

        verify(insumoRepository).existsById(id);
        verify(insumoRepository, never()).deleteById(any());
    }
}
