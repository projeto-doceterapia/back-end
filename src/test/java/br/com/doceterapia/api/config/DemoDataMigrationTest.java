package br.com.doceterapia.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.doceterapia.api.dto.ClienteRequestDTO;
import br.com.doceterapia.api.entity.Cliente;
import br.com.doceterapia.api.enums.ClassificacaoCliente;
import br.com.doceterapia.api.enums.OrigemMovimentacao;
import br.com.doceterapia.api.enums.StatusAtivo;
import br.com.doceterapia.api.enums.StatusPedido;
import br.com.doceterapia.api.enums.TipoPessoa;
import br.com.doceterapia.api.enums.UnidadeMedida;
import br.com.doceterapia.api.repository.ConfiguracaoFarolAgendaRepository;
import br.com.doceterapia.api.service.ClienteService;
import br.com.doceterapia.api.service.MovimentacaoEstoqueService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Boots against a real MySQL with the demo dataset, so a schema or entity change that breaks the
 * dataset fails the build. Skipped when SCHEMA_TEST_DB_URL is absent. The database is wiped.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=${SCHEMA_TEST_DB_URL}",
    "spring.datasource.username=${SCHEMA_TEST_DB_USERNAME}",
    "spring.datasource.password=${SCHEMA_TEST_DB_PASSWORD}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.clean-disabled=false",
    "doceterapia.demo-data=true"
})
@EnabledIfEnvironmentVariable(named = "SCHEMA_TEST_DB_URL", matches = ".+")
class DemoDataMigrationTest {

    @TestConfiguration
    static class BancoLimpoConfig {

        @Bean
        FlywayMigrationStrategy limparEMigrar() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Autowired
    private List<JpaRepository<?, ?>> repositorios;

    @Autowired
    private ConfiguracaoFarolAgendaRepository farolRepository;

    @Test
    void migracaoDoDemoFoiAplicadaDepoisDoSchema() {
        // Arrange and Act
        List<String> versoes = jdbc.queryForList(
            "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank", String.class);

        // Assert
        assertThat(versoes).containsExactly("1", "1.1");
    }

    @Test
    void totalIgualSomaDosItens() {
        // Arrange and Act
        Integer divergentes = contar("""
            SELECT COUNT(*) FROM pagamento pg
            WHERE pg.valor_total <> (SELECT SUM(i.valor_total) FROM item_pedido i WHERE i.fk_pedido = pg.fk_pedido)
            """);

        // Assert
        assertThat(contar("SELECT COUNT(*) FROM pagamento")).isPositive();
        assertThat(divergentes).isZero();
    }

    @Test
    void sinalERestanteFecham() {
        // Arrange and Act
        Integer divergentes = contar("""
            SELECT COUNT(*) FROM pagamento
            WHERE valor_sinal <> ROUND(valor_total * 0.5, 2) OR valor_sinal + valor_restante <> valor_total
            """);
        Integer impares = contar("""
            SELECT COUNT(*) FROM pagamento
            WHERE valor_total = 133.15 AND valor_sinal = 66.58 AND valor_restante = 66.57
            """);

        // Assert
        assertThat(divergentes).isZero();
        assertThat(impares).isEqualTo(1);
    }

    @Test
    void todosOsStatusPresentes() {
        // Arrange and Act
        List<String> presentes = jdbc.queryForList("SELECT DISTINCT status_pedido FROM pedido", String.class);

        // Assert
        assertThat(presentes).containsExactlyInAnyOrderElementsOf(
            Arrays.stream(StatusPedido.values()).map(Enum::name).toList());
    }

