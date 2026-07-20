# Module 16 — Enums (Condensed)

> Part D: Core Java Toolkit · Prerequisites: Module 08, 09

## The Problem With Old-Style Constants

```java
public class EmployeeStatus {
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 2;
    // ...
}
```
Issues: no type safety (`status = 99` compiles), no meaningful print (shows `1`, not `"ACTIVE"`), no attachable behavior, namespace pollution. Enums fix all of this.

---

## 1. Basic Enum

```java
public enum EmployeeStatus { ACTIVE, INACTIVE, TERMINATED, ON_LEAVE }
```
`EmployeeStatus` is now a **type** — only these four values are valid; anything else is a compile error.

```java
EmployeeStatus status = EmployeeStatus.ACTIVE;
System.out.println(status);           // ACTIVE
System.out.println(status.name());    // ACTIVE
System.out.println(status.ordinal()); // 0 — zero-based declaration position
```

---

## 2. Enum in `switch`

```java
switch (status) {
    case ACTIVE -> System.out.println("Full access");
    case ON_LEAVE -> System.out.println("Read-only access");
    case INACTIVE, TERMINATED -> System.out.println("No access");
}
```
Traditional `case X: ... break;` also works; Java 14+ arrow syntax is cleaner and supports multiple labels per case, and can return a value directly:
```java
String access = switch (status) {
    case ACTIVE -> "Full access";
    case ON_LEAVE -> "Read-only";
    case INACTIVE, TERMINATED -> "No access";
};
```

---

## 3. Enum With Fields and Methods
An enum is a class under the hood — constants can carry data + behavior.

```java
public enum Department {
    ENGINEERING("Engineering", "Pune", 500000.0),
    HR("Human Res.", "Mumbai", 200000.0);

    private final String displayName; private final String location; private final double budget;

    Department(String displayName, String location, double budget) {
        this.displayName = displayName; this.location = location; this.budget = budget;
    }

    public String getDisplayName() { return displayName; }
    public boolean isInCity(String city) { return location.equalsIgnoreCase(city); }
    public String budgetCategory() {
        return budget >= 400000 ? "High" : budget >= 250000 ? "Medium" : "Low";
    }
}
```
```java
for (Department d : Department.values()) {   // iterate all constants
    System.out.println(d.getDisplayName() + " " + d.budgetCategory());
}
```

---

## 4. Enum With Abstract Methods
Each constant overrides the method with its own implementation:

```java
public enum JobGrade {
    JUNIOR {
        @Override public double applyBonus(double salary) { return salary * 0.05; }
        @Override public String describe() { return "Junior (0-2 yrs)"; }
    },
    SENIOR {
        @Override public double applyBonus(double salary) { return salary * 0.20; }
        @Override public String describe() { return "Senior (5+ yrs)"; }
    };

    public abstract double applyBonus(double salary);
    public abstract String describe();
}
```
```java
for (JobGrade g : JobGrade.values()) {
    System.out.println(g.describe() + " → " + g.applyBonus(75000));  // per-constant logic
}
```

---

## 5. Built-in Enum Methods

```java
EmployeeStatus[] all = EmployeeStatus.values();          // all constants, array
EmployeeStatus s = EmployeeStatus.valueOf("ACTIVE");      // by name; throws if invalid
EmployeeStatus.ACTIVE.name();                             // "ACTIVE"
EmployeeStatus.TERMINATED.ordinal();                      // 2
EmployeeStatus.ACTIVE.compareTo(EmployeeStatus.TERMINATED); // compares by ordinal
```

---

## 6. `EnumSet` — Fast Set of Enum Constants
Bit-vector backed, much faster than `HashSet<EnumType>`.

```java
EnumSet<EmployeeStatus> all      = EnumSet.allOf(EmployeeStatus.class);
EnumSet<EmployeeStatus> active   = EnumSet.of(EmployeeStatus.ACTIVE, EmployeeStatus.ON_LEAVE);
EnumSet<EmployeeStatus> range    = EnumSet.range(EmployeeStatus.ACTIVE, EmployeeStatus.ON_LEAVE);
EnumSet<EmployeeStatus> inactive = EnumSet.complementOf(active);   // everything NOT in active
```

---

## 7. `EnumMap` — Fast Map With Enum Keys
Optimized for enum keys, faster than `HashMap<EnumType, V>`.

```java
EnumMap<Department, List<String>> deptEmployees = new EnumMap<>(Department.class);
deptEmployees.put(Department.ENGINEERING, new ArrayList<>(List.of("Sonu", "Monu")));
deptEmployees.forEach((dept, names) -> System.out.println(dept.getDisplayName() + " " + names));
```

---

## 8. Enum vs Old Constants

| Aspect | `static final int/String` | Enum |
|---|---|---|
| Type safety | No | Yes |
| Readable print | No (prints 1, 2, 3) | Yes (prints ACTIVE) |
| Switch support | Yes | Yes, cleaner |
| Methods/fields | No | Yes |
| Iteration | Manual array | `values()` |
| Singleton guarantee | No | Yes — each constant is one instance |

---

## 9. Practical Pattern — Enum Fields in a Class

```java
public class Employee {
    private Department department; private EmployeeStatus status; private JobGrade grade;

    public double calculateBonus() {
        if (status != EmployeeStatus.ACTIVE) return 0;
        return grade.applyBonus(salary);       // delegates to enum's own logic
    }
}
```
`calculateBonus()` doesn't need any if/else on grade — the enum constant itself decides the bonus rate via its overridden `applyBonus()`.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Basic enum | Type-safe named constants; only declared values valid |
| `values()` | All constants as array |
| `valueOf(String)` | Constant by name, throws if not found |
| `name()` / `ordinal()` | Name string / zero-based position |
| Enum + fields | Constructor + fields — each constant carries data |
| Enum + abstract method | Each constant supplies its own implementation |
| `EnumSet` | Fast set — prefer over `HashSet<EnumType>` |
| `EnumMap` | Fast map — prefer over `HashMap<EnumType, V>` |

## Next: Module 17 — Exception Handling