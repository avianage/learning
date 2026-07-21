# Core Java — Complete Line-by-Line Guide

This guide is built directly from your actual course materials: the conceptual courseware in `Courseware/01-core-java/` (modules 01–32) and the real, runnable example code in `Code/Core Java/src/com/acme/demo/` (day1–day4). Every topic below pairs a synthesized summary of the courseware module with the actual course code, reproduced verbatim and explained line by line. Where the course repo has no dedicated example for a topic, that gap is called out explicitly rather than papered over.

---

## 1. Introduction and Setup

Java is a general-purpose, statically-typed, object-oriented language built around **WORA (Write Once, Run Anywhere)**: source code (`.java`) is compiled by `javac` into platform-independent **bytecode** (`.class`), which any platform's **JVM** can execute. Three terms to keep straight: the **JDK** (what you install as a developer — compiler + runtime + tools), the **JRE** (runtime only — JVM + standard libraries, not separately distributed since Java 11), and the **JVM** (the actual execution engine, platform-specific but bytecode-compatible everywhere). Java 8 (2014, lambdas/streams) and Java 17/21 (modern LTS releases) are the version markers most worth remembering; this courseware baselines on Java 8 syntax.

Every program's entry point is `public static void main(String[] args)` — the JVM looks for this exact signature. `public` so the JVM can call it from outside the class, `static` so it can be invoked without first creating an object, `void` because it returns nothing, and `String[] args` to receive command-line arguments. The compiler enforces strict rules: every statement ends in `;`, Java is case-sensitive throughout, the public class name must match the filename exactly, and curly braces must balance. The compiler also breaks source into **tokens** — keywords, identifiers, literals, operators, separators, comments — and Java has firm identifier rules (no leading digit, no keywords, `_`/`$` allowed) plus (unenforced but universal) naming conventions: PascalCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants.

### Code — `Hello.java`

```java
1:  package com.acme.demo;
2:  
3:  // Java - versions 
4:  
5:  public class Hello {
6:  
7:  	public static void main(String[] args) {
8:  
9:  //		byte b1 = 100;
10: //		byte b2 = 30;
11: //		int b3 = b1 + b2;
12: //		byte b3 = (byte) (b1 + b2);
13: 		
14: 		byte b1 = 100;
15: 		byte b2 = 30;
16: 		byte b3 = (byte) (b1 + b2);
17: 
18: 		System.out.println(b3);
19: 
20: 		System.out.println("Hello world! 2");
21: 
22: 	}
23: 
24: }
```

- **Line 1** — `package com.acme.demo;` must be the first non-comment line in the file. It places `Hello` in the `com.acme.demo` namespace, which also dictates the required directory structure (`com/acme/demo/Hello.java`) on disk.
- **Line 5** — `public class Hello` — the file must be named `Hello.java` because the public top-level class name and filename must match exactly.
- **Line 7** — the mandatory JVM entry-point signature, explained above.
- **Lines 9–12** — commented-out code showing what the instructor first tried: `int b3 = b1 + b2;` would compile fine (byte + byte promotes to int), but `byte b3 = b1 + b2;` would **not** compile without a cast, because the result of any `byte`/`short` arithmetic is always widened to `int` — assigning an `int` back into a `byte` variable needs an explicit narrowing cast.
- **Line 14–15** — `byte b1 = 100; byte b2 = 30;` declare two 1-byte integers (range -128 to 127).
- **Line 16** — `byte b3 = (byte) (b1 + b2);` — `b1 + b2` is computed as `int` (130), then explicitly narrowed back to `byte` with `(byte)`. Since 130 exceeds `byte`'s max of 127, this **overflows and wraps around** (130 − 256 = −126) — a classic narrowing-cast gotcha the instructor is deliberately demonstrating.
- **Line 18** — prints the wrapped value; running this prints `-126`, not `130`, illustrating why blind narrowing casts are dangerous.
- **Line 20** — a plain `println` — the "real" hello-world output.

---

## 2. JVM Architecture

This module explains what happens after you type `java Employee`. The JVM works in three phases: **Loading** (the Class Loader reads `.class` bytecode off disk into the **Method Area**, verifying it and initializing static state), **Execution** (the Execution Engine runs bytecode, either via the **Interpreter** — instruction by instruction — or, for "hot" methods called repeatedly, via the **JIT Compiler**, which compiles them to native machine code for near-native speed after a warm-up period), and **Garbage Collection** (objects with no live reference are reclaimed automatically).

The key memory areas: the **Method Area** (shared, holds class metadata, static variables, the string constant pool — one copy per class), the **Heap** (shared, holds every object created with `new`, subdivided into Young Generation — Eden + two Survivor spaces — and Old Generation for long-lived, promoted objects), the **Stack** (one per thread, holds method call frames — local variables and parameters — pushed on call and popped on return; exhausting it throws `StackOverflowError`, classically from unterminated recursion), the **PC Register** (per-thread, tracks the current bytecode instruction), and the **Native Method Stack** (for JNI/native calls). A critical distinction to internalize: static fields live once in the Method Area regardless of how many objects exist, while instance fields live per-object on the heap. `System.gc()` only *requests* garbage collection — the JVM is free to ignore it, and calling it in production code is discouraged since it can trigger a GC pause at an inopportune moment. Common runtime errors map directly to these regions: `StackOverflowError` (stack exhausted), `OutOfMemoryError: Java heap space` (heap exhausted), `OutOfMemoryError: Metaspace` (method area exhausted, e.g. from dynamic class generation), `ClassNotFoundException`/`NoClassDefFoundError` (classpath/dependency problems at load time vs. runtime).

No dedicated runnable file exists for this topic in the course code folder — it is a conceptual module. The closest illustration in the codebase is `Hello.java` (Topic 1 above): its `byte b3 = (byte)(b1 + b2);` line is itself bytecode-level behavior — the JVM's execution engine performs the addition as an `int` operation (per JVM arithmetic instruction rules) and the explicit narrowing cast is a separate bytecode instruction (`i2b`) that truncates the result, which is why the overflow/wraparound happens at runtime rather than being caught at compile time.

---

## 3. Datatypes, Variables and Operators

