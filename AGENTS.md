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
| `observations` | `ObservationsEntity`, `ObservationsRepository` (`findByPatientIdIn`, `findFirstByPatientId`, `findByPatientId`), `ObservationService`/`ObservationServiceImpl`, `ObservationController`. Listens to `PatientCreatedEvent` and `PatientUpdatedEvent` |
| `calendar` | `GoogleCalendarService`/`GoogleCalendarServiceImpl`, `GoogleCalendarApiClient`, `CalendarAsyncService` (with `@Async` methods) |
| `users` | `UsersEntity`, `UsersRepository`, `UsersService`/`UsersServiceImpl`, `UserDetailsImpl`. Publishes `UserCreatedEvent` for email notifications |
| `auth` | `AuthController` with `POST /auth/register`, `POST /auth/login`, and `POST /auth/logout`. `TokenBlacklist` (in-memory token blacklist). `BlacklistJwtDecoder` wraps NimbusJwtDecoder to reject blacklisted tokens. `SecurityConfig` configures OAuth2 resource server with JWT bearer tokens |
| `kernel` | Shared interfaces: `Patient`, `PatientAccess`, `CalendarEventStore`, `ObservationStore` |

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
- **Calendar Event Store**: `CalendarAsyncService` persists `googleEventId` via the kernel interface `CalendarEventStore` (not `SessionRepository` directly). The implementation `CalendarEventStoreImpl` lives in `session/service/impl/` and breaks the circular dependency between `calendar` and `session`. Never inject `SessionRepository` into the `calendar` module.
- **Patient Domain Events**: `PatientCreatedEvent` and `PatientUpdatedEvent` are published by the `patients` module. The `observations` module listens via `PatientCreatedEventListener` to optionally create observations.
- **Async Processing**: Use `@Async` on service methods for fire-and-forget side effects (Google Calendar API calls, email sending). Enable with `@EnableAsync` on the application class.
- Event records live in `patients/event/` package; listeners live in the consuming module (e.g., `observations/listener/`).
- **JWT Token Blacklist**: `TokenBlacklist` stores blacklisted JWT tokens in an in-memory `Set<String>` (backed by `ConcurrentHashMap.newKeySet()`). `BlacklistJwtDecoder` wraps `NimbusJwtDecoder` in the `jwtDecoder()` bean to check the blacklist on every request. The blacklist is reset on application restart.
- **Logout endpoint**: `POST /auth/logout` is `permitAll()` so the endpoint is accessible. The token is extracted from the `Authorization` header (optional — missing header is silently ignored).

## Application Flows

### Module Dependency Map

