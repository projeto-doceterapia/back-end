package br.com.doceterapia.api.service;

import br.com.doceterapia.api.exception.FiltroInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record PeriodoConsulta(LocalDate de, LocalDate ate) {

    public static final int MAXIMO_DIAS = 366;

    public static PeriodoConsulta de(LocalDate de, LocalDate ate) {
        if (de == null || ate == null) {
            throw new FiltroInvalidoException("Informe as datas 'de' e 'ate' no formato yyyy-MM-dd.");
        }
        if (de.isAfter(ate)) {
            throw new FiltroInvalidoException("A data 'de' deve ser anterior ou igual à data 'ate'.");
        }
        if (ChronoUnit.DAYS.between(de, ate) > MAXIMO_DIAS) {
            throw new FiltroInvalidoException("O período máximo por consulta é de 366 dias.");
        }
        return new PeriodoConsulta(de, ate);
    }

    public LocalDateTime inicio() {
        return de.atStartOfDay();
    }

    // The last day must be covered whole, so the upper bound is the next midnight.
    public LocalDateTime fimExclusivo() {
        return ate.plusDays(1).atStartOfDay();
    }
}
