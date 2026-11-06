package br.com.doceterapia.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Security settings from {@code seguranca.*}. A blank orchestrator token turns
 * the service credential off.
 */
@Component
@ConfigurationProperties(prefix = "seguranca")
public class SegurancaProperties {

    private final Orquestrador orquestrador = new Orquestrador();

    public Orquestrador getOrquestrador() {
        return orquestrador;
    }

    public static class Orquestrador {

        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
