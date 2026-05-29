# Psicología Aplicada

Aplicación Spring Boot para la gestión de pacientes y sesiones en una práctica de psicología. Integración con Google Calendar para la creación y eliminación automática de eventos de sesión.

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 (Eclipse Temurin) |
| Framework | Spring Boot 3.5.3 |
| Organización | Spring Modulith |
| Base de datos | MySQL 8 |
| Calendar API | Google Calendar (cuenta de servicio v3 REST) |
| Contenedor | Docker + GraalVM Native Image |
| Hilos virtuales | `spring.threads.virtual.enabled: true` |

## Arquitectura

Organización por módulos de dominio:

```
kernel/        → Interfaces compartidas (Patient, PatientAccess, CalendarEventStore, ObservationStore)
patients/      → CRUD de pacientes + alta/baja
session/       → CRUD de sesiones
observations/  → Consulta de observaciones por paciente
calendar/      → Servicio asíncrono de Google Calendar (CalendarAsyncService)
auth/          → Autenticación JWT + registro de usuarios
users/         → Gestión de usuarios del sistema
web/           → Manejador global de excepciones
```

Los módulos se comunican a través de la API definida en `kernel/`. Las operaciones de Google Calendar se ejecutan de forma asíncrona mediante `CalendarAsyncService` (con `@Async` y `CompletableFuture`), sin eventos de dominio.

## Prerrequisitos

- **Java 21+** (Eclipse Temurin JDK 21, instalado en `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`)
- **Maven** o usar `./mvnw`
- `JAVA_HOME` debe apuntar al JDK 21
- **MySQL 8** corriendo en `localhost:3306`
- **Cuenta de servicio de Google Cloud** con Calendar API habilitada
- **Docker Desktop** (solo para ejecución con contenedor)

## Configuración de Google Cloud

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto o selecciona uno existente
3. Habilita la **Google Calendar API**
4. Ve a **IAM y Administración → Cuentas de servicio**
5. Crea una cuenta de servicio y descarga su clave JSON
6. En el Calendario de Google al que quieras enviar eventos, comparte el calendario con el email de la cuenta de servicio (permiso: **Hacer cambios en eventos**)

Coloca el archivo JSON descargado en la raíz del proyecto con un nombre como `psicologiaaplicada-XXXXX.json`.

> **Importante:** este archivo contiene credenciales sensibles. **No lo subas a Git.** El `.gitignore` ya lo excluye.

## Variables de Entorno

Copia `.env` desde la plantilla:

```env
DB_URL=jdbc:mysql://host.docker.internal:3306/psicologia_aplicada
DB_USERNAME=root
DB_PASSWORD=root
GOOGLE_CALENDAR_CREDENTIALS_PATH=psicologiaaplicada-495911-e202675ec73d.json
GOOGLE_CALENDAR_ID=tu-email@gmail.com
GOOGLE_CALENDAR_APPLICATION_NAME=Psicologia Aplicada
```

> **Importante:** el `.env` contiene credenciales sensibles. Ya está en `.gitignore` — no lo subas a Git.

### Variables disponibles

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | JDBC URL de MySQL |
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Contraseña de MySQL |
| `GOOGLE_CALENDAR_CREDENTIALS_PATH` | Ruta al JSON de la cuenta de servicio |
| `GOOGLE_CALENDAR_ID` | Email del calendario de Google donde crear eventos |
| `GOOGLE_CALENDAR_APPLICATION_NAME` | Nombre mostrado en la API |

## Ejecución Local (Desarrollo)

### 1. Asegúrate de que MySQL esté corriendo

```bash
# Verifica que MySQL 8 está en localhost:3306
```

### 2. Crea la base de datos

```sql
CREATE DATABASE IF NOT EXISTS psicologia_aplicada;
```

### 3. Configura `.env` con los valores correctos

### 4. Ejecuta la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación arranca en `http://localhost:8080/api`.

> El `ddl-auto: update` de Hibernate crea/actualiza las tablas automáticamente.

## Ejecución con Docker

### 1. Construye la imagen nativa

```bash
docker build -t psicologia-aplicada .
```

Este paso compila un binario nativo con GraalVM. La primera vez puede tardar varios minutos.

### 2. Asegúrate de que MySQL esté accesible

MySQL debe estar corriendo y accesible desde el contenedor. Con Docker Desktop en Windows la URL `host.docker.internal` apunta al host.

### 3. Configura `.env`

Asegúrate de que `DB_URL` apunte a `host.docker.internal:3306` y que el archivo JSON de Google Cloud esté en la raíz del proyecto.

### 4. Inicia el contenedor

```bash
docker compose up
```

La aplicación arranca en `http://localhost:8080/api`.

