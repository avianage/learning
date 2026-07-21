# Maven — Complete Line-by-Line Guide

This guide is grounded strictly in the actual course materials: `maven-courseware.md` and `maven-projects-guide.md`, plus two real Maven projects used in the course — a single-module WAR webapp (`maven-webapp-hello`) and a three-module parent/child project (`organized-multi-module-hello` with `common`, `service`, and `api` modules). Every command, config snippet, and code walkthrough below traces back to one of these sources — nothing is invented.

---

## 1. What is Maven & Why

Maven is a **build automation and dependency management tool** for Java. The problem it solves: without it, teams pass around JARs manually, builds work "on my machine" only, and there's no standard project layout. Maven fixes all three — you declare what your project needs in `pom.xml`, and Maven fetches dependencies, compiles, tests, and packages consistently, on any machine.

Its core philosophy is **convention over configuration**: Maven expects a specific folder layout (`src/main/java`, `src/test/java`, etc.) and, if you follow it, requires very little config. Fight the convention and you fight the tool.

Compared to alternatives: **Ant** is older and fully script-based (you write every step); **Gradle** is newer, uses a Groovy/Kotlin DSL, and is common in Android; **Maven** is XML-based, opinionated, and remains the standard for Java enterprise and Spring Boot work.

**Cross-tool comparison (from the courseware FAQ):** if you know npm, you already understand Maven's job conceptually:

| | Maven (Java) | npm (Node.js) |
|---|---|---|
| Config file | `pom.xml` | `package.json` |
| Dependency store | `~/.m2/repository` | `node_modules/` |
| Install deps | `mvn install` | `npm install` |
| Run build | `mvn package` | `npm run build` |
| Registry | Maven Central | npmjs.com |

Unlike `node_modules` (per-project, duplicated everywhere), Maven's `.m2` is a single shared cache across all your projects on the machine — download a dependency once, every project reuses it.

---

## 2. Setup & Environment

Prerequisites: JDK 11+ installed, `JAVA_HOME` set, and (for this course) Eclipse 2023-06+.

Install steps: download the Maven **binary zip** from `maven.apache.org/download.cgi`, extract it (e.g. `C:\Program Files\Maven\apache-maven-3.9.x`), then set a `MAVEN_HOME` system environment variable pointing at that folder and add `%MAVEN_HOME%\bin` to `PATH`. Verify with:

```bash
mvn -version
```

which should print the Maven version, Maven home, and Java version.

Separately, the projects-guide walks through installing **Apache Tomcat** (for running the WAR project): download the ZIP from `tomcat.apache.org`, extract to e.g. `C:\tomcat\apache-tomcat-10.1.xx`, then start it via `bin\startup.bat` and confirm `http://localhost:8080` shows the Tomcat welcome page. `shutdown.bat` stops it. Java itself is verified separately with `java -version`, expecting Java 17.

**The local repository (`.m2`)** lives at `C:\Users\<YourName>\.m2\repository`. This is Maven's dependency cache — analogous to npm's global cache, not `node_modules`. **Never commit `.m2` to git** — it's a local cache, can be hundreds of MBs, and is fully reproducible from `pom.xml`, which is the single source of truth.

`settings.xml` (at `C:\Users\<YourName>\.m2\settings.xml`, optional) configures mirror/proxy repositories, custom repository URLs, and credentials for private repos — comparable to `.npmrc`.

---

## 3. POM Structure & GAV Coordinates

