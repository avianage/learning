# Spring REST — Complete Line-by-Line Guide

This guide is grounded in two sources, referenced throughout as "courseware" and "the real project": the courseware file `06_Spring_REST_API.md` (Employee Management System / EMS theme), and a real local project, **Spring Rest Client** (`com.acme.client`), which is Aakash's actual working code.

**Critical orientation before anything else:** the local `Spring Rest Client` project is a **REST CLIENT (consumer)**, not a REST API provider. It has no `@Entity`, no JPA repository, and no business logic of its own — its whole job is to call an *external* Employee REST API (conceptually the `employee-service` from the Microservices folder, running on `localhost:8090`) using `RestTemplate` and `WebClient`, and re-expose thin proxy endpoints on its own port (`8095`) so you can test both client styles side by side. Every `@RestController` / `@GetMapping` / `@PathVariable` example that *provides* a REST API comes from the courseware only — don't confuse the `EmployeeClientController` in this project (which forwards to a remote server) with a from-scratch REST API controller. Section 2 covers the provider side conceptually from courseware; Section 10 onward walks the real client code.

---

## 1. REST Principles, HTTP Verbs, Status Codes

REST (Representational State Transfer) is an architectural style for HTTP APIs built on four constraints from the courseware:

| Constraint | Meaning |
|---|---|
| **Stateless** | Each request carries all needed info — no server-side session (contrast: a Flask session cookie) |
| **Client-Server** | UI and backend are separate, talk only via the API |
| **Uniform Interface** | Standard HTTP verbs, resource-based URLs |
| **Resource-based** | Everything is a resource identified by a URL |

HTTP verbs map to CRUD:

| HTTP Method | CRUD | Example URL | Notes |
|---|---|---|---|
| `GET` | Read | `/api/employees`, `/api/employees/42` | Safe, idempotent, no body |
| `POST` | Create | `/api/employees` | Not idempotent — repeat calls create duplicates |
| `PUT` | Update (full) | `/api/employees/42` | Replace the entire resource — send all fields |
| `PATCH` | Update (partial) | `/api/employees/42` | Send only changed fields |
| `DELETE` | Delete | `/api/employees/42` | Idempotent — deleting twice still ends in "gone" |

Status codes from the courseware table:

| Code | Meaning | When |
|---|---|---|
| `200 OK` | Success | GET/PUT with a body |
| `201 Created` | Resource created | POST success |
| `204 No Content` | Success, no body | DELETE success |
| `400 Bad Request` | Invalid input | Validation failure |
| `401 Unauthorized` | Not authenticated | Missing/invalid token |
| `403 Forbidden` | Not authorized | Valid token, no permission |
| `404 Not Found` | Resource missing | Wrong ID |
| `409 Conflict` | Duplicate resource | e.g. email already exists |
| `500 Internal Server Error` | Server crash | Unhandled exception |

---

## 2. `@RestController`, `@RequestMapping` and HTTP-Method Shortcuts (provider side — courseware)

From the courseware's full EMS REST API example (`EmployeeRestController`):

```java
@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin(origins = "*")
public class EmployeeRestController {
    ...
}
```

