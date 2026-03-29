# Modern Java CRUD — Project Management API

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)

API REST para gerenciamento de portfólio de projetos, desenvolvida com Java 21 e Spring Boot 3.5. O projeto vai além de um CRUD anêmico: o domínio é rico, a lógica de negócio vive nas entidades, e cada decisão de implementação tem uma justificativa técnica clara.

---

## Stack Tecnológica

| Tecnologia                  | Versão | Uso                                                      |
| --------------------------- | ------ | -------------------------------------------------------- |
| Java                        | 21     | Records, Pattern Matching, Streams com method references |
| Spring Boot                 | 3.5    | Base da aplicação                                        |
| Spring Data JPA + Hibernate | —      | Persistência com Specifications e Criteria API           |
| Spring Security             | —      | Autenticação Basic Auth + controle por role              |
| Spring Cloud OpenFeign      | —      | Client HTTP declarativo para API externa de membros      |
| PostgreSQL                  | 16     | Banco de dados com funções nativas e tipos ENUM          |
| Flyway                      | —      | Versionamento de schema                                  |
| MapStruct                   | —      | Mapeamento DTO ↔ Entity em tempo de compilação           |
| Lombok                      | —      | Redução de boilerplate                                   |
| SpringDoc OpenAPI           | —      | Documentação interativa (Swagger UI)                     |
| Testcontainers              | —      | Testes de integração com PostgreSQL real                 |
| Instancio                   | —      | Geração de dados para testes                             |
| Spotless                    | —      | Formatação de código automatizada (Eclipse formatter)    |

---

## Requisitos vs Implementação

| Requisito                                                  | Implementação                                                                                          |
| ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| CRUD completo de projetos                                  | `ProjectController` + `ProjectService` com fluxo map → validate → resolve → saveAndRefresh → log → map |
| Campos: nome, datas, orçamento, descrição, gerente, status | Entidade `Project` com todos os campos, validações via `@PrePersist`/`@PreUpdate`                      |
| Classificação de risco dinâmica (LOW / MEDIUM / HIGH)      | `RiskLevel` enum com thresholds por orçamento e duração; calculado via `@Formula` + função PostgreSQL  |
| Status sequenciais com máquina de estados                  | `ProjectStatus.getNext()` + `canChangeTo()` — transições inválidas lançam `BusinessException`          |
| CANCELLED aplicável a qualquer momento                     | `canChangeTo()` sempre permite `CANCELLED`, independente do status atual                               |
| Bloqueio de exclusão em STARTED, IN_PROGRESS, FINISHED     | `EnumSet NOT_DELETABLE` + `validateDelete()` chamado via `@PreRemove`                                  |
| Somente EMPLOYEE pode ser membro do projeto                | Validado em `MemberService` consultando a role via Feign antes de associar                             |
| Limite de 3 projetos ativos por membro                     | `projectRepository.count(ProjectSpec.withMemberIdAndStatusNotIn(...))` antes de associar               |
| Cadastro de membros via API externa mockada                | `MemberClient` (Feign) + Mock Server; URL externalizada em `clients.member.url`                        |
| Relatório de portfólio                                     | `ReportService.getSummary()` com 4 métricas via Criteria API                                           |
| Paginação e filtros na listagem de projetos                | `Pageable` + `ProjectFilterDto` + `ProjectSpec` com 7 filtros opcionais                                |
| Spring Security com controle de acesso                     | `ADMIN` acessa `/reports/**`; demais endpoints exigem autenticação; usuários via properties            |
| Tratamento global de exceções                              | `GlobalExceptionHandler` com RFC 7807 (`ProblemDetail`)                                                |
| Swagger / OpenAPI                                          | SpringDoc com autenticação Basic configurada no schema                                                 |
| Testes unitários                                           | 9 classes de teste cobrindo domain, service, controller e exception                                    |

---

## Destaques de Implementação

### Domínio Rico — Entidade com comportamento

A entidade `Project` não é um container passivo de dados. Ela valida seu próprio estado e rejeita transições inválidas:

