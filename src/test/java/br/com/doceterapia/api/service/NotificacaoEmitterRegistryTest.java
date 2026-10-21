package br.com.doceterapia.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class NotificacaoEmitterRegistryTest {

    private ScheduledExecutorService scheduler;
    private Runnable heartbeatTask;
    private NotificacaoEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        scheduler = mock(ScheduledExecutorService.class);
        // Capture the scheduled heartbeat so the test can fire it without waiting 25 s.
        doAnswer(invocation -> {
            heartbeatTask = invocation.getArgument(0);
            return null;
        }).when(scheduler).scheduleAtFixedRate(any(Runnable.class), anyLong(), eq(25000L), eq(TimeUnit.MILLISECONDS));

        registry = new NotificacaoEmitterRegistry(scheduler, 25000L);
        registry.iniciarHeartbeat();
    }

    @Test
    @DisplayName("Registra o emitter e o mantém como conexão ativa")
    void registraEmitter() {
        registry.registrar();

        assertThat(registry.conexoesAtivas()).isEqualTo(1);
    }

    @Test
    @DisplayName("O heartbeat é agendado no intervalo injetado e executa sem derrubar conexões ativas")
    void heartbeatAgendadoEExecuta() {
        registry.registrar();

        assertThat(heartbeatTask).as("tarefa de heartbeat agendada").isNotNull();
        heartbeatTask.run();

        assertThat(registry.conexoesAtivas()).isEqualTo(1);
    }
}
