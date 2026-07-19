# Module 26 — Collections and Generics (Condensed)

> Part G: I/O and Collections · Prerequisites: Module 08–13, 19–21

## Why Collections?
Arrays are fixed-size. The **Java Collections Framework** provides growable, well-tested data structures.

```
Collection
├── List   — ordered, duplicates allowed → ArrayList, LinkedList
├── Set    — no duplicates → HashSet, LinkedHashSet, TreeSet
└── Queue  — processing order → PriorityQueue, Deque → ArrayDeque

Map (separate hierarchy) — key-value → HashMap, LinkedHashMap, TreeMap
```

---

## 1. Generics — Compile-Time Type Safety

Pre-generics: `List employees = new ArrayList();` → wrong types compile, fail at runtime (`ClassCastException`).
With generics: `List<Employee> employees = new ArrayList<>();` → wrong type is a **compile error**.

**Generic class:**
```java
public class Pair<A, B> {
    private A first; private B second;
    public Pair(A first, B second) { this.first = first; this.second = second; }
}
Pair<String, Double> entry = new Pair<>("Sonu", 75000.0);
```

**Generic method:**
```java
public static <T> void printAll(List<T> list) { list.forEach(System.out::println); }
```

**Bounded type parameter** — restrict to a type or its subtypes:
```java
public static <T extends Number> double sum(List<T> numbers) {
    return numbers.stream().mapToDouble(Number::doubleValue).sum();
}
```

**Wildcards:**
```java
List<?> unknown;                                   // unknown type
List<? extends Employee> readable;                  // upper bound — reading, T or subtype
List<? super Employee> writable;                     // lower bound — writing, T or supertype
```

---

## 2. `List` — Ordered, Duplicates Allowed

**`ArrayList`** — dynamic array, O(1) random access, slower mid-insert/delete:
```java
List<Employee> employees = new ArrayList<>();
employees.add(e);
employees.add(0, e);            // insert at index
employees.get(0); employees.set(1, e); employees.remove(0);
employees.contains(e); employees.indexOf(e);
employees.sort(Comparator.comparing(Employee::getSalary));
List<Employee> top3 = employees.subList(0, 3);   // VIEW, not a copy
```

**`LinkedList`** — doubly-linked, fast insert/delete at known position, slow random access:
```java
LinkedList<Employee> queue = new LinkedList<>();
queue.addFirst(e); queue.addLast(e); queue.getFirst(); queue.removeFirst();
```
Use `LinkedList` only for frequent insert/remove at both ends — `ArrayList` is faster otherwise.

---

## 3. `Set` — No Duplicates

**`HashSet`** — O(1) ops, no order, needs correct `equals()`/`hashCode()`:
```java
Set<String> depts = new HashSet<>();
depts.add("Engineering"); depts.add("Engineering");   // duplicate silently ignored
```

**`LinkedHashSet`** — same as HashSet + preserves insertion order.

**`TreeSet`** — sorted (natural order or `Comparator`):
```java
Set<Employee> bySalary = new TreeSet<>(Comparator.comparing(Employee::getSalary));
```

---

## 4. `Queue` and `Deque`

**`PriorityQueue`** — elements removed in priority order:
```java
PriorityQueue<Employee> pq = new PriorityQueue<>(Comparator.comparing(Employee::getSalary));
pq.offer(e);
Employee lowest = pq.poll();   // removes + returns head (lowest salary)
```

**`ArrayDeque`** — double-ended, use as stack OR queue, faster than `Stack`/`LinkedList`:
```java
Deque<String> deque = new ArrayDeque<>();
deque.offer("A"); deque.poll();     // Queue (FIFO) — add tail, remove head
deque.push("B"); deque.pop();       // Stack (LIFO) — add head, remove head
```

---

## 5. `Map` — Key-Value Pairs

**`HashMap`** — O(1) average, no order, keys need `equals()`/`hashCode()`:
```java
Map<Integer, Employee> empById = new HashMap<>();
empById.put(101, e);
empById.get(101); empById.get(999);                       // null if missing
empById.getOrDefault(999, defaultEmployee);
empById.containsKey(101); empById.containsValue(e);
empById.remove(103); empById.remove(101, e);                // remove only if key maps to this value
for (Map.Entry<Integer, Employee> entry : empById.entrySet()) { ... }
empById.forEach((id, emp) -> ...);
empById.computeIfAbsent(104, id -> new Employee(id, "Ponu", 91000, "Finance"));
empById.putIfAbsent(105, e);
```

