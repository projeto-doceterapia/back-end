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

    @Test
    @DisplayName("Busca por período inclui o último dia inteiro e exclui o dia seguinte à meia-noite")
    void buscarPorPeriodoComFimExclusivo() {
        salvarEm("p:antes", LocalDateTime.of(2026, 11, 2, 23, 59));
        salvarEm("p:inicio", LocalDateTime.of(2026, 11, 3, 0, 0));
        salvarEm("p:fim", LocalDateTime.of(2026, 11, 4, 23, 59));
        salvarEm("p:depois", LocalDateTime.of(2026, 11, 5, 0, 0));

        var encontradas = repository.buscarPorPeriodo(null,
                LocalDateTime.of(2026, 11, 3, 0, 0), LocalDateTime.of(2026, 11, 5, 0, 0));

        assertEquals(2, encontradas.size());
        assertEquals(LocalDateTime.of(2026, 11, 4, 23, 59), encontradas.get(0).getOcorridoEm());
        assertEquals(LocalDateTime.of(2026, 11, 3, 0, 0), encontradas.get(1).getOcorridoEm());
    }

    @Test
    @DisplayName("Busca por período filtra por insumo quando informado")
    void buscarPorPeriodoFiltraPorInsumo() {
        salvarEm("p:a", LocalDateTime.of(2026, 11, 3, 10, 0));
        Insumo outro = new Insumo();
        outro.setCategoria(insumo.getCategoria());
        outro.setNome("Açúcar");
        outro.setQuantidadeAtual(BigDecimal.ZERO);
        outro.setQuantidadeMinima(BigDecimal.ZERO);
        outro.setUnidade(UnidadeMedida.KG);
        outro.setStatus(StatusAtivo.ATIVO);
        outro = insumoRepository.save(outro);
        MovimentacaoEstoque doOutro = novaMovimentacao("p:b", null);
        doOutro.setInsumo(outro);
        doOutro.setOcorridoEm(LocalDateTime.of(2026, 11, 3, 11, 0));
        repository.saveAndFlush(doOutro);

        var inicio = LocalDateTime.of(2026, 11, 3, 0, 0);
        var fim = LocalDateTime.of(2026, 11, 4, 0, 0);

        assertEquals(2, repository.buscarPorPeriodo(null, inicio, fim).size());
        assertEquals(1, repository.buscarPorPeriodo(insumo.getIdInsumo(), inicio, fim).size());
    }

    private void salvarEm(String chave, LocalDateTime ocorridoEm) {
        MovimentacaoEstoque m = novaMovimentacao(chave, null);
        m.setOcorridoEm(ocorridoEm);
        repository.saveAndFlush(m);
    }
}
