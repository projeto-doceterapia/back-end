# Doce Terapia — API REST (Back-end)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23C0E12F?style=for-the-badge&logo=swagger&logoColor=black)

API REST completa para gestao da doceria Doce Terapia. Gerencia clientes, produtos,
insumos, pedidos, producao, pagamentos e notificacoes.

## Tecnologias

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0
- **Persistencia:** Spring Data JPA / H2 (dev) / MySQL (prod)
- **Seguranca:** Spring Security + JWT (HMAC-SHA256)
- **Documentacao:** Swagger UI (OpenAPI 3.0)
- **Testes:** JUnit 5 + Mockito + JaCoCo
- **Build:** Maven

## Funcionalidades

| Modulo | Entidades | Endpoints |
|--------|-----------|-----------|
| **Autenticacao** | Usuario | Login, cadastro |
| **Clientes** | Cliente | CRUD completo |
| **Produtos** | CategoriaProduto, Produto, ProdutoInsumo | CRUD completo, estoque por insumo |
| **Insumos** | CategoriaInsumo, Insumo | CRUD completo, controle de quantidade |
| **Pedidos** | Pedido, ItemPedido | CRUD + status machine + itens |
| **Pagamentos** | Pagamento | Auto-criacao, sinal, restante |
| **Cancelamentos** | CancelamentoPedido | Vinculado ao pedido |
| **Producao** | Producao | CRUD + status machine |
| **Notificacoes** | Notificacao, ConfiguracaoFarolAgenda | Agendamento e alertas |

## Requisitos

- JDK 21
- Maven (ou usar o wrapper `mvnw.cmd`)
- MySQL 8+ (para producao)

## Configuracao

### Variaveis de ambiente (obrigatorias)

| Variavel | Descricao |
|----------|-----------|
| `JWT_SECRET` | Chave Base64 de no minimo 32 bytes para assinar tokens JWT |

### Variaveis de ambiente (opcionais)

| Variavel | Default | Descricao |
|----------|---------|-----------|
| `JWT_VALIDITY` | `3600` | Validade do token JWT em segundos |

### Banco de dados

Em desenvolvimento (default): H2 em memoria, schema criado automaticamente.

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Para MySQL, sobrescreva em `application.properties` ou via variaveis:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_doce_terapia
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```

## Execucao

```bash
# Usando o wrapper (recomendado)
./mvnw.cmd spring-boot:run

# Ou com Maven instalado
mvn spring-boot:run
```

Acesse a API em `http://localhost:8080`.

## Documentacao Swagger

Com a aplicacao rodando:

> http://localhost:8080/swagger-ui/index.html

## Testes

```bash
./mvnw.cmd test
```

98 testes unitarios nos services (JUnit 5 + Mockito).

## Estrutura

```
src/main/java/br/com/doceterapia/
├── config/        # Seguranca, JWT, CORS, Swagger
├── controller/    # Endpoints REST
├── dto/           # Request/Response DTOs
├── entity/        # Entidades JPA (14 tabelas)
├── enums/         # Enums do dominio (13 tipos)
├── exception/     # Excecoes customizadas
├── mapper/        # Conversao Entity <-> DTO
├── repository/    # Spring Data JPA
├── service/       # Regras de negocio
└── swagger/       # Interfaces OpenAPI
```