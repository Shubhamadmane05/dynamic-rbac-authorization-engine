# Dynamic RBAC Authorization Engine

A Spring Boot backend where **roles and permissions are stored in the database and
evaluated at runtime** via a custom `PermissionEvaluator`. No endpoint anywhere in this
codebase calls `hasRole(...)` or `hasAuthority(...)` — every authorization decision is
resolved dynamically from `role_permissions` / `user_role_mappings`.

---

## 1. Authorization Flow (how a request gets authorized)

```
Client sends request with HTTP Basic credentials (username/password)
        │
        ▼
Spring Security authenticates the user
  -> CustomUserDetailsService loads the AppUser + BCrypt-hashed password
  -> password compared by DaoAuthenticationProvider
        │  (authentication only - "who are you")
        ▼
Controller method is reached, annotated e.g.
  @PreAuthorize("hasPermission('ROLE', 'CREATE')")
        │
        ▼
Spring's method-security interceptor evaluates the SpEL expression
  -> delegates hasPermission(...) to CustomPermissionEvaluator
        │  (authorization - "what are you allowed to do")
        ▼
CustomPermissionEvaluator:
  1. requiredPermission = "ROLE" + "_" + "CREATE" = "ROLE_CREATE"
  2. grantedPermissions = SELECT permission.name
                           FROM user_role_mappings
                           JOIN role ON ...
                           JOIN role_permissions ON ...
                           JOIN permission ON ...
                           WHERE username = :currentUser
     (cached per-username; evicted whenever an assignment changes)
  3. return grantedPermissions.contains(requiredPermission)
        │
        ├── true  -> controller method executes normally
        └── false -> AccessDeniedException -> GlobalExceptionHandler -> HTTP 403
```

Authentication (who you are) and authorization (what you can do) are deliberately kept
in two separate layers:

| Layer | Mechanism | Data source |
|---|---|---|
| Authentication | Spring Security `DaoAuthenticationProvider` + `CustomUserDetailsService` | `app_users` table |
| Authorization | `@PreAuthorize("hasPermission(...)")` + `CustomPermissionEvaluator` | `role_permissions` / `user_role_mappings` (live query) |

## 2. Permission Evaluation Logic

Permissions are named using a fixed convention: **`{RESOURCE}_{ACTION}`**, e.g.
`ROLE_CREATE`, `PERMISSION_CREATE`, `ROLE_PERMISSION_ASSIGN`, `USER_ROLE_ASSIGN`,
`SECURE_DATA_READ`.

Every `@PreAuthorize` in the project follows the same two-argument shape:

```java
@PreAuthorize("hasPermission('<RESOURCE>', '<ACTION>')")
```

`CustomPermissionEvaluator` concatenates those two arguments (upper-cased) into the
exact permission name and checks whether it's in the current user's resolved
permission set. This means **adding a new protected operation never requires touching
the evaluator** — you just add a new permission row, attach it to a role, and reference
it from a new `@PreAuthorize` annotation.

## 3. How `PermissionEvaluator` Is Used

`PermissionEvaluator` is a Spring Security interface with two methods:

```java
boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission);
boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission);
```

`CustomPermissionEvaluator` implements both (the second delegates to the first, since
this project doesn't need per-instance/object-level checks — only resource-type-level
checks). It's registered with Spring Security's method-security infrastructure in
`MethodSecurityConfig`, via a `DefaultMethodSecurityExpressionHandler`:

```java
@Bean
static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator permissionEvaluator) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
}
```

Once registered, the `hasPermission(...)` function becomes available inside every
`@PreAuthorize` SpEL expression in the app.

## 4. Why Hardcoded Roles Are Avoided

The assignment explicitly forbids `hasRole('ADMIN')`-style checks, and this project
avoids them everywhere, including on the "Role: ADMIN" endpoints listed in the spec
(`POST /roles`, `POST /permissions`, the two assignment endpoints). Instead, those
endpoints are protected by granular permissions (`ROLE_CREATE`, `PERMISSION_CREATE`,
`ROLE_PERMISSION_ASSIGN`, `USER_ROLE_ASSIGN`) that happen to all be granted to the seeded
ADMIN role — but the code has no idea a role called "ADMIN" exists. If tomorrow you want
a `SUPPORT` role that can create permissions but not roles, that's a data change
(`POST /roles/{supportRoleId}/permissions/{permissionCreateId}`), not a code change.

