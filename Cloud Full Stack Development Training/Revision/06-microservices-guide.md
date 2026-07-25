# Microservices — Complete Line-by-Line Guide

This guide is grounded in two sources: the course Q&A file (`03-maven/maven_microservices_discussion_qa.md`, Part B — Microservices, questions 21–55) and the real 5-module project under `Code/Microservices` — an Employee Management System (EMS) built as Spring Boot microservices: `eureka-server`, `api-gateway`, `employee-service`, `department-service`, `project-service`. No other courseware file (checked `04-spring-all/08_Spring_Boot_Advanced.md` and `04-spring-all/spring_discussion_qa.md` specifically) contains microservices-specific content — the earlier keyword grep hits on other files were false positives on generic words. Everything below traces directly to these two sources.

One structural note up front: in this snapshot of the repo, none of the three business services (`employee-service`, `department-service`, `project-service`) contain a hand-written `@RestController`. Instead they use **Spring Data REST** (`spring-boot-starter-data-rest` + `@RepositoryRestResource` on the repository interfaces) to auto-expose the JPA repositories as full HAL/JSON REST APIs with zero controller code. This is a real, deliberate architectural choice documented in the project's own `README.md` ("Zero controller code needed"), not a gap — so this guide treats it as the actual REST layer of these services.

Also note: every `application.yml` in the project (eureka-server, api-gateway, and all three business services) is **entirely commented out** in this snapshot — every line is prefixed with `#`. It reads as a fully worked-out reference configuration (ports, Eureka client settings, datasource, Spring Data REST base path, Actuator exposure) but as committed, none of these services would actually run with this config active — you'd need to uncomment it. The line-by-line walkthroughs below explain what each key *would* do once active, and call this out explicitly at each file.

---

## 1. What Is a Microservice, and Why Decompose a Monolith?

**Microservices** (Q21) is an architectural style where an application is built as a collection of small, independently deployable services, each responsible for one business capability, running in its own process, communicating over the network (REST, messaging). Contrast with a **monolith** (Q22), which packages all capabilities (auth, employees, departments, reporting) into one deployable unit.

The EMS project is the textbook illustration: instead of one Spring Boot app with `EmployeeController`/`DepartmentController`/`ProjectController` sharing one database, there are three independently deployable services, each with its own H2 database, each independently startable/scalable/replaceable.

**Benefits (Q23):** independent deployment, independent scaling, technology flexibility per service, fault isolation, smaller focused codebases.

**Challenges (Q24):** network latency/failures between services, cross-service data consistency (no distributed transactions), service discovery complexity, distributed tracing difficulty, operational overhead, testing complexity.

**When NOT to use microservices (Q25):** small teams, early-stage products, or apps without a real independent-scaling need. "Monolith first" — extract services once boundaries and scaling needs are clear. Don't over-engineer early.

**Vertical vs horizontal decomposition (Q51):** always decompose **vertically** — by business capability (EmployeeService, DepartmentService, ProjectService, exactly as in this project), not by technical layer (which creates coupling).

---

## 2. Service Discovery (Eureka)

