package br.com.doceterapia.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Catalog tuning from {@code assistente.catalogo.*}. {@code limiteMaximo}
 * caps and defaults the page size of {@code GET /insumos/catalogo}.
 */
@Component
@ConfigurationProperties(prefix = "assistente.catalogo")
public class CatalogoProperties {

    private int limiteMaximo = 200;

    public int getLimiteMaximo() {
        return limiteMaximo;
    }

    public void setLimiteMaximo(int limiteMaximo) {
        this.limiteMaximo = limiteMaximo;
    }
}
