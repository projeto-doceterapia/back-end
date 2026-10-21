package br.com.doceterapia.api.service;

import br.com.doceterapia.api.exception.CursorInvalidoException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encodes and decodes the opaque keyset cursor for the supply catalog.
 * The cursor carries the last seen {@code (nomeNormalizado, id)}
 * pair so pages stay stable when supplies are inserted. A malformed cursor is
 * rejected with {@link CursorInvalidoException} (HTTP 400, PT-BR).
 */
@Component
public class CatalogoCursorCodec {

    private static final String SEPARADOR = "\u0000";

    public String codificar(String nomeNormalizado, Integer id) {
        String bruto = (nomeNormalizado == null ? "" : nomeNormalizado) + SEPARADOR + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bruto.getBytes(StandardCharsets.UTF_8));
    }

    public Cursor decodificar(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String bruto = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int posicao = bruto.lastIndexOf(SEPARADOR);
            if (posicao < 0) {
                throw new CursorInvalidoException();
            }
            String nomeNormalizado = bruto.substring(0, posicao);
            Integer id = Integer.valueOf(bruto.substring(posicao + SEPARADOR.length()));
            return new Cursor(nomeNormalizado, id);
        } catch (IllegalArgumentException e) {
            throw new CursorInvalidoException();
        }
    }

    public record Cursor(String nomeNormalizado, Integer id) {
    }
}