**Service discovery (Q28)** lets services find each other dynamically without hardcoded URLs. Services register themselves with a registry on startup; callers query the registry for the current address of a target service. (Kubernetes gives you this for free via DNS-based service discovery — `http://employee-service/api/employees` — which is conceptually the same idea Eureka implements at the application layer, so if you're used to Kubernetes Services/CoreDNS from your homelab, Eureka is doing the same job one layer up in the Spring Cloud stack rather than at the network/DNS layer.)

**Eureka (Q40):** a Netflix/Spring Cloud service registry — a central server where microservices register at startup. Client services query Eureka to discover addresses instead of hardcoding URLs. Supports client-side load balancing — the caller (or the gateway, via `spring-cloud-starter-loadbalancer`) chooses which instance to call among multiple registered instances of the same service name.

In the EMS project this is realized literally: `eureka-server` is the registry (port 8761); `employee-service`, `department-service`, `project-service`, and `api-gateway` all carry `spring-cloud-starter-netflix-eureka-client` and register themselves under their `spring.application.name` (e.g. `employee-service` → `127.0.0.1:8081`).

---

## 3. API Gateway Pattern

**API Gateway (Q27):** a single entry point for all client requests to the microservices backend. Handles routing, authentication, rate limiting, SSL termination, request aggregation, logging — so individual microservices don't each reimplement these concerns. Examples given in the courseware: Spring Cloud Gateway, Kong, AWS API Gateway, NGINX.

If you've run an nginx reverse proxy at home fronting multiple backend containers by path (`/grafana/` → grafana container, `/plex/` → plex container), Spring Cloud Gateway is the same pattern, Spring-native: it terminates all external traffic on one port (8080 in this project) and forwards to the right backend based on path predicates — except instead of a static upstream list, it resolves the backend address dynamically via Eureka (`lb://employee-service` instead of a fixed IP:port).

In the EMS project, `api-gateway` is exactly this: clients only ever talk to `http://localhost:8080`; the gateway's route rules decide which of the three business services actually handles each request.

---

## 4. Inter-Service Communication

**Synchronous vs asynchronous (Q26):** synchronous (REST/HTTP, gRPC) — caller waits, both services must be up simultaneously, simple. Asynchronous (RabbitMQ, Kafka) — caller fires a message and continues, decouples services, adds complexity (eventual consistency, ordering).

The courseware Q&A does not mention Feign or WebClient by name anywhere in the 55 questions — the only inter-service communication concretely covered is the sync-vs-async distinction (Q26) plus the API Gateway routing to services via Eureka lookups (`lb://` URIs, Q27/Q40). The EMS project itself has no service-to-service call code (no Feign client, no `RestTemplate`/`WebClient` usage anywhere in the business services) — the only "inter-service" wiring present is the gateway → business-service routing via `lb://`, and the cross-service references are just plain foreign-key ID fields (e.g. `Employee.departmentId`) with no code that resolves them across services. So: don't invent Feign/WebClient examples for this course — the project's actual inter-service mechanism is gateway-mediated REST routing only.

**Idempotent APIs (Q44):** an idempotent API produces the same result whether called once or many times. `DELETE /employees/1` is idempotent. Important because microservices retries (circuit breaker, network retry) are common — idempotency prevents duplicate side effects. The Spring-Data-REST-generated endpoints in this project (`PUT`, `DELETE` on `/employees/{id}` etc.) are naturally idempotent by HTTP semantics; `POST` is not.

---

## 5. Resilience Patterns

**Circuit Breaker (Q29, Q46):** monitors calls to a downstream service; when failure rate exceeds a threshold it "trips" so subsequent calls fail fast without hitting the failing service (prevents cascade failure — callers piling up waiting threads until they themselves run out of threads and fail). After a timeout, it lets a test request through; success closes the circuit again. Implemented with **Resilience4j** in Spring Boot.

**Retry vs Circuit Breaker (Q30):** Retry re-attempts a failed call N times — good for transient blips. Circuit Breaker stops trying after repeated failures to protect both sides. Combine: retry first (transient errors), then circuit breaker (persistent failures).

**Resilience4j (Q42):** lightweight fault-tolerance library — Circuit Breaker, Retry, Rate Limiter, Bulkhead, Time Limiter, all as function decorators. Integrates via `spring-boot-starter-resilience4j`, exposes metrics via Actuator.

**Bulkhead (Q54):** isolates resources per downstream service (ship-compartment analogy) — if one service's thread pool is exhausted it doesn't starve the others. `@Bulkhead(name = "employeeService", type = THREADPOOL)`.

Note: the EMS project code itself has **no Resilience4j dependency and no circuit breaker code** — this is purely conceptual courseware content (Q29/Q30/Q42/Q46/Q54), not something implemented in the project's `pom.xml` files.

---

## 6. Config Management

**Spring Cloud Config Server (Q41):** externalizes configuration for all microservices into a centralized Git repo. Services fetch config at startup via HTTP (`http://config-server/employee-service/prod`). Changing a property doesn't require redeployment — services can refresh at runtime via `/actuator/refresh`.

The EMS project does **not** use a Config Server — each service keeps its own local `application.yml` (currently fully commented out, as noted above). This is consistent with the project being a "Hello-World-level" learning sandbox per its own README, not a config-externalized production setup.

**12-Factor App (Q55):** the courseware ties Spring Boot microservices back to the 12-Factor methodology — codebase in VCS, explicit dependencies, config in environment variables, backing services as attached resources, separate build/run stages, stateless processes, port binding, concurrency via process scaling, disposability, dev/prod parity, logs as streams, admin processes. Spring Boot microservices naturally align with most of these (e.g. port binding = `server.port`; config in environment = externalized `application.yml`/env vars rather than hardcoded values).

---

## 7. Database-per-Service Pattern

**Database per service (Q35):** each microservice owns its own database/schema; no other service accesses it directly. Data sharing happens via APIs or events. Ensures loose coupling; each service can pick the best-fit database technology.

This is directly evidenced in the EMS project's code: `employee-service` has its own `Employee` entity/repository and its own H2 instance (`employeedb`), `department-service` has `Department`+`Job` entities/repositories and its own H2 (`departmentdb`), `project-service` has `Project` entity/repository and its own H2 (`projectdb`). There is no shared `DataSource`, no cross-service JPA `@ManyToOne`/`@JoinColumn` — cross-service relationships (e.g. an `Employee`'s department) are modeled as plain `Long departmentId` fields, not JPA joins, precisely because JPA can't join across two separate databases/processes. This is called out explicitly in a code comment in `Employee.java`: *"Department and Job are in a separate service. We keep only foreign-key IDs here (no JPA join across services)."*

**Eventual consistency (Q36):** since each service has its own DB and (potentially) async communication, cross-service data isn't instantly consistent. Example given: OrderService creates an order, InventoryService decrements stock only after receiving an event — a brief inconsistency window is the trade-off for loose coupling. (Not directly demonstrated in the EMS project, which has no event-driven code — this is conceptual Q&A content.)

**CQRS (Q37):** separates the write model (commands) from the read model (queries), each optimized independently.

**Event sourcing (Q38):** stores the sequence of events that led to current state rather than just current state; state is derived by replaying events. Full audit trail, time-travel debugging.

---

## 8. Other Patterns the Q&A Covers

