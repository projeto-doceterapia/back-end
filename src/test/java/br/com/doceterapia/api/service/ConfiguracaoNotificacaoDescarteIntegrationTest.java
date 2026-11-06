package br.com.doceterapia.api.service;

import br.com.doceterapia.api.dto.ConfiguracaoNotificacaoRequestDTO;
import br.com.doceterapia.api.entity.Notificacao;
import br.com.doceterapia.api.enums.StatusEnvioNotificacao;
import br.com.doceterapia.api.enums.TipoNotificacao;
import br.com.doceterapia.api.repository.ConfiguracaoNotificacaoRepository;
import br.com.doceterapia.api.repository.NotificacaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Testes de integração do descarte de pendentes ao desligar um interruptor")
class ConfiguracaoNotificacaoDescarteIntegrationTest {

    @Autowired
    private ConfiguracaoNotificacaoService service;

    @Autowired
    private ConfiguracaoNotificacaoRepository configuracaoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private final List<Integer> criadas = new ArrayList<>();

    @BeforeEach
    void setUp() {
        configuracaoRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        notificacaoRepository.deleteAllById(criadas);
        configuracaoRepository.deleteAll();
    }

    @Test
    @DisplayName("Desligar um interruptor muda só as pendentes, e religar não as reativa")
    void desligarMudaSoPendentesEReligarNaoReativa() {
        // Arrange
        Integer pendente1 = criar(StatusEnvioNotificacao.PENDENTE);
        Integer pendente2 = criar(StatusEnvioNotificacao.PENDENTE);
        Integer enviada = criar(StatusEnvioNotificacao.ENVIADA);

        // Act
        service.atualizar(requestCom(false, true));

        // Assert
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, statusDe(pendente1));
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, statusDe(pendente2));
        assertEquals(StatusEnvioNotificacao.ENVIADA, statusDe(enviada));

        // Act
        service.atualizar(requestCom(true, true));

        // Assert
        assertEquals(StatusEnvioNotificacao.NAO_ENVIAR, statusDe(pendente1));
    }

    private Integer criar(StatusEnvioNotificacao status) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(TipoNotificacao.ESTOQUE_BAIXO);
        notificacao.setTitulo("Estoque baixo");
        notificacao.setMensagem("Insumo fictício abaixo do mínimo");
        notificacao.setDataCriacao(LocalDateTime.of(2026, 11, 18, 10, 0));
        notificacao.setStatusEnvio(status);
        Integer id = notificacaoRepository.save(notificacao).getIdNotificacao();
        criadas.add(id);
        return id;
    }

    private StatusEnvioNotificacao statusDe(Integer id) {
        return notificacaoRepository.findById(id).orElseThrow().getStatusEnvio();
    }

    private ConfiguracaoNotificacaoRequestDTO requestCom(boolean avisos, boolean assistente) {
        ConfiguracaoNotificacaoRequestDTO request = new ConfiguracaoNotificacaoRequestDTO();
        request.setInicioExpediente(LocalTime.of(8, 0));
        request.setFimExpediente(LocalTime.of(18, 0));
        request.setDiasExpediente(List.of(DayOfWeek.MONDAY));
        request.setHorarioResumoInicio(LocalTime.of(8, 0));
        request.setHorarioResumoFim(LocalTime.of(17, 30));
        request.setDiasAntecedencia(3);
        request.setResumoInicioDiaAtivo(true);
        request.setResumoFimDiaAtivo(true);
        request.setEstoqueBaixoAtivo(true);
        request.setDiaLotadoAtivo(true);
        request.setAvisosAutomaticosAtivo(avisos);
        request.setAssistenteAtivo(assistente);
        return request;
    }
}