Every Maven project has a `pom.xml` — the **Project Object Model**, the single config file that identifies the project and drives its build (this is Maven's `package.json`/`requirements.txt` equivalent, but XML and far more central — it also encodes build behavior, not just dependency lists).

Every artifact (your project, or any library you depend on) is identified by **GAV coordinates**:

| Coordinate | Description | Example |
|---|---|---|
| `groupId` | Organization / namespace | `org.springframework` |
| `artifactId` | Project/library name | `spring-core` |
| `version` | Version | `6.1.0` |

Mnemonic from the courseware: groupId is the city, artifactId the street, version the house number — together a unique address for any artifact in a repository.

A minimal `pom.xml` declares `modelVersion` (always `4.0.0`), the GAV triple, `packaging` (defaults to `jar`), an optional `name`, and a `<dependencies>` block listing what the project needs.

**Standard directory structure** (convention over configuration in action):

```
my-project/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/          ← source code
    │   └── resources/     ← config files
    └── test/
        ├── java/          ← test classes
        └── resources/     ← test-specific config
```

Rule: source code outside `src/main/java` will not be compiled by Maven.

---

## 4. Build Lifecycle & Phases

Maven builds run through a fixed sequence of **phases**:

```
validate → compile → test → package → verify → install → deploy
```

| Phase | What it does |
|---|---|
| `validate` | Checks the project is correct |
| `compile` | Compiles `src/main/java` |
| `test` | Runs unit tests |
| `package` | Bundles into JAR/WAR |
| `install` | Copies the built artifact into your local `.m2` repository |
| `deploy` | Pushes the artifact to a remote repository |

**Key insight:** invoking a later phase automatically runs every phase before it — `mvn package` implies validate, compile, and test have already run. You never invoke each phase individually in normal use.

Commands used throughout the course:

```bash
mvn compile              # Just compile
mvn test                 # Compile + run tests
mvn package              # Compile + test + package to JAR/WAR
mvn install              # Package + install to local .m2
mvn clean                # Delete the target/ folder
mvn clean package        # Clean build — start fresh then package
mvn clean install -DskipTests   # Skip tests (fast builds during dev)
```

`mvn clean install` is called out explicitly as the command you'll type constantly — always memorize it. The FAQ distinguishes `install` from `package` precisely: `package` builds the JAR/WAR into `target/`; `install` does that *and* copies the result into `.m2` so other local projects (e.g. sibling modules) can depend on it. Use `install` whenever something else needs to consume this artifact locally; use `package` when you just want the build output.

Other reference commands from the wrap-up:

```bash
mvn dependency:tree          # Debug dependency conflicts
mvn help:effective-pom       # See the final computed pom (parent + child + defaults merged)
```

`target/` is the build output directory; it, too, should not be committed to git (implied by the same "don't commit generated artifacts" logic as `.m2`).

npm comparison, extended: `mvn package` ≈ `npm run build`; there's no exact npm equivalent of `mvn install`'s "publish to local cache for other local projects to consume," since npm workspaces/linking solve that differently.

---

## 5. Dependency Management & Scopes

By default a dependency is available at compile time, test time, and runtime, and gets packaged into the output. **Scope** narrows that.

The 6 scopes:

| Scope | Compile | Test | Runtime | Packaged | Use case |
|---|:---:|:---:|:---:|:---:|---|
| `compile` (default) | ✅ | ✅ | ✅ | ✅ | Most dependencies |
| `provided` | ✅ | ✅ | ❌ | ❌ | Servlet API — the server supplies it |
| `runtime` | ❌ | ✅ | ✅ | ✅ | JDBC drivers, logging impls |
| `test` | ❌ | ✅ | ❌ | ❌ | JUnit, Mockito |
| `system` | ✅ | ✅ | ✅ | ❌ | Local JAR files (courseware: avoid this) |
| `import` | — | — | — | — | Importing a BOM into `dependencyManagement` |

Decision guide from the courseware: "Do I need it to compile? No → try `runtime`. Does the server provide it? Yes → `provided`. Only for tests? Yes → `test`. Otherwise → `compile` (default)."

**`provided` is the scope the real `maven-webapp-hello` project actually uses** — see Section 8 below for the exact XML.

**BOM / `import` scope:** a BOM (Bill of Materials) is a special POM listing curated dependency versions. You pull it into `dependencyManagement` with `<type>pom</type><scope>import</scope>`, then declare dependencies from it without versions — exactly how `spring-boot-starter-parent` works under the hood.

**Dependency conflicts:** when two dependencies transitively need different versions of the same library, Maven resolves it with "nearest wins" (the version closest to your project in the dependency tree). Use `mvn dependency:tree` to spot conflicts, and pin an explicit version in your own `pom.xml` to override if needed.

---

## 6. Plugins

Maven does its actual work through **plugins** — every lifecycle phase is executed by one. Plugins are declared in `<build><plugins>`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Common plugins covered:

| Plugin | Purpose |
|---|---|
| `maven-compiler-plugin` | Compiles Java code (source/target Java version) |
| `maven-surefire-plugin` | Runs unit tests |
| `maven-war-plugin` | Packages WAR files |
| `maven-jar-plugin` | Packages JAR files |
| `spring-boot-maven-plugin` | Creates runnable Spring Boot JARs |
| `tomcat7-maven-plugin` | Runs a webapp directly via `mvn tomcat7:run` (Tomcat embedded during dev) |

For Spring Boot specifically, `mvn spring-boot:run` handles everything via the embedded Tomcat — no separate plugin config needed for basic run.

---

## 7. Maven Web Applications (JAR vs WAR)

Two packaging types matter here:

| Type | Used for | Runs on |
|---|---|---|
| `jar` | Standalone apps, libraries | JVM / Spring Boot embedded server |
| `war` | Traditional web apps | External servlet container (Tomcat, JBoss, GlassFish) |

Spring Boot projects mostly use `jar` (embedded Tomcat); WAR is for deploying into an external container — exactly what the real `maven-webapp-hello` project does (see Section 8).

Standard webapp structure adds a `webapp/` folder under `src/main`:

```
my-webapp/
├── pom.xml
└── src/main/
    ├── java/com/example/HelloServlet.java
    ├── resources/
    └── webapp/
        ├── WEB-INF/web.xml     ← deployment descriptor
        └── index.jsp           ← web pages
```

The `pom.xml` for a webapp sets `<packaging>war</packaging>`, adds the Servlet API dependency with `<scope>provided</scope>` (the container supplies the Servlet API at runtime — bundling it would conflict), and includes `maven-war-plugin` to actually produce the WAR.

A servlet example from the courseware:

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.getWriter().println("Hello from Maven Web App!");
    }
}
```

(Note: the real `maven-webapp-hello` project in this course does *not* use a servlet — it serves a static JSP directly. See Section 8.)

Deployment workflow taught in the projects-guide, and used to run the real webapp: `mvn clean package` → WAR appears in `target/` → copy the WAR into Tomcat's `webapps/` folder → restart Tomcat (`shutdown.bat` then `startup.bat`) → hit `http://localhost:8080/<war-name>/` in a browser. The WAR's file name (minus `.war`) becomes the context path.

