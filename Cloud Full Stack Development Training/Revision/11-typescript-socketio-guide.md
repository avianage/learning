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

**Why TypeScript over plain JavaScript** — the course's `script.ts` demo
(walked through line-by-line in Part B.1) makes the case concretely: an
untyped `salary` lets you reassign it to a string with zero warning until
the bug surfaces at runtime, while an explicit `number` annotation turns
that same mistake into a compile-time error. TypeScript brings that
guarantee to JS/Node without giving up JS's dynamic runtime or npm
ecosystem.

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
scripted walkthrough meant to be uncommented live in class, one section at a
time. It reveals the same story reference section 2 tells conceptually:

```typescript
let salary = 90000.25;        // no annotation — TS still infers `number`
let salary;                   // no initializer, no annotation → strict mode error:
                               // "implicitly has an 'any' type"
salary = 'abc';                // once salary is `number`: ts(2322) compile error
let salary: number = 10.25;   // explicit annotation — same effect, documents intent

const addNums = (a: number, b: number): number => { return a + b; };
const addNumsPrint = (a: number, b: number): void => { console.log(a + b); };

let myData: string | number;
myData = 'abc';
myData = 10.25;
// myData = false;            // Error — boolean isn't part of the union

let myData2: any;             // accepts anything, no complaints — avoid
let myData3: unknown;         // accepts anything too, but requires narrowing before use
```

- Even without an annotation, `let salary = 90000.25` is still checked as
  `number` via **type inference** — untyped-*looking* isn't the same as
  untyped.
- `myData2: any` vs `myData3: unknown` is the file's concrete illustration
  of why `unknown` is the safe escape hatch: both accept any assignment,
  but only `any` lets you call arbitrary methods on the result with zero
  compiler pushback.

The rest of the file (primitives, arrays, tuples, enums, `const enum`,
`any`/`unknown`/`never`/`void`, type assertions) is a verbatim copy of
reference section 2 kept for live uncommenting — already covered in Part
A.2.

## B.2 — `ts-classes-etc.ts`

A reveal script (mostly commented out) that builds up the same `Animal`
class from reference section 4, one capability at a time:

```typescript
class Animal {
    readonly id: number;
    public name: string;
    protected species: string;
    #hashField: string;            // true private (ES2022 field)

    constructor(name: string, species: string, hf: string) {
        this.id = Math.random();
        this.name = name;
        this.species = species;
        this.#hashField = hf;
    }
}
const anml1 = new Animal('dog', 'dog specie', 'hashvalue');
// anml1.#hashField = 'abc';     // error: only accessible inside the class body
```

- The class first appears with just `readonly`/`public`/`protected`/`#private`
  fields and a constructor, to isolate access-modifier syntax before adding
  behavior. `anml1.#hashField = 'abc'` is commented out because JS's own
  private-field enforcement rejects it — this fails before TS's compiler
  even gets involved, and the field is invisible to casual enumeration
  (`Object.keys`), proving the privacy is real, not cosmetic.

A commented-out fuller version then adds a `speak()` method interpolating
`#sound`, a `get info()` accessor, a `set sound(value)` setter (the *only*
way to mutate `#sound` from outside), and a `static create(...)` factory —
matching reference section 4's complete `Animal` shape.

Later blocks build on it progressively, each still commented out for live
reveal:

- `Dog extends Animal` uses **parameter properties** (`public breed: string,
  private readonly age: number` right in the constructor parameter list) to
  declare-and-assign in one step instead of a separate field declaration
  plus `this.breed = breed;`. Its `speak()` override needs no `@Override`
  keyword — TS checks override-compatibility structurally.
- `interface Serializable` (`toJSON()`, `toString()`) and `class User
  implements Serializable` reuse the same parameter-property shorthand on a
  plain, non-inherited class.
- `abstract class Shape` has two bodyless `abstract` methods plus one
  concrete `describe()` that calls them polymorphically; `Circle extends
  Shape` supplies the concrete implementations.
- A final block shows the **compiled-JS-shaped** equivalent of the class:
  every `: type`, `readonly`, and `public`/`protected`/`private` keyword is
  gone, while `#sound` survives untouched — because `#field` is native JS
  syntax, it's the one privacy mechanism TS doesn't need to erase.

