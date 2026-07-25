# Spring Framework — Core, Data Access & MVC: Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual course materials: the three courseware files (`01_Spring_Core.md`, `02_Spring_Data_Access.md`, `03_Spring_MVC.md`, all under the "EMS — Employee Management System" theme) and the real Spring MVC project checked into the repo (`Code/Spring MVC`, Maven artifact `acme-spring-mvc-demo`). No invented examples are used — every code sample below is either copied from the courseware or is the literal, unmodified project source.

A likely point of confusion: **the real project deliberately mixes two different data-access patterns in the same app.** `Department` and `Project` are relational: `Department` goes through **Spring JDBC** (`JdbcTemplate` + a hand-written `DepartmentRepository`), and `Project` goes through **Hibernate/ORM** (a `SessionFactory`-based DAO, `ProjectDaoImpl`), both backed by the same in-memory **H2** database. `Employee`, by contrast, is **not relational at all** — it's a `@Document`-annotated POJO stored in **MongoDB** via Spring Data's `MongoRepository` (a repository *interface* with zero implementation code — Spring generates the implementation at runtime). So the same "layered architecture" (Controller → Service → Repository/DAO) is realized three different ways for three different entities in one codebase.

---

# Part I — Concepts (from the courseware)

## 1. Introduction to Spring Framework

Spring is an open-source, lightweight, enterprise Java framework whose core value proposition is **removing plumbing code** — object creation, wiring, transaction boilerplate, JDBC boilerplate — so you write only business logic.

- **IoC container** manages object lifecycle instead of you calling `new` everywhere.
- **Dependency Injection (DI)** wires objects together instead of hard-coded dependencies.
- **Spring JDBC / JPA templates** replace boilerplate JDBC.
- **Declarative `@Transactional`** replaces manual commit/rollback code.
- **AOP** centralizes cross-cutting concerns (logging, auditing) instead of scattering them through business methods.

The courseware's analogy: Spring is like an HR department for your Java objects — HR (the container) creates "employees" (objects), assigns them their tools and teammates (dependencies), and manages their "employment lifecycle" (init/destroy). You only write the job description (configuration).

**Ecosystem layering:** Spring Core (IoC/DI) sits underneath Spring JDBC (DB access), Spring ORM (Hibernate integration), Spring MVC (web layer), and Spring AOP (cross-cutting concerns). Spring Boot, Spring Data JPA, Spring Security, Spring Batch, and Spring Cloud build on top of that core framework — the project you'll walk through below is **classic Spring** (not Boot): everything is wired explicitly via `@Configuration` classes and an `AbstractAnnotationConfigDispatcherServletInitializer`, not auto-configured.

---

## 2. IoC & Dependency Injection — The Core Idea

**Inversion of Control (IoC):** in traditional Java, a class creates its own dependencies (`private EmployeeRepository repo = new EmployeeRepository();`) — tight coupling, hard to test, hard to swap implementations. With IoC, the class only declares a dependency (`private EmployeeRepository repo;`) and the **Spring Container** supplies ("injects") it.

**The Spring IoC Container** reads configuration (XML, Java `@Configuration` classes, or annotations), instantiates objects (**beans**), wires their dependencies together, and manages their full lifecycle. Two container implementations:

| Container | Characteristics |
|---|---|
| `BeanFactory` | Basic container, lazy-loads beans, rarely used directly |
| `ApplicationContext` | Full-featured, eager-loads singleton beans by default — the one you always use |