---

## 8. The Real Webapp Project — `maven-webapp-hello`

### 8.1 `pom.xml`

```xml
1:  <project xmlns="http://maven.apache.org/POM/4.0.0"
2:           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
3:           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
4:           http://maven.apache.org/xsd/maven-4.0.0.xsd">
5:
6:      <modelVersion>4.0.0</modelVersion>
7:
8:      <groupId>com.demo</groupId>
9:      <artifactId>maven-webapp-hello</artifactId>
10:     <version>1.0.0</version>
11:     <packaging>war</packaging>
12:
13:     <dependencies>
14:         <dependency>
15:             <groupId>jakarta.servlet</groupId>
16:             <artifactId>jakarta.servlet-api</artifactId>
17:             <version>6.0.0</version>
18:             <scope>provided</scope>
19:         </dependency>
20:     </dependencies>
21:
22: </project>
```

- **Lines 1–4** — the `<project>` root element with the Maven POM XML namespace and schema location. This is boilerplate every `pom.xml` carries; it tells tools (Eclipse, `mvn`) which POM schema version to validate against.
- **Line 6** — `<modelVersion>4.0.0</modelVersion>`: the POM model version, always `4.0.0` for current Maven. Not your project's version.
- **Line 8** — `groupId` = `com.demo`: the namespace for this course's demo artifacts (matches the `com.demo` Java package used in the multi-module project too).
- **Line 9** — `artifactId` = `maven-webapp-hello`: the project's unique name; this also becomes the base name of the generated WAR file (`maven-webapp-hello-1.0.0.war`).
- **Line 10** — `version` = `1.0.0`: this project's version, combined into the output artifact's filename.
- **Line 11** — `<packaging>war</packaging>`: the key line that tells Maven to invoke `maven-war-plugin` at the `package` phase and produce a deployable WAR instead of a plain JAR. This is what makes `mvn clean package` produce `maven-webapp-hello-1.0.0.war` in `target/`, ready to drop into Tomcat's `webapps/`.
- **Lines 13–20** — a single dependency: the Jakarta Servlet API.
- **Line 15–16** — `jakarta.servlet:jakarta.servlet-api` version `6.0.0` — this supplies the servlet/JSP-related classes used to *compile* against (JSP compilation and any servlet code would need these types), matching Tomcat 10.1's Jakarta EE 10 API level.
- **Line 18** — `<scope>provided</scope>`: critical. Tomcat itself ships the Servlet API implementation, so this dependency must be available at compile time (line up with types) but **not** bundled into the WAR — bundling it would duplicate/conflict with the container's own copy. This is the textbook `provided` use case from Section 5's scope table, applied for real.
- **No `<build>` / plugins block** — unlike the courseware's generic webapp example (which explicitly adds `maven-war-plugin`), this real POM has none declared. Maven's **default WAR lifecycle binding** already maps `war:war` to the `package` phase whenever `packaging` is `war`, so the plugin is used implicitly with default configuration — no explicit `<plugin>` block is required for a bare-bones project like this.

