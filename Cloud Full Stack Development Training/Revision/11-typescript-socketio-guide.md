# TypeScript & Socket.io — Complete Line-by-Line Guide

This guide is grounded entirely in Aakash's actual course materials: the
`TypeScript_Reference.md` and `socketio-quick-reference.md` courseware, three
TypeScript language-fundamentals demo files (`script.ts`, `ts-classes-etc.ts`,
`ts-interfaces.ts`), a 5-part progressive "EMS" (Employee Management System)
TypeScript project (`ems-ts/`) that builds a single typed domain model
assignment-by-assignment, and two Socket.io demos — a real chat/notifications
module wired into the Express EMS backend (`notifications.js`) and a
standalone demo file (`socket-demo.js`).

Audience note: you already know Java (nominal typing, `private`/`protected`,
interfaces, generics with erasure) and Python (dynamic, duck-typed) well.
Wherever TypeScript's type system does something structurally different from
Java, that's called out explicitly rather than re-explained from scratch.

---

# PART A — TypeScript Language Reference

(Numbered sections below map 1:1 to the sections in `TypeScript_Reference.md`.)

## 1. Getting Started & Project Setup

TypeScript is not a runtime — it's a compiler (`tsc`) that erases types and
emits plain JS. The toolchain from the reference:

```bash
npm install -D typescript ts-node @types/node
npx tsc --init      # creates tsconfig.json
```

- `typescript` — the compiler itself (`tsc`).
- `ts-node` — runs `.ts` files directly (compiles in-memory, no `dist/`
  step) — this is what both the fundamentals demos and the EMS assignments
  are run with (`npx ts-node src/assignment1.ts`).
- `@types/node` — ambient type declarations for Node's built-in globals
  (`process`, `Buffer`, `fs`, etc.) — without this package, TypeScript has no
  idea what `fs.promises.readFile` returns.