**Dependency Injection (DI)** is the *mechanism* IoC uses. Three flavors, all covered in the courseware:
1. **Setter Injection** — via setter methods, after object construction.
2. **Constructor Injection** — via the constructor, at construction time (Spring team's recommended default).
3. **Field Injection** — `@Autowired` directly on a field; convenient but least preferred (can't make the field `final`, harder to unit test without reflection/Spring).

---

## 3. Setter Injection

Spring instantiates the bean with a no-arg constructor, then calls the relevant setter methods to push in dependencies.

XML example from the courseware (EMS `Department`/`EmployeeService`):
```xml
<bean id="hrDepartment" class="com.ems.model.Department">
    <property name="name" value="Human Resources"/>
    <property name="location" value="Mumbai"/>
</bean>

<bean id="employeeService" class="com.ems.service.EmployeeService">
    <property name="serviceName" value="EMS Core Service"/>
    <property name="department" ref="hrDepartment"/>  <!-- ref = reference to another bean -->
</bean>
```
- `<property name="..." value="...">` injects a primitive/`String` — calls the matching setter with a literal.
- `<property name="..." ref="...">` injects a *reference to another bean* — calls the matching setter with the object identified by that bean id.
- The container is bootstrapped with `ClassPathXmlApplicationContext("beans.xml")`, and beans are retrieved with `context.getBean("employeeService")` (cast required — this is the untyped, string-based lookup API).

---

## 4. Constructor Injection

Spring calls a constructor with the required arguments at bean-creation time — dependencies are supplied atomically, so the object can never exist in a partially-wired state. Preferred for **mandatory** dependencies.

```xml
<bean id="employee1" class="com.ems.model.Employee">
    <constructor-arg index="0" value="101"/>
    <constructor-arg index="1" value="Alice"/>
    <constructor-arg index="2" ref="hrDepartment"/>
</bean>

<bean id="employee2" class="com.ems.model.Employee">
    <constructor-arg name="id" value="102"/>
    <constructor-arg name="name" value="Bob"/>
    <constructor-arg name="department" ref="hrDepartment"/>
</bean>
```
`index="N"` matches by positional argument; `name="..."` matches by parameter name (requires the `-parameters` compiler flag or debug symbols — notably, the real project's `pom.xml` explicitly enables `<arg>-parameters</arg>` on the compiler plugin, which is exactly what makes name-based constructor binding/param-name reflection work).

**Setter vs Constructor — courseware comparison table:**

| Aspect | Setter Injection | Constructor Injection |
|---|---|---|
| When injected | After object creation | During object creation |
| Optional dependencies | Good for optional | All args required |
| Immutability | Object can change | Object stays consistent (fields can be `final`) |
| Circular dependency | Can resolve | Causes error |
| Best for | Optional config | Mandatory dependencies |

The courseware states plainly: **the Spring team recommends constructor injection** for required dependencies, setter injection only for optional ones. This is exactly the pattern the real project follows for `DepartmentController`, `DepartmentRepository`, and `DepartmentService` (all constructor-injected) — while `EmployeeController` and `ProjectController` use field injection (`@Autowired` on the field) — a concrete illustration of the "old style vs recommended style" contrast.

---

## 5. Auto-Wiring

Rather than writing explicit `ref="beanId"` in XML, Spring can automatically detect and inject matching beans.

**XML autowire modes:**

| Mode | Behavior |
|---|---|
| `no` | Default — no autowiring |
| `byName` | Matches bean `id` to the property name |
| `byType` | Matches bean type to the property type |
| `constructor` | Like `byType`, but resolves constructor arguments |

**Annotation-based (modern, far more common):** `@Autowired` on a field, setter, or constructor tells Spring to locate a bean of the matching type in the container and inject it automatically — no XML at all.
```java
@Autowired
private EmployeeRepository employeeRepository;

@Autowired
public EmployeeService(DepartmentService departmentService) {
    this.departmentService = departmentService;
}
```
If more than one bean of the required type exists, `@Autowired` alone is ambiguous and Spring throws `NoUniqueBeanDefinitionException`. **`@Qualifier("beanName")`** disambiguates by specifying exactly which bean to inject:
```java
@Autowired
@Qualifier("hrDepartmentService")
private DepartmentService departmentService;
```

---

## 6. Bean Lifecycle

Spring fully manages a bean's life from creation to destruction:

```
Container starts
    → Bean instantiated
    → Dependencies injected
    → init-method called   ← your custom init logic
    → Bean is ready to use
    → ... application runs ...
    → destroy-method called ← your custom cleanup
    → Container shuts down
```

Three ways to hook into this, in increasing order of modernity:

1. **XML `init-method`/`destroy-method` attributes**, paired with plain methods on the bean class.
2. **`InitializingBean`/`DisposableBean` interfaces** — implement `afterPropertiesSet()` and `destroy()`.
3. **`@PostConstruct`/`@PreDestroy` annotations** (from `jakarta.annotation`) — the modern, preferred approach; put them directly on any method and Spring calls them at the right lifecycle phase without any XML or interface coupling.

---

## 7. Bean Scopes, Dependency Checking & Inner Beans

**Scope** controls how many instances of a bean the container creates:

| Scope | Meaning | Typical use |
|---|---|---|
| `singleton` | **Default.** One instance per container | Services, Repositories |
| `prototype` | New instance every `getBean()` call | Stateful objects |
| `request` | One per HTTP request | Web apps only |
| `session` | One per HTTP session | Web apps only |
| `application` | One per `ServletContext` | Web apps only |

```java
@Component
@Scope("prototype")
public class EmployeeForm { ... }
```

**Inner beans** — a `<bean>` declared *inside* another bean's `<property>` element. It's anonymous (no `id`) and can only be used by its enclosing bean; it cannot be referenced or reused elsewhere in the XML:
```xml
<bean id="employee" class="com.ems.model.Employee">
    <property name="department">
        <bean class="com.ems.model.Department">
            <property name="name" value="Finance"/>
        </bean>
    </property>
</bean>
```

---

## 8. Externalizing Configuration with Properties

Hard-coding config values (DB URLs, service names, connection limits) is bad practice — the courseware pushes these into `.properties` files.

**XML approach** — load the file, then reference values with `${...}` placeholder syntax:
```xml
<context:property-placeholder location="classpath:application.properties"/>
<bean id="dataSource" class="com.ems.config.DataSourceConfig">
    <property name="url" value="${ems.db.url}"/>
</bean>
```

**Annotation approach — `@Value`:** injects a single property value straight into a field, with automatic type conversion (`String` → `int` shown below):
```java
@Value("${ems.service.name}")
private String serviceName;

@Value("${ems.db.maxConnections}")
private int maxConnections;
```
In the real project, properties are read manually via a `ResourceDatabasePopulator`/`DriverManagerDataSource` rather than `@Value` injection — a slight departure from the "textbook" pattern (see Part II).

---

## 9. Standalone Collections

Spring can inject `List`, `Set`, `Map`, and `Properties` values directly through XML, without you constructing the collection in Java:
```xml
<property name="skills">
    <list>
        <value>Java</value>
        <value>Spring Boot</value>
    </list>
</property>
<property name="certifications">
    <map>
        <entry key="AWS Solutions Architect" value="2023-06-15"/>
    </map>
</property>
<property name="contactInfo">
    <props>
        <prop key="email">diana@ems.com</prop>
    </props>
</property>
```
`<list>` maps to `List<String>`, `<map>`/`<entry>` maps to `Map<K,V>`, `<props>`/`<prop>` maps specifically to `java.util.Properties` (String-to-String only).

---

## 10. Stereotype Annotations & Component Scanning

Stereotype annotations mark a class as a Spring-managed bean, eliminating explicit `<bean>` XML declarations entirely.

| Annotation | Marks | Layer |
|---|---|---|
| `@Component` | A generic Spring-managed bean | Any |
| `@Service` | Business logic | Service layer |
| `@Repository` | Data access | DAO/Repository layer |
| `@Controller` | Web request handling (returns view names) | Presentation layer |
| `@RestController` | REST API (implicit `@ResponseBody` on every method) | Presentation layer |

**Why the specialized annotations instead of just `@Component` everywhere?**
- `@Repository` enables Spring's **exception translation** — it wraps a `PersistenceExceptionTranslationPostProcessor` around the bean that converts vendor-specific exceptions (`SQLException`, Hibernate exceptions) into Spring's unified `DataAccessException` hierarchy, so calling code doesn't need to catch JDBC-driver-specific or Hibernate-specific exception types.
- `@Service` carries no extra runtime behavior over `@Component` by itself, but signals intent (business logic layer) and is a natural target for AOP pointcuts (e.g., `@within(org.springframework.stereotype.Service)`).
- `@Controller` is recognized by Spring MVC's request-mapping handler infrastructure specifically — it is what makes `@RequestMapping`/`@GetMapping` methods on the class discoverable as HTTP handlers.

**Enabling the scan** — Spring only turns `@Component`/`@Service`/`@Repository`/`@Controller` classes into beans if component scanning is switched on for the package(s) containing them:
```xml
<context:component-scan base-package="com.ems"/>
```
```java
@Configuration
@ComponentScan("com.ems")
public class AppConfig { }
```
At startup, Spring scans the classpath under the given base package(s), finds every class annotated with a stereotype annotation (or a meta-annotation composed from one), instantiates it, and registers it as a bean — all without a single explicit `<bean>` tag.

---

## 11. Injecting Interfaces (Programming to Interfaces)

Always depend on an **interface** type, not a concrete class — this keeps code loosely coupled and swappable/testable. When multiple implementations of an interface exist as beans, `@Qualifier` picks the right one:
```java
public interface NotificationService {
    void sendNotification(String employeeEmail, String message);
}

@Service("emailNotification")
public class EmailNotificationService implements NotificationService { ... }

@Service("smsNotification")
public class SmsNotificationService implements NotificationService { ... }

@Service
public class EmployeeOnboardingService {
    private final NotificationService notificationService;

    @Autowired
    @Qualifier("emailNotification")
    public EmployeeOnboardingService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```
Note `@Service("emailNotification")` gives the bean an **explicit name** (overriding the default camel-cased class name) — that name is what `@Qualifier` references. This exact pattern — interface + `@Qualifier`-disambiguated implementations — is directly relevant to the real project's `ProjectService` interface, which has **two** implementation classes on disk (`ProjectServiceImpl` and a commented-out stub `ProjectServiceImpl2`) — see Part II §37.

---

## 12. Java Configuration

Modern Spring favors **Java-based configuration** over XML: type-safe, refactorable, navigable in an IDE, and catches config errors at compile time instead of at container startup.

```java
@Configuration          // Marks this class as a source of bean definitions
@ComponentScan("com.ems")          // Enable annotation scanning
@PropertySource("classpath:application.properties")  // Load properties for @Value
public class AppConfig {

    @Value("${ems.service.name}")
    private String serviceName;

    @Bean
    public Department hrDepartment() {
        Department dept = new Department();
        dept.setName("Human Resources");
        return dept;
    }

    @Bean
    public EmployeeService employeeService() {
        EmployeeService service = new EmployeeService();
        service.setDepartment(hrDepartment()); // Spring manages this — no duplicate bean
        return service;
    }
}
```
- `@Configuration` tells Spring's CGLIB-based container that this class itself declares bean *factories* — Spring proxies the class so that calling `hrDepartment()` from inside `employeeService()` doesn't create a second `Department` instance; it intercepts the call and returns the already-registered singleton bean.
- `@Bean` on a method registers that method's return value as a bean, with the bean's id defaulting to the method name (`hrDepartment`, `employeeService`).
- Loading: `new AnnotationConfigApplicationContext(AppConfig.class)` instead of `ClassPathXmlApplicationContext`.

**XML vs Java Config vs pure annotations — when to use each (courseware table):**

| Approach | When to Use |
|---|---|
| XML Config | Legacy projects, when you can't modify source code |
| Java Config (`@Configuration`) | Explicit wiring, third-party bean setup (e.g., `DataSource`, `RestTemplate`) |
| Annotation (`@Component`, `@Autowired`) | Your own classes — cleanest, most common |

The real project uses **Java config exclusively** — no XML anywhere — which matches the "most common in practice" guidance, and separates third-party bean wiring (`H2Config`, `HibernateConfig`, `MongoConfig`) from component scanning (`WebConfig`'s `@ComponentScan`), exactly mirroring this table's advice.

---

## 13. Spring JDBC — `JdbcTemplate`

Plain JDBC requires manual `Connection`/`PreparedStatement`/`ResultSet` lifecycle management with try/catch/finally in every method — tedious and error-prone (resource leaks if you forget to close something). **`JdbcTemplate`** eliminates all of that: you supply only the SQL and a way to map rows to objects; `JdbcTemplate` handles obtaining/releasing connections, exception translation, and resource cleanup.

Configuring it as a bean:
```java
@Bean
public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:mysql://localhost:3306/emsdb");
    ...
    return ds;
}

@Bean
public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

Typical repository methods:
```java
public int save(Employee emp) {
    String sql = "INSERT INTO employees (name, email, salary, department_id) VALUES (?, ?, ?, ?)";
    return jdbcTemplate.update(sql, emp.getName(), emp.getEmail(), emp.getSalary(), emp.getDepartmentId());
}

public Employee findById(int id) {
    String sql = "SELECT * FROM employees WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, new EmployeeRowMapper(), id);
}

public List<Employee> findAll() {
    return jdbcTemplate.query("SELECT * FROM employees", new EmployeeRowMapper());
}
```
- `update(sql, args...)` — for INSERT/UPDATE/DELETE; returns the number of affected rows; `?` placeholders are bound positionally from the varargs.
- `queryForObject(sql, RowMapper, args...)` — expects exactly one row back; throws if zero or more than one row matches.
- `query(sql, RowMapper, args...)` — returns a `List<T>`, one entry per row.
- `queryForObject(sql, Integer.class, args...)` — single-column, single-row scalar query (e.g. `COUNT(*)`).

---

## 14. RowMapper

`RowMapper<T>` is the interface you implement to convert one JDBC `ResultSet` row into a domain object:
```java
public class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getInt("id"));
        emp.setName(rs.getString("name"));
        return emp;
    }
}
```
`mapRow` is called once per row `JdbcTemplate` reads from the `ResultSet`; `rowNum` is the zero-based row index (rarely used). In modern code (and in the real project's `DepartmentRepository`) this is written far more concisely as a **lambda** assigned to a `RowMapper<T>`-typed field, since `RowMapper` is a functional interface (single abstract method `mapRow`).

**`NamedParameterJdbcTemplate`** — uses `:paramName` placeholders instead of positional `?`, bound via a `MapSqlParameterSource`, which is more readable/maintainable when a query has many parameters:
```java
String sql = "INSERT INTO employees (name, email, salary) VALUES (:name, :email, :salary)";
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("name", emp.getName())
    .addValue("email", emp.getEmail());
namedJdbcTemplate.update(sql, params);
```

---

## 15. Spring ORM — Hibernate Integration

Spring ORM integrates Hibernate (a JPA provider) *into* the Spring container, so Hibernate sessions/transactions get dependency injection, declarative transaction management, and the same exception-translation benefit `@Repository` gives JDBC code.

**Entity mapping** uses standard JPA annotations:
```java
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();
}
```
- `@Entity` marks the class as a persistent JPA/Hibernate entity — mandatory for anything you want Hibernate to manage.
- `@Table(name=...)` maps it to a specific DB table (defaults to the class name if omitted).
- `@Id` marks the primary key field; `@GeneratedValue(strategy = GenerationType.IDENTITY)` delegates PK generation to the DB's auto-increment column.
- `@Column(...)` fine-tunes column mapping (name, nullability, length, uniqueness).
- `@OneToMany`/`@ManyToOne` model relationships; `mappedBy` marks the non-owning side; `fetch = FetchType.LAZY` defers loading the associated collection/entity until it's actually accessed.

**Wiring Hibernate as Spring beans:**
```java
@Configuration
@EnableTransactionManagement
public class OrmConfig {
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
        sf.setDataSource(dataSource);
        sf.setPackagesToScan("com.ems.model");
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        sf.setHibernateProperties(props);
        return sf;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sf) {
        return new HibernateTransactionManager(sf);
    }
}
```
- `LocalSessionFactoryBean` is a Spring `FactoryBean` that builds and exposes a Hibernate `SessionFactory` as a Spring bean — it wires the `DataSource`, tells Hibernate which packages to scan for `@Entity` classes, and carries the raw Hibernate properties (dialect, DDL auto-generation mode, SQL logging).
- `hibernate.hbm2ddl.auto=update` tells Hibernate to auto-create/alter tables to match the entity mappings at startup — convenient for demos, dangerous in production.
- `HibernateTransactionManager` is the `PlatformTransactionManager` implementation that makes `@Transactional` actually begin/commit/rollback Hibernate sessions.
- `@EnableTransactionManagement` switches on Spring's `@Transactional` annotation processing (without it, `@Transactional` is inert).

**Repository using the Hibernate `Session` directly:**
```java
@Repository
public class EmployeeOrmRepository {
    @Autowired
    private SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public void save(Employee emp) {
        currentSession().persist(emp);
    }

