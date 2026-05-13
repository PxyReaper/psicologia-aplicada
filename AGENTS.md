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
- **Domain Events**: Use records (`SessionCreatedEvent`) as domain events published via `ApplicationEventPublisher` to decouple modules within the application.
- **Async Processing**: Use `@Async` on `@EventListener` methods for fire-and-forget side effects (e.g., Google Calendar API calls). Enable with `@EnableAsync` on the application class.
- Event records live in `session/event/` package; listeners live in the consuming module (e.g., `calendar/`).

## Application Flows

### Module Dependency Map

```
                    ┌──────────────┐
                    │   kernel     │
                    │  Patient     │
                    │  PatientAccess│
                    └──────┬───────┘
                           │ implements
               ┌───────────┴───────────┐
               │                       │
     ┌─────────▼───────┐   ┌──────────▼──────────┐
     │ patients        │   │  PatientAccessImpl  │
     │ (owns the data) │   │  (bridge service)   │
     └─────────────────┘   └──────────┬───────────┘
                                      │ injected via kernel API
                          ┌───────────┼──────────────┐
                          │           │              │
                    ┌─────▼────┐ ┌───▼────────┐ ┌───▼──────────┐
                    │ session  │ │observations │ │   calendar   │
                    │          │ │            │ │              │
                    │ findById │ │findActiveIn│ │ listens to   │
                    │          │ │Range       │ │SessionCreated│
                    │ publishes│ │            │ │Event (async) │
                    │ Event ───┼─┼────────────┼─┼──────────────┘
                    └──────────┘ └────────────┘
```

### Flow 1: Create Patient

```
POST /api/patients
Content-Type: application/json

Request:
{
  "name": "Juan",
  "surname": "Pérez",
  "birthDay": "1990-05-15",
  "cellPhone": "612345678",
  "genre": "masculino"
}
```

**Step-by-step**:

1. `PatientsController.save()` receives the `PatientsRequestDTO`
2. Calls `PatientsService.save()` → `PatientsServiceImpl.save()`
3. A `PatientsEntity` is created with:
   - `startDate = LocalDate.now()` (registered today)
   - `endDate = null` (active patient, no discharge date)
   - `genre` as enum `Genre.MASCULINO` (deserialized via `@JsonCreator`)
   - Remaining fields from the DTO
4. Persisted with `patientsRepository.save(patient)`
5. **Response**: `201 Created` with Location `/api/patients`

> **Patient discharge**: there is currently no discharge endpoint. To discharge a patient, `endDate` would be set to a specific date, causing `findActiveInRange()` to exclude them from active results.

### Flow 2: Create Session

```
POST /api/session
Content-Type: application/json

Request:
{
  "dateSession": "2026-05-11T10:00:00",
  "dateSessionEnd": "2026-05-11T11:00:00",
  "observatory": "El paciente presenta ansiedad...",
  "observatorySummary": "Sesión de ansiedad",
  "idPatient": 1
}
```

**Step-by-step**:

1. `SessionController.save()` receives the `SessionRequestDTO`
2. `SessionServiceImpl.save()` executes:
   - **Validation**: looks up the patient via `PatientAccess.findById(idPatient)`
     - `PatientAccessImpl` → `PatientsRepository.findById()`
     - If not found: throws `IllegalArgumentException("Patient not found")` → `RestExceptionHandler` returns `400 Bad Request`
   - **Persistence**: creates a `SessionEntity` with `pay=false` and saves it via `sessionRepository.save(session)`
   - **Event**: publishes `SessionCreatedEvent` with patient and session data
3. **Immediate response**: `201 Created` with Location `/api/session`
4. **Async** (does not block the response):
   - `SessionCalendarEventListener.onSessionCreated()` receives the event (annotated with `@Async` + `@EventListener`)
   - Calls `GoogleCalendarService.createSessionEvent()` which:
     a. Builds a Google Calendar client from OAuth2 service account credentials
     b. Looks up or creates the "Psicología Aplicada" calendar
     c. If created, shares it with the configured email as "writer"
     d. Inserts an event with title "Session: {full name}", start/end times
   - On failure (invalid credentials, network, etc.), the error is logged but does NOT affect the HTTP response

