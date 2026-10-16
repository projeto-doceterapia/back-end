package br.com.doceterapia.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * In-memory registry of open {@link SseEmitter} connections for the notification
 * stream. Assumes a single back-end instance: events reach only clients connected
 * to the instance that committed the change, so running more than one instance
 * would need a shared pub/sub channel instead of this in-memory list.
 *
 * <p>A heartbeat comment ({@code : ping}) is sent periodically so proxies do not
 * drop idle connections. The interval and scheduler are injected so tests can
 * drive the heartbeat without waiting the production 25 s.
 */
@Component
public class NotificacaoEmitterRegistry {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;
    private final long heartbeatIntervalMillis;

    public NotificacaoEmitterRegistry(
            ScheduledExecutorService notificacaoHeartbeatScheduler,
            @Value("${assistente.notificacao.stream.heartbeat-millis:25000}") long heartbeatIntervalMillis) {
        this.scheduler = notificacaoHeartbeatScheduler;
        this.heartbeatIntervalMillis = heartbeatIntervalMillis;
    }

    @PostConstruct
    void iniciarHeartbeat() {
        scheduler.scheduleAtFixedRate(
                this::enviarHeartbeat,
                heartbeatIntervalMillis,
                heartbeatIntervalMillis,
                TimeUnit.MILLISECONDS);
    }

    public SseEmitter registrar() {
        return registrar(new SseEmitter(0L));
    }

    // Package-private seam: lets a test register a capturing emitter.
    SseEmitter registrar(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void enviar(String nomeEvento, Object dados) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(nomeEvento).data(dados));
            } catch (IOException | IllegalStateException e) {
                removerComFalha(emitter, e);
            }
        }
    }

    void enviarHeartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (IOException | IllegalStateException e) {
                removerComFalha(emitter, e);
            }
        }
    }

    public int conexoesAtivas() {
        return emitters.size();
    }

    private void removerComFalha(SseEmitter emitter, Throwable causa) {
        emitters.remove(emitter);
        emitter.completeWithError(causa);
    }

    @PreDestroy
    void encerrar() {
        scheduler.shutdownNow();
    }
}