### 8.2 `index.jsp`

```jsp
1:  <html>
2:  <head>
3:      <title>Hello Maven Web App</title>
4:  </head>
5:  <body>
6:      <h1>Hello World from Maven Web Application</h1>
7:  </body>
8:  </html>
```

- **Placement matters, not just content**: this file lives at `src/main/webapp/index.jsp`. `maven-war-plugin`'s default configuration treats everything under `src/main/webapp/` as the WAR's web root — so this file is copied straight into the WAR root as `index.jsp`, which servlet containers serve as the default welcome file.
- **Lines 1–8** — plain static HTML with no JSP scriptlets (`<% %>`) or expressions (`<%= %>`) at all; despite the `.jsp` extension, this file behaves as static HTML. It's the minimal possible page proving the WAR deployed correctly.
- **Line 6** — the exact string the projects-guide's "expected output" describes after deployment: browsing to `http://localhost:8080/maven-webapp-hello-1.0.0/` should show "Hello World from Maven Web Application", confirming the deploy pipeline (`mvn clean package` → copy WAR → restart Tomcat) worked end to end.

**Mechanic demonstrated:** `packaging: war` + `provided`-scope Servlet API + convention-based `src/main/webapp` — the complete, minimal recipe for a deployable Java web app with zero custom plugin configuration.

---

## 9. Multi-Module Projects

### 9.1 Why split into modules

Real applications are split into modules rather than one monolith, e.g.:

```
ecommerce-app/
├── ecommerce-common/      ← shared models/utilities
├── ecommerce-data/        ← DB layer
├── ecommerce-service/     ← business logic
└── ecommerce-web/         ← REST/web layer
```

Benefits: build only what changed, teams can own individual modules, independent versioning is possible, and modules can be reused across projects. The recommended dependency flow is **one direction only, no cycles**: `api → service → domain/data → common`.

### 9.2 The parent POM

The parent `pom.xml` ties modules together and must declare `<packaging>pom</packaging>` (not `jar` or `war` — a parent POM produces no artifact of its own, it's purely an aggregator/config holder). It lists child folders under `<modules>`:

```xml
<modules>
    <module>ecommerce-common</module>
    <module>ecommerce-data</module>
    <module>ecommerce-service</module>
    <module>ecommerce-web</module>
</modules>
```

In Eclipse: create the parent as a simple Maven project with packaging `pom`, then right-click → New → Other → Maven Module for each child; Eclipse wires the `<parent>` reference automatically.

Build from the parent directory:

```bash
mvn clean install                            # builds ALL modules, in dependency order
mvn clean install -pl ecommerce-common       # build just one module
mvn clean install -pl ecommerce-web -am      # build a module + everything it depends on
```

### 9.3 Organizing a multi-module project

**`dependencyManagement`** (parent-declared, child-consumed) centralizes versions so they aren't repeated in every child:

```xml
<!-- PARENT -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```xml
<!-- CHILD — no version needed, inherited -->
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

Rule of thumb: declare versions once in the parent's `dependencyManagement`; children just reference groupId/artifactId. **`pluginManagement`** does the identical thing for build plugins (e.g. centralizing `maven-compiler-plugin`'s `source`/`target` config so every child inherits it). **Properties** (e.g. `<java.version>17</java.version>`, `<spring.version>...</spring.version>`) avoid hardcoding versions inline and are referenced via `${property.name}`.

**Inter-module dependencies**: a child module depends on a sibling module exactly like any other Maven dependency — using its GAV coordinates:

```xml
<!-- in ecommerce-web/pom.xml -->
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>ecommerce-service</artifactId>
    <version>${project.version}</version>
</dependency>
```

Maven computes the correct build order automatically from these inter-module dependency edges — it will always build `ecommerce-service` before `ecommerce-web` if `web` depends on `service`.

---

## 10. The Real Multi-Module Project — `organized-multi-module-hello`

This is a concrete, working instance of everything in Section 9: a parent POM aggregating three modules — `common` (no dependencies), `service` (depends on `common`), and `api` (depends on `service`) — demonstrating the `api → service → common` one-directional dependency chain the courseware recommends.

### 10.1 Parent `pom.xml`

