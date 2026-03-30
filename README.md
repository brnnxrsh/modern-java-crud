# Modern Java CRUD — Project Management API

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)

API REST para gerenciamento de portfólio de projetos desenvolvida com Java 21 e Spring Boot 3.5. O sistema controla o ciclo de vida completo de projetos — da análise de viabilidade ao encerramento — incluindo gerenciamento de equipe, orçamento e classificação de risco.

---

## Stack Tecnológica

| Tecnologia                  | Versão | Uso                                                       |
| --------------------------- | ------ | --------------------------------------------------------- |
| Java                        | 21     | Records, Pattern Matching, Streams com method references  |
| Spring Boot                 | 3.5    | Base da aplicação                                         |
| Spring Data JPA + Hibernate | —      | Persistência com Specifications e Criteria API            |
| Spring Security             | —      | Autenticação Basic Auth + controle por role               |
| Spring Cloud OpenFeign      | —      | Client HTTP declarativo para API externa de membros       |
| Resilience4j                | —      | Circuit breaker, retry e time limiter na integração Feign |
| PostgreSQL                  | 16     | Banco de dados com funções nativas e tipos ENUM           |
| Flyway                      | —      | Versionamento de schema com separação vendor-specific     |
| MapStruct                   | —      | Mapeamento DTO ↔ Entity em tempo de compilação            |
| Lombok                      | —      | Redução de boilerplate                                     |
| SpringDoc OpenAPI           | —      | Documentação interativa (Swagger UI)                      |
| Testcontainers              | —      | Testes de integração com PostgreSQL real e Mock Server    |
| Instancio                   | —      | Geração de dados aleatórios para testes                   |
| Spotless                    | —      | Formatação de código automatizada (Eclipse formatter)     |

---

## Requisitos vs Implementação

| Requisito | Implementação |
| --------- | ------------- |
| CRUD completo de projetos | `ProjectController` + `ProjectService` com fluxo map → validate → resolve → saveAndRefresh → log → map |
| Campos: nome, datas, orçamento, descrição, gerente, status | Entidade `Project` com todos os campos e validações via `@PrePersist`/`@PreUpdate` |
| Classificação de risco dinâmica (LOW / MEDIUM / HIGH) | `RiskLevel` enum com thresholds por orçamento e duração; calculado via `@Formula` + função PostgreSQL |
| Status sequenciais com máquina de estados | `ProjectStatus.getNext()` + `canChangeTo()` — transições inválidas lançam `BusinessException` |
| CANCELLED aplicável a qualquer momento | `canChangeTo()` sempre permite `CANCELLED`, independente do status atual |
| Bloqueio de exclusão em status ativos | `EnumSet NOT_DELETABLE` + `validateDelete()` chamado via `@PreRemove` |
| Membros via API externa mockada | `MemberClient` (Feign) + Mock Server; URL externalizada em `clients.member.url` |
| Resiliência na integração externa | Circuit breaker + retry + time limiter via Resilience4j; configuração completa em `application.yaml` |
| Apenas funcionários podem ser membros | Validado em `MemberService` consultando a role via Feign antes de associar |
| Limite de 1–10 membros; máx. 3 projetos ativos por membro | `projectRepository.count(ProjectSpec.withMemberIdAndStatusNotIn(...))` antes de associar |
| Relatório de portfólio (4 métricas) | `ReportService.getSummary()` com Criteria API — count+sum por status, média de duração, membros/gerentes únicos |
| Paginação e filtros na listagem | `Pageable` + `ProjectFilterDto` + `ProjectSpec` com 7 filtros opcionais |
| Controle de acesso por role | `ADMIN` acessa `/reports/**`; demais endpoints exigem autenticação; usuários via properties |
| Tratamento global de exceções | `GlobalExceptionHandler` com RFC 7807 (`ProblemDetail`) |
| Swagger/OpenAPI | SpringDoc com autenticação Basic configurada no schema |

---

## Endpoints e Acesso

Todos os endpoints privados usam autenticação HTTP Basic.

