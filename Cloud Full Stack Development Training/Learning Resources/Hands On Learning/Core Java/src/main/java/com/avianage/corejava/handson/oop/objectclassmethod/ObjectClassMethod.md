# Module 14 — Object Class Methods (Condensed)

> Part D: Core Java Toolkit · Prerequisites: Module 08–13

## Core Idea
Every class implicitly extends `java.lang.Object` — so every object inherits: `toString()`, `equals()`, `hashCode()`, `getClass()`, `clone()`, `wait()/notify()/notifyAll()`, `finalize()` (deprecated). The first three you override regularly; the rest are situational.

---

## 1. `toString()`

**Default** (unoverridden): `ClassName@hexHashCode` — useless for debugging.

**Always override:**
```java
@Override
public String toString() {
    return String.format("Employee{id=%d, name='%s', dept='%s', salary=%.2f}",
                         id, name, department, salary);
}
```
Auto-invoked by `println(obj)`, string concatenation (`"x: " + obj`), debuggers, logs.

---

## 2. `equals()`

**Default:** uses `==` (reference identity) — two objects with identical data are "unequal" unless overridden.

**Override for content equality** (4-step pattern):
```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;                       // 1. same ref
    if (obj == null) return false;                       // 2. null check
    if (getClass() != obj.getClass()) return false;       // 3. type check
    Employee other = (Employee) obj;                      // 4. field compare
    return id == other.id
        && Objects.equals(name, other.name)
        && Double.compare(salary, other.salary) == 0
        && Objects.equals(department, other.department);
}
```
`Objects.equals(a, b)` is null-safe (true if both null, false if only one, else `a.equals(b)`).

---

## 3. `hashCode()`

**Contract:** if `a.equals(b)` → `a.hashCode() == b.hashCode()`. (Reverse not required — collisions allowed.) **Always override alongside `equals()`**, or `HashMap`/`HashSet` break — lookups land in the wrong bucket and `contains()`/`get()` silently fail.

```java
@Override
public int hashCode() {
    return Objects.hash(id, name, salary, department);   // same fields as equals()
}
```

With both overridden correctly:
```java
Set<Employee> set = new HashSet<>();
set.add(e1);
set.contains(new Employee(101, "Sonu", 75000, "Engineering"));  // true
```

---

## 4. `getClass()`

Returns the actual runtime `Class`. Used for logging/debugging and strict-type equality checks.

```java
e.getClass();              // class com.ems.bean.Manager
e.getClass().getSimpleName(); // "Manager"
```
`instanceof` accepts subclasses; `getClass() ==` requires exact type match — use the latter in `equals()`.

---

## 5. `clone()`

Requires implementing `Cloneable` + overriding `clone()`.

**Shallow copy** (default `Object.clone()`) — copies field values; reference fields still point to the same underlying object:
```java
public class Department implements Cloneable {
    private String name, location;
    @Override
    public Department clone() {
        try { return (Department) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }
}
```

**Deep copy** — needed when a field is mutable (array, List, etc.):
```java
public class Project implements Cloneable {
    private String[] teamMembers;
    @Override
    public Project clone() {
        try {
            Project copy = (Project) super.clone();
            copy.teamMembers = teamMembers.clone();   // deep-copy the array
            return copy;
        } catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }
}
```

**Modern alternative (preferred):** copy constructor — no checked exception, no marker interface.
```java
public Employee(Employee source) {
    this.id = source.id; this.name = source.name;
    this.salary = source.salary; this.department = source.department;
}
Employee copy = new Employee(original);
```

---

## 6. `wait()` / `notify()` / `notifyAll()`
Thread coordination via a shared object's monitor — must be called inside a `synchronized` block.

| Method | Effect |
|---|---|
| `wait()` | Releases lock, suspends thread until notified |
| `wait(long ms)` | Same, but times out |
| `notify()` | Wakes one arbitrary waiting thread |
| `notifyAll()` | Wakes all waiting threads |

```java
public synchronized void waitForPayroll() throws InterruptedException {
    while (!payrollReady) wait();          // releases lock, suspends
}
public synchronized void markPayrollReady() {
    payrollReady = true;
    notifyAll();                            // wake all waiters
}
```
Classic Producer-Consumer pattern. Modern code prefers `BlockingQueue`, `CountDownLatch`, `CompletableFuture` (Modules 23–24), but `wait/notify` underlies them.

---

## 7. `finalize()` — Deprecated, Never Use
Was called by GC before reclaiming memory; intended for cleanup. **Deprecated (Java 9+)** because: no guarantee it runs, allows object resurrection, adds GC overhead.

**Modern replacement — `AutoCloseable` + try-with-resources:**
```java
public class ReportGenerator implements AutoCloseable {
    public ReportGenerator(String name) { /* open */ }
    public void generate() { /* ... */ }
    @Override public void close() { /* cleanup, guaranteed */ }
}

try (ReportGenerator rg = new ReportGenerator("Sonu")) {
    rg.generate();
}   // close() always runs, even on exception
```

---

## Quick Reference

| Method | When to Override | Key Rule |
|---|---|---|
| `toString()` | Always | Include fields useful for debugging |
| `equals()` | When logical equality matters | 4-step pattern; `Objects.equals` for fields |
| `hashCode()` | Always alongside `equals()` | `Objects.hash()` on the same fields |
| `getClass()` | Rarely overridden | Runtime type info, strict equality |
| `clone()` | When copying needed | Prefer a copy constructor instead |
| `wait()`/`notify()` | Rarely directly | Must be inside `synchronized`; prefer higher-level concurrency APIs |
| `finalize()` | Never | Deprecated — use `AutoCloseable` |

## Next: Module 15 — Inner Classes (regular inner, static nested, method-local, anonymous)**