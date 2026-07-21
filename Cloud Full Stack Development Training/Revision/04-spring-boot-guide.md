# Spring Boot — Complete Line-by-Line Guide

## Intro — What This Guide Covers, and an Important Honesty Check

This guide is built strictly from Aakash's course materials: the four courseware files (`04_Spring_Boot_Basics.md`, `05_Spring_Data_JPA.md`, `07_Spring_Security.md`, `08_Spring_Boot_Advanced.md`) plus the real project at `Code/Springboot`. Every code sample below is reproduced from those sources — nothing is invented.

**Critical finding, stated honestly up front:** the folder named `Code/Springboot` is **not actually a Spring Boot project**, despite the folder name. Verified facts:

- `pom.xml` -> `groupId=com.demo`, `artifactId=acme-spring-mvc-demo`, `packaging=war`. There is **no** `spring-boot-starter-parent`, **no** `spring-boot-maven-plugin`, and **no** `spring-boot-starter-*` dependency anywhere. Dependencies are raw `spring-webmvc`, `spring-jdbc`, `spring-orm`, `hibernate-core`, `spring-data-mongodb`, plus `jakarta.servlet-api` (scope `provided`) and `tomcat-embed-jasper` for JSP.
- `grep -rl "SpringBootApplication" src` returns **zero matches** — there is no `@SpringBootApplication` class anywhere in the project, and no dedicated `main()` entry point at all.
- `AppInitializer.java` extends `AbstractAnnotationConfigDispatcherServletInitializer` — this is the **classic Spring MVC** programmatic replacement for `web.xml` (Servlet 3.0+ `ServletContainerInitializer` mechanism), not anything Spring Boot provides.
- The project is deployed as a **WAR** (`<packaging>war</packaging>`) to an external servlet container, not run as an executable JAR with an embedded server.

So: **this is the same "Code/Spring MVC" style EMS project you've already seen, reused/mislabeled under a "Springboot" folder — it is a hand-configured, Java-config Spring MVC + Hibernate + Spring Data MongoDB + Spring JDBC application.** It is genuinely useful for this guide because the courseware's own EMS narrative walks the *same* Employee/Department/Project domain from Spring MVC to Spring Boot, and this repo happens to preserve the "before" state (manual config) while the courseware's `04_Spring_Boot_Basics.md` describes the "after" state (`@SpringBootApplication`, starters, `application.properties`, embedded Tomcat). Where the two diverge, this guide calls it out explicitly rather than pretending the repo is genuine Boot.

**What true Spring Boot removes/automates, per the courseware, versus what this repo still does by hand:**

| Concern | Classic Spring MVC (this repo, `Code/Springboot`) | True Spring Boot (per `04_Spring_Boot_Basics.md`) |
|---|---|---|
| Bootstrapping | `AppInitializer` (`AbstractAnnotationConfigDispatcherServletInitializer`) registers `DispatcherServlet` manually, mapped in `getServletMappings()` | `@SpringBootApplication` + `SpringApplication.run()` — Boot auto-registers `DispatcherServlet` via `DispatcherServletAutoConfiguration` |
| Server | None bundled — WAR deployed to external Tomcat (needs `tomcat-embed-jasper` just for JSP compiling, `jakarta.servlet-api` as `provided`) | Embedded Tomcat inside the JAR — `java -jar app.jar`, no external server install |
| DataSource | `H2Config` manually builds a `DriverManagerDataSource` bean, wires `JdbcTemplate` and a `DataSourceInitializer` for SQL scripts by hand | Auto-configured from `spring.datasource.*` properties — `DataSourceAutoConfiguration` builds the bean for you |
| Hibernate/JPA | `HibernateConfig` manually builds `LocalSessionFactoryBean`, sets `hibernate.dialect`/`hibernate.hbm2ddl.auto`/`hibernate.show_sql` as raw `Properties`, and a separate `HibernateTransactionManager` bean | `spring-boot-starter-data-jpa` + `spring.jpa.*` properties — `HibernateJpaAutoConfiguration` wires `EntityManagerFactory` and `JpaTransactionManager` automatically |
| Web MVC | `WebConfig` manually adds `@EnableWebMvc`, `@ComponentScan`, and hand-builds an `InternalResourceViewResolver` bean for JSP | Auto-configured `ViewResolver`/Jackson message converters via `WebMvcAutoConfiguration`; JSP isn't even the default (Thymeleaf is, per `08_Spring_Boot_Advanced.md`) |
| MongoDB | `MongoConfig extends AbstractMongoClientConfiguration`, manually overrides `getDatabaseName()` and builds a `MongoClients.create(...)` bean | `spring-boot-starter-data-mongodb` + `spring.data.mongodb.*` properties — auto-configured |
| Config files | `application.properties` here is *not* Boot's central config — it holds a handful of ad hoc keys (`mongodb.host`, `h2.url`, etc.) that nothing in the code actually binds via `@ConfigurationProperties`/`Environment` in the read files; each `*Config` class hardcodes its own connection constants instead | One `application.properties`/`.yml` (plus profile variants) that Boot's auto-configuration classes read directly — `spring.datasource.url`, `spring.jpa.hibernate.ddl-auto`, etc. |
| Packaging | WAR, external deploy | Executable JAR, `mvn spring-boot:run` or `java -jar` |

Keep this comparison in mind through Section 6 — the real project is annotated as "what Boot replaces," not as a Boot example.

---

# Part A — Courseware Topics

## 1. Spring Boot Basics (`04_Spring_Boot_Basics.md`)

### 1.1 Why Spring Boot — the pitch

Traditional Spring needs: XML/Java config for `DispatcherServlet`, manual `DataSource`/`TransactionManager`/`JdbcTemplate` beans, WAR + external Tomcat, and separate configs per environment. Spring Boot auto-configures nearly all of it. The courseware's analogy: **traditional Spring is building a PC from parts; Spring Boot is buying a pre-assembled laptop** — same power, still user-serviceable if you open it up (i.e., you can still override any auto-configured bean).

Cross-framework comparison for you: this is directly analogous to Django's app-based auto-registration (`INSTALLED_APPS` + convention-based settings) versus Flask's "wire everything yourself" style — Spring Boot behaves like Django in that it scans what's "installed" (on the classpath) and configures sensible defaults, while classic Spring MVC behaves like Flask/vanilla WSGI where you assemble every piece.

Key Spring Boot features named in the courseware:
- **Auto-configuration** — detects libraries on the classpath and configures them.
- **Embedded server** — Tomcat/Jetty inside the JAR.
- **Opinionated defaults** — overridable sensible defaults.
- **Spring Initializr** — generates a starter project (start.spring.io).
- **Actuator** — production monitoring out of the box.

### 1.2 Project setup and structure

Recommended generation flow: start.spring.io -> Maven, Java 17, Spring Boot 3.2.x, packaging Jar, dependencies Spring Web / Spring Data JPA / MySQL Driver / Lombok / DevTools.

Generated structure:
```
ems-api/
+-- src/main/java/com/ems/
    +-- EmsApiApplication.java    <- Entry point
    +-- controller/ service/ repository/ model/
    +-- resources/application.properties (+ -dev / -prod variants)
+-- pom.xml
+-- mvnw
```
The `mvnw` (Maven wrapper) means no local Maven install is required — comparable to how a `poetry`/`pipenv` lockfile pins tooling in a Python project, though here it also ships the build tool binary itself.

### 1.3 `@SpringBootApplication` and auto-configuration internals

```java
1:  @SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
2:  public class EmsApiApplication {
3:      public static void main(String[] args) {
4:          SpringApplication.run(EmsApiApplication.class, args);
5:      }
6:  }
```
- **Line 1** — `@SpringBootApplication` is a meta-annotation bundling three annotations: `@Configuration` (this class can declare `@Bean` methods), `@EnableAutoConfiguration` (turn on classpath-driven auto-config), and `@ComponentScan` (scan this package and sub-packages for `@Component`/`@Service`/`@Repository`/`@Controller`). This is the single annotation that replaces the 50+ lines of manual config the courseware contrasts it against.
- **Line 4** — `SpringApplication.run(...)` boots the embedded servlet container, creates the `ApplicationContext`, and triggers auto-configuration. This is the Boot equivalent of Django's `manage.py runserver` combined app-loading step, except it also starts the HTTP server process itself (embedded Tomcat) rather than delegating to WSGI/ASGI.

**How auto-configuration works**, per the courseware: on startup Boot scans the classpath for known libraries (Jackson, Hibernate, MySQL driver, etc.), applies a default config for each one found, lets your `application.properties` override the defaults, and — critically — **backs off if you define your own `@Bean` of the same type** (your bean wins). The actual mechanism: `spring-boot-autoconfigure.jar` contains `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, listing 100+ conditional `@Configuration` classes (e.g. `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, `DispatcherServletAutoConfiguration`, `JacksonAutoConfiguration`). To see exactly what got auto-configured and why: `logging.level.org.springframework.boot.autoconfigure=DEBUG`.

### 1.4 `application.properties` — central configuration

```properties
1:  # -- Server ----------------------------------------------
2:  server.port=8080
3:  server.servlet.context-path=/ems
4:
5:  # -- Database ----------------------------------------------
6:  spring.datasource.url=jdbc:mysql://localhost:3306/emsdb
7:  spring.datasource.username=root
8:  spring.datasource.password=password
9:  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
10:
11: # -- JPA / Hibernate -----------------------------------------
12: spring.jpa.hibernate.ddl-auto=update
13: spring.jpa.show-sql=true
14: spring.jpa.properties.hibernate.format_sql=true
15: spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
16:
17: # -- Logging -------------------------------------------------
18: logging.level.com.ems=DEBUG
19: logging.level.org.springframework.web=INFO
20:
21: # -- Application custom properties ---------------------------
22: ems.app.name=Employee Management System
23: ems.app.max-employees=500
```
- **Lines 2–3** — `server.port` / `server.servlet.context-path` configure the embedded Tomcat directly through properties — no `web.xml`, no manual `ServletContextInitializer`.
- **Lines 6–9** — these four keys are all `DataSourceAutoConfiguration` needs to build a working `DataSource` bean; contrast with `H2Config` in the real project, which builds the same kind of object (`DriverManagerDataSource`) entirely by hand in Java.
- **Lines 12–15** — `spring.jpa.hibernate.ddl-auto` controls schema generation (`update`/`validate`/`create-drop`, etc.); this is the properties-driven equivalent of the raw `Properties` object `HibernateConfig.sessionFactory()` builds manually in the real project (`hibernate.hbm2ddl.auto`, `hibernate.dialect`, `hibernate.show_sql`).
- **Lines 18–19** — per-package log level tuning, no Logback XML required.
- **Lines 22–23** — arbitrary custom properties; bindable into your own `@ConfigurationProperties` class or injected with `@Value("${ems.app.name}")`.

### 1.5 `pom.xml` — starter POMs

