package br.com.doceterapia.api.repository.projecao;

import java.time.LocalDate;

public record ContagemPorDia(LocalDate data, long quantidade) {
}
