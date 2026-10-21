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
import br.com.doceterapia.api.exception.InsumoHasMovimentacoesException;
import br.com.doceterapia.api.exception.InsumoIdDontExistsException;
import br.com.doceterapia.api.mapper.InsumoMapper;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

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
        Insumo insumo2 = new Insumo();
        insumo2.setIdInsumo(2);
        insumo2.setNome("Açúcar refinado");

        when(insumoRepository.findAll()).thenReturn(List.of(insumo, insumo2));

        List<Insumo> resultado = insumoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Farinha de trigo", resultado.get(0).getNome());
        assertEquals("Açúcar refinado", resultado.get(1).getNome());
        verify(insumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver insumos")
    void testListarTodosVazio() {
        when(insumoRepository.findAll()).thenReturn(List.of());

        List<Insumo> resultado = insumoService.listarTodos();

        assertTrue(resultado.isEmpty());
        verify(insumoRepository).findAll();
    }

    @Test
    @DisplayName("Deve cadastrar insumo com saldo inicial através da movimentação")
    void testCadastrarComSaldoInicial() {
        Insumo novo = new Insumo();
        novo.setQuantidadeAtual(BigDecimal.valueOf(10.5));
        when(categoriaInsumoRepository.existsById(1)).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(novo);

        insumoService.cadastrar(insumoRequestDTO);

        // balance is driven by the movement service, not written directly
        assertEquals(0, BigDecimal.ZERO.compareTo(novo.getQuantidadeAtual()));
        verify(insumoRepository).save(novo);
        verify(movimentacaoEstoqueService).registrarSaldoInicial(eq(novo), eq(BigDecimal.valueOf(10.5)));
    }

    @Test
    @DisplayName("Não deve criar saldo inicial quando a quantidade é zero")
    void testCadastrarSemSaldoInicial() {
        Insumo novo = new Insumo();
        novo.setQuantidadeAtual(BigDecimal.ZERO);
        when(categoriaInsumoRepository.existsById(1)).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(novo);

        insumoService.cadastrar(insumoRequestDTO);

        verify(insumoRepository).save(novo);
        verify(movimentacaoEstoqueService, never()).registrarSaldoInicial(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar insumo com categoria inexistente")
    void testCadastrarCategoriaInexistente() {
        when(categoriaInsumoRepository.existsById(1)).thenReturn(false);

        assertThrows(CategoriaInsumoIdDontExistsException.class, () -> insumoService.cadastrar(insumoRequestDTO));

        verify(insumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve pedir o ajuste para a quantidade alvo ao atualizar")
    void testAtualizarComAjuste() {
        insumo.setQuantidadeAtual(BigDecimal.valueOf(10));
        insumoRequestDTO.setQuantidadeAtual(BigDecimal.valueOf(7));

        Insumo dados = new Insumo();
        dados.setCategoria(categoriaInsumo);
        dados.setNome("Farinha de trigo");
        dados.setQuantidadeMinima(BigDecimal.valueOf(2.0));
        dados.setUnidade(UnidadeMedida.KG);
        dados.setStatus(StatusAtivo.ATIVO);

        when(insumoRepository.findById(1)).thenReturn(Optional.of(insumo));
        when(categoriaInsumoRepository.existsById(1)).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(dados);

        insumoService.atualizar(1, insumoRequestDTO);

        verify(movimentacaoEstoqueService).registrarAjusteParaAlvo(eq(insumo), eq(BigDecimal.valueOf(7)));
        verify(insumoRepository).save(insumo);
    }

    @Test
    @DisplayName("Deve delegar o ajuste mesmo quando a quantidade não muda")
    void testAtualizarSemMudancaDeQuantidade() {
        insumo.setQuantidadeAtual(BigDecimal.valueOf(10));
        insumoRequestDTO.setQuantidadeAtual(BigDecimal.valueOf(10));

        when(insumoRepository.findById(1)).thenReturn(Optional.of(insumo));
        when(categoriaInsumoRepository.existsById(1)).thenReturn(true);
        when(insumoMapper.toEntity(insumoRequestDTO)).thenReturn(new Insumo());

        insumoService.atualizar(1, insumoRequestDTO);

        verify(movimentacaoEstoqueService).registrarAjusteParaAlvo(eq(insumo), eq(BigDecimal.valueOf(10)));
        verify(insumoRepository).save(insumo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar insumo inexistente")
    void testAtualizarInexistente() {
        when(insumoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(InsumoIdDontExistsException.class, () -> insumoService.atualizar(999, insumoRequestDTO));

        verify(insumoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar insumo sem movimentações")
    void testDeletarSemMovimentacoes() {
        when(insumoRepository.existsById(1)).thenReturn(true);
        when(movimentacaoEstoqueRepository.existsByInsumoIdInsumo(1)).thenReturn(false);

        insumoService.deletar(1);

        verify(insumoRepository).deleteById(1);
    }

    @Test
    @DisplayName("Deve bloquear a exclusão de insumo com movimentações (409)")
    void testDeletarComMovimentacoes() {
        when(insumoRepository.existsById(1)).thenReturn(true);
        when(movimentacaoEstoqueRepository.existsByInsumoIdInsumo(1)).thenReturn(true);

        assertThrows(InsumoHasMovimentacoesException.class, () -> insumoService.deletar(1));

        verify(insumoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar insumo inexistente")
    void testDeletarInexistente() {
        when(insumoRepository.existsById(999)).thenReturn(false);

        assertThrows(InsumoIdDontExistsException.class, () -> insumoService.deletar(999));

        verify(insumoRepository, never()).deleteById(any());
    }
}