- **`@RestController`** = `@Controller` + `@ResponseBody` applied to every method. Per the Q&A doc: `@Controller` returns view names for template rendering, `@RestController` serializes return values directly to the HTTP response body (JSON via Jackson). Always use `@RestController` for JSON APIs.
- **`@RequestMapping("/api/v1/employees")`** at class level sets a base path all methods add onto — analogous to a Flask `Blueprint` prefix or an Express `router.use('/api/v1/employees', ...)`.
- **`@CrossOrigin(origins = "*")`** enables CORS for all origins (courseware notes: restrict this in production) — needed because the API is "consumed by Angular/React frontends".
- **Method shortcuts** (from Q&A #24): `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` are all just `@RequestMapping(method = ...)` shorthand — prefer them over raw `@RequestMapping` for readability and to be explicit about the verb.

The courseware's full CRUD set (each endpoint uses `ResponseEntity<T>` — see Section 5):

- `GET` (no path) → `getAll()` — returns `ResponseEntity.ok(list)`.
- `GET /{id}` → `getById(@PathVariable Long id)`.
- `POST` (no path) → `create(@Valid @RequestBody EmployeeRequest request)` — builds a `Location` header via `ServletUriComponentsBuilder` and returns `ResponseEntity.created(location).body(...)`, i.e. `201 Created` with a `Location` header pointing at the new resource — REST best practice.
- `PUT /{id}` → `update(...)` — full replace.
- `PATCH /{id}/salary` → `updateSalary(...)` — partial update of just the salary field, taking a raw `Map<String, BigDecimal>` body instead of a full DTO (since only one field changes).
- `DELETE /{id}` → `delete(...)` — returns `ResponseEntity.noContent().build()` → `204 No Content`.
- `GET /department/{deptName}` → a custom finder-style GET using `@PathVariable String deptName`.

---

## 3. `@PathVariable`, `@RequestParam`, `@RequestBody`, `@ResponseBody`

From the Q&A doc (#25, #26):

- **`@PathVariable`** extracts a segment from the URL path: `/employees/{id}` → `@PathVariable Long id`. Required by default — a missing path variable is a routing mismatch, not something Spring "defaults." You can mark it `@PathVariable(required = false)` only in more complex route patterns (Q&A #65).
- **`@RequestParam`** extracts a query-string parameter: `/employees?dept=Engineering` → `@RequestParam String dept`. Commonly given `defaultValue` and made optional with `required = false`. Rule of thumb from the Q&A doc: path variables identify *which* resource, query params filter/adjust *how* you fetch it.
- **`@RequestBody`** deserializes the JSON request body into a Java object via Jackson's `HttpMessageConverter` — equivalent to `request.json` in Flask or `req.body` in Express after a JSON body-parser.
- **`@ResponseBody`** serializes a method's return value into the JSON response body. `@RestController` applies it to every method automatically, so you never see it written explicitly in a `@RestController`-annotated class — only needed on individual methods inside a plain `@Controller`.

Example combining all four (courseware, paging endpoint):

```java
@GetMapping
public ResponseEntity<Page<EmployeeResponse>> getAll(
        @RequestParam(defaultValue = "0")         int page,
        @RequestParam(defaultValue = "10")        int size,
        @RequestParam(defaultValue = "lastName")  String sortBy,
        @RequestParam(defaultValue = "asc")       String direction) { ... }
```

Four `@RequestParam`s with defaults, all optional from the caller's perspective.

---

## 4. `ResponseEntity`

Per Q&A #27: `ResponseEntity<T>` gives full control over status code, headers, and body — use it whenever you need something other than the framework's default `200 OK`.

Patterns seen in the courseware:
- `ResponseEntity.ok(body)` → `200` with body.
- `ResponseEntity.created(location).body(body)` → `201` with `Location` header, used on POST.
- `ResponseEntity.noContent().build()` → `204`, no body, used on DELETE.
- `ResponseEntity.ok().header("Content-Disposition", "...").body(csv)` → custom header + `200`, used for a CSV export endpoint.
- `ResponseEntity.status(HttpStatus.CREATED).body(...)` — equivalent to `.created(...)` but without setting `Location` (this exact pattern is what the *local client's own controller* uses on its POST endpoints — see Section 13); this whole `ResponseEntity` mechanism is Spring's equivalent of Python's `flask.make_response(body, status, headers)` or Express's `res.status(201).json(body)`.

---

## 5. DTOs vs Entities — Why Not Expose Entities Directly

Per Q&A #47: a **DTO (Data Transfer Object)** is a plain object used to move data across the API boundary, decoupling the internal domain/entity model from the public API contract.

Reasons the courseware and Q&A give:
1. **Hide sensitive fields** — an `Employee` entity might carry a password hash or internal audit columns; the DTO simply omits them.
2. **Shape the response independently of the DB schema** — e.g. `EmployeeResponse` flattens `department.getName()` into a plain `departmentName` string instead of exposing the whole `Department` entity graph.
3. **Avoid serializing JPA proxies** — lazy-loaded Hibernate proxy objects fail or explode into huge nested JSON if serialized directly; mapping through a DTO sidesteps this entirely.
4. **Independent validation** — `@Valid` + Bean Validation annotations (`@NotBlank`, `@Email`, `@Positive`) belong on the *request* DTO, not the entity, since the entity may have different invariants once persisted.

Courseware's request/response/mapper triad:

```java
@Data
public class EmployeeRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email    private String email;
    @NotNull  @Positive private BigDecimal salary;
    @NotNull  private Long departmentId;
}

@Data
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal salary;
    private String departmentName;
    private String status;
    private LocalDate joinDate;
}

public class EmployeeMapper {
    public static EmployeeResponse toResponse(Employee e) { ... }
}
```

Cross-language comparison: this is precisely the role Pydantic models play in FastAPI (`EmployeeRequest`/`EmployeeCreate` for input validation, `EmployeeResponse`/`EmployeeOut` for output shaping) — Spring just uses Lombok `@Data` + JSR-380 annotations instead of Pydantic's type system.

Related: **`@Valid`** on a controller parameter (`@Valid @RequestBody EmployeeRequest request`) triggers Bean Validation; Spring auto-returns `400 Bad Request` with a validation-error body if it fails (Q&A #46). `@Validated` on the class enables method-level (not just body) validation.

---

## 6. RestTemplate vs WebClient — Conceptual Overview (courseware)

The courseware introduces both REST *client* styles conceptually before the real project's code is walked below.

**RestTemplate (traditional, synchronous/blocking):**

```java
@Service
public class HrIntegrationService {
    private final RestTemplate restTemplate;

    public HrIntegrationService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }

    public EmployeeResponse getEmployeeFromHrSystem(Long id) {
        String url = "https://hr-system.company.com/api/employees/{id}";
        return restTemplate.getForObject(url, EmployeeResponse.class, id);
    }
    ...
}
```

- Built with `RestTemplateBuilder` (courseware) to set connect/read timeouts.
- `getForObject(url, Type.class, uriVars...)` — simplest call, returns the deserialized body only.
- `exchange(url, HttpMethod, HttpEntity, ParameterizedTypeReference)` — full control; needed for generic types like `List<EmployeeResponse>` because Java erases generics at runtime (`ParameterizedTypeReference` works around that).
- `postForEntity(url, entity, Type.class)` — POST with headers/body wrapped in `HttpEntity`, returns `ResponseEntity` (status + headers + body).
- Errors are caught with typed exceptions: `HttpClientErrorException.NotFound`, `HttpServerErrorException`.
- The thread calling `restTemplate.xxx()` **blocks** until the HTTP response arrives (see the RestTemplate-vs-WebClient comparison below for the full contrast).

**WebClient (modern, reactive/non-blocking):**

```java
@Service
public class PayrollClient {
    private final WebClient webClient;

    public PayrollClient(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("https://payroll.company.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public PayrollRecord getPayroll(Long empId) {
        return webClient.get()
            .uri("/api/payroll/{id}", empId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                resp -> Mono.error(new ResourceNotFoundException("Not found")))
            .bodyToMono(PayrollRecord.class)
            .block();
    }

    public Mono<PayrollRecord> getPayrollAsync(Long empId) {
        return webClient.get()
            .uri("/api/payroll/{id}", empId)
            .retrieve()
            .bodyToMono(PayrollRecord.class);
    }
}
```

- `WebClient.Builder` sets a `baseUrl` and default headers once — every call reuses it.
- `.get()/.post()/.put()/.delete()` start a request spec; `.uri(...)` fills in the path (and can template path variables or build query params via a `UriBuilder` lambda).
- `.retrieve()` is the simple path: it triggers the call and auto-raises an error for 4xx/5xx unless you customize with `.onStatus(...)`.
- `.bodyToMono(Type.class)` deserializes a single object into a `Mono<T>` (a reactive "future" of 0-or-1 item); `.bodyToFlux(Type.class)` deserializes a stream/list into `Flux<T>` (0-to-N items).
- `.block()` converts the reactive pipeline into a blocking call, giving back the plain `T` — this is how you use WebClient synchronously in an otherwise non-reactive app (like the local client project). Without `.block()`, `getPayrollAsync` returns the `Mono<PayrollRecord>` itself, and the calling thread is free to do other work while the HTTP call is in flight — the actual reactive/non-blocking mode.
- WebClient is reactive the way `httpx.AsyncClient` + `async/await` is in Python, or `axios` promises awaited concurrently in Node — one underlying event-loop-style I/O thread handles many in-flight requests instead of one thread per request; RestTemplate is the `requests`-library model of one thread blocked per call. Spring's own docs mark `RestTemplate` as **in maintenance mode** (not formally deprecated, but no new features are being added) with `WebClient` as the recommended replacement even for blocking use cases — "spirit-deprecated" is the right way to think about it.

---

## 7. Exception Handling for REST

From the Q&A doc (#28): `@ExceptionHandler` inside a controller handles exceptions thrown by that controller's own methods. `@ControllerAdvice` (or `@RestControllerAdvice`) makes handlers **global**, applying across every controller in the app — combine the two so one class centrally maps exception types to HTTP responses (e.g. `ResourceNotFoundException` → `404`, `MethodArgumentNotValidException` → `400` with field errors). This is the Spring analogue of Flask's `@app.errorhandler` or Express's centralized error-handling middleware. The courseware itself doesn't show a full `@ControllerAdvice` class, but references `ResourceNotFoundException` being thrown from the RestTemplate error-handling example (Section 6 above) — the exception thrown there would, in a full API, be caught by a global handler and translated to a proper `404 ResponseEntity`.

*(The courseware file does not contain a `ProblemDetail`/RFC-7807 example, so this guide does not invent one. `ProblemDetail` is Spring 6's standard structured-error-body type, but it isn't demonstrated in Aakash's materials.)*

---

## 8. Micro Services & REST Concepts

- A **microservice** is a small, independently deployable service scoped to one business domain — contrasted in the courseware with a monolith EMS (`EmployeeController`, `DepartmentController`, `ProjectController`, `PayrollController` all in one WAR) split into `Employee Service :8081`, `Department Svc :8082`, `Payroll Service :8083`.
- **Communication styles:** synchronous = REST/HTTP or gRPC; asynchronous = message queues (RabbitMQ, Kafka — covered separately in Spring JMS material).
- **Service registration (Eureka):** each service registers itself so others can discover it by name:
```properties
spring.application.name=employee-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```
This is directly relevant context for the real client project: the local `Spring Rest Client` talks to `http://localhost:8090/api/v1/employees`, i.e. it is designed to consume exactly this kind of standalone `employee-service`.

---

## 9. Spring Data REST, Paging/Sorting, JSON Customization, Swagger (courseware, for completeness)

The courseware also covers these related topics — summarized since they are not present in the local client project code:

- **Spring Data REST** (`spring-boot-starter-data-rest`) auto-exposes a `JpaRepository` as a full REST API with zero controller code — `GET/POST/PUT/PATCH/DELETE /employees` map straight onto `findAll/save/save/partial-update/deleteById`. `@RepositoryRestResource(collectionResourceRel, path)` customizes the JSON key and URL path. Responses come back in **HATEOAS** format with a `_links` block (`self`, `employee`, `department`) — the REST maturity level (Q&A #66) where responses embed links to related actions/resources, via Spring HATEOAS's `EntityModel`/`CollectionModel`/`WebMvcLinkBuilder`.
- **Custom finder methods**: `@RestResource(path = "byEmail", rel = "byEmail")` on a repository method changes the auto-exposed search URL (`/employees/search/byEmail?email=...`); `@RestResource(exported = false)` hides a method from REST exposure entirely.
- **Custom controller methods on top of Spring Data REST**: `@RepositoryRestController` integrates a hand-written controller into the same Spring Data REST context for endpoints the auto-exposure can't express (bulk salary raise, department transfer, CSV export).
- **Paging and sorting** at the REST layer: `Pageable pageable = PageRequest.of(page, size, sort)`, `Page<Employee> empPage = employeeRepository.findAll(pageable)`, then `empPage.map(EmployeeMapper::toResponse)` to get `Page<EmployeeResponse>` — queried via `?page=0&size=5&sortBy=salary&direction=desc`.
- **JSON serialization control** via Jackson annotations on the DTO: `@JsonProperty("emailAddress")` renames a field in output, `@JsonFormat(pattern = "dd-MM-yyyy")` formats a date, `@JsonIgnore` excludes a field (e.g. `passwordHash`), `@JsonInclude(NON_NULL)` omits nulls (or forces inclusion), `@JsonSerialize(using = ...)` plugs in a custom serializer. Global tuning via a `Jackson2ObjectMapperBuilderCustomizer` bean. `@JsonView` lets one entity produce different JSON shapes per caller role (`Public`/`Manager`/`Admin` view classes applied per-field, selected per-endpoint with `@JsonView(EmployeeViews.Manager.class)` on the handler method).
- **Testing with Postman**: collections + collection variables (`baseUrl`), and JS test scripts using `pm.test(...)`, `pm.response.to.have.status(...)`, `pm.response.json()`, `pm.collectionVariables.set(...)` to chain requests (e.g. capture a created employee's ID for the next request) and assert response time.
- **Swagger/OpenAPI**: `springdoc-openapi-starter-webmvc-ui` dependency + `springdoc.*` properties expose `/swagger-ui.html`. Controller/DTO are annotated with `@Tag`, `@Operation`, `@ApiResponses`/`@ApiResponse`, `@Parameter`, and `@Schema(description=..., example=..., minimum=...)` to generate interactive docs directly from the code.

---

# Part II — Walking the Real Client Project (`Spring Rest Client`)

Reminder: everything below is **client/consumer** code — it has no `@Entity`, no database, and no REST-provider controller of its own logic; `EmployeeClientController` just forwards to the two services, which in turn call a remote `employee-service`. Order: DTOs first (they define the wire contract), then `AppConfig` (defines the two client beans), then both services contrasted line-by-line, then the controller and runner that use them, then the test.

## 10. `pom.xml`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>

<dependencies>
    <!-- Spring Web (includes RestTemplate + WebMVC) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Spring WebFlux (provides WebClient) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <!-- Lombok, optional so it doesn't leak to consumers -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

- Inherits `spring-boot-starter-parent` 3.3.0, which pins compatible dependency versions across the project (Maven's BOM mechanism) — comparable to a Python `requirements.txt` pinned via a shared lockfile.
- `spring-boot-starter-web` pulls in Spring MVC, embedded Tomcat, Jackson, **and `RestTemplate`** (it lives in `spring-web`, part of this starter); `spring-boot-starter-webflux` pulls in Project Reactor and **`WebClient`**. Both are present simultaneously purely to demo/compare both clients side by side — a real production client would typically only need `webflux` unless also serving traditional MVC endpoints, which this one is (`EmployeeClientController` uses `@RestController`, standard Spring MVC).
- Lombok is `optional=true` so it doesn't leak as a transitive dependency, and the `spring-boot-maven-plugin` excludes it from the packaged jar (it's a compile-time-only annotation processor, not a runtime dependency). `spring-boot-starter-test` (scope `test`) brings JUnit 5, AssertJ, Mockito, and Spring's test utilities (used in Section 19).

---

## 11. `dto/employee/EmployeeRequest.java`

```java
package com.acme.client.dto.employee;

/**
 * Request DTO sent to the Employee REST API for create / update operations.
 * Field names must match what the server-side @Valid EmployeeRequest expects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private Double salary;
}
```

- `@Data` (Lombok) generates getters, setters, `equals()`, `hashCode()`, and `toString()` — cuts out boilerplate you'd never write in Python (a `dataclass`-equivalent auto-generation). `@Builder` generates a fluent builder (`EmployeeRequest.builder().firstName(...).build()`), used extensively in `EmployeeClientRunner` (Section 17) — the Java analogue of constructing a Pydantic model with keyword arguments. `@NoArgsConstructor` + `@AllArgsConstructor` are required *alongside* `@Builder` because Jackson needs a no-args constructor to deserialize JSON by reflection, while the all-args constructor backs the builder.
- Plain fields, no `@Valid`/`@NotBlank`/`@Email` annotations here (unlike the courseware's server-side `EmployeeRequest`) — deliberate: this DTO is **outbound only**, sent *to* the remote API which does its own server-side validation. Field names must match exactly what the remote `employee-service` expects as JSON keys — the wire contract.

---

## 12. `dto/employee/EmployeeResponse.java`

```java
package com.acme.client.dto.employee;

/**
 * Response DTO received from the Employee REST API.
 * Field names must match the JSON keys returned by the server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private Double salary;
}
```

- Same Lombok stack as `EmployeeRequest`; `@NoArgsConstructor` is what lets Jackson build this object automatically when deserializing the remote server's JSON (used by both `RestTemplate`'s message converters and `WebClient`'s `bodyToMono`/`bodyToFlux`).
- `id` is a `String` here (not `Long`, unlike the courseware's provider-side entity ID) — this client treats the ID as an opaque identifier it never does arithmetic on, only passes back in URLs, matching whatever type the remote `employee-service` actually returns (likely a Mongo/UUID-style ID rather than an auto-increment JPA `Long`).
- `department` is a flat `String` (not a nested object) and `salary` a `Double` — exactly the DTO-flattening pattern from Section 5: the client doesn't care about the server's internal `Department` entity shape, only the flat JSON contract it receives.

---

## 13. `config/AppConfig.java`

```java
1:  package com.acme.client.config;
2:  
3:  import org.springframework.beans.factory.annotation.Value;
4:  import org.springframework.context.annotation.Bean;
5:  import org.springframework.context.annotation.Configuration;
6:  import org.springframework.web.client.RestTemplate;
7:  import org.springframework.web.reactive.function.client.WebClient;
8:  
9:  @Configuration
10: public class AppConfig {
11: 
12: 	@Value("${employee.api.base-url}")
13: 	private String baseUrl;
14: 
15: 	/**
16: 	 * RestTemplate — classic, synchronous/blocking HTTP client.
17: 	 * Still widely used in enterprise Spring Boot apps.
18: 	 */
19: 	@Bean
20: 	public RestTemplate restTemplate() {
21: 		return new RestTemplate();
22: 	}
23: 
24: 	/**
25: 	 * WebClient — modern, non-blocking/reactive HTTP client introduced in Spring 5.
26: 	 * Preferred for new development; supports both sync and async usage.
27: 	 */
28: 	@Bean
29: 	public WebClient webClient() {
30: 		return WebClient.builder()
31: 				.baseUrl(baseUrl)
32: 				.build();
33: 	}
34: }
```

- `@Configuration` marks this class as a source of bean definitions (Java-based config, no XML — Q&A #13); `@Value("${employee.api.base-url}")` injects the property from `application.properties` (Section 18) directly into a field (Q&A #50), equivalent to reading an environment variable / `.env` value into a config object in a Python/Node app.
- `@Bean public RestTemplate restTemplate()` registers a plain `new RestTemplate()` as a singleton bean — the simplest possible `RestTemplate` bean, no timeouts configured, contrasting with the courseware's `HrIntegrationService`, which used `RestTemplateBuilder` to set `setConnectTimeout`/`setReadTimeout`; production code should set timeouts, this demo doesn't.
- `@Bean public WebClient webClient()` builds **one shared `WebClient`** via `WebClient.builder().baseUrl(baseUrl).build()`. Baking the `baseUrl` in here means every call site in `EmployeeWebClientService` only needs to supply the *relative* path (`"/"`, `"/{id}"`, etc.) — contrast with `EmployeeRestTemplateService`, which has to concatenate the full `baseUrl` itself on every call (Section 14). Both beans are created **once** at startup and injected wherever `RestTemplate`/`WebClient` is a constructor parameter (constructor injection, per Q&A #7).

---

## 14. `service/EmployeeRestTemplateService.java` — RestTemplate

```java
@Service
public class EmployeeRestTemplateService {

    @Value("${employee.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public EmployeeRestTemplateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<EmployeeResponse> getAllEmployees() {
        ResponseEntity<List<EmployeeResponse>> response = restTemplate.exchange(
                baseUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<EmployeeResponse>>() {});
        return response.getBody();
    }

    public EmployeeResponse getEmployeeById(String id) {
        String url = baseUrl + "/{id}";
        return restTemplate.getForObject(url, EmployeeResponse.class, id);
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/email")
                .queryParam("email", email).toUriString();
        return restTemplate.getForObject(url, EmployeeResponse.class);
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        return restTemplate.postForObject(baseUrl, request, EmployeeResponse.class);
    }

    public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
        String url = baseUrl + "/{id}";
        HttpEntity<EmployeeRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                url, HttpMethod.PUT, httpEntity, EmployeeResponse.class, id);
        return response.getBody();
    }

    public void deleteEmployee(String id) {
        restTemplate.delete(baseUrl + "/{id}", id);
    }
}
```

- Each Spring-managed bean that needs `employee.api.base-url` re-reads it independently via its own `@Value` injection (`AppConfig` has one copy, this service has another). The `RestTemplate` bean itself is constructor-injected from `AppConfig` (Section 13) — Spring matches by type since there's only one such bean in the context.
- **`exchange(url, HttpMethod, HttpEntity, responseType)`** is the most general-purpose call: you specify the verb explicitly, pass `null` for the request body/headers on a bodiless GET, and supply a `ParameterizedTypeReference<List<T>>() {}` — an anonymous-subclass trick required because Java erases generic type parameters at runtime, so `List<EmployeeResponse>.class` isn't expressible; `ParameterizedTypeReference` captures the full generic type via reflection. `exchange()` always returns a `ResponseEntity<T>`, giving access to status/headers, which is why it's used for PUT too (`RestTemplate.put()` exists but returns `void`).
- **`getForObject(url, Type.class, uriVars...)`** is the simplest GET: URL template + target type + varargs to fill `{placeholders}` positionally, returning the deserialized body directly with no access to status/headers (like `requests.get(url).json()` vs. checking `.status_code` manually). A sibling `getForEntity(...)` exists for when you *do* need the status/headers back, wrapping the same call in a `ResponseEntity`.
- **`UriComponentsBuilder.fromHttpUrl(...).queryParam(...).toUriString()`** builds a properly encoded query string — Spring's equivalent of `requests.get(url, params={...})`, needed because `RestTemplate` has no `params` map convenience. The same pattern (and the `exchange()` + `ParameterizedTypeReference` pattern) repeats for the other search variants in the real file — one taking the term as a `@RequestParam`-style query string, one as a path segment — mirroring Section 3's `@RequestParam` vs `@PathVariable` distinction.
- **`postForObject(url, body, Type.class)`** implicitly wraps `request` in an `HttpEntity`, sends it, and returns just the deserialized body; a `postForEntity(...)` sibling returns the full `ResponseEntity` so the `201 CREATED` status is inspectable (consistent with the courseware provider's `ResponseEntity.created(...)`). No custom headers are set here — the courseware's `HrIntegrationService.syncToPayroll` additionally set `HttpHeaders` (`setContentType`/`setBearerAuth`); this simpler local version doesn't need auth against a local dev server.
- **`restTemplate.delete(url, id)`** is the simplest delete call, returning nothing; the server is expected to answer `204 No Content`.
- **Blocking behavior**: every method here calls straight through to `restTemplate.xxx()` and returns a plain Java value — there is no `Future`/`Mono`/`CompletableFuture` anywhere in this class, and the calling thread is fully blocked for the duration of each HTTP round trip. Contrast directly with Section 6's WebClient discussion and Section 15 below.

---

## 15. `service/EmployeeWebClientService.java` — WebClient

```java
@Service
public class EmployeeWebClientService {

    private final WebClient webClient;

    public EmployeeWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<EmployeeResponse> getAllEmployees() {
        return webClient.get()
                .uri("/")
                .retrieve()
                .bodyToFlux(EmployeeResponse.class)
                .collectList()
                .block();
    }

    public EmployeeResponse getEmployeeById(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .block();
    }

    public EmployeeResponse getEmployeeByEmail(String email) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/email").queryParam("email", email).build())
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .block();
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        return webClient.post()
                .uri("/")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmployeeResponse.class)
                .block();
    }

    public void deleteEmployee(String id) {
        webClient.delete()
                .uri("/{id}", id)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
```

- Constructor injection of the single shared `WebClient` bean from `AppConfig`, which already has `baseUrl` baked in (Section 13) — that's why every `.uri(...)` call is a *relative* path (`"/"`, `"/{id}"`), unlike `EmployeeRestTemplateService`, which concatenated the full absolute URL itself.
- **`webClient.get()`** opens a fluent request builder (like starting an `axios({method:'get',...})` chain). **`.uri(...)`** resolves against the base URL, or — via the lambda form, `.uri(uriBuilder -> uriBuilder.path(...).queryParam(...).build())` — gives programmatic query-string construction, WebClient's equivalent of `RestTemplate`'s `UriComponentsBuilder` usage (Section 14). **`.retrieve()`** executes the exchange and, by default, treats any 4xx/5xx as an error, raising `WebClientResponseException` automatically — the reactive equivalent of `response.raise_for_status()`. **`.bodyToMono(Type.class)`** deserializes a single object (0-or-1 item); **`.bodyToFlux(Type.class)`** deserializes a JSON array into a reactive stream of 0-to-N items, then **`.collectList()`** gathers it into a `Mono<List<T>>` when the method needs to return a plain `List`.
- **`.block()`** subscribes and blocks the calling thread until completion, unwrapping to the plain value — this is the point where the method **opts back into synchronous/blocking behavior** despite using WebClient, legitimate when the calling code isn't itself reactive. The genuinely non-blocking alternative is to return the `Mono`/`Flux` directly and let the caller subscribe, so the request-handling thread is never blocked — the actual payoff of WebClient over RestTemplate, not realized in this demo's blocking usage throughout.
- **`bodyValue(request)`** (POST/PUT) serializes the DTO to JSON as the request body — same Jackson message-converter machinery as `@RequestBody` server-side. `updateEmployee` follows the identical shape with `.put(...)`.
- **`toBodilessEntity()`** (DELETE) is the WebClient counterpart for endpoints that return `204 No Content` with no body (contrast `bodyToMono`, which expects a JSON payload); returns `Mono<ResponseEntity<Void>>`, and `.block()` just waits for completion and discards the result.

**RestTemplate vs WebClient, side by side (from this project's own two services):**

| Aspect | `EmployeeRestTemplateService` | `EmployeeWebClientService` |
|---|---|---|
| Base URL | Concatenated manually per call (`baseUrl + "/{id}"`) | Baked into the shared `WebClient` bean, calls use relative paths |
| Single object GET | `getForObject(url, Type.class, id)` | `.get().uri(...).retrieve().bodyToMono(Type.class).block()` |
| List GET | `exchange(...) ` + `ParameterizedTypeReference<List<T>>` | `.retrieve().bodyToFlux(Type.class).collectList().block()` |
| POST | `postForObject`/`postForEntity` | `.post().bodyValue(request).retrieve().bodyToMono(...).block()` |
| PUT | `exchange(url, HttpMethod.PUT, entity, Type.class, id)` | `.put().uri(...).bodyValue(request).retrieve().bodyToMono(...).block()` |
| DELETE | `restTemplate.delete(url, id)` (void) | `.delete().uri(...).retrieve().toBodilessEntity().block()` |
| Threading model | Always blocking — no non-blocking option | Reactive by design; `.block()` opts back into blocking (used throughout this demo for simplicity); returning `Mono`/`Flux` directly is the genuinely non-blocking form |
| Spring status | "Classic," in maintenance mode | Modern, recommended for new code (Spring 5+) |

---

## 16. `controller/EmployeeClientController.java`

```java
@RestController
@RequestMapping("/client")
public class EmployeeClientController {

    private final EmployeeRestTemplateService rtService;
    private final EmployeeWebClientService    wcService;

    public EmployeeClientController(EmployeeRestTemplateService rtService,
                                    EmployeeWebClientService wcService) {
        this.rtService = rtService;
        this.wcService = wcService;
    }

    @GetMapping("/rt/employees")
    public ResponseEntity<List<EmployeeResponse>> rtGetAll() {
        return ResponseEntity.ok(rtService.getAllEmployees());
    }

    @GetMapping("/rt/employees/{id}")
    public ResponseEntity<EmployeeResponse> rtGetById(@PathVariable String id) {
        return ResponseEntity.ok(rtService.getEmployeeById(id));
    }

    @GetMapping("/rt/employees/email")
    public ResponseEntity<EmployeeResponse> rtGetByEmail(@RequestParam String email) {
        return ResponseEntity.ok(rtService.getEmployeeByEmail(email));
    }

    @PostMapping("/rt/employees")
    public ResponseEntity<EmployeeResponse> rtCreate(@RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rtService.createEmployee(request));
    }

    @PutMapping("/rt/employees/{id}")
    public ResponseEntity<EmployeeResponse> rtUpdate(@PathVariable String id,
                                                      @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(rtService.updateEmployee(id, request));
    }

    @DeleteMapping("/rt/employees/{id}")
    public ResponseEntity<Void> rtDelete(@PathVariable String id) {
        rtService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // WebClient routes → /client/wc/** — identical structure and handler bodies,
    // one-for-one, just delegating to wcService instead of rtService.
}
```

- `@RestController` + `@RequestMapping("/client")`: this class *is* a real REST provider — but its "business logic" is entirely delegation to the two client services, so despite using the same annotations as the courseware's provider-side `EmployeeRestController`, its actual job is proxying calls out to another server, not owning any data. It's a REST API façade over a REST client. Both service beans are constructor-injected simultaneously; Spring resolves each by type since they're distinct classes (no `@Qualifier` needed).
- `rtGetAll` just wraps the service call in `ResponseEntity.ok(...)` → always `200`. `rtGetById` uses `@PathVariable String id` (Section 3). `rtGetByEmail` uses a required `@RequestParam String email` with no default, so a request missing `?email=...` returns `400` automatically (Spring's built-in `MissingServletRequestParameterException` handling). There's also a `search`/`search/{firstName}` pair mirroring both DTO-facing variants from Section 14/15's services (one `@RequestParam`, one `@PathVariable`).
- `rtCreate` deserializes via `@RequestBody EmployeeRequest request` and returns `ResponseEntity.status(HttpStatus.CREATED).body(...)` — matching Section 4's `201` pattern, though unlike the courseware provider example no `Location` header is set. `rtUpdate` combines `@PathVariable` (which resource) with `@RequestBody` (new representation) for a full `PUT` replace, `200 OK` on success. `rtDelete` calls the void service method then manually returns `ResponseEntity.noContent().build()` → `204`.
- The `/wc/**` routes are a structurally identical mirror of the `/rt/**` routes, just delegating to `wcService` — this 1:1 parallel structure is what makes it possible to hit `/client/rt/employees` and `/client/wc/employees` from Postman and compare RestTemplate vs WebClient behavior against the exact same remote server, live.

---

## 17. `EmployeeClientRunner.java`

```java
@Component
public class EmployeeClientRunner implements CommandLineRunner {

    private final EmployeeRestTemplateService rtService;
    private final EmployeeWebClientService    wcService;

    public EmployeeClientRunner(EmployeeRestTemplateService rtService,
                                EmployeeWebClientService wcService) {
        this.rtService = rtService;
        this.wcService = wcService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            List<EmployeeResponse> all = rtService.getAllEmployees();
            if (!all.isEmpty()) {
                String firstId = all.get(0).getId();
                rtService.getEmployeeById(firstId);
                rtService.getEmployeeByEmail(all.get(0).getEmail());
                rtService.getEmployeesByFirstName(all.get(0).getFirstName());
            }

            EmployeeRequest newEmployee = EmployeeRequest.builder()
                    .firstName("Sonu").lastName("Reddy").email("sonu.reddy@acme.com")
                    .department("Engineering").salary(75000.0).build();
            EmployeeResponse created = rtService.createEmployee(newEmployee);

            EmployeeRequest updated = EmployeeRequest.builder()
                    .firstName("Sonu").lastName("Reddy").email("sonu.reddy@acme.com")
                    .department("Architecture").salary(90000.0).build();
            rtService.updateEmployee(created.getId(), updated);
            rtService.deleteEmployee(created.getId());

        } catch (Exception e) {
            LOG.warn("[RestTemplate] Server may not be running: {}", e.getMessage());
        }

        // WebClient block — identical structure, calls wcService, uses "Monu"/"Rao" test data
    }
}
```

- `implements CommandLineRunner` — per Q&A #67, its `run()` executes automatically once the Spring application context is fully started; here it exercises the whole CRUD cycle against the remote server as a live smoke test on every app boot, not for genuine production data seeding. `@Component` registers it as a managed bean so `SpringApplication.run` discovers and invokes it after startup.
- Calls chain through the RestTemplate service's public methods (Section 14), pulling `getId()`/`getEmail()`/`getFirstName()` off the first returned employee to feed each subsequent call — a realistic sequential API-consumption flow. `EmployeeRequest.builder()...build()` is the Lombok `@Builder` from Section 11 in actual use, followed by the full CRUD lifecycle: create → update → delete.
- A broad `catch (Exception e)` wraps the whole RestTemplate block, logging a warning rather than failing app startup, so the app doesn't crash if the remote `employee-service` isn't up on `localhost:8090` — a pragmatic try/catch for a startup-time smoke test, not the `@ExceptionHandler`/`@ControllerAdvice` REST exception-mapping from Section 7 (which translates exceptions into HTTP responses for a controller, not a runner).
- The WebClient block (omitted here, present in the real file) is structurally identical: `wcService.getAllEmployees()` → `getEmployeeById` → `getEmployeeByEmail` → `getEmployeesByFirstName` → `createEmployee` (with "Monu Rao" test data) → `updateEmployee` → `deleteEmployee`, in its own `try/catch` — reinforcing the RestTemplate-vs-WebClient comparison the whole project is built around. Final log lines report the real server address (`localhost:8095`, matching `server.port=8095`, Section 18) and the two route prefixes exposed by the controller.

---

## 18. `application.properties`

```properties
1:  server.port=8095
2:  
3:  # Base URL of the Employee REST API server
4:  employee.api.base-url=http://localhost:8090/api/v1/employees
5:  
```

- **Line 1** — `server.port=8095`: this client app itself listens on `8095` (its own controller's proxy endpoints, Section 16).
- **Line 4** — `employee.api.base-url=http://localhost:8090/api/v1/employees`: the property both `AppConfig` (Section 13) and `EmployeeRestTemplateService` (Section 14) read via `@Value`. Port `8090` is *different* from this app's own `8095`, confirming this project is a separate process calling out to another Spring Boot app (the `employee-service`) — consistent with the courseware's microservice port-per-service pattern (Section 8) and matching the URL path shape (`/api/v1/employees`) from the courseware's provider-side `EmployeeRestController` (Section 2).

---

## 19. `EmployeeRestTemplateServiceTest.java`

```java
@RestClientTest(components = {EmployeeRestTemplateService.class})
class EmployeeRestTemplateServiceTest {

    @Autowired private MockRestServiceServer mockServer;
    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;

    private EmployeeRestTemplateService service;
    private static final String BASE_URL = "http://localhost:8080/api/v1/employees";

    @BeforeEach
    void setUp() {
        service = new EmployeeRestTemplateService(restTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", BASE_URL);
    }

    @Test
    void getAllEmployees_shouldReturnList() throws Exception {
        List<EmployeeResponse> employees = List.of(
                EmployeeResponse.builder().id("1").firstName("Alice").email("alice@acme.com").build(),
                EmployeeResponse.builder().id("2").firstName("Bob").email("bob@acme.com").build());

        mockServer.expect(requestTo(BASE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(employees), MediaType.APPLICATION_JSON));

        List<EmployeeResponse> result = service.getAllEmployees();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void deleteEmployee_shouldComplete() {
        mockServer.expect(requestTo(BASE_URL + "/10"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        service.deleteEmployee("10");
    }
}
```

- **`@RestClientTest(components = {...})`** is a Spring Boot test slice that boots only the beans needed to test a `RestTemplate`-based client: it auto-configures a `RestTemplate`, a Jackson `ObjectMapper`, and a `MockRestServiceServer`, without starting the full application context (no embedded server, no unrelated beans) — much faster than `@SpringBootTest`. Conceptually similar to `unittest.mock.patch('requests.get', ...)` in Python, but Spring intercepts at the HTTP-client level rather than function-patching.
- **`MockRestServiceServer`** intercepts every outgoing call made through the injected `RestTemplate` and matches it against `.expect(...)` rules instead of hitting the network — these tests run with **no live server**, unlike `EmployeeClientRunner`, which genuinely calls `localhost:8090`. The service under test is constructed manually (`new EmployeeRestTemplateService(restTemplate)`) rather than autowired, because `baseUrl` needs a test-specific value.
- **`ReflectionTestUtils.setField(service, "baseUrl", BASE_URL)`** bypasses the normal `@Value` injection mechanism (which only runs inside a full Spring context) and sets the private field directly via reflection, simulating what `@Value("${employee.api.base-url}")` would have done in production — necessary because this test slice doesn't load `application.properties`.
- **`mockServer.expect(requestTo(url)).andExpect(method(...)).andRespond(withSuccess(json, contentType))`** declares: "when a request matching this URL/verb arrives, respond with this canned JSON and `200 OK`." The RestTemplate-testing equivalent of `responses.add(responses.GET, url, json=..., status=200)` in Python's `responses` library, or `nock` in Node. The real test file has four such tests covering GET-list, GET-by-id, POST-create, and DELETE — each following this same expect/respond/assert shape against a different verb, with `withNoContent()` used to stub the DELETE's bodiless `204` response.
- **Notably absent**: there is no equivalent test file for `EmployeeWebClientService`. Testing reactive `WebClient` calls typically uses `WebClient` bound to a mocked `ExchangeFunction`, or Spring's `MockWebServer`/`WireMock` — a different mechanism from `MockRestServiceServer`. The RestTemplate service has unit test coverage; the WebClient service currently does not.

---

## 20. Quick-Reference Summary Table (courseware + real project combined)

| Feature | Annotation / Class | Purpose | Where seen |
|---|---|---|---|
| REST Controller | `@RestController` | JSON API controller | Courseware `EmployeeRestController`; real `EmployeeClientController` |
| Request body | `@RequestBody` | Parse JSON into a DTO | Both |
| Response control | `ResponseEntity<T>` | Status + headers + body | Both |
| Path variable | `@PathVariable` | URL segment extraction | Both |
| Query param | `@RequestParam` | Query string extraction | Both |
| Partial update | `@PatchMapping` | Partial resource update | Courseware only (not in real client) |
| Classic REST client | `RestTemplate` | Synchronous/blocking HTTP client | Courseware `HrIntegrationService`; real `EmployeeRestTemplateService` |
| Modern REST client | `WebClient` | Reactive/non-blocking HTTP client | Courseware `PayrollClient`; real `EmployeeWebClientService` |
| Spring Data REST | `@RepositoryRestResource` | Auto-expose repository as REST | Courseware only |
| JSON control | `@JsonProperty`, `@JsonIgnore` | Serialization customization | Courseware only |
| API docs | `@Operation`, `@ApiResponse` | Swagger/OpenAPI annotations | Courseware only |
| Startup smoke test | `CommandLineRunner` | Runs after context startup | Real `EmployeeClientRunner` |
| Client testing | `@RestClientTest` + `MockRestServiceServer` | Test RestTemplate calls without network | Real `EmployeeRestTemplateServiceTest` |
