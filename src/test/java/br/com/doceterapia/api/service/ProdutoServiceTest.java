package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.doceterapia.api.dto.ProdutoRequestDTO;
import br.com.doceterapia.api.dto.ProdutoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaProduto;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.CategoriaProdutoIdDontExistsException;
import br.com.doceterapia.api.exception.ProdutoIdDontExistsException;
import br.com.doceterapia.api.mapper.ProdutoMapper;
import br.com.doceterapia.api.repository.CategoriaProdutoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import br.com.doceterapia.api.service.precificacao.PrecificacaoProduto;
import br.com.doceterapia.api.service.precificacao.PrecificacaoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Mock
    private PrecificacaoService precificacaoService;

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
        produto.setPrecoAtual(BigDecimal.valueOf(89.90));
        produto.setMargemLucro(BigDecimal.valueOf(64.40));
        produto.setUnidadeProducao(UnidadeMedida.UNIDADE);
        produto.setStatus(StatusAtivo.ATIVO);
        produto.setDescricao("Bolo de chocolate com recheio de brigadeiro");

        produtoRequestDTO = new ProdutoRequestDTO();
        produtoRequestDTO.setFkCategoriaProduto(1);
        produtoRequestDTO.setNome("Bolo de Chocolate");
        produtoRequestDTO.setPrecoAtual(BigDecimal.valueOf(89.90));
        produtoRequestDTO.setMargemLucro(BigDecimal.valueOf(64.40));
        produtoRequestDTO.setUnidadeProducao(UnidadeMedida.UNIDADE);
        produtoRequestDTO.setStatus(StatusAtivo.ATIVO);
        produtoRequestDTO.setDescricao("Bolo de chocolate com recheio de brigadeiro");
    }

    @Test
    @DisplayName("Deve listar todos os produtos com preço calculado")
    void testListarTodos() {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setIdProduto(2);
        produto2.setNome("Torta de Morango");
        List<Produto> produtos = List.of(produto, produto2);
        PrecificacaoProduto precificacao1 = new PrecificacaoProduto(new BigDecimal("5.21"), new BigDecimal("10.50"), null);
        PrecificacaoProduto precificacao2 = PrecificacaoProduto.indisponivel(null);
        ProdutoResponseDTO dto1 = new ProdutoResponseDTO();
        ProdutoResponseDTO dto2 = new ProdutoResponseDTO();

        when(produtoRepository.findAll()).thenReturn(produtos);
        when(precificacaoService.calcular(produtos)).thenReturn(Map.of(1, precificacao1, 2, precificacao2));
        when(produtoMapper.toResponseDTO(produto, precificacao1)).thenReturn(dto1);
        when(produtoMapper.toResponseDTO(produto2, precificacao2)).thenReturn(dto2);

        // Act
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        // Assert
        assertEquals(List.of(dto1, dto2), resultado);
    }

    @Test
    @DisplayName("Deve carregar as fichas de todos os produtos numa única consulta")
    void listarCarregaFichasNumaConsulta() {
        // Arrange
        Produto produto2 = new Produto();
        produto2.setIdProduto(2);
        Produto produto3 = new Produto();
        produto3.setIdProduto(3);
        List<Produto> produtos = List.of(produto, produto2, produto3);
        when(produtoRepository.findAll()).thenReturn(produtos);
        when(precificacaoService.calcular(produtos)).thenReturn(Map.of());

        // Act
        produtoService.listarTodos();

        // Assert
        verify(precificacaoService, times(1)).calcular(produtos);
        verify(precificacaoService, never()).calcular(any(Produto.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver produtos")
    void testListarTodosVazio() {
        // Arrange
        when(produtoRepository.findAll()).thenReturn(List.of());

        // Act
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(produtoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar produto por id com preço calculado")
    void buscarPorId() {
        // Arrange
        PrecificacaoProduto precificacao = new PrecificacaoProduto(new BigDecimal("5.21"), new BigDecimal("10.50"), null);
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(precificacaoService.calcular(produto)).thenReturn(precificacao);
        when(produtoMapper.toResponseDTO(produto, precificacao)).thenReturn(dto);

        // Act & Assert
        assertSame(dto, produtoService.buscarPorId(1));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void buscarPorIdInexistente() {
        // Arrange
        when(produtoRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProdutoIdDontExistsException.class, () -> produtoService.buscarPorId(999));
    }

    @Test
    @DisplayName("Deve cadastrar um novo produto com sucesso")
    void testCadastrar() {
        // Arrange
        when(categoriaProdutoRepository.existsById(produtoRequestDTO.getFkCategoriaProduto())).thenReturn(true);
        when(produtoMapper.toEntity(produtoRequestDTO)).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        when(produtoMapper.toResponseDTO(any(Produto.class), any())).thenReturn(dto);

        // Act
        ProdutoResponseDTO resultado = produtoService.cadastrar(produtoRequestDTO);

        // Assert
        assertSame(dto, resultado);
        verify(precificacaoService).calcular(produto);
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
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        when(produtoMapper.toResponseDTO(any(Produto.class), any())).thenReturn(dto);

        // Act
        ProdutoResponseDTO resultado = produtoService.atualizar(idProduto, produtoRequestDTO);

        // Assert
        assertSame(dto, resultado);
        assertEquals(idProduto, produto.getIdProduto());
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
