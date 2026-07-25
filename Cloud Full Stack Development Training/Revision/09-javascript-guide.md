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

for (var i = 0; i < 3; i++) { setTimeout(() => console.log(i), 100); }
// Prints: 3 3 3 — var leaks out of the block, every closure captured the same `i`
for (let j = 0; j < 3; j++) { setTimeout(() => console.log(j), 100); }
// Prints: 0 1 2 — let creates a fresh binding per iteration

const user = { name: 'Alice', age: 30 };
user.age = 31;           // mutating the object is fine — const freezes the binding, not the value
user = { name: 'Bob' };  // TypeError — reassigning the binding is not allowed
```

### The 8 Data Types

**7 primitives + 1 object type**: `string`, `number` (one numeric type — no int/float/double split like Java), `bigint` (suffix `n`, for integers beyond `Number.MAX_SAFE_INTEGER` = 2^53−1), `boolean`, `undefined`, `null`, `symbol`, and `object` (arrays, functions, dates are all objects).

`typeof null === 'object'` is a famous historical bug — always check `value === null` directly, never rely on `typeof`.

### `null` vs `undefined`

`undefined` = the engine's way of saying "never assigned" (unassigned variable, missing property, function with no return). `null` = the programmer's explicit "no value here." Both are falsy; `value == null` is the one legitimate use of `==` since it catches both in one check.

### Type Coercion

The `+` operator: if **either** operand is a string, `+` becomes concatenation (`"5" + 3` → `"53"`, not `8`). `-`, `*`, `/` always coerce to numbers (`"10" - 5` → `5`). `1 + 2 + "3"` → `"33"` (left to right: numbers add first, then the string appends).

**`==` vs `===`** — the single most important comparison rule in the language: `==` coerces types before comparing (`"5" == 5` → `true`, `0 == false` → `true`, `null == undefined` → `true` but `null == 0` → `false`, an inconsistency). `===` requires matching types with no coercion. **Always use `===`/`!==`**; the only accepted `==` usage is `value == null`.

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

`map`, `filter`, `reduce`, `find`, `findIndex`, `every`, `some` are the functional backbone (full detail in Module 7). **`forEach` vs `map`**: `forEach` is for side effects and returns `undefined`; `map` returns a new transformed array. `numbers.forEach(n => n*2)` result is `undefined`.

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

These points surface in `11_MCQ_Assessment.md` and `javascript_discussion_qa.md` but aren't spelled out as their own subsection in Modules 01–10.

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

The files below are the actual working code from `Code/UI (HTML, CSS, JS, Ts, Node)/js/`. Several files contain large commented-out blocks left in place by design — they show the *progression* of a concept (e.g., "here's the broken version, here's the fixed version") that the instructor built up interactively. The explanation below covers those commented blocks too, calling out explicitly which lines actually execute.

## 12. `js-functions-syntaxes.js` — the Four Function Forms and `this` in Object Methods

```javascript
function fun1() { console.log('fun1 called.'); }; fun1();     // declaration — hoisted
const fun2 = () => { console.log('fun2 called.'); }; fun2();   // arrow expression — not hoisted

const gstCalc = amount => amount * 1.18;   // implicit return, no braces needed
console.log(gstCalc(100));

const employee = {
    firstName: 'Sonu', lastName: 'Joshi', salary: 10.25,
    address: { pin: 500001, city: 'Pune' }, isIndian: true,
    phones: [9876543210, 6789012345],
    printSalary: () => { console.log(this.salary); },        // undefined — arrow has no own `this`
    printSalary2: function () { console.log(this.salary); }  // 10.25 — regular fn, `this` = employee
};
employee.printSalary();
employee.printSalary2();

const myFuns = { fun1: () => { }, fun2: () => { } };
myFuns.fun2();
```

- `fun1` (declaration, hoisted) vs `fun2` (arrow expression, not hoisted) — same behavior when called, different hoisting rules.
- `employee` has two near-identical methods that behave *differently* because of `this` binding: `printSalary` (arrow property) has no own `this`, so it inherits the outer scope's `this` → `this.salary` is `undefined`. `printSalary2` (regular `function`) gets `this` bound to `employee` at call time → `this.salary` is `10.25`. This is the single most important `this` gotcha from Module 5.7: **never use arrow functions for object methods that need `this` to refer to the object.**
- `myFuns` shows arrow functions can also live as plain object property values and be invoked via property access, independent of the `this` issue above.

---

## 13. `js-functions.js` — Function Declarations, Defaults, and Argument-Count Mismatches

```javascript
// Old JS function
function fun1() { console.log('fun1 function called.'); }
fun1();

