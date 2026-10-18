# Cloud-Native Kotlin Backend

[![CI](https://github.com/TarasovVP/cloud-native-kotlin-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/TarasovVP/cloud-native-kotlin-backend/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Ktor](https://img.shields.io/badge/Ktor-2.3.12-087CFA?logo=ktor&logoColor=white)](https://ktor.io/)
[![Google Cloud Run](https://img.shields.io/badge/Google_Cloud-Run-4285F4?logo=googlecloud&logoColor=white)](https://cloud.google.com/run)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A containerized REST backend built with Kotlin, Ktor, and PostgreSQL. The service is designed for Google Cloud Run, connects to Cloud SQL through the Cloud SQL Socket Factory, and preserves its original 2024 development history.

## What the project demonstrates

- a layered Kotlin backend with clear HTTP, service, storage, and database boundaries;
- RESTful CRUD operations with JSON serialization;
- type-safe PostgreSQL queries and schema management with SQLDelight;
- transaction-safe batch upserts;
- dependency injection with Koin;
- pooled JDBC connections with HikariCP;
- environment-based local and cloud configuration;
- a non-root Docker runtime suitable for Cloud Run;
- route and service tests that do not require a live database;
- continuous integration with GitHub Actions.

## Architecture

```text
HTTP clients
    │ JSON over HTTP
    ▼
Google Cloud Run
┌──────────────────────────────────────────────┐
│ Ktor + Netty                                 │
│                                              │
│ Routes                                       │
│   └── DemoObjectService                      │
│         └── DemoObjectStore                  │
│               └── SQLDelight queries         │
│                     └── HikariCP              │
└──────────────────────┬───────────────────────┘
                       │ JDBC / Cloud SQL Socket Factory
                       ▼
             Cloud SQL for PostgreSQL
```

The request flow is:

1. Ktor routes deserialize and validate incoming JSON.
2. Response mappers translate between API DTOs and domain models.
3. `DemoObjectService` enforces application-level requirements.
4. `DemoObjectStore` separates business logic from persistence.
5. The SQLDelight implementation executes type-safe PostgreSQL queries.
6. HikariCP manages the JDBC connection pool.

The database table is created during application startup when it does not already exist.

## Project structure

```text
.
├── core/
│   └── src/main/kotlin/com/tarasovvp/cloudnativebackend/
│       └── domain/                  # Domain models, API DTOs, mapper contracts
├── server/
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/tarasovvp/cloudnativebackend/
│       │   │   ├── Application.kt  # Netty entry point and Ktor plugins
│       │   │   ├── db/             # HikariCP / JDBC configuration
│       │   │   ├── di/             # Koin dependency graph
│       │   │   ├── http/           # REST routes
│       │   │   ├── mapper/         # DTO and database mapping
│       │   │   ├── service/        # Application operations
│       │   │   └── storage/        # Persistence boundary and SQLDelight store
│       │   ├── resources/          # Logback configuration
│       │   └── sqldelight/         # PostgreSQL schema and queries
│       └── test/                    # Route and service tests
├── .github/workflows/ci.yml         # Test, JAR, and Docker verification
└── gradle/                          # Version catalog and Gradle wrapper
```

Only the `server` module is deployed. The `core` module provides the models and contracts it uses.

## REST API

| Method | Endpoint | Purpose | Successful response |
|---|---|---|---|
| `POST` | `/demoObjects` | Insert or update a list of objects | `201 Created` |
| `GET` | `/demoObjects` | Return all objects | `200 OK` with JSON array |
| `GET` | `/demoObjects/{id}` | Return one object | `200 OK`, or `404 Not Found` |
| `DELETE` | `/demoObjects/{id}` | Delete one object | `200 OK` |

`POST /demoObjects` performs an upsert. A row with the same `demoObjectId` is updated instead of duplicated, and the complete batch is written in one transaction. Blank or missing IDs are rejected with `400 Bad Request`.

Example request:

```json
[
  {
    "demoObjectId": "sample-1",
    "name": "Sample object",
    "html_url": "https://example.com/sample-1",
    "description": "Example API object",
    "owner": {
      "login": "demo-user",
      "id": "42",
      "avatar_url": "https://example.com/avatar.png",
      "url": "https://example.com/users/demo-user"
    }
  }
]
```

## Technology stack

| Area | Technology |
|---|---|
| Language and runtime | Kotlin 2.0.20, JVM 17 |
| HTTP server | Ktor 2.3.12 with Netty |
| Serialization | kotlinx.serialization |
| Dependency injection | Koin 4.0 |
| Database | PostgreSQL 42.7.3 |
| Queries and schema | SQLDelight 2.0.2, PostgreSQL dialect |
| Connection pooling | HikariCP 5.0.1 |
| Cloud database connection | Google Cloud SQL PostgreSQL Socket Factory |
| Packaging | Gradle fat JAR and Docker |
| Runtime | Google Cloud Run |
| CI | GitHub Actions |

## Configuration

The service reads runtime settings from environment variables:

| Variable | Required | Description |
|---|---|---|
| `PORT` | No | HTTP port; defaults to `8080` |
| `JDBC_URL` | Yes for a real deployment | PostgreSQL JDBC URL or Cloud SQL Socket Factory URL |
| `DRIVER_CLASS_NAME` | No | Defaults to `org.postgresql.Driver` |
| `DB_USER_NAME` | Yes | PostgreSQL user |
| `DB_USER_PASSWORD` | Yes | PostgreSQL password |

Do not commit real credentials. Supply production values through Secret Manager or protected Cloud Run environment configuration.

Local JDBC URL example:

```text
jdbc:postgresql://localhost:5432/demo_objects
```

Cloud SQL JDBC URL example:

```text
jdbc:postgresql:///DATABASE_NAME?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory
```

## Run locally

Requirements: JDK 17+ and a reachable PostgreSQL database.

```bash
export JDBC_URL='jdbc:postgresql://localhost:5432/demo_objects'
export DB_USER_NAME='postgres'
export DB_USER_PASSWORD='change-me'

./gradlew :server:run
```

The API is available at `http://localhost:8080`.

## Tests and build

Run the route and service tests:

```bash
./gradlew :server:test
```

Build the executable JAR:

```bash
./gradlew :server:fatJar
java -jar server/build/libs/cloud-native-kotlin-backend.jar
```

The tests use an in-memory implementation of the storage boundary, so PostgreSQL credentials are not needed for the test task.

## Docker

Build the fat JAR first, then build from the repository root:

```bash
./gradlew :server:fatJar
docker build -f server/Dockerfile -t cloud-native-kotlin-backend .

docker run --rm -p 8080:8080 \
  -e JDBC_URL='jdbc:postgresql://host.docker.internal:5432/demo_objects' \
  -e DB_USER_NAME='postgres' \
  -e DB_USER_PASSWORD='change-me' \
  cloud-native-kotlin-backend
```

The image uses a Java 17 JRE and runs under an unprivileged user.

## Google Cloud deployment

The intended production infrastructure consists of:

- **Cloud Run** for the stateless Ktor container and public HTTPS endpoint;
- **Cloud SQL for PostgreSQL** for application data;
- **Cloud SQL Socket Factory** for the managed database connection;
- **Artifact Registry** for the container image;
- protected environment variables or **Secret Manager** for database credentials.

The repository's GitHub Actions workflow provides continuous integration: it runs tests, builds the executable JAR, and verifies the Docker image on every push to `main` and every pull request. Production deployment remains an explicit Google Cloud operation because project-specific credentials and resource names are not stored in the repository.

## Current scope

This repository is a focused demonstration backend, not a hardened public API. Before exposing it to untrusted clients, add authentication and authorization, restrict CORS, introduce versioned database migrations, add request limits and pagination, and expand integration and security testing.

## Project origin

This backend was originally built in 2024 as the [server component](https://github.com/TarasovVP/Android-Architecture-Templates/tree/kmp_with_server_implementation) of the [Android Architecture Templates](https://github.com/TarasovVP/Android-Architecture-Templates) project. It accompanies a two-part article series covering the Ktor implementation and its deployment to Google Cloud. In 2026, the backend was extracted into this standalone repository to make it easier to explore, build, and extend independently while preserving its original Git history.

Article series:

- [KMP: Server Implementation](https://medium.com/@vptarasov/kmp-server-implementation-49a0a4389b34)
- [Deploying a KMP Server on Google Cloud: Step-by-Step Guide](https://medium.com/@vptarasov/deploying-a-kmp-server-on-google-cloud-step-by-step-guide-f32ade8c4be3)

## License

Copyright 2024 Volodymyr Tarasov. Licensed under the [Apache License 2.0](LICENSE).
