# Module 21 — Stream API (Condensed)

> Part E: Java 8 Features · Prerequisites: Module 19, 20

## Core Idea
A stream = **pipeline for processing sequences of elements** (filter, transform, aggregate) functionally. Not a data structure — doesn't store elements, processes on demand.

```
Source → [intermediate ops] → [intermediate ops] → terminal op → result
```
```java
double totalSeniorSalary = employees.stream()
    .filter(e -> e.getSalary() > 80000)
    .mapToDouble(Employee::getSalary)
    .sum();
```

**Key properties:**
- **Lazy** — intermediate ops don't run until a terminal op is called
- **Non-mutating** — never modifies the source
- **Consumed once** — reuse after a terminal op throws `IllegalStateException`
- **Sequential or parallel** — `parallelStream()` splits across threads

---

## 1. Creating Streams
```java
employees.stream();                                  // from Collection
Arrays.stream(names);                                 // from array
Stream.of("Ponu", "Gonu");                             // from values
Stream.empty();
Stream.generate(Math::random);                         // infinite — needs limit()
Stream.iterate(1, n -> n + 1);                          // infinite — 1,2,3...
IntStream.range(1, 6);                                  // 1..5
IntStream.rangeClosed(1, 5);                            // 1..5 inclusive
DoubleStream.of(75000, 82000, 55000);
```

---

## 2. Intermediate Operations (return a new Stream, lazy)

```java
.filter(e -> e.getSalary() > 70000)                     // keep matching
.map(Employee::getName)                                  // transform each
.mapToDouble(Employee::getSalary)                         // → primitive stream, unlocks sum()/average()
.flatMap(e -> e.getProjects().stream())                    // flatten Stream<List<T>> → Stream<T>
.sorted(Comparator.comparing(Employee::getSalary))          // sort
.sorted(Comparator.comparing(Employee::getSalary).reversed())
.sorted(Comparator.comparing(Employee::getDepartment).thenComparing(Employee::getName))  // multi-level
.distinct()                                                   // remove dupes
.limit(3)                                                      // first N — pagination
.skip(2)                                                        // skip first N — combine with limit for pages
.peek(e -> System.out.println(e.getName()))                     // debug only, not for prod side-effects
```

---

## 3. Terminal Operations (trigger execution, consume the stream)

**`forEach`:** `employees.stream().forEach(e -> System.out.println(e.getName()));`

**`collect`** — most versatile:
```java
.collect(Collectors.toList())
.collect(Collectors.toSet())
.collect(Collectors.toMap(Employee::getId, e -> e))
.collect(Collectors.joining(", "))                    // "Sonu, Monu, ..."
.collect(Collectors.joining(", ", "[", "]"))           // "[Sonu, Monu, ...]"
```

**`groupingBy`** — group into a Map:
```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

Map<String, Double> avgByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.averagingDouble(Employee::getSalary)));
```

**`partitioningBy`** — split into exactly two groups (true/false):
```java
Map<Boolean, List<Employee>> split = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.getSalary() > 80000));
// split.get(true) = senior, split.get(false) = junior
```

**`count`:** `employees.stream().filter(...).count();`

**`reduce`** — aggregate to a single value:
```java
double total = employees.stream().mapToDouble(Employee::getSalary).reduce(0, Double::sum);
Optional<Employee> topEarner = employees.stream()
    .reduce((a, b) -> a.getSalary() > b.getSalary() ? a : b);
```

**`findFirst` / `findAny`:** `findFirst()` deterministic; `findAny()` may be faster in parallel streams.

**`anyMatch` / `allMatch` / `noneMatch`:**
```java
boolean anyHigh = employees.stream().anyMatch(e -> e.getSalary() > 90000);
```

**`min` / `max`:** `employees.stream().max(Comparator.comparing(Employee::getSalary));`

**`toArray`:** `employees.stream().toArray(Employee[]::new);`

---

## 4. Statistics
```java
DoubleSummaryStatistics stats = employees.stream()
    .collect(Collectors.summarizingDouble(Employee::getSalary));
stats.getCount(); stats.getSum(); stats.getAverage(); stats.getMin(); stats.getMax();
```

---

## 5. Parallel Streams
```java
double total = employees.parallelStream().mapToDouble(Employee::getSalary).sum();
```
**Use when:** large datasets (1000s+), CPU-intensive stateless work, order doesn't matter.
**Avoid when:** small collections (thread overhead > gain), shared mutable state (race conditions), I/O-bound work, order-sensitive results.

```java
// UNSAFE — shared mutable list, race condition
List<String> names = new ArrayList<>();
employees.parallelStream().map(Employee::getName).forEach(names::add);   // ❌

// SAFE — collect() is thread-safe
List<String> safeNames = employees.parallelStream()
    .map(Employee::getName).collect(Collectors.toList());                 // ✓
```

---

## 6. Lazy Evaluation in Action
```java
Stream<Employee> pipeline = employees.stream()
    .filter(e -> { System.out.println("filter: " + e.getName()); return e.getSalary() > 70000; })
    .map(e -> { System.out.println("map: " + e.getName()); return e; });

pipeline.findFirst();   // short-circuits — stops at the FIRST match, rest untouched
```

---

## 7. Common Mistakes

| Mistake | Problem | Fix |
|---|---|---|
| Reusing a consumed stream | `IllegalStateException` | Build a new stream each time |
| Modifying source inside stream | `ConcurrentModificationException` | Collect to a list, then modify source |
| No terminal operation | Nothing executes | Always end with `forEach`/`collect`/etc. |
| Accumulating via `forEach` | Compile error (var must be effectively final) | Use `mapToDouble().sum()` or `reduce()` |

---

## Quick Reference

| Operation | Type | Method |
|---|---|---|
| `filter` | Intermediate | `filter(Predicate)` |
| `map` | Intermediate | `map(Function)` |
| `mapToDouble/Int/Long` | Intermediate | `mapToDouble(ToDoubleFunction)` |
| `flatMap` | Intermediate | `flatMap(Function<T, Stream<R>>)` |
| `sorted` | Intermediate | `sorted(Comparator)` |
| `distinct`/`limit`/`skip`/`peek` | Intermediate | as named |
| `forEach` | Terminal | `forEach(Consumer)` |
| `collect` | Terminal | `collect(Collector)` |
| `count` | Terminal | `count()` |
| `reduce` | Terminal | `reduce(BinaryOperator)` |
| `findFirst`/`findAny` | Terminal | as named |
| `anyMatch`/`allMatch`/`noneMatch` | Terminal | `X(Predicate)` |
| `min`/`max` | Terminal | `min/max(Comparator)` |
| `toArray` | Terminal | `toArray(IntFunction)` |
| `sum`/`average`/`min`/`max` | Terminal (primitive) | On `IntStream`/`DoubleStream` |

## Next: Module 22 — Optional (eliminating null checks and NPEs)