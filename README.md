# Dynamic RBAC Authorization Engine

Spring Boot backend where roles and permissions are stored in the database and evaluated
**at runtime** via a custom `PermissionEvaluator`. No `hasRole()` / `hasAuthority()` used
anywhere — every authorization decision comes from a live DB query.

## Tech Stack
Java 17 · Spring Boot · Spring Security · Spring Data JPA · H2 · Lombok 

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
./mvnw clean verify                  
coverage < 80%
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

### How PermissionEvaluator Is Used

`PermissionEvaluator` is a Spring Security interface with two methods:

```java
boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission);
boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission);
```

`CustomPermissionEvaluator` implements both — the two-argument version does the actual
work (matches how every `@PreAuthorize` in this project calls `hasPermission(...)`), and
the four-argument version simply delegates to it. It's registered with Spring Security's
method-security infrastructure in `MethodSecurityConfig`:

```java
@Bean
static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator permissionEvaluator) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
}
```

Once registered, `hasPermission(...)` becomes available inside every `@PreAuthorize`
expression in the app.

### Example Permission Checks

```java
// RoleController — create a role
@PostMapping
@PreAuthorize("hasPermission('ROLE', 'CREATE')")
public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) { ... }

// RoleController — assign a permission to a role
@PostMapping("/{roleId}/permissions/{permissionId}")
@PreAuthorize("hasPermission('ROLE_PERMISSION', 'ASSIGN')")
public ResponseEntity<ApiMessageResponse> assignPermissionToRole(...) { ... }

// SecureDataController — example protected resource
@GetMapping
@PreAuthorize("hasPermission('SECURE_DATA', 'READ')")
public ResponseEntity<Map<String, Object>> getSecureData() { ... }
```

## Assumptions & Design Decisions
- `POST /users/register` added (spec needed users to exist but didn't define creation) — public, grants zero roles by default.
- `GET /users`, `/roles`, `/permissions` added to discover IDs for assignment calls.
- Even admin-labeled endpoints use dynamic permissions, not `hasRole("ADMIN")` — the no-hardcoded-checks rule is applied project-wide.
- `CommandLineRunner` used for seeding (not `data.sql`) so passwords are BCrypt-hashed properly at startup.
- HTTP Basic auth used instead of JWT to keep focus on the authorization engine itself.