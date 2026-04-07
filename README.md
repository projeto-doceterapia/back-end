# 🍬 Doce Terapia - API REST (Back-end)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23C0E12F?style=for-the-badge&logo=swagger&logoColor=black)

Esta é a camada de inteligência e persistência do sistema **Doce Terapia**. A API foi desenvolvida seguindo o padrão arquitetural REST para gerenciar o ecossistema da doceria, focando em performance e organização de dados.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3+
* **Persistência de Dados:** Spring Data JPA / MySQL
* **Documentação:** Swagger UI (OpenAPI 3.0)
* **Gerenciador de Dependências:** Maven

## 📋 Funcionalidades do Backend

A API centraliza as operações principais do negócio, divididas em dois pilares principais:

### 1. Gestão de Clientes
* **CRUD Completo:** Cadastro, listagem, atualização e exclusão de clientes.
* **Validação de Dados:** Garantia de integridade para campos como e-mail e telefone.

### 2. Gestão de Pedidos
* **Registro de Vendas:** Criação de novos pedidos vinculados a clientes.
* **Fluxo de Status:** Controle do ciclo de vida do pedido (Pendente, Em Produção, Finalizado).
* **Histórico:** Consulta de movimentações passadas para relatórios.

---

## 📖 Documentação Interativa (Swagger)

A API conta com documentação automatizada, permitindo testar todos os endpoints em tempo real sem a necessidade de ferramentas externas como Postman.

Com a aplicação rodando, acesse:
> [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🚀 Como Executar

### Pré-requisitos
* **JDK 21** ou superior instalado.
* **Maven** configurado no PATH.
* Instância do **MySQL** em execução.

### Passos para Instalação

1.  **Configuração do Banco de Dados:**
    Crie um banco de dados chamado `db_doce_terapia` no seu MySQL.

2.  **Ajuste de Credenciais:**
    No arquivo `src/main/resources/application.properties`, configure seu usuário e senha:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/db_doce_terapia
    spring.datasource.username=seu_usuario
    spring.datasource.password=sua_senha
    ```

3.  **Compilar e Rodar:**
    ```bash
    # Na raiz da pasta backend:
    mvn clean install
    mvn spring-boot:run
    ```


## 📂 Estrutura de Pastas

```text
src/main/java/br/com/doceterapia/
├── config/        # Configurações do projeto (Swagger, CORS, etc.)
├── controller/    # Endpoints da API (Entrada de requisições)
├── dto/           # Objetos de Transferência de Dados (Request/Response)
├── entity/        # Entidades JPA (Mapeamento das tabelas do banco)
├── exception/     # Tratamento de erros e exceções customizadas
├── mapper/        # Conversão entre Entidades e DTOs
├── repository/    # Interface de comunicação com o banco de dados
└── service/       # Regras de negócio da aplicação
