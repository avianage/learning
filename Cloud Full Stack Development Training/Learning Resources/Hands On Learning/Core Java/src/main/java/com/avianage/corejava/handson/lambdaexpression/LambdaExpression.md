# Module 19 — Lambda Expressions (Condensed)

> Part E: Java 8 Features · Prerequisites: Module 15, 17

## The Problem Lambdas Solve
Pre-Java 8, passing behavior meant an anonymous class — 6 lines to say "compare by salary":
```java
Arrays.sort(employees, new Comparator<Employee>() {
    @Override public int compare(Employee a, Employee b) {
        return Double.compare(a.getSalary(), b.getSalary());
    }
});
```
With a lambda — 1 line:
```java
Arrays.sort(employees, (a, b) -> Double.compare(a.getSalary(), b.getSalary()));
```

## Core Idea
A lambda = **anonymous function**: `(parameters) -> body`. Only usable where a **functional interface** (exactly one abstract method) is expected — the lambda supplies that method's implementation.

---

## 1. Syntax Forms

```java
(Employee a, Employee b) -> { return Double.compare(a.getSalary(), b.getSalary()); }  // full
(a, b) -> { return Double.compare(a.getSalary(), b.getSalary()); }                     // types inferred
(a, b) -> Double.compare(a.getSalary(), b.getSalary())                                 // single expr, no braces/return
name -> name.toUpperCase()                                                              // single param, no parens needed
() -> System.out.println("Processing...")                                               // no params — () required
(Employee e) -> {                                                                        // multi-line — braces + return required
    double bonus = e.getSalary() * 0.10;
    return bonus;
}
```

## 2. Evolution: Named Class → Anonymous → Lambda
All four below do the same thing:
```java
// Named class
class SalaryComparator implements Comparator<Employee> {
    public int compare(Employee a, Employee b) { return Double.compare(a.getSalary(), b.getSalary()); }
}
// Anonymous class
new Comparator<Employee>() { public int compare(Employee a, Employee b) { ... } };
// Lambda (full)
(Employee a, Employee b) -> { return Double.compare(a.getSalary(), b.getSalary()); };
// Lambda (concise)
(a, b) -> Double.compare(a.getSalary(), b.getSalary());
```
A lambda is just concise syntax for implementing a single-method interface — not magic.

---

## 3. Built-in Functional Interfaces (preview — full catalogue in Module 20)

```java
Runnable task = () -> System.out.println("Payroll started.");           // no params, no return
Predicate<Employee> isSenior = e -> e.getSalary() > 80000;               // T -> boolean
Function<Employee, String> getName = e -> e.getName().toUpperCase();     // T -> R
Consumer<Employee> printer = e -> System.out.println(e.getName());       // T -> void
```

---

## 4. Capturing Variables
Lambdas can reference enclosing-scope variables, but they must be **effectively final** (never reassigned after being read by the lambda).

```java
double taxRate = 0.10;                                    // effectively final
Function<Double, Double> netCalc = salary -> salary - salary * taxRate;
// taxRate = 0.15;  // ❌ compile error — already captured
```
Reason: lambdas may run on a different thread/time — a mutable capture risks race conditions.

Instance/static fields have **no such restriction**:
```java
public class PayrollCalculator {
    private double taxRate = 0.10;   // instance field — fine to use inside lambda
    public void calculate(List<Employee> emps) {
        emps.forEach(e -> { double net = e.getSalary() - e.getSalary() * taxRate; });
    }
}
```

---

## 5. Method References
Shorthand when the lambda body just calls an existing method.

| Type | Lambda | Method Reference |
|---|---|---|
| Static method | `s -> Integer.parseInt(s)` | `Integer::parseInt` |
| Specific instance | `() -> emp.getName()` | `emp::getName` |
| Arbitrary instance of a type | `e -> e.getRole()` | `Employee::getRole` |
| Constructor | `(n, s) -> new Employee(n, s)` | `Employee::new` |

Most common in streams:
```java
employees.stream()
         .map(Employee::getName)          // e -> e.getName()
         .forEach(System.out::println);   // s -> System.out.println(s)
```

---

## 6. Lambdas vs Anonymous Classes

| Aspect | Anonymous Class | Lambda |
|---|---|---|
| `this` | The anonymous class instance | The enclosing class instance |
| Scope | New scope | Shares enclosing scope |
| State | Can have instance fields | Stateless |
| Interface | Multi-method OK | Single-abstract-method only |
| Compilation | Generates `.class` file | `invokedynamic`, no class file |

```java
Runnable r1 = new Runnable() { public void run() { System.out.println(this.getClass()); } }; // ScopeDemo$1
Runnable r2 = () -> System.out.println(this.name);   // 'this' = enclosing ScopeDemo instance
```

---

## 7. Practical Pattern
```java
employees.sort((a, b) -> Double.compare(b.getSalary(), a.getSalary()));      // sort desc by salary

Predicate<Employee> highEarner = e -> e.getSalary() > 70000;
employees.stream().filter(highEarner).forEach(e -> System.out.println(e.getName()));

Consumer<Employee> applyRaise = e -> e.applyRaise(10);
employees.forEach(applyRaise);

employees.stream().map(Employee::getName).forEach(System.out::println);
```

---

## 8. Composing Lambdas

**Predicates** — `and()`, `or()`, `negate()`:
```java
Predicate<Employee> isEngineering = e -> e.getDepartment().equals("Engineering");
Predicate<Employee> isHighSalary  = e -> e.getSalary() > 75000;
Predicate<Employee> combined = isEngineering.and(isHighSalary);
```

**Functions** — `andThen()` (this, then next), `compose()` (arg first, then this):
```java
Function<Double, Double> applyTax   = s -> s * 0.90;
Function<Double, Double> applyBonus = s -> s * 1.10;

Function<Double, Double> taxThenBonus = applyTax.andThen(applyBonus);  // tax first, then bonus
Function<Double, Double> bonusThenTax = applyTax.compose(applyBonus);  // bonus first, then tax
```

---

## Quick Reference

| Syntax | When to Use |
|---|---|
| `() -> expr` | No params, returns value |
| `() -> { stmts; }` | No params, multiple statements |
| `x -> expr` | One param, returns value |
| `(x, y) -> expr` | Two params, returns value |
| `(x, y) -> { stmts; return val; }` | Two params, multi-statement |
| `ClassName::staticMethod` | Static method reference |
| `instance::method` | Specific instance method reference |
| `ClassName::instanceMethod` | Arbitrary instance method reference |
| `ClassName::new` | Constructor reference |

| Concept | Key Point |
|---|---|
| Lambda requires | A functional interface — exactly one abstract method |
| Captured variables | Must be effectively final |
| `this` in lambda | Refers to enclosing class, not the lambda |
| Method reference | Shorthand for a lambda that calls an existing method |
| Composition | `and()`, `or()`, `negate()`, `andThen()`, `compose()` |

## Next: Module 20 — Functional Interfaces (full `java.util.function` catalogue)