# Module 30 — Java 17 Features (Condensed)

> Part I: Modern Java · Prerequisites: Module 29 · Requires JDK 17+

## About Java 17
Current most-widely-adopted LTS. Standardized features previewed since 14–16:

| Feature | Preview Since | Standard In |
|---|---|---|
| Records | Java 14 | Java 16 |
| Pattern matching for `instanceof` | Java 14 | Java 16 |
| Sealed classes | Java 15 | Java 17 |
| Text blocks | Java 13 | Java 15 |

---

## 1. Records (Java 16)

**Problem:** a data-carrying class needs 40+ lines (constructor, getters, equals/hashCode/toString) for what's conceptually 4 fields.

**Solution:**
```java
public record EmployeeRecord(int id, String name, double salary, String department) { }
```
One line — compiler generates: canonical constructor, private final fields, accessors (`id()`, `name()` — **no "get" prefix**), `equals()`/`hashCode()`/`toString()` based on all components.

```java
EmployeeRecord e = new EmployeeRecord(101, "Sonu", 75000.0, "Engineering");
e.name();       // "Sonu" — accessor, not getName()
e.toString();   // EmployeeRecord[id=101, name=Sonu, salary=75000.0, department=Engineering]
```

**Immutable:** all fields `private final`, no setters. To "update," create a new record:
```java
EmployeeRecord promoted = new EmployeeRecord(e.id(), e.name(), 85000.0, e.department());
```

**Compact constructor** — validate/normalize without restating params:
```java
public record EmployeeRecord(int id, String name, double salary, String department) {
    public EmployeeRecord {                        // no parameter list
        if (id <= 0) throw new IllegalArgumentException("ID must be positive: " + id);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name blank.");
        name = name.strip();                         // modify param before auto-assignment
    }
}
```

**Adding methods** — instance/static methods and static fields OK; no extra instance fields beyond components:
```java
public record EmployeeRecord(int id, String name, double salary, String department) {
    public static final double MIN_SALARY = 15000.0;
    public double calculateBonus(double pct) { return salary * (pct / 100); }
    public static EmployeeRecord of(String csv) { /* parse and construct */ }
}
```

**Can implement interfaces:**
```java
public record EmployeeRecord(...) implements Displayable { @Override public void display() { ... } }
```

**Work correctly in collections** (equals/hashCode auto-correct):
```java
Set<EmployeeRecord> unique = new HashSet<>();
unique.add(new EmployeeRecord(101, "Sonu", 75000, "Engineering"));
unique.add(new EmployeeRecord(101, "Sonu", 75000, "Engineering"));   // duplicate — size stays 1
```

**When to use:**

| Use records for | Don't use for |
|---|---|
| DTOs, value objects (money, coordinates) | Entities with mutable state |
| Multi-value method return types | Classes needing inheritance |
| Immutable config holders | JPA/Hibernate entities (need no-arg ctor + setters) |

---

## 2. Sealed Classes (Java 17)

**Problem:** without sealed classes, any class can be subclassed by anyone — can't express "only these three types are valid subtypes."

```java
public sealed class Employee permits Manager, Developer, Contractor {
    private final int id; private final String name; private final double salary;
}
```
Each permitted subclass must be exactly one of:
- `final` — no further extension
- `sealed` — extendable, but only by its own listed subclasses
- `non-sealed` — reopens extension to anyone

```java
public final class Manager extends Employee { private final int teamSize; }
public final class Developer extends Employee { private final String techStack; }
public non-sealed class Contractor extends Employee { }   // open again

// public class Intern extends Employee { }   // ❌ compile error — not permitted
```

**Sealed interfaces** work the same way: `public sealed interface Payable permits Employee, Vendor, Freelancer { }`

**Why it matters:** enables **exhaustive pattern matching** — compiler knows all subtypes, so a switch over them needs no `default` and can't silently miss a case.

---

## 3. Pattern Matching for `instanceof` (Java 16)

