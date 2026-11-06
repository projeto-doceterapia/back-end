package br.com.doceterapia.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.doceterapia.api.ApiDoceterapiaApplication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

class DemoDataFlywayConfigTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
        .withUserConfiguration(DemoDataFlywayConfig.class);

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

    @Test
    void semAPropriedadeNaoRegistraOCustomizador() {
        // Arrange, Act and Assert
        contexto.run(ctx -> assertThat(ctx).doesNotHaveBean(FlywayConfigurationCustomizer.class));
    }

    @Test
    void comFalseNaoRegistraOCustomizador() {
        // Arrange, Act and Assert
        contexto.withPropertyValues("doceterapia.demo-data=false")
            .run(ctx -> assertThat(ctx).doesNotHaveBean(FlywayConfigurationCustomizer.class));
    }

    @Test
    void comTrueAcrescentaALocationSemTirarAsMigracoes() {
        // Arrange
        FluentConfiguration configuracao = Flyway.configure().locations("classpath:db/migration");

        // Act
        contexto.withPropertyValues("doceterapia.demo-data=true")
            .run(ctx -> ctx.getBean(FlywayConfigurationCustomizer.class).customize(configuracao));

        // Assert
        assertThat(Arrays.stream(configuracao.getLocations()).map(Location::getDescriptor))
            .containsExactlyInAnyOrder("classpath:db/migration", "classpath:db/demo");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SCHEMA_TEST_DB_URL", matches = ".+")
    void semDemoDataNaoCarrega() {
        // Arrange and Act
        try (ConfigurableApplicationContext ctx = subir("--doceterapia.demo-data=false")) {
            JdbcTemplate jdbc = ctx.getBean(JdbcTemplate.class);

            // Assert
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class)).isZero();
            assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1.1'", Integer.class)).isZero();
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SCHEMA_TEST_DB_URL", matches = ".+")
    void demoInvalidoFalhaNaSubida() {
        // Arrange, Act and Assert
        assertThatThrownBy(() -> subir(
            "--doceterapia.demo-data=true",
            "--doceterapia.demo-data-location=classpath:db/demo-quebrado"))
            .satisfies(erro -> assertThat(causas(erro)).anyMatch(FlywayException.class::isInstance));
    }

    private static List<Throwable> causas(Throwable erro) {
        List<Throwable> cadeia = new ArrayList<>();
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            cadeia.add(atual);
        }
        return cadeia;
    }

    private static ConfigurableApplicationContext subir(String... extras) {
        List<String> argumentos = new ArrayList<>(List.of(
            "--server.port=0",
            "--spring.datasource.url=" + System.getenv("SCHEMA_TEST_DB_URL"),
            "--spring.datasource.username=" + System.getenv("SCHEMA_TEST_DB_USERNAME"),
            "--spring.datasource.password=" + System.getenv("SCHEMA_TEST_DB_PASSWORD"),
            "--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
            "--spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
            "--spring.jpa.hibernate.ddl-auto=validate",
            "--spring.flyway.enabled=true",
            "--spring.flyway.clean-disabled=false"));
        argumentos.addAll(List.of(extras));
        return new SpringApplicationBuilder(ApiDoceterapiaApplication.class, BancoLimpoConfig.class)
            .run(argumentos.toArray(String[]::new));
    }
}
