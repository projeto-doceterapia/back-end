package br.com.doceterapia.api.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Normalizes supply names for duplicate matching and accent-insensitive search:
 * trim, lowercase, strip accents and collapse internal whitespace.
 * The result is stored in {@code insumo.nome_normalizado} and used verbatim by
 * the catalog queries, so write-side and read-side normalization stay identical.
 */
@Component
public class NomeNormalizador {

    public String normalizar(String nome) {
        if (nome == null) {
            return null;
        }
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return Arrays.stream(semAcento.trim().toLowerCase().split("\\s+"))
                .filter(palavra -> !palavra.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