```java
public void fillStatus(final ProjectStatus newStatus) {
    if (!this.status.canChangeTo(newStatus))
        throw new BusinessException("O status não pode ser alterado de %s para %s."
            .formatted(status, newStatus));
    this.status = newStatus;
}

@PrePersist protected void prePersist() { this.fillInitialStatus(); this.validateCreate(); }
@PreUpdate  protected void preUpdate()  { this.validateUpdate(); }
@PreRemove  protected void preRemove()  { this.validateDelete(); }
```

A validação acontece em dois lugares intencionalmente: no service (para mensagens de erro contextuais) e nos lifecycle callbacks da JPA (como última linha de defesa antes da persistência).

---

### Máquina de Estados no Enum

As transições de status são encapsuladas no próprio `ProjectStatus`, eliminando lógica condicional espalhada pela aplicação:

```
IN_REVIEW → REVIEW_COMPLETED → REVIEW_APPROVED → STARTED → PLANNED → IN_PROGRESS → FINISHED
                                                                    ↘ CANCELLED (qualquer momento)
```

```java
public boolean canChangeTo(ProjectStatus status) {
    return status == CANCELLED || status == getNext();
}
```

---

### Cálculo de Risco e Duração no Banco de Dados

`RiskLevel` e `durationMonths` são calculados por funções PostgreSQL e mapeados com `@Formula` do Hibernate. A aplicação nunca calcula isso em memória — o banco é a fonte de verdade:

```java
@Formula("fn_months_between(start_at, fn_effective_date(end_at, expected_end_at))")
private Integer durationMonths;
```

Após salvar, um `entityManager.flush() + refresh()` garante que esses campos calculados sejam populados imediatamente no objeto retornado ao cliente.

---

### Migrations Portáveis — Separação entre Common e Vendor

As migrations Flyway são divididas em dois diretórios carregados via propriedade `${spring.datasource.platform}`:

```
db/migration/
├── common/          → SQL padrão, funciona em qualquer banco
│   ├── V1__create_projects_table.sql
│   └── V2__create_members_table.sql
└── postgres/        → Específico do PostgreSQL
    ├── V1.1__create_fn_months_between.sql
    ├── V1.2__create_fn_days_between.sql
    ├── V1.3__create_fn_effective_date.sql
    └── V1.4__add_indexes_projects.sql   ← GIN + pg_trgm + índices em funções
```

```yaml
# application.yaml
flyway:
  locations:
    - classpath:db/migration/common
    - classpath:db/migration/${spring.datasource.platform}
```

Toda a lógica específica do PostgreSQL — cálculo de duração em meses, data efetiva de término e funções usadas nos índices — foi isolada em funções `IMMUTABLE` no diretório `postgres/`. Para migrar para outro banco, basta criar um novo diretório com as mesmas funções reescritas na sintaxe do banco alvo. O código da aplicação não muda.

---

### Consultas Type-Safe com Specifications + Criteria API

Filtros dinâmicos sem concatenação de strings ou risco de SQL Injection. O metamodel JPA garante que nomes de campos sejam verificados em tempo de compilação:

```java
public static Specification<Project> withFilter(final ProjectFilterDto filter) {
    return Specification.allOf(
        withName(filter.name()),
        withStatuses(filter.statuses()),
        withRiskLevels(filter.riskLevels()),
        withManagerIds(filter.managerIds()),
        withMemberIds(filter.memberIds()),
        withStartAtAfter(filter.startAtAfter()),
        withEndAtBefore(filter.endAtBefore())
    );
}
```

Filtros nulos são ignorados automaticamente pelo `Specification.allOf`.

O relatório de portfólio usa Criteria API diretamente para 4 queries agregadas distintas — count+sum por status, média de duração, e contagem de membros/gerentes únicos — todas com suporte a filtros opcionais.

---

### Tratamento de Exceções RFC 7807

Todas as respostas de erro seguem o padrão `ProblemDetail` (RFC 7807), com URNs semânticas:

```json
{
  "type": "urn:portfolio:error:business-rule",
  "status": 422,
  "detail": "O status não pode ser alterado de IN_PROGRESS para IN_REVIEW.",
  "timestamp": "2026-03-29T14:00:00Z"
}
```

Hierarquia de exceções clara: `BusinessException` (422) para violações de regra de negócio e `ResourceNotFoundException` (404) para entidades inexistentes.

---

