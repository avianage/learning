# Core Java — Complete Line-by-Line Guide

This guide is built directly from your actual course materials: the conceptual courseware in `Courseware/01-core-java/` (modules 01–32) and the real, runnable example code in `Code/Core Java/src/com/acme/demo/` (day1–day4). Every topic below pairs a synthesized summary of the courseware module with the actual course code, reproduced verbatim and explained line by line. Where the course repo has no dedicated example for a topic, that gap is called out explicitly rather than papered over.

---

## 1. Introduction and Setup

Java is a general-purpose, statically-typed, object-oriented language built around **WORA (Write Once, Run Anywhere)**: source code (`.java`) is compiled by `javac` into platform-independent **bytecode** (`.class`), which any platform's **JVM** can execute. Three terms to keep straight: the **JDK** (what you install as a developer — compiler + runtime + tools), the **JRE** (runtime only — JVM + standard libraries, not separately distributed since Java 11), and the **JVM** (the actual execution engine, platform-specific but bytecode-compatible everywhere). Java 8 (2014, lambdas/streams) and Java 17/21 (modern LTS releases) are the version markers most worth remembering; this courseware baselines on Java 8 syntax.

Every program's entry point is `public static void main(String[] args)` — the JVM looks for this exact signature. `public` so the JVM can call it from outside the class, `static` so it can be invoked without first creating an object, `void` because it returns nothing, and `String[] args` to receive command-line arguments. The compiler enforces strict rules: every statement ends in `;`, Java is case-sensitive throughout, the public class name must match the filename exactly, and curly braces must balance. The compiler also breaks source into **tokens** — keywords, identifiers, literals, operators, separators, comments — and Java has firm identifier rules (no leading digit, no keywords, `_`/`$` allowed) plus (unenforced but universal) naming conventions: PascalCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants.

### Code — `Hello.java`

```java
1:  package com.acme.demo;
3:  // Java - versions 
5:  public class Hello {
7:  	public static void main(String[] args) {
9:  //		byte b1 = 100;
10: //		byte b2 = 30;
11: //		int b3 = b1 + b2;
12: //		byte b3 = (byte) (b1 + b2);
14: 		byte b1 = 100;
15: 		byte b2 = 30;
16: 		byte b3 = (byte) (b1 + b2);
18: 		System.out.println(b3);
20: 		System.out.println("Hello world! 2");
22: 	}
24: }
```

- **Lines 9–12** (commented) — without the cast, `byte b3 = b1 + b2;` would **not** compile, because any `byte`/`short` arithmetic result is always widened to `int` first.
- **Line 16** — `byte b3 = (byte) (b1 + b2);` — `b1 + b2` computes as `int` (130), then is narrowed to `byte`. Since 130 exceeds `byte`'s max of 127, this **overflows and wraps around** to `-126` — a classic narrowing-cast gotcha, confirmed by the `println` on line 18.

---

## 2. JVM Architecture

This module explains what happens after you type `java Employee`. The JVM works in three phases: **Loading** (the Class Loader reads `.class` bytecode off disk into the **Method Area**, verifying it and initializing static state), **Execution** (the Execution Engine runs bytecode, either via the **Interpreter** — instruction by instruction — or, for "hot" methods called repeatedly, via the **JIT Compiler**, which compiles them to native machine code for near-native speed after a warm-up period), and **Garbage Collection** (objects with no live reference are reclaimed automatically).

The key memory areas: the **Method Area** (shared, holds class metadata, static variables, the string constant pool — one copy per class), the **Heap** (shared, holds every object created with `new`, subdivided into Young Generation — Eden + two Survivor spaces — and Old Generation for long-lived, promoted objects), the **Stack** (one per thread, holds method call frames — local variables and parameters — pushed on call and popped on return; exhausting it throws `StackOverflowError`, classically from unterminated recursion), the **PC Register** (per-thread, tracks the current bytecode instruction), and the **Native Method Stack** (for JNI/native calls). A critical distinction to internalize: static fields live once in the Method Area regardless of how many objects exist, while instance fields live per-object on the heap. `System.gc()` only *requests* garbage collection — the JVM is free to ignore it, and calling it in production code is discouraged since it can trigger a GC pause at an inopportune moment. Common runtime errors map directly to these regions: `StackOverflowError` (stack exhausted), `OutOfMemoryError: Java heap space` (heap exhausted), `OutOfMemoryError: Metaspace` (method area exhausted, e.g. from dynamic class generation), `ClassNotFoundException`/`NoClassDefFoundError` (classpath/dependency problems at load time vs. runtime).

No dedicated runnable file exists for this conceptual module; the closest illustration is `Hello.java`'s `byte b3 = (byte)(b1 + b2);` (Topic 1) — the addition runs as an `int` operation and the narrowing cast is a separate bytecode instruction (`i2b`) that truncates the result, which is why the overflow/wraparound happens at runtime.

---

## 3. Datatypes, Variables and Operators

