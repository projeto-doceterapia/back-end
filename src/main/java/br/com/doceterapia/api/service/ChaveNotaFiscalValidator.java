package br.com.doceterapia.api.service;

import org.springframework.stereotype.Component;

/**
 * Validates a 44-digit NF-e/NFC-e access key: all digits plus the modulo-11
 * check digit in the 44th position.
 */
@Component
public class ChaveNotaFiscalValidator {

    private static final int TAMANHO = 44;

    public boolean isValida(String chave) {
        if (chave == null || chave.length() != TAMANHO || !chave.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return digitoVerificador(chave) == Character.getNumericValue(chave.charAt(TAMANHO - 1));
    }

    private int digitoVerificador(String chave) {
        int peso = 2;
        int soma = 0;
        for (int i = TAMANHO - 2; i >= 0; i--) {
            soma += Character.getNumericValue(chave.charAt(i)) * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        int digito = 11 - resto;
        return (digito == 10 || digito == 11) ? 0 : digito;
    }
}