```xml
1:  <project xmlns="http://maven.apache.org/POM/4.0.0"
2:           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
3:           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
4:           http://maven.apache.org/xsd/maven-4.0.0.xsd">
5:
6:      <modelVersion>4.0.0</modelVersion>
7:
8:      <groupId>com.demo</groupId>
9:      <artifactId>organized-multi-module-hello</artifactId>
10:     <version>1.0.0</version>
11:     <packaging>pom</packaging>
12:
13:     <properties>
14:         <java.version>17</java.version>
15:     </properties>
16:
17:     <modules>
18:         <module>common</module>
19:         <module>service</module>
20:         <module>api</module>
21:     </modules>
22:
23: </project>
```

- **Line 8** — `groupId` = `com.demo`: shared by every module in this project (parent and all three children use this same groupId — see child POMs below), and matches the Java package prefix `com.demo.*` used throughout the source (`com.demo.api`, `com.demo.common`, `com.demo.service`).
- **Line 9** — `artifactId` = `organized-multi-module-hello`: the parent/aggregator project's own identity — distinct from any child's artifactId.
- **Line 11** — `<packaging>pom</packaging>`: required for any aggregator POM. This POM builds no JAR/WAR itself; `mvn` treats it purely as a container that (a) lists modules to build and (b) can supply shared config/dependency management to children via `<parent>` inheritance.
- **Lines 13–15** — a `<properties>` block defining `java.version = 17`. Declared but note: it is *not* actually wired into a `maven-compiler-plugin` configuration anywhere in this parent (no `<build>`/`<pluginManagement>` block exists) — in this minimal course project it's illustrative/reserved rather than actively enforced, unlike the courseware's generic example which does feed such properties into `dependencyManagement`/plugin config.
- **Lines 17–21** — the `<modules>` list, naming three sibling directories: `common`, `service`, `api`, **in that order**. This order is what the projects-guide refers to when it says modules "build in sequence: `common → service → api`" — Maven's reactor actually computes build order from the dependency graph (not listing order), but here the listing order and the true dependency order happen to coincide, since `service` depends on `common` and `api` depends on `service`.
- **No `<dependencyManagement>` or `<pluginManagement>`** — unlike the courseware's fuller "Organizing the Multi Module Project" example (Section 9.3), this real parent is intentionally minimal: it only aggregates modules and shares a groupId/version, it does not centralize dependency versions. Each child instead hardcodes `<version>1.0.0</version>` when referencing a sibling (see below) rather than using `${project.version}`.

### 10.2 Child `common/pom.xml`

```xml
1:  <project xmlns="http://maven.apache.org/POM/4.0.0">
2:
3:      <modelVersion>4.0.0</modelVersion>
4:
5:      <parent>
6:          <groupId>com.demo</groupId>
7:          <artifactId>organized-multi-module-hello</artifactId>
8:          <version>1.0.0</version>
9:      </parent>
10:
11:     <artifactId>common</artifactId>
12:
13: </project>
```

- **Lines 5–9** — the `<parent>` block: this is the mechanism of **POM inheritance**. By pointing at `groupId=com.demo`, `artifactId=organized-multi-module-hello`, `version=1.0.0`, this child inherits the parent's `groupId` and `version` automatically — that's why `common`'s own POM never redeclares them.
- **Line 11** — `<artifactId>common</artifactId>` is the *only* identity field this child needs to declare; combined with the inherited groupId/version, its full GAV becomes `com.demo:common:1.0.0`.
- **No explicit `<packaging>`** — omitted means it defaults to `jar`. `common` is a pure library module: no dependencies of its own, sitting at the bottom of the dependency chain, exactly matching the courseware's guidance that a `-common` module should have "no deps on other modules."
- **No `<dependencies>` block** — confirms `common` is a leaf module other modules build on, not one that consumes anything itself.

### 10.3 `common`'s Java source — `CommonMessage.java`

```java
1:  package com.demo.common;
2:
3:  public class CommonMessage {
4:
5:      public static String getMessage() {
6:          return "Hello from Common";
7:      }
8:  }
```

- **Line 1** — package `com.demo.common`, placed physically at `common/src/main/java/com/demo/common/CommonMessage.java` per Maven's standard source layout (Section 3) — the package path must mirror the directory path under `src/main/java`.
- **Lines 5–7** — a single static method, `getMessage()`, returning the literal string `"Hello from Common"`. This is the base value that gets built up by the `service` and `api` layers (see below) — a minimal but real demonstration of one module's code being consumed by another module purely through a compiled dependency, not source copy-paste.

### 10.4 Child `service/pom.xml`

