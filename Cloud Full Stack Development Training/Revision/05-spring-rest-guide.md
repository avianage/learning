# Spring REST — Complete Line-by-Line Guide

This guide is grounded in two sources: the courseware file `06_Spring_REST_API.md` (Employee Management System / EMS theme), and a real local project, **Spring Rest Client** (`com.acme.client`), which is Aakash's actual working code.

**Critical orientation before anything else:** the local `Spring Rest Client` project is a **REST CLIENT (consumer)**, not a REST API provider. It has no `@Entity`, no JPA repository, and no business logic of its own — its whole job is to call an *external* Employee REST API (conceptually the `employee-service` from the Microservices folder, running on `localhost:8090`) using `RestTemplate` and `WebClient`, and re-expose thin proxy endpoints on its own port (`8095`) so you can test both client styles side by side. Every `@RestController` / `@GetMapping` / `@PathVariable` example that *provides* a REST API (i.e. the classic "build a CRUD controller" material) comes from the courseware only — there is no local code demonstrating the provider side, so don't confuse the `EmployeeClientController` in this project (which forwards to a remote server) with a from-scratch REST API controller. Section 2 below covers the provider side conceptually from courseware; Section 10 onward walks the real client code.

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

Cross-language note: this is exactly the contract you already know from Flask/FastAPI/Express — the difference in Spring is that the framework enforces it through annotations and `ResponseEntity` rather than you manually setting `res.status(201)`.

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

Four `@RequestParam`s with defaults, all optional from the caller's perspective — directly comparable to `request.args.get('page', 0)` in Flask.

---

## 4. `ResponseEntity`

Per Q&A #27: `ResponseEntity<T>` gives full control over status code, headers, and body — use it whenever you need something other than the framework's default `200 OK`.

