# Module 13 — Encapsulation (Condensed)

> Part C: OOP · Prerequisites: Module 08, 09

## Core Idea
Encapsulation = **bundle data + the methods that operate on it, and control access**. An object manages its own state; outside code must go through its methods, not touch fields directly. (Vending machine analogy: press a button, get a result — you can't reach in and grab whatever.)

---

## 1. The Problem Without It

```java
public class Employee {
    public int id; public String name; public double salary;  // public — no protection
}

e.salary = -99999;   // valid, no one stops this
e.name   = null;     // valid — corrupt state, no complaints
```
No way to guarantee valid data. Any code anywhere can break the object.

---

## 2. The Fix — Private Fields + Validated Setters

```java
public class Employee {
    private int id; private String name; private double salary; private String department;

    public Employee(int id, String name, double salary, String department) {
        setId(id); setName(name); setSalary(salary); setDepartment(department);
    }

    public int getId() { return id; }
    // ... other getters

    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive: " + id);
        this.id = id;
    }
    public void setSalary(double salary) {
        if (salary < 0) throw new IllegalArgumentException("Salary cannot be negative: " + salary);
        this.salary = salary;
    }
    // setName/setDepartment similarly validate null/blank
}
```
```java
e.setSalary(-5000);   // throws IllegalArgumentException — caught at runtime
e.setSalary(82000);   // valid — accepted
```
The object now enforces its own rules.

---

## 3. Read-Only and Write-Only Fields

**Read-only** (getter only) — for identity fields that shouldn't change:
```java
private final int id;
private final String name;
public int getId() { return id; }      // no setId()
public String getName() { return name; }
```

**Write-only** (setter only) — for sensitive data:
```java
public class UserAccount {
    private String passwordHash;
    public void setPassword(String plain) { this.passwordHash = hash(plain); }
    // no getPassword()
}
```

---

## 4. Immutable Classes
Most locked-down form of encapsulation — state can never change after creation.

**Rules:**
1. Class `final`
2. All fields `private final`
3. No setters
4. Init all fields in constructor
5. Defensive copy any mutable field on getter

```java
public final class EmployeeSnapshot {
    private final int id; private final String name;
    private final double salary; private final String department;

    public EmployeeSnapshot(int id, String name, double salary, String department) {
        // validate...
        this.id = id; this.name = name; this.salary = salary; this.department = department;
    }
    public int getId() { return id; }
    // ...other getters

    // "modification" returns a NEW object
    public EmployeeSnapshot withSalary(double newSalary) {
        return new EmployeeSnapshot(id, name, newSalary, department);
    }
}
```
```java
EmployeeSnapshot snap = new EmployeeSnapshot(101, "Sonu", 75000, "Engineering");
EmployeeSnapshot raised = snap.withSalary(82000);
// snap unchanged (75000), raised is a new object (82000)
```
Benefits: thread-safe by default, safe to share freely, ideal for value objects. `String` is Java's canonical immutable class.

---

## 5. Defensive Copies for Mutable Fields
If a field is a mutable object (array, List), clone on both input and output — otherwise callers can mutate your internal state through the reference you handed out.

```java
public final class Project {
    private final String name;
    private final String[] members;

    public Project(String name, String[] members) {
        this.name = name;
        this.members = members.clone();   // copy on input
    }
    public String[] getMembers() { return members.clone(); }   // copy on output
}
```
```java
String[] team = {"Sonu", "Monu"};
Project p = new Project("Apollo", team);
team[0] = "HACKED";                    // mutating the original array
p.getMembers()[0];                     // still "Sonu" — Project unaffected
```

---

## 6. JavaBeans Convention
Standard pattern used by frameworks (Spring, Hibernate, JPA) via reflection:
- Private fields
- Public no-arg constructor
- Getters: `getFieldName()` (or `isFieldName()` for booleans)
- Setters: `setFieldName(value)`
- Often `implements Serializable`

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; private boolean active;

    public Employee() { }                      // required no-arg ctor
    public int getId() { return id; }
    public boolean isActive() { return active; }  // 'is' prefix for booleans
    public void setId(int id) { this.id = id; }
    public void setActive(boolean active) { this.active = active; }
}
```
Naming matters — frameworks call these via reflection.

---

## 7. Encapsulation ≠ Just Getters/Setters
Mechanical private fields + no-op getters/setters is **not** real encapsulation — it's boilerplate ceremony.

```java
// NOT encapsulation — no validation, might as well be public
public void setSalary(double salary) { this.salary = salary; }

// IS encapsulation — enforces invariants
public void setSalary(double salary) {
    if (salary < MIN_SALARY || salary > MAX_SALARY)
        throw new IllegalArgumentException("Salary out of range: " + salary);
    this.salary = salary;
}

// IS encapsulation — business logic lives in the class
public void applyRaise(double percent) {
    if (percent < 0 || percent > 50)
        throw new IllegalArgumentException("Raise percent out of range: " + percent);
    this.salary *= (1 + percent / 100);
}
```
Real encapsulation = the class **owns and enforces** its own state.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Private fields | Foundation — data hidden from outside |
| Getters/setters | Controlled access; validation belongs in setters |
| Read-only field | Getter only, no setter |
| Immutable class | `final` class + fields, no setters, defensive copies |
| Defensive copy | Clone mutable in/out to prevent state leakage |
| JavaBeans | No-arg ctor + `getX()`/`setX()`/`isX()` convention |
| True encapsulation | Class enforces its own invariants, not just wraps fields |

---

## OOP Pillars — Complete

| Pillar | Module | Core Idea |
|---|---|---|
| Encapsulation | 13 | Hide data, control access through methods |
| Inheritance | 10 | Acquire and extend parent behavior |
| Polymorphism | 11 | One interface, many implementations |
| Abstraction | 12 | Define what, not how |

## Next: Module 14 — Object Class Methods (`equals()`, `hashCode()`, `toString()`, `clone()`, threading methods)