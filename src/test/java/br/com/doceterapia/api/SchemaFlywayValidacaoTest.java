package br.com.doceterapia.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Boots the application against a real MySQL, with Flyway applying V1 and Hibernate validating
 * every entity against it. The context only starts if both succeed. Skipped when
 * SCHEMA_TEST_DB_URL is absent. The target database is wiped, so it must be disposable.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=${SCHEMA_TEST_DB_URL}",
    "spring.datasource.username=${SCHEMA_TEST_DB_USERNAME}",
    "spring.datasource.password=${SCHEMA_TEST_DB_PASSWORD}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.clean-disabled=false"
})
@EnabledIfEnvironmentVariable(named = "SCHEMA_TEST_DB_URL", matches = ".+")
class SchemaFlywayValidacaoTest {

    private static final String CONSULTA_COLUNA = """
        SELECT COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
        """;

    private static final String CONSULTA_INDICE = """
        SELECT COLUMN_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?
        ORDER BY SEQ_IN_INDEX
        """;

    @TestConfiguration
    static class BancoLimpoConfig {

        // Reruns against the same container must still prove a migration from an empty schema.
        @Bean
        FlywayMigrationStrategy limparEMigrar() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextoSobeComSchemaValidado() {
        // Arrange and Act: the context start already ran the migration and the validation.
        Integer aplicadas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = 1",
            Integer.class);

        // Assert
        assertThat(aplicadas).isEqualTo(1);
    }

    @Test
    void colunasDaSprintExistemComPadrao() {
        // Arrange and Act: V1 was applied at context start; each helper call reads information_schema.
        // Assert
        assertColuna("usuario", "perfil", "varchar(20)", "NO", "DONA");
        assertColuna("pagamento", "forma_pagamento_sinal", "varchar(30)", "YES", null);
        assertColuna("pagamento", "forma_pagamento_restante", "varchar(30)", "YES", null);
        assertColuna("pedido", "confirmado_com_falta_insumo", "bit(1)", "NO", "b'0'");
        assertColuna("pedido", "cep", "varchar(8)", "YES", null);
        assertColuna("cliente", "cep", "varchar(8)", "YES", null);
        assertColuna("movimentacao_estoque", "tipo", "varchar(20)", "NO", null);
        assertColuna("movimentacao_estoque", "origem", "varchar(20)", "NO", null);
        assertColuna("configuracao_notificacao", "avisos_automaticos_ativo", "bit(1)", "NO", "b'1'");
        assertColuna("configuracao_notificacao", "assistente_ativo", "bit(1)", "NO", "b'1'");
        assertColuna("configuracao_precificacao", "margem_padrao", "decimal(5,2)", "NO", "0.00");
        assertColuna("configuracao_precificacao", "percentual_indiretos", "decimal(5,2)", "NO", "0.00");
    }

    @Test
    void configuracaoPrecificacaoTemLinhaUnica() {
        // Arrange and Act
        List<Map<String, Object>> linhas = jdbcTemplate.queryForList(
            "SELECT margem_padrao, percentual_indiretos FROM configuracao_precificacao");

        // Assert
        assertThat(linhas).hasSize(1);
        assertThat((BigDecimal) linhas.get(0).get("margem_padrao")).isEqualByComparingTo("0.00");
        assertThat((BigDecimal) linhas.get(0).get("percentual_indiretos")).isEqualByComparingTo("0.00");
    }

    @Test
    void configuracaoNotificacaoTemLinhaUnicaComInterruptoresLigados() {
        // Arrange and Act
        Integer linhas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM configuracao_notificacao", Integer.class);
        Integer ligadas = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM configuracao_notificacao "
                + "WHERE avisos_automaticos_ativo = 1 AND assistente_ativo = 1",
            Integer.class);

        // Assert
        assertThat(linhas).isEqualTo(1);
        assertThat(ligadas).isEqualTo(1);
    }

    @Test
    void indicesCandidatosExistem() {
        // Arrange and Act
        List<String> indicePedido = colunasDoIndice("pedido", "idx_pedido_status_entrega");
        List<String> indiceMovimentacao = colunasDoIndice("movimentacao_estoque", "idx_movimentacao_ocorrido_em");

        // Assert
        assertThat(indicePedido).containsExactly("status_pedido", "data_entrega");
        assertThat(indiceMovimentacao).containsExactly("ocorrido_em");
    }

    @Test
    void emailDoUsuarioEhUnico() {
        // Arrange and Act
        Integer naoUnicos = jdbcTemplate.queryForObject(
            "SELECT NON_UNIQUE FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'uk_usuario_email'",
            Integer.class);

        // Assert
        assertThat(naoUnicos).isZero();
    }

    private void assertColuna(String tabela, String coluna, String tipo, String nulo, String padrao) {
        Map<String, Object> linha = jdbcTemplate.queryForMap(CONSULTA_COLUNA, tabela, coluna);
        String contexto = tabela + "." + coluna;
        assertThat(linha.get("COLUMN_TYPE")).as(contexto).isEqualTo(tipo);
        assertThat(linha.get("IS_NULLABLE")).as(contexto).isEqualTo(nulo);
        assertThat(linha.get("COLUMN_DEFAULT")).as(contexto).isEqualTo(padrao);
    }

    private List<String> colunasDoIndice(String tabela, String indice) {
        return jdbcTemplate.queryForList(CONSULTA_INDICE, String.class, tabela, indice);
    }
}
