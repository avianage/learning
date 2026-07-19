# Module 15 — Inner Classes (Condensed)

> Part D: Core Java Toolkit · Prerequisites: Module 08–13

## Core Idea
Four kinds of classes defined inside another class:

| Kind | Defined | Access | Use When |
|---|---|---|---|
| Regular inner | Inside class body | All outer members (incl. `private`) | Helper tightly coupled to outer class |
| Static nested | Inside class body + `static` | Only outer static members | Logically grouped, independent helper |
| Method-local | Inside a method body | Local `final`/effectively-final vars | Narrow, one-method use case |
| Anonymous | Inline at point of use | Local `final`/effectively-final vars | One-off interface/abstract-class impl |

---

## 1. Regular Inner Class
No `static` — has full access to outer instance members, including `private`.

```java
public class Department {
    private String name; private double budget;

    public class BudgetReport {           // regular inner class
        private String reportedBy;
        public void print() {
            System.out.println(name);      // direct access to outer field
            System.out.println(budget);
        }
    }
}
```

**Requires an outer instance to create:**
```java
Department dept = new Department("Engineering", 500000.0);
Department.BudgetReport report = dept.new BudgetReport("Ponu");
report.print();
```

**Shadowing:** use `Outer.this.field` to disambiguate:
```java
class Department {
    private String name = "Engineering";
    class BudgetReport {
        private String name = "Q1 Report";
        void print() {
            System.out.println(name);                // "Q1 Report" (inner)
            System.out.println(Department.this.name); // "Engineering" (outer)
        }
    }
}
```

---

## 2. Static Nested Class
`static` — no reference to an outer instance; just logically grouped.

```java
public class Employee {
    private double salary;
    public double getSalary() { return salary; }

    public static class PayrollSummary {     // static nested
        private int count; private double totalSalary;
        public void add(Employee e) { count++; totalSalary += e.getSalary(); } // via getter
        public void print() { /* ... */ }
    }
}
```

**No outer instance needed:**
```java
Employee.PayrollSummary summary = new Employee.PayrollSummary();
summary.add(new Employee(101, "Sonu", 75000));
```

**Regular Inner vs Static Nested**

| Aspect | Regular Inner | Static Nested |
|---|---|---|
| Outer instance required? | Yes | No |
| Access to outer instance fields | Directly | Only via a reference |
| Memory | Holds implicit outer ref | Independent |
| Use case | Needs outer's state | Grouped but independent |

---

## 3. Method-Local Inner Class
Defined inside a method; visible only there. Can access `final`/effectively-final locals.

```java
public void processForDepartment(String deptName, double[] salaries) {
    final double TAX_RATE = 0.10;

    class SalaryFormatter {                 // method-local
        void print(int i, double gross) {
            double net = gross - gross * TAX_RATE;   // captures TAX_RATE
        }
    }
    SalaryFormatter formatter = new SalaryFormatter();
    // use formatter in the loop...
}
```
Rarely used in modern Java — lambdas cover most cases. Still useful for a one-off helper needing **multiple methods**.

---

## 4. Anonymous Inner Class
No name; declared + instantiated in one expression. One-off implementation of an interface/abstract class.

```java
public interface Printable { void print(); }

Printable p = new Printable() {           // anonymous class
    @Override public void print() { System.out.println("printed"); }
};
p.print();
```

**Classic use — `Comparator` (pre-Java 8):**
```java
Arrays.sort(employees, new Comparator<Employee>() {
    @Override
    public int compare(Employee a, Employee b) {
        return Double.compare(b.getSalary(), a.getSalary());
    }
});
```

**Lambda equivalent (Java 8+, Module 19):**
```java
Arrays.sort(employees, (a, b) -> Double.compare(b.getSalary(), a.getSalary()));
```

**Anonymous classes still needed when:**
- Interface has **more than one method** (lambdas only work for single-method/functional interfaces)
- Implementation needs **instance state** (fields)
- Implementing an **abstract class**, not an interface

---

## Quick Reference

| Type | Keyword | Outer Instance? | Access | Use When |
|---|---|---|---|---|
| Regular inner | none | Required | All outer members | Tightly coupled helper |
| Static nested | `static` | Not needed | Outer static only | Grouped, independent |
| Method-local | none (in method) | Depends | Effectively-final locals | One-off, multi-method helper |
| Anonymous | none (inline) | Depends | Effectively-final locals | One-off single implementation |

### Modern Replacements

| Old Way | Modern |
|---|---|
| Anonymous `Runnable` | `() -> { ... }` |
| Anonymous `Comparator` | `(a, b) -> ...` |
| Anonymous single-method interface | Lambda |
| Method-local class, one method | Lambda |

Anonymous/method-local classes are less common post-Java 8; regular inner and static nested classes remain widely used.

## Next: Module 16 — Enums (type-safe constants, methods, fields, switch support)