    @Transactional
    public void updateSalary(int empId, double newSalary) {
        Employee emp = currentSession().get(Employee.class, empId);
        emp.setSalary(newSalary); // Hibernate detects change — auto UPDATE on commit
    }
}
```
`sessionFactory.getCurrentSession()` returns the Hibernate `Session` bound to the currently active Spring-managed transaction (thread-local) — you never open/close it manually. Note the "dirty checking" behavior in `updateSalary`: simply mutating a managed entity's field is enough; Hibernate compares it against its snapshot at flush/commit time and issues an `UPDATE` automatically — no explicit `save()` call needed for updates on already-managed entities.

---

## 16. `@Transactional` — Declarative Transaction Management

```java
@Transactional                                            // begin, commit on success, rollback on RuntimeException
@Transactional(readOnly = true)                            // optimization hint for SELECT-only methods
@Transactional(rollbackFor = Exception.class)               // also roll back on checked exceptions
@Transactional(propagation = Propagation.REQUIRES_NEW)      // always start a brand-new transaction
```
`@Transactional` works via a **proxy**: Spring wraps the annotated bean in a dynamic proxy (JDK interface proxy or CGLIB subclass proxy) that opens a transaction before the method runs and commits (or rolls back, on an unchecked exception by default) after it returns. This only works for calls that go **through the proxy** — i.e., from outside the bean, or between beans; a method calling another `@Transactional` method on `this` within the same class bypasses the proxy and the second annotation has no effect.

**Placement guidance from the courseware:** put `@Transactional` on a class means all its methods are transactional; place it on the **service layer**, never on the repository or the controller. (The real project's `ProjectDaoImpl` puts `@Transactional` at the DAO/repository level instead — see Part II §35.)

---

## 17. Spring AOP

AOP separates **cross-cutting concerns** (logging, auditing, performance monitoring, security checks) from business logic, so you don't have to hand-write them inside every method. Courseware analogy: a hotel's "Do Not Disturb" protocol applies to every room without being part of each room's individual design — that's what an aspect does to your methods.

**Core vocabulary:**

| Term | Meaning |
|---|---|
| Aspect | The cross-cutting concern itself (e.g. `LoggingAspect`) |
| Advice | The action taken (log before/after) |
| Pointcut | Which methods to intercept (an expression) |
| JoinPoint | The actual method invocation being intercepted |
| Weaving | Applying the aspect to the target at runtime (Spring does this via proxies) |

**Advice types:**

| Advice | Runs |
|---|---|
| `@Before` | Before the method executes |
| `@After` | After the method, always (like `finally`) |
| `@AfterReturning` | After the method returns successfully |
| `@AfterThrowing` | After the method throws |
| `@Around` | Wraps the method entirely — you control whether/when it proceeds |

```java
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* com.ems.service.*.*(..))")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logBefore(JoinPoint jp) {
        log.info(">>> Calling: {}.{}", jp.getTarget().getClass().getSimpleName(), jp.getSignature().getName());
    }
}
```
`@Aspect` marks the class as containing advice; `@Component` is still required to make it a Spring bean (an `@Aspect` alone isn't picked up by component scanning). `@Pointcut("execution(...)")` on an empty method defines a **named, reusable pointcut expression** — `execution(* com.ems.service.*.*(..))` reads as "any return type, any class directly in `com.ems.service`, any method name, any arguments." `@EnableAspectJAutoProxy` on a `@Configuration` class turns this weaving on.

`@Around` advice receives a `ProceedingJoinPoint` and must explicitly call `pjp.proceed()` to actually invoke the target method — this is what lets it measure elapsed time or add audit logging both before and after in a single method.

---

## 18. Spring MVC Architecture

Spring MVC is a Model-View-Controller web framework built on the Servlet API.

```
Browser Request
      │
      ▼
DispatcherServlet   ← Front Controller — every request passes through here
      │  asks HandlerMapping: "which controller handles this URL?"
      ▼
Controller           ← your @Controller class processes the request
      │  returns ModelAndView (data + view name)
      ▼
ViewResolver          ← resolves the view name to an actual JSP file
      ▼
View (JSP)            ← renders HTML using Model data
      ▼
Browser Response
```

| Component | Role |
|---|---|
| `DispatcherServlet` | Entry point — routes all requests |
| `HandlerMapping` | Maps a URL to a controller method |
| `Controller` | Handles the request, prepares the model |
| `Model` | Data handed to the view |
| `ViewResolver` | Translates a logical view name into a file path |
| `View` | Renders the actual response (JSP, Thymeleaf, ...) |

`DispatcherServlet` is the classic **Front Controller pattern**: instead of each URL mapping to a separate raw Servlet, one servlet receives every HTTP request and internally dispatches to the right `@Controller` method based on `@RequestMapping`/`@GetMapping`/`@PostMapping` metadata.

---

## 19. Setup & Configuration (Java, No `web.xml`)

Traditional Java EE web apps register servlets in `web.xml`. Spring MVC replaces that with a programmatic initializer:
```java
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{AppConfig.class}; // non-web beans (services, repos)
    }
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class}; // web/MVC beans (controllers, view resolver)
    }
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"}; // DispatcherServlet handles everything
    }
}
```
This class is picked up automatically by the Servlet 3.0+ container (Tomcat) via `ServletContainerInitializer` SPI discovery — no `web.xml` needed at all. It creates **two** Spring contexts: a **root context** (`getRootConfigClasses`, typically services/repositories, shared across the whole app) and a **servlet/web context** (`getServletConfigClasses`, MVC-specific beans like controllers and the view resolver, scoped to the `DispatcherServlet`). The web context can see beans from the root context, but not vice versa. The real project's `AppInitializer` (Part II §28) returns `null` for `getRootConfigClasses()` — it puts *everything* into the single servlet context instead of splitting root/web contexts.

```java
@Configuration
@EnableWebMvc
@ComponentScan("com.ems.controller")
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".jsp");
        return vr;
    }
}
```
`@EnableWebMvc` turns on Spring MVC's full annotation-driven configuration (handler mappings for `@RequestMapping`, message converters, validation, etc.) — without it, only a minimal default configuration applies. `InternalResourceViewResolver` turns a logical view name like `"employees/list"` into the path `/WEB-INF/views/employees/list.jsp` by concatenating `prefix + viewName + suffix`. Files under `WEB-INF` are not directly browser-accessible — reachable only via a server-side forward performed by the view resolver, a deliberate security measure that stops users hitting the raw JSP directly.

---

## 20. Writing Controllers

```java
@Controller
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/list")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees/list";  // resolves to /WEB-INF/views/employees/list.jsp
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/employees/list";
    }
}
```
`@RequestMapping("/employees")` at the class level establishes a base path all method-level mappings are relative to. `@GetMapping`/`@PostMapping` are shorthand meta-annotations for `@RequestMapping(method = RequestMethod.GET/POST)`. Returning a plain `String` from a `@Controller` method is treated as a **logical view name**, handed to the `ViewResolver` — unless it's prefixed `redirect:` (browser-level HTTP redirect, new request) or `forward:` (server-side forward, same request, URL unchanged in the browser).

---

## 21. Sending Data from Controller to UI

Three mechanisms:

**1. `Model`** — simplest; `addAttribute(name, value)` puts a key/value pair the JSP can read via EL (`${employees}`).

**2. `ModelAndView`** — bundles the model *and* the view name into a single return object:
```java
@GetMapping("/view/{id}")
public ModelAndView viewEmployee(@PathVariable int id) {
    ModelAndView mav = new ModelAndView();
    mav.setViewName("employees/detail");
    mav.addObject("employee", employeeService.findById(id));
    return mav;
}
```

**3. `@ModelAttribute`** on a *method* (not a parameter) — that method runs **before every handler method in the controller** and its return value is automatically added to the model under the given name. Useful for populating data (like a dropdown list) needed on every page the controller serves:
```java
@ModelAttribute("departments")
public List<Department> populateDepartments() {
    return departmentService.findAll(); // available in all views rendered by this controller
}
```

**JSP consumption** uses JSTL (`<c:forEach>`, `<c:if>`) and EL (`${...}`) to read model attributes:
```jsp
<c:forEach var="emp" items="${employees}">
    <tr><td>${emp.id}</td><td>${emp.name}</td></tr>
</c:forEach>
```

---

## 22. Sending Data from UI to Controller

**`@RequestParam`** — binds a query-string or form-encoded parameter:
```java
@GetMapping("/search")
public String searchEmployees(
        @RequestParam("name") String name,
        @RequestParam(value = "deptId", required = false) Integer deptId,
        Model model) { ... }
