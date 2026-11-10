package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ProdutoInsumoRequestDTO;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.Produto;
import br.com.doceterapia.api.entity.ProdutoInsumo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.ProdutoInsumoIdDontExistsException;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
import br.com.doceterapia.api.mapper.ProdutoInsumoMapper;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.ProdutoInsumoRepository;
import br.com.doceterapia.api.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProdutoInsumoService")
class ProdutoInsumoServiceTest {

    @Mock
    private ProdutoInsumoRepository repository;

    @Mock
    private ProdutoInsumoMapper mapper;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private InsumoRepository insumoRepository;

    private ProdutoInsumoService service;

    private Insumo insumoPorUnidade;
    private Insumo insumoEmKg;
    private ProdutoInsumoRequestDTO gramasNoInsumoPorUnidade;

    @BeforeEach
    void setUp() {
        service = new ProdutoInsumoService(repository, mapper, produtoRepository, insumoRepository,
                new ConversaoUnidadeService());

        insumoPorUnidade = new Insumo();
        insumoPorUnidade.setIdInsumo(3);
        insumoPorUnidade.setUnidade(UnidadeMedida.UNIDADE);

        insumoEmKg = new Insumo();
        insumoEmKg.setIdInsumo(4);
        insumoEmKg.setUnidade(UnidadeMedida.KG);

        gramasNoInsumoPorUnidade = new ProdutoInsumoRequestDTO();
        gramasNoInsumoPorUnidade.setFkInsumo(3);
        gramasNoInsumoPorUnidade.setQuantidadeUtilizada(new BigDecimal("60.000"));
        gramasNoInsumoPorUnidade.setUnidade(UnidadeMedida.G);
    }

    private ProdutoInsumo linhaDoProduto(int idLinha, int idProduto, Insumo insumo) {
        Produto produto = new Produto();
        produto.setIdProduto(idProduto);
        ProdutoInsumo linha = new ProdutoInsumo();
        linha.setIdProdutoInsumo(idLinha);
        linha.setProduto(produto);
        linha.setInsumo(insumo);
        return linha;
    }

    @Test
    @DisplayName("Deve recusar no cadastro unidade incompatível com a do insumo")
    void cadastrarComUnidadeIncompativelLanca422() {
        when(produtoRepository.existsById(1)).thenReturn(true);
        when(insumoRepository.findById(3)).thenReturn(Optional.of(insumoPorUnidade));

        UnidadeIncompativelException erro = assertThrows(UnidadeIncompativelException.class,
                () -> service.cadastrar(1, gramasNoInsumoPorUnidade));

        assertEquals("Unidade G é incompatível com a unidade UNIDADE do insumo", erro.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar na atualização unidade incompatível, inclusive ao trocar de insumo")
    void atualizarComUnidadeIncompativelLanca422() {
        when(repository.findById(9)).thenReturn(Optional.of(linhaDoProduto(9, 1, insumoEmKg)));
        when(insumoRepository.findById(3)).thenReturn(Optional.of(insumoPorUnidade));

        assertThrows(UnidadeIncompativelException.class,
                () -> service.atualizar(1, 9, gramasNoInsumoPorUnidade));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve aceitar unidade compatível com conversão ao atualizar")
    void atualizarComUnidadeCompativelGrava() {
        ProdutoInsumo linha = linhaDoProduto(9, 1, insumoEmKg);
        ProdutoInsumoRequestDTO dto = new ProdutoInsumoRequestDTO();
        dto.setFkInsumo(4);
        dto.setQuantidadeUtilizada(new BigDecimal("500.000"));
        dto.setUnidade(UnidadeMedida.G);
        when(repository.findById(9)).thenReturn(Optional.of(linha));
        when(insumoRepository.findById(4)).thenReturn(Optional.of(insumoEmKg));

        service.atualizar(1, 9, dto);

        assertEquals(UnidadeMedida.G, linha.getUnidade());
        verify(repository).save(linha);
    }

    @Test
    @DisplayName("Deve responder 404 ao atualizar item da ficha de outro produto")
    void atualizarItemDeOutroProdutoLanca404() {
        when(repository.findById(9)).thenReturn(Optional.of(linhaDoProduto(9, 2, insumoEmKg)));

        assertThrows(ProdutoInsumoIdDontExistsException.class,
                () -> service.atualizar(1, 9, gramasNoInsumoPorUnidade));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve responder 404 ao deletar item inexistente")
    void deletarInexistenteLanca404() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProdutoInsumoIdDontExistsException.class, () -> service.deletar(1, 99));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve responder 404 ao deletar item da ficha de outro produto")
    void deletarItemDeOutroProdutoLanca404() {
        when(repository.findById(9)).thenReturn(Optional.of(linhaDoProduto(9, 2, insumoEmKg)));

        assertThrows(ProdutoInsumoIdDontExistsException.class, () -> service.deletar(1, 9));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve deletar item da ficha do próprio produto")
    void deletarItemDoProdutoRemove() {
        ProdutoInsumo linha = linhaDoProduto(9, 1, insumoEmKg);
        when(repository.findById(9)).thenReturn(Optional.of(linha));

        service.deletar(1, 9);

        verify(repository).delete(linha);
    }
}