```xml
1:  <parent>
2:      <groupId>org.springframework.boot</groupId>
3:      <artifactId>spring-boot-starter-parent</artifactId>
4:      <version>3.2.0</version>
5:  </parent>
6:
7:  <dependencies>
8:      <dependency>
9:          <groupId>org.springframework.boot</groupId>
10:         <artifactId>spring-boot-starter-web</artifactId>
11:     </dependency>
12:     <dependency>
13:         <groupId>org.springframework.boot</groupId>
14:         <artifactId>spring-boot-starter-data-jpa</artifactId>
15:     </dependency>
16:     ...
17: </dependencies>
```
- **Lines 1–5** — `spring-boot-starter-parent` is a Maven parent POM that centrally manages dependency **versions** (a Bill-of-Materials) so you never specify a version per Boot dependency — one of the biggest ergonomic wins over hand-rolled Spring, where you pin `spring-webmvc`, `spring-orm`, `hibernate-core` versions individually and must keep them mutually compatible yourself (exactly what `Code/Springboot`'s `pom.xml` does: `spring-webmvc 6.1.8`, `spring-orm 6.1.8`, `hibernate-core 6.5.2.Final`, `spring-data-mongodb 4.3.1` — all pinned by hand).
- **Lines 9–11 / 12–14** — `spring-boot-starter-web` pulls in Spring MVC + embedded Tomcat + Jackson in one line; `spring-boot-starter-data-jpa` pulls in Spring Data JPA + Hibernate + a transaction manager in one line. Compare to the real project's `pom.xml`, which lists `spring-webmvc`, `spring-orm`, `hibernate-core`, and `spring-jdbc` as four **separate, individually-versioned** dependencies — precisely what a "starter" collapses into one.

### 1.6 Sample EMS resources (Entity/Repository/Service/Controller)

The courseware walks a full `Employee` vertical slice using Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`), `JpaRepository`, constructor-injected `@Service`, and a `@RestController`. This is expanded fully in Section 2 (JPA) and Section 6 (real project) below, so it isn't duplicated here — the key novel piece for *this* section is `@ResponseStatus`:

```java
1:  @ResponseStatus(HttpStatus.NOT_FOUND)
2:  public class ResourceNotFoundException extends RuntimeException {
3:      public ResourceNotFoundException(String message) {
4:          super(message);
5:      }
6:  }
```
- **Line 1** — `@ResponseStatus` tells Spring MVC to map this exception type to an HTTP 404 automatically whenever it propagates uncaught out of a controller method — no `try/catch` needed in the controller itself.

Running: `mvn spring-boot:run` (dev loop) or `java -jar target/ems-api-1.0-SNAPSHOT.jar` (the whole point of embedded Tomcat — the JAR *is* the deployable artifact, no WAR/external container needed, unlike `Code/Springboot`'s `<packaging>war</packaging>`).

### 1.7 Profiles

Profiles let you swap configuration per environment (dev uses H2, prod uses MySQL) without changing code.

```properties
1:  # application.properties (common)
2:  ems.app.name=Employee Management System
3:  spring.profiles.active=dev
```
```properties
1:  # application-dev.properties
2:  spring.datasource.url=jdbc:h2:mem:emsdb
3:  spring.datasource.driver-class-name=org.h2.Driver
4:  spring.jpa.hibernate.ddl-auto=create-drop
5:  spring.h2.console.enabled=true
```
- Boot loads `application.properties` always, then layers `application-{profile}.properties` on top based on the active profile — later values override earlier ones.
- Activation: CLI arg `--spring.profiles.active=prod`, env var `SPRING_PROFILES_ACTIVE=prod`, or the property itself. This is directly analogous to Django's `DJANGO_SETTINGS_MODULE` swapping between `settings/dev.py` and `settings/prod.py`, or a `.env`-driven `FLASK_ENV`.

Profile-scoped beans:
```java
1:  @Bean
2:  @Profile("dev")
3:  public NotificationService mockNotification() {
4:      return (email, msg) -> System.out.println("[MOCK] Email to " + email + ": " + msg);
5:  }
```
- **Line 2** — `@Profile("dev")` means this bean is only registered when the `dev` profile is active; a parallel `@Profile("prod")` bean provides the real implementation. Spring picks whichever bean matches the active profile at context-startup time — same interface, environment-swapped implementation, a la Django's settings-driven backend swapping (e.g., `EMAIL_BACKEND`).

### 1.8 Logging

Boot ships **SLF4J** (facade) + **Logback** (implementation) pre-wired — zero setup, unlike classic Spring where you'd configure a logging framework yourself.

```java
1:  private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
2:
3:  public Employee findById(Long id) {
4:      log.debug("Finding employee with id: {}", id);
5:      Employee emp = employeeRepository.findById(id)
6:          .orElseThrow(() -> {
7:              log.warn("Employee not found - id: {}", id);
8:              return new ResourceNotFoundException("Employee not found: " + id);
9:          });
10:     log.info("Found employee: {} in dept: {}", emp.getName(), ...);
11:     return emp;
12: }
```
- **Line 1** — one logger per class, named after the class; `{}` placeholders avoid string concatenation cost when the log level is disabled (Python analogy: `logging.debug("x=%s", x)` vs f-string interpolation).
- Lombok's `@Slf4j` class annotation auto-generates this `log` field so you don't write line 1 yourself.

Levels in order of increasing severity: `TRACE -> DEBUG -> INFO -> WARN -> ERROR`. Setting a level shows that level and everything more severe (`logging.level.com.ems=DEBUG` shows DEBUG/INFO/WARN/ERROR but not TRACE).

Config keys: `logging.level.<package>=<LEVEL>`, `logging.file.name`, `logging.file.max-size`, `logging.pattern.console`/`logging.pattern.file`. For finer control (per-profile appenders, rolling files), drop a `logback-spring.xml` with `<springProfile name="dev">...</springProfile>` blocks — Boot understands the `springProfile` tag natively (plain `logback.xml` does not get profile-awareness).

### 1.9 Actuator — health checks and metrics

Adding one dependency exposes production-ready monitoring endpoints with zero code:
```xml
1:  <dependency>
2:      <groupId>org.springframework.boot</groupId>
3:      <artifactId>spring-boot-starter-actuator</artifactId>
4:  </dependency>
```
```properties
1:  management.endpoints.web.exposure.include=health,info,metrics,env,beans,mappings
2:  management.endpoint.health.show-details=always
3:  info.app.name=EMS API
```
Key endpoints (all under `/actuator` by default, configurable via `management.endpoints.web.base-path`): `/health` (app + DB status), `/info` (custom metadata), `/metrics` (JVM/CPU/request counts), `/beans` (full bean graph), `/mappings` (URL->controller table), `/env` (resolved config), `/loggers` (runtime-adjustable log levels — you can flip a package to DEBUG in production without redeploying).

Custom health indicator:
```java
1:  @Component
2:  public class EmsHealthIndicator implements HealthIndicator {
3:      @Autowired
4:      private EmployeeRepository employeeRepository;
5:
6:      @Override
7:      public Health health() {
8:          try {
9:              long count = employeeRepository.count();
10:             return Health.up().withDetail("employeeCount", count).build();
11:         } catch (Exception e) {
12:             return Health.down().withDetail("error", e.getMessage()).build();
13:         }
14:     }
15: }
```
- **Line 2** — implementing `HealthIndicator` and registering it as a `@Component` automatically merges this into `/actuator/health`'s `components` map — Actuator discovers it by type, no manual registration.

Custom metrics use Micrometer's `MeterRegistry`/`Counter`, injected via constructor, incremented on business events (`hireCounter.increment()`), and queryable at `/actuator/metrics/ems.employees.hired`.

### 1.10 Section 1 Summary Table (from courseware)

| Feature | How | Key Property/Annotation |
|---|---|---|
| Auto-configuration | Classpath detection | `@SpringBootApplication` |
| Run the app | Embedded Tomcat | `SpringApplication.run()` |
| Configuration | Properties files | `application.properties` |
| Dev/Prod environments | Profiles | `spring.profiles.active` |
| Logging | SLF4J + Logback | `logging.level.com.ems=DEBUG` |
| Health monitoring | Actuator | `spring-boot-starter-actuator` |
| Custom metrics | Micrometer | `MeterRegistry`, `Counter` |
| DevTools | Hot reload | `spring-boot-devtools` |

---

## 2. Spring Data JPA (`05_Spring_Data_JPA.md`)

### 2.1 What Spring Data JPA removes

Sits on top of JPA + Hibernate; you write an interface, Spring generates the implementation at runtime — `save()`, `findById()`, `findAll()`, `delete()`, derived-name queries, pagination/sorting all come free. This is the Java equivalent of Django's `Model.objects` manager — except here *you* declare the repository interface (Spring proxies it at runtime), whereas Django generates the manager implicitly from the model class.

### 2.2 Entity mapping — full EMS model

```java
1:  @Entity
2:  @Table(name = "employees",
3:         indexes = {
4:             @Index(name = "idx_email", columnList = "email"),
5:             @Index(name = "idx_dept", columnList = "department_id")
6:         })
7:  @Data @NoArgsConstructor @AllArgsConstructor @Builder
8:  public class Employee {
9:
10:     @Id
11:     @GeneratedValue(strategy = GenerationType.IDENTITY)
12:     private Long id;
13:
14:     @Column(name = "first_name", nullable = false, length = 50)
15:     private String firstName;
16:
17:     @Column(unique = true, nullable = false, length = 150)
18:     private String email;
19:
20:     @Column(nullable = false, precision = 10, scale = 2)
21:     private BigDecimal salary;
22:
23:     @Enumerated(EnumType.STRING)
24:     @Column(name = "status")
25:     private EmployeeStatus status = EmployeeStatus.ACTIVE;
26:
27:     @ManyToOne(fetch = FetchType.LAZY)
28:     @JoinColumn(name = "department_id", nullable = false)
29:     private Department department;
30:
31:     @ManyToMany(fetch = FetchType.LAZY)
32:     @JoinTable(name = "employee_roles",
33:         joinColumns = @JoinColumn(name = "employee_id"),
34:         inverseJoinColumns = @JoinColumn(name = "role_id"))
35:     private Set<Role> roles = new HashSet<>();
36:
37:     @CreationTimestamp
38:     @Column(name = "created_at", updatable = false)
39:     private LocalDateTime createdAt;
40:
41:     @Transient
42:     public String getFullName() { return firstName + " " + lastName; }
43:
44:     public enum EmployeeStatus { ACTIVE, ON_LEAVE, TERMINATED }
45: }
```
- **Line 1** — `@Entity` marks the class as a JPA-managed persistent type; Hibernate creates a proxy/mapping for it. This is the single mandatory annotation — without it, none of the other JPA annotations do anything.
- **Line 2** — `@Table(name=...)` maps the class to a specific table name (defaults to the class name if omitted); `indexes` declares DB indexes as part of DDL generation.
- **Line 7** — Lombok annotations generate getters/setters/`toString`/`equals`/`hashCode` (`@Data`), a no-arg constructor JPA requires internally (`@NoArgsConstructor`), an all-args constructor (`@AllArgsConstructor`), and a fluent builder (`@Builder`) — this is boilerplate JPA itself does *not* generate; Lombok is a compile-time code generator, not a Spring feature.
- **Lines 10–12** — `@Id` marks the primary-key field; `@GeneratedValue(strategy = GenerationType.IDENTITY)` delegates PK generation to the DB's auto-increment column (MySQL `AUTO_INCREMENT`).
- **Lines 14, 17, 20** — `@Column` customizes the mapped column: name override, `nullable`, `unique`, `length` (VARCHAR size), or `precision`/`scale` for exact decimal columns (critical for money — hence `BigDecimal salary`, not `double`, to avoid floating-point rounding errors).
- **Lines 23–25** — `@Enumerated(EnumType.STRING)` stores the enum's *name* (`"ACTIVE"`) rather than its ordinal index, so column values stay stable if enum constants are reordered later — always prefer `STRING` over the default `ORDINAL`.
- **Lines 27–29** — `@ManyToOne` + `@JoinColumn` is the "many" side of a one-to-many: many `Employee` rows reference one `Department` row via the `department_id` FK column; `fetch = FetchType.LAZY` means the department isn't loaded from the DB until `.getDepartment()` is actually called.
- **Lines 31–35** — `@ManyToMany` + `@JoinTable` describes a peer-to-peer relationship backed by a join table (`employee_roles`), with `joinColumns`/`inverseJoinColumns` naming the two FK columns in that join table.
- **Lines 37–39** — `@CreationTimestamp` auto-populates the field with the insert timestamp; `updatable = false` prevents it from ever being touched by later UPDATEs.
- **Lines 41–42** — `@Transient` excludes a field/method from persistence — it's a computed value, never a DB column.

### 2.3 Repository pattern

```java
1:  @Repository
2:  public interface EmployeeRepository extends JpaRepository<Employee, Long> {
3:      // custom methods go here
4:  }
```
- **Line 2** — `JpaRepository<Employee, Long>` — first generic parameter is the entity type, second is the primary key's type. Extending it gives you `save`, `saveAll`, `findById`, `findAll`, `existsById`, `count`, `deleteById`, `delete`, `deleteAll`, `flush` — all implemented by a Spring-generated runtime proxy; you never write a class body. This is the direct Java analogue of Django's `Model.objects` manager, except explicit and interface-driven rather than implicit.
- **Line 1** — `@Repository` is technically optional on interfaces extending `JpaRepository` (Spring Data detects them via the extended type), but it documents intent and enables Spring's exception-translation (wrapping DB-specific exceptions into Spring's `DataAccessException` hierarchy).

### 2.4 Derived query methods

```java
1:  Optional<Employee> findByEmail(String email);
2:  List<Employee> findByStatus(Employee.EmployeeStatus status);
3:  List<Employee> findByDepartmentName(String deptName);        // traverses relationship
4:  List<Employee> findBySalaryGreaterThan(BigDecimal amount);
5:  List<Employee> findByFirstNameContainingIgnoreCase(String name);
6:  List<Employee> findByDepartmentNameAndStatus(String deptName, Employee.EmployeeStatus status);
7:  List<Employee> findByDepartmentNameOrderBySalaryDesc(String deptName);
8:  long countByDepartmentId(Long deptId);
9:  boolean existsByEmail(String email);
10: void deleteByStatus(Employee.EmployeeStatus status);
```
- **Line 1** — Spring parses the method name (`findBy` + `Email`), matches `Email` to the entity's `email` field, and generates `WHERE email = ?` — no SQL, no annotation.
- **Line 3** — `findByDepartmentName` traverses the `department` association's `name` field — Spring Data JPA understands nested-property navigation from camelCase method names alone.
- **Line 6** — `And` in the method name compiles multiple conditions with SQL `AND`.
- **Line 7** — `OrderBy...Desc` appends an `ORDER BY salary DESC` clause.
- Keyword reference table from the courseware: `And`/`Or`/`Between`/`LessThan`/`GreaterThan`/`Like`/`Containing`/`StartingWith`/`OrderBy`/`IsNull`/`In` all map to their obvious SQL equivalents.

### 2.5 JPQL and native queries

```java
1:  @Query("SELECT e FROM Employee e WHERE e.salary > :minSalary " +
2:         "AND e.status = 'ACTIVE' ORDER BY e.salary DESC")
3:  List<Employee> findHighEarners(@Param("minSalary") BigDecimal minSalary);
4:
5:  @Query(value = "SELECT * FROM employees WHERE YEAR(join_date) = :year",
6:         nativeQuery = true)
7:  List<Employee> findHiredInYear(@Param("year") int year);
8:
9:  @Modifying
10: @Transactional
11: @Query("UPDATE Employee e SET e.salary = e.salary * :factor WHERE e.department.id = :deptId")
12: int applyRaiseToAllInDept(@Param("deptId") Long deptId, @Param("factor") double factor);
```
- **Lines 1–3** — `@Query` with JPQL (Java Persistence Query Language) references **entity/field names** (`Employee`, `e.salary`), not table/column names — it's a Java-object-oriented query language that Hibernate translates to SQL. `@Param` binds named parameters (`:minSalary`) to method arguments.
- **Lines 5–7** — `nativeQuery = true` switches to raw SQL against actual table/column names (`employees`, `join_date`) — used when JPQL can't express something (DB-specific functions like `YEAR()`).
- **Lines 9–11** — `@Modifying` is required on any `@Query` that isn't a `SELECT` (UPDATE/DELETE); paired with `@Transactional` since bulk writes must run inside a transaction. Returns the number of affected rows (`int`).

DTO projection via JPQL constructor expressions (`SELECT new com.ems.dto.EmployeeSummaryDto(...)`) lets you fetch a slim, denormalized view without loading full entities — analogous to Django's `.values()`/`only()` projections.

### 2.6 Entity relationships, fetch types, cascades

- `@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)` on the "one" side; `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(...)` on the "many" side — `mappedBy` marks the non-owning side (the FK physically lives on the `@ManyToOne` side).
- Fetch type defaults: `LAZY` for `@OneToMany`/`@ManyToMany`, `EAGER` for `@ManyToOne`/`@OneToOne` — courseware recommends **always forcing LAZY** and loading explicitly when needed.
- **N+1 problem**, explicitly called out: loading 100 employees then calling `.getDepartment()` on each fires 100 extra queries under LAZY loading. Fix: `@Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")` — `JOIN FETCH` pulls the association in the same query.
- Cascade types: `CascadeType.ALL` (propagate every operation to children), `PERSIST` (save child when parent saved), `REMOVE` (delete children when parent deleted), and `orphanRemoval = true` (delete a child automatically if it's removed from the parent's collection, even without an explicit delete cascade).