**Why TypeScript over plain JavaScript** — this is the whole point of the
course's `script.ts` demo (see Part B), and it boils down to what happens
when you write `let salary;` with no annotation vs. `let salary: number;`:
in the first case TS infers `any` (or complains "implicitly has an 'any'
type" under `strict` mode) and lets you reassign `salary = 'abc'` with zero
warning — the bug only surfaces at runtime, same as plain JS. With an
explicit `number` annotation, `salary = 'abc'` is a **compile-time** error
(`ts(2322)`) — the same class of bug Java's compiler already catches you.
TypeScript brings that guarantee to JS/Node without giving up JS's dynamic
runtime or npm ecosystem.

The reference's recommended Node.js `tsconfig.json` (compare against the
actual `ems-ts/tsconfig.json` walked through in Part C — they differ in
several deliberate ways):

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "NodeNext",
    "moduleResolution": "NodeNext",
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "declaration": true,
    "sourceMap": true,
    "resolveJsonModule": true,
    "forceConsistentCasingInFileNames": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist", "**/*.test.ts"]
}
```

---

## 2. TypeScript Basics & Types

```typescript
let name: string = 'Alice';
let age: number = 30;
let isAdmin: boolean = true;
let score: number | undefined = undefined;

let numbers: number[] = [1, 2, 3];
let names: Array<string> = ['Alice', 'Bob'];

let point: [number, number] = [10, 20];
let user: [string, number, boolean] = ['Alice', 30, true];
const [username, userAge] = user;

enum Direction { Up, Down, Left, Right }
enum Status { Active = 'ACTIVE', Inactive = 'INACTIVE', Pending = 'PENDING' }
const dir: Direction = Direction.Up;
const status: Status = Status.Active;

const enum HttpMethod { GET = 'GET', POST = 'POST', PUT = 'PUT', DELETE = 'DELETE' }

let data: any = JSON.parse(jsonString);

let raw: unknown = fetchData();
if (typeof raw === 'string') raw.toUpperCase();

function fail(msg: string): never {
  throw new Error(msg);
}

function log(msg: string): void {
  console.log(msg);
}

const input = document.getElementById('email') as HTMLInputElement;
const len = (someValue as string).length;
```

- Primitives (`string`, `number`, `boolean`) are lowercase in TS, unlike
  Java's boxed `String`/`Integer`/`Boolean` — TS primitives always map to JS
  primitive values, never wrapper objects.
- **Tuples** (`[number, number]`) have no Java equivalent without a record
  class — TS tuples are just arrays with a fixed length and a per-index type,
  enforced only at compile time (erased at runtime, so `.length` etc. still
  work like a normal array).
- **Enums**: numeric enums (`Direction`) compile to a real JS object with a
  reverse mapping (`Direction[0] === 'Up'`); string enums (`Status`) don't get
  reverse mapping. `const enum` is inlined at compile time — no object is
  emitted at all, closest to Java's compile-time-constant feel, but it means
  the enum values must be knowable at compile time and can't be iterated at
  runtime.
- `any` fully disables type checking for that value (escape hatch — avoid).
- `unknown` is the type-safe counterpart: you can assign anything to it, but
  you cannot call methods on it until you **narrow** it (`typeof raw ===
  'string'`) — this is used deliberately throughout the EMS assignments for
  `catch` blocks (see Assignment 4/5) instead of the old `catch (err: any)`.
- `never` — a function that never returns normally (throws or infinite
  loops). Used for exhaustiveness checks (see discriminated unions).
- `void` — "returns nothing meaningful," distinct from `never`. A `void`
  function *does* return (`undefined`), it just isn't typed to have a
  meaningful value.
- `as` — a type **assertion**, not a conversion. It tells the compiler "trust
  me," it does not change the runtime value at all — unlike Java's cast,
  which can throw a `ClassCastException` at runtime, a bad `as` assertion in
  TS just silently lies to the compiler and can blow up later.

---

## 3. Interfaces and Type Aliases

```typescript
interface User {
  readonly id: number;
  name: string;
  email: string;
  age?: number;
  address?: {
    city: string;
    country: string;
  };
}

interface AdminUser extends User {
  role: 'admin';
  permissions: string[];
}

type StringOrNumber = string | number;
type UserOrAdmin = User | AdminUser;
type ID = string | number;
type Callback<T> = (error: Error | null, result: T) => void;

type Point = { x: number; y: number };
type Point3D = Point & { z: number };
```

- `readonly id` — assignable once (in the object literal / constructor),
  reassignment afterward is a compile error, not a runtime one (unlike
  Java's `final`, which is also compile-enforced but for local
  variables/fields uniformly — TS's `readonly` only exists on interface/class
  members).
- `age?` and `address?` — optional members (`?`), equivalent in spirit to a
  `Optional<Integer>`/nullable field in Java, but TS represents it as
  `T | undefined` under the hood.
- `interface X extends Y` — structural extension; **critical TS-vs-Java
  distinction**: TypeScript is **structurally typed** (duck typing, checked
  at compile time), Java is **nominally typed**. In Java, a class must
  explicitly `implements`/`extends` to be assignable to a type. In TS, *any*
  object with the right shape satisfies an interface — no explicit
  relationship needed. An object literal with `id`, `name`, `email` fields
  satisfies `User` even if it was never declared `: User` anywhere.
- `type` aliases vs `interface`: interfaces can be re-opened/merged
  (declaration merging) and extended; `type` is required for unions,
  intersections, mapped, and conditional types, which interfaces cannot
  express. Rule of thumb from the reference: interface for object shapes,
  type for everything else.
- `Point & { z: number }` — an **intersection type**: the resulting type
  must satisfy *both* sides simultaneously (`x`, `y`, and `z` all present).
  This is TS's version of interface extension via composition rather than
  inheritance.

---

## 4. Classes & Access Modifiers

```typescript
class Animal {
  readonly id: number;
  public name: string;
  protected species: string;
  private #sound: string;

  constructor(name: string, species: string) {
    this.id = Math.random();
    this.name = name;
    this.species = species;
    this.#sound = 'generic';
  }

  speak(): string {
    return `${this.name} says ${this.#sound}`;
  }

  get info(): string { return `${this.name} (${this.species})`; }
  set sound(value: string) { this.#sound = value; }

  static create(name: string, species: string): Animal {
    return new Animal(name, species);
  }
}

class Dog extends Animal {
  constructor(
    name: string,
    public breed: string,
    private readonly age: number
  ) {
    super(name, 'dog');
  }

  speak(): string {
    return `${this.name} barks!`;
  }
}

interface Serializable {
  toJSON(): object;
  toString(): string;
}

class User implements Serializable {
  constructor(public name: string, public email: string) {}
  toJSON() { return { name: this.name, email: this.email }; }
  toString() { return `${this.name} <${this.email}>`; }
}

abstract class Shape {
  abstract area(): number;
  abstract perimeter(): number;

  describe(): string {
    return `Area: ${this.area()}, Perimeter: ${this.perimeter()}`;
  }
}

class Circle extends Shape {
  constructor(private radius: number) { super(); }
  area(): number { return Math.PI * this.radius ** 2; }
  perimeter(): number { return 2 * Math.PI * this.radius; }
}
```

- `public` / `protected` / `private` behave like Java's access modifiers
  **at compile time only** — TS erases them on compile, so a `private` field
  is still a normal, accessible property on the emitted JS object (nothing
  stops JS code from reading it at runtime).
- `#sound` (the JS **private field** syntax, ES2022) is different: it's
  enforced by the *JavaScript runtime itself*, not just the TS compiler.
  Accessing `instance.#sound` from outside the class is a `SyntaxError` even
  in plain compiled JS. This is TS's `private` field's true equivalent to
  Java's runtime-enforced privacy — `private field: T` is TS-only/compile-time,
  `#field` is real privacy.
- Parameter properties (`Dog`'s constructor: `public breed: string, private
  readonly age: number`) — a shorthand that both declares the class field
  *and* assigns it from the constructor argument in one line. No Java
  equivalent; in Java you'd write the field declaration and the
  `this.breed = breed;` assignment separately.
- `implements Serializable` — this is where TS looks nominal (explicit
  `implements` keyword, familiar from Java), but the check underneath is
  still structural: `class User` only needs to provide methods matching the
  interface's shape; there's no interface *table* wired up at runtime the
  way Java's vtable dispatch works. `implements` in TS is really just a
  compile-time shape assertion.
- `abstract class Shape` — like Java's `abstract class`: cannot be
  instantiated directly, `abstract` members have no body and must be
  implemented by subclasses, concrete methods (`describe()`) are inherited
  as-is. TS abstract classes, like Java, can mix abstract and concrete
  members (unlike interfaces, which — pre-default-methods — cannot).
- `get info()` / `set sound()` — accessor syntax, compiles to
  `Object.defineProperty` under the hood; called like a plain property
  (`animal.info`, `animal.sound = 'x'`), not a method call.
- `static create(...)` — a static factory method, same concept as Java's
  static factory methods; called on the class itself (`Animal.create(...)`).

---

## 5. Advanced Types

```typescript
type StringOrNumber = string | number;
function format(val: StringOrNumber): string {
  return typeof val === 'string' ? val : val.toFixed(2);
}

type Direction = 'north' | 'south' | 'east' | 'west';
type DiceRoll = 1 | 2 | 3 | 4 | 5 | 6;
type HttpStatus = 200 | 201 | 400 | 401 | 403 | 404 | 500;

type Shape =
  | { kind: 'circle'; radius: number }
  | { kind: 'square'; side: number }
  | { kind: 'rectangle'; width: number; height: number };

function area(shape: Shape): number {
  switch (shape.kind) {
    case 'circle':    return Math.PI * shape.radius ** 2;
    case 'square':    return shape.side ** 2;
    case 'rectangle': return shape.width * shape.height;
  }
}

function isString(val: unknown): val is string {
  return typeof val === 'string';
}

function isUser(val: unknown): val is User {
  return typeof val === 'object' && val !== null && 'name' in val && 'email' in val;
}

type PartialUser = Partial<User>;
type RequiredUser = Required<User>;
type ReadonlyUser = Readonly<User>;
type UserName = Pick<User, 'name' | 'email'>;
type WithoutId = Omit<User, 'id'>;
type StringValues = Record<string, string>;
type StringOrNull = NonNullable<string | null | undefined>;

type Optional<T> = { [K in keyof T]?: T[K] };
type Nullable<T> = { [K in keyof T]: T[K] | null };

type IsString<T> = T extends string ? 'yes' : 'no';
type Flatten<T> = T extends Array<infer U> ? U : T;

type EventName = `on${Capitalize<string>}`;
type CSSProperty = `--${string}`;
```

- **Union types** (`string | number`) — a value can be *either* type; must
  be narrowed (`typeof val === 'string'`) before using type-specific members
  (`.toFixed` only exists on `number`). This is `typeof`-based **type
  narrowing** — the compiler tracks the narrowed type inside each branch of
  the conditional.
- **Literal types** (`'north' | 'south' | ...`, `1 | 2 | 3 | 4 | 5 | 6`) —
  TS lets you use literal values themselves as types; this is exactly the
  mechanism the EMS project uses for `Role` and `Status` (Part C,
  Assignment 1) instead of an `enum`.
- **Discriminated unions** — each member of the `Shape` union has a common
  literal field (`kind`) that TypeScript uses to narrow which branch you're
  in inside a `switch`; omit a case and, combined with `strict` mode, TS can
  flag the switch as non-exhaustive (returning `undefined` implicitly is
  caught by `noImplicitReturns` in the reference's recommended config).
- **Type guards** (`val is string`) — a function whose return type is a
  **type predicate**. After calling `isString(x)` inside an `if`, TS narrows
  `x` to `string` for the rest of that block. There's no Java equivalent —
  `instanceof` in Java only narrows nominal types; TS type guards can narrow
  structural/primitive types too.
- **Utility types** — all built into the standard TS lib, all *mapped types*
  under the hood:
  - `Partial<T>` — every field optional. Used constantly in the EMS project's
    `Repository.update(id, changes: Partial<T>)` (Part C).
  - `Required<T>` — every field mandatory (drops `?`).
  - `Readonly<T>` — every field `readonly`.
  - `Pick<T, K>` / `Omit<T, K>` — select or exclude a subset of keys.
  - `Record<K, V>` — shorthand for an index signature `{ [key: K]: V }`.
  - `NonNullable<T>` — strips `null | undefined` from a union.
- **Mapped types** (`{ [K in keyof T]?: T[K] }`) — iterate over the keys of
  `T` to build a new type; this is what `Partial`/`Readonly`/etc. are
  implemented with internally.
- **Conditional types** (`T extends string ? 'yes' : 'no'`) — a type-level
  ternary, evaluated at compile time. `infer U` extracts a sub-type
  (`Flatten<number[]>` extracts `number` from inside the array).
- **Template literal types** — build string literal types out of other
  string literal types (`` `on${Capitalize<string>}` ``), useful for typing
  event-name conventions.

---

## 6. Generics

```typescript
function identity<T>(arg: T): T { return arg; }

function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

class Stack<T> {
  private items: T[] = [];
  push(item: T): void { this.items.push(item); }
  pop(): T | undefined { return this.items.pop(); }
  peek(): T | undefined { return this.items[this.items.length - 1]; }
  get size(): number { return this.items.length; }
}

const numStack = new Stack<number>();

async function fetchData<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json() as Promise<T>;
}
```

- Syntactically close to Java generics (`<T>`), but semantically different:
  Java generics are **erased** at compile time for the *JVM* but the
  compiler still enforces bounds strictly and boxing rules exist. TS
  generics are erased too (no runtime trace of `T` at all — `new Stack<number>()`
  and `new Stack<string>()` produce identical JS), and TS additionally
  supports **structural constraints** (`K extends keyof T`) that read the
  actual key names of `T` — something with no Java parallel since Java can't
  introspect a type's member names at the type level.
- `Repository<T extends Identifiable>` (seen throughout Part C) is the
  course's central generic-constraint example: `T` must have at least an
  `id: number` field, and the constraint is enforced structurally, not by
  `T` extending a named interface hierarchy the way Java would require.

---

## 7. Decorators

```typescript
// tsconfig.json: "experimentalDecorators": true

function Singleton<T extends { new(...args: any[]): {} }>(constructor: T) {
  let instance: T;
  return class extends constructor {
    constructor(...args: any[]) {
      if (instance) return instance;
      super(...args);
      instance = this as any;
    }
  };
}

function log(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  const original = descriptor.value;
  descriptor.value = function (...args: any[]) {
    console.log(`Calling ${propertyKey} with`, args);
    const result = original.apply(this, args);
    console.log(`${propertyKey} returned`, result);
    return result;
  };
  return descriptor;
}

@Singleton
class Database {
  constructor(private url: string) {}
  query(sql: string) { return []; }
}

class UserService {
  @log
  findById(id: number): User | null {
    return null;
  }
}
```

- Decorators require `experimentalDecorators: true` in `tsconfig.json` (a
  TC39-stage proposal at the time this syntax was designed) — this exact
  flag is set in the EMS project's `tsconfig.json` (Part C) because
  Assignment 3's `@LogCall` decorator depends on it.
- A **method decorator** receives `(target, propertyKey, descriptor)` — the
  `PropertyDescriptor` is the same object `Object.defineProperty` uses in
  plain JS. Wrapping `descriptor.value` is how you intercept every call to
  that method — exactly the pattern the EMS `@LogCall` decorator uses (Part
  C, Assignment 3).
- A **class decorator** receives the constructor function and can return a
  *replacement* constructor (`Singleton` above wraps `constructor` to force
  a single shared instance) — closest Java analogy is a factory/proxy
  pattern, but here it's baked into the language via `@` syntax rather than
  hand-rolled.

---

## 8–12. Webpack with TypeScript & Location-Finder Project

The reference closes with a project-level example bundling TS with Webpack
(`ts-loader`) for a browser app that geocodes addresses via the Google Maps
API — `interface Coordinates`, a `LocationApp` class with `private map:
google.maps.Map`, and `as HTMLElement`/`as HTMLInputElement` type assertions
for DOM elements (the DOM API is untyped/`unknown`-shaped from TS's
perspective without an assertion, since `document.getElementById` returns
the broad `HTMLElement | null`). This section is webpack/bundler
configuration rather than new TS language material — the `tsconfig.json`
principles and `as` assertions already covered above apply directly.

---

# PART B — TypeScript Fundamentals Demo Files

These three files (`ts/script.ts`, `ts/ts-classes-etc.ts`,
`ts/ts-interfaces.ts`) are **teaching scripts**: almost every block is kept
commented-out, meant to be uncommented one section at a time live in class
to progressively reveal syntax. They map directly onto reference sections
2–4 above.

## B.1 — `script.ts`

Every line in this file is a comment (`//`) — nothing executes as-is; it's a
scripted walkthrough. Line numbers below refer to the actual file.

```typescript
1:  // // // // // console.log('TS Hello world!');
...
6:  // // // // let salary = 90000.25;
7:  // // // // console.log(salary);
9:  // // // // // salary = 'abc';
15: // // // // Type notation in TS
18: // // // // let salary; //   Variable 'salary' implicitly has an 'any' type
20: // // // // salary = 'abc'; // Type 'string' is not assignable to type 'number'.ts(2322)
30: // // // // let salary : number = 10.25;
```

- **Line 6–7** — plain JS-style: `let salary = 90000.25;` with no type
  annotation — TS still infers `number` from the initializer (**type
  inference**), so this isn't actually loosely typed, it just doesn't
  *look* typed.
- **Line 18** — `let salary;` with **no initializer and no annotation** — TS
  cannot infer anything, so under `strict` mode it errors: *"Variable
  'salary' implicitly has an 'any' type."* This is the exact moment the
  course uses to motivate explicit annotations.
- **Line 20** — once `salary` has settled on `number` (whether by inference
  from an initializer, or explicit annotation), `salary = 'abc'` is
  rejected with `ts(2322)` — a compile-time type error, the core selling
  point of TS vs. JS.
- **Line 30** — `let salary : number = 10.25;` — the explicit-annotation
  form; functionally identical to line 6–7's inferred form, but the
  annotation documents intent and works even without an initializer.

```typescript
39: // // // const addNums = (a: number, b: number): number => {
40: // // //     return a + b;
41: // // // };
46: // // // const addNumsPrint = (a: number, b: number): void => {
47: // // //     console.log(a + b);
48: // // // };
```

- **Line 39–41** — arrow function with typed parameters (`a: number, b:
  number`) and a typed return value (`: number` after the parameter list).
  Java-familiar: this is the same triple of (param types, return type,
  body) Java requires, just with the annotation position after the colon
  instead of before the name.
- **Line 46–48** — `addNumsPrint`'s return type is annotated `void` — it
  calls `console.log` and returns nothing meaningful; distinct from
  `undefined` in that TS specifically flags trying to *use* the return
  value of a `void` function as likely a mistake.

```typescript
55: // // let myData: string | number;
57: // // myData = 'abc';
60: // // myData = 10.25;
63: // // // myData = false; // Error
66: // // let myData2: any;
77: // // let myData3: unknown;
```

- **Line 55–63** — `myData: string | number` is a **union type**: only
  `string` or `number` values are legal; `myData = false` (boolean) is
  rejected at compile time (line 63, commented out because it *would*
  error).
- **Line 66–75** — `myData2: any` accepts string, number, and boolean with
  zero complaints — demonstrating why `any` should be avoided: it silently
  reintroduces every bug class TS exists to prevent.
- **Line 77–86** — `myData3: unknown` also accepts any assignment, but
  (per reference section 2) using `unknown` values requires narrowing
  first — the safe version of `any`.

```typescript
89:  // // Primitive types
95:  // // Arrays
99:  // // Tuple: fixed-length, fixed-type array
104: // // Enums
110: // // const enum (inlined at compile time — no JS enum object)
113: // // any: opt-out of type checking (avoid!)
116: // // unknown: safe version of any — must narrow before use
120: // // never: function that never returns (throws or infinite loop)
125: // // void: function that returns nothing meaningful
130: // // Type assertions
```

- **Lines 89–132** are a verbatim copy of reference section 2 ("TypeScript
  Basics & Types") kept in the demo file for live uncommenting during class
  — same primitives, arrays vs. `Array<T>` generic syntax, tuples with
  destructuring, numeric vs. string `enum`, `const enum`, `any`/`unknown`/
  `never`/`void`, and `as` type assertions already explained in Part A.2.

## B.2 — `ts-classes-etc.ts`

```typescript
1:  class Animal {
2:      readonly id: number;
3:      public name: string;
4:      protected species: string;
5:      // #sound: string;     // true private (ES2022 field)
6:      // private sound: string;     // true private (ES2022 field)
7:      #hashField: string;
8:  
9:      constructor(name: string, species: string, hf: string) {
10:         this.id = Math.random();
11:         this.name = name;
12:         this.species = species;
13:         this.#hashField = hf;
14:         // this.sound = 'generic';
15:     }
16: }
17: const anml1 = new Animal('dog', 'dog specie', 'hashvalue');
18: // anml1.sound = 'another value';
19: // anml1.#hashField = 'abc';
20: console.log(anml1);
```

- **Line 2** — `readonly id: number` — set once inside the constructor
  (line 10), any later reassignment from outside is a compile error.
- **Line 3** — `public name: string` — explicit `public` (the default
  visibility if omitted, but written here to contrast with the next two
  lines).
- **Line 4** — `protected species: string` — visible to `Animal` and any
  subclass, not to outside callers.
- **Line 7** — `#hashField: string` — a real ES2022 private class field
  (runtime-enforced, not just compiler-enforced). Note the comment on line
  6 shows the class originally used TS's `private` keyword (`private
  sound`) before being swapped for the `#`-syntax to demonstrate the
  stronger guarantee.
- **Line 9–15** — constructor takes `name`, `species`, and `hf`, and
  assigns `hf` into the true-private `#hashField` (line 13). Line 14 is
  dead/commented code left from an earlier iteration of the class that used
  a `sound` field.
- **Line 17** — instantiates with `new Animal('dog', 'dog specie',
  'hashvalue')` — note the constructor signature takes 3 `string` params in
  this version, positional and required (no optional params here).
- **Line 18–19** — both commented out because both *would* fail:
  `anml1.sound` doesn't exist on this version of the class at all (dead
  reference to the removed `sound` property), and `anml1.#hashField` fails
  because `#hashField` can only be accessed from **inside** the `Animal`
  class body — this is JS's own private-field enforcement, catching the
  violation even before TypeScript's compiler would flag it as a type error.
- **Line 20** — logs the constructed object; the private `#hashField` won't
  show up on casual property enumeration (e.g. `Object.keys`), reinforcing
  that this privacy is real, not cosmetic.

```typescript
23: // class Animal {
...
27:     #sound: string;     // true private (ES2022 field)
...
36:     speak(): string {
37:         return `${this.name} says ${this.#sound}`;
38:     }
40:     get info(): string { return `${this.name} (${this.species})`; }
41:     set sound(value: string) { this.#sound = value; }
43:     static create(name: string, species: string): Animal {
44:         return new Animal(name, species);
45:     }
46: }
```

- **Lines 23–46** (commented alternate version) — this is the fuller
  reference-section-4 shape of `Animal`: adds a `speak()` instance method
  interpolating the private `#sound` field (line 37), a `get info()`
  accessor (line 40) combining `name` and the `protected species` field, a
  `set sound(value)` setter that writes into the private field (line 41,
  the *only* way to mutate `#sound` from outside the class), and a
  `static create(...)` factory method (line 43–45) that constructs and
  returns a new `Animal` without exposing `new` to callers.

```typescript
49: // // class Dog extends Animal {
52:         public breed: string,       // declares + assigns in one step
53:         private readonly age: number
55:         super(name, 'dog');
58:     speak(): string {
59:         return `${this.name} barks!`;
```

- **Lines 49–61** (commented) — `Dog extends Animal` using **parameter
  properties**: `public breed: string` and `private readonly age: number`
  directly in the constructor parameter list declare and assign those
  fields in one step (line 52–53), instead of writing `breed: string` as a
  separate field declaration plus `this.breed = breed;` in the body. `super(name,
  'dog')` (line 55) calls the parent `Animal` constructor. `speak()` (line
  58–59) **overrides** the parent's method — no `@Override` annotation
  needed like Java; TS checks override-compatibility structurally against
  the base class signature.

```typescript
64: // //     toJSON(): object;
65: // //     toString(): string;
69: // // class User implements Serializable {
70: // //     constructor(public name: string, public email: string) { }
```

- **Lines 63–73** (commented) — `interface Serializable` with `toJSON()`
  and `toString()`, and `class User implements Serializable` satisfying it
  via parameter properties in the constructor (line 70) — the same
  parameter-property shorthand as `Dog`, this time for a plain non-inherited
  class.

```typescript
76: // //     abstract area(): number;      // must be implemented by subclasses
77: // //     abstract perimeter(): number;
80: // //     describe(): string {
81: // //         return `Area: ${this.area()}, Perimeter: ${this.perimeter()}`;
86: // //     constructor(private radius: number) { super(); }
```

- **Lines 75–89** (commented) — `abstract class Shape` with two `abstract`
  methods (no body — must be implemented by subclasses, lines 76–77) and
  one concrete method `describe()` (lines 80–82) that calls the not-yet-
  implemented abstract methods polymorphically. `Circle extends Shape`
  (lines 85–89) supplies concrete `area()`/`perimeter()` and uses a
  parameter property (`private radius: number`, line 86) again.

```typescript
105: // class Animal {
106: //     id;
107: //     name;
108: //     species;
109: //     #sound;
111: //     constructor(id, name, species, sound) {
118: //     printData() { console.log(this.#sound); };
```

- **Lines 105–124** (commented) — this block is the **compiled-JS-shaped**
  equivalent of the class: no type annotations at all (`id;`, `name;` with
  no `: type`), constructor parameters untyped, `#sound` private field
  syntax preserved (because `#field` is native JS, it survives compilation
  unchanged — it's the one privacy mechanism TS doesn't need to erase).
  This is the file's way of showing exactly what compiling the earlier
  TS-annotated class strips away: every `: type`, every `public`/
  `protected`/`private` keyword, every `readonly` — all gone in the emitted
  `.js`, while `#sound` and the class/method structure itself survive
  untouched.

## B.3 — `ts-interfaces.ts`

The entire file is commented out (a reveal script), directly implementing
reference section 3.

```typescript
1:  // interface User {
2:  //     readonly id: number;         // readonly: can't reassign
3:  //     name: string;
4:  //     email: string;
5:  //     age?: number;                // optional
6:  //     address?: {
7:  //         city: string;
8:  //         country: string;
9:  //     };
10: // }
12: // const user1: User = {
13: //     id: 1,
14: //     name: 'Sonu',
15: //     email: 'a@b.c'
16: // };
18: // console.log(user1);
19: // // user1.id = 2;// error
20: // user1.name = 'Monu';
21: // console.log(user1);
25: // interface AdminUser extends User {
26: //     role: 'admin';
27: //     permissions: string[];
28: // }
30: // const adminUser: AdminUser = {
31: //     role: 'admin',
32: //     name: 'Tonu',
33: //     email: 'a',
34: //     id: 3,
35: //     permissions: []
36: // }
```

- **Line 2** — `readonly id: number` on the interface.
- **Line 5** — `age?: number` — optional member; omitted entirely from
  `user1` (lines 12–16) and that's legal because it's optional.
- **Line 6–9** — `address?` is an **inline nested object type literal**
  (not a separately named interface) — also omitted from `user1` since it's
  optional too.
- **Line 12–16** — `user1: User` satisfies the interface with just `id`,
  `name`, `email` — proof that optional fields really are optional at the
  literal-construction site.
- **Line 19** — `user1.id = 2;` is commented out specifically *because* it
  errors: `id` is `readonly`, so reassignment after construction is a
  compile-time error (`ts(2540)`-class error), demonstrating `readonly` is
  enforced at every write site, not just the initial one.
- **Line 20** — `user1.name = 'Monu';` **is** allowed and left uncommented
  in the reveal — `name` has no `readonly` modifier, so it's freely
  mutable.
- **Lines 25–28** — `interface AdminUser extends User` — adds `role: 'admin'`
  (a single-value literal type, not the broader `string`) and
  `permissions: string[]`. Extending an interface is additive: `AdminUser`
  must satisfy every member of `User` plus its own.
- **Lines 30–36** — `adminUser: AdminUser` supplies every required field
  from both `User` (`name`, `email`, `id` — `age`/`address` still optional
  and omitted) and `AdminUser` itself (`role`, `permissions`). Note `id: 3`
  can still only be set once — the `readonly` constraint from the parent
  interface still applies through the `extends` chain.

---

# PART C — TypeScript EMS Assignments Project

Courseware: `typescript-assignments-ems.md`. Code: `ems-ts/`. This is a
single Employee Management System built progressively across 5 assignments
— each later assignment imports and extends the previous one's types and
classes, culminating in a working Node CLI app (per the courseware's
"Progression summary" table and the `README.md`'s explicit note that running
a later assignment also re-runs and re-logs all earlier ones' demo code,
since they're executed top-level on import).

## C.1 — What each assignment asks for (courseware)

- **Assignment 1 — Types, interfaces & data modelling.** Foundation, no
  dependencies. Define `Role`/`Status` literal-union types; `Department`,
  `Employee`, `Project` interfaces; utility functions `getFullLabel`,
  `isActive`, `daysUntilDeadline`; and mock data (3 departments, 6
  employees, 2 projects).
- **Assignment 2 — Generic repository class.** Builds on Assignment 1's
  interfaces. A base `Identifiable { id: number }` interface, and a generic
  `Repository<T extends Identifiable>` wrapping a private `Map<number, T>`
  with `add`/`findById`/`getAll`/`update` (via `Partial<T>`)/`remove`/
  `query(predicate)`. Mirrors the repository pattern used later in Angular
  services (explicitly called out in the courseware as a forward reference).
- **Assignment 3 — Service layer with abstract classes & decorators.**
  Builds on Assignment 2's `Repository<T>`. A `@LogCall` method decorator
  that logs method name/args/return value; an abstract `BaseService<T
  extends Identifiable>` delegating CRUD to a `Repository<T>`;
  `EmployeeService extends BaseService<Employee>` adding `getByDepartment`,
  `promote`, `getSalaryReport`; `ProjectService extends BaseService<Project>`
  adding `assignEmployee`, `getOverdue`. `@LogCall` is applied to `promote`,
  `assignEmployee`, `getSalaryReport`. Explicitly framed as the same shape
  Angular uses for services and component decorators.
- **Assignment 4 — Async operations & event callbacks.** Builds on
  Assignment 3's services. A `delay(ms)` helper simulating a DB call; each
  service method wrapped to be `async`/`await delay(300)`; an `EventBus`
  class (`on`/`emit`); events fired from services (`employee:added`,
  `employee:promoted`, `employee:removed`, `project:assigned`,
  `project:overdue-check`); a `main()` that subscribes to all events, runs
  an async sequence (add → assign → promote → report), and wraps it in
  `try/catch` with the error typed `unknown`. Framed as mirroring Angular's
  `HttpClient` + `EventEmitter` pattern.
- **Assignment 5 — Persistence, reporting & CLI runner.** Builds on
  everything. `saveAll(path)` serialising a repository to JSON via
  `fs.promises.writeFile`; a static `loadAll<T>(path)` reading/parsing JSON
  (returning `[]`, not throwing, if the file is missing); a `ReportService`
  with `departmentSummary()` and `projectStatus()`; a `main()` that loads-or-
  seeds data, performs async operations, prints both reports via
  `console.table()`, and saves state back to JSON — plus a bonus
  `EMSSnapshot` type capturing the full system state, saved as
  `ems-snapshot.json` every run.

## C.2 — `tsconfig.json`

```json
1:  {
2:    "compilerOptions": {
3:      "target": "ES2020",
4:      "module": "commonjs",
5:      "lib": ["ES2020", "dom"],
6:      "outDir": "./dist",
7:      "rootDir": "./src",
8:      "strict": true,
9:      "esModuleInterop": true,
10:     "experimentalDecorators": true,
11:     "emitDecoratorMetadata": true,
12:     "types": ["node"]
13:   }
14: }
```

- **Line 3** — `"target": "ES2020"` — the JS language level emitted output
  targets; ES2020 supports optional chaining (`?.`), nullish coalescing
  (`??`) — both used in the assignments (e.g. `assignment4.ts`'s `??`
  default for the listener array).
- **Line 4** — `"module": "commonjs"` — emits `require`/`module.exports`
  rather than ESM `import`/`export`, matching how `ts-node` and plain Node
  scripts in this project are actually run (`npx ts-node
  src/assignment1.ts`), unlike the reference's suggested `NodeNext` for a
  pure-ESM setup.
- **Line 5** — `"lib": ["ES2020", "dom"]` — the ambient type declarations
  available: `ES2020` standard-library types (`Map`, `Promise`, etc. — both
  used heavily, e.g. `Repository`'s internal `Map<number, T>`) plus `dom`
  types (included defensively even though this is a CLI app with no browser
  APIs used — harmless but notably broader than strictly required).
- **Line 6–7** — `outDir`/`rootDir` — compiled output goes to `dist/`,
  source lives in `src/` (matches the `assignment1.ts`…`assignment5.ts`
  layout).
- **Line 8** — `"strict": true` — enables the full strict family
  (`strictNullChecks`, `noImplicitAny`, etc. as one flag) — this is what
  makes `let salary;` error in `script.ts` line 18, and what forces every
  `Employee`/`Department`/`Project` field to be fully typed with no silent
  gaps.
- **Line 9** — `"esModuleInterop": true` — required for `assignment5.ts`'s
  `import * as fs from "fs"` / `import * as path from "path"` to work
  cleanly against CommonJS modules without namespace-import friction.
- **Line 10–11** — `"experimentalDecorators": true` +
  `"emitDecoratorMetadata": true` — required specifically for
  `assignment3.ts`'s `@LogCall` method decorator (`emitDecoratorMetadata`
  isn't strictly needed by `@LogCall` itself since it doesn't use
  reflection, but it's the standard pairing enabled whenever decorators are
  turned on, and the courseware's own setup instructions list both flags
  together).
- **Line 12** — `"types": ["node"]` — restricts which `@types/*` packages
  are auto-included as ambient globals to just `@types/node`; without this,
  TS would auto-include *every* `@types/*` package found in
  `node_modules`, which could pull in unwanted global type pollution (e.g.
  DOM globals from an unrelated package) even though `lib` already includes
  `dom` type *definitions* for explicit use.

## C.3 — `assignment1.ts` — Types, interfaces & data modelling

```typescript
7:  export type Role = "engineer" | "manager" | "hr" | "intern";
8:  export type Status = "active" | "inactive" | "on-leave";
12: export interface Department {
13:   id: number;
14:   name: string;
15:   location: string;
16: }
18: export interface Employee {
19:   id: number;
20:   name: string;
21:   role: Role;
22:   salary: number;
23:   status: Status;
24:   departmentId: number;
25:   email?: string;
26: }
28: export interface Project {
29:   id: number;
30:   title: string;
31:   budget: number;
32:   employeeIds: number[];
33:   deadline: Date;
34: }
38: export function getFullLabel(e: Employee): string {
39:   return `${e.name} — ${e.role}`;
40: }
42: export function isActive(e: Employee): boolean {
43:   return e.status === "active";
44: }
46: export function daysUntilDeadline(p: Project): number {
47:   const now = new Date();
48:   const diff = p.deadline.getTime() - now.getTime();
49:   return Math.ceil(diff / (1000 * 60 * 60 * 24));
50: }
```

- **Line 7–8** — `Role` and `Status` are **literal-union types**, not
  `enum`s — a deliberate choice throughout the EMS project: string literal
  unions serialize to plain JSON strings with no runtime object needed
  (matters a lot for Assignment 5's JSON persistence — an `enum`'s numeric
  values or extra reverse-mapping object would complicate the
  save/load round trip).
- **Line 12–16, 18–26, 28–34** — three `export interface` declarations,
  every field in `Employee`/`Department`/`Project` typed explicitly. Note
  `role: Role` and `status: Status` (line 21, 23) reuse the literal-union
  types from line 7–8 rather than repeating `string` — this is what lets
  `promote(id, newRole: Role)` in Assignment 3 reject a typo like
  `"managr"` at compile time.
- **Line 25** — `email?: string` — the *only* optional field in the whole
  model; every mock employee either has or omits it (see lines 61–67 below).
- **Line 33** — `deadline: Date` — a real `Date` object, not a string; this
  choice is what forces Assignment 5 to manually reconstruct `Date` objects
  after loading from JSON (JSON has no native date type — see C.7).
- **Line 38–40** — `getFullLabel(e: Employee): string` — simple typed
  utility, template-literal string interpolation (`${e.name} — ${e.role}`).
- **Line 42–44** — `isActive(e: Employee): boolean` — a one-line predicate
  comparing against the `Status` literal `"active"`.
- **Line 46–50** — `daysUntilDeadline(p: Project): number` — computes the
  millisecond difference between `p.deadline` and `new Date()` (line 48),
  converts to days with `Math.ceil(diff / (1000 * 60 * 60 * 24))` (line
  49) — `Math.ceil` rounds up so a deadline "tomorrow at 1am" doesn't read
  as "0 days left." This function is reused unmodified all the way through
  Assignment 5's `projectStatus()` report.

```typescript
54: export const departments: Department[] = [
60: export const employees: Employee[] = [
61:   { id: 1, name: "Alice",   role: "manager",  salary: 90000, status: "active",    departmentId: 1, email: "alice@ems.com" },
69: export const projects: Project[] = [
82:     deadline: new Date("2024-06-30"),        // intentionally in the past (overdue)
```

- **Line 54–58, 60–67, 69–84** — mock data arrays, each explicitly typed
  (`: Department[]`, `: Employee[]`, `: Project[]`) so any malformed literal
  is caught immediately rather than at first use.
- **Line 82** — a deadline deliberately set in the past — a planted test
  fixture so `getOverdue()` (Assignment 3) and `daysUntilDeadline`
  (Assignment 1) always have at least one real overdue project to report
  on without needing the demo to be run on a specific date.
- **Line 88–101** — a `console.log` demo section prints departments,
  employees (via `getFullLabel`/`isActive`), and projects (via
  `daysUntilDeadline`) — this demo block re-runs every time a later
  assignment imports `assignment1.ts`, which is why running `assignment5.ts`
  reprints Assignment 1's, 2's, 3's, and 4's demo output too (per the
  README's explicit note).

## C.4 — `assignment2.ts` — Generic repository class

```typescript
6:  import {
7:    Department, Employee, Project,
8:    departments, employees, projects,
9:    getFullLabel,
10: } from "./assignment1";
14: export interface Identifiable {
15:   id: number;
16: }
20: export class Repository<T extends Identifiable> {
21:   private store: Map<number, T> = new Map();
23:   add(item: T): void {
24:     if (this.store.has(item.id)) {
25:       throw new Error(`Item with id ${item.id} already exists.`);
26:     }
27:     this.store.set(item.id, item);
28:   }
30:   findById(id: number): T | undefined {
31:     return this.store.get(id);
32:   }
34:   getAll(): T[] {
35:     return Array.from(this.store.values());
36:   }
38:   update(id: number, changes: Partial<T>): boolean {
39:     const existing = this.store.get(id);
40:     if (!existing) return false;
41:     this.store.set(id, { ...existing, ...changes });
42:     return true;
43:   }
45:   remove(id: number): boolean {
46:     return this.store.delete(id);
47:   }
49:   query(predicate: (item: T) => boolean): T[] {
50:     return this.getAll().filter(predicate);
51:   }
52: }
```

- **Line 6–10** — imports the three interfaces and the mock arrays plus
  `getFullLabel` straight from `assignment1.ts`, confirming the courseware's
  claim that Assignment 2 builds directly on Assignment 1's types.
- **Line 14–16** — `Identifiable` — the minimal structural constraint: any
  type with at least an `id: number` field satisfies it. `Employee`,
  `Department`, and `Project` all qualify automatically (structural typing
  again — none of them ever writes `implements Identifiable`).
- **Line 20** — `class Repository<T extends Identifiable>` — the generic
  constraint (`extends Identifiable`) is what allows `item.id` to be
  referenced safely inside the class body (line 24, 27) — without the
  constraint, TS would have no reason to believe `T` has an `id` at all.
- **Line 21** — `private store: Map<number, T> = new Map();` — a `private`
  field (TS-compile-time private, not `#`-private) holding the actual
  storage, keyed by numeric id.
- **Line 23–28** — `add(item: T): void` — throws if an item with that id
  already exists (line 24–26, using the same `item.id` structural guarantee),
  otherwise inserts.
- **Line 30–32** — `findById(id): T | undefined` — the `| undefined` return
  type is TS forcing every caller to handle the "not found" case (seen used
  with optional chaining, `bob?.salary`, in the demo below).
- **Line 34–36** — `getAll(): T[]` — converts the `Map`'s values iterator to
  a plain array via `Array.from`.
- **Line 38–43** — `update(id, changes: Partial<T>): boolean` — this is the
  reference's `Partial<T>` utility type (Part A.5) in real use: `changes`
  can supply *any subset* of `T`'s fields. Line 41 spreads the existing
  object and overlays `changes` (`{ ...existing, ...changes }`), so an
  update to just `{ salary: 75000 }` leaves every other field untouched.
- **Line 45–47** — `remove(id): boolean` — delegates directly to
  `Map.prototype.delete`, which already returns a boolean for
  found/not-found.
- **Line 49–51** — `query(predicate: (item: T) => boolean): T[]` — a typed
  higher-order function parameter; `predicate` must accept a `T` and return
  `boolean` — this is what lets `empRepo.query(e => e.status === "active" &&
  e.role === "engineer")` type-check `e` as `Employee` automatically with no
  extra annotation needed at the call site.

```typescript
56: export function seedRepositories() {
57:   const empRepo = new Repository<Employee>();
58:   const deptRepo = new Repository<Department>();
59:   const projectRepo = new Repository<Project>();
61:   departments.forEach(d => deptRepo.add(d));
62:   employees.forEach(e => empRepo.add(e));
63:   projects.forEach(p => projectRepo.add(p));
65:   return { empRepo, deptRepo, projectRepo };
66: }
```

- **Line 56–66** — `seedRepositories()` instantiates three separately typed
  `Repository<T>`s (line 57–59, each supplying a different `T`) and fills
  them from Assignment 1's mock arrays (line 61–63). Returns an object with
  all three — this exact function is re-called by Assignment 3's
  `buildServices()` (see C.5) and by Assignment 4's `main()`.

```typescript
75: const activeEngineers = empRepo.query(
76:   e => e.status === "active" && e.role === "engineer"
77: );
82: empRepo.update(2, { salary: 75000 });
83: const bob = empRepo.findById(2);
84: console.log(`\nBob's updated salary: ${bob?.salary}`);
87: projectRepo.remove(2);
91: const engTeam = empRepo.query(e => e.departmentId === 1);
```

- **Line 75–79** — demonstrates `query()` filtering active engineers.
- **Line 82–84** — demonstrates `update()`, then `findById()`, then reads
  `bob?.salary` — the `?.` optional chaining is required precisely because
  `findById` returns `T | undefined` (line 30), so TS won't let you access
  `.salary` without either a null check or `?.`.
- **Line 87** — demonstrates `remove()`.
- **Line 91** — demonstrates `query()` again for a department filter.

## C.5 — `assignment3.ts` — Service layer with abstract classes & decorators

```typescript
6:  import { Repository, Identifiable, seedRepositories } from "./assignment2";
7:  import { Employee, Project, Role, daysUntilDeadline } from "./assignment1";
11: export function LogCall(
12:   _target: object,
13:   key: string,
14:   descriptor: PropertyDescriptor
15: ): PropertyDescriptor {
16:   const original = descriptor.value;
17:   descriptor.value = function (...args: unknown[]) {
18:     const result = original.apply(this, args);
19:     console.log(`[LogCall] ${key}(${JSON.stringify(args)}) => ${JSON.stringify(result)}`);
20:     return result;
21:   };
22:   return descriptor;
23: }
```

- **Line 6–7** — imports `Repository`/`Identifiable` from Assignment 2 and
  `Role`/`daysUntilDeadline` from Assignment 1 — Assignment 3 sits directly
  on top of both prior layers.
- **Line 11–23** — `LogCall` is a **method decorator** matching the exact
  shape from Part A.7 (`(target, key, descriptor)`). `_target` is prefixed
  with `_` by convention to signal "intentionally unused parameter" (paired
  with the reference tsconfig's `noUnusedParameters`, though this project's
  own tsconfig doesn't set that flag). Line 16 grabs the original method,
  line 17–21 replaces `descriptor.value` with a wrapper that calls the
  original (`original.apply(this, args)`, preserving `this` binding) and
  logs `key`, `args`, and `result` as JSON — this is what produces the
  `[LogCall] promote(...) => ...` console lines when `@LogCall` is applied
  below.

```typescript
27: export abstract class BaseService<T extends Identifiable> {
28:   constructor(protected repo: Repository<T>) {}
30:   add(item: T): void           { this.repo.add(item); }
31:   findById(id: number)         { return this.repo.findById(id); }
32:   getAll(): T[]                { return this.repo.getAll(); }
33:   update(id: number, changes: Partial<T>): boolean { return this.repo.update(id, changes); }
34:   remove(id: number): boolean  { return this.repo.remove(id); }
35: }
```

- **Line 27** — `abstract class BaseService<T extends Identifiable>` —
  cannot be instantiated directly; generic over the same `Identifiable`
  constraint as `Repository<T>`.
- **Line 28** — `constructor(protected repo: Repository<T>) {}` — a
  parameter property again, this time `protected` rather than `public` or
  `private`: subclasses (`EmployeeService`, `ProjectService`) need direct
  access to `this.repo` (used in `getByDepartment`, `assignEmployee`, etc.
  below), but outside callers should not reach into it directly.
- **Line 30–34** — five one-line methods, each simply delegating to the
  matching `Repository<T>` method — this is the **facade/delegation**
  pattern: `BaseService` doesn't reimplement CRUD, it just forwards to the
  repository it was constructed with.

```typescript
39: export class EmployeeService extends BaseService<Employee> {
40:   getByDepartment(deptId: number): Employee[] {
41:     return this.repo.query(e => e.departmentId === deptId);
42:   }
44:   @LogCall
45:   promote(id: number, newRole: Role): boolean {
46:     return this.repo.update(id, { role: newRole });
47:   }
49:   @LogCall
50:   getSalaryReport(): { total: number; average: number; highest: Employee } {
51:     const all = this.repo.getAll();
52:     const total = all.reduce((sum, e) => sum + e.salary, 0);
53:     const average = Math.round(total / all.length);
54:     const highest = all.reduce((top, e) => (e.salary > top.salary ? e : top), all[0]);
55:     return { total, average, highest };
56:   }
57: }
```

- **Line 39** — `EmployeeService extends BaseService<Employee>` — locks the
  generic `T` to `Employee` for this subclass; all the delegated CRUD
  methods from `BaseService` are now concretely typed for `Employee`.
- **Line 40–42** — `getByDepartment` — a thin wrapper around
  `this.repo.query(...)`, made possible because `repo` is `protected` (line
  28) and therefore visible here.
- **Line 44–47** — `@LogCall` applied directly above `promote`; `promote`
  itself just calls `this.repo.update(id, { role: newRole })` — note
  `{ role: newRole }` satisfies `Partial<Employee>` (only one field
  supplied).
- **Line 49–56** — `getSalaryReport()` returns an **inline object type**
  (`{ total: number; average: number; highest: Employee }`) rather than a
  named interface — legal and common for one-off return shapes. Line 52
  totals every salary with `reduce`; line 53 averages and rounds; line 54
  finds the highest earner via `reduce` comparing `e.salary > top.salary`,
  seeded with `all[0]`. Decorated with `@LogCall` too (line 49).

```typescript
61: export class ProjectService extends BaseService<Project> {
62:   @LogCall
63:   assignEmployee(projectId: number, empId: number): boolean {
64:     const project = this.repo.findById(projectId);
65:     if (!project) return false;
66:     if (project.employeeIds.includes(empId)) return false;
67:     return this.repo.update(projectId, {
68:       employeeIds: [...project.employeeIds, empId],
69:     });
70:   }
72:   getOverdue(): Project[] {
73:     return this.repo.query(p => daysUntilDeadline(p) < 0);
74:   }
75: }
```

- **Line 61** — `ProjectService extends BaseService<Project>`.
- **Line 62–70** — `assignEmployee` — guards against a missing project
  (line 65) and against a duplicate assignment (line 66,
  `.includes(empId)`), then updates `employeeIds` immutably by spreading the
  existing array and appending (`[...project.employeeIds, empId]`, line
  68) — never mutates the original array in place.
- **Line 72–74** — `getOverdue()` reuses Assignment 1's `daysUntilDeadline`
  (imported line 7) inside a `query()` predicate — direct proof of the
  cross-assignment reuse chain: Assignment 1's function → Assignment 3's
  service method.

```typescript
79: export function buildServices() {
80:   const { empRepo, deptRepo, projectRepo } = seedRepositories();
81:   const employeeService = new EmployeeService(empRepo);
82:   const projectService  = new ProjectService(projectRepo);
83:   return { employeeService, projectService, deptRepo };
84: }
```

- **Line 79–84** — `buildServices()` calls Assignment 2's
  `seedRepositories()` (line 80) then wraps the resulting repos in the two
  concrete services (line 81–82) — this is the function Assignment 4 and 5
  both call to bootstrap the whole stack in one call.

## C.6 — `assignment4.ts` — Async operations & event callbacks

```typescript
6:  import { EmployeeService, ProjectService, buildServices } from "./assignment3";
7:  import { Employee } from "./assignment1";
8:  import { Repository } from "./assignment2";
12: export function delay(ms: number): Promise<void> {
13:   return new Promise(resolve => setTimeout(resolve, ms));
14: }
```

- **Line 12–14** — `delay(ms): Promise<void>` — a `Promise` that resolves
  after `ms` milliseconds via `setTimeout`; the return type `Promise<void>`
  means callers `await` it purely for the timing effect, not for a value.

```typescript
18: type EventHandler = (payload: unknown) => void;
20: export class EventBus {
21:   private listeners: Map<string, EventHandler[]> = new Map();
23:   on(event: string, handler: EventHandler): void {
24:     const existing = this.listeners.get(event) ?? [];
25:     this.listeners.set(event, [...existing, handler]);
26:   }
28:   emit(event: string, payload: unknown): void {
29:     const handlers = this.listeners.get(event) ?? [];
30:     handlers.forEach(h => h(payload));
31:   }
32: }
```

- **Line 18** — `EventHandler = (payload: unknown) => void` — a function
  type alias; `payload` is typed `unknown` deliberately, since an `EventBus`
  is generic across *any* event shape and can't know in advance what each
  listener expects — matches the reference's guidance to use `unknown`
  rather than `any` for values whose shape isn't known yet.
- **Line 21** — `private listeners: Map<string, EventHandler[]>` — each
  event name maps to an array of handlers (multiple subscribers allowed).
- **Line 23–26** — `on(event, handler)` — line 24 uses `??` (nullish
  coalescing) to default to an empty array if no listeners exist yet for
  that event, then line 25 appends immutably (spread + new array) rather
  than pushing onto the existing array in place.
- **Line 28–31** — `emit(event, payload)` — looks up handlers (again
  defaulting via `??`) and calls each one with `payload` (line 30).

```typescript
36: export class AsyncEmployeeService extends EmployeeService {
37:   constructor(repo: Repository<Employee>, private bus: EventBus) {
38:     super(repo);
39:   }
41:   async addAsync(emp: Employee): Promise<void> {
42:     await delay(300);
43:     this.add(emp);
44:     this.bus.emit("employee:added", { id: emp.id, name: emp.name });
45:   }
47:   async promoteAsync(id: number, newRole: Parameters<EmployeeService["promote"]>[1]): Promise<boolean> {
48:     await delay(300);
49:     const result = this.promote(id, newRole);
50:     if (result) this.bus.emit("employee:promoted", { id, newRole });
51:     return result;
52:   }
54:   async removeAsync(id: number): Promise<boolean> {
55:     await delay(300);
56:     const result = this.remove(id);
57:     if (result) this.bus.emit("employee:removed", { id });
58:     return result;
59:   }
61:   async getSalaryReportAsync() {
62:     await delay(300);
63:     return this.getSalaryReport();
64:   }
65: }
```

- **Line 36** — `AsyncEmployeeService extends EmployeeService` — wraps
  Assignment 3's synchronous service with async, event-emitting versions of
  its methods rather than modifying it, preserving the sync API too.
- **Line 37–39** — constructor takes both a `Repository<Employee>` (passed
  straight to `super(repo)`, satisfying `EmployeeService`'s inherited
  constructor) and a `private bus: EventBus` — the parameter property
  pattern again, this time on a subclass constructor with an extra
  non-repo parameter.
- **Line 41–45** — `addAsync` — awaits the simulated delay (line 42), calls
  the inherited synchronous `add` (line 43), then emits `"employee:added"`
  with a minimal payload (line 44).
- **Line 47** — `newRole: Parameters<EmployeeService["promote"]>[1]` — a
  notable utility-type usage not explicitly in the reference's list but
  built from the same mapped/conditional-type machinery: `Parameters<F>`
  extracts a function type's parameter list as a tuple, `["promote"]`
  indexes into `EmployeeService`'s method by name, and `[1]` picks the
  second parameter's type — i.e., this expression evaluates to exactly
  `Role`, but *derived* from the original method's signature instead of
  hand-typed again, so if `promote`'s signature ever changes, this stays in
  sync automatically. This is a strictly TS capability with no Java
  parallel — Java has no way to reference "the type of another method's
  n-th parameter" at the type level.
- **Line 49–51** — calls the inherited sync `promote`, and only emits
  `"employee:promoted"` if it actually succeeded (`if (result)`, line 50).
- **Line 54–59**, **61–64** — same async-wrap-then-emit pattern for
  `removeAsync` and `getSalaryReportAsync` (the latter has no explicit
  emit — it's a pure async passthrough).

```typescript
69: export class AsyncProjectService extends ProjectService {
70:   constructor(repo: Repository<import("./assignment1").Project>, private bus: EventBus) {
71:     super(repo);
72:   }
74:   async assignEmployeeAsync(projectId: number, empId: number): Promise<boolean> {
75:     await delay(300);
76:     const result = this.assignEmployee(projectId, empId);
77:     if (result) this.bus.emit("project:assigned", { projectId, empId });
78:     return result;
79:   }
81:   async getOverdueAsync() {
82:     await delay(300);
83:     const overdue = this.getOverdue();
84:     this.bus.emit("project:overdue-check", { count: overdue.length });
85:     return overdue;
86:   }
87: }
```

- **Line 70** — `Repository<import("./assignment1").Project>` — an
  **inline import type** used purely for its type, avoiding a top-level
  named import just for this one parameter annotation; equivalent in effect
  to importing `Project` at the top of the file, just written inline.
- **Line 74–79** — mirrors `AsyncEmployeeService`'s pattern: delay, call
  sync method, conditionally emit `"project:assigned"`.
- **Line 81–86** — `getOverdueAsync` — unconditionally emits
  `"project:overdue-check"` with just a `count` (line 84), regardless of
  whether any projects are actually overdue, since "the check ran" is
  itself the event, not "overdue projects exist."

```typescript
91: async function main() {
97:   const { empRepo, projectRepo } = (() => {
98:     const { employeeService, projectService } = buildServices();
100:     const es = employeeService as unknown as { repo: Repository<Employee> };
101:     const ps = projectService as unknown as { repo: Repository<import("./assignment1").Project> };
102:     return { empRepo: es.repo, projectRepo: ps.repo };
103:   })();
```

- **Line 97–103** — an immediately-invoked arrow function extracting the
  private `repo` field out of `employeeService`/`projectService` purely for
  demo purposes. Line 100–101 uses a **double assertion**
  (`as unknown as { repo: ... }`) — TS normally refuses a direct cast
  between two unrelated types (`EmployeeService as { repo: ... }` would
  error, since neither is assignable to the other structurally in the
  compiler's eyes given `repo` is `protected`), so going through `unknown`
  first bypasses that check. This is explicitly a demo-only escape hatch,
  not something you'd do in production code, and the comment on line 99
  ("extract repos via casting for demo purposes") says so directly.
- **Line 105** — `const bus = new EventBus();`, then lines 106–107
  construct `AsyncEmployeeService`/`AsyncProjectService` wired to the same
  bus.

```typescript
114: events.forEach(ev =>
115:   bus.on(ev, payload =>
116:     console.log(`  [EVENT] ${ev} →`, JSON.stringify(payload))
117:   )
118: );
120: try {
122:   console.log("1. Adding new employee Grace...");
123:   await empService.addAsync({
124:     id: 7, name: "Grace", role: "engineer",
125:     salary: 72000, status: "active", departmentId: 1,
126:   });
...
150: } catch (err: unknown) {
151:   if (err instanceof Error) {
152:     console.error("Error:", err.message);
153:   } else {
154:     console.error("Unknown error:", err);
155:   }
156: }
```

- **Line 114–118** — subscribes the same logging handler to all five event
  names in a loop, matching the courseware's required `[EVENT] name →
  payload` log format.
- **Line 120–156** — the required `try { sequence } catch (err: unknown)`
  block: adds Grace (line 123–126), assigns her to a project, promotes her,
  runs the salary report, checks overdue projects, and removes Eva — each
  step `await`-ed in order. The `catch (err: unknown)` (line 150) then
  narrows with `err instanceof Error` (line 151) before accessing
  `.message` — exactly the type-guard pattern from Part A.5, applied to
  error handling specifically because a `catch` clause's error is typed
  `unknown` under `strict` mode (not `any`), so it cannot be used without
  narrowing first.
- **Line 159** — `main();` — the whole async function is invoked at the
  bottom (fire-and-forget top-level call, standard for a CommonJS CLI
  script since top-level `await` isn't available under this `module`
  setting).

## C.7 — `assignment5.ts` — Persistence, reporting & CLI runner

```typescript
6:  import * as fs from "fs";
7:  import * as path from "path";
8:  import {
9:    Employee, Department, Project,
10:   departments as mockDepts,
11:   employees  as mockEmps,
12:   projects   as mockProjects,
13:   daysUntilDeadline,
14: } from "./assignment1";
15: import { Repository, Identifiable } from "./assignment2";
16: import { buildServices } from "./assignment3";
17: import {
18:   AsyncEmployeeService,
19:   AsyncProjectService,
20:   EventBus,
21:   delay,
22: } from "./assignment4";
24: const DATA_DIR = path.join(__dirname, "../data");
```

- **Line 6–7** — `import * as fs`/`import * as path` — namespace imports,
  needed because `esModuleInterop`/`commonjs` interop is enabled (Part C.2)
  and Node's built-in modules don't have a single default export.
- **Line 8–22** — imports span **all four** prior assignments: types +
  mock data + `daysUntilDeadline` from Assignment 1, `Repository`/
  `Identifiable` from Assignment 2, `buildServices` from Assignment 3, and
  the async services + `EventBus` + `delay` from Assignment 4 — the single
  most direct evidence in the codebase of the "Assignment 5 builds on
  everything" claim in the courseware.
- **Line 10–12** — renames the mock arrays on import (`as mockDepts`,
  `as mockEmps`, `as mockProjects`) to avoid name collisions with the
  locally loaded data (`depts`, `emps`, `projs` below).
- **Line 24** — `DATA_DIR` resolved relative to `__dirname` (available
  because `module: "commonjs"`, not ESM) — points at `ems-ts/data/`.

```typescript
28: type EMSSnapshot = {
29:   employees: Employee[];
30:   departments: Department[];
31:   projects: Project[];
32:   savedAt: string;
33: };
```

- **Line 28–33** — the bonus `EMSSnapshot` type from the courseware, using
  `type` (not `interface`) for a plain data shape — either would work here,
  but `type` is used consistently for "just data" shapes throughout this
  file.

```typescript
37: async function saveAll<T>(filePath: string, items: T[]): Promise<void> {
38:   await fs.promises.mkdir(path.dirname(filePath), { recursive: true });
39:   await fs.promises.writeFile(filePath, JSON.stringify(items, null, 2), "utf-8");
40: }
42: async function loadAll<T>(filePath: string): Promise<T[]> {
43:   try {
44:     const raw = await fs.promises.readFile(filePath, "utf-8");
45:     return JSON.parse(raw) as T[];
46:   } catch {
47:     return [];
48:   }
49: }
51: function seedRepo<T extends Identifiable>(items: T[]): Repository<T> {
52:   const repo = new Repository<T>();
53:   items.forEach(item => repo.add(item));
54:   return repo;
55: }
```

- **Line 37–40** — `saveAll<T>` — a standalone generic function (the
  courseware describes this as a `BaseService` method; this file implements
  it as a free function instead, applied per-entity-array in `main()`).
  Line 38 ensures the target directory exists (`mkdir` with `recursive:
  true`, safe to call even if it already exists) before line 39 writes
  pretty-printed JSON (`JSON.stringify(items, null, 2)`).
- **Line 42–49** — `loadAll<T>` — reads and `JSON.parse`s the file (line
  44–45, asserting the parsed result `as T[]` since `JSON.parse`'s return
  type is always `any`), and the bare `catch { return []; }` (line 46–47)
  is exactly the courseware's requirement: a missing file returns an empty
  array, not a thrown error — callers don't need their own try/catch around
  every load call.
- **Line 51–55** — `seedRepo<T extends Identifiable>` — a small helper
  consolidating the "new Repository + forEach add" pattern seen inline in
  `seedRepositories()` (Assignment 2) into a one-liner reusable per entity
  type.

```typescript
59: class ReportService {
60:   constructor(
61:     private empRepo: Repository<Employee>,
62:     private deptRepo: Repository<Department>,
63:     private projectRepo: Repository<Project>
64:   ) {}
66:   departmentSummary(): { dept: string; headcount: number; avgSalary: number }[] {
67:     return this.deptRepo.getAll().map(dept => {
68:       const members = this.empRepo.query(e => e.departmentId === dept.id);
69:       const avgSalary =
70:         members.length === 0
71:           ? 0
72:           : Math.round(members.reduce((s, e) => s + e.salary, 0) / members.length);
73:       return { dept: dept.name, headcount: members.length, avgSalary };
74:     });
75:   }
77:   projectStatus(): { title: string; teamSize: number; daysLeft: number; overdue: boolean }[] {
78:     return this.projectRepo.getAll().map(p => {
79:       const daysLeft = daysUntilDeadline(p);
80:       return {
81:         title: p.title,
82:         teamSize: p.employeeIds.length,
83:         daysLeft,
84:         overdue: daysLeft < 0,
85:       };
86:     });
87:   }
88: }
```

- **Line 59–64** — `ReportService` — a plain (non-`BaseService`) class,
  taking all three repositories via three separate `private` parameter
  properties, since reports need to cross-reference employees against
  departments and projects rather than operate on a single entity type.
- **Line 66–75** — `departmentSummary()` — for each department (line 67),
  queries employees belonging to it (line 68), computes an average salary
  guarding against divide-by-zero for empty departments (the ternary on
  lines 70–72), and returns an inline-typed object per department — the
  exact shape declared in the method's return-type annotation (line 66).
- **Line 77–87** — `projectStatus()` — reuses `daysUntilDeadline` (line
  79, imported from Assignment 1) to compute `daysLeft` and derives
  `overdue: daysLeft < 0` (line 84) inline, rather than reusing
  `ProjectService.getOverdue()` from Assignment 3 — a slightly different
  code path than Assignment 3's, but same underlying logic
  (`daysUntilDeadline(p) < 0`).

```typescript
100: let loadedEmps   = await loadAll<Employee>(empFile);
101: let loadedDepts  = await loadAll<Department>(deptFile);
102: let loadedProjs  = await loadAll<Project>(projFile);
105: loadedProjs = loadedProjs.map(p => ({ ...p, deadline: new Date(p.deadline) }));
107: const emps   = loadedEmps.length   ? loadedEmps   : mockEmps;
108: const depts  = loadedDepts.length  ? loadedDepts  : mockDepts;
109: const projs  = loadedProjs.length  ? loadedProjs  : mockProjects;
111: const source = loadedEmps.length ? "saved files" : "mock data (first run)";
```

- **Line 100–102** — loads each entity type via `loadAll<T>`, generic type
  argument supplying the exact type per call.
- **Line 105** — **this is the JSON round-trip gotcha the courseware
  implicitly tests for**: `Project.deadline` is typed `Date` (Assignment
  1, line 33), but `JSON.stringify`/`JSON.parse` serialize `Date` objects
  as plain ISO strings with no way to auto-restore them — so after
  `loadAll<Project>`, every `deadline` is actually a `string` at runtime
  even though TS still believes it's `Date` (the type assertion `as T[]`
  inside `loadAll`, line 45, is a lie at this point). Line 105 manually
  reconstructs real `Date` objects (`new Date(p.deadline)`) before the data
  is used anywhere that calls `.getTime()` (i.e., `daysUntilDeadline`) —
  without this line, `daysUntilDeadline` would throw or silently misbehave
  on loaded data.
- **Line 107–109** — falls back to the Assignment 1 mock data if nothing
  was loaded (`.length` check on each loaded array).
- **Line 111** — determines the log message based on whether real data was
  loaded — a nice small UX touch to make the first-run vs. subsequent-run
  behavior visible in the console.

```typescript
119: const bus         = new EventBus();
120: const empService  = new AsyncEmployeeService(empRepo, bus);
121: const projService = new AsyncProjectService(projRepo, bus);
122: const reporter    = new ReportService(empRepo, deptRepo, projRepo);
132: await empService.addAsync({
133:   id: 8, name: "Helen", role: "engineer",
134:   salary: 74000, status: "active", departmentId: 1,
135: });
137: await projService.assignEmployeeAsync(1, 8);
138: await empService.promoteAsync(8, "manager");
139: await empService.removeAsync(6);   // remove inactive Frank
```

- **Line 119–122** — wires up the same `AsyncEmployeeService`/
  `AsyncProjectService` classes from Assignment 4, plus the new
  `ReportService`, all sharing the repositories seeded from loaded-or-mock
  data.
- **Line 132–139** — the required operation sequence: add Helen, assign her
  to project 1, promote her to manager, and remove Frank (id 6, the
  `"inactive"` manager planted in Assignment 1's mock data specifically to
  give this step something realistic to remove).

```typescript
147: console.log("--- Department Summary ---");
148: console.table(reporter.departmentSummary());
150: console.log("--- Project Status ---");
151: console.table(reporter.projectStatus());
154: await saveAll(empFile,  empRepo.getAll());
155: await saveAll(deptFile, deptRepo.getAll());
156: await saveAll(projFile, projRepo.getAll());
160: const snapshot: EMSSnapshot = {
161:   employees:   empRepo.getAll(),
162:   departments: deptRepo.getAll(),
163:   projects:    projRepo.getAll(),
164:   savedAt:     new Date().toISOString(),
165: };
167: const snapshotFile = path.join(DATA_DIR, "ems-snapshot.json");
168: await fs.promises.writeFile(snapshotFile, JSON.stringify(snapshot, null, 2), "utf-8");
```

- **Line 147–151** — `console.table()` is used exactly as the courseware
  specifies, printing both reports as formatted tables straight from the
  array-of-objects shapes `departmentSummary()`/`projectStatus()` return.
- **Line 154–156** — saves each repository's current state back to its own
  JSON file via `saveAll`, completing the persistence round-trip (loaded at
  the top of `main()`, saved again at the bottom, so the next run picks up
  where this one left off).
- **Line 160–168** — builds the bonus `EMSSnapshot` (typed against the
  interface at line 28–33), stamping `savedAt` with `new
  Date().toISOString()`, and writes it directly with `fs.promises.writeFile`
  rather than going through `saveAll` (since `saveAll` takes an array, and
  the snapshot is a single object, not an array).
- **Line 171** — `main();` — same fire-and-forget top-level invocation
  pattern as Assignment 4.

---

# PART D — Socket.io Reference

(Numbered sections below map 1:1 to `socketio-quick-reference.md`.)

## 1. The Two Core Objects

| Object | What it is | Available in |
|---|---|---|
| `io` | The entire server — represents all connections | Server only |
| `socket` | One individual connection — one browser tab | Server + Client |

Conceptually: `io` is the broadcast bus, `socket` is one subscriber
connection on it. If you've worked with MQTT in a homelab context, `io` is
roughly the broker's global reach and a `socket` is one client's live
connection/session — except Socket.io's "topics" (rooms, below) are joined
dynamically per-connection at runtime rather than being a fixed topic tree,
and delivery is over a persistent bidirectional WebSocket (falling back to
HTTP long-polling) rather than pub/sub over a separate broker process.

## 2. Emission Methods

| Method | Sends to |
|---|---|
| `io.emit(event, data)` | Every connected socket |
| `socket.emit(event, data)` | This socket only (sender) |
| `socket.to(room).emit(event, data)` | Room members **except** sender |
| `io.to(room).emit(event, data)` | Room members **including** sender |
| `socket.to(socketId).emit(event, data)` | One specific socket (private message) |
| `socket.broadcast.emit(event, data)` | Everyone **except** this socket (no room) |

This table is the single most important reference for reading
`notifications.js` (Part E) — every line in that file's handlers uses one of
these six emission forms, and picking the wrong one is the most common bug
class in Socket.io code (e.g. using `io.emit` when you meant
`socket.broadcast.emit`, which would echo an event back to its own sender).

## 3. Server-side Methods

| Method | What it does |
|---|---|
| `io.on('connection', cb)` | Listen for new client connections |
| `socket.on(event, cb)` | Listen for a custom event from this client |
| `socket.join(room)` | Add this socket to a named room |
| `socket.leave(room)` | Remove this socket from a room |
| `socket.disconnect()` | Force-disconnect this socket |

`socket.join(room)` is how a socket gets added to an arbitrary logical
group — rooms aren't declared anywhere in advance, they're created
implicitly the first time any socket joins them, and destroyed implicitly
when the last member leaves.

## 4. Server-side Properties

| Property | What it holds | Example value |
|---|---|---|
| `socket.id` | Unique ID for this connection | `"T9sneGYO3NreXAllAAAB"` |
| `socket.rooms` | Set of rooms this socket is in | `Set { "T9sne...", "general" }` |
| `socket.handshake.query` | Query params passed on connect | `{ token: "abc123" }` |
| `socket.handshake.address` | Client's IP address | `"192.168.1.5"` |
| `io.engine.clientsCount` | Total number of connected clients | `42` |

Note `socket.rooms` always contains the socket's own `id` as an implicit
default room — every socket is automatically in a private room named after
its own `id`, which is exactly what enables `io.to(socketId).emit(...)` to
work as a private-message mechanism (Part D.2).

## 5. Client-side Methods & Properties

| Method | What it does |
|---|---|
| `io(url)` | Connect to the server |
| `socket.emit(event, data, cb)` | Send an event to the server |
| `socket.on(event, cb)` | Listen for an event from the server |
| `socket.off(event)` | Stop listening for an event |
| `socket.disconnect()` | Disconnect from the server |

| Property | What it holds | Example value |
|---|---|---|
| `socket.id` | This client's socket ID | `"T9sneGYO3NreXAllAAAB"` |
| `socket.connected` | Is currently connected? | `true` / `false` |

The client API is deliberately near-symmetric with the server's
`socket`-level API (`emit`/`on` exist on both sides) — the asymmetry is that
only the server has an `io` object representing *all* connections; a client
only ever sees its own single `socket`.

## 6. Built-in Events — Server

| Event | When it fires |
|---|---|
| `connection` | A new client connects |
| `disconnect` | A client disconnects |
| `disconnecting` | Client about to disconnect (rooms still accessible) |

```javascript
io.on('connection', (socket) => {
  socket.on('disconnecting', () => {
    console.log(socket.rooms); // can still read rooms here
  });
  socket.on('disconnect', (reason) => {
    console.log('Disconnected:', reason);
  });
});
```

The `disconnecting` vs `disconnect` distinction matters because by the time
`disconnect` fires, Socket.io has already removed the socket from all its
rooms — so any cleanup logic that needs to know *which rooms the socket was
in* (e.g., notifying other room members) must run in `disconnecting`, not
`disconnect`. `notifications.js` (Part E) instead tracks room/user
membership in its own `connectedUsers` Map rather than relying on
`socket.rooms`, sidestepping this ordering issue entirely.

## 7. Built-in Events — Client

| Event | When it fires |
|---|---|
| `connect` | Successfully connected to server |
| `disconnect` | Disconnected from server |
| `connect_error` | Failed to connect |
| `reconnect` | Successfully reconnected |

```javascript
socket.on('connect', () => { console.log('Connected:', socket.id); });
socket.on('disconnect', (reason) => { console.log('Disconnected:', reason); });
socket.on('connect_error', (err) => { console.error('Error:', err.message); });
socket.on('reconnect', (attempt) => { console.log('Reconnected after', attempt, 'attempts'); });
```

## 8. Disconnect Reasons

| Reason | Meaning |
|---|---|
| `transport close` | Browser tab closed or network dropped |
| `server namespace disconnect` | Server called `socket.disconnect()` |
| `client namespace disconnect` | Client called `socket.disconnect()` |
| `ping timeout` | Client stopped responding to heartbeats |

The heartbeat mechanism behind `ping timeout` is Socket.io's equivalent of
an MQTT keep-alive/last-will: the server pings on an interval
(`pingInterval`) and if no pong comes back within `pingTimeout` (Part D.11),
it tears down the connection and fires `disconnect` with this reason.

## 9. Acknowledgement Pattern

```javascript
// Client — emits and waits for confirmation
socket.emit('join', { username: 'Alice', room: 'general' }, (error) => {
  if (error) {
    alert(error);
  } else {
    console.log('Joined successfully');
  }
});

// Server — receives and calls back
socket.on('join', (data, callback) => {
  const { error, user } = addUser(data);
  if (error) return callback(error);
  callback();
});
```

An acknowledgement is just a callback function passed as the *last*
argument to `emit`; Socket.io transports it over the wire and invokes it on
the other side when the corresponding `callback(...)` is called. It's the
one place Socket.io gives you a request/response-style round trip on top of
what's otherwise a fire-and-forget event system — conceptually similar to
an HTTP webhook that expects a 200 back, except the "response" here is an
arbitrary payload, not just a status code.

## 10. Attach Custom Data to a Socket

```javascript
socket.on('join', ({ username, room }) => {
  socket.username = username;
  socket.room     = room;
});

socket.on('sendMessage', (text) => {
  console.log(socket.username);
  io.to(socket.room).emit('message', { username: socket.username, text });
});
```

Because each `socket` object persists for the lifetime of the connection,
attaching arbitrary properties to it (`socket.username = ...`) is a simple
way to carry per-connection state across every other event handler
registered on that same socket, without a separate session store — this is
the pattern `notifications.js` uses its own `connectedUsers` Map for
instead (Part E), which is a slightly more explicit variant of the same
idea (external map keyed by `socket.id` rather than properties bolted
directly onto the socket object).

## 11. Namespace vs Room

| | Namespace | Room |
|---|---|---|
| What | A separate connection endpoint | A logical group within a namespace |
| How client joins | Connects to it: `io('/admin')` | Server calls `socket.join('room')` |
| Default | `/` — all sockets are here | None — must join explicitly |
| Use case | Separate concerns on one server | Chat rooms, game lobbies, doc sessions |

```javascript
const adminNamespace = io.of('/admin');
adminNamespace.on('connection', (socket) => {
  console.log('Admin connected');
});

socket.join('general');
io.to('general').emit('message', { text: 'Hello room!' });
```

Neither of the course's two Socket.io code files uses a custom namespace —
both operate entirely in the default `/` namespace, using rooms only
(`notifications.js`'s `employee:${employeeId}` per-user room, Part E).

## 12. Server Setup Options

```javascript
const io = new Server(httpServer, {
  cors:             { origin: '*' },
  pingTimeout:      5000,
  pingInterval:     10000,
  maxHttpBufferSize: 1e6,
});
```

- `cors.origin` — which origins may open a Socket.io connection to this
  server (same purpose as Express CORS middleware, applied at the
  WebSocket/polling transport layer instead of HTTP routes).
- `pingTimeout` / `pingInterval` — the heartbeat parameters referenced in
  Part D.8's `ping timeout` disconnect reason.
- `maxHttpBufferSize` — caps the size of any single message (default 1MB) —
  a safeguard against a client flooding the server with an oversized
  payload.

## 13. Minimal Working Example

```javascript
// server.js
import { createServer } from 'http';
import { Server } from 'socket.io';

const io = new Server(createServer());

io.on('connection', (socket) => {
  console.log('Connected:', socket.id);

  socket.on('message', (text) => {
    io.emit('message', `${socket.id} says: ${text}`);
  });

  socket.on('disconnect', () => {
    console.log('Disconnected:', socket.id);
  });
});

io.listen(3001);
```

```javascript
// client.js
import { io } from 'socket.io-client';

const socket = io('http://localhost:3001');

socket.on('connect', () => {
  socket.emit('message', 'Hello everyone!');
});

socket.on('message', (data) => {
  console.log(data);
});
```

This minimal example is the skeleton both real course files below extend:
`io.on('connection', socket => { socket.on(event, handler) ...
socket.on('disconnect', ...) })` on the server, `socket.on('connect', ...)`
/ `socket.emit(...)` on the client.

---

# PART E — Socket.io Demo Files

## E.1 — `notifications.js` (Express EMS backend)

This is the course's real, fully implemented Socket.io module — it wires a
chat/notification layer into the Express-based EMS backend.

```javascript
1:  // src/sockets/notifications.js
2:  // Demonstrates: Real-Time Web Applications with Socket.io (Chat App topic)
4:  const setupSocketIO = (io) => {
5:    // Track connected users: socketId → employeeId
6:    const connectedUsers = new Map();
8:    io.on('connection', (socket) => {
9:      console.log(`🔌 Socket connected: ${socket.id}`);
```

- **Line 4** — `setupSocketIO = (io) => { ... }` — the whole module is a
  single factory function taking the server's `io` instance (constructed
  elsewhere, e.g. `new Server(httpServer)`, and passed in) — this is the
  standard pattern for keeping Socket.io wiring in its own file separate
  from Express route files.
- **Line 6** — `connectedUsers = new Map()` — a **module-level** (really,
  closure-level — scoped to one call of `setupSocketIO`) map from
  `socket.id` to `{ employeeId, name }`. This is the "attach custom data"
  pattern from Part D.10, implemented as an external map instead of
  properties bolted onto `socket` directly.
- **Line 8** — `io.on('connection', (socket) => { ... })` — the standard
  entry point (Part D.6): fires once per new client connection, and every
  handler below is registered *inside* this callback, scoped to that one
  `socket`.
- **Line 9** — logs the new connection's `socket.id` immediately.

```javascript
12: socket.on('identify', ({ employeeId, name }) => {
13:   connectedUsers.set(socket.id, { employeeId, name });
14:   socket.join(`employee:${employeeId}`); // personal room
16:   // Notify others
17:   socket.broadcast.emit('user:online', { employeeId, name });
19:   // Send current online list back to the new joiner
20:   const onlineList = [...connectedUsers.values()];
21:   socket.emit('users:online', onlineList);
23:   console.log(`👤 Employee identified: ${name} (${employeeId})`);
24: });
```

- **Line 12** — `socket.on('identify', ({ employeeId, name }) => {...})` —
  a custom application event (not a Socket.io built-in): fires when the
  client sends `socket.emit('identify', { employeeId, name })` after
  connecting, since the initial `connection` event has no knowledge of
  *who* the user is yet.
- **Line 13** — records this socket's identity in `connectedUsers`.
- **Line 14** — `socket.join(\`employee:${employeeId}\`)` — joins a
  **personal room** named after the employee's id. This is exactly what
  makes private, per-employee notifications possible later
  (`io.to('employee:5').emit(...)`, line 33) — a room with (usually) exactly
  one member, used as an addressable private channel rather than a
  broadcast group.
- **Line 17** — `socket.broadcast.emit('user:online', ...)` — Part D.2's
  "everyone except this socket" form: every *other* connected client is
  told this employee just came online; the newly-identified socket itself
  doesn't need to be told it's online.
- **Line 20–21** — builds the current online-user list
  (`[...connectedUsers.values()]`) and sends it back with `socket.emit`
  (Part D.2's "this socket only" form) — this is how a freshly-connected
  client discovers who else is already online, since it missed all the
  earlier `user:online` broadcasts.
- **Line 23** — logs the identification server-side.

```javascript
27: socket.on('notify:all', ({ message, type = 'info' }) => {
28:   io.emit('notification', { message, type, timestamp: new Date() });
29: });
32: socket.on('notify:employee', ({ employeeId, message, type = 'info' }) => {
33:   io.to(`employee:${employeeId}`).emit('notification', {
34:     message,
35:     type,
36:     timestamp: new Date(),
37:   });
38: });
```

- **Line 27–29** — `notify:all` — a global-broadcast custom event. Uses
  `io.emit` (Part D.2's "every connected socket," including the sender
  itself) rather than `socket.broadcast.emit`, since a system-wide
  notification should reach the socket that triggered it too. `type =
  'info'` is a default parameter applied via destructuring if the caller
  omits it.
- **Line 32–38** — `notify:employee` — targets a single employee's personal
  room (joined at line 14) with `io.to(room).emit(...)` — Part D.2's
  room-targeted, sender-inclusive form; since only that one employee's
  socket(s) are members of `employee:${employeeId}`, this is effectively a
  private message even though it's technically a room broadcast.

```javascript
41: socket.on('project:update', ({ projectId, projectName, status }) => {
42:   io.emit('project:updated', {
43:     projectId,
44:     projectName,
45:     status,
46:     updatedBy: connectedUsers.get(socket.id),
47:     timestamp: new Date(),
48:   });
49: });
```

- **Line 41–49** — `project:update` — broadcasts a project status change to
  everyone (`io.emit`, line 42) and enriches the payload with
  `updatedBy: connectedUsers.get(socket.id)` (line 46), looking up the
  sender's identity from the map populated back in the `identify` handler
  — demonstrating exactly why that map exists: to attribute later events to
  a human-readable employee identity instead of just a raw socket id.

```javascript
51: socket.on('disconnect', () => {
52:   const user = connectedUsers.get(socket.id);
53:   if (user) {
54:     socket.broadcast.emit('user:offline', user);
55:     connectedUsers.delete(socket.id);
56:   }
57:   console.log(`🔌 Socket disconnected: ${socket.id}`);
58: });
59:  });
60: };
62: module.exports = setupSocketIO;
```

- **Line 51** — `socket.on('disconnect', () => {...})` — the built-in event
  (Part D.6), fires when the connection drops for any reason.
- **Line 52–56** — looks up whether this socket had ever identified itself
  (guards against a socket disconnecting before ever sending `identify`,
  line 53's `if (user)`), broadcasts `user:offline` to everyone else (line
  54, mirroring the `user:online` broadcast from the `identify` handler),
  then cleans up the map entry (line 55) — without this deletion,
  `connectedUsers` would leak an entry per disconnected socket forever.
- **Line 57** — logs the disconnection.
- **Line 59** — closes the `io.on('connection', ...)` callback.
- **Line 60–62** — closes `setupSocketIO` and exports it via CommonJS
  (`module.exports`), matching this file's plain-JS (not TS) nature — it's
  called elsewhere in the Express app's bootstrap with the constructed `io`
  instance, e.g. `setupSocketIO(io)`.

## E.2 — `socket-demo.js` (standalone demo)

```javascript
1:  // socket-demo.js
```

This file is a **stub** — its entire content is a single header comment,
with no actual implementation. There is no server setup, no
`io.on('connection', ...)`, and no client code in it to walk through. For
assessment purposes, treat every Socket.io *pattern* (connection handling,
custom events, rooms, broadcasting, cleanup on disconnect) as covered
exclusively by `notifications.js` above and by the reference examples in
Part D — this file exists as a placeholder in the project layout but was
never filled in with a second worked example.

---

# Quick Self-Check Index

- TS type system fundamentals (primitives, unions, literals, `any` vs
  `unknown` vs `never` vs `void`) — Part A.2, demoed in `script.ts` (Part
  B.1).
- Interfaces vs type aliases, structural vs nominal typing — Part A.3,
  demoed in `ts-interfaces.ts` (Part B.3).
- Classes, access modifiers, `#`-private vs TS `private`, `implements`,
  `abstract` — Part A.4, demoed in `ts-classes-etc.ts` (Part B.2).
- Advanced types: discriminated unions, type guards, utility/mapped/
  conditional types — Part A.5 (no dedicated demo file; applied throughout
  the EMS project, e.g. `Partial<T>` in `Repository.update`).
- Generics and generic constraints — Part A.6, the backbone of
  `Repository<T extends Identifiable>` (Part C.4) and `BaseService<T
  extends Identifiable>` (Part C.5).
- Decorators — Part A.7, applied as `@LogCall` in `assignment3.ts` (Part
  C.5).
- The full 5-assignment EMS build order and cross-assignment dependencies —
  Part C, especially the courseware summary (C.1) and each file's import
  lines.
- Socket.io core objects, emission methods, built-in events, rooms vs
  namespaces, acknowledgements — Part D.
- A complete, real Socket.io chat/notification server with room-based
  private messaging and connection-state tracking — Part E.1
  (`notifications.js`); `socket-demo.js` is an empty stub with nothing to
  study (Part E.2).
