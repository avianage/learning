# Module 17 — Exception Handling (Condensed)

> Part D: Core Java Toolkit · Prerequisites: Module 08–13

## Core Idea
An exception is an event disrupting normal execution. Unhandled → JVM prints stack trace and terminates. Exception handling lets you intercept, respond, and keep running.

```
Normal:    step1 → step2 → step3
Exception: step1 → step2 → [thrown] → handler → recovery
```

---

## Exception Hierarchy

```
Throwable
├── Error                    — JVM-level, do NOT catch (OutOfMemoryError, StackOverflowError)
└── Exception                — application-level
    ├── IOException, SQLException, ParseException   — checked
    └── RuntimeException     — unchecked
        ├── NullPointerException, ArrayIndexOutOfBoundsException
        ├── ClassCastException, IllegalArgumentException
        ├── NumberFormatException, ArithmeticException
```

| Type | Examples | Compiler requires handling? | Cause |
|---|---|---|---|
| Checked | `IOException`, `SQLException` | Yes | External factors (missing file, DB down) |
| Unchecked | `NullPointerException`, `IllegalArgumentException` | No | Programming bugs |
| Error | `OutOfMemoryError` | No | JVM failure — don't catch |

---

## 1. `try-catch`
```java
try {
    double salary = Double.parseDouble(input);
} catch (NumberFormatException e) {
    System.out.println("Invalid salary: " + input);
}
```

## 2. `finally`
**Always runs** — try completes normally, exception caught, exception uncaught (runs before propagating), or `return` in try/catch. Only `System.exit()` bypasses it.

```java
try { reader = new FileReader(path); }
catch (IOException e) { /* handle */ }
finally { if (reader != null) reader.close(); }
```

## 3. `try-with-resources` (Java 7+) — Preferred
Any `AutoCloseable`/`Closeable` auto-closes on exit:
```java
try (FileReader reader = new FileReader("employees.txt");
     BufferedReader br = new BufferedReader(reader)) {
    String line;
    while ((line = br.readLine()) != null) System.out.println(line);
}   // both closed automatically, in reverse declaration order
```
If both try-body and close() throw, the close exception is **suppressed** (attached to the primary exception).

## 4. Multiple / Multi-Catch
```java
try { ... }
catch (NumberFormatException e) { ... }     // specific first
catch (NullPointerException e) { ... }
catch (Exception e) { ... }                  // catch-all, last

// Multi-catch — same handling for unrelated types
catch (NumberFormatException | IllegalArgumentException e) { ... }
```
Rule: more specific exceptions must precede more general ones (compiler-enforced); first match wins.

---

## 5. `throw` — Throw Manually
```java
public void setSalary(double salary) {
    if (salary < 0) throw new IllegalArgumentException("Salary cannot be negative: " + salary);
    this.salary = salary;
}
```
Always give a descriptive message — it's what ends up in logs.

## 6. `throws` — Declare Checked Exceptions
```java
public Employee readFromFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return parse(br.readLine());
    }
    // IOException not caught here — caller must handle
}
```
Contract: "I might throw this — caller, deal with it."

---

## 7. Custom Exceptions

```java
// Checked — extends Exception
public class EmployeeNotFoundException extends Exception {
    private final int employeeId;
    public EmployeeNotFoundException(int id) {
        super("Employee not found with ID: " + id);
        this.employeeId = id;
    }
    public EmployeeNotFoundException(int id, Throwable cause) {
        super("Employee not found with ID: " + id, cause);
        this.employeeId = id;
    }
}

// Unchecked — extends RuntimeException
public class InvalidSalaryException extends RuntimeException {
    public InvalidSalaryException(double salary) {
        super(String.format("Salary %.2f invalid. Must be 15000–10000000.", salary));
    }
}
```
Checked custom exceptions need `throws` in the signature; unchecked ones don't.