### Segurança com Controle por Role

```
GET  /reports    → apenas ADMIN
POST /projects   → qualquer usuário autenticado
GET  /swagger-ui → público
```

Usuários configurados via properties (`app.security.users`), sem hardcode no código fonte.

---

## Estrutura de Pacotes

```
com.brenner.modern_java_crud/
├── client/        → Feign clients para APIs externas
├── config/        → SecurityConfig, OpenApiConfig, UserConfig
├── controller/    → ProjectController, MemberController, ReportController
├── domain/        → Project, Member, ProjectStatus, RiskLevel, MemberRole
├── dto/           → Records de entrada/saída por operação
├── exception/     → BusinessException, ResourceNotFoundException, GlobalExceptionHandler
├── mapper/        → ProjectMapper (MapStruct)
├── repository/
│   ├── projection/  → ProjectStatusMetricsProjection
│   └── spec/        → ProjectSpec, SpecUtils
├── service/       → ProjectService, MemberService, ReportService
└── util/          → Ensure, SpecUtils
```

---

## Estratégia de Testes

| Classe                       | Tipo       | O que cobre                                                 |
| ---------------------------- | ---------- | ----------------------------------------------------------- |
| `ProjectTest`                | Unitário   | Validações de domínio: datas, membros, transições de status |
| `ProjectServiceTest`         | Unitário   | Orquestração do fluxo via `InOrder` do Mockito              |
| `MemberServiceTest`          | Unitário   | Validação de role EMPLOYEE, limite de 3 projetos por membro |
| `ReportServiceTest`          | Unitário   | Workflow de geração de relatório                            |
| `ProjectControllerTest`      | WebMvcTest | Endpoints REST + autenticação                               |
| `MemberControllerTest`       | WebMvcTest | Endpoints REST + autenticação                               |
| `ReportControllerTest`       | WebMvcTest | Acesso restrito a ADMIN + 401/403                           |
| `GlobalExceptionHandlerTest` | Unitário   | Handlers de exceção Feign                                   |
| `ProjectServiceIT`           | Integração | Regras de negócio com PostgreSQL real via Testcontainers    |

Os testes unitários de service verificam a **ordem** das operações com `inOrder()`, não apenas o resultado — garantindo que o fluxo `map → validate → resolve → saveAndRefresh` seja respeitado.

### Testes de Integração — Cobertura do Core do Sistema

`ProjectServiceIT` usa Testcontainers (PostgreSQL real) e cobre os dois comportamentos mais críticos do sistema:

**Classificação de risco — todos os limites de fronteira, para criação e atualização:**

| Budget     | Risk Level |
| ---------- | ---------- |
| 500.000,01 | HIGH       |
| 500.000,00 | MEDIUM     |
| 100.001,00 | MEDIUM     |
| 100.000,99 | LOW        |
| 0,00       | LOW        |

| Duração (meses) | Risk Level |
| --------------- | ---------- |
| 7               | HIGH       |
| 6               | MEDIUM     |
| 3               | MEDIUM     |
| 2               | LOW        |
| 0               | LOW        |

Cada combinação é testada tanto no `create` quanto no `update`, garantindo que a recalculação via `@Formula` + `entityManager.refresh()` funciona corretamente após persistência.

**Máquina de estados — cobertura total:**

- `advanceStep` cobre todos os status possíveis como destino (exceto CANCELLED), avançando o projeto sequencialmente até atingir cada um
- `cancel` cobre todos os status como ponto de partida, garantindo que CANCELLED é sempre permitido

```bash
./mvnw test
```

---

## Como Rodar

### Pré-requisitos

- JDK 21+
- Docker

### Passos

```bash
# 1. Clonar o repositório
git clone https://github.com/brnnxrsh/modern-java-crud.git
cd modern-java-crud

# 2. Configurar propriedades locais
cp src/main/resources/application-lcl.yml.example src/main/resources/application-lcl.yml

# 3. Rodar a aplicação (Docker Compose sobe o PostgreSQL automaticamente)
./mvnw spring-boot:run
```

Swagger UI disponível em: `http://localhost:8080/swagger-ui.html`

> Autenticar com as credenciais definidas em `application-lcl.yml` (padrão: `admin/admin`).