`CustomUserDetailsService` does still build `ROLE_*` `GrantedAuthority` objects — this
is unavoidable because `UserDetails` requires *some* authority collection to satisfy
Spring Security's authentication contract. **Nothing in this codebase reads those
authorities for an authorization decision** — no `hasRole()`, no `hasAuthority()`, no
`@Secured`. Every actual "can this user do X" check goes through
`CustomPermissionEvaluator` and a live database query.

## 5. Entities

| Entity | Table | Purpose |
|---|---|---|
| `AppUser` | `app_users` | Login credentials only (username, BCrypt password, enabled flag) |
| `Role` | `roles` | A named label (e.g. `ADMIN`, `USER`) |
| `Permission` | `permissions` | A named, fine-grained capability (e.g. `ROLE_CREATE`) |
| `RolePermission` | `role_permissions` | Which permissions a role grants |
| `UserRoleMapping` | `user_role_mappings` | Which roles a user holds |

## 6. API Endpoints

| Method & Path | Required permission | Description |
|---|---|---|
| `POST /users/register` | *(public)* | Self-service account creation (see Assumptions) |
| `POST /roles` | `ROLE_CREATE` | Create a role |
| `GET /roles` | `ROLE_READ` | List roles |
| `POST /permissions` | `PERMISSION_CREATE` | Create a permission |
| `GET /permissions` | `PERMISSION_READ` | List permissions |
| `POST /roles/{roleId}/permissions/{permissionId}` | `ROLE_PERMISSION_ASSIGN` | Attach a permission to a role |
| `POST /users/{userId}/roles/{roleId}` | `USER_ROLE_ASSIGN` | Attach a role to a user |
| `GET /secure-data` | `SECURE_DATA_READ` | Example protected resource |

### Example permission checks

```java
// RoleController
@PostMapping
@PreAuthorize("hasPermission('ROLE', 'CREATE')")
public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) { ... }

// SecureDataController
@GetMapping
@PreAuthorize("hasPermission('SECURE_DATA', 'READ')")
public ResponseEntity<Map<String, Object>> getSecureData() { ... }
```

## 7. Seed Data

`DataSeeder` runs once on startup (only if the `roles` table is empty) and creates:

| Username | Password | Role | Notable permissions |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | all 7 seeded permissions |
| `john` | `user123` | `USER` | `SECURE_DATA_READ` |

All 7 base permissions (`ROLE_CREATE`, `ROLE_READ`, `PERMISSION_CREATE`,
`PERMISSION_READ`, `ROLE_PERMISSION_ASSIGN`, `USER_ROLE_ASSIGN`, `SECURE_DATA_READ`) are
created and attached to `ADMIN`. This solves RBAC's inherent bootstrap problem: creating
a role requires `ROLE_CREATE`, but the only way to grant `ROLE_CREATE` is via a role —
so *something* outside the normal API flow has to seed the first admin. Toggle this off
with `rbac.seed.enabled: false` in `application.yml`.

## 8. Steps to Run

**Prerequisites:** Java 17+, Maven 3.9+ (or use the included `mvnw` if you add one).

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. H2 console (dev only) is at
`http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:rbacdb`, user `sa`, no password.

### Try it with curl

```bash
# Admin creates a new permission
curl -u admin:admin123 -X POST http://localhost:8080/permissions \
     -H "Content-Type: application/json" -d '{"name":"REPORT_VIEW"}'

# Admin creates a new role
curl -u admin:admin123 -X POST http://localhost:8080/roles \
     -H "Content-Type: application/json" -d '{"name":"AUDITOR"}'

# Admin attaches the permission to the role (assume role id=3, permission id=8)
curl -u admin:admin123 -X POST http://localhost:8080/roles/3/permissions/8

# A new user registers
curl -X POST http://localhost:8080/users/register \
     -H "Content-Type: application/json" -d '{"username":"alice","password":"secret123"}'

# Admin assigns the AUDITOR role to alice (assume user id=3)
curl -u admin:admin123 -X POST http://localhost:8080/users/3/roles/3

# john (seeded USER) can read secure data out of the box
curl -u john:user123 http://localhost:8080/secure-data
```