// Modern JS function == preferred choice
const fun2 = () => { console.log('fun2 function called.'); };
fun2();

const fun3 = () => { console.log('fun3 called.'); /* no return */ };
const output = fun3();
console.log(output); // undefined

const addNums = (a, b) => { console.log(a + b); };
addNums();          // NaN — a, b both undefined
addNums(10);        // NaN — b undefined
addNums(10, 20);     // 30
addNums(10, 20, 30); // 30 — extra arg silently ignored

const addNumsDefaulted = (a = 4, b = 5) => { console.log(a + b); };
addNumsDefaulted();       // 9  — both defaults apply
addNumsDefaulted(10);     // 15 — a overridden, b still defaults
```

- "Old" function declaration (`fun1`) vs the file's own-labeled "modern, preferred" arrow equivalent (`fun2`) — same behavior, style preference only.
- `fun3` has no `return` statement, so `fun3()` evaluates to `undefined` when logged — the concrete demo of "no explicit return → `undefined`."
- `addNums(a, b)` with **no defaults**: missing arguments leave params `undefined`, so `addNums()`/`addNums(10)` both give `NaN`. Extra args beyond the declared list are silently ignored — JS, unlike Java, never throws on arity mismatch.
- With both params defaulted (`a = 4, b = 5`), `addNumsDefaulted()` now gives `4 + 5 = 9` instead of `NaN` — defaults apply per-parameter, resolving the `NaN` bug from missing arguments.

---

## 14. `js-function-as-arg.js` — Passing Functions as Arguments (Named vs Anonymous) and `setTimeout`

```javascript
// Function as args in JS
const addNums = (a, b) => { console.log(a + b); };
addNums(10, 20); // 30

const fun = (arg) => { console.log('fun function called.'); arg(); };
// fun(10); fun('Sonu'); fun({ pin: 500001 });  // each would throw: arg is not a function
fun(() => { console.log('abc'); });               // works — arg is callable

const passedFun = () => { console.log('named function called.'); };
fun(passedFun);   // passes the function reference itself, not fun(passedFun()) which would call it immediately

console.log("One");
setTimeout(() => { console.log("Two"); }, 2000);
console.log("Three");
```

- `fun(arg)` calls `arg()` internally, so `arg` must be callable — passing a number/string/object would throw `TypeError: arg is not a function`; only a function value works. Shows JS does no compile-time parameter type checking.
- `fun(passedFun)` passes the *function itself* (a reference); `fun(passedFun())` would be the classic trap — calling it immediately and passing its return value instead.
- `setTimeout(callback, delayMs)`: `"One"`/`"Three"` are synchronous and run immediately; `setTimeout(..., 2000)` schedules `"Two"` as a **macrotask** that only fires after the timer expires and the call stack is empty. Actual order: `One`, `Three`, then `Two` two seconds later — even though `"Two"` is written between them in the source.

---

## 15. `js-callbacks.js` — Callback Hell → Promise → `async`/`await`, Three Solutions to the Same Problem

```javascript
// baseline — synchronous callback
const getDataSync = (arg) => { console.log('getData called'); arg({ city: 'Bengaluru' }); };
getDataSync((data) => { console.log(data.city); });

// "the problem of async JS" — getData has no return of its own
const getDataBroken = () => {
    setTimeout(() => { return { city: 'Bengaluru' }; }, 2000);   // this return goes nowhere
};
const output = getDataBroken();
console.log(output);         // undefined — console.log(output.city) would throw

// Solution 1 — callback, invoked inside the setTimeout body once "ready"
console.log("Start");
const getDataCb = (arg) => {
    console.log('getData called');
    setTimeout(() => { arg({ city: 'Bengaluru' }); }, 2000);
};
getDataCb((data) => { console.log(data.city); });

