# Module 31 — Java 21 Features (Condensed)

> Part I: Modern Java · Prerequisites: Module 30, 23, 24 · Requires JDK 21+

## About Java 21
Current recommended LTS — most impactful release since Java 8. Delivered Project Loom's virtual threads.

| Feature | Status |
|---|---|
| Virtual Threads | Standard (JEP 444) |
| Sequenced Collections | Standard (JEP 431) |
| Record Patterns | Standard (JEP 440) |
| Pattern Matching in Switch | Standard (JEP 441) |
| String Templates | Preview (JEP 430) |
| Unnamed Classes & Instance Main | Preview (JEP 445) |
| Structured Concurrency | Preview (JEP 453) |

---

## 1. Virtual Threads (Project Loom)

**Problem:** platform threads map 1:1 to OS threads (~1MB stack each) — 50,000 concurrent requests = 50GB just for stacks. Workaround was complex async/reactive code.

**Solution:** virtual threads are **JVM-managed**, not OS-managed — millions possible, microsecond startup, few KB each. When a virtual thread blocks (I/O, sleep), the JVM unmounts it from the OS thread automatically. Code stays plain blocking style — no callbacks.

```java
Thread vt = Thread.ofVirtual().name("payroll-processor").start(() -> processPayroll());
Thread vt2 = Thread.startVirtualThread(() -> sendEmail("Sonu"));   // shortcut
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();  // 1 VT per task, no pool sizing
```

**Impact example:** 10,000 tasks × 100ms I/O each — platform pool (200 threads) ≈ 5200ms; virtual threads ≈ 130ms (nearly all run in parallel).

**Good for:** I/O-bound work (DB calls, HTTP, file reads). **Not for:** CPU-bound computation — use a fixed pool sized to `Runtime.getRuntime().availableProcessors()`.