> `docker-compose.yml` solo define el contenedor de la app. **MySQL no está incluido** — debe ejecutarse aparte.

## Endpoints

### Pacientes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/patients` | Crear paciente (campo `observation` opcional) |
| `PUT` | `/api/patients/{id}` | Actualizar paciente (si se envía `observation`, se añade a la tabla observations) |
| `POST` | `/api/patients/{id}/discharge` | Dar de baja (asigna `endDate = today`) |
| `GET` | `/api/patients/{id}` | Obtener paciente por ID (incluye lista de observaciones) |

### Sesiones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/session` | Crear sesión. `CalendarAsyncService` crea el evento en Google Calendar y guarda el `googleEventId` |
| `PUT` | `/api/session/{id}` | Actualizar sesión. `CalendarAsyncService` actualiza el evento existente en Google Calendar vía `PUT` (no borra + crea) |
| `DELETE` | `/api/session/{id}` | Eliminar sesión. `CalendarAsyncService` borra el evento de Google Calendar por `eventId` |

### Observaciones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/observations/patients?rangeStart=&rangeEnd=` | Pacientes activos con observaciones en un rango |

### Autenticación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar usuario (retorna `201 Created` sin datos del usuario) |
| `POST` | `/auth/login` | Iniciar sesión (retorna JWT token) |
| `POST` | `/auth/logout` | Cerrar sesión — invalida el token incrementando `tokenVersion` del usuario |

## Estructura de Tablas

| Tabla | Columnas principales |
|-------|---------------------|
| `patients` | `id`, `name`, `surname`, `start_date`, `end_date`, `birthday`, `cell_phone`, `genre` |
| `session` | `id`, `session_date`, `session_date_end`, `observation`, `observation_summary`, `pay`, `id_patient`, `google_event_id` |
| `observations` | `id`, `observation`, `id_patient` |
| `users` | `id`, `email`, `username`, `password`, `name`, `surname`, `role`, `enabled`, `created_at`, `token_version` |

No hay relaciones JPA (`@ManyToOne`). Las referencias entre tablas son columnas `Long`.

## Seguridad

- Autenticación con JWT (HMAC-SHA256) vía `spring-security` OAuth2 Resource Server
- Login retorna un token JWT con claims: `sub` (email), `role`, `tokenVersion`
- Cada token lleva un `tokenVersion` embebido que se valida contra la BD en cada petición
- Al cerrar sesión se incrementa `tokenVersion` del usuario, invalidando todos sus tokens anteriores
- Los endpoints `/auth/login` y `/auth/logout` son públicos (`permitAll()`)
- El resto requiere rol `ADMIN` o `PSYCHOLOGIST`
- `TokenVersionValidator` maneja de forma segura el claim numérico usando `instanceof Number` (Nimbus decodifica enteros JSON como `Long`)
- Las credenciales de la BD y de Google Cloud se inyectan vía variables de entorno
- El archivo JSON de la cuenta de servicio de Google está excluido de Git (`.gitignore`)

## Google Calendar — Flujo Asíncrono

Las operaciones con Google Calendar se ejecutan mediante `CalendarAsyncService` (en `calendar/`), un componente con métodos `@Async` que corre en un hilo separado. No utiliza eventos de dominio.

El guardado del `googleEventId` tras crear/actualizar un evento se realiza a través de la interfaz `CalendarEventStore` (en `kernel/`). `CalendarAsyncService` depende de esta interfaz, no de `SessionRepository` ni de `SessionServiceImpl`, evitando así dependencias circulares entre los módulos `calendar` y `session`. La implementación reside en `session/service/impl/CalendarEventStoreImpl`.

| Operación | BD (síncrono) | Calendar (asíncrono vía CalendarAsyncService) |
|-----------|---------------|------------------------------------------------|
| `POST /session` | Guarda sesión | Crea evento en Google Calendar, guarda `google_event_id` vía `CalendarEventStore` |
| `PUT /session/{id}` | Actualiza sesión | Actualiza el evento en Google Calendar in-place (vía `PUT`). Si no hay `googleEventId`, busca por fecha/nombre y actualiza; si no existe, crea uno nuevo |
| `DELETE /session/{id}` | Elimina sesión | Borra evento de Google Calendar por `eventId` |

## Observaciones al Crear/Actualizar Paciente

El DTO `PatientsRequestDTO` incluye un campo opcional `observation`. Si se envía:

- **Creación**: se publica `PatientCreatedEvent` → `PatientCreatedEventListener` crea la observación
- **Actualización**: se publica `PatientUpdatedEvent` → el mismo listener crea una nueva observación (no reemplaza existentes)

## Testing

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=SessionRepositoryTest
```

Los tests usan H2 en memoria con `ddl-auto: create-drop`.
