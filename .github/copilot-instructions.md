# Copilot project instructions — musicapi

Purpose
- Provide Copilot with concise context about this Java/Gradle project and a small set of coding patterns and rules to follow when generating code.
- This is a lightweight starting point; we'll iterate on it together.

Project overview
- Language: Java (Gradle build).
- Root package: com.kiran.stockapi
- Main source: src/main/java
- Integration tests: src/integrationTest/java (uses Testcontainers; e.g. PostgresContainer, KafkaContainer)
- Tests: src/test/java
- Build: use the Gradle wrapper (./gradlew or gradlew.bat on Windows)

High-level contract for generated code
- Inputs: follow existing method signatures and DTOs in the project; prefer explicit types over raw types.
- Outputs: return the same domain types used in the project (packages under com.kiran.stockapi).
- Error modes: prefer throwing checked exceptions for recoverable problems or IllegalArgumentException for invalid params; prefer Optional<T> for absent values when appropriate.
- Success criteria: code compiles with project's Gradle build and has at least one unit test or an integration test when it modifies runtime behavior.

Package structure for REST API classes
- Base package: com.kiran.stockapi.<feature|datasource>.api
  - <feature|datasource>: the feature name or data source (e.g., spotify, price, catalog)
- Within the API package:
  - client: REST client classes that call external APIs
  - resources: REST resources/endpoints (controllers)
  - contract: POJOs representing request and response DTOs
- Example: For a Spotify integration:
  - com.kiran.stockapi.spotify.api.client (SpotifyClient)
  - com.kiran.stockapi.spotify.api.resources (SpotifyResource)
  - com.kiran.stockapi.spotify.api.contract (SpotifyTrackRequest, SpotifyTrackResponse)

Coding patterns & style (baseline)
- Follow Google Code Sytle for Java (https://google.github.io/styleguide/javaguide.html) unless overridden here.
- When creating java POJOs prefer java Record classes unless the POJO contains BigDecimal or ZonedDateTime fields.
- All record classes must have Lombok @Builder annotation.
- All POJO classes must have Lombok @Builder, @Getter, @ToString, and @EqualsHashCode annotations.
- If a Java POJO has BigDecimal or ZonedDateTime fields, also include @AllArgsConstructor and use @Builder with toBuilder parameter.
- Further to the above point the BigDecimal fields must be excluded from EqualsAndHashCode using Lombok's @EqualsAndHashCode.Exclude and instead add a getter method that returns the BigDecimal with stripped trailing zeros(if not null). 
  This method must be added to EqualsAndHashCode using Lombok's @EqualsAndHashCode.Include annotation. Add a jackson JsonIgnore annotation to this method to avoid serialization issues.
- Similarly ZonedDateTime fields  must be excluded from EqualsAndHashCode using Lombok's @EqualsAndHashCode.Exclude and instead add a getter method that returns Instant fields.
  This method must be added to EqualsAndHashCode using Lombok's @EqualsAndHashCode.Include annotation. Add a jackson JsonIgnore annotation to this method to avoid serialization issues.
- Prefer immutability: prefer final fields, return unmodifiable collections, use builders for complex DTOs.
- Keep methods small (<= ~100 lines) and single-responsibility.
- Prefer SLF4J logging (logger.debug/info/warn/error) over System.out/err.
- When using Lambdas keep them smaller than 5 lines. If its getting bigger consider extracting to a named method.
- Avoid nulls where possible; prefer Optional<T> for nullable returns.
- Use try-with-resources for I/O and JDBC resources.
- Avoid hard-coded credentials, URLs, or secrets. Read configuration from application.properties or environment variables.
- Do not introduce new external dependencies without confirming via a PR; prefer standard JDK and existing project libraries.
- Keep imports minimal and avoid unused imports.

Tests & integration
- New features must include unit tests in src/test/java. For infra-related changes (DB, Kafka), include integration tests in src/integrationTest/java and reuse existing Testcontainers setup.
- When creating containers use the project's existing container classes (e.g. PostgresContainer, KafkaContainer) and testcontainers properties under src/integrationTest/resources.
- All JUnit tests must have @DisplayName annotation describing what the unit test does in a concise manner.
- Split each unit test into given, when, then sections with clear javadocs (// Given:, // When:, // Then:) explaining what each section attempts to do.
- Add javadoc on top of each unit test class that describes in tabular format (markdown style, non-HTML) the list of tests being performed and expected results.

Build & verification
- Do not run verification commands yourself and instead ask the user to run them.


Pull requests & commits
- Keep changes small and focused; include a short description and testing steps in PR description.
- Commit message format: <area>: short description — e.g. service: add price fetcher

Do/Don't quick list
- Do follow existing code style and package layout.
- Do add tests for new behavior.
- Do reuse existing utilities and configuration.
- Don't add secrets or hard-coded env values.
- Don't change public APIs without bumping version and adding migration notes.

How Copilot should behave when generating code
- Prefer conservative, small changes that match the project's style.
- When in doubt, create a TODO or comment referencing a specific file or config rather than guessing (e.g. "// TODO: confirm DB schema name matches src/main/resources/db/migration").
- If adding dependencies or making broad structural changes, include a short explanation and include/update tests and build config changes.

How to extend this file
- Add specific linter/formatter rules (Checkstyle/Spotless) here if/when they are introduced.
- Add examples of common patterns used in this repo (e.g. repository method signatures, DTO builders, exception classes).

Contact / maintainer notes
- Primary package owner: com.kiran.stockapi
- Use existing tests under build/reports/tests for examples of test structure and naming.

---
This is a starter file — tell me what conventions or patterns you'd like to add or change and I'll update it.

