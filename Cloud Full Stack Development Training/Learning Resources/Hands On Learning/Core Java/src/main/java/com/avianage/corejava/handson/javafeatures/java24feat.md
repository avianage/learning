# Module 32 — Java 24 Features (Condensed)

> Part I: Modern Java · Prerequisites: Module 31 · Requires JDK 24+

## About Java 24
Non-LTS, stepping stone to Java 25 LTS. Theme: **Project Loom + Valhalla maturing** — concurrency easier/safer, smarter type system.

| Feature | JEP | Status |
|---|---|---|
| Primitive Types in Patterns | 488 | 2nd Preview |
| Flexible Constructor Bodies | 492 | 3rd Preview |
| Structured Concurrency | 499 | 4th Preview |
| Scoped Values | 487 | 4th Preview |
| Stream Gatherers | 485 | **Standard** |
| Class-File API | 484 | Standard |
| AOT Class Loading | 483 | **Standard** |
| Remove Finalization | 421 | **Finalized** |
| Quantum-Resistant Crypto | 496, 497 | Standard |

---

## 1. Primitive Types in Patterns (Preview)
Extends `instanceof`/`switch` pattern matching to primitives directly — no more autoboxing detour.

```java
if (value instanceof int i) { ... }             // direct primitive match

long bigNumber = 75000L;
if (bigNumber instanceof int i) { ... }           // narrows if it fits in int range

double salary = 75000.0;
if (salary instanceof int i) { ... }              // narrows if it's a whole number that fits
```
**In switch:**
```java
String band = switch (salaryObj) {
    case Integer i when i > 100000 -> "Executive";
    case Double d when d > 80000   -> "Senior (Double)";
    case null                       -> "No salary";
    default                         -> "Unknown type";
};
```
Removes boxing/unboxing overhead and inconsistency between primitives and reference types in patterns.

---

## 2. Stream Gatherers (Standard, JEP 485)
Custom intermediate stream operations — previously impossible to extend `filter`/`map`/`sorted` cleanly.

A `Gatherer` has 4 optional parts: **initializer** (state), **integrator** (per-element), **combiner** (merge for parallel), **finisher** (final output).

**Built-in gatherers:**
```java
employees.stream().gather(Gatherers.windowFixed(2)).forEach(batch -> ...);     // non-overlapping groups of 2
employees.stream().map(Employee::getSalary).gather(Gatherers.windowSliding(3)).forEach(...);  // overlapping windows
employees.stream().gather(Gatherers.scan(() -> 0.0, (acc, s) -> acc + s)).forEach(...);  // running accumulation
// Gatherers.fold(...)              — like reduce, but emits intermediates
// Gatherers.mapConcurrent(n, fn)    — parallel map with concurrency limit
```

**Custom gatherer:**
```java
public static <T> Gatherer<T, ?, T> firstN(int n, Predicate<T> pred) {
    return Gatherer.ofSequential(
        () -> new int[]{0},                          // initializer: state
        (state, element, downstream) -> {              // integrator
            if (pred.test(element) && state[0] < n) { state[0]++; return downstream.push(element); }
            return !downstream.isRejecting();
        });
}
employees.stream().gather(firstN(2, e -> e.getSalary() > 70000)).forEach(...);
```

---

## 3. Structured Concurrency — Maturing (4th Preview, JEP 499)
Same core model as Java 21 (Module 31), API refined:
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var eng = scope.fork(() -> fetchDeptPayroll("Engineering"));
    var fin = scope.fork(() -> fetchDeptPayroll("Finance"));
    scope.join().throwIfFailed();          // chained — cleaner than Java 21
    double total = eng.get().total() + fin.get().total();
}
```

---

## 4. Scoped Values (4th Preview, JEP 487)
Alternative to `ThreadLocal`, designed for virtual threads + structured concurrency. **Immutable within scope** — eliminates a class of bugs.

| Aspect | `ThreadLocal` | `ScopedValue` |
|---|---|---|
| Mutability | Mutable, `set()` any time | Immutable within scope |
| Virtual-thread friendly | Works but costly | Purpose-built |
| Inheritance | Explicit `InheritableThreadLocal` | Automatic in structured concurrency |
| Lifecycle | Manual `remove()` | Auto-cleaned at scope boundary |

```java
static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();

