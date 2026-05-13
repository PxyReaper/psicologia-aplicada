# Psicología Aplicada

Aplicación Spring Boot para la gestión de pacientes y sesiones en una práctica de psicología. Integración con Google Calendar para la creación automática de eventos de sesión.

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Organización | Spring Modulith |
| Base de datos | MySQL 8 |
| Calendar API | Google Calendar (cuenta de servicio) |
| Contenedor | Docker + GraalVM Native Image |
| Hilos virtuales | `spring.threads.virtual.enabled: true` |

## Arquitectura

Organización por módulos de dominio:

```
kernel/        → Interfaces compartidas (Patient, PatientAccess)
patients/      → CRUD de pacientes
session/       → CRUD de sesiones + publicación de eventos de dominio
observations/  → Consulta de observaciones por paciente
calendar/      → Escucha eventos de dominio y crea eventos en Google Calendar (async)
web/           → Manejador global de excepciones
```

Los módulos se comunican a través de la API definida en `kernel/` y mediante eventos de dominio (Spring `ApplicationEventPublisher`).

## Prerrequisitos

- **Java 21+** (solo para desarrollo local con Maven)
- **Maven** o usar `./mvnw`
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

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/patients` | Crear paciente |
| `POST` | `/api/session` | Crear sesión (publica evento → Google Calendar asíncrono) |
| `GET` | `/api/observations/patients?rangeStart=&rangeEnd=` | Pacientes activos con observaciones en un rango |

## Estructura de Tablas

| Tabla | Columnas principales |
|-------|---------------------|
| `patients` | `id`, `name`, `surname`, `start_date`, `end_date`, `birthday`, `cell_phone`, `genre` |
| `session` | `id`, `session_date`, `session_date_end`, `observation`, `observation_summary`, `pay`, `id_patient` |
| `observations` | `id`, `observation`, `id_patient` |

No hay relaciones JPA (`@ManyToOne`). Las referencias entre tablas son columnas `Long`.

## Seguridad

- Las credenciales de la BD y de Google Cloud se inyectan vía variables de entorno
- El archivo JSON de la cuenta de servicio de Google está excluido de Git (`.gitignore`)
- `spring-security` está como dependencia pero no completamente configurado

## Testing

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=SessionRepositoryTest
```

Los tests usan H2 en memoria con `ddl-auto: create-drop`.