- **Saga pattern (Q31):** manages distributed transactions across services as a sequence of local transactions, each publishing an event that triggers the next; failures trigger compensating transactions to undo prior steps. Two styles: Choreography (event-driven, no coordinator) and Orchestration (central coordinator).
- **Strangler Fig pattern (Q32):** incremental monolith→microservices migration — new features go to microservices, old features stay in the monolith until extracted, gateway routes between them, monolith gradually shrinks.
- **Event-driven architecture (Q33):** services publish/consume events via a broker (Kafka, RabbitMQ); publisher doesn't know its consumers (loose coupling); events represent facts (`EmployeeCreated`, `SalaryUpdated`).
- **Kafka vs RabbitMQ (Q34):** RabbitMQ = traditional broker, routes messages to queues, consumed once — good for task queues/RPC. Kafka = distributed event streaming platform, events written to retained logs, replayable by multiple independent consumers — good for event sourcing, audit logs, high-throughput streams.
- **Distributed tracing (Q43):** a single request may touch 5–10 services; tracing assigns a trace ID + span IDs to follow the request through the whole chain. Tools: Micrometer Tracing + Zipkin/Jaeger.
- **Sidecar pattern (Q52):** an extra container in the same Pod handling cross-cutting concerns (logging, service mesh proxy, secrets injection, monitoring) without touching the main container's code. Heavily used in Istio.
- **Service mesh (Q53):** infrastructure layer handling service-to-service comms transparently via sidecar proxies (Envoy) — mTLS, traffic management, circuit breaking, retries, tracing — without app code changes. Istio, Linkerd.
- **Authentication across microservices (Q47):** JWTs issued by a centralized auth service/IdP; the API Gateway validates the JWT once per incoming request and forwards user identity as a trusted header; downstream services trust the header rather than re-validating the JWT each time.
- **Zero-downtime deployment (Q49):** Kubernetes rolling updates (`maxUnavailable: 0, maxSurge: 1`), readiness probes so traffic only reaches fully-started Pods, backward-compatible DB migrations so old and new versions can run against the same schema simultaneously.
- **Testing microservices (Q50):** unit tests per class, integration tests per service with a real DB (testcontainers), contract tests (Spring Cloud Contract, Pact) between consumer/producer without deploying both, end-to-end tests in staging.
- **Diagnosing a slow inter-service call (Q45):** add distributed tracing (Zipkin), check for missing DB indexes (`EXPLAIN`), check connection pool exhaustion, add a circuit breaker with timeout so the slow service can't block callers indefinitely.
- **Diagnosing a data-consistency bug (Q48):** check the broker for stuck/unprocessed messages, check consumer error logs, check offset-commit timing (at-least-once vs exactly-once), add consumer idempotency, add a dead letter queue for repeatedly-failing messages.

None of these (Saga, Kafka/RabbitMQ, sidecar, service mesh, distributed tracing tooling) appear in the EMS project's actual code — they are conceptual courseware content only, useful for the assessment's discussion-style questions but not demonstrated in the project.

---

## 9. Spring Cloud Toolkit Summary

**Spring Cloud (Q39):** the umbrella of tools for distributed systems on top of Spring Boot: service discovery (Eureka), circuit breakers (Resilience4j), distributed config (Config Server), API gateway (Spring Cloud Gateway), distributed tracing (Micrometer + Zipkin), load balancing (Spring Cloud LoadBalancer). The EMS project uses exactly two of these concretely: **Eureka** (`spring-cloud-starter-netflix-eureka-server`/`-client`) and **Spring Cloud Gateway** (`spring-cloud-starter-gateway`) plus **Spring Cloud LoadBalancer** (`spring-cloud-starter-loadbalancer`, required for `lb://` URIs to work) in the gateway.

---

# Part 2 — Walking Through the Real Project

## 10. Parent Aggregator `pom.xml`

```xml
1:  <?xml version="1.0" encoding="UTF-8"?>
2:  <project xmlns="http://maven.apache.org/POM/4.0.0"
3:           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
4:           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
5:      <modelVersion>4.0.0</modelVersion>
6:
7:      <groupId>com.ems</groupId>
8:      <artifactId>ems-microservices</artifactId>
9:      <version>1.0.0</version>
10:     <packaging>pom</packaging>
11:     <name>EMS Microservices Parent</name>
12:
13:     <modules>
14:         <module>eureka-server</module>
15:         <module>api-gateway</module>
16:         <module>employee-service</module>
17:         <module>department-service</module>
18:         <module>project-service</module>
19:     </modules>
20:
21:     <parent>
22:         <groupId>org.springframework.boot</groupId>
23:         <artifactId>spring-boot-starter-parent</artifactId>
24:         <version>3.2.5</version>
25:         <relativePath/>
26:     </parent>
27:
28:     <properties>
29:         <java.version>17</java.version>
30:         <spring-cloud.version>2023.0.1</spring-cloud.version>
31:     </properties>
32:
33:     <dependencyManagement>
34:         <dependencies>
35:             <dependency>
36:                 <groupId>org.springframework.cloud</groupId>
37:                 <artifactId>spring-cloud-dependencies</artifactId>
38:                 <version>${spring-cloud.version}</version>
39:                 <type>pom</type>
40:                 <scope>import</scope>
41:             </dependency>
42:         </dependencies>
43:     </dependencyManagement>
44:
45:     <build>
46:         <pluginManagement>
47:             <plugins>
48:                 <plugin>
49:                     <groupId>org.springframework.boot</groupId>
50:                     <artifactId>spring-boot-maven-plugin</artifactId>
51:                 </plugin>
52:             </plugins>
53:         </pluginManagement>
54:     </build>
55: </project>
```

- **Line 10** — `<packaging>pom</packaging>`: this artifact produces no JAR of its own; it exists purely to aggregate modules and centralize shared config — what makes it a "multi-module Maven project" (Q10).
- **Lines 13–19** — `<modules>`: lists the five child modules; `mvn clean install` from this root builds all five in dependency order in one shot (Q10, Q20).
- **Lines 21–26** — inherits from `spring-boot-starter-parent` 3.2.5, providing pre-tested dependency versions plus sane default plugin config. `<relativePath/>` (empty) tells Maven to resolve the parent from the repository rather than the local filesystem.
- **Lines 28–31** — `java.version=17` / `spring-cloud.version=2023.0.1`: Spring Cloud release-train versions must match the Spring Boot version (2023.0.1 pairs with Boot 3.2.x) — a mismatch is a classic source of `NoSuchMethodError`/bean-creation failures.
- **Lines 33–43** — `<dependencyManagement>` importing `spring-cloud-dependencies` as a **BOM** (Q13): `<type>pom</type>` + `<scope>import</scope>` makes all Spring Cloud artifact versions available to child modules without specifying a version — how `eureka-server`'s and `api-gateway`'s `spring-cloud-starter-*` dependencies below get resolved with no `<version>` tag.
- **Lines 45–53** — `<pluginManagement>` centralizes `spring-boot-maven-plugin` config so each child just references it by groupId/artifactId, version already pinned by the parent.

