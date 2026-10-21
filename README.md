# Doce Terapia — API REST (back-end)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23C0E12F?style=for-the-badge&logo=swagger&logoColor=black)

API REST da doceria Doce Terapia. Gerencia clientes, produtos, insumos, pedidos,
produção, pagamentos, estoque e notificações.

- **Java** 21 · **Spring Boot** 4.0 · **Spring Data JPA**
- **Banco:** H2 em memória (perfil `local`) / MySQL 8 (perfil `prod`)
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
| `local` (default) | H2 em memória | `create-drop` | Console H2 em `/h2-console`; `JWT_SECRET` tem default de desenvolvimento |
| `prod` | MySQL 8 | `validate` | Todos os valores vêm do ambiente, sem defaults; a aplicação não inicia sem `JWT_SECRET` |

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

## Endpoints

Rotas sem versionamento, autenticadas por cookie de sessão (exceto login/cadastro).

| Recurso | Base | Operações |
|---------|------|-----------|
| Usuários / Autenticação | `/usuarios` | `POST` cadastro, `POST /login`, `POST /logout`, `GET` perfil |
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

- **Insumos por nome e unidade:** não há índice único em `(nome_normalizado, unidade)`.
  Em `POST /insumos/obter-ou-criar` concorrente, duas requisições com o mesmo nome e
  unidade podem criar insumos duplicados. A correção definitiva exige mudança de schema.
- **Linha única de configuração:** as configurações de notificação e do farol da agenda
  assumem uma única linha, sem restrição de unicidade que a garanta.
- **Autenticação das novas rotas:** as rotas de estoque, catálogo e notificações seguem o
  mesmo modelo das demais e não exigem autenticação própria.
- **Instância única:** o registro de conexões SSE e os jobs agendados assumem uma única
  instância da aplicação; não há coordenação entre réplicas.
