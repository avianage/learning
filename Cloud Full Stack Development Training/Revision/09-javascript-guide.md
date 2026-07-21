# JavaScript — Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual JavaScript courseware (`03-js/01_Introduction.md` through `10_Modules_Modern_JS.md`, plus the MCQ assessment and discussion Q&A files) and in the 9 real demo scripts under `Code/UI (HTML, CSS, JS, Ts, Node)/js/`. Every example, gotcha, and line-by-line explanation below traces back to one of those source files — nothing is invented. Part I walks the 10 courseware modules topic by topic; Part II walks the real demo files (function syntaxes, callbacks, array ops, OOP, DOM, API calls) line by line, the way the Core Java guide walked real `.java` files, with an explicit JS-vs-Java OOP contrast since Aakash is coming from Java.

---

# Part I — Courseware Topics (Modules 01–10)

## 1. Introduction to JavaScript

JavaScript is **dynamic, interpreted, single-threaded, and garbage-collected**. Despite the name, it has no lineage relationship with Java — the similarity is marketing. Key contrasts for a Java developer:

| Concept | Java | JavaScript |
|---|---|---|
| Type system | Static, strong | Dynamic, weak |
| Compilation | Compiled → bytecode | JIT-compiled at runtime (parse → AST → Ignition bytecode → Turbofan machine code) |
| Concurrency | Multi-threaded (JVM threads) | Single-threaded + event loop |
| OOP model | Class-based inheritance | Prototype-based (`class` is sugar) |
| "Nothing" values | `null` only | `null` **and** `undefined` |
| Entry point | `public static void main` | Top-level code runs immediately |
| Modules | `package` | ES modules (`import`/`export`) |

**Mental model shift:** Java thinks "types first, behavior through methods on classes." JavaScript thinks "functions first — objects are dictionaries that optionally have prototypes." Expect to pass functions as arguments (callbacks), return functions from functions (closures/factories), and compose small functions constantly.

**Where it runs:** the same V8 engine powers Chrome and Node.js — only the available APIs differ (DOM/fetch/localStorage in the browser vs. `fs`/`http`/`process` in Node).

**ECMAScript (ES)** is the standard, maintained by TC39. Know the milestones: ES5 (2009, strict mode), ES6/ES2015 (`let`/`const`, arrow functions, classes, Promises, modules, destructuring, template literals), ES2017 (`async`/`await`), ES2020 (`?.`, `??`, `BigInt`), ES2022 (class fields, `at()`, top-level `await`, `structuredClone`). Write ES2020+.

**Key consequence of runtime compilation:** errors Java catches at compile time (type mismatches) only surface at *runtime* in plain JavaScript — this is the entire motivation for TypeScript.

`'use strict'` converts silent bugs (like an undeclared global assignment) into thrown errors. **ES modules are always strict automatically** — you rarely type it by hand in modern code.

---

## 2. Variables, Types & Coercion

### `var` / `let` / `const`

Rule: **`const` by default, `let` when you must reassign, never `var`.**

```javascript
const PI = 3.14159;      // block-scoped, binding cannot be reassigned
let counter = 0;         // block-scoped, reassignable
var x = 10;               // function-scoped, hoisted — avoid
```

`var` is dangerous because it leaks out of blocks and, in loops, all closures share the *same* variable:

```javascript
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100);
}
// Prints: 3 3 3 — every closure captured the same `i`

for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log(j), 100);
}
// Prints: 0 1 2 — `let` creates a fresh binding per iteration
```

`const` freezes the **binding**, not the value:

```javascript
const user = { name: 'Alice', age: 30 };
user.age = 31;           // ✅ mutating the object is fine
user = { name: 'Bob' };  // ❌ TypeError — reassigning the binding is not
```

### The 8 Data Types

**7 primitives + 1 object type**: `string`, `number` (one numeric type — no int/float/double split like Java), `bigint` (suffix `n`, for integers beyond `Number.MAX_SAFE_INTEGER` = 2^53−1), `boolean`, `undefined`, `null`, `symbol`, and `object` (arrays, functions, dates are all objects).

`typeof null === 'object'` is a famous historical bug — always check `value === null` directly, never rely on `typeof`.

### `null` vs `undefined`

`undefined` = the engine's way of saying "never assigned" (unassigned variable, missing property, function with no return). `null` = the programmer's explicit "no value here." Both are falsy; `value == null` is the one legitimate use of `==` since it catches both in one check.

### Type Coercion

The `+` operator: if **either** operand is a string, `+` becomes concatenation (`"5" + 3` → `"53"`, not `8`). `-`, `*`, `/` always coerce to numbers (`"10" - 5` → `5`). `1 + 2 + "3"` → `"33"` (left to right: numbers add first, then the string appends).

**`==` vs `===`** — the single most assessment-relevant rule in the language: `==` coerces types before comparing (`"5" == 5` → `true`, `0 == false` → `true`, `null == undefined` → `true` but `null == 0` → `false`, an inconsistency). `===` requires matching types with no coercion. **Always use `===`/`!==`**; the only accepted `==` usage is `value == null`.

### Truthy/Falsy — memorize the 8 falsy values

`false, 0, -0, 0n, "", null, undefined, NaN`. Everything else is truthy, including the classic gotchas `"0"`, `"false"`, `[]`, and `{}` — an empty array/object is truthy, so `if (!arr)` is never true for an existing array; check `arr.length === 0` instead.

### Explicit Conversion

`String(42)`, `Number("42")`, `Boolean(0)`, `!!value` (idiomatic double-negation to boolean), `parseInt("42px", 10)` (stops at first non-numeric char), unary `+"42"` (quick number conversion). Gotchas: `Number("")` → `0`, `Number(null)` → `0`, `Number(undefined)` → `NaN`.

### `NaN` and `Infinity`

`typeof NaN === 'number'` (quirk). `NaN === NaN` is `false` — NaN is the only value not equal to itself. Use `Number.isNaN(x)`, never the legacy `isNaN(x)` (which coerces first). `Number.isFinite()` similarly avoids coercion surprises.

---

## 3. Operators

**Arithmetic:** same as Java except `/` is always floating point (no integer division) and `**` is exponentiation (ES2016, replaces `Math.pow`). `Math.floor`/`Math.trunc` give integer division. Floating-point precision is IEEE 754 (shared with Java) — `0.1 + 0.2 !== 0.3`; fix display with `.toFixed(2)`, fix money math by working in integer cents/paise.

**Comparison:** always `===`/`!==`. String comparison is lexicographic — `"10" > "9"` is `false` because `"1" < "9"` character-wise; use numbers for numeric comparisons.

**Logical operators return operands, not booleans** — this is a real divergence from Java:

```javascript
"hello" && 42      // 42 — both truthy, returns last
null && "hello"    // null — first is falsy, short-circuits
false || "hello"   // "hello" — returns first truthy
0 || 42            // 42
```

This enables the guard pattern `const name = user && user.name;` but also creates the classic **`||` default bug**: `const display = count || 'No items'` replaces a legitimate `0` with `'No items'`.

**Nullish coalescing `??` (ES2020)** fixes that: it only substitutes when the left side is `null`/`undefined`, not for other falsy values like `0` or `""`. Rule: use `??` for defaults, `||` only when you deliberately want to replace all falsy values.

**Optional chaining `?.` (ES2020)** replaces the `a && a.b && a.b.c` chain with `a?.b?.c`; returns `undefined` (no throw) when any link is missing. Works with method calls (`user?.getName?.()`) and array access (`user?.roles?.[0]`). Combine with `??` for a default: `user?.profile?.city ?? 'Unknown'`.

**Ternary:** `age >= 18 ? 'Adult' : 'Minor'`; chainable but keep it readable.

**Spread `...` (expands)** vs **rest `...` (collects)** — same syntax, opposite jobs. Spread is in a *call or literal*: `Math.max(...arr)`, `[...a, ...b]`, `{...base, z: 3}` (shallow copy / merge, later key wins). Rest is in a *definition*: `function sum(...numbers) {}`.

**Logical assignment (ES2021):** `||=` assigns if falsy, `??=` assigns if null/undefined (preferred for lazy init), `&&=` assigns only if currently truthy.

**`in` and `instanceof`:** `'make' in car` checks property existence (including inherited); `dog instanceof Animal` walks the prototype chain.

**Precedence gotcha:** `a ?? b || c` is a `SyntaxError` — `??` and `||` cannot be mixed without explicit parens.

---

## 4. Control Flow

`if/else if/else` is identical to Java syntactically. The professional pattern is **guard clauses** (early returns) instead of deep nesting — flatten `if (order) { if (order.isValid) { ... } }` into `if (!order) return 'No order'; if (!order.isValid) return 'Invalid order'; ...`.

`switch` uses `===` internally (no coercion). Missing `break` falls through. For many cases mapping to values or handlers, an **object lookup table** is the modern alternative:

```javascript
const handlers = { CREATE: handleCreate, UPDATE: handleUpdate, DELETE: handleDelete };
const handler = handlers[action.type];
if (handler) handler(action.payload);
else throw new Error(`Unknown action: ${action.type}`);
```