---

## 11. eureka-server

### 11.1 `eureka-server/pom.xml`

```xml
1:  <?xml version="1.0" encoding="UTF-8"?>
2:  <project xmlns="http://maven.apache.org/POM/4.0.0"
3:           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
4:           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
5:      <modelVersion>4.0.0</modelVersion>
6:
7:      <parent>
8:          <groupId>com.ems</groupId>
9:          <artifactId>ems-microservices</artifactId>
10:         <version>1.0.0</version>
11:     </parent>
12:
13:     <artifactId>eureka-server</artifactId>
14:     <name>EMS Eureka Server</name>
15:
16:     <dependencies>
17:         <dependency>
18:             <groupId>org.springframework.cloud</groupId>
19:             <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
20:         </dependency>
21:         <dependency>
22:             <groupId>org.springframework.boot</groupId>
23:             <artifactId>spring-boot-starter-actuator</artifactId>
24:         </dependency>
25:     </dependencies>
26:
27:     <build>
28:         <plugins>
29:             <plugin>
30:                 <groupId>org.springframework.boot</groupId>
31:                 <artifactId>spring-boot-maven-plugin</artifactId>
32:             </plugin>
33:         </plugins>
34:     </build>
35: </project>
```

- **Lines 7–11** — `<parent>` points at the aggregator POM, inheriting Java 17, the Spring Cloud BOM import, and plugin management.
- **Lines 17–20** — `spring-cloud-starter-netflix-eureka-server`: pulls in the Eureka Server implementation, version resolved via the imported BOM — this is what makes `@EnableEurekaServer` available on the classpath.
- **Lines 21–24** — `spring-boot-starter-actuator`: exposes `/actuator/*` monitoring endpoints.
- **Lines 27–33** — `spring-boot-maven-plugin`: repackages classes + dependencies into an executable fat JAR (Q18) — `java -jar eureka-server.jar` runs it directly with an embedded server.

### 11.2 `eureka-server/src/main/java/com/ems/eureka/EurekaServerApplication.java`

```java
1:  package com.ems.eureka;
2:
3:  import org.springframework.boot.SpringApplication;
4:  import org.springframework.boot.autoconfigure.SpringBootApplication;
5:  import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
6:
7:  @SpringBootApplication
8:  @EnableEurekaServer
9:  public class EurekaServerApplication {
10:     public static void main(String[] args) {
11:         SpringApplication.run(EurekaServerApplication.class, args);
12:     }
13: }
```

- **Line 7** — `@SpringBootApplication`: standard meta-annotation combining `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`.
- **Line 8** — `@EnableEurekaServer`: the single annotation that turns this ordinary Spring Boot app into a Eureka registry server — activates the auto-configuration that stands up the registry's REST API and dashboard UI. Without it, `spring-cloud-starter-netflix-eureka-server` on the classpath alone does nothing.

### 11.3 `eureka-server/src/main/resources/application.yml`

```yaml
1:  #server:
2:  #  port: 8761
3:  #
4:  #spring:
5:  #  application:
6:  #    name: eureka-server
7:  #
8:  #eureka:
9:  #  instance:
10: #    hostname: localhost
11: #  client:
12: #    register-with-eureka: false   # This server itself does NOT register
13: #    fetch-registry: false          # This server does NOT fetch registry
14: #    service-url:
15: #      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
16: #  server:
17: #    wait-time-in-ms-when-sync-empty: 0   # Faster startup in dev
18: #
19: #management:
20: #  endpoints:
21: #    web:
22: #      exposure:
23: #        include: "*"
```

**Every line in this file is commented out** (`#` prefix) — as committed, this service would boot on Spring Boot's default port 8080 with default Eureka self-registration behavior, not port 8761 as intended. The explanations below describe what each key controls once uncommented.

- **Line 2** — `server.port: 8761`: the Eureka convention port, so client `defaultZone` URLs are predictable.
- **Lines 5–6** — `spring.application.name: eureka-server`: the logical service name; largely cosmetic here since the server doesn't self-register (see line 12).
- **Line 12** — `eureka.client.register-with-eureka: false`: **the key line explaining why a Eureka server doesn't register with itself.** Every `spring-cloud-starter-netflix-eureka-*` app, client or server, is built on the same Eureka client library and by default tries to both register itself and fetch the registry. Left `true`, the server would register itself as a client of its own registry — harmless but semantically wrong, since the registry isn't a "service" other apps should discover. Setting this `false` disables self-registration.
- **Line 13** — `eureka.client.fetch-registry: false`: a standalone server has no peer to sync from, so it disables the registry-fetch a normal client would otherwise perform.
- **Line 15** — `eureka.client.service-url.defaultZone`: the URL to reach the registry's REST API — `http://localhost:8761/eureka/` — the same URL every other service's `application.yml` points at.
- **Line 17** — `eureka.server.wait-time-in-ms-when-sync-empty: 0`: in a multi-node cluster a new server waits for peers to sync their registries before serving traffic; `0` skips that wait, appropriate for single-node dev.
- **Lines 19–23** — `management.endpoints.web.exposure.include: "*"`: exposes all Actuator endpoints — fine for a learning sandbox, normally restricted in production.

