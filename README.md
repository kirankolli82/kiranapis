# musicapi

A small Spring Boot demo application that fetches stock quote data and exposes a simple HTTP API to return quotes. The project also uses jOOQ for database code generation and Flyway for database migrations. Testcontainers are used in the Gradle build to provide a PostgreSQL instance for jOOQ generation and tests.

## What this project does

- Exposes a REST endpoint to fetch stock quotes (`GET /quotes`).
- Delegates external stock-data lookups to a REST client (`QuoteClient`) that calls an external stock-data provider.
- Uses Flyway for database migrations (SQL migration files live under `src/main/resources/db/migration`).
- Uses jOOQ to generate Java classes from the database schema. The generated sources are placed under `build/generated-src/jooq` and compiled with the project.

## Project structure (relevant parts)

- `src/main/java/com/kiran/musicapi` - application code
  - `MusicapiApplication.java` - Spring Boot entrypoint
  - `config/` - Spring configuration classes and properties
  - `stockdata/api/` - client interfaces and DTOs used to call and model the third-party stock-data API
    - `resources/StockDataResource.java` - exposes `GET /quotes`
    - `client/QuoteClient.java` - JAX-RS-style interface for the remote API
    - `contract/` - DTOs: `StockApiResponse`, `Quote`, `Meta`
- `src/main/resources/db/migration` - Flyway SQL migrations
- `build.gradle` - Gradle build logic: testcontainers + Flyway + jOOQ integration

## API

### GET /quotes
- Description: Returns a `StockApiResponse` containing metadata and a map of ticker symbols to `Quote` objects.
- Path: `/quotes`
- Produces: application/json
- Example (simplified):
  ```json
  {
    "meta": { /* ... */ },
    "data": {
      "NVDA": { "ticker": "NVDA", "price": 420.12, "volume": 123456, /* ... */ },
      "AAPL": { /* ... */ }
    }
  }
  ```

The `StockDataResource` currently requests the symbols `NVDA,MSFT,AAPL` by default. The client wiring uses `QuoteClient` and `QuoteClientRequestFilter` to attach the configured API token.

## Configuration

Application-level configuration is in `src/main/resources/application.properties`.
Key properties:
- `server.port` — HTTP port for the Spring Boot app (default `8888` in the repo)
- `quote-client.base-url` — base URL for the external stock-data API
- `spring.datasource.*` — defaults for a local Postgres instance. During jOOQ generation the Gradle script starts a Testcontainers Postgres and points jOOQ / Flyway at that container.

Sensitive values (like the external API token) should be provided via environment variables or an external properties file in production.

## Generating jOOQ classes during build

This project invokes a Testcontainers PostgreSQL instance from the Gradle script and runs Flyway migrations before jOOQ generation. The generated sources are written into `build/generated-src/jooq` and added to the `main` source set so `compileJava` includes them.

By default the `generateJooq` step is wired into the build but may be gated behind a Gradle project property. To force jOOQ generation during the compile phase use the following command from the project root (Windows PowerShell):

```powershell
cd /d D:\Work\Java\musicapi\musicapi
./gradlew.bat clean classes -PgenerateJooq=true
```

Notes & troubleshooting when generating jOOQ:
- The Gradle script starts a PostgreSQL Testcontainer and runs Flyway migrations inside the Gradle JVM. If Flyway complains it cannot handle the JDBC URL ("No database found to handle jdbc:postgresql://..."), it usually means the PostgreSQL JDBC driver is not available to the Gradle buildscript classpath. Ensure `org.postgresql:postgresql` is present on the buildscript/plugin classpath. The repo already includes this in the `build.gradle` buildscript classpath.
- If you change buildscript classpath dependencies, stop the Gradle daemon before re-running to ensure the daemon picks up new classpath entries:

```powershell
./gradlew.bat --stop
./gradlew.bat clean classes -PgenerateJooq=true --no-daemon -i
```

- If jOOQ generation fails, inspect the Gradle output for the printed `jdbc url:` line (the Gradle script prints the Testcontainer JDBC URL before running Flyway). Use that URL to confirm connectivity with external tools if needed.

## Common commands

- Build and run tests:
```powershell
./gradlew.bat clean test
```

- Run the application locally (after building):
```powershell
./gradlew.bat bootRun
```

- Generate jOOQ classes and compile (explicit property):
```powershell
./gradlew.bat clean classes -PgenerateJooq=true
```

## Development tips

- Keep `application.properties` values for local development, but use environment variables or secret stores for tokens and production DB credentials.
- The application now fetches the StockData API token from Google Cloud Secret Manager with automatic caching. See `SECRETS_MANAGER_SETUP.md` for detailed setup instructions and caching configuration.
- **Troubleshooting Secret Manager issues?** See `TROUBLESHOOTING_SECRET_MANAGER.md` for detailed debugging steps and solutions.
- For local testing, authenticate with `gcloud auth application-default login` and ensure you have access to the secret in GCP.
- Configure `GCP_PROJECT_ID` and `API_TOKEN_SECRET_ID` environment variables for your environment.
- Enable diagnostics by setting `gcp.diagnostics.enabled=true` in `application.properties` to debug authentication issues.
- When debugging Flyway/jOOQ in the Gradle script, enable Gradle info/trace logging to view classpath and Flyway logs (`-i` or `--stacktrace` / `--debug`).

## Where to look next
- SQL migrations: `src/main/resources/db/migration`
- jOOQ configuration: `build.gradle` (the `jooq` block and the `tasks.withType(nu.studer.gradle.jooq.JooqGenerate)` configuration)
- REST client wiring: `config/StockdataConfig.java` and `stockdata/api/client`
- Secret Manager setup: `SECRETS_MANAGER_SETUP.md`

If you want, I can:
- Add example cURL commands for the `/quotes` endpoint.
- Run the Gradle jOOQ generation task here and paste logs (if you want me to run it).

---

Generated on 2025-10-13.