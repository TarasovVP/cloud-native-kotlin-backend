# Cloud-Native Kotlin Backend

REST backend written in Kotlin with Ktor and PostgreSQL. The server is packaged as a Docker container and designed to run on Google Cloud Run with a Cloud SQL for PostgreSQL database.

The project demonstrates backend development, layered server architecture, relational data persistence, dependency injection, containerization and deployment on Google Cloud.

## Backend architecture

```text
KMP / web / HTTP clients
          │
          │ JSON over HTTP
          ▼
Google Cloud Run
┌─────────────────────────────────────────┐
│ Ktor + Netty                            │
│                                         │
│ Routes                                  │
│   └── DemoObjectService                 │
│         └── SQLDelight queries          │
│               └── HikariCP              │
└───────────────────┬─────────────────────┘
                    │ Cloud SQL Socket Factory / JDBC
                    ▼
          Cloud SQL for PostgreSQL
```

The request flow is:

1. Ktor routes receive and deserialize HTTP requests.
2. Response mappers convert JSON DTOs into domain models and back.
3. `DemoObjectService` contains the application operations.
4. SQLDelight generates type-safe PostgreSQL queries.
5. HikariCP manages the JDBC connection pool.
6. Koin creates and connects the server components.

The database schema is created during application startup if it does not already exist.

## Backend module

```text
server/
├── Dockerfile
├── build.gradle.kts
└── src/main/
    ├── kotlin/com/vnteam/architecturetemplates/
    │   ├── Application.kt                 # Netty server and Ktor plugins
    │   ├── Constants.kt                   # default server/database configuration
    │   ├── http/Routes.kt                 # REST endpoints
    │   ├── demo_object_service/            # service interface and implementation
    │   ├── db/DatabaseDriverFactory.kt     # HikariCP and JDBC configuration
    │   ├── di/SeverModule.kt               # Koin dependency graph
    │   └── mapperimpls/                    # DTO/domain mapping
    ├── sqldelight/.../ServerDatabase.sq   # PostgreSQL schema and queries
    └── resources/logback.xml              # server logging
```

The `core` module contains serializable DTOs and domain models shared by the server and its Kotlin Multiplatform clients. The remaining application modules are API consumers and examples; they are not part of the deployed backend process.

## REST API

| Method | Endpoint | Purpose | Successful response |
|---|---|---|---|
| `POST` | `/demoObjects` | Insert or update a list of objects | `201 Created` |
| `GET` | `/demoObjects` | Return all objects | `200 OK` with JSON array |
| `GET` | `/demoObjects/{id}` | Return one object | `200 OK`, or `404 Not Found` |
| `DELETE` | `/demoObjects/{id}` | Delete one object | `200 OK` |

`POST /demoObjects` performs an upsert: a row with the same `demoObjectId` is updated instead of duplicated.

Example request body:

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
| Language | Kotlin 2.0.20, JVM 17 |
| HTTP server | Ktor 2.3.12 with Netty |
| Serialization | kotlinx.serialization |
| Dependency injection | Koin 4.0 |
| Database | PostgreSQL 42.7.3 |
| Queries and schema | SQLDelight 2.0.2, PostgreSQL dialect |
| Connection pooling | HikariCP 5.0.1 |
| Cloud database connection | Google Cloud SQL PostgreSQL Socket Factory |
| Packaging | Gradle fat JAR and Docker |
| Runtime | Google Cloud Run |

## Configuration

The backend reads its runtime settings from environment variables:

| Variable | Required | Description |
|---|---|---|
| `PORT` | No | HTTP port; defaults to `8080` |
| `JDBC_URL` | Yes for a real deployment | PostgreSQL JDBC URL or Cloud SQL Socket Factory URL |
| `DRIVER_CLASS_NAME` | No | Defaults to `org.postgresql.Driver` |
| `DB_USER_NAME` | Yes | PostgreSQL user |
| `DB_USER_PASSWORD` | Yes | PostgreSQL password |

Do not commit real credentials. Configure production values as Cloud Run environment variables or secrets.

Example local JDBC URL:

```text
jdbc:postgresql://localhost:5432/demo_objects
```

Example Cloud SQL JDBC URL:

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

The API is then available at `http://localhost:8080`.

## Build and run the container

Build the fat JAR first, then build Docker from the repository root because the Dockerfile copies the server artifact from `server/build`.

```bash
./gradlew :server:fatJar
docker build -f server/Dockerfile -t cloud-native-kotlin-backend .

docker run --rm -p 8080:8080 \
  -e JDBC_URL='jdbc:postgresql://host.docker.internal:5432/demo_objects' \
  -e DB_USER_NAME='postgres' \
  -e DB_USER_PASSWORD='change-me' \
  cloud-native-kotlin-backend
```

## Google Cloud deployment

The deployed infrastructure consists of:

- **Cloud Run** — runs the stateless Ktor container and provides the public HTTPS endpoint;
- **Cloud SQL for PostgreSQL** — stores application data;
- **Cloud SQL Socket Factory** — lets the JVM application connect to the managed database;
- **Artifact Registry / container image** — stores the deployable server image;
- **Cloud Build** — builds and deploys new revisions when configured for the project.

For production, the Cloud Run service needs access to the Cloud SQL instance and the database credentials should be supplied through Secret Manager or protected environment configuration.

## Current scope and limitations

This is a demonstration backend rather than a production-ready public API. The current implementation has no authentication or authorization, allows CORS from any host, and uses broad exception handling in its routes. Production hardening would include authentication, request validation, structured error responses, database migrations, health checks, automated tests, restricted CORS and secret management.

## Project origin

The backend was originally developed in 2024 in the `kmp_with_server_implementation` branch of [Android Architecture Templates](https://github.com/TarasovVP/Android-Architecure-Templates). It was extracted into this standalone repository in 2026 so the backend and Google Cloud implementation can be presented independently while retaining the original development history.

Related articles:

- [KMP: Server Implementation](https://medium.com/@vptarasov/kmp-server-implementation-49a0a4389b34)
- [Deploying a KMP Server on Google Cloud: Step-by-Step Guide](https://medium.com/@vptarasov/deploying-a-kmp-server-on-google-cloud-step-by-step-guide-f32ade8c4be3)

## License

Copyright 2023 Volodymyr Tarasov. Licensed under the [Apache License 2.0](LICENSE).
