# Modern Java CRUD - Project Management API

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)

Esta é uma API robusta para gerenciamento de projetos, desenvolvida com o que há de mais moderno no ecossistema Java. O projeto foca em **Domain-Driven Design (DDD)**, tipos seguros e uma lógica de domínio rica que vai além de um CRUD anêmico tradicional.

---

## 🚀 Stack Tecnológica

- **Java 21:** Utilização de Records, Pattern Matching e Virtual Threads (quando aplicável).
- **Spring Boot 3.x:** Base da aplicação para autoconfiguração e produtividade.
- **Spring Security:** Implementação de camadas de segurança para proteção de recursos e controle de acesso.
- **Spring Data JPA + Hibernate:** Persistência avançada com **Criteria API (Specifications)** para filtros dinâmicos e seguros.
- **Flyway:** Gerenciamento de migrações de banco de dados, garantindo versionamento de schema.
- **MapStruct:** Mapeamento de objetos (DTO <-> Entity) gerado em tempo de compilação para máxima performance.
- **SpringDoc OpenAPI (Swagger):** Documentação interativa da API gerada automaticamente, facilitando a integração com o front-end.
- **Testcontainers:** Garantia de qualidade com testes de integração reais utilizando Docker.
- **Instancio:** Geração inteligente de dados para testes automatizados consistentes.

---

## 🏗️ Diferenciais da Arquitetura

### 🧠 Domínio Rico e Máquina de Estados

A lógica de transição de status do projeto reside na própria entidade. O projeto implementa uma **Máquina de Estados** que valida integridade e regras de negócio (ex: impossibilidade de excluir projetos em andamento ou cancelar projetos finalizados) antes de qualquer persistência.

### 📊 Inteligência em Nível de Banco de Dados

Para otimizar a performance e reduzir o processamento na camada de aplicação:

- **RiskLevel:** Calculado via SQL `CASE/WHEN` diretamente no banco, baseado em orçamento e duração.
- **DurationMonths:** Calculado através de funções nativas de data do PostgreSQL.
- Mapeamento via **`@Formula`** do Hibernate, permitindo que a aplicação consuma campos calculados em tempo real com o `EntityManager.refresh()`.

### 🔍 Consultas Dinâmicas (Specifications)

A API suporta filtros complexos e opcionais através do padrão Specification. Isso permite que o cliente combine filtros de nome, status, data e nível de risco em uma única query sem acoplamento com Strings ou risco de SQL Injection.

---

## 🛠️ Como Rodar o Projeto

### Pré-requisitos

- JDK 21+
- Docker (essencial para Testcontainers e Banco de Dados)
- Maven 3.9+

### Execução

1.  **Clonar o repositório:**
    ```bash
    git clone https://github.com/brnnxrsh/modern-java-crud.git
    ```
2.  **Configurar propriedades para o ambiente local**
    ```bash
    Duplique application-lcl.yml.example e renomeie para application-lcl.yml
    ```
3.  **Subir o ambiente local com Docker Compose:**
    ```bash
    docker-compose up -d
    ```
4.  **Rodar a aplicação:**
    ```bash
    ./mvnw spring-boot:run
    ```

A interface do **Swagger UI** estará disponível em: `http://localhost:8080/swagger-ui.html`

---

## 🧪 Estratégia de Testes

O projeto adota uma abordagem de testes rigorosa:

- **Unitários:** Mockito com `InOrder` para validar a orquestração do `ProjectService`.
- **Integração:** `@SpringBootTest` com **Testcontainers** para validar o comportamento real das queries e triggers do PostgreSQL.

```bash
./mvnw test
```