```
`required = false` makes the parameter optional (throws `MissingServletRequestParameterException` by default if a required param is absent).

**`@PathVariable`** — binds a segment of the URL path itself:
```java
@GetMapping("/view/{id}")
public String viewEmployee(@PathVariable("id") int id, Model model) { ... }
```

**Form submission (POST) with `@ModelAttribute` on a parameter** — Spring auto-populates a command object's fields from matching request-parameter names, and runs data binding/validation:
```java
@PostMapping("/save")
public String saveEmployee(@ModelAttribute("employee") Employee employee,
                           BindingResult result,
                           RedirectAttributes redirectAttrs) {
    if (result.hasErrors()) {
        return "employees/form"; // validation failed — back to the form
    }
    employeeService.save(employee);
    redirectAttrs.addFlashAttribute("successMsg", "Employee saved successfully!");
    return "redirect:/employees/list"; // PRG pattern — prevents double submit
}
```
`BindingResult` **must immediately follow** the `@ModelAttribute`/`@Valid` parameter in the method signature — Spring captures binding/validation errors into it instead of throwing, letting you branch on `result.hasErrors()`. `RedirectAttributes.addFlashAttribute(...)` stores a value that survives exactly one redirect (stored server-side briefly, e.g., in the session) — used to show a success message on the page you're redirected to, since a normal `Model` attribute wouldn't survive a redirect (redirect = a brand-new request).

The **PRG (Post-Redirect-Get) pattern** — responding to a `POST` with a `redirect:` rather than directly rendering a view — prevents the classic "resubmit form on refresh" browser warning, because the browser's last action becomes the `GET` on the redirect target, not the `POST`.

The Spring form tag library (`<form:form>`, `<form:input path="...">`, `<form:errors path="...">`) two-way binds form fields directly to a model-attribute object's properties by name, and can render field-specific validation errors inline.

---

## 23. `ModelMap`, Return Types, Redirect vs Forward

`ModelMap` is an enhanced `Map` implementation with the same `addAttribute()`-chaining API as `Model` — functionally interchangeable for most purposes; use whichever the codebase already favors.

```java
// String return — model already populated on the Model/ModelMap parameter
@GetMapping("/list")
public String list(Model model) { ... return "employees/list"; }

// ModelAndView — package model + view name into one return object
@GetMapping("/report")
public ModelAndView report() { ... }

// redirect: — new browser request, PRG pattern
return "redirect:/employees/list";

// forward: — server-side forward, same request, URL in browser doesn't change
return "forward:/employees/list";
```

---

## 24. Spring MVC + ORM — Full CRUD Flow

A complete pattern combining everything above — controller delegates to a `@Transactional` service, which delegates to a repository:
```java
@Service
@Transactional
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> findAll()  { return employeeRepository.findAll(); }
    public void save(Employee e)     { employeeRepository.save(e); }
    public void delete(int id)       { employeeRepository.delete(id); }
}
```
Putting `@Transactional` at the **class level of the service** (rather than the repository or controller) is exactly the courseware's stated best practice from §16 above — every public method on `EmployeeService` runs inside its own transaction boundary.

---

## 25. Spring MVC + AJAX (jQuery) — `@ResponseBody`

For partial page updates without a full reload, controller methods can return data (not a view name) directly as JSON:
```java
@GetMapping(value = "/search/ajax", produces = "application/json")
@ResponseBody
public List<Employee> searchAjax(@RequestParam String query) {
    return employeeService.search(query);
}
```
`@ResponseBody` tells Spring MVC to serialize the return value (via Jackson, if on the classpath) directly into the HTTP response body — **skipping the `ViewResolver` entirely**. This is the single-method equivalent of what `@RestController` does for an entire class. `produces = "application/json"` sets the `Content-Type` response header and also participates in content negotiation if the client sends an `Accept` header.

Client side: jQuery's `$.ajax({...})` fires the request and processes JSON in a `success` callback, updating the DOM directly (e.g., building an `<ul>` of search results) without navigating away from the page.

---

## 26. Complete Java Configuration for Web Apps

Putting it all together — a fully XML-free web app structure:
```
src/main/
├── java/com/ems/
│   ├── config/       AppConfig.java (services/repos), DataConfig.java (DataSource), WebConfig.java (MVC)
│   ├── init/         WebAppInitializer.java  ← replaces web.xml
│   ├── controller/, service/, repository/, model/
└── webapp/
    ├── WEB-INF/views/   ← JSP files, not directly browser-reachable
    └── resources/css, js
```

A fleshed-out `WebConfig`:
```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.ems.controller")
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver r = new InternalResourceViewResolver();
        r.setPrefix("/WEB-INF/views/");
        r.setSuffix(".jsp");
        r.setOrder(2);
        return r;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/employees/list");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("http://localhost:3000").allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        return ms;
    }
}
```
- `addResourceHandlers` tells Spring MVC to serve static files (CSS/JS) directly from a folder instead of trying to route them through `DispatcherServlet`'s controller-mapping logic.
- `addViewControllers` lets you map a URL straight to a view (or redirect) with no controller method at all — useful for trivial routes like a root `"/"` redirect.
- `addCorsMappings` configures Cross-Origin Resource Sharing for API endpoints, relevant when a separate frontend (e.g., a React/Angular SPA on a different origin) consumes the backend.
- `MessageSource` supports internationalization (i18n) — externalizing user-facing text into `messages.properties` files.
- `WebMvcConfigurer` is an interface with default (no-op) methods for all of these hooks — implementing it and overriding only what you need is the idiomatic Java-config customization point for Spring MVC, as opposed to XML `<mvc:resources>`/`<mvc:cors>` tags.

---

# Part II — The Real Project: `Code/Spring MVC` (`acme-spring-mvc-demo`)

This is a classic (non-Boot) Spring MVC WAR application. It is walked through below in dependency order: build file, config classes (which wire the infrastructure), models, DAO/Repository layer (contrasting three different data-access patterns), service layer, controllers, and finally a representative JSP view.

## 27. `pom.xml` — Dependencies

```java
1:	<project ...>

	<modelVersion>4.0.0</modelVersion>
	<groupId>com.demo</groupId>
	<artifactId>acme-spring-mvc-demo</artifactId>
	<packaging>war</packaging>       <!-- deployable WAR, not a runnable jar -->

	<properties>
		<maven.compiler.source>17</maven.compiler.source>
		<maven.compiler.target>17</maven.compiler.target>
	</properties>

	<dependencies>
		<!-- Spring MVC — @EnableWebMvc, @Controller, DispatcherServlet infra -->
		<dependency><groupId>org.springframework</groupId><artifactId>spring-webmvc</artifactId></dependency>

		<!-- Servlet API — compile-only, container supplies it at runtime -->
		<dependency><groupId>jakarta.servlet</groupId><artifactId>jakarta.servlet-api</artifactId><scope>provided</scope></dependency>

		<!-- JSP support: Jasper compiler + JSTL API + JSTL implementation -->
		<dependency><artifactId>tomcat-embed-jasper</artifactId></dependency>
		<dependency><groupId>jakarta.servlet.jsp.jstl</groupId><artifactId>jakarta.servlet.jsp.jstl-api</artifactId></dependency>
		<dependency><groupId>org.glassfish.web</groupId><artifactId>jakarta.servlet.jsp.jstl</artifactId></dependency>

		<!-- Spring JDBC + H2 in-memory DB — Department -->
		<dependency><groupId>org.springframework</groupId><artifactId>spring-jdbc</artifactId></dependency>
		<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId></dependency>

		<!-- Spring ORM + Hibernate — Project -->
		<dependency><groupId>org.springframework</groupId><artifactId>spring-orm</artifactId></dependency>
		<dependency><groupId>org.hibernate.orm</groupId><artifactId>hibernate-core</artifactId></dependency>

		<!-- Spring Data MongoDB + driver — Employee -->
		<dependency><groupId>org.springframework.data</groupId><artifactId>spring-data-mongodb</artifactId></dependency>
		<dependency><groupId>org.mongodb</groupId><artifactId>mongodb-driver-sync</artifactId></dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin> <!-- maven-compiler-plugin -->
				<configuration><compilerArgs><arg>-parameters</arg></compilerArgs></configuration>
			</plugin>
			<plugin> <!-- maven-war-plugin -->
				<configuration><failOnMissingWebXml>false</failOnMissingWebXml></configuration>
			</plugin>
		</plugins>
	</build>