**Old way** — check then cast:
```java
if (obj instanceof Manager) {
    Manager m = (Manager) obj;   // redundant cast
}
```

**New way** — check + bind in one step:
```java
if (obj instanceof Manager m) {
    System.out.println(m.getTeamSize());   // m already correctly typed, no cast
}
if (obj instanceof Developer d && d.getSalary() > 80000) {   // bound var usable in condition
    System.out.println(d.getName() + " senior");
}
```
**Negation pattern:**
```java
if (!(obj instanceof Manager m)) {
    return;              // m NOT in scope here
}
// m IS in scope here — match succeeded above
```

---

## 4. Pattern Matching in `switch` (Preview in 17 → Standard Java 21)
Combined with sealed classes → exhaustive, type-safe dispatch, no `default` needed:
```java
public static String describeEmployee(Employee e) {
    return switch (e) {
        case Manager    m -> "Manager with team of " + m.getTeamSize();
        case Developer  d -> "Developer working on " + d.getTechStack();
        case Contractor c -> "Contractor (daily rate basis)";
        // no default — Employee is sealed, all permitted types covered
    };
}
```
If a new permitted subtype is added later and the switch isn't updated, the **compiler catches it immediately** — sealed classes + pattern matching = zero `instanceof` chains, guaranteed exhaustiveness.

---

## 5. Other Java 17 Highlights

- **Strong encapsulation of JDK internals:** `sun.*` / internal `com.sun.*` blocked by default → `InaccessibleObjectException` if a library relies on them; usually means the library needs updating.
- **Always-strict floating-point (JEP 306):** all FP ops now consistently strict; `strictfp` keyword still exists but redundant.
- **New Random Number Generators (JEP 356):**
```java
RandomGenerator rng = RandomGenerator.of("Xoshiro256PlusPlus");
double randomSalary = 50000 + rng.nextDouble() * 100000;

SplittableRandom sr = new SplittableRandom();
int[] ids = sr.ints(5, 100, 200).toArray();   // 5 random ints in [100, 200)
```

---

## 6. Practical Pattern — Records + Sealed + Pattern Matching Together

```java
public sealed class Employee permits FullTimeEmployee, PartTimeEmployee, Contractor { }

public record FullTimeEmployee(int id, String name, double salary, String department) extends Employee { }
public record PartTimeEmployee(int id, String name, double hourlyRate, int hoursPerWeek) extends Employee { }
public record Contractor(int id, String name, double dailyRate, String endDate) extends Employee { }
```
```java
public static double calculateMonthlyPay(Employee e) {
    return switch (e) {
        case FullTimeEmployee fte -> fte.salary();
        case PartTimeEmployee pte -> pte.hourlyRate() * pte.hoursPerWeek() * 4;
        case Contractor c         -> c.dailyRate() * 22;
    };   // exhaustive — sealed hierarchy, no default
}
```
Records give immutable, boilerplate-free data types; sealed classes constrain the hierarchy; switch pattern matching dispatches on type safely and exhaustively — together they replace most `instanceof` chains and manual `equals`/`hashCode`/`toString`.

---

## Quick Reference

| Feature | Key Point |
|---|---|
| Records | Immutable data classes — auto constructor, accessors, equals/hashCode/toString |
| Record accessor | `name()` not `getName()` |
| Compact constructor | Validate/normalize without restating params |
| Sealed class | Explicitly lists permitted subclasses — closed hierarchy |
| `permits` | Subclasses must be `final`, `sealed`, or `non-sealed` |
| `non-sealed` | Reopens extension to anyone |
| Pattern matching `instanceof` | `obj instanceof Manager m` — check + bind, no cast |
| Switch pattern matching | Type-based dispatch — exhaustive with sealed hierarchies |
| Records + sealed | Records can be `permits` targets — immutable sealed subtypes |

## Next: Module 31 — Java 21 Features (virtual threads, record patterns, sequenced collections, structured concurrency)