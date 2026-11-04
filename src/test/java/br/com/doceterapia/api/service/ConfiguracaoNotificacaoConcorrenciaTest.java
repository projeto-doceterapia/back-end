package br.com.doceterapia.api.service;

import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Teste de concorrência da linha única de configuração de notificações")
class ConfiguracaoNotificacaoConcorrenciaTest {

    @Autowired
    private ConfiguracaoNotificacaoService service;

    @Autowired
    private ConfiguracaoNotificacaoRepository repository;

    @Test
    @DisplayName("Leituras concorrentes com a tabela vazia criam uma única linha")
    void leiturasConcorrentesCriamUmaLinha() throws Exception {
        // Arrange
        repository.deleteAll();
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                service.obterOuCriar();
                return null;
            }));
        }

        // Act
        largada.countDown();
        for (Future<?> futuro : futuros) {
            futuro.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        // Assert
        assertEquals(1, repository.count());
    }
}
