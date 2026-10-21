package br.com.doceterapia.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class NotificacaoStreamConfig {

    /**
     * Single-thread scheduler that drives the SSE heartbeat. A test can replace
     * this bean to control the heartbeat timing instead of waiting the real
     * interval.
     */
    @Bean
    public ScheduledExecutorService notificacaoHeartbeatScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "notificacao-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }
}