Every variable has a type, a name, and a value. Java splits types into **primitives** (8 built-in value types: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean` — each with a fixed size and, for numeric ones, a fixed range) and **reference types** (point to heap objects). Local variables (inside methods) have no default value and *must* be explicitly initialized before use — the compiler rejects reads of a definitely-unassigned local; class-level fields (instance/static), by contrast, get automatic defaults (`0`, `0.0`, `false`, `null`, `' '`).

Literals come in several forms: integer literals can be decimal, binary (`0b...`), octal (`0...`), or hex (`0x...`); `long` literals need an `L` suffix when they exceed `int` range; `float` literals need an `f` suffix (a bare decimal like `3.14` defaults to `double` and won't implicitly narrow into a `float` variable); underscores can visually separate digit groups. `char` literals use single quotes and store a Unicode code point (`'A'` == `65`). `String` is a reference type, not a primitive, but gets literal syntax and pool-backed sharing.

**Type conversion**: widening (`byte→short→int→long→float→double`, with `char` feeding into `int`) is automatic and safe. Narrowing requires an explicit cast `(type)` and can lose data — critically, narrowing a `double`/`float` to an integer type **truncates**, it does not round (`(int) 9.99` → `9`). Arithmetic **promotion rules** matter: any expression involving `byte`/`short` promotes to `int`; involving `long` promotes to `long`; involving `float`/`double` promotes accordingly. This is why `byte b3 = b1 + b2;` is a compile error without a cast — the `+` always produces (at minimum) an `int`.

**Operators**: standard arithmetic (`+ - * / %`), with the classic **integer-division gotcha** — `int / int` truncates the fractional part entirely (`7 / 8` is `0`, not `0.875`); to get a decimal result, cast or use a `double` literal divisor. Assignment operators (`+= -= *= /= %=`). Increment/decrement have pre- and post- forms that differ in *when* the value is read versus mutated (`b = a++` uses `a`'s old value then increments; `c = ++a` increments first then uses the new value). Relational operators always yield `boolean`; `==` compares primitive *values* but object *references* (not content — use `.equals()` for content, covered fully in Module 14). Logical `&&`/`||` **short-circuit** — the second operand is skipped once the result is determined, which is not just an optimization but a null-safety technique (`e != null && e.getSalary() > 50000`). Bitwise operators (`& | ^ ~ << >> >>>`) operate on individual bits; `>>>` is the unsigned right shift (fills with `0` regardless of sign). The ternary `condition ? a : b` is a compact if-else for simple value selection. Operator precedence exists but the practical advice is: use parentheses to make intent explicit rather than memorizing the table. Variable **scope** is block-bound (`{}`) — a variable declared inside an `if` block doesn't exist outside it. `var` (Java 10+) is compile-time type inference for local variables only — not a dynamic type.

### Code — `day1/CommonDemo.java`

```java
1:  package com.acme.demo.day1;
2:  
3:  public class CommonDemo {
4:  
5:  	public static void main(String[] args) {
6:  		// Employee data -
7:  		int id = 1;
8:  		String name = "Sonu";
9:  		double salary = 10.75;
10: //		long[] phones = { 9876543210L, 678901235L };
11: 		int i = 97;
12: 		char c = (char) i;
13: 		System.out.println(c);
14: 
15: 	}
16: 
17: }
```

- **Line 7** — `int id = 1;` — a standard 4-byte whole number, the default choice for integers in Java.
- **Line 8** — `String name = "Sonu";` — a reference type holding a string literal (interned in the string pool, see Module 7).
- **Line 9** — `double salary = 10.75;` — the default floating-point type for decimal values (no `f`/`d` suffix needed since `double` is the default for decimal literals).
- **Line 10** — commented-out `long[] phones = {...}` — shows a `long[]` array of phone numbers; the `L` suffix would be needed on the literals since they exceed `int` range (9,876,543,210 > `Integer.MAX_VALUE`).
- **Line 11** — `int i = 97;` — an `int` holding the ASCII/Unicode code point for the character `'a'`.
- **Line 12** — `char c = (char) i;` — explicit narrowing cast from `int` to `char`. `char` is a 16-bit unsigned type holding Unicode code points; casting `int` to `char` reinterprets the numeric value as a code point. Since `97` fits within `char`'s range, no data is lost here (unlike the `byte` overflow example in Module 1).
- **Line 13** — prints `a` — proof that `c` now holds the character corresponding to code point 97.

### Code — `Hello.java` (see Module 1)

Already covered above; it is the second demonstration file relevant to this module because of its narrowing-cast/overflow behavior on `byte` arithmetic.

---

## 4. Wrapper Classes

Primitives are fast but are **not objects** — they can't go into collections (which only hold objects), can't represent "no value" via `null`, and don't carry utility methods. Java's fix is a **wrapper class** per primitive (`Integer`, `Double`, `Boolean`, `Character`, `Byte`, `Short`, `Long`, `Float` — all in `java.lang`, auto-imported). You create them via `valueOf()` (preferred over `new Integer(...)`, which is deprecated) or, most commonly in real code, by **parsing strings**: `Integer.parseInt(s)`, `Double.parseDouble(s)`, `Boolean.parseBoolean(s)` — these throw `NumberFormatException` on invalid input. Converting the other direction uses `Integer.toString(x)`, `String.valueOf(x)`, or string concatenation.

**Autoboxing/unboxing** (Java 5+) automate the primitive↔wrapper conversion: assigning an `int` to an `Integer` variable auto-boxes; using an `Integer` in arithmetic auto-unboxes. The dangerous edge case: unboxing a `null` wrapper throws `NullPointerException` — always null-check a wrapper before using it in arithmetic. A separate, very testable gotcha is the **Integer cache**: Java caches `Integer` objects for values −128 to 127, so `==` accidentally "works" for small cached values but fails for larger ones — the rule is to **always use `.equals()`**, never `==`, to compare wrapper objects. Useful static members exist on each wrapper (`Integer.MAX_VALUE`/`MIN_VALUE`, `Integer.toBinaryString()`, `Integer.compare()`, `Character.isDigit()`/`isLetter()`/`toUpperCase()`, `Boolean.parseBoolean()` which is case-insensitive but only recognizes `"true"`). The module closes with `printf`/`String.format` formatting specifiers (`%d %f %.2f %s %n %-15s`), which is adjacent but frequently tested alongside wrapper/number handling.

### Code — `day2/commons/wrapper/WrapperDemo.java`

```java
1:  package com.acme.demo.day2.commons.wrapper;
2:  
3:  import java.util.Scanner;
4:  
5:  import com.acme.demo.day1.constructor.Employee;
6:  
7:  public class WrapperDemo {
8:  
9:  	public static void main(String[] args) {
10: 
11: //		Employee emp = new Employee();
12: //		Scanner sc = new Scanner(System.in);
13: //		int num = 10;
14: //		emp. // insance methods field 
15: //		num. // primittive field 
16: 
17: //		int num = 10; // primitive 
18: //		Integer num2 = 20; // object 
19: 
20: //		int num = 10; 
21: //		Integer num2 = num; // boxing - autoboxing 
22: //		Integer num3 = Integer.valueOf(num); // boxing - manual boxing 
23: //		int num4 = num3; // unboxing - auto unboxing 
24: //		int num5 = num3.intValue(); // unboxing - manual unboxing 
25: 		
26: //		int num6 = Integer.parseInt("8888");
27: 		int num6 = Integer.parseInt("aaa"); //exception 
28: 		System.out.println(num6);
29: //		Integer num2 = 10; 
30: //		Integer. // static methods and fields 
31: //		num2. // instance methods and fields 
32: 
33: 	}
34: }
```

- **Line 3–5** — imports: `Scanner` (unused in the live code, left from an earlier exploration) and `Employee` from a sibling package — shown only in commented lines to illustrate that a primitive (`num.`) has no callable methods/fields (it's not an object), while an object reference (`emp.`) does.
- **Lines 17–18** — commented notes: `int num = 10;` is a raw primitive value; `Integer num2 = 20;` autoboxes the literal `20` into an `Integer` object — the instructor is annotating the conceptual difference.
- **Lines 20–24** — a commented walkthrough of all four boxing/unboxing forms: `Integer num2 = num;` (autoboxing), `Integer.valueOf(num)` (manual/explicit boxing), `int num4 = num3;` (auto-unboxing), `num3.intValue()` (manual unboxing via the wrapper's own instance method).
- **Line 26** — commented alternative showing the "happy path": `Integer.parseInt("8888")` would succeed and return `8888`.
- **Line 27** — `int num6 = Integer.parseInt("aaa");` — the line actually left active. `"aaa"` is not a valid integer, so this throws `NumberFormatException` **at runtime** (the code compiles fine — this is a runtime, not compile-time, failure). This is a deliberate live demonstration of the exception `parseInt` throws on malformed input.
- **Line 28** — never reached when the program is run as-is, because line 27 throws before execution gets here.

### Code — `day2/Test.java` (Integer cache, `==` vs `.equals()`)

```java
1:  package com.acme.demo.day2;
2:  
3:  public class Test {
4:  
5:  	public static void main(String[] args) {
6:  
7:  		Integer i = 200;
8:  		Integer j = 200;
9:  		System.out.println(i == j);
10: 		System.out.println(i.equals(j));
11: 
12: 		Integer k = 10;
13: 		Integer l = 10;
14: 		System.out.println(k == l);
15: 		System.out.println(k.equals(l));
16: 
17: 	}
18: }
```

- **Line 7–8** — `Integer i = 200; Integer j = 200;` — both autobox the literal `200`. Since `200` is outside the cached range (−128 to 127), the compiler emits `Integer.valueOf(200)` **twice**, producing two distinct `Integer` objects on the heap.
- **Line 9** — `i == j` compares **references**, not values — since `i` and `j` are different objects, this prints `false`.
- **Line 10** — `i.equals(j)` compares the wrapped `int` values — prints `true`, regardless of caching.
- **Line 12–13** — `Integer k = 10; Integer l = 10;` — `10` is inside the cached range, so both autoboxing calls return the **same** cached `Integer` object from the internal `IntegerCache`.
- **Line 14** — `k == l` prints `true` here — but only because both point at the same cached object, not because `==` correctly compares wrapper values. This is precisely the "gotcha" the courseware warns about: `==` appears to work for small numbers by accident.
- **Line 15** — `k.equals(l)` also prints `true`, correctly and reliably regardless of value range — reinforcing that `.equals()` is the only comparison method that should be trusted for wrappers.

---

## 5. Flow Control

Java executes top-to-bottom by default; flow control adds decisions, repetition, and jumps. **`if`/`else if`/`else`** chains are evaluated top to bottom and stop at the first true condition; always use braces even for single statements. **`switch`** compares one variable against multiple fixed values — classic `switch` requires `break` to prevent fall-through (deliberate fall-through, e.g. shared `case` labels, is legal but risky), and works only on `int`/`byte`/`short`/`char`/`String`/`enum` (not `double`/`float`/`long`/`boolean`). The Java 14+ **arrow `switch`** (`case X -> value;`) removes fall-through entirely and can be used as an expression that returns a value directly; multi-statement arms use `yield` to produce the value.

**Loops**: `while` checks the condition *before* each iteration (may run zero times); `do-while` checks *after*, guaranteeing at least one execution — useful for menus/validation prompts; `for` is best when the iteration count is known upfront, with all three clauses (`init; condition; update`) individually optional (`for(;;)` is an intentional infinite loop); **for-each** (`for (T x : collection)`) is the cleanest way to visit every element when you don't need the index, but you cannot mutate the underlying array through the loop variable or access the index. `break` exits the nearest enclosing loop entirely; `continue` skips to the next iteration; **labeled** `break`/`continue` (`outer: for(...) { ... break outer; }`) target an outer loop from inside nested loops — legal but should be used sparingly since it hurts readability (often a method extraction with `return` is cleaner).

### Code — `day1/ControlDemo.java`

```java
1:  package com.acme.demo.day1;
2:  
3:  public class ControlDemo {
4:  
5:  	public static void main(String[] args) {
6:  
7:  //		int i = 10, j = 10;
8:  //
9:  //		if (i > j) {
10: //			System.out.println("High");
11: //		} else if (i < j)
12: //			System.out.println("Low");
13: //		else
14: //			System.out.println("Same");
15: 		
16: //		switch (key) {
17: //		case value: {
18: //			
19: //			yield type;
20: //		}
21: //		default:
22: //			throw new IllegalArgumentException("Unexpected value: " + key);
23: //		}
24: 
25: 	}
26: 
27: }
```

- **Line 7** — `int i = 10, j = 10;` (commented) — declares two `int`s of equal value on a single line (comma-separated multi-declaration).
- **Lines 9–14** (commented) — an `if / else if / else` skeleton: `if (i > j)` prints "High"; the `else if (i < j)` branch prints "Low" (note it has no braces — only the single following statement belongs to it, which is legal but discouraged per the courseware's "always use braces" advice); the final `else` (also brace-less) prints "Same" when neither comparison holds — with `i == j == 10` here, "Same" is the branch that would actually execute if uncommented.
- **Lines 16–23** (commented) — a generic `switch` skeleton using a traditional block-style `case value: { ... yield type; }` and a `default` that throws `IllegalArgumentException` — this is actually **new-style switch expression syntax** (using `yield` inside a traditional `case:` block), a hybrid form legal from Java 13+. It demonstrates the idiomatic pattern of using `default: throw ...` to fail loudly on unexpected values rather than silently doing nothing.
- This file, as committed, has no live/uncommented logic — it exists as an instructor scratchpad/skeleton for live-coding `if-else` and `switch` during the session.

### Code — `day1/scanner/ScannerDemo.java` (interactive input driving control flow)

```java
1:  package com.acme.demo.day1.scanner;
2:  
3:  import java.util.Scanner;
4:  
5:  // take user inputs 
6:  
7:  public class ScannerDemo {
8:  
9:  	public static void main(String[] args) {
10: 
11: 		Scanner sc = new Scanner(System.in);
12: 
13: 		System.out.println("Welcome\nEnter your name:");
14: 		String username = sc.next();
15: 		System.out.println("Welcome " + username + "!");
16: 
17: 		sc.close();
18: 
19: 	}
20: 
21: }
```

- **Line 3** — imports `java.util.Scanner`, the standard class for reading console input; unlike `java.lang`, `java.util` is not auto-imported.
- **Line 11** — `new Scanner(System.in)` wraps standard input as a `Scanner`; this object is the typical driver behind interactive loops like the courseware's "keep prompting until valid input" `while` pattern.
- **Line 13** — `\n` inside the string literal is an escape sequence producing a newline within a single `println`.
- **Line 14** — `sc.next()` blocks execution, waiting for the user to type a single whitespace-delimited token, and returns it as a `String`.
- **Line 15** — string concatenation with `+` builds the greeting; each `+` on `String` operands internally creates a new `String` object (immutability, covered in Module 7).
- **Line 17** — `sc.close()` releases the underlying input stream resource. Closing `System.in`-backed scanners is often debated (it can prevent further console reads in the same JVM), but is shown here as good resource-hygiene practice.

---

## 6. Arrays

An array is a **fixed-size, zero-indexed, homogeneous** collection — once created its length cannot change, and the array itself is a heap object (even when it holds primitives). Declaration and creation are separate steps (`int[] salaries; salaries = new int[5];`), though usually combined. Elements not explicitly set get type-appropriate defaults (`0`/`0.0`/`false`/`null`). The **array initializer** shorthand (`int[] ids = {101, 102, 103};`) infers size from the values; the `new Type[]{...}` form is required when creating an array inline without a variable (e.g. as a method argument).

Access is via `array[index]`; `array.length` is a **field**, not a method (no parentheses) — contrast with `String.length()`. Out-of-bounds access throws `ArrayIndexOutOfBoundsException` at runtime (not caught at compile time). Iteration uses either an index-based `for` (when the index is needed) or `for-each` (when only values matter). `java.util.Arrays` supplies utility methods: `sort()` (in-place), `binarySearch()` (array must already be sorted), `fill()`, `copyOf()`/`copyOfRange()`, `equals()` (content comparison — `==` on arrays only compares references), and critically `Arrays.toString()` — printing an array directly (`System.out.println(arr)`) prints an unhelpful type-and-hashcode string like `[I@1b6d3586`.

**2D arrays** are arrays of arrays, accessed `[row][col]`; rows can have independent lengths (**jagged arrays**). Arrays are **passed by reference** into methods — the method receives the same array's address, so element mutations inside the method are visible to the caller (this is distinct from primitives, which are passed by value). Common pitfalls: off-by-one loop bounds (`i <= arr.length` instead of `<`), forgetting arrays are fixed-size (no "adding" an element — use `ArrayList` for dynamic sizing), and calling `.length` on a `null` array reference (`NullPointerException`).

### Code — `day2/commons/arrays/ArrayDemo.java`

```java
1:  package com.acme.demo.day2.commons.arrays;
2:  
3:  import java.util.Arrays;
4:  
5:  public class ArrayDemo {
6:  
7:  	public static void main(String[] args) {
8:  
9:  		int[] arr = { 25, 31, 17, 9, 22 };
10: 		System.out.println("Original array");
11: 		for (int a : arr)
12: 			System.out.println(a);
13: 		System.out.println(arr.length);
14: 		Arrays.sort(arr);
15: 		System.out.println("sorted array");
16: 		for (int a : arr)
17: 			System.out.println(a);
18: 		
19: //		Arrays.
20: 
21: 	}
22: 
23: }
```

- **Line 3** — imports `java.util.Arrays`, the utility class providing static helper methods for array operations.
- **Line 9** — `int[] arr = { 25, 31, 17, 9, 22 };` — array-initializer syntax; the compiler infers a length-5 array and fills it left to right.
- **Lines 11–12** — a `for-each` loop; `a` takes each element's value in order (index not needed, so `for-each` is the idiomatic choice here per the courseware's guidance).
- **Line 13** — `arr.length` — the array's fixed size (a field access, `5`), printed to show it before sorting mutates the *contents* (length itself never changes).
- **Line 14** — `Arrays.sort(arr)` sorts `arr` **in place** (ascending, using a dual-pivot quicksort variant for primitives) — the original array object is mutated, no new array is returned.
- **Lines 16–17** — re-iterates with the same `for-each` pattern to show the now-sorted contents (`9, 17, 22, 25, 31`).
- **Line 19** — commented `Arrays.` — an IDE autocomplete exploration stub, left in to show the instructor was browsing `Arrays`' static method list live (e.g. `binarySearch`, `fill`, `copyOf`).

---

## 7. String Handling

`String` is a **class**, not a primitive, but gets literal syntax (`"Sonu"`) and special JVM support via the **string pool** (part of the Method Area): identical string literals are shared — `String a = "HR"; String b = "HR";` makes `a` and `b` reference the *same* pooled object, so `a == b` is `true`, but `new String("HR")` forces a **new**, separate heap object, so `a == c` is `false`. The absolute rule: **use `.equals()` for content comparison, never `==`** — and putting the literal first (`"HR".equals(dept)`) is a defensive idiom that avoids `NullPointerException` if the variable side is `null`.

The single most important property of `String` is **immutability**: once created, a `String`'s characters never change. Every apparent mutation (`name = name + " Sharma"`) actually creates a **new** `String` object and reassigns the reference; the original is left untouched (and eventually garbage collected). Immutability exists for security (safe sharing of sensitive values like paths/passwords), thread-safety (no synchronization needed), and to make string-pool sharing safe in the first place.

Key methods: `length()`, `charAt(i)`, `indexOf`/`lastIndexOf`, `toUpperCase`/`toLowerCase`, `trim()` (ASCII whitespace only) vs `strip()` (Unicode-aware, Java 11+), `substring(start, end)` (end exclusive), `startsWith`/`endsWith`/`contains`, `isEmpty()` (length 0) vs `isBlank()` (whitespace-only, Java 11+), `replace`/`replaceAll` (regex)/`replaceFirst`, `split(regex)` and `String.join(sep, ...)`, `equals`/`equalsIgnoreCase`/`compareTo`, and `String.valueOf`/`String.format` for building output.

Because every `+` on strings allocates a new object, building strings in a loop with `+=` is wasteful; **`StringBuilder`** is the mutable, in-place alternative (`append`, `insert`, `delete`, `replace`, `reverse`, method chaining) and is the correct default for repeated string construction. **`StringBuffer`** is functionally identical but synchronized (thread-safe, slightly slower) — use only in genuinely multi-threaded contexts.

### Code — `day2/commons/strings/StringDemo.java`

```java
1:  package com.acme.demo.day2.commons.strings;
2:  
3:  public class StringDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		
7:  		String str = "abcdef";
8:  		System.out.println(str);
9:  		System.out.println(str.length());
10: 		System.out.println(str.charAt(0));
11: 		System.out.println(str.concat(str));
12: 		System.out.println(String.valueOf(10 == 10));
13: //		String.
14: 		
15: 	}
16: }
```

- **Line 7** — `String str = "abcdef";` — a string literal, placed in the string pool.
- **Line 9** — `str.length()` — an instance **method** returning the character count (`6`); note this contrasts with array's `.length`, which is a field, not a method — a frequent point of confusion for beginners.
- **Line 10** — `str.charAt(0)` — returns the character at index `0` (`'a'`), zero-indexed like arrays.
- **Line 11** — `str.concat(str)` — concatenates `str` with itself, returning a **new** `String` (`"abcdefabcdef"`); `str` itself is unchanged because `String` is immutable — this line demonstrates immutability directly, since printing `str` again later would still show `"abcdef"`.
- **Line 12** — `String.valueOf(10 == 10)` — `10 == 10` evaluates to the `boolean` `true`; `String.valueOf(boolean)` converts it to the literal string `"true"`. `String.valueOf` is overloaded for every type and is the standard "convert anything to a String" utility.
- **Line 13** — commented `String.` — again an IDE-autocomplete exploration stub, showing the instructor browsing `String`'s static method list live.

---

## 8. Classes and Objects

OOP bundles data (**fields**) and behavior (**methods**) into a single unit — a **class** is the blueprint, an **object** is a runtime instance created from it with `new`. A typical class has: **instance fields** (per-object state, conventionally `private`), a **static field** (one copy shared by the whole class, e.g. an instance counter), an optional **static block** (runs exactly once, when the class is first loaded), an optional **instance initializer block** (runs before *every* constructor call), one or more **constructors** (same name as the class, no return type — the compiler supplies a no-arg default only if you define none yourself), **instance methods** (operate via an implicit `this` on the calling object) and **static methods** (belong to the class, have no `this`, cannot touch instance fields directly), plus conventionally **getters/setters** and an overridden **`toString()`**.

**Constructor chaining** via `this(...)` (must be the first statement) lets one constructor delegate to another, avoiding duplicated init logic; **method overloading** (same name, different parameter lists, resolved at compile time) lets a class expose multiple call shapes for one operation. The keyword **`this`** disambiguates a field from a same-named parameter (`this.id = id;`), calls a sibling constructor, or passes the current object elsewhere. Object variables hold **references** (heap addresses), not the object itself — assigning one reference variable to another copies the address, so both variables see mutations through either one; `null` means "points to nothing," and dereferencing it throws `NullPointerException`. Objects are passed to methods by **value of the reference** — the callee can mutate the object's state through that reference, but reassigning the local parameter inside the method has no effect on the caller's variable. **Varargs** (`double... salaries`) let a method accept a variable number of arguments as an implicit array; the varargs parameter must be last.

Execution order when `new` runs: static block (once, at class load) → instance block (every construction) → constructor body.

### Code — `day1/classes/Employee.java` and `day1/classes/ClassDemo.java` (fields, static field, default constructor, `toString`)

```java
1:  package com.acme.demo.day1.classes;
2:  
3:  public class Employee {
4:  
5:  	static long officePhone = 123L;
6:  
7:  	int id;
8:  	String name;
9:  	double salary;
10: 	long phone;
11: 
12: 	@Override
13: 	public String toString() {
14: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
15: 	}
16: 
17: }
```

- **Line 5** — `static long officePhone = 123L;` — a **static** field: one copy exists for the whole `Employee` class, shared by every instance, and is accessible via `Employee.officePhone` without needing an object.
- **Lines 7–10** — instance fields (`id`, `name`, `salary`, `phone`) — package-private (no modifier), each object gets its own independent copy; default values apply since they're never explicitly initialized (`id`→`0`, `name`→`null`, `salary`→`0.0`, `phone`→`0L`).
- **Line 12** — `@Override` on `toString()` — an annotation instructing the compiler to verify this method actually overrides a method from a superclass (here, `Object`); it fails to compile if it doesn't match a real overridable signature — a safety net against typos.
- **Line 13–14** — the overridden `toString()`, string-concatenating each field into a readable representation; this is what `System.out.println(obj)` and `"" + obj` invoke implicitly instead of the default `ClassName@hashcode` form.

```java
1:  package com.acme.demo.day1.classes;
2:  
3:  public class ClassDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		
7:  		Employee obj = new Employee();
8:  		System.out.println(obj.toString());
9:  		obj.id = 1;
10: 		obj.name = "Sonu";
11: //		obj.salary = 10.75;
12: 		System.out.println(obj.toString());
13: 		System.out.println(Employee.officePhone);
14: 		
15: 		Employee obj2 = new Employee();
16: 		System.out.println(obj2.toString());
17: 		obj2.id = 2;
18: 		obj2.name = "Monu";
19: 		obj2.salary = 11.25;
20: 		System.out.println(obj2.toString());
21: 	}
22: 
23: }
```

- **Line 7** — `Employee obj = new Employee();` — since `Employee` defines no explicit constructor, the compiler supplies a **default no-arg constructor** automatically; `new` allocates the object on the heap and `obj` is assigned its reference.
- **Line 8** — prints the default-initialized state: `id=0, name=null, salary=0.0`.
- **Lines 9–10** — field assignment directly on the object (legal because the fields are package-private, not `private`, and `ClassDemo` is in the same package). This is exactly the anti-pattern Module 13 (Encapsulation) later argues against.
- **Line 11** — commented `obj.salary = 10.75;` — left unset deliberately, so line 12's print still shows `salary=0.0`, illustrating that fields not explicitly assigned keep their default.
- **Line 13** — `Employee.officePhone` — accessed through the **class name**, not an object reference, because it's `static`; using `obj.officePhone` would also compile but is discouraged/misleading since it implies per-object state that doesn't exist.
- **Lines 15–20** — a second, fully independent `Employee` object (`obj2`) is created and populated — demonstrating that each object has its own instance-field state (`obj`'s fields are unaffected by `obj2`'s assignments) while `officePhone` (static) would be shared and identical if printed from either.

### Code — `day1/constructor/Employee.java` and `day1/constructor/ConstructorDemo.java` (constructor overloading, chaining side effect via `super()`)

```java
1:  package com.acme.demo.day1.constructor;
2:  
3:  public class Employee {
4:  
5:  	int id;
6:  	String name;
7:  	double salary;
8:  
9:  	public Employee() {
10: 		super();
11: 		System.out.println("Default constructor");
12: 	}
13: 
14: 	public Employee(int id, String name) {
15: 		super();
16: 		System.out.println("2 args constructor");
17: 		this.id = id;
18: 		this.name = name;
19: 	}
20: 
21: 	public Employee(int id, String name, double salary) {
22: 		super();
23: 		System.out.println("All args constructor");
24: 		this.id = id;
25: 		this.name = name;
26: 		this.salary = salary;
27: 	}
28: 
29: 	@Override
30: 	public String toString() {
31: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
32: 	}
33: 
34: }
```

- **Lines 9–12, 14–19, 21–27** — three **overloaded constructors**: no-arg, two-arg (`id`, `name`), and three-arg (`id`, `name`, `salary`). The compiler picks the right one based on the argument list at each `new Employee(...)` call site — this is compile-time (static) polymorphism.
- **Line 10, 15, 22** — `super();` explicitly calls `Object`'s no-arg constructor. This is what the compiler inserts automatically anyway when a constructor doesn't start with `this(...)` or `super(...)`; writing it explicitly here is purely for teaching visibility into what's implicit.
- **Line 17–18, 24–26** — `this.id = id;` etc. — `this.` disambiguates the field from the identically-named constructor parameter; without `this.`, `id = id;` would just assign the parameter to itself and leave the field untouched.
- **Line 11, 16, 23** — each constructor prints a distinct message, so running `ConstructorDemo` makes visible, at runtime, exactly which overload the compiler selected for each `new` call.

```java
1:  package com.acme.demo.day1.constructor;
2:  
3:  public class ConstructorDemo {
4:  
5:  	public static void main(String[] args) {
6:  
7:  		Employee emp1 = new Employee();
8:  		emp1.id = 1;
9:  		emp1.name = "Sonu";
10: 		emp1.salary = 10.75;
11: 		System.out.println(emp1.toString());
12: 		
13: 		Employee emp2 = new Employee();
14: 		emp2.id = 2;
15: 		emp2.name = "Monu";
16: 		emp2.salary = 11.25;
17: 		System.out.println(emp2.toString());
18: 
19: 		Employee emp3 = new Employee(3, "Tonu", 12.50);
20: 		System.out.println(emp3.toString());
21: 
22: 		Employee emp4 = new Employee(4, "Tonu");
23: 		System.out.println(emp4.toString());
24: 
25: 	}
26: }
```

- **Line 7** — `new Employee()` invokes the no-arg overload → prints `"Default constructor"`, then fields are set individually via direct field access on lines 8–10 (again legal only because the fields are package-private, in the same package).
- **Line 19** — `new Employee(3, "Tonu", 12.50)` matches the three-arg constructor by argument count/types → prints `"All args constructor"` and all three fields are set in one call.
- **Line 22** — `new Employee(4, "Tonu")` matches the two-arg overload → prints `"2 args constructor"`; note `salary` is left at its default `0.0` since that constructor never touches it.
- This file demonstrates overload resolution concretely: the exact same class exposes three different construction "shapes," and the compiler statically picks the matching one at each call site based purely on argument signature.

### Code — `day1/methods/MethodDemo.java` (instance vs static methods)

```java
1:  package com.acme.demo.day1.methods;
2:  
3:  public class MethodDemo {
4:  
5:  	// instance method == objectName.methodName();
6:  	void printNums() {
7:  		for (int i = 1; i <= 5; i++)
8:  			System.out.println(i);
9:  	}
10: 
11: 	// static method == ClassName.methodName();
12: 	static void printNums2() {
13: 		for (int i = 1; i <= 5; i++)
14: 			System.out.println(i);
15: 	}
16: 
17: 	public static void main(String[] args) {
18: 
19: 		MethodDemo obj = new MethodDemo();
20: 		obj.printNums(); // works
21: 		MethodDemo.printNums2(); // works
22: //		MethodDemo.printNums(); // CE 
23: //		obj.printNums2(); // warning 
24: 
25: //		printNums2();
26: 
27: 	}
28: 
29: }
```

- **Line 6–9** — `printNums()` is an **instance method** (no `static`); it must be called on an object (`obj.printNums()`), because conceptually it could reference `this`/instance fields even though this particular one happens not to.
- **Line 12–15** — `printNums2()` is `static`; it belongs to the class itself and is called via the class name.
- **Line 19–20** — `obj.printNums()` — correct, calling an instance method on an object.
- **Line 21** — `MethodDemo.printNums2()` — correct, calling a static method via the class name.
- **Line 22** — commented `MethodDemo.printNums(); // CE` — a genuine **compile error**: you cannot call an instance method through the class name alone, because there is no object context (no `this`) for the JVM to bind to.
- **Line 23** — commented `obj.printNums2(); // warning` — this actually **compiles**, but the compiler emits a warning, because calling a static method via an object reference is misleading (it doesn't use `obj`'s state at all — it's resolved purely by `obj`'s declared/compile-time type). Idiomatic Java always calls static members via the class name.
- **Line 25** — commented `printNums2();` — calling a static method "bare" (unqualified) *would* work here **only** because it's being called from within the same class (`main` is in `MethodDemo` too) — this shorthand is legal but the explicit `MethodDemo.printNums2()` form is clearer.

### Code — `day1/object/Employee.java` and `day1/object/ObjectDemo.java` (bare object creation)

```java
1:  package com.acme.demo.day1.object;
2:  	
3:  public class Employee {
4:  	
5:  	int id;
6:  	String name;
7:  	double salary;
8:  	
9:  
10: }
```

- A minimal POJO: three package-private instance fields with no constructors, methods, or `toString()` — the simplest possible class shape, used here purely to demonstrate object instantiation mechanics in `ObjectDemo`.

```java
1:  package com.acme.demo.day1.object;
2:  	
3:  public class ObjectDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		Employee emp = new Employee();
7:  		emp.salary = 10.25;
8:  		
9:  	}
10: 
11: }
```

- **Line 6** — `new Employee()` — the compiler-supplied default constructor runs (since none is defined), allocating a new `Employee` object on the heap with all fields at their defaults.
- **Line 7** — `emp.salary = 10.25;` — direct field mutation through the reference; the object now has `salary = 10.25` while `id` and `name` remain at their defaults (`0` and `null`). Nothing is printed in this file — it exists purely to demonstrate the mechanics of creating and mutating an object's state.

---

## 9. Access Modifiers and Packages

Java has four access levels, from tightest to widest: **`private`** (class only), **default/package-private** (no keyword — same package only), **`protected`** (same package + subclasses in *any* package), **`public`** (everywhere). The standard practice: fields `private` (forces access through methods, the foundation of encapsulation), getters/setters and constructors `public`, internal helper methods `private`, constants `public static final`. A **top-level class** can only be `public` or default — `private`/`protected` are illegal on top-level classes (only on nested classes, Module 15); exactly one `public` class is allowed per file, and it must match the filename.

A **package** is a namespace that avoids class-name collisions and organizes related classes; the `package` statement must be the very first non-comment line, and its dotted name must mirror the on-disk directory structure. Convention: all-lowercase, reverse-domain style (`com.ems.bean`). To use a class from another package you either `import` it (single-class or wildcard `import pkg.*;` — wildcard does **not** pull in sub-packages) or reference its **fully qualified name** inline. **Static imports** (`import static java.lang.Math.PI;`) let you use static members without the class-name prefix, but are best used sparingly since they obscure where a symbol comes from. `java.lang` is the only auto-imported package.

### Code — `day1/modifiers/FinalDemo.java`

```java
1:  package com.acme.demo.day1.modifiers;
2:  
3:  public class FinalDemo {
4:  
5:  	static int staticField;
6:  	int instanceField;
7:  	static final int NUM_VALUE = 30;
8:  
9:  	public static void main(String[] args) {
10: 
11: //		FinalDemo.staticField = 10;
12: //		System.out.println(FinalDemo.staticField);
13: //		FinalDemo obj = new FinalDemo();
14: //		obj.instanceField = 20;
15: //		System.out.println(obj.instanceField);
16: //		System.out.println(FinalDemo.NUM_VALUE);
17: ////		FinalDemo.NUM_VALUE = 35; // CE 
18: //		System.out.println(FinalDemo.NUM_VALUE);
19: 		
20: 		System.out.println(Integer.BYTES);
21: 		System.out.println(Integer.SIZE);
22: 		System.out.println(Integer.MIN_VALUE);
23: 		System.out.println(Integer.MAX_VALUE);
24: 
25: 	}
26: 
27: }
```

