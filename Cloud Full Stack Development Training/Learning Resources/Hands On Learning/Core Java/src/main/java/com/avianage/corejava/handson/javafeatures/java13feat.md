# Module 29 — Java 13 and 14 Features (Condensed)

> Part I: Modern Java · Prerequisites: Module 28 · Requires JDK 14+ (text blocks standard in 15)

## What Changed

| Feature | Introduced | Standard |
|---|---|---|
| Text Blocks | Java 13 (preview) | Java 15 |
| Switch Expressions | Java 12 (preview) | Java 14 |
| Helpful NullPointerExceptions | Java 14 | Java 14 |
| `instanceof` pattern matching / Records | Java 14 (preview) | Java 16 (covered in Module 30) |

---

## 1. Text Blocks — `"""`

**Problem (pre-Java 13):**
```java
String json = "{\n" + "    \"id\": 101,\n" + "    \"name\": \"Sonu\"\n" + "}";  // escaping hell
```

**Solution:**
```java
String json = """
        {
            "id": 101,
            "name": "Sonu"
        }
        """;
```
No concatenation, no `\n`, no escaped quotes.

### Rules
- Opening `"""` **must** be followed by a newline — no content on the same line.
- **Incidental whitespace stripping**: compiler finds the common leading whitespace across all lines + the closing `"""`, and strips it. **Position of the closing `"""` controls how much is stripped:**
```java
String a = """
        Line 1
        Line 2
        """;   // closing at same indent → strips 8 spaces → "Line 1\nLine 2\n"

String b = """
        Line 1
        Line 2
""";              // closing at column 0 → strips nothing → "        Line 1\n        Line 2\n"
```
- **Trailing newline**: present if closing `"""` is on its own line; omit by putting it on the last content line: `"""hello"""` vs `"""hello\n"""`.

### Text Block Methods
```java
str.indent(4);            // adds 4 spaces to each line
str.stripIndent();          // removes common leading whitespace (runtime version of compiler algorithm)
str.translateEscapes();     // interprets literal \n, \t etc. as actual control chars
```

### Practical Use
```java
// SQL
String query = """
        SELECT e.id, e.name FROM employees e
        WHERE e.salary > 70000
        """;

// JSON with formatting
String json = """
        {
            "id": %d,
            "name": "%s"
        }
        """.formatted(id, name);   // String.formatted() — instance method form of String.format(), Java 15
```

---

## 2. Switch Expressions (Java 14)
Switch was a statement (no value); now it can be an **expression**.

**Old statement (verbose, break-prone):**
```java
switch (department) {
    case "Engineering": bonusPercent = 20; break;
    case "Sales": bonusPercent = 25; break;
    default: bonusPercent = 5;
}
```

**New arrow expression:**
```java
int bonusPercent = switch (department) {
    case "Engineering" -> 20;
    case "Sales"       -> 25;
    case "HR", "Finance" -> 10;   // comma-separated multi-label
    default            -> 5;
};
```
- `->` replaces `case:` + `break` — no fall-through
- Returns a value directly
- `default` required unless exhaustive (see below)

**Multi-line case — use `yield`:**
```java
String band = switch ((int)(salary / 10000)) {
    case 10, 11, 12 -> "Executive";
    default -> {
        if (salary < 20000) yield "Trainee";
        else yield "Junior";
    }
};
```
`yield` only works inside switch expression blocks — not a general return-from-lambda keyword.

**Old colon syntax still works with `yield`:**
```java
int bonus = switch (department) {
    case "Engineering": yield 20;
    default: yield 5;
};
```
Prefer arrow syntax for new code.

**Exhaustiveness:** `String`/`int` switches need `default`. `enum` switches covering all constants don't need `default` — and adding a new constant later without updating the switch is a **compile error**, a useful safety net:
```java
int bonus = switch (dept) {   // dept: Department enum
    case ENGINEERING -> 20;
    case HR -> 10;
    case FINANCE -> 15;
    case OPERATIONS -> 8;
    // no default needed — all constants covered
};
```

---

## 3. Helpful NullPointerExceptions (Java 14+)
Automatic, no code changes needed (always-on from Java 15; opt-in flag `-XX:+ShowCodeDetailsInExceptionMessages` in Java 14).

**Before:**
```
NullPointerException
    at EmployeeDemo.main(EmployeeDemo.java:8)
```
**After:**
```
NullPointerException: Cannot invoke "Employee.getDepartment()" because "employee" is null
    at EmployeeDemo.main(EmployeeDemo.java:8)
```
Works through chains too — pinpoints exactly which link in `company.getHeadOffice().getAddress().getCity()` was null.

---

## 4. Practical Pattern
```java
public static String generateJsonReport(List<Employee> employees) {
    String rows = employees.stream()
        .map(e -> """
                    {"id": %d, "name": "%s", "band": "%s"}""".formatted(
                    e.getId(), e.getName(), getSalaryBand(e.getSalary())))
        .collect(Collectors.joining(",\n"));
    return """
            {"employees": [
        %s
            ]}
            """.formatted(rows);
}

static String getSalaryBand(double salary) {
    return switch ((int)(salary / 10000)) {
        case 9, 10 -> "Senior";
        case 7, 8  -> "Mid-level";
        default    -> salary > 100000 ? "Executive" : "Trainee";
    };
}
```

---

## Quick Reference

| Feature | Key Point |
|---|---|
| Text blocks `"""` | Multi-line strings — no concatenation, no quote-escaping |
| Incidental whitespace | Common leading indent stripped; closing `"""` position controls it |
| `String.formatted()` | Instance-method form of `String.format()` — pairs with text blocks |
| Switch expression `->` | Returns a value, no fall-through, comma-separated multi-label |
| `yield` | Returns a value from a multi-statement switch block |
| Switch exhaustiveness | `default` needed for String/int; enum can be exhaustive without it |
| Helpful NPE | JDK 14+ names exactly which variable/expression was null |

## Next: Module 30 — Java 17 Features (records, sealed classes, `instanceof` pattern matching)