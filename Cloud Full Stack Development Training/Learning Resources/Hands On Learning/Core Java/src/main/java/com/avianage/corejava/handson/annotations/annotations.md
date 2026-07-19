# Module 18 — Annotations (Condensed)

> Part D: Core Java Toolkit · Prerequisites: Module 08–13

## Core Idea
An annotation is **metadata attached to code** — does nothing by itself. Tools, frameworks, and the compiler read and act on it.

```java
@Override
public String toString() { ... }
```

---

## 1. Built-in Annotations

**`@Override`** — compiler verifies the method actually overrides a parent method; catches typos that would otherwise silently create a new method.
```java
@Override
public String getRole() { return "Manager"; }   // compiler confirms this exists in parent
```

**`@Deprecated`** — marks outdated code; compiler warns callers.
```java
@Deprecated(since = "2.0", forRemoval = true)   // Java 9+ attributes
public void legacyMethod() { ... }
```

**`@SuppressWarnings`** — silences a specific compiler warning; use sparingly.
```java
@SuppressWarnings("unchecked")
public void addToRawList() { List list = new ArrayList(); }

@SuppressWarnings({"unchecked", "deprecation"})   // multiple
```

**`@FunctionalInterface`** — enforces exactly one abstract method (compiler-checked); Module 20.
```java
@FunctionalInterface
public interface SalaryCalculator {
    double calculate(double base);              // the one abstract method
    default double withBonus(double base) { return calculate(base) * 1.1; }  // ok — default
}
```

**`@SafeVarargs`** — suppresses unchecked warnings on generic varargs methods you're certain are type-safe.
```java
@SafeVarargs
public final void processEmployees(List<Employee>... lists) { ... }
```

---

## 2. Meta-Annotations (annotate annotations)

**`@Retention`** — when the annotation info survives:

| Value | Available |
|---|---|
| `SOURCE` | Compiler only, discarded after compile |
| `CLASS` | In `.class` file, not at runtime (default) |
| `RUNTIME` | Readable via Reflection |

**`@Target`** — where it can be applied: `TYPE`, `METHOD`, `FIELD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `PACKAGE`.

**`@Documented`** — includes it in generated Javadoc.

**`@Inherited`** — a class-level annotation is auto-inherited by subclasses.

```java
@Inherited @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
public @interface Auditable { }

@Auditable public class Employee { }
public class Manager extends Employee { }
// Manager.class.isAnnotationPresent(Auditable.class) → true
```

---

## 3. Writing Custom Annotations
Declared with `@interface`. Elements look like methods but are attribute declarations; support `default` values.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface AuditLog {
    String action() default "UNKNOWN";
    String module() default "GENERAL";
    boolean enabled() default true;
}
```
```java
public class EmployeeService {
    @AuditLog(action = "CREATE", module = "Employee")
    public void addEmployee(Employee emp) { ... }

    @AuditLog(action = "DELETE", module = "Employee", enabled = false)
    public void removeEmployee(int id) { ... }
}
```
(A single element named `value()` can be used unnamed: `@AuditLog("CREATE")`.)

---

## 4. Reading Annotations via Reflection
Requires `RetentionPolicy.RUNTIME`.

```java
for (Method method : clazz.getDeclaredMethods()) {
    if (method.isAnnotationPresent(AuditLog.class)) {
        AuditLog audit = method.getAnnotation(AuditLog.class);
        if (audit.enabled()) {
            System.out.printf("%s | action=%s | module=%s%n",
                method.getName(), audit.action(), audit.module());
        }
    }
}
```

---

## 5. Practical Pattern — Field Validation
Same mechanism Bean Validation / Hibernate Validator uses.

```java
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
public @interface NotBlank { String message() default "Field must not be blank."; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
public @interface MinValue { double value(); String message() default "Value is below minimum."; }
```
```java
public class EmployeeRequest {
    @NotBlank(message = "Employee name is required.")
    private String name;
    @MinValue(value = 15000, message = "Salary must be at least 15000.")
    private double salary;
}
```
Validator reads fields via reflection, checks each annotation:
```java
for (Field field : clazz.getDeclaredFields()) {
    field.setAccessible(true);
    Object value = field.get(obj);
    if (field.isAnnotationPresent(NotBlank.class)) {
        if (value == null || value.toString().isBlank())
            errors.add(field.getName() + ": " + field.getAnnotation(NotBlank.class).message());
    }
    if (field.isAnnotationPresent(MinValue.class)) {
        MinValue mv = field.getAnnotation(MinValue.class);
        if (value instanceof Number n && n.doubleValue() < mv.value())
            errors.add(field.getName() + ": " + mv.message());
    }
}
```
This is essentially how Spring's `@Valid` + Hibernate Validator works: annotate fields, a processor reads them via reflection.

---

## 6. Annotations in Frameworks (context for later modules)

| Annotation | Framework | Purpose |
|---|---|---|
| `@Component` | Spring | Register class as a Spring-managed bean |
| `@Autowired` | Spring | Inject a dependency |
| `@RestController` | Spring MVC | Handle HTTP requests |
| `@GetMapping` | Spring MVC | Map GET requests to a method |
| `@Transactional` | Spring | Wrap method in a DB transaction |
| `@Entity` | JPA/Hibernate | Map class to a DB table |
| `@Column` | JPA/Hibernate | Map field to a table column |
| `@NotNull`, `@Size` | Bean Validation | Validate field values |

Understanding raw Java annotations makes all of these immediately intuitive.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| Annotation | Metadata attached to code — inert by itself |
| `@Override` | Compiler verifies actual override |
| `@Deprecated` | Marks outdated code, compiler warns |
| `@SuppressWarnings` | Silences a specific warning |
| `@FunctionalInterface` | Enforces exactly one abstract method |
| `@Retention(RUNTIME)` | Required to read via Reflection |
| `@Target` | Restricts where annotation applies |
| `@interface` | Syntax for a custom annotation |
| Reflection | API to inspect/read annotations at runtime |

## Next: Part E — Java 8 Features. Module 19: Lambda Expressions