Patterns seen in the courseware:
- `ResponseEntity.ok(body)` → `200` with body.
- `ResponseEntity.created(location).body(body)` → `201` with `Location` header, used on POST.
- `ResponseEntity.noContent().build()` → `204`, no body, used on DELETE.
- `ResponseEntity.ok().header("Content-Disposition", "...").body(csv)` → custom header + `200`, used for a CSV export endpoint.
- `ResponseEntity.status(HttpStatus.CREATED).body(...)` — equivalent to `.created(...)` but without setting `Location` (this exact pattern is what the *local client's own controller* uses on its POST endpoints — see Section 13).

Cross-language comparison: this is Spring's equivalent of Python's `flask.make_response(body, status, headers)` or Express's `res.status(201).json(body)`.

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
- The thread calling `restTemplate.xxx()` **blocks** until the HTTP response arrives — same model as Python's `requests.get(...)`.

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
- Cross-language comparison: WebClient is reactive the way `httpx.AsyncClient` + `async/await` is in Python, or `axios` promises awaited concurrently in Node — one underlying event-loop-style I/O thread handles many in-flight requests instead of one thread per request. RestTemplate is the `requests`-library model: one thread, blocked, per call. Spring's own docs mark `RestTemplate` as **in maintenance mode** (not formally deprecated, but no new features are being added) with `WebClient` as the recommended replacement even for blocking use cases — "spirit-deprecated" is the right way to think about it.

---

## 7. Exception Handling for REST

From the Q&A doc (#28): `@ExceptionHandler` inside a controller handles exceptions thrown by that controller's own methods. `@ControllerAdvice` (or `@RestControllerAdvice`) makes handlers **global**, applying across every controller in the app — combine the two so one class centrally maps exception types to HTTP responses (e.g. `ResourceNotFoundException` → `404`, `MethodArgumentNotValidException` → `400` with field errors). This is the Spring analogue of Flask's `@app.errorhandler` or Express's centralized error-handling middleware. The courseware itself doesn't show a full `@ControllerAdvice` class, but references `ResourceNotFoundException` being thrown from the RestTemplate error-handling example (Section 6 above) — the exception thrown there would, in a full API, be caught by a global handler and translated to a proper `404 ResponseEntity`.

*(Note: the courseware file does not contain a `ProblemDetail`/RFC-7807 example, so this guide does not invent one — if asked about `ProblemDetail` on the assessment, know that it's Spring 6's standard structured-error-body type, but it isn't demonstrated in Aakash's materials.)*

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

- **Spring Data REST** (`spring-boot-starter-data-rest`) auto-exposes a `JpaRepository` as a full REST API with zero controller code — `GET/POST/PUT/PATCH/DELETE /employees` map straight onto `findAll/save/save/partial-update/deleteById`. `@RepositoryRestResource(collectionResourceRel, path)` customizes the JSON key and URL path. Responses come back in **HATEOAS** format with a `_links` block (`self`, `employee`, `department`) — Q&A #66 defines HATEOAS as the REST maturity level where responses embed links to related actions/resources, via Spring HATEOAS's `EntityModel`/`CollectionModel`/`WebMvcLinkBuilder`.
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
1:  <?xml version="1.0" encoding="UTF-8"?>
2:  <project xmlns="http://maven.apache.org/POM/4.0.0"
3:  	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
4:  	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
5:  	<modelVersion>4.0.0</modelVersion>
6:  
7:  	<parent>
8:  		<groupId>org.springframework.boot</groupId>
9:  		<artifactId>spring-boot-starter-parent</artifactId>
10: 		<version>3.3.0</version>
11: 		<relativePath />
12: 	</parent>
13: 
14: 	<groupId>com.acme</groupId>
15: 	<artifactId>acme-rest-client-demo</artifactId>
16: 	<version>0.0.1-SNAPSHOT</version>
17: 	<name>acme-rest-client-demo</name>
18: 	<description>Spring REST Client Demo - consuming Employee REST API</description>
19: 
20: 	<properties>
21: 		<java.version>17</java.version>
22: 	</properties>
23: 
24: 	<dependencies>
25: 
26: 		<!-- Spring Web (includes RestTemplate + WebMVC) -->
27: 		<dependency>
28: 			<groupId>org.springframework.boot</groupId>
29: 			<artifactId>spring-boot-starter-web</artifactId>
30: 		</dependency>
31: 
32: 		<!-- Spring WebFlux (provides WebClient) -->
33: 		<dependency>
34: 			<groupId>org.springframework.boot</groupId>
35: 			<artifactId>spring-boot-starter-webflux</artifactId>
36: 		</dependency>
37: 
38: 		<!-- Lombok for boilerplate reduction -->
39: 		<dependency>
40: 			<groupId>org.projectlombok</groupId>
41: 			<artifactId>lombok</artifactId>
42: 			<optional>true</optional>
43: 		</dependency>
44: 
45: 		<!-- Testing -->
46: 		<dependency>
47: 			<groupId>org.springframework.boot</groupId>
48: 			<artifactId>spring-boot-starter-test</artifactId>
49: 			<scope>test</scope>
50: 		</dependency>
51: 
52: 	</dependencies>
53: 
54: 	<build>
55: 		<plugins>
56: 			<plugin>
57: 				<groupId>org.springframework.boot</groupId>
58: 				<artifactId>spring-boot-maven-plugin</artifactId>
59: 				<configuration>
60: 					<excludes>
61: 						<exclude>
62: 							<groupId>org.projectlombok</groupId>
63: 							<artifactId>lombok</artifactId>
64: 						</exclude>
65: 					</excludes>
66: 				</configuration>
67: 			</plugin>
68: 		</plugins>
69: 	</build>
70: 
71: </project>
```

- **Line 7–12** — inherits `spring-boot-starter-parent` 3.3.0, which pins compatible dependency versions across the whole project (Maven's BOM mechanism) — comparable to a Python `requirements.txt` pinned via a shared lockfile/constraints file.
- **Line 29–30** — `spring-boot-starter-web` pulls in Spring MVC, embedded Tomcat, Jackson, **and `RestTemplate`'s classes** (`RestTemplate` itself lives in `spring-web`, part of this starter).
- **Line 34–36** — `spring-boot-starter-webflux` pulls in Project Reactor and **`WebClient`**. Note both starters are present simultaneously here purely to demonstrate/compare both clients side by side in one demo app — a real production client project would typically only need `webflux` (for `WebClient`) unless it's also serving traditional MVC endpoints, which this one is (`EmployeeClientController` uses `@RestController`, standard Spring MVC).
- **Line 40–43** — Lombok, `optional=true` so it doesn't leak as a transitive dependency to consumers of this jar.
- **Line 47–50** — `spring-boot-starter-test`, scoped to `test` only — brings JUnit 5, AssertJ, Mockito, and Spring's test utilities (used by the `RestTemplateServiceTest` in Section 18).
- **Line 60–65** — excludes Lombok from the final packaged jar (it's a compile-time-only annotation processor, not a runtime dependency).

---

## 11. `dto/employee/EmployeeRequest.java`

```java
1:  package com.acme.client.dto.employee;
2:  
3:  import lombok.AllArgsConstructor;
4:  import lombok.Builder;
5:  import lombok.Data;
6:  import lombok.NoArgsConstructor;
7:  
8:  /**
9:   * Request DTO sent to the Employee REST API for create / update operations.
10:  * Field names must match what the server-side @Valid EmployeeRequest expects.
11:  */
12: @Data
13: @Builder
14: @NoArgsConstructor
15: @AllArgsConstructor
16: public class EmployeeRequest {
17: 
18: 	private String firstName;
19: 	private String lastName;
20: 	private String email;
21: 	private String department;
22: 	private Double salary;
23: }
```

- **Line 12** — `@Data` (Lombok) generates getters, setters, `equals()`, `hashCode()`, and `toString()` for all fields — cuts out ~50 lines of Java boilerplate you'd never write in Python (a `dataclass`-equivalent auto-generation).
- **Line 13** — `@Builder` generates a fluent builder (`EmployeeRequest.builder().firstName(...).build()`), used extensively in `EmployeeClientRunner` (Section 14) — the Java analogue of constructing a Pydantic model with keyword arguments.
- **Line 14–15** — `@NoArgsConstructor` + `@AllArgsConstructor` are required *alongside* `@Builder`: Jackson needs a no-args constructor to deserialize incoming JSON by reflection, and the all-args constructor backs the builder.
- **Line 18–22** — plain fields, no `@Valid`/`@NotBlank`/`@Email` annotations here (unlike the courseware's server-side `EmployeeRequest`) — this is deliberate: **this DTO is outbound only**, sent *to* the remote API which does its own server-side validation; the client doesn't re-validate before sending. Field names (`firstName`, `department`, `salary`, etc.) must match exactly what the remote `employee-service` expects as JSON keys — this is the wire contract mentioned in the class comment (line 9–10).

---

## 12. `dto/employee/EmployeeResponse.java`

```java
1:  package com.acme.client.dto.employee;
2:  
3:  import lombok.AllArgsConstructor;
4:  import lombok.Builder;
5:  import lombok.Data;
6:  import lombok.NoArgsConstructor;
7:  
8:  /**
9:   * Response DTO received from the Employee REST API.
10:  * Field names must match the JSON keys returned by the server.
11:  */
12: @Data
13: @Builder
14: @NoArgsConstructor
15: @AllArgsConstructor
16: public class EmployeeResponse {
17: 
18: 	private String id;
19: 	private String firstName;
20: 	private String lastName;
21: 	private String email;
22: 	private String department;
23: 	private Double salary;
24: }
```

- **Line 12–15** — same Lombok stack as `EmployeeRequest`; `@NoArgsConstructor` is what lets Jackson build this object automatically when deserializing the remote server's JSON response body (used by both `RestTemplate`'s message converters and `WebClient`'s `bodyToMono`/`bodyToFlux`).
- **Line 18** — `id` is a `String` here (not `Long`, unlike the courseware's provider-side entity ID) — this client treats the ID as an opaque identifier it never needs to do arithmetic on, only pass back in URLs; matches whatever type the remote `employee-service` actually returns (likely a Mongo/UUID-style ID rather than an auto-increment JPA `Long`).
- **Line 22–23** — `department` is a flat `String` (not a nested object) and `salary` a `Double` — this is exactly the DTO-flattening pattern from Section 5: the client doesn't care about the server's internal `Department` entity shape, only the flat JSON contract it actually receives.

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

- **Line 9** — `@Configuration` marks this class as a source of bean definitions (Java-based config, no XML) — per Q&A #13.
- **Line 12–13** — `@Value("${employee.api.base-url}")` injects the property from `application.properties` (Section 15) directly into a field — Q&A #50; equivalent to reading an environment variable / `.env` value into a config object in a Python/Node app.
- **Line 19–22** — `@Bean public RestTemplate restTemplate()` registers a plain `new RestTemplate()` as a singleton bean in the container. This is the simplest possible `RestTemplate` bean — no timeouts configured (contrast with the courseware's `HrIntegrationService`, which used `RestTemplateBuilder` to set `setConnectTimeout`/`setReadTimeout`); a gap worth noting for the assessment: production code should set timeouts, this demo doesn't.
- **Line 28–33** — `@Bean public WebClient webClient()` builds **one shared `WebClient`** via `WebClient.builder().baseUrl(baseUrl).build()`. Baking the `baseUrl` in here means every call site in `EmployeeWebClientService` only needs to supply the *relative* path (`"/"`, `"/{id}"`, etc.) — contrast with `EmployeeRestTemplateService`, which has to concatenate the full `baseUrl` itself on every call (see Section 16) because `RestTemplate` has no equivalent built-in base-URL concept in this config. This is one of the concrete ergonomic differences between the two clients worth calling out on the assessment.
- Both beans are created **once** at startup and injected wherever `RestTemplate`/`WebClient` is a constructor parameter (constructor injection, per Q&A #7) — see `EmployeeRestTemplateService` and `EmployeeWebClientService` below.

---

## 14. `service/EmployeeRestTemplateService.java` — RestTemplate

```java
1:  package com.acme.client.service;
2:  
3:  import java.util.Arrays;
4:  import java.util.List;
5:  
6:  import org.slf4j.Logger;
7:  import org.slf4j.LoggerFactory;
8:  import org.springframework.beans.factory.annotation.Value;
9:  import org.springframework.core.ParameterizedTypeReference;
10: import org.springframework.http.HttpEntity;
11: import org.springframework.http.HttpMethod;
12: import org.springframework.http.ResponseEntity;
13: import org.springframework.stereotype.Service;
14: import org.springframework.web.client.RestTemplate;
15: import org.springframework.web.util.UriComponentsBuilder;
16: 
17: import com.acme.client.dto.employee.EmployeeRequest;
18: import com.acme.client.dto.employee.EmployeeResponse;
19: 
20: @Service
21: public class EmployeeRestTemplateService {
22: 
23: 	private static final Logger LOG = LoggerFactory.getLogger(EmployeeRestTemplateService.class);
24: 
25: 	@Value("${employee.api.base-url}")
26: 	private String baseUrl;
27: 
28: 	private final RestTemplate restTemplate;
29: 
30: 	public EmployeeRestTemplateService(RestTemplate restTemplate) {
31: 		this.restTemplate = restTemplate;
32: 	}
33: 
34: 	public List<EmployeeResponse> getAllEmployees() {
35: 		LOG.info("[RestTemplate] GET {}", baseUrl);
36: 
37: 		ResponseEntity<List<EmployeeResponse>> response = restTemplate.exchange(
38: 				baseUrl,
39: 				HttpMethod.GET,
40: 				null,
41: 				new ParameterizedTypeReference<List<EmployeeResponse>>() {});
42: 
43: 		LOG.info("[RestTemplate] Status: {}", response.getStatusCode());
44: 		return response.getBody();
45: 	}
46: 
47: 	public EmployeeResponse getEmployeeById(String id) {
48: 		String url = baseUrl + "/{id}";
49: 		LOG.info("[RestTemplate] GET {} (id={})", url, id);
50: 
51: 		return restTemplate.getForObject(url, EmployeeResponse.class, id);
52: 	}
53: 
54: 	public EmployeeResponse getEmployeeByIdWithEntity(String id) {
55: 		String url = baseUrl + "/{id}";
56: 		LOG.info("[RestTemplate] GET (entity) {} (id={})", url, id);
57: 
58: 		ResponseEntity<EmployeeResponse> response =
59: 				restTemplate.getForEntity(url, EmployeeResponse.class, id);
60: 
61: 		LOG.info("[RestTemplate] Status: {}", response.getStatusCode());
62: 		return response.getBody();
63: 	}
64: 
65: 	public EmployeeResponse getEmployeeByEmail(String email) {
66: 		String url = UriComponentsBuilder
67: 				.fromHttpUrl(baseUrl + "/email")
68: 				.queryParam("email", email)
69: 				.toUriString();
70: 
71: 		LOG.info("[RestTemplate] GET {}", url);
72: 		return restTemplate.getForObject(url, EmployeeResponse.class);
73: 	}
74: 
75: 	public List<EmployeeResponse> getEmployeesByFirstName(String firstName) {
76: 		String url = UriComponentsBuilder
77: 				.fromHttpUrl(baseUrl + "/search")
78: 				.queryParam("firstName", firstName)
79: 				.toUriString();
80: 
81: 		LOG.info("[RestTemplate] GET {}", url);
82: 
83: 		ResponseEntity<List<EmployeeResponse>> response = restTemplate.exchange(
84: 				url,
85: 				HttpMethod.GET,
86: 				null,
87: 				new ParameterizedTypeReference<List<EmployeeResponse>>() {});
88: 
89: 		return response.getBody();
90: 	}
91: 
92: 	public List<EmployeeResponse> getEmployeesByFirstNamePath(String firstName) {
93: 		String url = baseUrl + "/search/{firstName}";
94: 		LOG.info("[RestTemplate] GET {} (firstName={})", url, firstName);
95: 
96: 		ResponseEntity<List<EmployeeResponse>> response = restTemplate.exchange(
97: 				url,
98: 				HttpMethod.GET,
99: 				null,
100: 				new ParameterizedTypeReference<List<EmployeeResponse>>() {},
101: 				firstName);
102: 
103: 		return response.getBody();
104: 	}
105: 
106: 	public EmployeeResponse createEmployee(EmployeeRequest request) {
107: 		LOG.info("[RestTemplate] POST {}", baseUrl);
108: 
109: 		return restTemplate.postForObject(baseUrl, request, EmployeeResponse.class);
110: 	}
111: 
112: 	public EmployeeResponse createEmployeeWithEntity(EmployeeRequest request) {
113: 		LOG.info("[RestTemplate] POST (entity) {}", baseUrl);
114: 
115: 		ResponseEntity<EmployeeResponse> response =
116: 				restTemplate.postForEntity(baseUrl, request, EmployeeResponse.class);
117: 
118: 		LOG.info("[RestTemplate] Status: {}", response.getStatusCode()); // 201 CREATED
119: 		return response.getBody();
120: 	}
121: 
122: 	public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
123: 		String url = baseUrl + "/{id}";
124: 		LOG.info("[RestTemplate] PUT {} (id={})", url, id);
125: 
126: 		HttpEntity<EmployeeRequest> httpEntity = new HttpEntity<>(request);
127: 
128: 		ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
129: 				url,
130: 				HttpMethod.PUT,
131: 				httpEntity,
132: 				EmployeeResponse.class,
133: 				id);
134: 
135: 		LOG.info("[RestTemplate] Status: {}", response.getStatusCode());
136: 		return response.getBody();
137: 	}
138: 
139: 	public void deleteEmployee(String id) {
140: 		String url = baseUrl + "/{id}";
141: 		LOG.info("[RestTemplate] DELETE {} (id={})", url, id);
142: 		restTemplate.delete(url, id);
143: 		LOG.info("[RestTemplate] Employee {} deleted (204 No Content)", id);
144: 	}
145: }
```

- **Line 25–26** — a *second* `@Value` injection of the same `employee.api.base-url` property, independent of `AppConfig`'s copy — each Spring-managed bean that needs the property re-reads it from the environment via `@Value`.
- **Line 30–32** — constructor injection of the `RestTemplate` bean created in `AppConfig` (Section 13) — Spring matches by type since there's only one `RestTemplate` bean in the context.
- **Line 37–41** — `restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType)` is the most general-purpose RestTemplate call: you specify the verb explicitly (`HttpMethod.GET`), pass `null` for the request body/headers (`HttpEntity`) since a GET has none, and supply a `ParameterizedTypeReference<List<EmployeeResponse>>() {}` — an anonymous subclass trick required because Java erases generic type parameters at runtime, so `List<EmployeeResponse>.class` isn't expressible; `ParameterizedTypeReference` captures the full generic type via reflection on the anonymous class's superclass.
- **Line 43–44** — `response.getStatusCode()` / `response.getBody()`: `exchange()` always returns a `ResponseEntity<T>`, giving access to status and headers, unlike `getForObject`.
- **Line 51** — `restTemplate.getForObject(url, EmployeeResponse.class, id)` — the simplest possible GET: pass a URL template with a `{id}` placeholder, the target type, and varargs to fill placeholders positionally. Returns the deserialized body directly — no access to status code or headers (equivalent to `requests.get(url).json()` versus `requests.get(url)` and manually checking `.status_code`).
- **Line 58–59** — `getForEntity(...)` — same as `getForObject` but wraps the result in a `ResponseEntity`, so you *do* get status/headers back; shown here as an alternate method (`getEmployeeByIdWithEntity`) purely for comparison, not called by the runner.
- **Line 66–69** — `UriComponentsBuilder.fromHttpUrl(...).queryParam("email", email).toUriString()` builds a URL with a properly encoded query string — Spring's equivalent of `requests.get(url, params={...})` or manually building a `URLSearchParams` in JS; necessary here because `RestTemplate` has no `params` map convenience like `requests` does.
- **Line 83–90 / 96–104** — repeat the `exchange()` + `ParameterizedTypeReference` pattern for two more GET variants: one taking the search term as a `@RequestParam` (query string), one as a `@PathVariable` (path segment) — directly illustrating the courseware's `@RequestParam` vs `@PathVariable` distinction (Section 3) from the *client's* perspective (constructing, not receiving, the request).
- **Line 109** — `restTemplate.postForObject(url, requestBody, ResponseType.class)` — POST convenience method: implicitly wraps `request` in an `HttpEntity`, sends it, and returns just the deserialized response body.
- **Line 115–116** — `postForEntity(...)` — same as `postForObject` but returns the full `ResponseEntity`, so the actual `201 CREATED` status from the server is inspectable (line 118's comment confirms this is expected to be 201, consistent with Section 1's status-code table and the courseware provider example's `ResponseEntity.created(...)`).
- **Line 126** — `new HttpEntity<>(request)` wraps the request DTO as the body of a manually-built HTTP request, with no custom headers here (the courseware's `HrIntegrationService.syncToPayroll` example additionally set `HttpHeaders` with `setContentType`/`setBearerAuth` — this simpler local version doesn't need auth against a local dev server).
- **Line 128–133** — `exchange(url, HttpMethod.PUT, httpEntity, EmployeeResponse.class, id)` — PUT via `exchange()` because, per the comment on line 191–194 of the original source, `RestTemplate.put()` exists but returns `void`; using `exchange()` instead lets this method return the server's updated representation.
- **Line 142** — `restTemplate.delete(url, id)` — the simplest delete call, returns nothing; the server is expected to answer `204 No Content` (line 143's log message and Section 1's status table agree).
- **Blocking behavior**: every method here calls straight through to `restTemplate.xxx()` and returns a plain Java value (`List<EmployeeResponse>`, `EmployeeResponse`, `void`) — there is no `Future`/`Mono`/`CompletableFuture` anywhere in this class. The calling thread is fully blocked for the duration of each HTTP round trip. This is the "classic/blocking" behavior called out throughout the courseware and Q&A — contrast directly with Section 6's WebClient discussion and Section 15 below.

---

## 15. `service/EmployeeWebClientService.java` — WebClient

```java
1:  package com.acme.client.service;
2:  
3:  import java.util.List;
4:  
5:  import org.slf4j.Logger;
6:  import org.slf4j.LoggerFactory;
7:  import org.springframework.core.ParameterizedTypeReference;
8:  import org.springframework.http.HttpStatus;
9:  import org.springframework.stereotype.Service;
10: import org.springframework.web.reactive.function.client.WebClient;
11: 
12: import com.acme.client.dto.employee.EmployeeRequest;
13: import com.acme.client.dto.employee.EmployeeResponse;
14: 
15: import reactor.core.publisher.Mono;
16: 
17: @Service
18: public class EmployeeWebClientService {
19: 
20: 	private static final Logger LOG = LoggerFactory.getLogger(EmployeeWebClientService.class);
21: 
22: 	private final WebClient webClient;
23: 
24: 	public EmployeeWebClientService(WebClient webClient) {
25: 		this.webClient = webClient;
26: 	}
27: 
28: 	public List<EmployeeResponse> getAllEmployees() {
29: 		LOG.info("[WebClient] GET /");
30: 
31: 		return webClient.get()
32: 				.uri("/")
33: 				.retrieve()
34: 				.bodyToFlux(EmployeeResponse.class)
35: 				.collectList()
36: 				.block();
37: 	}
38: 
39: 	public EmployeeResponse getEmployeeById(String id) {
40: 		LOG.info("[WebClient] GET /{}", id);
41: 
42: 		return webClient.get()
43: 				.uri("/{id}", id)
44: 				.retrieve()
45: 				.bodyToMono(EmployeeResponse.class)
46: 				.block();
47: 	}
48: 
49: 	public EmployeeResponse getEmployeeByEmail(String email) {
50: 		LOG.info("[WebClient] GET /email?email={}", email);
51: 
52: 		return webClient.get()
53: 				.uri(uriBuilder -> uriBuilder
54: 						.path("/email")
55: 						.queryParam("email", email)
56: 						.build())
57: 				.retrieve()
58: 				.bodyToMono(EmployeeResponse.class)
59: 				.block();
60: 	}
61: 
62: 	public List<EmployeeResponse> getEmployeesByFirstName(String firstName) {
63: 		LOG.info("[WebClient] GET /search?firstName={}", firstName);
64: 
65: 		return webClient.get()
66: 				.uri(uriBuilder -> uriBuilder
67: 						.path("/search")
68: 						.queryParam("firstName", firstName)
69: 						.build())
70: 				.retrieve()
71: 				.bodyToFlux(EmployeeResponse.class)
72: 				.collectList()
73: 				.block();
74: 	}
75: 
76: 	public List<EmployeeResponse> getEmployeesByFirstNamePath(String firstName) {
77: 		LOG.info("[WebClient] GET /search/{}", firstName);
78: 
79: 		return webClient.get()
80: 				.uri("/search/{firstName}", firstName)
81: 				.retrieve()
82: 				.bodyToFlux(EmployeeResponse.class)
83: 				.collectList()
84: 				.block();
85: 	}
86: 
87: 	public EmployeeResponse createEmployee(EmployeeRequest request) {
88: 		LOG.info("[WebClient] POST /");
89: 
90: 		return webClient.post()
91: 				.uri("/")
92: 				.bodyValue(request)
93: 				.retrieve()
94: 				.onStatus(status -> status == HttpStatus.CREATED,
95: 						response -> Mono.empty())
96: 				.bodyToMono(EmployeeResponse.class)
97: 				.block();
98: 	}
99: 
100: 	public EmployeeResponse updateEmployee(String id, EmployeeRequest request) {
101: 		LOG.info("[WebClient] PUT /{}", id);
102: 
103: 		return webClient.put()
104: 				.uri("/{id}", id)
105: 				.bodyValue(request)
106: 				.retrieve()
107: 				.bodyToMono(EmployeeResponse.class)
108: 				.block();
109: 	}
110: 
111: 	public void deleteEmployee(String id) {
112: 		LOG.info("[WebClient] DELETE /{}", id);
113: 
114: 		webClient.delete()
115: 				.uri("/{id}", id)
116: 				.retrieve()
117: 				.toBodilessEntity()
118: 				.block();
119: 
120: 		LOG.info("[WebClient] Employee {} deleted (204 No Content)", id);
121: 	}
122: }
```

- **Line 24–26** — constructor injection of the single shared `WebClient` bean from `AppConfig`, which already has `baseUrl` baked in (Section 13) — that's why every `.uri(...)` call below is a *relative* path (`"/"`, `"/{id}"`), unlike `EmployeeRestTemplateService`, which concatenated the full absolute URL itself every time.
- **Line 31** — `webClient.get()` opens a `WebClient.RequestHeadersUriSpec` for a GET request — the fluent-builder entry point, analogous to starting an `axios({method: 'get', ...})` chain.
- **Line 32** — `.uri("/")` resolves against the base URL to hit exactly `employee.api.base-url` (i.e. `.../employees`) with nothing appended.
- **Line 33** — `.retrieve()` executes the exchange and hands you a `ResponseSpec`; by default it treats any 4xx/5xx as an error and raises `WebClientResponseException` — the reactive equivalent of `response.raise_for_status()` in `requests`, except automatic.
- **Line 34** — `.bodyToFlux(EmployeeResponse.class)` — deserializes a JSON array response body into a `Flux<EmployeeResponse>`, a reactive stream that can emit 0-to-N items over time (as opposed to a `Mono`, which emits 0-or-1). Used here because the endpoint returns a list.
- **Line 35** — `.collectList()` — a Reactor operator that gathers every item the `Flux` emits into a single `Mono<List<EmployeeResponse>>` — needed because the method's return type is a plain `List`, not a `Flux`.
- **Line 36** — `.block()` — subscribes to the `Mono` and blocks the calling thread until it completes, then unwraps to the plain `List<EmployeeResponse>`. This is the point where the method **opts back into synchronous/blocking behavior** despite using WebClient — legitimate when the calling code (here, a plain `@Service`/`@RestController` method) isn't itself reactive. The commented-out block at the bottom of the original file (`getEmployeeByIdReactive`, `getAllEmployeesReactive`) shows the *true* non-blocking alternative: return the `Mono`/`Flux` directly and let the caller subscribe (or let Spring WebFlux's reactive controller support handle it), so the request-handling thread is never blocked waiting on the remote call — that's the actual payoff of using WebClient over RestTemplate, not realized in this demo's blocking usage.
- **Line 43** — `.uri("/{id}", id)` — same URI-template + varargs mechanism as `RestTemplate`'s `getForObject`, just on WebClient's fluent builder.
- **Line 45** — `.bodyToMono(EmployeeResponse.class)` — single-object deserialization (as opposed to `bodyToFlux` for lists).
- **Line 53–56** — `.uri(uriBuilder -> uriBuilder.path("/email").queryParam("email", email).build())` — the lambda form of `.uri(...)` gives you a `UriBuilder` for programmatic query-string construction, WebClient's equivalent of `RestTemplate`'s `UriComponentsBuilder` usage in Section 14 (line 66–69) — same job, different API shape.
- **Line 90–98 (`createEmployee`)** — `.post().uri("/").bodyValue(request)` — `bodyValue(request)` serializes the DTO to JSON and sets it as the request body (Jackson under the hood, same message-converter machinery as `@RequestBody` on the server side). `.onStatus(status -> status == HttpStatus.CREATED, response -> Mono.empty())` is a slightly unusual guard: normally `.onStatus` is used to turn *error* statuses into a custom `Mono.error(...)`; here it intercepts the *success* status `201 CREATED` and maps it to `Mono.empty()`, which — combined with `retrieve()`'s default error-only behavior — effectively is a no-op safety net making sure `201` is never misclassified as an error path before falling through to `.bodyToMono(...)` to actually read the body. This is present as instructional emphasis that WebClient treats "outside 2xx" (not just 2xx-vs-not-201) as success by default, so 201 already flows through fine without this line — but the developer added it explicitly to make the "yes, 201 is success" point visible in the code.
- **Line 103–109 (`updateEmployee`)** — same `.put().uri("/{id}", id).bodyValue(request).retrieve().bodyToMono(...).block()` shape as POST, just with `HttpMethod.PUT`.
- **Line 114–118 (`deleteEmployee`)** — `.delete().uri("/{id}", id).retrieve().toBodilessEntity()` — `toBodilessEntity()` is the WebClient counterpart for endpoints that return `204 No Content` with genuinely no body (contrast `bodyToMono`, which expects a JSON payload to deserialize); returns `Mono<ResponseEntity<Void>>`, then `.block()` waits for completion and discards the result — the method's return type is `void`.

**RestTemplate vs WebClient, side by side (from this project's own two services):**

| Aspect | `EmployeeRestTemplateService` | `EmployeeWebClientService` |
|---|---|---|
| Base URL | Concatenated manually per call (`baseUrl + "/{id}"`) | Baked into the shared `WebClient` bean, calls use relative paths |
| Single object GET | `getForObject(url, Type.class, id)` | `.get().uri(...).retrieve().bodyToMono(Type.class).block()` |
| List GET | `exchange(...) ` + `ParameterizedTypeReference<List<T>>` | `.retrieve().bodyToFlux(Type.class).collectList().block()` |
| POST | `postForObject`/`postForEntity` | `.post().bodyValue(request).retrieve().bodyToMono(...).block()` |
| PUT | `exchange(url, HttpMethod.PUT, entity, Type.class, id)` | `.put().uri(...).bodyValue(request).retrieve().bodyToMono(...).block()` |
| DELETE | `restTemplate.delete(url, id)` (void) | `.delete().uri(...).retrieve().toBodilessEntity().block()` |
| Threading model | Always blocking — no non-blocking option | Reactive by design; `.block()` opts back into blocking (used throughout this demo for simplicity); commented-out `Mono`/`Flux`-returning variants show the genuinely non-blocking form |
| Spring status | "Classic," in maintenance mode | Modern, recommended for new code (Spring 5+) |

---

## 16. `controller/EmployeeClientController.java`

```java
1:  package com.acme.client.controller;
2:  
3:  import java.util.List;
4:  
5:  import org.springframework.http.HttpStatus;
6:  import org.springframework.http.ResponseEntity;
7:  import org.springframework.web.bind.annotation.DeleteMapping;
8:  import org.springframework.web.bind.annotation.GetMapping;
9:  import org.springframework.web.bind.annotation.PathVariable;
10: import org.springframework.web.bind.annotation.PostMapping;
11: import org.springframework.web.bind.annotation.PutMapping;
12: import org.springframework.web.bind.annotation.RequestBody;
13: import org.springframework.web.bind.annotation.RequestMapping;
14: import org.springframework.web.bind.annotation.RequestParam;
15: import org.springframework.web.bind.annotation.RestController;
16: 
17: import com.acme.client.dto.employee.EmployeeRequest;
18: import com.acme.client.dto.employee.EmployeeResponse;
19: import com.acme.client.service.EmployeeRestTemplateService;
20: import com.acme.client.service.EmployeeWebClientService;
21: 
22: @RestController
23: @RequestMapping("/client")
24: public class EmployeeClientController {
25: 
26: 	private final EmployeeRestTemplateService rtService;
27: 	private final EmployeeWebClientService    wcService;
28: 
29: 	public EmployeeClientController(EmployeeRestTemplateService rtService,
30: 	                                EmployeeWebClientService wcService) {
31: 		this.rtService = rtService;
32: 		this.wcService = wcService;
33: 	}
34: 
35: 	@GetMapping("/rt/employees")
36: 	public ResponseEntity<List<EmployeeResponse>> rtGetAll() {
37: 		return ResponseEntity.ok(rtService.getAllEmployees());
38: 	}
39: 
40: 	@GetMapping("/rt/employees/{id}")
41: 	public ResponseEntity<EmployeeResponse> rtGetById(@PathVariable String id) {
42: 		return ResponseEntity.ok(rtService.getEmployeeById(id));
43: 	}
44: 
45: 	@GetMapping("/rt/employees/email")
46: 	public ResponseEntity<EmployeeResponse> rtGetByEmail(@RequestParam String email) {
47: 		return ResponseEntity.ok(rtService.getEmployeeByEmail(email));
48: 	}
49: 
50: 	@GetMapping("/rt/employees/search")
51: 	public ResponseEntity<List<EmployeeResponse>> rtSearchByName(@RequestParam String firstName) {
52: 		return ResponseEntity.ok(rtService.getEmployeesByFirstName(firstName));
53: 	}
54: 
55: 	@GetMapping("/rt/employees/search/{firstName}")
56: 	public ResponseEntity<List<EmployeeResponse>> rtSearchByNamePath(@PathVariable String firstName) {
57: 		return ResponseEntity.ok(rtService.getEmployeesByFirstNamePath(firstName));
58: 	}
59: 
60: 	@PostMapping("/rt/employees")
61: 	public ResponseEntity<EmployeeResponse> rtCreate(@RequestBody EmployeeRequest request) {
62: 		return ResponseEntity.status(HttpStatus.CREATED).body(rtService.createEmployee(request));
63: 	}
64: 
65: 	@PutMapping("/rt/employees/{id}")
66: 	public ResponseEntity<EmployeeResponse> rtUpdate(@PathVariable String id,
67: 	                                                  @RequestBody EmployeeRequest request) {
68: 		return ResponseEntity.ok(rtService.updateEmployee(id, request));
69: 	}
70: 
71: 	@DeleteMapping("/rt/employees/{id}")
72: 	public ResponseEntity<Void> rtDelete(@PathVariable String id) {
73: 		rtService.deleteEmployee(id);
74: 		return ResponseEntity.noContent().build();
75: 	}
76: 
77: 	// WebClient routes → /client/wc/** (identical structure, calls wcService instead)
78: 	@GetMapping("/wc/employees")
79: 	public ResponseEntity<List<EmployeeResponse>> wcGetAll() {
80: 		return ResponseEntity.ok(wcService.getAllEmployees());
81: 	}
82: 
83: 	@GetMapping("/wc/employees/{id}")
84: 	public ResponseEntity<EmployeeResponse> wcGetById(@PathVariable String id) {
85: 		return ResponseEntity.ok(wcService.getEmployeeById(id));
86: 	}
87: 
88: 	@GetMapping("/wc/employees/email")
89: 	public ResponseEntity<EmployeeResponse> wcGetByEmail(@RequestParam String email) {
90: 		return ResponseEntity.ok(wcService.getEmployeeByEmail(email));
91: 	}
92: 
93: 	@GetMapping("/wc/employees/search")
94: 	public ResponseEntity<List<EmployeeResponse>> wcSearchByName(@RequestParam String firstName) {
95: 		return ResponseEntity.ok(wcService.getEmployeesByFirstName(firstName));
96: 	}
97: 
98: 	@GetMapping("/wc/employees/search/{firstName}")
99: 	public ResponseEntity<List<EmployeeResponse>> wcSearchByNamePath(@PathVariable String firstName) {
100: 		return ResponseEntity.ok(wcService.getEmployeesByFirstNamePath(firstName));
101: 	}
102: 
103: 	@PostMapping("/wc/employees")
104: 	public ResponseEntity<EmployeeResponse> wcCreate(@RequestBody EmployeeRequest request) {
105: 		return ResponseEntity.status(HttpStatus.CREATED).body(wcService.createEmployee(request));
106: 	}
107: 
108: 	@PutMapping("/wc/employees/{id}")
109: 	public ResponseEntity<EmployeeResponse> wcUpdate(@PathVariable String id,
110: 	                                                  @RequestBody EmployeeRequest request) {
111: 		return ResponseEntity.ok(wcService.updateEmployee(id, request));
112: 	}
113: 
114: 	@DeleteMapping("/wc/employees/{id}")
115: 	public ResponseEntity<Void> wcDelete(@PathVariable String id) {
116: 		wcService.deleteEmployee(id);
117: 		return ResponseEntity.noContent().build();
118: 	}
119: }
```

- **Line 22–23** — `@RestController` + `@RequestMapping("/client")`: this class *is* a real REST provider — but its "business logic" is entirely delegation to the two client services, so despite using the same annotations as the courseware's provider-side `EmployeeRestController`, its actual job is proxying calls out to another server, not owning any data. Important distinction for the assessment: don't describe this class as "the REST API" — it's a REST API façade over a REST client.
- **Line 29–33** — constructor injection of *both* service beans simultaneously — Spring resolves each parameter by type since `EmployeeRestTemplateService` and `EmployeeWebClientService` are distinct types (no `@Qualifier` needed).
- **Line 35–38 (`rtGetAll`)** — `@GetMapping("/rt/employees")` maps `GET /client/rt/employees`; body just wraps the RestTemplate-service call in `ResponseEntity.ok(...)` → always `200`.
- **Line 40–43 (`rtGetById`)** — `@PathVariable String id` extracts the ID segment; matches Section 3's `@PathVariable` explanation directly.
- **Line 45–48 (`rtGetByEmail`)** — `@RequestParam String email` — required query param, no default, so a request missing `?email=...` returns `400` automatically (Spring's built-in `MissingServletRequestParameterException` handling).
- **Line 50–58** — two parallel search endpoints, one via `@RequestParam` (`/search?firstName=`) and one via `@PathVariable` (`/search/{firstName}`) — deliberately mirrors both DTO-facing variants implemented in the services (Section 14/15), letting Aakash compare query-param vs path-segment styles for the same underlying search.
- **Line 60–63 (`rtCreate`)** — `@RequestBody EmployeeRequest request` deserializes the incoming JSON into the same client-side DTO used to call the remote server; `ResponseEntity.status(HttpStatus.CREATED).body(...)` explicitly returns `201`, matching Section 4's pattern (though, unlike the courseware provider example, no `Location` header is set here — a real difference worth noting: this controller returns bare `201` without the `Location` header REST best practice recommends).
- **Line 65–69 (`rtUpdate`)** — combines `@PathVariable` (which resource) with `@RequestBody` (new representation) for a full `PUT` replace, `200 OK` on success.
- **Line 71–75 (`rtDelete`)** — calls `rtService.deleteEmployee(id)` (void) then manually returns `ResponseEntity.noContent().build()` → `204`, matching Section 1's status table exactly.
- **Line 78–118** — the `/wc/**` routes are a structurally identical mirror of the `/rt/**` routes, just delegating to `wcService` instead of `rtService` — this 1:1 parallel structure is what makes it possible to hit `/client/rt/employees` and `/client/wc/employees` from Postman and compare RestTemplate vs WebClient behavior against the exact same remote server, live.

---

## 17. `EmployeeClientRunner.java`

```java
1:  package com.acme.client;
2:  
3:  import java.util.List;
4:  
5:  import org.slf4j.Logger;
6:  import org.slf4j.LoggerFactory;
7:  import org.springframework.boot.CommandLineRunner;
8:  import org.springframework.stereotype.Component;
9:  
10: import com.acme.client.dto.employee.EmployeeRequest;
11: import com.acme.client.dto.employee.EmployeeResponse;
12: import com.acme.client.service.EmployeeRestTemplateService;
13: import com.acme.client.service.EmployeeWebClientService;
14: 
15: @Component
16: public class EmployeeClientRunner implements CommandLineRunner {
17: 
18: 	private static final Logger LOG = LoggerFactory.getLogger(EmployeeClientRunner.class);
19: 
20: 	private final EmployeeRestTemplateService rtService;
21: 	private final EmployeeWebClientService    wcService;
22: 
23: 	public EmployeeClientRunner(EmployeeRestTemplateService rtService,
24: 	                            EmployeeWebClientService wcService) {
25: 		this.rtService = rtService;
26: 		this.wcService = wcService;
27: 	}
28: 
29: 	@Override
30: 	public void run(String... args) throws Exception {
31: 
32: 		LOG.info("=============================================================");
33: 		LOG.info("  Employee REST Client Demo — startup runner");
34: 		LOG.info("=============================================================");
35: 
36: 		LOG.info("\n--- RestTemplate: getAllEmployees ---");
37: 		try {
38: 			List<EmployeeResponse> all = rtService.getAllEmployees();
39: 			all.forEach(e -> LOG.info("  {}", e));
40: 
41: 			if (!all.isEmpty()) {
42: 				String firstId = all.get(0).getId();
43: 
44: 				LOG.info("\n--- RestTemplate: getEmployeeById ({}) ---", firstId);
45: 				EmployeeResponse byId = rtService.getEmployeeById(firstId);
46: 				LOG.info("  {}", byId);
47: 
48: 				LOG.info("\n--- RestTemplate: getEmployeeByEmail ({}) ---", byId.getEmail());
49: 				EmployeeResponse byEmail = rtService.getEmployeeByEmail(byId.getEmail());
50: 				LOG.info("  {}", byEmail);
51: 
52: 				LOG.info("\n--- RestTemplate: searchByFirstName ({}) ---", byId.getFirstName());
53: 				List<EmployeeResponse> byName = rtService.getEmployeesByFirstName(byId.getFirstName());
54: 				byName.forEach(e -> LOG.info("  {}", e));
55: 			}
56: 
57: 			LOG.info("\n--- RestTemplate: createEmployee ---");
58: 			EmployeeRequest newEmployee = EmployeeRequest.builder()
59: 					.firstName("Sonu")
60: 					.lastName("Reddy")
61: 					.email("sonu.reddy@acme.com")
62: 					.department("Engineering")
63: 					.salary(75000.0)
64: 					.build();
65: 			EmployeeResponse created = rtService.createEmployee(newEmployee);
66: 			LOG.info("  Created: {}", created);
67: 
68: 			LOG.info("\n--- RestTemplate: updateEmployee ({}) ---", created.getId());
69: 			EmployeeRequest updated = EmployeeRequest.builder()
70: 					.firstName("Sonu")
71: 					.lastName("Reddy")
72: 					.email("sonu.reddy@acme.com")
73: 					.department("Architecture")
74: 					.salary(90000.0)
75: 					.build();
76: 			EmployeeResponse updatedResp = rtService.updateEmployee(created.getId(), updated);
77: 			LOG.info("  Updated: {}", updatedResp);
78: 
79: 			LOG.info("\n--- RestTemplate: deleteEmployee ({}) ---", created.getId());
80: 			rtService.deleteEmployee(created.getId());
81: 			LOG.info("  Deleted successfully.");
82: 
83: 		} catch (Exception e) {
84: 			LOG.warn("[RestTemplate] Server may not be running: {}", e.getMessage());
85: 		}
86: 
87: 		// (WebClient block — identical structure, calls wcService, uses "Monu"/"Rao" test data)
88: 
89: 		LOG.info("\n=============================================================");
90: 		LOG.info("  Runner complete. Hit http://localhost:8095/client/rt|wc/**");
91: 		LOG.info("=============================================================");
92: 	}
93: }
```

- **Line 16** — `implements CommandLineRunner` — per Q&A #67, `CommandLineRunner`'s `run()` executes automatically once the Spring application context is fully started; here it's used to exercise the whole CRUD cycle against the remote server as a live smoke test on every app boot, not for genuine production data seeding.
- **Line 15** — `@Component` registers this runner as a managed bean so Spring Boot discovers and invokes it (`SpringApplication.run` calls every `CommandLineRunner` bean's `run()` after startup).
- **Line 23–27** — constructor injection of both services, same pattern as the controller.
- **Line 38, 45, 49, 53** — calls straight through the RestTemplate service's public methods (`getAllEmployees`, `getEmployeeById`, `getEmployeeByEmail`, `getEmployeesByFirstName`) exactly as documented in Section 14 — this runner is effectively an integration-smoke-test client for the client itself.
- **Line 42** — pulls `getId()` off the first returned employee to chain subsequent calls (`getEmployeeById`, `getEmployeeByEmail`, search-by-name) — demonstrates a realistic sequential API-consumption flow, comparable to chaining `requests` calls in a Python script using data from a prior response.
- **Line 58–64** — `EmployeeRequest.builder()...build()` — the Lombok `@Builder` from Section 11 in actual use, constructing a request DTO fluently.
- **Line 65–66, 76–77, 80–81** — full CRUD lifecycle exercised in sequence: create → update → delete, logging each result, directly demonstrating `createEmployee`/`updateEmployee`/`deleteEmployee` from `EmployeeRestTemplateService` end-to-end against a live server.
- **Line 83–85** — a broad `catch (Exception e)` around the entire RestTemplate block, logging a warning rather than failing app startup — acknowledges this runner is a demo that shouldn't crash the whole app if the remote `employee-service` isn't up on `localhost:8090`. This is a pragmatic pattern for a startup-time smoke test, but note it is *not* the `@ExceptionHandler`/`@ControllerAdvice` REST exception-mapping discussed in Section 7 — that pattern is for a controller translating exceptions into HTTP responses; this is just try/catch around a demo routine.
- **Line 87** — the WebClient block (omitted here for brevity, but present in the real file) is structurally identical: `wcService.getAllEmployees()` → `getEmployeeById` → `getEmployeeByEmail` → `getEmployeesByFirstName` → `createEmployee` (with "Monu Rao" test data) → `updateEmployee` → `deleteEmployee`, wrapped in its own `try/catch`. The parallel structure again reinforces the RestTemplate-vs-WebClient comparison the whole project is built around.
- **Line 90** — the closing log line documents the real server address (`localhost:8095`, matching `server.port=8095` in `application.properties`, Section 18) and the two route prefixes (`/client/rt/**`, `/client/wc/**`) exposed by the controller.

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
- **Line 4** — `employee.api.base-url=http://localhost:8090/api/v1/employees`: the property both `AppConfig` (Section 13) and `EmployeeRestTemplateService` (Section 14) read via `@Value`. Port `8090` is a *different* port from this app's own `8095` — confirms concretely that this project is a separate process calling out to another Spring Boot app (the `employee-service`) running on `8090`, consistent with the courseware's microservice port-per-service pattern (Section 8: `:8081`, `:8082`, `:8083` examples) and exactly matching the URL path shape (`/api/v1/employees`) from the courseware's provider-side `EmployeeRestController` (`@RequestMapping("/api/v1/employees")`, Section 2) — strong evidence the remote server this client targets is built the same way as the courseware's provider example.

---

## 19. `EmployeeRestTemplateServiceTest.java`

```java
1:  package com.acme.client.service;
2:  
3:  import static org.assertj.core.api.Assertions.assertThat;
4:  import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
5:  import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
6:  
7:  import java.util.List;
8:  
9:  import org.junit.jupiter.api.BeforeEach;
10: import org.junit.jupiter.api.Test;
11: import org.springframework.beans.factory.annotation.Autowired;
12: import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
13: import org.springframework.http.HttpMethod;
14: import org.springframework.http.MediaType;
15: import org.springframework.test.web.client.MockRestServiceServer;
16: import org.springframework.web.client.RestTemplate;
17: 
18: import com.fasterxml.jackson.databind.ObjectMapper;
19: import com.acme.client.dto.employee.EmployeeRequest;
20: import com.acme.client.dto.employee.EmployeeResponse;
21: 
22: @RestClientTest(components = {EmployeeRestTemplateService.class})
23: class EmployeeRestTemplateServiceTest {
24: 
25: 	@Autowired
26: 	private MockRestServiceServer mockServer;
27: 
28: 	@Autowired
29: 	private RestTemplate restTemplate;
30: 
31: 	@Autowired
32: 	private ObjectMapper objectMapper;
33: 
34: 	private EmployeeRestTemplateService service;
35: 
36: 	private static final String BASE_URL = "http://localhost:8080/api/v1/employees";
37: 
38: 	@BeforeEach
39: 	void setUp() {
40: 		service = new EmployeeRestTemplateService(restTemplate);
41: 		org.springframework.test.util.ReflectionTestUtils.setField(service, "baseUrl", BASE_URL);
42: 	}
43: 
44: 	@Test
45: 	void getAllEmployees_shouldReturnList() throws Exception {
46: 		List<EmployeeResponse> employees = List.of(
47: 				EmployeeResponse.builder().id("1").firstName("Alice").email("alice@acme.com").build(),
48: 				EmployeeResponse.builder().id("2").firstName("Bob").email("bob@acme.com").build()
49: 		);
50: 
51: 		mockServer.expect(requestTo(BASE_URL))
52: 				.andExpect(method(HttpMethod.GET))
53: 				.andRespond(withSuccess(objectMapper.writeValueAsString(employees),
54: 						MediaType.APPLICATION_JSON));
55: 
56: 		List<EmployeeResponse> result = service.getAllEmployees();
57: 
58: 		assertThat(result).hasSize(2);
59: 		assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
60: 	}
61: 
62: 	@Test
63: 	void getEmployeeById_shouldReturnEmployee() throws Exception {
64: 		EmployeeResponse employee =
65: 				EmployeeResponse.builder().id("42").firstName("Carol").email("carol@acme.com").build();
66: 
67: 		mockServer.expect(requestTo(BASE_URL + "/42"))
68: 				.andExpect(method(HttpMethod.GET))
69: 				.andRespond(withSuccess(objectMapper.writeValueAsString(employee),
70: 						MediaType.APPLICATION_JSON));
71: 
72: 		EmployeeResponse result = service.getEmployeeById("42");
73: 
74: 		assertThat(result.getId()).isEqualTo("42");
75: 		assertThat(result.getFirstName()).isEqualTo("Carol");
76: 	}
77: 
78: 	@Test
79: 	void createEmployee_shouldReturnCreatedEmployee() throws Exception {
80: 		EmployeeRequest request = EmployeeRequest.builder()
81: 				.firstName("Dave").lastName("Jones").email("dave@acme.com")
82: 				.department("HR").salary(60000.0).build();
83: 
84: 		EmployeeResponse created = EmployeeResponse.builder()
85: 				.id("99").firstName("Dave").email("dave@acme.com").build();
86: 
87: 		mockServer.expect(requestTo(BASE_URL))
88: 				.andExpect(method(HttpMethod.POST))
89: 				.andRespond(withSuccess(objectMapper.writeValueAsString(created),
90: 						MediaType.APPLICATION_JSON));
91: 
92: 		EmployeeResponse result = service.createEmployee(request);
93: 
94: 		assertThat(result.getId()).isEqualTo("99");
95: 		assertThat(result.getFirstName()).isEqualTo("Dave");
96: 	}
97: 
98: 	@Test
99: 	void deleteEmployee_shouldComplete() {
100: 		mockServer.expect(requestTo(BASE_URL + "/10"))
101: 				.andExpect(method(HttpMethod.DELETE))
102: 				.andRespond(withNoContent());
103: 
104: 		service.deleteEmployee("10");
105: 	}
106: }
```

- **Line 22** — `@RestClientTest(components = {EmployeeRestTemplateService.class})` — a Spring Boot test slice that boots only the beans needed to test a `RestTemplate`-based client: it auto-configures a `RestTemplate`, Jackson `ObjectMapper`, and a `MockRestServiceServer`, without starting the full application context (no embedded server, no unrelated beans) — much faster than `@SpringBootTest`. Conceptually similar to `unittest.mock.patch('requests.get', ...)` in Python, but Spring's version intercepts at the HTTP-client level rather than function-patching.
- **Line 25–26** — `MockRestServiceServer`: intercepts every outgoing call made through the injected `RestTemplate` and matches it against `.expect(...)` rules instead of hitting the network — so these tests run with **no live server**, unlike `EmployeeClientRunner`, which genuinely calls `localhost:8090`.
- **Line 28–29, 31–32** — the test also gets the auto-configured `RestTemplate` bean (the *same instance* the mock server is wired against) and an `ObjectMapper` to hand-serialize expected JSON responses.
- **Line 34, 40** — the test constructs `EmployeeRestTemplateService` manually (`new EmployeeRestTemplateService(restTemplate)`) rather than autowiring it, because the `baseUrl` field needs a test-specific value injected next.
- **Line 41** — `ReflectionTestUtils.setField(service, "baseUrl", BASE_URL)` — bypasses the normal `@Value` injection mechanism (which only runs inside a full Spring context) and sets the private field directly via reflection, simulating what `@Value("${employee.api.base-url}")` would have done in production. Necessary because this test slice doesn't load `application.properties`.
- **Line 51–54** — `mockServer.expect(requestTo(BASE_URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(json, MediaType.APPLICATION_JSON))` — declares: "when a GET request arrives for this exact URL, respond with this canned JSON and `200 OK`." This is the request/response contract stub, expressed fluently — the RestTemplate-testing equivalent of `responses.add(responses.GET, url, json=..., status=200)` in Python's `responses` library, or `nock` in Node.
- **Line 56, 58–59** — calls the real service method (`getAllEmployees()`), which internally calls `restTemplate.exchange(...)` exactly as in Section 14 — the mock server intercepts that call transparently, and the test asserts on the deserialized result with AssertJ's fluent `assertThat(...)`.
- **Line 67–76 (`getEmployeeById_shouldReturnEmployee`)** — same pattern, proving the `{id}` URL templating (`baseUrl + "/{id}"` → `getForObject(url, EmployeeResponse.class, id)`, Section 14 line 48–51) resolves to the literal URL `BASE_URL + "/42"` that the mock server expects — this test would fail if the URL-templating logic in the service were wrong, which is exactly what it's guarding against.
- **Line 87–96 (`createEmployee_shouldReturnCreatedEmployee`)** — verifies the POST path (`postForObject`, Section 14 line 106–109) sends to `BASE_URL` and correctly deserializes the created-employee response, including the server-assigned `id`.
- **Line 99–105 (`deleteEmployee_shouldComplete`)** — `withNoContent()` stubs a `204` response with no body, matching `restTemplate.delete(...)`'s expectation (Section 14 line 139–144); the test just asserts the call completes without throwing — there's nothing to deserialize on a `204`.
- **Notably absent**: there is no equivalent test file for `EmployeeWebClientService` in this project — testing reactive `WebClient` calls typically uses `WebClient` bound to a mocked `ExchangeFunction`, or Spring's `MockWebServer`/`WireMock`, which is a different mechanism from `MockRestServiceServer`. Worth knowing for the assessment that this asymmetry exists in the actual codebase — the RestTemplate service has unit test coverage, the WebClient service currently does not.

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