</project>
```
- `<packaging>war</packaging>`: a deployable Web Application Archive, not a runnable jar — must be deployed into a Servlet container (Tomcat, etc.), consistent with there being no embedded server dependency (no Spring Boot starter).
- `spring-webmvc`: brings in `DispatcherServlet`, `@Controller`, `@RequestMapping`, `ViewResolver` infrastructure.
- `jakarta.servlet-api` with `<scope>provided</scope>`: needed to *compile* against (`HttpServletRequest` etc.) but supplied by the container at runtime — bundling it in the WAR would cause classpath conflicts.
- `tomcat-embed-jasper` (JSP compiler) + JSTL API/impl (`org.glassfish.web`): together let JSPs use `<c:...>`/`<fmt:...>` tags — API alone won't render anything, both are required.
- `spring-jdbc` (provides `JdbcTemplate`, used by `DepartmentRepository`) + `h2` (embeddable in-memory relational DB, used for `Department` and `Project` data — no external DB server needed).
- `spring-orm` (bridges Spring DI/transactions with Hibernate) + `hibernate-core` (the ORM engine, used by `ProjectDaoImpl`) + `spring-data-mongodb` (repository abstraction — what makes `EmployeeRepository extends MongoRepository<...>` work with **zero implementation code**) + `mongodb-driver-sync` (low-level driver `spring-data-mongodb` sits on top of; provides `MongoClient`/`MongoClients` used in `MongoConfig`).
- `-parameters` compiler flag: preserves real parameter names in bytecode (normally erased to `arg0`, `arg1`...). Lets Spring bind `@RequestParam`/`@PathVariable` **without** an explicit `value` when names match, and is required for constructor-arg-by-name DI resolution.
- `failOnMissingWebXml=false`: don't fail the build over the absence of `web.xml`, since this project uses `AppInitializer` (Java-based Servlet 3.0+ registration) instead.

---

## 28. `AppInitializer.java` — Replaces `web.xml`

```java
1:	package com.demo.config;
2:	
3:	import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
4:	
5:	public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
6:	
7:		@Override
8:		protected Class<?>[] getRootConfigClasses() {
9:			return null;
10:		}
11:	
12:		@Override
13:		protected Class<?>[] getServletConfigClasses() {
14:			return new Class[] { WebConfig.class };
15:		}
16:	
17:		@Override
18:		protected String[] getServletMappings() {
19:			return new String[] { "/" };
20:		}
21:	}
```
- **Line 5** — extends `AbstractAnnotationConfigDispatcherServletInitializer`, a Spring base class implementing the Servlet 3.0+ `ServletContainerInitializer` SPI. Tomcat auto-discovers this class on startup (via `META-INF/services` scanning baked into the Spring jar) and uses it to programmatically register the `DispatcherServlet` — no `web.xml` file exists anywhere in this project.
- **Line 8–10** — `getRootConfigClasses()` returns `null`: unlike the "textbook" two-context split described in §19 (root context for services/repos, separate web context for MVC), this project puts **everything** (config, controllers, services, repositories, DAOs) into a *single* context via `getServletConfigClasses()`. This is a valid simplification for small apps but a deliberate departure from the split-context pattern taught in the courseware.
- **Line 13–15** — `getServletConfigClasses()` returns `WebConfig.class`: this one class is the entire Spring configuration for the app — it is where component scanning is triggered (see §29), which is how `H2Config`, `HibernateConfig`, `MongoConfig`, all controllers, services, and repositories actually get registered (they're picked up transitively by `@ComponentScan(basePackages = "com.demo")` inside `WebConfig`, not listed explicitly here).
- **Line 18–20** — `getServletMappings()` returns `{"/"}`: the `DispatcherServlet` is mapped to every URL under the app's context path — all requests are front-controlled through Spring MVC.

---

## 29. `WebConfig.java` — MVC Configuration & Component Scan Root

```java
1:	package com.demo.config;
2:	
3:	import org.springframework.context.annotation.Bean;
4:	import org.springframework.context.annotation.ComponentScan;
5:	import org.springframework.context.annotation.Configuration;
6:	import org.springframework.web.servlet.ViewResolver;
7:	import org.springframework.web.servlet.config.annotation.EnableWebMvc;
8:	import org.springframework.web.servlet.view.InternalResourceViewResolver;
9:	
10:	@Configuration
11:	@EnableWebMvc
12:	@ComponentScan(basePackages = "com.demo")
13:	public class WebConfig {
14:	
15:		@Bean
16:		public ViewResolver viewResolver() {
17:			InternalResourceViewResolver resolver = new InternalResourceViewResolver();
18:			resolver.setPrefix("/WEB-INF/views/");
19:			resolver.setSuffix(".jsp");
20:			return resolver;
21:		}
22:	}
```
- **Line 10** — `@Configuration`: registers this class as a Java-based bean-definition source; Spring proxies it (CGLIB) so `@Bean` methods behave as singleton factories.
- **Line 11** — `@EnableWebMvc`: activates Spring MVC's full annotation-driven infrastructure — handler mappings for `@Controller`/`@RequestMapping`, argument resolvers for `@RequestParam`/`@PathVariable`/`@ModelAttribute`, message converters, etc. Without this, only a bare-minimum default MVC configuration would apply.
- **Line 12** — `@ComponentScan(basePackages = "com.demo")`: this is the single line responsible for discovering **every** `@Controller`, `@Service`, `@Repository`, and other `@Configuration` class in the app (`H2Config`, `HibernateConfig`, `MongoConfig` all live under `com.demo` and get picked up here too, since `@Configuration` is itself meta-annotated with `@Component`). This is why `AppInitializer` (§28) doesn't need to list every config class explicitly — scanning `com.demo` from this one root pulls in the entire object graph.
- **Line 15–21** — the `viewResolver()` bean: `InternalResourceViewResolver` is the standard JSP view resolver; `setPrefix`/`setSuffix` mean a controller returning `"employees"` resolves to `/WEB-INF/views/employees.jsp`. Placing views under `WEB-INF` prevents them from being requested directly by URL (only reachable via a server-side forward that Spring MVC performs after resolving the view name).

---

## 30. `H2Config.java` — Relational DataSource + JdbcTemplate (Department, Project)

```java
1:	package com.demo.config;
2:	
3:	import javax.sql.DataSource;
4:	
5:	import org.springframework.context.annotation.Bean;
6:	import org.springframework.context.annotation.Configuration;
7:	import org.springframework.core.io.ClassPathResource;
8:	import org.springframework.jdbc.core.JdbcTemplate;
9:	import org.springframework.jdbc.datasource.DriverManagerDataSource;
10:	import org.springframework.jdbc.datasource.init.DataSourceInitializer;
11:	import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
12:	
13:	@Configuration
14:	public class H2Config {
15:	
16:		private static final String URL = "jdbc:h2:mem:employeedb;DB_CLOSE_DELAY=-1";
17:		private static final String USERNAME = "sa";
18:		private static final String PASSWORD = "";
19:	
20:		@Bean
21:		public DataSource dataSource() {
22:			DriverManagerDataSource ds = new DriverManagerDataSource();
23:			ds.setDriverClassName("org.h2.Driver");
24:			ds.setUrl(URL);
25:			ds.setUsername(USERNAME);
26:			ds.setPassword(PASSWORD);
27:			return ds;
28:		}
29:	
30:		@Bean
31:		public JdbcTemplate jdbcTemplate(DataSource dataSource) {
32:			return new JdbcTemplate(dataSource);
33:		}
34:	
35:		@Bean
36:		public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
37:			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
38:			populator.addScript(new ClassPathResource("departments.sql"));
39:			populator.addScript(new ClassPathResource("projects.sql"));
40:			populator.setContinueOnError(false);
41:	
42:			DataSourceInitializer initializer = new DataSourceInitializer();
43:			initializer.setDataSource(dataSource);
44:			initializer.setDatabasePopulator(populator);
45:			return initializer;
46:		}
47:	}
```
- **Line 13** — `@Configuration`, no `@ComponentScan` here — this class is itself discovered by `WebConfig`'s scan of `com.demo` (§29).
- **Line 16** — `DB_CLOSE_DELAY=-1` keeps the **in-memory** H2 database alive for the JVM's lifetime even when the last connection closes — without it, H2 wipes the DB as soon as all connections drop, losing data between short-lived per-request connections.
- **Line 21–28** — `DriverManagerDataSource` is Spring's simplest (non-pooling) `DataSource` — opens a fresh JDBC connection per request rather than pooling; fine for a demo, not production (contrast the courseware's pooling `HikariDataSource`, §13).
- **Line 30–33** — `jdbcTemplate(DataSource dataSource)`: Spring autowires by parameter type into `@Bean` factory methods, supplying the `DataSource` bean above with no explicit wiring — this is the exact bean `DepartmentRepository` depends on.
- **Line 35–46** — `dataSourceInitializer`: exists purely for its **side effect** at startup — `ResourceDatabasePopulator` runs `departments.sql`/`projects.sql` against the `DataSource` to create/seed the schema, since H2 starts empty every JVM restart. `setContinueOnError(false)` aborts startup on any SQL error rather than continuing with a partial schema. Never referenced elsewhere — registering it is enough to trigger the populate-on-startup lifecycle callback.

---

## 31. `HibernateConfig.java` — ORM Layer (Project)

```java
1:	package com.demo.config;
2:	
3:	import java.util.Properties;
4:	
5:	import javax.sql.DataSource;
6:	
7:	import org.hibernate.SessionFactory;
8:	import org.springframework.context.annotation.Bean;
9:	import org.springframework.context.annotation.Configuration;
10:	import org.springframework.orm.hibernate5.HibernateTransactionManager;
11:	import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
12:	import org.springframework.transaction.annotation.EnableTransactionManagement;
13:	@Configuration
14:	@EnableTransactionManagement
15:	public class HibernateConfig {
16:		
17:		// Inside your @Configuration class
18:	
19:		@Bean
20:		public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
21:		    LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
22:		    sf.setDataSource(dataSource);
23:		    sf.setPackagesToScan("com.demo.model");
24:		    Properties props = new Properties();
25:		    props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
26:		    props.put("hibernate.hbm2ddl.auto", "update");
27:		    props.put("hibernate.show_sql", "true");
28:		    sf.setHibernateProperties(props);
29:		    return sf;
30:		}
31:	
32:		@Bean
33:		public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
34:		    return new HibernateTransactionManager(sessionFactory);
35:		}
36:	
37:	}
```
- **Line 14** — `@EnableTransactionManagement`: activates processing of `@Transactional` for this context — without it, `@Transactional` on `ProjectDaoImpl` (§35) would be a no-op.
- **Line 20** — the `DataSource` parameter is autowired by type and is **the same `DataSource` bean defined in `H2Config`** (§30) — Hibernate (`Project`) and `JdbcTemplate` (`Department`) share the exact same underlying H2 database/connection source.
- **Line 21–29** — `setPackagesToScan("com.demo.model")` tells Hibernate to scan for `@Entity` classes (only `Project` qualifies). `hibernate.hbm2ddl.auto=update` auto-creates/alters the `projects` table from the `@Entity` mapping at startup — contrast with `departments`, whose schema is instead created manually via the `departments.sql` script (§30), another instance of the two-pattern split, this time at schema-management level. `hibernate.show_sql=true` logs generated SQL — debug-only, not for production.
- **Line 32–35** — registers `HibernateTransactionManager`, the `PlatformTransactionManager` that makes `@Transactional` on `ProjectDaoImpl` actually begin/commit/rollback a Hibernate `Session`. Note `sessionFactory()` returns `LocalSessionFactoryBean` (a `FactoryBean<SessionFactory>`) but Spring auto-unwraps it, injecting the *product* `SessionFactory` here.

---

## 32. `MongoConfig.java` — Document Store for Employee

```java
1:	package com.demo.config;
2:	
3:	import org.springframework.context.annotation.Configuration;
4:	
5:	import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
6:	import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
7:	
8:	import com.mongodb.client.MongoClient;
9:	import com.mongodb.client.MongoClients;
10:	
11:	@Configuration
12:	@EnableMongoRepositories(basePackages = "com.demo.repository")
13:	public class MongoConfig extends AbstractMongoClientConfiguration {
14:	
15:		@Override
16:		protected String getDatabaseName() {
17:			return "acme-ems"; // MongoDB will create this DB automatically if it doesn't exist
18:		}
19:	
20:		@Override
21:		public MongoClient mongoClient() {
22:			// For local MongoDB running on default port 27017
23:			return MongoClients.create("mongodb://localhost:27017");
24:		}
25:	}
```
- **Line 12** — `@EnableMongoRepositories(basePackages = "com.demo.repository")`: makes Spring Data generate a runtime (dynamic proxy) implementation of any interface under `com.demo.repository` extending `MongoRepository` — specifically `EmployeeRepository` (§36). No manual implementation class is written at all.
- **Line 13** — extends `AbstractMongoClientConfiguration`: a Spring Data base class providing default beans (`MongoTemplate`, converters) once you supply just the database name and client, via the two overrides below.
- **Line 15–18** — `getDatabaseName()`: MongoDB is schemaless — the `"acme-ems"` database and its `emps` collection (`@Document(collection = "emps")` on `Employee`, §34) are created lazily on first write; no equivalent of `departments.sql`/`hbm2ddl.auto` is needed.
- **Line 20–24** — `mongoClient()`: manually constructs a `MongoClient` pointed at `localhost:27017` — a **hard requirement**: unlike in-memory H2, `Employee` CRUD needs a real, separately-running MongoDB server reachable at that address or every `EmployeeRepository` call fails at runtime.

---

## 33. `application.properties`

```java
1:	 MongoDB (existing)
2:	mongodb.host=localhost
3:	mongodb.port=27017
4:	mongodb.database=acme-ems
5:	
6:	# H2 — Spring JDBC (Department)
7:	h2.url=jdbc:h2:mem:employeedb;DB_CLOSE_DELAY=-1
8:	h2.username=sa
9:	h2.password=
10:	h2.driver=org.h2.Driver
11:	
```
- **Line 1** — a comment line missing its leading `#` — not a valid `.properties` comment. Since nothing in the codebase actually loads this file via `@PropertySource`/`@Value` (all connection details are hard-coded directly in `MongoConfig`/`H2Config`, §30 and §32), it is effectively **inert documentation**, not live configuration — a notable gap versus the "Externalizing Configuration" pattern taught in §8/§12: the values described here don't actually feed the `@Bean` definitions.
- **Line 2–10** — documents the intended MongoDB and H2 connection parameters, matching the values hard-coded in `MongoConfig`/`H2Config` respectively.

