package br.com.doceterapia.api.service;

import br.com.doceterapia.api.exception.FiltroInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PeriodoConsulta")
class PeriodoConsultaTest {

    @Test
    @DisplayName("Aceita 366 dias e recusa 367")
    void limiteDe366Dias() {
        PeriodoConsulta aceito = PeriodoConsulta.de(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 2));
        assertEquals(LocalDate.of(2027, 1, 2), aceito.ate());

        FiltroInvalidoException erro = assertThrows(FiltroInvalidoException.class,
                () -> PeriodoConsulta.de(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 3)));
        assertEquals("O período máximo por consulta é de 366 dias.", erro.getMessage());
    }

    @Test
    @DisplayName("Recusa datas ausentes ou invertidas")
    void recusaAusentesEInvertidas() {
        assertThrows(FiltroInvalidoException.class, () -> PeriodoConsulta.de(null, LocalDate.of(2026, 1, 1)));
        assertThrows(FiltroInvalidoException.class, () -> PeriodoConsulta.de(LocalDate.of(2026, 1, 1), null));
        assertThrows(FiltroInvalidoException.class,
                () -> PeriodoConsulta.de(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1)));
    }

    @Test
    @DisplayName("Aceita um único dia e cobre o dia inteiro com fim exclusivo")
    void umDiaCobreDiaInteiro() {
        PeriodoConsulta dia = PeriodoConsulta.de(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3));

        assertEquals(LocalDateTime.of(2026, 11, 3, 0, 0), dia.inicio());
        assertEquals(LocalDateTime.of(2026, 11, 4, 0, 0), dia.fimExclusivo());
    }
}
