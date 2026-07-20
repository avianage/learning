# Module 11 — Polymorphism (Condensed)

> Part C: OOP · Prerequisites: Module 10

## Core Idea
Polymorphism = "many forms." One reference type (e.g. `Employee`) can point to different actual object types (`Manager`, `Developer`), and the **correct behavior is picked automatically** — no need to know which subtype you're dealing with.

| Type | Also Called | Resolved At |
|---|---|---|
| Method overloading | Compile-time / Static | Compile time |
| Method overriding | Runtime / Dynamic | Runtime |

---

## 1. Compile-Time Polymorphism — Overloading
Compiler picks the method based on **number/type of arguments**.

```java
double calculateBonus(double salary) { return salary * 0.10; }
double calculateBonus(double salary, double percent) { return salary * (percent/100); }
double calculateBonus(double salary, double percent, int years) { ... }
```
- Resolved at compile time, based on the call site.
- **Not overloading:** changing only the return type → compile error.

---

## 2. Runtime Polymorphism — Overriding + Dynamic Dispatch
Parent reference holding a child object → **the child's method runs**, chosen at runtime by the JVM.

```java
Employee e = new Manager(201, "Ponu", 110000, "Engineering", 8, "Apollo");
e.applyRaise(10);  // Manager's version runs
```

**Why it matters:** process a mixed `Employee[]` array uniformly —
```java
for (Employee e : team) e.display();       // each type's own display() runs
for (Employee e : team) e.applyRaise(10);   // Developer gets 15%, others 10%
```
Without polymorphism you'd need ugly `instanceof` + cast chains for every type.

---

## 3. Upcasting vs Downcasting

**Upcasting** — child → parent ref. Implicit, always safe.
```java
Employee e = new Manager(...);   // no cast needed
e.display();        // ok
e.conductReview();   // ❌ compile error — Employee doesn't declare this
```

**Downcasting** — parent → child ref. Explicit, risky.
```java
Manager m = (Manager) e;   // needed to call conductReview()
```
- Wrong downcast compiles but throws `ClassCastException` at runtime.
- **Always guard with `instanceof`:**
```java
if (e instanceof Manager m) {       // Java 16+ pattern matching (check+cast in one)
    m.conductReview();
}
```
(Full coverage of pattern-matching `instanceof` → Module 30.)

---

## 4. How Dynamic Dispatch Works (vtable)
Each class gets a **virtual method table** mapping method names → actual implementations.

`e.display()` on an `Employee` ref holding a `Manager`:
1. JVM checks the **actual runtime type** → `Manager`
2. Looks up `display()` in `Manager`'s vtable
3. Calls `Manager.display()` (or falls back to `Employee.display()` if not overridden)

→ JVM always dispatches based on actual object type, never the reference type.

---

## 5. Trap: Overridable Methods in Constructors
Calling an overridden method from a **parent constructor** invokes the **child's version**, before the child's fields are initialized.

```java
class Employee {
    Employee() { printDetails(); }        // calls subclass override — dangerous
    void printDetails() { ... }
}
class Manager extends Employee {
    String projectName = "Apollo";
    @Override void printDetails() {
        System.out.println("Project: " + projectName);  // null! not initialized yet
    }
}
```
**Rule:** never call overridable methods from constructors — use `private`/`final` methods instead.

---

## 6. Practical Pattern — Payroll Processor
```java
static void processPayroll(Employee[] employees, double raisePercent) {
    for (Employee e : employees) {
        double before = e.getSalary();
        e.applyRaise(raisePercent);          // polymorphic call
        double after = e.getSalary();
        String type = e.getClass().getSimpleName();  // actual runtime type
        // print before/after
    }
}
```
`processPayroll` doesn't know or care which subtype it's handling — each `applyRaise()` call resolves to the correct override (e.g. Developers get +5% extra).

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Overloading | Same name, different params — compiler resolves |
| Overriding | Same signature, subclass redefines — JVM resolves at runtime |
| Upcasting | Implicit, safe, restricts you to parent's API |
| Downcasting | Explicit, unsafe without `instanceof` check |
| `getClass().getSimpleName()` | Actual runtime type as String |
| Dynamic dispatch | Always uses actual object's vtable, not reference type |
| Constructors + overriding | Never call overridable methods there |

## Next: Module 12 — Abstraction (abstract classes & interfaces)