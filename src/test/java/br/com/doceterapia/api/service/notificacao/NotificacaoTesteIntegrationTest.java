package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.dto.NotificacaoResponseDTO;
import br.com.doceterapia.api.entity.ConfiguracaoNotificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.exception.NotificacaoTesteCooldownException;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import br.com.doceterapia.api.service.NotificacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("Testes de integração da notificação de teste")
class NotificacaoTesteIntegrationTest {

    @Autowired
    private NotificacaoTesteService service;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private ConfiguracaoNotificacaoRepository configuracaoRepository;

    private void limpar() {
        notificacaoRepository.findAll().stream()
                .filter(notificacao -> notificacao.getChaveUnicidade() != null
                        && notificacao.getChaveUnicidade().startsWith(NotificacaoTesteService.PREFIXO_CHAVE))
                .forEach(notificacaoRepository::delete);
        configuracaoRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        limpar();
    }

    @AfterEach
    void tearDown() {
        limpar();
    }

    @Test
    @DisplayName("Cria no sino com os parâmetros do template e recusa o segundo teste no intervalo")
    void criaNoSinoERecusaOSegundo() {
        // Act
        NotificacaoResponseDTO criada = service.enviar();

        // Assert
        assertEquals(StatusEnvioNotificacao.PENDENTE, criada.getStatusEnvio());
        assertEquals("alerta_operacional", criada.getTemplateKey());
        assertTrue(criada.getParametros().contains("\"mensagem\":\"Teste de notificação"));
        assertTrue(notificacaoService.listarNaoLidas().stream()
                .anyMatch(item -> item.getIdNotificacao().equals(criada.getIdNotificacao())));

        NotificacaoTesteCooldownException erro =
                assertThrows(NotificacaoTesteCooldownException.class, service::enviar);
        assertTrue(erro.getSegundosRestantes() > 0);
    }

    @Test
    @DisplayName("Com um interruptor desligado o teste fica só no sino")
    void interruptorDesligadoFicaSoNoSino() {
        // Arrange
        ConfiguracaoNotificacao config = new ConfiguracaoNotificacao();
        config.setAvisosAutomaticosAtivo(false);
        configuracaoRepository.save(config);

        // Act
        NotificacaoResponseDTO criada = service.enviar();

        // Assert
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, criada.getStatusEnvio());
    }
}