// Solution 2 — Promise (getData wraps the setTimeout in `new Promise(...)`, same body as Solution 3)
// getData().then(response => console.log(response.city)).catch(error => console.log(error.message));

// Solution 3 — Promise + async/await — the actually-executing code
const getData = () => {
    console.log('getData called');
    const isDataAvailable = false; // true false
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (isDataAvailable) resolve({ city: 'Bengaluru' });
            else reject({ message: 'Data not available' });
        }, 2000);
    });
};
const consumeData = async () => {
    try {
        const data = await getData();
        console.log(data.city);
    } catch (error) {
        console.log(error.message);
    }
};
consumeData();
```

- **Baseline** — a **synchronous callback**: `getData` immediately invokes `arg({...})`. Works because nothing is actually asynchronous yet.
- **"The problem of async JS"** — `getData` wraps its logic in `setTimeout` but has no `return` of its own — the arrow passed to `setTimeout` returns a value only the timer callback receives, discarded by `setTimeout` itself. `getData()` therefore evaluates to `undefined`, and `output.city` would throw. This is why async operations can't return values synchronously.
- **Solution 1 (callback)** — `getData(arg)` takes a callback and invokes it **inside** the `setTimeout` body once data is "ready." `console.log("Start")` logs before the callback ever fires — sync code runs immediately, the callback only fires after the 2-second macrotask.
- **Solution 2 (Promise)** — `getData()` returns `new Promise((resolve, reject) => {...})`; the caller chains `.then`/`.catch` instead of nesting callbacks. Because `isDataAvailable` is hardcoded `false`, `.catch` fires with `'Data not available'`.
- **Solution 3 (Promise + `async`/`await`) — the actually-executing code:** `getData` is unchanged from Solution 2. `consumeData` is `async`, so `await getData()` inside `try` pauses until the Promise settles; since it rejects, control jumps to `catch`. Real output order: `"Start"` (sync) → `"getData called"` (sync, runs the instant `getData()` is invoked) → *2s pause* → `"Data not available"` (after the timer rejects and `await` re-throws into `catch`). Flipping `isDataAvailable` to `true` only changes which branch executes, not the control-flow structure.

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
23: console.log(addNums(2, 3, 4, 7, 1));
```

- **Line 1** — the file's own header comment says "OOP in JS," but nothing below is OOP — a labeling artifact from the instructor's working notes.
- **Lines 3–7, 11–14** (commented, not executing) — positional **array destructuring** (`const [a,b,c,d,e] = arr`) and **rest-in-destructuring** (`const [a, b, ...remaining] = arr`, collecting everything after the first two into a new array).
- **Lines 18–23 — the actually-executing code:** `addNums = (...args) => args;` uses **rest parameters** to collect any number of arguments into a real array and return it directly — `addNums(2,3)` → `[2,3]`, `addNums(2,3,4,7,1)` → `[2,3,4,7,1]`.
- **Terminology check:** `...args` here is **rest** (collecting, in a function definition). The same `...` at a call site (`addNums(...someArray)`) would be **spread** (expanding) — same characters, opposite direction depending on position.

---

## 17. `js-oop-concepts.js` — Classes, Constructors, Inheritance — and How This Differs from Java

