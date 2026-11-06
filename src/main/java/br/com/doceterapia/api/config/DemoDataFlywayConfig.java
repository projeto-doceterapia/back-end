package br.com.doceterapia.api.config;

import org.flywaydb.core.api.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the fictitious demo dataset to the Flyway locations. Its version runs right after the
 * baseline schema, and a script that no longer fits the schema fails the migration, so the
 * application does not start with a broken dataset.
 */
@Configuration
@ConditionalOnProperty(name = "doceterapia.demo-data", havingValue = "true")
public class DemoDataFlywayConfig {

    private final String demoLocation;

    public DemoDataFlywayConfig(@Value("${doceterapia.demo-data-location:classpath:db/demo}") String demoLocation) {
        this.demoLocation = demoLocation;
    }

    @Bean
    public FlywayConfigurationCustomizer demoDataLocation() {
        return configuration -> {
            List<String> locations = new ArrayList<>();
            for (Location location : configuration.getLocations()) {
                locations.add(location.getDescriptor());
            }
            locations.add(demoLocation);
            configuration.locations(locations.toArray(String[]::new));
        };
    }
}
