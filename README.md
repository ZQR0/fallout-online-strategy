# Fallout: Битва за Пустошь

> Реактивная многопользовательская стратегия с захватом территорий на карте вселенной Fallout.

Reactive real-time multiplayer strategy built on **Spring Boot microservices**. Up to **4 players** per session, each controlling a faction, moving squads between iconic locations (Vault 101, Megaton, Tenpenny Tower, …) and battling for control of the wasteland.

The map is a static graph of nodes overlaid on a background image — players click a node to issue commands (`attack`, `move`, `reinforce`). State lives in Redis under atomic Lua scripts; events flow through Kafka; the browser talks to the cluster over WebSocket through a single gateway.

The project is a portfolio-ready demo of **reactive programming, event-driven architecture, Redis as a state store, and microservice patterns** in Java.

Full design and sprint plan — [`fallout-wasteland-battle.md`](fallout-wasteland-battle.md) (authoritative spec).
Architecture diagram — `Диаграмма без названия.drawio`.
AI-assistant context — [`CLAUDE.md`](CLAUDE.md).

---

## ✨ Features

- **Real-time gameplay** — every command is applied instantly through WebSocket; no turn-based delays.
- **Atomic game logic** — all mutating actions (attack, move, reinforce) execute as **Lua scripts in Redis**, guaranteeing correctness under concurrent commands from 4 players.
- **Event-driven backbone** — commands and state deltas propagate through **Kafka** (keyed by `sessionId` for per-game ordering), so the engine and broadcaster scale independently.
- **Reactive end-to-end** — Spring WebFlux + Netty across every service. No servlet stack anywhere.
- **Service discovery** — every backend registers in **Eureka** and is reached through a single **Spring Cloud Gateway**.
- **Persistent history** — all game events are archived to **PostgreSQL** for replays and stats.
- **Horizontally scalable** — partition by `sessionId` in Kafka; run as many engine/broadcaster instances as you need.

---

## 🧱 Tech stack

| Layer | Technology |
|---|---|
| Language | **Java 21** (toolchain enforced) |
| Framework | **Spring Boot 4.1.0** + **Spring WebFlux** (Netty, no servlet stack) |
| Microservice infra | **Spring Cloud 2025.1.2** (Gateway, Netflix Eureka) |
| Messaging | **Apache Kafka** (key = `sessionId`, per-game partitioning) |
| State store | **Redis** + **Lettuce** reactive client + atomic Lua scripts |
| Relational DB | **PostgreSQL** (R2DBC + JDBC, Liquibase) |
| WebSocket | **Spring WebFlux WebSocket** |
| Build | **Gradle 9.5.1** (one wrapper per module) |
| Tooling | **Lombok**, Amplicode JPA, Testcontainers (planned) |
| Container | **Docker / Docker Compose** for local infra |

---

## 🗂 Module layout

This repo is **not** a single multi-module Gradle build. It is **7 independent Gradle subprojects at the root**, each with its own `build.gradle`, `settings.gradle`, and `gradlew`. There is no parent `build.gradle` or root `settings.gradle` — cross-module wiring is on the roadmap.

| Module      | Type             | Port  | Responsibility                                                            |
|-------------|------------------|-------|---------------------------------------------------------------------------|
| `core`      | `java-library`   | —     | Shared domain: enums, models, Kafka command/event classes                |
| `discovery` | Spring Boot app  | 8761  | **Eureka Server** — service registry                                      |
| `gateway`   | Spring Boot app  | 8080  | **API Gateway** (WebFlux, Netty) — routes REST + WebSocket via Eureka     |
| `lobby`     | Spring Boot app  | 8081  | **Session lifecycle**: create/join, init Redis on game start, persist to PG |
| `engine`    | Spring Boot app  | 8082  | **Game core**: Kafka consumer for commands, executes Lua scripts in Redis |
| `broadcast` | Spring Boot app  | 8090  | **WebSocket fan-out**: keeps `sessionId → ws sessions`, consumes `game-events` |
| `history`   | Spring Boot app  | 8083  | **Event archive**: consumes `game-events`, writes to PG for replays       |

**Group**: `com.fallout` in every `build.gradle`. Versions `0.0.1-SNAPSHOT` (core/gateway are `0.0.1`).

---

## 🏗 Architecture

```
[Browser]
   │  WebSocket (raw or STOMP)
   ▼
[Gateway :8080] ──► [Eureka :8761]
   │
   ├─ REST  /lobby/**        → [Lobby :8081]   ──► PostgreSQL (WAITING sessions)
   │                              └─► Redis (init session state on start)
   │                              └─► Kafka lobby-events  (SESSION_STARTED)
   │
   ├─ WS    /ws/**           → [Broadcast :8090]
   │                              └─► Kafka game-commands (commands from clients)
   │
   └─ (engine + history resolve via Eureka)

[Engine :8082]  ← Kafka game-commands
                  └─► Lua scripts in Redis  (atomic state mutations)
                  └─► Kafka game-events     (state deltas, battle reports, finish)

[Broadcast :8090]  ← Kafka game-events  ──► WebSocket push to clients

[History :8083]   ← Kafka game-events   ──► PostgreSQL (replay storage)
```