public static void processRequest(String user, String requestId) {
    ScopedValue.where(CURRENT_USER, user).run(() -> {
        // bound only within this block
        validateEmployee();   // reads CURRENT_USER.get()
    });
    // unbound here
}
```
With millions of concurrent virtual threads, scoped values are far cheaper than `ThreadLocal` — no mutable state, no manual cleanup, auto-propagates through structured scopes.

---

## 5. Ahead-of-Time Class Loading (Standard, JEP 483)
JVM caches class-loading work from a training run, replays it at startup — cuts warm-up time significantly (e.g. Spring Boot apps: 3-5s → under 1s), no code changes needed.
```bash
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.ems.Main   # training run
java -XX:AOTMode=on -XX:AOTConfiguration=app.aotconf -cp app.jar com.ems.Main         # production run
```

## 6. Finalization Removed (Done, JEP 421)
`Object.finalize()` and all related infra fully removed in Java 24 (compile warnings since Java 18). Use `AutoCloseable` + try-with-resources instead (Module 27).

## 7. Quantum-Resistant Cryptography (Standard, JEP 496/497)
NIST-standardized post-quantum algorithms, integrated into `java.security`:
```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM-768");   // key exchange
KeyPair keyPair = kpg.generateKeyPair();

Signature sig = Signature.getInstance("ML-DSA-65");                    // digital signatures
sig.initSign(sigKeyPair.getPrivate());
sig.update(data); byte[] signature = sig.sign();
```

## 8. Flexible Constructor Bodies (3rd Preview, JEP 492)
Pre-24: all statements had to come **after** `super()`/`this()` — couldn't validate/prepare args first.
```java
// Java 24 — statements allowed BEFORE super(), as long as `this` isn't touched
public Manager(int id, String name, double salary, String department, int teamSize) {
    if (teamSize <= 0) throw new IllegalArgumentException("Team size must be positive: " + teamSize);
    super(id, name, salary, department);   // now runs after validation
    this.teamSize = teamSize;
}
```
Also useful for transforming args before delegating: `var name = rawName == null ? "Unknown" : rawName.strip(); super(id, name, ...);`

---

## Practical Pattern — Gatherers + Scoped Values Together
```java
record PayslipEntry(String name, double gross, double tax, double net) {
    PayslipEntry { if (gross < 0) throw new IllegalArgumentException("Gross cannot be negative."); }
}

static final ScopedValue<String> APPROVER = ScopedValue.newInstance();

static void processPayrollRun(List<Employee> employees, String approver) {
    ScopedValue.where(APPROVER, approver).run(() -> {
        List<PayslipEntry> payslips = generatePayslips(employees);
        payslips.stream()
            .gather(Gatherers.windowFixed(2))            // batch printing
            .forEach(batch -> batch.forEach(p -> System.out.println(p)));
        System.out.println("Approved by: " + APPROVER.get());
    });
}
```

---

## Quick Reference

| Feature | Status | Key Point |
|---|---|---|
| Primitive types in patterns | Preview | `instanceof int i` — direct primitive matching |
| Stream Gatherers | **Standard** | Custom intermediate ops — `windowFixed`, `windowSliding`, `scan` |
| Structured Concurrency | Preview | Task groups — failure cancels siblings |
| Scoped Values | Preview | Immutable `ThreadLocal` replacement for virtual threads |
| AOT Class Loading | **Standard** | Faster startup via cached class-loading replay |
| `finalize()` removed | **Done** | Fully gone — use `AutoCloseable` |
| Quantum cryptography | **Standard** | ML-KEM (key exchange), ML-DSA (signatures) |
| Flexible constructors | Preview | Statements allowed before `super()` |

---

## The Modern Java Journey — Full Arc

```
Java 8     Lambdas, Streams, Optional, default methods    — Modules 19–22
Java 11    String/Files API, HttpClient, var in lambdas    — Module 28
Java 13/14 Text blocks, switch expressions, helpful NPE    — Module 29
Java 17    Records, sealed classes, pattern matching        — Module 30
Java 21    Virtual threads, sequenced collections            — Module 31
Java 24    Gatherers, scoped values, primitive patterns       — Module 32
```
**Java 21 LTS** = production-ready baseline for most teams. Java 24 features suit greenfield/cutting-edge work. The core skills from Modules 01–27 remain the foundation everything else builds on.