### 2.7 Pagination and sorting

```java
1:  @GetMapping
2:  public Page<Employee> getEmployees(
3:          @RequestParam(defaultValue = "0")  int page,
4:          @RequestParam(defaultValue = "10") int size,
5:          @RequestParam(defaultValue = "lastName") String sortBy,
6:          @RequestParam(defaultValue = "asc") String direction) {
7:
8:      Sort sort = direction.equalsIgnoreCase("desc")
9:          ? Sort.by(sortBy).descending()
10:         : Sort.by(sortBy).ascending();
11:
12:     Pageable pageable = PageRequest.of(page, size, sort);
13:     return employeeRepository.findAll(pageable);
14: }
```
- **Line 12** — `PageRequest.of(page, size, sort)` builds a `Pageable` (page index, page size, sort spec); `JpaRepository.findAll(Pageable)` is a built-in overload that returns a `Page<T>` — no custom count-query or offset math needed, unlike hand-rolled `LIMIT ?, ?` SQL. `Page<T>` carries `content`, `totalElements`, `totalPages`, `first`/`last` flags in its JSON serialization — comparable to DRF's `PageNumberPagination` response envelope.

### 2.8 Database caching

```java
1:  @Cacheable(value = "employees", key = "#id")
2:  public Employee findById(Long id) {
3:      System.out.println("Fetching from DB: " + id); // only prints on first call
4:      return employeeRepository.findById(id)
5:          .orElseThrow(() -> new ResourceNotFoundException("Not found: " + id));
6:  }
7:
8:  @CachePut(value = "employees", key = "#result.id")
9:  public Employee save(Employee emp) { return employeeRepository.save(emp); }
10:
11: @CacheEvict(value = "employees", key = "#id")
12: public void delete(Long id) { employeeRepository.deleteById(id); }
```
- **Line 1** — `@Cacheable` caches the method's return value keyed by `#id` (SpEL expression referencing the parameter); subsequent calls with the same `id` skip the method body entirely and return the cached object.
- **Line 8** — `@CachePut` always executes the method (unlike `@Cacheable`) *and* writes the result into the cache — used for writes that must also refresh the cache. `#result` refers to the method's return value in SpEL.
- **Line 11** — `@CacheEvict` removes an entry (or, with `allEntries = true`, the whole cache region) when data changes — keeps the cache consistent with the DB.
- Requires `@EnableCaching` on the `@SpringBootApplication` class plus `spring-boot-starter-cache` (optionally backed by Redis via `spring-boot-starter-data-redis` and `spring.cache.redis.time-to-live`).

### 2.9 Validations

```java
1:  @Data
2:  public class EmployeeRequest {
3:      @NotBlank(message = "First name is required")
4:      @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
5:      private String firstName;
6:
7:      @NotBlank(message = "Email is required")
8:      @Email(message = "Invalid email format")
9:      private String email;
10:
11:     @NotNull(message = "Salary is required")
12:     @DecimalMin(value = "10000.00", message = "Salary must be at least 10,000")
13:     private BigDecimal salary;
14: }
```
- **Lines 3–4** — `@NotBlank` rejects null/empty/whitespace-only strings; `@Size` bounds string length. Requires `spring-boot-starter-validation` (Jakarta Bean Validation).
- **Line 8** — `@Email` validates format via regex.

```java
1:  @PostMapping
2:  public ResponseEntity<Employee> create(@Valid @RequestBody EmployeeRequest request) {
3:      Employee emp = employeeService.create(request);
4:      return ResponseEntity.status(HttpStatus.CREATED).body(emp);
5:  }
```
- **Line 2** — `@Valid` on the `@RequestBody` parameter triggers Bean Validation before the method body runs; if validation fails, Spring throws `MethodArgumentNotValidException` before your code ever executes — you don't call a `.validate()` method yourself, unlike, e.g., a manually-invoked Pydantic/marshmallow schema in Flask.