**Pinning — avoid:** a virtual thread is pinned (can't unmount) inside `synchronized` blocks or native calls — loses the scalability benefit.
```java
// Avoid — synchronized pins the virtual thread during sleep
synchronized (lock) { Thread.sleep(100); }

// Prefer — ReentrantLock allows unmounting
ReentrantLock lock = new ReentrantLock();
lock.lock();
try { Thread.sleep(100); } finally { lock.unlock(); }
```

---

## 2. Sequenced Collections (JEP 431)
New interfaces adding defined first/last access to ordered collections.

```
SequencedCollection<E>: addFirst/addLast, getFirst/getLast, removeFirst/removeLast, reversed()
SequencedSet<E> extends SequencedCollection<E>, Set<E>
SequencedMap<K,V>: firstEntry/lastEntry, putFirst/putLast, pollFirstEntry/pollLastEntry, reversed()
```

**List/Deque (implement `SequencedCollection`):**
```java
List<Employee> employees = new ArrayList<>(List.of(e1, e2, e3));
employees.getFirst(); employees.getLast();
employees.addFirst(boss); employees.addLast(newHire);
employees.removeFirst(); employees.removeLast();
List<Employee> reversed = employees.reversed();   // view, no copy
```

**Map (LinkedHashMap/TreeMap implement `SequencedMap`):**
```java
LinkedHashMap<Integer, String> idToName = new LinkedHashMap<>();
idToName.firstEntry(); idToName.lastEntry();
idToName.putFirst(100, "Boss"); idToName.putLast(104, "Ponu");
idToName.sequencedKeySet();
idToName.reversed().forEach((k, v) -> ...);
```
Pre-21 this required manual iteration — now direct methods.

---

## 3. Record Patterns (JEP 440)
Extends Java 16's `instanceof` pattern matching to **destructure** record components directly.

```java
record EmployeeRecord(int id, String name, double salary, String department) { }
record Address(String city, String country) { }
record EmployeeWithAddress(EmployeeRecord employee, Address address) { }
```

**Destructuring `instanceof`:**
```java
if (obj instanceof EmployeeRecord(int id, String name, double salary, String dept)) {
    System.out.println(name + " earns " + salary + " in " + dept);
}
```

**Nested destructuring:**
```java
if (record instanceof EmployeeWithAddress(
        EmployeeRecord(int id, String name, double salary, String dept),
        Address(String city, String country))) {
    System.out.printf("%s from %s, %s earns %.0f%n", name, city, country, salary);
}
```

**In switch:**
```java
static String formatEmployee(Object obj) {
    return switch (obj) {
        case EmployeeRecord(var id, var name, var salary, var dept) when salary > 80000 ->
            String.format("[SENIOR] %s (%s)", name, dept);
        case EmployeeRecord(var id, var name, var salary, var dept) ->
            String.format("[STAFF]  %s (%s)", name, dept);
        default -> "Unknown";
    };
}
```

---

## 4. Pattern Matching in Switch — Now Standard (JEP 441)

**`when` guards** — conditions on switch cases:
```java
String classification = switch (e) {
    case Employee emp when emp.getSalary() > 100000 -> "Executive";
    case Employee emp when emp.getSalary() > 80000  -> "Senior";
    case Employee emp                                -> "Junior";
};
```

**`null` handled explicitly** — previously threw NPE:
```java
String result = switch (e) {
    case null -> "No employee provided";
    case Employee emp when emp.getSalary() > 80000 -> "Senior: " + emp.getName();
    case Employee emp -> "Staff: " + emp.getName();
};
```

---

## 5. Structured Concurrency (Preview, JEP 453)
Treats multiple concurrently-running tasks as **one unit of work** — one failure cancels the rest; scope closing guarantees all subtasks are done or cancelled.

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var eng = scope.fork(() -> calculateDeptPayroll("Engineering"));
    var fin = scope.fork(() -> calculateDeptPayroll("Finance"));
    var hr  = scope.fork(() -> calculateDeptPayroll("HR"));

    scope.join();              // wait for all (or one failure)
    scope.throwIfFailed();      // re-throws if any subtask failed

    double total = eng.get() + fin.get() + hr.get();
}
```
`ShutdownOnFailure` — any failure cancels siblings. `ShutdownOnSuccess` — first success cancels the rest (race pattern). Pairs naturally with virtual threads: structured concurrency = lifecycle, virtual threads = scale.

---

## 6. Other Java 21 Highlights (Preview)

**Unnamed classes / instance main:**
```java
void main() {   // no class declaration, no public static
    System.out.println("Hello from Java 21!");
}
```
Good for scripting/teaching.

**Unnamed patterns/variables (`_`)** — for matches where you don't need the bound variable:
```java
switch (obj) {
    case Manager _ -> System.out.println("It's a manager");   // variable unused
    case Developer d -> System.out.println(d.getTechStack());  // variable used
}
```

---

## Practical Pattern — Virtual Threads + Sequenced Map
```java
record DeptResult(String department, double total, int count, long processingMs) {}

LinkedHashMap<String, DeptResult> results = new LinkedHashMap<>();
try (ExecutorService vPool = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<DeptResult>> futures = new ArrayList<>();
    for (String dept : depts) futures.add(vPool.submit(() -> processDepartment(dept, all)));
    for (int i = 0; i < depts.length; i++) results.put(depts[i], futures.get(i).get());
}
// 4 tasks × 200ms run concurrently on virtual threads ≈ 215ms total

DeptResult first = results.firstEntry().getValue();   // SequencedMap
DeptResult last  = results.lastEntry().getValue();
```

---

## Quick Reference

| Feature | Key Point |
|---|---|
| Virtual threads | Millions of cheap, JVM-managed threads |
| `newVirtualThreadPerTaskExecutor()` | One virtual thread per submitted task |
| Best for | I/O-bound work |
| Not for | CPU-bound computation — use fixed pool |
| Avoid `synchronized` w/ VT | Causes pinning — use `ReentrantLock` |
| Sequenced collections | `getFirst`/`getLast`, `addFirst`/`addLast`, `reversed()` |
| `SequencedMap` | `firstEntry`/`lastEntry`, `putFirst`/`putLast` |
| Record patterns | Destructure record components in `instanceof`/`switch` |
| `when` guards | Conditional switch patterns |
| `null` in switch | Handled explicitly — no NPE |
| Structured concurrency | Tasks as a unit — failure cancels siblings (preview) |

## Next: Module 32 — Java 24 Features