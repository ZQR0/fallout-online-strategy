# CLAUDE.md

> AI assistant context for **Fallout: Битва за Пустошь** (Fallout: Battle for the Wasteland) — a reactive multiplayer strategy game built on Spring Boot microservices.

## What this project is

A real-time multiplayer strategy game in the Fallout universe. Up to **4 players** per session, each controlling a faction, moving squads between iconic locations (Vault 101, Megaton, Tenpenny Tower, etc.), attacking and reinforcing points. The map is a static graph of nodes overlaid on a background image; players click nodes to issue commands.

- **State**: stored in Redis with atomic Lua scripts for concurrent commands.
- **Events**: propagated through Kafka topics for decoupling and horizontal scaling.
- **Real-time**: WebSocket connection per player; state deltas pushed on every change.
- **Goal of the codebase**: a portfolio-ready demo of reactive programming, event-driven architecture, Redis-as-state-store, and microservice patterns.

Full design lives in [`fallout-wasteland-battle.md`](fallout-wasteland-battle.md) (authoritative spec — architecture, sprint plan, Redis schema, API surface). Architecture diagram: `Диаграмма без названия.drawio`.

---

## Module map

This is **not a multi-module Gradle project**. It is **7 independent Gradle subprojects at the root**, each with its own `build.gradle`, `settings.gradle`, and `gradlew`. There is no parent `build.gradle` or root `settings.gradle` — cross-module dependencies are not yet wired.

| Module      | Type             | Port  | Responsibility                                                            | Key dependencies |
|-------------|------------------|-------|---------------------------------------------------------------------------|------------------|
| `core`      | java-library     | —     | Shared domain: enums, models, Kafka command/event classes                 | `jackson-annotations`, Lombok |
| `discovery` | Spring Boot app  | 8761  | Eureka Server — service registry                                          | `spring-cloud-starter-netflix-eureka-server` |
| `gateway`   | Spring Boot app  | 8080  | API Gateway (WebFlux, Netty) — routes REST + WebSocket via Eureka         | `spring-cloud-starter-gateway-server-webflux` |
| `lobby`     | Spring Boot app  | 8081  | Session lifecycle: create/join, init Redis on game start, persist to PG    | WebFlux, R2DBC + JDBC, Liquibase, WebSocket, Redis, PG, Eureka client |
| `engine`    | Spring Boot app  | 8082  | Game core: Kafka consumer for commands, executes Lua scripts in Redis     | WebFlux, Reactive Redis, Kafka, Eureka client |
| `broadcast` | Spring Boot app  | 8090  | WebSocket fan-out: keeps `sessionId → ws sessions`, consumes game-events | WebFlux, WebSocket, Kafka, Eureka client |
| `history`   | Spring Boot app  | 8083  | Event archive: consumes game-events, writes to PG for replays             | WebFlux, R2DBC + JDBC, Liquibase, Kafka, Eureka client |

**Stack**: Java 21 (toolchain), Spring Boot 4.1.0, Spring Cloud 2025.1.2, Gradle 9.5.1, Lombok (`compileOnly` + `annotationProcessor`).

---

## Architecture

```
[Browser]
   │  WebSocket (raw or STOMP)
   ▼
[Gateway :8080] ──► [Eureka :8761]
   │
   ├─ REST  /lobby/**        → [Lobby :8081]  ──► PostgreSQL (WAITING sessions)
   │                              └─► Redis (init session state on start)
   │                              └─► Kafka lobby-events (SESSION_STARTED)
   │
   ├─ WS    /ws/**           → [Broadcast :8090]
   │                              └─► Kafka game-commands (commands from clients)
   │
   └─ (engine + history resolve via Eureka)

[Engine :8082]  ← Kafka game-commands
                  └─► Lua scripts in Redis (atomic state mutations)
                  └─► Kafka game-events (state deltas, battle reports, finish)

[Broadcast :8090]  ← Kafka game-events  ──► WebSocket push to clients

[History :8083]   ← Kafka game-events  ──► PostgreSQL (replay storage)
```

### Kafka topics

| Topic           | Producer                | Consumer(s)                       | Key (for partitioning) |
|-----------------|-------------------------|-----------------------------------|------------------------|
| `game-commands` | Broadcast               | Engine                            | `sessionId`            |
| `game-events`   | Engine                  | Broadcast, History                | `sessionId`            |
| `lobby-events`  | Lobby                   | (other interested services)       | `sessionId`            |

Always key by `sessionId` so all messages for a game land in the same partition (preserves order).

### Redis key schema

Prefix: `game:{sessionId}:`

| Key              | Type   | Contents |
|------------------|--------|----------|
| `meta`           | Hash   | `{ status, turn, startedAt }` |
| `player:{id}`    | Hash   | `{ name, resources, totalUnits }` |
| `node:{id}`      | Hash   | `{ owner, garrison, fortification }` |
| `edges`          | Set    | `"nodeA:nodeB"` adjacency markers |
| `static:nodes`   | Hash   | Map coordinates & display names (loaded at startup) |

All mutating game actions are implemented as **Lua scripts** (`attack.lua`, `move.lua`, `reinforce.lua`) invoked from the Engine via Lettuce reactive client.

### Game actions (`com.fallout.core.enums.ActionType`)

`ATTACK`, `MOVE`, `REINFORCE`, `LEAVE` — see spec §3.5 for the REST + WebSocket API.

---

## Build & run

Each module is built and run independently. From the project root:

```bash
cd <module>        # e.g. cd engine
./gradlew bootRun  # starts the service
```