## B.3 — `ts-interfaces.ts`

The entire file is commented out (a reveal script), directly implementing
reference section 3: `interface User` with a `readonly id`, an optional
`age?`, and an optional inline-nested `address?` object; a `user1: User`
literal that legally omits both optional fields; then `interface AdminUser
extends User` adding `role: 'admin'` (a literal type) and `permissions:
string[]`, with `adminUser: AdminUser` supplying every required field from
both interfaces.

- `user1.id = 2` is left commented out specifically because it *would*
  error — `readonly` is enforced at every write site after construction,
  not just the first one — while `user1.name = 'Monu'` is left uncommented
  since `name` has no `readonly` modifier.
- The `readonly` constraint on `id` still applies through the `extends`
  chain: `adminUser.id` can also only be set once, even though `id` is
  declared on the parent interface, not `AdminUser` itself.

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

- `"target": "ES2020"` supports the optional chaining/nullish coalescing
  used throughout the assignments; `"module": "commonjs"` matches how
  `ts-node` actually runs these files (`npx ts-node src/assignment1.ts`),
  unlike the reference's suggested `NodeNext` for a pure-ESM setup.
- `"lib": ["ES2020", "dom"]` includes `dom` defensively even though this is
  a CLI app with no browser APIs — harmless but broader than strictly
  required.
- `"strict": true` is what makes `let salary;` error in `script.ts`, and
  forces every model field to be fully typed with no silent gaps.
- `"experimentalDecorators"` + `"emitDecoratorMetadata"` are required
  specifically for `assignment3.ts`'s `@LogCall` decorator.
- `"types": ["node"]` restricts auto-included ambient `@types/*` packages to
  just `@types/node`, instead of pulling in every `@types/*` package found in
  `node_modules`.

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

- `Role`/`Status` are **literal-union types**, not `enum`s — a deliberate
  choice throughout the EMS project: string literal unions serialize to
  plain JSON strings with no runtime object needed, which matters for
  Assignment 5's JSON persistence (an `enum`'s numeric values/reverse-mapping
  object would complicate the round trip). `Employee.role: Role` reusing this
  type (rather than `string`) is what lets `promote(id, newRole: Role)` in
  Assignment 3 reject a typo like `"managr"` at compile time.
- `Employee.email?: string` is the only optional field in the whole model.
- `Project.deadline: Date` is a real `Date` object, not a string — this
  choice is what forces Assignment 5 to manually reconstruct `Date` objects
  after loading from JSON, since JSON has no native date type (see C.7).
- `daysUntilDeadline` converts the millisecond gap to days with
  `Math.ceil(...)` — rounding up so "tomorrow at 1am" doesn't read as "0
  days left." Reused unmodified through Assignment 5's `projectStatus()`.

Mock data (`departments`, `employees`, `projects`) is explicitly typed as
arrays of the interfaces above, so a malformed literal is caught
immediately. One project's `deadline` is deliberately set in the past — a
planted fixture so `getOverdue()` (Assignment 3) always has a real overdue
project to report without depending on the current date. A closing
`console.log` demo block re-runs every time a later assignment imports this
file, which is why running `assignment5.ts` reprints every earlier
assignment's demo output too.

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

- `Identifiable { id: number }` is the minimal structural constraint:
  `Employee`, `Department`, and `Project` all qualify automatically since
  none of them needs to write `implements Identifiable` (structural typing).
- `class Repository<T extends Identifiable>` — the `extends Identifiable`
  constraint is what allows `item.id` to be referenced safely inside the
  class body; without it TS has no reason to believe `T` has an `id` at all.
- `update(id, changes: Partial<T>): boolean` is `Partial<T>` (Part A.5) in
  real use: `changes` supplies any subset of `T`'s fields, spread over the
  existing object (`{ ...existing, ...changes }`) so an update to just
  `{ salary: 75000 }` leaves every other field untouched.
- `findById` returns `T | undefined`, forcing every caller to handle "not
  found" — this is why the demo below reads `bob?.salary` with optional
  chaining rather than `bob.salary`.
