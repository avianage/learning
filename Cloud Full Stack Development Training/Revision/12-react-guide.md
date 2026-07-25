# React — Complete Line-by-Line Guide

This guide is grounded in two sources from Aakash's course: the 16-module React courseware (`Courseware/06-react/00-getting-started.md` through `15-bonus-and-roundup.md`, plus `react_discussion_qa.md`), and a real React + TypeScript "EMS" (Employee Management System) frontend at `Code/React/src`, which has routing, a Context-based auth layer, Redux Toolkit state, and an Axios service layer.

Part 1 walks the courseware topic by topic, numbered exactly as the modules are numbered (00–15). Part 2 walks the actual project file by file, in dependency order — models → services → redux → context → routing → components → pages → app bootstrap — reproducing each file with line numbers and explaining every non-trivial line. Where the project code diverges from the "textbook" pattern shown in the courseware (commented-out `BrowserRouter`/`StrictMode`, `any`-typed context, a couple of latent bugs), that is called out explicitly rather than smoothed over.

As covered in the JavaScript/TypeScript guides from this same course, destructuring, spread, arrow functions, `async/await`, interfaces, and generics are assumed knowledge — explanations below focus on what is React-specific.

---

# PART 1 — Courseware, Module by Module

## 0. Getting Started with React

React is a UI **library**, not a framework — it owns only the View layer; routing (React Router), HTTP (Axios), state (Redux Toolkit), and forms are separate libraries you choose yourself. Its selling points are: **component-based** UI, **declarative** rendering (you describe *what*, React figures out *how*), the **Virtual DOM** (an in-memory tree React diffs against the real DOM so it only patches what changed), and **unidirectional data flow** (data flows parent → child only, which makes bugs traceable).

**Scaffolding:** `npm create vite@latest acme-ems-app -- --template react-ts` creates a Vite + React + TypeScript project. Vite is the build tool: it serves ES modules natively in dev (near-instant startup, unlike Webpack which bundles everything upfront) and produces a tree-shaken, minified, hashed production bundle on `npm run build`.

**Bootstrap chain:** `index.html` contains `<div id="root"></div>` and a `<script type="module" src="/src/main.tsx">`. `main.tsx` is the entry point:

```tsx
1:  import { StrictMode } from 'react'
2:  import { createRoot } from 'react-dom/client'
3:  import './index.css'
4:  import App from './App.tsx'
5:
6:  createRoot(document.getElementById('root')!).render(
7:    <StrictMode>
8:      <App />
9:    </StrictMode>,
10: )
```

- **Line 2** — `createRoot` is the React 18+ root API (replaces the legacy `ReactDOM.render`). It enables concurrent features.
- **Line 6** — `document.getElementById('root')!` — the `!` is a TypeScript **non-null assertion**: you're telling the compiler "trust me, this element exists," since `getElementById` returns `Element | null`.
- **Lines 7–9** — `<StrictMode>` is a dev-only wrapper: it double-invokes render functions and effects to surface side-effect bugs (e.g., missing cleanup). It is a complete no-op in production — zero cost.

**Full render cycle (first paint):** browser requests `index.html` → parses it, finds the empty `#root` and the `main.tsx` script → Vite transforms TS/JSX to JS → `main.tsx` runs → `createRoot().render(<App/>)` → React calls `App()`, gets JSX back → JSX becomes a Virtual DOM tree → React diffs it against nothing (first render, everything is new) → real DOM nodes are created and inserted → browser paints. On every subsequent state change, the same ⑥–⑩ steps repeat, but the diff step means only changed nodes are touched — that's the performance win.

Available scripts: `npm run dev` (hot reload dev server), `npm run build` (compile to `dist/`), `npm run preview` (serve the production build locally), `npm run lint`.

---

## 1. JavaScript & TypeScript Refresh

Skimmed here since Aakash already has dedicated JS/TS guides — only the React-specific angle:

- **`const` everywhere**, `let` only for reassignment/loops, never `var`.
- **Arrow functions** are the default for event handlers, `.map()` callbacks, and `useEffect` bodies — `<button onClick={() => setCount(c => c + 1)}>`.
- **Destructuring** is how every component reads its props: `function EmployeeCard({ employee, onRemove }: Props)`. Array destructuring is how every `useState` call works: `const [count, setCount] = useState(0)`.
- **Spread** is the mechanism for all immutable state updates: `setForm(prev => ({ ...prev, name: 'Bob' }))`, `setList(prev => [...prev, item])`, `setList(prev => prev.filter(...))`. React state must never be mutated in place — spreading (or `.map`/`.filter`, which already return new arrays) is what keeps object/array references "new" so React's reference-equality checks detect the change.
- **`async/await`** is required for API calls, but a `useEffect` callback itself **cannot be `async`** (an async function returns a Promise, and React expects either nothing or a cleanup function back from the effect) — so the pattern is always: define an inner `async` function, then call it immediately.
- **Optional chaining `?.`** and **nullish coalescing `??`** guard against `undefined`/`null` API data: `employee?.address?.city ?? 'Unknown'`. Note `??` only falls back on `null`/`undefined`, unlike `||` which also falls back on `0` or `''` — important for numeric fields like `salary`.
- **TypeScript interfaces** are the preferred shape for props and API data; **type aliases with unions** (`type Department = 'Engineering' | 'Marketing' | ...`) model closed sets of string values; **generics** parameterize `useState<Employee[]>([])`.

---

## 2. JSX and Components

JSX is **not HTML** — it's syntax sugar that the compiler turns into `React.createElement(type, props, children)` calls, each of which returns a plain JS object (`{ type, props, key, ref }`) that React assembles into the Virtual DOM tree.

**The rules, all of them:**
1. **One root element** per return — wrap siblings in a `<div>` or, preferably, a `<>...</>` Fragment (no extra DOM node).
2. **Every tag must close** — `<img />`, `<input />`, `<br />`.
3. **`class` → `className`, `for` → `htmlFor`** (both are reserved JS words).
4. **camelCase attributes** — `onClick`, `tabIndex` (exception: `data-*`/`aria-*` stay hyphenated).
5. **`{}` holds expressions, not statements** — `{isAdmin ? 'Admin' : 'User'}` works, `{if (x) {...}}` does not, because `if` is a statement.
6. Short-circuit `&&` works because it's an expression too: `{isAdmin && <span>Admin</span>}`.

**Function components:** must start with an **uppercase letter** — this is how React tells `<div />` (real DOM element) apart from `<Greeting />` (a call to your function). A component returns JSX or `null`.

**Composition** is the core mental model: complex UIs are trees of small components nested inside each other (`App` → `Header` → `NavBar`).

**File conventions used throughout the course:** one component per file, file name = component name; `components/` for reusable UI, `pages/` for route-level components, `hooks/` for custom hooks, `services/` for API calls, `types/` for shared interfaces.

---

## 3. Props, State, and Events

**Props** are read-only inputs a parent passes to a child — effectively function parameters for components. Rules: they flow **one way** (parent → child), and a child **must never mutate** them. If a child needs to communicate upward, it invokes a callback prop the parent provided (`onRemove={handleRemove}`), and the parent updates its own state.

The **`children` prop** is whatever is nested between a component's opening/closing tags, typed `ReactNode`. It's how wrapper/layout components work.

**State** (`useState`) is data a component owns that changes over time and, when changed via its setter, triggers a re-render:

```
const [value, setValue] = useState(initialValue)
```

When `setValue` is called: React schedules a re-render → the component function runs again from the top → JSX is re-evaluated with the new value → React diffs new vs. previous Virtual DOM → only the changed real DOM nodes are patched → browser repaints. Directly mutating the underlying variable (`count = count + 1`) skips step one entirely — React has no idea anything changed, so the screen stays stale. **You must always call the setter.**

**Object/array state updates must be immutable:** `setForm(prev => ({ ...prev, name: 'Bob' }))`, `setList(prev => [...prev, item])`, `setList(prev => prev.filter(e => e.id !== id))`, `setList(prev => prev.map(e => e.id === id ? {...e, isActive: !e.isActive} : e))`.

**Functional updates** (`setCount(prev => prev + 1)`) matter whenever new state depends on old state, because multiple `setState` calls in the same handler are batched and would otherwise all read the same stale closed-over value.

**Events** are wrapped in a **SyntheticEvent** for cross-browser consistency; handler names are camelCase (`onClick`, `onChange`, `onSubmit`). Critical distinction: `onClick={handleClick}` passes the function reference (correct); `onClick={handleClick()}` **calls it during render** and passes its return value instead (almost always wrong). To pass arguments, wrap in an arrow: `onClick={() => handleDelete(id)}`.

**Lifting state up:** when siblings need the same data, the state moves to their nearest common parent, which owns it and passes both the value and the updater callbacks down as props.

---

## 4. Lists and Conditionals

Arrays are rendered only via `.map()` returning JSX elements, each requiring a **`key`** prop that is unique among siblings and **stable across renders** (an ID, not an array index). React uses `key` to match elements between renders when the list changes (add/remove/reorder) — without a stable key, React may reuse the wrong DOM node or component state for the wrong data (a classic symptom: typed text appearing in the wrong input after a list reorders).

