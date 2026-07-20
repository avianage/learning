# Module 22 — Optional (Condensed)

> Part E: Java 8 Features · Prerequisites: Module 19, 20, 21

## The Null Problem
`null` is a terrible way to represent "no value" — it carries no explanation, is easy to forget to check, and errors surface far from where the null was introduced.

```java
public Employee findById(int id) { return database.get(id); }  // may return null
Employee emp = service.findById(999);
emp.getName();   // NullPointerException
```

**`Optional<T>`** makes the possibility of absence explicit in the type system.

---

## 1. Creating Optional
```java
Optional<String> name = Optional.of("Sonu");        // must be non-null — NPE if null
Optional<String> safe = Optional.ofNullable(value);   // empty if null, present if not
Optional<Employee> none = Optional.empty();            // explicitly no value
```

## 2. Checking / Getting

```java
result.isPresent();                          // true if has value
result.isEmpty();                             // Java 11+
result.get();                                  // throws NoSuchElementException if empty — avoid raw use

service.findById(101).ifPresent(e -> System.out.println(e.getName()));   // run only if present

service.findById(999).ifPresentOrElse(          // Java 9+
    e -> System.out.println("Found: " + e.getName()),
    () -> System.out.println("Not found.")
);
```

## 3. Getting a Value With a Default

```java
Employee emp = service.findById(999).orElse(new Employee(0, "Guest", 0, "None"));
// orElse ALWAYS evaluates its argument — even if a value is present

Employee emp2 = service.findById(999).orElseGet(() -> createDefaultEmployee());
// orElseGet is LAZY — supplier runs only if empty

Employee emp3 = service.findById(999).orElseThrow(() -> new EmployeeNotFoundException(999));
Employee emp4 = service.findById(101).orElseThrow();   // Java 10+, throws NoSuchElementException
```
**Rule of thumb:** use `orElseGet` instead of `orElse` when the default is expensive to construct.

---

## 4. Transforming Values (the real power)

```java
Optional<String> dept = service.findById(101).map(Employee::getDepartment);   // map — transform if present

// flatMap — when the mapper itself returns an Optional (avoids Optional<Optional<T>>)
Optional<String> location = service.findById(101)
    .flatMap(Employee::getDepartment)     // Optional<Department>
    .map(Department::getLocation);         // Optional<String>

Optional<Employee> seniorEng = service.findById(101)
    .filter(e -> e.getDepartment().equals("Engineering"))
    .filter(e -> e.getSalary() > 80000);
```

## 5. Chaining — Eliminates Nested Null Checks

```java
// Without Optional — 3 null checks
public String getManagerName(int id) {
    Employee emp = findById(id);
    if (emp == null) return "Unknown";
    Department dept = emp.getDepartment();
    if (dept == null) return "Unknown";
    Employee manager = dept.getManager();
    return manager == null ? "Unknown" : manager.getName();
}

// With Optional — clean chain
public String getManagerName(int id) {
    return findById(id)
        .map(Employee::getDepartment)
        .flatMap(Department::getManager)
        .map(Employee::getName)
        .orElse("Unknown");
}
```
Empty propagates through the whole chain — first empty step short-circuits to `orElse`.

---

## 6. Where to Use Optional

| Where | Use It? |
|---|---|
| Return type | **Yes** — signals possible absence clearly |
| Method parameter | **No** — awkward; use overloading or a null check instead |
| Class field | **No** — not `Serializable`, adds overhead; use plain `null` |

```java
public Optional<Employee> findById(int id) { return Optional.ofNullable(database.get(id)); }
public Optional<Employee> findByName(String name) {
    return employees.stream().filter(e -> e.getName().equals(name)).findFirst();
}
```

---

## 7. `stream()` — Java 9+
Converts Optional to a 0-or-1-element Stream — great for batch lookups, filtering out empties in one pass:

```java
List<Employee> found = ids.stream()
    .map(service::findById)          // Stream<Optional<Employee>>
    .flatMap(Optional::stream)        // Stream<Employee> — empties dropped
    .collect(Collectors.toList());
```

---

## 8. Practical Pattern

```java
public class EmployeeLookupService {
    public Optional<Employee> findById(int id) { return Optional.ofNullable(store.get(id)); }
    public Optional<Employee> findByName(String name) {
        return store.values().stream().filter(e -> e.getName().equalsIgnoreCase(name)).findFirst();
    }
    public Optional<Employee> findTopEarnerIn(String dept) {
        return store.values().stream()
            .filter(e -> e.getDepartment().equals(dept))
            .max(Comparator.comparing(Employee::getSalary));
    }
}
```
```java
double salary = service.findByName("Monu").map(Employee::getSalary).orElse(0.0);

service.findTopEarnerIn("Engineering").ifPresentOrElse(
    e -> System.out.printf("Top: %s (%.0f)%n", e.getName(), e.getSalary()),
    () -> System.out.println("No engineers found.")
);
```

---

## Quick Reference

| Method | Behavior |
|---|---|
| `Optional.of(v)` | Create with non-null value — NPE if null |
| `Optional.ofNullable(v)` | Empty if null, present if not |
| `Optional.empty()` | Create empty |
| `isPresent()` / `isEmpty()` | Has value? (isEmpty is Java 11+) |
| `get()` | Value or exception — avoid without a check |
| `ifPresent(consumer)` | Run if present |
| `ifPresentOrElse(c, r)` | Present branch + empty branch (Java 9+) |
| `orElse(default)` | Value or default — default always evaluated |
| `orElseGet(supplier)` | Value or supplier result — lazy |
| `orElseThrow(supplier)` | Value or throw |
| `map(fn)` | Transform if present |
| `flatMap(fn)` | Transform when fn returns Optional |
| `filter(pred)` | Keep only if predicate passes |
| `stream()` | 0/1-element stream (Java 9+) |

| Do | Don't |
|---|---|
| Use as return type for possible absence | Use as method parameter |
| Chain with `map`/`flatMap`/`filter` | Use as a class field |
| Use `orElseGet` for expensive defaults | Call `get()` without checking |
| Use `ifPresentOrElse` for both branches | Use `Optional.of(null)` |

## Next: Part F — Concurrency. Module 23: Multithreading