**`LinkedHashMap`** — HashMap + insertion order preserved.

**`TreeMap`** — sorted by key:
```java
Map<String, List<Employee>> byDept = new TreeMap<>();
byDept.computeIfAbsent(e.getDepartment(), k -> new ArrayList<>()).add(e);   // alphabetical dept keys
```

---

## 6. `Collections` Utility Class
```java
Collections.sort(list, Comparator.comparing(Employee::getSalary));
Collections.reverse(list);
Collections.shuffle(list);
Collections.min(list, comparator); Collections.max(list, comparator);
Collections.frequency(list, item);
List<Employee> readOnly = Collections.unmodifiableList(list);   // throws on modify
List<Employee> syncList = Collections.synchronizedList(list);
```

---

## 7. Java 9+ Immutable Factory Methods
```java
List<String> names = List.of("Sonu", "Monu", "Tonu");
Set<String> depts = Set.of("Engineering", "HR");
Map<Integer, String> map = Map.of(101, "Sonu", 102, "Monu");
Map<Integer, String> big = Map.ofEntries(Map.entry(101, "Sonu"), Map.entry(102, "Monu"));
// names.add(...) → UnsupportedOperationException; null elements not allowed
```

---

## 8. Choosing the Right Collection

| Need | Use |
|---|---|
| Ordered, fast random access | `ArrayList` |
| Frequent insert/delete at ends | `ArrayDeque` (or `LinkedList`) |
| No duplicates, fast lookup | `HashSet` |
| No duplicates, insertion order | `LinkedHashSet` |
| No duplicates, sorted | `TreeSet` |
| Key-value, fast lookup | `HashMap` |
| Key-value, insertion order | `LinkedHashMap` |
| Key-value, sorted by key | `TreeMap` |
| Priority-based processing | `PriorityQueue` |
| Stack (LIFO) / Queue (FIFO) | `ArrayDeque` |
| Thread-safe map | `ConcurrentHashMap` |
| Thread-safe producer-consumer | `BlockingQueue` |
| Small, fixed, immutable | `List.of()`, `Set.of()`, `Map.of()` |

---

## 9. Practical Pattern — Multi-Index Registry
```java
public class EmployeeRegistry {
    private final Map<Integer, Employee> byId = new LinkedHashMap<>();
    private final Map<String, List<Employee>> byDepartment = new TreeMap<>();
    private final TreeSet<Employee> bySalary =
        new TreeSet<>(Comparator.comparing(Employee::getSalary).reversed().thenComparing(Employee::getId));

    public void add(Employee e) {
        byId.put(e.getId(), e);
        byDepartment.computeIfAbsent(e.getDepartment(), k -> new ArrayList<>()).add(e);
        bySalary.add(e);
    }
    public Optional<Employee> findById(int id) { return Optional.ofNullable(byId.get(id)); }
    public List<Employee> topEarners(int n) { return bySalary.stream().limit(n).collect(Collectors.toList()); }
    public Map<String, DoubleSummaryStatistics> salaryStatsByDept() {
        return byId.values().stream()
            .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.summarizingDouble(Employee::getSalary)));
    }
}
```
One `add()` keeps three different views in sync — lookup by ID, grouping by department, sorted by salary — each backed by the collection best suited to that access pattern.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Generics `<T>` | Type errors caught at compile time, not runtime |
| `ArrayList` | Default List — fast random access, slow mid-insert |
| `LinkedList` | Fast insert/remove at ends — rarely needed over ArrayList |
| `HashSet` | Default Set — fast, unordered |
| `LinkedHashSet` | Set + insertion order |
| `TreeSet` | Sorted set — needs Comparable/Comparator |
| `HashMap` | Default Map — fast key lookup |
| `LinkedHashMap` | Map + insertion order |
| `TreeMap` | Sorted by key |
| `ArrayDeque` | Stack and Queue — faster than Stack/LinkedList |
| `PriorityQueue` | Priority-ordered processing |
| `Collections` | sort/reverse/shuffle/unmodifiable/synchronized utilities |
| `List.of()`/`Set.of()`/`Map.of()` | Immutable, concise, null-safe (Java 9+) |

## Next: Part H — Memory. Module 27: Garbage Collection & object lifecycle