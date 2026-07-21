# React — Complete Line-by-Line Guide

This guide is grounded in two sources from Aakash's course: the 16-module React courseware (`Courseware/06-react/00-getting-started.md` through `15-bonus-and-roundup.md`, plus `react_discussion_qa.md`), and a real React + TypeScript "EMS" (Employee Management System) frontend at `Code/React/src`, which has routing, a Context-based auth layer, Redux Toolkit state, and an Axios service layer.

Part 1 walks the courseware topic by topic, numbered exactly as the modules are numbered (00–15), so every concept the assessment can draw on is covered. Part 2 walks the actual project file by file, in dependency order — models → services → redux → context → routing → components → pages → app bootstrap — reproducing each file with line numbers and explaining every non-trivial line. Where the project code diverges from the "textbook" pattern shown in the courseware (and it does, in a few places — commented-out `BrowserRouter`/`StrictMode`, `any`-typed context, a couple of latent bugs), that is called out explicitly rather than smoothed over, because recognizing those differences is exactly the kind of thing an assessment tests.

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
1:  import axios from "axios";
2:
3:  const apiUrl = 'http://localhost:3000';
4:
5:  const api = axios.create({ baseURL: apiUrl });
6:
7:  api.interceptors.request.use((config) => {
8:      console.log(config);
9:      const token = localStorage.getItem('token');
10:     console.log(token);
11:     if (token) {
12:         config.headers.Authorization = `Bearer ${token}`;
13:     }
14:     return config;
15: });
16:
17: // export const loginUser = async (user: any) => api.post('/api/auth/login', user);
18: // export const register = async (user: any) => api.post('/api/auth/register', user);
19: // export const logout = async () => api.post('/api/auth/logout');
20:
21: export default api;
```

- **Lines 3–5** — a single centralised Axios instance with a fixed `baseURL`, exactly the pattern from Module 08: every service imports this `api` object rather than calling `axios` directly.
- **Lines 7–15** — a **request interceptor**, run before every outgoing request. It reads the JWT from `localStorage` (the storage mechanism the courseware explicitly flags as XSS-vulnerable but acceptable for training) and, if present, attaches it as an `Authorization: Bearer <token>` header. This is how every authenticated API call in the app gets its token — no individual service function has to remember to add it.
- **Line 9** — mutating `config.headers.Authorization` directly and then returning `config` on line 14 is the required interceptor contract: request interceptors must return the (possibly modified) config object.
- **Lines 8, 10** — leftover `console.log` debug statements; harmless but would be stripped before production per the Module 14 deployment checklist.
- Note there is **no response interceptor** here (unlike the courseware's Module 08 example, which adds one to globally catch 401s and force a redirect to `/login`) — in this project, expired/invalid tokens are instead handled implicitly wherever a protected route re-checks `isLoggedIn`.

### `src/services/employee.service.ts`

```typescript
1:
2:  import api from "./api.service";
3:
4:  export const getEmployeeById = async (id) => {
5:      console.log(id);
6:      return await api.get(`/api/employees/${id}`);
7:  };
8:
9:  export const getAllEmployees = async () => {
10:     console.log(`${api}/api/employees`);
11:     console.log('getAllEmployees');
12:     return await api.get('/api/employees');
13: };
```

- **Line 4** — `id` has no type annotation (implicit `any`), a looseness the project accepts elsewhere too (see `Employee.tsx`'s `handleInput`/`getEmp` params). A stricter version would type it `id: string`.
- **Lines 4–7, 9–13** — both functions are thin, typed-by-convention wrappers around `api.get`, returning the full Axios response object (so callers read `.data` themselves) — the "typed service layer" pattern from Module 08, minus explicit TS generics on the `.get<T>()` calls.

### `src/services/user.service.ts`

```typescript
1:
2:  import api from "./api.service";
3:
4:  export const loginUser = async (user) => {
5:      console.log(user);
6:      return await api.post('/api/auth/login', user);
7:  };
8:
9:  export const registerUser = async (user) => {
10:     console.log(user);
11:     return await api.post('/api/auth/register', user);
12: };
13:
14: export const logoutUser = async () => {
15:     return await api.post('/api/auth/logout');
16: };
```

Three thin POST wrappers around the shared `api` instance — `loginUser`/`registerUser` forward the form payload; `logoutUser` posts with no body. `AuthProvider.tsx` calls `loginUser` directly; `Login.tsx` calls the context's `login()` rather than this service directly, keeping the token-persistence logic in one place (the provider).

### `src/services/user.service.test.ts`

```typescript
17: import '@testing-library/jest-dom';
18: import api from './api.service';
19: import { loginUser, registerUser, logoutUser } from './user.service';
20:
21: jest.mock('./api.service');
22:
23: const mockedApi = api as jest.Mocked<typeof api>;
24:
25: describe('user.service', () => {
26:     beforeEach(() => {
27:         jest.clearAllMocks();
28:     });
29:
30:     test('loginUser posts credentials to /api/auth/login and returns the response', async () => {
31:         const credentials = { email: 'trainee@example.com', password: 'secret123' };
32:         mockedApi.post.mockResolvedValue({ data: { token: 'jwt-abc', employee: { id: 1 } } });
33:
34:         const result = await loginUser(credentials);
35:
36:         expect(mockedApi.post).toHaveBeenCalledTimes(1);
37:         expect(mockedApi.post).toHaveBeenCalledWith('/api/auth/login', credentials);
38:         expect(result.data.token).toBe('jwt-abc');
39:     });
40: // ...similar tests for failure, registerUser, logoutUser
```

- **Line 21** — `jest.mock('./api.service')` auto-mocks the entire module: every exported function (here, the default-exported `api` Axios instance's methods) becomes a `jest.fn()` returning `undefined` unless configured.
- **Line 23** — casting the mocked module `as jest.Mocked<typeof api>` gives TypeScript-aware autocomplete/type-checking on `.mockResolvedValue(...)` etc.
- **Line 27** — `jest.clearAllMocks()` in `beforeEach` resets call history (but not implementations) between tests so assertions like `toHaveBeenCalledTimes(1)` on line 36 aren't polluted by a previous test's calls.
- **Lines 32, 34, 36–38** — the classic mock-arrange-act-assert shape: stub what the dependency returns, call the real function under test, assert both *that* the dependency was called correctly (line 37 — proves `loginUser` forwards to the right endpoint with the right payload) and *that* the function's return value is correct (line 38 — proves it doesn't transform the response unexpectedly). This is testing the service layer in isolation from the network, exactly the "test behaviour, not implementation, but do it fast/offline" idea from Module 13.

## 2.3 Redux — `src/redux/store.ts` and `src/redux/empSlice.tsx`

### `src/redux/store.ts`

```typescript
1:  import { configureStore } from "@reduxjs/toolkit";
2:  import empReducer from './empSlice';
3:
4:  // createStore();
5:
6:  console.log('1. store configured');
7:
8:  // store = configureStore({
9:  //     reducer : {}
10: // });
11:
12: const store = configureStore({
13:     reducer: {
14:         emp: empReducer
15:         // , dept: deptReducer, jobs: jobReducer etc
16:     }
17: });
18:
19: export default store;
20:
21: export type RootState = ReturnType<typeof store.getState>;
```

- **Lines 12–17** — `configureStore` (Redux Toolkit's modern replacement for the legacy `createStore` + manual middleware wiring) is given a `reducer` map with one key, `emp`, pointing at the employee slice's reducer. Every top-level key here becomes a branch of the global state tree (`state.emp.*`); the comment on line 15 shows the intended extension point for more slices (departments, jobs) that this project doesn't build out. Notably there is **no separate `auth` slice** — unlike the courseware's Module 11 example which puts both `employees` and `auth` reducers in the store, this project keeps auth entirely in React Context (see 2.4 below) and Redux only for employee data.
- **Line 21** — `RootState` is inferred directly from the store's own `getState()` return type via `ReturnType<typeof ...>`, so it can never drift out of sync with the actual reducer shape — this is the type imported everywhere a `useSelector` needs to know the state shape (`state: RootState`).
- This project does **not** define the typed `useAppDispatch`/`useAppSelector` wrapper hooks the courseware recommends (Module 11, §11.3) — components here call the raw `useDispatch`/`useSelector` from `react-redux` directly and annotate the selector's `state` parameter inline (`(state: RootState) => state.emp.empData`), which works but loses the convenience of not having to import `RootState` at every call site.

### `src/redux/empSlice.tsx`

```tsx
1:  import { createSlice } from "@reduxjs/toolkit";
2:
3:  // slice = createSlice({name, intitalState, reducers});
4:
5:  console.log('empSlice');
6:
7:  const EmpSlice = createSlice(
8:      {
9:          name: 'emp',
10:         initialState: {
11:             empData: {
12:                 id: '',
13:                 firstName: '',
14:                 lastName: '',
15:                 email: '',
16:                 salary: ''
17:
18:             },
19:             allEmpData: []
20:         },
21:         reducers: {
22:             getEmpById: (state, action) => {
23:                 console.log(state);
24:                 state.empData = action.payload;
25:             },
26:             getAllEmps: (state, action) => {
27:                 console.log(state);
28:                 state.allEmpData = action.payload;
29:             }
30:         }
31:     }
32: );
33:
34: export const { getEmpById, getAllEmps } = EmpSlice.actions;
35:
36: export default EmpSlice.reducer;
```

- **Lines 9–20** — `createSlice` takes a `name` (used as the prefix for auto-generated action types, e.g. `'emp/getEmpById'`), an `initialState` (a single-employee shape `empData`, plus `allEmpData: []` for the list page), and a `reducers` map.
- **Lines 22–25, 26–29** — each reducer receives the slice's current `state` and the dispatched `action` (`{ type, payload }`), and appears to **mutate `state` directly** (`state.empData = action.payload`). As covered in Module 11, `createSlice` reducers run inside **Immer**, which intercepts these "mutations" on a draft and produces a correctly immutable new state object under the hood — this is *safe* here specifically because it's inside `createSlice`; the same pattern outside Redux Toolkit (plain `useState`) would be the "Bug 1 — state mutation" antipattern from Module 06.
- **Line 34** — `EmpSlice.actions` is auto-generated by `createSlice`: calling `getEmpById(someEmployee)` produces the action object `{ type: 'emp/getEmpById', payload: someEmployee }` ready to `dispatch(...)`.
- **Line 36** — the slice's `.reducer` (its combining reducer function across all the `reducers` keys) is the default export wired into `store.ts` as `emp: empReducer`.
- No `extraReducers`/`createAsyncThunk` are used here — unlike the courseware's fuller example (Module 11, §11.4), the async API call happens in the **component** (`Employee.tsx`, `EmployeeList.tsx`) via the service layer, and only the *result* is dispatched into Redux as a plain synchronous action. This is a simpler, more manual variant of the same one-way data flow, appropriate for a small teaching app.

## 2.4 Context — Auth

### `src/context/AuthContextType.tsx`

```tsx
1:  import { createContext } from "react";
2:
3:  const AuthContext = createContext<any>({
4:      isLoggedIn: false,
5:      employee: null,
6:      login: (_employee: any, _token: string) => { },
7:      logout: () => { }
8:  });
9:
10: export default AuthContext;
```

- **Line 3** — `createContext<any>(...)` creates the context object with a **default value** used only if a component reads it via `useContext` without any `<AuthContext.Provider>` above it in the tree. Typing it `<any>` (rather than a proper `AuthContextType` interface, as the courseware's Module 07/12 examples do) trades away compile-time safety on every `useContext(AuthContext)` call site — `login`, `logout`, `isLoggedIn`, `employee` are all effectively untyped wherever they're consumed (visible in `NavBar`, `Login`, `AppRoutes` below, none of which get autocomplete or type errors on context misuse). This is a deliberate simplification for the course but is exactly the kind of thing worth tightening in production code (an interface like the one shown in Module 12 — `AuthContextValue` with `user`, `isLoading`, `login`, `logout`, `isAuthenticated`, `isAdmin` — would be the fix).
- **Lines 4–7** — the default value's shape mirrors what `AuthProvider` will actually supply, so any accidental usage outside a provider degrades gracefully (functions are no-ops, `isLoggedIn` is `false`) instead of throwing — a softer failure mode than the courseware's pattern of throwing inside a `useAuth()` wrapper hook if the context is `null`. This project has no such wrapper hook; every consumer calls `useContext(AuthContext)` directly.

### `src/context/AuthProvider.tsx`

```tsx
1:  // AuthProvider.tsx
2:  import { useState } from "react";
3:  import AuthContext from "./AuthContextType";
4:  import { loginUser } from "../services/user.service";
5:
6:  const AuthProvider = ({ children }: any) => {
7:      const storedEmployee = localStorage.getItem('employee');
8:      const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));
9:      const [employee, setEmployee] = useState(storedEmployee ? JSON.parse(storedEmployee) : null);
10:
11:     const login = async (credentials: { email: string; password: string }) => {
12:         const response: any = await loginUser(credentials);
13:         if (!response.data?.token) {
14:             throw new Error('Invalid credentials');
15:         }
16:         localStorage.setItem('token', response.data.token);
17:         localStorage.setItem('employee', JSON.stringify(response.data.employee));
18:         setEmployee(response.data.employee);
19:         setIsLoggedIn(true);
20:         return response.data.employee;
21:     };
22:
23:     const logout = () => {
24:         localStorage.removeItem('token');
25:         localStorage.removeItem('employee');
26:         setEmployee(null);
27:         setIsLoggedIn(false);
28:     };
29:
30:     return (
31:         <AuthContext.Provider value={{ isLoggedIn, employee, login, logout }}>
32:             {children}
33:         </AuthContext.Provider>
34:     );
35: };
36:
37: export default AuthProvider;
```

- **Line 6** — `{ children }: any` — the `children` prop (whatever JSX is nested inside `<AuthProvider>...</AuthProvider>`, here `<AppRoutes />`) is typed `any` rather than `ReactNode` (the type Module 03/07 recommends for children props); functionally identical, but again loses a layer of type safety.
- **Line 7** — reads a possibly-`null` string synchronously from `localStorage` **during render**, before any state exists — this is the value used to *seed* `useState` on line 9, not read every render (React only calls the `useState` initializer once, on mount).
- **Line 8** — `!!localStorage.getItem('token')` coerces "a token string exists" into a boolean; this is how the app "remembers" a logged-in session across page reloads/tab closes without re-hitting the login endpoint — a simpler version of the courseware's Module 12 pattern (which also has a commented-out call to `authService.getProfile()` to *verify* the token server-side on mount; this project trusts the stored token's mere presence, with no expiry check or server verification, and has no `isLoading` state gating renders while that check would happen).
- **Line 9** — `JSON.parse(storedEmployee)` reconstructs the employee object from its JSON string; the ternary falls back to `null` if nothing was stored.
- **Lines 11–21** — `login` is `async`, calling the `loginUser` service (line 12), then explicitly checking `response.data?.token` (optional chaining guards against `response.data` itself being missing) and **throwing** if it's absent (line 14) rather than silently failing — this is what lets `Login.tsx`'s `try/catch` around `await login(user)` show an error message. On success it writes both the token and the (JSON-stringified) employee object to `localStorage`, updates both pieces of React state (lines 18–19, which is what actually triggers `NavBar`/`AppRoutes`/`Login` to re-render and reflect the new logged-in status), and returns the employee object to the caller (useful if a caller wants the freshly logged-in user without waiting for a re-render, though none of the current call sites use that return value).
- **Lines 23–28** — `logout` is fully synchronous: clears both storage keys and resets both state variables. Note this project's `logout` does **not** call `logoutUser()` (the server-side logout endpoint) — a difference from the courseware's Module 12 pattern (which fires the API call with `.catch(() => {})`, "fire and forget," before clearing local state) — here, logout is purely a client-side session clear.
- **Lines 30–34** — the `Provider` supplies `{ isLoggedIn, employee, login, logout }` down the tree; every descendant that calls `useContext(AuthContext)` gets this exact object, and (because it's a fresh object literal every render) any component consuming the whole context value re-renders whenever `AuthProvider` re-renders — the standard Context trade-off flagged in Module 11 (§11.8: "Context rerenders whole subtree").

### `src/context/AuthProvider.test.tsx`

```tsx
1:  import { useContext } from 'react';
2:  import { render, screen, fireEvent, waitFor } from '@testing-library/react';
3:  import '@testing-library/jest-dom';
4:  import AuthContext from '../context/AuthContextType';
5:  import AuthProvider from './AuthProvider';
6:  import { loginUser } from '../services/user.service';
7:
8:  const TestConsumer = () => {
9:      const { isLoggedIn, employee, login, logout } = useContext(AuthContext);
10:
11:     const handleLoginClick = async () => {
12:         try {
13:             await login({ email: 'trainee@example.com', password: 'secret123' });
14:         } catch {
15:             // swallow here -- the error itself is asserted separately below
16:         }
17:     };
18:
19:     return (
20:         <div>
21:             <p>isLoggedIn: {String(isLoggedIn)}</p>
22:             <p>employee: {employee ? employee.name : 'none'}</p>
23:             <button onClick={handleLoginClick}>trigger-login</button>
24:             <button onClick={logout}>trigger-logout</button>
25:         </div>
26:     );
27: };
28:
29: const renderWithProvider = () =>
30:     render(
31:         <AuthProvider>
32:             <TestConsumer />
33:         </AuthProvider>
34:     );
35:
36: jest.mock('../services/user.service', () => ({
37:     loginUser: jest.fn(),
38: }));
39:
40: const mockedLoginUser = loginUser as jest.Mock;
41:
42: describe('AuthProvider', () => {
43:     beforeEach(() => {
44:         localStorage.clear();
45:         mockedLoginUser.mockReset();
46:     });
47:
48:     test('hydrates isLoggedIn/employee from localStorage on mount', () => {
49:         localStorage.setItem('token', 'existing-jwt');
50:         localStorage.setItem('employee', JSON.stringify({ name: 'Stored Employee' }));
51:
52:         renderWithProvider();
53:
54:         expect(screen.getByText('isLoggedIn: true')).toBeInTheDocument();
55:         expect(screen.getByText('employee: Stored Employee')).toBeInTheDocument();
56:     });
57:  // ...
```

- **Lines 8–27** — since `AuthProvider` has no meaningful UI of its own, the test defines a minimal `TestConsumer` component whose only job is to expose the context's internals (`isLoggedIn`, `employee`) as visible text, and wire the `login`/`logout` functions to buttons — this is the standard way to test a Context Provider's *behaviour* through RTL, since you can't directly call hooks/functions outside of a rendered component.
- **Line 9** — destructures straight out of `useContext(AuthContext)`, proving the actual shape supplied by `AuthContext.Provider value={...}` in the real provider.
- **Lines 36–38** — `jest.mock('../services/user.service', () => ({ loginUser: jest.fn() }))` replaces the entire module with a factory returning just a mock `loginUser` — since `AuthProvider` imports `loginUser` directly (not the whole service default-exported), only that named export needs mocking.
- **Line 40** — casts the mocked import so TypeScript knows it has Jest mock methods (`.mockResolvedValue`, `.mockReset`, etc.).
- **Lines 43–46** — `beforeEach` clears `localStorage` (so tests don't leak state into each other via the real browser-like storage jsdom provides) and resets the mock's call history/implementation.
- **Lines 48–56** — this test seeds `localStorage` **before** `render()` is called, proving the provider's `useState` initializers (lines 8–9 of `AuthProvider.tsx`) correctly hydrate from storage on mount — exactly the "remember me across reload" behaviour described above.
- Later tests (not fully reproduced here) exercise: default logged-out state with empty storage, a successful `login()` persisting token/employee and updating both storage and rendered text (using `waitFor` since `login` is async), a `login()` failure leaving state/storage untouched, that the thrown `Error('Invalid credentials')` actually propagates to the caller (captured via a second consumer component and asserted with `toBeInstanceOf(Error)`), and `logout()` clearing everything back to the logged-out state. Together they cover both branches of every conditional in `AuthProvider.tsx`.

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
29:                 <Route
30:                     path="/employees" element={isLoggedIn ? <Employee /> : <Navigate to="/login" replace />}
31:                 />
32:                 <Route
33:                     path="/employees/:id" element={isLoggedIn ? <EmployeeDetails /> : <Navigate to="/login" replace />}
34:                 />
35:                 <Route
36:                     path="/employeeslist" element={isLoggedIn ? <EmployeeList /> : <Navigate to="/login" replace />}
37:                 />
38:                 <Route
39:                     path="/parent" element={isLoggedIn ? <Parent /> : <Navigate to="/login" replace />}
40:                 />
41:                 <Route path="*" element={<Page404 />} />
42:                 <Route path="/register" element={<Register />} />
43:             </Routes>
44:         </BrowserRouter>
45:     );
46: };
47:
48: export default AppRoutes;
```

- **Line 2** — routing primitives are imported from the `react-router` package directly (this project's dependency, v8.1.0), not `react-router-dom` as most of the courseware's snippets show; the API surface used here (`BrowserRouter`, `Routes`, `Route`, `Navigate`) is identical either way.
- **Line 17** — reads `isLoggedIn` straight from context at the top of the component; because `AppRoutes` itself consumes the context, it re-renders whenever `AuthProvider`'s value changes (login/logout), which is exactly what's needed for the route guards below to react live.
- **Line 20** — `<BrowserRouter>` is instantiated **inside `AppRoutes`**, not in `main.tsx` as Module 09's canonical example does. This means the router context is only established once `AppRoutes` itself renders — functionally equivalent here since `AppRoutes` is rendered once, high in the tree (inside `AuthProvider`), but it does mean nothing above `AppRoutes` (i.e., `AuthProvider`, `Provider`) can use router hooks like `useNavigate`.
- **Line 21** — `<NavBar />` is rendered as a sibling of `<Routes>`, both inside `<BrowserRouter>`, so it appears on every page (it's not itself gated by a route) and can safely use `useNavigate`/`Link` since it's inside the router context.
- **Line 23** — the root path `/` immediately redirects to `/home` via `<Navigate replace />` — `replace` swaps the current history entry rather than pushing a new one, so the back button doesn't get stuck bouncing between `/` and `/home`.
- **Lines 25–28, 29–40** — this project implements route protection with **inline ternaries per route** (`isLoggedIn ? <Page /> : <Navigate to="/login" replace />`) rather than the courseware's `<ProtectedRoute>` wrapper-component-with-`<Outlet/>` pattern (Module 09 §9.9, Module 12 §12.5). Functionally similar (unauthenticated users get redirected), but more repetitive — every protected route repeats the same ternary instead of nesting them under one guard component, and there's no "remember where I was going" `state={{ from: location }}` behaviour here, so after logging in the user always lands wherever `Login.tsx` hardcodes (`/employeeslist`), not back at the page they originally tried to reach.
- **Line 27** — the `/login` route is the mirror image of the others: if **already** logged in, redirect *away* from the login page.
- **Line 33** — `path="/employees/:id"` declares a URL parameter `id`, read inside `EmployeeDetails` via `useParams()`.
- **Line 41** — the catch-all `path="*"` renders `Page404`; **critically it must be the last-matched pattern to act as a true fallback** — but note **line 42's `/register` route is declared after it**. React Router's `<Routes>` matches routes by best-match ranking, not strictly by declaration order, so `/register` still resolves correctly to `Register` rather than falling through to the wildcard — but this ordering is fragile style (the courseware explicitly notes "404 — must be last") and would be worth reordering for clarity/safety even though it happens to work.

## 2.6 Components

### `src/components/navBar.tsx`

```tsx
1:  import { useContext } from "react";
2:  import { Link, useNavigate } from "react-router";
3:  import AuthContext from "../context/AuthContextType";
4:  // import toggleTheme from "./toggleTheme";
5:
6:  const NavBar = () => {
7:      const { isLoggedIn, logout } = useContext(AuthContext);
8:      const navigate = useNavigate();
9:
10:     const handleLogout = () => {
11:         logout();
12:         navigate('/login');
13:     };
14:
15:     return (
16:         <>
17:             <p>{isLoggedIn ? 'logged in' : 'logged out'}</p>
18:             <nav>
19:                 <Link to='/home'>Home</Link>
20:                 {isLoggedIn && <Link to='/parent'>Parent</Link>}
21:                 {isLoggedIn && <Link to='/employees'>Employees</Link>}
22:                 {isLoggedIn && <Link to='/employeeslist'>EmployeesList</Link>}
23:                 {!isLoggedIn && <Link to='/login'>Login</Link>}
24:                 {!isLoggedIn && <Link to='/register'>Register</Link>}
25:                 {isLoggedIn && <button onClick={handleLogout}>Logout</button>}
26:                 {/* <button onClick={toggleTheme}>Color Mode</button> */}
27:             </nav>
28:         </>
29:     );
30: };
31:
32: export default NavBar;
```

- **Line 7** — pulls `isLoggedIn` and `logout` off the context; because this component doesn't call the `login` function, TypeScript (were the context properly typed) wouldn't need to know about it here — but since the context is `any`, nothing is enforced either way.
- **Line 8** — `useNavigate()` returns the imperative navigation function; only usable because `NavBar` renders inside `<BrowserRouter>` (established in `AppRoutes`, see 2.5).
- **Lines 10–13** — `handleLogout` first clears the auth state via the context's `logout()` (synchronous — see `AuthProvider.tsx`), then imperatively navigates to `/login`. Note this fires **after** `logout()` runs, so by the time `/login` renders, `isLoggedIn` is already `false` and `AppRoutes`' `/login` ternary correctly shows `<Login/>` rather than redirecting away.
- **Line 17** — a simple ternary rendering a plain status string — always visible regardless of auth state, unlike the links below it.
- **Lines 20–24** — five `&&` short-circuit expressions gating each nav link on `isLoggedIn` (or its negation) — the exact Pattern 2 from Module 04, applied five times to build a role-appropriate nav bar without ever rendering `false`/`null` visibly (since `false` renders as nothing).
- **Line 25** — the Logout button itself is also gated on `isLoggedIn`, so it can never appear when there's nothing to log out of.
- **Line 26** — a commented-out call to the (unused-in-production) `toggleTheme` utility described next.

### `src/components/toggleTheme.tsx`

```tsx
1:  const toggleTheme = () => {
2:
3:      const html = document.documentElement;
4:
5:      if (html.dataset.theme === "light") {
6:          html.dataset.theme = "dark";
7:      } else {
8:          html.dataset.theme = "light";
9:      }
10: };
11: export default toggleTheme;
```

- **Line 3** — `document.documentElement` is the `<html>` root element — this function reaches directly into the DOM, which is fine for something that isn't React-managed state (there's nothing to re-render; it's a pure side effect / imperative styling toggle, the same category of DOM-manipulation the courseware's Module 05 §5.7 dark-mode section describes: `document.documentElement.setAttribute('data-theme', theme)`).
- **Lines 5–9** — `html.dataset.theme` reads/writes the `data-theme` HTML attribute (the `dataset` API auto-converts `data-theme` ↔ `.theme`); toggling this attribute is what a CSS rule like `[data-theme='dark'] { --color-bg: ...; }` (Module 05) would key off to swap the whole app's colour tokens with zero React re-render needed.
- This is **not a hook** — it's a plain function, not `useToggleTheme`, so it can't hold React state itself; it's currently wired up only as a commented-out `onClick` in `NavBar` (line 26 above), meaning the feature exists in the codebase but isn't actually active in the running app.

## 2.7 Pages, in Logical Flow

### `src/pages/Login.tsx`

```tsx
1:  import { useContext, useEffect, useState } from "react";
2:  import { useNavigate } from "react-router"; // this
3:  import AuthContext from "../context/AuthContextType";
4:
5:  const Login = () => {
6:
7:      const { login, isLoggedIn } = useContext(AuthContext);
8:      const [user, setUser] = useState({ email: '', password: '' });
9:      const [message, setMessage] = useState('');
10:     const navigate = useNavigate();
11:
12:     useEffect(() => {
13:         console.log(isLoggedIn);
14:         if (isLoggedIn) {
15:             navigate('/employeeslist', { replace: true });
16:         }
17:     }, [isLoggedIn, navigate]);
18:
19:     const handleInput = (evt: any) => {
20:         const { name, value } = evt.target;
21:         setUser((prevUser) => ({
22:             ...prevUser,
23:             [name]: value,
24:         }));
25:     };
26:
27:     const submitInput = async (evt: any) => {
28:         evt.preventDefault();
29:         try {
30:             await login(user);
31:             setMessage('Login successful, going to Employee list...');
32:             setMessage('');
33:             navigate('/employeeslist');
34:         } catch (error) {
35:             setMessage('Invalid credentials.');
36:             console.error(error);
37:         } finally {
38:             setUser({ email: '', password: '' });
39:         }
40:     };
41:
42:     return (
43:         <>
44:             <h1>Login Component (Controlled Form) </h1>
45:             <p>This is login component.</p>
46:
47:             <form onSubmit={submitInput}>
48:                 <input
49:                     type="email"
50:                     name="email"
51:                     value={user.email}
52:                     onChange={handleInput}
53:                     autoFocus
54:                     placeholder="Enter your email"
55:                 />
56:                 <br />
57:                 <input
58:                     type="password"
59:                     name="password"
60:                     value={user.password}
61:                     onChange={handleInput}
62:                     placeholder="Enter your password"
63:                 />
64:                 <br />
65:                 <button type="submit">🔓Login</button>
66:             </form>
67:             <p>{message}</p>
68:         </>
69:     );
70: };
71:
72: export default Login;
```

- **Line 7** — destructures `login` and `isLoggedIn` off context; `login` here is the async function defined in `AuthProvider`.
- **Line 8** — a single `user` state object holds **both** form fields (`email`, `password`) rather than two separate `useState` calls — the "state with objects" pattern from Module 03 §3.4.
- **Lines 12–17** — this `useEffect` re-runs whenever `isLoggedIn` or `navigate` changes (line 17's dependency array); on mount, and again any time `isLoggedIn` flips to `true` (e.g., because `login()` succeeded elsewhere or because a stored token hydrated it on refresh), it redirects away from the login page with `replace: true` (so `/login` doesn't linger in browser history for an already-authenticated user). Including `navigate` in the deps array is correct-but-usually-redundant — `useNavigate()`'s return value is stable across renders in React Router v6+, so this effect effectively only re-fires on `isLoggedIn` changes.
- **Lines 19–25** — `handleInput` is a **single shared handler** for both inputs, using the DOM `name` attribute (line 20) to know which field to update, and computed-property-name spread (`[name]: value` on line 23) to update just that one key of the `user` object immutably — the standard multi-field-controlled-form pattern.
- **Line 27** — `submitInput` is `async` because it `await`s the context's `login`.
- **Line 28** — `evt.preventDefault()` stops the browser's native form submission (which would cause a full page reload) — mandatory on every controlled form submit handler, as the courseware repeatedly stresses.
- **Lines 29–39** — `try/catch/finally`: on success, sets a (briefly, and then immediately overwritten — see below) success message and navigates to `/employeeslist`; on failure, sets an error message and logs the error; `finally` **always** clears the form fields back to empty, regardless of success or failure — visible in the test `'form fields are cleared after submit, whether login succeeds or fails'`.
- **Lines 31–32** — a real logic quirk worth flagging: `setMessage('Login successful, going to Employee list...')` is immediately followed by `setMessage('')` on the very next line, in the same synchronous block — because React **batches** these two state updates (per Module 15's batching note and Module 06's related discussion), only the *last* value (`''`) is what actually ever reaches the screen; the success message is set but effectively never rendered. This is exactly the kind of subtle bug the debugging module trains you to spot by reading state-update call order carefully.
- **Line 33** — `navigate('/employeeslist')` (no `replace` here, unlike the `useEffect`'s redirect on line 15) pushes a new history entry, so the back button *would* return to `/login` after a fresh manual login — an intentional (or at least consistent) difference from the "already logged in" auto-redirect case.
- **Lines 48–55, 57–63** — two fully controlled inputs: `value={user.email}`/`value={user.password}` are driven by state, `onChange={handleInput}` keeps state in sync with every keystroke — the "React owns the value" model from Module 10 §10.2, contrasted directly with `Register.tsx`'s uncontrolled approach below.
- **Line 67** — `{message}` renders the current error/status text; since `message` starts as `''` and `''` is falsy-but-renders-as-nothing in this position (an empty string produces no visible text node), no stray text shows before a submit attempt.

There is a **known, intentionally-left-in gap** referenced in `Login.test.tsx`'s skipped test: an earlier commented-out version of this file had a `validateInput()` function whose boolean return value was computed but never actually checked before calling `login(user)` — meaning the current active code has *no* client-side pre-submit validation at all (it relies entirely on the server rejecting bad credentials). The skipped test documents this as a known pending fix.

### `src/pages/Login.test.tsx`

```tsx
1:  import { render, screen, fireEvent, waitFor } from '@testing-library/react';
2:  import '@testing-library/jest-dom';
3:  import AuthContext from '../context/AuthContextType';
4:  import Login from './Login';
5:
6:  const mockNavigate = jest.fn();
7:  jest.mock('react-router', () => ({
8:      useNavigate: () => mockNavigate,
9:  }));
10:
11: const renderLogin = (contextValue: any) => {
12:     return render(
13:         <AuthContext.Provider value={contextValue}>
14:             <Login />
15:         </AuthContext.Provider>
16:     );
17: };
```

- **Lines 6–9** — mocks the entire `react-router` module so `useNavigate()` inside `Login` returns a Jest spy (`mockNavigate`) instead of a real navigation function requiring an actual `<BrowserRouter>` ancestor — this decouples the test from needing a full router setup just to render one page component.
- **Lines 11–17** — a small test helper that renders `<Login>` wrapped directly in `<AuthContext.Provider value={contextValue}>` (bypassing the real `AuthProvider` entirely) — this lets each test supply a **custom** `login`/`isLoggedIn` combination (a stubbed `login: jest.fn().mockResolvedValue(...)` or `.mockRejectedValue(...)`) to drive `Login`'s different branches without touching `localStorage` or real network calls — a clean example of testing a consumer component in isolation from its actual provider.
- Later tests (seen in full earlier) verify: both inputs render, typing updates the controlled value via `fireEvent.change`, submit calls `login()` with the exact current field values, a successful login navigates to `/employeeslist`, a failed login shows `'Invalid credentials.'` and does **not** navigate, fields clear after submit either way, and — testing the `useEffect` redirect directly — rendering with `isLoggedIn: true` from the start immediately triggers `mockNavigate` with `('/employeeslist', { replace: true })`, proving the effect on `Login.tsx` lines 12–17 runs correctly on mount.

### `src/pages/Register.tsx`

```tsx
1:  import { useRef } from "react";
2:  import { registerUser } from "../services/user.service";
3:  import { useNavigate } from "react-router"; // this
4:
5:  const Register = () => {
6:
7:      const navigate = useNavigate();
8:
9:      const firstNameRef = useRef<HTMLInputElement>(null);
10:     const lastNameRef = useRef<HTMLInputElement>(null);
11:     const emailRef = useRef<HTMLInputElement>(null);
12:     const passwordRef = useRef<HTMLInputElement>(null);
13:
14:     const handleRegister = async (evt) => {
15:         evt.preventDefault();
16:
17:         const employee = {
18:             firstName: firstNameRef.current?.value,
19:             lastName: lastNameRef.current?.value,
20:             email: emailRef.current?.value,
21:             password: passwordRef.current?.value
22:         };
23:         if (!employee.firstName || !employee.lastName ) {
24:             console.log('All fields are required.');
25:             return;
26:         }
27:
28:         try {
29:             const response = await registerUser(employee);
30:             console.log(response.data);
31:             navigate('/login');
32:             if (firstNameRef.current) firstNameRef.current.value = "";
33:             if (lastNameRef.current) lastNameRef.current.value = "";
34:             if (emailRef.current) emailRef.current.value = "";
35:             if (passwordRef.current) passwordRef.current.value = "";
36:         } catch (error) {
37:             console.error(error);
38:         }
39:     };
40:
41:     return (
42:         <>
43:             <h1>Register Component (Uncontrolled Form) </h1>
44:             <form onSubmit={handleRegister}>
45:                 <label htmlFor="">First Name</label>
46:                 <input
47:                     type="text"
48:                     ref={firstNameRef}
49:                     required
50:                     minLength={4}
51:                     maxLength={30}
52:                     pattern="[A-Za-z ]"
53:                     title="Only letters and spaces are allowed."
54:                     autoFocus
55:                 />
56:                 <br />
57:                 (lastName, email, password inputs follow the same ref pattern)
58:                 <button type="submit">
59:                     Register
60:                 </button>
61:             </form>
62:         </>
63:     );
64: };
65:
66: export default Register;
```

- **Line 5's title, "Uncontrolled Form"** — this page is the deliberate counterpoint to `Login.tsx`'s controlled form, matching Module 10 §10.1's comparison table exactly.
- **Lines 9–12** — four `useRef<HTMLInputElement>(null)` calls, one per field. Unlike `useState`, assigning to `.current` (which React does automatically via the `ref` prop) does **not** trigger a re-render — the DOM itself is the single source of truth for these fields' current values, only read when actually needed (on submit).
- **Lines 17–22** — values are read directly off `ref.current?.value` at submit time — the `?.` guards against `.current` still being `null` (which it can be, in principle, before the input mounts, though in practice `handleRegister` only runs after the form has rendered).
- **Lines 23–26** — a minimal guard-clause validation (Module 04 Pattern 4): if `firstName`/`lastName` are falsy (`undefined` or empty string), log and `return` early, skipping the API call entirely. Note this only checks two of the four fields — `email`/`password` presence relies solely on the native HTML `required` attribute (line 49 etc.) rather than this JS check.
- **Lines 32–35** — after a successful registration, the form is manually cleared by directly setting `.value = ""` on each ref's current DOM node — this is only possible/necessary because the inputs are uncontrolled; a controlled form would instead reset its `useState` object, as `Login.tsx` does in its `finally` block.
- **Lines 49–54** — native HTML validation attributes (`required`, `minLength`, `maxLength`, `pattern`, `title` for the tooltip shown on pattern-mismatch) do real client-side validation work **without any React state at all** — the browser itself blocks submission (and shows its native validation UI) if these constraints aren't met, which is part of why this page can afford to skip JS-side validation for those fields.

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
- **Line 6** — the simplest possible `useState` usage: a single boolean toggling a UI panel.
- **Line 14** — `onClick={() => setIsOpen(!isOpen)}` — an inline arrow function needed here because an argument-free toggle *could* pass the setter directly (`onClick={() => setIsOpen(o => !o)}` would be the more robust functional-update form, avoiding any stale-closure risk from calling `!isOpen` on a value captured at render time; in a simple single-click-at-a-time UI like this it's a low-risk shortcut, but functionally it is exactly the "read `count` directly rather than via `prev =>`" pattern Module 06 flags as a latent stale-closure bug source).
- **Line 15** — a ternary swapping button label based on `isOpen`.
- **Line 18** — a dynamic `className`, combined via a plain ternary+template-literal-free string (Module 05 §5.4's "template literal — simple cases" approach, here without even needing interpolation since there are only two whole-string outcomes) — this is what drives the CSS transition (Module 15's "Bonus 03 — Animations": `.panel { transition: ...; }` + a `.open` modifier class = zero-JS animation, React's only job is toggling the class).

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
32:                 console.log(response.data);
33:                 setEmployee(response.data);
34:                 dispatch(getEmpById(response.data));
35:                 setEmployeeId('');
36:             })
37:             .catch(err => console.error(err));
38:     };
39:     return (
40:         <>
41:             <h1>Employee Component</h1>
42:             <p>This is employee component.</p>
43:             <>
44:             <p>Sample: 6a44b9534f9ff7e746643008</p>
45:                 <form onSubmit={getEmp}>
46:                     <input type="text" name="firstName" value={employeeId} onChange={handleInput} autoFocus placeholder="Please enter employee id" />
47:                     <button type="submit">Find Employee</button>
48:                 </form>
49:             </>
50:             <h3>Data</h3>
51:             <>{employee && (<>
52:                 <p>Id: {dataFromStore.id}</p>
53:                 <p>First Name: {dataFromStore.firstName}</p>
54:                 <p>last Name: {dataFromStore.lastName}</p>
55:                 <p>Email: {dataFromStore.email}</p>
56:                 <p>Salary: {dataFromStore.salary}</p>
57:             </>)} </>
58:         </>
59:     );
60: };
61: export default Employee;
```

- **Lines 5–7** — `react-redux`'s `useSelector`/`useDispatch` and the slice's `getEmpById` action creator plus the store's `RootState` type — this component both reads from and writes to the global Redux store, unlike `Login`/`Register`/`Home` which use only local/context state.
- **Line 14** — `useSelector` subscribes this component to `state.emp.empData`; whenever that slice of state changes (i.e., whenever any dispatched action updates `empData`), this component re-renders automatically — this is the Redux equivalent of `useContext`, but with per-slice subscription granularity rather than "the whole context value changed."
- **Lines 18–19** — **local** `useState` also exists here, separate from Redux: `employee` (used only as a presence flag, see line 51) and `employeeId` (the controlled text input's value).
- **Line 21** — a `useEffect` with an empty dependency array runs its console log once on mount — present purely as a demonstration of the mount lifecycle, doing no real work.
- **Lines 28–38** — `getEmp` is **not `async`**; instead it uses the Promise `.then()/.catch()` chain form directly (functionally equivalent to `await`/`try-catch`, just older-style syntax) to call the service, then: sets local `employee` state (line 33 — used only to gate the JSX below), **dispatches** `getEmpById(response.data)` into Redux (line 34 — this is what actually updates `dataFromStore`), and clears the input (line 35).
- **Line 33 vs. lines 52–56 — a real latent bug worth flagging explicitly**: `setEmployee(response.data)` sets the **local** `employee` state, and the JSX at line 51 checks `{employee && (...)}` — but the actual displayed values inside that block (lines 52–56) all read from `dataFromStore` (the **Redux** state), not from local `employee`. Because `dispatch` on line 34 triggers the Redux update in the same tick, this happens to work in practice (by the time either state's update is reflected in a render, both have updated), but it's inconsistent: the *condition* gating the block and the *data* rendered inside it come from two different, redundant state sources holding the same value. A cleaner version would either drop the local `employee` state entirely (relying solely on Redux) or consistently render from `employee` throughout.
- **Line 46** — a controlled input (`value={employeeId}`, `onChange={handleInput}`) submitting the typed ID via the form's `onSubmit={getEmp}` (with `evt.preventDefault()` on line 29, as required).
- **Line 51** — short-circuit `&&` conditional rendering (Module 04 Pattern 2) — the detail block only renders once an employee has actually been looked up.

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
17:         console.log('loadEmployees');
18:         evt.preventDefault();
19:         try {
20:             const response = await getAllEmployees();
21:             console.log(response.data.data);
22:             dispatch(getAllEmps(response.data.data));
23:         }
24:         catch (error) {
25:             console.error(error);
26:         }
27:     };
28:
29:     return (
30:         <>
31:             <h1>Employee List Component</h1>
32:             <>
33:                 <button onClick={loadEmployees}>Load Employees List</button>
34:             </>
35:             <>
36:                 <h3>List of the employees:</h3>
37:                 {empList && empList.length > 0 ? (
38:                     <>
39:                         <h3>List of employees: ({empList.length})</h3>
40:                         <>
41:                             <div>
42:                                 <span>#</span>
43:                                 <span>First Name</span>
44:                                 <span>Last Name</span>
45:                             </div>
46:                             {empList.map((emp: EmployeeType, index: number) => (
47:                                 <div key={emp._id}>
48:                                     <span>{index + 1}</span>
49:                                     <span>{emp.firstName} {emp.lastName}</span>
50:                                     <Link to={`/employees/${emp._id}`}>{emp.firstName} {emp.lastName}</Link>
51:                                 </div>
52:                             ))}
53:                         </>
54:                     </>
55:                 ) : (
56:                     <p>No employees loaded. Click the button above.</p>
57:                 )}
58:             </>
59:         </>
60:     );
61: };
62:
63: export default EmployeeList;
```

- **Line 12** — `empList` is read from Redux's `allEmpData` (populated by the `getAllEmps` reducer) rather than local state — the list page has no `useState` of its own at all; Redux **is** its state.
- **Lines 16–27** — `loadEmployees` is triggered by a button click (not automatically on mount via `useEffect` — a deliberate design here: the list only loads when the user explicitly asks for it), calls the service, and dispatches the response's `.data.data` (note the double `.data` — the Axios response wraps the server's own `{ data: [...] }` envelope) into Redux via `getAllEmps`.
- **Line 18** — even though this handler is attached to a `<button onClick>` rather than a form's `onSubmit`, `evt.preventDefault()` is still called; harmless here (a plain button click has no default browser action to prevent), but shows the pattern applied slightly over-cautiously, likely copied from the form-submit handlers elsewhere in the file set.
- **Line 37** — a ternary (Module 04 Pattern 1) choosing between the populated-list branch and an empty-state message, gated on `empList && empList.length > 0` (guarding against `empList` being `undefined`/`null` in addition to just empty, though given the slice's `initialState.allEmpData: []`, it should never actually be `null`/`undefined` in practice).
- **Line 46** — `.map()` over `empList`, with **`key={emp._id}`** (line 47) — a real, stable, unique identifier (the MongoDB-style ID from the model), exactly the "use a real unique ID" best practice from Module 04 §4.2, and explicitly **not** the array `index` antipattern the same module warns against.
- **Lines 48–50** — each row renders the index (for display numbering only, **not** used as the key), the name as plain text, and again as a `<Link>` — a bit of duplication (the name appears twice per row) that looks like leftover iteration during development, but functionally the `<Link to={...}>` on line 50 is what actually enables navigating to `/employees/:id`, wired up in `AppRoutes` to render `EmployeeDetails`.

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
11:
12:     const emp = useParams();
13:     const dispatch = useDispatch();
14:     const empData = useSelector((s: RootState) => s.emp.empData);
15:
16:     useEffect(() => {
17:         console.log(emp.id);
18:         getEmployeeById(emp.id)
19:             .then((response) => {
20:                 console.log(response.data);
21:                 dispatch(getEmpById(response.data));
22:             })
23:             .catch();
24:     }, []);
25:
26:     return (
27:         <>
28:             <h1>Employee Details</h1>
29:             <>
30:                 {empData && (<>
31:                     <p>Id: {empData.id}</p>
32:                     <p>First name: {empData.firstName}</p>
33:                     <p>Last name: {empData.lastName}</p>
34:                     <p>Email: {empData.email}</p>
35:                     <p>Salary: {empData.salary}</p>
36:                 </>)}
37:             </>
38:         </>
39:     );
40: };
41:
42: export default EmployeeDetails;
```

- **Line 12** — `useParams()` (no generic type argument here, unlike the courseware's `useParams<{ id: string }>()` convention) reads the dynamic `:id` segment declared in `appRoutes.tsx`'s `path="/employees/:id"`; `emp.id` is used directly.
- **Line 14** — subscribes to Redux's `empData`, same slice `Employee.tsx` writes to — meaning navigating from `EmployeeList` → clicking a `<Link>` → landing here, then this page's own fetch overwrites `empData` with the detail response, which is what actually gets rendered (this page does **not** have the local-vs-Redux double-state issue `Employee.tsx` has, since it reads exclusively from `empData`).
- **Lines 16–24** — this is the courseware's "data fetching on mount via `useEffect`" pattern (Module 07/09): the empty dependency array (line 24) means this runs exactly once, when the component mounts — i.e., once per navigation to a given `/employees/:id` URL, which is correct since React Router **remounts** this component on navigating between two different `:id` values only if the route element itself changes identity; in practice here it does re-run per distinct `/employees/:id` visit because the whole page is freshly mounted by the router each time. **A real gap flagged by the empty deps array**: if `emp.id` (from `useParams`) were to change *without* a full remount (an edge case not really hit by this app's specific route structure), the effect would **not** re-fetch, since `emp.id`/`id` isn't listed as a dependency — the courseware's Module 09 §9.7 example explicitly lists `[id]` as the dependency for exactly this reason.
- **Line 23** — `.catch()` with **no handler function at all** — this silently swallows any fetch error entirely; no error state, no console log, no user feedback. This is a genuine gap relative to the loading/error/success three-state pattern Module 08 §8.5 prescribes for every async operation — this page shows only a "successful data" branch and has no error or loading UI at all.
- **Line 30** — the conditional render gate is `{empData && (...)}` — but since the slice's `initialState.empData` is an object with empty-string fields (not `null`/`undefined`), `empData` is always **truthy** even before any fetch resolves, meaning this guard doesn't actually prevent a brief render of empty-string labels before the real data arrives — a subtler version of the same "no loading state" gap.

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

- **`Parent.tsx` line 8** — `dataFromChild` is state **owned by the parent**, initialized empty; this is the "single source of truth" the child's data will eventually update.
- **Lines 10–13** — `getData` is the callback the parent hands down; when invoked (by the child), it updates the parent's own state — the parent never lets the child touch `dataFromChild` directly.
- **Line 19** — `<Child def={getData} abc={parentData} />` passes **two** props down: `abc` (a plain string, parent → child, one-way) and `def` (a function reference — the callback channel child → parent). Prop names `abc`/`def` are intentionally generic/non-descriptive here, presumably to emphasize that *any* prop name works, not just the conventional `onXxx` naming.
- **`Child.tsx` line 2** — `props` has no type annotation (implicit `any`), losing type safety on `props.abc`/`props.def` — a stricter version would define `interface ChildProps { abc: string; def: (data: string) => void }`.
- **Line 5** — `dataFromParent` reads `props.abc` — proof that props are **read** but the child stores it under its own local name; the child cannot reassign `props.abc` itself (props are read-only, per Module 03 §3.1's rule 2).
- **Lines 7–10** — `sendData` calls `props.def(childData)` — this is the entire mechanism by which data crosses back up the tree: the child never has direct access to the parent's `setDataFromChild`; it can only invoke the function reference the parent chose to expose.
- **Line 16** — the button click triggers `sendData`, which triggers the parent's `getData`, which calls `setDataFromChild`, which re-renders `Parent` (and, since `dataFromChild` is now interpolated into `Parent`'s own JSX on line 18, the new value appears in the parent's own paragraph — demonstrating the full round trip in the UI).

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

- **Lines 2–5** — imports the router-owning component, the Context provider, the configured Redux `store`, and Redux's own `<Provider>` component.
- **Lines 10–20** — this is the **provider nesting order** the whole app's data flow depends on, from outside in: `<Provider store={store}>` (Redux) → `<AuthProvider>` (auth Context) → `<AppRoutes>` (which itself renders `<BrowserRouter>` → `<NavBar>` + `<Routes>`, per 2.5 above).
  - **Redux `<Provider>` is outermost** — this makes the store available to `useSelector`/`useDispatch` calls anywhere below it, including inside `AuthProvider` if it ever needed Redux (it currently doesn't — auth state is Context-only, per 2.4's note).
  - **`AuthProvider` is next** — everything below it, including all of `AppRoutes` and every page, can call `useContext(AuthContext)`.
  - **`AppRoutes` (and its internal `BrowserRouter`) is innermost** — meaning routing context is established *last*; nothing outside `AppRoutes` (i.e., not `App` itself, not `AuthProvider`) has access to router hooks like `useNavigate`, which is fine since neither of those components needs to navigate.
  - This ordering matters concretely: `AuthProvider`'s `login`/`logout` functions don't depend on Redux or routing, so their position relative to those providers is flexible; but `AppRoutes` **does** depend on `AuthContext` (reads `isLoggedIn` on line 17 of `appRoutes.tsx`) and pages inside it depend on both Redux (`useSelector`/`useDispatch`) and the router (`useParams`, `Link`) — so it must be nested inside **both** `Provider` and `AuthProvider`, which is exactly what lines 13–17 establish.
- **Line 9** — a `console.log` documenting the render order (paired with similar logs numbered "1." in `store.ts`/`empSlice.tsx`, forming a breadcrumb trail through the module load and initial render sequence — a debugging technique straight out of Module 06 §6.2's "console strategies").
- **Line 12** — wraps everything in a semantic `<main>` element — the only actual DOM structure `App` itself contributes; all real UI lives in the pages/components below.
- This project's `App.tsx` does **not** include an `ErrorBoundary` wrapping the tree (Module 06 §6.6's recommended global catch-all) — an unhandled render error anywhere in this app would still unmount the whole tree to a blank screen.

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

- **Line 2** — `createRoot` from `react-dom/client`, the React 18+ concurrent-rendering root API — same as the courseware's canonical `main.tsx`.
- **Line 7** — `document.getElementById('root')!` — the non-null assertion, exactly as in Module 00's example, telling TypeScript to trust that the `#root` div (declared in `index.html`) exists.
- **Lines 1, 8, 10** — `<StrictMode>` is imported and referenced but **entirely commented out**, meaning this project runs **without** the dev-time double-render/double-effect behaviour Module 00 §0.8 describes. Practically: `console.log`s that would appear twice under StrictMode (a common source of "is my effect actually buggy or is this just StrictMode?" confusion) appear exactly once here, and any accidental effect-cleanup bugs that StrictMode is specifically designed to surface early would **not** be caught by this project's current dev setup — a real, checkable difference from the courseware's stated default. Re-enabling it would only require uncommenting the three lines.
- **Line 5** — imports `App` with an explicit `.tsx` extension in the import path — allowed by this project's Vite/TS configuration, though many configs omit the extension.

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
