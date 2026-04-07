package school.sptech.projetoindividual.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Doceterapia")
                        .version("1.0.0")
                        .description("API REST para gerenciamento de clientes e pedidos de um serviço de confeitaria")
                        .contact(new Contact()
                                .name("Doceterapia")
                                .email("contato@doceterapia.com")
                                .url("https://doceterapia.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
