# Module 12 — Abstraction (Condensed)

> Part C: OOP · Prerequisites: Module 10, 11

## Core Idea
Abstraction = **define what something does, not how**. Expose the contract; let subclasses fill in the details.

| Tool | Description |
|---|---|
| `abstract` class | Partially implemented — mix of abstract + concrete methods |
| `interface` | Fully abstract contract (until Java 8 `default` methods) |

---

## 1. Abstract Classes

- **Cannot be instantiated** (`new Employee()` → compile error if abstract)
- Can have abstract methods (no body — must be implemented by subclass), concrete methods, fields, constructors, statics
- Subclass **must** implement all abstract methods, or itself become abstract

```java
public abstract class Employee {
    private int id; private String name; private double salary; private String department;

    public Employee(int id, String name, double salary, String department) { ... }

    public abstract double calculateBonus();   // MUST be implemented
    public abstract String getRole();

    public void applyRaise(double percent) {   // concrete — inherited as-is
        salary = salary * (1 + percent / 100);
    }
    public void display() { /* prints via getRole() etc. */ }
    // getters/setters...
}
```

**Concrete subclasses** implement the abstract methods with their own logic:
```java
public class Manager extends Employee {
    private int teamSize;
    @Override public double calculateBonus() { return getSalary()*0.20 + teamSize*500; }
    @Override public String getRole() { return "Manager"; }
}

public class Developer extends Employee {
    private String techStack; private int experienceYears;
    @Override public double calculateBonus() {
        double base = getSalary()*0.15;
        return experienceYears > 5 ? base*1.25 : base;
    }
    @Override public String getRole() { return "Developer"; }
}

public class Contractor extends Employee {
    private double dailyRate;
    public Contractor(...) { super(id, name, 0, department); }  // salary N/A
    @Override public double calculateBonus() { return 0; }
    @Override public String getRole() { return "Contractor"; }
}
```

Used polymorphically:
```java
Employee[] team = { new Manager(...), new Developer(...), new Contractor(...) };
for (Employee e : team) {
    double bonus = e.calculateBonus();   // each subtype's own logic runs
}
```

---

## 2. Interfaces

A **pure contract** — no implementation (pre-Java 8).

| Aspect | Rule |
|---|---|
| Methods | Implicitly `public abstract` (unless `default`/`static`) |
| Fields | Implicitly `public static final` (constants only) |
| Keyword | `implements` (not `extends`) — **multiple allowed** |
| Instantiation | Never; no constructors |

```java
public interface Payable {
    double calculateNetPay();
    void   generatePayslip();
    double TAX_RATE = 0.10;   // public static final
}

public interface Reviewable {
    void   conductReview(String comments);
    String getLastReviewSummary();
}
```

**Implementing multiple interfaces:**
```java
public class Manager extends Employee implements Payable, Reviewable {
    @Override public double calculateBonus() { return getSalary()*0.20; }
    @Override public String getRole() { return "Manager"; }

    @Override public double calculateNetPay() {
        return getSalary() + calculateBonus() - getSalary()*Payable.TAX_RATE;
    }
    @Override public void generatePayslip() { /* print breakdown */ }

    @Override public void conductReview(String comments) { this.lastReview = comments; }
    @Override public String getLastReviewSummary() { return lastReview; }
}
```

**Interface as a type** — restricts you to that contract only:
```java
Payable p = new Manager(...);
p.calculateNetPay();     // ok
p.conductReview("...");   // ❌ compile error — Payable doesn't declare this
```

---

## 3. Java 8: `default` and `static` Interface Methods
Solves the "adding a method breaks all implementers" problem.

**`default`** — has a body; implementers inherit it or override it:
```java
public interface Payable {
    double calculateNetPay();
    default String getPaymentMode() { return "Bank Transfer"; }  // overridable default
}

class Contractor implements Payable {
    // doesn't override → uses default "Bank Transfer"
}
class SalesRep implements Payable {
    @Override public String getPaymentMode() { return "Cheque"; }  // overrides
}
```

**`static`** — belongs to the interface itself, called via interface name:
```java
public interface Payable {
    static double calculateTax(double salary) { return salary * 0.10; }
}
double tax = Payable.calculateTax(75000);   // Payable.method(), not instance.method()
```

---

## 4. Abstract Class vs Interface

| Aspect | Abstract Class | Interface |
|---|---|---|
| Instantiation | No | No |
| Fields | Any type | `public static final` only |
| Constructors | Yes | No |
| Methods | abstract + concrete | abstract + default + static |
| Inheritance | `extends` (single) | `implements` (multiple) |
| Use when | Shared **state** + partial implementation | Shared **contract** across unrelated classes |

- **Abstract class**: classes share common fields/behavior (e.g. `Employee` — id, name, salary, `applyRaise()` shouldn't be duplicated).
- **Interface**: a capability mixed into unrelated hierarchies (e.g. `Payable` could apply to `Employee`, `Vendor`, `Contractor` — no common ancestor needed).

---

## 5. Interface Segregation
Split fat interfaces by concern — classes implement only what they need:

```java
public interface Payable    { double calculateNetPay(); void generatePayslip(); }
public interface Reviewable { void conductReview(String c); String getLastReviewSummary(); }
public interface Trainable  { void assignTraining(String course); boolean hasCompletedTraining(String course); }

class Manager extends Employee implements Payable, Reviewable, Trainable { }  // needs all
class Contractor extends Employee implements Payable { }                       // pay only
class Intern extends Employee implements Trainable { }                         // training only
```

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Abstract class | Cannot instantiate; mix of abstract + concrete members |
| Abstract method | No body — subclass implements or stays abstract |
| Interface | Pure contract, `implements`, multiple allowed |
| `default` method | Interface method with body (Java 8+), overridable |
| `static` interface method | Called as `InterfaceName.method()` |
| Abstract class fields | Any visibility, mutable |
| Interface fields | Always `public static final` constants |
| Pick abstract class | Shared state + partial implementation |
| Pick interface | Shared contract, unrelated classes, multiple inheritance |

## Next: Module 13 — Encapsulation