## 9. Testing & Coverage

```bash
mvn clean test jacoco:report
```

HTML coverage report: `target/site/jacoco/index.html`. The build is also configured
(`jacoco-maven-plugin` `check` goal, bound to `verify`) to **fail the build** if line
coverage drops below 80%:

```bash
mvn clean verify
```

Test suite layout:
- `security/CustomPermissionEvaluatorTest` — the core authorization-decision logic
- `security/CustomUserDetailsServiceTest`
- `service/*ImplTest` — business logic, mocked repositories
- `exception/GlobalExceptionHandlerTest`
- `integration/AuthorizationFlowIntegrationTest` — full HTTP, real H2 database, drives
  the entire create-permission → create-role → assign → check-access flow exactly like
  a real client, including 401/403/404/409/400 paths

## 10. SonarQube Integration

The `pom.xml` includes `jacoco-maven-plugin` (coverage) and `sonar-maven-plugin`
already wired together (`sonar.coverage.jacoco.xmlReportPaths` points at JaCoCo's XML
report). With a SonarQube server running locally (or `docker run -d -p 9000:9000
sonarqube:community`):

```bash
mvn clean verify sonar:sonar \
    -Dsonar.host.url=http://localhost:9000 \
    -Dsonar.login=<your-sonarqube-token>
```

This runs the tests, generates the JaCoCo report, then uploads code quality +
coverage metrics to SonarQube.

## 11. Deployment (optional)

A `Dockerfile` is included. To deploy on Render/Railway:

1. Push this repo to GitHub.
2. Create a new "Web Service" pointing at the repo; it will detect the `Dockerfile`.
3. Set the service port to `8080` (matches `application.yml`).
4. No environment variables are required — H2 is in-memory, so data resets on
   restart. For a persistent deployment, swap the `spring.datasource.url` to a
   managed Postgres instance and add the `postgresql` driver dependency.

Local Docker:
```bash
docker build -t rbac-engine .
docker run -p 8080:8080 rbac-engine
```

## 12. Assumptions & Design Decisions

- **`POST /users/register` was added.** The spec's API list has no endpoint for
  creating a user, but `POST /users/{userId}/roles/{roleId}` clearly requires users to
  already exist. Registration is deliberately `permitAll` (a brand-new account has no
  credentials to authenticate with yet) and grants **zero roles/permissions** by
  default — an admin must explicitly assign a role afterward, matching the spec's
  admin-driven assignment flow.
- **Admin-labeled endpoints use dynamic permissions, not `hasRole("ADMIN")`.** The spec
  labels several endpoints "Role: ADMIN", but the mandatory rule "no hardcoded role
  checks" is treated as applying project-wide, not just to `/secure-data`. All five
  admin-facing endpoints are secured with granular permissions instead, which happen to
  all be granted to the seeded `ADMIN` role — but nothing in the code assumes a role
  named `ADMIN` exists.
- **`CommandLineRunner` seeding instead of `data.sql`.** Chosen so the seeded admin
  password is BCrypt-hashed by the real `PasswordEncoder` bean at startup, rather than
  a hand-computed hash pasted into SQL.
- **HTTP Basic auth**, not JWT. The spec doesn't mandate a token scheme and Basic auth
  keeps the demo focused on the authorization engine itself rather than token
  plumbing; swapping in JWT would only change the authentication filter, not
  `CustomPermissionEvaluator`.
- **Optional caching implemented.** `CustomPermissionEvaluator#resolveUserPermissions`
  is `@Cacheable("userPermissions")`; every assignment-changing operation
  (`AssignmentServiceImpl`) does a simple `@CacheEvict(allEntries = true)`. This trades
  a small window of "evict more than strictly necessary" for a much simpler mental
  model than per-user cache key invalidation — acceptable for this scale.
- **`GET /roles` and `GET /permissions`** were added beyond the spec's explicit list,
  guarded by `ROLE_READ`/`PERMISSION_READ`, purely so an admin (and the test suite) can
  discover the IDs generated by the `POST` endpoints without needing the H2 console.
#   d y n a m i c - r b a c - a u t h o r i z a t i o n - e n g i n e  
 