| Método | Endpoint | Acesso necessário |
| ------ | -------- | ----------------- |
| `GET` | `/swagger-ui.html` | Público |
| `GET` | `/swagger-ui/**` | Público |
| `GET` | `/v3/api-docs/**` | Público |
| `GET` | `/projects` | Autenticado (qualquer role) |
| `GET` | `/projects/{id}` | Autenticado (qualquer role) |
| `POST` | `/projects` | Autenticado (qualquer role) |
| `PUT` | `/projects/{id}` | Autenticado (qualquer role) |
| `DELETE` | `/projects/{id}` | Autenticado (qualquer role) |
| `PATCH` | `/projects/{id}/next-step` | Autenticado (qualquer role) |
| `PATCH` | `/projects/{id}/cancel` | Autenticado (qualquer role) |
| `GET` | `/members/{id}` | Autenticado (qualquer role) |
| `POST` | `/members` | Autenticado (qualquer role) |
| `GET` | `/reports` | `ADMIN` |

---

## Destaques de Implementação

### Domínio Rico — a entidade tem comportamento

A entidade `Project` não é um container passivo de dados. Ela valida seu próprio estado e rejeita transições inválidas antes mesmo de chegar ao banco:

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

A validação acontece em dois lugares intencionalmente: no service (para mensagens de erro ricas e contextuais) e nos lifecycle callbacks da JPA (como barreira final antes da persistência, independente de quem chamou o save).

---

### Máquina de Estados no Enum

As transições de status são encapsuladas no próprio `ProjectStatus`. Nenhum `if/switch` no service:

```
IN_REVIEW → REVIEW_COMPLETED → REVIEW_APPROVED → STARTED → PLANNED → IN_PROGRESS → FINISHED
                                                                    ↘ CANCELLED (qualquer momento)
```

```java
public boolean canChangeTo(ProjectStatus status) {
    return status == CANCELLED || status == getNext();
}
```

`CANCELLED` é tratado como caso universal sem nenhuma condição especial no service. `getNext()` usa `switch` com pattern matching. Transições inválidas lançam `BusinessException` com mensagem contextual.

---

### Cálculo de Risco e Duração no Banco de Dados

`RiskLevel` e `durationMonths` são calculados por funções PostgreSQL e mapeados com `@Formula` do Hibernate. A aplicação nunca calcula isso em memória — o banco é a fonte de verdade:

```java
@Formula("fn_months_between(start_at, fn_effective_date(end_at, expected_end_at))")
private Integer durationMonths;
```

Após salvar, um `entityManager.flush() + refresh()` garante que os campos calculados sejam populados imediatamente no objeto retornado. Isso elimina inconsistências entre o que o banco calculou e o que a aplicação exibe.

---

### Resiliência na Integração com API Externa

A integração com o serviço externo de membros tem duas camadas de resiliência complementares:

```
MemberService
  → MemberClientAdapter.findById()   ← @Retry  (resilience4j-spring-boot3)
      → MemberClient.findById()      ← CircuitBreaker  (Spring Cloud Feign CB)
```

| Camada | Mecanismo | Comportamento |
|--------|-----------|---------------|
| Circuit Breaker | Spring Cloud Feign CB + Resilience4j | Após 50% de falhas em 5 chamadas, abre o circuito por 30s. Retorna HTTP 503 durante o período aberto. 404s são ignorados — não contam como falha. |
| Retry | `@Retry(name = "member-client")` no adapter | Reprocessa até 3 vezes com backoff exponencial (500ms → 1s → 2s) em `IOException` e `RetryableException`. Não reprocessa `CallNotPermittedException` — se o circuito está aberto, falha imediatamente. |

Toda a configuração de thresholds, janelas e timeouts fica no `application.yaml`, sem hardcode nos beans Java.

---

### Specifications + Criteria API — Consultas Type-Safe

Filtros dinâmicos sem concatenação de strings ou risco de SQL Injection. O metamodel JPA (`Project_`) garante que nomes de campo sejam verificados em tempo de compilação:

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

Filtros nulos são ignorados automaticamente pelo `Specification.allOf`. O relatório de portfólio usa Criteria API diretamente para 4 queries agregadas — count+sum por status, média de duração (apenas encerrados), e contagem de membros/gerentes únicos.

---

### Migrations Portáveis — Separação Common vs. Vendor

```
db/migration/
├── common/          → SQL padrão, funciona em qualquer banco
└── postgres/        → Específico do PostgreSQL (funções IMMUTABLE, índices GIN + pg_trgm)
```

```yaml
flyway:
  locations:
    - classpath:db/migration/common
    - classpath:db/migration/${spring.datasource.platform}
```