```javascript
1:  // OOP in JS 
2:  
3:  // class in JS 
4:  class Animal {
5:      name;
6:      color;
7:      food;
8:  
9:      // constructor() { }
10:     // A class may only have one constructor
11: 
12:     constructor(name, color, food) {
13:         this.name = name;
14:         this.color = color;
15:         this.food = food;
16:     };
17: 
18:     toPrint() {
19:         return `{name: '${this.name}', color: '${this.color}, food: '${this.food}'}`;
20:     };
21: }
22: // const animal2 = new Animal('Bob', 'Black');   // food left undefined — missing arg
23: 
24: const animal3 = new Animal('Moti', 'White', 'Bread');
25: console.log(animal3.toPrint());
26: 
27: class Alive { }
28: 
29: // inheritance 
30: // class Dog extends Animal , Alive  { // not working 
31: class Dog extends Animal { }
32: 
33: const animal4 = new Dog('Anny', 'Grey');
34: console.log(animal4.toPrint());
35: const animal5 = new Dog('Soni', 'Pink', 'Biscuits');
36: console.log(animal5.toPrint());
```

- An earlier, superseded version (not shown) declared bare fields with no constructor and set them one at a time via dot-notation after `new Animal()` — no encapsulation.
- **Lines 4–21 — the real `Animal` class:** `name; color; food;` are **class field declarations** (ES2022) — optional documentation; `this.field = ...` in the constructor works with or without them. The commented `constructor() {}` plus "A class may only have one constructor" (line 10) confirms JS has no real overload resolution — only default parameters simulate it. `toPrint()` is an instance method, functionally a `toString()`-equivalent, but must be called explicitly — `console.log(animal3)` would **not** invoke it automatically.
- **Line 22** — `new Animal('Bob', 'Black')` (only 2 of 3 args) would leave `food` as `undefined` — missing constructor args are silently `undefined`, no compile-time arity check, unlike Java.
- **Lines 27–31** — an empty `Alive` class exists purely so line 30's commented `class Dog extends Animal, Alive` can demonstrate a real limitation: JavaScript classes support only **single inheritance**, not multiple `extends`.
- **Lines 31–36 — actual working inheritance:** `class Dog extends Animal {}` has no constructor of its own, so it implicitly gets a default constructor calling `super(...args)`, forwarding whatever was passed straight to `Animal`'s constructor. `toPrint()` is found by walking the prototype chain (`animal4 → Dog.prototype → Animal.prototype`).

### JavaScript OOP vs Java OOP — explicit contrast (referencing the Core Java guide's Module 8 "Classes and Objects" and Module 10 "Inheritance")

| Aspect | Java (Core Java guide) | JavaScript (`js-oop-concepts.js`) |
|---|---|---|
| Underlying model | True class-based — a class is a compiler-level blueprint; `new` allocates from a fixed layout | **Prototype-based** — `class` is syntax sugar; `new Dog(...)` really builds an object linked via `[[Prototype]]` to `Dog.prototype`, which links to `Animal.prototype` |
| Constructors | Overloading allowed — multiple constructors, resolved by parameter *types and count* at compile time | **Only one constructor per class** (file's own comment confirms this) — "overloading" is simulated with default parameters, not real overload resolution |
| Missing constructor args | Compile error — signature must match exactly | **Silently `undefined`** — no compile-time arity/type check at all (`new Animal('Bob', 'Black')`) |
| Field declarations | Mandatory — every field needs an explicit type | **Optional** (documentation-only) — `this.x = x` in the constructor works with or without a prior field declaration |
| Multiple inheritance | Not via `extends` (single class inheritance), but multiple `implements Interface` is standard and common | **Not supported at all** (`extends Animal, Alive` fails) — no interfaces either; only single-class `extends` |
| Method resolution | Vtable-based dynamic dispatch through the class hierarchy, resolved by the JVM | **Prototype chain walk** — `toPrint()` is looked up on `animal4` itself, then `Dog.prototype`, then `Animal.prototype`, stopping at the first match |
| `toString()` equivalent | Override `Object`'s `toString()`; automatically invoked by `println`/string concatenation | No automatic hook unless you specifically override `toString()`; this file's `toPrint()` is a **custom-named** method and must be called explicitly — `console.log(animal3)` would NOT use it |
| Access control | `private`/`protected`/`public`/package-private, enforced by the compiler | No enforcement here at all — `name`, `color`, `food` are fully public; true privacy needs the `#field` syntax (Module 6.6), not used in this file |

---

## 18. `js-dom-html.js` — DOM Selection, Event Listeners, and `confirm()`

```javascript
1:  // element.addEventListener('event-to-capture', () => { /* handler */ });
2:  
3:  document.getElementById("submit").addEventListener("click", function () {
4:      const name = document.getElementById("username").value;
5:      const output = document.getElementById("output");
6:      // output.textContent = `Hi ${name}!`;   // earlier greeting version, replaced
7:      // alert(`Hi ${name}!`);                  // earlier alert() version, replaced
8:      output.textContent = confirm("Are you sure?") ? 'Yes' : 'No';
9:  });
```