---

## 12. api-gateway

### 12.1 `api-gateway/pom.xml`

```xml
1:  <?xml version="1.0" encoding="UTF-8"?>
2:  <project xmlns="http://maven.apache.org/POM/4.0.0"
3:           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
4:           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
5:      <modelVersion>4.0.0</modelVersion>
6:
7:      <parent>
8:          <groupId>com.ems</groupId>
9:          <artifactId>ems-microservices</artifactId>
10:         <version>1.0.0</version>
11:     </parent>
12:
13:     <artifactId>api-gateway</artifactId>
14:     <name>EMS API Gateway</name>
15:
16:     <dependencies>
17:         <!-- Spring Cloud Gateway (reactive, not servlet) -->
18:         <dependency>
19:             <groupId>org.springframework.cloud</groupId>
20:             <artifactId>spring-cloud-starter-gateway</artifactId>
21:         </dependency>
22:         <!-- Eureka Client so the gateway can discover services by name -->
23:         <dependency>
24:             <groupId>org.springframework.cloud</groupId>
25:             <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
26:         </dependency>
27:         <!-- Load balancer required for lb:// URIs -->
28:         <dependency>
29:             <groupId>org.springframework.cloud</groupId>
30:             <artifactId>spring-cloud-starter-loadbalancer</artifactId>
31:         </dependency>
32:         <dependency>
33:             <groupId>org.springframework.boot</groupId>
34:             <artifactId>spring-boot-starter-actuator</artifactId>
35:         </dependency>
36:     </dependencies>
37:
38:     <build>
39:         <plugins>
40:             <plugin>
41:                 <groupId>org.springframework.boot</groupId>
42:                 <artifactId>spring-boot-maven-plugin</artifactId>
42:             </plugin>
43:         </plugins>
44:     </build>
45: </project>
```

- **Lines 17–21** — `spring-cloud-starter-gateway`: built on **Spring WebFlux** (reactive, Netty-based), *not* the traditional servlet stack (Tomcat/`spring-web`) used by the business services below — you cannot mix `spring-boot-starter-web` and `spring-cloud-starter-gateway` in the same app without conflicts, so the gateway module deliberately has no `spring-boot-starter-web` dependency.
- **Lines 23–26** — Eureka client: registers the gateway as `api-gateway`, and lets it *query* Eureka to resolve `lb://employee-service` etc. to actual instance addresses at request time.
- **Lines 28–31** — `spring-cloud-starter-loadbalancer`: implements the `lb://` URI scheme — when a route's `uri` starts with `lb://`, this library asks the Eureka client for healthy instances of that service name and picks one (client-side load balancing, Q40). Without it, `lb://` URIs fail to resolve.
- **Lines 32–35** — Actuator: `/actuator/gateway/routes` is a Gateway-specific endpoint for inspecting the live route table.

### 12.2 `api-gateway/src/main/java/com/ems/gateway/ApiGatewayApplication.java`

```java
1:  package com.ems.gateway;
2:
3:  import org.springframework.boot.SpringApplication;
4:  import org.springframework.boot.autoconfigure.SpringBootApplication;
5:
6:  @SpringBootApplication
7:  public class ApiGatewayApplication {
8:      public static void main(String[] args) {
9:          SpringApplication.run(ApiGatewayApplication.class, args);
10:     }
11: }
```

- **Line 6** — `@SpringBootApplication` only — unlike Eureka Server's `@EnableEurekaServer`, Spring Cloud Gateway auto-configures itself purely from `spring-cloud-starter-gateway` being on the classpath, no annotation required. All routing behavior (routes, predicates, filters) is driven entirely by `application.yml`, not Java code.

### 12.3 `api-gateway/src/main/resources/application.yml`

```yaml
1:  #server:
2:  #  port: 8080
3:  #
4:  #spring:
5:  #  application:
6:  #    name: api-gateway
7:  #
8:  #  cloud:
9:  #    gateway:
10: #      # Automatically create routes from Eureka-registered services
11: #      # e.g. /employee-service/** -> lb://employee-service/**
12: #      discovery:
13: #        locator:
14: #          enabled: true
15: #          lower-case-service-id: true   # Use lowercase service names in URLs
16: #
17: #      # Manual fine-grained routes (optional, but shows the concept clearly)
18: #      routes:
19: #
20: #        # ── Employee Service ────────────────────────────────────────────────
21: #        - id: employee-service
22: #          uri: lb://employee-service           # lb = client-side load balancing via Eureka
23: #          predicates:
24: #            - Path=/api/employees/**
25: #          filters:
26: #            - StripPrefix=1                    # Strip /api before forwarding
27: #
28: #        # ── Department Service (two routes, same uri, different paths) ────────
29: #        - id: department-service-dept
30: #          uri: lb://department-service
31: #          predicates:
32: #            - Path=/api/departments/**
33: #          filters:
34: #            - StripPrefix=1
35: #
36: #        # ── Project Service ─────────────────────────────────────────────────
37: #        - id: project-service
38: #          uri: lb://project-service
39: #          predicates:
40: #            - Path=/api/projects/**
41: #          filters:
42: #            - StripPrefix=1
43: #
44: #eureka:
45: #  client:
46: #    service-url:
47: #      defaultZone: http://localhost:8761/eureka/
48: #  instance:
49: #    prefer-ip-address: true
50: #
51: #management:
52: #  endpoints:
53: #    web:
54: #      exposure:
55: #        include: "*"
56: #  endpoint:
57: #    health:
58: #      show-details: always
```