- **Line 5** — `static int staticField;` — package-private static field, one copy per class, defaults to `0`.
- **Line 6** — `int instanceField;` — package-private instance field, one copy per object.
- **Line 7** — `static final int NUM_VALUE = 30;` — a **constant**: `static` (one copy, class-level) plus `final` (cannot be reassigned once initialized). This is the idiomatic shape for named constants in Java (analogous to `Integer.MAX_VALUE` used further below).
- **Line 11–15** (commented) — demonstrate normal static/instance field access: `FinalDemo.staticField = 10;` via class name, versus `obj.instanceField = 20;` which requires an object because it's per-instance state.
- **Line 17** — commented `FinalDemo.NUM_VALUE = 35; // CE` — a genuine compile error the instructor is flagging: `final` fields, once assigned, can never be reassigned; attempting to do so is caught at compile time, not runtime.
- **Lines 20–23** — the live code: `Integer.BYTES` (`4`, the number of bytes an `int` occupies), `Integer.SIZE` (`32`, the number of bits), `Integer.MIN_VALUE`/`MAX_VALUE` (`-2147483648`/`2147483647`) — all of these are themselves `public static final` constants defined inside the `Integer` wrapper class, making this a live illustration of the exact same `static final` pattern just discussed above, but from the JDK's own source.

### Code — `day1/modifiers/package1/SpecifierDemo.java`

```java
1:  package com.acme.demo.day1.modifiers.package1;
2:  
3:  public class SpecifierDemo {
4:  
5:  	public static int num1 = 10;
6:  	protected static int num2 = 20;
7:  	/*default*/ static int num3 = 30;
8:  	private static int num4 = 40;
9:  
10: 	public static void main(String[] args) {
11: 		
12: 		System.out.println(SpecifierDemo.num1);
13: 		System.out.println(SpecifierDemo.num2);
14: 		System.out.println(SpecifierDemo.num3);
15: 		System.out.println(SpecifierDemo.num4);
16: 
17: 	}
18: 
19: }
```

- **Lines 5–8** — one field for each of the four access levels, all otherwise identical (`static int`): `public num1`, `protected num2`, package-private `num3` (the `/*default*/` comment is just documentation — there is no `default` keyword for member access; omitting a modifier *is* default access), and `private num4`.
- **Lines 12–15** — all four prints succeed here, because they're accessed from **inside the declaring class itself** — `private` is always visible within its own class, and the wider modifiers are trivially visible too. This file alone cannot demonstrate the *restrictions*; that requires the companion files below.

### Code — `day1/modifiers/package1/WithinPackage.java` (same package, different class)

```java
1:  package com.acme.demo.day1.modifiers.package1;
2:  
3:  public class WithinPackage {
4:  
5:  	public static void main(String[] args) {
6:  
7:  		System.out.println(SpecifierDemo.num1);
8:  		System.out.println(SpecifierDemo.num2);
9:  		System.out.println(SpecifierDemo.num3);
10: //		System.out.println(SpecifierDemo.num4); // CE
11: 
12: 	}
13: }
```

- **Line 1** — same package (`package1`) as `SpecifierDemo`, but a **different class**.
- **Line 7** — `num1` (public) — accessible, as expected.
- **Line 8** — `num2` (protected) — accessible, because `protected` grants access to the whole package, not just subclasses.
- **Line 9** — `num3` (default/package-private) — accessible, because `WithinPackage` is in the same package.
- **Line 10** — commented `num4 // CE` — genuine compile error: `private` restricts access to the *declaring class only* (`SpecifierDemo`), so even another class in the same package cannot reach it. This line is the concrete proof of `private`'s "class-only" boundary.

### Code — `day1/modifiers/package2/OutsidePackage.java` (different package, not a subclass)

```java
1:  package com.acme.demo.day1.modifiers.package2;
2:  
3:  import com.acme.demo.day1.modifiers.package1.SpecifierDemo;
4:  
5:  public class OutsidePackage {
6:  	
7:  	public static void main(String[] args) {
8:  
9:  		System.out.println(SpecifierDemo.num1);
10: //		System.out.println(SpecifierDemo.num2);
11: //		System.out.println(SpecifierDemo.num3);
12: //		System.out.println(SpecifierDemo.num4); // CE
13: 
14: 	}
15: 
16: }
```

- **Line 1** — declared in `package2`, a sibling package to `package1` — no inheritance relationship to `SpecifierDemo` exists.
- **Line 3** — `import` is required here because `SpecifierDemo` lives in a different package; unlike `WithinPackage.java`, this class cannot see `SpecifierDemo` "for free."
- **Line 9** — `num1` (public) — the only member that remains accessible: `public` is visible everywhere, unconditionally.
- **Lines 10–12** (all commented) — `num2` (protected), `num3` (default), and `num4` (private) are **all** inaccessible here: `protected` fails because `OutsidePackage` is neither in the same package nor a subclass of `SpecifierDemo`; default fails because it's a different package; `private` fails for the same reason it always fails outside the declaring class. This file, read alongside `WithinPackage.java`, is the complete matrix proof of the access-modifier visibility table from the courseware.

### Code — `day2/commons/packages/PackageDemo.java` (importing JDK packages)

```java
1:  package com.acme.demo.day2.commons.packages;
2:  
3:  import java.util.Random;
4:  import java.util.Scanner;
5:  
6:  public class PackageDemo {
7:  
8:  	public static void main(String[] args) {
9:  
10: //		Scanner sc = new Scanner(System.in);
11: //		System.out.println("Enter:");
12: //		int num = sc.nextInt();
13: //		System.out.println(num);
14: //		sc.close();
15: 
16: //		Random random = new Random();
17: //		int num = random.nextInt(1000, 9999); // 4 digit otp 
18: //		System.out.println(num);
19: 
20: 	}
21: 
22: }
```