- `query(predicate: (item: T) => boolean): T[]` is a typed higher-order
  parameter — it's what lets `empRepo.query(e => e.status === "active")`
  type-check `e` as `Employee` automatically with no extra annotation.

`seedRepositories()` instantiates three separately typed `Repository<T>`s
and fills each from Assignment 1's mock arrays — this exact function is
re-called by Assignment 3's `buildServices()` (C.5) and Assignment 4's
`main()`. The file's demo section exercises `query`, `update` +
`findById`, and `remove` in turn.

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

- `LogCall` is a **method decorator** matching the exact `(target, key,
  descriptor)` shape from Part A.7. It grabs the original method and
  replaces `descriptor.value` with a wrapper that calls it via
  `original.apply(this, args)` (preserving `this` binding) and logs `key`,
  `args`, and `result` as JSON — producing the `[LogCall] promote(...) =>
  ...` console lines seen when `@LogCall` is applied below. `_target` is
  prefixed with `_` by convention to signal "intentionally unused
  parameter."

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

- `abstract class BaseService<T extends Identifiable>` cannot be
  instantiated directly. Its constructor uses `protected repo:
  Repository<T>` — a parameter property this time `protected` rather than
  `public`/`private`, so subclasses can reach `this.repo` directly but
  outside callers cannot. Its five CRUD methods simply delegate to the
  matching `Repository<T>` method (a facade/delegation pattern).

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

- `EmployeeService extends BaseService<Employee>` locks the generic `T` to
  `Employee`. `getByDepartment` is a thin wrapper around `this.repo.query`,
  reachable only because `repo` is `protected`. `promote` (decorated with
  `@LogCall`) calls `this.repo.update(id, { role: newRole })` — note `{
  role: newRole }` satisfies `Partial<Employee>` with just one field.
  `getSalaryReport()` (also `@LogCall`-decorated) returns an **inline
  object type** (`{ total; average; highest }`) rather than a named
  interface — legal and common for one-off return shapes.

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

`ProjectService extends BaseService<Project>`. `assignEmployee` guards
against a missing project and a duplicate assignment, then updates
`employeeIds` immutably (`[...project.employeeIds, empId]`, never mutating
in place). `getOverdue()` reuses Assignment 1's `daysUntilDeadline` inside a
`query()` predicate — direct proof of the cross-assignment reuse chain.

`buildServices()` calls Assignment 2's `seedRepositories()` then wraps the
resulting repos in the two concrete services — this is the function
Assignment 4 and 5 both call to bootstrap the whole stack in one call.

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

- `AsyncEmployeeService extends EmployeeService`, wrapping the synchronous
  service with async, event-emitting versions of its methods rather than
  modifying it — the sync API stays available too. Its constructor takes
  both a `Repository<Employee>` (passed to `super(repo)`) and a `private
  bus: EventBus`. Each async method (`addAsync`, `promoteAsync`,
  `removeAsync`, `getSalaryReportAsync`) awaits a simulated `delay(300)`,
  calls the inherited sync method, and conditionally emits an event only if
  the operation actually succeeded.
- `newRole: Parameters<EmployeeService["promote"]>[1]` — `Parameters<F>`
  extracts a function type's parameter list as a tuple, `["promote"]`
  indexes into the method by name, `[1]` picks the second parameter. This
  evaluates to `Role`, but *derived* from `promote`'s actual signature
  rather than hand-typed again, so it stays in sync automatically if that
  signature ever changes — a TS capability with no Java parallel.

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

`AsyncProjectService` mirrors `AsyncEmployeeService`'s pattern. Its
constructor uses `Repository<import("./assignment1").Project>` — an
**inline import type**, purely for its type, avoiding a top-level named
import just for one parameter annotation. `getOverdueAsync` unconditionally
emits `"project:overdue-check"` with just a `count`, regardless of whether
any projects are actually overdue, since "the check ran" is itself the
event.

```typescript
91: async function main() {
97:   const { empRepo, projectRepo } = (() => {
98:     const { employeeService, projectService } = buildServices();
100:     const es = employeeService as unknown as { repo: Repository<Employee> };
101:     const ps = projectService as unknown as { repo: Repository<import("./assignment1").Project> };
102:     return { empRepo: es.repo, projectRepo: ps.repo };
103:   })();
```