Other useful tasks: `./gradlew build`, `./gradlew test`.

**No root-level Gradle invocation works** — there is no parent build script.

For local infra (Postgres, Redis, Kafka, Eureka via Docker), a `docker-compose.yml` is planned but **not yet created** — see "Open work" below.

---

## Code conventions

- **Java 21** with toolchain enforcement in every module's `build.gradle`.
- **Lombok** everywhere — do not hand-write getters/setters/builders; use `@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.
- **Reactive-first**: every Spring Boot service uses WebFlux + Netty (no servlet stack anywhere). Use `Mono`/`Flux`, `ReactiveRedis*`, `@KafkaListener` with reactive templates.
- **Package root**: `com.fallout.<module>` (e.g. `com.fallout.lobby`, `com.fallout.core`).
- **Group**: `com.fallout` in every `build.gradle`; versions `0.0.1-SNAPSHOT` (core/gateway are `0.0.1`).
- **Config format**: prefer YAML; `lobby` currently uses `application.properties` (format inconsistency to clean up).
- **Logging**: include `sessionId` via MDC on every Kafka handler and WebSocket message.

When adding new code, match the density, naming, and idioms of any existing non-stub file in the same module.

---

## Current implementation status

The codebase is **mostly scaffolding**. Don't assume completed functionality exists — verify before answering "where is X implemented?".

| Area                              | Status |
|-----------------------------------|--------|
| `core` enums (`ActionType`, `MapPointsType`, `GameStatus`) | ✅ Defined, with Russian Javadoc on `MapPointsType` |
| `core` model classes (`Player`, `Node`, `MapPoint`, `Edge`) | ⚠️ Empty class shells |
| `core` Kafka command/event classes | ⚠️ Skeletons; only `AttackCommand` has a real constructor |
| Service `@SpringBootApplication` classes | ✅ All 6 services compile |
| REST controllers, WebSocket handlers, Kafka listeners, Redis repos | ❌ None |
| Lua scripts (`attack.lua`, `move.lua`, `reinforce.lua`) | ❌ None |
| Liquibase changelogs              | ❌ None |
| `docker-compose.yml`, Dockerfiles | ❌ None |
| Eureka / Kafka / Redis / Postgres connection config | ❌ Only `spring.application.name` is set everywhere |
| Tests                              | ⚠️ Each Spring Boot module has one empty `*ApplicationTests.contextLoads()`; `core` has none |

---

## Open work — Sprint 0 priorities

In order of dependency, before any gameplay work:

1. **`docker-compose.yml` at project root** with PostgreSQL, Redis, Kafka (Zookeeper or KRaft), Eureka Server.
2. **Wire cross-module dependency** from each service onto `core` (currently each `build.gradle` is isolated — add `implementation project(':core')` after converting to a composite build, or use local file dependencies).
3. **Connection settings** in each `application.yaml`: Eureka URL (`http://localhost:8761/eureka/`), Kafka bootstrap servers, Redis host/port, Postgres R2DBC URL, Liquibase changelog location.
4. **Liquibase changelogs** under `lobby/src/main/resources/db/changelog/` and `history/src/main/resources/db/changelog/`.
5. **Lua scripts** under `engine/src/main/resources/lua/` (attack/move/reinforce). Load by SHA via `RedisScript.of(...)`.
6. **Domain models** in `core`: flesh out `Player`, `Node`/`MapPoint`, `Edge`, command/event classes.
7. **Engine Lua executor** (reactive Lettuce), `RedisGameRepository`, concurrency tests with Testcontainers.

After Sprint 0 → follow the sprint plan in `fallout-wasteland-battle.md` §4 (Sprint 1: pure core + Redis tests; Sprint 2: Lobby REST; Sprint 3: WebSocket without Kafka; Sprint 4: introduce Kafka; Sprint 5: Gateway + JWT + UI; Sprint 6: history, metrics, polish).

---

## Test status

- **Placeholder tests only**: each Spring Boot module has a default `*ApplicationTests` with an empty `contextLoads()`.
- **`core` has no tests.**
- **Planned** (Sprint 1): JUnit 5 + Testcontainers (Redis, Kafka, Postgres) for atomicity/concurrency verification on Engine; spec recommends simulating 4 concurrent players with `wrk` or JMeter for load.
- When writing tests, follow the module's chosen dependency — `core` uses `junit-jupiter` already.

---

## Reference docs

- [`fallout-wasteland-battle.md`](fallout-wasteland-battle.md) — **authoritative spec**: project description, full stack table, microservice architecture, Kafka topology, Redis schema, REST/WebSocket API, and the complete 6-sprint roadmap.
- `Диаграмма без названия.drawio` — visual architecture diagram (User → Gateway → Eureka → backends; Kafka topics; Redis; Postgres; KeyCloak — *KeyCloak is a planned future auth component, not yet present*).
- Per-module `HELP.md` files — auto-generated Spring Boot reference links (not maintained by hand).

---

## Tooling notes

- **IDE**: IntelliJ IDEA (`.idea/` folder present). Only `gateway` is currently registered in `.idea/modules.xml` — open other modules via Gradle import.
- **Amplicode JPA plugin** is installed (`.idea/amplicode-jpa.xml`). Useful for the `lobby` and `history` R2DBC entities when those are added.
- **Java**: 21 (set toolchain). **Gradle**: 9.5.1 (each module has its own wrapper).
- **No Docker tooling yet** — install Docker Desktop or OrbStack for local infra later.