Every variable has a type, a name, and a value. Java splits types into **primitives** (8 built-in value types: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean` — each with a fixed size and, for numeric ones, a fixed range) and **reference types** (point to heap objects). Local variables (inside methods) have no default value and *must* be explicitly initialized before use — the compiler rejects reads of a definitely-unassigned local; class-level fields (instance/static), by contrast, get automatic defaults (`0`, `0.0`, `false`, `null`, `' '`).

Literals come in several forms: integer literals can be decimal, binary (`0b...`), octal (`0...`), or hex (`0x...`); `long` literals need an `L` suffix when they exceed `int` range; `float` literals need an `f` suffix (a bare decimal like `3.14` defaults to `double` and won't implicitly narrow into a `float` variable); underscores can visually separate digit groups. `char` literals use single quotes and store a Unicode code point (`'A'` == `65`). `String` is a reference type, not a primitive, but gets literal syntax and pool-backed sharing.

**Type conversion**: widening (`byte→short→int→long→float→double`, with `char` feeding into `int`) is automatic and safe. Narrowing requires an explicit cast `(type)` and can lose data — critically, narrowing a `double`/`float` to an integer type **truncates**, it does not round (`(int) 9.99` → `9`). Arithmetic **promotion rules** matter: any expression involving `byte`/`short` promotes to `int`; involving `long` promotes to `long`; involving `float`/`double` promotes accordingly. This is why `byte b3 = b1 + b2;` is a compile error without a cast — the `+` always produces (at minimum) an `int`.

**Operators**: standard arithmetic (`+ - * / %`), with the classic **integer-division gotcha** — `int / int` truncates the fractional part entirely (`7 / 8` is `0`, not `0.875`); to get a decimal result, cast or use a `double` literal divisor. Assignment operators (`+= -= *= /= %=`). Increment/decrement have pre- and post- forms that differ in *when* the value is read versus mutated (`b = a++` uses `a`'s old value then increments; `c = ++a` increments first then uses the new value). Relational operators always yield `boolean`; `==` compares primitive *values* but object *references* (not content — use `.equals()` for content, covered fully in Module 14). Logical `&&`/`||` **short-circuit** — the second operand is skipped once the result is determined, which is not just an optimization but a null-safety technique (`e != null && e.getSalary() > 50000`). Bitwise operators (`& | ^ ~ << >> >>>`) operate on individual bits; `>>>` is the unsigned right shift (fills with `0` regardless of sign). The ternary `condition ? a : b` is a compact if-else for simple value selection. Operator precedence exists but the practical advice is: use parentheses to make intent explicit rather than memorizing the table. Variable **scope** is block-bound (`{}`) — a variable declared inside an `if` block doesn't exist outside it. `var` (Java 10+) is compile-time type inference for local variables only — not a dynamic type.

### Code — `day1/CommonDemo.java`

```java
1:  package com.acme.demo.day1;
3:  public class CommonDemo {
5:  	public static void main(String[] args) {
6:  		// Employee data -
7:  		int id = 1;
8:  		String name = "Sonu";
9:  		double salary = 10.75;
10: //		long[] phones = { 9876543210L, 678901235L };
11: 		int i = 97;
12: 		char c = (char) i;
13: 		System.out.println(c);
15: 	}
17: }
```

- **Line 10** (commented) — a `long[]` array would need the `L` suffix on its literals since they exceed `int` range (9,876,543,210 > `Integer.MAX_VALUE`).
- **Line 12** — `char c = (char) i;` — explicit narrowing cast from `int` to `char` (a 16-bit unsigned Unicode-code-point type). Since `97` fits within `char`'s range, no data is lost here (unlike the `byte` overflow example in Module 1) — printing `c` shows `a`.

### Code — `Hello.java` (see Module 1)

Already covered above — relevant here too for its narrowing-cast/overflow behavior on `byte` arithmetic.

---

## 4. Wrapper Classes

Primitives are fast but are **not objects** — they can't go into collections (which only hold objects), can't represent "no value" via `null`, and don't carry utility methods. Java's fix is a **wrapper class** per primitive (`Integer`, `Double`, `Boolean`, `Character`, `Byte`, `Short`, `Long`, `Float` — all in `java.lang`, auto-imported). You create them via `valueOf()` (preferred over `new Integer(...)`, which is deprecated) or, most commonly in real code, by **parsing strings**: `Integer.parseInt(s)`, `Double.parseDouble(s)`, `Boolean.parseBoolean(s)` — these throw `NumberFormatException` on invalid input. Converting the other direction uses `Integer.toString(x)`, `String.valueOf(x)`, or string concatenation.

**Autoboxing/unboxing** (Java 5+) automate the primitive↔wrapper conversion: assigning an `int` to an `Integer` variable auto-boxes; using an `Integer` in arithmetic auto-unboxes. The dangerous edge case: unboxing a `null` wrapper throws `NullPointerException` — always null-check a wrapper before using it in arithmetic. A separate, very testable gotcha is the **Integer cache**: Java caches `Integer` objects for values −128 to 127, so `==` accidentally "works" for small cached values but fails for larger ones — the rule is to **always use `.equals()`**, never `==`, to compare wrapper objects. Useful static members exist on each wrapper (`Integer.MAX_VALUE`/`MIN_VALUE`, `Integer.toBinaryString()`, `Integer.compare()`, `Character.isDigit()`/`isLetter()`/`toUpperCase()`, `Boolean.parseBoolean()` which is case-insensitive but only recognizes `"true"`). The module closes with `printf`/`String.format` formatting specifiers (`%d %f %.2f %s %n %-15s`), which is adjacent but frequently tested alongside wrapper/number handling.

### Code — `day2/commons/wrapper/WrapperDemo.java`

```java
1:  package com.acme.demo.day2.commons.wrapper;
3:  import java.util.Scanner;
5:  import com.acme.demo.day1.constructor.Employee;
7:  public class WrapperDemo {
9:  	public static void main(String[] args) {
11: //		Employee emp = new Employee();
12: //		Scanner sc = new Scanner(System.in);
13: //		int num = 10;
14: //		emp. // insance methods field 
15: //		num. // primittive field 
17: //		int num = 10; // primitive 
18: //		Integer num2 = 20; // object 
20: //		int num = 10; 
21: //		Integer num2 = num; // boxing - autoboxing 
22: //		Integer num3 = Integer.valueOf(num); // boxing - manual boxing 
23: //		int num4 = num3; // unboxing - auto unboxing 
24: //		int num5 = num3.intValue(); // unboxing - manual unboxing 
26: //		int num6 = Integer.parseInt("8888");
27: 		int num6 = Integer.parseInt("aaa"); //exception 
28: 		System.out.println(num6);
29: //		Integer num2 = 10; 
30: //		Integer. // static methods and fields 
31: //		num2. // instance methods and fields 
33: 	}
34: }
```

- **Lines 20–24** (commented) — a walkthrough of all four boxing/unboxing forms: `Integer num2 = num;` (autoboxing), `Integer.valueOf(num)` (manual boxing), `int num4 = num3;` (auto-unboxing), `num3.intValue()` (manual unboxing).
- **Line 27** — `int num6 = Integer.parseInt("aaa");` — `"aaa"` is not a valid integer, so this throws `NumberFormatException` **at runtime** (the code compiles fine — this is a runtime, not compile-time, failure). Line 28's print is never reached as a result.

### Code — `day2/Test.java` (Integer cache, `==` vs `.equals()`)

```java
1:  package com.acme.demo.day2;
3:  public class Test {
5:  	public static void main(String[] args) {
7:  		Integer i = 200;
8:  		Integer j = 200;
9:  		System.out.println(i == j);
10: 		System.out.println(i.equals(j));
12: 		Integer k = 10;
13: 		Integer l = 10;
14: 		System.out.println(k == l);
15: 		System.out.println(k.equals(l));
17: 	}
18: }
```

- **Lines 7–10** — `200` is outside the cached range (−128 to 127), so `Integer i = 200; Integer j = 200;` produces two distinct heap objects — `i == j` prints `false`, but `i.equals(j)` correctly prints `true`.
- **Lines 12–15** — `10` is inside the cached range, so `k` and `l` reference the **same** cached `Integer` from the internal `IntegerCache` — `k == l` prints `true` here, but only by accident of caching, not because `==` correctly compares wrapper values. This is exactly the gotcha the courseware warns about; `k.equals(l)` is the only comparison that's reliable regardless of value range.

---

## 5. Flow Control

Java executes top-to-bottom by default; flow control adds decisions, repetition, and jumps. **`if`/`else if`/`else`** chains are evaluated top to bottom and stop at the first true condition; always use braces even for single statements. **`switch`** compares one variable against multiple fixed values — classic `switch` requires `break` to prevent fall-through (deliberate fall-through, e.g. shared `case` labels, is legal but risky), and works only on `int`/`byte`/`short`/`char`/`String`/`enum` (not `double`/`float`/`long`/`boolean`). The Java 14+ **arrow `switch`** (`case X -> value;`) removes fall-through entirely and can be used as an expression that returns a value directly; multi-statement arms use `yield` to produce the value.

**Loops**: `while` checks the condition *before* each iteration (may run zero times); `do-while` checks *after*, guaranteeing at least one execution — useful for menus/validation prompts; `for` is best when the iteration count is known upfront, with all three clauses (`init; condition; update`) individually optional (`for(;;)` is an intentional infinite loop); **for-each** (`for (T x : collection)`) is the cleanest way to visit every element when you don't need the index, but you cannot mutate the underlying array through the loop variable or access the index. `break` exits the nearest enclosing loop entirely; `continue` skips to the next iteration; **labeled** `break`/`continue` (`outer: for(...) { ... break outer; }`) target an outer loop from inside nested loops — legal but should be used sparingly since it hurts readability (often a method extraction with `return` is cleaner).

### Code — `day1/ControlDemo.java`

```java
1:  package com.acme.demo.day1;
3:  public class ControlDemo {
5:  	public static void main(String[] args) {
7:  //		int i = 10, j = 10;
8:  //
9:  //		if (i > j) {
10: //			System.out.println("High");
11: //		} else if (i < j)
12: //			System.out.println("Low");
13: //		else
14: //			System.out.println("Same");
16: //		switch (key) {
17: //		case value: {
18: //			
19: //			yield type;
20: //		}
21: //		default:
22: //			throw new IllegalArgumentException("Unexpected value: " + key);
23: //		}
25: 	}
27: }
```

- **Lines 9–14** (commented) — an `if / else if / else` skeleton: the `else if`/`else` branches are brace-less, legal but discouraged per the courseware's "always use braces" advice; with `i == j == 10` here, "Same" is the branch that would actually run.
- **Lines 16–23** (commented) — a `switch` skeleton mixing traditional `case value:` with `yield` (a hybrid legal since Java 13+), and `default: throw new IllegalArgumentException(...)` to fail loudly on unexpected values rather than silently doing nothing.
- This file has no live/uncommented logic — it's an instructor scratchpad for live-coding `if-else`/`switch`.

### Code — `day1/scanner/ScannerDemo.java` (interactive input driving control flow)

```java
1:  package com.acme.demo.day1.scanner;
3:  import java.util.Scanner;
5:  // take user inputs 
7:  public class ScannerDemo {
9:  	public static void main(String[] args) {
11: 		Scanner sc = new Scanner(System.in);
13: 		System.out.println("Welcome\nEnter your name:");
14: 		String username = sc.next();
15: 		System.out.println("Welcome " + username + "!");
17: 		sc.close();
19: 	}
21: }
```

- **Line 11** — `new Scanner(System.in)` wraps standard input; `java.util` classes (unlike `java.lang`) always need an explicit import.
- **Line 14** — `sc.next()` blocks, waiting for a single whitespace-delimited token, and returns it as a `String`.
- **Line 17** — `sc.close()` releases the input stream — closing a `System.in`-backed scanner is debated (it can prevent further console reads in the same JVM) but shown here as resource-hygiene practice.

---

## 6. Arrays

An array is a **fixed-size, zero-indexed, homogeneous** collection — once created its length cannot change, and the array itself is a heap object (even when it holds primitives). Declaration and creation are separate steps (`int[] salaries; salaries = new int[5];`), though usually combined. Elements not explicitly set get type-appropriate defaults (`0`/`0.0`/`false`/`null`). The **array initializer** shorthand (`int[] ids = {101, 102, 103};`) infers size from the values; the `new Type[]{...}` form is required when creating an array inline without a variable (e.g. as a method argument).

Access is via `array[index]`; `array.length` is a **field**, not a method (no parentheses) — contrast with `String.length()`. Out-of-bounds access throws `ArrayIndexOutOfBoundsException` at runtime (not caught at compile time). Iteration uses either an index-based `for` (when the index is needed) or `for-each` (when only values matter). `java.util.Arrays` supplies utility methods: `sort()` (in-place), `binarySearch()` (array must already be sorted), `fill()`, `copyOf()`/`copyOfRange()`, `equals()` (content comparison — `==` on arrays only compares references), and critically `Arrays.toString()` — printing an array directly (`System.out.println(arr)`) prints an unhelpful type-and-hashcode string like `[I@1b6d3586`.

**2D arrays** are arrays of arrays, accessed `[row][col]`; rows can have independent lengths (**jagged arrays**). Arrays are **passed by reference** into methods — the method receives the same array's address, so element mutations inside the method are visible to the caller (this is distinct from primitives, which are passed by value). Common pitfalls: off-by-one loop bounds (`i <= arr.length` instead of `<`), forgetting arrays are fixed-size (no "adding" an element — use `ArrayList` for dynamic sizing), and calling `.length` on a `null` array reference (`NullPointerException`).

### Code — `day2/commons/arrays/ArrayDemo.java`

```java
1:  package com.acme.demo.day2.commons.arrays;
3:  import java.util.Arrays;
5:  public class ArrayDemo {
7:  	public static void main(String[] args) {
9:  		int[] arr = { 25, 31, 17, 9, 22 };
10: 		System.out.println("Original array");
11: 		for (int a : arr)
12: 			System.out.println(a);
13: 		System.out.println(arr.length);
14: 		Arrays.sort(arr);
15: 		System.out.println("sorted array");
16: 		for (int a : arr)
17: 			System.out.println(a);
19: //		Arrays.
21: 	}
23: }
```

- **Line 13** — `arr.length` is a **field** access (`5`), not a method call — printed before sorting to show the fixed size never changes even though contents do.
- **Line 14** — `Arrays.sort(arr)` sorts **in place** (dual-pivot quicksort for primitives) — the original array object is mutated, no new array returned; re-iterating on lines 16–17 shows the sorted result (`9, 17, 22, 25, 31`).

---

## 7. String Handling

`String` is a **class**, not a primitive, but gets literal syntax (`"Sonu"`) and special JVM support via the **string pool** (part of the Method Area): identical string literals are shared — `String a = "HR"; String b = "HR";` makes `a` and `b` reference the *same* pooled object, so `a == b` is `true`, but `new String("HR")` forces a **new**, separate heap object, so `a == c` is `false`. The absolute rule: **use `.equals()` for content comparison, never `==`** — and putting the literal first (`"HR".equals(dept)`) is a defensive idiom that avoids `NullPointerException` if the variable side is `null`.

The single most important property of `String` is **immutability**: once created, a `String`'s characters never change. Every apparent mutation (`name = name + " Sharma"`) actually creates a **new** `String` object and reassigns the reference; the original is left untouched (and eventually garbage collected). Immutability exists for security (safe sharing of sensitive values like paths/passwords), thread-safety (no synchronization needed), and to make string-pool sharing safe in the first place.

Key methods: `length()`, `charAt(i)`, `indexOf`/`lastIndexOf`, `toUpperCase`/`toLowerCase`, `trim()` (ASCII whitespace only) vs `strip()` (Unicode-aware, Java 11+), `substring(start, end)` (end exclusive), `startsWith`/`endsWith`/`contains`, `isEmpty()` (length 0) vs `isBlank()` (whitespace-only, Java 11+), `replace`/`replaceAll` (regex)/`replaceFirst`, `split(regex)` and `String.join(sep, ...)`, `equals`/`equalsIgnoreCase`/`compareTo`, and `String.valueOf`/`String.format` for building output.

Because every `+` on strings allocates a new object, building strings in a loop with `+=` is wasteful; **`StringBuilder`** is the mutable, in-place alternative (`append`, `insert`, `delete`, `replace`, `reverse`, method chaining) and is the correct default for repeated string construction. **`StringBuffer`** is functionally identical but synchronized (thread-safe, slightly slower) — use only in genuinely multi-threaded contexts.

### Code — `day2/commons/strings/StringDemo.java`

```java
1:  package com.acme.demo.day2.commons.strings;
3:  public class StringDemo {
5:  	public static void main(String[] args) {
7:  		String str = "abcdef";
8:  		System.out.println(str);
9:  		System.out.println(str.length());
10: 		System.out.println(str.charAt(0));
11: 		System.out.println(str.concat(str));
12: 		System.out.println(String.valueOf(10 == 10));
13: //		String.
15: 	}
16: }
```

- **Line 9** — `str.length()` is an instance **method** (unlike array's `.length` field) — a frequent point of confusion for beginners.
- **Line 11** — `str.concat(str)` returns a **new** `String` (`"abcdefabcdef"`); `str` itself stays `"abcdef"` because `String` is immutable — this line demonstrates immutability directly.
- **Line 12** — `String.valueOf(10 == 10)` converts the `boolean` `true` to the literal string `"true"` — `String.valueOf` is overloaded for every type.

---

## 8. Classes and Objects

OOP bundles data (**fields**) and behavior (**methods**) into a single unit — a **class** is the blueprint, an **object** is a runtime instance created from it with `new`. A typical class has: **instance fields** (per-object state, conventionally `private`), a **static field** (one copy shared by the whole class, e.g. an instance counter), an optional **static block** (runs exactly once, when the class is first loaded), an optional **instance initializer block** (runs before *every* constructor call), one or more **constructors** (same name as the class, no return type — the compiler supplies a no-arg default only if you define none yourself), **instance methods** (operate via an implicit `this` on the calling object) and **static methods** (belong to the class, have no `this`, cannot touch instance fields directly), plus conventionally **getters/setters** and an overridden **`toString()`**.

**Constructor chaining** via `this(...)` (must be the first statement) lets one constructor delegate to another, avoiding duplicated init logic; **method overloading** (same name, different parameter lists, resolved at compile time) lets a class expose multiple call shapes for one operation. The keyword **`this`** disambiguates a field from a same-named parameter (`this.id = id;`), calls a sibling constructor, or passes the current object elsewhere. Object variables hold **references** (heap addresses), not the object itself — assigning one reference variable to another copies the address, so both variables see mutations through either one; `null` means "points to nothing," and dereferencing it throws `NullPointerException`. Objects are passed to methods by **value of the reference** — the callee can mutate the object's state through that reference, but reassigning the local parameter inside the method has no effect on the caller's variable. **Varargs** (`double... salaries`) let a method accept a variable number of arguments as an implicit array; the varargs parameter must be last.

Execution order when `new` runs: static block (once, at class load) → instance block (every construction) → constructor body.

### Code — `day1/classes/Employee.java` and `day1/classes/ClassDemo.java` (fields, static field, default constructor, `toString`)

```java
1:  package com.acme.demo.day1.classes;
3:  public class Employee {
5:  	static long officePhone = 123L;
7:  	int id;
8:  	String name;
9:  	double salary;
10: 	long phone;
12: 	@Override
13: 	public String toString() {
14: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
15: 	}
17: }
```

- **Line 5** — `static long officePhone = 123L;` — a **static** field: one copy shared by the whole class, accessible via `Employee.officePhone` without an object.
- **Lines 7–10** — instance fields default automatically since never explicitly initialized (`id`→`0`, `name`→`null`, `salary`→`0.0`, `phone`→`0L`).
- **Line 12** — `@Override` makes the compiler verify this method genuinely overrides `Object`'s — a safety net against typos that would otherwise silently create an unrelated new method.

```java
1:  package com.acme.demo.day1.classes;
3:  public class ClassDemo {
5:  	public static void main(String[] args) {
7:  		Employee obj = new Employee();
8:  		System.out.println(obj.toString());
9:  		obj.id = 1;
10: 		obj.name = "Sonu";
11: //		obj.salary = 10.75;
12: 		System.out.println(obj.toString());
13: 		System.out.println(Employee.officePhone);
15: 		Employee obj2 = new Employee();
16: 		System.out.println(obj2.toString());
17: 		obj2.id = 2;
18: 		obj2.name = "Monu";
19: 		obj2.salary = 11.25;
20: 		System.out.println(obj2.toString());
21: 	}
23: }
```

- **Line 7** — `Employee obj = new Employee();` — since `Employee` defines no explicit constructor, the compiler supplies a **default no-arg constructor** automatically.
- **Lines 9–10** — direct field assignment is legal only because the fields are package-private (not `private`) and `ClassDemo` is in the same package — exactly the anti-pattern Module 13 (Encapsulation) argues against.
- **Line 13** — `Employee.officePhone` is accessed through the **class name** since it's `static`; `obj.officePhone` would also compile but is misleading, implying per-object state that doesn't exist.
- **Lines 15–20** — a second, independent `Employee` object (`obj2`) shows each object has its own instance-field state, while `officePhone` (static) is shared across both.

### Code — `day1/constructor/Employee.java` and `day1/constructor/ConstructorDemo.java` (constructor overloading, chaining side effect via `super()`)

```java
1:  package com.acme.demo.day1.constructor;
3:  public class Employee {
5:  	int id;
6:  	String name;
7:  	double salary;
9:  	public Employee() {
10: 		super();
11: 		System.out.println("Default constructor");
12: 	}
14: 	public Employee(int id, String name) {
15: 		super();
16: 		System.out.println("2 args constructor");
17: 		this.id = id;
18: 		this.name = name;
19: 	}
21: 	public Employee(int id, String name, double salary) {
22: 		super();
23: 		System.out.println("All args constructor");
24: 		this.id = id;
25: 		this.name = name;
26: 		this.salary = salary;
27: 	}
29: 	@Override
30: 	public String toString() {
31: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
32: 	}
34: }
```

- **Lines 9–12, 14–19, 21–27** — three **overloaded constructors** (no-arg, two-arg, three-arg); the compiler picks the right one by argument list at each `new Employee(...)` call site — compile-time polymorphism.
- **Line 10, 15, 22** — `super();` explicitly calls `Object`'s no-arg constructor — what the compiler inserts automatically anyway; written here purely for teaching visibility.
- **Line 17–18, 24–26** — `this.id = id;` — `this.` disambiguates the field from the identically-named parameter; without it, `id = id;` would assign the parameter to itself and leave the field untouched.
- Each constructor prints a distinct message so running `ConstructorDemo` makes visible which overload was selected for each `new` call.

```java
1:  package com.acme.demo.day1.constructor;
3:  public class ConstructorDemo {
5:  	public static void main(String[] args) {
7:  		Employee emp1 = new Employee();
8:  		emp1.id = 1;
9:  		emp1.name = "Sonu";
10: 		emp1.salary = 10.75;
11: 		System.out.println(emp1.toString());
13: 		Employee emp2 = new Employee();
14: 		emp2.id = 2;
15: 		emp2.name = "Monu";
16: 		emp2.salary = 11.25;
17: 		System.out.println(emp2.toString());
19: 		Employee emp3 = new Employee(3, "Tonu", 12.50);
20: 		System.out.println(emp3.toString());
22: 		Employee emp4 = new Employee(4, "Tonu");
23: 		System.out.println(emp4.toString());
25: 	}
26: }
```

- **Line 19** — `new Employee(3, "Tonu", 12.50)` matches the three-arg constructor by argument count/types.
- **Line 22** — `new Employee(4, "Tonu")` matches the two-arg overload; `salary` stays at its default `0.0` since that constructor never touches it.

### Code — `day1/methods/MethodDemo.java` (instance vs static methods)

```java
1:  package com.acme.demo.day1.methods;
3:  public class MethodDemo {
5:  	// instance method == objectName.methodName();
6:  	void printNums() {
7:  		for (int i = 1; i <= 5; i++)
8:  			System.out.println(i);
9:  	}
11: 	// static method == ClassName.methodName();
12: 	static void printNums2() {
13: 		for (int i = 1; i <= 5; i++)
14: 			System.out.println(i);
15: 	}
17: 	public static void main(String[] args) {
19: 		MethodDemo obj = new MethodDemo();
20: 		obj.printNums(); // works
21: 		MethodDemo.printNums2(); // works
22: //		MethodDemo.printNums(); // CE 
23: //		obj.printNums2(); // warning 
25: //		printNums2();
27: 	}
29: }
```

- **Line 22** — commented `MethodDemo.printNums(); // CE` — a genuine **compile error**: an instance method cannot be called through the class name alone, since there's no object context (`this`) to bind to.
- **Line 23** — commented `obj.printNums2(); // warning` — this **compiles** but warns, since calling a static method via an object reference is resolved purely by `obj`'s declared type, not its state — idiomatic Java always calls static members via the class name.
- **Line 25** — a bare, unqualified `printNums2();` call would work here only because `main` is in the same class (`MethodDemo`) — legal shorthand, but `MethodDemo.printNums2()` is clearer.

### Code — `day1/object/Employee.java` and `day1/object/ObjectDemo.java` (bare object creation)

A minimal POJO (`int id; String name; double salary;`, no constructors/methods) plus a driver that does only `Employee emp = new Employee(); emp.salary = 10.25;` — direct field mutation through the reference, `id`/`name` staying at their defaults. Nothing is printed; the pair exists purely to demonstrate bare object instantiation/mutation mechanics.

---

## 9. Access Modifiers and Packages

Java has four access levels, from tightest to widest: **`private`** (class only), **default/package-private** (no keyword — same package only), **`protected`** (same package + subclasses in *any* package), **`public`** (everywhere). The standard practice: fields `private` (forces access through methods, the foundation of encapsulation), getters/setters and constructors `public`, internal helper methods `private`, constants `public static final`. A **top-level class** can only be `public` or default — `private`/`protected` are illegal on top-level classes (only on nested classes, Module 15); exactly one `public` class is allowed per file, and it must match the filename.

A **package** is a namespace that avoids class-name collisions and organizes related classes; the `package` statement must be the very first non-comment line, and its dotted name must mirror the on-disk directory structure. Convention: all-lowercase, reverse-domain style (`com.ems.bean`). To use a class from another package you either `import` it (single-class or wildcard `import pkg.*;` — wildcard does **not** pull in sub-packages) or reference its **fully qualified name** inline. **Static imports** (`import static java.lang.Math.PI;`) let you use static members without the class-name prefix, but are best used sparingly since they obscure where a symbol comes from. `java.lang` is the only auto-imported package.

### Code — `day1/modifiers/FinalDemo.java`

```java
1:  package com.acme.demo.day1.modifiers;
3:  public class FinalDemo {
5:  	static int staticField;
6:  	int instanceField;
7:  	static final int NUM_VALUE = 30;
9:  	public static void main(String[] args) {
11: //		FinalDemo.staticField = 10;
12: //		System.out.println(FinalDemo.staticField);
13: //		FinalDemo obj = new FinalDemo();
14: //		obj.instanceField = 20;
15: //		System.out.println(obj.instanceField);
16: //		System.out.println(FinalDemo.NUM_VALUE);
17: ////		FinalDemo.NUM_VALUE = 35; // CE 
18: //		System.out.println(FinalDemo.NUM_VALUE);
20: 		System.out.println(Integer.BYTES);
21: 		System.out.println(Integer.SIZE);
22: 		System.out.println(Integer.MIN_VALUE);
23: 		System.out.println(Integer.MAX_VALUE);
25: 	}
27: }
```

- **Line 7** — `static final int NUM_VALUE = 30;` — a **constant**: `static` (one copy, class-level) plus `final` (cannot be reassigned once initialized) — the idiomatic shape for named constants.
- **Line 17** (commented) — `FinalDemo.NUM_VALUE = 35; // CE` — a genuine compile error: `final` fields, once assigned, can never be reassigned, caught at compile time not runtime.
- **Lines 20–23** — `Integer.BYTES`/`SIZE`/`MIN_VALUE`/`MAX_VALUE` are themselves `public static final` constants inside the JDK's `Integer` class — a live illustration of the same `static final` pattern from the JDK's own source.

### Code — `day1/modifiers/package1/SpecifierDemo.java`

```java
1:  package com.acme.demo.day1.modifiers.package1;
3:  public class SpecifierDemo {
5:  	public static int num1 = 10;
6:  	protected static int num2 = 20;
7:  	/*default*/ static int num3 = 30;
8:  	private static int num4 = 40;
10: 	public static void main(String[] args) {
12: 		System.out.println(SpecifierDemo.num1);
13: 		System.out.println(SpecifierDemo.num2);
14: 		System.out.println(SpecifierDemo.num3);
15: 		System.out.println(SpecifierDemo.num4);
17: 	}
19: }
```

- **Lines 5–8** — one field per access level (`public`, `protected`, package-private — the `/*default*/` comment is just documentation, there's no `default` keyword — and `private`).
- **Lines 12–15** — all four prints succeed since they're accessed **inside the declaring class itself**; the restrictions only show up in the companion files below.

### Code — `day1/modifiers/package1/WithinPackage.java` (same package, different class)

```java
1:  package com.acme.demo.day1.modifiers.package1;
3:  public class WithinPackage {
5:  	public static void main(String[] args) {
7:  		System.out.println(SpecifierDemo.num1);
8:  		System.out.println(SpecifierDemo.num2);
9:  		System.out.println(SpecifierDemo.num3);
10: //		System.out.println(SpecifierDemo.num4); // CE
12: 	}
13: }
```

- From a **different class in the same package**: `num1` (public) and `num2` (protected — grants access to the whole package, not just subclasses) and `num3` (package-private) are all accessible.
- **Line 10** (commented) — `num4 // CE` — genuine compile error: `private` restricts access to the *declaring class only*, so even another class in the same package cannot reach it.

### Code — `day1/modifiers/package2/OutsidePackage.java` (different package, not a subclass)

```java
1:  package com.acme.demo.day1.modifiers.package2;
3:  import com.acme.demo.day1.modifiers.package1.SpecifierDemo;
5:  public class OutsidePackage {
7:  	public static void main(String[] args) {
9:  		System.out.println(SpecifierDemo.num1);
10: //		System.out.println(SpecifierDemo.num2);
11: //		System.out.println(SpecifierDemo.num3);
12: //		System.out.println(SpecifierDemo.num4); // CE
14: 	}
16: }
```

- Declared in a sibling package with no inheritance relationship to `SpecifierDemo`, so an explicit `import` is required (line 3).
- **Line 9** — `num1` (public) is the only member accessible here: `protected`/default/private (lines 10–12, all commented) fail since `OutsidePackage` is neither same-package nor a subclass. Read alongside `WithinPackage.java`, this is the complete matrix proof of the access-modifier visibility table.

### Code — `day2/commons/packages/PackageDemo.java` (importing JDK packages)

```java
1:  package com.acme.demo.day2.commons.packages;
3:  import java.util.Random;
4:  import java.util.Scanner;
6:  public class PackageDemo {
8:  	public static void main(String[] args) {
10: //		Scanner sc = new Scanner(System.in);
11: //		System.out.println("Enter:");
12: //		int num = sc.nextInt();
13: //		System.out.println(num);
14: //		sc.close();
16: //		Random random = new Random();
17: //		int num = random.nextInt(1000, 9999); // 4 digit otp 
18: //		System.out.println(num);
20: 	}
22: }
```

- **Lines 10–14** (commented) — `sc.nextInt()` reads an `int` token (contrast with `ScannerDemo.java`'s `sc.next()`, which reads a `String`).
- **Lines 16–18** (commented) — `random.nextInt(1000, 9999)` (the two-arg overload, Java 17+) generates a random `int` in `[1000, 9999)` — a 4-digit OTP generator. The whole `main` body is commented out; this file exists purely to show the needed imports.

---

## 10. Inheritance

Inheritance lets a class (**subclass**/child) acquire the fields and methods of another (**superclass**/parent) via `extends`, avoiding duplication when classes share state/behavior — the design litmus test is an **IS-A** relationship (`Manager` IS-A `Employee`, checkable with `instanceof`), as opposed to **HAS-A** (composition — prefer this when the relationship isn't a true specialization). **`super`** has two uses: `super(...)` calls the parent constructor and *must* be the first statement in the child constructor (if omitted, Java inserts a no-arg `super()` automatically — which fails to compile if the parent has no no-arg constructor); `super.method()` invokes the parent's version of an overridden method from inside the override.

**Method overriding**: a subclass redefines an inherited method with an identical signature (and same or covariant return type, same-or-wider access modifier); always mark it `@Override` so the compiler verifies a genuine override is happening. Overriding is resolved at **runtime** (dynamic dispatch, Module 11) versus overloading's **compile-time** resolution — this distinction is one of the most commonly tested facts in Java assessments. **`final`** applied to a method blocks overriding, applied to a class blocks subclassing entirely (`String` is `final`), and applied to a field/variable blocks reassignment after first initialization.

Java supports single, multilevel (`A→B→C`), and hierarchical (multiple children from one parent) inheritance for classes, but explicitly **not multiple inheritance of classes** (`class X extends A, B` is illegal) — this avoids the Diamond Problem; interfaces (Module 12) fill that gap instead. Constructor chaining always runs bottom of the hierarchy last: `Object() → Employee() → Manager()`, top-down. Every class implicitly extends `java.lang.Object` if it declares no `extends` clause, inheriting `toString()`, `equals()`, `hashCode()`, `getClass()`, `clone()`, `wait/notify/notifyAll`, and the deprecated `finalize()` (all detailed in Module 14).

### Code — `day2/oop/inheritance/Phone.java` and `InheritanceDemo.java`

```java
1:  package com.acme.demo.day2.oop.inheritance;
3:  public class Phone {
5:  }
7:  class BasicPhone {
9:  	public void call() {
10: 		System.out.println("calling...");
11: 	}
13: 	public void sms() {
14: 		System.out.println("texting...");
15: 	}
16: }
18: class FeaturePhone extends BasicPhone {
20: 	public void music() {
21: 		System.out.println("playing...");
22: 	}
24: }
26: class SmartPhone extends FeaturePhone {
28: 	@Override
29: 	public void music() {
30: 		System.out.println("playing dolby...");
31: 	}
33: 	public void camera() {
34: 		System.out.println("clicking...");
35: 	}
37: }
```

- **Line 3–5** — `public class Phone {}` is an unused placeholder (needed so the filename matches a public top-level class); the real hierarchy lives in the package-private classes below it in the same file.
- **Line 18, 26** — `FeaturePhone extends BasicPhone` and `SmartPhone extends FeaturePhone` form a **multilevel** chain (`SmartPhone → FeaturePhone → BasicPhone → Object`), each adding new behavior (`music()`, `camera()`).
- **Line 28–31** — `SmartPhone` **overrides** `FeaturePhone`'s `music()` with a more specific implementation rather than inheriting the plain version — resolved at runtime.

```java
1:  package com.acme.demo.day2.oop.inheritance;
3:  public class InheritanceDemo {
5:  	public static void main(String[] args) {
7:  		BasicPhone phone1 = new BasicPhone();
8:  		phone1.call();
9:  		phone1.sms();
11: 		FeaturePhone phone2 = new FeaturePhone();
12: 		phone2.call();
13: 		phone2.sms();
14: 		phone2.music();
16: 		SmartPhone phone3 = new SmartPhone();
17: 		phone3.call();
18: 		phone3.sms();
19: 		phone3.music();
20: 		phone3.camera();
22: 		BasicPhone phone4 = new SmartPhone();
23: 		phone4.call();
24: 		phone4.sms();
25: //		phone4.music(); // CE 
26: //		phone4.camera(); // CE 
27: //		advantages ? 
30: 	}
32: }
```

- **Lines 7–20** — each object (`phone1`..`phone3`) can only call methods defined up to its own place in the hierarchy; `SmartPhone` inherits everything and adds `camera()` plus its overridden `music()`.
- **Line 22** — `BasicPhone phone4 = new SmartPhone();` is **upcasting** — implicit and always safe, since a `SmartPhone` IS-A `BasicPhone`.
- **Lines 25–26** (commented) — `phone4.music(); // CE` / `phone4.camera(); // CE` — genuine compile errors: even though the runtime object is a `SmartPhone`, the **compile-time (declared) type** of `phone4` is `BasicPhone`, and the compiler only allows methods the declared type knows about. Calling them requires an explicit downcast, `((SmartPhone) phone4).camera();`.

---

## 11. Polymorphism

Polymorphism ("many forms") lets one reference type stand for objects of different actual types, with the correct behavior selected automatically. Java has two kinds: **compile-time polymorphism** (method **overloading** — the compiler picks the matching method by argument count/types at the call site, before the program runs; changing *only* the return type does not count as overloading and is a compile error) and **runtime polymorphism** (method **overriding** + **dynamic dispatch** — when a parent-typed reference holds a child object, the JVM looks up the method in the *actual object's* virtual method table at runtime and calls the child's version, not the reference type's).

This is what makes processing heterogeneous collections clean: iterating an `Employee[]` containing `Manager`/`Developer`/`Contractor` objects and calling `e.display()` on each runs each object's own override, with no `if (obj instanceof X)` chain required. **Upcasting** (child → parent reference) is implicit and always safe; **downcasting** (parent → child reference) requires an explicit cast and can throw `ClassCastException` at runtime if the actual object isn't really of that subtype — always guard with `instanceof` first (or use Java 16+'s pattern-matching `instanceof`, `if (e instanceof Manager m) { ... }`, which checks and casts in one step). A subtle trap: calling an overridable method from a parent constructor invokes the **child's** override even though the child's own fields haven't been initialized yet — a source of confusing `null`/default-value bugs; the fix is to only call `private` or `final` methods from constructors.

### Code — `day2/oop/polymorphism/Calc.java` and `PolymorphismDemo.java` (compile-time / overload resolution)

```java
1:  package com.acme.demo.day2.oop.polymorphism;
3:  public class Calc {
5:  	public static void addNums(int i, long j) {
6:  		System.out.println(i + j);
7:  	}
9:  	public static void addNums(long i, int j) {
10: 		System.out.println(i + j);
11: 	}
13: 	public static void addNums(int i, int j) {
14: 		System.out.println(i + j);
15: 	}
17: 	public static void addNums(int i, int j, int k) {
18: 		System.out.println(i + j + k);
19: 	}
21: 	public static void addNums(int i, int j, int k, int l) {
22: 		System.out.println(i + j + k + l);
23: 	}
24: }
```

- **Lines 5–15** — `(int, long)` and `(long, int)` are distinct legal overloads, but a call like `addNums(10, 20)` (two `int` literals) resolves to the exact-match `addNums(int, int)` overload (line 13) rather than either widening overload, since exact-type match wins over widening.
- **Lines 17–23** — the three- and four-arg overloads are distinguished purely by **argument count** — the simplest form of overloading.

```java
1:  package com.acme.demo.day2.oop.polymorphism;
3:  public class PolymorphismDemo {
5:  	public static void main(String[] args) {
7:  		Calc.addNums(10, 20);
8:  		Calc.addNums(10, 20, 30);
9:  		Calc.addNums(10, 20, 30, 40);
11: 	}
13: }
```

- All three calls resolve purely from argument count/type at compile time (`30`, `60`, `100`) — contrasting with the runtime (override) polymorphism in the Inheritance module's `Phone`/`SmartPhone` example.

---

## 12. Abstraction

Abstraction means exposing *what* something does while hiding *how*. Java offers two tools: **abstract classes** (partial implementation — a mix of abstract methods with no body and concrete methods with full implementations, plus fields/constructors like any class; cannot be instantiated directly with `new`) and **interfaces** (a pure contract — before Java 8, 100% abstract method signatures with implicitly `public static final` fields; a class `implements` — not `extends` — an interface, and can implement **multiple** interfaces, which is how Java works around its "no multiple class inheritance" restriction). Any concrete subclass of an abstract class **must** implement every abstract method it inherits, or itself remain abstract.

Java 8 added **`default`** methods to interfaces (a method with a body that implementing classes inherit as-is or can override) — this let interface authors add new methods without breaking every existing implementer — and **`static`** methods on interfaces (belong to the interface itself, called as `InterfaceName.method()`, never inherited by implementers). Decision rule: choose an **abstract class** when classes share real state and partial implementation (e.g. a common `Employee` base with fields and a shared `applyRaise()`); choose an **interface** when you need to describe a capability that spans otherwise-unrelated class hierarchies (e.g. `Payable` applying to `Employee`, `Vendor`, and `Contractor` alike), or when a class needs to satisfy more than one contract simultaneously.

### Code — `day2/oop/OopDemo.java` (conceptual overview, no runtime logic)

```java
1:  package com.acme.demo.day2.oop;
3:  /**
4:   * Encapsulation - bind data and code together
5:   * 
6:   *  - make fields private, and create public getters and setters 
7:   * 
8:   * Inheritance - access properties of super types
9:   * 
10:  * AbstractionDemo - minimum necessary representation ==
11:  * 
12:  * - hide unnecessary details
13:  * 
14:  * Polymorphism - methods with the same name behave differently
15:  * 
16:  * Method overloading 
17:  * 
18:  * Method overriding 
19:  * 
20:  */
22: public class OopDemo {
24: }
```

- Lines 3–20 are a Javadoc-style comment summarizing all four OOP pillars; the class body itself is empty, serving purely as an anchor for the overview at the start of the `day2.oop` package.

### Code — `day2/oop/abstractconcrete/AbsDemo.java` (abstract class basics: concrete + abstract methods)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
3:  // abstract class 
4:  public abstract  class AbsDemo {
6:  //	concrete method - what to do and how to do both 
8:  	// method signature - what does this method do ?
9:  	public void doThis() 
10: 	// method body - how does it do it ?
11: 	{
12: 		System.out.println("doing");
13: 	}
15: //	 abstract method only what to do
16: 	public abstract void doThisToo();
17: }
```

- **Line 4** — `abstract` means `AbsDemo` **cannot** be instantiated with `new`, even though it has no abstract members forcing that — any class can be marked `abstract` regardless.
- **Line 16** — `public abstract void doThisToo();` — signature only, no body, terminated with `;`. Any concrete subclass of `AbsDemo` is *required* to implement this or itself remain `abstract`.

### Code — `day2/oop/abstractconcrete/Banking.java` (abstract class + multiple interfaces)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
3:  public class Banking {
5:  }
7:  interface StateGovt {
9:  }
11: interface GovtOfIndia {
13: //	public abstract void checkNationality();
14: 	void checkNationality();
16: }
18: abstract class Rbi {
20: 	abstract void doKyc();
22: 	public abstract void payInterest();
24: }
26: class HdfcBank extends Rbi implements GovtOfIndia, StateGovt {
28: 	@Override
29: 	public void doKyc() {
30: 		System.out.println("Aadhaar KYC");
31: 	}
33: 	@Override
34: 	public void payInterest() {
35: 		System.out.println("Paying 4% interest");
36: 	}
38: 	@Override
39: 	public void checkNationality() {
40: 		System.out.println("Is Indian?");
41: 	}
42: }
44: class IciciBank extends Rbi implements GovtOfIndia {
46: 	@Override
47: 	public void doKyc() {
48: 		System.out.println("PAN Card KYC");
49: 	}
51: 	@Override
52: 	public void payInterest() {
53: 		System.out.println("Paying 5% interest");
54: 	}
56: 	@Override
57: 	public void checkNationality() {
58: 		System.out.println("Is Foreign?");
59: 	}
61: }
```

- **Lines 7–14** — `StateGovt` is an empty **marker interface** (classes `implements` it to signal a category, similar to `java.io.Serializable`); line 13 vs 14 shows the fully explicit `public abstract void checkNationality();` versus the idiomatic terse form — **all interface methods are implicitly `public abstract`** unless `default`/`static`.
- **Lines 18–26** — `abstract class Rbi` mixes a package-private abstract method (`doKyc`) with an explicitly `public abstract` one (`payInterest`), showing abstract classes aren't restricted to `public` members like interfaces are; `HdfcBank extends Rbi implements GovtOfIndia, StateGovt` combines **single class inheritance** with **multiple interface implementation**, and must implement every abstract method from both.
- **Line 44** — `IciciBank` implements the same abstract class/interface with different logic, and notably skips `StateGovt` — interface adoption is chosen independently per class.

### Code — `day2/oop/abstractconcrete/AbstractAndConcreteDemo.java` (driving the hierarchy)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
3:  public class AbstractAndConcreteDemo {
5:  	public static void main(String[] args) {
7:  		HdfcBank bank1 = new HdfcBank();
8:  		bank1.doKyc();
9:  		bank1.payInterest();
10: 		bank1.checkNationality();
11: 		IciciBank bank2 = new IciciBank();
12: 		bank2.doKyc();
13: 		bank2.payInterest();
14: 		bank2.checkNationality();
16: 		Rbi bank3 = new HdfcBank();
17: 		bank3.doKyc();
19: 	}
21: }
```

- **Line 7** — `new HdfcBank()` is legal because `HdfcBank` is **concrete**; `new Rbi()`/`new GovtOfIndia()` directly would both be compile errors.
- **Line 16** — `Rbi bank3 = new HdfcBank();` is **upcasting** to the abstract superclass type — legal and implicit.
- **Line 17** — `bank3.doKyc()` works since `doKyc()` is declared on `Rbi`, but `bank3.checkNationality()` (from `GovtOfIndia`) would **not** compile through this reference — the declared type gates which methods are callable, even though the runtime object implements more.

### Code — `day2/oop/abstraction/AbstractionDemo.java`

```java
1:  package com.acme.demo.day2.oop.abstraction;
3:  public class AbstractionDemo {
5:  }
```

- An empty stub/placeholder — the substantive abstraction examples live in the sibling `abstractconcrete` package above.

---

## 13. Encapsulation

Encapsulation bundles data and the methods that operate on it, and **controls access** to that data so an object can enforce its own validity — the object should manage its own state, and outside code should only be able to change it through defined methods (which can validate/transform/restrict). Without encapsulation (public fields), any code anywhere can set an object into an invalid state (`salary = -99999`) with no way to prevent it. The standard fix: **private fields + public getters/setters**, with the setters performing validation (throwing `IllegalArgumentException` on bad input, for instance) — this is what actually distinguishes real encapsulation from mechanical getter/setter boilerplate that just re-exposes the field with no added logic.

Encapsulation also lets you shape exactly what's readable/writable: **read-only** fields (getter only, often paired with `final` so the value can only be set once, in the constructor — appropriate for identity fields like an ID), and rarer **write-only** fields (setter only — e.g. a password that's hashed on write and never exposed via a getter). Taken to its logical extreme, an **immutable class** (`final` class, all fields `private final`, no setters, all state set in the constructor, defensive copies of any mutable field on both input and output) can never change after construction — inherently thread-safe and safe to share freely; `String` is Java's canonical immutable class. The **JavaBeans convention** (private fields, public no-arg constructor, `getX()`/`setX()`/`isX()` for booleans) is the standard shape frameworks like Spring and Hibernate expect, since they use reflection to discover and call these methods by name.

### Code — `day2/oop/encapsulation/Employee.java` and `EncapsulationDemo.java`

```java
1:  package com.acme.demo.day2.oop.encapsulation;
3:  import java.util.Objects;
5:  public class Employee {
7:  	private int id;
8:  	private String name;
9:  	private double salary;
11: 	public Employee() {
12: 		super();
13: 	}
15: 	public Employee(int id, String name, double salary) {
16: 		super();
17: 		this.id = id;
18: 		this.name = name;
19: 		this.salary = salary;
20: 	}
22: 	public int getId() {
23: 		return id;
24: 	}
26: 	public void setId(int id) {
27: 		this.id = id;
28: 	}
30: 	public String getName() {
31: 		return name;
32: 	}
34: 	public void setName(String name) {
35: 		this.name = name;
36: 	}
38: 	public double getSalary() {
39: 		return salary;
40: 	}
42: 	public void setSalary(double salary) {
43: 		this.salary = salary;
44: 	}
46: 	@Override
47: 	public int hashCode() {
48: 		return Objects.hash(id, name, salary);
49: 	}
51: 	@Override
52: 	public boolean equals(Object obj) {
53: 		if (this == obj)
54: 			return true;
55: 		if (obj == null)
56: 			return false;
57: 		if (getClass() != obj.getClass())
58: 			return false;
59: 		Employee other = (Employee) obj;
60: 		return id == other.id && Objects.equals(name, other.name)
61: 				&& Double.doubleToLongBits(salary) == Double.doubleToLongBits(other.salary);
62: 	}
64: 	@Override
65: 	public String toString() {
66: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
67: 	}
69: }
```

- **Lines 7–9, 26–44** — `private` fields are the core encapsulation move (unreachable outside `Employee`); these particular setters have **no validation logic**, flagged by the courseware as "boilerplate, not real encapsulation" — still a single choke point where validation could later be added, but mechanical passthroughs as written.
- **Lines 48, 53–61** — `Objects.hash(id, name, salary)` uses **exactly the same fields** as `equals()`, satisfying the contract required for correct `HashMap`/`HashSet` behavior; `equals()` follows the standard four-step pattern (same-reference shortcut, null check, `getClass()` exact-type check, field comparison), using `Double.doubleToLongBits(...)` for `salary` since it treats `NaN`/`-0.0` consistently unlike naive `==`.

```java
1:  package com.acme.demo.day2.oop.encapsulation;
3:  public class EncapsulationDemo {
5:  	public static void main(String[] args) {
6:  		Employee emp = new Employee();
7:  		System.out.println(emp.toString());
8:  //		emp.salary = 10.25;
9:  		emp.setSalary(10.25);
10: //		System.out.println(emp.salary);
11: 		System.out.println(emp.getSalary());
12: 		System.out.println(emp.toString());
14: 	}
16: }
```

- **Lines 8, 10** (commented) — `emp.salary = 10.25;` / `emp.salary` would both be **compile errors**: `salary` is `private`, and even a same-package class like `EncapsulationDemo` cannot reach a `private` member — direct proof encapsulation is compiler-enforced, not just convention. Lines 9, 11 show the only legal path: the public setter/getter.

---

## 14. Object Class Methods

Every class in Java implicitly extends `java.lang.Object`, inheriting `toString()`, `equals(Object)`, `hashCode()`, `getClass()`, `clone()`, `wait()/notify()/notifyAll()`, and `finalize()` (deprecated since Java 9). The three you override constantly: **`toString()`** (default is `ClassName@hexHashCode`, useless for debugging — always override it; it's invoked implicitly by `println`, string concatenation, and debuggers), **`equals()`** (default uses `==`, i.e. reference identity — override with the standard four-step pattern: same-reference shortcut, null check, `getClass()` type check, then field-by-field comparison, using `Objects.equals()` for null-safe field comparisons), and **`hashCode()`** — which must **always** be overridden together with `equals()`, following the contract that equal objects (per `equals`) must produce equal hash codes (the converse isn't required — hash collisions between unequal objects are fine). Breaking this contract silently corrupts `HashMap`/`HashSet` lookups: an object added to a `HashSet` may appear "not contained" even when an equals-equivalent object is queried, because the lookup goes to the wrong hash bucket. `Objects.hash(field1, field2, ...)` is the idiomatic way to build a well-distributed hash from multiple fields — always using the *same* fields referenced in `equals()`.

`getClass()` returns the actual runtime `Class` object — stricter than `instanceof` (which also matches subclasses), and the right choice inside `equals()` for exact-type checks. `clone()` (via the `Cloneable` marker interface) produces a **shallow copy** by default — reference fields in the clone still point at the *same* underlying objects as the original, so mutable fields (arrays, collections) need to be cloned individually for a true **deep copy**; in modern practice, a **copy constructor** is usually preferred over `clone()` since it avoids the checked `CloneNotSupportedException`, the marker-interface requirement, and the shallow-copy default. `wait()/notify()/notifyAll()` are low-level thread-coordination primitives that must be called from a `synchronized` context — mostly superseded today by higher-level concurrency utilities (`BlockingQueue`, `CompletableFuture`), but foundational to understanding them. `finalize()` is deprecated and unreliable (no execution guarantee, GC overhead, possible object resurrection) — `AutoCloseable` + `try-with-resources` is the modern, deterministic replacement for cleanup logic.

### Code — `day2/commons/objects/Employee.java` and `ObjectDemo.java`

```java
1:  package com.acme.demo.day2.commons.objects;
3:  import java.util.Objects;
5:  public class Employee {
7:  	int id;
8:  	String name;
9:  	double salary;
11: 	public Employee() {
12: 		super();
13: 	}
15: 	public Employee(int id, String name, double salary) {
16: 		super();
17: 		this.id = id;
18: 		this.name = name;
19: 		this.salary = salary;
20: 	}
22: 	// getters setters 
24: 	@Override
25: 	public int hashCode() {
26: 		return Objects.hash(id, name, salary);
27: 	}
29: 	@Override
30: 	public boolean equals(Object obj) {
31: 		if (this == obj)
32: 			return true;
33: 		if (obj == null)
34: 			return false;
35: 		if (getClass() != obj.getClass())
36: 			return false;
37: 		Employee other = (Employee) obj;
38: 		return id == other.id && Objects.equals(name, other.name)
39: 				&& Double.doubleToLongBits(salary) == Double.doubleToLongBits(other.salary);
40: 	}
42: 	@Override
43: 	public String toString() {
44: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
45: 	}
47: }
```

- **Lines 7–9** — fields here are package-private (unlike the Encapsulation module's `Employee`) since this file's focus is `Object` method overrides, not access control; it skips getters/setters entirely (`// getters setters` comment marks the omission).
- Same `hashCode()`/`equals()`/`toString()` pattern as `EncapsulationDemo`'s `Employee` above — see that walkthrough for the field-by-field reasoning.

```java
1:  package com.acme.demo.day2.commons.objects;
3:  public class ObjectDemo {
5:  	public static void main(String[] args) {
7:  		Employee emp1 = new Employee(1, "Sonu", 10.25);
8:  		Employee emp2 = new Employee(1, "Sonu", 10.25);
10: 		System.out.println(emp1.toString());
11: 		System.out.println(emp2.toString());
12: 		System.out.println(emp1.hashCode());
13: 		System.out.println(emp2.hashCode());
14: 		System.out.println(emp1.equals(emp2));
16: 	}
18: }
```

- **Lines 7–14** — `emp1`/`emp2` are two separate heap objects with identical field values: `hashCode()` matches for both (satisfying the equals/hashCode contract), and `emp1.equals(emp2)` prints `true` because `equals()` was overridden to compare content, not reference identity — without the override, this would print `false` despite identical data.

---

---

## 15. Inner Classes

Java defines four kinds of nested classes, differing in where they're declared and what they can access: **regular (member) inner classes**, **static nested classes**, **method-local inner classes**, and **anonymous inner classes**.

A **regular inner class** is declared inside a class body without `static`. It implicitly holds a reference to its enclosing instance, so it can read and write *all* members of the outer class — including `private` fields — without getters. Consequently, you cannot instantiate an inner class on its own: you need an outer instance first, then `outer.new Inner(...)`, e.g. `dept.new BudgetReport("Ponu")`. This is fundamentally different from a Python nested class, which is just a name scoped inside another class body with no automatic binding to an outer instance — Java's inner class is closer to a closure over `this`. If the inner class declares a field with the same name as an outer field, the inner one shadows it; to reach the outer field explicitly, use `Outer.this.fieldName`.

A **static nested class** is declared with `static` and does *not* hold a reference to an enclosing instance — it's just namespaced under the outer class for logical grouping (e.g. `Employee.PayrollSummary`). It can only touch the outer class's `static` members directly; for instance data it needs an explicit object reference (e.g., via a getter). It's created without any outer instance: `new Employee.PayrollSummary()`. The courseware stresses this static/non-static distinction as the single most important gotcha: forgetting `static` on a nested class that doesn't need outer state wastes memory (each instance would carry a hidden outer reference) and forces awkward instantiation syntax.

A **method-local inner class** is declared inside a method body and is visible only within that method. It can capture local variables from the enclosing method, but only if they are `final` or **effectively final** (never reassigned after initialization) — the compiler enforces this because the local class may outlive the method's stack frame conceptually (its instance can be returned/stored), so it captures copies of those variables, not live references. The courseware notes these are rarely used in modern Java since lambdas (Module 19) cover most one-off cases more concisely; they remain useful only when you need multiple methods bundled in a throwaway helper.

An **anonymous inner class** has no name and is declared and instantiated in a single expression: `new SomeInterface() { ... }`, providing an inline one-off implementation of an interface or abstract class. Same effectively-final capture rule applies. Before Java 8, this was the standard way to supply behavior like a custom `Comparator`. Since Java 8, lambdas replace anonymous classes for functional interfaces (single abstract method), but anonymous classes are still required when: the type has more than one abstract method, you need instance state, or you're extending an abstract class rather than implementing an interface.

The courseware's summary table maps old-style anonymous-class idioms (`Runnable`, `Comparator`) to their Java 8 lambda equivalents, reinforcing that lambdas are the "modern replacement" for single-method anonymous classes, while regular inner classes and static nested classes remain broadly used.

### `Calc.java`

```java
     1	package com.acme.demo.day3.inner;
     3	@FunctionalInterface
     4	interface Tax {
     6		public abstract double gst(double amount);
     7	}
     9	@FunctionalInterface
    10	public interface Calc {
    12	public abstract int addNums(int i, int j);
    14	//	public abstract int subNums(int i, int j);
    16	}
    18	class CalcMethods implements Calc {
    20		@Override
    21		public int addNums(int i, int j) {
    22		return i + j;
    23	}
    25	//	@Override
    26	//	public int subNums(int i, int j) {
    27	//		return i - j;
    28	//	}
    30	}
```

- **Line 3, 9** — `@FunctionalInterface`: a marker annotation that tells the compiler to *verify* the annotated interface has exactly one abstract method, so it can legally be a lambda target — the tie-in to Module 20.
- **Line 14** (commented) — if this second abstract method `subNums` were uncommented, `@FunctionalInterface` would cause a **compile error** — the annotation actively catches an accidental second abstract method rather than failing mysteriously at lambda-conversion sites.
- **Line 18** — `CalcMethods implements Calc` is a traditional named-class implementation (the "Option 1" approach referenced in `InnerDemo.java`, contrasted with anonymous-class and lambda approaches below).

### `InnerDemo.java`

```java
     1	package com.acme.demo.day3.inner;
     3	// use abstract method from an interface 
     5	public class InnerDemo {
     7		public static void main(String[] args) {
     9		Tax tax = (amount) -> {
    10			return amount * 1.18;
    11	};
    12		Tax tax2 = amount -> {
    13		return amount * 1.18;
    14	};
    16	Tax tax3 = amount -> amount * 1.18;
    18	double finalAmount = tax.gst(100);
    19	System.out.println(finalAmount);
    21	//		// Option 1 - use concrete class
    22	//		Calc calc = new CalcMethods();
    23	//		calc.addNums(10, 20);
    24	//
    25	//		// Option 2 - use annon inner class
    26	//		Calc calc2 = new Calc() {
    27	//			@Override
    28	//			public int addNums(int i, int j) {
    29	//				return i + j;
    30	//			}
    31	//
    32	//		};
    33	//		calc2.addNums(10, 20);
    34	//		
    35	////		Option 3 - use lambda - 		
    36	//		
    37	////		Calc calc3 = (i, j) -> { return i + j;};
    38	//		Calc calc3 = (i, j) ->  i + j; 
    39	//		
    40	//		calc3.addNums(10, 20);
    41	//		
    42	}
    43	}
```
*(The file also retains commented-out drafts exploring `LocalClass`, `InstanceClass`, and `StaticClass` placeholders for the other three inner-class kinds discussed in the courseware.)*

- **Lines 9–16** — three progressively terser forms of the same lambda: `(amount) -> { return amount * 1.18; };` (full form), `amount -> { ... }` (parens dropped for a single inferred-type parameter), and `amount -> amount * 1.18;` (expression-lambda, no braces/`return`) — all functionally equivalent to `new Tax() { public double gst(double amount) {...} }`, but far more concise.
- **Lines 21–41** (commented) — three explicit strategies for implementing `Calc` side-by-side: **Option 1** the named class `CalcMethods` (`new CalcMethods()`); **Option 2** an **anonymous inner class** (`new Calc() { @Override public int addNums(...) {...} }`); **Option 3** a **lambda** (`(i, j) -> i + j`) — a deliberate teaching device showing the same behavior with decreasing verbosity.

## 16. Enums

The courseware motivates enums by first showing the old pattern of `public static final int` constants (e.g. `ACTIVE = 1`, `INACTIVE = 2`) and listing its failures: no type safety (any `int` is accepted where the constant is expected), unreadable output (prints `1` instead of `"ACTIVE"`), no way to attach behavior, and namespace pollution. Java's `enum` keyword fixes all of these at once.

A **basic enum** (`public enum EmployeeStatus { ACTIVE, INACTIVE, TERMINATED, ON_LEAVE }`) creates a genuine reference *type* whose only legal values are the listed constants — assigning an int or String to that type is a compile error. Each constant is a singleton instance, so `==` comparison is always safe and reliable (unlike, say, boxed-Integer or String comparisons). Java's `enum` is fundamentally a **class under the hood** — implicitly extending `java.lang.Enum` — which is a stronger guarantee than Python's `enum.Enum`, where members are typically singletons too but the underlying type system doesn't give the same compile-time exhaustiveness/type-checking in `switch`. Every enum constant automatically gets `.name()` (declared identifier as String), `.ordinal()` (zero-based declaration-order index), a default `.toString()` equal to `.name()`, `.compareTo()` (compares by ordinal), and the auto-generated static methods `values()` (array of all constants) and `valueOf(String)` (look up by name, throwing `IllegalArgumentException` if no match — a gotcha worth remembering since it's easy to trigger with bad input).

**Enums work natively in `switch`** — both classic (`case ACTIVE: ... break;`, no need to qualify with `EmployeeStatus.ACTIVE`) and the modern Java 14+ arrow-switch expression form (`case ACTIVE -> "Full access";`), which the courseware favors as cleaner.

Because an enum is a class, **each constant can carry fields and methods**: declare `private final` fields, a constructor (implicitly `private`/package-visible — enum constructors can never be `public` since you cannot `new` an enum type externally), and instance methods, then pass constructor arguments in parentheses after each constant name, e.g. `ENGINEERING("Engineering", "Pune", 500000.0)`. Every constant shares the same set of methods but can hold different data.

Enums also support **per-constant behavior via abstract methods**: declare `public abstract` methods in the enum body, and each constant supplies its own body (`JUNIOR { @Override public double applyBonus(...) {...} }`). This is effectively a compile-time-safe substitute for a strategy pattern without extra classes.

Two specialized collections are enum-optimized: **`EnumSet`** — a bit-vector-backed `Set` implementation for enum types, created via `allOf`, `of`, `range`, or `complementOf`, and dramatically faster than a `HashSet<EnumType>`; and **`EnumMap`** — a `Map` keyed by an enum type, internally array-backed and faster than `HashMap` for enum keys. The courseware explicitly recommends preferring these over generic `HashSet`/`HashMap` whenever the key/element type is an enum.

The closing comparison table crystallizes why enums win over int/String constants: type safety, readable printing, switch support, methods/fields, iteration via `values()`, `null`-safety (each constant is a non-null singleton), and singleton guarantees — none of which raw constants provide.

### `DayOfWeek.java`

```java
     1	package com.acme.demo.day3.miscelleneous;
     3	public enum DayOfWeek {
     5		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
     7	}
```

- **Line 3** — `public enum DayOfWeek`: under the hood the compiler generates `final class DayOfWeek extends java.lang.Enum<DayOfWeek>` with `private` constructors, so it can never be subclassed or instantiated with `new` from outside.
- **Line 5** — each constant becomes a `public static final DayOfWeek` singleton in declaration order (backing `ordinal()`) — the "basic enum" form, just type-safe named constants. Note only six days are listed (no `SUNDAY`) — a data omission in the demo, not a syntax feature.

### `EnumDemo.java`

```java
     1	package com.acme.demo.day3.miscelleneous;
     3	public class EnumDemo {
     5		public static void main(String[] args) {
     7	//		String today = "Monday";
     8	//		today = "Friday";
     9	//		today = "Dryday";
    11	DayOfWeek today = DayOfWeek.MONDAY;
    12	System.out.println(today);
    13	today = DayOfWeek.FRIDAY;
    14	System.out.println(today);
    15	//		today = "Dryday"; // CE 
    17	}
    19	}
```
*(A fully commented-out earlier iteration follows below, exploring `final` static constants — `private final static int NUM`, `private final static Employee EMPLOYEE` — as a contrast point, showing how `final` fields (single-assignment) differ from enums (a fixed, closed set of typed values) even though both involve "constant-like" declarations.)*

- **Lines 7–9** (commented) — the **before** version using a plain `String` for `today`, deliberately including a typo-able value `"Dryday"` that would silently compile — the exact "no type safety" problem enums fix.
- **Line 12** — `System.out.println(today)` implicitly calls `.toString()`, which for a plain enum defaults to `.name()`, printing `MONDAY` — "readable by default" versus an int constant printing `1`.
- **Line 15** (commented) — `today = "Dryday"; // CE` — the payoff line: unlike the String version above, assigning a String literal to an enum-typed variable does not compile.

## 17. Exception Handling

An exception is a runtime event signaling disrupted normal flow; the JVM builds an exception object and throws it, and if nothing catches it, the JVM prints a stack trace and terminates. Java's exception hierarchy roots at `Throwable`, splitting into `Error` (JVM-level failures like `OutOfMemoryError`, `StackOverflowError` — never meant to be caught) and `Exception` (application-level problems). Under `Exception`, everything that is *not* a `RuntimeException` is a **checked** exception (e.g. `IOException`, `SQLException`); everything under `RuntimeException` (e.g. `NullPointerException`, `ArithmeticException`, `IllegalArgumentException`) is **unchecked**.

This checked/unchecked split is the single biggest Java-vs-Python divergence in error handling. **Checked exceptions are compiler-enforced**: if a method can throw one and doesn't catch it, it *must* declare it via `throws`, and callers are then forced to either catch it or re-declare it — the compiler will not let checked exceptions silently disappear. Python has no equivalent concept — all exceptions there are effectively "unchecked" in Java's sense; nothing forces a caller to acknowledge that a function might raise. Java's checked exceptions are meant for *external* failure modes (a missing file, a down database) that a caller genuinely needs to plan for; **unchecked exceptions represent programming bugs** (null dereference, bad index, invalid argument) that should be fixed in code, not routinely caught.

`try-catch` intercepts exceptions; `finally` **always executes** — whether the try succeeds, an exception is caught, an exception propagates uncaught (finally still runs before it propagates further up), or there's a `return` inside try/catch. The sole escape hatch is `System.exit()` inside the try, which terminates the JVM before `finally` gets a chance. **`try-with-resources`** (Java 7+) is the modern, preferred replacement for manual `finally`-based cleanup: any `AutoCloseable`/`Closeable` resource declared in the parentheses is closed automatically when the block exits, in **reverse declaration order**; if both the try body and the close() throw, the close-time exception is *suppressed* (attached to the primary exception's suppressed list) rather than replacing it — this preserves the original failure as the primary diagnostic signal.

**Multiple `catch` blocks** are checked top-to-bottom, first match wins, and the compiler enforces that more specific exception types must precede more general ones (a `catch(Exception e)` placed before a `catch(IOException e)` would be dead code and a compile error). **Multi-catch** (`catch (TypeA | TypeB e)`, Java 7+) lets unrelated exception types share one handler; the caught variable is implicitly `final`.

`throw` manually raises an exception instance from your own code — always with a descriptive message since it ends up in logs/stack traces. `throws` on a method signature is a *contract*, declaring which checked exceptions a method might propagate without handling them itself, obligating the caller to deal with them.

**Custom exceptions** extend `Exception` (to make them checked) or `RuntimeException` (to make them unchecked), typically providing constructors that build a descriptive message via `super(...)` and optionally accept a `Throwable cause`. **Exception chaining** — passing the original exception as `cause` when wrapping it in a new one — preserves the full diagnostic trail (`getCause()`), which is lost if you just swallow the original and throw a fresh exception with no reference back to it.

**Assertions** (`assert condition : message;`) are developer-only sanity checks, disabled by default at runtime (must be enabled with `-ea`), intended for internal invariants/postconditions — never for validating public method arguments (use `IllegalArgumentException` for that) or production error handling.

Best practices called out explicitly: catch specific exceptions rather than broad `catch (Exception e) {}`; never swallow an exception silently (at minimum log it); don't use exceptions for ordinary flow control; always prefer try-with-resources for cleanup; and always write meaningful exception messages.

### `ExceptionDemo.java`

```java
     1	package com.acme.demo.day2.exception;
     3	import java.util.InputMismatchException;
     4	import java.util.Scanner;
     6	public class ExceptionDemo {
     8		public static void main(String[] args) {
    10	int num3 = 0;
    12	try (Scanner sc = new Scanner(System.in);) {
    13	System.out.println("Enter an integer: ");
    14	int num = sc.nextInt();
    15	System.out.println("Enter another one: ");
    16	int num2 = sc.nextInt();
    17	num3 = num / num2;
    18	} catch (InputMismatchException | ArithmeticException e) {
    19	System.out.println("Wrong!");
    20	} finally {
    21	System.out.println(num3);
    22	}
    24	}
    26	}
```
*(Retains commented-out earlier revisions: manual `Scanner`/`sc.close()` — the pre-Java-7 pattern `try-with-resources` replaces — separate specific-before-general catches, and an earliest version with no try/catch, letting the exception propagate uncaught.)*

- **Lines 10, 12** — `int num3 = 0;` is declared *outside* the try block so it's still printable in `finally` even if the division aborts; `try (Scanner sc = new Scanner(System.in);)` is **try-with-resources** — `Scanner` implements `Closeable`, so it's closed automatically on any exit path.
- **Lines 17–18** — *integer* division throws `ArithmeticException` ("/ by zero") if `num2` is `0` (unlike floating-point division, which would silently produce `Infinity`/`NaN`); `catch (InputMismatchException | ArithmeticException e)` is **multi-catch** — one handler for two unrelated unchecked types, both printing the generic "Wrong!", a simplification less informative than separate catches.
- **Line 21** — prints `num3`, `0` if any exception occurred or the real quotient otherwise — proving why the variable must be initialized outside the try.

### `NoAgeEligibilityException.java`

```java
     1	package com.acme.demo.day2.exception;
     3	public class NoAgeEligibilityException extends RuntimeException {
     5		private static final long serialVersionUID = 628355502492179165L;
     7	public NoAgeEligibilityException() {
     8	super();
     9	}
    11	public NoAgeEligibilityException(String message) {
    12	super(message);
    13	}
    14	}
```

- **Line 3** — extending `RuntimeException` (not `Exception`) makes this a **custom unchecked exception** — callers are *not* compiler-forced to catch or declare it, matching the courseware's guidance that unchecked is right for violated-business-rule exceptions.
- **Line 5** — `serialVersionUID` is auto-generated by IDEs to silence a compiler warning (since `Throwable` implements `Serializable`) — no bearing on normal exception-handling behavior.
- **Lines 11–13** — the message-taking constructor forwards to `super(message)` so `getMessage()` returns that text — this is the one actually used by `ThrowDemo.java`.

### `ThrowDemo.java`

```java
     1	package com.acme.demo.day2.exception;
     3	public class ThrowDemo {
     5		public static void main(String[] args) {
     6	System.out.println("Start");
     7	try {
     8	ThrowDemo.checkEligibility(17);
     9	} catch (RuntimeException e) {
    10	e.printStackTrace();
    11	}
    12	System.out.println("End");
    13	}
    15	static void checkEligibility(int age) {
    16	if (age >= 18) {
    17	System.out.println("Eligible");
    18	} else {
    19	// code
    20	throw new NoAgeEligibilityException("Age is < 18!");
    21	}
    23	}
    25	}
```
*(Followed by a fully commented-out earlier draft that throws a plain `RuntimeException` instead of the custom type.)*

- **Line 9** — `catch (RuntimeException e)` catches by the **supertype** rather than the specific `NoAgeEligibilityException` — works via polymorphic catching, but is looser than ideal per the "catch specific" best practice.
- **Line 15** — no `throws` clause needed on `checkEligibility` since `NoAgeEligibilityException` is unchecked — a checked exception would force a `throws` declaration here instead.
- **Line 20** — `throw new NoAgeEligibilityException("Age is < 18!");` manually raises the custom exception, unwinding immediately to the nearest matching catch.

### `ThrowsDemo.java`

```java
     1	package com.acme.demo.day2.exception;
     3	public class ThrowsDemo {
     5	public static void main(String[] args) {
     6	System.out.println("Start");
     7	try {
     8	ThrowsDemo.printNums();
     9	} catch (InterruptedException e) {
    10	e.printStackTrace(); // custom handling 
    11	}
    12	System.out.println("End");
    13	}
    15	public static void printNums() throws InterruptedException {
    16	for (int i = 1; i <= 10; i++) {
    17	Thread.sleep(250);
    18	System.out.println(i);
    19	}
    20	}
    21	}
```
*(Followed by a commented-out earlier draft where `printNums` catches `InterruptedException` internally via its own try/catch around `Thread.sleep`, instead of declaring `throws`.)*

- **Line 10** — `catch (InterruptedException e)`: `InterruptedException` is a **checked** exception, so the compiler *forces* this catch (or a `throws` re-declaration on `main`) — omitting it is a compile error. The clearest example in the code set of the checked-exception contract in action.
- **Line 16** — `throws InterruptedException` is the **declaration half of the contract** — it tells callers "I might propagate this; you must handle or re-declare it." Without it, line 18's `Thread.sleep(250)` (itself `throws InterruptedException`) wouldn't compile here without a local try/catch.

## 18. Annotations

An annotation is metadata attached to code — a structured tag on a class, method, field, or parameter — that by itself does nothing at runtime; its effect comes entirely from tools, the compiler, or a framework that reads and reacts to it via reflection or compile-time processing. This is conceptually similar to Python decorators in that both attach extra information/behavior to a declaration, but mechanically very different: a Python decorator is executable code that wraps/replaces the decorated object at *definition time*; a Java annotation is inert declarative data that some *external reader* (compiler, JVM reflection API, annotation processor) chooses to act on — the annotation itself has no `__call__`.

**Built-in annotations** covered: `@Override` tells the compiler to verify the annotated method actually overrides a superclass/interface method — without it, a misspelled method name silently creates an unrelated new method instead of overriding, a classic and hard-to-spot bug that `@Override` converts into a compile error. `@Deprecated` marks a member as outdated, causing compiler warnings at call sites; since Java 9 it accepts `since` and `forRemoval` attributes, with `forRemoval = true` signaling a stronger "will actually be deleted" warning. `@SuppressWarnings("unchecked")` (or with multiple values, `{"unchecked","deprecation"}`) tells the compiler to stop emitting a specific warning category for the annotated element — use sparingly since warnings usually indicate a real issue. `@FunctionalInterface` (as seen in `Calc.java`/`Tax` above) enforces that an interface has exactly one abstract method, enabling lambda-target compatibility and catching accidental extra abstract methods at compile time. `@SafeVarargs` suppresses unchecked warnings on varargs methods using generics, for methods the author has manually verified are type-safe.

**Meta-annotations** — annotations that annotate other annotation declarations, controlling how *your own* annotations behave. `@Retention(RetentionPolicy.X)` controls how long the annotation's data survives: `SOURCE` (compiler-only, discarded after compilation — e.g. `@Override`, `@SuppressWarnings`), `CLASS` (kept in the `.class` file but not visible at runtime — the default if unspecified), or `RUNTIME` (available via the Reflection API at runtime) — `RUNTIME` retention is *mandatory* if you intend to read the annotation programmatically, which is the crux of every custom-annotation-processing example. `@Target({ElementType.X, ...})` restricts which kinds of declarations (`TYPE`, `METHOD`, `FIELD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `PACKAGE`) the annotation may legally be placed on — misuse is a compile error. `@Documented` includes the annotation in generated Javadoc. `@Inherited` makes a `TYPE`-target annotation automatically apply to subclasses when placed on a superclass (checked via `Class#isAnnotationPresent`).

**Writing custom annotations** uses the `@interface` keyword. Elements inside look like abstract methods (`String action() default "UNKNOWN";`) but are really typed attribute slots with optional default values; allowed types are primitives, `String`, `Class`, enums, other annotations, and arrays of these. A single element named `value()` gets special syntax sugar — it can be supplied without naming it (`@AuditLog("CREATE")`). The courseware builds a full worked example: an `@AuditLog(action=..., module=..., enabled=...)` annotation applied to service methods, and a separate `AuditProcessor` that uses `java.lang.reflect.Method.isAnnotationPresent(...)` / `getAnnotation(...)` to inspect each method and act differently based on the annotation's attribute values (only reachable because the annotation was declared `@Retention(RUNTIME)`). A second worked example builds field-level validation annotations (`@NotBlank`, `@MinValue(value=..., message=...)`) read via `Field.setAccessible(true)` + `Field.get(obj)` + `isAnnotationPresent`/`getAnnotation` — explicitly described as a miniature version of what Spring's `@Valid` + Hibernate Validator do under the hood. The courseware closes by tying this to real frameworks (`@Component`, `@Autowired`, `@RestController`, `@GetMapping`, `@Transactional` in Spring; `@Entity`, `@Column` in JPA/Hibernate) — all of which are just custom `RUNTIME`-retention annotations read via reflection (or compile-time annotation processing) exactly like the hand-built examples here.

### Code

No dedicated annotation example exists in the course code folder — see courseware explanation above only. A search of the `Core Java` source tree (`grep`-ing for `Optional`, `@interface`, `@Retention`, `@Target`, `@Documented`, `@Inherited`) found no custom annotation declarations or reflection-based annotation-processing code anywhere in `com.acme.demo`. The only annotations present in the codebase are the standard built-ins (`@Override`, `@FunctionalInterface`) scattered incidentally across unrelated demo files (e.g. `Calc.java` in the Inner Classes section above), not a purpose-built annotations lesson. `day3/miscelleneous/Demo.java` — checked since it was otherwise unassigned — is an empty skeleton (`public class Demo { public static void main(String[] args) { } }`) with no annotation content, confirming there is no annotations-specific demo to reproduce.

## 19. Lambda Expressions

A lambda expression is Java's syntax for an anonymous function: a block of code (parameters + body) with no name and no enclosing class declaration. It exists to eliminate the ceremony of anonymous inner classes — a six-line `new Comparator<Employee>(){...}` collapses to `(a, b) -> Double.compare(a.getSalary(), b.getSalary())`. Crucially, a lambda can only appear where a **functional interface** (an interface with exactly one abstract method) is the target type — the lambda body becomes the implementation of that single method.

Syntax forms, from most to least explicit: full form with typed parameters and braces/`return`; type inference (omit parameter types); single-expression form (omit `return` and braces — the expression's value is returned implicitly); single-parameter form (parentheses become optional, e.g. `name -> name.toUpperCase()`); zero-parameter form (empty parentheses are *mandatory*: `() -> ...`); and multi-line bodies, which *require* braces and an explicit `return`.

The courseware walks the same behavior through four stages — named class → anonymous class → full-form lambda → concise lambda — to show a lambda is not new capability, just concise syntax for implementing a single-method interface. It previews `java.util.function` types (`Runnable`, `Predicate`, `Function`, `Consumer`) as common lambda targets, deferred to Module 20.

**Variable capture — effectively final.** A lambda may reference local variables from its enclosing scope, but only if those variables are effectively final (declared `final`, or simply never reassigned after being read by the lambda). Reassigning a captured local is a *compile* error, not a runtime one. The rationale: lambdas can outlive the stack frame that created them (run on another thread, later), so a variable that could mutate after capture would introduce race conditions — Java sidesteps this by disallowing mutation entirely rather than doing copy-on-write or synchronization. Instance and static fields are exempt from this restriction since they're accessed via `this`/the class, not captured by value.

**Method references** are a further contraction for lambdas that only delegate to an existing method, in four flavors: static (`Integer::parseInt`), bound instance (`emp::getName` — instance already fixed), unbound/arbitrary instance (`Employee::getRole`, where the lambda's parameter *becomes* the receiver — very common in `.map(Employee::getName)`), and constructor references (`Employee::new`).

**Lambdas vs. anonymous classes** — not the same construct wearing different syntax: `this` inside a lambda refers to the *enclosing* instance (lexical scoping, no new scope created), whereas `this` inside an anonymous class refers to the anonymous class instance itself; lambdas are stateless and can only target single-abstract-method interfaces (anonymous classes can implement multi-method interfaces and hold their own fields); and lambdas compile via `invokedynamic` rather than generating a separate `.class` file per lambda.

**Composition**: `Predicate` exposes `and()`, `or()`, `negate()`; `Function` exposes `andThen()` (apply this, then the argument) and `compose()` (apply the argument, then this) for building pipelines out of small lambdas.

### Real code — `Java8Features.java`

This single file is the source for topics 19, 20, and 21. Full listing shown once here; topics 20 and 21 reference line ranges from this same listing without repeating the whole file.

```java
     1	package com.acme.demo.day3.features;
     3	import java.time.LocalDate;
     4	import java.time.Period;
     5	import java.util.*;
     6	import java.util.function.BinaryOperator;
     7	import java.util.function.Supplier;
     8	import java.util.stream.Collectors;
    10	public class Java8Features {
    12		public static void main(String[] args) {
    14	// Lambda Expression
    16	Runnable r = () -> System.out.println("Hi from Lambda");
    18	r.run();
    20	// Stream API
    22	List<String> names = List.of("Sonu", "Monu", "Tonu", "Ponu");
    24	//		 option 1 
    25	names.forEach((name) -> {
    26	System.out.println(name);
    27	});
    28	//		option 2 
    29	names.forEach(name -> System.out.println(name));
    30	//		option 3 - method reference 
    32	names.forEach(System.out::println);
    34	names.stream().filter(n -> n.startsWith("A")).forEach(System.out::println);
    36	// map()
    38	List<String> upper = names.stream().map(String::toUpperCase).collect(Collectors.toList());
    40	System.out.println(upper);
    42	// sorted()
    44	names.stream().sorted().forEach(System.out::println);
    46	// count()
    48	long count = names.stream().filter(n -> n.startsWith("A")).count();
    50	System.out.println(count);
    52	// Optional
    54	Optional<String> name = Optional.of("Sonu");
    55	//		name.
    57	name.ifPresent(System.out::println);
    59	System.out.println(name.orElse("Unknown"));
    61	// forEach + Method Reference
    63	//		names.forEach(System.out::println);
    65	// Date Time API
    67	LocalDate today = LocalDate.now();
    68	//		today.
    70	LocalDate birthday = LocalDate.of(1990, 5, 15);
    72	Period age = Period.between(birthday, today);
    74	System.out.println(age.getYears());
    76	// Default Method
    78	MyGreeter g = new MyGreeter();
    80	g.greet();
    82	// Functional Interface
    84	Calculator c = (a, b) -> a + b;
    86	System.out.println(c.add(10, 20));
    88	// Predicate
    89	List<Integer> nums = List.of(10, 15, 20, 25);
    91	nums.stream().filter(n -> n % 2 == 0).forEach(System.out::println);
    93	// Consumer
    95	names.forEach(n -> System.out.println("Hello " + n));
    97	// Supplier
    99	Supplier<String> s = () -> "Java 8";
   101	System.out.println(s.get());
   103	// Binary Operator
   105	BinaryOperator<Integer> bo = (a, b) -> a * b;
   107	System.out.println(bo.apply(5, 6));
   108	}
   109	}
   111	// Default Method Example
   113	interface Greeter {
   115	default void greet() {
   117	System.out.println("Hello!");
   118	}
   119	}
   121	class MyGreeter implements Greeter {
   123	}
   125	// Functional Interface Example
   127	@FunctionalInterface
   128	interface Calculator {
   130	int add(int a, int b);
   131	}
```
*(Note: source line numbering has a duplicate `89` in the original file around the `nums` declaration — reproduced exactly as it appears via `cat -n`.)*

**Line-by-line — lambda syntax focus:**

- **Line 16** — `Runnable r = () -> ...;` — zero-parameter lambda form (`()` mandatory, unlike Python's implicit no-arg lambda); invoked via `r.run()`, whichever method the target interface declares, not `r()` as Python would.
- **Lines 25–32** — full-form `(name) -> { ... }` versus parens/braces-dropped `name -> ...` are functionally identical, the latter idiomatic; `System.out::println` (line 32) is a method reference, semantically equivalent and preferred in real pipelines.
- **Line 38, 84** — `String::toUpperCase` is an unbound instance method reference (stream element becomes the receiver); `Calculator c = (a, b) -> a + b;` targets a *custom* functional interface (line 127) — Java lambdas are inherently tied to a target type, unlike Python's first-class lambdas.
- No local variable in this file is captured and later reassigned, so the effectively-final rule is never demonstrated here — a gap between the courseware's compile-error example and this working demo.

## 20. Functional Interfaces

A functional interface is any interface with **exactly one abstract method** — that method is the "shape" a lambda must fill. `@FunctionalInterface` is an optional but recommended annotation: it makes the compiler *enforce* the single-abstract-method rule at compile time (fails to build if a second abstract method is added), functioning like a documented contract rather than a language requirement — the annotation itself has no runtime effect.

Java 8 ships 43 such interfaces in `java.util.function`, but they reduce to a handful of core shapes, each covered with its two-argument sibling:

- **`Predicate<T>`** — `boolean test(T)` — condition-testing; composable via `and()`, `or()`, `negate()`, and the static `Predicate.not()` (Java 11+). Two-arg sibling: `BiPredicate<T,U>`.
- **`Function<T,R>`** — `R apply(T)` — transforms a value; composable via `andThen()` (this-then-arg) and `compose()` (arg-then-this). `Function.identity()` returns a no-op passthrough function. `UnaryOperator<T>` and `BinaryOperator<T>` are specializations of `Function`/`BiFunction` where the input(s) and output share the same type. Two-arg sibling: `BiFunction<T,U,R>`.
- **`Consumer<T>`** — `void accept(T)` — side-effecting, no return value; composable via `andThen()` to chain consumers sequentially on the same input. Two-arg sibling: `BiConsumer<T,U>`.
- **`Supplier<T>`** — `T get()` — no input, produces a value; the canonical use is *lazy* evaluation/default creation — the supplier is only invoked (`.get()`) if actually needed, avoiding unnecessary object construction.

**Primitive specializations** (`IntPredicate`, `DoubleConsumer`, `LongSupplier`, etc.) exist to avoid autoboxing overhead when working with primitives in bulk — a Java-specific performance concern with no equivalent friction in Python, where ints are objects regardless.

**Writing custom functional interfaces**: any time none of the ~43 built-ins fit (e.g., a 3-argument transform), you declare your own single-abstract-method interface and annotate it `@FunctionalInterface`. The lambda-needs-a-target-type rule applies identically to custom interfaces as to built-in ones — there's no such thing as an untyped lambda value in Java, unlike Python where `lambda x: x+1` is a first-class object independent of any interface/protocol.

### Real code — same file, functional-interface focus

Relevant excerpt (original line numbers from the full listing above):

```java
     6	import java.util.function.BinaryOperator;
     7	import java.util.function.Supplier;
    16	Runnable r = () -> System.out.println("Hi from Lambda");
    18	r.run();
    84	Calculator c = (a, b) -> a + b;
    86	System.out.println(c.add(10, 20));
    99	Supplier<String> s = () -> "Java 8";
   101	System.out.println(s.get());
   105	BinaryOperator<Integer> bo = (a, b) -> a * b;
   107	System.out.println(bo.apply(5, 6));
   127	@FunctionalInterface
   128	interface Calculator {
   130	int add(int a, int b);
   131	}
```

**Line-by-line — which functional interfaces are used, and why:**

- **Line 84** — `Calculator c = (a, b) -> a + b;` targets the custom `Calculator` interface (lines 127–131), demonstrating that "functional interface" isn't restricted to `java.util.function` — any interface with exactly one abstract method qualifies.
- **Lines 105–107** — `BinaryOperator<Integer> bo = (a, b) -> a * b;` is a `BiFunction<T,T,T>` specialization where all three type parameters collapse to one — preferred over a custom interface since it interoperates with any API expecting `BinaryOperator<Integer>` (e.g. `Stream.reduce`).
- **Lines 127–131** — the hand-written `@FunctionalInterface Calculator` causes the compiler to error if a second abstract method were later added — functionally analogous to `BinaryOperator<Integer>`, illustrating that reaching for `java.util.function` first is usually preferable to a redundant custom interface.
- Also present in the file (not excerpted above): `Predicate` (line 91) and `Consumer` (line 95), both inferred implicitly from the target parameter type.

## 21. Stream API

A stream is a **pipeline**, not a data structure — it stores nothing and processes elements on demand, following the model `Source → intermediate ops → intermediate ops → terminal op → result`. This is the closest Java analogue to Python generator pipelines/generator expressions: like a Python generator, nothing runs until something pulls a result, but unlike Python generators, streams give you a fluent method-chain API (`.filter().map().collect()`) rather than nested comprehensions.

Four properties are called out explicitly:
- **Lazy** — intermediate operations (`filter`, `map`, `sorted`, `distinct`, `limit`, `skip`, `flatMap`, `peek`) just describe work; nothing executes until a **terminal** operation (`forEach`, `collect`, `reduce`, `count`, `findFirst`/`findAny`, `anyMatch`/`allMatch`/`noneMatch`, `min`/`max`, `toArray`) is invoked. The courseware demonstrates this with a `peek`-instrumented pipeline where `findFirst()` short-circuits — only the first matching element is ever processed, proving laziness enables early termination, not just deferred execution.
- **Non-mutating** — a stream never modifies its source collection; it produces a new result.
- **Consumed once** — reusing a stream after a terminal op throws `IllegalStateException`. Different from Python iterables, which can sometimes be re-iterated depending on type — Java streams are explicitly single-use regardless of source.
- **Sequential or parallel** — `parallelStream()` (or `.parallel()`) splits work across the common `ForkJoinPool`.

**Creating streams**: from a `Collection` (`.stream()`), an array (`Arrays.stream(...)`), varargs (`Stream.of(...)`), `Stream.empty()`, infinite generators (`Stream.generate(Supplier)`, `Stream.iterate(seed, UnaryOperator)` — both require `.limit()` to terminate), and primitive-specialized `IntStream`/`LongStream`/`DoubleStream` (via `.range()`, `.rangeClosed()`, `.of()`) which avoid boxing and unlock numeric terminal ops like `sum()`/`average()`.

**Key intermediate ops**: `filter`, `map` (1:1 transform), `mapToInt/Double/Long` (transform into a primitive stream to unlock `sum`/`average`/`min`/`max` without boxing), `flatMap` (flattens a stream-of-streams — the direct analogue of Python's nested-comprehension flattening or `itertools.chain.from_iterable`), `sorted` (natural or `Comparator`-based, composable with `.thenComparing()`), `distinct`, `limit`/`skip` (pagination), and `peek` (side-effect debugging hook — explicitly **debug-only**, not for production side effects, since its execution isn't guaranteed if the pipeline short-circuits).

**Key terminal ops**: `forEach`; `collect(Collector)` with `Collectors.toList()/toSet()/toMap()/joining()`; `groupingBy` (→ `Map<K, List<V>>`, optionally with a downstream collector like `counting()` or `averagingDouble()`); `partitioningBy` (splits into exactly `Map<Boolean, List<T>>`); `count`; `reduce` (aggregate to one value — with no identity, returns `Optional<T>` since the stream could be empty; with an identity value, returns the raw type directly); `findFirst`/`findAny`; `anyMatch`/`allMatch`/`noneMatch`; `min`/`max` (return `Optional<T>`); `toArray`. `Collectors.summarizingDouble` produces a `DoubleSummaryStatistics` with count/sum/average/min/max in one pass.

**Parallel streams**: appropriate for large datasets and CPU-bound, stateless, order-independent work; inappropriate for small collections (thread overhead dominates), shared mutable state (race conditions — the courseware shows `forEach(names::add)` on a plain `ArrayList` as an explicit anti-pattern, versus thread-safe `collect(Collectors.toList())`), I/O-bound work, or when strict ordering is required.

**Common mistakes flagged explicitly**: reusing a consumed stream; mutating the source collection from inside a stream operation (`ConcurrentModificationException`); forgetting the terminal operation entirely (pipeline silently does nothing); and trying to accumulate into a local variable via `forEach` (won't compile since the variable isn't effectively final — use `reduce`/`mapToX().sum()` instead).

### Real code — same file, stream-pipeline focus

Relevant excerpt (original line numbers):

```java
     8	import java.util.stream.Collectors;
    22	List<String> names = List.of("Sonu", "Monu", "Tonu", "Ponu");
    34	names.stream().filter(n -> n.startsWith("A")).forEach(System.out::println);
    38	List<String> upper = names.stream().map(String::toUpperCase).collect(Collectors.toList());
    44	names.stream().sorted().forEach(System.out::println);
    48	long count = names.stream().filter(n -> n.startsWith("A")).count();
    91	nums.stream().filter(n -> n % 2 == 0).forEach(System.out::println);
```

**Line-by-line — stream pipeline focus:**

- **Line 22** — `List.of(...)` creates an **immutable** list (Java 9+); the stream source doesn't need to be mutable anyway since streams never mutate their source.
- **Line 34** — `filter` is intermediate (lazy); `forEach` is terminal, triggering iteration. Since no name starts with "A", the pipeline prints nothing — a real but silent runtime outcome, not a bug.
- **Line 38** — `map` (1:1 transform) then `collect(Collectors.toList())` materializes the lazy pipeline into a concrete list — Java's equivalent of a Python list comprehension, but explicit about the lazy-build/eager-materialize split.
- **Line 48** — a *fresh* stream must be created (`names.stream()` called again) since streams are single-use — reusing line 34's stream would throw `IllegalStateException`. `count()` returns `long`, not `int`.
- **Line 91** — stays a boxed `Stream<Integer>` rather than an `IntStream`, since `nums.stream()` on `List<Integer>` yields a reference-type stream — the `mapToInt`/`IntStream` primitive-specialization pattern isn't exercised here.
- Not present in this file: `flatMap`, `groupingBy`/`partitioningBy`, `reduce`, `min`/`max`, `limit`/`skip`, `peek`, or parallel streams.

## 22. Optional

`Optional<T>` is Java 8's explicit container for "a value that may or may not be present," introduced as a direct response to `null`'s ambiguity (the module opens with Tony Hoare's "billion-dollar mistake" framing). A method returning `Optional<Employee>` documents in its *type signature* that absence is a legitimate outcome — unlike returning a bare `Employee` that might silently be `null`, where callers have no compiler-enforced signal to check.

**Creation**: `Optional.of(value)` (throws `NullPointerException` immediately if `value` is null — use only when you're certain it's non-null); `Optional.ofNullable(value)` (safe wrapper — empty if null, present otherwise, the usual choice when wrapping a value from an API that might return null); `Optional.empty()` (explicit empty instance).

**Checking/extracting**: `isPresent()`/`isEmpty()` (Java 11+) for explicit boolean checks; `get()` throws `NoSuchElementException` if empty and should essentially never be called without a preceding presence check — the courseware steers toward the functional alternatives instead. `ifPresent(Consumer)` runs code only if present, with no `if`-block needed; `ifPresentOrElse(Consumer, Runnable)` (Java 9+) supplies both branches at once, replacing an `if/else`.

**Defaults**: `orElse(value)` always evaluates its argument eagerly, even when the Optional is present — a real cost if constructing the default is expensive. `orElseGet(Supplier)` is the lazy counterpart, only invoking the supplier when the Optional is actually empty — directly mirroring `Supplier`'s lazy-evaluation role from Module 20. `orElseThrow(Supplier<Exception>)` throws a custom exception when empty; the no-arg `orElseThrow()` (Java 10+) throws `NoSuchElementException` without needing a supplier.

**Transformation/chaining — Optional's real value**: `map(Function)` transforms the contained value if present, passes through empty otherwise, with no explicit null check required at any step. `flatMap(Function<T, Optional<R>>)` is needed whenever the mapping function itself returns an `Optional`, to avoid nesting (`Optional<Optional<T>>`) — directly analogous to why `flatMap` exists on streams. `filter(Predicate)` keeps the value only if it matches, otherwise collapses to empty. The courseware's centerpiece example shows a 3-level null-check chain collapsing to a single 5-line `.map().flatMap().map().orElse()` chain — because an empty `Optional` short-circuits the rest of the chain automatically, the equivalent of Python's `obj and obj.attr and obj.attr.attr2` idiom but type-safe and without repeating the object at each step.

**Where to use Optional — rules explicitly stated**: yes, as a **method return type** to signal possible absence. No, as a **method parameter** (awkward — prefer overloading or a null check with a clear exception). No, as a **class field** (not `Serializable`, adds wrapper overhead — just use `null` or an empty string directly).

**`stream()` (Java 9+)**: converts an `Optional<T>` into a `Stream` of zero or one elements, so a `Stream<Optional<T>>` can be flattened with `.flatMap(Optional::stream)` to drop all the empties in one step — avoiding manual filtering of `Optional`s.

### Real code — Optional usage in `Java8Features.java`

`JavaFeatures.java` is an empty stub (just a package declaration and an empty class body — no Optional or any other code). The only real Optional example in the course code tree is the short block inside `Java8Features.java` (confirmed via `grep -ril "Optional"` across the whole `com/acme/demo` tree — only this one file matches):

```java
    54	Optional<String> name = Optional.of("Sonu");
    55	//		name.
    57	name.ifPresent(System.out::println);
    59	System.out.println(name.orElse("Unknown"));
```

**Line-by-line:**

- **Line 54** — `Optional.of("Sonu")`, not `Optional.ofNullable` — correct since `"Sonu"` is a compile-time non-null literal; using `.of` on a value that could actually be null would risk an immediate `NullPointerException` at wrap time.
- **Line 57** — `name.ifPresent(System.out::println);` executes the `Consumer` since `name` is present — no `if (name.isPresent())` guard needed, the point of the functional style.
- **Line 59** — `name.orElse("Unknown")` returns the contained value directly since `name` is present (prints `"Sonu"`, not the fallback) — this demo never exercises the *empty* path, nor `orElseGet`, `orElseThrow`, `map`, `flatMap`, `filter`, `ifPresentOrElse`, or `stream()`; rely on the courseware explanation above for those.
</content>

---

## 23. Multithreading

**What it teaches:** A thread is the smallest schedulable unit of execution; every Java program starts with one (`main`). Multithreading lets a program do several things "simultaneously" — either via time-sliced context switching on one core, or true parallelism across cores.

**Thread lifecycle:** `NEW → RUNNABLE → RUNNING → (BLOCKED/WAITING/TIMED_WAITING) → TERMINATED`. `NEW` means `start()` hasn't been called; `BLOCKED` means waiting on a lock; `WAITING`/`TIMED_WAITING` cover `join()`/`notify()` and `sleep()`/timed `join()` respectively.

**Creating threads**, three ways, in order of preference: (1) extend `Thread` and override `run()` — couples task to thread, blocks further inheritance; (2) implement `Runnable` and pass it to a `Thread` — preferred, decouples "what" from "how," a class can still extend something else; (3) a lambda, since `Runnable` is a functional interface (one abstract method: `run()`). Critically, **`start()` creates a new OS thread and schedules `run()` on it; calling `run()` directly just executes it synchronously on the current thread** — a very common exam trap.

**Key `Thread` methods and gotchas:**
- `sleep(ms)` pauses the *current* thread and throws checked `InterruptedException` — always catch it and call `Thread.currentThread().interrupt()` to restore the interrupted status flag rather than swallowing it silently.
- `join()` blocks the *caller* until the target thread finishes; `join(timeout)` gives up waiting after the timeout but doesn't kill the thread.
- `interrupt()` only *signals* — a thread must cooperatively check `isInterrupted()` or handle `InterruptedException` to actually stop; nothing forces it to stop.
- `setDaemon(true)` must be called *before* `start()`. Daemon threads are killed automatically when all non-daemon threads finish — good for heartbeats/log flushing, bad for anything that must complete (its work can be cut off mid-operation).
- `setPriority(int)` is only a scheduling *hint* (1–10, default 5/`NORM_PRIORITY`) — the OS is not obligated to honor it.

**Race conditions:** `count++` is not atomic — it's read-modify-write across three separate steps, so two threads interleaving can lose updates. This is the classic example of why "looks like one line" doesn't mean "one operation."

**`synchronized`:** guarantees (1) mutual exclusion — only one thread holds the intrinsic lock/monitor of a given object at a time, and (2) visibility — changes made inside a synchronized block become visible to the next thread that acquires the same lock (this second guarantee is often forgotten but is just as important as exclusion). Prefer synchronized *blocks* over whole synchronized *methods* — synchronize only the smallest section touching shared state, to avoid needlessly serializing unrelated work.

**`volatile`:** guarantees visibility (reads/writes go to main memory, not a thread-local CPU cache) but **not** atomicity — fine for a simple boolean flag (`running`), unsatisfactory for compound operations like `count++`.

**Atomics (`java.util.concurrent.atomic`):** `AtomicInteger`, etc. give lock-free thread-safe operations (`incrementAndGet()`, `compareAndSet()`) using CPU-level CAS (compare-and-swap) instructions — faster than `synchronized` for simple counters because there's no OS-level lock contention.

**Deadlock:** two threads each hold a lock the other needs, both block forever waiting for the other to release. Prevention: always acquire multiple locks in the same global order across all threads, use `tryLock()` with a timeout (from `java.util.concurrent.locks`), or minimize how many locks are held at once.

**`ThreadLocal<T>`:** gives each thread its own independent copy of a variable — no sharing, no synchronization needed. Widely used in web frameworks for per-request context (current user, transaction ID) without threading it through every method signature.

**Cross-language note (Python/ML background):** Python's GIL means only one thread executes Python bytecode at a time, so classic race conditions on plain Python objects are rarer (though not absent — `x += 1` can still race in edge cases, and I/O-releases-the-GIL windows create real races). Java has no GIL — true parallel execution on multi-core hardware means every shared mutable field is a potential race unless explicitly protected. This is why Java's memory model (`volatile`, `synchronized`, happens-before) is a first-class exam topic in a way it rarely is in Python.

### Code — `day3/threads/MultiThread.java`

```java
1:	package com.acme.demo.day3.threads;
3:	public class MultiThread extends Thread {
5:		public int num;
7:		@Override
8:		public void run() {
9:			printNums();
10:	}
12:		public synchronized void printNums() {
13:			for (int i = 1; i <= 10; i++) {
14:				num++;
15:				try {
16:					Thread.sleep(250);
17:				} catch (InterruptedException e) {
18:					e.printStackTrace();
19:				}
20:				System.out.print(i);
21:			}
22:			System.out.println("num: " + num);
23:		}
24:	}
```

- **Line 3** — `MultiThread extends Thread` — the "extend Thread" style of creation; every instance *is-a* `Thread`.
- **Line 12** — `synchronized` on an *instance* method locks on `this` (the specific object). Since `MultithreadingDemo` creates three separate `MultiThread` objects, each has its own lock — `synchronized` here does **not** serialize them against each other; it only matters if two threads share the *same* instance. A common misunderstanding: `synchronized` means "threads sharing this object's monitor wait for each other," not "all threads wait for each other."
- **Lines 15–19** — `Thread.sleep(250)`'s checked `InterruptedException` is caught and `printStackTrace()`'d, but the program does *not* call `Thread.currentThread().interrupt()` to restore the interrupt flag — technically an anti-pattern flagged in the courseware, common in throwaway demos.

### Code — `day3/threads/MultithreadingDemo.java`

```java
1:	package com.acme.demo.day3.threads;
3:	public class MultithreadingDemo {
5:		public static void main(String[] args) {
7:			MultiThread obj = new MultiThread();
8:			obj.start();
9:			MultiThread obj2 = new MultiThread();
10:		obj2.start();
11:			MultiThread obj3 = new MultiThread();
12:		obj3.start();
14://		for (int i = 1; i <= 10; i++) {
15://			MultiThread obj = new MultiThread();
16://			obj.start();
17://		}
19:	}
20:	}
```

- **Lines 7–12** — `start()` (not `run()`) is what actually spins up a new OS thread; calling `obj.run()` instead would execute all three "threads" sequentially on the main thread with no concurrency at all — the single most common mistake this module warns about.
- `main()` doesn't `join()` the threads, so the JVM keeps running until all non-daemon threads finish on their own.

### Code — `day3/threads/ThreadDemo.java`

```java
Thread t1 = new Thread(new Worker(), "Sonu");   // new Thread(Runnable, String) -- the "implement Runnable" style
Thread t2 = new Thread(new Worker(), "Monu");
Thread t3 = new Thread(new Worker(), "Tonu");
t1.start(); t2.start(); t3.start();
```

### Code — `day3/threads/Worker.java`

```java
class Worker implements Runnable {           // preferred: pure task, agnostic to how it's run
	public void run() { method(); }
	public void method() {
		for (int i = 1; i <= 3; i++)
			System.out.println(Thread.currentThread().getName() + " working...");
	}
}
```

- The second `Thread` constructor argument names the thread, retrievable via `Thread.currentThread().getName()` — how a `Runnable` (which has no `Thread` reference of its own) discovers its own thread's name at runtime. Output ordering across the three started threads is scheduler-dependent, not guaranteed.

---

## 24. Executor Framework

**What it teaches:** Manually managing raw `Thread` objects doesn't scale — each OS thread costs ~512KB–1MB of stack, there's no bound on how many you create, no built-in way to get a return value, no clean exception propagation, and no scheduling support. `java.util.concurrent`'s Executor Framework separates the *task* (what to run) from the *execution policy* (how many threads, when, how failures are handled) — conceptually the same separation Python's `concurrent.futures.Executor` (`ThreadPoolExecutor`/`ProcessPoolExecutor`) provides, though Java's version predates Python's by several years and integrates far more deeply with the language (checked-exception-aware `Callable`, `Future`, scheduling, and now virtual threads in Java 21+).

**Executors factory methods:**
- `newFixedThreadPool(n)` — bounded pool, tasks queue when all n threads are busy; good for known/bounded workloads.
- `newCachedThreadPool()` — grows on demand, reuses idle threads (60s keep-alive); good for many short-lived tasks, but unbounded growth is a real production risk.
- `newSingleThreadExecutor()` — one thread, sequential execution in submission order — a serialization guarantee you cannot get from a `newFixedThreadPool(1)` semantically-different-but-similar setup without more care.
- `newScheduledThreadPool(n)` — for delayed/periodic tasks.

**`execute()` vs `submit()`:** `execute(Runnable)` is fire-and-forget, returns `void`, and any exception goes to the thread's uncaught-exception handler (easy to lose silently). `submit(Runnable)` returns `Future<?>`; `submit(Callable<T>)` returns `Future<T>` — exceptions are captured inside the `Future` and only surface when you call `get()` (wrapped in `ExecutionException`). This is a critical exam point: **submit() failures are silent until you call get()**.

**`Callable<T>` vs `Runnable`:** `Runnable.run()` returns nothing and cannot throw checked exceptions; `Callable<T>.call()` returns a `T` and can throw checked `Exception`. Use `Callable` whenever a background task needs to report a result or propagate a checked failure.

**`Future<T>`:** `get()` blocks until the result is ready (or throws `InterruptedException`/`ExecutionException`); `get(timeout, unit)` bounds the wait and throws `TimeoutException`; `isDone()`, `isCancelled()`, and `cancel(mayInterruptIfRunning)` round out the handle.

**Shutdown discipline:** an `ExecutorService` you never shut down keeps its threads alive and prevents the JVM from exiting. `shutdown()` stops accepting new tasks but lets running ones finish; `shutdownNow()` attempts to interrupt running tasks; `awaitTermination(timeout, unit)` blocks until termination or timeout. The idiomatic pattern wraps submission/`get()` in try/catch and calls `shutdown()` in `finally`.

**`invokeAll()`** submits a batch and blocks until *all* complete, returning a `List<Future<T>>`. **`invokeAny()`** submits a batch, returns the first successful result, and cancels the rest — useful for "ask several sources, take whichever answers first."

**`ScheduledExecutorService`:** `schedule()` (run once after a delay), `scheduleAtFixedRate()` (period measured from start-to-start — can overlap if a task runs long), `scheduleWithFixedDelay()` (period measured from end-of-one-to-start-of-next — never overlaps).

**`CompletableFuture`** (Java 8): a composable, non-blocking alternative to `Future` — `supplyAsync`/`runAsync` kick off work, `thenApply`/`thenAccept`/`thenApplyAsync` chain transformations, `thenCombine` merges two independent futures, `exceptionally`/`whenComplete` handle errors without blocking. This is Java's rough analogue to Python's `asyncio` futures / JS Promises, except it still runs on real OS/pool threads underneath rather than an event loop.

**Concurrent collections:** `ConcurrentHashMap` (segmented locking, safe concurrent reads/writes, much better throughput than `Collections.synchronizedMap`), `CopyOnWriteArrayList` (lock-free reads, every write copies the whole backing array — good only for read-heavy/write-rare lists), `BlockingQueue` implementations (`ArrayBlockingQueue`/`LinkedBlockingQueue`) with `put()`/`take()` blocking on full/empty — the standard building block for producer-consumer pipelines.

### Code — `day3/concurency/CallableDemo.java`

```java
1:	package com.acme.demo.day3.concurency;
3:	import java.util.concurrent.*;
5:	public class CallableDemo {
7:	    public static void main(String[] args)
8:	            throws Exception {
10:	        ExecutorService service =
11:	                Executors.newSingleThreadExecutor();
13:	        Callable<Integer> task = () -> {
15:	            return 100;
16:	        };
18:	        Future<Integer> future =
19:	                service.submit(task);
21:	        System.out.println(future.get());
23:	        service.shutdown();
24:	    }
25:	}
```

- **Line 7** — `throws Exception` on `main` is a scratch-demo shortcut avoiding individual `InterruptedException`/`ExecutionException` catches — production code would handle each distinctly.
- **Lines 13–16** — a `Callable<Integer>` lambda; since `Callable`'s abstract method is `T call() throws Exception`, `return 100;` becomes its implementation — unlike `Runnable`, this compiles specifically because `Callable` declares a return type.
- **Line 21** — `future.get()` blocks the *main* thread until the callable finishes.
- **Line 23** — `shutdown()` is essential; without it, the executor's worker thread stays alive as non-daemon and the JVM hangs after `main()` returns.

### Code — `day3/concurency/ConcurrencyDemo.java`

```java
1:	package com.acme.demo.day3.concurency;
3:	import java.util.ArrayList;
4:	import java.util.List;
5:	import java.util.concurrent.Callable;
6:	import java.util.concurrent.ExecutorService;
7:	import java.util.concurrent.Executors;
8:	import java.util.concurrent.Future;
9:	import java.util.concurrent.TimeUnit;
11:	public class ConcurrencyDemo {
13:		public static void main(String[] args) throws Exception {
15:			Callable<Integer> sumTask = () -> {
16:				int sum = 0;
17:				for (int i = 1; i <= 10; i++)
18:					sum += i;
19:				System.out.println(Thread.currentThread().getName() + " computed: " + sum);
20:				return sum;
21:			};
23:			// Fixed pool with 3 threads
24:			ExecutorService pool = Executors.newFixedThreadPool(3);
26:			// Submit multiple tasks, collect futures
27:			List<Future<Integer>> futures = new ArrayList<>();
29:			for (int i = 0; i < 5; i++) {
30:				futures.add(pool.submit(sumTask));
31:			}
33:			// Collect all results
34:			int grandTotal = 0;
35:			for (Future<Integer> f : futures) {
36:				grandTotal += f.get(); // blocks per future
37:			}
39:			System.out.println("Grand total: " + grandTotal); // 55 x 5 = 275
41:			// invokeAll -- submit all and get all results at once
42:			List<Future<Integer>> all = pool.invokeAll(List.of(sumTask, sumTask, sumTask));
43://		System.out.println(all);
44:			// invokeAny -- return first successful result, cancel others
45:			Integer first = pool.invokeAny(List.of(sumTask, sumTask, sumTask));
46:			System.out.println("First result: " + first);
48:			pool.shutdown();
49:			pool.awaitTermination(10, TimeUnit.SECONDS);
50:		}
51:	}
```

- **Lines 15–24** — `sumTask` is a reusable `Callable<Integer>` (sums 1..10 = 55) referencing no external mutable state, so it's safe to submit concurrently; a fixed pool of 3 threads runs at most 3 of the 5 submitted tasks at once, queueing the rest.
- **Lines 34–37** — iterates futures *in submission order*, blocking on `futures.get(0)` first even if a later task finishes sooner — `Future.get()` is per-future blocking, not "wait for whichever finishes first."
- **Lines 42, 45** — `invokeAll(...)` blocks until every batch task is done, returning `List<Future<T>>` all already completed; `invokeAny(...)` blocks until the first succeeds, returns that raw value directly (not wrapped in `Future`), and best-effort cancels the rest.
- **Lines 48–49** — the "clean shutdown" pattern: `shutdown()` stops new submissions, `awaitTermination(10, SECONDS)` blocks up to 10s for in-flight tasks.

### Code — `day3/concurency/ConcurrencyDemo2.java`

```java
1:	package com.acme.demo.day3.concurency;
3:	import java.util.concurrent.ExecutorService;
4:	import java.util.concurrent.Executors;
6:	public class ConcurrencyDemo2 {
8:	    public static void main(String[] args) {
10:	        ExecutorService service =
11:	                Executors.newFixedThreadPool(3);
13:	        for (int i = 1; i <= 5; i++) {
15:	            int taskId = i;
17:	            service.execute(() -> {
19:	                System.out.println(
20:	                        "Task " + taskId
21:	                                + " : "
22:	                                + Thread.currentThread().getName()
23:	                );
24:	            });
25:	        }
27:	        service.shutdown();
28:	    }
29:	}
```

- **Line 15** — `int taskId = i;` copies the loop variable into a new effectively-final local per iteration — required because the lambda captures `taskId` by value and Java lambdas can only capture effectively-final variables; capturing the mutating `i` directly would not compile. A common gotcha for developers used to Python/JS closures, which capture by reference.
- **Line 17** — `execute(Runnable)` (not `submit`) is fire-and-forget: no `Future` is returned, so an exception thrown here would propagate to the pool thread's default handler, invisible to the caller.
- **Line 27** — `shutdown()` only, no `awaitTermination()` — `main()` can return before all tasks finish printing.

---

## 25. IO Streams

**What it teaches:** Java's I/O centers on **streams** — sequences of data flowing in or out. `java.io` is the classic API (byte streams: `InputStream`/`OutputStream` for binary; character streams: `Reader`/`Writer` for text, which additionally handle character encoding). `java.nio.file` (Java 7+) is the modern replacement with `Path`/`Paths`/`Files`, offering better error messages and atomic operations; prefer it for new code.

**Byte vs character streams:** every concrete I/O class descends from one of the four abstract bases (`InputStream`, `OutputStream`, `Reader`, `Writer`). Use byte streams for binary data (images, serialized objects); character streams for text, since they translate bytes ↔ characters using an encoding.

**Buffered streams are mandatory for real code:** reading/writing one byte or character at a time (`FileReader.read()`) triggers a system call (or near it) per character — extremely slow. Wrapping in `BufferedReader`/`BufferedWriter` batches I/O into larger chunks and adds `readLine()` (returns `null` at EOF — the loop-termination idiom to memorize) and `newLine()` (platform-independent line separator).

**`PrintWriter`** adds `print`/`println`/`printf` on top of any `Writer` — the layering here is the **Decorator pattern**: `new PrintWriter(new BufferedWriter(new FileWriter(path)))` reads inside-out — `FileWriter` is the actual sink, `BufferedWriter` adds batching, `PrintWriter` adds formatting convenience. Recognizing this nesting pattern (and being able to construct/explain it) is a recurring exam theme.

**`DataInputStream`/`DataOutputStream`** read/write typed Java primitives in binary form (`writeInt`, `writeUTF`, `writeDouble`) — you must read back in exactly the same order you wrote, since the format carries no field names or self-description.

**Serialization:** a class must implement the `Serializable` marker interface (no methods) to be written via `ObjectOutputStream.writeObject()`/read via `ObjectInputStream.readObject()`. `transient` fields are skipped (use for secrets, recomputable fields, or non-serializable resources like DB connections/threads). `serialVersionUID` guards against deserializing a byte stream produced by an incompatible class version — mismatch throws `InvalidClassException`.

**Modern file API (`java.nio.file`):** `Path`/`Paths.get()` (or `Path.of()`, Java 11+) represent paths without touching the filesystem; `Files` provides static methods for nearly everything — `readAllLines`, `write`, `readString`/`writeString` (Java 11+, whole file as one `String`), `lines()` (lazy `Stream<String>`, good for large files — comparable to Python's line-by-line file iteration but explicitly a `Stream` you must close via try-with-resources since it holds an open file handle), existence/type checks, `copy`/`move`/`delete`, and directory traversal (`list`, `walk` recursive, `find` with a predicate).

**Legacy `File` class:** still seen in old code (`exists()`, `mkdirs()`, `listFiles()`, etc.) — courseware explicitly says prefer `java.nio.file` for anything new.

**Always use try-with-resources** for any stream — they hold OS file handles that must be released deterministically; this is the direct analogue of Python's `with open(...) as f:` context manager, except in Java it's built on the `AutoCloseable` interface and any class implementing it (including custom ones) gets this deterministic-cleanup guarantee.

### Code and data files

The three demo files below read/write the plain-text fixture files that sit alongside the source tree: `sample.txt` (`"This is sample text from sample file. "`, read by `IoDemo` and `BufferedDemo`), `sample2.txt` (written by `BufferedWriterDemo`, currently containing `"Sonu"` from a prior run), and `demo.txt` (`"Hello Java 13"`, written/read by `Java13Features.java` in Module 29 below via `Files.writeString`/`readString`).

### Code — `day3/iosdemo/IoDemo.java`

```java
1:	package com.acme.demo.day3.iosdemo;
3:	import java.io.FileReader;
5:	public class IoDemo {
7:		public static void main(String[] args) {
8:			System.out.println("Start");
10:		try {
11:				FileReader reader = new FileReader("sample.txt");
12:				int ch;
14:			while ((ch = reader.read()) != -1) {
16:				System.out.print((char) ch);
17:				}
19:			reader.close();
21:		} catch (Exception e) {
22:				e.printStackTrace();
23:			}
24:			System.out.println("End");
26:	}
27:	}
```

- **Line 11** — `"sample.txt"` is relative to the JVM's working directory at launch, *not* the source file's location — a frequent source of `FileNotFoundException` confusion.
- **Line 12** — `int ch`, not `char ch`: `Reader.read()` returns an `int` so it can represent both a valid character and the sentinel `-1` for end-of-stream — a `char` cannot hold `-1`.
- **Line 19** — `reader.close()` is inside the `try` block, *before* the catch (not try-with-resources) — if `read()` throws mid-loop, `close()` is skipped and the handle leaks. This file demonstrates the *old*, less-safe idiom the courseware says to avoid in favor of `try (FileReader r = ...)`.

### Code — `day3/iosdemo/BufferedDemo.java`

```java
1:	package com.acme.demo.day3.iosdemo;
3:	import java.io.BufferedReader;
4://import java.io.
5:	import java.io.FileReader;
7:	public class BufferedDemo {
9:		public static void main(String[] args) {
11:		try {
12:				BufferedReader br = new BufferedReader(new FileReader("sample.txt"));
14:			String line;
16:			while ((line = br.readLine()) != null) {
18:				System.out.println(line);
19:				}
21:			br.close();
23:		} catch (Exception e) {
24:				e.printStackTrace();
25:			}
26:	}
27:	}
```

- **Line 12** — the Decorator pattern in action: `new BufferedReader(new FileReader(...))` — `BufferedReader` wraps the raw `FileReader` to add buffering *and* `readLine()`, which plain `FileReader` lacks.
- **Line 16** — `br.readLine()` returns a full line (without the terminator) or `null` at EOF — the "right way" to read text, contrasting `IoDemo`'s character-by-character approach above.
- **Line 21** — again closes manually rather than via try-with-resources — worth mentally rewriting as `try (BufferedReader br = ...)`, the pattern expected in an assessment answer.

### Code — `day3/iosdemo/BufferedWriterDemo.java`

```java
1:	package com.acme.demo.day3.iosdemo;
3:	import java.io.BufferedWriter;
4:	import java.io.FileWriter;
6:	public class BufferedWriterDemo {
8:		public static void main(String[] args) {
10:		String file = "sample2.txt";
12:		try {
14:			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
16:			bw.write("Sonu");
18:			bw.close();
20:		} catch (Exception e) {
21:				e.printStackTrace();
22:			}
23:	}
24:	}
```

- **Line 14** — `new FileWriter(file)` with no second `true` argument means overwrite/truncate mode (not append) — every run replaces `sample2.txt`'s contents entirely.
- **Line 18** — `bw.close()` implicitly flushes any buffered content before releasing the handle — *why* the write lands on disk despite no explicit `flush()` call; forgetting to close (e.g. an exception between `write()` and `close()`) is a classic way to lose data — another argument for try-with-resources.

---

## 26. Collections and Generics

**What it teaches:** Arrays are fixed-size; the Java Collections Framework provides growable, well-tested structures: `List` (ordered, duplicates allowed — `ArrayList`, `LinkedList`), `Set` (no duplicates — `HashSet`, `LinkedHashSet`, `TreeSet`), `Queue`/`Deque` (`PriorityQueue`, `ArrayDeque`), and `Map` (key-value, not technically a `Collection` but part of the framework — `HashMap`, `LinkedHashMap`, `TreeMap`).

**Generics:** before Java 5, collections held raw `Object` — you could add a `String` to a collection meant for `Employee`, and it would compile, only to blow up as a `ClassCastException` at runtime on retrieval. `List<Employee>` moves that error to compile time. `<T>` is a *type parameter*. Generic classes (`class Pair<A,B>`) and generic methods (`static <T> void printAll(List<T> list)`) both use this. **Bounded type parameters** (`<T extends Number>`) restrict what types are legal, letting you call methods declared on the bound (e.g., `Number::doubleValue`) without an unsafe cast. **Wildcards**: `List<?>` (unknown type, read-only-ish use), `List<? extends T>` (upper-bounded — "producer," safe to *read* `T` or a subtype from it, per the PECS mnemonic — Producer Extends), `List<? super T>` (lower-bounded — "consumer," safe to *write* a `T` into it, Consumer Super).

**`ArrayList` vs `LinkedList`:** `ArrayList` is backed by a dynamic array — O(1) random access via `get(i)`, but inserting/removing mid-list shifts elements (O(n)). `LinkedList` is a doubly-linked list — O(1) insert/remove once you're at a position, but O(n) random access. In practice `ArrayList` wins for most workloads; `LinkedList` only pays off for frequent insert/remove at both ends (and even then `ArrayDeque` usually beats it).

**`Set` implementations** all require correct `equals()`/`hashCode()` on the element type to behave correctly for deduplication and lookup. `HashSet` — no order, O(1) average operations, backed by a `HashMap` internally. `LinkedHashSet` — same but preserves insertion order. `TreeSet` — sorted (natural order via `Comparable`, or an explicit `Comparator`); the equivalent for `Map` is `TreeMap`.

**`Map` implementations** parallel the `Set` family: `HashMap` (workhorse, O(1) average, no order), `LinkedHashMap` (insertion order), `TreeMap` (sorted by key). Useful non-trivial `Map` methods: `getOrDefault`, `putIfAbsent`, `computeIfAbsent` (lazily initialize a value, e.g., grouping into `List`s), `merge`.

**`Queue`/`Deque`:** `PriorityQueue` dequeues in priority order (natural or `Comparator`), not FIFO — a common trap if you expect insertion order. `ArrayDeque` is a fast double-ended queue usable as either a stack (`push`/`pop`, LIFO) or queue (`offer`/`poll`, FIFO) — preferred over the legacy `Stack` class and often over `LinkedList` for these roles.

**`Collections` utility class:** static helpers — `sort`, `reverse`, `shuffle`, `min`/`max`, `frequency`, `unmodifiableList` (a read-only *view* — mutating attempts throw `UnsupportedOperationException`), `synchronizedList` (adds coarse-grained locking, largely superseded by `java.util.concurrent` collections for real concurrent use).

**Java 9+ factory methods:** `List.of(...)`, `Set.of(...)`, `Map.of(...)` produce genuinely immutable collections (not just unmodifiable *views*) — attempting to mutate throws immediately, and `null` elements/keys/values are disallowed entirely (throws `NullPointerException` at creation, not later).

**Cross-language note:** Java's `List<T>`/`ArrayList<T>` map roughly to Python's `list`, `Set<T>`/`HashSet<T>` to Python's `set`, `Map<K,V>`/`HashMap<K,V>` to Python's `dict` — but Java's static typing means the compiler enforces element types, and Java's collections are far more explicit about ordering guarantees, mutability, and thread-safety as *distinct implementation choices* rather than one flexible built-in type doing everything.

### Code — `day3/collection/CollctionWithGenerics.java`

```java
1:	package com.acme.demo.day3.collection;
3:	import java.util.ArrayList;
5:	public class CollctionWithGenerics { 
7:		public static void main(String[] args) {
9://		ArrayList<String> friends = new ArrayList<String>();
10:		ArrayList<String> friends = new ArrayList<>();
12:		System.out.println(friends.size());
13:			System.out.println(friends);
14:			friends.add("Sonu");
15:			friends.add("Monu");
16:			friends.add("Tonu");
17:			System.out.println(friends.size());
18:			System.out.println(friends);
19://		friends.add(10.25);
20://		friends.add(false);
21://		friends.add(null);
22:		System.out.println(friends.size());
23:			System.out.println(friends);
25:	}
27:	}
```

- **Line 9 vs 10** — pre-diamond-operator style `new ArrayList<String>()` versus the Java 7+ diamond `new ArrayList<>()`, which infers the type argument from the declared variable type — both compile identically, the diamond is the modern idiom.
- **Lines 19–21** (commented) — `friends.add(10.25)`/`add(false)` would each be a **compile error** — the entire point of the demo, showing generics catch what pre-generics code would only catch at runtime via `ClassCastException`. `friends.add(null)` would actually compile and run fine — `ArrayList` permits `null` (unlike Java 9+ `List.of()`).

### Code — `day3/collection/CollectionDemo.java`

```java
1:	package com.acme.demo.day3.collection;
3:	import java.util.ArrayList;
5:	public class CollectionDemo {
7:		public static void main(String[] args) {
9://		String[] str = { "Sonu", "Monu", "Tonu" };
11:		ArrayList friends = new ArrayList();
12:			System.out.println(friends.size());
13:			System.out.println(friends);
14:			friends.add("Sonu");
15:			friends.add("Monu");
16:			friends.add("Tonu");
17:			System.out.println(friends.size());
18:			System.out.println(friends);
19:			friends.remove(2);
20:			System.out.println(friends.size());
21:			System.out.println(friends);
22:			friends.remove("Zonu");
23:			System.out.println(friends.size());
24:			System.out.println(friends);
26:	}
28:	}
```

- **Line 11** — `ArrayList friends = new ArrayList();` — a **raw type**, deliberately contrasting with the previous file: with no type parameter, every element is treated as `Object`, losing compile-time checking entirely — legal for backward compatibility, but exactly what modern style guides forbid.
- **Line 19** — `friends.remove(2)` is the **overload trap**: `List.remove(int index)` removes by *position*, so this removes `"Tonu"` (index 2), not a value equal to `2`. Even for `Integer` lists, `remove(2)` still calls the index overload — you'd need `remove(Integer.valueOf(2))` to remove by value. A classic assessment gotcha.
- **Line 22** — `friends.remove("Zonu")` calls the *other* overload, `remove(Object)`, because the argument is a `String` — since `"Zonu"` was never added, this is a safe no-op (`false`, no exception), unlike an out-of-range index which throws.

### Code — `day3/collection/CollectionIteration.java`

```java
1:	package com.acme.demo.day3.collection;
3:	import java.util.ArrayList;
4:	import java.util.Iterator;
5:	import java.util.List;
7:	public class CollectionIteration {
9:		public static void main(String[] args) {
11:		List<String> friends = new ArrayList<>();
13:		friends.add("Sonu");
14:			friends.add("Monu");
15:			friends.add("Tonu");
17://		iterate  - for loop, for each loop, iterator, forEach 
19:		System.out.println("List of the friends using forEach method ");
20://		friends.forEach((friend) -> {
21://			System.out.println(friend);
22://		});
24:		friends.forEach(friend -> System.out.println(friend));
25://		friends.forEach(null);
27:		System.out.println("List of the friends using iterator method ");
29:		Iterator<String> it = friends.iterator();
31:		while (it.hasNext())
32:				System.out.println(it.next());
34:	}
36:	}
```

- **Line 11** — declared as the `List<String>` *interface* type rather than `ArrayList<String>` — code against the interface so the concrete implementation can be swapped without touching call sites.
- **Line 17** (comment) — enumerates the four idiomatic ways to iterate: indexed `for`, `for-each`, explicit `Iterator`, and `forEach(Consumer)` — `Iterator` uniquely supports safe removal *during* iteration via `it.remove()`, which `for-each` does not (throws `ConcurrentModificationException` instead).
- **Line 25** (commented) — `friends.forEach(null)` would compile but throw `NullPointerException` at runtime the moment `forEach` invokes it — generic parameters are still ordinary references that can be null.
- **Lines 31–32** — the classic `while (it.hasNext()) { ... it.next(); }` idiom — calling `next()` past the end throws `NoSuchElementException`, which is why the `hasNext()` guard is mandatory.

### Code — `day3/collection/CollectionMethods.java`

```java
1:	package com.acme.demo.day3.collection;
3:	import java.util.ArrayList;
4:	import java.util.LinkedList;
5:	import java.util.List;
7:	public class CollectionMethods {
9:		public static void main(String[] args) {
11:		ArrayList<String> friends = new ArrayList<>();
13:		List<String> friends2 = new ArrayList<>();
14:			int num = 10;
16://		friends =  new LinkedList<String>();
17:		friends2 = new LinkedList<>();
19:		friends.add("Sonu");
20:			friends.add("Monu");
21:			friends.add("Tonu");
23:		System.out.println(friends);
25:		@SuppressWarnings("unchecked")
26:			ArrayList<String> friends3 = (ArrayList<String>) friends.clone();
27://		ArrayList<String> friends3 =  new ArrayList<>(friends);
29:		System.out.println(friends3);
31:		friends3.add("Ponu");
33:		System.out.println(friends);
34:			System.out.println(friends2);
35:			System.out.println(friends3);
36://		friends.
38://		 friends2.clone(); // CE 
39:	}
40:	}
```

- **Lines 11 vs 13, 16–17** — `friends` is declared as concrete `ArrayList<String>`; `friends2` as interface type `List<String>` — because `friends2`'s *static* type is `List`, it can be reassigned to any implementation including `LinkedList` (line 17), while `friends`'s commented `= new LinkedList<String>();` (line 16) would be a **compile error** — a direct illustration of "program to the interface."
- **Lines 25–26** — `friends.clone()` performs a **shallow copy** (new `ArrayList`, same object references inside) — requires `@SuppressWarnings("unchecked")` since the cast is unchecked under type erasure. Discouraged in modern Java in favor of the copy-constructor idiom on line 27, `new ArrayList<>(friends)`.
- **Line 38** (comment) — `friends2.clone()` would be a compile error because `clone()` isn't declared on the `List` interface (only re-exposed `public` by concrete classes) — `friends2`'s static type gates visibility regardless of its runtime `LinkedList` type.

**Remaining collection files (briefly):**
- **`CollectionTest.java`** (19 lines) is a minimal, single-purpose demo: builds `List<Integer> nums = {1,2,3}`, calls `nums.remove(1)`, and prints the result. It reinforces the same `remove(int)`-vs-`remove(Object)` overload trap as `CollectionDemo.java` above, but with an `Integer` list — `remove(1)` here removes by *index* (dropping `2`, the element at index 1), producing `[1, 3]`; to remove the *value* `1` you would need `nums.remove(Integer.valueOf(1))`.
- **`MapDemo.java`** and **`SetDemo.java`** are both empty stub classes (just a package declaration and an empty class body — 5 lines each, no `main` method or logic). They exist as placeholders in the course repo but contain no runnable example; the `Map`/`Set` behavior described in the courseware summary above (`HashMap`, `TreeMap`, `HashSet`, `TreeSet`, etc.) has no corresponding hands-on code file in this repo — treat the courseware's inline snippets as the reference material for those two structures.

---

## 27. Garbage Collection

**What it teaches:** every object goes through four stages — created (allocated on the heap, constructor runs) → in use (reachable from at least one live reference) → eligible (no live references remain) → collected (memory reclaimed). The developer's only real job is making sure objects become unreachable when done with them; the GC handles the rest automatically — a sharp contrast with C/C++ manual memory management, and also meaningfully different from Python's reference-counting model (see below).

**GC roots:** the GC traces reachability starting from a fixed set of roots — local variables/parameters in active stack frames, static fields, JNI references, and live thread objects themselves. Anything transitively reachable from a root is kept; everything else is eligible for collection, including **cycles with no external reference** (e.g., two objects referencing each other but reachable from nothing else) — Java's tracing GC handles cycles correctly, unlike naive reference-counting schemes.

**Cross-language note:** CPython's primary GC mechanism is reference counting (an object is freed the instant its refcount hits zero) plus a supplementary cycle collector bolted on specifically because refcounting alone can't reclaim cycles. Java has never used reference counting — it's a pure tracing collector from GC roots, so cycles were never a special case requiring extra machinery. The practical corollary: Python's refcounting gives immediate, deterministic destruction (which is why `__del__` timing is more predictable), whereas Java's tracing GC runs on its own schedule — you cannot know precisely *when* an eligible object will actually be collected, only that it eventually will be if the JVM needs the memory.

**Heap regions and GC cycle:** the heap splits into **Young Generation** (Eden + two Survivor spaces) and **Old Generation**. New objects land in Eden; when Eden fills, a **Minor GC** runs — cheap and frequent, since most objects die young (created for a single method call, then abandoned). Survivors get copied between S0/S1 across cycles and eventually promoted to Old Gen. **Major/Full GC** collects Old Gen — slower, less frequent, and the source of the "Stop-The-World" pauses noticeable in production.

**Reference strength hierarchy** (strongest to weakest), each interacting differently with the GC:
- **Strong** (the default, an ordinary variable) — never collected while referenced.
- **Soft** (`SoftReference<T>`) — collected only under memory pressure; good for memory-sensitive caches.
- **Weak** (`WeakReference<T>`) — collected at the *next* GC cycle regardless of memory pressure, as soon as no strong references remain; underlies `WeakHashMap`.
- **Phantom** (`PhantomReference<T>`) — `get()` *always* returns `null`; used purely to detect "has this object actually finished being collected yet" via a `ReferenceQueue`, replacing the deprecated `finalize()` mechanism for post-GC cleanup.

**GC algorithms:** Serial (single-threaded, small apps), Parallel (multi-threaded, Java 8 default, throughput-oriented), **G1** (default since Java 9 — region-based, collects the garbage-heaviest regions first, tunable pause target via `-XX:MaxGCPauseMillis`), **ZGC** (Java 15+, concurrent, sub-millisecond pauses regardless of heap size), Shenandoah (similar low-pause goals). As a developer you rarely tune these directly but should recognize the flags in production configs.

**Memory leaks in Java are real** despite GC — they happen when objects remain *reachable* but are never used again (GC can only reclaim *unreachable* objects). Classic causes: (1) static collections that only ever grow (a `static Map` is itself a GC root, so anything in it stays alive forever unless explicitly removed); (2) listeners registered but never unregistered; (3) a long-lived object holding a reference to a short-lived one in an instance field, extending the short-lived object's effective lifetime to match the long-lived one's.

**`System.gc()`** is only a *hint* — the JVM is free to ignore it, and calling it in production risks triggering an expensive Full GC at an inopportune moment while giving false confidence memory was actually freed. Avoid it outside of testing/benchmarking.

**`finalize()`** was deprecated in Java 9 and fully **removed in Java 18** — do not use it (it may never run, may run too late, and can even "resurrect" an object by re-establishing a reference to it from within the finalizer). The modern replacement is `AutoCloseable` + try-with-resources for deterministic, guaranteed cleanup.

### Code — `day3/garbage/GcDemo.java`

```java
1:	package com.acme.demo.day3.garbage;
3:	public class GcDemo {
5:		@Override
6:		protected void finalize() {
7:			System.out.println("GC called");
8:		}
10:		public static void main(String[] args) {
12:		GcDemo obj = new GcDemo();
13:			System.out.println(obj.toString());
15:		obj = null;
17:		System.gc();
18:	}
19:	}
```

- **Lines 5–8** — overrides `Object.finalize()`, which was deprecated for removal and *removed entirely* by Java 18 (this file won't even compile on JDK 18+ without legacy flags) — shown purely as a teaching artifact of how it used to work.
- **Line 15** — `obj = null` removes the only strong reference, making the object eligible for collection.
- **Line 17** — `System.gc()` is only a *suggestion* — not guaranteed to run immediately, or at all. In practice most JVMs eagerly collect here so "GC called" usually prints, but relying on that behavior is exactly the anti-pattern the courseware warns against.

### Code — `day3/garbage/ReferenceDemo.java`

```java
1:	package com.acme.demo.day3.garbage;
3:	import java.lang.ref.PhantomReference;
4:	import java.lang.ref.ReferenceQueue;
5:	import java.lang.ref.SoftReference;
6:	import java.lang.ref.WeakReference;
8:	public class ReferenceDemo {
10:		public static void main(String[] args) throws InterruptedException {
12:		// Strong Reference
13:			String name = new String("Hello");
14:			System.out.println("Strong ref: " + name);
15:			name = null; // now eligible for GC
16:			System.gc();
17:			System.out.println("Strong ref set to null -> object may be collected\n");
19:		// Soft Reference
20:			SoftReference<byte[]> cache = new SoftReference<>(new byte[1024]);
22:		byte[] data = cache.get(); // returns object if still alive
23:			if (data != null) {
24:				System.out.println("Soft ref - Cache hit: " + data.length + " bytes");
25:			} else {
26:				System.out.println("Soft ref - Cache miss: GC cleared it (low memory)");
27:			}
28:			System.out.println();
30:		// Weak Reference
31:			String data2 = new String("temporary");
32:			WeakReference<String> weak = new WeakReference<>(data2);
34:		System.out.println("Weak ref - Before GC: " + weak.get());
36:		data2 = null; // remove strong reference
37:			System.gc();
38:			Thread.sleep(100); // give GC a moment to run
40:		System.out.println("Weak ref - After  GC: " + weak.get() + "\n");
42:		// Phantom Reference
43:			// PhantomReference requires a ReferenceQueue.
44:			// get() ALWAYS returns null - the object is already finalized.
45:			// We detect collection by polling the queue.
47:		ReferenceQueue<Object> queue = new ReferenceQueue<>();
49:		Object resource = new Object();
50:			PhantomReference<Object> phantom = new PhantomReference<>(resource, queue);
52:		System.out.println("Phantom ref - get() before GC: " + phantom.get()); // always null
54:		resource = null; // remove strong reference
55:			System.gc();
56:			Thread.sleep(100); // give GC time to enqueue the phantom ref
58:		// Poll the queue to detect that the object has been collected
59:			if (queue.poll() != null) {
60:				System.out.println("Phantom ref - object collected -> safe to run cleanup");
61:			} else {
62:				System.out.println("Phantom ref - object not yet collected");
63:			}
64:	}
65:	}
```

- **Line 13** — `new String("Hello")` deliberately forces a new heap-allocated object (bypassing the string constant pool a plain literal would use) — necessary since the pool holds a permanent strong reference to interned literals, which would defeat the "make it eligible for GC" demonstration.
- **Line 20** — `SoftReference<byte[]>` is cleared only under memory pressure — in a quick-running demo like this the reference almost always survives, so `cache.get()` (line 22) typically prints "Cache hit." This null-checking pattern is *mandatory* whenever working with `SoftReference`/`WeakReference`.
- **Line 36** — unlike `SoftReference`, a `WeakReference`'s referent is eligible for collection at the *very next* GC cycle with no memory-pressure requirement — the defining Soft/Weak difference; after the GC pass, `weak.get()` (line 40) typically returns `null`.
- **Lines 49–52** — `PhantomReference.get()` **always returns null**, even while the object is technically still alive — categorically different from Soft/Weak, not just "even weaker." It exists solely to let you know, via the `ReferenceQueue`, *when* an object has become phantom-reachable, via `queue.poll()` (line 59) — the only way to observe collection through a `PhantomReference`.

---

## 29. Java 13 and 14 Features

**What it teaches:** two non-LTS releases (Sept 2019, Mar 2020) that delivered high day-to-day-impact syntax improvements: text blocks (preview in 13, standardized in 15), switch expressions (preview in 12, standardized in 14), and helpful NullPointerExceptions (Java 14).

**Text blocks (`"""..."""`):** eliminate the old pattern of `"line1\n" + "line2\n"` concatenation with manually escaped quotes. The opening `"""` **must** be followed by a newline (no content on the same line). The compiler computes the *common leading whitespace* across all content lines **and the closing `"""`**, then strips exactly that much from every line — so the position of the closing delimiter directly controls how much indentation survives into the resulting string; putting it at column 0 preserves all leading spaces, while aligning it with the content strips down to that column. A text block ends with a trailing newline if the closing `"""` sits on its own line; put it right after the last character of content to suppress that trailing newline. Three companion `String` methods: `indent(n)` (add indentation), `stripIndent()` (apply the same common-whitespace-stripping algorithm programmatically to any runtime string), `translateEscapes()` (interpret literal `\n`/`\t` sequences as actual control characters). `String.formatted(...)` (Java 15) is the instance-method mirror of `String.format(...)`, and pairs naturally with text blocks for templated JSON/SQL/HTML.

**Switch expressions:** the old `switch` was purely a *statement* — no return value, `break` required per case to prevent fall-through (a notorious bug source), and unusable inline. The new arrow form (`case X -> value;`) is an *expression* — it evaluates to a value, has no fall-through by default, supports comma-separated multiple labels per case (`case "HR", "Finance" -> 10;`), and (for a multi-statement case) uses `yield` inside a block to produce the value (not `return` — `yield` is switch-expression-specific and is not a general control-flow keyword usable elsewhere, e.g. not inside a lambda). The old colon syntax (`case X:`) still works and can also `yield` a value from within a switch expression, but the arrow form is preferred going forward. Critically, a switch **expression** must be exhaustive: `String`/`int` selectors require `default`; an `enum` selector can omit `default` if every constant is covered — and if a new constant is added later without updating the switch, that's now a *compile error*, not a silent runtime gap — a genuinely useful safety net worth calling out on an assessment.

**Helpful NullPointerExceptions (Java 14):** previously an NPE's message was just the bare exception type with no detail about *what* was null. From Java 14 (opt-in via `-XX:+ShowCodeDetailsInExceptionMessages`) and always-on from Java 15, the JVM pinpoints exactly which variable or which link in a method-chain expression (`company.getHeadOffice().getAddress().getCity()`) was the null culprit — no code changes required, purely a diagnostics improvement.

### Code — `day3/features/Java13Features.java`

```java
1:	package com.acme.demo.day3.features;
3:	public class Java13Features {
5:	    public static void main(String[] args) {
7:	        // Text Blocks
9:	        String html = """
10:	                <html>
11:	                    <body>
12:	                        <h1>Java 13</h1>
13:	                    </body>
14:	                </html>
15:	                """;
17:	        System.out.println(html);
19:	        // switch expression
21:	        String day = "MONDAY";
23:	        String type = switch (day) {
25:	        case "SATURDAY", "SUNDAY" -> "Weekend";
27:	        default -> "Weekday";
28:	        };
30:	        System.out.println(type);
32:	        // yield in switch
34:	        int num = 2;
36:	        String result = switch (num) {
38:	        case 1:
39:	            yield "One";
41:	        case 2:
42:	            yield "Two";
44:	        default:
45:	            yield "Unknown";
46:	        };
48:	        System.out.println(result);
50:	        // String methods
52:	        String name = "   Java 13   ";
54:	        System.out.println(name.strip());
56:	        System.out.println("".isBlank());
58:	        System.out.println("Java\nPython".lines().count());
60:	        // File Read/Write (NIO improvements)
62:	        try {
64:	            java.nio.file.Path path = java.nio.file.Path.of("demo.txt");
66:	            java.nio.file.Files.writeString(path, "Hello Java 13");
68:	            String data = java.nio.file.Files.readString(path);
70:	            System.out.println(data);
72:	        } catch (Exception e) {
74:	            e.printStackTrace();
75:	        }
76:	    }
77:	}
```

- **Lines 9–15** — a text-block literal for HTML; the closing `"""` sits at the same indentation column as the content lines, so the compiler strips that common leading whitespace — output prints flush at column 0, not indented to match the source's visual nesting.
- **Lines 23–36** — line 23 is a switch *expression* (trailing semicolon required, unlike statements); line 25's multi-label case doesn't match `"MONDAY"` so `default` wins. Line 36 repeats with **traditional colon syntax** + `yield` instead of arrow syntax — colon-style cases must explicitly `yield` (no implicit fall-through-avoidance).
- **Lines 54, 58, 64–68** — `.strip()` (Unicode-aware `.trim()`), `.lines()` (splits into a `Stream<String>`), and `Path.of(...)` + `Files.writeString`/`readString` (all Java 11+) write/read a whole file in one call each — the round trip is why line 70's output echoes what was just written.

### Code — `day3/features/Java14Features.java`

```java
1:	package com.acme.demo.day3.features;
3:	public class Java14Features {
5:	    public static void main(String[] args) {
7:	        // switch expression
9:	        int day = 6;
11:	        String result = switch (day) {
13:	        case 6, 7 -> "Weekend";
15:	        default -> "Weekday";
16:	        };
18:	        System.out.println(result);
20:	        // Record (Preview in Java 14)
22:	        Employee e = new Employee(101, "Sonu", 50000);
24:	        System.out.println(e.id());
26:	        System.out.println(e.name());
28:	        System.out.println(e.salary());
30:	        System.out.println(e);
32:	        // instanceof pattern matching
34:	        Object obj = "Java 14";
36:	        if (obj instanceof String s) {
38:	            System.out.println(s.toUpperCase());
39:	        }
41:	        // NullPointerException improvement
43:	        String str = null;
45:	        try {
47:	            System.out.println(str.length());
49:	        } catch (Exception ex) {
51:	            ex.printStackTrace();
52:	        }
54:	        // Helpful JVM info
56:	        System.out.println(Runtime.version());
57:	    }
58:	}
60:	// Record Example
62:	record Employee(int id, String name, double salary) {
63:	}
```

- **Lines 9–16** — the same arrow-switch mechanics as Module 29's example, applied to an `int` selector instead of `String`.
- **Lines 22–30, 62** — `record Employee(...)` at file scope (only *preview* when this course targeted Java 14, standard since 16, covered in depth in Module 30); `e.id()`/`e.name()`/`e.salary()` are compiler-generated **accessors** — note the naming convention `id()`, not `getId()`, a deliberate departure from JavaBean getters.
- **Line 36** — `if (obj instanceof String s)` — pattern-matching `instanceof` (preview in 14, standard in 16): check and cast-and-bind happen in one expression.
- **Line 43, 47** — deliberately triggers a `NullPointerException` to exercise Java 14's helpful-NPE feature: the message would specify `"str"` was null, rather than a bare, contextless exception.

---

## 30. Java 17 Features

*(No dedicated code file exists in the course repo for this module — confirmed via `find ... -iname "*17*"` returning no matches. The examples below are original, clearly-labeled illustrative examples written to match the courseware's content precisely, not sourced from the repo.)*

**What it teaches:** Java 17 (Sept 2021) is the current widely-adopted LTS, standardizing three structural features that had been maturing through preview status since Java 14–16: **records** (data-carrier classes with generated boilerplate), **sealed classes** (closed, explicitly-enumerated type hierarchies), and **pattern matching for `instanceof`** (check-and-bind in one step). Together, sealed classes + pattern matching in `switch` (finalized later, in Java 21) give you compiler-verified *exhaustive* dispatch over a fixed set of types — no `default` needed, and the compiler flags a missing case as a compile error rather than a silent runtime gap.

**Records recap (see Module 29 for the mechanics; Module 30 adds):** a compact constructor (`public EmployeeRecord { ... }`, no parameter list — runs *before* the automatic field assignment, ideal for validation/normalization of parameters *before* they're assigned to the generated `final` fields) lets you enforce invariants without restating every parameter. Records can have static fields, static factory methods, and instance methods beyond the accessors, but **cannot** declare additional instance fields beyond the record's components — they are strictly immutable value carriers. Records automatically get correct `equals()`/`hashCode()` based on *all* components, so they work correctly as `HashSet` elements or `HashMap` keys with no extra work — a real, common pain point with ordinary classes where developers forget to override these.

**Sealed classes:** `public sealed class Employee permits Manager, Developer, Contractor { ... }` — the `permits` clause is an exhaustive, compiler-enforced whitelist of direct subclasses; anything not listed simply won't compile as a subclass. Every permitted subclass must itself declare exactly one of `final` (closes the hierarchy further, no more subclassing), `sealed` (extendable, but only by *its own* `permits` list), or `non-sealed` (explicitly reopens the hierarchy to unrestricted extension by anyone) — one of these three modifiers is *mandatory* on a direct subclass of a sealed type, and forgetting it is a compile error. Interfaces can be sealed the same way.

**Pattern matching for `instanceof`:** `if (obj instanceof Manager m) { ... }` replaces the old two-step "check, then redundant cast" idiom. The bound variable (`m`) is only in scope where the match is statically guaranteed true — including the negation case: `if (!(obj instanceof Manager m)) { return; }` followed by code that uses `m` — the compiler tracks that if execution reaches past the early return, the `instanceof` must have succeeded, so `m` is legally in scope there too (flow-sensitive typing).

**Other Java 17 items worth knowing:** JDK internals (`sun.*`) are now strongly encapsulated by default — code depending on them can throw `InaccessibleObjectException`; `strictfp` is now the permanent default for all floating-point ops (the keyword is vestigial); a new `RandomGenerator` interface hierarchy (`RandomGenerator.of("Xoshiro256PlusPlus")`, `SplittableRandom`) modernizes random-number generation, particularly for parallel streams.

### Illustrative example (not from course repo) — Records, Sealed Classes, and Pattern Matching

```java
1:	package com.acme.demo.day3.illustrative;
3:	import java.util.List;
5:	public class Java17FeaturesIllustrative {
7:		// Sealed hierarchy -- only these three classes may extend Employee
8:		sealed interface Employee permits Manager, Developer, Contractor { }
10:		record Manager(int id, String name, double salary, int teamSize) implements Employee {
11:			public Manager {
12:				if (teamSize <= 0)
13:					throw new IllegalArgumentException("Team size must be positive: " + teamSize);
14:			}
15:		}
17:		record Developer(int id, String name, double salary, String techStack) implements Employee { }
19:		record Contractor(int id, String name, double dailyRate) implements Employee { }
21:		static String describe(Employee e) {
22:			return switch (e) {
23:				case Manager m    -> "Manager with team of " + m.teamSize();
24:				case Developer d  -> "Developer working on " + d.techStack();
25:				case Contractor c -> "Contractor at " + c.dailyRate() + "/day";
26:				// no default needed -- Employee is sealed, all permitted types covered
27:			};
28:		}
30:		static String employeeName(Object obj) {
31:			if (obj instanceof Manager m) {
32:				return m.name();
33:			} else if (obj instanceof Developer d && d.salary() > 80000) {
34:				return d.name() + " (senior)";
35:			}
36:			return "Unknown";
37:		}
39:		public static void main(String[] args) {
41:			List<Employee> team = List.of(
42:				new Manager(201, "Ponu", 110000, 8),
43:				new Developer(301, "Monu", 82000, "Java, Spring"),
44:				new Contractor(401, "Gonu", 2500)
45:			);
47:			for (Employee e : team) {
48:				System.out.println(describe(e) + "  |  name via instanceof: " + employeeName(e));
49:			}
50:		}
51:	}
```

- **Line 8** — `sealed interface Employee permits Manager, Developer, Contractor` declares a closed type hierarchy — `class Intern implements Employee` elsewhere would be a compile error.
- **Lines 10–15** — `Manager` is a `record` implementing the sealed interface; records satisfy the sealed hierarchy's implicit "must be final/sealed/non-sealed" requirement automatically since records are implicitly `final`. Its **compact constructor** (lines 11–14) validates before the (compiler-generated) field assignments happen.
- **Lines 22–27** — a **pattern-matching switch expression**: each `case` matches on runtime type and binds a typed variable in one step, no casts. Because the compiler knows `Employee`'s `permits` list exhaustively, **no `default` branch is required or even permitted to be missing** — if a fourth type were added later without updating this switch, the code fails to compile rather than silently misbehaving at runtime. This is the biggest practical payoff of combining sealed types with pattern-matching switch.
- **Lines 30–37** — plain `instanceof` pattern matching (not switch-based): line 33's `d.salary() > 80000` reads `d` safely since short-circuit `&&` guarantees the `instanceof` already succeeded. Unlike the sealed-switch above, this `instanceof` chain is *not* compiler-verified exhaustive — `Contractor` (line 44) matches neither branch and falls through to `"Unknown"`, illustrating why the switch-based approach is preferred once a hierarchy is sealed.

---

## 31. Java 21 Features

*(No dedicated code file exists in the course repo for this module — confirmed empty via the same `find` check above. The example below is original and clearly labeled.)*

**What it teaches:** Java 21 (Sept 2023) is the current recommended LTS and, per the courseware, "the most impactful Java release since Java 8" — it delivers Project Loom's virtual threads plus several finalized language features.

**Virtual threads (Project Loom, JEP 444, standard):** traditional ("platform") threads map 1:1 to OS threads, each costing roughly 1MB of stack — a JVM comfortably handles maybe 1,000–10,000 of them, which is why high-concurrency servers historically resorted to complex async/reactive code as a workaround. **Virtual threads are JVM-managed, not OS-managed** — millions can exist simultaneously, each starting in microseconds and using only a few KB. When a virtual thread blocks on I/O (a DB call, an HTTP request, `Thread.sleep`), the JVM automatically **unmounts** it from its underlying OS ("carrier") thread, freeing that OS thread for other work, then remounts it when the blocking operation completes — all while your code reads like ordinary synchronous blocking code, no callbacks or reactive chains required. Creation: `Thread.ofVirtual().start(task)`, `Thread.startVirtualThread(task)`, or — most commonly in application code — `Executors.newVirtualThreadPerTaskExecutor()`, which gives each submitted task its own virtual thread. **Crucial nuance:** virtual threads help *I/O-bound* work; they do **nothing** for CPU-bound computation — for that, stick with a fixed thread pool sized to `Runtime.getRuntime().availableProcessors()`. **Pinning** is the other major gotcha: a virtual thread cannot unmount (behaves like an expensive platform thread) while inside a `synchronized` block or during native-code calls — prefer `java.util.concurrent.locks.ReentrantLock` over `synchronized` in virtual-thread-heavy code specifically to avoid pinning during blocking operations like `sleep()` inside a critical section.

**Sequenced Collections (JEP 431, standard):** three new interfaces — `SequencedCollection` (`addFirst`/`addLast`, `getFirst`/`getLast`, `removeFirst`/`removeLast`, `reversed()` as a *view*, not a copy), `SequencedSet`, `SequencedMap` (`firstEntry`/`lastEntry`, `putFirst`/`putLast`, `pollFirstEntry`/`pollLastEntry`, `reversed()`). Before Java 21, getting the first/last entry of a `LinkedHashMap` required an awkward iterator dance; now it's a direct method call — `List` and `Deque` gain this interface too.

**Record Patterns (JEP 440, standard):** extends pattern matching to *destructure* a record's components directly, both in `instanceof` (`if (obj instanceof EmployeeRecord(int id, String name, double salary, String dept))`) and in `switch`, including **nested** destructuring through composed records (`EmployeeWithAddress(EmployeeRecord(var id, var name, var salary, var dept), Address(var city, var country))`) — you can reach arbitrarily deep into a record's structure in one pattern.

**Pattern matching in switch — standardized (JEP 441):** two additions beyond what Module 30 covered — `when` **guards** (`case Employee emp when emp.getSalary() > 100000 -> "Executive";`, adding a boolean condition onto a type pattern) and explicit **`null` handling** in switch (`case null -> "No employee provided";` — previously, passing `null` into any switch threw `NullPointerException` unconditionally; now you can match it like any other case).

**Structured Concurrency (JEP 453, preview in 21):** `StructuredTaskScope` treats a group of concurrently-forked subtasks as a single unit — `scope.fork(callable)` launches each, `scope.join()` waits for all, and (with `ShutdownOnFailure`) if *any* subtask fails, the rest are automatically cancelled and the failure surfaces via `scope.throwIfFailed()`. This directly addresses the classic problem of "manually tracking and cancelling a fan-out of async work when one branch fails," which raw `ExecutorService`/`Future` code handles only clumsily.

### Illustrative example (not from course repo) — Virtual Threads, Sequenced Collections, Record Patterns

```java
1:	package com.acme.demo.day3.illustrative;
3:	import java.util.*;
4:	import java.util.concurrent.*;
6:	public class Java21FeaturesIllustrative {
8:		record EmployeeRecord(int id, String name, double salary, String department) { }
10:		static double fetchSalaryFromDb(int employeeId) throws InterruptedException {
11:			Thread.sleep(100);   // simulate a blocking DB call
12:			return 50000 + (employeeId % 5) * 10000;
13:		}
15:		static String classify(Object obj) {
16:			return switch (obj) {
17:				case null -> "No data";
18:				case EmployeeRecord(var id, var name, var salary, var dept)
19:						when salary > 80000 -> name + " is senior in " + dept;
20:				case EmployeeRecord(var id, var name, var salary, var dept) -> name + " is staff in " + dept;
21:				default -> "Unrecognized";
22:			};
23:		}
25:		public static void main(String[] args) throws Exception {
27:			// Virtual threads: process 1000 "employees" concurrently
28:			long start = System.currentTimeMillis();
29:			List<EmployeeRecord> results = new ArrayList<>();
31:			try (ExecutorService vExec = Executors.newVirtualThreadPerTaskExecutor()) {
32:				List<Future<EmployeeRecord>> futures = new ArrayList<>();
33:				for (int i = 1; i <= 1000; i++) {
34:					int id = i;
35:					futures.add(vExec.submit(() -> {
36:						double salary = fetchSalaryFromDb(id);
37:						return new EmployeeRecord(id, "Emp-" + id, salary, "Dept-" + (id % 3));
38:					}));
39:				}
40:				for (Future<EmployeeRecord> f : futures) {
41:					results.add(f.get());
42:				}
43:			}
44:			System.out.println("Processed " + results.size() + " employees in "
45:					+ (System.currentTimeMillis() - start) + "ms using virtual threads");
47:			// Sequenced collections
48:			LinkedHashMap<Integer, EmployeeRecord> byId = new LinkedHashMap<>();
49:			for (EmployeeRecord e : results) byId.put(e.id(), e);
51:			System.out.println("First: " + byId.firstEntry().getValue().name());
52:			System.out.println("Last:  " + byId.lastEntry().getValue().name());
54:			// Record patterns with `when` guard and null case
55:			System.out.println(classify(results.get(0)));
56:			System.out.println(classify(null));
57:		}
58:	}
```

- **Line 17** — `case null -> "No data";` — explicit `null` handling (Java 21 standard, no longer throws `NullPointerException` as pre-21). **Lines 18–20** combine a **record pattern** (destructures `EmployeeRecord` without accessors) with a **`when` guard** (`salary > 80000`); the unguarded case on line 20 catches anything that fails the guard — cases evaluate top-to-bottom, so it must come after. Line 55's salary (`60000`) fails the guard, so it prints `"Emp-1 is staff in Dept-1"`.
- **Line 31** — `Executors.newVirtualThreadPerTaskExecutor()` gives every task its own virtual thread; `try`-with-resources works since `ExecutorService` implements `AutoCloseable` (Java 19+) — `close()` implicitly calls `shutdown()`/blocks until done, replacing Module 24's manual boilerplate. **Lines 33–38**: 1000 tasks each sleep 100ms, but with one virtual thread per task all sleeps overlap, completing in well under a second — the entire point of the demo.
- **Line 48** — `LinkedHashMap` implements `SequencedMap` as of Java 21, gaining `firstEntry()`/`lastEntry()` (lines 51–52) for free — direct access replacing manual iteration.

---

## 32. Java 24 Features

*(No dedicated code file exists in the course repo for this module — confirmed empty via the same `find` check above. The example below is original and clearly labeled.)*

**What it teaches:** Java 24 (March 2025) is a non-LTS stepping stone toward Java 25 LTS. Its theme, per the courseware, is "Project Loom and Project Valhalla reach maturity" — concurrency gets easier and safer, and the type system gets smarter. Several features are still in preview (expected to finalize in Java 25), but a few are already standard.

**Primitive Types in Patterns (JEP 488, second preview):** extends `instanceof`/`switch` pattern matching — previously reference-type-only — to primitives directly, including **narrowing** checks (does this `long`/`double` value actually fit into an `int` without loss?), eliminating the previous requirement to autobox everything (`Integer`, `Double`) just to participate in pattern matching, which had real allocation overhead.

**Stream Gatherers (JEP 485, standard):** streams' built-in intermediate operations (`filter`, `map`, `sorted`, etc.) couldn't previously be extended with custom ones cleanly — `Gatherer` fixes this, exposing a pluggable intermediate-operation interface with four (mostly optional) components: an initializer (mutable state), an integrator (per-element logic), a combiner (merging state across parallel-stream segments), and a finisher (final output). The JDK ships useful built-ins via `Gatherers`: `windowFixed(n)` (non-overlapping batches), `windowSliding(n)` (overlapping windows), `fold`/`scan` (accumulation, `scan` emitting each intermediate running value), `mapConcurrent(n, fn)` (bounded-concurrency parallel map). You can also write fully custom gatherers via `Gatherer.ofSequential(...)`.

**Structured Concurrency (JEP 499, fourth preview)** and **Scoped Values (JEP 487, fourth preview)** continue maturing toward Java 25 standardization. `ScopedValue<T>` is positioned as `ThreadLocal`'s replacement, specifically designed for virtual threads and structured concurrency: values are **immutable** for the duration of a `ScopedValue.where(KEY, value).run(() -> { ... })` block (bound via `.get()` inside, automatically unbound outside), require no manual cleanup (`ThreadLocal.remove()` has no equivalent need here), and propagate automatically through structured task scopes — eliminating the classes of bugs that come from `ThreadLocal`'s mutability (accidental cross-task leakage, forgotten `remove()` calls causing memory leaks in pooled-thread environments).

**Other Java 24 items:** Ahead-of-Time class loading (JEP 483, standard) caches JVM class-loading work from a training run and replays it at startup for dramatically faster warm-up (no code changes needed); `Object.finalize()` is **completely removed** (JEP 421, finalized) — code overriding it simply won't compile/run correctly anymore, cementing `AutoCloseable`/try-with-resources as the only supported cleanup mechanism (as previewed already in Module 27); Flexible Constructor Bodies (JEP 492, third preview) relax the old rule that *nothing* could run before `super()`/`this()` in a constructor — you can now run validation or argument-preparation statements before the super call, as long as you don't touch `this` before it; quantum-resistant cryptography (ML-KEM key exchange, ML-DSA signatures) is now standard in `java.security`.

### Illustrative example (not from course repo) — Stream Gatherers, Scoped Values, Flexible Constructors

```java
1:	package com.acme.demo.day3.illustrative;
3:	import java.util.*;
4:	import java.util.stream.*;
6:	public class Java24FeaturesIllustrative {
8:		record EmployeeRecord(int id, String name, double salary) { }
10:		static final ScopedValue<String> APPROVER = ScopedValue.newInstance();
12:		static class Manager {
13:			private final String name;
14:			private final int teamSize;
16:			Manager(String rawName, int teamSize) {
17:				var cleanedName = rawName == null ? "Unknown" : rawName.strip(); // runs BEFORE super()
18:				if (teamSize <= 0) throw new IllegalArgumentException("teamSize must be positive");
19:				this.name = cleanedName;   // assigning fields directly since this class has no explicit super here
20:				this.teamSize = teamSize;
21:			}
22:		}
24:		static void printBatch(List<EmployeeRecord> batch) {
25:			System.out.println("--- Batch (approved by " + APPROVER.get() + ") ---");
26:			batch.forEach(e -> System.out.printf("  %-8s %.2f%n", e.name(), e.salary()));
27:		}
29:		public static void main(String[] args) {
31:			List<EmployeeRecord> employees = List.of(
32:				new EmployeeRecord(101, "Sonu", 75000),
33:				new EmployeeRecord(102, "Monu", 82000),
34:				new EmployeeRecord(103, "Tonu", 55000),
35:				new EmployeeRecord(104, "Ponu", 91000),
36:				new EmployeeRecord(105, "Gonu", 68000)
37:			);
39:			// Stream Gatherers: process in fixed-size batches
40:			ScopedValue.where(APPROVER, "Ponu").run(() -> {
41:				employees.stream()
42:					.gather(Gatherers.windowFixed(2))
43:					.forEach(Java24FeaturesIllustrative::printBatch);
45:				double totalGross = employees.stream()
46:					.mapToDouble(EmployeeRecord::salary)
47:					.sum();
48:				System.out.printf("Total gross: %.2f (approved by %s)%n", totalGross, APPROVER.get());
49:			});
51:			// Flexible constructor body -- validation runs before this class's field init
52:			Manager m = new Manager("  Rina  ", 5);
53:			System.out.println("Manager: " + m.name + ", team of " + m.teamSize);
54:		}
55:	}
```

- **Line 10** — `ScopedValue` is declared `static final` like a `ThreadLocal` would be, but has no `set()` method at all — it can only be bound for the duration of a `run()`/`call()` block, enforcing immutability by construction rather than convention.
- **Lines 16–21** — `Manager`'s constructor demonstrates **flexible constructor bodies**: lines 17–18 compute/validate *before* any field assignment. Under the pre-Java-24 rule, if this class extended another, *no* statement could precede the mandatory `super()`; Java 24 relaxes that to allow prep/validation ahead of `super()`, as long as `this` isn't referenced first.
- **Line 40** — `ScopedValue.where(APPROVER, "Ponu").run(() -> {...})` binds `APPROVER` for the lambda's duration — any code called transitively (including `printBatch` via the method reference on line 43) can read `APPROVER.get()`; outside the block it's unbound (`.get()` would throw `NoSuchElementException`).
- **Line 42** — `Gatherers.windowFixed(2)`, the Java 24 **Stream Gatherer** standard feature, groups the 5-element stream into non-overlapping windows of size 2 — `[Sonu, Monu]`, `[Tonu, Ponu]`, `[Gonu]` (short last window since 5 isn't evenly divisible).
- **Line 43** — each windowed batch is passed to `printBatch`, demonstrating the scoped value correctly propagates into a method called deep inside a stream pipeline, with no `approver` parameter threaded through manually.

---

## What's next

Core Java is now fully covered end to end, sourced from your actual courseware and code: setup/JVM/datatypes through OOP, exceptions, inner classes, enums, lambdas/streams/Optional, concurrency, I/O, collections/generics, garbage collection, and Java 13 through 24 features.

Reply "next" and I'll build the same courseware-plus-code-grounded guide for **Maven**.