An immediately-invoked arrow function extracts the private `repo` field out
of `employeeService`/`projectService` purely for demo purposes, using a
**double assertion** (`as unknown as { repo: ... }`): TS normally refuses a
direct cast between two unrelated types (`repo` is `protected`, so neither
side is structurally assignable to the other), and routing through
`unknown` first bypasses that check. The file's own comment flags this as a
demo-only escape hatch, not production practice. `EventBus` and both async
services are then constructed, wired to the same bus.

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

A loop subscribes the same logging handler to all five event names, then a
`try { sequence } catch (err: unknown)` block runs the required operation
sequence (add, assign, promote, report, check overdue, remove), each step
`await`-ed. `catch (err: unknown)` narrows with `err instanceof Error`
before accessing `.message` — the type-guard pattern from Part A.5, needed
here because a `catch` clause's error is typed `unknown` under `strict`
mode (not `any`), so it can't be used without narrowing first. `main()` is
invoked fire-and-forget at the bottom, since top-level `await` isn't
available under `module: "commonjs"`.

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

- `import * as fs`/`import * as path` are namespace imports, needed because
  Node's built-in modules don't have a single default export under
  `esModuleInterop`/`commonjs`.
- Imports span **all four** prior assignments — the single most direct
  evidence in the codebase of "Assignment 5 builds on everything." The mock
  arrays are renamed on import (`as mockDepts` etc.) to avoid colliding with
  the locally loaded data.
- `DATA_DIR` is resolved relative to `__dirname` (available under
  `commonjs`, not ESM).

```typescript
28: type EMSSnapshot = {
29:   employees: Employee[];
30:   departments: Department[];
31:   projects: Project[];
32:   savedAt: string;
33: };
```

The bonus `EMSSnapshot` type uses `type` (not `interface`) for a plain data
shape — either would work, but `type` is used consistently for "just data"
shapes throughout this file.

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

- `saveAll<T>` is a standalone generic function (the courseware describes
  this as a `BaseService` method; this file implements it as a free
  function instead). It ensures the target directory exists (`mkdir` with
  `recursive: true`) before writing pretty-printed JSON.
