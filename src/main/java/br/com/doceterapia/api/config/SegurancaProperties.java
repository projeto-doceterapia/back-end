package br.com.doceterapia.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Security settings from {@code seguranca.*}. A blank orchestrator token turns
 * the service credential off; an empty origin list allows no cross-origin call.
 */
@Component
@ConfigurationProperties(prefix = "seguranca")
public class SegurancaProperties {

    private final Orquestrador orquestrador = new Orquestrador();
    private final Cors cors = new Cors();

    public Orquestrador getOrquestrador() {
        return orquestrador;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Cors {

        private List<String> origensPermitidas = List.of();

        public List<String> getOrigensPermitidas() {
            return origensPermitidas;
        }

        public void setOrigensPermitidas(List<String> origensPermitidas) {
            this.origensPermitidas = origensPermitidas;
        }
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