    @Test
    void pagamentoEProducaoSeguemOStatus() {
        // Arrange and Act
        Integer orcamentoComPagamento = contar("""
            SELECT COUNT(*) FROM pedido p JOIN pagamento pg ON pg.fk_pedido = p.id_pedido
            WHERE p.status_pedido = 'ORCAMENTO'
            """);
        Integer confirmadoSemPagamento = contar("""
            SELECT COUNT(*) FROM pedido p LEFT JOIN pagamento pg ON pg.fk_pedido = p.id_pedido
            WHERE p.tipo_pedido = 'PEDIDO_CONFIRMADO' AND pg.id_pagamento IS NULL
            """);
        Integer sinalPendenteComDados = contar("""
            SELECT COUNT(*) FROM pedido p JOIN pagamento pg ON pg.fk_pedido = p.id_pedido
            WHERE p.status_pedido = 'AGUARDANDO_SINAL'
              AND (pg.status_pagamento <> 'AGUARDANDO_SINAL' OR pg.data_pagamento_sinal IS NOT NULL
                   OR pg.forma_pagamento_sinal IS NOT NULL)
            """);
        Integer producaoDivergente = contar("""
            SELECT COUNT(*) FROM pedido p JOIN producao pr ON pr.fk_pedido = p.id_pedido
            WHERE (p.status_pedido = 'ESPERANDO_FAZER' AND pr.status_producao <> 'ESPERANDO_FAZER')
               OR (p.status_pedido = 'EM_PRODUCAO' AND pr.status_producao <> 'EM_PRODUCAO')
               OR (p.status_pedido = 'AGUARDANDO_ENTREGA' AND pr.status_producao <> 'PRONTO_PARA_ENTREGA')
               OR (p.status_pedido = 'ENTREGUE' AND pr.status_producao <> 'FINALIZADO')
            """);

        // Assert
        assertThat(orcamentoComPagamento).isZero();
        assertThat(confirmadoSemPagamento).isZero();
        assertThat(sinalPendenteComDados).isZero();
        assertThat(producaoDivergente).isZero();
    }

    @Test
    void telefonesNaFaixaFicticia() {
        // Arrange and Act
        List<String> telefones = jdbc.queryForList("SELECT telefone FROM cliente", String.class);

        // Assert
        assertThat(telefones).isNotEmpty().allMatch(t -> t.matches("^\\+55 11 90000-\\d{4}$"));
    }

    @Test
    void textoComAcentoChegaIntacto() {
        // Arrange and Act
        List<String> enderecos = jdbc.queryForList("SELECT endereco FROM cliente", String.class);

        // Assert
        assertThat(enderecos).allMatch(e -> e.endsWith("São Paulo"));
    }

    @Test
    void semUsuarios() {
        // Arrange, Act and Assert
        assertThat(contar("SELECT COUNT(*) FROM usuario")).isZero();
    }

    @Test
    void saldoIgualMovimentacoes() {
        // Arrange and Act
        Integer divergentes = contar("""
            SELECT COUNT(*) FROM insumo i
            WHERE i.quantidade_atual <>
                (SELECT SUM(m.quantidade) FROM movimentacao_estoque m WHERE m.fk_insumo = i.id_insumo)
            """);
        Integer comprasSemCusto = contar(
            "SELECT COUNT(*) FROM movimentacao_estoque WHERE tipo = 'ENTRADA_COMPRA' AND custo_unitario IS NULL");

        // Assert
        assertThat(divergentes).isZero();
        assertThat(comprasSemCusto).isZero();
    }

    @Test
    void insumosAbaixoDoMinimo() {
        // Arrange, Act and Assert
        assertThat(contar("SELECT COUNT(*) FROM insumo WHERE quantidade_atual < quantidade_minima"))
            .isGreaterThanOrEqualTo(3);
    }

    @Test
    void cancelamentosSemCreditoEDentroDoPago() {
        // Arrange and Act
        Integer fora = contar("""
            SELECT COUNT(*) FROM cancelamento_pedido c LEFT JOIN pagamento pg ON pg.fk_pedido = c.fk_pedido
            WHERE c.valor_retorno < 0
               OR c.valor_retorno > IF(pg.data_pagamento_sinal IS NULL, 0, pg.valor_sinal)
            """);
        List<String> retornos = jdbc.queryForList(
            "SELECT DISTINCT tipo_retorno FROM cancelamento_pedido", String.class);

        // Assert
        assertThat(fora).isZero();
        assertThat(retornos).containsExactlyInAnyOrder("DEVOLUCAO", "SEM_DEVOLUCAO");
    }