- `loadAll<T>` reads and `JSON.parse`s the file, asserting the result `as
  T[]` since `JSON.parse`'s return type is always `any`. Its bare `catch {
  return []; }` is the courseware's explicit requirement: a missing file
  returns an empty array rather than throwing, so callers don't need their
  own try/catch around every load call.
- `seedRepo<T extends Identifiable>` consolidates the "new Repository +
  forEach add" pattern from `seedRepositories()` (Assignment 2) into a
  reusable one-liner per entity type.

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

`ReportService` is a plain (non-`BaseService`) class taking all three
repositories via separate `private` parameter properties, since reports
cross-reference employees against departments and projects rather than
operate on one entity type. `departmentSummary()` computes an average
salary per department, guarding against divide-by-zero for empty
departments with a ternary. `projectStatus()` reuses `daysUntilDeadline` to
compute `daysLeft` and derives `overdue: daysLeft < 0` inline — a separate
code path from `ProjectService.getOverdue()` (Assignment 3) but the same
underlying logic.

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

Each entity type is loaded via `loadAll<T>`, then falls back to Assignment
1's mock data if nothing was loaded (`.length` check on each array).

**The JSON round-trip gotcha the courseware implicitly tests for**:
`Project.deadline` is typed `Date`, but `JSON.stringify`/`JSON.parse`
serialize `Date` objects as plain ISO strings with no way to auto-restore
them — so after `loadAll<Project>`, every `deadline` is actually a `string`
at runtime even though TS still believes it's `Date` (the `as T[]`
assertion inside `loadAll` is a lie at this point). The loaded projects are
manually mapped through `new Date(p.deadline)` to reconstruct real `Date`
objects before anything calls `.getTime()` on them (i.e.
`daysUntilDeadline`) — without this step, that function would throw or
silently misbehave on loaded data.

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

This wires up `AsyncEmployeeService`/`AsyncProjectService` plus the new
`ReportService`, sharing repositories seeded from loaded-or-mock data, then
runs the required operation sequence: add Helen, assign her to project 1,
promote her to manager, remove Frank (the `"inactive"` manager planted in
Assignment 1's mock data for this purpose).

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

`console.table()` prints both reports straight from the array-of-objects
shapes the report methods return. Each repository's state is saved back to
its own JSON file via `saveAll`, completing the persistence round-trip
(loaded at the top of `main()`, saved at the bottom, so the next run picks
up where this one left off). The bonus `EMSSnapshot` is written directly
with `fs.promises.writeFile` rather than through `saveAll`, since `saveAll`
takes an array and the snapshot is a single object. `main()` is invoked
fire-and-forget, same as Assignment 4.

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
const setupSocketIO = (io) => {
  const connectedUsers = new Map(); // socketId → { employeeId, name }

  io.on('connection', (socket) => {
    console.log(`Socket connected: ${socket.id}`);

    socket.on('identify', ({ employeeId, name }) => {
      connectedUsers.set(socket.id, { employeeId, name });
      socket.join(`employee:${employeeId}`); // personal room

      socket.broadcast.emit('user:online', { employeeId, name });

      const onlineList = [...connectedUsers.values()];
      socket.emit('users:online', onlineList);
    });

    socket.on('notify:all', ({ message, type = 'info' }) => {
      io.emit('notification', { message, type, timestamp: new Date() });
    });

    socket.on('notify:employee', ({ employeeId, message, type = 'info' }) => {
      io.to(`employee:${employeeId}`).emit('notification', { message, type, timestamp: new Date() });
    });

    socket.on('project:update', ({ projectId, projectName, status }) => {
      io.emit('project:updated', {
        projectId, projectName, status,
        updatedBy: connectedUsers.get(socket.id),
        timestamp: new Date(),
      });
    });

    socket.on('disconnect', () => {
      const user = connectedUsers.get(socket.id);
      if (user) {
        socket.broadcast.emit('user:offline', user);
        connectedUsers.delete(socket.id);
      }
      console.log(`Socket disconnected: ${socket.id}`);
    });
  });
};

module.exports = setupSocketIO;
```

The whole module is a single factory function taking the server's `io`
instance (constructed elsewhere, e.g. `new Server(httpServer)`) — the
standard pattern for keeping Socket.io wiring in its own file, separate
from Express routes. `connectedUsers` is a closure-level map from
`socket.id` to `{ employeeId, name }` — the "attach custom data" pattern
from Part D.10, implemented as an external map instead of properties
bolted onto `socket` directly.

- `identify` is a custom application event (not a Socket.io built-in),
  fired by the client once it knows who the user is (the initial
  `connection` event doesn't). It joins a **personal room** named after the
  employee's id — a room with (usually) exactly one member, used as an
  addressable private channel — which is what later makes
  `io.to('employee:5').emit(...)` work as a private message. It then
  `socket.broadcast.emit`s `user:online` to everyone else, and separately
  `socket.emit`s the current online list back to just the new joiner (who
  missed all earlier broadcasts).
- `notify:all` uses `io.emit` (including the sender) rather than
  `socket.broadcast.emit`, since a system-wide notification should reach
  the socket that triggered it too. `notify:employee` targets a single
  employee's personal room with `io.to(room).emit(...)` — effectively a
  private message even though it's technically a room broadcast.
- `project:update` enriches its broadcast payload with `updatedBy:
  connectedUsers.get(socket.id)` — this is exactly why the map exists: to
  attribute events to a human-readable identity instead of a raw socket id.
- `disconnect` guards against a socket that disconnects before ever
  identifying (`if (user)`), broadcasts `user:offline` to mirror the
  `identify` handler's `user:online`, then deletes the map entry — without
  that deletion, `connectedUsers` would leak an entry per disconnect
  forever.

## E.2 — `socket-demo.js` (standalone demo)

This file is a **stub** — its entire content is a single header comment,
with no actual implementation: no server setup, no
`io.on('connection', ...)`, and no client code. Every Socket.io pattern
(connection handling, custom events, rooms, broadcasting, cleanup on
disconnect) is covered exclusively by `notifications.js` above and the
reference examples in Part D — this file is a placeholder that was never
filled in with a second worked example.

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
