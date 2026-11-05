# Doce Terapia — API REST (back-end)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23C0E12F?style=for-the-badge&logo=swagger&logoColor=black)

API REST da doceria Doce Terapia. Gerencia clientes, produtos, insumos, pedidos,
produção, pagamentos, estoque e notificações.

- **Java** 21 · **Spring Boot** 4.0 · **Spring Data JPA**
- **Banco:** H2 em memória (perfil `local`) / MySQL 8 com migrações Flyway (perfil `prod`)
- **Segurança:** Spring Security + JWT (HMAC-SHA256), autenticação por cookie
- **Docs:** Swagger UI (OpenAPI 3) · **Testes:** JUnit 5 + Mockito + JaCoCo

## Requisitos

- JDK 21 (via `JAVA_HOME`)
- Wrapper Maven `mvnw.cmd` (Windows) / `mvnw` (Unix)
- MySQL 8+ apenas para o perfil `prod`

## Perfis

O perfil ativo vem de `SPRING_PROFILES_ACTIVE` (default `local`).

| Perfil | Banco | `ddl-auto` | Observações |
|--------|-------|------------|-------------|
| `local` (default) | H2 em memória | `create-drop` | Flyway desligado; console H2 em `/h2-console`; `JWT_SECRET` tem default de desenvolvimento |
| `prod` | MySQL 8 | `validate` | Flyway ligado; todos os valores vêm do ambiente, sem defaults; a aplicação não inicia sem `JWT_SECRET` |

## Variáveis de ambiente

No perfil `prod` os nomes abaixo são obrigatórios e vêm do ambiente:

| Variável | Descrição |
|----------|-----------|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (`prod` em produção) |
| `DB_URL` | URL JDBC do MySQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave Base64 (mínimo 32 bytes) para assinar os tokens JWT |

Opcionais:

| Variável | Default | Descrição |
|----------|---------|-----------|
| `JWT_VALIDITY` | `3600` | Validade do token JWT em segundos |
| `NOTIFICACAO_INTERVALO_VERIFICACAO_MS` | `300000` | Intervalo (ms) da verificação agendada dos resumos diários |
| `ASSISTENTE_FUSO_HORARIO` | `America/Sao_Paulo` | Fuso horário usado para calcular os limites de dia e de expediente das notificações |
| `COOKIE_SECURE` | `false` | Atributo `secure` do cookie de sessão, no login e no logout. Use `true` só com HTTPS na frente da aplicação |

## Execução

```bash
# Desenvolvimento (perfil local, H2)
./mvnw.cmd spring-boot:run

# Produção (perfil prod, MySQL)
SPRING_PROFILES_ACTIVE=prod ./mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Swagger UI em
`http://localhost:8080/swagger-ui/index.html`.

## Testes

```bash
./mvnw.cmd test          # testes unitários e de fatia
./mvnw.cmd clean verify  # gate completo + cobertura JaCoCo em target/site/jacoco/
```

O `clean verify` falha se a cobertura do projeto ficar abaixo do piso: 65% de linhas e
65% de branches. A classe `ApiDoceterapiaApplication` e o pacote `config` ficam fora da
conta. O piso está na execução `check` do `jacoco-maven-plugin`, no `pom.xml`.

## Schema do banco

O schema de produção nasce das migrações Flyway em `src/main/resources/db/migration`.
Na subida com o perfil `prod`, o Flyway aplica o `V1__schema.sql` no banco de `DB_URL`, e
o Hibernate confere as entidades com `ddl-auto=validate`. Se algo divergir, a aplicação
não sobe. O usuário de `DB_USERNAME` precisa de permissão de DDL nesse banco.

- **Sem dados reais, o `V1__schema.sql` é editado no lugar.** Uma migração `V2` em diante só
  existe quando houver dados a preservar.
- **Editar o V1 exige recriar o banco.** O Flyway guarda o checksum do script aplicado e
  recusa a subida quando o arquivo muda. Um banco criado sem o Flyway também é recusado, por
  não estar vazio e não ter histórico de migrações. Nos dois casos, apague o volume do banco
  e suba de novo. Com Docker Compose: `docker compose down -v`. Esse comando apaga **todos**
  os dados do volume.
- **Não use** `baseline-on-migrate` nem `flyway repair` para contornar a recusa: o banco
  ficaria sem as colunas novas e o erro apareceria depois.
- **O perfil `local` e os testes não rodam o Flyway.** O H2 usa o schema gerado pelo Hibernate,
  então não tem as linhas semeadas nem os índices do V1. As linhas únicas de configuração
  são criadas na primeira leitura quando faltam.

O V1 semeia uma linha em `configuracao_notificacao` e outra em `configuracao_precificacao`,
ambas com os valores padrão das colunas.

### Validar o schema contra um MySQL real

O `SchemaFlywayValidacaoTest` só roda com `SCHEMA_TEST_DB_URL` definida; sem ela, é pulado. O
CI define as variáveis e usa um serviço `mysql:8.4.4`. Para rodar localmente, use um container
descartável e credenciais fictícias, porque **o teste apaga tudo o que houver no banco
apontado**:

