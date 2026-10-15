package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoMovimentacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de persistência para MovimentacaoEstoqueRepository")
class MovimentacaoEstoqueRepositoryTest {

    @Autowired
    private MovimentacaoEstoqueRepository repository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    private Insumo insumo;

    @BeforeEach
    void setUp() {
        CategoriaInsumo categoria = new CategoriaInsumo();
        categoria.setNome("Farináceos");
        categoria = categoriaInsumoRepository.save(categoria);

        insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome("Farinha de trigo");
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setQuantidadeMinima(BigDecimal.ZERO);
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setStatus(StatusAtivo.ATIVO);
        insumo = insumoRepository.save(insumo);
    }

    private MovimentacaoEstoque novaMovimentacao(String chaveIdempotencia, String chaveNota) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setInsumo(insumo);
        m.setTipo(TipoMovimentacao.ENTRADA_COMPRA);
        m.setQuantidade(BigDecimal.valueOf(10));
        m.setQuantidadeInformada(BigDecimal.valueOf(10));
        m.setUnidadeInformada(UnidadeMedida.KG);
        m.setOrigem(OrigemMovimentacao.NOTA_IMAGEM);
        m.setChaveNota(chaveNota);
        m.setChaveIdempotencia(chaveIdempotencia);
        m.setHashConteudo("hash");
        m.setOcorridoEm(LocalDateTime.now());
        m.setCriadoEm(LocalDateTime.now());
        return m;
    }

    @Test
    @DisplayName("Deve persistir uma movimentação com chave de idempotência única")
    void persisteChaveUnica() {
        repository.saveAndFlush(novaMovimentacao("fluxo:1:1", null));
        assertTrue(repository.findByChaveIdempotencia("fluxo:1:1").isPresent());
    }

    @Test
    @DisplayName("Deve rejeitar duas movimentações com a mesma chave de idempotência")
    void rejeitaChaveDuplicada() {
        repository.saveAndFlush(novaMovimentacao("fluxo:1:1", null));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(novaMovimentacao("fluxo:1:1", null)));
    }

    @Test
    @DisplayName("Deve permitir múltiplas movimentações manuais sem chave de idempotência")
    void permiteMultiplasSemChave() {
        repository.saveAndFlush(novaMovimentacao(null, null));
        repository.saveAndFlush(novaMovimentacao(null, null));

        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Deve listar os ids de insumo registrados para uma chave de nota")
    void listaInsumosPorChaveNota() {
        repository.saveAndFlush(novaMovimentacao("fluxo:1:1", "12345678901234567890123456789012345678901234"));

        assertEquals(1, repository.findInsumoIdsByChaveNota("12345678901234567890123456789012345678901234").size());
        assertTrue(repository.findInsumoIdsByChaveNota("00000000000000000000000000000000000000000000").isEmpty());
    }
}