**Five conditional-rendering patterns:**
1. **Ternary** — `{isActive ? '● Active' : '○ Inactive'}`.
2. **Short-circuit `&&`** — `{isAdmin && <button>Delete</button>}`. **Gotcha:** `{count && <p>{count} items</p>}` renders the literal `0` on screen when `count` is `0`, because `0` is falsy but still a renderable value — fix with `{count > 0 && ...}` or a ternary.
3. **Nullish coalescing in content** — `{department ?? '—'}`.
4. **Early return / guard clause** — `if (!id) return <p>Select an employee.</p>` before the main return.
5. **Variable assigned before return**, computed via `if/else if` chains, then rendered as `{content}`.

Returning `null` renders nothing at all (no DOM node). `<Fragment key={...}>` is needed instead of the `<>` shorthand whenever a mapped fragment needs a key (the shorthand syntax can't carry props).

---

## 5. Styling React Components

Four approaches, in increasing scope-specificity:

| Approach | Scope | Notes |
|---|---|---|
| Global CSS | Whole app | Imported once in `main.tsx`; good for resets, CSS custom-property design tokens, utility classes |
| CSS Modules | Per component | `Card.module.css` → `import styles from './Card.module.css'` → Vite hashes class names (`Card_card__3xKp1`) so there are zero collisions app-wide |
| Inline styles | Per element | JS object, camelCase keys, `style={{ width: `${pct}%` }}` — reserve for values only known at runtime; CSS custom properties (`var(--x)`) don't work inside inline style objects |
| CSS-in-JS | Per component | Not used in this course; extra bundle weight |

Dynamic class combination: template literals for simple cases, array `.filter(Boolean).join(' ')` or the `clsx` library for complex conditional combinations. Dark mode is implemented by overriding CSS custom properties under `[data-theme='dark']` or `@media (prefers-color-scheme: dark)` — no JS changes needed in individual components since they already reference `var(--color-x)`.

---

## 6. Debugging React Apps

**React DevTools** (browser extension) adds Components and Profiler tabs. Components tab: inspect any component's live props/state, edit state values directly, "highlight updates" flashes components on re-render. Profiler tab: record an interaction, see which components re-rendered and how long each took.

**Console techniques:** `console.table(arr)` for arrays of objects, `console.group/groupEnd`, `console.time/timeEnd`, and the `debugger` statement to pause execution at an exact line when DevTools is open.

**The seven canonical React bugs**, all of which recur through the modules:
1. **State mutation** — mutating an object/array in place then calling the setter with the same reference; React's shallow-equality check sees "no change" and skips the re-render. Fix: always produce a new reference via spread/map/filter.
2. **Stale closures** — `setCount(count + 1)` called multiple times in one handler all read the same stale `count`. Fix: functional updates, `setCount(prev => prev + 1)`.
3. **`useEffect` infinite loops** — no dependency array (runs every render, and if it also sets state, loops forever), or an object/array literal recreated every render inside the deps array.
4. **Calling instead of passing a handler** — `onClick={handleRemove(id)}` invokes it during render.
5. **Reading state immediately after setting it** — updates are asynchronous/queued; `count` on the next line is still old.
6. **Missing or index-based keys.**
7. **The `{0 && ...}` falsy-but-renderable gotcha.**

**TypeScript as a debugger:** red squiggles for typos in prop names, wrong argument types, and missing required props catch entire bug classes before the code ever runs.

**Error Boundaries** are the one place **class components are still required** in modern React — they use `static getDerivedStateFromError()` and `componentDidCatch()` (lifecycle methods with no function-component equivalent) to catch render-time errors in their subtree and show fallback UI instead of a blank white screen. They do **not** catch errors in event handlers, async code, or inside themselves — those still need `try/catch`.

---

## 7. Deep Dive on Components — Hooks & Internals

**Component lifecycle in function-component terms:** MOUNT (first render, `useEffect(fn, [])` fires after) → UPDATE (props/state change, `useEffect(fn, [dep])` fires when a listed dep changes) → UNMOUNT (the cleanup function returned from `useEffect` runs).

**`useEffect`** runs **after** the browser paints — it's for side effects (fetching, subscriptions, timers, manual DOM work) that shouldn't block rendering. Four dependency-array modes: no array (every render — rare, risky), `[]` (once, on mount), `[dep1, dep2]` (whenever a listed dependency changes), and a **returned cleanup function** (runs before the next effect invocation and on unmount — critical for removing event listeners, clearing intervals, and aborting in-flight fetches via `AbortController`). An `useEffect` callback can never itself be `async`; wrap the async logic in an inner function and invoke it.

**`useRef`** stores a mutable value that (1) persists across renders, (2) does **not** trigger a re-render when changed, and (3) can hold a reference to a real DOM node (`<input ref={inputRef} />` → `inputRef.current.focus()`). Use it for DOM access, timer IDs, or "previous value" tracking — anything the UI doesn't need to react to.

**`useMemo`** memoizes an expensive **computed value**, recalculating only when its dependency array changes — e.g. a filtered/sorted derived list. **`useCallback`** memoizes a **function reference** so it stays identical across renders unless its deps change; this only matters when that function is passed as a prop to a `React.memo`-wrapped child (otherwise `useCallback` buys nothing, since the child re-renders anyway).

**`React.memo`** wraps a component so React skips its re-render if its props are shallow-equal to last time. By default, when a parent re-renders, **every child re-renders too**, regardless of whether its own props changed — `React.memo` + stable (`useCallback`'d) function props is how you opt out of that.

**Custom hooks** are functions whose name starts with `use` that call other hooks internally — the mechanism for extracting and reusing stateful logic across components without changing the component tree shape (`useLocalStorage`, `useDebounce`, a domain hook like `useEmployees` that bundles state + derived data + handlers into one return object).

**Context API** solves **prop drilling** (passing a prop through several layers of components that don't themselves use it, just to reach a deep descendant). Mechanics: `createContext(defaultValue)` creates a context object; `<XContext.Provider value={...}>` supplies a value to every descendant; any descendant reads it with `useContext(XContext)`. The idiomatic pattern wraps this in a custom hook (`useTheme()`) that throws if called outside the provider, so misuse fails loudly at the call site rather than silently returning `undefined`.

---

## 8. HTTP & Ajax with Axios

**Why Axios over `fetch`:** automatic JSON parsing, throws on any 4xx/5xx (not just network failure), request/response **interceptors**, configurable base URL and timeout, and better TypeScript generics (`axios.get<Employee[]>(...)`).

**Environment variables:** Vite only exposes variables prefixed `VITE_` to frontend code (`.env`, `.env.production`), read via `import.meta.env.VITE_API_BASE_URL`; never put real secrets here since they end up in the shipped bundle.

**Centralised Axios instance:** create one `axios.create({ baseURL, timeout, headers })` and import it everywhere — components should never call `axios.get()` directly. A **request interceptor** runs before every outgoing request and is the standard place to attach the auth token from storage (`config.headers.Authorization = 'Bearer ' + token`). A **response interceptor** runs after every response and is the standard place to handle global 401s (clear the stored token, redirect to `/login`) and log 5xx errors once instead of at every call site.

**Typed service layer:** each feature gets a `xService.ts` file (e.g. `employeeService`) whose functions call the shared Axios instance and return typed `Employee`/`Employee[]` data — pages import the service, never Axios.

**Loading/error/success state pattern:** every async operation needs three pieces of state — `loading`, `error`, and the data itself — usually wrapped in a custom hook (`useEmployeesApi`) with a `try/catch/finally`, where `finally` guarantees the loading flag clears regardless of outcome.

**Cancellation:** an `AbortController` created inside `useEffect` and passed as `{ signal }` to the request, with `return () => controller.abort()` as cleanup, cancels in-flight requests when the component unmounts — avoiding "setState on an unmounted component" warnings and wasted network traffic.

---

## 9. Multi-Page Routing (React Router)

SPA routing swaps components client-side with no full page reload, unlike traditional server routing. Core API: `<BrowserRouter>` (provides routing context via the History API — wraps the app once), `<Routes>`/`<Route path element>` (URL → component mapping), `<Link>`/`<NavLink>` (navigation without reload; `NavLink` auto-adds an active class), `<Outlet />` (renders the matched child route inside a layout route), `<Navigate>` (declarative redirect), and the hooks `useNavigate()` (imperative navigation, e.g. after form submit), `useParams()` (reads `:id`-style URL segments, always as strings), `useSearchParams()` (reads/writes the `?key=value` query string so filters are shareable/back-button-friendly), `useLocation()`.

**Protected routes** are built as a wrapper component rendering `<Outlet />` only if authenticated, else `<Navigate to="/login" state={{ from: location }} replace />` — passing the attempted location along so the login page can redirect back to it after a successful login (`navigate(from, { replace: true })`).

---

## 10. Forms and Validation

**Controlled vs. uncontrolled:** in a controlled input, React state is the single source of truth — `value={state}` and `onChange={e => setState(e.target.value)}` — enabling per-keystroke validation and dynamic UI. In an uncontrolled input, the DOM itself holds the value, read via a `ref` (`inputRef.current.value`) only when needed (typically on submit); this causes fewer re-renders and is the only option for file inputs, whose value cannot be set programmatically.

**All input types**, controlled: text/number/email/password bind via `value`/`onChange`; checkboxes bind via `checked`/`e.target.checked` (not `.value`); radios compare `checked={selected === value}`; selects behave like text inputs; dates use `YYYY-MM-DD` strings.

**React Hook Form (RHF)** is the recommended library for anything beyond a couple of fields: `register('field', rules)` connects an input, `handleSubmit(onSubmit)` runs validation before calling your handler, `formState: { errors, isSubmitting, isDirty }` exposes validation and submission state, `reset(values)` repopulates the form (used to prefill an edit form after an async fetch).

**Zod** provides schema-based, type-safe validation (`z.object({...})`, `z.infer<typeof schema>` derives the TS type from the schema — single source of truth), wired into RHF via `resolver: zodResolver(schema)`. Cross-field validation uses `.refine()` (e.g., end date must be after start date).

---

## 11. Redux — Centralised State Management

**When to reach for Redux** instead of local `useState` or Context: server data shared across many unrelated pages, or state with many actions coming from many different places. Rule of thumb: if the same data would need to pass through 3+ component layers, or is needed on multiple pages, use Redux.

**One-way data flow:** a component dispatches an action → the relevant slice's reducer computes new state from `(currentState, action)` → the store saves the new state → any component that **selected** the changed slice of state re-renders.

**`configureStore`** combines feature reducers into one store and (unlike legacy Redux) includes `redux-thunk` middleware automatically. Typed wrapper hooks `useAppDispatch`/`useAppSelector` (built from `RootState`/`AppDispatch` inferred straight off the store) should always be used instead of the raw `react-redux` hooks, so selector/dispatch types stay accurate as the store evolves.

**`createSlice`** bundles a slice's initial state, synchronous **reducers**, and (via `extraReducers`) async-thunk lifecycle handling into one object, and auto-generates action creators. Crucially, reducers written with `createSlice` use **Immer** under the hood — code that looks like a direct mutation (`state.list.push(x)`, `emp.isActive = !emp.isActive`) is actually safe and produces a correctly immutable update, because Immer intercepts the "mutation" and produces a new state tree from it.

**`createAsyncThunk`** wraps an async operation (typically an API call) and auto-dispatches three action types — `pending`, `fulfilled`, `rejected` — which `extraReducers` handles to drive `loading`/`error`/`data` state, the same pattern the courseware's `useEmployeesApi` hook implements manually with `useState`.

**Selectors** are plain functions reading from `RootState`; **`createSelector`** (from Reselect, bundled with RTK) builds **memoized** derived selectors that only recompute when their listed input selectors' results actually change — the Redux equivalent of `useMemo`.

**Redux vs. Context:** Context has zero setup and is fine for small-medium shared state (theme, auth) but re-renders the whole subtree on any value change and has no time-travel debugging; Redux Toolkit adds DevTools (action log, diff view, time travel), a structured async pattern (`createAsyncThunk`), and granular selector-based re-render control — better for complex, large, or server-heavy app state.

---

## 12. Authentication

**Flow:** a protected route checks for a token → if absent, redirect to `/login`; if present, decode/verify and set the user in context/state → render the protected page.

**Auth service** wraps login/logout/profile API calls and exposes `saveToken`/`getToken`/`removeToken` around `localStorage`.

**AuthContext/AuthProvider**: state (`user`, `isLoading`) lives in a Context, not local component state, because auth needs to be readable from anywhere in the tree (nav bar, protected routes, role-gated UI). On mount, a `useEffect` checks for a saved token (`isLoading` starts `true` so `ProtectedRoute` doesn't redirect prematurely while that check is in flight). `login()` calls the service, persists the token, and sets user state; `logout()` clears storage and state.

**Login page**: controlled `email`/`password` inputs, `e.preventDefault()` on submit (always, on every form), `try/catch/finally` around the async `login()` call, and — using the location state saved by `ProtectedRoute` — `navigate(from, { replace: true })` to return the user to wherever they were trying to go.

**Role-based UI**: components read `user.role` (or `isAdmin`) from the context and conditionally render admin-only controls — this is enforced at the route level (via `ProtectedRoute requireAdmin`) *and* at the component level, since hiding a button is not the same as actually blocking the action server-side.

**Token storage security note** (explicit in the courseware): `localStorage`/`sessionStorage` are readable by any JS on the page (XSS risk) but immune to CSRF; `httpOnly` cookies are the opposite. `localStorage` is acceptable for training/demo purposes; production systems should prefer an `httpOnly` cookie.

---

## 13. Testing React Applications

**Philosophy: test behaviour, not implementation.** "Clicking Delete removes the employee from the list" is a good test; "handleDelete sets state correctly" is not — tests should survive internal refactors.

**Stack:** Vitest (test runner/assertions/mocking, the modern Vite-native equivalent of Jest — this project actually uses **Jest** directly, see Part 2), React Testing Library (RTL, renders components and queries the resulting DOM the way a user would), `@testing-library/user-event` (simulates realistic multi-event user interactions, preferred over the lower-level `fireEvent` for things like typing), and MSW (Mock Service Worker, intercepts real network calls so components can be tested against a fake but realistic backend rather than mocking modules directly).

**Query priority** (most to least preferred): `getByRole`, `getByLabelText`, `getByPlaceholderText`, `getByText`, `getByTestId` (last resort). `getBy*` throws if not found (use for asserting presence); `queryBy*` returns `null` (use to assert **absence**); `findBy*` is the async, polling version (use when waiting for something to appear after a state update).

**Testing custom hooks** in isolation uses `renderHook` + `act` (to wrap state-updating calls so React processes them synchronously in the test).

**Testing Redux slices**: dispatch actions (including hand-constructed thunk lifecycle actions like `{ type: fetchEmployees.fulfilled.type, payload: [...] }`) against a freshly configured store and assert on `store.getState()`.

---

## 14. Deploying the App

`npm run build` runs TypeScript compilation, JSX transform, tree-shaking, code-splitting, minification, and content-hashing (`index-Bx3k9Prt.js`, for cache-busting) into `dist/`. `npm run preview` serves that build locally for a final sanity check.

Environment variables are set per-environment (`.env.development`, `.env.production`); only `VITE_`-prefixed ones ship in the bundle.

Deploy targets covered: **Vercel** (zero-config, auto-detects Vite; needs a `vercel.json` rewrite rule so deep links like `/employees/1` don't 404 on refresh — SPA routing means only `index.html` really exists server-side), **Netlify** (same SPA-fallback concern, solved with a `public/_redirects` file: `/* /index.html 200`), **Docker** (multi-stage build — a `node:alpine` stage runs `npm run build`, then an `nginx:alpine` stage serves the static `dist/` output, with `nginx.conf`'s `try_files $uri $uri/ /index.html;` doing the same SPA-fallback job), and **GitHub Actions CI/CD** (lint → test → build → upload artifact → deploy-on-`main`-only job, gated with `needs: build-and-test`).

Performance checklist before shipping: bundle-size analysis (`rollup-plugin-visualizer`), route-level code splitting with `React.lazy` + `<Suspense>`, manual vendor chunk splitting, environment variables set correctly, tests green in CI, and no stray `console.log`/debug code left in.

---

## 15. Bonus & Roundup

Concept-level coverage of tools beyond the core stack:

- **Webpack** — the module bundler most enterprise/legacy React projects still use (Entry → Dependency Graph → Loaders → Plugins → Output); Vite uses Rollup under the hood and is far faster in dev because it serves native ESM instead of pre-bundling everything.
- **Next.js** — a full React *framework* (file-based routing, SSR/SSG, Server Components, built-in API routes) versus the client-side-only (CSR) Vite+React Router app built in this course; reach for it when SEO or first-paint performance matters.
- **Animations** — CSS transitions/`@keyframes` for zero-JS cases; **Framer Motion** (`motion.div` with `initial`/`animate`/`exit`/`transition`, `AnimatePresence` for exit animations on unmount, `layout` for automatic reflow animation) for anything richer.
- **Redux Saga** — a generator-function-based (`function*`, `yield`) middleware alternative to `createAsyncThunk` for genuinely complex async orchestration (cancellable long-running work, race conditions via `race()`, retry-with-backoff, `takeLatest` auto-cancelling stale in-flight requests). The course's explicit guidance: **stick with `createAsyncThunk` for most apps**; reach for Saga only when thunks get unmanageable.
- **Complete hooks reference** including React 19 additions: `useOptimistic` (instant UI update while an async mutation is in flight, auto-reverted on error), `useFormStatus` (read submission `pending` state from inside a form's descendant without prop drilling it), `useActionState` (manage form-action state, pairing with the native `<form action={fn}>` pattern).
- **Rules of Hooks** (non-negotiable): hooks are called **only** at the top level of a component or custom hook — never inside a condition, loop, or nested/regular function — because React tracks hook identity by **call order** across renders; anything that changes that order between renders desyncs state.

### Supplementary gotchas (from `react_discussion_qa.md`)

A handful of exam-relevant points not already folded in above:

- **Batching**: React groups multiple `setState` calls from the same event handler into a single re-render (React 18 extended this to async code too — "automatic batching"). This is *why* stale-closure bugs happen when you don't use the functional-update form.
- **Reconciliation's key assumption**: elements of a different `type` are assumed to produce entirely different subtrees (old subtree is destroyed, not diffed); for lists, `key` is the identity heuristic that makes this an O(n) algorithm instead of a theoretical O(n³) general tree diff.
- **`useEffect` vs `useLayoutEffect`**: `useEffect` fires asynchronously *after* paint (the default, almost always correct choice); `useLayoutEffect` fires synchronously *before* paint — only needed for DOM measurement or preventing visual flicker.
- **`forwardRef` / `useImperativeHandle`**: `ref` is not forwarded to a function component's inner DOM node automatically the way normal props are — `forwardRef` opts a component into receiving one, and `useImperativeHandle` (used with it) lets the child expose a curated API (`{ focus, reset }`) instead of the raw DOM node.
- **`null`, `undefined`, `false` in JSX** all render as literally nothing — this is precisely why `{isAdmin && <AdminPanel />}` works cleanly, and precisely why `{0 && <p/>}` does *not* (0 is falsy but not one of the three "invisible" values).

---

# PART 2 — The Real EMS Project, File by File

The project lives at `Code/React/src` (package name `acme-react-demo`). Key dependencies from `package.json`: `react`/`react-dom` 19.2.7, `react-router` 8.1.0 (note: the unified `react-router` package, not `react-router-dom` — the courseware's examples import from `react-router-dom`, but this codebase imports routing APIs directly from `react-router`, which is the current React Router package structure), `axios` 1.18.1, `@reduxjs/toolkit` 2.12.0, `react-redux` 9.3.0. Testing uses **Jest** + `@testing-library/react` + `babel-jest` (not Vitest, despite the courseware's Module 13 defaulting to Vitest — same RTL concepts, different runner/config).

Every file below still contains large blocks of commented-out earlier iterations (the instructor's teaching history of building the feature up in steps). Those are left untouched in the reproductions since they're real file content, but the explanations focus on the **active, uncommented code**.

## 2.1 Model — `src/models/employee.model.ts`

```typescript
1:  import type { Key } from "react";
2:
3:  export interface EmployeeType {
4:      _id: Key;
5:      firstName: string;
6:      lastName: string;
7:      email: string;
8:      salary: number;
9:      password: string;
10: }
```

- **Line 1** — `import type` is a TypeScript-only import (erased at compile time, no runtime JS emitted); `Key` is React's own type for values valid as a `key` prop (`string | number | bigint`).
- **Lines 3–10** — `EmployeeType` is the shared shape for an employee record throughout the app, imported by pages, the Redux slice, and the service layer. `_id` is typed as `Key` specifically so it can be dropped straight into a list's `key={emp._id}` without a cast — a deliberate design choice tying the model to its most common usage (MongoDB-style `_id`, matching the `EmployeeList.tsx` usage below). Including `password` in a type also used for read responses is questionable in a hardened production API, but is consistent with how the mock backend for this course round-trips data.

## 2.2 Service Layer

### `src/services/api.service.ts`

```typescript
const api = axios.create({ baseURL: 'http://localhost:3000' });

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) { config.headers.Authorization = `Bearer ${token}`; }
    return config;
});

export default api;
```

A single centralised Axios instance with a fixed `baseURL`, exactly the pattern from Module 08: every service imports this `api` object rather than calling `axios` directly. The **request interceptor** reads the JWT from `localStorage` (the storage mechanism the courseware explicitly flags as XSS-vulnerable but acceptable for training) and, if present, attaches it as an `Authorization: Bearer <token>` header — no individual service function has to remember to add it; returning `config` is the required interceptor contract. There is **no response interceptor** here (unlike the courseware's Module 08 example, which adds one to globally catch 401s and redirect to `/login`) — expired/invalid tokens are instead handled implicitly wherever a protected route re-checks `isLoggedIn`.

### `src/services/employee.service.ts`

```typescript
export const getEmployeeById = async (id) => await api.get(`/api/employees/${id}`);
export const getAllEmployees = async () => await api.get('/api/employees');
```

Thin, typed-by-convention wrappers around `api.get` — the "typed service layer" pattern from Module 08, minus explicit TS generics on the calls (`id` is implicit `any`, a looseness the project accepts elsewhere too). Each returns the full Axios response object; callers read `.data` themselves.

### `src/services/user.service.ts`

```typescript
export const loginUser = async (user) => await api.post('/api/auth/login', user);
export const registerUser = async (user) => await api.post('/api/auth/register', user);
export const logoutUser = async () => await api.post('/api/auth/logout');
```

Three thin POST wrappers, same pattern as `employee.service.ts` above. `AuthProvider.tsx` calls `loginUser` directly; `Login.tsx` calls the context's `login()` rather than this service, keeping token-persistence logic in one place (the provider).

### `src/services/user.service.test.ts`

```typescript
jest.mock('./api.service');
const mockedApi = api as jest.Mocked<typeof api>;

describe('user.service', () => {
    beforeEach(() => { jest.clearAllMocks(); });

    test('loginUser posts credentials to /api/auth/login and returns the response', async () => {
        const credentials = { email: 'trainee@example.com', password: 'secret123' };
        mockedApi.post.mockResolvedValue({ data: { token: 'jwt-abc', employee: { id: 1 } } });
        const result = await loginUser(credentials);
        expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/login', credentials);
        expect(result.data.token).toBe('jwt-abc');
    });
    // ...similar tests for failure, registerUser, logoutUser
```

- **Line 21** — `jest.mock('./api.service')` auto-mocks the entire module: every exported function becomes a `jest.fn()` returning `undefined` unless configured; line 23 casts it `as jest.Mocked<typeof api>` for TypeScript-aware `.mockResolvedValue(...)` etc. `beforeEach`'s `jest.clearAllMocks()` (line 27) resets call history so assertions like `toHaveBeenCalledTimes(1)` aren't polluted by a previous test.
- The rest is the classic mock-arrange-act-assert shape: stub what the dependency returns, call the real function under test, then assert both that the dependency was called correctly and the return value is correct — testing the service layer in isolation from the network, the "test behaviour, fast/offline" idea from Module 13.

## 2.3 Redux — `src/redux/store.ts` and `src/redux/empSlice.tsx`

### `src/redux/store.ts`

```typescript
const store = configureStore({
    reducer: {
        emp: empReducer
        // , dept: deptReducer, jobs: jobReducer etc
    }
});

export default store;
export type RootState = ReturnType<typeof store.getState>;
```

`configureStore` is given a `reducer` map with one key, `emp` (every top-level key becomes a branch of the global state tree, `state.emp.*`; the comment shows the extension point for more slices this project doesn't build out). Notably there is **no separate `auth` slice** — unlike the courseware's Module 11 example, which puts `employees` and `auth` reducers side by side, this project keeps auth entirely in React Context (see 2.4) and Redux only for employee data. `RootState` is inferred from `store.getState()` via `ReturnType<typeof ...>`, so it can never drift out of sync with the actual reducer shape. This project does **not** define the typed `useAppDispatch`/`useAppSelector` wrapper hooks the courseware recommends (Module 11 §11.3) — components call the raw `useDispatch`/`useSelector` directly and annotate the selector's `state` parameter inline.

### `src/redux/empSlice.tsx`

```tsx
const EmpSlice = createSlice({
    name: 'emp',
    initialState: {
        empData: { id: '', firstName: '', lastName: '', email: '', salary: '' },
        allEmpData: []
    },
    reducers: {
        getEmpById: (state, action) => { state.empData = action.payload; },
        getAllEmps: (state, action) => { state.allEmpData = action.payload; }
    }
});

export const { getEmpById, getAllEmps } = EmpSlice.actions;
export default EmpSlice.reducer;
```

`createSlice` takes a `name` (prefixes auto-generated action types, e.g. `'emp/getEmpById'`), an `initialState` (a single-employee shape `empData`, plus `allEmpData: []` for the list page), and a `reducers` map. Each reducer receives the slice's current `state` and the dispatched `action` (`{ type, payload }`), and appears to **mutate `state` directly** (`state.empData = action.payload`). As covered in Module 11, `createSlice` reducers run inside **Immer**, which intercepts these "mutations" on a draft and produces a correctly immutable new state object under the hood — this is *safe* here specifically because it's inside `createSlice`; the same pattern outside Redux Toolkit (plain `useState`) would be the "Bug 1 — state mutation" antipattern from Module 06. `EmpSlice.actions` is auto-generated: calling `getEmpById(someEmployee)` produces `{ type: 'emp/getEmpById', payload: someEmployee }` ready to `dispatch(...)`.

No `extraReducers`/`createAsyncThunk` are used here — unlike the courseware's fuller example (Module 11, §11.4), the async API call happens in the **component** via the service layer, and only the *result* is dispatched into Redux as a plain synchronous action — a simpler, more manual variant of the same one-way data flow, appropriate for a small teaching app.

## 2.4 Context — Auth

### `src/context/AuthContextType.tsx`

```tsx
const AuthContext = createContext<any>({
    isLoggedIn: false,
    employee: null,
    login: (_employee: any, _token: string) => { },
    logout: () => { }
});
export default AuthContext;
```

`createContext<any>(...)` creates the context object with a **default value**, used only if a component reads it via `useContext` without a `<AuthContext.Provider>` above it. Typing it `<any>` (rather than a proper `AuthContextType` interface, as Module 07/12 do) trades away compile-time safety on every `useContext(AuthContext)` call site — `login`, `logout`, `isLoggedIn`, `employee` are all effectively untyped wherever consumed (`NavBar`, `Login`, `AppRoutes`), a deliberate course simplification worth tightening in production. The default value's shape mirrors what `AuthProvider` actually supplies, so accidental usage outside a provider degrades gracefully (no-op functions, `isLoggedIn: false`) instead of throwing — a softer failure mode than the courseware's pattern of throwing inside a `useAuth()` wrapper hook if the context is `null`. This project has no such wrapper hook; every consumer calls `useContext(AuthContext)` directly.

### `src/context/AuthProvider.tsx`

```tsx
const AuthProvider = ({ children }: any) => {
    const storedEmployee = localStorage.getItem('employee');
    const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));
    const [employee, setEmployee] = useState(storedEmployee ? JSON.parse(storedEmployee) : null);

    const login = async (credentials: { email: string; password: string }) => {
        const response: any = await loginUser(credentials);
        if (!response.data?.token) { throw new Error('Invalid credentials'); }
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('employee', JSON.stringify(response.data.employee));
        setEmployee(response.data.employee);
        setIsLoggedIn(true);
        return response.data.employee;
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('employee');
        setEmployee(null);
        setIsLoggedIn(false);
    };

    return (
        <AuthContext.Provider value={{ isLoggedIn, employee, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
export default AuthProvider;
```

- **Line 6** — `{ children }: any` is typed `any` rather than `ReactNode` (the type Module 03/07 recommends); functionally identical, but loses a layer of type safety.
- **Lines 7–9** — `localStorage` is read synchronously during render to *seed* the two `useState` calls (React only runs the initializer once, on mount): `!!localStorage.getItem('token')` coerces "a token string exists" into `isLoggedIn`, and `JSON.parse(storedEmployee)` reconstructs the employee object — how the app "remembers" a session across reloads without re-hitting the login endpoint. This is a simpler version of the courseware's Module 12 pattern, which also verifies the token server-side via a commented-out `authService.getProfile()` call gated on `isLoading`; this project trusts the stored token's mere presence, with no expiry check or server verification.
- **Lines 11–21** — `login` is `async`, calls the `loginUser` service, then **throws** if `response.data?.token` is absent rather than failing silently — this is what lets `Login.tsx`'s `try/catch` show an error message. On success it persists to `localStorage`, updates both pieces of React state (triggering `NavBar`/`AppRoutes`/`Login` to re-render), and returns the employee object (unused by current call sites).
- **Lines 23–34** — `logout` is fully synchronous and does **not** call `logoutUser()` (the server-side logout endpoint) — unlike the courseware's Module 12 "fire and forget" API call before clearing local state, this is purely a client-side session clear. The `Provider` supplies `{ isLoggedIn, employee, login, logout }` down the tree as a fresh object literal every render, so any component consuming the whole context value re-renders whenever `AuthProvider` re-renders — the standard Context trade-off flagged in Module 11 §11.8.

### `src/context/AuthProvider.test.tsx`

```tsx
const TestConsumer = () => {
    const { isLoggedIn, employee, login, logout } = useContext(AuthContext);
    // exposes context internals as visible text/buttons so RTL can assert on them
    return (
        <div>
            <p>isLoggedIn: {String(isLoggedIn)}</p>
            <p>employee: {employee ? employee.name : 'none'}</p>
            <button onClick={() => login({ email: '...', password: '...' }).catch(() => {})}>trigger-login</button>
            <button onClick={logout}>trigger-logout</button>
        </div>
    );
};

jest.mock('../services/user.service', () => ({ loginUser: jest.fn() }));
const mockedLoginUser = loginUser as jest.Mock;

describe('AuthProvider', () => {
    beforeEach(() => { localStorage.clear(); mockedLoginUser.mockReset(); });

    test('hydrates isLoggedIn/employee from localStorage on mount', () => {
        localStorage.setItem('token', 'existing-jwt');
        localStorage.setItem('employee', JSON.stringify({ name: 'Stored Employee' }));
        render(<AuthProvider><TestConsumer /></AuthProvider>);
        expect(screen.getByText('isLoggedIn: true')).toBeInTheDocument();
        expect(screen.getByText('employee: Stored Employee')).toBeInTheDocument();
    });
    // ...
```

Since `AuthProvider` has no meaningful UI of its own, the test defines a minimal `TestConsumer` that exposes the context's internals as visible text and wires `login`/`logout` to buttons — the standard way to test a Context Provider's *behaviour* through RTL, since you can't call hooks/functions outside of a rendered component. `jest.mock('../services/user.service', () => ({ loginUser: jest.fn() }))` replaces only that named export (all `AuthProvider` needs), cast `as jest.Mock` so TypeScript knows it has Jest mock methods. `beforeEach` clears `localStorage` (jsdom's storage is real and would otherwise leak between tests) and resets the mock.

The reproduced test seeds `localStorage` **before** `render()`, proving the provider's `useState` initializers correctly hydrate from storage on mount — the "remember me across reload" behaviour described above. Later tests (not reproduced) exercise: default logged-out state with empty storage, a successful `login()` persisting token/employee and updating rendered text (via `waitFor`, since `login` is async), a `login()` failure leaving state/storage untouched, the thrown `Error('Invalid credentials')` propagating to the caller, and `logout()` clearing everything back to logged-out. Together they cover both branches of every conditional in `AuthProvider.tsx`.

## 2.5 Routing — `src/routes/appRoutes.tsx`

```tsx
1:  import { useContext } from "react";
2:  import { BrowserRouter, Navigate, Route, Routes } from "react-router";
3:
4:  import Login from "../pages/Login";
5:  import Home from "../pages/Home";
6:  import Register from "../pages/Register";
7:  import NavBar from "../components/navBar";
8:  import Page404 from "../pages/Page404";
9:  import AuthContext from "../context/AuthContextType";
10: import Parent from "../pages/Parent";
11: import Employee from "../pages/Employee";
12: import EmployeeList from "../pages/EmployeeList";
13: import EmployeeDetails from "../pages/EmployeeDetails";
14:
15: const AppRoutes = () => {
16:
17:     const { isLoggedIn } = useContext(AuthContext);
18:
19:     return (
20:         <BrowserRouter>
21:             <NavBar />
22:             <Routes>
23:                 <Route path="/" element={<Navigate to="/home" replace />} />
24:                 <Route path="/home" element={<Home />} />
25:                 <Route
26:                     path="/login"
27:                     element={isLoggedIn ? <Navigate to="/home" replace /> : <Login />}
28:                 />
29:                 <Route path="/employees" element={isLoggedIn ? <Employee /> : <Navigate to="/login" replace />} />
30:                 {/* /employees/:id, /employeeslist, /parent repeat the identical ternary, only the element differs */}
31:                 <Route path="/employees/:id" element={isLoggedIn ? <EmployeeDetails /> : <Navigate to="/login" replace />} />
32:                 <Route path="/employeeslist" element={isLoggedIn ? <EmployeeList /> : <Navigate to="/login" replace />} />
33:                 <Route path="/parent" element={isLoggedIn ? <Parent /> : <Navigate to="/login" replace />} />
34:                 <Route path="*" element={<Page404 />} />
42:                 <Route path="/register" element={<Register />} />
43:             </Routes>
44:         </BrowserRouter>
45:     );
46: };
47:
48: export default AppRoutes;
```

- **Line 2** — routing primitives are imported from the `react-router` package directly (this project's dependency, v8.1.0), not `react-router-dom` as most of the courseware's snippets show; the API surface used (`BrowserRouter`, `Routes`, `Route`, `Navigate`) is identical either way.
- **Lines 17, 20–21** — `isLoggedIn` is read from context at the top of the component so `AppRoutes` re-renders on login/logout, driving the route guards below live. `<BrowserRouter>` is instantiated **inside `AppRoutes`**, not in `main.tsx` as Module 09's canonical example does — functionally equivalent since `AppRoutes` renders once, high in the tree, but nothing above it (`AuthProvider`, `Provider`) can use router hooks like `useNavigate`. `<NavBar />` sits as a sibling of `<Routes>` inside `<BrowserRouter>`, so it appears on every page and can safely use `useNavigate`/`Link`.
- **Line 23** — the root path `/` immediately redirects to `/home` via `<Navigate replace />` — `replace` swaps the current history entry rather than pushing a new one, so the back button doesn't get stuck bouncing between `/` and `/home`.
- **Lines 25–40** — this project implements route protection with **inline ternaries per route** (`isLoggedIn ? <Page /> : <Navigate to="/login" replace />`, and the mirror-image check on line 27 for `/login` itself: if already logged in, redirect *away*) rather than the courseware's `<ProtectedRoute>` wrapper-component-with-`<Outlet/>` pattern (Module 09 §9.9, Module 12 §12.5). Functionally similar, but more repetitive — every protected route repeats the same ternary — and there's no "remember where I was going" `state={{ from: location }}` behaviour, so after logging in the user always lands wherever `Login.tsx` hardcodes (`/employeeslist`), not back at the page they originally tried to reach. `path="/employees/:id"` (line 33) declares a URL parameter `id`, read inside `EmployeeDetails` via `useParams()`.
- **Line 41** — the catch-all `path="*"` renders `Page404`; **critically it must be the last-matched pattern to act as a true fallback** — but note **line 42's `/register` route is declared after it**. React Router's `<Routes>` matches by best-match ranking, not strictly declaration order, so `/register` still resolves correctly — but this ordering is fragile style (the courseware explicitly notes "404 — must be last") and would be worth reordering for clarity/safety even though it happens to work.

## 2.6 Components

### `src/components/navBar.tsx`

```tsx
const NavBar = () => {
    const { isLoggedIn, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const handleLogout = () => { logout(); navigate('/login'); };

    return (
        <nav>
            <Link to='/home'>Home</Link>
            {isLoggedIn && <Link to='/parent'>Parent</Link>}
            {isLoggedIn && <Link to='/employees'>Employees</Link>}
            {isLoggedIn && <Link to='/employeeslist'>EmployeesList</Link>}
            {!isLoggedIn && <Link to='/login'>Login</Link>}
            {!isLoggedIn && <Link to='/register'>Register</Link>}
            {isLoggedIn && <button onClick={handleLogout}>Logout</button>}
            {/* <button onClick={toggleTheme}>Color Mode</button> */}
        </nav>
    );
};
export default NavBar;
```

- **Lines 7–13** — pulls `isLoggedIn`/`logout` off the context (`useContext`, usable because the context is `any`, nothing enforces that `login` isn't called too); `useNavigate()` works because `NavBar` renders inside `<BrowserRouter>` (established in `AppRoutes`, see 2.5). `handleLogout` clears auth state via `logout()` first, *then* navigates to `/login` — by the time that route renders, `isLoggedIn` is already `false`, so `AppRoutes`' ternary correctly shows `<Login/>` instead of redirecting away.
- **Lines 17–25** — five `&&` short-circuit expressions (Module 04 Pattern 2) gate each nav link on `isLoggedIn` or its negation, including the Logout button itself, so nothing appears when there's nothing to act on. Line 26 is a commented-out call to the `toggleTheme` utility described next.

### `src/components/toggleTheme.tsx`

```tsx
const toggleTheme = () => {
    const html = document.documentElement;
    html.dataset.theme = html.dataset.theme === "light" ? "dark" : "light";
};
export default toggleTheme;
```

`document.documentElement` reaches directly into the DOM (the `<html>` root), fine here since it's a pure side effect with nothing to re-render, the same category of imperative DOM manipulation Module 05 §5.7's dark-mode section describes. `html.dataset.theme` reads/writes the `data-theme` attribute; toggling it is what a CSS rule like `[data-theme='dark'] { --color-bg: ...; }` would key off to swap the app's colour tokens with zero React re-render needed. This is **not a hook** — a plain function, not `useToggleTheme`, so it holds no React state itself; it's wired up only as a commented-out `onClick` in `NavBar` above, so the feature exists in the codebase but isn't active in the running app.

## 2.7 Pages, in Logical Flow

### `src/pages/Login.tsx`

```tsx
const Login = () => {
    const { login, isLoggedIn } = useContext(AuthContext);
    const [user, setUser] = useState({ email: '', password: '' });
    const [message, setMessage] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        if (isLoggedIn) { navigate('/employeeslist', { replace: true }); }
    }, [isLoggedIn, navigate]);

    const handleInput = (evt: any) => {
        const { name, value } = evt.target;
        setUser((prevUser) => ({ ...prevUser, [name]: value }));
    };

    const submitInput = async (evt: any) => {
        evt.preventDefault();
        try {
            await login(user);
            setMessage('Login successful, going to Employee list...');
            setMessage('');
            navigate('/employeeslist');
        } catch (error) {
            setMessage('Invalid credentials.');
            console.error(error);
        } finally {
            setUser({ email: '', password: '' });
        }
    };

    return (
        <>
            <form onSubmit={submitInput}>
                <input type="email" name="email" value={user.email} onChange={handleInput} autoFocus placeholder="Enter your email" />
                <input type="password" name="password" value={user.password} onChange={handleInput} placeholder="Enter your password" />
                <button type="submit">🔓Login</button>
            </form>
            <p>{message}</p>
        </>
    );
};
export default Login;
```

A single `user` state object holds **both** form fields rather than two separate `useState` calls — the "state with objects" pattern from Module 03 §3.4. The `useEffect` redirects away from `/login` with `replace: true` (so it doesn't linger in history) whenever `isLoggedIn` becomes/is `true` — on mount or after a successful login; `navigate` in the deps array is correct-but-redundant since its identity is stable in React Router v6+. `handleInput` is a **single shared handler** for both inputs, using the DOM `name` attribute plus computed-property spread (`[name]: value`) to update just that one key of `user` immutably.

`submitInput` is `async`, calls `evt.preventDefault()`, then `try/catch/finally`: `finally` always clears the form fields regardless of outcome. On success it navigates to `/employeeslist` (no `replace`, unlike the effect's redirect, so back *would* return to `/login`). A real logic quirk: `setMessage('Login successful...')` is immediately overwritten by `setMessage('')` in the same synchronous block — React **batches** both updates, so only the last value ever reaches the screen and the success message never actually renders. The two inputs are fully controlled (`value`/`onChange` driven by state), the "React owns the value" model from Module 10 §10.2, contrasted with `Register.tsx`'s uncontrolled approach below.

There is a **known, intentionally-left-in gap** referenced in `Login.test.tsx`'s skipped test: an earlier commented-out version of this file had a `validateInput()` function whose boolean return value was computed but never actually checked before calling `login(user)` — the current active code has *no* client-side pre-submit validation, relying entirely on the server rejecting bad credentials.

### `src/pages/Login.test.tsx`

```tsx
const mockNavigate = jest.fn();
jest.mock('react-router', () => ({ useNavigate: () => mockNavigate }));

const renderLogin = (contextValue: any) =>
    render(<AuthContext.Provider value={contextValue}><Login /></AuthContext.Provider>);
```

Mocks the entire `react-router` module so `useNavigate()` returns a Jest spy (`mockNavigate`) instead of needing a real `<BrowserRouter>` ancestor, and `renderLogin` wraps `<Login>` directly in `<AuthContext.Provider value={contextValue}>` (bypassing the real `AuthProvider`) so each test can supply a **custom** `login`/`isLoggedIn` combination — a clean example of testing a consumer component in isolation from its actual provider. Later tests (not reproduced) verify: both inputs render, typing updates the controlled value via `fireEvent.change`, submit calls `login()` with the exact current field values, a successful login navigates to `/employeeslist`, a failed login shows `'Invalid credentials.'` and does **not** navigate, fields clear after submit either way, and rendering with `isLoggedIn: true` from the start immediately triggers `mockNavigate('/employeeslist', { replace: true })`, proving `Login.tsx`'s effect runs correctly on mount.

### `src/pages/Register.tsx`

```tsx
const Register = () => {
    const navigate = useNavigate();
    const firstNameRef = useRef<HTMLInputElement>(null);
    const lastNameRef = useRef<HTMLInputElement>(null);
    const emailRef = useRef<HTMLInputElement>(null);
    const passwordRef = useRef<HTMLInputElement>(null);

    const handleRegister = async (evt) => {
        evt.preventDefault();
        const employee = {
            firstName: firstNameRef.current?.value, lastName: lastNameRef.current?.value,
            email: emailRef.current?.value, password: passwordRef.current?.value
        };
        if (!employee.firstName || !employee.lastName) { console.log('All fields are required.'); return; }

        try {
            await registerUser(employee);
            navigate('/login');
            if (firstNameRef.current) firstNameRef.current.value = "";
            // lastNameRef/emailRef/passwordRef cleared the same way
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <form onSubmit={handleRegister}>
            <input type="text" ref={firstNameRef} required minLength={4} maxLength={30} pattern="[A-Za-z ]" title="Only letters and spaces are allowed." autoFocus />
            {/* lastName, email, password inputs follow the same ref pattern */}
            <button type="submit">Register</button>
        </form>
    );
};
export default Register;
```

This page is the deliberate counterpoint to `Login.tsx`'s controlled form, matching Module 10 §10.1's comparison table exactly.

- **Lines 9–12, 17–22** — four `useRef<HTMLInputElement>(null)` calls, one per field. Unlike `useState`, assigning to `.current` (which React does automatically via the `ref` prop) does **not** trigger a re-render — the DOM itself is the source of truth, only read via `ref.current?.value` at submit time (the `?.` guards against `.current` still being `null`).
- **Lines 23–26** — a minimal guard-clause validation (Module 04 Pattern 4) checks only `firstName`/`lastName` and returns early if falsy; `email`/`password` presence relies solely on the native HTML `required` attribute rather than this JS check.
- **Lines 32–35** — after a successful registration, the form is manually cleared by directly setting `.value = ""` on each ref's current DOM node — only possible/necessary because the inputs are uncontrolled; a controlled form would instead reset its `useState` object, as `Login.tsx` does in its `finally` block.
- **Lines 49–54** — native HTML validation attributes (`required`, `minLength`, `maxLength`, `pattern`, `title` for the pattern-mismatch tooltip) do real client-side validation **without any React state at all** — the browser blocks submission and shows its own validation UI if unmet, which is part of why this page can skip JS-side validation for those fields.

### `src/pages/Home.tsx`

```tsx
1:  import { useState } from "react";
2:  import "./Home.css";
3:
4:  const Home = () => {
5:
6:      const [isOpen, setIsOpen] = useState(false);
7:
8:      return (
9:          <>
10:             <h1>Home Component</h1>
11:             <p>This is home component.</p>
12:             <h3>Animation in React</h3>
13:
14:             <button onClick={() => setIsOpen(!isOpen)}>
15:                 {isOpen ? 'Close Panel' : 'Open Panel'}
16:             </button>
17:
18:             <div className={isOpen ? 'panel open' : 'panel'}>
19:                 <p>This panel slides open and closed using a CSS transition.</p>
20:                 <p>The animation is purely CSS — React only toggles the class.</p>
21:             </div>
22:         </>
23:     );
24: };
25:
26: export default Home;
```

- **Line 2** — a plain (non-module) global CSS import, per Module 05 §5.2 — `Home.css`'s class names (`.panel`, `.panel.open`) apply app-wide, not scoped to this component.
- **Line 6, 14** — the simplest possible `useState` usage: a boolean toggled by `onClick={() => setIsOpen(!isOpen)}`, which reads `!isOpen` directly rather than via a functional update (`setIsOpen(o => !o)`) — low-risk here, but the same stale-closure shape Module 06 flags as a bug source elsewhere.
- **Line 18** — a dynamic `className` (ternary) drives the CSS transition — Module 15's "zero-JS animation" pattern: `.panel { transition: ...; }` + a `.open` modifier class, React's only job is toggling the class.

### `src/pages/Employee.tsx`

```tsx
1:  import { useEffect, useState } from "react";
2:  import type { EmployeeType } from "../models/employee.model";
3:  import { getEmployeeById } from "../services/employee.service";
4:
5:  import { useSelector, useDispatch } from 'react-redux'
6:  import { getEmpById } from '../redux/empSlice';
7:  import { type RootState } from '../redux/store';
8:
9:  const Employee = () => {
10:
11:     const dispatch = useDispatch();
12:
13:     // get data from store
14:     const dataFromStore = useSelector((state: RootState) => { return state.emp.empData; });
15:
16:     console.log(dataFromStore);
17:
18:     const [employee, setEmployee] = useState<EmployeeType>();
19:     const [employeeId, setEmployeeId] = useState('');
20:
21:     useEffect(() => { console.log('useEffect'); }, []);
22:
23:     const handleInput = (evt) => {
24:         console.log(evt.target);
25:         setEmployeeId(evt.target.value);
26:     };
27:
28:     const getEmp = (evt) => {
29:         evt.preventDefault();
30:         getEmployeeById(employeeId)
31:             .then((response: any) => {
32:                 setEmployee(response.data);
33:                 dispatch(getEmpById(response.data));
34:                 setEmployeeId('');
35:             })
36:             .catch(err => console.error(err));
37:     };
38:     return (
39:         <>
40:             <form onSubmit={getEmp}>
41:                 <input type="text" value={employeeId} onChange={handleInput} placeholder="Please enter employee id" />
42:                 <button type="submit">Find Employee</button>
43:             </form>
44:             {employee && (<>
45:                 <p>Id: {dataFromStore.id}</p>
46:                 {/* First Name / Last Name / Email / Salary follow the same <p>Label: {dataFromStore.field}</p> pattern */}
47:             </>)}
48:         </>
49:     );
50: };
51: export default Employee;
```

- **Lines 5–7, 14** — `react-redux`'s `useSelector`/`useDispatch` and the slice's `getEmpById` action creator plus `RootState` — this component reads from and writes to the global Redux store, unlike `Login`/`Register`/`Home`. `useSelector` subscribes it to `state.emp.empData`, re-rendering whenever that slice changes — the Redux equivalent of `useContext`, but with per-slice subscription granularity.
- **Lines 18–19, 21** — **local** `useState` also exists here, separate from Redux: `employee` (a presence flag, see line 51) and `employeeId` (the controlled input's value). The empty-deps `useEffect` (line 21) just logs once on mount to demonstrate the lifecycle.
- **Lines 28–38** — `getEmp` is **not `async`**; it uses the Promise `.then()/.catch()` chain form (functionally equivalent to `await`/`try-catch`) to call the service, then sets local `employee` state (gating the JSX below), **dispatches** `getEmpById(response.data)` into Redux (which actually updates `dataFromStore`), and clears the input.
- **A latent bug**: `setEmployee(response.data)` sets the **local** `employee` state, and the JSX checks `{employee && (...)}`, but the values displayed inside that block all read from `dataFromStore` (the **Redux** state), not local `employee`. Because `dispatch` updates Redux in the same tick this happens to work, but the gating condition and the rendered data come from two different, redundant state sources. A cleaner version would drop the local `employee` state entirely.
- The form is a controlled input submitting the typed ID via `onSubmit={getEmp}`; the `&&` short-circuit (Module 04 Pattern 2) gates the detail block until an employee has actually been looked up.

### `src/pages/EmployeeList.tsx`

```tsx
1:  import { getAllEmployees } from "../services/employee.service";
2:  import { useSelector, useDispatch } from 'react-redux'
3:  import { getAllEmps } from '../redux/empSlice';
4:  import { type RootState } from '../redux/store';
5:  import type { EmployeeType } from "../models/employee.model";
6:  import { Link } from "react-router";
7:
8:  const EmployeeList = () => {
9:
10:     const dispatch = useDispatch();
11:
12:     const empList: EmployeeType[] = useSelector((state: RootState) => { return state.emp.allEmpData; });
13:
14:     console.log(empList);
15:
16:     const loadEmployees = async (evt) => {
17:         evt.preventDefault();
18:         try {
19:             const response = await getAllEmployees();
20:             dispatch(getAllEmps(response.data.data));
21:         } catch (error) {
22:             console.error(error);
23:         }
24:     };
25:
26:     return (
27:         <>
28:             <button onClick={loadEmployees}>Load Employees List</button>
29:             {empList && empList.length > 0 ? (
30:                 <>
31:                     <h3>List of employees: ({empList.length})</h3>
32:                     {empList.map((emp: EmployeeType, index: number) => (
33:                         <div key={emp._id}>
34:                             <span>{index + 1}</span>
35:                             <Link to={`/employees/${emp._id}`}>{emp.firstName} {emp.lastName}</Link>
36:                         </div>
37:                     ))}
38:                 </>
39:             ) : (
40:                 <p>No employees loaded. Click the button above.</p>
41:             )}
42:         </>
43:     );
44: };
45:
46: export default EmployeeList;
```

Follows the same Redux read/dispatch pattern as `Employee.tsx` above, but without the local-state duplication: `empList` (line 12) is read straight from `allEmpData`, so the list page has no `useState` of its own at all — Redux **is** its state. `loadEmployees` (lines 16–27) fires on a button click rather than automatically via `useEffect` (loads only when the user asks), calling the service and dispatching the response's `.data.data` (the Axios response wraps the server's own `{ data: [...] }` envelope) into Redux via `getAllEmps`.

- **Line 37** — a ternary (Module 04 Pattern 1) between the populated-list branch and an empty-state message, gated on `empList && empList.length > 0`.
- `.map()` uses **`key={emp._id}`** — a real, stable, unique identifier, the "use a real unique ID" best practice from Module 04 §4.2, explicitly not the array-`index` antipattern the same module warns against (the `index` is rendered too, but only for display numbering, never as the key). The `<Link to={`/employees/${emp._id}`}>` is what enables navigating to `EmployeeDetails`.

### `src/pages/EmployeeDetails.tsx`

```tsx
1:  // EmployeeDetails.tsx
2:
3:  import { useEffect } from "react";
4:  import { useParams } from "react-router";
5:  import { getEmployeeById } from "../services/employee.service";
6:  import { getEmpById } from "../redux/empSlice";
7:  import { useDispatch, useSelector } from "react-redux";
8:  import { type RootState } from '../redux/store';
9:
10: const EmployeeDetails = () => {
11:     const emp = useParams();
12:     const dispatch = useDispatch();
13:     const empData = useSelector((s: RootState) => s.emp.empData);
14:
15:     useEffect(() => {
16:         getEmployeeById(emp.id)
17:             .then((response) => dispatch(getEmpById(response.data)))
18:             .catch();
19:     }, []);
20:
21:     return (
22:         <>
23:             {empData && (<>
24:                 <p>Id: {empData.id}</p>
25:                 {/* First name / Last name / Email / Salary follow the same <p>Label: {empData.field}</p> pattern */}
26:             </>)}
27:         </>
28:     );
29: };
30:
31: export default EmployeeDetails;
```

Reads the same Redux `empData` slice `Employee.tsx` writes to, but reads exclusively from it (no local-state duplication). `useParams()` (line 12, no generic type argument here unlike the courseware's `useParams<{ id: string }>()` convention) reads `:id` from the route; `emp.id` fetches on mount via the courseware's "data fetching via `useEffect`" pattern (Module 07/09) and dispatches the result into Redux.

- **A gap**: the effect's empty deps array means it never re-fetches if `emp.id` changes without a full remount — the courseware's Module 09 §9.7 example lists `[id]` for exactly this reason.
- `.catch()` with **no handler function at all** silently swallows any fetch error — no error state, no console log, no user feedback, a gap relative to the loading/error/success three-state pattern Module 08 §8.5 prescribes.
- The conditional render gate is `{empData && (...)}`, but since the slice's `initialState.empData` has empty-string fields (not `null`/`undefined`), `empData` is always **truthy** even before any fetch resolves — a subtler version of the same "no loading state" gap.

### `src/pages/Parent.tsx` and `src/pages/Child.tsx` — Composition & Prop-Callback Demo

```tsx
1:
2:  import { useState } from "react";
3:  import Child from "./Child";
4:
5:  const Parent = () => {
6:
7:      const parentData: string = 'Sonu';
8:      const [dataFromChild, setDataFromChild] = useState('');
9:
10:     const getData = (data: string) => {
11:         console.log(data);
12:         setDataFromChild(data);
13:     };
14:
15:     return (<>
16:         <h1>Parent Component </h1>
17:         <p>Parent data in parent: {parentData}</p>
18:         <p>Child data in parent: {dataFromChild}</p>
19:         <Child def={getData} abc={parentData} />
20:     </>);
21: };
22:
23: export default Parent;
```

```tsx
1:
2:  const Child = (props) => {
3:
4:      const childData: string = 'Monu';
5:      const dataFromParent: string = props.abc;
6:
7:      const sendData = () => {
8:          console.log(childData);
9:          props.def(childData);
10:     };
11:
12:     return (<>
13:         <h1>Child Component </h1>
14:         <p>Parent data in child: {dataFromParent}</p>
15:         <p>Child data in child: {childData}</p>
16:         <button onClick={sendData}>Send data to parent</button>
17:     </>);
18: };
19:
20: export default Child;
```

This pair is the course's minimal illustration of **props flowing down** and **callbacks flowing data back up** (Module 03 §3.1, and Q6/Q13 of the discussion Q&A: "a component cannot change its own props… it emits an event via a callback function passed as a prop, and the parent updates its own state").

- **`Parent.tsx`** — `dataFromChild` (line 8) is state **owned by the parent**, initialized empty, the "single source of truth" the child's data eventually updates via the `getData` callback. `<Child def={getData} abc={parentData} />` (line 19) passes **two** props: `abc` (plain string, parent → child, one-way) and `def` (a function reference — the callback channel child → parent); the generic `abc`/`def` names emphasize *any* prop name works, not just conventional `onXxx` naming.
- **`Child.tsx`** — `props` (line 2) has no type annotation (implicit `any`), losing type safety on `props.abc`/`props.def` — a stricter version would define `interface ChildProps { abc: string; def: (data: string) => void }`. `dataFromParent` reads `props.abc` under a local name; the child cannot reassign `props.abc` itself (props are read-only). The button's `sendData` calls `props.def(childData)` — the entire mechanism by which data crosses back up the tree, since the child has no direct access to the parent's `setDataFromChild`. That triggers `getData` → `setDataFromChild` → `Parent` re-renders with the new value, completing the round trip.

### `src/pages/Page404.tsx`

```tsx
1:
2:  const Page404 = () => {
3:
4:      return (
5:          <>
6:              <h1>Page 404</h1>
7:              <p>Page not found!</p>
8:          </>
9:      );
10: };
11: export default Page404;
```

The simplest possible component — no props, no state, static JSX — wired into `appRoutes.tsx`'s catch-all `path="*"` route, matching any URL that doesn't hit an earlier, more specific pattern.

## 2.8 App Bootstrap — `App.tsx` and `main.tsx`

### `src/App.tsx`

```tsx
1:  // import './App.css';
2:  import AppRoutes from "./routes/appRoutes";
3:  import AuthProvider from './context/AuthProvider';
4:  import store from './redux/store';
5:  import { Provider } from 'react-redux';
6:  // import './styles/styles.css';
7:  const App = () => {
8:
9:      console.log('2. store provided to the app');
10:     return (
11:         <>
12:             <main>
13:                 <Provider store={store}>
14:                     <AuthProvider>
15:                         <AppRoutes />
16:                     </AuthProvider>
17:                 </Provider>
18:             </main>
19:         </>
20:     );
21: };
22:
23: export default App;
```

- **Lines 10–20** — this is the **provider nesting order** the whole app's data flow depends on, from outside in: `<Provider store={store}>` (Redux) → `<AuthProvider>` (auth Context) → `<AppRoutes>` (which itself renders `<BrowserRouter>` → `<NavBar>` + `<Routes>`, per 2.5 above). Redux `<Provider>` is outermost so `useSelector`/`useDispatch` work anywhere below it; `AuthProvider` is next so everything below can call `useContext(AuthContext)`; `AppRoutes` (and its internal `BrowserRouter`) is innermost, so nothing outside it has router hooks — fine, since neither `App` nor `AuthProvider` needs to navigate. This ordering matters concretely because `AppRoutes` reads `isLoggedIn` from context (line 17 of `appRoutes.tsx`) and its pages depend on both Redux and the router, so it must be nested inside both providers.
- **Lines 9, 12** — line 9's `console.log` (paired with similar "1." logs in `store.ts`/`empSlice.tsx`) forms a breadcrumb trail through the module load and initial render sequence, a debugging technique from Module 06 §6.2. Line 12 wraps everything in a semantic `<main>` element — the only DOM structure `App` itself contributes.
- This project's `App.tsx` does **not** include an `ErrorBoundary` wrapping the tree (Module 06 §6.6's recommended global catch-all) — an unhandled render error anywhere would still unmount the whole tree to a blank screen.

### `src/main.tsx`

```tsx
1:  // import { StrictMode } from 'react'
2:  import { createRoot } from 'react-dom/client'
3:  // import './index.css'
4:  // import './styles/styles.css';
5:  import App from './App.tsx'
6:
7:  createRoot(document.getElementById('root')!).render(
8:    // <StrictMode>
9:    <App />
10:   // </StrictMode>,
11: )
```

- **Lines 2, 5, 7** — `createRoot` from `react-dom/client` (the React 18+ concurrent-rendering root API) and `document.getElementById('root')!`'s non-null assertion match Module 00's canonical example exactly; `App` is imported with an explicit `.tsx` extension, allowed by this project's Vite/TS configuration though many configs omit it.
- **Lines 1, 8, 10** — `<StrictMode>` is imported and referenced but **entirely commented out**, so this project runs **without** the dev-time double-render/double-effect behaviour Module 00 §0.8 describes: `console.log`s that would appear twice under StrictMode appear once here, and effect-cleanup bugs StrictMode is designed to surface early would not be caught by this project's current dev setup. Re-enabling it only requires uncommenting the three lines.

---

## Quick-Reference Summary Tables

### Hooks used in the actual project

| Hook | Where | Purpose |
|---|---|---|
| `useState` | `AuthProvider`, `Login`, `Home`, `Employee` | Local reactive state (auth session, form fields, panel toggle, lookup-by-id form) |
| `useEffect` | `Login`, `Employee`, `EmployeeDetails` | Redirect-if-logged-in on mount/`isLoggedIn` change; mount-only log; fetch-on-mount |
| `useRef` | `Register` | Uncontrolled form field access |
| `useContext` | `NavBar`, `Login`, `AppRoutes`, tests | Read auth state/functions from `AuthContext` |
| `useNavigate` | `NavBar`, `Login`, `Register` | Imperative navigation after logout/login/register |
| `useParams` | `EmployeeDetails` | Read `:id` from the URL |
| `useSelector` / `useDispatch` | `Employee`, `EmployeeList`, `EmployeeDetails` | Read/write the Redux `emp` slice |

### Where this project diverges from the courseware's "textbook" pattern

| Area | Courseware pattern | This project |
|---|---|---|
| Auth context typing | `AuthContextValue` interface, `useAuth()` wrapper hook that throws outside a provider | `createContext<any>`, raw `useContext(AuthContext)` everywhere |
| Route protection | `<ProtectedRoute>` + `<Outlet/>`, remembers `location.state.from` | Inline `isLoggedIn ? <Page/> : <Navigate/>` ternary per route, no "return to intended page" |
| Redux store | `employees` + `auth` slices, `createAsyncThunk`, `useAppDispatch`/`useAppSelector` wrappers | Only an `emp` slice; async calls happen in components, plain sync actions dispatch results; raw `useDispatch`/`useSelector` |
| Testing runner | Vitest | Jest (`babel-jest`, `jest-environment-jsdom`) |
| `StrictMode` | Always on in dev | Commented out in `main.tsx` |
| `BrowserRouter` location | Wraps `<App/>` in `main.tsx` | Instantiated inside `AppRoutes` itself |
| Error Boundaries | Wrapping the whole app in `main.tsx`/`App.tsx` | Not present in this project |
