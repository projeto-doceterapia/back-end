package br.com.doceterapia.api.service;

import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.exception.StatusTransitionInvalidException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaquinaStatusPedidoTest {
    private final MaquinaStatusPedido maquina = new MaquinaStatusPedido();

    @ParameterizedTest
    @CsvSource({"ORCAMENTO,AGUARDANDO_SINAL", "ORCAMENTO,CANCELADO", "AGUARDANDO_SINAL,ESPERANDO_FAZER",
            "AGUARDANDO_SINAL,CANCELADO", "ESPERANDO_FAZER,EM_PRODUCAO", "ESPERANDO_FAZER,CANCELADO",
            "EM_PRODUCAO,AGUARDANDO_ENTREGA", "EM_PRODUCAO,CANCELADO", "AGUARDANDO_ENTREGA,ENTREGUE",
            "AGUARDANDO_ENTREGA,CANCELADO"})
    void deveAceitarTransicoesValidas(StatusPedido atual, StatusPedido novo) {
        assertDoesNotThrow(() -> maquina.validar(atual, novo));
    }

    @ParameterizedTest
    @CsvSource({"ORCAMENTO,ENTREGUE", "AGUARDANDO_SINAL,EM_PRODUCAO", "ESPERANDO_FAZER,ENTREGUE",
            "EM_PRODUCAO,ORCAMENTO", "AGUARDANDO_ENTREGA,EM_PRODUCAO", "ENTREGUE,CANCELADO",
            "CANCELADO,ORCAMENTO"})
    void deveRecusarTransicoesInvalidas(StatusPedido atual, StatusPedido novo) {
        assertThrows(StatusTransitionInvalidException.class, () -> maquina.validar(atual, novo));
    }
}
