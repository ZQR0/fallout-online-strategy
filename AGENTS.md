# Repository Guidelines

This document provides guidelines for contributors and AI agents working on **Fallout: Битва за Пустошь**. This project is a reactive multiplayer strategy game built on a Spring Boot microservices architecture.

## Project Structure & Module Organization

The repository consists of **7 independent Gradle subprojects**. There is no root-level Gradle build; each module must be managed individually.

- `core/`: Shared domain models, enums, and Kafka command/event classes.
- `discovery/`: Eureka Service Registry (Port 8761).
- `gateway/`: Spring Cloud Gateway for REST and WebSocket routing (Port 8080).
- `lobby/`: Session lifecycle and PostgreSQL persistence (Port 8081).
- `engine/`: Game logic execution via Redis Lua scripts (Port 8082).
- `broadcast/`: WebSocket fan-out for real-time state updates (Port 8090).
- `history/`: Game event archiving to PostgreSQL (Port 8083).

## Build, Test, and Development Commands

Since modules are independent, always navigate to the specific module directory before running commands.

- **Run a service**: `cd <module> && ./gradlew bootRun`
- **Build a module**: `cd <module> && ./gradlew build`
- **Run tests**: `cd <module> && ./gradlew test`
- **Infra Setup**: `docker compose -f docker-compose.dev.yaml up -d` (Starts Postgres, Redis, Kafka, and Eureka).

## Coding Style & Naming Conventions

- **Language**: Java 21 (toolchain enforced).
- **Paradigm**: Reactive-first. Use **Spring WebFlux**, `Mono`, and `Flux`. No servlet stack.
- **Boilerplate**: Use **Lombok** (`@Data`, `@Builder`, `@RequiredArgsConstructor`) to avoid manual getters/setters.
- **Naming**: Package root follows `com.fallout.<module>`.
- **Logging**: Always include `sessionId` in logs via MDC for traceability across microservices.

## Testing Guidelines

- **Framework**: JUnit 5.
- **Approach**: Focus on integration tests using **Testcontainers** for Redis, Kafka, and PostgreSQL to verify atomic game state mutations in the `engine`.
- **Location**: Tests reside in `src/test/java` within each module.

## Commit & Pull Request Guidelines

- **Commits**: Use concise, imperative messages. Reference sprint goals from `fallout-wasteland-battle.md` if applicable.
- **PRs**: Include a description of the change, linked issues, and verification results (e.g., test logs or screenshots).
