package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.enums.TipoNotificacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real create/mark-as-read -> event publication -> AFTER_COMMIT
 * listener -> registry path. A capturing emitter stands in for a connected
 * client so the test can assert exactly what reached it.
 */
@SpringBootTest
class NotificacaoStreamIntegrationTest {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NotificacaoEmitterRegistry registry;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private NotificacaoRequestDTO alertaEstoqueBaixo() {
        NotificacaoRequestDTO request = new NotificacaoRequestDTO();
        request.setTipo(TipoNotificacao.ESTOQUE_BAIXO);
        request.setTitulo("Estoque baixo de farinha");
        request.setMensagem("O insumo farinha está abaixo do mínimo");
        return request;
    }

    private EmitterCaptura conectarCliente() {
        EmitterCaptura emitter = new EmitterCaptura();
        registry.registrar(emitter);
        return emitter;
    }

    @Test
    @DisplayName("Notificação criada e commitada chega ao cliente conectado como evento 'notificacao'")
    void notificacaoCommitadaChegaAoCliente() {
        EmitterCaptura cliente = conectarCliente();

        NotificacaoResponseDTO criada = notificacaoService.cadastrar(alertaEstoqueBaixo());

        assertThat(cliente.marcadores).anyMatch(m -> m.contains("notificacao"));
        assertThat(cliente.payloads).anySatisfy(payload -> {
            assertThat(payload).isInstanceOf(NotificacaoResponseDTO.class);
            NotificacaoResponseDTO dto = (NotificacaoResponseDTO) payload;
            assertThat(dto.getIdNotificacao()).isEqualTo(criada.getIdNotificacao());
            assertThat(dto.getTipo()).isEqualTo(TipoNotificacao.ESTOQUE_BAIXO);
        });
    }

    @Test
    @DisplayName("Marcação de leitura commitada chega ao cliente conectado como evento 'lida'")
    void marcacaoLidaCommitadaChegaAoCliente() {
        NotificacaoResponseDTO criada = notificacaoService.cadastrar(alertaEstoqueBaixo());
        EmitterCaptura cliente = conectarCliente();

        notificacaoService.marcarLida(criada.getIdNotificacao());

        assertThat(cliente.marcadores).anyMatch(m -> m.contains("lida"));
        assertThat(cliente.payloads).anySatisfy(payload -> {
            assertThat(payload).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> mapa = (Map<String, Object>) payload;
            assertThat(mapa).containsEntry("id", criada.getIdNotificacao());
        });
    }

    @Test
    @DisplayName("Transação revertida não empurra nenhum evento ao cliente")
    void transacaoRevertidaNaoEmpurraEvento() {
        EmitterCaptura cliente = conectarCliente();

        transactionTemplate.execute(status -> {
            notificacaoService.cadastrar(alertaEstoqueBaixo());
            status.setRollbackOnly();
            return null;
        });

        assertThat(cliente.payloads).isEmpty();
    }

    @Test
    @DisplayName("Cliente que fecha a conexão é removido e deixa de receber eventos")
    void clienteFechadoEhRemovido() {
        EmitterCaptura cliente = conectarCliente();
        int antes = registry.conexoesAtivas();
        assertThat(antes).isGreaterThanOrEqualTo(1);

        cliente.onCompletionCallback.run();

        assertThat(registry.conexoesAtivas()).isEqualTo(antes - 1);

        notificacaoService.cadastrar(alertaEstoqueBaixo());
        assertThat(cliente.payloads).isEmpty();
    }

    /**
     * Captures what the registry sends. The data parts rendered by the SSE
     * builder are kept as-is: the event-name marker is a string, and the payload
     * is the unserialized object, so a test can inspect it directly.
     */
    private static final class EmitterCaptura extends SseEmitter {

        private final List<String> marcadores = new ArrayList<>();
        private final List<Object> payloads = new ArrayList<>();
        private Runnable onCompletionCallback = () -> {
        };

        private EmitterCaptura() {
            super(0L);
        }

        @Override
        public void onCompletion(Runnable callback) {
            this.onCompletionCallback = callback;
        }

        @Override
        public void send(SseEventBuilder builder) {
            for (DataWithMediaType parte : builder.build()) {
                Object dados = parte.getData();
                if (dados instanceof String texto) {
                    marcadores.add(texto);
                } else {
                    payloads.add(dados);
                }
            }
        }

        @Override
        public void send(Object object, MediaType mediaType) {
            payloads.add(object);
        }
    }
}