### Kafka topics

| Topic           | Producer    | Consumer(s)            | Key (for partitioning) |
|-----------------|-------------|------------------------|------------------------|
| `game-commands` | Broadcast   | Engine                 | `sessionId`            |
| `game-events`   | Engine      | Broadcast, History     | `sessionId`            |
| `lobby-events`  | Lobby       | (other interested svcs)| `sessionId`            |

> **Always key by `sessionId`** — all messages for a game land in the same partition, preserving order.

### Redis key schema

Prefix: `game:{sessionId}:`

| Key              | Type | Contents |
|------------------|------|----------|
| `meta`           | Hash | `{ status, turn, startedAt }` |
| `player:{id}`    | Hash | `{ name, resources, totalUnits }` |
| `node:{id}`      | Hash | `{ owner, garrison, fortification }` |
| `edges`          | Set  | `"nodeA:nodeB"` adjacency markers |
| `static:nodes`   | Hash | Map coordinates & display names (loaded at startup) |

Every mutating action is a **Lua script** (`attack.lua`, `move.lua`, `reinforce.lua`) invoked from the Engine via the reactive Lettuce client.

### Game actions (`com.fallout.core.enums.ActionType`)

`ATTACK`, `MOVE`, `REINFORCE`, `LEAVE` — see `fallout-wasteland-battle.md` §3.5 for the REST + WebSocket API.

---

## 🚀 Quick start

> Each module is built and run independently. **There is no root-level Gradle invocation** — no parent `build.gradle` exists.

### Prerequisites