```xml
1:  <project xmlns="http://maven.apache.org/POM/4.0.0">
2:
3:      <modelVersion>4.0.0</modelVersion>
4:
5:      <parent>
6:          <groupId>com.demo</groupId>
7:          <artifactId>organized-multi-module-hello</artifactId>
8:          <version>1.0.0</version>
9:      </parent>
10:
11:     <artifactId>service</artifactId>
12:
13:     <dependencies>
14:         <dependency>
15:             <groupId>com.demo</groupId>
16:             <artifactId>common</artifactId>
17:             <version>1.0.0</version>
18:         </dependency>
19:     </dependencies>
20:
21: </project>
```

- **Lines 5–9, 11** — same parent-inheritance pattern as `common`: full GAV resolves to `com.demo:service:1.0.0`.
- **Lines 13–19** — this is the concrete instance of **inter-module dependency** described in Section 9.3: `service` declares a normal `<dependency>` on `common`, using `common`'s GAV coordinates (`com.demo:common:1.0.0`) exactly as if `common` were any third-party library. This is what makes `com.demo.service.ServiceMessage` (below) able to `import com.demo.common.CommonMessage`.
- **Line 17** — the version is hardcoded (`1.0.0`) rather than referenced via `${project.version}` as the courseware's cleaner example does — a simplification appropriate for this minimal course project, but worth knowing the more maintainable pattern for real multi-module apps.
- Because of this `<dependency>` on `common`, Maven's reactor build (`mvn clean install` from the parent) must compile and install `common` **before** attempting `service` — this is the real mechanism behind the projects-guide's stated build order `common → service → api`, not just the `<modules>` listing order.

### 10.5 `service`'s Java source — `ServiceMessage.java`

```java
1:  package com.demo.service;
2:
3:  import com.demo.common.CommonMessage;
4:
5:  public class ServiceMessage {
6:
7:      public static String getServiceMessage() {
8:          return CommonMessage.getMessage() + " -> Service Layer";
9:      }
10: }
```

