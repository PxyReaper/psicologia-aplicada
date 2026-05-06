# AGENTS.md

## Project

Spring Boot 3.5.3 app (Java 21) for a psychology practice. Uses **Spring Modulith** to organize domain packages.

## Commands

| Action | Command |
|--------|---------|
| Build | `./mvnw clean package` |
| Run dev | `./mvnw spring-boot:run` (requires MySQL running) |
| Test | `./mvnw test` |
| Single test | `./mvnw test -Dtest=ClassName` |
| Native build | `./mvnw native:compile -Pnative` |

## Prerequisites

- **MySQL** must be running on `localhost:3306`. Connects with `root`/`root` to database `psicologia_aplicada`.
- `docker-compose.yml` only defines the app image — it does **not** include a MySQL service. Start MySQL separately.

## Architecture

Package-per-module layout under `src/main/java/com/tfg/proyectolibreria/psicologiaAplicada/`:

| Package | Role |
|---------|------|
| `patients` | `PatientsEntity`, `PatientsRepository` (JpaRepository), `PatientsService`/`PatientsServiceImpl`, `PatientsController`, `Genre` enum + converter |
| `session` | `SessionEntity` (with `patientId` Long field), `SessionRepository` (has `findByPatientId`), `SessionService`/`SessionServiceImpl`, `SessionController` |
| `observations` | `ObservationsEntity` (with `patientId` Long field), `ObservationsRepository` (has `findByPatientIdIn`), `ObservationsService`/`ObservationsServiceImpl`, `ObservationsController` |
| `users` | Placeholder `Users` component + empty `UserRepository` — not wired to Spring Security yet |
| `kernel` | Shared interfaces: `Patient`, `PatientAccess` |

Entity relationships: `SessionEntity` and `ObservationsEntity` reference patients via a `Long patientId` column (not `@ManyToOne`).

## Repository Naming Convention

Spring Data JPA method names must match field names exactly. For a field `patientId`, use `findByPatientId` (NOT `findByIdPatient` which Spring parses as `id.patient`).

## Key config

- `application.yml`: `ddl-auto: create-drop` — schema is **recreated on every run** (data not persisted across restarts).
- Virtual threads enabled (`spring.threads.virtual.enabled: true`).
- `show-sql: false` by default.

## Docker

`Dockerfile` builds a **GraalVM native image** (two-stage build: JDK 21 builder → Debian slim runtime). Native binary named `psicologia-aplicada` (no `.jar` extension).

## Testing

- Single test: `PsicologiaAplicadaApplicationTests` — loads context and runs `ApplicationModules.verify()` to check modulith boundaries.
- Add `@SpringBootTest` for integration tests; use `spring-modulith-starter-test` for module-level tests.
- Use `@DataJpaTest` with `@Import(TestJpaConfig.class)` and `@ActiveProfiles("test")` for repository tests.
- Use `@WebMvcTest` with `excludeAutoConfiguration = SecurityAutoConfiguration.class` to avoid Spring Security blocking controller tests when security is auto-configured but not yet fully set up.

## Conventions

- Lombok on entities (`@Getter`, `@AllArgsConstructor`, `@NoArgsConstructor`).
- Entity naming: `*Entity` suffix.
- Repository interfaces in `*/*/repository/` subpackage.
- Enums stored as string columns via custom `AttributeConverter` (`autoApply = true`).
- Constructor injection with `private final` fields for services and controllers.

## Change Log

### 2026-05-06
- Fixed `SessionRepository`: renamed `findByIdPatient` → `findByPatientId` to match `SessionEntity.patientId` field name (Spring Data JPA parses `findByIdPatient` as `id.patient`)
- Added missing `@Import` import to `ObservationsRepositoryTest`, `PatientsRepositoryTest`, and `SessionRepositoryTest`
- Fixed `ObservationControllerTest`: excluded `SecurityAutoConfiguration` to resolve 401 Unauthorized error from Spring Security auto-configuration
- Updated architecture documentation to reflect current state: `patients`, `session`, `observations` packages now have full controller/service/repository layers; added `kernel` package with shared interfaces