Centralised error handling:
```java
1:  @RestControllerAdvice
2:  public class GlobalExceptionHandler {
3:
4:      @ExceptionHandler(MethodArgumentNotValidException.class)
5:      public ResponseEntity<Map<String, Object>> handleValidationErrors(
6:              MethodArgumentNotValidException ex) {
7:          Map<String, String> fieldErrors = new HashMap<>();
8:          ex.getBindingResult().getFieldErrors().forEach(err ->
9:              fieldErrors.put(err.getField(), err.getDefaultMessage())
10:         );
11:         ...
12:         return ResponseEntity.badRequest().body(response);
13:     }
14:
15:     @ExceptionHandler(ResourceNotFoundException.class)
16:     public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) { ... }
17: }
```
- **Line 1** — `@RestControllerAdvice` (`= @ControllerAdvice + @ResponseBody`) applies these exception handlers globally, across **every** `@RestController` in the app — one place to catch `MethodArgumentNotValidException` (validation failures), custom exceptions like `ResourceNotFoundException`, and a catch-all `Exception` handler, rather than try/catch in each controller method. This is the Spring analogue of a Flask/Django global error handler / DRF exception handler.
- **Line 4** — `@ExceptionHandler(Type.class)` routes any uncaught exception of that type (thrown anywhere in a request's call stack) to this method.

### 2.10 Section 2 Summary Table (from courseware)

| Feature | Annotation/Class | Purpose |
|---|---|---|
| Entity | `@Entity`, `@Table` | Map class to DB table |
| Primary Key | `@Id`, `@GeneratedValue` | Auto-generated PK |
| Column | `@Column` | Map field to column |
| One-to-Many | `@OneToMany`, `@ManyToOne` | Parent-child relationship |
| Many-to-Many | `@ManyToMany`, `@JoinTable` | Peer relationship |
| Repository | `JpaRepository<T, ID>` | Free CRUD operations |
| Derived Query | `findByNameAndStatus()` | SQL from method name |
| Custom Query | `@Query` | JPQL or native SQL |
| Bulk update | `@Modifying` + `@Query` | UPDATE/DELETE |
| Pagination | `PageRequest`, `Page<T>` | Paginated results |
| Caching | `@Cacheable`, `@CacheEvict` | Reduce DB load |
| Validation | `@Valid`, `@NotNull`, `@Email` | Input validation |
| Error handling | `@RestControllerAdvice` | Centralised exceptions |

---

## 3. Spring Security (`07_Spring_Security.md`)

### 3.1 Concepts and the filter chain

Authentication = "who are you?"; authorisation = "what can you do?" — courseware's analogy: a security guard checking an ID badge (authentication) plus an access-card system that only opens permitted floors (authorisation). Every request runs through a chain of servlet filters before reaching a controller: `JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter -> BasicAuthenticationFilter -> ExceptionTranslationFilter -> FilterSecurityInterceptor -> Controller`.

> Adding just `spring-boot-starter-security` to a project's classpath immediately locks down **every** endpoint behind HTTP Basic Auth with a randomly generated password printed to the console at startup — auto-configuration applying a maximally-secure-by-default posture, which you then relax explicitly via a `SecurityFilterChain` bean.

### 3.2 Basic authentication

```java
1:  @Configuration
2:  @EnableWebSecurity
3:  public class SecurityConfig {
4:
5:      @Bean
6:      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
7:          http
8:              .csrf(csrf -> csrf.disable())  // disable for REST APIs
9:              .authorizeHttpRequests(auth -> auth
10:                 .requestMatchers("/actuator/health").permitAll()
11:                 .requestMatchers("/api/v1/employees/**").authenticated()
12:                 .anyRequest().authenticated()
13:             )
14:             .httpBasic(Customizer.withDefaults());
15:         return http.build();
16:     }
17:
18:     @Bean
19:     public UserDetailsService userDetailsService() {
20:         UserDetails user = User.withDefaultPasswordEncoder()
21:             .username("alice").password("password").roles("EMPLOYEE").build();
22:         return new InMemoryUserDetailsManager(user, ...);
23:     }
24: }
```
- **Line 2** — `@EnableWebSecurity` activates Spring Security's web infrastructure (registers the filter chain into the servlet container).
- **Line 6** — the `SecurityFilterChain` bean is the modern (Spring Security 6+) way to configure HTTP security — a lambda-DSL builder replacing the older `WebSecurityConfigurerAdapter` subclassing style.
- **Line 8** — `csrf(csrf -> csrf.disable())` — CSRF protection (relevant for cookie/session-based browser forms) is disabled because stateless REST APIs authenticate via tokens/headers, not cookies, so CSRF doesn't apply the same way.
- **Lines 9–13** — `authorizeHttpRequests` declares URL-pattern-based access rules, evaluated top-to-bottom, first match wins; `anyRequest().authenticated()` is the catch-all default-deny-unless-logged-in rule at the end.
- **Line 14** — `httpBasic(...)` enables HTTP Basic Auth (credentials in the `Authorization: Basic base64(user:pass)` header on every request) — simple but stateful-feeling since credentials repeat each call; contrasted with JWT below.
- **Lines 18–23** — `UserDetailsService` is the interface Spring Security calls to look up a user by username; `InMemoryUserDetailsManager` is a trivial in-memory implementation for demos (production would query a `UserRepository` against a DB-backed `User` entity, shown next).

### 3.3 JWT authentication (the production pattern)

Flow: client `POST /auth/login` -> server validates credentials -> returns a signed JWT -> client sends `Authorization: Bearer <token>` on every subsequent request -> server validates the token's signature/expiry, no server-side session state (stateless).

```java
1:  @Entity
2:  @Table(name = "users")
3:  public class User implements UserDetails {
4:      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
5:      private Long id;
6:      @Column(unique = true, nullable = false)
7:      private String username;
8:      @Column(nullable = false)
9:      private String password;  // stored as BCrypt hash
10:     @ElementCollection(fetch = FetchType.EAGER)
11:     @CollectionTable(name = "user_roles")
12:     @Enumerated(EnumType.STRING)
13:     private Set<UserRole> roles = new HashSet<>();
14:
15:     @Override
16:     public Collection<? extends GrantedAuthority> getAuthorities() {
17:         return roles.stream()
18:             .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
19:             .collect(Collectors.toSet());
20:     }
21:     @Override public boolean isAccountNonExpired()  { return true; }
22: }
```
- **Line 3** — implementing `UserDetails` directly on the JPA entity is how Spring Security's authentication machinery understands your domain `User` — it needs `getUsername()`, `getPassword()`, `getAuthorities()`, and four account-status booleans.
- **Lines 16–20** — `getAuthorities()` converts your domain roles into Spring Security's `GrantedAuthority` objects, prefixing `ROLE_` — this prefix convention is required for `hasRole("ADMIN")` checks to match (Spring Security strips/adds `ROLE_` automatically depending on which API you use — `hasRole` vs `hasAuthority`).
- **Line 9** — password is stored **hashed** (BCrypt), never plaintext — hashing happens at registration time via `PasswordEncoder.encode(...)`, shown in `AuthController` below.

```java
1:  @Component
2:  public class JwtUtil {
3:      @Value("${ems.jwt.secret}")
4:      private String secretKey;
5:
6:      public String generateToken(UserDetails user) {
7:          Map<String, Object> claims = new HashMap<>();
8:          claims.put("roles", user.getAuthorities().stream()
9:              .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
10:         return Jwts.builder()
11:             .claims(claims)
12:             .subject(user.getUsername())
13:             .issuedAt(new Date())
14:             .expiration(new Date(System.currentTimeMillis() + expiration))
15:             .signWith(getSigningKey())
16:             .compact();
17:     }
18: }
```
- **Line 3** — `@Value("${ems.jwt.secret}")` injects a property value directly into a field — the signing secret lives in `application.properties`, not hardcoded.
- **Lines 10–16** — builds a signed JWT: claims (custom data, here `roles`), `subject` (username), `issuedAt`/`expiration` timestamps, `signWith` (HMAC signature using the secret key) — `compact()` serializes to the three-part `header.payload.signature` base64 string.

```java
1:  @Component
2:  public class JwtAuthenticationFilter extends OncePerRequestFilter {
3:      @Override
4:      protected void doFilterInternal(HttpServletRequest request,
5:                                      HttpServletResponse response,
6:                                      FilterChain chain) throws ServletException, IOException {
7:          final String authHeader = request.getHeader("Authorization");
8:          if (authHeader == null || !authHeader.startsWith("Bearer ")) {
9:              chain.doFilter(request, response);
10:             return;
11:         }
12:         String jwt = authHeader.substring(7); // strip "Bearer "
13:         String username = jwtUtil.extractUsername(jwt);
14:         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
15:             UserDetails user = userDetailsService.loadUserByUsername(username);
16:             if (jwtUtil.isTokenValid(jwt, user)) {
17:                 UsernamePasswordAuthenticationToken authToken =
18:                     new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
19:                 SecurityContextHolder.getContext().setAuthentication(authToken);
20:             }
21:         }
22:         chain.doFilter(request, response);
23:     }
24: }
```
- **Line 2** — `OncePerRequestFilter` guarantees this filter's logic runs exactly once per request even if the request is forwarded internally.
- **Lines 7–11** — no `Authorization: Bearer ...` header -> skip straight to the next filter (`chain.doFilter`), letting the rest of the chain decide (e.g., reject as unauthenticated later).
- **Lines 17–19** — on a valid token, manually constructs an authenticated `UsernamePasswordAuthenticationToken` and stashes it in `SecurityContextHolder` — this is what makes the rest of the request "logged in" for downstream authorization checks.

```java
1:  @Bean
2:  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
3:      http
4:          .csrf(csrf -> csrf.disable())
5:          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
6:          .authorizeHttpRequests(auth -> auth
7:              .requestMatchers("/api/auth/**").permitAll()
8:              .requestMatchers(HttpMethod.GET, "/api/v1/employees/**")
9:                  .hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
10:             .requestMatchers(HttpMethod.POST, "/api/v1/employees").hasAnyRole("HR", "ADMIN")
11:             .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")
12:             .anyRequest().authenticated()
13:         )
14:         .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
15:     return http.build();
16: }
```
- **Line 5** — `SessionCreationPolicy.STATELESS` tells Spring Security to never create or use an `HttpSession` — every request must carry its own proof of identity (the JWT), which is what makes horizontal scaling trivial (no sticky sessions / shared session store needed).
- **Lines 8–11** — per-HTTP-verb, per-path role rules: GET is broad (any authenticated role), POST/PUT restricted to HR/ADMIN, DELETE restricted to ADMIN only — this is the URL-level enforcement of the EMS role matrix (below).
- **Line 14** — `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` inserts the custom JWT filter into the chain *before* Spring's built-in username/password filter, so token-based auth is checked first.

EMS role matrix from the courseware:

| Endpoint | EMPLOYEE | MANAGER | HR | ADMIN |
|---|---|---|---|---|
| GET /employees | own | team | all | all |
| POST /employees | no | no | yes | yes |
| DELETE /employees/{id} | no | no | no | yes |
| GET /payroll | no | no | yes | yes |
| GET /reports | no | yes | yes | yes |

### 3.4 Method-level security

```java
1:  @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
2:  public Employee create(EmployeeRequest request) { ... }
3:
4:  @PreAuthorize("hasRole('ADMIN')")
5:  public void delete(Long id) { ... }
6:
7:  @PreAuthorize("hasRole('MANAGER') or #id == authentication.principal.id")
8:  public Employee findById(Long id) { ... }
9:
10: @PostAuthorize("returnObject.email == authentication.name or hasRole('ADMIN')")
11: public Employee getByEmail(String email) { ... }
```
- Enabled by `@EnableMethodSecurity` on the security config class.
- **Line 1** — `@PreAuthorize` evaluates a SpEL security expression **before** the method body runs — if it evaluates false, the call is rejected with `AccessDeniedException` and the method body never executes.
- **Line 7** — expressions can reference method parameters (`#id`) and the current `authentication` object — enabling row-level rules like "you may view your own record" alongside role checks.
- **Line 10** — `@PostAuthorize` evaluates **after** the method runs, with `returnObject` bound to whatever it returned — useful when the authorization decision depends on data you only have post-fetch (e.g., "this record's email matches the caller's identity").

`@AuthenticationPrincipal UserDetails currentUser` injects the authenticated user directly into a controller method parameter — an alternative to manually pulling it from `SecurityContextHolder.getContext().getAuthentication()`.

### 3.5 Section 3 Summary Table (from courseware)

| Feature | Annotation/Class | Purpose |
|---|---|---|
| Security config | `@EnableWebSecurity` | Enable Spring Security |
| HTTP rules | `HttpSecurity.authorizeHttpRequests()` | URL-level access control |
| JWT filter | `OncePerRequestFilter` | Extract and validate JWT |
| Password hash | `BCryptPasswordEncoder` | Secure password storage |
| Method security | `@PreAuthorize` | Fine-grained method access |
| Current user | `@AuthenticationPrincipal` | Inject current user |
| Role check | `hasRole()`, `hasAnyRole()` | Role-based rules |
| Stateless session | `SessionCreationPolicy.STATELESS` | No server-side session |

---

## 4. Spring Boot Advanced (`08_Spring_Boot_Advanced.md`)

### 4.1 Thymeleaf — server-side rendering

Thymeleaf is a server-side Java template engine, an alternative to JSP, using "natural templates" (valid HTML even unrendered). Courseware analogy: a mail-merge template — design once with placeholders, Spring fills real data before sending to the browser.

```properties
1:  spring.thymeleaf.prefix=classpath:/templates/
2:  spring.thymeleaf.suffix=.html
3:  spring.thymeleaf.cache=false  # disable in dev — auto-reload
```
- **Line 1** — templates live under `src/main/resources/templates/`, auto-resolved by Boot's `ThymeleafAutoConfiguration`.

The web controller for Thymeleaf pages uses plain `@Controller` (not `@RestController` — it returns a view name string, not a JSON body):
```java
1:  @Controller
2:  @RequestMapping("/ems")
3:  public class EmsWebController {
4:      @GetMapping("/employees")
5:      public String listEmployees(Model model,
6:              @RequestParam(defaultValue = "") String search,
7:              @RequestParam(defaultValue = "0") int page) {
8:          Page<Employee> empPage = employeeService.search(search,
9:              PageRequest.of(page, 10, Sort.by("lastName")));
10:         model.addAttribute("employees", empPage.getContent());
11:         ...
12:         return "employees/list";  // -> templates/employees/list.html
13:     }
14: }
```
- **Line 12** — the returned `String` is a **logical view name**; Boot's configured `ViewResolver` (Thymeleaf's, via `prefix`/`suffix`) resolves it to `templates/employees/list.html`.