- `addEventListener(eventName, handlerFunction)` is a **higher-order function** — same "function as argument" pattern as Module 5.4. `getElementById("submit")` selects the button by id; a regular (non-arrow) function is attached as the handler, consistent with the courseware guidance that handlers are a context where a regular function's dynamic `this` can be useful, even though this handler doesn't use `this`.
- Two earlier response versions (plain greeting via `textContent`, then `alert(...)`) were commented out and replaced by the line that actually runs: `confirm("Are you sure?") ? 'Yes' : 'No'`. `confirm()` is a **blocking, synchronous** browser dialog returning a boolean (`true`=OK, `false`=Cancel), fed straight into a ternary.
- **DOM-specific gotchas:** `.value` reads a form-input's current value; `.textContent` sets plain text (safe from HTML injection) vs `.innerHTML` (parses HTML, riskier, not used here). `confirm()`/`alert()` block the entire single JS thread until the user responds — one of the few places synchronous blocking UI is still idiomatic in plain browser JS.

---

## 19. `js-api-calls.js` — Two Ways to Consume a REST API: `.then()/.catch()` vs `async`/`await`

```javascript
1:  const apiUrl = 'https://jsonplaceholder.typicode.com/users/2';
2:  
3:  // Promise-chain style
4:  fetch(apiUrl)
5:      .then((response) => { return response.json() })
6:      .then((data) => { console.log(data); })
7:      .catch((error) => { console.error(error); });
8:  
9:  // async / await style
10: const consumeRestApi = async () => {
11:     try {
12:         const response = await fetch(apiUrl);
13:         const data = await response.json();
14:         console.log(data);
15:     }
16:     catch (error) {
17:         console.error(error);
18:     }
19: };
20: consumeRestApi(); 
```

- **Promise-chain style (lines 4–7):** `fetch(apiUrl)` returns a Promise that resolves to a `Response` object, not the JSON body — `response.json()` (line 5) returns *another* Promise, and returning it from `.then` makes the chain wait before the second `.then` (line 6) receives parsed `data`. Gotcha (Module 9.6): `.catch` catches network failures but **not** a non-2xx HTTP status, since `fetch` doesn't reject on 4xx/5xx and this file never checks `response.ok`.
- **`async`/`await` style (lines 10–20), functionally equivalent:** `await fetch(apiUrl)` pauses for the `Response`, `await response.json()` pauses again for the parsed body; `try/catch` replaces `.then/.catch` — same missing-`response.ok` caveat applies. The async function must still be explicitly called (line 20) — declaring it does nothing by itself.
- **Both blocks run independently and concurrently** when the file loads (no shared chaining), producing two separate, interleaved sets of console output. This file is the cleanest side-by-side proof that `async`/`await` (Module 9.5) is syntactic sugar over the identical Promise-chain mechanics (Module 9.4).

---

## 20. `script.js` — The Foundational Sandbox: Declarations, Coercion, `==`/`===`, Truthy/Falsy, Arrays, Objects

```javascript
// java: int num = 10;
// JS:
num = 10;          // don't use this — implicit global
var num2 = 20;      // don't use this too
const num3 = 30;    // preferred choice
let num4 = 40;      // use when needed

const num1 = 10;
num1 = 20;           // TypeError: Assignment to constant variable

let num;             // typeof num -> 'undefined'
num = 10;             // 'number'
num = 20.35;          // 'number' — same type as 20 (single numeric type)
num = 'abc';           // 'string' — let allows changing type, not just value
num = false;            // 'boolean'

let firstName = 'Sonu', lastName = 'Rao';
let fullName = firstName + " " + lastName;      // concatenation
let fullName2 = `${firstName} ${lastName}`;      // template literal, same result

let n;  let n2 = 20;
console.log(n + n2);  // NaN — undefined coerced in arithmetic
console.log(n - n2);  // NaN

let a1 = 10, a2 = '20';
console.log(a1 + a2);  // "1020" — + concatenates when either side is a string
console.log(a1 - a2);  // -10  — - always coerces to numbers

let b1 = 30, b2 = 'abc';
console.log(b1 + b2);  // "30abc"
console.log(b1 - b2);  // NaN — 'abc' can't be coerced to a number

let c1 = 10, c2 = '10';
console.log(c1 == c2);   // true  — loose equality coerces '10' to 10
console.log(c1 === c2);  // false — strict, types differ

// falsy -> false, 0, '', undefined, null, NaN ; truthy -> everything else
let input = 'Sonu';
if (input) console.log('Yes');   // non-empty string is truthy

const arr = [10, 20.5, 'abc', false, null, ['a', 3, true], 'sonu', 'monu', {}];

const employee = {
    firstName: 'Sonu', lastName: 'Joshi', salary: 10.25,
    address: { pin: 500001, city: 'Pune' },
    isIndian: true, phones: [9876543210, 6789012345],
    print: () => { }
};
console.log(employee.firstName);      // dot access
console.log(employee.address.city);   // chained dot access into nested object
console.log(employee.phones[1]);      // array-index access on an object property
```

