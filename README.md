🍯 Doce Terapia – API REST (Back-end)
Este repositório contém o núcleo lógico e a camada de persistência da Doce Terapia. A API foi desenvolvida para gerenciar de forma centralizada o cadastro de clientes, o fluxo de pedidos e a inteligência de estoque da doceria.

🚀 Tecnologias e Ferramentas
Linguagem: Java 21

Framework: Spring Boot 3+

Gerenciador de Dependências: Maven

Banco de Dados: MySQL (Persistência) / H2 (Testes)

Documentação: Swagger (OpenAPI 3)

Arquitetura: REST

🛠️ Funcionalidades Principais
O backend expõe endpoints para as seguintes operações:

Gestão de Clientes (CRUD): Cadastro, consulta, atualização e exclusão de clientes da doceria.

Controle de Pedidos: Registro de vendas, vinculação de itens e cálculo de status.

Regras de Negócio: Validação de disponibilidade de estoque ao processar novos pedidos.

Documentação Automática: Interface interativa para testes de endpoints.

📖 Documentação da API (Swagger)
A API utiliza o Swagger UI para facilitar o consumo pelo time de frontend e para testes manuais. Com a aplicação rodando, você pode acessar a documentação detalhada, esquemas de dados e testar as requisições em:

http://localhost:8080/swagger-ui/index.html

⚙️ Configuração e Instalação
Pré-requisitos
JDK 21

Maven 3.8+

Instância MySQL ativa

Instalação
Clone o repositório:

Bash
git clone https://github.com/seu-usuario/doce-terapia.git
cd doce-terapia/back-end
Configuração do Banco de Dados:
Edite o arquivo src/main/resources/application.properties com as credenciais do seu ambiente local:

Properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_doce_terapia
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
Execução:

Bash
mvn clean install
mvn spring-boot:run
📂 Estrutura de Pacotes
controller/: Camada de exposição dos endpoints REST.

service/: Camada de regras de negócio e validações.

repository/: Interfaces de comunicação com o banco de dados (Spring Data JPA / JdbcTemplate).

model/: Entidades que representam as tabelas do banco de dados (Cliente, Pedido, Ingrediente).

dto/: Objetos de transferência de dados para segurança e performance.

config/: Configurações de CORS, Swagger e Segurança.