Key Thymeleaf template directives: `th:each` (loop), `th:text` (set element text from an expression), `th:if`/`th:unless` (conditional rendering), `th:object`/`th:field` (two-way form binding to a model object, similar to Django/WTForms model-bound forms), `sec:authorize="hasRole('ADMIN')"` (Spring Security integration — hide/show markup based on the current user's roles).

### 4.2 Spring Batch — large-scale processing

Courseware analogy: a payroll department's end-of-month run — reads all employee records (**Reader**), calculates net pay (**Processor**), writes payslips (**Writer**), in chunks.

```java
1:  @Bean
2:  public Step payrollStep(JpaPagingItemReader<Employee> reader, JpaItemWriter<PayrollRecord> writer) {
3:      return new StepBuilder("payrollStep", jobRepository)
4:          .<Employee, PayrollRecord>chunk(100, transactionManager)
5:          .reader(reader)
6:          .processor(payrollProcessor)
7:          .writer(writer)
8:          .faultTolerant()
9:          .skipLimit(10)
10:         .skip(Exception.class)  // skip bad records, don't fail whole job
11:         .build();
12: }
13:
14: @Bean
15: public Job payrollJob(Step payrollStep) {
16:     return new JobBuilder("monthlyPayrollJob", jobRepository)
17:         .start(payrollStep)
18:         .build();
19: }
```
- **Line 4** — `.chunk(100, transactionManager)` processes and commits 100 records at a time — bounds memory usage and limits transaction size for very large datasets (contrast with loading the entire employee table into memory and looping).
- **Lines 8–10** — fault tolerance: up to 10 bad records can be skipped (logged, not fatal) before the whole job is aborted — critical for batch jobs where one malformed CSV row shouldn't kill an overnight run.
- `@Scheduled(cron = "0 0 0 L * *")` triggers the job on a cron schedule (here: midnight on the last day of every month) via `JobLauncher.run(payrollJob, params)`.

### 4.3 Spring JMS / messaging

Courseware analogy: a company notice board — HR posts "New Employee Hired" (producer); IT and payroll check the board independently at their own pace (consumers) — HR doesn't block waiting for them.

```java
1:  @Service
2:  public class EmployeeService {
3:      private final JmsTemplate jmsTemplate;
4:
5:      public Employee hire(EmployeeRequest request) {
6:          Employee emp = employeeRepository.save(toEntity(request));
7:          EmployeeEvent event = new EmployeeEvent(emp.getId(), emp.getFullName(),
8:              emp.getEmail(), "HIRED", LocalDateTime.now());
9:          jmsTemplate.convertAndSend(JmsConfig.EMPLOYEE_QUEUE, event);
10:         return emp;
11:     }
12: }
13:
14: @Component
15: public class EmployeeEventListener {
16:     @JmsListener(destination = JmsConfig.EMPLOYEE_QUEUE)
17:     public void handleEmployeeEvent(EmployeeEvent event) {
18:         switch (event.getEventType()) {
19:             case "HIRED" -> emailService.sendWelcomeEmail(event.getEmail(), event.getEmployeeName());
20:             ...
21:         }
22:     }
23: }
```
- **Line 9** — `jmsTemplate.convertAndSend(queue, event)` serializes the `event` object (via a configured `MappingJackson2MessageConverter`) and publishes it to the named queue — fire-and-forget, the hire request returns immediately without waiting on the email/notification work.
- **Line 16** — `@JmsListener(destination=...)` marks a method as an async consumer for that queue — Spring invokes it whenever a message arrives, entirely decoupled from the producer's request/response cycle.
- Queue vs Topic: `spring.jms.pub-sub-domain=false` (default) is point-to-point (one consumer gets each message); `=true` is publish-subscribe (every `@JmsListener` on that destination gets its own copy) — useful for broadcast-style notifications to multiple independent consumers.

### 4.4 Testing with MockMvc

```java
1:  @WebMvcTest(EmployeeRestController.class)  // loads only MVC layer, not full context
2:  class EmployeeControllerTest {
3:      @Autowired private MockMvc mockMvc;
4:      @MockBean private EmployeeService employeeService;  // mock the service — don't use real DB
5:      @Autowired private ObjectMapper objectMapper;
6:
7:      @Test
8:      void getAll_ReturnsEmployeeList() throws Exception {
9:          when(employeeService.findAll()).thenReturn(List.of(testEmployee));
10:         mockMvc.perform(get("/api/v1/employees").contentType(MediaType.APPLICATION_JSON))
11:             .andExpect(status().isOk())
12:             .andExpect(jsonPath("$", hasSize(1)))
13:             .andExpect(jsonPath("$[0].firstName", is("Alice")));
14:     }
15: }
```
- **Line 1** — `@WebMvcTest` boots only the web/MVC layer (controllers, filters, validators) — not the full `ApplicationContext`, not a real DB — dramatically faster than a full integration test.
- **Line 4** — `@MockBean` replaces the real `EmployeeService` bean in the test context with a Mockito mock, so the controller's dependency is stubbed rather than hitting a real service/DB.
- **Lines 10–13** — `mockMvc.perform(get(...))` simulates an HTTP request without a running server; `.andExpect(...)` chains assertions on status code and JSON body (`jsonPath` — similar in spirit to Python's `jmespath`/dict-key assertions in a `pytest` + `requests`-mock test).

Full integration variant uses `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureMockMvc` — boots the **entire** application context (real beans, real embedded DB via `application-test.properties`) on a random free port, for end-to-end verification rather than isolated unit testing.

### 4.5 Section 4 Summary Table (from courseware)

| Feature | Annotation/Class | Purpose |
|---|---|---|
| Thymeleaf template | `th:each`, `th:text`, `th:field` | Server-side HTML rendering |
| Security integration | `sec:authorize` | Show/hide based on role |
| Batch job | `@EnableBatchProcessing`, `Job`, `Step` | Large-scale processing |
| Chunk processing | `.chunk(100)` | Process N records at a time |
| JMS producer | `JmsTemplate.convertAndSend()` | Publish async message |
| JMS consumer | `@JmsListener` | React to async messages |
| Controller test | `@WebMvcTest` | Test MVC layer in isolation |
| Service mock | `@MockBean` | Mock dependencies |
| Integration test | `@SpringBootTest` | Full context test |

---

# Part B — The Real Project (`Code/Springboot`)

## 5. What This Project Actually Is

Reminder from the intro: **this is a hand-configured Spring MVC + Hibernate + Spring Data MongoDB + Spring JDBC application, not Spring Boot.** It reuses the exact EMS domain (Employee, Department, Project) that the courseware's Boot chapters model with `@SpringBootApplication`/JPA/starters — but here each concern is wired manually. It's a good "before" reference for seeing exactly what auto-configuration would otherwise do for you. Notably, the three entities each use a **different persistence technology**: `Department` -> raw Spring JDBC (`JdbcTemplate`), `Employee` -> Spring Data MongoDB, `Project` -> Hibernate/JPA via a manual DAO — a deliberate three-way comparison of data-access styles within one codebase.

## 6. Walkthrough — Config Layer

### 6.1 `AppInitializer.java` — replaces `web.xml`

```java
1:  package com.demo.config;
2:
3:  import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
4:
5:  public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
6:
7:      @Override
8:      protected Class<?>[] getRootConfigClasses() {
9:          return null;
10:     }
11:
12:     @Override
13:     protected Class<?>[] getServletConfigClasses() {
14:         return new Class[] { WebConfig.class };
15:     }
16:
17:     @Override
18:     protected String[] getServletMappings() {
19:         return new String[] { "/" };
20:     }
21: }
```
- **Line 5** — extending `AbstractAnnotationConfigDispatcherServletInitializer` is the Servlet-3.0+ programmatic mechanism the servlet container (Tomcat) auto-detects at deploy time (via `SpringServletContainerInitializer` + `META-INF/services`) — it's what `web.xml` used to do declaratively, now expressed in Java. **This entire class has no equivalent in a true Spring Boot app** — Boot's embedded Tomcat auto-registers `DispatcherServlet` through `DispatcherServletAutoConfiguration`; you'd never write this class in a real Boot project.
- **Lines 8–10** — `getRootConfigClasses()` returning `null` means there's no separate "root" application context (typically holding service/repository beans shared across multiple `DispatcherServlet`s) — everything here lives in one servlet-scoped context.
- **Lines 13–15** — `getServletConfigClasses()` returns `WebConfig.class` — this is the `@Configuration` class that gets loaded into the `DispatcherServlet`'s own context (see 6.4 below).
- **Lines 18–20** — `"/"` maps the `DispatcherServlet` to handle every request path.

### 6.2 `H2Config.java` — manual `DataSource`

```java
1:  package com.demo.config;
2:  ...
3:  @Configuration
4:  public class H2Config {
5:
6:      private static final String URL = "jdbc:h2:mem:employeedb;DB_CLOSE_DELAY=-1";
7:      private static final String USERNAME = "sa";
8:      private static final String PASSWORD = "";
9:
10:     @Bean
11:     public DataSource dataSource() {
12:         DriverManagerDataSource ds = new DriverManagerDataSource();
13:         ds.setDriverClassName("org.h2.Driver");
14:         ds.setUrl(URL);
15:         ds.setUsername(USERNAME);
16:         ds.setPassword(PASSWORD);
17:         return ds;
18:     }
19:
20:     @Bean
21:     public JdbcTemplate jdbcTemplate(DataSource dataSource) {
22:         return new JdbcTemplate(dataSource);
23:     }
24:
25:     @Bean
26:     public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
27:         ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
28:         populator.addScript(new ClassPathResource("departments.sql"));
29:         populator.addScript(new ClassPathResource("projects.sql"));
30:         populator.setContinueOnError(false);
31:         DataSourceInitializer initializer = new DataSourceInitializer();
32:         initializer.setDataSource(dataSource);
33:         initializer.setDatabasePopulator(populator);
34:         return initializer;
35:     }
36: }
```
- **Line 3** — `@Configuration` marks this as a Java-based bean-definition class — the direct analogue of an XML `<beans>` file, predates Boot but is still exactly how Boot's own auto-config classes are written internally.
- **Lines 10–18** — builds a `DataSource` bean entirely by hand: driver class name, JDBC URL, credentials hardcoded as constants. **This is precisely what `spring.datasource.*` properties + `DataSourceAutoConfiguration` would generate for you in true Boot** — compare directly against Section 1.4's `application.properties` snippet.
- **Lines 20–23** — `JdbcTemplate` wraps the raw `DataSource` with convenience methods (`query`, `update`) that handle `Connection`/`Statement`/`ResultSet` lifecycle and exception translation — used directly by `DepartmentRepository` (Section 8.1).
- **Lines 25–35** — `DataSourceInitializer` + `ResourceDatabasePopulator` runs `departments.sql`/`projects.sql` against the DB at startup — a manual substitute for Boot's convention of auto-running `schema.sql`/`data.sql` from the classpath root with zero configuration.

### 6.3 `HibernateConfig.java` — manual `SessionFactory`

```java
1:  @Configuration
2:  @EnableTransactionManagement
3:  public class HibernateConfig {
4:
5:      @Bean
6:      public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
7:          LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
8:          sf.setDataSource(dataSource);
9:          sf.setPackagesToScan("com.demo.model");
10:         Properties props = new Properties();
11:         props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
12:         props.put("hibernate.hbm2ddl.auto", "update");
13:         props.put("hibernate.show_sql", "true");
14:         sf.setHibernateProperties(props);
15:         return sf;
16:     }
17:
18:     @Bean
19:     public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
20:         return new HibernateTransactionManager(sessionFactory);
21:     }
22: }
```
- **Line 2** — `@EnableTransactionManagement` turns on Spring's `@Transactional` annotation processing (AOP-based proxying) — in Boot this is auto-enabled by `spring-boot-starter-data-jpa`/`spring-boot-starter-jdbc`.
- **Lines 6–16** — builds a native Hibernate `SessionFactory` (via Spring's `LocalSessionFactoryBean` wrapper) by hand: which packages to scan for `@Entity` classes (`com.demo.model`), and raw Hibernate `Properties` for dialect/DDL-auto/SQL-logging. This is the **exact same information** as `spring.jpa.properties.hibernate.*` and `spring.jpa.hibernate.ddl-auto` in `application.properties` (Section 1.4) — just expressed as a Java `Properties` object instead of externalized config.
- **Lines 18–21** — a `HibernateTransactionManager`, separate and distinct from JPA's `JpaTransactionManager` (which is what Boot's `HibernateJpaAutoConfiguration` wires when you use `spring-boot-starter-data-jpa`) — this project uses the native Hibernate API (`SessionFactory`/`Session`) rather than the JPA `EntityManager` API, visible in `ProjectDaoImpl` (Section 7.2).

### 6.4 `MongoConfig.java` — manual Mongo client

```java
1:  @Configuration
2:  @EnableMongoRepositories(basePackages = "com.demo.repository")
3:  public class MongoConfig extends AbstractMongoClientConfiguration {
4:
5:      @Override
6:      protected String getDatabaseName() {
7:          return "acme-ems"; // MongoDB will create this DB automatically if it doesn't exist
8:      }
9:
10:     @Override
11:     public MongoClient mongoClient() {
12:         return MongoClients.create("mongodb://localhost:27017");
13:     }
14: }
```
- **Line 2** — `@EnableMongoRepositories` turns on Spring Data MongoDB's repository-proxy mechanism (the Mongo equivalent of `JpaRepository` support) for interfaces under `com.demo.repository`.
- **Line 3** — extending `AbstractMongoClientConfiguration` is Spring Data MongoDB's template base class for Java-config Mongo setup — you override just the database name and client factory method; it wires the `MongoTemplate` bean for you internally.
- **Line 12** — connection string is hardcoded (`localhost:27017`) rather than sourced from `spring.data.mongodb.uri` — in Boot, `spring-boot-starter-data-mongodb` + those properties would auto-configure the `MongoClient`/`MongoTemplate` beans without any `@Configuration` class at all.

### 6.5 `WebConfig.java` — manual MVC + view resolver

```java
1:  @Configuration
2:  @EnableWebMvc
3:  @ComponentScan(basePackages = "com.demo")
4:  public class WebConfig {
5:
6:      @Bean
7:      public ViewResolver viewResolver() {
8:          InternalResourceViewResolver resolver = new InternalResourceViewResolver();
9:          resolver.setPrefix("/WEB-INF/views/");
10:         resolver.setSuffix(".jsp");
11:         return resolver;
12:     }
13: }
```
- **Line 2** — `@EnableWebMvc` turns on Spring MVC's full configuration (message converters, formatters, validation) — Boot applications almost never add this explicitly, because `WebMvcAutoConfiguration` already does it (adding your own `@EnableWebMvc` in a Boot app actually **disables** Boot's auto-config and reverts to fully-manual mode — an important gotcha).
- **Line 3** — `@ComponentScan(basePackages = "com.demo")` explicitly declares which package to scan for `@Component`/`@Service`/`@Repository`/`@Controller` beans — in Boot this is implicit via `@SpringBootApplication`'s bundled `@ComponentScan`, which defaults to the package of the annotated class and everything beneath it.
- **Lines 6–12** — builds a JSP `ViewResolver` by hand: any controller method returning `"employees"` resolves to `/WEB-INF/views/employees.jsp`. Note per Section 4.1, Boot's own default view technology in the courseware is **Thymeleaf**, not JSP — this project's JSP setup is itself a further sign it predates or diverges from the Boot chapters.

## 7. Walkthrough — Project (JPA/Hibernate) Vertical Slice

### 7.1 `Project.java` — the entity

```java
1:  package com.demo.model;
2:
3:  import jakarta.persistence.*;
4:
5:  @Entity // mandatory
6:  @Table(name = "projects")
7:  public class Project {
8:
9:      @Id // mandatory
10:     @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL ?
11:     private int id;
12:
13:     private String name;
14:     private String description;
15:     private String status;
16:
17:     public Project() {
18:     }
19:
20:     public Project(String name, String description, String status) {
21:         this.name = name;
22:         this.description = description;
23:         this.status = status;
24:     }
25:
26:     public int getId() { return id; }
27:     public void setId(int id) { this.id = id; }
28:     ...
29: }
```
- **Line 5** — `@Entity` (the code comment "// mandatory" is the student's own annotation, correctly identifying this as the non-negotiable annotation for JPA management).
- **Line 6** — `@Table(name = "projects")` — without it, Hibernate would default to the class name `Project` as the table name.
- **Lines 9–10** — `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` — same PK pattern as the courseware's `Employee` entity (Section 2.2), here on an `int` rather than `Long` (courseware convention for PKs is `Long`, a Java best-practice this project doesn't follow — worth noting for the assessment: `int` PKs work but `Long`/`Integer` wrapper types are generally preferred so JPA can represent "no id yet" as `null`).
- **Lines 17, 20** — a no-arg constructor (required by Hibernate to instantiate entities via reflection) plus a convenience all-args-minus-id constructor — hand-written here, versus Lombok's `@NoArgsConstructor`/`@AllArgsConstructor` generating both automatically in the courseware's style.
- No Lombok anywhere in this file — every getter/setter is hand-written boilerplate, which is exactly the tedium `@Data` eliminates.

### 7.2 `ProjectDao` / `ProjectDaoImpl` — manual DAO over native Hibernate

```java
1:  package com.demo.dao;
2:
3:  import com.demo.model.Project;
4:  import java.util.List;
5:
6:  public interface ProjectDao {
7:      void addProject(Project project);
8:      List<Project> getAllProjects();
9:      Project getProjectById(int id);
10:     void updateProject(Project project);
11:     void deleteProject(int id);
12: }
```
- This interface is the **DAO pattern** — an abstraction the courseware's `JpaRepository` renders unnecessary: a Spring Data JPA repository interface (`extends JpaRepository<Project, Integer>`) would give you all five of these operations (`save`, `findAll`, `findById`, `save` again for update, `deleteById`) with **zero implementation code**, whereas here every method needs a hand-written body.

```java
1:  package com.demo.dao;
2:  ...
3:  @Repository
4:  @Transactional
5:  public class ProjectDaoImpl implements ProjectDao {
6:
7:      @Autowired
8:      private SessionFactory sessionFactory;
9:
10:     @Override
11:     public void addProject(Project project) {
12:         sessionFactory.getCurrentSession().persist(project);
13:     }
14:
15:     @Override
16:     public List<Project> getAllProjects() {
17:         return sessionFactory.getCurrentSession().createQuery("from Project", Project.class).list();
18:     }
19:
20:     @Override
21:     public Project getProjectById(int id) {
22:         return sessionFactory.getCurrentSession().get(Project.class, id);
23:     }
24:
25:     @Override
26:     public void updateProject(Project project) {
27:         sessionFactory.getCurrentSession().merge(project);
28:     }
29:
30:     @Override
31:     public void deleteProject(int id) {
32:         Project p = sessionFactory.getCurrentSession().get(Project.class, id);
33:         if (p != null) {
34:             sessionFactory.getCurrentSession().remove(p);
35:         }
36:     }
37: }
```
- **Line 3** — `@Repository` here is a plain stereotype annotation (marks this as a persistence-layer bean, enables Spring's exception translation) — unlike `JpaRepository` subinterfaces, this class is **fully hand-implemented**, not proxy-generated.
- **Line 4** — `@Transactional` at the class level wraps every public method in a database transaction — same annotation the courseware uses on `@Service` classes, here applied at the DAO level instead.
- **Line 7–8** — `SessionFactory` injected via field `@Autowired` — this is the **native Hibernate API** (`Session`, not JPA's `EntityManager`), obtained from `HibernateConfig`'s `LocalSessionFactoryBean` (Section 6.3). `sessionFactory.getCurrentSession()` returns the transaction-bound `Session` for the current thread.
- **Line 12** — `.persist(project)` is Hibernate's native insert operation (JPA's `EntityManager.persist()` has an equivalent signature — Hibernate's native API and JPA's API are near-identical by design since Hibernate is a JPA provider).
- **Line 17** — `createQuery("from Project", Project.class)` is **HQL** (Hibernate Query Language) — nearly identical syntax to the courseware's JPQL (`"SELECT e FROM Employee e ..."`), since JPQL was modeled on HQL; the difference here is `"from Project"` uses HQL's shorthand omitting `SELECT p`.
- **Line 22** — `.get(Project.class, id)` is a direct primary-key lookup, equivalent to `JpaRepository.findById(id)`.
- **Line 27** — `.merge(project)` reattaches a detached entity and copies its state onto the managed instance — Hibernate's "upsert-by-copy" operation, roughly what `JpaRepository.save()` does under the hood for an entity that already has an ID.
- **Lines 30–35** — delete requires a manual load-then-remove — `JpaRepository.deleteById(id)` collapses this into one call.

### 7.3 `ProjectService` / `ProjectServiceImpl` — service layer over the DAO

```java
1:  public interface ProjectService {
2:      void addProject(Project project);
3:      List<Project> getAllProjects();
4:      Project getProjectById(int id);
5:      void updateProject(Project project);
6:      void deleteProject(int id);
7:  }
```
```java
1:  @Service
2:  public class ProjectServiceImpl implements ProjectService {
3:
4:      @Autowired
5:      private ProjectDao projectDao;
6:
7:      @Override
8:      public void addProject(Project project) {
9:          projectDao.addProject(project);
10:     }
11:     ...
12: }
```
- **Line 1** (interface) — the service layer mirrors the DAO's method signatures exactly and simply delegates — in a larger app this is where business logic (validation, orchestration across multiple DAOs, computed fields) would live; here it's a thin pass-through, which is realistic for an early-course example.
- **`ProjectServiceImpl2.java`** exists but its entire body is commented out — dead scaffold code, not used by the app; worth knowing it exists but not worth further analysis for the assessment.

### 7.4 `ProjectController.java` — MVC controller

```java
1:  @Controller
2:  public class ProjectController {
3:
4:      @Autowired
5:      private ProjectService projectService;
6:
7:      @GetMapping("/projects")
8:      public String listProjects(Model model) {
9:          model.addAttribute("projects", projectService.getAllProjects());
10:         return "projects";
11:     }
12:
13:     @PostMapping("/projects/add")
14:     public String addProject(@RequestParam String name, @RequestParam String description,
15:             @RequestParam String status, Model model) {
16:         projectService.addProject(new Project(name, description, status));
17:         model.addAttribute("projects", projectService.getAllProjects());
18:         model.addAttribute("message", "Project added successfully!");
19:         return "projects";
20:     }
21: }
```
- **Line 1** — `@Controller` (not `@RestController`) — this method returns a **view name**, not a JSON body; it's rendered as HTML/JSP through `WebConfig`'s `ViewResolver` (Section 6.5). This matters for the assessment: `@Controller` methods that *should* return raw data need `@ResponseBody` explicitly added (see `HelloController` below); `@RestController` = `@Controller` + `@ResponseBody` applied to every method automatically.
- **Line 9** — `model.addAttribute("projects", ...)` populates the model that the JSP template reads via EL/JSTL (`${projects}`) — the classic Spring MVC server-rendered-page pattern, directly analogous to Django's `render(request, template, {"projects": ...})` or Flask's `render_template(..., projects=...)`.
- **Lines 13–16** — `@RequestParam` binds individual HTML form fields to method parameters by name — no `@RequestBody`/JSON here, because this is a traditional HTML form POST, not a REST API call.

## 8. Walkthrough — Department (Spring JDBC) Vertical Slice

### 8.1 `DepartmentRepository.java` — raw `JdbcTemplate`, no ORM

```java
1:  @Repository
2:  public class DepartmentRepository {
3:
4:      private final JdbcTemplate jdbcTemplate;
5:
6:      public DepartmentRepository(JdbcTemplate jdbcTemplate) {
7:          this.jdbcTemplate = jdbcTemplate;
8:      }
9:
10:     private final RowMapper<Department> rowMapper = (rs, rowNum) ->
11:         new Department(rs.getInt("id"), rs.getString("name"));
12:
13:     public List<Department> findAll() {
14:         return jdbcTemplate.query("SELECT * FROM departments", rowMapper);
15:     }
16:
17:     public Department findById(int id) {
18:         List<Department> result = jdbcTemplate.query(
19:             "SELECT * FROM departments WHERE id = ?", rowMapper, id);
20:         return result.isEmpty() ? null : result.get(0);
21:     }
22:
23:     public int save(Department dept) {
24:         return jdbcTemplate.update(
25:             "INSERT INTO departments (id, name) VALUES (?, ?)", dept.getId(), dept.getName());
26:     }
27: }
```
- **Line 6** — constructor injection of `JdbcTemplate` (built by `H2Config`, Section 6.2) — this is the *third* distinct persistence style in this project: no JPA/Hibernate at all here, just raw SQL + manual row mapping.
- **Lines 10–11** — `RowMapper<Department>` is a functional interface (lambda) that converts one JDBC `ResultSet` row into a `Department` object — you write this mapping by hand for every entity, exactly what `@Entity` + Hibernate's reflection-based mapping automates away.
- **Line 14** — `jdbcTemplate.query(sql, rowMapper)` executes the SQL and applies the row mapper to every row, returning a `List<Department>` — no `Connection`/`Statement`/`ResultSet` open/close boilerplate (that's what `JdbcTemplate` itself abstracts, versus raw JDBC).
- **Line 19** — `?` placeholders are positional parameters, bound safely (parameterized query, SQL-injection-safe) via the varargs at the end of `.query(sql, rowMapper, id)`.
- **Line 24** — `jdbcTemplate.update(...)` is used for INSERT/UPDATE/DELETE, returning the number of affected rows (`int`) — same semantic as `@Modifying` JPQL queries in Section 2.5, just via raw SQL.

### 8.2 `Department.java` — plain POJO, no persistence annotations at all

```java
1:  public class Department {
2:      private int id;
3:      private String name;
4:      public Department() { }
5:      public Department(int id, String name) { this.id = id; this.name = name; }
6:      public int getId() { return id; }
7:      ...
8:  }
```
- No `@Entity`, no `@Table`, no `@Id` — because this class is never handed to Hibernate/JPA; it's a manually-mapped plain Java object, populated entirely by the `RowMapper` lambda above. This is a useful contrast point: the same "an object represents a table row" idea, achieved with zero ORM annotations versus the courseware's fully-annotated `@Entity` classes.

### 8.3 `DepartmentService.java` / `DepartmentController.java`

```java
1:  @Service
2:  public class DepartmentService {
3:      private final DepartmentRepository repository;
4:      public DepartmentService(DepartmentRepository repository) {
5:          this.repository = repository;
6:      }
7:      public List<Department> getAllDepartments() { return repository.findAll(); }
8:      public Department getDepartmentById(int id) { return repository.findById(id); }
9:      public int addDepartment(Department department) { return repository.save(department); }
10: }
```
- **Lines 4–6** — constructor injection (no `@Autowired` needed on the constructor itself when there's only one constructor — Spring auto-wires it) — same pattern the courseware's `EmployeeService` uses in Section 1.6.

```java
1:  @Controller
2:  public class DepartmentController {
3:      private final DepartmentService service;
4:      public DepartmentController(DepartmentService service) { this.service = service; }
5:
6:      @GetMapping("/departments")
7:      public String getDepartments(Model model) {
8:          List<Department> list = service.getAllDepartments();
9:          model.addAttribute("departments", list);
10:         return "departments";
11:     }
12:
13:     @GetMapping("/departments/find")
14:     public String findDepartmentById(@RequestParam("id") int id, Model model) {
15:         Department dept = service.getDepartmentById(id);
16:         if (dept != null) {
17:             model.addAttribute("foundDepartment", dept);
18:         } else {
19:             model.addAttribute("notFound", "No department found with ID: " + id);
20:         }
21:         model.addAttribute("departments", service.getAllDepartments());
22:         return "departments";
23:     }
24: }
```
- **Line 14** — `@RequestParam("id")` explicitly names the query-string parameter to bind (`?id=2`) — explicit naming matters here because it doesn't match the Java parameter name pattern Spring can infer without `-parameters` compiler flag support (though the `pom.xml`'s `maven-compiler-plugin` does set `<arg>-parameters</arg>`, so implicit binding would actually work too — the explicit name is defensive/clear style).
- **Lines 16–20** — manual null-check-and-branch to decide which model attribute to populate — a controller doing view-decision logic that a `ResourceNotFoundException` + `@RestControllerAdvice` pattern (Section 2.9) would centralize in a REST API context; here, since it's a server-rendered page, the "404" is just a different message rendered on the same page rather than an HTTP status code change.

## 9. Walkthrough — Employee (Spring Data MongoDB) Vertical Slice

### 9.1 `Employee.java` — MongoDB document, not a JPA entity

```java
1:  package com.demo.model;
2:
3:  import org.springframework.data.annotation.Id;
4:  import org.springframework.data.mongodb.core.mapping.Document;
5:
6:  @Document(collection = "emps") // maps this class to "employees" collection in MongoDB
7:  public class Employee {
8:
9:      @Id
10:     // String
11:     private int id; // MongoDB will use this as the _id field
12:     private String name;
13:     private double salary;
14:
15:     public Employee() { super(); }
16:     public Employee(int id, String name, double salary) {
17:         super();
18:         this.id = id; this.name = name; this.salary = salary;
19:     }
20:     ...
21: }
```
- **Line 6** — `@Document(collection = "emps")` is Spring Data MongoDB's analogue of JPA's `@Entity`/`@Table` — maps this class to the `emps` MongoDB collection (note the code comment says "employees" but the actual `collection` value is `"emps"` — a discrepancy worth flagging if this comes up in the assessment: **the comment is stale/wrong**, the real collection name is `emps`).
- **Line 3** — the `@Id` import here is `org.springframework.data.annotation.Id` (the generic Spring Data marker), **not** `jakarta.persistence.Id` used by `Project` (Section 7.1) — different `@Id` annotations for different Spring Data modules; mixing them up is a common student mistake worth calling out for the assessment.
- **Line 11** — using `int` as MongoDB's `_id` is atypical (Mongo's native `_id` is usually an `ObjectId`), but Spring Data MongoDB allows any type you declare, including a plain `int`, as this project demonstrates — a pragmatic simplification for teaching purposes rather than production-realistic Mongo schema design.
- No Lombok, no `@Entity`, no relational `@Column`/`@Table` — document-model persistence has fundamentally different mapping annotations from relational JPA, and this file is a clean example of that distinction alongside `Project.java`.
- The file also contains an entire commented-out legacy version of the same class with zero Mongo annotations — visible evidence of the class's evolution from a plain in-memory POJO to a Mongo-backed document.

### 9.2 `EmployeeRepository.java` — `MongoRepository`, the Mongo equivalent of `JpaRepository`

```java
1:  package com.demo.repository;
2:
3:  import org.springframework.data.mongodb.repository.MongoRepository;
4:  import org.springframework.stereotype.Repository;
5:
6:  import com.demo.model.Employee;
7:
8:  @Repository
9:  public interface EmployeeRepository extends MongoRepository<Employee, Integer> {
10:     // MongoRepository gives you these for free — no code needed:
11:     // findAll() -> getAllEmployees
12:     // findById(id) -> getEmployeeById
13:     // save(employee) -> addEmployee, updateEmployee
14:     // deleteById(id) -> deleteEmployee (for later)
15: }
```
- **Line 9** — `MongoRepository<Employee, Integer>` mirrors `JpaRepository<T, ID>` structurally: entity type, ID type — same Spring Data "define an interface, get an implementation" pattern (Section 2.3) applied to a document store instead of a relational one. This is the cleanest proof-point in the whole project that Spring Data's *repository abstraction* is storage-agnostic — the same programming model works across JPA, MongoDB, and (via other Spring Data modules) Redis, Elasticsearch, etc.
- Registered via `@EnableMongoRepositories(basePackages = "com.demo.repository")` in `MongoConfig` (Section 6.4) — this is the Mongo-specific bootstrapping step; in true Spring Boot with `spring-boot-starter-data-mongodb`, this scanning is auto-configured and you wouldn't write `@EnableMongoRepositories` yourself either.
- Commented-out lines show a derived query method (`findByName`) and a `@Query` example that were never activated — same derived-query and `@Query` mechanisms as JPA (Section 2.4–2.5), just against Mongo's query dialect.

### 9.3 `EmployeeService.java` / `EmployeeController.java`

```java
1:  @Service
2:  public class EmployeeService {
3:      private final EmployeeRepository repository;
4:      public EmployeeService(EmployeeRepository repository) { this.repository = repository; }
5:
6:      public List<Employee> getAllEmployees() { return repository.findAll(); } // reads from MongoDB
7:      public Employee getEmployeeById(int id) {
8:          return repository.findById(id).orElse(null); // findById returns Optional
9:      }
10:     public Employee addEmployee(Employee employee) { return repository.save(employee); } // inserts into MongoDB
11: }
```
- **Line 8** — `repository.findById(id)` returns `Optional<Employee>` (same as `JpaRepository`, Section 2.3) — `.orElse(null)` unwraps it, choosing to return `null` on absence rather than propagating the `Optional` or throwing (contrast with the courseware's `EmployeeService.findById` in Section 1.6, which throws `ResourceNotFoundException` via `.orElseThrow(...)` — a more robust pattern this project's simpler version doesn't use).

```java
1:  @Controller
2:  public class EmployeeController {
3:      @Autowired
4:      private EmployeeService employeeService;
5:
6:      @GetMapping("/employees")
7:      public String getEmployees(Model model) {
8:          List<Employee> list = employeeService.getAllEmployees();
9:          model.addAttribute("employees", list);
10:         return "employees";
11:     }
12:
13:     @PostMapping("/employees/add")
14:     public String addEmployee(@RequestParam("id") int id, @RequestParam("name") String name,
15:             @RequestParam("salary") double salary, Model model) {
16:         Employee employee = new Employee(id, name, salary);
17:         employeeService.addEmployee(employee);
18:         model.addAttribute("employees", employeeService.getAllEmployees());
19:         model.addAttribute("message", "Employee added successfully!");
20:         return "employees";
21:     }
22:
23:     // assignment
24:     //  getEmployeeByName()
25:     //  updateEmployee()
26:     //  deleteEmployee()
27: }
```
- **Line 3** — field injection (`@Autowired` directly on the field) rather than constructor injection — contrast with `DepartmentController`/`ProjectController` in this same codebase, which both use constructor injection. Field injection works but is generally discouraged versus constructor injection (harder to unit-test, hides required dependencies, can't be `final`) — this inconsistency across controllers in the same project is worth noting for the assessment as a "spot the anti-pattern" example.
- **Lines 23–26** — explicit comment marking this as unfinished student assignment work (`getEmployeeByName`, `updateEmployee`, `deleteEmployee` not yet implemented) — confirms this file is a teaching scaffold, not a finished CRUD controller.

### 9.4 `HelloController.java` — minimal `@Controller` + `@ResponseBody`

```java
1:  @Controller
2:  public class HelloController {
3:
4:      @GetMapping("/")
5:      @ResponseBody
6:      public String home() {
7:          System.out.println("home");
8:          return "Welcome!";
9:      }
10:
11:     @GetMapping("/hi")
12:     @ResponseBody
13:     public String hi() {
14:         System.out.println("hi");
15:         return "Hi! How're you?";
16:     }
17: }
```
- **Line 1 vs Line 5** — this is the clearest illustration in the whole project of `@Controller` vs `@RestController`: `@Controller` alone means a returned `String` ("Welcome!") is treated as a *view name* to resolve via the `ViewResolver`; adding `@ResponseBody` on line 5 overrides that, telling Spring to write the return value **directly as the HTTP response body** instead. `@RestController` (used nowhere in this project, but ubiquitous in the courseware's Boot examples, e.g. `EmployeeController` in Section 1.6) is simply `@Controller` + `@ResponseBody` applied automatically to every method, saving you from repeating `@ResponseBody` on each one.

## 10. Property Files — `application.properties` in This Project vs. True Boot

```properties
1:   MongoDB (existing)
2:  mongodb.host=localhost
3:  mongodb.port=27017
4:  mongodb.database=acme-ems
5:
6:  # H2 — Spring JDBC (Department)
7:  h2.url=jdbc:h2:mem:employeedb;DB_CLOSE_DELAY=-1
8:  h2.username=sa
9:  h2.password=
10: h2.driver=org.h2.Driver
```
- **Line 1** — note the missing `#` comment marker on the first line (likely a copy/paste artifact — `" MongoDB (existing)"` reads as a stray unstructured line, not a valid `key=value` property or well-formed comment).
- **Critical point**: none of the `*.java` config classes read from this file. `H2Config`'s URL/username/password (Section 6.2) and `MongoConfig`'s `getDatabaseName()`/connection string (Section 6.4) are **hardcoded Java constants**, not `@Value("${h2.url}")`-injected from these properties. This file's keys are effectively **dead configuration** in the current codebase — present, perhaps intended for future externalization, but not actually wired up. In a true Spring Boot project, `application.properties` is never optional decoration like this — it's the live, auto-read source of truth that `DataSourceAutoConfiguration`/`MongoAutoConfiguration` bind directly (as in Section 1.4). This gap is itself a good study point: it shows what "not actually Boot" means in practice, beyond just the missing `@SpringBootApplication` class.

---

# Assessment Quick-Reference — Cross-Cutting Comparison Table

| Concern | Courseware's Boot EMS (`04`/`05`/`07`/`08`) | Real project `Code/Springboot` |
|---|---|---|
| Bootstrap | `@SpringBootApplication` + `SpringApplication.run()` | `AppInitializer` (manual `web.xml` replacement) |
| Packaging | Executable JAR, embedded Tomcat | WAR, external Tomcat |
| DataSource | `spring.datasource.*` properties, auto-configured | `H2Config` hand-builds `DriverManagerDataSource` |
| JPA/Hibernate | `spring.jpa.*` properties, `HibernateJpaAutoConfiguration` | `HibernateConfig` hand-builds `LocalSessionFactoryBean` + raw `Properties` |
| Repositories | `JpaRepository<Employee, Long>` — zero implementation | `ProjectDao`/`ProjectDaoImpl` — fully hand-written using native `SessionFactory`/`Session` |
| Mongo | `spring-boot-starter-data-mongodb` + properties | `MongoConfig extends AbstractMongoClientConfiguration`, hardcoded connection string |
| MVC | Auto-configured, default view tech Thymeleaf | `@EnableWebMvc` + manual `InternalResourceViewResolver` for JSP |
| Component scanning | Implicit via `@SpringBootApplication` | Explicit `@ComponentScan(basePackages="com.demo")` in `WebConfig` |
| Raw SQL access | Not shown in courseware (JPA-first) | `DepartmentRepository` — plain `JdbcTemplate` + `RowMapper`, no ORM |
| `@Controller` vs `@RestController` | Uses `@RestController` throughout for APIs | Uses `@Controller` + explicit `@ResponseBody` (`HelloController`) or view names (`DepartmentController`, `EmployeeController`, `ProjectController`) |
| Config source of truth | `application.properties` actively read by auto-config | `application.properties` present but **not actually wired** — config classes use hardcoded constants instead |