## 8. Exception Chaining
Preserve the original cause when wrapping one exception in another:
```java
public Employee loadFromDatabase(int id) throws EmployeeNotFoundException {
    try {
        return dbConnection.query("...", id);
    } catch (SQLException e) {
        throw new EmployeeNotFoundException(id, e);   // e preserved as cause
    }
}
```
```java
catch (EmployeeNotFoundException ex) {
    System.out.println(ex.getMessage());
    System.out.println("Caused by: " + ex.getCause().getMessage());
}
```
Without chaining, the original `SQLException` is lost.

---

## 9. Assertions
Developer sanity checks — **disabled by default in production**, must run with `-ea`.
```java
assert salary >= 0 : "Salary should never be negative, got: " + salary;
```
Use for: internal invariants, post-conditions. **Don't use for**: validating public method args (use `IllegalArgumentException`), production error handling, logic that must always run.

---

## 10. Best Practices

| Rule | Bad | Good |
|---|---|---|
| Catch specific | `catch (Exception e) {}` | `catch (IOException e) { ... }` |
| Never swallow | `catch (IOException e) { }` | Log/handle it — never empty |
| Not for flow control | `try { parseInt } catch { value=0 }` | Check with `isNumeric()` first |
| Clean up resources | manual `finally` | try-with-resources, always |
| Meaningful messages | `throw new IllegalArgumentException("ID")` | `"Employee ID must be positive, got: " + id` |

---

## Top Exceptions in Production

| Exception | Cause | Fix |
|---|---|---|
| `NullPointerException` | Method call on null | Null check / Optional |
| `ArrayIndexOutOfBoundsException` | Bad index | Bounds check |
| `ClassCastException` | Invalid downcast | `instanceof` check first |
| `NumberFormatException` | Parsing non-numeric string | Validate before parsing |
| `IllegalArgumentException` | Invalid method arg | Validate args upfront |
| `IllegalStateException` | Wrong-time method call | Check preconditions |
| `StackOverflowError` | Infinite recursion | Add base case |
| `OutOfMemoryError` | Heap exhausted | Fix leak / raise heap |
| `ConcurrentModificationException` | Mutating collection while iterating | Use iterator's remove |
| `IOException` | File/network failure | try-with-resources + catch |

---

## Practical Pattern

```java
public class EmployeeService {
    public void addEmployee(Employee emp) {
        if (emp == null) throw new IllegalArgumentException("Employee cannot be null.");
        if (employees.containsKey(emp.getId()))
            throw new IllegalStateException("Employee already exists with ID: " + emp.getId());
        employees.put(emp.getId(), emp);
    }

    public Employee getEmployee(int id) throws EmployeeNotFoundException {
        Employee emp = employees.get(id);
        if (emp == null) throw new EmployeeNotFoundException(id);
        return emp;
    }

    public void updateSalary(int id, double newSalary) throws EmployeeNotFoundException {
        if (newSalary < MIN_SALARY || newSalary > MAX_SALARY)
            throw new InvalidSalaryException(newSalary);   // unchecked
        getEmployee(id).setSalary(newSalary);                // checked — propagates
    }
}
```
Callers catch `EmployeeNotFoundException` (checked, declared) and `InvalidSalaryException` (unchecked, optional to catch) separately.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Checked | Compiler enforces handling — external failures |
| Unchecked | Runtime bugs — fix the code, don't just catch |
| `try-catch-finally` | `finally` always runs |
| `try-with-resources` | Auto-closes `AutoCloseable` — preferred |
| `throw` | Throw from your own code |
| `throws` | Declare checked exceptions a method may throw |
| Custom exception | Extend `Exception` (checked) or `RuntimeException` (unchecked) |
| Chaining | Pass original as `cause` — preserves diagnostic trail |
| Multi-catch | `catch (A \| B e)` for shared handling |
| Assertions | Dev-only checks, disabled by default |

## Next: Module 18 — Annotations