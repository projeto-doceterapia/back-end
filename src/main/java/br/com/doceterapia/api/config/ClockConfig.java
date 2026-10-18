package br.com.doceterapia.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    private final String fusoHorario;

    public ClockConfig(@Value("${assistente.fuso-horario:America/Sao_Paulo}") String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    /**
     * Clock used by the notification creation service to decide day and
     * business-hour boundaries. Built on an explicit business time zone instead
     * of the server default so those boundaries do not depend on the host zone.
     * Tests inject a fixed clock to assert delivery windows deterministically.
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