    @Test
    void existeDiaLotadoNaAgenda() {
        // Arrange
        Integer limiteVermelho = farolRepository.findAll().get(0).getLimiteVermelho();

        // Act
        Integer maior = contar("""
            SELECT COALESCE(MAX(t.quantidade), 0) FROM (
                SELECT COUNT(*) AS quantidade FROM pedido
                WHERE status_pedido <> 'CANCELADO'
                  AND data_entrega BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7' DAY
                GROUP BY data_entrega
            ) t
            """);

        // Assert
        assertThat(limiteVermelho).isEqualTo(7);
        assertThat(maior).isGreaterThanOrEqualTo(limiteVermelho);
    }

    @Test
    void configuracoesDeLinhaUnicaAtualizadas() {
        // Arrange and Act
        Map<String, Object> precificacao = jdbc.queryForMap(
            "SELECT margem_padrao, percentual_indiretos FROM configuracao_precificacao");

        // Assert
        assertThat(contar("SELECT COUNT(*) FROM configuracao_precificacao")).isEqualTo(1);
        assertThat(contar("SELECT COUNT(*) FROM configuracao_notificacao")).isEqualTo(1);
        assertThat((BigDecimal) precificacao.get("margem_padrao")).isEqualByComparingTo("60.00");
        assertThat((BigDecimal) precificacao.get("percentual_indiretos")).isEqualByComparingTo("10.00");
    }

    @Test
    void notificacoesNuncaSaoEnviadas() {
        // Arrange, Act and Assert
        assertThat(contar("SELECT COUNT(*) FROM notificacao WHERE chave_unicidade LIKE 'demo-%' AND status_envio = 'NAO_ENVIAR'"))
            .isEqualTo(6);
    }

    @Test
    @Transactional
    void cadastroAposDemoNaoColide() {
        // Arrange
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome("Cliente Exemplo Novo");
        request.setTelefone("+55 11 90000-9999");
        request.setEndereco("Rua Exemplo, 1 - Centro, São Paulo");
        request.setTipoPessoa(TipoPessoa.FISICA);
        request.setClassificacaoCliente(ClassificacaoCliente.PADRAO);
        request.setStatus(StatusAtivo.ATIVO);

        // Act
        Cliente cliente = clienteService.cadastrar(request);

        // Assert
        assertThat(cliente.getIdCliente()).isGreaterThan(25);
    }

    @Test
    void reenvioDeUmaCompraDoDemoEhReconhecidoComoDuplicado() {
        // Arrange: the same content and key must match the hash the service computes.
        Map<String, Object> compra = jdbc.queryForMap("""
            SELECT fk_insumo, quantidade, quantidade_informada, unidade_informada, custo_unitario, origem,
                   fluxo_id, chave_idempotencia
            FROM movimentacao_estoque
            WHERE tipo = 'ENTRADA_COMPRA' AND unidade_informada IN ('G', 'ML')
            ORDER BY id_movimentacao LIMIT 1
            """);
        BigDecimal valorLiquido = ((BigDecimal) compra.get("quantidade"))
            .multiply((BigDecimal) compra.get("custo_unitario"));

        // Act
        MovimentacaoEstoqueService.ResultadoMovimentacao resultado = movimentacaoEstoqueService.registrarEntradaCompra(
            new MovimentacaoEstoqueService.EntradaCompra(
                (Integer) compra.get("fk_insumo"),
                (BigDecimal) compra.get("quantidade_informada"),
                UnidadeMedida.valueOf((String) compra.get("unidade_informada")),
                valorLiquido,
                OrigemMovimentacao.valueOf((String) compra.get("origem")),
                null,
                (String) compra.get("fluxo_id"),
                (String) compra.get("chave_idempotencia"),
                null));

        // Assert
        assertThat(resultado.duplicado()).isTrue();
    }

    @Test
    void entidadesLeemTodasAsLinhas() {
        // Arrange and Act: an enum value the entity does not know throws while loading the row.
        repositorios.forEach(JpaRepository::findAll);

        // Assert
        assertThat(repositorios).isNotEmpty();
    }

    private Integer contar(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}