- **Line 3** — `import com.demo.common.CommonMessage;`: this import only *compiles* because `service/pom.xml` declares `common` as a Maven dependency (Section 10.4). Without that `<dependency>` block, this import would fail to resolve — the clearest possible illustration of how POM-level dependencies enable cross-module Java compilation.
- **Lines 7–9** — `getServiceMessage()` calls `CommonMessage.getMessage()` (from the `common` module's compiled classes) and appends `" -> Service Layer"`, producing `"Hello from Common -> Service Layer"`. This chained string is exactly the output the projects-guide documents as the expected console output when running the whole app.

### 10.6 Child `api/pom.xml`

```xml
1:  <project xmlns="http://maven.apache.org/POM/4.0.0">
2:
3:      <modelVersion>4.0.0</modelVersion>
4:
5:      <parent>
6:          <groupId>com.demo</groupId>
7:          <artifactId>organized-multi-module-hello</artifactId>
8:          <version>1.0.0</version>
9:      </parent>
10:
11:     <artifactId>api</artifactId>
12:
13:     <dependencies>
14:         <dependency>
15:             <groupId>com.demo</groupId>
16:             <artifactId>service</artifactId>
17:             <version>1.0.0</version>
18:         </dependency>
19:     </dependencies>
20:
21: </project>
```

- Same parent-inheritance and structure as `service`, but this time the dependency (lines 13–19) points to `service` (GAV `com.demo:service:1.0.0`) — one layer up the chain. `api` does **not** declare a direct dependency on `common`; it reaches `common`'s functionality only *transitively*, through `service`. This is Maven's **transitive dependency resolution** in action: because `service` depends on `common`, and `api` depends on `service`, `api`'s classpath transitively includes `common` too — confirmed by `App.java` compiling successfully even though it never imports `com.demo.common` directly.
- Full GAV: `com.demo:api:1.0.0`. This module is the runnable entry point of the whole project.

### 10.7 `api`'s Java source — `App.java`

```java
1:  package com.demo.api;
2:
3:  import com.demo.service.ServiceMessage;
4:
5:  public class App {
6:
7:      public static void main(String[] args) {
8:          System.out.println(ServiceMessage.getServiceMessage());
9:      }
10: }
```

- **Line 3** — imports `ServiceMessage` from the `service` module, enabled by the `<dependency>` declared in `api/pom.xml` (Section 10.6).
- **Lines 7–8** — `main()` calls `ServiceMessage.getServiceMessage()`, which internally calls into `CommonMessage.getMessage()` (Section 10.5) — so a single `System.out.println` call here triggers execution across all three modules: `api → service → common`. The printed result is `"Hello from Common -> Service Layer"`, matching the projects-guide's documented expected output for both the command-line run and the Eclipse "Run As → Java Application" flow.

### 10.8 Running the built project

Per the projects-guide, after `mvn clean package` builds all three modules (in dependency order: `common`, then `service`, then `api`), the compiled classes are run directly with the JVM, manually assembling a classpath across all three modules' `target/classes` directories:

```bash
java -cp "common\target\classes;service\target\classes;api\target\classes" com.demo.api.App
```

This is notable specifically because it does **not** use a Maven-generated runnable/uber JAR (no `maven-assembly-plugin` or `spring-boot-maven-plugin` shading here) — it relies on Maven only to compile each module into `target/classes`, and then wires the multi-module classpath manually via the OS path separator (`;` on Windows). Expected console output: `Hello from Common -> Service Layer`.

---

## 11. Profiles

Profiles let one `pom.xml` support multiple environment-specific configurations (dev/staging/prod) without maintaining separate POM files — analogous to different `npm` script targets driven by `NODE_ENV`, but built into Maven's model.

Defined under `<profiles>`, each with an `<id>`, optional `<activation>`, and its own `<properties>`/`<dependencies>`:

```xml
<profile>
    <id>dev</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <db.url>jdbc:h2:mem:devdb</db.url>
        <log.level>DEBUG</log.level>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</profile>
```

Activate from the command line with `-P`:

```bash
mvn clean install -P prod          # activate prod
mvn clean install -P dev,test      # activate multiple
mvn clean install -P !prod         # deactivate one
```

In Eclipse: Run As → Maven build..., fill the **Profiles** field. Profiles can also auto-activate by OS (`<os><name>...</name></os>`), JDK version (`<jdk>17</jdk>`), or environment variable (`<property><name>env.BUILD_ENV</name><value>production</value></property>`).

**Resource filtering** lets profile properties get substituted into resource files at build time: placeholders like `db.url=${db.url}` in `src/main/resources/application.properties`, combined with `<filtering>true</filtering>` on the `<resource>` entry in `<build><resources>`, cause Maven to replace `${db.url}` with the active profile's value during the build.

Best practices called out: make `dev` the default (`activeByDefault`), never hardcode credentials in `pom.xml`, keep per-profile differences minimal, and don't use profiles as a substitute for proper externalized app config (e.g., prefer Spring's `application-{env}.properties` for actual application settings).

---

## 12. Maven in Eclipse

Eclipse ships with **M2Eclipse (m2e)** built in — no separate plugin install needed.

- **New project**: File → New → Maven Project. Check "Create a simple project (skip archetype selection)" for a blank project, or uncheck it to pick an **archetype** (a project template) — `maven-archetype-quickstart` for a plain Java project, `maven-archetype-webapp` for a WAR-producing web app, `maven-archetype-j2ee-simple` for an enterprise skeleton.
- **Multi-module in Eclipse**: create the parent as a simple project with packaging `pom`, then right-click → New → Other → Maven Module for each child; Eclipse wires up the `<parent>` linkage automatically.
- **Add dependencies**: either edit `pom.xml`'s XML directly (recommended, per the courseware, to build XML fluency) or use the Dependencies tab's Add... search UI. After adding, **right-click project → Maven → Update Project** (or `Alt+F5`) to force Eclipse to actually download the new JARs and refresh its classpath.
- **Import an existing project**: File → Import → Maven → Existing Maven Projects, browse to the folder with `pom.xml`, Finish — Maven auto-downloads all dependencies (this is exactly the workflow used to bring both `maven-webapp-hello` and `organized-multi-module-hello` into Eclipse per the projects-guide).
- **Run goals**: right-click project → Run As → "Maven build..." (type a goal like `clean install`), or the shortcuts "Maven install"/"Maven clean". For the webapp, the projects-guide uses Run As → Maven build with goals `clean package`, then copies the resulting WAR into Tomcat's `webapps/` and restarts Tomcat exactly as in the command-line flow.
- **Run a plain Java class**: for the multi-module project, right-click `api/src/main/java/com/demo/api/App.java` → Run As → Java Application; Eclipse resolves the module classpath automatically (no manual `-cp` string needed, unlike the command-line flow in Section 10.8).
- **Common fixes**: red errors after adding a dependency → Maven → Update Project (`Alt+F5`); dependency not downloading → check internet or run `mvn dependency:resolve`; `pom.xml` XML errors → validate via the Overview tab; missing source attachment → right-click dependency in Build Path → Download Sources.

