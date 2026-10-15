package br.com.doceterapia.api.service;

import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.repository.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Teste de concorrência de idempotência de entradas de estoque")
class MovimentacaoEstoqueIdempotenciaConcorrenciaTest {

    @Autowired
    private MovimentacaoEstoqueService service;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Test
    @DisplayName("Mesma chave concorrente grava uma linha e um único não-duplicado")
    void mesmaChaveConcorrente() throws Exception {
        CategoriaInsumo categoria = new CategoriaInsumo();
        categoria.setNome("Idempotência");
        categoria = categoriaInsumoRepository.save(categoria);

        Insumo insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome("Insumo idempotente");
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setQuantidadeMinima(BigDecimal.ZERO);
        insumo.setUnidade(UnidadeMedida.UNIDADE);
        insumo.setStatus(StatusAtivo.ATIVO);
        final Integer insumoId = insumoRepository.save(insumo).getIdInsumo();

        final String chave = "chave-compartilhada";
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barreira = new CyclicBarrier(threads);
        List<Future<MovimentacaoEstoqueService.ResultadoMovimentacao>> futuros = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futuros.add(pool.submit(() -> {
                barreira.await();
                return service.registrarEntradaCompra(new MovimentacaoEstoqueService.EntradaCompra(
                        insumoId, BigDecimal.TEN, UnidadeMedida.UNIDADE, null,
                        OrigemMovimentacao.TEXTO, null, "fluxo-fixo", chave, null));
            }));
        }

        int naoDuplicados = 0;
        for (Future<MovimentacaoEstoqueService.ResultadoMovimentacao> futuro : futuros) {
            MovimentacaoEstoqueService.ResultadoMovimentacao resultado = futuro.get(30, TimeUnit.SECONDS);
            if (!resultado.duplicado()) {
                naoDuplicados++;
            }
        }
        pool.shutdown();

        assertEquals(1, naoDuplicados, "Apenas uma resposta deveria ser não-duplicada");
        assertEquals(1, movimentacaoRepository.buscarHistorico(insumoId, null, null).size(),
                "Deveria existir exatamente uma movimentação");
        BigDecimal saldoFinal = insumoRepository.findById(insumoId).orElseThrow().getQuantidadeAtual();
        assertEquals(0, BigDecimal.TEN.compareTo(saldoFinal), "O saldo deveria refletir uma única entrada");
    }
}