- **Line 2** — `server.port: 8080`: the gateway is the single public entry point (Q27), so it conventionally sits on the "main" port clients hit.
- **Lines 12–15** — `spring.cloud.gateway.discovery.locator.enabled: true`: an alternative to manual routes — auto-generates a route for *every* service registered in Eureka, at `/<service-id>/**` → `lb://<service-id>`. `lower-case-service-id: true` normalizes the path segment to lowercase. Coexists with the manual `routes:` list below since the path shapes differ (`/api/employees/**` vs auto-generated `/employee-service/**`).
- **Lines 18–42** — the **manual routes** list, one entry per business capability, each with three parts:
  - `id`: an arbitrary unique route name (department-service needs two entries pointing at the same `uri` but different `Path` predicates, since one Eureka service name can back multiple path patterns).
  - `uri: lb://<service-name>`: the **logical** destination, resolved at request time by `spring-cloud-starter-loadbalancer` querying Eureka (the concrete mechanism behind Q28/Q40).
  - `predicates: - Path=/api/employees/**`: matches requests by path; Gateway supports other predicate types (Method, Header, etc.) but this project uses only `Path`.
  - `filters: - StripPrefix=1`: **what makes routing work end-to-end.** `GET /api/employees/5` resolves via `lb://employee-service` to e.g. `127.0.0.1:8081`; `StripPrefix=1` removes `/api` so the downstream request becomes `GET http://127.0.0.1:8081/employees/5`, matching what Spring Data REST exposes. Without it, the downstream service 404s.
- **Line 47** — `eureka.client.service-url.defaultZone`: points the gateway at the registry, same pattern as every other client service.
- **Line 49** — `eureka.instance.prefer-ip-address: true`: registers the instance's actual IP rather than hostname — matters in containerized/multi-host environments where hostnames may not resolve across hosts.
- **Lines 51–58** — Actuator exposure plus `endpoint.health.show-details: always`, showing full health details at `/actuator/health`.

---

## 13. Business Services: employee-service, department-service, project-service

These three services share an identical shape (pom dependency set, `@SpringBootApplication` main class with a seed-data `CommandLineRunner` bean, one or more `@Entity` classes, matching `@RepositoryRestResource`-annotated repository interfaces, and an `application.yml` differing only in port/DB name). They're explained together, calling out the differences.

### 13.1 pom.xml — shared and differing dependencies

All three (`employee-service`, `department-service`, `project-service`) declare the identical dependency set:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

There is no differentiation between the three business services' dependency sets — same starters, same scopes, only `<artifactId>` differs. They're all "flavors" of the same pattern: JPA entity + repository, auto-exposed via Spring Data REST, registered with Eureka.

- `spring-boot-starter-data-rest`: makes `@RepositoryRestResource` meaningful — auto-generates a full HAL/JSON REST controller layer from `JpaRepository` interfaces at startup, which is why none of these services has a hand-written `@RestController`.
- `spring-boot-starter-data-jpa`: standard Spring Data JPA — entity management, repository infrastructure, Hibernate as the JPA provider.
- `h2` with `<scope>runtime</scope>` (Q12): needed to *run* the app (JDBC connectivity) but not to *compile* it (no H2-specific API referenced in code) — the textbook `runtime` scope use case.
- `spring-cloud-starter-netflix-eureka-client`: registers this service under its `spring.application.name`.
- `spring-boot-starter-actuator`: health/metrics endpoints.

### 13.2 Main application classes

**`employee-service/.../EmployeeServiceApplication.java`**

```java
1:  package com.ems.employee;
2:
3:  import com.ems.employee.entity.Employee;
4:  import com.ems.employee.repository.EmployeeRepository;
5:  import org.springframework.boot.CommandLineRunner;
6:  import org.springframework.boot.SpringApplication;
7:  import org.springframework.boot.autoconfigure.SpringBootApplication;
8:  import org.springframework.context.annotation.Bean;
9:
10: @SpringBootApplication
11: public class EmployeeServiceApplication {
12:
13:     public static void main(String[] args) {
14:         SpringApplication.run(EmployeeServiceApplication.class, args);
15:     }
16:
17:     /** Seed some demo data on startup */
18:     @Bean
19:     CommandLineRunner seedData(EmployeeRepository repo) {
20:         return args -> {
21:             repo.save(new Employee("Alice", "Johnson", "alice@ems.com", 1L, 1L));
22:             repo.save(new Employee("Bob",   "Smith",   "bob@ems.com",   1L, 2L));
23:             repo.save(new Employee("Carol", "Williams","carol@ems.com", 2L, 3L));
24:             System.out.println("✅ Employee Service: seed data loaded.");
25:         };
26:     }
27: }
```

- **Line 10** — `@SpringBootApplication`, standard bootstrap; no `@EnableEurekaClient` needed — since Spring Cloud 2020+, having `spring-cloud-starter-netflix-eureka-client` on the classpath is sufficient for auto-registration.
- **Lines 18–26** — `@Bean CommandLineRunner seedData(...)`: Spring Boot runs any `CommandLineRunner` bean automatically, once, right after the context is initialized and before the app serves requests — this is what populates the in-memory H2 database on every startup (H2 here is `jdbc:h2:mem:...`, wiped on restart).
- **Method injection (`EmployeeRepository repo`, line 19)**: Spring resolves this from the context automatically — the repository bean is created by Spring Data JPA's infrastructure with no manual implementation needed.

`DepartmentServiceApplication.java` and `ProjectServiceApplication.java` follow the identical shape — `@SpringBootApplication` + a `CommandLineRunner` bean seeding their respective entities. Department's seeder saves 3 `Department` rows and 3 `Job` rows via two injected repositories (`DepartmentRepository`, `JobRepository`); Project's seeder saves 3 `Project` rows (using `java.time.LocalDate` for `startDate`/`endDate`), each carrying a `departmentId` cross-reference exactly like `Employee.departmentId`.

