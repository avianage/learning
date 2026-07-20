# Module 20 — Functional Interfaces (Condensed)

> Part E: Java 8 Features · Prerequisites: Module 19

## Core Idea
A functional interface has **exactly one abstract method** — the contract a lambda implements.

```java
@FunctionalInterface
public interface SalaryCalculator {
    double calculate(double baseSalary);   // the one abstract method
}
SalaryCalculator withBonus = salary -> salary * 1.10;
```
`@FunctionalInterface` is optional but recommended — compiler enforces the single-method rule.

Java ships **43** functional interfaces in `java.util.function`. Core set below.

---

## 1. `Predicate<T>` — test a condition
`boolean test(T t)`

```java
Predicate<Employee> isSenior = e -> e.getSalary() > 80000;
isSenior.test(sonu);   // true/false
```

**Composition:**
```java
Predicate<Employee> seniorEngineer = isEngineering.and(isSenior);
Predicate<Employee> either = isEngineering.or(isSenior);
Predicate<Employee> not = isEngineering.negate();
Predicate<String> notBlank = Predicate.not(String::isBlank);   // Java 11+
```

**`BiPredicate<T,U>`** (two args): `(e, threshold) -> e.getSalary() > threshold`

---

## 2. `Function<T,R>` — transform a value
`R apply(T t)`

```java
Function<Employee, String> getName = Employee::getName;
Function<Employee, String> salaryLabel = e -> e.getName() + ": " + e.getSalary();
```

**Composition:**
```java
Function<Double, Double> netWithAllowance = deductTax.andThen(addAllowance);  // deductTax → addAllowance
Function<Double, Double> allowanceThenTax = deductTax.compose(addAllowance);  // addAllowance → deductTax
```

`Function.identity()` — returns input unchanged, useful as a neutral placeholder.

**`BiFunction<T,U,R>`** (two args):
```java
BiFunction<Employee, Double, String> appraisal =
    (e, target) -> e.getSalary() >= target ? "meets target" : "below target";
```

**`UnaryOperator<T>` / `BinaryOperator<T>`** — `Function` specializations where input/output types match:
```java
UnaryOperator<Double>  applyTax = salary -> salary * 0.90;
BinaryOperator<Double> totalPay = (base, bonus) -> base + bonus;
```

---

## 3. `Consumer<T>` — do something with a value, no return
`void accept(T t)`

```java
Consumer<Employee> printEmployee = e -> System.out.printf("%s %.2f%n", e.getName(), e.getSalary());
Consumer<Employee> applyRaise = e -> e.applyRaise(10);
```

**Chaining:** `logEmployee.andThen(printEmployee)` — runs both, in order.

**`BiConsumer<T,U>`** (two args): `(e, project) -> System.out.println(e.getName() + " → " + project)`

---

## 4. `Supplier<T>` — provide a value, no input
`T get()`

```java
Supplier<Employee> defaultEmployee = () -> new Employee(0, "Placeholder", 0, "Unassigned");
Supplier<List<Employee>> emptyList = ArrayList::new;
```

**Lazy evaluation** — avoids creating an expensive default unless actually needed:
```java
// Eager — defaultValue always constructed, even if unused
Employee getOrDefault(int id, Employee defaultValue) { ... }

// Lazy — only constructed if found == null
Employee getOrDefault(int id, Supplier<Employee> defaultSupplier) {
    Employee found = find(id);
    return found != null ? found : defaultSupplier.get();
}
```

---

## 5. Generic Helper Pattern
```java
static <T> List<T> filter(List<T> list, Predicate<T> pred) { ... }
static <T, R> List<R> transform(List<T> list, Function<T, R> fn) { ... }
static <T> void process(List<T> list, Consumer<T> action) { ... }

List<Employee> senior = filter(employees, e -> e.getSalary() > 70000);
List<String> names = transform(employees, Employee::getName);
process(senior, e -> e.applyRaise(15));
```

---

## 6. Primitive Specializations
Avoid autoboxing overhead when working with primitives.

| Generic | int | long | double |
|---|---|---|---|
| `Predicate<T>` | `IntPredicate` | `LongPredicate` | `DoublePredicate` |
| `Function<T,R>` | `IntFunction<R>` | `LongFunction<R>` | `DoubleFunction<R>` |
| `Consumer<T>` | `IntConsumer` | `LongConsumer` | `DoubleConsumer` |
| `Supplier<T>` | `IntSupplier` | `LongSupplier` | `DoubleSupplier` |
| `UnaryOperator<T>` | `IntUnaryOperator` | `LongUnaryOperator` | `DoubleUnaryOperator` |

```java
IntPredicate isAdultAge = age -> age >= 18;
IntSupplier defaultId = () -> 0;
```

---

## 7. Custom Functional Interfaces
For behavior that doesn't fit the standard set:
```java
@FunctionalInterface
public interface TriFunction<A, B, C, R> { R apply(A a, B b, C c); }

TriFunction<String, Double, String, Employee> creator =
    (name, salary, dept) -> new Employee(0, name, salary, dept);
```

---

## Quick Reference

| Interface | Method | Takes | Returns | Use For |
|---|---|---|---|---|
| `Predicate<T>` | `test(T)` | T | boolean | Filtering, conditions |
| `BiPredicate<T,U>` | `test(T,U)` | T, U | boolean | Two-arg conditions |
| `Function<T,R>` | `apply(T)` | T | R | Transforming values |
| `BiFunction<T,U,R>` | `apply(T,U)` | T, U | R | Two-arg transforms |
| `UnaryOperator<T>` | `apply(T)` | T | T | Transform same type |
| `BinaryOperator<T>` | `apply(T,T)` | T, T | T | Combine two same-type values |
| `Consumer<T>` | `accept(T)` | T | void | Side effects, printing, saving |
| `BiConsumer<T,U>` | `accept(T,U)` | T, U | void | Two-arg side effects |
| `Supplier<T>` | `get()` | — | T | Lazy creation, defaults |
| `Runnable` | `run()` | — | void | Background tasks |

## Next: Module 21 — Stream API (filter, map, reduce, collect)