---

## 34. Model Layer

### 34.1 `Department.java` — Plain POJO (no persistence annotations)
```java
package com.demo.model;

public class Department {
	private int id;
	private String name;

	public Department() { }

	public Department(int id, String name) {   // used by DepartmentController.addDepartment(...) and the row-mapping lambda
		this.id = id;
		this.name = name;
	}

	// getId/setId, getName/setName — plain accessors, no persistence annotations
	@Override
	public String toString() {
		return "Department [id=" + id + ", name=" + name + "]";
	}
}
```
- No `@Entity`, no `@Document` — `Department` carries **zero persistence-framework annotations**. Consistent with it being accessed through hand-written SQL in `DepartmentRepository` (§36.1): `JdbcTemplate`'s `RowMapper` constructs `Department` objects manually from a `ResultSet`, so no ORM metadata is needed — the mapping logic lives entirely in the repository, not the model.

### 34.2 `Employee.java` — MongoDB Document
```java
package com.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "emps") // maps to the "emps" collection (comment claiming "employees" is stale)
public class Employee {

	@Id                    // org.springframework.data.annotation.Id — NOT jakarta.persistence.Id
	private int id;        // MongoDB maps this onto the native _id field
	private String name;
	private double salary;

	public Employee(int id, String name, double salary) { ... }
	// getId/setId, getName/setName, getSalary/setSalary, toString — plain accessors
}
```
- `@Document(collection = "emps")`: the Spring Data MongoDB equivalent of JPA's `@Entity`/`@Table`.
- `@Id` here is **`org.springframework.data.annotation.Id`**, not JPA's `jakarta.persistence.Id` used on `Project` (§34.3) — same annotation *name*, different package/framework/persistence mechanism entirely — a classic import trap.
- `private int id`: MongoDB documents natively use a `_id` field (normally an `ObjectId`); Spring Data maps whichever field carries `@Id` onto `_id` automatically, converting types as needed.
- (Dead code, not reproduced above: commented-out alternative id designs, and an earlier in-memory-only draft of `Employee` — evidence the class was migrated from plain in-memory to Mongo-backed during the course.)

### 34.3 `Project.java` — JPA/Hibernate Entity
```java
package com.demo.model;

import jakarta.persistence.*;   // JPA's @Entity/@Id — NOT Spring Data's (contrast Employee, §34.2)

@Entity
@Table(name = "projects")
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)   // delegates PK generation to DB auto-increment
	private int id;
	private String name;
	private String description;
	private String status;

	public Project(String name, String description, String status) { ... }   // id left unset — IDENTITY-generated on insert
	// getters/setters — no @Column annotations; Hibernate infers column names from field names
}
```
- `import jakarta.persistence.*`: this is **JPA's** `@Entity`/`@Id`, the standard specification Hibernate implements — contrast directly with `Employee`'s Spring-Data-specific `@Id` (§34.2). The clearest illustration in this codebase of "know your imports": same annotation name, different framework.
- `@Entity` is required for `HibernateConfig`'s `setPackagesToScan("com.demo.model")` (§31) to pick this class up. `@Table(name = "projects")` gives an explicit table name, matching what Hibernate auto-creates via `hibernate.hbm2ddl.auto=update`.

---

## 35. DAO Layer — `ProjectDao` / `ProjectDaoImpl` (Hibernate Pattern)

### 35.1 `ProjectDao.java` — the interface
```java
public interface ProjectDao {
	void addProject(Project project);
	List<Project> getAllProjects();
	Project getProjectById(int id);
	void updateProject(Project project);
	void deleteProject(int id);
}
```
- A plain interface with **no Spring annotations at all** — this is the classic "program to interfaces" principle from §11: the DAO's *contract* is decoupled from its implementation, and callers (`ProjectServiceImpl`, §37.4) depend only on this interface type, never on `ProjectDaoImpl` directly.