```
                    ┌──────────────────────┐
                    │       kernel         │
                    │  Patient             │
                    │  PatientAccess       │
                    │  CalendarEventStore  │
                    └──┬───────┬───────┬───┘
                       │       │       │
              ┌────────▼──┐ ┌──▼────┐ ┌▼──────────────┐
              │ patients  │ │session│ │CalendarEvent  │
              │(owns data)│ │       │ │StoreImpl      │
              │publishes  │ │calls  │ │(session mod)  │
              │events     │ │Calend.│ │               │
              └───┬───────┘ │Async  │ │injects        │
                  │events   │Svc    │ │SessionRepo    │
                  ▼         └──┬────┘ └───────────────┘
           ┌──────────┐       │                 ▲
           │observat. │       │ @Async          │ implements
           │listens to│       ▼                 │
           │Pat*Evt   │ ┌──────────────┐        │
           └──────────┘ │ calendar     ├────────┘
                        │GoogleCalSvc │
                        │CalAsyncSvc  │
                        └──────────────┘
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

### Flow 4: Logout

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**Step-by-step**:

1. `AuthController.logout()` receives the `Authorization` header
2. Extracts the token string (after `"Bearer "` prefix)
3. Calls `JwtUtils.getSubject(token)` to extract the email from the JWT payload (no signature verification needed — just decodes)
4. Calls `UsersService.incrementTokenVersion(email)` — increments the user's `tokenVersion` in the database
5. **Response**: `200 OK`

**Subsequent requests with the same token**:

1. `BearerTokenAuthenticationFilter` extracts the token from the request
2. `NimbusJwtDecoder` validates the JWT (signature, expiry, issuer)
3. `TokenVersionValidator` checks the JWT's `tokenVersion` claim against the user's current DB value:
   - Reads `tokenVersion` from the JWT as `Number` (Nimbus stores JSON integers as `Long`) and calls `.intValue()` to avoid `ClassCastException`
   - Looks up the user by `token.getSubject()`
   - Compares `impl.getTokenVersion() == tokenVersion`
   - If mismatch: returns `OAuth2TokenValidatorResult.failure()` → Spring Security returns `401 Unauthorized`
   - If match: returns `success()` → request proceeds normally

> **Token version invalidation**: The `tokenVersion` field is an `int` on the `users` table (default 0). It is embedded as a JWT claim at token creation. On logout, the counter is incremented in the DB, making all previously-issued tokens for that user invalid. This is persisted across restarts (unlike the older in-memory blacklist approach). The `TokenVersionValidator` safely handles the `Long` → `int` conversion via `instanceof Number` because `NimbusJwtDecoder` deserializes JSON integers as `Long`. Skip to entry `2026-05-28` for the full implementation details.

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
| `POST` | `/api/auth/logout` | `Authorization: Bearer <token>` header | `200 OK` | auth |

### Validation & Error Handling

- **Genre**: the `Genre` enum uses `@JsonCreator` with case-insensitive matching. Invalid value → `400 Bad Request`
- **Patient existence**: when creating a session, if `idPatient` does not exist → `IllegalArgumentException` → `400 Bad Request` with `{"timestamp": ..., "message": "Patient not found"}`
- **Global**: `RestExceptionHandler` catches `IllegalArgumentException` (400) and generic `Exception` (500)
- **No Bean Validation** (`@Valid`, `@NotBlank`, etc.) on DTOs currently

## Change Log

### 2026-05-28
- Added `tokenVersion` field (`int`, default 0) to `UsersEntity` so token invalidation is persisted in the DB.
- Copied `tokenVersion` into `UserDetailsImpl` and exposed via `getTokenVersion()`.
- Embedded `tokenVersion` as a JWT claim in `JwtUtils.generateToken()` (reads from `UserDetailsImpl`).
- Created `TokenVersionValidator` — an `OAuth2TokenValidator<Jwt>` that compares the JWT's `tokenVersion` claim against the user's current DB value; rejects with `token_version_mismatch` on mismatch.
- Uses `token.getClaim("tokenVersion") instanceof Number` to safely handle the claim (Nimbus decodes JSON integers as `Long`, not `Integer`).
- Added `incrementTokenVersion(String email)` to `UsersService`/`UsersServiceImpl` — increments the user's `tokenVersion` in the DB on logout.
- Changed `POST /auth/logout` to call `incrementTokenVersion(email)` via `JwtUtils.getSubject()` instead of an in-memory blacklist.
- Removed `TokenBlacklist` and `BlacklistJwtDecoder` (replaced by token-version approach).
- Fixed `SecurityConfig.jwtDecoder()` to inject `UsersService` (not `UsersRepository`) into `TokenVersionValidator`, avoiding a Spring Modulith boundary violation.
- Added `ObservationStore` kernel interface (`kernel/ObservationStore`) with `findObservationsByPatientId(Long)`.
- Created `ObservationStoreImpl` in `observations/service/impl/` implementing `ObservationStore`.
- Added `findByPatientId(Long)` to `ObservationsRepository`.
- Extended `PatientsResponseDTO` with `List<String> observations` field.
- Updated `PatientsServiceImpl.findById()` to fetch observations via `ObservationStore`.
- Updated `GET /api/patients/{id}` response to include observations array.
- Fixed `CalendarAsyncService.updateAndStoreEvent()` — uses Google Calendar API's `PUT` endpoint (`updateEvent`) to update the existing event in-place instead of delete + create, preventing duplicate calendar events when a session is rescheduled.
- Added `findSessionEventId(String, LocalDateTime)` to `GoogleCalendarService`/`GoogleCalendarServiceImpl` — searches events by patient name + date, returns the `eventId` if found.
- Rewrote `CalendarAsyncService.updateAndStoreEvent()` fallback (when `oldEventId` is null): now searches via `findSessionEventId()` and updates in-place, instead of delete + create.
- Fixed `SessionServiceImpl.update()` — added `updated.setGoogleEventId(existing.getGoogleEventId())` before `save()`. The `SessionEntity` constructor doesn't include `googleEventId`, so `sessionRepository.save(updated)` was overwriting the stored eventId with null via Hibernate's `merge()`, causing `getGoogleEventId()` to always return null on subsequent updates.
- Fixed `TokenVersionValidator` to safely read `tokenVersion` claim via `instanceof Number` instead of direct auto-unboxing — Nimbus decodes JSON integers as `Long`, not `Integer`, causing `ClassCastException`.
- Embedded `Logo.jpeg` as inline image in password emails via `MimeMessageHelper.addInline("logo", ...)` and replaced emoji div in template with `<img src="cid:logo">`.
- Updated `GoogleCalendarServiceImpl.deleteSessionEvent(by search)` to reuse `findSessionEventId()` internally.

### 2026-05-25
- Added `CalendarEventStore` kernel interface (`kernel/CalendarEventStore`) to break the circular dependency between `calendar` and `session` modules.
- Created `CalendarEventStoreImpl` in `session/service/impl/` — a separate `@Component` that implements `CalendarEventStore` and injects `SessionRepository` directly.
- Changed `CalendarAsyncService` to inject `CalendarEventStore` instead of `SessionRepository`.
- Removed `CalendarEventStore` implementation from `SessionServiceImpl` (no longer implements the interface, no `@Lazy` needed).
- Added `@NamedInterface("event")` on `patients/event/` and `@NamedInterface("dto")` on `users/dto/` to allow modulith-compliant cross-module access.

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