```bash
docker run -d --rm --name mysql-schema-teste -p 127.0.0.1:33306:3306   -e MYSQL_ROOT_PASSWORD=ficticia -e MYSQL_DATABASE=doce_terapia_ci   -e MYSQL_USER=teste -e MYSQL_PASSWORD=ficticia mysql:8.4.4

SCHEMA_TEST_DB_URL='jdbc:mysql://127.0.0.1:33306/doce_terapia_ci?allowPublicKeyRetrieval=true&useSSL=false' SCHEMA_TEST_DB_USERNAME=teste SCHEMA_TEST_DB_PASSWORD=ficticia ./mvnw.cmd test -Dtest=SchemaFlywayValidacaoTest

docker stop mysql-schema-teste
```

## Endpoints

Rotas sem versionamento. A sessão vem do cookie `authToken` ou do header
`Authorization: Bearer`. `POST /usuarios/login` e `POST /usuarios/logout` são públicas.

| Recurso | Base | Operações |
|---------|------|-----------|
| Usuários / Autenticação | `/usuarios` | `POST` cadastro (exige sessão; e-mail repetido devolve 409), `POST /login`, `POST /logout` (sempre 204, com ou sem sessão), `GET /me` usuário logado, `GET` lista |
| Clientes | `/clientes` | CRUD |
| Categorias de produto | `/categorias-produto` | CRUD |
| Produtos | `/produtos` | CRUD |
| Insumos de produto | `/produtos/{produtoId}/insumos` | CRUD |
| Categorias de insumo | `/categorias-insumo` | CRUD |
| Insumos | `/insumos` | CRUD, `GET /catalogo`, `POST /obter-ou-criar` |
| Pedidos | `/pedidos` | CRUD + transição de status |
| Itens de pedido | `/itens-pedido` | CRUD |
| Pagamentos | `/pagamentos` | `GET`, `GET /{id}`, `GET /pedido/{pedidoId}`, `PUT /{id}` |
| Cancelamentos | `/cancelamentos` | `POST /pedido/{pedidoId}`, consultas |
| Produção | `/producoes` | CRUD + transição de status |
| Estoque | `/estoque/movimentos` | `POST` entrada de compra, `GET` histórico |
| Notas fiscais | `/notas-fiscais/{chave}/status` | `GET` status |
| Farol da agenda | `/configuracao-farol` | `POST`, `GET`, `PUT` |
| Notificações | `/notificacoes` | CRUD, `PATCH /{id}/marcar-lida`, `GET /nao-lidas`, `GET /stream` (SSE), `GET /agenda`, `POST /{id}/confirmacao-envio` |
| Configuração de notificações | `/configuracao-notificacao` | `GET`, `PUT` |

## Notificações

Alertas e resumos são criados com uma chave de unicidade (sem duplicar) e uma
janela de entrega dentro do expediente.

- **Imediatos:** `ESTOQUE_BAIXO` quando uma movimentação cruza o mínimo para
  baixo; `DIA_LOTADO` quando um pedido faz uma data de entrega atingir o limite
  vermelho do farol da agenda.
- **Resumos:** `RESUMO_INICIO_DIA` e `RESUMO_FIM_DIA`, criados por uma
  verificação agendada quando o horário configurado já passou em um dia útil.
  Seções vazias aparecem como "nada pendente".

A configuração fica em `/configuracao-notificacao` (linha única):

| Campo | Default | Descrição |
|-------|---------|-----------|
| `inicioExpediente` / `fimExpediente` | `08:00` / `18:00` | Faixa do expediente |
| `diasExpediente` | `1,2,3,4,5,6` | Dias úteis (1=segunda … 7=domingo) |
| `horarioResumoInicio` / `horarioResumoFim` | `08:00` / `17:30` | Horário de cada resumo |
| `diasAntecedencia` | `3` | Dias à frente no resumo de fim do dia |
| `resumoInicioDiaAtivo` / `resumoFimDiaAtivo` | `true` | Habilita cada resumo |
| `alertaEstoqueBaixoAtivo` / `alertaDiaLotadoAtivo` | `true` | Habilita cada alerta imediato |

## Limitações conhecidas

- **Primeiro usuário:** o cadastro exige sessão e ainda não há criação automática do
  primeiro usuário. Com a tabela `usuario` vazia, ninguém consegue entrar.
- **Insumos por nome e unidade:** não há índice único em `(nome_normalizado, unidade)`.
  Em `POST /insumos/obter-ou-criar` concorrente, duas requisições com o mesmo nome e
  unidade podem criar insumos duplicados. A correção definitiva exige mudança de schema.
- **Linha única de configuração:** as configurações de notificação e do farol da agenda
  assumem uma única linha, sem restrição de unicidade que a garanta.
- **Autenticação das novas rotas:** as rotas de estoque, catálogo e notificações seguem o
  mesmo modelo das demais e não exigem autenticação própria.
- **Instância única:** o registro de conexões SSE e os jobs agendados assumem uma única
  instância da aplicação; não há coordenação entre réplicas.