- **Lines 3–4** — explicit `import java.util.Random;` and `import java.util.Scanner;` — both required because `java.util` is not auto-imported (only `java.lang` is), reinforcing the module's point about needing explicit imports for anything outside `java.lang`.
- **Lines 10–14** (commented) — a `Scanner`-based numeric input pattern: `sc.nextInt()` reads an `int` token from the console (contrast with `ScannerDemo.java`'s `sc.next()`, which reads a `String`).
- **Lines 16–18** (commented) — `random.nextInt(1000, 9999)` (the two-argument overload, Java 17+) generates a random `int` in `[1000, 9999)` — illustrated here as a 4-digit OTP generator, a realistic use of an imported utility class.
- The whole `main` body is commented out; this file exists to show *which imports are needed* for these two common `java.util` classes rather than to execute anything at runtime.

---

## 10. Inheritance

Inheritance lets a class (**subclass**/child) acquire the fields and methods of another (**superclass**/parent) via `extends`, avoiding duplication when classes share state/behavior — the design litmus test is an **IS-A** relationship (`Manager` IS-A `Employee`, checkable with `instanceof`), as opposed to **HAS-A** (composition — prefer this when the relationship isn't a true specialization). **`super`** has two uses: `super(...)` calls the parent constructor and *must* be the first statement in the child constructor (if omitted, Java inserts a no-arg `super()` automatically — which fails to compile if the parent has no no-arg constructor); `super.method()` invokes the parent's version of an overridden method from inside the override.

**Method overriding**: a subclass redefines an inherited method with an identical signature (and same or covariant return type, same-or-wider access modifier); always mark it `@Override` so the compiler verifies a genuine override is happening. Overriding is resolved at **runtime** (dynamic dispatch, Module 11) versus overloading's **compile-time** resolution — this distinction is one of the most commonly tested facts in Java assessments. **`final`** applied to a method blocks overriding, applied to a class blocks subclassing entirely (`String` is `final`), and applied to a field/variable blocks reassignment after first initialization.

Java supports single, multilevel (`A→B→C`), and hierarchical (multiple children from one parent) inheritance for classes, but explicitly **not multiple inheritance of classes** (`class X extends A, B` is illegal) — this avoids the Diamond Problem; interfaces (Module 12) fill that gap instead. Constructor chaining always runs bottom of the hierarchy last: `Object() → Employee() → Manager()`, top-down. Every class implicitly extends `java.lang.Object` if it declares no `extends` clause, inheriting `toString()`, `equals()`, `hashCode()`, `getClass()`, `clone()`, `wait/notify/notifyAll`, and the deprecated `finalize()` (all detailed in Module 14).

### Code — `day2/oop/inheritance/Phone.java` and `InheritanceDemo.java`

```java
1:  package com.acme.demo.day2.oop.inheritance;
2:  
3:  public class Phone {
4:  
5:  }
6:  
7:  class BasicPhone {
8:  
9:  	public void call() {
10: 		System.out.println("calling...");
11: 	}
12: 
13: 	public void sms() {
14: 		System.out.println("texting...");
15: 	}
16: }
17: 
18: class FeaturePhone extends BasicPhone {
19: 
20: 	public void music() {
21: 		System.out.println("playing...");
22: 	}
23: 
24: }
25: 
26: class SmartPhone extends FeaturePhone {
27: 
28: 	@Override
29: 	public void music() {
30: 		System.out.println("playing dolby...");
31: 	}
32: 	
33: 	public void camera() {
34: 		System.out.println("clicking...");
35: 	}
36: 
37: }
```

- **Line 3–5** — `public class Phone {}` — an unused public placeholder class (required so the filename `Phone.java` has a matching public top-level class); the real hierarchy lives in the package-private classes below it in the same file.
- **Lines 7–16** — `BasicPhone` is the root of the hierarchy, with no `extends` clause (implicitly extends `Object`); it defines `call()` and `sms()` as the baseline capability every phone in this hierarchy will have.
- **Line 18** — `class FeaturePhone extends BasicPhone` — **single inheritance**; `FeaturePhone` automatically has `call()` and `sms()` for free and adds `music()` (line 20) as new behavior — this is **multilevel inheritance** in the making, since `SmartPhone` will extend `FeaturePhone` next.
- **Line 26** — `class SmartPhone extends FeaturePhone` — extends `FeaturePhone`, making the full chain `SmartPhone → FeaturePhone → BasicPhone → Object` (**multilevel inheritance**).
- **Line 28–31** — `@Override public void music()` — `SmartPhone` overrides `FeaturePhone`'s `music()` with a more specific implementation ("playing dolby...") rather than inheriting the plain version unchanged; this is method overriding, resolved at runtime.
- **Line 33–35** — `camera()` is new behavior unique to `SmartPhone`, not present anywhere up the chain.

```java
1:  package com.acme.demo.day2.oop.inheritance;
2:  
3:  public class InheritanceDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		
7:  		BasicPhone phone1 = new BasicPhone();
8:  		phone1.call();
9:  		phone1.sms();
10: 		
11: 		FeaturePhone phone2 = new FeaturePhone();
12: 		phone2.call();
13: 		phone2.sms();
14: 		phone2.music();
15: 		
16: 		SmartPhone phone3 = new SmartPhone();
17: 		phone3.call();
18: 		phone3.sms();
19: 		phone3.music();
20: 		phone3.camera();
21: 		
22: 		BasicPhone phone4 = new SmartPhone();
23: 		phone4.call();
24: 		phone4.sms();
25: //		phone4.music(); // CE 
26: //		phone4.camera(); // CE 
27: //		advantages ? 
28: 
29: 		
30: 	}
31: 
32: }
```

- **Lines 7–9** — a plain `BasicPhone` can only `call()`/`sms()` — it has no `music()`/`camera()` because those are defined further down the hierarchy, not up.
- **Lines 11–14** — `FeaturePhone` inherits `call()`/`sms()` from `BasicPhone` and additionally exposes its own `music()`.
- **Lines 16–20** — `SmartPhone` inherits everything from both ancestors and additionally exposes `camera()`, plus its own overridden `music()` (which prints "playing dolby..." rather than the inherited "playing...").
- **Line 22** — `BasicPhone phone4 = new SmartPhone();` — this is **upcasting**: a `SmartPhone` object assigned to a `BasicPhone`-typed reference variable. This is implicit and always safe, since every `SmartPhone` genuinely IS-A `BasicPhone`.
- **Lines 23–24** — `phone4.call()`/`phone4.sms()` work fine — these are declared on `BasicPhone`, visible through the `BasicPhone`-typed reference.
- **Lines 25–26** — commented `phone4.music(); // CE` and `phone4.camera(); // CE` — genuine compile errors: even though the underlying object is a `SmartPhone` with both methods, the **compile-time (declared) type** of `phone4` is `BasicPhone`, and the compiler only allows calling methods that the declared type knows about. To call `music()`/`camera()` here you would need an explicit downcast, `((SmartPhone) phone4).camera();` — this is precisely the upcasting/downcasting distinction from Module 11 (Polymorphism).

---

## 11. Polymorphism

Polymorphism ("many forms") lets one reference type stand for objects of different actual types, with the correct behavior selected automatically. Java has two kinds: **compile-time polymorphism** (method **overloading** — the compiler picks the matching method by argument count/types at the call site, before the program runs; changing *only* the return type does not count as overloading and is a compile error) and **runtime polymorphism** (method **overriding** + **dynamic dispatch** — when a parent-typed reference holds a child object, the JVM looks up the method in the *actual object's* virtual method table at runtime and calls the child's version, not the reference type's).

This is what makes processing heterogeneous collections clean: iterating an `Employee[]` containing `Manager`/`Developer`/`Contractor` objects and calling `e.display()` on each runs each object's own override, with no `if (obj instanceof X)` chain required. **Upcasting** (child → parent reference) is implicit and always safe; **downcasting** (parent → child reference) requires an explicit cast and can throw `ClassCastException` at runtime if the actual object isn't really of that subtype — always guard with `instanceof` first (or use Java 16+'s pattern-matching `instanceof`, `if (e instanceof Manager m) { ... }`, which checks and casts in one step). A subtle trap: calling an overridable method from a parent constructor invokes the **child's** override even though the child's own fields haven't been initialized yet — a source of confusing `null`/default-value bugs; the fix is to only call `private` or `final` methods from constructors.

### Code — `day2/oop/polymorphism/Calc.java` and `PolymorphismDemo.java` (compile-time / overload resolution)

```java
1:  package com.acme.demo.day2.oop.polymorphism;
2:  
3:  public class Calc {
4:  
5:  	public static void addNums(int i, long j) {
6:  		System.out.println(i + j);
7:  	}
8:  
9:  	public static void addNums(long i, int j) {
10: 		System.out.println(i + j);
11: 	}
12: 
13: 	public static void addNums(int i, int j) {
14: 		System.out.println(i + j);
15: 	}
16: 
17: 	public static void addNums(int i, int j, int k) {
18: 		System.out.println(i + j + k);
19: 	}
20: 
21: 	public static void addNums(int i, int j, int k, int l) {
22: 		System.out.println(i + j + k + l);
23: 	}
24: }
```

- **Lines 5–7 vs 9–11** — two overloads that differ only in **parameter order and type** (`(int, long)` vs `(long, int)`) — both are legal and distinct overloads because their parameter signatures are different, even though a call like `addNums(10, 20)` (two `int` literals) is ambiguous between them *unless* an exact `(int, int)` overload also exists to win by best match.
- **Lines 13–15** — `addNums(int, int)` — this is the overload the compiler actually selects for a call like `addNums(10, 20)`, because an exact-type match beats the widening conversions the `(int, long)`/`(long, int)` overloads would require.
- **Lines 17–19, 21–23** — `addNums(int, int, int)` and `addNums(int, int, int, int)` — overloads distinguished purely by **argument count**, the simplest form of overloading.
- This class demonstrates that overload resolution can consider both **arity** (count) and **type/order** of parameters — all decided by the compiler at compile time, never at runtime.

```java
1:  package com.acme.demo.day2.oop.polymorphism;
2:  
3:  public class PolymorphismDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		
7:  		Calc.addNums(10, 20);
8:  		Calc.addNums(10, 20, 30);
9:  		Calc.addNums(10, 20, 30, 40);
10: 		
11: 	}
12: 
13: }
```

- **Line 7** — `Calc.addNums(10, 20)` — two `int` literals exactly match the `addNums(int, int)` overload (line 13 of `Calc.java`), so that one is chosen; output `30`.
- **Line 8** — `Calc.addNums(10, 20, 30)` — three arguments match `addNums(int, int, int)` uniquely; output `60`.
- **Line 9** — `Calc.addNums(10, 20, 30, 40)` — four arguments match `addNums(int, int, int, int)` uniquely; output `100`.
- All three calls are resolved by the compiler purely from the literal argument lists — no object state or runtime type information is involved, which is the defining trait of compile-time (overload) polymorphism versus the runtime (override) polymorphism demonstrated in the Inheritance module's `Phone`/`SmartPhone` example.

---

## 12. Abstraction

Abstraction means exposing *what* something does while hiding *how*. Java offers two tools: **abstract classes** (partial implementation — a mix of abstract methods with no body and concrete methods with full implementations, plus fields/constructors like any class; cannot be instantiated directly with `new`) and **interfaces** (a pure contract — before Java 8, 100% abstract method signatures with implicitly `public static final` fields; a class `implements` — not `extends` — an interface, and can implement **multiple** interfaces, which is how Java works around its "no multiple class inheritance" restriction). Any concrete subclass of an abstract class **must** implement every abstract method it inherits, or itself remain abstract.

Java 8 added **`default`** methods to interfaces (a method with a body that implementing classes inherit as-is or can override) — this let interface authors add new methods without breaking every existing implementer — and **`static`** methods on interfaces (belong to the interface itself, called as `InterfaceName.method()`, never inherited by implementers). Decision rule: choose an **abstract class** when classes share real state and partial implementation (e.g. a common `Employee` base with fields and a shared `applyRaise()`); choose an **interface** when you need to describe a capability that spans otherwise-unrelated class hierarchies (e.g. `Payable` applying to `Employee`, `Vendor`, and `Contractor` alike), or when a class needs to satisfy more than one contract simultaneously.

### Code — `day2/oop/OopDemo.java` (conceptual overview, no runtime logic)

```java
1:  package com.acme.demo.day2.oop;
2:  
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
21: 
22: public class OopDemo {
23: 
24: }
```

- **Lines 3–20** — a Javadoc-style comment block (opens with `/**`, so IDEs would render it as documentation) summarizing all four OOP pillars in the instructor's own words before diving into the abstraction-specific packages below. It calls abstraction "minimum necessary representation" and "hide unnecessary details" — the same framing as the courseware.
- **Line 22** — `public class OopDemo {}` — an empty class body; this file carries no executable logic, serving purely as an anchor point for the comment/overview at the start of the `day2.oop` package.

### Code — `day2/oop/abstractconcrete/AbsDemo.java` (abstract class basics: concrete + abstract methods)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
2:  
3:  // abstract class 
4:  public abstract  class AbsDemo {
5:  
6:  //	concrete method - what to do and how to do both 
7:  
8:  	// method signature - what does this method do ?
9:  	public void doThis() 
10: 	// method body - how does it do it ?
11: 	{
12: 		System.out.println("doing");
13: 	}
14: 
15: //	 abstract method only what to do
16: 	public abstract void doThisToo();
17: }
```

- **Line 4** — `public abstract class AbsDemo` — the `abstract` keyword means this class **cannot** be instantiated with `new AbsDemo()`, even though it has no abstract members forcing that in an obvious way here — any class can be marked `abstract` regardless of whether it declares abstract methods.
- **Lines 8–13** — `doThis()` is a **concrete** method: it has both a signature (what it does) and a body (how it does it) — subclasses inherit this implementation unchanged unless they choose to override it.
- **Line 16** — `public abstract void doThisToo();` — an **abstract method**: signature only, no body, terminated with `;` instead of `{}`. Any concrete (non-abstract) subclass of `AbsDemo` is *required* to provide an implementation for this method, or the compiler will refuse to compile it unless that subclass is itself declared `abstract`.

### Code — `day2/oop/abstractconcrete/Banking.java` (abstract class + multiple interfaces)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
2:  
3:  public class Banking {
4:  
5:  }
6:  
7:  interface StateGovt {
8:  
9:  }
10: 
11: interface GovtOfIndia {
12: 
13: //	public abstract void checkNationality();
14: 	void checkNationality();
15: 
16: }
17: 
18: abstract class Rbi {
19: 
20: 	abstract void doKyc();
21: 
22: 	public abstract void payInterest();
23: 
24: }
25: 
26: class HdfcBank extends Rbi implements GovtOfIndia, StateGovt {
27: 
28: 	@Override
29: 	public void doKyc() {
30: 		System.out.println("Aadhaar KYC");
31: 	}
32: 
33: 	@Override
34: 	public void payInterest() {
35: 		System.out.println("Paying 4% interest");
36: 	}
37: 
38: 	@Override
39: 	public void checkNationality() {
40: 		System.out.println("Is Indian?");
41: 	}
42: }
43: 
44: class IciciBank extends Rbi implements GovtOfIndia {
45: 
46: 	@Override
47: 	public void doKyc() {
48: 		System.out.println("PAN Card KYC");
49: 	}
50: 
51: 	@Override
52: 	public void payInterest() {
53: 		System.out.println("Paying 5% interest");
54: 	}
55: 
56: 	@Override
57: 	public void checkNationality() {
58: 		System.out.println("Is Foreign?");
59: 	}
60: 
61: }
```

- **Lines 3–5** — `public class Banking {}` — again just an anchor public class matching the filename; the meaningful types are the package-private declarations that follow in the same file (legal in Java: a single `.java` file may contain multiple top-level types as long as at most one is `public`).
- **Line 7–9** — `interface StateGovt {}` — an empty **marker interface** (no methods) — it exists purely so classes can `implements` it to signal a category/capability, without requiring any method implementation. Similar in spirit to `java.io.Serializable`.
- **Lines 11–16** — `interface GovtOfIndia` declares `checkNationality()`. Line 13 (commented) shows the fully explicit form `public abstract void checkNationality();`; line 14 is the same declaration written the idiomatic, terse way — **all interface methods are implicitly `public abstract`** unless marked `default`/`static`, so the modifiers are optional and conventionally omitted.
- **Lines 18–24** — `abstract class Rbi` declares two abstract methods: `doKyc()` (line 20, package-private — legal for an abstract method in an abstract class, unlike interface methods which must be public) and `payInterest()` (line 22, explicitly `public abstract`). This mixes access-modifier styles deliberately to show abstract classes are *not* restricted to `public` members the way interfaces are.
- **Line 26** — `class HdfcBank extends Rbi implements GovtOfIndia, StateGovt` — a single class combines **class inheritance** (`extends Rbi`, singular — only one superclass allowed) with **multiple interface implementation** (`implements GovtOfIndia, StateGovt`, comma-separated, any number allowed). `HdfcBank` must implement every abstract method from `Rbi` (`doKyc`, `payInterest`) and every abstract method from `GovtOfIndia` (`checkNationality`); `StateGovt` contributes no obligations since it's empty.
- **Lines 28–41** — `HdfcBank`'s three `@Override` implementations fulfill the contracts from `Rbi` and `GovtOfIndia` respectively, each with bank-specific logic (Aadhaar KYC, 4% interest, nationality check).
- **Line 44** — `class IciciBank extends Rbi implements GovtOfIndia` — a second, independent subclass of the same abstract class and interface, implementing all the same required methods but with entirely different logic (PAN Card KYC, 5% interest) — demonstrating that abstraction lets multiple concrete types share a contract while diverging completely in behavior, which is precisely the "what, not how" principle.
- Note `IciciBank` does **not** implement `StateGovt` — showing that interface implementation is independently chosen per class, not inherited from siblings.

### Code — `day2/oop/abstractconcrete/AbstractAndConcreteDemo.java` (driving the hierarchy)

```java
1:  package com.acme.demo.day2.oop.abstractconcrete;
2:  
3:  public class AbstractAndConcreteDemo {
4:  	
5:  	public static void main(String[] args) {
6:  		
7:  		HdfcBank bank1 = new HdfcBank();
8:  		bank1.doKyc();
9:  		bank1.payInterest();
10: 		bank1.checkNationality();
11: 		IciciBank bank2 = new IciciBank();
12: 		bank2.doKyc();
13: 		bank2.payInterest();
14: 		bank2.checkNationality();
15: 		
16: 		Rbi bank3 = new HdfcBank();
17: 		bank3.doKyc();
18: 		
19: 	}
20: 
21: }
```

- **Line 7** — `new HdfcBank()` — legal because `HdfcBank` is **concrete** (implements every abstract method it inherits); `new Rbi()` or `new GovtOfIndia()` directly would both be compile errors, since abstract classes and interfaces cannot be instantiated.
- **Lines 8–10** — calling all three methods on the `HdfcBank`-typed reference — every method (from both the abstract superclass and the interface) is visible because the reference's declared type is the concrete class itself.
- **Lines 11–14** — the same pattern for `IciciBank`, showing a second, differently-behaving implementation invoked through identical method calls.
- **Line 16** — `Rbi bank3 = new HdfcBank();` — **upcasting** a concrete `HdfcBank` object to its abstract superclass type `Rbi`. This is legal and implicit (an `HdfcBank` IS-A `Rbi`).
- **Line 17** — `bank3.doKyc();` — works, because `doKyc()` is declared on `Rbi` itself; however, through the `Rbi`-typed reference, `bank3.checkNationality()` (from `GovtOfIndia`) would **not** compile — `Rbi`'s declared type has no knowledge of that interface, illustrating again that the compile-time reference type gates which methods are callable, even though the actual runtime object (`HdfcBank`) does implement more.

### Code — `day2/oop/abstraction/AbstractionDemo.java`

```java
1:  package com.acme.demo.day2.oop.abstraction;
2:  
3:  public class AbstractionDemo {
4:  
5:  }
```

- An empty stub class with no members — this file exists as a placeholder/entry point in the dedicated `abstraction` package but carries no logic of its own; the substantive abstraction examples live in the sibling `abstractconcrete` package shown above.

---

## 13. Encapsulation

Encapsulation bundles data and the methods that operate on it, and **controls access** to that data so an object can enforce its own validity — the object should manage its own state, and outside code should only be able to change it through defined methods (which can validate/transform/restrict). Without encapsulation (public fields), any code anywhere can set an object into an invalid state (`salary = -99999`) with no way to prevent it. The standard fix: **private fields + public getters/setters**, with the setters performing validation (throwing `IllegalArgumentException` on bad input, for instance) — this is what actually distinguishes real encapsulation from mechanical getter/setter boilerplate that just re-exposes the field with no added logic.

Encapsulation also lets you shape exactly what's readable/writable: **read-only** fields (getter only, often paired with `final` so the value can only be set once, in the constructor — appropriate for identity fields like an ID), and rarer **write-only** fields (setter only — e.g. a password that's hashed on write and never exposed via a getter). Taken to its logical extreme, an **immutable class** (`final` class, all fields `private final`, no setters, all state set in the constructor, defensive copies of any mutable field on both input and output) can never change after construction — inherently thread-safe and safe to share freely; `String` is Java's canonical immutable class. The **JavaBeans convention** (private fields, public no-arg constructor, `getX()`/`setX()`/`isX()` for booleans) is the standard shape frameworks like Spring and Hibernate expect, since they use reflection to discover and call these methods by name.

### Code — `day2/oop/encapsulation/Employee.java` and `EncapsulationDemo.java`

```java
1:  package com.acme.demo.day2.oop.encapsulation;
2:  
3:  import java.util.Objects;
4:  
5:  public class Employee {
6:  
7:  	private int id;
8:  	private String name;
9:  	private double salary;
10: 
11: 	public Employee() {
12: 		super();
13: 	}
14: 
15: 	public Employee(int id, String name, double salary) {
16: 		super();
17: 		this.id = id;
18: 		this.name = name;
19: 		this.salary = salary;
20: 	}
21: 
22: 	public int getId() {
23: 		return id;
24: 	}
25: 
26: 	public void setId(int id) {
27: 		this.id = id;
28: 	}
29: 
30: 	public String getName() {
31: 		return name;
32: 	}
33: 
34: 	public void setName(String name) {
35: 		this.name = name;
36: 	}
37: 
38: 	public double getSalary() {
39: 		return salary;
40: 	}
41: 
42: 	public void setSalary(double salary) {
43: 		this.salary = salary;
44: 	}
45: 
46: 	@Override
47: 	public int hashCode() {
48: 		return Objects.hash(id, name, salary);
49: 	}
50: 
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
63: 
64: 	@Override
65: 	public String toString() {
66: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
67: 	}
68: 
69: }
```

- **Lines 7–9** — `private int id; private String name; private double salary;` — the core encapsulation move: fields are unreachable from any class other than `Employee` itself, so all access must go through the public methods below.
- **Lines 22–24, 30–32, 38–40** — standard getters, each returning the current field value; no validation needed on read.
- **Lines 26–28, 34–36, 42–44** — standard setters, following the JavaBeans naming convention (`setId`, `setName`, `setSalary`) — note these particular setters have **no validation logic**, which the courseware explicitly flags as the "boilerplate, not real encapsulation" case; they still provide the *structural* benefit (a single choke point where validation could later be added without changing calling code), but as written they're mechanical passthroughs.
- **Line 48** — `Objects.hash(id, name, salary)` — combines all three fields into a single well-distributed hash code; critically, it uses **exactly the same fields** referenced in `equals()`, satisfying the `equals`/`hashCode` contract required for correct behavior in `HashMap`/`HashSet` (see Module 14).
- **Lines 53–58** — the standard four-step `equals()` pattern: same-reference shortcut (`this == obj`), null check, exact-type check via `getClass()` (stricter than `instanceof`, which would also match subclasses), then field-by-field comparison after casting.
- **Line 60–61** — `Objects.equals(name, other.name)` null-safely compares the `String` fields; `Double.doubleToLongBits(salary) == Double.doubleToLongBits(other.salary)` is the correct way to compare `double`s for `equals()` purposes because it treats `NaN`/`-0.0` consistently (unlike a naive `==` on primitive doubles).
- **Line 66** — `toString()` builds a readable field dump, overriding `Object`'s default `ClassName@hashcode` representation.

```java
1:  package com.acme.demo.day2.oop.encapsulation;
2:  
3:  public class EncapsulationDemo {
4:  
5:  	public static void main(String[] args) {
6:  		Employee emp = new Employee();
7:  		System.out.println(emp.toString());
8:  //		emp.salary = 10.25;
9:  		emp.setSalary(10.25);
10: //		System.out.println(emp.salary);
11: 		System.out.println(emp.getSalary());
12: 		System.out.println(emp.toString());
13: 
14: 	}
15: 
16: }
```

- **Line 6** — `new Employee()` — uses the no-arg constructor; all fields sit at their defaults (`id=0`, `name=null`, `salary=0.0`).
- **Line 8** — commented `emp.salary = 10.25;` — this line, if uncommented, would be a **compile error**: `salary` is `private` in `Employee`, and `EncapsulationDemo`, despite being in the same package, cannot reach a `private` member (recall `private` is class-only, stricter than default/package access). This is the direct, concrete proof that encapsulation is being enforced by the compiler, not just convention.
- **Line 9** — `emp.setSalary(10.25);` — the only legal way to change `salary` from outside `Employee`, going through the public setter.
- **Line 10** — commented `System.out.println(emp.salary);` — same compile-error reasoning as line 8, this time for reading rather than writing.
- **Line 11** — `emp.getSalary()` — the only legal way to read `salary` externally.
- **Line 12** — prints the full object state via `toString()`, confirming the salary update actually took effect (`salary=10.25`).

---

## 14. Object Class Methods

Every class in Java implicitly extends `java.lang.Object`, inheriting `toString()`, `equals(Object)`, `hashCode()`, `getClass()`, `clone()`, `wait()/notify()/notifyAll()`, and `finalize()` (deprecated since Java 9). The three you override constantly: **`toString()`** (default is `ClassName@hexHashCode`, useless for debugging — always override it; it's invoked implicitly by `println`, string concatenation, and debuggers), **`equals()`** (default uses `==`, i.e. reference identity — override with the standard four-step pattern: same-reference shortcut, null check, `getClass()` type check, then field-by-field comparison, using `Objects.equals()` for null-safe field comparisons), and **`hashCode()`** — which must **always** be overridden together with `equals()`, following the contract that equal objects (per `equals`) must produce equal hash codes (the converse isn't required — hash collisions between unequal objects are fine). Breaking this contract silently corrupts `HashMap`/`HashSet` lookups: an object added to a `HashSet` may appear "not contained" even when an equals-equivalent object is queried, because the lookup goes to the wrong hash bucket. `Objects.hash(field1, field2, ...)` is the idiomatic way to build a well-distributed hash from multiple fields — always using the *same* fields referenced in `equals()`.

`getClass()` returns the actual runtime `Class` object — stricter than `instanceof` (which also matches subclasses), and the right choice inside `equals()` for exact-type checks. `clone()` (via the `Cloneable` marker interface) produces a **shallow copy** by default — reference fields in the clone still point at the *same* underlying objects as the original, so mutable fields (arrays, collections) need to be cloned individually for a true **deep copy**; in modern practice, a **copy constructor** is usually preferred over `clone()` since it avoids the checked `CloneNotSupportedException`, the marker-interface requirement, and the shallow-copy default. `wait()/notify()/notifyAll()` are low-level thread-coordination primitives that must be called from a `synchronized` context — mostly superseded today by higher-level concurrency utilities (`BlockingQueue`, `CompletableFuture`), but foundational to understanding them. `finalize()` is deprecated and unreliable (no execution guarantee, GC overhead, possible object resurrection) — `AutoCloseable` + `try-with-resources` is the modern, deterministic replacement for cleanup logic.

### Code — `day2/commons/objects/Employee.java` and `ObjectDemo.java`

```java
1:  package com.acme.demo.day2.commons.objects;
2:  
3:  import java.util.Objects;
4:  
5:  public class Employee {
6:  
7:  	int id;
8:  	String name;
9:  	double salary;
10: 
11: 	public Employee() {
12: 		super();
13: 	}
14: 
15: 	public Employee(int id, String name, double salary) {
16: 		super();
17: 		this.id = id;
18: 		this.name = name;
19: 		this.salary = salary;
20: 	}
21: 	
22: 	// getters setters 
23: 
24: 	@Override
25: 	public int hashCode() {
26: 		return Objects.hash(id, name, salary);
27: 	}
28: 
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
41: 
42: 	@Override
43: 	public String toString() {
44: 		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + "]";
45: 	}
46: 
47: }
```

- **Line 3** — `import java.util.Objects;` — the utility class supplying the null-safe `equals()` and multi-field `hash()` helpers used below.
- **Lines 7–9** — fields here are package-private (not `private`, unlike the Encapsulation module's `Employee`) — this file's focus is `Object` method overrides, not access control, so it doesn't bother with getters/setters (see the frank `// getters setters` comment on line 22 marking the omission).
- **Lines 24–27** — `hashCode()` override delegates to `Objects.hash(id, name, salary)`, combining all three fields exactly as `equals()` uses them below — required by the `equals`/`hashCode` contract.
- **Lines 30–40** — the canonical `equals()` implementation: identity shortcut (line 31), null guard (line 33), strict type check via `getClass()` (line 35, stricter than `instanceof`), then a cast and field comparison (lines 37–39) — `id == other.id` for the primitive `int`, `Objects.equals(name, other.name)` for the null-safe `String` comparison, and `Double.doubleToLongBits(...)` comparison for `salary` (the correct way to compare `double` fields in `equals`, since it handles `NaN`/`-0.0` edge cases that plain `==` on primitive doubles would get wrong).
- **Lines 42–45** — `toString()` overridden to produce a readable field dump instead of `Object`'s default hash-based string.

```java
1:  package com.acme.demo.day2.commons.objects;
2:  
3:  public class ObjectDemo {
4:  
5:  	public static void main(String[] args) {
6:  
7:  		Employee emp1 = new Employee(1, "Sonu", 10.25);
8:  		Employee emp2 = new Employee(1, "Sonu", 10.25);
9:  		
10: 		System.out.println(emp1.toString());
11: 		System.out.println(emp2.toString());
12: 		System.out.println(emp1.hashCode());
13: 		System.out.println(emp2.hashCode());
14: 		System.out.println(emp1.equals(emp2));
15: 
16: 	}
17: 
18: }
```

- **Lines 7–8** — `emp1` and `emp2` are constructed with **identical field values** but are two entirely separate objects on the heap (`new` is called twice) — `emp1 == emp2` would be `false`, since `==` compares references.
- **Lines 10–11** — both `toString()` calls print the same formatted string (`Employee [id=1, name=Sonu, salary=10.25]`) because the field *values* are identical, even though the underlying objects are different.
- **Lines 12–13** — `hashCode()` for both objects prints the **same** integer, because `Objects.hash(id, name, salary)` produces the same result for the same field values, regardless of which object instance it's called on — this satisfies the "equal objects must have equal hash codes" contract in advance of the `equals()` check.
- **Line 14** — `emp1.equals(emp2)` prints `true`: because `equals()` was overridden to compare field *content* rather than reference identity, two distinct objects with matching data are correctly reported as equal. Without the override (i.e. relying on `Object`'s default `equals`), this would print `false` even though the data is identical — the exact contrast the courseware's Module 14 uses to motivate overriding `equals()`/`hashCode()` in the first place.

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
     2	
     3	@FunctionalInterface
     4	interface Tax {
     5	
     6		public abstract double gst(double amount);
     7	}
     8	
     9	@FunctionalInterface
    10	public interface Calc {
    11	
    12	public abstract int addNums(int i, int j);
    13	
    14	//	public abstract int subNums(int i, int j);
    15	
    16	}
    17	
    18	class CalcMethods implements Calc {
    19	
    20		@Override
    21		public int addNums(int i, int j) {
    22		return i + j;
    23	}
    24	
    25	//	@Override
    26	//	public int subNums(int i, int j) {
    27	//		return i - j;
    28	//	}
    29	
    30	}
```

- **Line 1** — package declaration; groups this file with the rest of the `day3.inner` demo package.
- **Line 3** — `@FunctionalInterface`: a marker annotation. It doesn't change runtime behavior; it tells the compiler to *verify* the annotated interface has exactly one abstract method, so it can legally be the target type of a lambda expression. This is the tie-in to Module 20 (lambdas) foreshadowed by the inner-classes/anonymous-class discussion.
- **Line 4** — `interface Tax` declared without `public`: package-private visibility, only usable within `com.acme.demo.day3.inner`. Unlike a top-level `public` class, a file can have any number of package-private top-level types alongside one `public` one.
- **Line 6** — `public abstract double gst(double amount);`: interface methods are implicitly `public abstract`; writing the modifiers explicitly here is redundant but harmless and sometimes used for clarity/teaching.
- **Line 9** — same `@FunctionalInterface` marker applied to `Calc`.
- **Line 10** — `public interface Calc`: this one is the file's single `public` top-level type (must match the filename `Calc.java`).
- **Line 12** — declares the sole abstract method `addNums(int, int)` — this is what makes `Calc` a valid functional-interface target for a lambda `(i, j) -> i + j`.
- **Line 14** — commented-out second abstract method `subNums`; if uncommented, `@FunctionalInterface` would cause a **compile error** because the interface would then have two abstract methods — this is the annotation actively doing verification work, illustrating why it's useful (catches an accidental second abstract method early rather than failing mysteriously at lambda-conversion sites).
- **Line 18** — `class CalcMethods implements Calc`: a concrete, package-private, named class providing a traditional implementation (the "Option 1" approach referenced in `InnerDemo.java`, contrasted with anonymous-class and lambda approaches).
- **Lines 20–23** — `@Override public int addNums(...) { return i + j; }`: standard method implementation; `@Override` here lets the compiler confirm this signature actually matches an interface method (protects against typos).
- **Lines 25–28** — commented-out override of the (also commented-out) `subNums`, kept in sync with line 14's commented declaration, showing how the two files evolve together as the demo removes/adds a second interface method.

### `InnerDemo.java`

```java
     1	package com.acme.demo.day3.inner;
     2	
     3	// use abstract method from an interface 
     4	
     5	public class InnerDemo {
     6	
     7		public static void main(String[] args) {
     8	
     9		Tax tax = (amount) -> {
    10			return amount * 1.18;
    11	};
    12		Tax tax2 = amount -> {
    13		return amount * 1.18;
    14	};
    15	
    16	Tax tax3 = amount -> amount * 1.18;
    17	
    18	double finalAmount = tax.gst(100);
    19	System.out.println(finalAmount);
    20	
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
*(The file also retains two fully commented-out earlier drafts below the active code, exploring the same demo — including a version with a method-local class `LocalClass`, a regular inner class `InstanceClass`, and a static nested class `StaticClass` as inert placeholders illustrating the syntax for all four inner-class kinds discussed in the courseware.)*

- **Line 5** — `public class InnerDemo`: the file's public entry-point class, matches filename.
- **Line 7** — `public static void main(String[] args)`: JVM entry point.
- **Line 9** — `Tax tax = (amount) -> { return amount * 1.18; };`: a **lambda expression** assigned to a variable of the functional-interface type `Tax`. Because `Tax` has exactly one abstract method (`gst`), the compiler knows this lambda's parameter list `(amount)` and body implement `gst`. This is functionally equivalent to an anonymous inner class `new Tax() { public double gst(double amount) { return amount * 1.18; } }`, but far more concise — the whole point of Module 15's closing "Modern Perspective" table.
- **Line 12** — same lambda, but parentheses around the single parameter are omitted (`amount -> { ... }`) — legal Java syntax when there is exactly one inferred-type parameter; purely stylistic, same result as `tax`.
- **Line 16** — `Tax tax3 = amount -> amount * 1.18;`: the **expression-lambda** form — no braces, no explicit `return`; the expression's value is implicitly returned. This is the most concise equivalent of the anonymous-class version.
- **Line 18** — `tax.gst(100)` invokes the lambda body, passing `100` (auto-widened to `double`) as `amount`.
- **Line 19** — prints the computed GST-inclusive amount.
- **Lines 21–41** — large commented-out block enumerating three explicit strategies for implementing the `Calc` functional interface side-by-side: **Option 1** uses the named concrete class `CalcMethods` from `Calc.java` (`new CalcMethods()`); **Option 2** uses an **anonymous inner class** (`new Calc() { @Override public int addNums(...) {...} }`) — the direct anonymous-inner-class syntax the courseware describes as "create an object that implements `Calc`, using this body as the implementation"; **Option 3** uses a **lambda** (`(i, j) -> i + j`), the modern one-liner replacement. Keeping all three commented side-by-side is a deliberate teaching device showing the same behavior expressed three different ways with decreasing verbosity.

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
     2	
     3	public enum DayOfWeek {
     4	
     5		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
     6	
     7	}
```

- **Line 1** — package declaration.
- **Line 3** — `public enum DayOfWeek`: declares a new reference type with `enum` instead of `class`. Under the hood the compiler generates a `final class DayOfWeek extends java.lang.Enum<DayOfWeek>` with `private` constructors, so this type can never be subclassed and can never be instantiated with `new` from outside the enum body.
- **Line 5** — the constant list `MONDAY, TUESDAY, ...`: each identifier becomes a `public static final DayOfWeek` singleton instance, in declaration order (this order backs `ordinal()`). No fields/constructor are declared here, so this is the "basic enum" form from the courseware — just type-safe named constants, nothing more. Note only six days are listed (no `SUNDAY`), a data omission in the demo, not a syntax feature.

### `EnumDemo.java`

```java
     1	package com.acme.demo.day3.miscelleneous;
     2	
     3	public class EnumDemo {
     4	
     5		public static void main(String[] args) {
     6	
     7	//		String today = "Monday";
     8	//		today = "Friday";
     9	//		today = "Dryday";
    10	
    11	DayOfWeek today = DayOfWeek.MONDAY;
    12	System.out.println(today);
    13	today = DayOfWeek.FRIDAY;
    14	System.out.println(today);
    15	//		today = "Dryday"; // CE 
    16	
    17	}
    18	
    19	}
```
*(A fully commented-out earlier iteration follows below, exploring `final` static constants — `private final static int NUM`, `private final static Employee EMPLOYEE` — as a contrast point, showing how `final` fields (single-assignment) differ from enums (a fixed, closed set of typed values) even though both involve "constant-like" declarations.)*

- **Line 3** — `public class EnumDemo`: entry-point class matching the filename.
- **Line 5** — `main` method — JVM entry point.
- **Lines 7–9** — commented-out **before** version using a plain `String` for `today`, deliberately including a typo-able value `"Dryday"` that would silently compile (a String accepts any text) — this is the exact "no type safety" problem the courseware calls out as the reason to prefer enums.
- **Line 11** — `DayOfWeek today = DayOfWeek.MONDAY;`: declares a variable of the enum type and assigns the singleton constant `MONDAY`, accessed via the enum type name (mandatory qualification, unlike inside a `switch` case where the type is inferred from context).
- **Line 12** — `System.out.println(today)` implicitly calls `today.toString()`, which for a plain enum defaults to `.name()`, printing `MONDAY` — demonstrating the "readable by default" property versus an int constant printing `1`.
- **Line 13** — reassigns `today` to a different constant, `FRIDAY`. Legal because `today` is a local variable, not `final`; the enum *type* is what's fixed, not this particular reference.
- **Line 14** — prints `FRIDAY`.
- **Line 15** — commented-out `today = "Dryday";` annotated `// CE` (compile error) — the payoff line proving the type-safety claim: unlike the String version above, assigning a String literal to an enum-typed variable does not compile.

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
     2	
     3	import java.util.InputMismatchException;
     4	import java.util.Scanner;
     5	
     6	public class ExceptionDemo {
     7	
     8		public static void main(String[] args) {
     9	
    10	int num3 = 0;
    11	
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
    23	
    24	}
    25	
    26	}
```
*(Below the active code, the file also retains fully commented-out earlier revisions: a version using a plain, non-try-with-resources `Scanner` with manual `sc.close()` in `finally` — the pre-Java-7-style pattern that `try-with-resources` replaces — a version with separate `catch (InputMismatchException e)`, `catch (ArithmeticException e)`, and a trailing `catch (RuntimeException e)` catch-all, illustrating the specific-before-general ordering rule; and an earliest version with no try/catch at all, where an exception would propagate uncaught to the JVM.)*

- **Line 3** — imports `java.util.InputMismatchException`, thrown by `Scanner` when input doesn't match the requested type (e.g., non-numeric text for `nextInt()`); an unchecked `RuntimeException` subtype, so no `throws` declaration is needed.
- **Line 4** — imports `java.util.Scanner`, used to read console input.
- **Line 10** — `int num3 = 0;` declared *outside* the try block, initialized to a default, so it remains accessible (and printable) in `finally` even if an exception aborts the division on line 17.
- **Line 12** — `try (Scanner sc = new Scanner(System.in);)`: **try-with-resources**. `Scanner` implements `Closeable`, so it is automatically closed when the block exits (success, caught exception, or uncaught exception), removing the need for a manual `sc.close()` in `finally`. The trailing semicolon after `Scanner sc = ...` inside the parens is optional/harmless (Java allows a trailing `;` before `)` in a resource list).
- **Lines 13–16** — prompt and read two integers via `sc.nextInt()`; each call can throw `InputMismatchException` if the input token isn't a valid `int`.
- **Line 17** — `num3 = num / num2;`: integer division; if `num2` is `0` this throws `ArithmeticException` ("/ by zero") — note this is *integer* division throwing at runtime, unlike floating-point division which would silently produce `Infinity`/`NaN`.
- **Line 18** — `catch (InputMismatchException | ArithmeticException e)`: **multi-catch** — a single handler for two unrelated exception types, both unchecked, so no `throws` was ever required on `main`.
- **Line 19** — generic recovery message; this swallows the specific cause (both types print the same "Wrong!"), a simplification the courseware would flag as "less informative" than separate catches, though acceptable for this teaching demo.
- **Line 20** — `finally` block: runs unconditionally after the try/catch, whether or not an exception occurred.
- **Line 21** — prints `num3`, which will be `0` if any exception occurred (since line 17 never completed) or the computed quotient otherwise — demonstrating why initializing the variable outside the try is necessary for `finally` to have something valid to print in all cases.

### `NoAgeEligibilityException.java`

```java
     1	package com.acme.demo.day2.exception;
     2	
     3	public class NoAgeEligibilityException extends RuntimeException {
     4	
     5		private static final long serialVersionUID = 628355502492179165L;
     6	
     7	public NoAgeEligibilityException() {
     8	super();
     9	}
    10	
    11	public NoAgeEligibilityException(String message) {
    12	super(message);
    13	}
    14	}
```

- **Line 3** — `public class NoAgeEligibilityException extends RuntimeException`: a **custom unchecked exception** — extending `RuntimeException` (not `Exception`) means callers are *not* forced by the compiler to catch or declare it, matching the courseware's guidance that "unchecked" is right for exceptions representing violated business rules the caller isn't strictly required to handle explicitly (as opposed to genuinely external failures).
- **Line 5** — `private static final long serialVersionUID`: a field the Java serialization mechanism uses to verify a serialized object matches the class version when deserializing; `Throwable` (and hence all exceptions) implements `Serializable`, so IDEs commonly auto-generate this field to silence a compiler warning about missing an explicit version ID. It has no bearing on normal exception-handling behavior.
- **Lines 7–9** — no-arg constructor calling `super()`, i.e., `RuntimeException`'s no-arg constructor (empty message, no cause).
- **Lines 11–13** — a constructor taking a `String message`, forwarding it to `super(message)` so `getMessage()` on a caught instance returns that text — this is the constructor actually used by `ThrowDemo.java`.

### `ThrowDemo.java`

```java
     1	package com.acme.demo.day2.exception;
     2	
     3	public class ThrowDemo {
     4	
     5		public static void main(String[] args) {
     6	System.out.println("Start");
     7	try {
     8	ThrowDemo.checkEligibility(17);
     9	} catch (RuntimeException e) {
    10	e.printStackTrace();
    11	}
    12	System.out.println("End");
    13	}
    14	
    15	static void checkEligibility(int age) {
    16	if (age >= 18) {
    17	System.out.println("Eligible");
    18	} else {
    19	// code
    20	throw new NoAgeEligibilityException("Age is < 18!");
    21	}
    22	
    23	}
    24	
    25	}
```
*(Followed by a fully commented-out earlier draft that throws a plain `RuntimeException` instead of the custom type.)*

- **Line 8** — calls `checkEligibility(17)` inside a `try`, anticipating it might throw.
- **Line 9** — `catch (RuntimeException e)`: catches by the **supertype** `RuntimeException` rather than the specific `NoAgeEligibilityException` — works because `NoAgeEligibilityException` *is-a* `RuntimeException`, demonstrating polymorphic catching; also means this handler catches *any* unchecked exception, looser than ideal per the "catch specific" best practice, but valid.
- **Line 10** — `e.printStackTrace()`: prints the exception type, message, and full call stack to `System.err` — useful for debugging.
- **Line 15** — `static void checkEligibility(int age)`: no `throws` clause needed even though this method `throw`s — because `NoAgeEligibilityException` is unchecked, the compiler does not require declaration, unlike a checked exception which would force a `throws NoAgeEligibilityException` here.
- **Lines 16–17** — normal-path branch when `age >= 18`.
- **Line 20** — `throw new NoAgeEligibilityException("Age is < 18!");`: manually raises the custom exception with an explicit descriptive message, per the courseware's "always provide a clear message" guidance — control immediately exits this method and unwinds to the nearest matching catch (line 9's `main`).

### `ThrowsDemo.java`

```java
     1	package com.acme.demo.day2.exception;
     2	
     3	public class ThrowsDemo {
     4	
     5	public static void main(String[] args) {
     6	System.out.println("Start");
     7	try {
     8	ThrowsDemo.printNums();
     9	} catch (InterruptedException e) {
    10	e.printStackTrace(); // custom handling 
    11	}
    12	System.out.println("End");
    13	}
    14	
    15	public static void printNums() throws InterruptedException {
    16	for (int i = 1; i <= 10; i++) {
    17	Thread.sleep(250);
    18	System.out.println(i);
    19	}
    20	}
    21	}
```
*(Followed by a commented-out earlier draft where `printNums` catches `InterruptedException` internally via its own try/catch around `Thread.sleep`, instead of declaring `throws`.)*

- **Line 9** — calls `printNums()` inside a `try`, required because `printNums` declares a checked exception it doesn't itself handle.
- **Line 10** — `catch (InterruptedException e)`: `InterruptedException` is a **checked** exception (`Exception` subtype, not `RuntimeException`), so the compiler *forces* this catch (or a `throws` re-declaration on `main`) to exist — omitting it entirely would be a compile error. This is the clearest example in the code set of the checked-exception contract in action.
- **Line 16** — `public static void printNums() throws InterruptedException`: the `throws` clause is the **declaration half of the checked-exception contract** — it tells any caller "I might propagate this checked exception; you must handle or re-declare it." Without this clause, line 18's `Thread.sleep(250)` (which itself declares `throws InterruptedException`) would not compile inside this method unless wrapped in a local try/catch.
- **Line 18** — `Thread.sleep(250)`: pauses the current thread ~250ms; a checked-exception-throwing method by design because a thread can be interrupted asynchronously by another thread while sleeping — treated as an external, must-handle condition rather than a bug.
- **Line 19** — prints the loop counter, executing after each successful sleep.

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
     2	
     3	import java.time.LocalDate;
     4	import java.time.Period;
     5	import java.util.*;
     6	import java.util.function.BinaryOperator;
     7	import java.util.function.Supplier;
     8	import java.util.stream.Collectors;
     9	
    10	public class Java8Features {
    11	
    12		public static void main(String[] args) {
    13	
    14	// Lambda Expression
    15	
    16	Runnable r = () -> System.out.println("Hi from Lambda");
    17	
    18	r.run();
    19	
    20	// Stream API
    21	
    22	List<String> names = List.of("Sonu", "Monu", "Tonu", "Ponu");
    23	
    24	//		 option 1 
    25	names.forEach((name) -> {
    26	System.out.println(name);
    27	});
    28	//		option 2 
    29	names.forEach(name -> System.out.println(name));
    30	//		option 3 - method reference 
    31	
    32	names.forEach(System.out::println);
    33	
    34	names.stream().filter(n -> n.startsWith("A")).forEach(System.out::println);
    35	
    36	// map()
    37	
    38	List<String> upper = names.stream().map(String::toUpperCase).collect(Collectors.toList());
    39	
    40	System.out.println(upper);
    41	
    42	// sorted()
    43	
    44	names.stream().sorted().forEach(System.out::println);
    45	
    46	// count()
    47	
    48	long count = names.stream().filter(n -> n.startsWith("A")).count();
    49	
    50	System.out.println(count);
    51	
    52	// Optional
    53	
    54	Optional<String> name = Optional.of("Sonu");
    55	//		name.
    56	
    57	name.ifPresent(System.out::println);
    58	
    59	System.out.println(name.orElse("Unknown"));
    60	
    61	// forEach + Method Reference
    62	
    63	//		names.forEach(System.out::println);
    64	
    65	// Date Time API
    66	
    67	LocalDate today = LocalDate.now();
    68	//		today.
    69	
    70	LocalDate birthday = LocalDate.of(1990, 5, 15);
    71	
    72	Period age = Period.between(birthday, today);
    73	
    74	System.out.println(age.getYears());
    75	
    76	// Default Method
    77	
    78	MyGreeter g = new MyGreeter();
    79	
    80	g.greet();
    81	
    82	// Functional Interface
    83	
    84	Calculator c = (a, b) -> a + b;
    85	
    86	System.out.println(c.add(10, 20));
    87	
    88	// Predicate
    89	
    89	List<Integer> nums = List.of(10, 15, 20, 25);
    90	
    91	nums.stream().filter(n -> n % 2 == 0).forEach(System.out::println);
    92	
    93	// Consumer
    94	
    95	names.forEach(n -> System.out.println("Hello " + n));
    96	
    97	// Supplier
    98	
    99	Supplier<String> s = () -> "Java 8";
   100	
   101	System.out.println(s.get());
   102	
   103	// Binary Operator
   104	
   105	BinaryOperator<Integer> bo = (a, b) -> a * b;
   106	
   107	System.out.println(bo.apply(5, 6));
   108	}
   109	}
   110	
   111	// Default Method Example
   112	
   113	interface Greeter {
   114	
   115	default void greet() {
   116	
   117	System.out.println("Hello!");
   118	}
   119	}
   120	
   121	class MyGreeter implements Greeter {
   122	
   123	}
   124	
   125	// Functional Interface Example
   126	
   127	@FunctionalInterface
   128	interface Calculator {
   129	
   130	int add(int a, int b);
   131	}
```
*(Note: source line numbering has a duplicate `89` in the original file around the `nums` declaration — reproduced exactly as it appears via `cat -n`.)*

**Line-by-line — lambda syntax focus:**

- **Lines 3–8** — Imports. `java.util.function.BinaryOperator` and `Supplier` are imported explicitly (needed as named types), while `java.util.*` wildcard-imports `List`, `Optional`, etc. `java.util.stream.Collectors` is needed for `.collect(...)`.
- **Line 16** — `Runnable r = () -> System.out.println("Hi from Lambda");` — zero-parameter lambda form (`()` is mandatory, unlike Python's implicit no-arg lambda). Target type is `Runnable`, whose single abstract method is `void run()`. The body is a single expression-statement, so no braces/`return` are needed (the method's return type is `void`).
- **Line 18** — `r.run()` — invokes the lambda through the functional interface's abstract method name. This is a key contrast to Python: in Python you'd just call `r()`; in Java you must call whatever single method the target interface declares.
- **Lines 25–27** — `names.forEach((name) -> { System.out.println(name); });` — full-ish form: parenthesized single parameter, braced body. Because the body is one statement, this is functionally identical to the more concise line 29.
- **Line 29** — `names.forEach(name -> System.out.println(name));` — parentheses dropped (single inferred-type parameter), braces/`return` dropped (single expression body) — the idiomatic shorthand.
- **Line 32** — `names.forEach(System.out::println);` — a method reference, semantically equivalent to `name -> System.out.println(name)`. Preferred style in real pipelines.
- **Line 34** — `names.stream().filter(n -> n.startsWith("A")).forEach(System.out::println);` — a `Predicate<String>` lambda (`n -> n.startsWith("A")`) captures nothing from enclosing scope; `n` is the stream element, scoped only to the lambda.
- **Line 38** — `String::toUpperCase` is an unbound instance method reference: the stream element itself becomes the receiver of `toUpperCase()`, equivalent to `s -> s.toUpperCase()`.
- **Line 84** — `Calculator c = (a, b) -> a + b;` — a lambda targeting a *custom* functional interface (`Calculator`, defined at line 127) rather than a built-in one. Types of `a`, `b` are inferred from the interface's `add(int a, int b)` signature — lambdas are inherently tied to a target type and cannot be written standalone (unlike Python lambdas, which are first-class values with no interface requirement).
- **Lines 105–107** — `BinaryOperator<Integer> bo = (a, b) -> a * b; ... bo.apply(5, 6);` — two-parameter, same-input/output-type lambda; invoked via `apply`, inherited from `BiFunction` that `BinaryOperator` specializes.
- No local variable in this file is captured and later reassigned, so the effectively-final rule is never violated or demonstrated here — a conceptual gap between the courseware (which shows a compile-error example) and this working demo file.

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
   129	
   130	int add(int a, int b);
   131	}
```

**Line-by-line — which functional interfaces are used, and why:**

- **Line 16** — `Runnable r = ...` — `Runnable` (from `java.lang`, canonical functional interface: single abstract method `void run()`). Chosen for a task with no input and no output.
- **Line 18** — `r.run()` — you must call the interface's *named* abstract method, not some generic "invoke" syntax. A Python callable is invoked with `r()` regardless of what it is; a Java lambda's invocation syntax is dictated entirely by whichever interface it targets.
- **Line 84** — `Calculator c = (a, b) -> a + b;` — targets the custom `Calculator` interface declared at lines 127–131. The compiler resolves `(a, b) -> a + b` against `int add(int a, int b)`: infers `a`/`b` are `int`, and the expression's value satisfies the return type. Demonstrates that "functional interface" is not restricted to `java.util.function` — any interface, first-party or custom, qualifies as long as it has exactly one abstract method.
- **Lines 99–101** — `Supplier<String> s = () -> "Java 8"; ... s.get();` — `Supplier<T>` chosen because the lambda needs zero inputs and produces one output; `get()` is the interface's abstract method. The demo doesn't show the lazy-evaluation payoff, just basic mechanics.
- **Lines 105–107** — `BinaryOperator<Integer> bo = (a, b) -> a * b; ... bo.apply(5, 6);` — `BinaryOperator<T>` is a `BiFunction<T,T,T>` specialization where all three type parameters collapse to one (`Integer`); invoked via the inherited `apply(T,T)`. Using it instead of a custom interface means the lambda interoperates with any API expecting a `BinaryOperator<Integer>` (e.g., `Stream.reduce`).
- **Lines 127–131** — `@FunctionalInterface interface Calculator { int add(int a, int b); }` — a hand-written single-abstract-method interface. The annotation causes the compiler to error if a second abstract method were later added, catching accidental interface-contract breakage before it silently turns `Calculator` into something no lambda can target. This interface is functionally analogous to `BinaryOperator<Integer>`, illustrating that reaching for `java.util.function` first is usually preferable to writing a redundant custom interface.
- Not shown in this excerpt but present in the full file: `Predicate` usage at line 91 (`n -> n % 2 == 0` passed to `.filter(...)`) and `Consumer` usage at line 95 (`n -> System.out.println("Hello " + n)` passed to `.forEach(...)`) — both implicit, inferred by the target parameter type of `filter`/`forEach` rather than being assigned to an explicitly-typed local variable first.

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

- **Line 8** — imports `Collectors`, required for `.collect(Collectors.toList())` on line 38 — a factory class of ready-made `Collector` implementations, conceptually analogous to how Python's comprehension-target syntax lets you materialize an iterable into a `list`/`set`/`dict`.
- **Line 22** — `List<String> names = List.of(...)` — `List.of` creates an **immutable** list (Java 9+ factory method); the stream source itself doesn't need to be mutable since streams never mutate their source anyway.
- **Line 34** — `names.stream().filter(n -> n.startsWith("A")).forEach(System.out::println);` — `stream()` opens the pipeline (source); `filter` is intermediate (lazy, returns a new `Stream<String>` without evaluating anything yet); `forEach` is the terminal op that triggers iteration. Since no name in this list starts with "A", the pipeline prints nothing — a real but silent runtime outcome, not a bug.
- **Line 38** — `map` is intermediate (1:1 transform, `String → String`); `collect(Collectors.toList())` is terminal and materializes the lazy pipeline into a concrete `List<String>`. Java's equivalent of a Python list comprehension `[s.upper() for s in names]`, but explicit about the two-phase lazy-build/eager-materialize split.
- **Line 44** — `sorted()` with no arguments uses natural ordering, requiring the element type to implement `Comparable` (`String` does, via lexicographic `compareTo`). `sorted()` returns a new stream and doesn't mutate `names` — a direct illustration of the "non-mutating" property.
- **Line 48** — a *fresh* stream is created here (`names.stream()` called again) because streams are single-use; reusing the stream from line 34 would throw `IllegalStateException`. `count()` is terminal and returns `long` (not `int` — Java's counting APIs default to `long` to handle very large collections, unlike Python's `len()` which returns a plain `int`).
- **Line 91** — same filter/forEach pattern applied to `List<Integer>`; note this stays a boxed `Stream<Integer>` rather than an `IntStream` since `nums.stream()` on a `List<Integer>` yields a reference-type stream — the courseware's `mapToInt`/`IntStream` primitive-specialization pattern isn't exercised in this demo file at all.
- Not present in this file: `flatMap`, `groupingBy`/`partitioningBy`, `reduce`, `min`/`max`, `summarizingDouble`, `limit`/`skip`, `peek`, or parallel streams — the demo covers only the basic filter/map/sorted/count/forEach subset of what the courseware teaches.

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
    56	
    57	name.ifPresent(System.out::println);
    58	
    59	System.out.println(name.orElse("Unknown"));
```

**Line-by-line:**

- **Line 54** — `Optional<String> name = Optional.of("Sonu");` — uses `Optional.of`, not `Optional.ofNullable`. Since `"Sonu"` is a compile-time non-null literal, `Optional.of` is the correct/safe choice here — using it on a value that could actually be null would risk an immediate `NullPointerException` at wrap time, defeating the purpose of using `Optional` at all. This line creates a *present* `Optional`.
- **Line 55** — `// name.` — a commented-out IDE autocomplete artifact left in the source (likely someone was exploring the `Optional` API surface via autocomplete and left the trigger character in as a comment). Not executable; signals the file is exploratory/demo code rather than production-quality.
- **Line 57** — `name.ifPresent(System.out::println);` — since `name` is present, executes the `Consumer`, printing `"Sonu"`. `ifPresent` takes a `Consumer<String>`; `System.out::println` is a bound-overload method reference matching the `void accept(String)` shape. No `if (name.isPresent())` guard needed — the point of the functional style.
- **Line 59** — `System.out.println(name.orElse("Unknown"));` — `orElse` returns the contained value directly since `name` is present, printing `"Sonu"` again (not `"Unknown"` — the fallback branch is unreached in this demo). This means the demo never exercises the *empty* path of `Optional`, nor `orElseGet`, `orElseThrow`, `map`, `flatMap`, `filter`, `ifPresentOrElse`, or `stream()` — all covered by the courseware but with no corresponding example in the real code file. For those behaviors, rely on the courseware explanation above only.
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
2:	
3:	public class MultiThread extends Thread {
4:	
5:		public int num;
6:	
7:		@Override
8:		public void run() {
9:			printNums();
10:	}
11:	
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

- **Line 3** — `MultiThread extends Thread`: this is the "extend Thread" style of thread creation. Every `MultiThread` instance *is-a* `Thread`.
- **Line 5** — `num` is an *instance* field, not static — each `MultiThread` object has its own `num`, starting at 0.
- **Lines 7–10** — overrides `Thread.run()`. This is the method the JVM invokes on the new OS thread once `start()` is called. Here it just delegates to `printNums()`.
- **Line 12** — `synchronized` on an *instance* method locks on `this` (the specific `MultiThread` object). Since `MultithreadingDemo` (below) creates three separate `MultiThread` objects (`obj`, `obj2`, `obj3`) and starts each one, each has its own lock — so `synchronized` here does **not** serialize `obj`, `obj2`, and `obj3` against each other; it would only matter if two threads shared the *same* `MultiThread` instance. This is a common misunderstanding: the presence of `synchronized` doesn't automatically mean "all threads wait for each other" — it means "threads sharing this particular object's monitor wait for each other."
- **Line 14** — `num++` — not atomic, but since (per the above) no two threads touch the same object's `num`, there's no actual race in this particular program even though the increment isn't synchronized-safe in the abstract sense.
- **Lines 15–19** — `Thread.sleep(250)` pauses this thread for 250ms between increments (simulates work); the checked `InterruptedException` is caught and `printStackTrace()`'d — note this program does *not* call `Thread.currentThread().interrupt()` to restore the interrupt flag, which is technically an anti-pattern flagged in the courseware (should be corrected in production code, but is common in throwaway demos).
- **Line 20** — prints each `i` value without a newline, so you'll see interleaved digit streams from all three threads running concurrently on the console.
- **Line 22** — prints the final `num` (always 10 for each object, since each thread only touches its own instance's counter across 10 iterations).

### Code — `day3/threads/MultithreadingDemo.java`

```java
1:	package com.acme.demo.day3.threads;
2:	
3:	public class MultithreadingDemo {
4:	
5:		public static void main(String[] args) {
6:	
7:			MultiThread obj = new MultiThread();
8:			obj.start();
9:			MultiThread obj2 = new MultiThread();
10:		obj2.start();
11:			MultiThread obj3 = new MultiThread();
12:		obj3.start();
13:	
14://		for (int i = 1; i <= 10; i++) {
15://			MultiThread obj = new MultiThread();
16://			obj.start();
17://		}
18:	
19:	}
20:	}
```

- **Lines 7–12** — creates three independent `MultiThread` objects and calls `start()` on each. `start()` (not `run()`) is what actually spins up a new OS thread; calling `obj.run()` instead here would execute all three "threads" sequentially on the main thread with no concurrency at all — the single most common mistake this module warns about.
- Because each `obj`, `obj2`, `obj3` is a distinct object, their `synchronized printNums()` locks are independent — all three genuinely interleave their console output.
- **Lines 14–17** — commented-out code showing an alternative that would spin up 10 threads in a loop; left in as a "try this too" exercise.
- `main()` returns immediately after starting the three threads — it does not `join()` them, so the JVM keeps running until all non-daemon threads (including these three) finish on their own.

### Code — `day3/threads/ThreadDemo.java`

```java
1:	package com.acme.demo.day3.threads;
2:	
3:	
4:	public class ThreadDemo {
5:	
6:		public static void main(String[] args) {
7:	
8:			Thread t1 = new Thread(new Worker(), "Sonu");
9:			Thread t2 = new Thread(new Worker(), "Monu");
10:		Thread t3 = new Thread(new Worker(), "Tonu");
11:	
12:		t1.start();
13:			t2.start();
14:			t3.start();
15:	}
16:	}
```

- **Line 8** — `new Thread(Runnable, String)`: this is the "implement Runnable" style — a `Worker` (a plain `Runnable`, not a `Thread` subclass) is wrapped in a `Thread`. The second constructor argument names the thread ("Sonu"), retrievable via `Thread.currentThread().getName()`.
- **Lines 8–10** — three separate `Worker` instances, each wrapped in its own named `Thread`. Each `Worker` here has no shared state, so there's nothing to race on.
- **Lines 12–14** — `start()` on each launches genuine OS-level concurrency; output ordering between "Sonu", "Monu", "Tonu" lines is scheduler-dependent and not guaranteed.

### Code — `day3/threads/Worker.java`

```java
1:	package com.acme.demo.day3.threads;
2:	
3:	class Worker implements Runnable {
4://class Worker extends Thread {
5:	
6:		@Override
7:		public void run() {
8:			method();
9:		}
10:	
11:		public void method() {
12:	
13:			for (int i = 1; i <= 3; i++) {
14:	
15:				System.out.println(Thread.currentThread().getName() + " working...");
16:			}
17:		}
18:	}
```

- **Line 3** — `implements Runnable`, the preferred pattern per the courseware — `Worker` is a pure task, agnostic to how it's actually run (could be handed to a raw `Thread`, an `ExecutorService`, or run synchronously for testing).
- **Line 4** — commented-out alternative showing the class *could* instead extend `Thread`; kept as a teaching contrast.
- **Line 15** — `Thread.currentThread()` returns whichever `Thread` object is executing this code right now — this is how a `Runnable` (which has no `Thread` reference of its own) discovers its own thread's name at runtime. This only works correctly because `ThreadDemo` gave each wrapping `Thread` a distinct name.


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
2:	
3:	import java.util.concurrent.*;
4:	
5:	public class CallableDemo {
6:	
7:	    public static void main(String[] args)
8:	            throws Exception {
9:	
10:	        ExecutorService service =
11:	                Executors.newSingleThreadExecutor();
12:	
13:	        Callable<Integer> task = () -> {
14:	
15:	            return 100;
16:	        };
17:	
18:	        Future<Integer> future =
19:	                service.submit(task);
20:	
21:	        System.out.println(future.get());
22:	
23:	        service.shutdown();
24:	    }
25:	}
```

- **Line 7** — `throws Exception` on `main` is a shortcut so the demo doesn't need to catch `InterruptedException`/`ExecutionException` individually — acceptable in a scratch demo, not in production code where you'd want to handle each distinctly (e.g., restoring the interrupt flag).
- **Line 11** — a single-thread executor: only ever one worker thread, tasks run strictly in submission order.
- **Lines 13–16** — a `Callable<Integer>` lambda; since `Callable`'s single abstract method is `T call() throws Exception`, the lambda body's `return 100;` becomes the implementation of `call()`. Unlike `Runnable`, this compiles specifically because `Callable` declares a return type.
- **Line 19** — `submit(Callable<T>)` returns `Future<Integer>` immediately (non-blocking) — the task is now running (or queued) asynchronously on the executor's thread.
- **Line 21** — `future.get()` blocks the *main* thread until the callable finishes and returns `100`.
- **Line 23** — `shutdown()` is essential; without it, the single-thread executor's worker thread stays alive as a non-daemon thread and the JVM process would hang after `main()` returns.

### Code — `day3/concurency/ConcurrencyDemo.java`

```java
1:	package com.acme.demo.day3.concurency;
2:	
3:	import java.util.ArrayList;
4:	import java.util.List;
5:	import java.util.concurrent.Callable;
6:	import java.util.concurrent.ExecutorService;
7:	import java.util.concurrent.Executors;
8:	import java.util.concurrent.Future;
9:	import java.util.concurrent.TimeUnit;
10:	
11:	public class ConcurrencyDemo {
12:	
13:		public static void main(String[] args) throws Exception {
14:	
15:			Callable<Integer> sumTask = () -> {
16:				int sum = 0;
17:				for (int i = 1; i <= 10; i++)
18:					sum += i;
19:				System.out.println(Thread.currentThread().getName() + " computed: " + sum);
20:				return sum;
21:			};
22:	
23:			// Fixed pool with 3 threads
24:			ExecutorService pool = Executors.newFixedThreadPool(3);
25:	
26:			// Submit multiple tasks, collect futures
27:			List<Future<Integer>> futures = new ArrayList<>();
28:	
29:			for (int i = 0; i < 5; i++) {
30:				futures.add(pool.submit(sumTask));
31:			}
32:	
33:			// Collect all results
34:			int grandTotal = 0;
35:			for (Future<Integer> f : futures) {
36:				grandTotal += f.get(); // blocks per future
37:			}
38:	
39:			System.out.println("Grand total: " + grandTotal); // 55 x 5 = 275
40:	
41:			// invokeAll -- submit all and get all results at once
42:			List<Future<Integer>> all = pool.invokeAll(List.of(sumTask, sumTask, sumTask));
43://		System.out.println(all);
44:			// invokeAny -- return first successful result, cancel others
45:			Integer first = pool.invokeAny(List.of(sumTask, sumTask, sumTask));
46:			System.out.println("First result: " + first);
47:	
48:			pool.shutdown();
49:			pool.awaitTermination(10, TimeUnit.SECONDS);
50:		}
51:	}
```

- **Lines 15–21** — `sumTask` is a reusable `Callable<Integer>` (sums 1..10 = 55). Because it's a lambda referencing no external mutable state, the exact same `sumTask` object can safely be submitted multiple times concurrently — each submission runs the lambda body independently with its own local `sum`.
- **Line 24** — a fixed pool of exactly 3 worker threads: with 5 tasks submitted (line 29–31), at most 3 run at once and the rest queue.
- **Line 27, 29–31** — submits `sumTask` five times, storing each returned `Future<Integer>` in a list without blocking — all five submissions return instantly even though only 3 can execute concurrently.
- **Lines 34–37** — iterates the futures *in submission order* and calls `get()` on each — note this blocks on `futures.get(0)` first even if a later task finishes sooner; this is a common subtlety (`Future.get()` is per-future blocking, not "wait for whichever finishes first").
- **Line 39** — `275` = `55 x 5`, confirming all five tasks completed correctly despite concurrent execution.
- **Line 42** — `invokeAll(Collection<Callable<T>>)` submits a batch and blocks the *calling* thread until every task in the batch is done, returning `List<Future<T>>` all already completed.
- **Line 45** — `invokeAny(Collection<Callable<T>>)` submits the batch, blocks until the first one finishes successfully, returns that raw value directly (not wrapped in `Future`), and best-effort cancels the still-running others — useful when several alternatives could answer and you only need the fastest.
- **Lines 48–49** — proper shutdown sequence: `shutdown()` stops new submissions, `awaitTermination(10, SECONDS)` blocks up to 10s for in-flight tasks to finish before the method returns — this is the "clean shutdown" pattern the courseware calls out explicitly.

### Code — `day3/concurency/ConcurrencyDemo2.java`

```java
1:	package com.acme.demo.day3.concurency;
2:	
3:	import java.util.concurrent.ExecutorService;
4:	import java.util.concurrent.Executors;
5:	
6:	public class ConcurrencyDemo2 {
7:	
8:	    public static void main(String[] args) {
9:	
10:	        ExecutorService service =
11:	                Executors.newFixedThreadPool(3);
12:	
13:	        for (int i = 1; i <= 5; i++) {
14:	
15:	            int taskId = i;
16:	
17:	            service.execute(() -> {
18:	
19:	                System.out.println(
20:	                        "Task " + taskId
21:	                                + " : "
22:	                                + Thread.currentThread().getName()
23:	                );
24:	            });
25:	        }
26:	
27:	        service.shutdown();
28:	    }
29:	}
```

- **Line 15** — `int taskId = i;` copies the loop variable into a new effectively-final local for each iteration. This is required because the lambda on line 17 captures `taskId` by value, and Java lambdas can only capture variables that are effectively final -- capturing the mutating loop variable `i` directly would not compile. This is a very common Java-specific gotcha for developers coming from Python/JS closures, which capture by reference to the enclosing scope rather than requiring immutability.
- **Line 17** — `execute(Runnable)` (not `submit`) -- fire-and-forget: no `Future` is returned, so there's no way to retrieve a result or observe an exception from this lambda if one were thrown (it would propagate to the pool thread's default uncaught-exception handler and typically just print a stack trace to stderr, invisible to the caller).
- **Lines 19–23** — prints which pool thread ("pool-1-thread-N") executed each task; because the pool has 3 threads and 5 tasks are submitted, output order across tasks is not deterministic, and you'll see thread names repeat as the pool reuses freed threads for later tasks.
- **Line 27** — `shutdown()` only, no `awaitTermination()` -- `main()` can return before all tasks finish printing, though the JVM will still wait for the pool's non-daemon threads before actually exiting.


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
2:	
3:	import java.io.FileReader;
4:	
5:	public class IoDemo {
6:	
7:		public static void main(String[] args) {
8:			System.out.println("Start");
9:	
10:		try {
11:				FileReader reader = new FileReader("sample.txt");
12:				int ch;
13:	
14:			while ((ch = reader.read()) != -1) {
15:	
16:				System.out.print((char) ch);
17:				}
18:	
19:			reader.close();
20:	
21:		} catch (Exception e) {
22:				e.printStackTrace();
23:			}
24:			System.out.println("End");
25:	
26:	}
27:	}
```

- **Line 11** — `FileReader` is a character stream opened against the relative path `"sample.txt"` -- relative to the JVM's working directory at launch, *not* the source file's location, a frequent source of `FileNotFoundException` confusion for students.
- **Line 12** — `int ch`, not `char ch`: `Reader.read()` returns an `int` so it can represent both a valid character (0–65535) and the sentinel `-1` for end-of-stream -- a `char` cannot hold `-1`, hence the wider type.
- **Line 14** — the assignment `ch = reader.read()` happens *inside* the while condition; this combines "read the next character" and "check for EOF" into a single idiomatic loop header, a pattern used identically for `BufferedReader.readLine() != null` below.
- **Line 16** — casts the `int` back to `char` for printing -- safe here since we already excluded `-1`.
- **Line 19** — `reader.close()` releases the underlying file handle. Because this is inside the `try` block (not try-with-resources) and *before* the catch, if `read()` throws mid-loop, `close()` is skipped and the handle leaks -- this file demonstrates the *old*, less-safe idiom that Module 25's courseware explicitly says to avoid in favor of `try (FileReader r = ...)`.
- **Line 21** — catches the general `Exception` (covers `FileNotFoundException` at open time and `IOException` during `read()`), which is broad but acceptable for a teaching demo -- production code would typically distinguish these.

### Code — `day3/iosdemo/BufferedDemo.java`

```java
1:	package com.acme.demo.day3.iosdemo;
2:	
3:	import java.io.BufferedReader;
4://import java.io.
5:	import java.io.FileReader;
6:	
7:	public class BufferedDemo {
8:	
9:		public static void main(String[] args) {
10:	
11:		try {
12:				BufferedReader br = new BufferedReader(new FileReader("sample.txt"));
13:	
14:			String line;
15:	
16:			while ((line = br.readLine()) != null) {
17:	
18:				System.out.println(line);
19:				}
20:	
21:			br.close();
22:	
23:		} catch (Exception e) {
24:				e.printStackTrace();
25:			}
26:	}
27:	}
```

- **Line 12** — the Decorator pattern in action: `new BufferedReader(new FileReader("sample.txt"))` -- `FileReader` is the raw character source, `BufferedReader` wraps it to add internal buffering *and* the higher-level `readLine()` method that plain `FileReader` lacks.
- **Line 16** — `br.readLine()` returns a full line (without the line terminator) or `null` at EOF -- contrast with `IoDemo` above, which had to read and reassemble character-by-character; this is the "right way" to read text the courseware advocates for.
- Since `sample.txt` in this repo has no embedded newline, this loop prints exactly one line then terminates.
- **Line 21** — again closes manually rather than via try-with-resources, consistent with `IoDemo`'s older style -- worth mentally rewriting both files as `try (BufferedReader br = new BufferedReader(new FileReader("sample.txt"))) { ... }` as an exercise, since that's the pattern expected in an assessment answer.

### Code — `day3/iosdemo/BufferedWriterDemo.java`

```java
1:	package com.acme.demo.day3.iosdemo;
2:	
3:	import java.io.BufferedWriter;
4:	import java.io.FileWriter;
5:	
6:	public class BufferedWriterDemo {
7:	
8:		public static void main(String[] args) {
9:		
10:		String file = "sample2.txt";
11:	
12:		try {
13:	
14:			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
15:	
16:			bw.write("Sonu");
17:	
18:			bw.close();
19:	
20:		} catch (Exception e) {
21:				e.printStackTrace();
22:			}
23:	}
24:	}
```

- **Line 14** — `new FileWriter(file)` with no second `true` argument means overwrite/truncate mode (not append) -- every run of this program replaces `sample2.txt`'s entire contents; this matches the current contents (`"Sonu"`) seen in the fixture file.
- **Line 16** — `bw.write("Sonu")` buffers the string internally; nothing is guaranteed to reach disk until the buffer is flushed.
- **Line 18** — `bw.close()` implicitly flushes any buffered content before releasing the handle -- this is *why* the write actually lands on disk despite no explicit `flush()` call; forgetting to close a `BufferedWriter` (e.g., if an exception is thrown between `write()` and `close()`) is a classic way to lose data that "should have" been written -- another argument for try-with-resources, which guarantees `close()` (and thus the flush) runs even on an exception path.


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
2:	
3:	import java.util.ArrayList;
4:	
5:	public class CollctionWithGenerics { 
6:	
7:		public static void main(String[] args) {
8:	
9://		ArrayList<String> friends = new ArrayList<String>();
10:		ArrayList<String> friends = new ArrayList<>();
11:	
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
24:	
25:	}
26:	
27:	}
```

- **Line 9 (comment) vs Line 10** — shows the pre-diamond-operator style (`new ArrayList<String>()`, explicit type argument on both sides) versus the Java 7+ diamond operator (`new ArrayList<>()`), which infers the type argument from the declared variable type `ArrayList<String> friends`. Both compile identically; the diamond is the modern idiom.
- **Line 12** — `friends.size()` is `0` right after construction — an empty `ArrayList` is not `null`, it's a valid zero-length container (unlike an uninitialized reference).
- **Line 13** — printing an empty `ArrayList` calls its `toString()`, which produces `[]`.
- **Lines 14–16** — `add(String)` is legal *only* because `friends` is declared `ArrayList<String>` — the type parameter is enforced by the compiler at every call site.
- **Lines 19–21 (commented)** — `friends.add(10.25)` and `friends.add(false)` would each be a **compile error** — `double`/`boolean` are not `String` and won't autobox to one; this is the entire point of the demo — showing that generics catch what pre-generics code would only catch at runtime via `ClassCastException`. `friends.add(null)` would actually *compile and run fine* — `ArrayList` permits `null` elements (unlike the Java 9+ `List.of()` factories) — worth noting as a contrast point.
- **Lines 22–23** — confirms size is still 3 and contents are `[Sonu, Monu, Tonu]` since none of the commented lines executed.

### Code — `day3/collection/CollectionDemo.java`

```java
1:	package com.acme.demo.day3.collection;
2:	
3:	import java.util.ArrayList;
4:	
5:	public class CollectionDemo {
6:	
7:		public static void main(String[] args) {
8:	
9://		String[] str = { "Sonu", "Monu", "Tonu" };
10:	
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
25:	
26:	}
27:	
28:	}
```

- **Line 11** — `ArrayList friends = new ArrayList();` — a **raw type**, deliberately contrasting with the previous file. With no type parameter, the compiler treats every element as `Object`; you lose compile-time type checking entirely and the IDE/compiler will emit an unchecked-operations warning. This still compiles (raw types are legal for backward compatibility with pre-Java-5 code) but is exactly what modern Java style guides forbid.
- **Line 19** — `friends.remove(2)` — this is the **overload trap**: `List.remove(int index)` removes by *position*. Because `friends` holds `String`s, and there's no ambiguity with `remove(Object)` when the argument is a primitive `int` literal, the compiler picks the `int`-index overload — this removes `"Tonu"` (index 2), not an element equal to the integer `2`. If the list held `Integer`s instead, `remove(2)` would still call the index overload (not `remove(Object)` with an autoboxed `2`) — you'd need `remove(Integer.valueOf(2))` or `remove((Object) 2)` to remove *by value*. This exact ambiguity is a classic Java assessment gotcha.
- **Line 22** — `friends.remove("Zonu")` calls the *other* overload, `remove(Object o)`, because the argument is a `String` reference, not an `int`. Since `"Zonu"` was never added, `remove()` returns `false` and the list is unchanged (no exception — removing a non-existent object is a safe no-op, unlike removing an out-of-range index, which *does* throw `IndexOutOfBoundsException`).
- **Trailing block comment** — an alternate/earlier version of the file kept for reference, showing plain array usage and demonstrating that arrays are fixed-size (`str[3] = "Ponu"` would throw `ArrayIndexOutOfBoundsException`) versus mutable-in-place assignment within bounds (`str[2] = "Ponu"`) — left as a contrast against the resizable `ArrayList` above.

### Code — `day3/collection/CollectionIteration.java`

```java
1:	package com.acme.demo.day3.collection;
2:	
3:	import java.util.ArrayList;
4:	import java.util.Iterator;
5:	import java.util.List;
6:	
7:	public class CollectionIteration {
8:	
9:		public static void main(String[] args) {
10:	
11:		List<String> friends = new ArrayList<>();
12:	
13:		friends.add("Sonu");
14:			friends.add("Monu");
15:			friends.add("Tonu");
16:	
17://		iterate  - for loop, for each loop, iterator, forEach 
18:	
19:		System.out.println("List of the friends using forEach method ");
20://		friends.forEach((friend) -> {
21://			System.out.println(friend);
22://		});
23:		
24:		friends.forEach(friend -> System.out.println(friend));
25://		friends.forEach(null);
26:		
27:		System.out.println("List of the friends using iterator method ");
28:		
29:		Iterator<String> it = friends.iterator();
30:		
31:		while (it.hasNext())
32:				System.out.println(it.next());
33:		
34:	}
35:	
36:	}
```

- **Line 11** — declared as the `List<String>` *interface* type rather than `ArrayList<String>` — standard best practice: code against the interface, so the concrete implementation can be swapped (e.g., to `LinkedList`) without touching call sites.
- **Line 17 (comment)** — enumerates the four idiomatic ways to iterate a Java collection: classic indexed `for`, enhanced `for-each`, explicit `Iterator`, and the `forEach(Consumer)` method (Java 8+) — all four are conceptually equivalent to just doing `for x in friends:` in Python, but Java exposes each mechanism as a distinct, separately-named tool, each with different trade-offs (e.g., `Iterator` supports safe removal *during* iteration via `it.remove()`, which `for-each` does not — modifying a list mid-for-each throws `ConcurrentModificationException`).
- **Lines 20–22 (commented)** — the block-lambda form of the same `forEach` call, shown as an equivalent-but-more-verbose alternative to line 24's expression lambda.
- **Line 24** — `friends.forEach(friend -> System.out.println(friend))` — `forEach` takes a `Consumer<String>`; this could be further simplified to a method reference `friends.forEach(System.out::println)`.
- **Line 25 (commented)** — `friends.forEach(null)` would compile (the parameter type accepts a null `Consumer` reference) but throw `NullPointerException` at runtime the moment `forEach` tries to invoke it — a reminder that generic method parameters are still ordinary references that can be null unless guarded.
- **Line 29** — `friends.iterator()` returns an `Iterator<String>` — a cursor object with internal position state, separate from the list itself.
- **Lines 31–32** — the classic `while (it.hasNext()) { ... it.next(); }` idiom: `hasNext()` checks without advancing, `next()` both advances and returns the element — calling `next()` past the end throws `NoSuchElementException`, which is why the `hasNext()` guard is mandatory.

### Code — `day3/collection/CollectionMethods.java`

```java
1:	package com.acme.demo.day3.collection;
2:	
3:	import java.util.ArrayList;
4:	import java.util.LinkedList;
5:	import java.util.List;
6:	
7:	public class CollectionMethods {
8:	
9:		public static void main(String[] args) {
10:	
11:		ArrayList<String> friends = new ArrayList<>();
12:		
13:		List<String> friends2 = new ArrayList<>();
14:			int num = 10;
15:		
16://		friends =  new LinkedList<String>();
17:		friends2 = new LinkedList<>();
18:	
19:		friends.add("Sonu");
20:			friends.add("Monu");
21:			friends.add("Tonu");
22:	
23:		System.out.println(friends);
24:	
25:		@SuppressWarnings("unchecked")
26:			ArrayList<String> friends3 = (ArrayList<String>) friends.clone();
27://		ArrayList<String> friends3 =  new ArrayList<>(friends);
28:	
29:		System.out.println(friends3);
30:	
31:		friends3.add("Ponu");
32:	
33:		System.out.println(friends);
34:			System.out.println(friends2);
35:			System.out.println(friends3);
36://		friends.
37:	
38://		 friends2.clone(); // CE 
39:	}
40:	}
```

- **Lines 11 vs 13** — `friends` is declared as concrete `ArrayList<String>`; `friends2` is declared as interface type `List<String>` — this difference is the whole point of lines 16–17: because `friends2`'s *static* type is the interface `List`, it can be reassigned to *any* `List` implementation, including a `LinkedList` (line 17). `friends`'s static type is the concrete class `ArrayList`, so line 16's commented `friends = new LinkedList<String>();` would be a **compile error** — `LinkedList` is not an `ArrayList`, even though both implement `List`. This is a direct, concrete illustration of "program to the interface" and its practical payoff.
- **Line 25** — `@SuppressWarnings("unchecked")` suppresses the compiler warning generated by the cast on line 26, because `clone()` on a generic collection returns raw `Object` internally — the cast to `ArrayList<String>` is unchecked at the bytecode level (type erasure means the JVM cannot verify it), so the compiler warns even though it's logically safe here.
- **Line 26** — `friends.clone()` performs a **shallow copy**: it creates a new `ArrayList` with the same object references inside — for `String` elements (immutable) this behaves like a deep copy for practical purposes, but if the elements were mutable objects, both lists would still point to the *same* underlying objects, and mutating one would be visible through the other. `clone()` on collections is generally discouraged in modern Java in favor of the copy-constructor idiom shown commented on line 27, `new ArrayList<>(friends)`, which is clearer and doesn't require an unchecked cast.
- **Line 31** — `friends3.add("Ponu")` adds only to the cloned list, not the original — proving `clone()` did produce an independent list *container* (even though shallow at the element level).
- **Lines 33–35** — output confirms `friends` is unaffected by `friends3`'s addition (`[Sonu, Monu, Tonu]` vs `[Sonu, Monu, Tonu, Ponu]`), and `friends2` remains an empty `LinkedList` (`[]`) since nothing was ever added to it.
- **Line 38 (comment)** — `friends2.clone()` would be a compile error because `clone()` is not declared on the `List` interface (it's `protected` on `Object` and only re-exposed as `public` by concrete classes like `ArrayList`) — since `friends2`'s static type is `List`, the compiler has no visibility into `clone()` regardless of the runtime type actually being a `LinkedList`. This reinforces the interface-vs-implementation distinction from the earlier note.

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
2:	
3:	public class GcDemo {
4:	
5:		@Override
6:		protected void finalize() {
7:			System.out.println("GC called");
8:		}
9:	
10:		public static void main(String[] args) {
11:	
12:		GcDemo obj = new GcDemo();
13:			System.out.println(obj.toString());
14:	
15:		obj = null;
16:	
17:		System.gc();
18:	}
19:	}
```

- **Lines 5–8** — overrides `Object.finalize()` (deprecated for removal, and in fact *removed entirely* by Java 18 — this file will not even compile on JDK 18+ without the `-XX` legacy flags, or will simply have the override do nothing depending on the exact JDK). It's shown here purely as a teaching artifact of *how finalize used to work*, immediately followed by the courseware's explicit guidance to never use it in real code.
- **Line 12** — allocates a `GcDemo` object; `obj` is a strong reference held by a local variable in `main()`'s stack frame — a GC root.
- **Line 13** — `obj.toString()` uses the default `Object.toString()` (no override here), producing something like `com.acme.demo.day3.garbage.GcDemo@<hashcode>`.
- **Line 15** — `obj = null` removes the only strong reference to the `GcDemo` instance, making it eligible for collection (assuming nothing else references it, which is true here).
- **Line 17** — `System.gc()` is only a *suggestion* to the JVM to run garbage collection soon — it is **not guaranteed** to run immediately, or at all, or to actually collect the now-eligible object before the program exits. In practice on most JVMs this demo *does* usually print "GC called" because `System.gc()` triggers an eager collection pass in typical implementations, but relying on that behavior is explicitly the anti-pattern the courseware warns against — this file exists to demonstrate the mechanism, not to model good practice.

### Code — `day3/garbage/ReferenceDemo.java`

```java
1:	package com.acme.demo.day3.garbage;
2:	
3:	import java.lang.ref.PhantomReference;
4:	import java.lang.ref.ReferenceQueue;
5:	import java.lang.ref.SoftReference;
6:	import java.lang.ref.WeakReference;
7:	
8:	public class ReferenceDemo {
9:	
10:		public static void main(String[] args) throws InterruptedException {
11:	
12:		// Strong Reference
13:			String name = new String("Hello");
14:			System.out.println("Strong ref: " + name);
15:			name = null; // now eligible for GC
16:			System.gc();
17:			System.out.println("Strong ref set to null -> object may be collected\n");
18:	
19:		// Soft Reference
20:			SoftReference<byte[]> cache = new SoftReference<>(new byte[1024]);
21:	
22:		byte[] data = cache.get(); // returns object if still alive
23:			if (data != null) {
24:				System.out.println("Soft ref - Cache hit: " + data.length + " bytes");
25:			} else {
26:				System.out.println("Soft ref - Cache miss: GC cleared it (low memory)");
27:			}
28:			System.out.println();
29:	
30:		// Weak Reference
31:			String data2 = new String("temporary");
32:			WeakReference<String> weak = new WeakReference<>(data2);
33:	
34:		System.out.println("Weak ref - Before GC: " + weak.get());
35:	
36:		data2 = null; // remove strong reference
37:			System.gc();
38:			Thread.sleep(100); // give GC a moment to run
39:	
40:		System.out.println("Weak ref - After  GC: " + weak.get() + "\n");
41:	
42:		// Phantom Reference
43:			// PhantomReference requires a ReferenceQueue.
44:			// get() ALWAYS returns null - the object is already finalized.
45:			// We detect collection by polling the queue.
46:	
47:		ReferenceQueue<Object> queue = new ReferenceQueue<>();
48:	
49:		Object resource = new Object();
50:			PhantomReference<Object> phantom = new PhantomReference<>(resource, queue);
51:	
52:		System.out.println("Phantom ref - get() before GC: " + phantom.get()); // always null
53:	
54:		resource = null; // remove strong reference
55:			System.gc();
56:			Thread.sleep(100); // give GC time to enqueue the phantom ref
57:	
58:		// Poll the queue to detect that the object has been collected
59:			if (queue.poll() != null) {
60:				System.out.println("Phantom ref - object collected -> safe to run cleanup");
61:			} else {
62:				System.out.println("Phantom ref - object not yet collected");
63:			}
64:	}
65:	}
```

- **Line 13** — `new String("Hello")` deliberately forces a new heap-allocated `String` object (bypassing the string constant pool that a plain `"Hello"` literal would use) — necessary here because the constant pool holds a permanent strong reference to interned literals, which would defeat the entire "make it eligible for GC" demonstration.
- **Line 15** — nulling the only strong reference makes the `String` object eligible; line 16's `System.gc()` is again just a hint, shown for illustration.
- **Line 20** — `SoftReference<byte[]>` wraps a 1KB byte array. The JVM is permitted to clear soft references before throwing `OutOfMemoryError`, but is *not required* to clear them at any particular point otherwise — under normal heap pressure (as in this quick-running demo) the reference almost always survives, so `data` on line 22 is typically non-null and prints "Cache hit."
- **Line 22** — `cache.get()` returns the referent if it's still alive, or `null` if the GC has already cleared it — this null-checking pattern is *mandatory* whenever working with `SoftReference`/`WeakReference`, since the object can vanish between any two lines of your code.
- **Lines 31–32** — `data2` is a strong reference to a fresh `String`; `weak` wraps it in a `WeakReference`, meaning `weak.get()` currently returns the string ("Before GC" line 34 succeeds) but the moment the only strong reference (`data2`) is nulled, `weak` cannot keep the object alive by itself.
- **Line 36** — removes the strong reference; unlike `SoftReference`, a `WeakReference`'s referent is eligible for collection at the *very next* GC cycle with no memory-pressure requirement — this is the defining difference between Soft and Weak.
- **Line 38** — `Thread.sleep(100)` is a pragmatic (not guaranteed-correct) way to give the JVM's background GC thread(s) a moment to actually run after the `System.gc()` hint — there's no hard guarantee 100ms is enough, but it's typically sufficient for a demo. In production code you would never write logic that depends on this timing.
- **Line 40** — after the GC pass, `weak.get()` typically returns `null`, demonstrating that a weak reference alone could not keep the `String` alive.
- **Lines 49–50** — `PhantomReference<Object>` differs fundamentally from Soft/Weak: its `get()` **always returns null**, even while the object is technically still alive — you cannot resurrect or even read the referent through a phantom reference at all. It exists solely to let you know, via the associated `ReferenceQueue`, *when* an object has become phantom-reachable (post-finalization, pre-reclamation) so you can run cleanup logic.
- **Line 52** — confirms `phantom.get()` is `null` even *before* any GC has run — reinforcing that phantom references are categorically different from Soft/Weak, not just "even weaker."
- **Lines 54–58** — after nulling the strong reference and running/waiting for GC, the JVM enqueues the phantom reference into `queue` once the referent has been determined to be unreachable — `queue.poll()` (non-blocking) checks whether that enqueuing has happened yet, which is the *only* way to observe collection through a `PhantomReference`.


---

## 29. Java 13 and 14 Features

**What it teaches:** two non-LTS releases (Sept 2019, Mar 2020) that delivered high day-to-day-impact syntax improvements: text blocks (preview in 13, standardized in 15), switch expressions (preview in 12, standardized in 14), and helpful NullPointerExceptions (Java 14).

**Text blocks (`"""..."""`):** eliminate the old pattern of `"line1\n" + "line2\n"` concatenation with manually escaped quotes. The opening `"""` **must** be followed by a newline (no content on the same line). The compiler computes the *common leading whitespace* across all content lines **and the closing `"""`**, then strips exactly that much from every line — so the position of the closing delimiter directly controls how much indentation survives into the resulting string; putting it at column 0 preserves all leading spaces, while aligning it with the content strips down to that column. A text block ends with a trailing newline if the closing `"""` sits on its own line; put it right after the last character of content to suppress that trailing newline. Three companion `String` methods: `indent(n)` (add indentation), `stripIndent()` (apply the same common-whitespace-stripping algorithm programmatically to any runtime string), `translateEscapes()` (interpret literal `\n`/`\t` sequences as actual control characters). `String.formatted(...)` (Java 15) is the instance-method mirror of `String.format(...)`, and pairs naturally with text blocks for templated JSON/SQL/HTML.

**Switch expressions:** the old `switch` was purely a *statement* — no return value, `break` required per case to prevent fall-through (a notorious bug source), and unusable inline. The new arrow form (`case X -> value;`) is an *expression* — it evaluates to a value, has no fall-through by default, supports comma-separated multiple labels per case (`case "HR", "Finance" -> 10;`), and (for a multi-statement case) uses `yield` inside a block to produce the value (not `return` — `yield` is switch-expression-specific and is not a general control-flow keyword usable elsewhere, e.g. not inside a lambda). The old colon syntax (`case X:`) still works and can also `yield` a value from within a switch expression, but the arrow form is preferred going forward. Critically, a switch **expression** must be exhaustive: `String`/`int` selectors require `default`; an `enum` selector can omit `default` if every constant is covered — and if a new constant is added later without updating the switch, that's now a *compile error*, not a silent runtime gap — a genuinely useful safety net worth calling out on an assessment.

**Helpful NullPointerExceptions (Java 14):** previously an NPE's message was just the bare exception type with no detail about *what* was null. From Java 14 (opt-in via `-XX:+ShowCodeDetailsInExceptionMessages`) and always-on from Java 15, the JVM pinpoints exactly which variable or which link in a method-chain expression (`company.getHeadOffice().getAddress().getCity()`) was the null culprit — no code changes required, purely a diagnostics improvement.

### Code — `day3/features/Java13Features.java`

```java
1:	package com.acme.demo.day3.features;
2:	
3:	public class Java13Features {
4:	
5:	    public static void main(String[] args) {
6:	
7:	        // Text Blocks
8:	
9:	        String html = """
10:	                <html>
11:	                    <body>
12:	                        <h1>Java 13</h1>
13:	                    </body>
14:	                </html>
15:	                """;
16:	
17:	        System.out.println(html);
18:	
19:	        // switch expression
20:	
21:	        String day = "MONDAY";
22:	
23:	        String type = switch (day) {
24:	
25:	        case "SATURDAY", "SUNDAY" -> "Weekend";
26:	
27:	        default -> "Weekday";
28:	        };
29:	
30:	        System.out.println(type);
31:	
32:	        // yield in switch
33:	
34:	        int num = 2;
35:	
36:	        String result = switch (num) {
37:	
38:	        case 1:
39:	            yield "One";
40:	
41:	        case 2:
42:	            yield "Two";
43:	
44:	        default:
45:	            yield "Unknown";
46:	        };
47:	
48:	        System.out.println(result);
49:	
50:	        // String methods
51:	
52:	        String name = "   Java 13   ";
53:	
54:	        System.out.println(name.strip());
55:	
56:	        System.out.println("".isBlank());
57:	
58:	        System.out.println("Java\nPython".lines().count());
59:	
60:	        // File Read/Write (NIO improvements)
61:	
62:	        try {
63:	
64:	            java.nio.file.Path path = java.nio.file.Path.of("demo.txt");
65:	
66:	            java.nio.file.Files.writeString(path, "Hello Java 13");
67:	
68:	            String data = java.nio.file.Files.readString(path);
69:	
70:	            System.out.println(data);
71:	
72:	        } catch (Exception e) {
73:	
74:	            e.printStackTrace();
75:	        }
76:	    }
77:	}
```

- **Lines 9–15** — a text block literal for an HTML fragment. The closing `"""` on line 15 sits at the same indentation column as the content lines (16 spaces before `<html>` etc. and before the closing delimiter itself), so the compiler strips exactly that common leading whitespace from every line — the printed output starts flush at column 0, not indented to match the source code's visual nesting.
- **Line 23** — `String type = switch (day) { ... }` — a switch *expression* (note the trailing semicolon after the closing brace, required for expressions, unlike switch statements).
- **Line 25** — `case "SATURDAY", "SUNDAY" -> "Weekend";` — comma-separated multi-label case; since `day` is `"MONDAY"`, this branch doesn't match.
- **Line 27** — `default -> "Weekday";` executes because `"MONDAY"` isn't Saturday or Sunday — `type` becomes `"Weekday"`.
- **Line 36** — a second switch expression, this time using the **traditional colon syntax** (`case 1:`) combined with `yield` rather than the arrow syntax — demonstrating that `yield` works with both switch forms, though it's required for value-producing colon-style cases (there's no implicit fall-through-avoidance the way arrow-case gives you, so each colon branch must explicitly `yield`).
- **Lines 38–42** — since `num == 2`, execution matches `case 2:` and yields `"Two"`; note there's no `break` needed since `yield` both produces the value and exits the switch expression.
- **Line 54** — `.strip()` (Java 11+) is the Unicode-aware replacement for `.trim()` — removes leading/trailing whitespace including Unicode whitespace characters that `.trim()` doesn't recognize; here it turns `"   Java 13   "` into `"Java 13"`.
- **Line 56** — `"".isBlank()` checks whether a string is empty *or* consists entirely of whitespace — returns `true` for `""`.
- **Line 58** — `.lines()` (Java 11+) splits a string into a `Stream<String>` of its lines without needing a manual `split("\n")`; `.count()` on `"Java\nPython"` yields `2`.
- **Line 64** — `Path.of(...)` (Java 11+) is the modern equivalent of `Paths.get(...)`, both producing a `java.nio.file.Path`.
- **Line 66** — `Files.writeString(path, "Hello Java 13")` (Java 11+) writes the entire string to `demo.txt` in one call, no explicit `Writer`/stream management needed — and matches the fixture file's actual on-disk content (`"Hello Java 13"`, confirmed by inspecting `demo.txt` in the repo).
- **Line 68** — `Files.readString(path)` reads the whole file back into a `String` in one call — the write-then-read round trip on lines 66–68 is why the printed output on line 70 exactly echoes what was just written.

### Code — `day3/features/Java14Features.java`

```java
1:	package com.acme.demo.day3.features;
2:	
3:	public class Java14Features {
4:	
5:	    public static void main(String[] args) {
6:	
7:	        // switch expression
8:	
9:	        int day = 6;
10:	
11:	        String result = switch (day) {
12:	
13:	        case 6, 7 -> "Weekend";
14:	
15:	        default -> "Weekday";
16:	        };
17:	
18:	        System.out.println(result);
19:	
20:	        // Record (Preview in Java 14)
21:	
22:	        Employee e = new Employee(101, "Sonu", 50000);
23:	
24:	        System.out.println(e.id());
25:	
26:	        System.out.println(e.name());
27:	
28:	        System.out.println(e.salary());
29:	
30:	        System.out.println(e);
31:	
32:	        // instanceof pattern matching
33:	
34:	        Object obj = "Java 14";
35:	
36:	        if (obj instanceof String s) {
37:	
38:	            System.out.println(s.toUpperCase());
39:	        }
40:	
41:	        // NullPointerException improvement
42:	
43:	        String str = null;
44:	
45:	        try {
46:	
47:	            System.out.println(str.length());
48:	
49:	        } catch (Exception ex) {
50:	
51:	            ex.printStackTrace();
52:	        }
53:	
54:	        // Helpful JVM info
55:	
56:	        System.out.println(Runtime.version());
57:	    }
58:	}
59:	
60:	// Record Example
61:	
62:	record Employee(int id, String name, double salary) {
63:	}
```

- **Lines 9–16** — an `int`-based switch expression (`day = 6` matches `case 6, 7`), producing `"Weekend"` — the same arrow-switch mechanics as Module 29's text-block example, applied to an `int` selector instead of `String`.
- **Line 62** — `record Employee(int id, String name, double salary) { }` — a compact record declaration at the *file* scope (outside `Java14Features`, so it's a package-private top-level type in this file). Even though this course code was written targeting Java 14 (where records were only a *preview* feature requiring `--enable-preview` to compile/run), the syntax shown is exactly what became standard in Java 16 and is covered in depth in Module 30 below.
- **Line 22** — `new Employee(101, "Sonu", 50000)` invokes the compiler-generated canonical constructor — no explicit constructor was written in the record body.
- **Lines 24–28** — `e.id()`, `e.name()`, `e.salary()` are compiler-generated **accessor** methods — note the naming convention: `id()`, not `getId()`. This is a deliberate, exam-relevant departure from JavaBean getter conventions.
- **Line 30** — `System.out.println(e)` invokes the compiler-generated `toString()`, which prints all components in a standard format: `Employee[id=101, name=Sonu, salary=50000.0]`.
- **Line 36** — `if (obj instanceof String s)` — pattern-matching `instanceof` (preview in Java 14, standard in Java 16): the check and the cast-and-bind happen in one expression; `s` is scoped to the `if`'s true-branch, already typed as `String`, no explicit cast required.
- **Line 43, 47** — deliberately triggers a `NullPointerException` by calling `.length()` on a `null` `String` — this is the exact scenario that Java 14's helpful-NPE feature targets: the exception message (visible in `ex.printStackTrace()`'s output) would specify that `"str"` was null and that `String.length()` couldn't be invoked on it, rather than a bare, contextless `NullPointerException`.
- **Line 56** — `Runtime.version()` returns a `Runtime.Version` object describing the exact running JDK build — useful diagnostic/logging info, unrelated to the NPE feature itself but grouped here as a general "helpful JVM info" showcase.


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
2:	
3:	import java.util.List;
4:	
5:	public class Java17FeaturesIllustrative {
6:	
7:		// Sealed hierarchy -- only these three classes may extend Employee
8:		sealed interface Employee permits Manager, Developer, Contractor { }
9:	
10:		record Manager(int id, String name, double salary, int teamSize) implements Employee {
11:			public Manager {
12:				if (teamSize <= 0)
13:					throw new IllegalArgumentException("Team size must be positive: " + teamSize);
14:			}
15:		}
16:	
17:		record Developer(int id, String name, double salary, String techStack) implements Employee { }
18:	
19:		record Contractor(int id, String name, double dailyRate) implements Employee { }
20:	
21:		static String describe(Employee e) {
22:			return switch (e) {
23:				case Manager m    -> "Manager with team of " + m.teamSize();
24:				case Developer d  -> "Developer working on " + d.techStack();
25:				case Contractor c -> "Contractor at " + c.dailyRate() + "/day";
26:				// no default needed -- Employee is sealed, all permitted types covered
27:			};
28:		}
29:	
30:		static String employeeName(Object obj) {
31:			if (obj instanceof Manager m) {
32:				return m.name();
33:			} else if (obj instanceof Developer d && d.salary() > 80000) {
34:				return d.name() + " (senior)";
35:			}
36:			return "Unknown";
37:		}
38:	
39:		public static void main(String[] args) {
40:	
41:			List<Employee> team = List.of(
42:				new Manager(201, "Ponu", 110000, 8),
43:				new Developer(301, "Monu", 82000, "Java, Spring"),
44:				new Contractor(401, "Gonu", 2500)
45:			);
46:	
47:			for (Employee e : team) {
48:				System.out.println(describe(e) + "  |  name via instanceof: " + employeeName(e));
49:			}
50:		}
51:	}
```

- **Line 8** — `sealed interface Employee permits Manager, Developer, Contractor` — declares a closed type hierarchy: only these three types may ever implement `Employee`. Attempting `class Intern implements Employee` elsewhere in the codebase would be a compile error.
- **Lines 10–15** — `Manager` is a `record` implementing the sealed interface. Records satisfy the sealed hierarchy's implicit "must be final/sealed/non-sealed" requirement automatically, because records are implicitly `final` — they cannot be extended, so no explicit modifier is needed here (unlike an ordinary class permitted by a sealed type).
- **Lines 11–14** — a **compact constructor**: no parameter list restated, just validation logic. If `teamSize <= 0`, it throws before the (compiler-generated) field assignments happen; otherwise, `id`, `name`, `salary`, `teamSize` are assigned automatically to the record's `final` fields exactly as passed in.
- **Line 21** — `describe(Employee e)` takes the sealed interface type — the parameter's declared type is intentionally the closed hierarchy root, which is what lets the switch below be verified exhaustive.
- **Lines 22–27** — a **pattern-matching switch expression** over the sealed type: each `case` matches on runtime type and binds a typed variable (`m`, `d`, `c`) in one step — no casts anywhere. Because the compiler knows `Employee`'s `permits` list exhaustively, **no `default` branch is required or even permitted to be missing** — if a fourth permitted type were added to `Employee` later and this switch weren't updated, the code would fail to compile, not silently misbehave at runtime. This exhaustiveness guarantee is the single biggest practical payoff of combining sealed types with pattern-matching switch.
- **Lines 30–37** — a second, independent illustration of plain `instanceof` pattern matching (not switch-based): line 31's `obj instanceof Manager m` binds `m` only within that `if` branch; line 33 shows the pattern combined with `&&` — `d` is bound by the `instanceof` and immediately usable in the same boolean expression's right-hand side (`d.salary() > 80000`), which only evaluates if the `instanceof` already succeeded (short-circuit `&&` guarantees `d` is non-null and correctly typed by the time it's read).
- **Line 43** — `Developer`'s salary is `82000`, which is `> 80000`, so `employeeName` for this entry returns `"Monu (senior)"` via the line 33 branch.
- **Line 44** — `Contractor` matches neither `instanceof` branch in `employeeName`, so it falls through to `"Unknown"` on line 36 — illustrating that `instanceof`-chain dispatch (unlike the sealed-switch in `describe`) is *not* compiler-verified exhaustive; it's just a sequence of ordinary boolean checks, which is exactly why the switch-based approach above is preferred once your hierarchy is sealed.


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
2:	
3:	import java.util.*;
4:	import java.util.concurrent.*;
5:	
6:	public class Java21FeaturesIllustrative {
7:	
8:		record EmployeeRecord(int id, String name, double salary, String department) { }
9:	
10:		static double fetchSalaryFromDb(int employeeId) throws InterruptedException {
11:			Thread.sleep(100);   // simulate a blocking DB call
12:			return 50000 + (employeeId % 5) * 10000;
13:		}
14:	
15:		static String classify(Object obj) {
16:			return switch (obj) {
17:				case null -> "No data";
18:				case EmployeeRecord(var id, var name, var salary, var dept)
19:						when salary > 80000 -> name + " is senior in " + dept;
20:				case EmployeeRecord(var id, var name, var salary, var dept) -> name + " is staff in " + dept;
21:				default -> "Unrecognized";
22:			};
23:		}
24:	
25:		public static void main(String[] args) throws Exception {
26:	
27:			// Virtual threads: process 1000 "employees" concurrently
28:			long start = System.currentTimeMillis();
29:			List<EmployeeRecord> results = new ArrayList<>();
30:	
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
46:	
47:			// Sequenced collections
48:			LinkedHashMap<Integer, EmployeeRecord> byId = new LinkedHashMap<>();
49:			for (EmployeeRecord e : results) byId.put(e.id(), e);
50:	
51:			System.out.println("First: " + byId.firstEntry().getValue().name());
52:			System.out.println("Last:  " + byId.lastEntry().getValue().name());
53:	
54:			// Record patterns with `when` guard and null case
55:			System.out.println(classify(results.get(0)));
56:			System.out.println(classify(null));
57:		}
58:	}
```

- **Line 8** — `EmployeeRecord`, a plain record used as the payload type throughout — pattern-matched against later via record deconstruction.
- **Line 10** — `fetchSalaryFromDb` sleeps 100ms to simulate a blocking I/O call — chosen deliberately since virtual threads only pay off for exactly this kind of blocking workload.
- **Line 16** — `switch (obj)` over a plain `Object` — legal because `switch` in modern Java can pattern-match against any reference type, not just the traditional `int`/`String`/`enum` selectors.
- **Line 17** — `case null -> "No data";` — explicit `null` handling (Java 21 standard); without this case, passing `null` into this switch would throw `NullPointerException` as it always did pre-21.
- **Lines 18–19** — a **record pattern** destructures `EmployeeRecord` directly into four bound variables (`id`, `name`, `salary`, `dept`) without calling any accessor methods, combined with a **`when` guard** (`salary > 80000`) — both the type match *and* the guard condition must hold for this branch to be selected.
- **Line 20** — a second, un-guarded record pattern for the same type — matches any `EmployeeRecord` that didn't satisfy the guard above; switch pattern matching evaluates cases top-to-bottom, so this "catch the rest of this type" case must come after the guarded one.
- **Line 31** — `Executors.newVirtualThreadPerTaskExecutor()` — every task submitted to this executor gets its own dedicated virtual thread; the `try`-with-resources here relies on `ExecutorService` implementing `AutoCloseable` (Java 19+) — `close()` implicitly calls `shutdown()` and blocks until all tasks complete, so the explicit `shutdown()`/`awaitTermination()` boilerplate from Module 24 is no longer necessary with this modern idiom.
- **Lines 33–38** — submits 1000 tasks, each independently sleeping 100ms inside `fetchSalaryFromDb`. With platform threads and any reasonably-sized fixed pool, 1000 tasks x 100ms would take multiple seconds in batches; with one virtual thread per task, all 1000 sleeps effectively overlap, and the whole loop typically completes in well under a second — the entire point of the demo.
- **Line 48** — `LinkedHashMap` implements `SequencedMap` as of Java 21, so it gains `firstEntry()`/`lastEntry()` for free with no interface change needed at the call site — same class, new capability.
- **Lines 51–52** — `firstEntry()`/`lastEntry()` replace what would previously have required manually iterating the map (or using `entrySet().iterator().next()` for the first entry and a full loop to find the last) — direct O(1)-ish access to the map's encounter-order boundaries.
- **Line 55** — `results.get(0)` is `Emp-1` with salary `50000 + (1 % 5) * 10000 = 60000`, which is *not* `> 80000`, so `classify` falls through the guarded case (line 18–19) to the unguarded one (line 20), printing `"Emp-1 is staff in Dept-1"`.
- **Line 56** — `classify(null)` matches `case null` directly, printing `"No data"` — demonstrating the null-handling feature explicitly rather than relying on it as an edge case.


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
2:	
3:	import java.util.*;
4:	import java.util.stream.*;
5:	
6:	public class Java24FeaturesIllustrative {
7:	
8:		record EmployeeRecord(int id, String name, double salary) { }
9:	
10:		static final ScopedValue<String> APPROVER = ScopedValue.newInstance();
11:	
12:		static class Manager {
13:			private final String name;
14:			private final int teamSize;
15:	
16:			Manager(String rawName, int teamSize) {
17:				var cleanedName = rawName == null ? "Unknown" : rawName.strip(); // runs BEFORE super()
18:				if (teamSize <= 0) throw new IllegalArgumentException("teamSize must be positive");
19:				this.name = cleanedName;   // assigning fields directly since this class has no explicit super here
20:				this.teamSize = teamSize;
21:			}
22:		}
23:	
24:		static void printBatch(List<EmployeeRecord> batch) {
25:			System.out.println("--- Batch (approved by " + APPROVER.get() + ") ---");
26:			batch.forEach(e -> System.out.printf("  %-8s %.2f%n", e.name(), e.salary()));
27:		}
28:	
29:		public static void main(String[] args) {
30:	
31:			List<EmployeeRecord> employees = List.of(
32:				new EmployeeRecord(101, "Sonu", 75000),
33:				new EmployeeRecord(102, "Monu", 82000),
34:				new EmployeeRecord(103, "Tonu", 55000),
35:				new EmployeeRecord(104, "Ponu", 91000),
36:				new EmployeeRecord(105, "Gonu", 68000)
37:			);
38:	
39:			// Stream Gatherers: process in fixed-size batches
40:			ScopedValue.where(APPROVER, "Ponu").run(() -> {
41:				employees.stream()
42:					.gather(Gatherers.windowFixed(2))
43:					.forEach(Java24FeaturesIllustrative::printBatch);
44:	
45:				double totalGross = employees.stream()
46:					.mapToDouble(EmployeeRecord::salary)
47:					.sum();
48:				System.out.printf("Total gross: %.2f (approved by %s)%n", totalGross, APPROVER.get());
49:			});
50:	
51:			// Flexible constructor body -- validation runs before this class's field init
52:			Manager m = new Manager("  Rina  ", 5);
53:			System.out.println("Manager: " + m.name + ", team of " + m.teamSize);
54:		}
55:	}
```

- **Line 10** — `static final ScopedValue<String> APPROVER = ScopedValue.newInstance();` — declared exactly like a `ThreadLocal` would be (`static final`), but a `ScopedValue` has no `set()` method at all; it can only be bound for the duration of a `run()`/`call()` block, enforcing immutability by construction rather than by convention.
- **Lines 16–21** — `Manager`'s constructor demonstrates **flexible constructor bodies** conceptually: line 17 computes `cleanedName` and line 18 validates `teamSize` *before* any field assignment — under the pre-Java-24 rule, if this class extended another class, *no* statement could precede the mandatory `super()` call; Java 24 relaxes that specifically to allow argument preparation/validation ahead of `super()`, as long as `this` isn't referenced before the super call completes. (This particular class has no explicit superclass call to illustrate against since it only implicitly extends `Object`, but the pattern shown -- validate/prepare, then assign -- is exactly the shape JEP 492 unlocks for classes that *do* need to run logic before delegating to a real `super(...)`.)
- **Line 40** — `ScopedValue.where(APPROVER, "Ponu").run(() -> { ... })` binds `APPROVER` to `"Ponu"` for the entire duration of the lambda passed to `run()` -- any code called transitively from within this lambda (including `printBatch`, called indirectly via the method reference on line 43) can read `APPROVER.get()` and will see `"Ponu"`; outside this `run()` block, `APPROVER` is unbound again (calling `.get()` there would throw `NoSuchElementException`).
- **Line 42** — `employees.stream().gather(Gatherers.windowFixed(2))` -- a **Stream Gatherer**, the Java 24 standard feature: `windowFixed(2)` groups the 5-element stream into non-overlapping windows of size 2 -- `[Sonu, Monu]`, `[Tonu, Ponu]`, `[Gonu]` (the last window is short since 5 isn't evenly divisible by 2) -- each window delivered downstream as a `List<EmployeeRecord>`.
- **Line 43** — `.forEach(Java24FeaturesIllustrative::printBatch)` -- each windowed batch is passed to `printBatch`, which itself reads `APPROVER.get()` -- demonstrating that the scoped value correctly propagates into a method called from deep inside a stream pipeline, without threading an `approver` parameter through every method signature manually.
- **Line 48** — after the batched printing, the same lambda also computes and prints a grand total, again reading `APPROVER.get()` successfully since it's still within the `run()` block's dynamic scope.
- **Line 52** — outside the `ScopedValue.where(...).run(...)` block entirely -- `APPROVER` is not referenced here, only the flexible-constructor `Manager` is exercised, keeping the two feature demonstrations cleanly separated.

---

## What's next

Core Java is now fully covered end to end, sourced from your actual courseware and code: setup/JVM/datatypes through OOP, exceptions, inner classes, enums, lambdas/streams/Optional, concurrency, I/O, collections/generics, garbage collection, and Java 13 through 24 features.

Reply "next" and I'll build the same courseware-plus-code-grounded guide for **Maven**.
