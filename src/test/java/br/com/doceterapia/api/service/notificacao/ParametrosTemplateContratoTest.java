package br.com.doceterapia.api.service.notificacao;

import br.com.doceterapia.api.service.notificacao.resumo.GrupoDataResumo;
import br.com.doceterapia.api.service.notificacao.resumo.ItemResumoInsumo;
import br.com.doceterapia.api.service.notificacao.resumo.ItemResumoPedido;
import br.com.doceterapia.api.service.notificacao.resumo.ParametrosResumoFimDia;
import br.com.doceterapia.api.service.notificacao.resumo.ParametrosResumoInicioDia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the JSON each notification sends to the orchestrator templates. The
 * orchestrator test suite renders a copy of the same file, so a renamed or
 * reshaped parameter fails here instead of reaching WhatsApp as an empty section.
 */
@DisplayName("Contrato dos parâmetros de template com o orquestrador")
class ParametrosTemplateContratoTest {

    private static final LocalDate RESUMO = LocalDate.of(2026, 11, 16);
    private static final LocalDate ENTREGA = LocalDate.of(2026, 11, 18);

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    @DisplayName("resumo_inicio_dia envia data e seções com os nomes do template, em texto legível")
    void resumoInicioDia() throws IOException {
        ParametrosResumoInicioDia parametros = new ParametrosResumoInicioDia(
                RESUMO,
                List.of(new ItemResumoPedido(42, "Cliente Exemplo", ENTREGA)),
                List.of(),
                List.of(new ItemResumoPedido(40, "Cliente Teste", RESUMO), new ItemResumoPedido(41, null, RESUMO)),
                List.of());

        assertContrato("resumo_inicio_dia", parametros);
    }

    @Test
    @DisplayName("resumo_fim_dia separa prontos e aguardando sinal e envia insumos em texto legível")
    void resumoFimDia() throws IOException {
        ParametrosResumoFimDia parametros = new ParametrosResumoFimDia(
                RESUMO,
                List.of(new GrupoDataResumo(
                        ENTREGA,
                        List.of(new ItemResumoPedido(43, "Cliente Exemplo", ENTREGA)),
                        List.of(new ItemResumoPedido(44, "Cliente Teste", ENTREGA)))),
                List.of(new ItemResumoInsumo(
                        "Chocolate meio amargo", new BigDecimal("0.800"), new BigDecimal("2.000"), "KG")));

        assertContrato("resumo_fim_dia", parametros);
    }

    @Test
    @DisplayName("estoque_baixo envia insumo, saldo e mínimo")
    void estoqueBaixo() throws IOException {
        ParametrosEstoqueBaixo parametros = new ParametrosEstoqueBaixo(
                "Chocolate meio amargo", new BigDecimal("0.8"), new BigDecimal("2.5"));

        assertContrato("estoque_baixo", parametros);
    }

    @Test
    @DisplayName("dia_lotado envia a data em dd/MM/yyyy e a quantidade em pedidos")
    void diaLotado() throws IOException {
        ParametrosDiaLotado parametros = new ParametrosDiaLotado(LocalDate.of(2026, 11, 20), 6);

        assertContrato("dia_lotado", parametros);
    }

    private void assertContrato(String templateKey, ParametrosTemplate parametros) throws IOException {
        // Parsing the serialized text keeps the comparison on what is stored and sent.
        JsonNode enviado = objectMapper.readTree(objectMapper.writeValueAsString(parametros));
        assertEquals(contrato().get(templateKey), enviado);
    }

    private JsonNode contrato() throws IOException {
        try (InputStream arquivo = getClass().getResourceAsStream("/contrato/notificacao-parametros.json")) {
            return objectMapper.readTree(arquivo);
        }
    }
}
