<!--
Sync Impact Report - Constitution v1.0.0
========================================
Version Change: TEMPLATE → 1.0.0
Change Type: INITIAL - First concrete constitution replacing template placeholders
Modified Principles: All principles defined for first time
Added Sections: All sections populated with project-specific requirements
Removed Sections: None (template structure preserved)

Templates Requiring Updates:
✅ plan-template.md - Constitution Check section aligns with new principles
✅ spec-template.md - User scenarios and requirements align with testing principles
✅ tasks-template.md - Task categorization reflects new principle-driven types

Follow-up TODOs: None - all placeholders resolved

Date: 2026-03-01
-->

# MusicAPI Constitution

## Core Principles

### I. Code Quality & Style Standards

All code MUST adhere to the Google Java Style Guide unless explicitly overridden in this constitution. Code quality requirements:

- Follow explicit types over raw types in all method signatures and DTOs
- Prefer immutability: final fields, unmodifiable collections, builders for complex DTOs
- Keep methods small (≤100 lines) and single-responsibility
- Lambda expressions MUST be ≤5 lines; extract to named methods if larger
- Minimal imports; no unused imports
- No System.out/err usage

**Rationale**: Consistent style reduces cognitive load, improves maintainability, and prevents common defects through enforced immutability and bounded complexity.

### II. Type Safety & Null Handling (NON-NEGOTIABLE)

Null handling MUST follow strict conventions:

- Prefer `Optional<T>` for nullable returns; avoid returning null
- Throw `IllegalArgumentException` for invalid parameters
- Throw checked exceptions for recoverable problems
- Follow existing method signatures and domain types in `com.kiran.stockapi` packages

**Rationale**: Explicit null handling eliminates NullPointerExceptions and makes error modes visible in type signatures, improving reliability and API clarity.

### III. POJO & Record Standards

Data transfer objects and domain models MUST follow these patterns:

- Prefer Java **Record** classes unless the POJO contains `BigDecimal` or `ZonedDateTime` fields
- **All record classes** MUST have Lombok `@Builder` annotation
- **All POJO classes** MUST have Lombok `@Builder`, `@Getter`, `@ToString`, and `@EqualsHashCode` annotations
- POJOs with `BigDecimal` or `ZonedDateTime` fields MUST also include `@AllArgsConstructor` and use `@Builder(toBuilder = true)`
- `BigDecimal` fields MUST:
  - Be excluded from `EqualsAndHashCode` using `@EqualsAndHashCode.Exclude`
  - Include a getter method returning the BigDecimal with stripped trailing zeros (if not null)
  - Add that getter to `EqualsAndHashCode` using `@EqualsAndHashCode.Include`
  - Mark that getter with `@JsonIgnore` to avoid serialization issues
- `ZonedDateTime` fields MUST:
  - Be excluded from `EqualsAndHashCode` using `@EqualsAndHashCode.Exclude`
  - Include a getter method returning `Instant` representation
  - Add that getter to `EqualsAndHashCode` using `@EqualsAndHashCode.Include`
  - Mark that getter with `@JsonIgnore`

**Rationale**: Consistent DTO patterns ensure reliable serialization, value equality semantics, and builder-based immutability. Special handling for BigDecimal (precision) and ZonedDateTime (timezone) prevents subtle equality bugs.

### IV. Test-First Development (NON-NEGOTIABLE)

Tests are MANDATORY for all features modifying runtime behavior:

- New features MUST include unit tests in `src/test/java`
- Infrastructure changes (DB, Kafka) MUST include integration tests in `src/integrationTest/java`
- **All JUnit tests** MUST have `@DisplayName` annotation describing what the test does concisely
- **Each unit test** MUST be split into Given/When/Then sections with clear javadocs:
  - `// Given:` - explain initial state setup
  - `// When:` - explain action being tested
  - `// Then:` - explain expected outcome verification
- **Each test class** MUST have class-level javadoc in markdown table format (non-HTML) describing:
  - List of tests being performed
  - Expected results for each test
- Code compiles with project's Gradle build
- At least one test per behavioral change