This file is entirely commented out in the original source — it's the instructor's exploratory scratch pad, walked through live rather than executed as a whole. It sequences almost exactly through Modules 1–3 of the courseware; the snippet above condenses it into the executable shape (with only the outcome comments kept).

- **Declarations** — direct Java-to-JS contrast: Java's single `int num = 10;` versus JS's four options (undeclared/implicit global, `var`, `const`, `let`), and the concrete `const` reassignment error proving `const` locks the *binding*, not the value.
- **`typeof` walkthrough** — reassigning one `let num` repeatedly shows a `let` variable's type is never fixed (`number` → `string` → `boolean`), unlike Java where `int num` can never later hold a `String`.
- **Coercion trio** — `undefined` arithmetic → `NaN`; `+` concatenates when either operand is a string but `-` always coerces to numbers (`'20'` → `20`); a non-numeric string like `'abc'` makes `-` produce `NaN` even though `+` still concatenates.
- **Falsy list** here is a shorthand (`false, 0, '', undefined, null, NaN`) — the authoritative 8-item list also includes `-0` and `0n`.
- **Array literal** mixes every primitive type plus a nested array and empty object — proof JS arrays are not type-homogeneous like Java arrays/generics.
- **`employee` object** is the same shape reused in `js-functions-syntaxes.js`, showing it's a running example across the course.

---

## Quick Reference — Highest-Yield Assessment Traps (cross-referenced to source)

1. `==` vs `===` — always `===` (Module 2.5, Module 3.2, `script.js`).
2. `0.1 + 0.2 !== 0.3` — IEEE 754 floating point (Module 3.1).
3. `var` loop-closure bug vs `let` fixing it (Module 2.1).
4. Arrow function has no own `this` — object methods need regular `function` syntax when they use `this` (Module 5.7, `js-functions-syntaxes.js`).
5. `forEach` returns `undefined`; `map` returns a new array (Module 5.5, 7.5).
6. `sort()` is lexicographic by default — always pass `(a,b) => a-b` for numbers (Module 7.3).
7. `||` replaces *all* falsy values (including valid `0`); `??` only replaces `null`/`undefined` (Module 3.3–3.4).
8. `fetch` never throws on 4xx/5xx — must check `response.ok` manually (Module 9.6, `js-api-calls.js`).
9. Forgetting `await` returns a Promise, not the resolved value (Module 9.5).
10. Microtasks (Promises) run before macrotasks (`setTimeout`) — `console.log` ordering questions (Module 9.2, `js-function-as-arg.js`).
11. `const` freezes the binding, not the value — objects/arrays are still mutable (Module 2.1, 6.5).
12. Spread/rest share `...` syntax but do opposite things depending on call vs. definition position (Module 3.7–3.8, `js-array-ops.js`).
13. JS classes support only single inheritance and exactly one constructor — no method/constructor overloading, unlike Java (Module 6.6, `js-oop-concepts.js`).
14. Missing function/constructor arguments become `undefined` silently — no compile-time arity check (Module 5.2, `js-functions.js`, `js-oop-concepts.js`).
15. Passing `fn()` instead of `fn` as a callback calls it immediately instead of registering it (Discussion Q&A #62, `js-function-as-arg.js`).