**Loops:** classic `for`, `while`, `do...while` (runs at least once) are like Java. **`for...of`** is the preferred modern loop for arrays/iterables — gives values directly, works on arrays, strings, Sets, Maps, generators. **`for...in`** iterates enumerable *keys*, including inherited ones — dangerous on arrays, and on plain objects should be guarded with `hasOwnProperty` or replaced with `Object.entries()`. `break`/`continue` work like Java; labeled `break outer:` exists for nested loops but is rare.

**Error handling:** `try/catch/finally`, with an ES2019 optional catch binding (`catch { }` when you don't need the error object). `fs.readFileSync` catches use `error.code === 'ENOENT'` to distinguish and rethrow with context. Built-in error types mirror some of Java's (`TypeError`, `RangeError`, `ReferenceError`, `SyntaxError`).

**Custom error classes** — used in every Node/Express app in this course:

```javascript
class AppError extends Error {
  constructor(message, statusCode = 500) {
    super(message);
    this.name = 'AppError';
    this.statusCode = statusCode;
    this.isOperational = true;
    Error.captureStackTrace(this, this.constructor);
  }
}
class NotFoundError extends AppError {
  constructor(resource) { super(`${resource} not found`, 404); this.name = 'NotFoundError'; }
}
```

Note `Error.captureStackTrace` — no Java equivalent, V8-specific stack trace tooling.

**Destructuring in loops:** `for (const { id, name, role } of users)` and `for (const [key, value] of Object.entries(config))` are the idiomatic modern patterns.

---

## 5. Functions

### Four Ways to Define a Function

```javascript
function greet(name) { return `Hello, ${name}!`; }        // 1. declaration — hoisted
const greet = function(name) { return `Hello, ${name}!`; }; // 2. expression — NOT hoisted
const greet = (name) => `Hello, ${name}!`;                  // 3. arrow — no own `this`
const obj = { greet(name) { return `Hi, ${name}`; } };      // 4. method shorthand
```

**Hoisting:** function *declarations* are fully hoisted (callable before their textual definition). Function *expressions* and arrow functions stored in `const`/`let` are not — calling them early throws a `ReferenceError` (Temporal Dead Zone, see supplementary section).

Rule of thumb from the courseware: declarations for top-level utilities, arrow functions for callbacks/event handlers/variables, method shorthand for object and class methods.

### Parameters

Default parameters: `function createUser(name, role = 'user', isActive = true)`. Rest parameters collect trailing args into a real array: `function sum(...numbers) { return numbers.reduce((a,n)=>a+n,0); }`. **Named parameters via destructuring** replace Java's Builder pattern: `function createServer({ port = 3000, host = 'localhost' } = {}) {}` — the `= {}` default prevents a crash when called with zero arguments.

### Return Values

No explicit `return` → `undefined`. Arrow functions have **implicit return** with no braces: `const double = n => n * 2;`. Returning an *object literal* from an implicit-return arrow needs parens: `(x, y) => ({ x, y })` — without them, `{ x, y }` parses as a block, a `SyntaxError`.

### First-Class Functions & Higher-Order Functions

Functions are values — stored in variables/arrays/objects, passed as arguments, returned from other functions:

```javascript
function multiplier(factor) { return n => n * factor; }  // factory — returns a function
const double = multiplier(2);
double(5);  // 10
```

`map`, `filter`, `reduce`, `find`, `findIndex`, `every`, `some` are the functional backbone (full detail in Module 7). **`forEach` vs `map`**: `forEach` is for side effects and returns `undefined`; `map` returns a new transformed array. `numbers.forEach(n => n*2)` result is `undefined` — a classic assessment trap.

### Closures

A function that "remembers" variables from its enclosing scope after that scope has returned:

```javascript
function makeCounter() {
  let count = 0;   // closed over
  return {
    increment() { count++; },
    value() { return count; }
  };
}
const counter = makeCounter();
counter.increment(); counter.increment();
counter.value();  // 2 — count is private, not accessible from outside
```

Each call to `makeCounter()` creates an *independent* closure. This underlies module patterns, factories, and the private-state IIFE pattern (`const userModule = (() => { let _users = []; return { add, getAll }; })();`).

### `this` — the biggest Java-developer trap

In Java, `this` always refers to the current instance, resolved lexically. **In JavaScript, `this` depends on *how* the function is called, not where it's defined.**

```javascript
const obj = { name: 'Alice', greet() { console.log(this.name); } };
obj.greet();          // 'Alice' — called as obj.greet()
const fn = obj.greet;
fn();                 // undefined — this was LOST when detached from obj
```

Arrow functions have **no own `this`** — they close over (inherit) `this` from the enclosing lexical scope, always:

```javascript
class Timer {
  constructor() { this.seconds = 0; }
  start() {
    setInterval(function() { this.seconds++; }, 1000);   // ❌ TypeError — regular fn, this is undefined
    setInterval(() => { this.seconds++; }, 1000);          // ✅ arrow — this = Timer instance
  }
}
```

This is *the* classic assessment trap: a regular `function` callback passed to `setTimeout`/`setInterval`/array methods loses `this`; an arrow function callback keeps the enclosing `this`.

**Explicit binding:** `fn.call(thisArg, arg1, arg2)` (args listed), `fn.apply(thisArg, [arg1, arg2])` (args as array), `fn.bind(thisArg)` (returns a new function with `this` permanently fixed, does not call immediately). Modern guidance: arrow functions solve 90% of `this` problems in callbacks; use regular function/method syntax only where you *want* dynamic `this` (object methods, class methods).

### Pure Functions, IIFE, Composition

Pure function: same input → same output, no side effects — easy to test/parallelize. IIFE: `(function() { ... })();` or `(() => { ... })();` creates a private scope (pre-module pattern). Composition: `const compose = (f, g) => x => f(g(x));` and `const pipe = (...fns) => x => fns.reduce((v, f) => f(v), x);`.

---

## 6. Objects

### Literals, Access, Shorthand

```javascript
const user = {
  id: 1, name: 'Alice',
  address: { city: 'Bengaluru' },   // nested object
  roles: ['admin', 'user'],
  greet() { return `Hi, ${this.name}`; }   // method shorthand — preferred
};
user.name;         // dot notation
user['email'];     // bracket — for dynamic keys
```

Shorthand syntax: `{ name, age }` when the variable name equals the key. Computed keys: `{ [field]: value }`. Existence checks: `'email' in user` (includes inherited), `Object.prototype.hasOwnProperty.call(user, 'email')` (own only — safer than `user.hasOwnProperty` which can be shadowed).

### Destructuring

```javascript
const { name, role: userRole, theme = 'light', ...rest } = user;  // rename, default, rest-collect
const { address: { city, state } } = user2;                        // nested
function displayUser({ name, email, role = 'user' }) { ... }       // named-params pattern
```

### `Object.*` Toolkit

`Object.keys/values/entries` are the standard way to iterate — safer than `for...in`. `Object.fromEntries(entries)` builds an object back from pairs (useful with `Map` too). `Object.assign({}, a, b)` and spread `{...a, ...b}` both do **shallow** merges (spread preferred, more readable) — nested objects remain shared references, which is a real bug source:

```javascript
const copy = { ...original };
copy.address.city = 'LA';
original.address.city;  // 'LA' — mutated! shared nested reference
```

For a true deep copy use `structuredClone()` (ES2022) — it replaces the old `JSON.parse(JSON.stringify(x))` hack and handles `Date`/`Map`/`Set`, but not functions or DOM nodes.

`Object.freeze()` makes an object shallowly immutable (writes silently fail, throw in strict mode); `Object.seal()` allows modifying existing props but blocks add/delete. `Object.create(proto)` creates an object with an explicit prototype — the low-level mechanism classes sugar over.

### Classes — OOP in JavaScript

Classes are **syntactic sugar over prototypes**; they compile down to the same prototype-based mechanism, just with syntax familiar to a Java developer.

```javascript
class Animal {
  #sound = 'generic sound';   // private field (# prefix — truly private, ES2022)
  static count = 0;

  constructor(name, species) { this.name = name; this.species = species; Animal.count++; }
  speak() { return `${this.name} says: ${this.#sound}`; }
  get info() { return `${this.name} (${this.species})`; }
  set sound(v) { if (typeof v !== 'string') throw new TypeError('Sound must be string'); this.#sound = v; }
  static getCount() { return Animal.count; }
}

class Dog extends Animal {
  constructor(name) { super(name, 'dog'); }   // must call super() before using `this`
  speak() { return `${this.name} barks: Woof!`; }  // override
}
```

`#field` is truly private — not even accessible via `obj.#field` from outside the class, unlike Java's `private` which is convention-enforced by the compiler at the same visibility level as reflection can bypass. `super()` must run before `this` is touched in a subclass constructor, or you get a `ReferenceError` — same rule spirit as Java requiring `super()`/`this()` to be the first statement.

**Java vs JavaScript OOP table (from the courseware):**

| Java | JavaScript |
|---|---|
| `private String name;` | `#name` (truly private) or `this.name` (convention only) |
| `@Override` annotation | just redefine the method — no annotation |
| `super.method()` | `super.method()` — same |
| `implements Interface` | no interface keyword — TypeScript adds it |
| `abstract class` | no keyword — pattern or TypeScript |
| `final` | no keyword — TypeScript `readonly` |

### Prototypes and the Prototype Chain

Every object has a hidden `[[Prototype]]` link. `arr.push` is found by walking `arr → Array.prototype → Object.prototype → null`. `class B extends A` builds `b → B.prototype → A.prototype → Object.prototype → null`; property/method lookup checks the object first, then walks up the chain. This is the mechanism `instanceof` tests.

### JSON

`JSON.stringify(obj)` (accepts a replacer array/function and indent arg for pretty-print), `JSON.parse(str)` (accepts a reviver). **Gotcha:** `Date` objects become plain strings through the round trip — `typeof parsed.createdAt === 'string'`, requiring manual `new Date(str)` reconstruction, or a reviver function.

---

## 7. Arrays & Array Methods

### Creation and Access

```javascript
const fruits = ['apple', 'banana'];
Array.from({ length: 5 }, (_, i) => i);      // [0,1,2,3,4]
Array.from(new Set([1,2,2,3]));               // dedupe via spread-friendly conversion
arr.at(-1);                                    // ES2022 — negative index support (arr[-1] is undefined!)
Array.isArray([]);                             // true — the correct array check, not typeof
```

### Mutating vs Non-Mutating — the assessment's favorite distinction

**Mutating:** `push`, `pop`, `unshift`, `shift`, `splice(start, deleteCount, ...items)`, `sort()`, `reverse()`, `fill()`.

**`sort()` gotcha:** default sort is **lexicographic** (string-based) — `[10, 9, 2, 100].sort()` gives `[1, 10, 100, 2, 9]`, wrong for numbers. Always pass a comparator: `.sort((a,b) => a-b)`.

**Non-mutating (prefer these):** `slice(start, end)` (end exclusive), `concat`, spread-based add/remove (`[...arr, x]`), and the ES2023 non-mutating twins `toSorted`, `toReversed`, `toSpliced`, `with(i, x)`.

This matters concretely in React/Angular: mutating `push`/`splice` keeps the same array reference, so frameworks that do reference-equality change detection **won't re-render** — always return a new array.

### The Big 3 — `map` / `filter` / `reduce`

```javascript
numbers.map(n => n * 2);                       // same length, transform
numbers.filter(n => n % 2 === 0);               // shorter/equal, keep matching
numbers.reduce((sum, n) => sum + n, 0);         // accumulate to single value

// reduce building a lookup map — O(1) access pattern
const byId = users.reduce((acc, u) => { acc[u.id] = u; return acc; }, {});

// reduce for grouping
const byDept = people.reduce((groups, p) => {
  groups[p.dept] ??= [];
  groups[p.dept].push(p);
  return groups;
}, {});

// chaining
users.filter(u => u.role === 'admin').map(u => u.name).join(', ');
```

### Searching

`indexOf`/`lastIndexOf` (strict `===`, `-1` if not found), `includes` (handles `NaN` correctly, unlike `indexOf`), `find`/`findIndex` (by predicate, first match), `findLast`/`findLastIndex` (ES2023), `every`/`some` (note the vacuous-truth edge case: `[].every(x => false)` is `true`, `[].some(x => true)` is `false`).

### `flat` / `flatMap`

`arr.flat(depth)` flattens nested arrays (`Infinity` for full flatten); `flatMap` maps then flattens one level — more efficient than `.map().flat()`. Practical use: `orders.flatMap(o => o.items)` expands a one-to-many relationship.

### Destructuring

`const [first, second, ...rest] = arr;`, skip with empty slots `const [,, third] = arr;`, defaults, and the classic no-temp swap `[x, y] = [y, x];`.

### `Map` and `Set`

`Map` allows **any type as key** (unlike plain objects, which coerce keys to strings) and preserves insertion order; `Set` stores unique values. The most common `Set` use is deduplication: `[...new Set(arr)]`. Set algebra (`union`, `intersection`, `difference`) is built from spread + `filter` + `.has()`.

---

## 8. Strings & String Methods

Strings are **immutable** — every method returns a new string.

**Template literals** are preferred over concatenation, support any expression in `${}`, and allow multi-line strings without `\n`.

**Access:** `str[i]` / `str.at(-1)` (ES2022 negative index support — `str[-1]` is `undefined`, a Python habit trap).

**Searching:** `includes`/`startsWith`/`endsWith` (readable, case-sensitive) over `indexOf`. `match`/`matchAll` for regex extraction; `matchAll` (ES2020) with the `g` flag plus spread gives all matches with capture groups.

**Extracting:** `slice(start, end)` supports negative indices and is preferred over `substring` (which lacks negative support and silently swaps args if `start > end`).

**Transforming:** `toUpperCase`/`toLowerCase`, `trim`/`trimStart`/`trimEnd`. **`replace` only touches the first match** — `replaceAll` (ES2021) is required for global replacement, or `replace` with a regex `/g` flag. `padStart`/`padEnd` for zero-padding IDs.

**Regex essentials:** `.test()` for boolean checks, capture-group destructuring from `.match()`, named capture groups `(?<year>\d{4})` (ES2018).

**Number ↔ String:** `(255).toString(16)` (hex), `(3.14159).toFixed(2)` (rounds, returns a **string**), `Number('42')`/`parseInt('42px', 10)` the other way.

---

## 9. Asynchronous JavaScript

### Why: Single-Threaded + Event Loop

Java achieves concurrency with real OS threads. JavaScript is single-threaded and instead uses the **event loop** — one call stack, plus a microtask queue and a macrotask queue that feed work back into that stack when it's empty.

### Execution Priority — the classic exam question

1. Synchronous code (call stack) runs first.
2. **Microtasks** — `Promise.then/catch/finally`, `queueMicrotask()`.
3. **Macrotasks** — `setTimeout`, `setInterval`, I/O callbacks.

```javascript
console.log('1');
setTimeout(() => console.log('2'), 0);          // macrotask
Promise.resolve().then(() => console.log('3'));  // microtask
console.log('4');
// Output: 1, 4, 3, 2
```

### Callbacks and Callback Hell

Node's convention: first callback arg is the error. Nested callbacks for sequential async steps produce the "Pyramid of Doom" with error handling duplicated at every level — the motivating problem Promises solve.

### Promises

Three states: pending → fulfilled / rejected.

```javascript
const fetchData = new Promise((resolve, reject) => {
  if (success) resolve({ id: 1 }); else reject(new Error('Fetch failed'));
});
fetchData.then(v => v).catch(e => e.message).finally(() => cleanup());
```

Chaining flattens callback hell: `getUser(id).then(u => getOrders(u.id)).then(...).catch(handleError)` — **one `.catch()` handles every rejection in the chain.**

**Combinators:** `Promise.all` — all must resolve, fails fast on first rejection, runs in parallel. `Promise.allSettled` — waits for all, never throws, gives per-promise `{status, value|reason}`. `Promise.race` — first to *settle* (resolve or reject) wins. `Promise.any` (ES2021) — first to *resolve* wins, only rejects if all reject.

### `async`/`await`

Syntactic sugar over Promises. An `async function` always returns a Promise (its return value is auto-wrapped). `await` pauses execution until the awaited Promise settles. Errors use `try/catch` instead of `.catch()`.

**Sequential vs parallel — a common exam trap:**

```javascript
// ❌ Sequential — 300ms total
const users = await getUsers(); const products = await getProducts(); const orders = await getOrders();

// ✅ Parallel — 100ms total (independent calls)
const [users, products, orders] = await Promise.all([getUsers(), getProducts(), getOrders()]);
```

**Forgetting `await` gotcha:** `const response = fetch(...)` (no await) gives you a Promise object, not the response — `response.json is not a function`.

### `fetch`

Modern HTTP client (browser + Node 18+). **`fetch` does NOT throw on 4xx/5xx** — you must check `response.ok` and throw manually. POST/PATCH set `headers: { 'Content-Type': 'application/json' }` and `body: JSON.stringify(data)`.

### Error Handling Patterns

Standard `try/catch`, plus the Go-style tuple pattern to avoid nested try/catch:

```javascript
async function safeAsync(promise) {
  try { return [null, await promise]; } catch (error) { return [error, null]; }
}
const [err, user] = await safeAsync(getUser(id));
```

### Timers, Async Iteration

`setTimeout`/`clearTimeout`, `setInterval`/`clearInterval`. Promisified delay: `const delay = ms => new Promise(r => setTimeout(r, ms));` used for retry-with-backoff loops. `for await...of` iterates async iterables; async generators (`async function*`) support streaming/pagination patterns.

---

## 10. ES Modules & Modern JavaScript

### ES Modules

```javascript
// math.js
export const PI = 3.14159;
export function add(a, b) { return a + b; }
export default function greet(name) { return `Hello, ${name}!`; }

// app.js
import greet, { add, PI } from './math.js';   // default + named together
import * as math from './math.js';              // namespace import
const { add } = await import('./math.js');      // dynamic import — returns a Promise, lazy loads
```

**CommonJS (`require`/`module.exports`) vs ESM (`import`/`export`):** CJS is Node's original, synchronous system; ESM is the official standard, statically analyzed (enables tree-shaking), and works in both browser and modern Node (enable via `.mjs` extension or `"type": "module"` in `package.json`). You cannot `require()` inside an ESM file.

### Advanced Destructuring

Rename + default + nested-with-fallback-object in one expression: `const { name: fullName = 'Anonymous', address: { city = 'Unknown' } = {} } = user;` — the `= {}` on `address` prevents a crash if `address` itself is missing.

### `structuredClone` (ES2022)

The proper deep-copy built-in, replacing the old `JSON.parse(JSON.stringify(x))` hack (which loses `Date`s and can't handle circular refs) and `lodash.cloneDeep`. Handles `Date`, `Map`, `Set`, nested objects/arrays. Does **not** handle functions or DOM nodes.

### Symbols

`Symbol('id')` creates a guaranteed-unique primitive — `Symbol('id') === Symbol('id')` is `false` even with identical descriptions. Symbol-keyed properties are invisible to `for...in`/`Object.keys`. Well-known symbols like `Symbol.iterator` let a custom class work with `for...of` and spread.

### Generators

`function* counter() { while (true) yield start++; }` pauses at `yield`, resumes on `.next()`. Finite generators combine naturally with `for...of` and spread (`[...range(1,6)]`). Note from the discussion Q&A: **`async/await` is built on generators internally.**

### Top-Level `await`, `globalThis`

In ES modules only, `await` works at the top level without wrapping in an `async` function. `globalThis` is the environment-agnostic global object (`window` in browser, `global` in Node, `self` in workers).

---

## 11. Supplementary — Gotchas from the MCQ Bank and Discussion Q&A

These points surface in `11_MCQ_Assessment.md` and `javascript_discussion_qa.md` but aren't spelled out as their own subsection in Modules 01–10 — worth knowing explicitly for the assessment.

- **Temporal Dead Zone (TDZ):** `let`/`const` *are* hoisted (unlike the common misconception that only `var` hoists), but they stay uninitialized until their declaration line executes. Accessing them before that point throws a `ReferenceError`, not `undefined` like `var` would give. This is *why* `let`/`const` are considered safer — they turn a silent bug into a loud error.
- **Scope chain:** when a variable isn't found in the current scope, JS walks outward through each enclosing scope until global, throwing `ReferenceError` if never found. This is the mechanism that makes closures work.
- **Event bubbling / capturing / delegation** (DOM-specific, relevant since `js-dom-html.js` uses `addEventListener`): events travel down from root to target (capture phase), then bubble back up (bubble phase, the default). `event.stopPropagation()` halts further travel. **Event delegation** — one listener on a parent (e.g. `<ul>`) instead of one per child (`<li>`) — is the standard efficient pattern for dynamic lists.
- **Currying and memoization:** currying transforms `add(2, 3)` into `add(2)(3)` — a series of single-argument functions enabling partial application. Memoization caches a pure function's return value per input set, trading memory for speed.
- **`Object.is()` vs `===`:** identical to `===` except two edge cases — `Object.is(NaN, NaN)` is `true` (unlike `===`), and `Object.is(+0, -0)` is `false` (unlike `===`, which treats them equal). React and other frameworks use `Object.is` internally for change detection.
- **`WeakMap`/`WeakSet`:** like `Map`/`Set` but keys must be objects, and entries are automatically garbage-collected once nothing else references the key — useful for attaching metadata to objects without creating a memory leak.
- **Tail call optimization (TCO):** the spec defines it (ES6) so a tail-position recursive call could reuse the stack frame, but V8 never fully implemented it — deep recursion in real Node/browser code can still stack-overflow; use loops or trampolining for genuinely deep recursion.
- **`localStorage` vs `sessionStorage` vs cookies:** `localStorage` persists until explicitly cleared; `sessionStorage` clears when the tab closes; cookies are sent with every HTTP request and support `httpOnly`/`Secure`/expiry — use `httpOnly` cookies for anything server-auth-related, `localStorage`/`sessionStorage` for client-only state.
- **Callback-not-firing trap:** `btn.addEventListener('click', handleClick())` *calls* `handleClick` immediately (its return value becomes the listener) instead of passing the function reference — should be `addEventListener('click', handleClick)`. This exact class of mistake shows up in the demo files below.

---

# Part II — Real Demo Code Walkthrough

The files below are the actual working code from `Code/UI (HTML, CSS, JS, Ts, Node)/js/`. Several files contain large commented-out blocks left in place by design — they're kept as-is because they show the *progression* of a concept (e.g., "here's the broken version, here's the fixed version") that the instructor built up interactively. Where a block is commented out, the explanation still covers it, since it's part of the pedagogical sequence and the only actually-executing lines are called out explicitly.

## 12. `js-functions-syntaxes.js` — the Four Function Forms and `this` in Object Methods

```javascript
1:  // js-functions-syntaxes.js
2:  
3:  // function syntaxes
4:  
5:  // function fun1() {
6:  //     console.log('fun1 called.');
7:  // };
8:  // fun1();
9:  // const fun2 = () => {
10: //     console.log('fun2 called.');
11: // };
12: // fun2();
13: 
14: // // const gstCalc = (amount) => { return amount * 1.18; };
15: // const gstCalc = amount => amount * 1.18;
16: // console.log(gstCalc(100));
17: 
18: // const employee = {
19: //     firstName: 'Sonu',
20: //     lastName: 'Joshi',
21: //     salary: 10.25,
22: //     address: { pin: 500001, city: 'Pune' },
23: //     isIndian: true,
24: //     phones: [9876543210, 6789012345],
25: //     printSalary: () => { console.log(this.salary); }, // undefined
26: //     printSalary2: function () { console.log(this.salary); } // 10.25
27: // };
28: 
29: // employee.printSalary();
30: // employee.printSalary2();
31: 
32: // const myFuns = {
33: //     fun1: () => { },
34: //     fun2: () => { }
35: // };
36: 
37: // myFuns.fun2();
```

- **Lines 5–8** — a **function declaration** (`function fun1() {}`), then an immediate call. Declarations are hoisted, so this would work even if the call appeared above the definition (not exercised here, but true of this form per Module 5).
- **Lines 9–12** — the same behavior as a **function expression using an arrow function**, assigned to a `const`. Arrow expressions are *not* hoisted — `fun2()` must textually follow the assignment, unlike `fun1`.
- **Lines 14–16** — `gstCalc` demonstrates the arrow function's **implicit return**: `amount => amount * 1.18` needs no `return` keyword or braces because it's a single expression. The commented line above it (`(amount) => { return amount * 1.18; }`) is the equivalent explicit-block form — the file documents both to show they're identical, just less concise with braces.
- **Lines 18–27** — an object literal `employee` with two functionally-identical-looking methods that behave *differently* because of `this` binding: `printSalary` is defined as an **arrow function property** — arrow functions have no own `this`, so `this` here is inherited from the surrounding (module/top-level) scope, not `employee`. Result: `this.salary` is `undefined`. `printSalary2` uses the **regular `function` expression** form — when called as `employee.printSalary2()`, `this` is bound to `employee` at call time, so `this.salary` correctly resolves to `10.25`. This is the single most important `this` gotcha from Module 5.7, reproduced verbatim: **never use arrow functions for object methods that need `this` to refer to the object.**
- **Lines 32–37** — `myFuns` shows two arrow-function properties stored in an object and one being invoked (`myFuns.fun2()`); since neither body does anything (`{ }`), this is purely demonstrating that arrow functions can live as object values and be called via property access, independent of the `this` issue above.

**Assessment trap to flag:** if asked "why does `printSalary` print `undefined` but `printSalary2` prints `10.25`," the answer is arrow-function lexical `this` vs regular-function dynamic `this` — this exact file is built to illustrate that.

---

## 13. `js-functions.js` — Function Declarations, Defaults, and Argument-Count Mismatches

```javascript
1:  // // Functions in JS 
2:  // // ===============
3:  
4:  
5:  // // Old JS function
6:  // function fun1() {
7:  //     console.log('fun1 function called.');
8:  // }
9:  
10: // fun1();
11: 
12: // // Modern JS function == preferred choice 
13: // const fun2 = () => {
14: //     console.log('fun2 function called.');
15: // };
16: 
17: // fun2();
18: 
19: // const fun3 = () => {
20: //     console.log('fun3 called.');
21: //     // return 'some return value';
22: // };
23: 
24: // const output = fun3();
25: // console.log(output); // undefined 
26: 
27: // const addNums = (a, b) => {
28: //     console.log(a + b);
29: // };
30: 
31: // addNums(); // NaN 
32: // addNums(10); // NaN 
33: // addNums(10, 20); // 30 
34: // addNums(10, 20, 30); // 30
35: 
36: 
37: // const addNums = (a, b = 5) => {
38: //     console.log(a + b);
39: // };
40: 
41: // addNums(); // NaN 
42: // addNums(10); // 15 
43: // addNums(10, 20); // 30 
44: // addNums(10, 20, 30); // 30
45: 
46: // const addNums = (a = 4, b = 5) => {
47: //     console.log(a + b);
48: // };
49: 
50: // addNums(); // 9
51: // addNums(10); // 15 
52: // addNums(10, 20); // 30 
53: // addNums(10, 20, 30); // 30
```

- **Lines 6–10** — the "old" function declaration form (comment explicitly labels this as legacy compared to arrows) called immediately.
- **Lines 13–17** — the "modern, preferred" arrow function equivalent, per the file's own comment — matches the courseware's guidance table in Module 5.1.
- **Lines 19–25** — `fun3` has no `return` statement (the `return` is commented out), so `fun3()` evaluates to `undefined` when logged. This is the concrete demonstration of Module 5.3's "no explicit return → `undefined`" rule.
- **Lines 27–34** — `addNums(a, b)` with **no defaults**: calling with too few arguments leaves the missing parameters `undefined`. `addNums()` → `undefined + undefined` → `NaN`. `addNums(10)` → `10 + undefined` → `NaN`. Extra arguments beyond the declared parameter list (`addNums(10, 20, 30)`) are silently ignored unless captured with rest params — JavaScript, unlike Java, does not throw on arity mismatch; it just leaves unfilled parameters as `undefined`.
- **Lines 37–44** — adding **one default parameter** (`b = 5`): now `addNums(10)` gives `10 + 5 = 15`. But `addNums()` is still `NaN`, because `a` remains `undefined` (no default) and `undefined + 5` is `NaN`. This isolates that defaults apply *per parameter*, not to the call as a whole.
- **Lines 46–53** — both parameters now default (`a = 4, b = 5`): `addNums()` → `4 + 5 = 9`. This progression across three versions of `addNums` is a deliberate teaching sequence showing exactly how default parameters resolve `NaN` bugs from missing arguments — a very likely assessment scenario ("what does this print, given these three variants").

---

## 14. `js-function-as-arg.js` — Passing Functions as Arguments (Named vs Anonymous) and `setTimeout`

```javascript
1:  // // Function as args in JS 
2:  // // ======================
3:  
4:  // const addNums = (a, b) => {
5:  //     console.log(a + b);
6:  // };
7:  
8:  // addNums(10, 20); // 30 
9:  // const x = 5;
10: // const y = 6;
11: // addNums(x, y); // 11
12: 
13: // const fun = (arg) => {
14: //     console.log('fun function called.');
15: //     // console.log(arg - 1);
16: //     // console.log(`Hi ${arg}!`);
17: //     // console.log(arg.city);
18: //     arg();
19: // };
20: 
21: // // fun(10);
22: // // fun('Sonu');
23: // // fun({ pin: 500001 });
24: // fun(() => { console.log('abc'); });
25: 
26: 
27: // function [passed as arg ] to another function
28: 
29: // const fun = (arg) => {
30: //     console.log('fun function called.');
31: //     arg();
32: // };
33: 
34: // fun(() => { console.log('anonymous function called.'); });
35: 
36: // const passedFun = () => { console.log('named function called.'); };
37: // fun(passedFun);
38: 
39: // const fun = (arg) => {
40: //     console.log('fun function called.');
41: //     arg();
42: // };
43: 
44: // fun(() => { console.log('anonymous function called.'); });
45: 
46: // setTimeout(arg1, arg2);
47: // setTimeout(() => { }, timeout);
48: 
49: // console.log("One");
50: 
51: // setTimeout(() => {
52: //     console.log("Two");
53: // }, 2000);
54: 
55: // console.log("Three");
```

- **Lines 4–11** — a plain, non-callback function call (`addNums(x, y)`) establishing the baseline before demonstrating higher-order usage.
- **Lines 13–24** — `fun(arg)` is generic — `arg` could be a number, string, or object (each of the commented alternative calls on lines 21–23 illustrates a *type mismatch*: calling `arg()` when `arg` is `10` or `'Sonu'` would throw `TypeError: arg is not a function`). Only line 24, passing an **anonymous arrow function**, actually works, because `arg()` inside `fun` requires `arg` to *be* callable. This directly illustrates that JavaScript does no compile-time parameter type checking (Module 1.6's "errors caught at compile time in Java are runtime errors in JS").
- **Lines 29–37** — the same `fun` pattern, now contrasting an **anonymous function literal passed inline** (line 34) with a **named function reference passed by variable** (`passedFun`, lines 36–37). Both work identically — the key teaching point is `fun(passedFun)` passes the *function itself* (a reference), not the result of calling it; `fun(passedFun())` would be the trap (calls immediately, passes `undefined`'s return value instead of a function reference — the exact mistake documented in the Discussion Q&A's "callback function not receiving expected arguments" entry).
- **Lines 46–47** — comments documenting `setTimeout`'s general signature `setTimeout(callback, delayMs)`, immediately followed by the concrete pattern `setTimeout(() => {}, timeout)`.
- **Lines 49–55** — the canonical event-loop demo: `console.log("One")` and `console.log("Three")` are synchronous and run immediately in order; `setTimeout(..., 2000)` schedules `console.log("Two")` as a **macrotask** that only runs after the 2-second timer expires *and* the call stack is empty. Actual output order: `One`, `Three`, then `Two` two seconds later — even though `"Two"` is written *between* `"One"` and `"Three"` in the source. This is Module 9.2's event loop lesson made concrete and is a very common "what's the output order" assessment question.

---

## 15. `js-callbacks.js` — Callback Hell → Promise → `async`/`await`, Three Solutions to the Same Problem

```javascript
1:  
2:  // const getData = (arg) => {
3:  //     console.log('getData called');
4:  //     arg({ city: 'Bengaluru' });
5:  // };
6:  
7:  // getData((data) => {
8:  //     console.log('anonymous function called.');
9:  //     console.log(data.city);
10: // });
11: 
12: // // =====================
13: // // problem of async js 
14: // // =====================
15: 
16: // const getData = () => {
17: //     console.log('getData called');
18: //     setTimeout(() => {
19: //         return { city: 'Bengaluru' };
20: //     }, 2000);
21: // };
22: 
23: // const output = getData();
24: // // const output = undefined;
25: // console.log(output.city);
26: 
27: // // =====================
28: // // solution 1 - callback 
29: // // =====================
30: 
31: // console.log("Start");
32: 
33: // const getData = (arg) => {
34: //     console.log('getData called');
35: //     setTimeout(() => {
36: //         arg({ city: 'Bengaluru' });
37: //     }, 2000);
38: // };
39: 
40: // getData((data) => {
41: //     console.log('anonymous function called.');
42: //     console.log(data.city);
43: // });
44: 
45: // // ====================
46: // // solution 2 - Promise  
47: // // ====================
48: 
49: // console.log("Start");
50: 
51: // const getData = () => {
52: //     console.log('getData called');
53: //     const isDataAvailable = false; // true false 
54: //     return new Promise((resolve, reject) => {
55: //         setTimeout(() => {
56: //             if (isDataAvailable)
57: //                 resolve({ city: 'Bengaluru' });
58: //             else
59: //                 reject({ message: 'Data not available' });
60: //         }, 2000);
61: //     });
62: // };
63: 
64: // getData()
65: //     .then((response) => { console.log(response.city); })
66: //     .catch((error) => { console.log(error.message); });
67: 
68: 
69: // // ==========================
70: // // solution 3 - Promise and async / await
71: // // ==========================
72: 
73: console.log("Start");
74: 
75: const getData = () => {
76:     console.log('getData called');
77:     const isDataAvailable = false; // true false 
78:     return new Promise((resolve, reject) => {
79:         setTimeout(() => {
80:             if (isDataAvailable)
81:                 resolve({ city: 'Bengaluru' });
82:             else
83:                 reject({ message: 'Data not available' });
84:         }, 2000);
85:     });
86: };
87: 
88: const consumeData = async () => {
89:     try {
90:         const data = await getData();
91:         console.log(data.city);
92:     }
93:     catch (error) {
94:         console.log(error.message);
95:     }
96: };
97: 
98: consumeData();
```

This entire file is a deliberate four-stage progression toward `async`/`await` — the pedagogical core of Module 9.

- **Lines 2–10** — baseline: a **synchronous callback**. `getData` immediately invokes `arg({ city: 'Bengaluru' })`. Works fine because nothing is actually asynchronous yet.
- **Lines 16–25** — **"the problem of async JS."** `getData` now wraps its logic in `setTimeout`, but the outer function itself has no `return` — the arrow function passed to `setTimeout` returns `{ city: 'Bengaluru' }`, but that return value goes nowhere (it's the *timer callback's* return, discarded by `setTimeout`), not `getData`'s return value. `getData()` therefore evaluates to `undefined` (line 23's comment literally spells this out: `// const output = undefined;`). `console.log(output.city)` on line 25 would throw `TypeError: Cannot read properties of undefined (reading 'city')`. This is the exact "async operations can't return values synchronously" problem Module 9.1 introduces.
- **Lines 33–43 — Solution 1 (callback):** `getData(arg)` now takes a callback and invokes `arg({...})` **inside** the `setTimeout` body, once the delayed data is "ready." The caller passes a callback that receives the eventual data (`data => console.log(data.city)`). `console.log("Start")` on line 31 logs before `getData called` even appears to complete meaningfully — synchronous code and the top of `getData` run immediately, but the actual `arg(...)` call, and thus the callback's `console.log(data.city)`, only happens after the 2-second macrotask fires. This is Module 9.3's callback solution.
- **Lines 51–66 — Solution 2 (Promise):** `getData()` now **returns `new Promise((resolve, reject) => {...})`**. Inside the executor, after the timeout, `isDataAvailable` (hardcoded `false`) determines whether `resolve` or `reject` fires. The caller chains `.then(response => ...)` for success and `.catch(error => ...)` for failure — flat, linear, no nested callback. Because `isDataAvailable` is `false`, `.catch` fires with `{ message: 'Data not available' }`.
- **Lines 75–98 — Solution 3 (Promise + `async`/`await`) — the actually-executing code in the file:** `getData` is unchanged from Solution 2 (still Promise-returning). `consumeData` is declared `async`, allowing `await getData()` inside a `try` block — execution pauses at line 90 until the Promise settles. Since `isDataAvailable` is `false`, the Promise rejects, `await` throws, and control jumps to the `catch` block (line 93), logging `error.message` → `'Data not available'`. Real runtime output order: `"Start"` (line 73, sync) → `"getData called"` (line 76, sync — runs the instant `getData()` is invoked inside `await`) → *2-second pause* → `"Data not available"` (line 94, after the timer rejects and `await` re-throws into the `catch`).
- **Key trap to flag:** flipping `isDataAvailable` to `true` changes nothing about *control flow structure* — only which branch (`try`'s success path vs `catch`) executes. Students should be able to trace both outcomes.

---

## 16. `js-array-ops.js` — Destructuring, Rest, and Spread (mislabeled as "OOP in JS" in the file header)

```javascript
1:  // OOP in JS 
2:  
3:  // const arr = [22, 9, 31, 25, 17];
4:  // console.log(arr);
5:  
6:  // const [a, b, c, d, e] = arr;
7:  // console.log(a);
8:  
9:  // // array destructuring == rest operator 
10: 
11: // const [a, b, ...remaining] = arr;
12: // console.log(a);
13: // console.log(b);
14: // console.log(remaining);
15: 
16: // spread operator 
17: 
18: const addNums = (...args) => {
19:     return args;
20: };
21: 
22: console.log(addNums(2, 3));
23: console.log(addNums(2, 3, 4));
24: console.log(addNums(2, 3, 4, 7));
25: console.log(addNums(2, 3, 4, 7, 1));
```

- **Line 1** — the file's own header comment says "OOP in JS," but nothing below is OOP — it's array destructuring and rest/spread. Worth flagging as a labeling artifact from the instructor's working notes, not a content error to be confused by.
- **Lines 3–7** (commented, not executing) — standard positional **array destructuring**: `const [a, b, c, d, e] = arr;` pulls each element into its own named variable in one statement, matching Module 7.9.
- **Lines 11–14** (commented) — **rest in destructuring**: `const [a, b, ...remaining] = arr;` captures the first two elements individually and collects everything else into a new array `remaining`. This is the "collects" side of the spread/rest pair (Module 3.8).
- **Lines 18–25 — the actually-executing code:** `addNums = (...args) => args;` uses **rest parameters** to accept *any number* of arguments into a real array `args`, then simply returns that array. Each `console.log(addNums(...))` call demonstrates that regardless of how many arguments are passed (2, 3, 4, or 5), they all collect correctly: `addNums(2,3)` → `[2,3]`, `addNums(2,3,4,7,1)` → `[2,3,4,7,1]`. This is a clean, isolated demonstration of rest parameters decoupled from any specific use (like `sum`) — the return value itself *is* the array, letting you see the mechanism directly.
- **Terminology check for the assessment:** the *parameter* `...args` here is **rest** (collecting, in a function definition). If this same `...` syntax appeared in a call site (`addNums(...someArray)`) it would be **spread** (expanding). Same characters, opposite direction — Module 3.8's rule.

---

## 17. `js-oop-concepts.js` — Classes, Constructors, Inheritance — and How This Differs from Java

```javascript
1:  // OOP in JS 
2:  
3:  // // class in JS 
4:  // class Animal {
5:  //     name;
6:  //     color;
7:  // }
8:  // const animal1 = new Animal();
9:  // animal1.name = 'Tommy';
10: // animal1.color = 'Gold';
11: // console.log(animal1);
12: 
13: 
14: // class in JS 
15: class Animal {
16:     name;
17:     color;
18:     food;
19: 
20:     // constructor() { }
21:     // A class may only have one constructor
22: 
23:     constructor(name, color, food) {
24:         this.name = name;
25:         this.color = color;
26:         this.food = food;
27:     };
28: 
29:     toPrint() {
30:         return `{name: '${this.name}', color: '${this.color}, food: '${this.food}'}`;
31:     };
32: 
33: }
34: // const animal1 = new Animal();
35: // animal1.name = 'Tommy';
36: // animal1.color = 'Gold';
37: // console.log(animal1.toPrint());
38: 
39: // const animal2 = new Animal('Bob', 'Black');
40: // console.log(animal2.toPrint());
41: 
42: const animal3 = new Animal('Moti', 'White', 'Bread');
43: console.log(animal3.toPrint());
44: 
45: class Alive {
46: 
47: }
48: 
49: // inheritance 
50: // class Dog extends Animal , Alive  { // not working 
51: class Dog extends Animal {
52: 
53: }
54: 
55: const animal4 = new Dog('Anny', 'Grey');
56: console.log(animal4.toPrint());
57: const animal5 = new Dog('Soni', 'Pink', 'Biscuits');
58: console.log(animal5.toPrint());
```

- **Lines 4–11 (commented, superseded)** — the *first* version of `Animal` declares bare class field names (`name; color;`) with **no constructor**. Objects are built with the implicit default constructor, then fields are set one at a time via dot-notation after construction (`animal1.name = 'Tommy'`) — functionally similar to Java's no-arg constructor + setter pattern, except these are public fields being assigned directly (no encapsulation here — same "anti-pattern" caution the Core Java guide raises about package-private direct field access).
- **Lines 15–33 — the real, in-use `Animal` class:** `name; color; food;` (lines 16–18) are **class field declarations** (ES2022 syntax) — this is optional documentation of the shape; JavaScript classes don't strictly require field declarations before `this.field = ...` in the constructor works, unlike Java where every field must be declared. Line 20's commented `constructor() {}` plus the comment "A class may only have one constructor" (line 21) is the instructor explicitly noting a genuine JS/Java parallel: like Java, **a class can only have one constructor** — no constructor overloading in JS. Instead, JS relies on default parameters to simulate overloading (seen below).
- **Lines 23–27** — the real constructor takes `(name, color, food)` and assigns each to `this`. Unlike Java, there's no type annotation on parameters and no matching field-type declaration required — `this.name = name` works regardless of whether `name` was pre-declared on line 16.
- **Lines 29–31** — `toPrint()` is an **instance method** using a template literal to build a formatted string — functionally the class's `toString()`-equivalent, but note it's a differently-named method (`toPrint`, not the special `toString`), so it must be called explicitly (`animal3.toPrint()`); it does **not** get invoked automatically by `console.log(animal3)` the way overriding `toString()` would.
- **Line 42–43 — the code that actually runs:** `new Animal('Moti', 'White', 'Bread')` constructs with all three positional args; `toPrint()` returns the formatted string, printed via `console.log`.
- **Lines 39–40 (commented)** — a deliberately-left "what if I under-supply arguments" example: `new Animal('Bob', 'Black')` (only 2 of 3 params) would leave `food` as `undefined` inside the constructor — **JavaScript does not throw for missing constructor arguments**, unlike Java where a mismatched constructor signature is a compile error. This is a direct callback to Module 5.2's "missing args become `undefined`" behavior, now shown in a class/constructor context specifically.
- **Line 45–47** — an empty `Alive` class, set up purely to demonstrate a **JavaScript limitation**: line 50 (commented) attempts `class Dog extends Animal, Alive` — **multiple inheritance via `extends`**, which the instructor's own comment flags as "not working." JavaScript classes support only **single inheritance** (`extends` takes exactly one parent), same restriction as Java's `extends` (Java compensates with `implements` for multiple interfaces — JS has no native interface concept at all, per Module 6.6's Java/JS OOP table).
- **Lines 51–53 — the actual, working inheritance:** `class Dog extends Animal {}` — an empty subclass body. Because `Dog` defines no constructor of its own, it implicitly gets a default constructor that calls `super(...args)` with whatever arguments were passed, forwarding them straight to `Animal`'s constructor. This is why `new Dog('Anny', 'Grey')` (line 55, only 2 args — `food` ends up `undefined`) and `new Dog('Soni', 'Pink', 'Biscuits')` (line 57, all 3 args) both work and both can call `toPrint()` — `toPrint` isn't redefined on `Dog`, so the lookup walks the prototype chain (`animal4 → Dog.prototype → Animal.prototype`) and finds it on `Animal.prototype`, exactly as Module 6.7 describes.

### JavaScript OOP vs Java OOP — explicit contrast (referencing the Core Java guide's Module 8 "Classes and Objects" and Module 10 "Inheritance")

| Aspect | Java (Core Java guide) | JavaScript (`js-oop-concepts.js`) |
|---|---|---|
| Underlying model | True class-based — a class is a compiler-level blueprint; `new` allocates from a fixed layout | **Prototype-based** — `class` is syntax sugar; `new Dog(...)` really builds an object linked via `[[Prototype]]` to `Dog.prototype`, which links to `Animal.prototype` |
| Constructors | Overloading allowed — multiple constructors, resolved by parameter *types and count* at compile time | **Only one constructor per class** (line 21's own comment confirms this) — "overloading" is simulated with default parameters, not real overload resolution |
| Missing constructor args | Compile error — signature must match exactly | **Silently `undefined`** — no compile-time arity/type check at all (line 39's `new Animal('Bob', 'Black')`) |
| Field declarations | Mandatory — every field needs an explicit type | **Optional** (lines 16–18 are documentation-only) — `this.x = x` in the constructor works with or without a prior field declaration |
| Multiple inheritance | Not via `extends` (single class inheritance), but multiple `implements Interface` is standard and common | **Not supported at all** the way line 50 attempts it (`extends Animal, Alive` fails) — no interfaces either; only single-class `extends` |
| Method resolution | Vtable-based dynamic dispatch through the class hierarchy, resolved by the JVM | **Prototype chain walk** — `toPrint()` is looked up on `animal4` itself, then `Dog.prototype`, then `Animal.prototype`, stopping at the first match |
| `toString()` equivalent | Override `Object`'s `toString()`; automatically invoked by `println`/string concatenation | No automatic hook unless you specifically override `toString()`; this file's `toPrint()` is a **custom-named** method and must be called explicitly — `console.log(animal3)` would NOT use it |
| Access control | `private`/`protected`/`public`/package-private, enforced by the compiler | No enforcement here at all — `name`, `color`, `food` are fully public; true privacy needs the `#field` syntax (Module 6.6), not used in this file |

**Assessment-relevant summary:** this file is a good minimal example of "classes as sugar" — every behavior observed (single constructor, silent `undefined` on missing args, single-parent `extends`, prototype-chain method lookup) is explainable only by remembering JS classes ultimately compile to prototype objects, not to a JVM-style fixed class table.

---

## 18. `js-dom-html.js` — DOM Selection, Event Listeners, and `confirm()`

```javascript
1:  // // JS DOM Methods
2:  
3:  // // const sayHi = () => {
4:  // //     inputText = document.getElementById('username').value;
5:  // //     document.getElementById('output').innerText = `Hi ${inputText}!`;
6:  // // };
7:  
8:  // // document.getElementById("element-to-capture")
9:  // // .addEventListener(arg1, arg2);
10: // // .addEventListener('event-to-capture', () => {function-to-execute-on-that-event});
11: 
12: // document.getElementById("submit").addEventListener("click", function () {
13: //     const name = document.getElementById("username").value;
14: //     const output = document.getElementById("output");
15: //     output.textContent = `Hi ${name}!`;
16: // });
17: 
18: 
19: document.getElementById("submit").addEventListener("click", function () {
20:     const name = document.getElementById("username").value;
21:     const output = document.getElementById("output");
22:     // output.textContent = `Hi ${name}!`;
23:     // alert(`Hi ${name}!`);
24:     output.textContent = confirm("Are you sure?") ? 'Yes' : 'No';
25: });
```

- **Lines 3–6 (commented, earliest draft)** — a first-pass idea using an arrow function `sayHi` and `innerText`, never wired to an event; abandoned in favor of the `addEventListener` pattern below.
- **Lines 8–10 (comment)** — the generic template the instructor documents before using it: `element.addEventListener(eventName, handlerFunction)`. This is a **higher-order function usage** — `addEventListener` is itself a function that takes another function (the handler) as its second argument, the same "function as argument" pattern from Module 5.4/`js-function-as-arg.js`.
- **Lines 12–16 (commented, first working version)** — `document.getElementById("submit")` selects the button by its `id` attribute; `.addEventListener("click", function() {...})` attaches a **regular (non-arrow) function expression** as the click handler. Inside, `document.getElementById("username").value` reads the current text-input value, and `output.textContent = ...` writes a greeting into another element. Note: a **regular function** is used here deliberately — inside a plain DOM event handler, `this` would refer to the element the listener is attached to (the button), which the code doesn't actually use, so the choice of regular vs arrow doesn't matter functionally in this specific snippet, but it's consistent with the courseware's guidance that event handlers are one of the contexts where a regular function's dynamic `this` can be useful (Module 5.7).
- **Lines 19–25 — the actually-active listener:** identical setup (`getElementById("submit")`, `click` event) but the response logic is different. Line 22 (commented) shows the straightforward greeting version being intentionally replaced; line 23 (commented) shows an even simpler `alert(...)` version also abandoned; **line 24 is what actually executes**: `output.textContent = confirm("Are you sure?") ? 'Yes' : 'No';` — `confirm()` is a **blocking, synchronous browser dialog** that returns a boolean (`true` for OK, `false` for Cancel); the result feeds directly into a **ternary operator** (Module 3.6) to decide the text written to `output.textContent`.
- **DOM-specific gotchas worth flagging:** `.value` (line 20) is specifically for form-input elements' current value; `.textContent` (lines 21, 24) sets plain text (safe from HTML injection) as opposed to `.innerHTML` (which parses HTML — not used here, and generally riskier). `confirm()`, like `alert()`, blocks the entire single JS thread until the user responds — a rare case where synchronous, blocking UI interaction is still idiomatic in plain browser JS.

---

## 19. `js-api-calls.js` — Two Ways to Consume a REST API: `.then()/.catch()` vs `async`/`await`

```javascript
1:  
2:  
3:  
4:  
5:  
6:  
7:  
8:  // Consume REST APIs using .then().catch() and async / await 
9:  
10: const apiUrl = 'https://jsonplaceholder.typicode.com/users/2';
11: 
12: // Consume REST APIs using .then().catch()
13: // =======================================
14: 
15: fetch(apiUrl)
16:     .then((response) => { return response.json() })
17:     .then((data) => { console.log(data); })
18:     .catch((error) => { console.error(error); });
19: 
20: // Consume REST APIs using async / await
21: // =======================================
22: 
23: const consumeRestApi = async () => {
24: 
25:     try {
26:         const response = await fetch(apiUrl);
27:         const data = await response.json();
28:         console.log(data);
29:     }
30:     catch (error) {
31:         console.error(error);
32:     }
33: };
34: consumeRestApi(); 
```

- **Line 10** — `apiUrl` is a real public test API (JSONPlaceholder) fetching a single fake user by ID `2`.
- **Lines 15–18 — Promise-chain style:** `fetch(apiUrl)` immediately returns a **Promise that resolves to a `Response` object**, not the JSON body yet — that's why the first `.then` (line 16) calls `response.json()`, which itself returns *another* Promise (parsing the body is itself asynchronous). Returning that Promise from inside `.then` causes the **chain to wait** for it before running the second `.then` (line 17), which finally receives the parsed `data`. `.catch` (line 18) catches any rejection anywhere upstream in the chain — network failure, or (note the gap the courseware flags in Module 9.6) it would **not** catch a non-2xx HTTP status on its own, since `fetch` doesn't reject on 4xx/5xx; this file doesn't check `response.ok`, so a 404 here would still flow through both `.then`s and attempt to parse whatever JSON body error page was returned, not hit `.catch`.
- **Lines 23–33 — `async`/`await` style, functionally equivalent:** `consumeRestApi` is declared `async`. `await fetch(apiUrl)` (line 26) pauses until the `Response` arrives; `await response.json()` (line 27) pauses again until the body is parsed. `try/catch` (lines 25–32) replaces `.then/.catch` for error handling — same missing-`response.ok`-check caveat applies here too.
- **Line 34 — `consumeRestApi();`** — the async function must still be explicitly **called**; declaring an `async function` does nothing by itself, same as any other function declaration.
- **Both blocks run independently and concurrently in this file** — nothing prevents lines 15–18 and lines 23–34 from both firing near-simultaneously when the file loads, since neither blocks the other (no shared `await`/`.then` chaining between them). Expect two separate, interleaved sets of console output for the same user data, in a specific order following each block's own network round-trip time.
- **Assessment-relevant comparison point:** this file is the cleanest side-by-side proof that `async`/`await` (Module 9.5) is literally syntactic sugar over the identical Promise-chain mechanics (Module 9.4) — same `fetch`, same two-step "get Response, then parse JSON" shape, same error-handling responsibility, just different surface syntax.

---

## 20. `script.js` — The Foundational Sandbox: Declarations, Coercion, `==`/`===`, Truthy/Falsy, Arrays, Objects

```javascript
1:  
2:  // console.log("Hello world!");
3:  
4:  // ECMAScript ES
5:  
6:  // variable declaration, data types, operators, control structure, functions, etc
7:  
8:  // java
9:  // int num = 10;
10: 
11: // // JS
12: // num = 10; // don't use this
13: // var num2 = 20; // don't use this too
14: // const num3 = 30; // use this as preferred choice
15: // let num4 = 40; // use this when needed
16: // console.log(num);
17: // console.log(num2);
18: // console.log(num3);
19: // console.log(num4);
20: 
21: 
22: // const num1 = 10;
23: // console.log(num1);
24: // num1 = 20;
25: // console.log(num1);
26: 
27: 
28: // let num;
29: // console.log(typeof num);
30: // console.log(num);
31: // num = 10;
32: // console.log(typeof num);
33: // console.log(num);
34: // num = 20;
35: // console.log(typeof num);
36: // console.log(num);
37: // num = 20.35;
38: // console.log(typeof num);
39: // console.log(num);
40: // num = 'abc';
41: // console.log(typeof num);
42: // console.log(num);
43: // num = false;
44: // console.log(typeof num);
45: // console.log(num);
46: 
47: // let firstName = 'Sonu';
48: // let lastName = "Rao";
49: // let fullName = firstName + " " + lastName;
50: // let fullName2 = `${firstName} ${lastName}`;
51: // console.log(fullName);
52: // console.log(fullName2);
53: // console.log('Hello')
54: 
55: // let num;
56: // let num2 = 20;
57: // console.log(num + num2);
58: // console.log(num - num2);
59: 
60: // nan
61: // let num;
62: // let num2 = 20;
63: // console.log(num + num2);
64: // console.log(num - num2);
65: 
66: // let num1 = 10;
67: // let num2 = '20';
68: // console.log(num1 + num2);
69: // console.log(num1 - num2);
70: 
71: // let num3 = 30;
72: // let num4 = 'abc';
73: // console.log(num3 + num4);
74: // console.log(num3 - num4);
75: 
76: // let num1 = 10;
77: // let num2 = '10';
78: // console.log(num1 == num2);
79: // console.log(num1 === num2);
80: // console.log(num1 != num2);
81: // console.log(num1 !== num2);
82: 
83: // truthy, falsy values
84: 
85: // falsy -> false, 0, '', undefined, null, NaN
86: // truthy -> everything else 
87: 
88: // let input = 'Sonu';
89: 
90: // if (input)
91: //     console.log('Yes');
92: 
93: 
94: 
95: // // arrays in js 
96: // const arr = [10, 20.5, 'abc', false, null, ['a', 3, true], 'sonu', 'monu', {}];
97: // // console.log(arr);
98: 
99: 
100: // // object in js 
101: 
102: // const employee = {
103: //     firstName: 'Sonu',
104: //     lastName: 'Joshi',
105: //     salary: 10.25,
106: //     address: { pin: 500001, city: 'Pune' },
107: //     isIndian: true,
108: //     phones: [9876543210, 6789012345],
109: //     print: () => { }
110: // };
111: 
112: // // console.log(employee);
113: // console.log(employee.firstName);
114: // console.log(employee.address.city);
115: // console.log(employee.phones[1]);
```

This file is entirely commented out — it's the instructor's exploratory scratch pad, walked through live rather than executed as a whole. It sequences almost exactly through Modules 1–3 of the courseware, so it's read as a guided tour rather than a running program.

- **Lines 8–19** — direct Java-to-JS contrast: Java's `int num = 10;` (statically typed, one declaration keyword) versus JS's four options — undeclared assignment (`num = 10`, creates an implicit global, flagged "don't use this"), `var` (flagged "don't use this too"), `const` (flagged "preferred choice"), and `let` ("use when needed") — this is the file's own hands-on version of Module 2.1's `var`/`let`/`const` rule.
- **Lines 22–25** — demonstrates the `const` reassignment error directly: `const num1 = 10;` then `num1 = 20;` would throw `TypeError: Assignment to constant variable`, reinforcing that `const` locks the *binding*.
- **Lines 28–45** — a **step-by-step `typeof` walkthrough on a single `let num` variable**, reassigned repeatedly: undeclared (`undefined`), then `10` (`'number'`), `20` (`'number'`), `20.35` (`'number'` — same type as `20`, illustrating JS's single numeric type per Module 2.2), `'abc'` (`'number'` → `'string'`, showing `let` allows changing not just value but *type*, since JS is dynamically typed), and `false` (`'boolean'`). This is a live demonstration that a `let` variable's type is not fixed — unlike Java where `int num` can never later hold a `String`.
- **Lines 47–52** — string concatenation (`firstName + " " + lastName`) versus a **template literal** (`` `${firstName} ${lastName}` ``) producing the same result, side by side — the exact comparison Module 2.7/8.1 makes for "why prefer template literals."
- **Lines 55–58** — `num` is declared but never assigned (`undefined`); `num + num2` → `NaN` (arithmetic with `undefined`), `num - num2` → `NaN` as well. Matches Module 2.5's `undefined + 1 // NaN` rule.
- **Lines 66–69** — `num1 = 10` (number), `num2 = '20'` (string): `num1 + num2` → `"1020"` (string concatenation wins because one operand is a string — Module 2.5's `+` rule), `num1 - num2` → `-10` (subtraction always coerces to numbers, so `'20'` becomes `20`, then `10 - 20 = -10`).
- **Lines 71–74** — `num3 = 30`, `num4 = 'abc'`: `num3 + num4` → `"30abc"` (string concat again). `num3 - num4` → `NaN`, because `'abc'` cannot be coerced to a number (Module 2.5's `"10" - "abc" // NaN`).
- **Lines 76–81** — the `==` vs `===` demonstration with `num1 = 10` (number) and `num2 = '10'` (string): `num1 == num2` → `true` (loose equality coerces `'10'` to `10`), `num1 === num2` → `false` (strict equality, types differ), `num1 != num2` → `false`, `num1 !== num2` → `true`. This is the file's hands-on version of Module 2.5's central rule, and one of the most likely direct assessment questions.
- **Lines 85–91** — falsy-value list matching Module 2.5 (though the file's shorthand list omits `-0`, `0n`, and `NaN`'s exact wording — the courseware's authoritative list of 8 is the one to cite on an assessment: `false, 0, -0, 0n, "", null, undefined, NaN`), followed by a truthy check: `if (input)` with `input = 'Sonu'` (a non-empty string, truthy) logs `'Yes'`.
- **Lines 96–97** — an array literal `[10, 20.5, 'abc', false, null, ['a', 3, true], 'sonu', 'monu', {}]` mixing every primitive type plus a nested array and an empty object — concretely proving Module 7.1's "any types" claim (JS arrays are not type-homogeneous like Java arrays/generics).
- **Lines 102–115** — an `employee` object literal (same shape reused in `js-functions-syntaxes.js`, showing it's a running example across the course) with nested object (`address`), array (`phones`), and an arrow-function method (`print`). The three `console.log` calls at the end (lines 113–115) demonstrate the three main property-access forms: simple dot access (`employee.firstName`), **chained** dot access into a nested object (`employee.address.city`), and array-index access on an object property (`employee.phones[1]`) — all core to Module 6.1's object-access patterns.

---

## Quick Reference — Highest-Yield Assessment Traps (cross-referenced to source)

1. `==` vs `===` — always `===` (Module 2.5, Module 3.2, `script.js` lines 76–81).
2. `0.1 + 0.2 !== 0.3` — IEEE 754 floating point (Module 3.1).
3. `var` loop-closure bug vs `let` fixing it (Module 2.1).
4. Arrow function has no own `this` — object methods need regular `function` syntax when they use `this` (Module 5.7, `js-functions-syntaxes.js` lines 25–26).
5. `forEach` returns `undefined`; `map` returns a new array (Module 5.5, 7.5).
6. `sort()` is lexicographic by default — always pass `(a,b) => a-b` for numbers (Module 7.3).
7. `||` replaces *all* falsy values (including valid `0`); `??` only replaces `null`/`undefined` (Module 3.3–3.4).
8. `fetch` never throws on 4xx/5xx — must check `response.ok` manually (Module 9.6, `js-api-calls.js`).
9. Forgetting `await` returns a Promise, not the resolved value (Module 9.5).
10. Microtasks (Promises) run before macrotasks (`setTimeout`) — `console.log` ordering questions (Module 9.2, `js-function-as-arg.js` lines 49–55).
11. `const` freezes the binding, not the value — objects/arrays are still mutable (Module 2.1, 6.5).
12. Spread/rest share `...` syntax but do opposite things depending on call vs. definition position (Module 3.7–3.8, `js-array-ops.js`).
13. JS classes support only single inheritance and exactly one constructor — no method/constructor overloading, unlike Java (Module 6.6, `js-oop-concepts.js` lines 21, 50).
14. Missing function/constructor arguments become `undefined` silently — no compile-time arity check (Module 5.2, `js-functions.js`, `js-oop-concepts.js` line 39).
15. Passing `fn()` instead of `fn` as a callback calls it immediately instead of registering it (Discussion Q&A #62, `js-function-as-arg.js` lines 36–37).