**Rationale**: Test-first development catches regressions, documents intent, and ensures features are independently testable. Structured test documentation (DisplayName, Given/When/Then, class tables) makes test suites self-documenting and maintainable.

### V. Integration Testing

Integration tests MUST be used for infrastructure interactions:

- Focus areas requiring integration tests:
  - Database schema changes and migrations
  - Kafka or messaging infrastructure
  - External API contract changes
  - Inter-service communication
- Reuse existing Testcontainers setup (PostgresContainer, KafkaContainer classes)
- Use testcontainers properties under `src/integrationTest/resources`

**Rationale**: Integration tests validate that components work correctly with real infrastructure dependencies, catching integration issues that unit tests cannot detect.

### VI. Configuration Security

Configuration management MUST follow security-first principles:

- NO hard-coded credentials, URLs, or secrets in source code
- Read configuration from `application.properties` or environment variables
- Use Google Cloud Secret Manager for sensitive credentials
- Avoid introducing new external dependencies without PR approval

**Rationale**: Hard-coded secrets create security vulnerabilities and deployment rigidity. Externalized configuration enables secure credential rotation and environment-specific deployments.

### VII. Observability & Logging

Logging MUST use SLF4J throughout the codebase:

- Use `logger.debug/info/warn/error` for all diagnostic output
- NO `System.out` or `System.err` usage
- Include structured context in log messages for debuggability

**Rationale**: Structured logging with SLF4J enables production observability, log aggregation, and post-mortem debugging without code changes.

## Package Structure Requirements

REST API classes MUST follow this package structure:

**Base package**: `com.kiran.stockapi.<feature|datasource>.api`
- `<feature|datasource>`: feature name or data source (e.g., `spotify`, `price`, `catalog`)

**Within the API package**:
- `client`: REST client classes that call external APIs
- `resources`: REST resources/endpoints (controllers)
- `contract`: POJOs representing request and response DTOs

**Example**: For a Spotify integration:
- `com.kiran.stockapi.spotify.api.client` → `SpotifyClient`
- `com.kiran.stockapi.spotify.api.resources` → `SpotifyResource`
- `com.kiran.stockapi.spotify.api.contract` → `SpotifyTrackRequest`, `SpotifyTrackResponse`

**Rationale**: Consistent package structure improves discoverability, separates concerns (client vs. server vs. data), and scales across multiple integration features.

## Development Workflow

Build and verification workflow:

- Use Gradle wrapper: `./gradlew` (Linux/Mac) or `gradlew.bat` (Windows)
- Primary package owner: `com.kiran.stockapi`
- AI agents MUST NOT run verification commands themselves; instead ask the user to run them
- Use try-with-resources for I/O and JDBC resources

Pull requests and commits:

- Keep changes small and focused
- Include short description and testing steps in PR description
- Commit message format: `<area>: short description` (e.g., `service: add price fetcher`)
- Do not change public APIs without bumping version and adding migration notes

**Rationale**: Consistent workflow and commit conventions improve code review efficiency and maintain clear change history.

## Governance

This constitution supersedes all other practices. Compliance is NON-NEGOTIABLE:

- All PRs and code reviews MUST verify compliance with Core Principles
- Violations MUST be justified in plan.md Complexity Tracking section
- Amendments require:
  - Documentation of change rationale
  - Impact analysis on existing code
  - Migration plan for breaking changes
- Constitution amendments MUST increment version according to semantic versioning:
  - **MAJOR**: Backward-incompatible governance/principle removals or redefinitions
  - **MINOR**: New principle/section added or materially expanded guidance
  - **PATCH**: Clarifications, wording, typo fixes, non-semantic refinements
- Runtime development guidance is maintained in `.github/copilot-instructions.md`
- Extension of this constitution should include:
  - Specific linter/formatter rules (Checkstyle/Spotless) when introduced
  - Examples of common patterns (repository methods, DTO builders, exception classes)

**Version**: 1.0.0 | **Ratified**: 2026-03-01 | **Last Amended**: 2026-03-01
