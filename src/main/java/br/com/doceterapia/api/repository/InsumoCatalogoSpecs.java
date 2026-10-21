package br.com.doceterapia.api.repository;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.service.CatalogoCursorCodec;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA specifications for the supply catalog: active-only supplies,
 * per-word match on the normalized name, category filter, and the keyset
 * predicate on {@code (nomeNormalizado, id)} that drives stable cursor pages.
 */
public final class InsumoCatalogoSpecs {

    private static final char CARACTERE_ESCAPE = '\\';

    private InsumoCatalogoSpecs() {
    }

    public static Specification<Insumo> ativos() {
        return (root, query, cb) -> cb.equal(root.get("status"), StatusAtivo.ATIVO);
    }

    public static Specification<Insumo> nomeContemPalavras(List<String> palavras) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            for (String palavra : palavras) {
                String padrao = "%" + escaparCuringas(palavra) + "%";
                predicados.add(cb.like(root.get("nomeNormalizado"), padrao, CARACTERE_ESCAPE));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }

    /**
     * Escapes the LIKE wildcards so user text matches literally: the escape
     * character itself first, then {@code %} and {@code _}. Paired with the
     * explicit ESCAPE clause on the LIKE predicate.
     */
    private static String escaparCuringas(String termo) {
        return termo
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static Specification<Insumo> daCategoria(Integer categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("idCategoriaInsumo"), categoriaId);
    }

    public static Specification<Insumo> depoisDoCursor(CatalogoCursorCodec.Cursor cursor) {
        return (root, query, cb) -> cb.or(
                cb.greaterThan(root.get("nomeNormalizado"), cursor.nomeNormalizado()),
                cb.and(
                        cb.equal(root.get("nomeNormalizado"), cursor.nomeNormalizado()),
                        cb.greaterThan(root.get("idInsumo"), cursor.id())
                )
        );
    }
}
