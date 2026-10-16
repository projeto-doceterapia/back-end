package br.com.doceterapia.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes para ChaveNotaFiscalValidator")
class ChaveNotaFiscalValidatorTest {

    private final ChaveNotaFiscalValidator validator = new ChaveNotaFiscalValidator();

    private static final String CHAVE_VALIDA = "35200114200166000187650010000000181000000012";

    @Test
    @DisplayName("Aceita uma chave de 44 dígitos com dígito verificador correto")
    void chaveValida() {
        assertTrue(validator.isValida(CHAVE_VALIDA));
    }

    @Test
    @DisplayName("Rejeita uma chave com dígito verificador incorreto")
    void digitoVerificadorIncorreto() {
        String invalida = CHAVE_VALIDA.substring(0, 43) + "9";
        assertFalse(validator.isValida(invalida));
    }

    @Test
    @DisplayName("Rejeita uma chave com menos de 44 dígitos")
    void tamanhoIncorreto() {
        assertFalse(validator.isValida("123"));
    }

    @Test
    @DisplayName("Rejeita uma chave com caracteres não numéricos")
    void comLetras() {
        String comLetra = "A" + CHAVE_VALIDA.substring(1);
        assertFalse(validator.isValida(comLetra));
    }

    @Test
    @DisplayName("Rejeita chave nula")
    void chaveNula() {
        assertFalse(validator.isValida(null));
    }
}
