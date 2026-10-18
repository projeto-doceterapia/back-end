package br.com.doceterapia.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Supply defaults from {@code assistente.*}. {@code categoriaPadrao}
 * names the fallback category used by get-or-create when no valid category is
 * given; it is created on demand when missing.
 */
@Component
@ConfigurationProperties(prefix = "assistente")
public class InsumoDefaultsProperties {

    private String categoriaPadrao = "A classificar";

    public String getCategoriaPadrao() {
        return categoriaPadrao;
    }

    public void setCategoriaPadrao(String categoriaPadrao) {
        this.categoriaPadrao = categoriaPadrao;
    }
}
