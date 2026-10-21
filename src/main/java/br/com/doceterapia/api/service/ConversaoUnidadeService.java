package br.com.doceterapia.api.service;

import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.exception.UnidadeIncompativelException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Deterministic unit conversion for stock movements.
 * Converts a quantity from an informed unit into the supply unit:
 * KG/G and L/ML at a factor of 1000, and UNIDADE only to itself.
 */
@Service
public class ConversaoUnidadeService {

    private static final BigDecimal FATOR_MILHAR = BigDecimal.valueOf(1000);

    public BigDecimal converter(BigDecimal quantidadeInformada, UnidadeMedida unidadeInformada, UnidadeMedida unidadeInsumo) {
        if (!saoCompativeis(unidadeInformada, unidadeInsumo)) {
            throw new UnidadeIncompativelException(unidadeInformada, unidadeInsumo);
        }

        if (unidadeInformada == unidadeInsumo) {
            return quantidadeInformada;
        }

        return aplicarFator(quantidadeInformada, unidadeInformada, unidadeInsumo);
    }

    public boolean saoCompativeis(UnidadeMedida origem, UnidadeMedida destino) {
        return grandeza(origem) == grandeza(destino);
    }

    private BigDecimal aplicarFator(BigDecimal quantidade, UnidadeMedida origem, UnidadeMedida destino) {
        boolean origemMenor = origem == UnidadeMedida.G || origem == UnidadeMedida.ML;
        return origemMenor
                ? quantidade.divide(FATOR_MILHAR)
                : quantidade.multiply(FATOR_MILHAR);
    }

    private Grandeza grandeza(UnidadeMedida unidade) {
        return switch (unidade) {
            case KG, G -> Grandeza.MASSA;
            case L, ML -> Grandeza.VOLUME;
            case UNIDADE -> Grandeza.CONTAGEM;
        };
    }

    private enum Grandeza {
        MASSA,
        VOLUME,
        CONTAGEM
    }
}