---

## 13. Common Problems & Fixes (from the projects-guide)

| Problem | Fix |
|---|---|
| `mvn` command not found | `MAVEN_HOME`/`PATH` not configured correctly |
| Port 8080 already in use | Change Tomcat's port |
| Eclipse shows red errors | Maven → Update Project |
| WAR won't open in browser | Verify the URL matches the WAR's artifact name/version |
| 404 error | Verify the WAR was actually copied into `webapps/` |
| Tomcat won't start | Verify Java is installed correctly |

---

## 14. Repositories: Local, Central, Remote

Maven resolves dependencies through a layered repository system, as described across both source docs:

- **Local repository (`.m2`)** — `C:\Users\<YourName>\.m2\repository`: your machine's cache. Maven checks here first before going anywhere else.
- **Central repository** — Maven Central (the implicit default remote), where most public artifacts (Spring, Jackson, JUnit, etc.) live. Not downloaded until first requested.
- **Remote/mirror repositories** — configurable in `.m2\settings.xml`, typically used in corporate environments to proxy Maven Central through an internal server, or to host private/internal artifacts with credentials.

Flow: Maven looks in `.m2` local cache first; if the artifact isn't there, it resolves it from Central (or a configured mirror/remote), downloads it once, and caches it locally for every future build — mirroring npm's local-vs-registry cache model, but Maven's cache is shared machine-wide rather than per-project.

---

## 15. Wrap-Up Reference

Quick-reference summary as given in the courseware:

```
GAV          = groupId : artifactId : version
pom.xml      = project config file
.m2/         = local repository (cache)
target/      = build output folder

Lifecycle:   validate → compile → test → package → install → deploy

Scopes:      compile | provided | runtime | test | import

Commands:    mvn clean install     ← builds everything
             mvn -P <profile>      ← use a profile
             mvn -pl <module>      ← build one module
             mvn -DskipTests       ← skip tests
             mvn dependency:tree   ← see all deps
```

Where this connects next, per the courseware: **Spring Boot** projects use `spring-boot-starter-parent` as their parent POM and `spring-boot-maven-plugin` for packaging (the exact same `<parent>` inheritance mechanism demonstrated for real in Section 10); **testing** uses JUnit 5/Mockito at `test` scope, run via `mvn test` in CI; **CI/CD** pipelines (Jenkins/GitHub Actions) run `mvn clean install` on every push; **Docker** images `COPY` the Maven-built JAR/WAR in during image build.

---

## 16. Concrete Maven Mechanics Demonstrated in This Repo

Summarizing what's *actually* observable in the two real projects (not just courseware theory):

- **Parent POM inheritance** — `organized-multi-module-hello/pom.xml` (packaging `pom`) is referenced via `<parent>` in all three children (`common`, `service`, `api`), each of which inherits `groupId=com.demo` and `version=1.0.0` without redeclaring them.
- **Module aggregation** — the parent's `<modules>` list (`common`, `service`, `api`) causes a single `mvn clean install`/`package` from the parent directory to build all three in the correct dependency order.
- **Inter-module dependencies** — `service` depends on `common`; `api` depends on `service`; neither declares the transitively-available module directly (`api` never depends on `common` explicitly, yet uses its functionality transitively through `service`).
- **Packaging types in use** — `pom` (the aggregator), default `jar` (all three child modules, implicitly, since none declare `<packaging>`), and `war` (`maven-webapp-hello`, explicitly, to produce a Tomcat-deployable artifact).
- **`provided` scope in a real POM** — `maven-webapp-hello` depends on `jakarta.servlet-api` as `provided`, so it compiles against Servlet/JSP types but the container (Tomcat) supplies the actual implementation at runtime; the API is not bundled into the WAR.
- **Convention-based web root** — `maven-webapp-hello`'s `src/main/webapp/index.jsp` is picked up automatically as the WAR's root content with zero explicit `maven-war-plugin` configuration, relying entirely on the WAR packaging's default lifecycle bindings.
- **Manual multi-module classpath execution** — running the built multi-module app does not use a Maven-built runnable JAR; it's `java -cp "common\target\classes;service\target\classes;api\target\classes" com.demo.api.App`, showing Maven's role stops at compiling each module — assembling a runnable artifact (shading, `assembly:single`, Spring Boot repackaging, etc.) is a separate, not-yet-introduced step in this course.
