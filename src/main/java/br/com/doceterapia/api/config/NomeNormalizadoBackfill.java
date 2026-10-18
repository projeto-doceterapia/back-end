package br.com.doceterapia.api.config;

import br.com.doceterapia.api.entity.Insumo;
import br.com.doceterapia.api.repository.InsumoRepository;
import br.com.doceterapia.api.service.NomeNormalizador;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backfills {@code insumo.nome_normalizado} for rows loaded before the catalog
 * logic existed (seed data). Without this, such supplies would be invisible to
 * the accent-insensitive catalog search, which filters on the normalized column.
 * Runs once at startup and is a no-op when every row is already normalized.
 */
@Component
public class NomeNormalizadoBackfill implements ApplicationRunner {

    private final InsumoRepository repository;
    private final NomeNormalizador normalizador;

    public NomeNormalizadoBackfill(InsumoRepository repository, NomeNormalizador normalizador) {
        this.repository = repository;
        this.normalizador = normalizador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Insumo> semNormalizado = repository.findByNomeNormalizadoIsNull();
        for (Insumo insumo : semNormalizado) {
            insumo.setNomeNormalizado(normalizador.normalizar(insumo.getNome()));
        }
        if (!semNormalizado.isEmpty()) {
            repository.saveAll(semNormalizado);
        }
    }
}