- **JDK 21** (toolchain-pinned, install via [SDKMAN](https://sdkman.io/) or Homebrew)
- **Docker** (Docker Desktop or OrbStack) for PostgreSQL, Redis, Kafka, Eureka
- A terminal per service you want to run

### 1. Start the infrastructure

A `docker-compose.dev.yaml` is provided at the project root:

```bash
docker compose -f docker-compose.dev.yaml up -d
```

It brings up PostgreSQL, Redis, Kafka (KRaft), and the Eureka discovery service.

### 2. Run a service

From the project root:

```bash
cd <module>            # e.g. cd engine
./gradlew bootRun      # starts the service
```

Other useful tasks: `./gradlew build`, `./gradlew test`.

### 3. Boot order (recommended)

1. **`discovery`** (`./gradlew bootRun`) — Eureka must be up first so other services can register.
2. **`lobby`**, **`engine`**, **`broadcast`**, **`history`** — any order, they will discover each other.
3. **`gateway`** — the only service the browser talks to.

Default ports:

| Service     | URL                       |
|-------------|---------------------------|
| Gateway     | http://localhost:8080     |
| Discovery   | http://localhost:8761     |
| Lobby REST  | http://localhost:8081     |
| Engine      | http://localhost:8082     |
| Broadcast   | ws://localhost:8090/ws    |
| History     | http://localhost:8083     |

---

## 🛠 Development

### Code conventions

- **Java 21** — toolchain enforced in every `build.gradle`.
- **Lombok everywhere** — never hand-write getters/setters/builders; use `@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.
- **Reactive-first** — every Spring Boot service is WebFlux + Netty. Use `Mono`/`Flux`, `ReactiveRedis*`, `@KafkaListener` with reactive templates.
- **Package root** — `com.fallout.<module>` (e.g. `com.fallout.lobby`, `com.fallout.core`).
- **Config format** — prefer YAML; `lobby` currently uses `application.properties` (known inconsistency to clean up).
- **Logging** — include `sessionId` via **MDC** on every Kafka handler and WebSocket message.

When adding new code, match the density, naming, and idioms of any existing non-stub file in the same module.

### Open work — Sprint 0 priorities

In order of dependency, before any gameplay work:

1. **`docker-compose.yml`** at project root with PostgreSQL, Redis, Kafka (KRaft or Zookeeper), Eureka Server. *(WIP: `docker-compose.dev.yaml` exists.)*
2. **Wire cross-module dependency** from each service onto `core` (each `build.gradle` is currently isolated).
3. **Connection settings** in each `application.yaml`: Eureka URL, Kafka bootstrap servers, Redis host/port, Postgres R2DBC URL, Liquibase changelog location.
4. **Liquibase changelogs** under `lobby/src/main/resources/db/changelog/` and `history/src/main/resources/db/changelog/`.
5. **Lua scripts** under `engine/src/main/resources/lua/` (`attack.lua`, `move.lua`, `reinforce.lua`). Load by SHA via `RedisScript.of(...)`.
6. **Domain models** in `core` — flesh out `Player`, `Node`/`MapPoint`, `Edge`, command/event classes.
7. **Engine Lua executor** (reactive Lettuce), `RedisGameRepository`, concurrency tests with Testcontainers.

After Sprint 0 → follow the 6-sprint roadmap in `fallout-wasteland-battle.md` §4 (Sprint 1: pure core + Redis tests; Sprint 2: Lobby REST; Sprint 3: WebSocket without Kafka; Sprint 4: introduce Kafka; Sprint 5: Gateway + JWT + UI; Sprint 6: history, metrics, polish).

### Current implementation status

The codebase is **mostly scaffolding**. Don't assume completed functionality exists — verify before answering "where is X implemented?".

| Area                                                   | Status |
|--------------------------------------------------------|--------|
| `core` enums (`ActionType`, `MapPointsType`, `GameStatus`, `KafkaEventType`) | ✅ Defined, Russian Javadoc on `MapPointsType` |
| `core` model classes (`Player`, `Node`, `MapPoint`, `Edge`) | ⚠️ Empty class shells |
| `core` Kafka command/event classes                     | ⚠️ Skeletons; only `AttackCommand` has a real constructor |
| Service `@SpringBootApplication` classes               | ✅ All 6 services compile |
| REST controllers, WebSocket handlers, Kafka listeners, Redis repos | ❌ None |
| Lua scripts (`attack.lua`, `move.lua`, `reinforce.lua`) | ❌ None |
| Liquibase changelogs                                   | ❌ None |
| `docker-compose.yml` (root), Dockerfiles               | ⚠️ `docker-compose.dev.yaml` exists; no Dockerfiles yet |
| Eureka / Kafka / Redis / Postgres connection config    | ❌ Only `spring.application.name` is set everywhere |
| Tests                                                  | ⚠️ Each Spring Boot module has one empty `*ApplicationTests.contextLoads()`; `core` has none |

---

## 🧪 Testing

- **Placeholder tests only** today — each Spring Boot module ships a single `*ApplicationTests.contextLoads()`; `core` has none.
- **Planned** (Sprint 1): JUnit 5 + Testcontainers (Redis, Kafka, Postgres) for atomicity/concurrency verification on the Engine; the spec recommends `wrk` or JMeter for 4-player load.
- When writing tests, follow the module's chosen dependency — `core` uses `junit-jupiter` already.

Run a module's tests:

```bash
cd <module>
./gradlew test
```

---

## 📁 Project tree

```
fallout/
├── CLAUDE.md                       # AI-assistant context
├── README.md                       # ← you are here
├── fallout-wasteland-battle.md     # Authoritative spec
├── Диаграмма без названия.drawio   # Architecture diagram (draw.io)
├── docker-compose.dev.yaml         # Local infra (PG, Redis, Kafka, Eureka)
├── .env                            # Local infra credentials
│
├── core/                           # java-library — shared enums, models, DTOs
├── discovery/                      # Spring Boot — Eureka Server
├── gateway/                        # Spring Boot — API Gateway (WebFlux)
├── lobby/                          # Spring Boot — session lifecycle
├── engine/                         # Spring Boot — game core (Redis + Lua)
├── broadcast/                      # Spring Boot — WebSocket fan-out
└── history/                        # Spring Boot — event archive
```

---

## 📚 Reference docs

- **[`fallout-wasteland-battle.md`](fallout-wasteland-battle.md)** — authoritative spec: full stack table, microservice architecture, Kafka topology, Redis schema, REST/WebSocket API, and the 6-sprint roadmap.
- **`Диаграмма без названия.drawio`** — visual architecture diagram (Browser → Gateway → Eureka → backends; Kafka topics; Redis; Postgres; planned KeyCloak).
- **[`CLAUDE.md`](CLAUDE.md)** — AI-assistant context (architecture summary, module map, conventions, sprint-0 priorities).
- Per-module `HELP.md` files — auto-generated Spring Boot reference links.

---

## 🧰 Tooling notes

- **IDE**: IntelliJ IDEA (`.idea/` folder present). Only `gateway` is currently registered in `.idea/modules.xml` — open other modules via **Gradle import**.
- **Amplicode JPA plugin** is installed (`.idea/amplicode-jpa.xml`). Useful for the `lobby` and `history` R2DBC entities when those are added.
- **Java**: 21 (toolchain-pinned). **Gradle**: 9.5.1 (each module has its own wrapper).
- **No Docker tooling yet?** Install **Docker Desktop** or **OrbStack** to run the local infra.