### Flow 3: Get Active Patients with Observations

```
GET /api/observations/patients?rangeStart=2026-01-01&rangeEnd=2026-12-31
```

**Step-by-step**:

1. `ObservationController.getActivePatientsWithObservations()` receives `rangeStart` and `rangeEnd`
2. `ObservationServiceImpl.getActivePatientsWithObservations()`:
   a. **Step 1**: Gets active patients in the range:
      - Calls `PatientAccess.findActivePatientsInRange(rangeStart, rangeEnd)`
      - `PatientAccessImpl` → `PatientsRepository.findActiveInRange()`
      - **JPQL Query**: `SELECT p FROM PatientsEntity p WHERE p.startDate <= :rangeEnd AND COALESCE(p.endDate, :rangeEnd) >= :rangeStart`
      - This includes patients with `endDate = null` (COALESCE treats it as `rangeEnd`, meaning "still active")
   b. **Step 2**: Extracts the IDs of active patients
   c. **Step 3**: Fetches observations via `observationsRepository.findByPatientIdIn(patientIds)`
   d. **Step 4**: Groups observations by patient and builds `List<PatientObservationsDTO>`
3. **Response**: `200 OK` with JSON:
```json
[
  {
    "patient": { "id": 1, "name": "Juan", "surname": "Pérez" },
    "observations": ["observation 1", "observation 2"]
  }
]
```

### Data Model

No JPA relationships (`@ManyToOne`/`@OneToMany`). All cross-table references are `Long` columns.

| Table | Key columns | Patient reference |
|-------|-------------|-------------------|
| `patients` | `id` (PK), `name`, `surname`, `start_date`, `end_date`, `birthday`, `cell_phone`, `genre` | — |
| `session` | `id` (PK), `session_date`, `session_date_end`, `observation`, `observation_summary`, `pay`, `id_patient` | `id_patient` (Long) |
| `observations` | `id` (PK), `observation`, `id_patient` | `id_patient` (Long) |

### API Endpoints Summary

| Method | Endpoint | Request | Response | Module |
|--------|----------|---------|----------|--------|
| `POST` | `/api/patients` | `PatientsRequestDTO` | `201 Created` | patients |
| `POST` | `/api/session` | `SessionRequestDTO` | `201 Created` | session |
| `GET` | `/api/observations/patients` | `rangeStart`, `rangeEnd` (query params) | `200 OK` + `List<PatientObservationsDTO>` | observations |

### Validation & Error Handling

- **Genre**: the `Genre` enum uses `@JsonCreator` with case-insensitive matching. Invalid value → `400 Bad Request`
- **Patient existence**: when creating a session, if `idPatient` does not exist → `IllegalArgumentException` → `400 Bad Request` with `{"timestamp": ..., "message": "Patient not found"}`
- **Global**: `RestExceptionHandler` catches `IllegalArgumentException` (400) and generic `Exception` (500)
- **No Bean Validation** (`@Valid`, `@NotBlank`, etc.) on DTOs currently

## Change Log

### 2026-05-11
- Refactored `SessionServiceImpl.save()` to publish a `SessionCreatedEvent` via `ApplicationEventPublisher` instead of directly calling `GoogleCalendarService`.
- Created `calendar/SessionCalendarEventListener` annotated with `@Async` + `@EventListener` to handle the calendar event creation asynchronously.
- Added `@EnableAsync` to `PsicologiaAplicadaApplication`.
- Created `session/event/SessionCreatedEvent` record to carry patient data across module boundaries.
- Added Application Flows section with module dependency map and detailed flow documentation.

### 2026-05-06
- Fixed `SessionRepository`: renamed `findByIdPatient` → `findByPatientId` to match `SessionEntity.patientId` field name (Spring Data JPA parses `findByIdPatient` as `id.patient`)
- Added missing `@Import` import to `ObservationsRepositoryTest`, `PatientsRepositoryTest`, and `SessionRepositoryTest`
- Fixed `ObservationControllerTest`: excluded `SecurityAutoConfiguration` to resolve 401 Unauthorized error from Spring Security auto-configuration
- Updated architecture documentation to reflect current state: `patients`, `session`, `observations` packages now have full controller/service/repository layers; added `kernel` package with shared interfaces