### 13.3 Entity classes — JPA annotations

**`Employee.java`**

```java
1:  package com.ems.employee.entity;
2:
3:  import jakarta.persistence.*;
4:
5:  @Entity
6:  @Table(name = "employees")
7:  public class Employee {
8:
9:      @Id
10:     @GeneratedValue(strategy = GenerationType.IDENTITY)
11:     private Long id;
12:
13:     private String firstName;
14:     private String lastName;
15:     private String email;
16:
17:     // Department and Job are in a separate service.
18:     // We keep only foreign-key IDs here (no JPA join across services).
19:     private Long departmentId;
20:     private Long jobId;
21:
22:     public Employee() {}
23:
24:     public Employee(String firstName, String lastName, String email,
25:                     Long departmentId, Long jobId) {
26:         this.firstName = firstName;
27:         this.lastName = lastName;
28:         this.email = email;
29:         this.departmentId = departmentId;
30:         this.jobId = jobId;
31:     }
32:
33:     // getters/setters for all fields
34: }
```

- **Line 3** — `import jakarta.persistence.*`: the **Jakarta** persistence API, not the old `javax.persistence` — expected for Spring Boot 3.x's move to the Jakarta EE 9+ namespace.
- **Line 5** — `@Entity`: marks this class as JPA-managed — Hibernate maps it to a table and manages its lifecycle.
- **Line 10** — `@GeneratedValue(strategy = GenerationType.IDENTITY)`: delegates primary key generation to the database's native auto-increment (H2's `IDENTITY` column), not an application-side sequence/table generator.
- **Lines 19–20** — `departmentId`/`jobId` as plain `Long`, **not** `@ManyToOne private Department department`: the deliberate database-per-service consequence from Section 7 — JPA relationship annotations require both sides to live in the same persistence context/database, structurally impossible since `Department`/`Job` belong to a separate H2 instance. The only way to reference another service's data is by ID, resolved via an API call if/when needed (not implemented in this project — see Section 4).

**`Department.java`** and **`Job.java`** (department-service) follow the same `@Entity`/`@Table`/`@Id`/`@GeneratedValue(IDENTITY)` shape, with plain fields (`name`, `location` for Department; `title`, `description`, `minSalary`, `maxSalary` for Job) — no relationship annotation between them, even though Job logically belongs to Department and both live in the same database (a JPA join would have been possible, but the project keeps it simple).

**`Project.java`** (project-service) — same shape, with `startDate`/`endDate` as `java.time.LocalDate` (Hibernate maps this natively to SQL `DATE`, no `@Temporal` needed), a `status` field that's a free-text value (`PLANNING`/`ACTIVE`/`COMPLETED`, not an actual Java `enum`), and `departmentId` as the same kind of cross-service FK-by-ID as `Employee.departmentId`.

### 13.4 Repository interfaces — `JpaRepository<T, ID>` and `@RepositoryRestResource`

**`EmployeeRepository.java`**

```java
1:  package com.ems.employee.repository;
2:
3:  import com.ems.employee.entity.Employee;
4:  import org.springframework.data.jpa.repository.JpaRepository;
5:  import org.springframework.data.repository.query.Param;
6:  import org.springframework.data.rest.core.annotation.RepositoryRestResource;
7:
8:  import java.util.List;
9:
10: /**
11:  * Spring Data REST automatically exposes this repository as a full
12:  * HAL/JSON REST API at /employees without any controller code.
13:  *
14:  * Auto-generated endpoints:
15:  *   GET    /employees          - list all
16:  *   GET    /employees/{id}     - get one
17:  *   POST   /employees          - create
18:  *   PUT    /employees/{id}     - replace
19:  *   PATCH  /employees/{id}     - partial update
20:  *   DELETE /employees/{id}     - delete
21:  *   GET    /employees/search/findByDepartmentId?departmentId=1
22:  */
23: @RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
24: public interface EmployeeRepository extends JpaRepository<Employee, Long> {
25:
26:     List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);
27:
28:     List<Employee> findByJobId(@Param("jobId") Long jobId);
29: }
```

- **Line 24** — `extends JpaRepository<Employee, Long>`: this single line "buys for free" the entire CRUD implementation — `save()`, `findById()`, `findAll()`, `deleteById()`, pagination, sorting — implemented by Spring Data JPA at runtime via a dynamic proxy, zero hand-written code. `Employee` is the managed entity type, `Long` its primary key type.
- **Line 23** — `@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")`: stacks Spring Data REST on top of Spring Data JPA — auto-generates an HTTP-level REST controller for the repository, mapped at `path`. `collectionResourceRel` controls the HAL `_embedded` relation name in the JSON response (responses are HAL+JSON by default, with `_links` for pagination/self-reference).
- **Lines 26, 28** — `findByDepartmentId` / `findByJobId`: **derived query methods** — Spring Data JPA parses the method name and generates the JPQL query automatically, no `@Query` needed. Because the repository is `@RepositoryRestResource`-annotated, these are *also* auto-exposed as search endpoints, e.g. `GET /employees/search/findByDepartmentId?departmentId=1`.
- **Line 5** — `@Param("departmentId")`: names the query parameter for both JPQL binding and the HTTP query-string name Spring Data REST exposes — the safer alternative to relying on `-parameters` bytecode inference.

**`DepartmentRepository.java`** and **`JobRepository.java`** (department-service) follow the identical pattern: `@RepositoryRestResource(path = "departments")` / `@RepositoryRestResource(path = "jobs")`, each with one derived-query search method (`findByName` for Department, `findByTitle` for Job).

