package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.InsumoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoRequestDTO;
import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.CategoriaInsumo;
import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.CategoriaInsumoRepository;
import br.com.doceterapia.api.repository.InsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
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

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

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

        assertThat(cliente.notificacoesDeEstoqueBaixo()).isEmpty();
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
        assertThat(cliente.notificacoesDeEstoqueBaixo()).isEmpty();
    }

    @Test
    @DisplayName("Alerta automático de estoque baixo chega ao cliente depois do commit")
    void alertaAutomaticoDeEstoqueBaixoChegaAoCliente() {
        Insumo insumo = criarInsumo("Farinha alerta confirmado", "3", "2");
        EmitterCaptura cliente = conectarCliente();

        insumoService.atualizar(insumo.getIdInsumo(), edicao(insumo, "1", "2"));

        assertThat(cliente.notificacoesDoInsumo(insumo)).singleElement()
                .satisfies(dto -> {
                    assertThat(dto.getTipo()).isEqualTo(TipoNotificacao.ESTOQUE_BAIXO);
                    assertThat(dto.getIdNotificacao()).isNotNull();
                });
        assertThat(cliente.marcadores).anyMatch(m -> m.contains("notificacao"));
    }

    @Test
    @DisplayName("Alerta automático em transação revertida não chega ao cliente")
    void alertaAutomaticoRevertidoNaoChegaAoCliente() {
        Insumo insumo = criarInsumo("Farinha alerta revertido", "3", "2");
        EmitterCaptura cliente = conectarCliente();

        transactionTemplate.execute(status -> {
            insumoService.atualizar(insumo.getIdInsumo(), edicao(insumo, "1", "2"));
            status.setRollbackOnly();
            return null;
        });

        assertThat(cliente.notificacoesDoInsumo(insumo)).isEmpty();
    }

    private Insumo criarInsumo(String nome, String saldo, String minimo) {
        CategoriaInsumo categoria = new CategoriaInsumo();
        categoria.setNome("Categoria " + nome);
        categoriaInsumoRepository.save(categoria);

        Insumo insumo = new Insumo();
        insumo.setCategoria(categoria);
        insumo.setNome(nome);
        insumo.setQuantidadeAtual(new BigDecimal(saldo));
        insumo.setQuantidadeMinima(new BigDecimal(minimo));
        insumo.setUnidade(UnidadeMedida.KG);
        insumo.setStatus(StatusAtivo.ATIVO);
        return insumoRepository.save(insumo);
    }

    private InsumoRequestDTO edicao(Insumo insumo, String saldo, String minimo) {
        InsumoRequestDTO request = new InsumoRequestDTO();
        request.setFkCategoriaInsumo(insumo.getCategoria().getIdCategoriaInsumo());
        request.setNome(insumo.getNome());
        request.setQuantidadeAtual(new BigDecimal(saldo));
        request.setQuantidadeMinima(new BigDecimal(minimo));
        request.setUnidade(insumo.getUnidade());
        request.setStatus(insumo.getStatus());
        return request;
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

        // The summary scheduler may also push once when the context starts, so
        // assertions look only at the events their test can produce.
        private List<NotificacaoResponseDTO> notificacoesDeEstoqueBaixo() {
            return notificacoes().stream()
                    .filter(dto -> dto.getTipo() == TipoNotificacao.ESTOQUE_BAIXO)
                    .toList();
        }

        private List<NotificacaoResponseDTO> notificacoesDoInsumo(Insumo insumo) {
            return notificacoes().stream()
                    .filter(dto -> insumo.getIdInsumo().equals(dto.getFkInsumo()))
                    .toList();
        }

        private List<NotificacaoResponseDTO> notificacoes() {
            return payloads.stream()
                    .filter(NotificacaoResponseDTO.class::isInstance)
                    .map(NotificacaoResponseDTO.class::cast)
                    .toList();
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
