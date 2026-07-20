# Module 27 — Garbage Collection and Object Lifecycle (Condensed)

> Part H: Memory · Prerequisites: Module 02 (JVM Architecture)

## The Object Lifecycle
```
1. Created  — new Employee(...) → allocated on heap, constructor runs
2. In Use    — reachable via at least one live reference
3. Eligible  — no live references remain → GC may collect
4. Collected — memory reclaimed
```
GC handles stages 3–4 automatically; your job is to stop holding references longer than needed.

---

## 1. GC Roots — Where Reachability Starts
GC traces from **roots** (always live): local variables/params in active stack frames, static fields, JNI references, active threads. Anything reachable from a root is kept; everything else is eligible.

```java
public void processPayroll() {
    Employee e = new Employee(101, "Sonu", 75000, "Engineering");   // GC root: local var e
    calculate(e);
}   // method returns, e out of scope → object eligible for GC
```

## 2. When Objects Become Eligible

```java
e = null;                          // nulled reference
{ Employee temp = new Employee(...); }   // out of scope after block
e = new Employee(...);              // reassigned — old object now unreachable
```

**Island of isolation** — circular references with no external root reference are still eligible; Java's reachability-based GC handles cycles correctly (unlike reference-counting GCs):
```java
Node a = new Node(); Node b = new Node();
a.next = b; b.next = a;   // circular
a = null; b = null;        // neither reachable from any root → both eligible
```

---

## 3. Heap Regions and GC Cycles
```
Heap
├── Young Generation: Eden (new objects) → Survivor S0/S1 (survived cycles)
└── Old Generation: long-lived objects
```

**Minor GC** (Young Gen) — frequent, fast: Eden fills → live objects copied to survivor space → repeated-survivors promoted to Old Gen → dead objects discarded. Most objects die young, so this reclaims most memory cheaply.

**Major/Full GC** (Old Gen) — less frequent, slower, can Stop-The-World (pause all app threads) — this is the pause you notice in production.

---

## 4. Reference Types

| Type | Collected When | Use Case |
|---|---|---|
| Strong (default) | Never, while referenced | Everything normal |
| `SoftReference` | Under memory pressure | Memory-sensitive caches |
| `WeakReference` | Next GC cycle, once no strong refs | Short-lived caches, `WeakHashMap` |
| `PhantomReference` | After finalization, `get()` always null | Post-GC cleanup w/ `ReferenceQueue` |

```java
SoftReference<Employee> softRef = new SoftReference<>(new Employee(...));
Employee e = softRef.get();   // null if GC'd under memory pressure

WeakReference<Employee> weakRef = new WeakReference<>(new Employee(...));
// collected at next GC cycle regardless of memory

ReferenceQueue<Employee> queue = new ReferenceQueue<>();
PhantomReference<Employee> phantomRef = new PhantomReference<>(new Employee(...), queue);
// phantomRef.get() always null — used for cleanup tracking, replaced finalize()
```
`WeakHashMap` auto-removes entries once the key has no strong references — good for caches keyed on objects you don't own.

---

## 5. GC Algorithms

| Algorithm | Behavior | Best For |
|---|---|---|
| Serial GC | Single-threaded, Stop-The-World | Small, single-core apps |
| Parallel GC (Java 8 default) | Multi-threaded, still STW | Batch processing — throughput over latency |
| G1 (Java 9+ default) | Region-based, collects garbage-heaviest regions first | General-purpose production — good balance |
| ZGC (Java 15+) | Concurrent, sub-1ms pauses | Latency-sensitive apps |
| Shenandoah | Similar to ZGC, ultra-low pauses | OpenJDK, latency-sensitive |

```bash
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 MyApp
java -XX:+UseZGC MyApp
```

---

## 6. Common JVM Tuning Flags
```bash
-Xms512m                    # initial heap size
-Xmx4g                       # max heap size
-XX:+UseG1GC / -XX:+UseZGC   # GC selection
-XX:MaxGCPauseMillis=200      # G1 target pause
-Xlog:gc*                      # Java 9+ GC logging
-XX:MaxMetaspaceSize=256m      # class metadata (replaced PermGen, Java 8+)
```
As a developer you rarely tune GC directly — understand these to read production configs and diagnose memory issues.

---

## 7. Memory Leaks in Java
GC only collects **unreachable** objects — reachable-but-unused objects leak.

**Common causes:**
```java
// 1. Static collections that grow forever — static field = GC root, always reachable
private static final Map<Integer, Employee> cache = new HashMap<>();  // never removed

// 2. Listeners not unregistered — event bus holds a strong reference indefinitely
eventBus.register(employeeListener);   // forgot eventBus.unregister(...)

// 3. Long-lived object holding a short-lived one
public class ReportGenerator {
    private Employee lastEmployee;    // keeps Employee alive as long as ReportGenerator lives
}
```
**Detecting leaks:** heap profilers — VisualVM, YourKit, Java Mission Control — check heap histograms and retention trees over time.

---

## 8. `System.gc()` — Don't Use It
```java
System.gc();   // just a HINT — JVM may ignore it
```
Can trigger a Full GC at the wrong time (long pause), gives false confidence, interferes with GC heuristics. Never call in application code — only acceptable in benchmarking/testing.

## 9. `finalize()` — Deprecated (Java 9), Removed (Java 18)
```java
// Do NOT use
protected void finalize() { ... }   // may never run, run late, or cause resurrection
```
**Modern replacement:** `AutoCloseable` + try-with-resources:
```java
public class DatabaseConnection implements AutoCloseable {
    @Override public void close() { System.out.println("Connection closed."); }
}
try (DatabaseConnection conn = new DatabaseConnection()) {
    conn.query("SELECT * FROM employees");
}   // close() guaranteed, immediate, deterministic
```

---

## Practical Advice

| Situation | What To Do |
|---|---|
| Long-lived cache | `SoftReference` or bounded cache with eviction |
| Event listeners | Always unregister when done |
| Large object no longer needed, long-lived scope | Null the reference explicitly |
| Connection/stream/resource | Always `try-with-resources` |
| Suspected memory leak | Profile with VisualVM — heap histogram over time |
| Slow GC pauses | Switch Parallel → G1 or ZGC |
| `OutOfMemoryError: Java heap space` | Increase `-Xmx`, then profile for leaks |
| `OutOfMemoryError: Metaspace` | Too many classes — common with dynamic class generation |

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Eligible for GC | No live references — nulled, out of scope, or unreachable cycle |
| Strong reference | Never GC'd while held |
| Soft reference | GC'd under memory pressure — caches |
| Weak reference | GC'd next cycle — short-lived caches |
| Phantom reference | Post-finalization cleanup |
| Minor GC | Young gen — fast, frequent |
| Major/Full GC | Old gen — slower, less frequent, can STW |
| G1 GC | Default Java 9+ — good general-purpose choice |
| ZGC | Sub-millisecond pauses — latency-sensitive |
| Memory leak | Reachable but unused — GC can't help |
| `finalize()` | Deprecated/removed — use `AutoCloseable` |
| `System.gc()` | Don't call in production |

## Next: Part I — Modern Java. Modules 28–32: Java 11 through 24 features