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

- **Java 21** (Eclipse Temurin JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`). `JAVA_HOME` must point to this JDK.
- **MySQL** must be running on `localhost:3306`. Connects with `root`/`root` to database `psicologia_aplicada`.
- `docker-compose.yml` only defines the app image — it does **not** include a MySQL service. Start MySQL separately.

## Architecture

Package-per-module layout under `src/main/java/com/tfg/proyectolibreria/psicologiaAplicada/`:

| Package | Role |
|---------|------|
| `patients` | `PatientsEntity`, `PatientsRepository` (JpaRepository), `PatientsService`/`PatientsServiceImpl`, `PatientsController`, `Genre` enum + converter. Publishes `PatientCreatedEvent` and `PatientUpdatedEvent` |
| `session` | `SessionEntity` (with `patientId` Long field + `googleEventId`), `SessionRepository`, `SessionService`/`SessionServiceImpl`, `SessionController`. Calendar operations via `CalendarAsyncService` |
| `observations` | `ObservationsEntity`, `ObservationsRepository` (`findByPatientIdIn`, `findFirstByPatientId`), `ObservationService`/`ObservationServiceImpl`, `ObservationController`. Listens to `PatientCreatedEvent` and `PatientUpdatedEvent` |
| `calendar` | `GoogleCalendarService`/`GoogleCalendarServiceImpl`, `GoogleCalendarApiClient`, `CalendarAsyncService` (with `@Async` methods) |
| `users` | `UsersEntity`, `UsersRepository`, `UsersService`/`UsersServiceImpl`, `UserDetailsImpl`. Publishes `UserCreatedEvent` for email notifications |
| `auth` | `AuthController` with `POST /auth/register` and `POST /auth/login`. Register returns `201 Created` with no body |
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
- **Calendar Async**: Google Calendar operations use `CalendarAsyncService` with `@Async` methods. No domain events for calendar. The service runs in a separate thread and does not block the HTTP response.
- **Patient Domain Events**: `PatientCreatedEvent` and `PatientUpdatedEvent` are published by the `patients` module. The `observations` module listens via `PatientCreatedEventListener` to optionally create observations.
- **Async Processing**: Use `@Async` on service methods for fire-and-forget side effects (Google Calendar API calls, email sending). Enable with `@EnableAsync` on the application class.
- Event records live in `patients/event/` package; listeners live in the consuming module (e.g., `observations/listener/`).

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
     │ publishes events│   └──────────┬───────────┘
     └──────┬──────────┘             │ injected via kernel API
            │ events                 │
            ▼                        ▼
     ┌──────────┐            ┌──────────────────┐
     │observations│          │ session          │
     │            │          │                  │
     │listens to  │          │ save/update/     │
     │Patient*Evt │          │ delete calls     │
     └────────────┘          │ CalendarAsyncSvc │
                             └────────┬─────────┘
                                      │ @Async
                                      ▼
                              ┌──────────────────┐
                              │ calendar         │
                              │ GoogleCalendarSvc│
                              │ CalendarAsyncSvc │
                              └──────────────────┘
```

The `session` module calls `CalendarAsyncService` directly (no domain events). Calendar operations are `@Async` and run in a separate thread. `ObservationsRepository` has `findFirstByPatientId` for upsert patterns.

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
  "genre": "masculino",
  "observation": "Paciente deriva por ansiedad generalizada"
}
```

**Step-by-step**:

1. `PatientsController.save()` receives the `PatientsRequestDTO` (including optional `observation`)
2. Calls `PatientsService.save()` → `PatientsServiceImpl.save()`
3. A `PatientsEntity` is created with:
   - `startDate = LocalDate.now()` (registered today)
   - `endDate = null` (active patient, no discharge date)
   - `genre` as enum `Genre.MASCULINO` (deserialized via `@JsonCreator`)
   - Remaining fields from the DTO
4. Persisted with `patientsRepository.save(patient)`
5. If `observation` field is present & non-blank, publishes `PatientCreatedEvent`
6. `PatientCreatedEventListener.onPatientCreated()` receives the event and creates an `ObservationsEntity`
7. **Response**: `201 Created` with Location `/api/patients`

> **Patient discharge**: `POST /api/patients/{id}/discharge` sets `endDate = LocalDate.now()`, causing `findActiveInRange()` to exclude them from active results. <br/>
> **Patient update**: `PUT /api/patients/{id}` accepts the same DTO. If `observation` is provided, publishes `PatientUpdatedEvent` and creates a new observation (does not replace existing ones).

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
3. **Immediate response**: `201 Created` with Location `/api/session`
4. **Async** (does not block the response):
   - `CalendarAsyncService.createAndUpdateSessionOnCreate()` is called with the saved session
   - Calls `GoogleCalendarService.createSessionEvent()` which:
     a. Builds a Google Calendar client from OAuth2 service account credentials
     b. Looks up or creates the "Psicología Aplicada" calendar
     c. If created, shares it with the configured email as "writer"
     d. Inserts an event with title "Session: {full name}", start/end times
     e. **Returns the `eventId`** → saved to `SessionEntity.googleEventId`
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
| `session` | `id` (PK), `session_date`, `session_date_end`, `observation`, `observation_summary`, `pay`, `id_patient`, `google_event_id` | `id_patient` (Long) |
| `observations` | `id` (PK), `observation`, `id_patient` | `id_patient` (Long) |

### API Endpoints Summary

| Method | Endpoint | Request | Response | Module |
|--------|----------|---------|----------|--------|
| `POST` | `/api/patients` | `PatientsRequestDTO` | `201 Created` | patients |
| `PUT` | `/api/patients/{id}` | `PatientsRequestDTO` | `200 OK` | patients |
| `POST` | `/api/patients/{id}/discharge` | — | `200 OK` | patients |
| `POST` | `/api/session` | `SessionRequestDTO` | `201 Created` | session |
| `PUT` | `/api/session/{id}` | `SessionRequestDTO` | `200 OK` | session |
| `DELETE` | `/api/session/{id}` | — | `204 No Content` | session |
| `GET` | `/api/observations/patients` | `rangeStart`, `rangeEnd` (query params) | `200 OK` + `List<PatientObservationsDTO>` | observations |

### Validation & Error Handling

- **Genre**: the `Genre` enum uses `@JsonCreator` with case-insensitive matching. Invalid value → `400 Bad Request`
- **Patient existence**: when creating a session, if `idPatient` does not exist → `IllegalArgumentException` → `400 Bad Request` with `{"timestamp": ..., "message": "Patient not found"}`
- **Global**: `RestExceptionHandler` catches `IllegalArgumentException` (400) and generic `Exception` (500)
- **No Bean Validation** (`@Valid`, `@NotBlank`, etc.) on DTOs currently

## Change Log

### 2026-05-24
- Replaced `SessionCalendarEventListener` (domain events) with `CalendarAsyncService` — a dedicated `@Async` service called directly from `SessionServiceImpl`.
- Added `CalendarAsyncService` with methods: `createAndUpdateSessionOnCreate`, `createAndUpdateSessionOnUpdate`, `createAndUpdateSessionOnDelete`.
- Added `googleEventId` field to `SessionEntity` for reliable Google Calendar event update/deletion.
- Added `GoogleCalendarService` with overloaded `deleteSessionEvent(String eventId)` and `deleteSessionEvent(LocalDateTime startTime, LocalDateTime endTime, String patientName)`.
- Changed `GoogleCalendarService.createSessionEvent()` to return the `eventId` string.
- Added `PUT /api/session/{id}` and `DELETE /api/session/{id}` endpoints.
- Removed `session/event/SessionCreatedEvent`, `SessionUpdatedEvent`, and `SessionDeletedEvent` records.
- Removed `calendar/SessionCalendarEventListener`.
- Added `observation` optional field to `PatientsRequestDTO` for creating/updating observations when creating/updating patients.
- Added `PatientCreatedEvent` and `PatientUpdatedEvent` in `patients/event/`.
- Added `PatientCreatedEventListener` in `observations/listener/` to handle both events.
- Added `PUT /api/patients/{id}` and `POST /api/patients/{id}/discharge` endpoints.
- Added `findByPatientIdIn` and `findFirstByPatientId` to `ObservationsRepository`.
- Changed `UsersService.register()` to return `void` (response entity with no body).
- Configured `PsicologiaAplicadaApplication` package for Spring Modulith verification.

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