### 35.2 `ProjectDaoImpl.java` — the implementation
```java
1:	package com.demo.dao;
2:	
3:	import com.demo.model.Project;
4:	import org.hibernate.SessionFactory;
5:	import org.springframework.beans.factory.annotation.Autowired;
6:	import org.springframework.stereotype.Repository;
7:	import org.springframework.transaction.annotation.Transactional;
8:	
9:	import java.util.List;
10:	
11:	@Repository
12:	@Transactional
13:	public class ProjectDaoImpl implements ProjectDao {
14:	
15:		@Autowired
16:		private SessionFactory sessionFactory;
17:	
18:		@Override
19:		public void addProject(Project project) {
20:			sessionFactory.getCurrentSession().persist(project);
21:		}
22:	
23:		@Override
24:		public List<Project> getAllProjects() {
25:			return sessionFactory.getCurrentSession().createQuery("from Project", Project.class).list();
26:		}
27:	
28:		@Override
29:		public Project getProjectById(int id) {
30:			return sessionFactory.getCurrentSession().get(Project.class, id);
31:		}
32:	
33:		@Override
34:		public void updateProject(Project project) {
35:			sessionFactory.getCurrentSession().merge(project);
36:		}
37:	
38:		@Override
39:		public void deleteProject(int id) {
40:			Project p = sessionFactory.getCurrentSession().get(Project.class, id);
41:			if (p != null) {
42:				sessionFactory.getCurrentSession().remove(p);
43:			}
44:		}
45:	}
```
- **Line 11** — `@Repository`: registers this as a Spring-managed bean *and* enables exception translation (converts Hibernate's native exceptions into Spring's `DataAccessException` hierarchy, §10).
- **Line 12** — `@Transactional` at the **class level**: every public method runs in its own Spring-managed transaction (via the `HibernateTransactionManager` bean from `HibernateConfig`, §31). This deviates from the courseware's stated best practice in §16 ("put `@Transactional` on the service layer, not the repository") — functionally correct here, but not textbook layering.
- **Line 15–16** — field injection of the `SessionFactory` bean built in `HibernateConfig.sessionFactory(...)` (§31) — the *product* type, not `LocalSessionFactoryBean`, per the `FactoryBean` unwrapping explained there.
- **Line 20** — `getCurrentSession()` returns the Hibernate session bound to the active Spring-managed transaction for the current thread — you never call `openSession()`/`close()` manually in Spring-managed code. `persist(...)` INSERTs and populates the auto-generated id back onto `project`.
- **Line 25** — `createQuery("from Project", ...)` uses **HQL**: `"from Project"` queries by entity name (the Java class), not the SQL table name (`projects`) — a common point of confusion coming from raw SQL.
- **Line 30** — `get(...)` returns `null` if no row matches (unlike JPA's lazy `getReference`) — hence the explicit null check before `remove` on line 40.
- **Line 35** — `merge(project)` reattaches a **detached** entity (e.g. one built from form data outside an active session) by copying its state onto the managed instance and returning that instance — it does not mutate the passed-in object in place.
- **Line 39–44** — delete-by-id loads the managed entity via `get` first, then calls `remove` on it — Hibernate requires operating on a managed entity to delete safely, hence get-then-remove rather than a single call.

---

## 36. Repository Layer — Two Contrasting Patterns

### 36.1 `DepartmentRepository.java` — Hand-Written, `JdbcTemplate`-Based (SQL pattern)
```java
1:	package com.demo.repository;
2:	
3:	import java.util.List;
4:	
5:	import org.springframework.jdbc.core.JdbcTemplate;
6:	import org.springframework.jdbc.core.RowMapper;
7:	import org.springframework.stereotype.Repository;
8:	
9:	import com.demo.model.Department;
10:	
11:	@Repository
12:	public class DepartmentRepository {
13:	
14:		private final JdbcTemplate jdbcTemplate;
15:	
16:		public DepartmentRepository(JdbcTemplate jdbcTemplate) {
17:			this.jdbcTemplate = jdbcTemplate;
18:		}
19:	
20:		private final RowMapper<Department> rowMapper = (rs, rowNum) -> new Department(rs.getInt("id"),
21:				rs.getString("name"));
22:	
23:		public List<Department> findAll() {
24:			return jdbcTemplate.query("SELECT * FROM departments", rowMapper);
25:		}
26:	
27:		public Department findById(int id) {
28:			List<Department> result = jdbcTemplate.query("SELECT * FROM departments WHERE id = ?", rowMapper, id);
29:			return result.isEmpty() ? null : result.get(0);
30:		}
31:	
32:		public int save(Department dept) {
33:			return jdbcTemplate.update("INSERT INTO departments (id, name) VALUES (?, ?)", dept.getId(), dept.getName());
34:		}
35:	}
```
- **Line 11** — `@Repository`, no `@Transactional` anywhere — single-statement `JdbcTemplate` calls auto-commit at the JDBC level by default, unlike the Hibernate DAO which needs an active session/transaction to function at all.
- **Line 14, 16–18** — **constructor injection** of `JdbcTemplate` (the bean from `H2Config`, §30), stored `final` — the courseware's *recommended* DI style (§4), notably different from `EmployeeService`'s field-injection style elsewhere in this project.
- **Line 20–21** — `RowMapper<Department>` implemented as a **lambda** (a terser, modern alternative to the courseware's separate `EmployeeRowMapper` class, §14) — maps `id`/`name` columns onto the `Department(int, String)` constructor directly.
- **Line 27–30** — `findById`: uses the **list-returning** `query(...)` overload rather than `queryForObject` (which throws `EmptyResultDataAccessException` on zero rows), manually checking for an empty list — a defensive style that lets the caller receive a plain `null` for "not found" instead of catching an exception.
- **Line 32–34** — `save(...)`: `jdbcTemplate.update(sql, args...)` returns affected-row count, not a generated key — `id` is supplied explicitly by the caller since `departments` has no auto-increment configured in this hand-rolled SQL path (unlike `Project`'s `IDENTITY` id).

### 36.2 `EmployeeRepository.java` — Spring Data MongoDB (zero-implementation pattern)
```java
1:	package com.demo.repository;
2:	
3:	import org.springframework.data.mongodb.repository.MongoRepository;
4:	import org.springframework.stereotype.Repository;
5:	
6:	import com.demo.model.Employee;
7:	
8:	@Repository
9:	public interface EmployeeRepository extends MongoRepository<Employee, Integer> {
10:		// MongoRepository gives you these for free — no code needed:
11:		// findAll() → getAllEmployees
12:		// findById(id) → getEmployeeById
13:		// save(employee) → addEmployee, updateEmployee
14:		// deleteById(id) → deleteEmployee (for later)
15:	
16:	//	public abstract List<Employee> findByName(String name);
17:	//
18:	//	@Query("select u from User u where u.emailAddress = ?1")
19:	//	Employee findByEmailAddress(String emailAddress);
20:	
21:	}
```
- **Line 9** — the key contrast with §35/§36.1: **no implementation class exists anywhere in the project.** `MongoRepository<T, ID>` already declares `findAll()`, `findById(ID)`, `save(T)`, `deleteById(ID)`, etc. `@EnableMongoRepositories(basePackages = "com.demo.repository")` in `MongoConfig` (§32) makes Spring Data generate a **dynamic proxy implementation** at startup — no query code at all, only a declared method contract.
- **Line 8** — `@Repository` on the interface is slightly redundant with `@EnableMongoRepositories` (which already registers the proxy bean) but harmless.
- **Line 16–19** (commented out) — shows the next step for custom finders: `findByName(String name)` would be auto-implemented via Spring Data's method-name-parsing convention, but the `@Query(...)` example shown is stale JPQL syntax (`select u from User u where...`) left over from a JPA example — not valid for Mongo, which needs MongoDB JSON query syntax instead.

---

## 37. Service Layer

### 37.1 `DepartmentService.java`
```java
1:	package com.demo.service;
2:	
3:	import java.util.List;
4:	
5:	import org.springframework.stereotype.Service;
6:	
7:	import com.demo.model.Department;
8:	import com.demo.repository.DepartmentRepository;
9:	
10:	@Service
11:	public class DepartmentService {
12:	
13:	    private final DepartmentRepository repository;
14:	
15:	    public DepartmentService(DepartmentRepository repository) {
16:	        this.repository = repository;
17:	    }
18:	
19:	    public List<Department> getAllDepartments() {
20:	        return repository.findAll();
21:	    }
22:	
23:	    public Department getDepartmentById(int id) {
24:	        return repository.findById(id);
25:	    }
26:	
27:	    public int addDepartment(Department department) {
28:	        return repository.save(department);
29:	    }
30:	}
```
- **Line 10** — `@Service`: marks this as the business-logic-layer bean (§10), picked up by `WebConfig`'s `@ComponentScan("com.demo")`.
- **Line 13, 15–17** — constructor injection of `DepartmentRepository`, `final` field, with **no explicit `@Autowired`**: since Spring 4.3, a class with exactly **one** constructor is implicitly autowired without the annotation.
- **Line 19–29** — thin pass-through methods delegating directly to the repository — no business logic, validation, or transaction demarcation at this layer (contrast §24's courseware example, which wraps the equivalent service in class-level `@Transactional`). Harmless here since `DepartmentRepository` only issues single, auto-committing statements, but multi-step operations would not be atomic without it.

### 37.2 `EmployeeService.java`
Same structural pattern as `DepartmentService` (constructor injection, implicit `@Autowired`, thin delegation over `EmployeeRepository`). Only the differing lines:
```java
private final EmployeeRepository repository;   // constructor-injected, same as DepartmentService

public Employee getEmployeeById(int id) {
    return repository.findById(id).orElse(null); // findById returns Optional
}
```
- **Line 13** — injects `EmployeeRepository`, the **interface type** (§36.2); Spring transparently supplies the Spring-Data-generated proxy — the service has no idea there's no hand-written implementation.
- **Line 25** — `repository.findById(id).orElse(null)`: `MongoRepository.findById` returns `Optional<Employee>` (modern API design forcing callers to handle not-found explicitly); `.orElse(null)` unwraps it to match this app's nullable convention elsewhere (e.g. `DepartmentRepository.findById`).
- (Commented out further down: an earlier fully in-memory `ArrayList<Employee>` version of this service — same progression noted for the `Employee` model in §34.2.)

### 37.3 `ProjectService.java` — the interface
```java
public interface ProjectService {
	void addProject(Project project);
	List<Project> getAllProjects();
	Project getProjectById(int id);
	void updateProject(Project project);
	void deleteProject(int id);
}
```
- Again "program to interfaces" (§11): `ProjectController` (§38.4) is written against this interface, not a concrete class, meaning the implementation is swappable.

### 37.4 `ProjectServiceImpl.java` — the active implementation
Same thin-delegation pattern as `DepartmentService`/`EmployeeService` (every method one-line delegates to `projectDao`, no `@Transactional` here — transaction boundaries for `Project` sit one layer down on `ProjectDaoImpl`, §35.2). Only the differing lines:
```java
@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectDao projectDao;   // field injection, not constructor injection

	// every method: one-line delegation, e.g.
	public void addProject(Project project) {
		projectDao.addProject(project);
	}
}
```
Notable differences from `DepartmentService`:
- **Line 11** — `@Service` with **no explicit bean name** defaults to `projectServiceImpl`. Matters because a second implementation exists (§37.5) — if it were ever un-commented and also `@Service`-annotated, `ProjectController`'s field injection (§38.4) would become ambiguous and fail with `NoUniqueBeanDefinitionException` (the scenario §11's `@Qualifier` example is designed to prevent).
- **Line 14–15** — **field injection** (`@Autowired` on the field, not constructor injection like `DepartmentService`) of `ProjectDao`; Spring resolves it to `ProjectDaoImpl` (§35.2), the only implementing bean.

### 37.5 `ProjectServiceImpl2.java` — commented-out stub
```java
1:	//package com.demo.service;
2:	//
3:	//public class ProjectServiceImpl2 implements ProjectService {
4:	//	
5:	//}
```
- **Every line** — the entire file content is commented out; it is a placeholder for a **second** `ProjectService` implementation that was never completed. Its mere presence (even inert) illustrates what an interface-based design is *for* — the same shape as §11's dual `EmailNotificationService`/`SmsNotificationService` example: multiple implementations of one interface, disambiguated (if both were active) via `@Qualifier` or distinct `@Service("beanName")` values. To make both coexist: give each a distinct bean name (e.g. `@Service("projectServiceImpl2")`) and annotate the injection point in `ProjectController` with `@Qualifier(...)` to pick one explicitly.

---

## 38. Controller Layer

### 38.1 `HelloController.java` — Minimal `@ResponseBody` Example
```java
@Controller
public class HelloController {

	@GetMapping("/")
	@ResponseBody
	public String home() {
		System.out.println("home");
		return "Welcome!";
	}
	// hi() and hello() repeat the identical pattern for "/hi" and "/hello"
}
```
- `@Controller`: registers this as a Spring MVC handler bean.
- Each method pairs `@GetMapping(path)` with `@ResponseBody`: normally a `@Controller` method's `String` return is treated as a *view name* to resolve (§29); `@ResponseBody` overrides that and writes the string directly as the raw HTTP response body — no JSP involved. Exact mechanism from §25, applied per-method instead of class-level (as `@RestController` would do automatically).
- `System.out.println(...)` calls are just console confirmation the endpoint was reached — a "hello world" teaching habit, not production logging practice.

### 38.2 `DepartmentController.java`
```java
1:	package com.demo.controller;
2:	
3:	import java.util.List;
4:	
5:	import org.springframework.stereotype.Controller;
6:	import org.springframework.ui.Model;
7:	import org.springframework.web.bind.annotation.GetMapping;
8:	import org.springframework.web.bind.annotation.PostMapping;
9:	import org.springframework.web.bind.annotation.RequestParam;
10:	
11:	import com.demo.model.Department;
12:	import com.demo.service.DepartmentService;
13:	
14:	@Controller
15:	public class DepartmentController {
16:	
17:	    private final DepartmentService service;
18:	
19:	    public DepartmentController(DepartmentService service) {
20:	        this.service = service;
21:	    }
22:	
23:	    // GET /departments
24:	    @GetMapping("/departments")
25:	    public String getDepartments(Model model) {
26:	        List<Department> list = service.getAllDepartments();
27:	        model.addAttribute("departments", list);
28:	        return "departments";
29:	    }
30:	
31:	    // GET /departments/find?id=2
32:	    @GetMapping("/departments/find")
33:	    public String findDepartmentById(@RequestParam("id") int id, Model model) {
34:	        Department dept = service.getDepartmentById(id);
35:	        if (dept != null) {
36:	            model.addAttribute("foundDepartment", dept);
37:	        } else {
38:	            model.addAttribute("notFound", "No department found with ID: " + id);
39:	        }
40:	        model.addAttribute("departments", service.getAllDepartments());
41:	        return "departments";
42:	    }
43:	
44:	    // POST /departments/add
45:	    @PostMapping("/departments/add")
46:	    public String addDepartment(
47:	            @RequestParam("id") int id,
48:	            @RequestParam("name") String name,
49:	            Model model) {
50:	        Department dept = new Department(id, name);
51:	        service.addDepartment(dept);
52:	        model.addAttribute("departments", service.getAllDepartments());
53:	        model.addAttribute("message", "Department added successfully!");
54:	        return "departments";
55:	    }
56:	}
```
- **Line 14** — no `@RequestMapping` at the class level (unlike the courseware's `@RequestMapping("/employees")` base-path pattern, §20) — every method specifies its **full** path individually.
- **Line 17, 19–21** — constructor injection of `DepartmentService`, `final` field, implicit `@Autowired` (single constructor) — same recommended pattern as `DepartmentService` (§37.1); the most "textbook" of the four controllers in DI style.
- **Line 24–29** — `getDepartments`: simple `Model`-based list-and-render, returning logical view `"departments"` → `/WEB-INF/views/departments.jsp` (§39).
- **Line 32–42** — `findDepartmentById`: **always** re-adds the full `departments` list to the model (line 40) regardless of match, because `departments.jsp` (§39) always renders the full table *and* an optional "found" result on the same page.
- **Line 45–55** — `addDepartment`: handles raw `@RequestParam`s rather than binding via `@ModelAttribute` (contrast the courseware's `@ModelAttribute("employee") Employee employee` pattern in §22) — simpler for a two-field form but no automatic validation/`BindingResult`. Also **no PRG pattern**: returns the view directly instead of `"redirect:/departments"`, so a page refresh after submit would resubmit the POST (the exact problem PRG, §22, avoids).

### 38.3 `EmployeeController.java`
Structurally identical to `DepartmentController` — list, find-by-id (same "always include the full list" pattern), add, all via raw `@RequestParam`s, no PRG redirect. Only the differing lines:
```java
@Controller
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;   // field injection, not constructor injection

	@PostMapping("/employees/add")
	public String addEmployee(@RequestParam("id") int id, @RequestParam("name") String name,
			@RequestParam("salary") double salary, Model model) { ... }

	// assignment (left as exercise):
	//  getEmployeeByName()
	//  updateEmployee()
	//  deleteEmployee()
}
```
- **Field injection** (`@Autowired` directly on the field), deliberately different DI style from `DepartmentController`'s constructor injection — a side-by-side illustration of the two styles from §2–§4.
- The commented-out **assignment stub** (`getEmployeeByName`, `updateEmployee`, `deleteEmployee`) is left as an exercise. `EmployeeRepository`'s `MongoRepository` base already provides the underlying methods (`deleteById`, `save`) with zero extra code — only controller/service plumbing would be needed.

### 38.4 `ProjectController.java`
Same "always re-add full list, conditionally add found/not-found" pattern as the other two controllers. Only the differing lines:
```java
import org.springframework.web.bind.annotation.RequestMapping;  // imported but never used — dead import

@Autowired
private ProjectService projectService;   // interface type, field injection

@PostMapping("/projects/add")
public String addProject(@RequestParam String name, @RequestParam String description, @RequestParam String status,
		Model model) {
	projectService.addProject(new Project(name, description, status));
	...
}
```
- Field injection of `ProjectService` **the interface**, not `ProjectServiceImpl` — the injection point that would break with `NoUniqueBeanDefinitionException` if `ProjectServiceImpl2` (§37.5) were ever activated without a `@Qualifier`.
- `@RequestParam String name` with no explicit `"name"` string relies on the `-parameters` compiler flag (§27) — without it this would need `@RequestParam("name") String name` explicitly, as `DepartmentController`/`EmployeeController` do. A subtle binding-style inconsistency across controllers in the same project.

---

## 39. View Layer — `departments.jsp` (representative example)

```jsp
1:	<%@ page contentType="text/html;charset=UTF-8"%>
2:	<%@ taglib uri="jakarta.tags.core" prefix="c"%>
3:	...
4:	<h1>Department Details</h1>
5:	<table border="1" cellpadding="8">
6:	    <c:forEach items="${departments}" var="dept">
7:	    <tr><td>${dept.id}</td><td>${dept.name}</td></tr>
8:	    </c:forEach>
9:	</table>
10:	...
11:	<form action="${pageContext.request.contextPath}/departments/find" method="get">
12:	    ID: <input type="number" name="id" required />
13:	    <input type="submit" value="Find" />
14:	</form>
15:	<c:if test="${not empty foundDepartment}"> ... </c:if>
16:	<c:if test="${not empty notFound}"> ... </c:if>
17:	...
18:	<form action="${pageContext.request.contextPath}/departments/add" method="post">
19:	    ID:   <input type="number" name="id"   required /> <br/><br/>
20:	    Name: <input type="text"   name="name" required /> <br/><br/>
21:	    <input type="submit" value="Add Department" />
22:	</form>
```
Brief walkthrough (not exhaustive, per task scope): the `jakarta.tags.core` taglib (line 2) provides `<c:forEach>` and `<c:if>` — modern Jakarta EE namespace, consistent with the `jakarta.servlet.jsp.jstl` dependencies in `pom.xml` (§27). `${departments}`, `${foundDepartment}`, `${notFound}` are EL expressions reading directly from the request-scoped model attributes `DepartmentController` populated (§38.2) — this is the concrete rendering side of the `Model.addAttribute(...)` calls. `${pageContext.request.contextPath}` prefixes form actions with the deployed app's context path, so links work correctly regardless of what path the WAR is deployed under (rather than hard-coding `/departments/add`). The two `<form>` elements correspond exactly to `DepartmentController`'s GET `/departments/find` (query-param search) and POST `/departments/add` (raw-field form submission, no `<form:form modelAttribute="...">` Spring tag binding — a plain HTML form, matching the controller's raw-`@RequestParam` style rather than the courseware's `@ModelAttribute`/Spring-form-tag pattern from §22). `employees.jsp` and `projects.jsp` (siblings in the same directory) follow the identical structural pattern for their respective entities.

---

## Summary — Concept-to-Code Cross-Reference

| Pattern | Entity | Config | Repository/DAO | Query Mechanism | Schema Source |
|---|---|---|---|---|---|
| Spring JDBC (`JdbcTemplate`) | `Department` | `H2Config` | `DepartmentRepository` (class, hand-written) | Raw SQL + lambda `RowMapper` | `departments.sql` via `DataSourceInitializer` |
| Spring ORM (Hibernate) | `Project` | `HibernateConfig` (+ shares `H2Config`'s `DataSource`) | `ProjectDao`/`ProjectDaoImpl` (interface + class) | HQL via `SessionFactory.getCurrentSession()` | Auto-generated via `hibernate.hbm2ddl.auto=update` |
| Spring Data MongoDB | `Employee` | `MongoConfig` | `EmployeeRepository` (interface only, no class) | None written — Spring Data proxy | Schemaless, created lazily on first write |

This table condenses exactly why the same three-layer architecture (Controller → Service → Repository) looks different depending on which entity you're looking at.
