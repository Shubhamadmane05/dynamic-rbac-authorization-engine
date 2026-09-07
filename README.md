# Dynamic RBAC Authorization Engine

Spring Boot backend where roles and permissions are stored in the database and evaluated
**at runtime** via a custom `PermissionEvaluator`. No `hasRole()` / `hasAuthority()` used
anywhere — every authorization decision comes from a live DB query.

## Tech Stack
Java 17 · Spring Boot · Spring Security · Spring Data JPA · H2 · Lombok · JUnit 5 · JaCoCo · SonarQube

---

## Setup & Run

```bash
./mvnw spring-boot:run
```
App runs on `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:rbacdb`, user `sa`, no password).

**Seeded accounts** (created on startup by `DataSeeder`):

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin1` | `admin123` | ADMIN | all (role/permission management) |
| `shubham` | `shubham123` | USER | `SECURE_DATA_READ` |

**Run tests:**
```bash
./mvnw clean test jacoco:report      # HTML report: target/site/jacoco/index.html
./mvnw clean verify                  # fails build if coverage < 80%
```

**SonarQube:**
```bash
./mvnw clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```

---

## API Request Flow

Request (HTTP Basic auth)
→ Spring Security authenticates (CustomUserDetailsService)
→ Controller method hit, e.g. @PreAuthorize("hasPermission('ROLE','CREATE')")
→ CustomPermissionEvaluator builds "ROLE_CREATE" and queries the DB for the
user's live permission set (via user_role → role_permission → permission)
→ granted → method executes | denied → 403 via GlobalExceptionHandler


Permission naming convention: **`{RESOURCE}_{ACTION}`** (e.g. `ROLE_CREATE`, `SECURE_DATA_READ`).

### Endpoints
| Method & Path | Permission | Description |
|---|---|---|
| `POST /users/register` | public | Register a new user |
| `GET /users` | `USER_READ` | List users |
| `POST /roles` | `ROLE_CREATE` | Create a role |
| `GET /roles` | `ROLE_READ` | List roles |
| `POST /permissions` | `PERMISSION_CREATE` | Create a permission |
| `GET /permissions` | `PERMISSION_READ` | List permissions |
| `POST /roles/{roleId}/permissions/{permissionId}` | `ROLE_PERMISSION_ASSIGN` | Attach permission to role |
| `POST /users/{userId}/roles/{roleId}` | `USER_ROLE_ASSIGN` | Attach role to user |
| `GET /secure-data` | `SECURE_DATA_READ` | Example protected resource |

All requests are validated (`@Valid` + Bean Validation); all exceptions (validation, not-found,
duplicate, access-denied) are handled centrally in `GlobalExceptionHandler` with consistent
JSON responses.

---

## Assumptions & Design Decisions
- `POST /users/register` added (spec needed users to exist but didn't define creation) — public, grants zero roles by default.
- `GET /users`, `/roles`, `/permissions` added to discover IDs for assignment calls.
- Even admin-labeled endpoints use dynamic permissions, not `hasRole("ADMIN")` — the no-hardcoded-checks rule is applied project-wide.
- `CommandLineRunner` used for seeding (not `data.sql`) so passwords are BCrypt-hashed properly at startup.
- HTTP Basic auth used instead of JWT to keep focus on the authorization engine itself.