Toda a lógica específica do PostgreSQL — cálculo de duração em meses, data efetiva de término e funções usadas nos índices — fica isolada em funções `IMMUTABLE`. Para migrar para outro banco, basta criar um novo diretório com as funções reescritas. O código da aplicação não muda.

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

O `GlobalExceptionHandler` cobre: `BusinessException` (422), `ResourceNotFoundException` (404), `FeignException.NotFound` (404), `FeignException` genérico (502) e `CallNotPermittedException` quando o circuit breaker abre (503).

---

## Estrutura de Pacotes

```
com.brenner.modern_java_crud/
├── client/        → Feign clients + adapters de resiliência
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

## Arquitetura de Testes

| Camada | Framework | Banco real | Objetivo |
| ------ | --------- | ---------- | -------- |
| Unitário (`*Test`) | JUnit 5 + Mockito + Instancio | Não | Verificar fluxo de orquestração com `inOrder()` e regras isoladas |
| Web (`*ControllerTest`) | Spring `@WebMvcTest` + MockMvc | Não | Contrato HTTP, autenticação e status codes |
| Integração (`*IT`) | Spring Boot Test + Testcontainers | Sim | Comportamento fim a fim com PostgreSQL e Mock Server reais |

### Classes e cobertura

| Classe | Tipo | O que cobre |
| ----- | ---- | ----------- |
| `ProjectTest` | Unitário | Validações de domínio: datas, membros e transições de status |
| `ProjectStatusTest` / `RiskLevelTest` | Unitário | Regras dos enums de domínio |
| `ProjectServiceTest` | Unitário | Orquestração do service com `inOrder()` |
| `MemberServiceTest` | Unitário | Regra de role EMPLOYEE e limite de projetos ativos |
| `ReportServiceTest` | Unitário | Geração do relatório agregado |
| `ProjectControllerTest` | WebMvcTest | Endpoints de projeto + autenticação |
| `MemberControllerTest` | WebMvcTest | Endpoints de membro + autenticação |
| `ReportControllerTest` | WebMvcTest | Controle de acesso (ADMIN / 401 / 403) |
| `GlobalExceptionHandlerTest` | Unitário | Feign 404, Feign genérico, circuit breaker aberto |
| `ProjectServiceIT` | Integração | Regras críticas com PostgreSQL real (risco, máquina de estados) |
| `ProjectRepositoryIT` | Integração | Queries customizadas do `ProjectRepositoryImpl` (Criteria API) |
| `ReportServiceIT` | Integração | Métricas do relatório com dados reais no banco |
| `ModernJavaCrudApplicationTests` | Integração | Smoke test de contexto Spring |

### Como a infraestrutura de teste sobe

`TestcontainersConfiguration` inicializa `DockerComposeContainer` com `compose.test.yml` e sobe dois serviços:

- `postgres` (`postgres:16-alpine`) — banco real com Flyway rodando as migrations
- `member-api-mock` (`mockserver/mockserver:5.15.0`) — mock do serviço externo de membros

As propriedades de runtime são injetadas por `@DynamicPropertySource`. O Spring Docker Compose automático fica desabilitado — somente Testcontainers é usado nos testes.

Testes que dependem de Docker usam `@Tag("docker")` e podem ser excluídos via `-Pdocker-off`.

---

## Como Rodar os Testes

| Comando | O que executa |
| ------- | ------------- |
| `./mvnw test` | Unitários + web + testes com `@Tag("docker")` |
| `./mvnw verify` | Tudo de `test` + ITs via Failsafe |
| `./mvnw test -Pdocker-off` | Apenas unitários e web (sem Docker) |
| `./mvnw verify -Pdocker-off` | Mesmo do anterior, sem ITs |

```bash
# Feedback rápido sem Docker
./mvnw test -Pdocker-off

# Validação completa antes de abrir PR
./mvnw verify
```

---

## Como Rodar a Aplicação

**Pré-requisitos:** JDK 21+ e Docker

```bash
git clone https://github.com/brnnxrsh/modern-java-crud.git
cd modern-java-crud

cp src/main/resources/application-lcl.yml.example src/main/resources/application-lcl.yml

./mvnw spring-boot:run
```

Swagger UI disponível em: `http://localhost:8080/swagger-ui.html`

> Autenticar com as credenciais definidas em `application-lcl.yml` (padrão: `admin/admin`).