**`ProjectRepository.java`** (project-service): `@RepositoryRestResource(path = "projects")`, `extends JpaRepository<Project, Long>`, with two derived-query methods — `findByStatus` and `findByDepartmentId` — giving `/projects/search/findByStatus?status=ACTIVE` and `/projects/search/findByDepartmentId?departmentId=1`.

### 13.5 application.yml — per-service config (all commented out, as noted)

**`employee-service/src/main/resources/application.yml`**

```yaml
1:  #server:
2:  #  port: 8081
3:  #
4:  #spring:
5:  #  application:
6:  #    name: employee-service        # This name registers in Eureka
7:  #
8:  #  datasource:
9:  #    url: jdbc:h2:mem:employeedb   # In-memory H2 database
10: #    driver-class-name: org.h2.Driver
11: #    username: sa
12: #    password:
13: #
14: #  h2:
15: #    console:
16: #      enabled: true               # Browse at http://localhost:8081/h2-console
17: #      path: /h2-console
18: #
19: #  jpa:
20: #    database-platform: org.hibernate.dialect.H2Dialect
21: #    hibernate:
22: #      ddl-auto: create-drop       # Schema created fresh every startup
23: #    show-sql: true
24: #
25: #  data:
26: #    rest:
27: #      base-path: /                # Spring Data REST serves from root
28: #
29: #eureka:
30: #  client:
31: #    service-url:
32: #      defaultZone: http://localhost:8761/eureka/
33: #  instance:
34: #    prefer-ip-address: true
35: #
36: #management:
37: #  endpoints:
38: #    web:
39: #      exposure:
40: #        include: "*"
41: #  endpoint:
42: #    health:
43: #      show-details: always
```

- **Line 2** — `server.port: 8081`: each business service needs a **distinct port**, since they run as separate OS processes on the same machine (Department/Project use 8082/8083) — Eureka is what lets the gateway/other services find each instance's actual port dynamically instead of hardcoding it.
- **Line 6** — `spring.application.name: employee-service`: the name the service registers under in Eureka — and the exact string the gateway's route `uri: lb://employee-service` (Section 12.3) must match for routing to resolve.
- **Line 9** — `jdbc:h2:mem:employeedb`: an **in-memory** H2 database scoped to this JVM process — data is lost on every restart, which is why the `CommandLineRunner` seed data exists.
- **Line 22** — `hibernate.ddl-auto: create-drop`: Hibernate creates the schema from `@Entity` classes on startup and drops it on shutdown — fine for a demo/in-memory DB, never for real persistent production data (use migrations, e.g. Flyway/Liquibase, instead).
- **Line 27** — `spring.data.rest.base-path: /`: serves auto-generated endpoints at the root path (`/employees`) rather than a prefix — why the gateway's `StripPrefix=1` filter forwards to exactly `/employees`.
- **Line 34** — `prefer-ip-address: true`: same reasoning as the gateway's identical setting — advertise a real reachable IP rather than a hostname.

**`department-service/src/main/resources/application.yml`** and **`project-service/src/main/resources/application.yml`** are structurally identical, differing only in: `server.port` (8082 for department, 8083 for project), `spring.application.name` (`department-service` / `project-service`), and `spring.datasource.url` (`jdbc:h2:mem:departmentdb` / `jdbc:h2:mem:projectdb`) — each service's own isolated in-memory database, the concrete embodiment of the database-per-service pattern (Section 7 / Q35).

---

## 14. How the Pieces Fit Together — Request Flow Synthesis

Trace a client request `GET http://localhost:8080/api/employees` (per the project's own README walkthrough) through the full running system:

1. **Startup ordering (implicit, not enforced in code):** `eureka-server` starts first and stands up the registry at port 8761 (`register-with-eureka: false`, `fetch-registry: false` — a passive registry, not a self-registering client). Then `employee-service`, `department-service`, `project-service`, and `api-gateway` each start and register themselves with Eureka under their `spring.application.name`, while also fetching/caching the current registry so they know about their peers.
2. **Client sends the request** to `http://localhost:8080/api/employees` — the API Gateway, the single public entry point (Q27) that all external clients talk to, never the business services directly.
3. **Gateway route matching:** the routing engine (auto-activated by `spring-cloud-starter-gateway` on the classpath) matches the request path against the `employee-service` route's predicate `Path=/api/employees/**`.
4. **Eureka lookup + load balancing:** the matched route's `uri: lb://employee-service` triggers `spring-cloud-starter-loadbalancer` to ask the Eureka client for healthy `employee-service` instances and resolves to that instance's `host:8081`.
5. **Filter chain applied before forwarding:** `StripPrefix=1` removes the leading `/api` segment, so the outbound request becomes `GET http://<employee-service-host>:8081/employees` — matching the path Spring Data REST exposes.
6. **employee-service handles the forwarded request:** Spring Data REST's auto-generated controller layer (no hand-written `@RestController` — see Section 13.4) delegates to `EmployeeRepository`, which runs a `SELECT * FROM employees` against the in-memory `employeedb` H2 instance, populated at startup by the seed-data bean.
7. **Response returns as HAL+JSON** back through employee-service → gateway → client, each employee record carrying only a `departmentId`/`jobId` — not the full Department/Job objects, since those live in a separate service/database and would need a separate call (e.g. `GET /api/departments/1`) to resolve, which this project doesn't implement.

This single trace ties together every concept covered above: service discovery (step 1, 4), the API Gateway as centralized entry point and router (step 2–3, 5), client-side load balancing via Eureka (step 4), Spring Data REST replacing hand-written controllers (step 6), and database-per-service with ID-based cross-service references rather than JPA joins (step 7).
