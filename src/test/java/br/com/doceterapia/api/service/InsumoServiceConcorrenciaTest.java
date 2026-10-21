package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.entity.MovimentacaoEstoque;
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
@DisplayName("Teste de concorrência de atualização de saldo de insumo")
class InsumoServiceConcorrenciaTest {

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Test
    @DisplayName("PUTs concorrentes para o mesmo alvo deixam o saldo coerente com o razão")
    void atualizacoesConcorrentes() throws Exception {
        CategoriaInsumo categoria = new CategoriaInsumo();
        categoria.setNome("Concorrência ajuste");
        final Integer categoriaId = categoriaInsumoRepository.save(categoria).getIdCategoriaInsumo();

        Insumo insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome("Insumo ajuste concorrente");
        insumo.setQuantidadeAtual(BigDecimal.ZERO);
        insumo.setQuantidadeMinima(BigDecimal.ZERO);
        insumo.setUnidade(UnidadeMedida.UNIDADE);
        insumo.setStatus(StatusAtivo.ATIVO);
        final Integer insumoId = insumoRepository.save(insumo).getIdInsumo();

        BigDecimal alvo = BigDecimal.valueOf(100);
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                insumoService.atualizar(insumoId, novoRequest(categoriaId, alvo));
                return null;
            }));
        }

        largada.countDown();
        for (Future<?> futuro : futuros) {
            futuro.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        BigDecimal saldoFinal = insumoRepository.findById(insumoId).orElseThrow().getQuantidadeAtual();
        BigDecimal somaRazao = somarMovimentacoes(insumoId);

        assertEquals(0, alvo.compareTo(saldoFinal), "O saldo final deveria ser exatamente o alvo");
        assertEquals(0, saldoFinal.compareTo(somaRazao),
                "O saldo final deve ser igual à soma das movimentações do razão");
    }

    private BigDecimal somarMovimentacoes(Integer insumoId) {
        BigDecimal soma = BigDecimal.ZERO;
        for (MovimentacaoEstoque movimentacao : movimentacaoRepository.buscarHistorico(insumoId, null, null)) {
            soma = soma.add(movimentacao.getQuantidade());
        }
        return soma;
    }

    private InsumoRequestDTO novoRequest(Integer categoriaId, BigDecimal quantidadeAtual) {
        InsumoRequestDTO request = new InsumoRequestDTO();
        request.setFkCategoriaInsumo(categoriaId);
        request.setNome("Insumo ajuste concorrente");
        request.setQuantidadeAtual(quantidadeAtual);
        request.setQuantidadeMinima(BigDecimal.ZERO);
        request.setUnidade(UnidadeMedida.UNIDADE);
        request.setStatus(StatusAtivo.ATIVO);
        return request;
    }
}
