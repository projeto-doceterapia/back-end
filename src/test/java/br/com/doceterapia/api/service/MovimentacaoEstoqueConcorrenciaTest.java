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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Teste de concorrência de entradas de estoque")
class MovimentacaoEstoqueConcorrenciaTest {

    @Autowired
    private MovimentacaoEstoqueService service;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Test
    @DisplayName("Entradas concorrentes em um insumo somam o saldo final exato")
    void entradasConcorrentes() throws Exception {
        CategoriaInsumo categoria = new CategoriaInsumo();
        categoria.setNome("Concorrência");
        categoria = categoriaInsumoRepository.save(categoria);

        Insumo insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome("Insumo concorrente");
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setQuantidadeMinima(BigDecimal.ZERO);
        insumo.setUnidade(UnidadeMedida.UNIDADE);
        insumo.setStatus(StatusAtivo.ATIVO);
        final Integer insumoId = insumoRepository.save(insumo).getIdInsumo();

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int indice = i;
            futuros.add(pool.submit(() -> {
                largada.await();
                service.registrarEntradaCompra(new MovimentacaoEstoqueService.EntradaCompra(
                        insumoId, BigDecimal.ONE, UnidadeMedida.UNIDADE, null,
                        OrigemMovimentacao.TEXTO, null, "fluxo-" + indice, "chave-" + indice, null));
                return null;
            }));
        }

        largada.countDown();
        for (Future<?> futuro : futuros) {
            futuro.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        BigDecimal saldoFinal = insumoRepository.findById(insumoId).orElseThrow().getQuantidadeAtual();
        assertEquals(0, BigDecimal.valueOf(threads).compareTo(saldoFinal),
                "Saldo final deveria ser exatamente " + threads);
        assertEquals(threads, movimentacaoRepository.buscarHistorico(insumoId, null, null).size());
    }
}
