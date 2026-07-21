# Angular — Complete Line-by-Line Guide

This guide is built strictly from Aakash's own course materials in `Courseware/07-angular/` (modules `00-getting-started.md` through `14-mcq-assessment.md`, plus `angular_discussion_qa.md`). The courseware teaches **Angular 21** using standalone components and Signals, built around a running example app called the Employee Management System (EMS).

**IMPORTANT — grounding note on Case B:** The repo's `Code/Angular` folder does **not** contain genuine Angular code. It was verified before writing this guide (see Section 15 below for the full detail): `package.json` names the project `"acme-react-demo"`, dependencies are `react`, `react-dom`, `react-router`, `@reduxjs/toolkit`, `react-redux`, `axios`, and the build tool is `vite` — there is no `@angular/core`, no `@angular/cli`, and no `.ts`/`.html` file anywhere under `src/` contains `@Component`, `@NgModule`, `@Injectable`, or an `@angular/...` import. The files are `.tsx` React components (`App.tsx`, `pages/Login.tsx`, `redux/store.ts`, `routes/appRoutes.tsx`, etc.) — this is the same React/Redux/Vite project that appears elsewhere in the repo, mis-filed under an "Angular" folder name.

Because of that, Section 15 ("Real code walkthrough") is **not** a walkthrough of course-repo Angular code — no such code exists to walk through. Instead it states that plainly and gives a small number of clearly-labelled illustrative snippets (not from the course repo) for the handful of concepts where seeing the syntax matters most for the assessment. All other examples throughout this guide (Sections 1–14) are reproduced from the actual courseware files and are genuine course content.

---

## 1. Getting Started — Angular CLI, Project Structure, Bootstrap (Module 00)

### 1.1 What Angular is, and how it differs from React

The courseware is explicit: **Angular is a full, opinionated framework**; **React is a UI library**. Angular ships routing (`@angular/router`), HTTP (`HttpClient`), forms (reactive + template-driven), a DI container, a CLI, and a testing setup, all built in. React gives you the view layer only — you pick React Router, Axios/fetch, React Hook Form, Redux/Context, and Vite yourself. This is exactly the situation Aakash's own `Code/Angular` folder demonstrates in reverse: it's a "React demo" (`acme-react-demo`) that chose React Router, Axios, and Redux Toolkit — precisely the kind of à la carte stack Angular replaces with built-ins.

Other mental-model contrasts from the courseware:

| | React | Angular |
|--|-------|---------|
| Type | Library | Framework |
| Templates | JSX inside `.tsx` | Separate `.html` files |
| Data binding | One-way by default | Two-way with `[(ngModel)]` |
| State (modern) | `useState`, Signals (React 19) | Signals (Angular 16+) |
| Change detection | Virtual DOM diffing | Zone.js → now Signals |

### 1.2 Creating a project

```bash
ng new acme-ems-angular --routing --style=css --standalone
cd acme-ems-angular
ng serve
```

- `--routing` generates `app.routes.ts`.
- `--style=css` — plain CSS, no SCSS/LESS.
- `--standalone` — standalone components (no `NgModule` scaffolding). The course uses standalone components **exclusively**; legacy `NgModule` code is covered only so Aakash can recognize it in the wild.

### 1.3 Project file anatomy

```
src/
├── app/
│   ├── app.component.ts        ← Root component (class + template + styles)
│   ├── app.component.html      ← Root template
│   ├── app.component.css       ← Root styles (scoped)
│   └── app.routes.ts           ← Route configuration
├── index.html                  ← The one HTML file
├── main.ts                     ← Entry point — bootstraps Angular
└── styles.css                  ← Global stylesheet
```

- `<app-root></app-root>` in `index.html` is the custom element Angular replaces with `AppComponent`'s rendered template — the Angular analogue of React's `<div id="root">` that `ReactDOM.createRoot` mounts into.
- `<base href="/">` in `index.html` is required — the Angular Router uses it to calculate route paths (this is directly tested: MCQ Q8).

### 1.4 Bootstrap — `main.ts`

```ts
import { bootstrapApplication } from '@angular/platform-browser'
import { appConfig }            from './app/app.config'
import { AppComponent }         from './app/app.component'

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err))
```

`bootstrapApplication` is the modern standalone bootstrap API — no `NgModule` required. `app.config.ts` registers global providers (router, HTTP client, etc.) — the Angular equivalent of composing providers around `<App />` in a React `main.tsx`.

### 1.5 Full bootstrap sequence (as taught)

```
① Browser requests the app
② Dev server sends index.html
③ Browser finds <app-root>
④ main.ts runs bootstrapApplication(AppComponent, appConfig)
⑤ Angular reads @Component({ selector: 'app-root' }), finds <app-root> in the DOM
⑥ Angular compiles AppComponent's template, renders into <app-root>
⑦ Router reads the URL, matches a route, renders inside <router-outlet>
⑧ Browser paints
```

### 1.6 Standalone vs NgModule

```ts
// OLD approach (NgModule) — legacy code
@NgModule({
  declarations: [AppComponent, EmployeeCardComponent],
  imports: [BrowserModule, HttpClientModule],
  bootstrap: [AppComponent],
})
export class AppModule {}

// MODERN approach (Standalone) — used throughout this course
@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
})
export class EmployeeCardComponent {}
```

### 1.7 CLI commands reference

```bash
ng serve                                   # dev server, hot reload
ng generate component employees/employee-card    # or: ng g c ...
ng generate service   employees/employee          # or: ng g s ...
ng generate pipe      shared/salary-format
ng generate guard     auth/auth
ng build --configuration=production
ng test
ng e2e
ng lint
```

**Rule stressed throughout the course:** always use `ng generate` (`ng g`) — it wires files correctly instead of hand-creating them.

---

## 2. Components, Templates & Data Binding (Module 01)

### 2.1 A component is a decorated class

Every Angular UI is built from components — a TypeScript class decorated with `@Component`. A component is normally three files (`.ts`, `.html`, `.css`), generated together with `ng g c`.

```ts
// employee-card.component.ts
import { Component, signal } from '@angular/core'

@Component({
  selector:    'app-employee-card',
  standalone:  true,
  imports:     [],
  templateUrl: './employee-card.component.html',
  styleUrl:    './employee-card.component.css',
})
export class EmployeeCardComponent {
  name    = 'Alice Johnson'
  salary  = 95000
  isActive = true
}
```

`selector` is the custom HTML tag (`<app-employee-card>`) used to place the component elsewhere — this is unlike React, where a component is just a function/class you import and call as JSX; Angular templates reference components by an HTML tag name registered in the decorator.

### 2.2 The four data-binding types (core assessment topic)

| Syntax | Direction | Use for |
|--------|-----------|---------|
| `{{ value }}` | Component → Template | Display text (**interpolation**) |
| `[property]="expr"` | Component → Template | DOM properties, inputs (**property binding**) |
| `(event)="handler()"` | Template → Component | User actions (**event binding**) |
| `[(ngModel)]="prop"` | Both | Form inputs with sync (**two-way binding**) |

**Interpolation:**

```html
<h3>{{ employee.name }}</h3>
<p>{{ 2 + 2 }}</p>
<p>{{ isActive ? 'Active' : 'Inactive' }}</p>
```

**Property binding:**

```html
<img [src]="employee.avatarUrl" [alt]="employee.name" />
<button [disabled]="isSubmitting">Save</button>
<div [class.active]="employee.isActive">
<div [style.color]="employee.isActive ? 'green' : 'red'">
<td [attr.colspan]="colSpan">
```

The courseware note: `[src]="imageUrl"` and `src="{{ imageUrl }}"` are equivalent for strings; use property binding whenever the value is not a string (boolean, number, object).

**Event binding:**

```html
<button (click)="handleDelete()">Delete</button>
<input (input)="onInput($event)" />
<form (ngSubmit)="onSubmit()">
```

```ts
onInput(event: Event) {
  const value = (event.target as HTMLInputElement).value
  this.searchTerm = value
}
```

**Two-way binding** (requires `FormsModule` imported into the component):

```html
<input [(ngModel)]="searchTerm" placeholder="Search employees..." />
<p>You typed: {{ searchTerm }}</p>
```

```ts
import { FormsModule } from '@angular/forms'
@Component({ imports: [FormsModule] })
export class SearchComponent { searchTerm = '' }
```

**React contrast (explicit, per courseware framing):** React has one-way data flow only — a controlled input needs `value={searchTerm}` plus `onChange={e => setSearchTerm(e.target.value)}` written by hand. Angular's `[(ngModel)]="searchTerm"` — nicknamed "banana in a box" per `angular_discussion_qa.md` Q10 — collapses that same property-binding + event-binding pair into one directive, syncing both directions automatically without an explicit setState call.

### 2.3 Signals — modern reactive state (Angular 16+)

Before Signals, Angular used **Zone.js**, a library that patches browser async APIs and triggers a full change-detection pass on the whole component tree whenever anything async happens. Signals replace this with fine-grained reactivity: only components that read a changed signal re-render.

```ts
import { Component, signal, computed, effect } from '@angular/core'

export class EmployeeListComponent {
  employees  = signal<Employee[]>([])
  filter     = signal<string>('All')
  searchTerm = signal<string>('')

  filteredEmployees = computed(() =>
    this.employees()
      .filter(e => this.filter() === 'All' || e.department === this.filter())
      .filter(e => e.name.toLowerCase().includes(this.searchTerm().toLowerCase()))
  )

  constructor() {
    effect(() => {
      console.log('Employees updated:', this.employees().length)
    })
  }
}
```

- **Read** a signal by calling it: `this.employees()`.
- **Write** with `.set()` (replace) or `.update()` (derive from old value):

```ts
this.filter.set('Engineering')
this.employees.update(list => [...list, newEmployee])
this.employees.update(list => list.filter(e => e.id !== id))
```

Templates read signals automatically by calling them — no `.subscribe()`, no `async` pipe:

```html
<p>Total employees: {{ employees().length }}</p>
@for (emp of filteredEmployees(); track emp.id) {
  <app-employee-card [employee]="emp" />
}
```

### 2.4 New control-flow syntax (Angular 17+) vs legacy structural directives

Angular 17 introduced `@if` / `@for` / `@switch` as built-in template control flow, replacing `*ngIf` / `*ngFor` / `*ngSwitch` for new code (the old syntax "still works — you'll see it in legacy code," full coverage in Module 04/Section 4 below).

```html
@if (employees().length > 0) {
  <p>{{ employees().length }} employees found</p>
} @else {
  <p>No employees found.</p>
}

@for (emp of employees(); track emp.id) {
  <app-employee-card [employee]="emp" />
} @empty {
  <p>No employees yet.</p>
}

@switch (employee().role) {
  @case ('admin')   { <span>Administrator</span> }
  @case ('manager') { <span>Manager</span> }
  @default          { <span>Employee</span> }
}
```

`track` is **mandatory** in `@for` (unlike the optional `trackBy` on old `*ngFor`) — this is directly tested (MCQ Q20, debugging Error 7 in Module 03).

---

## 3. Components & Databinding Deep Dive (Module 02)

### 3.1 Inputs — receiving data from parent

**Modern `input()` signal API (Angular 17+):**

```ts
export class EmployeeCardComponent {
  employee      = input.required<Employee>()          // required — Angular errors if parent omits it
  compact       = input<boolean>(false)                // optional with default
  highlightId   = input<number | null>(null)           // optional, no default
  isHighlighted = computed(() => this.highlightId() === this.employee().id)
}
```

**Legacy `@Input()` decorator** (still common in existing code):

```ts
export class EmployeeCardComponent {
  @Input({ required: true }) employee!: Employee
  @Input() compact = false
  @Input({ transform: (v: string) => Number(v) }) salary = 0   // input transform, Angular 16+
}
```

The course's rule: **prefer `input()` signals in new code**; `@Input()` remains fully supported and is common in existing projects.

### 3.2 Outputs — emitting events to parent

**Modern `output()` API:**

```ts
export class EmployeeCardComponent {
  select = output<number>()
  onSelect() { this.select.emit(this.employee().id) }
}
```

```html
<app-employee-card [employee]="emp" (select)="handleSelect($event)" />
```

**Legacy `@Output()` + `EventEmitter`:**

```ts
@Output() select = new EventEmitter<number>()
```

Both use `.emit(value)`, and both bind in the template identically with `(select)="handler($event)"`.

**React contrast:** where React passes a callback prop down (`onSelect={handleSelect}`) and the child calls it directly, Angular's `output()`/`@Output()` is a declared, typed emitter the parent listens to via event binding — closer to a native DOM custom event than a plain function prop.

### 3.3 View encapsulation

By default Angular scopes component CSS so it doesn't leak (`ViewEncapsulation.Emulated`, the default — Angular adds attribute selectors). Alternatives: `ViewEncapsulation.None` (global styles) and `ViewEncapsulation.ShadowDom` (real Shadow DOM). Rule from the courseware: keep `Emulated`; only use `None` for a component whose sole purpose is providing shared global styles.

### 3.4 Lifecycle hooks

```
constructor()          → class created (DI runs)
ngOnInit()             → inputs available, component initialised
ngOnChanges()          → inputs changed (runs before ngOnInit too)
ngDoCheck()            → every change detection cycle
ngAfterContentInit()   → content projection done
ngAfterViewInit()      → component view + children initialised
ngOnDestroy()          → component about to be removed
```

```ts
export class EmployeeDetailComponent implements OnInit, OnDestroy, OnChanges {
  employeeId = input.required<number>()

  ngOnChanges(changes: SimpleChanges) {
    if (changes['employeeId'] && !changes['employeeId'].firstChange) {
      this.loadEmployee(this.employeeId())
    }
  }
  ngOnInit()    { this.loadEmployee(this.employeeId()) }
  ngOnDestroy() { this.subscription?.unsubscribe() }
}
```

With Signals, an `effect()` reading an input signal often replaces `ngOnChanges` entirely — it re-runs automatically whenever that input changes.

### 3.5 `ViewChild` / `ElementRef`

```ts
export class EmployeeListComponent implements AfterViewInit {
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>
  @ViewChild(SearchBarComponent) searchBar!: SearchBarComponent

  ngAfterViewInit() { this.searchInput.nativeElement.focus() }
}
```

`@ViewChild` references are only available starting in `ngAfterViewInit` (tested: MCQ Q32).

### 3.6 Content projection with `ng-content`

```html
<!-- card.component.html -->
<div class="card">
  <div class="card-header"><ng-content select="[card-title]" /></div>
  <div class="card-body"><ng-content /></div>
</div>
```

```html
<!-- Parent -->
<app-card>
  <h3 card-title>Alice Johnson</h3>
  <p>Engineering Department</p>
</app-card>
```

This is Angular's equivalent of React's `children` prop, extended with **named slots** via `select="[attr]"` — React's plain `children` has no built-in equivalent to named slots (you'd pass separate props instead).

---

## 4. Debugging Angular Apps (Module 03)

### 4.1 Angular DevTools

Browser extension (Chrome/Firefox) adding an **Angular** tab to DevTools. The **Component Tree** panel shows live inputs, signals, and computed values per component and lets you edit them live. The **Profiler** panel records interactions and shows which components were checked and how long each took, plus what triggered each change-detection cycle.

### 4.2 Source maps and breakpoints

`ng serve` generates source maps so the browser's **Sources** tab shows original `.ts` files, not compiled JS. Alternatively use the `debugger` statement inline:

```ts
handleEmployeeAction(action: EmployeeAction) {
  debugger
  switch (action.type) { /* ... */ }
}
```

### 4.3 Console debugging patterns taught

```ts
console.table(this.employees())
console.group('Signal update')
console.log('new filter:', this.filter())
console.groupEnd()

constructor() {
  effect(() => {
    console.log('[DEBUG] employees changed:', this.employees().length)
  })
}
```

### 4.4 Common errors and fixes (heavily assessment-relevant)

| Error | Cause | Fix |
|---|---|---|
| "Can't bind to 'X' since it isn't a known property" | Component/directive used in a template but missing from that component's `imports` array | Add it to `imports: [...]` |
| `NG0100: ExpressionChangedAfterItHasBeenCheckedError` | Value changes after Angular already checked it (dev mode, Zone.js) — e.g. mutating state in `ngAfterViewInit` | Defer with `setTimeout`, or set the value earlier (e.g. `ngOnInit`) |
| `NullInjectorError: No provider for X` | `@Injectable()` with no `providedIn` and not listed anywhere | Add `providedIn: 'root'`, or list in a component's `providers: []` |
| `Cannot read properties of undefined` | Async data not yet loaded / no null guard | `@if (employee()) { ... }` or optional chaining `employee()?.name` |
| Infinite loop / max call stack | An `effect()` writes to a signal it also reads | Use `computed()` for derived state instead |
| Template property typo (e.g. `nme`) | Caught only if `strictTemplates: true` is set | Enable `angularCompilerOptions.strictTemplates` in `tsconfig.json` |
| `@for loop must have a "track" expression` | Missing `track` clause | `@for (emp of employees(); track emp.id)` |

### 4.5 Strict mode

```json
// tsconfig.json
{
  "compilerOptions": { "strict": true, "noImplicitAny": true, "strictNullChecks": true },
  "angularCompilerOptions": {
    "strictTemplates": true,
    "strictInjectionParameters": true,
    "strictInputAccessModifiers": true
  }
}
```

### 4.6 Debugging checklist (as given in the courseware)

```
1. Read the terminal error in full.
2. Is the component missing from imports array?
3. Is the service missing providedIn or not in providers[]?
4. Is the signal null/undefined? Add @if or optional chaining.
5. Open Angular DevTools → check component properties and signal values.
6. Add console.log() inside the method or effect() being debugged.
7. Use debugger; to pause execution.
8. Is strict templates enabled?
9. Does the @for have a track expression?
10. Is an effect() accidentally writing to a signal it reads?
```

---

## 5. Directives Deep Dive (Module 04)

### 5.1 Three kinds of directives

| Kind | What it does | Example |
|------|-------------|---------|
| Component | Directive with a template | `<app-employee-card>` |
| Attribute | Changes appearance/behaviour of an element | `ngClass`, `ngStyle`, custom |
| Structural | Adds/removes elements from the DOM | `@if`, `@for`, `*ngIf`, `*ngFor` |

### 5.2 Built-in attribute directives

```html
<!-- NgClass, object syntax -->
<div [ngClass]="{
  'card--active':   employee.isActive,
  'card--selected': employee.id === selectedId(),
}">

<!-- Prefer single-class binding when possible -->
<div class="card" [class.card--active]="employee().isActive">

<!-- NgStyle -->
<div [ngStyle]="{ 'font-size': compact() ? '12px' : '14px' }">
<div [style.opacity]="employee().isActive ? 1 : 0.6">
```

### 5.3 Built-in structural directives — legacy `*` syntax

```html
<div *ngIf="employees().length > 0; else emptyState">...</div>
<ng-template #emptyState><p>No employees found.</p></ng-template>

<app-employee-card *ngFor="let emp of filteredEmployees(); trackBy: trackById" [employee]="emp" />

<div [ngSwitch]="employee().role">
  <span *ngSwitchCase="'admin'">Administrator</span>
  <span *ngSwitchDefault>Employee</span>
</div>
```

```ts
trackById(index: number, employee: Employee): number { return employee.id }
```

The `*` prefix is syntactic sugar — `*ngIf` expands to `[ngIf]` on an `<ng-template>` (this is directly tested, MCQ Q29).

### 5.4 New control flow (preferred, Angular 17+)

```html
@if (isLoading()) {
  <p>Loading employees...</p>
} @else if (error()) {
  <p class="error">{{ error() }}</p>
} @else {
  @for (emp of filteredEmployees(); track emp.id) {
    <app-employee-card [employee]="emp" />
  }
}
```

### 5.5 Custom attribute directive — `HighlightDirective`

```ts
@Directive({ selector: '[appHighlight]', standalone: true })
export class HighlightDirective {
  appHighlight = input<boolean>(false)
  color        = input<string>('#fff3cd')

  private el       = inject(ElementRef)
  private renderer = inject(Renderer2)

  constructor() {
    effect(() => {
      if (this.appHighlight()) {
        this.renderer.setStyle(this.el.nativeElement, 'background-color', this.color())
      } else {
        this.renderer.removeStyle(this.el.nativeElement, 'background-color')
      }
    })
  }
}
```

`Renderer2` is used instead of touching `nativeElement` directly because it's a safe abstraction that works outside a plain browser DOM context (e.g. server-side rendering) — this is directly tested (MCQ Q30).

### 5.6 Custom structural directive — `RepeatDirective`

```ts
@Directive({ selector: '[appRepeat]', standalone: true })
export class RepeatDirective {
  appRepeat = input<number>(0)
  private templateRef   = inject(TemplateRef<{ $implicit: number }>)
  private viewContainer = inject(ViewContainerRef)

  constructor() {
    effect(() => {
      this.viewContainer.clear()
      for (let i = 0; i < this.appRepeat(); i++) {
        this.viewContainer.createEmbeddedView(this.templateRef, { $implicit: i })
      }
    })
  }
}
```

```html
<div *appRepeat="3; let i">Row {{ i + 1 }}</div>
```

Custom structural directives use `TemplateRef` + `ViewContainerRef` to decide **when** a chunk of template gets inserted into the DOM — there is no equivalent primitive in React, where conditional/list rendering is just plain JS control flow inside JSX.

---

## 6. Services & Dependency Injection (Module 05)

### 6.1 Why services

Components should only handle what the user sees; business logic, data management, and cross-component communication belong in services. This is Angular's built-in separation-of-concerns layer.

### 6.2 Creating a service

```ts
@Injectable({ providedIn: 'root' })   // singleton — one instance app-wide
export class EmployeeService {
  private _employees  = signal<Employee[]>(INITIAL_EMPLOYEES)
  readonly employees  = this._employees.asReadonly()   // public read-only view

  readonly activeEmployees = computed(() => this._employees().filter(e => e.isActive))

  add(dto: Omit<Employee, 'id'>): Employee {
    const newEmployee: Employee = { ...dto, id: this.nextId++ }
    this._employees.update(list => [...list, newEmployee])
    return newEmployee
  }
  remove(id: number): void {
    this._employees.update(list => list.filter(e => e.id !== id))
  }
}
```

The private-signal + `asReadonly()`-public pattern is the course's standard: external code can read `employees()` but can only mutate state through the service's own methods.

### 6.3 Injecting services

**Modern `inject()` function (Angular 14+, preferred):**

```ts
export class EmployeeListComponent {
  private employeeService = inject(EmployeeService)
  employees = this.employeeService.employees
}
```

**Legacy constructor injection** (still common in existing codebases):

```ts
constructor(private employeeService: EmployeeService) {}
```

Both resolve to the same singleton instance — `inject()` is just the modern functional syntax (tested: MCQ Q34).

**React contrast:** Angular's DI + `providedIn: 'root'` singleton service is the framework-native analogue of a React Context provider or a Redux store (which Aakash's actual `Code/Angular` — really `acme-react-demo` — folder implements via `@reduxjs/toolkit` in `src/redux/store.ts` and `src/redux/empSlice.tsx`, and via `AuthContextType.tsx`/`AuthProvider.tsx`). Where React requires you to wire a `Provider` component around the tree and consume it with `useContext`/`useSelector`, Angular's injector resolves the dependency automatically from a hierarchical injector tree, with no wrapping component needed.

### 6.4 Injector hierarchy — `providedIn: 'root'` vs component-level

```
Root Injector (providedIn: 'root')
├── EmployeeService   ← singleton, one instance for entire app
└── AuthService

  Component Injector (providers: [SomeService] in @Component)
  └── SomeService     ← new instance just for this component + its children
```

```ts
@Injectable({ providedIn: 'root' })   // shared app-wide
export class EmployeeService {}

@Component({ providers: [EmployeeFormService] })   // fresh instance per component
export class CreateEmployeeComponent {
  private formService = inject(EmployeeFormService)
}
```

### 6.5 Service-to-service injection and cross-component communication

Services can inject other services. A `NotificationService` built purely on signals lets **any** component push/read toast notifications without a parent-child relationship — this is the course's answer to "sibling communication" (`angular_discussion_qa.md` Q51): lift to a common parent, or share a service.

```ts
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = signal<Notification[]>([])
  readonly notifications = this._notifications.asReadonly()

  show(type: Notification['type'], message: string, durationMs = 4000): void {
    const notification = { id: this.nextId++, type, message }
    this._notifications.update(list => [...list, notification])
    if (durationMs > 0) setTimeout(() => this.dismiss(notification.id), durationMs)
  }
  success(message: string) { this.show('success', message) }
}
```

---

## 7. Routing (Module 06)

### 7.1 How it works

Angular Router intercepts browser URL changes and maps them to components, without a full page reload — this is what makes Angular an SPA framework (same underlying concept as `react-router`, which Aakash's actual `Code/Angular`/`acme-react-demo` project uses in `src/routes/appRoutes.tsx`).

```
URL: /employees/3/edit
  → Router reads routes config
  → Matches { path: 'employees/:id/edit', component: EditEmployeePage }
  → Renders inside <router-outlet>
  → Component reads :id from ActivatedRoute
```

### 7.2 Route configuration

```ts
// app.routes.ts
export const routes: Routes = [
  { path: '', redirectTo: '/employees', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'employees', loadChildren: () => import('./employees/employees.routes').then(m => m.employeeRoutes) },
  { path: 'departments', canActivate: [authGuard], loadChildren: () => import('./departments/departments.routes').then(m => m.departmentRoutes) },
  { path: '**', loadComponent: () => import('./shared/pages/not-found/not-found.component').then(m => m.NotFoundComponent) }, // must be last
]
```

Register in `app.config.ts`:

```ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
  ],
}
```

`withComponentInputBinding()` (Angular 16+) automatically maps route params, query params, and route data to component `input()` signals — no manual `ActivatedRoute` subscription needed for the common case.

### 7.3 `<router-outlet>` and links

```html
<nav>
  <a routerLink="/employees" routerLinkActive="nav-active">Employees</a>
</nav>
<main><router-outlet /></main>
```

```ts
imports: [RouterLink, RouterLinkActive, RouterOutlet]
```

```html
<a [routerLink]="['/employees', emp.id]">{{ emp.name }}</a>
```

### 7.4 Reading route parameters

**With `withComponentInputBinding()` (cleanest, preferred):**

```ts
// Route: /employees/:id
export class EmployeeDetailPageComponent {
  id = input.required<string>()   // 'id' matches the :id param — bound automatically
  employee = computed(() => this.employeeService.getById(Number(this.id())))
}
```

**Legacy manual approach with `ActivatedRoute`:**

```ts
constructor() {
  this.route.paramMap.subscribe(params => {
    const id = Number(params.get('id'))
    this.employee.set(this.employeeService.getById(id) ?? null)
  })
}
```

### 7.5 Query parameters, route data, programmatic navigation

```ts
this.router.navigate([], { queryParams: { dept, page: 1 }, queryParamsHandling: 'merge' })
this.router.navigate(['/employees', saved.id])
this.router.navigateByUrl('/employees')
```

### 7.6 Route guards

```ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth   = inject(AuthService)
  const router = inject(Router)
  if (auth.isLoggedIn()) return true
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } })
}
```

A guard blocks navigation by returning `false` **or** a `UrlTree` (via `router.createUrlTree(...)`) that redirects elsewhere (tested, MCQ Q40 — the trick is that `false` alone is a valid block, and a `UrlTree` is a valid redirect; the answer key marks A, distinct from the "always B" pattern of most other questions).

`canDeactivate` guards unsaved-changes navigation away from a form:

```ts
export const unsavedChangesGuard: CanDeactivateFn<CanComponentDeactivate> =
  (component) => component.canDeactivate() ? true : window.confirm('You have unsaved changes. Leave anyway?')
```

### 7.7 Lazy loading

```ts
// Without lazy loading — all code downloaded upfront
{ path: 'employees', component: EmployeeListPageComponent }

// With lazy loading — code downloaded only when /employees is visited
{ path: 'employees', loadComponent: () => import('./employees/employee-list-page.component').then(m => m.EmployeeListPageComponent) }
```

---

## 8. Observables & RxJS (Module 07)

### 8.1 Observable vs Promise (core assessment topic)

| | Promise | Observable |
|--|---------|-----------|
| Values | One | Zero, one, or many |
| Lazy | No — starts immediately | Yes — starts only when subscribed |
| Cancellable | No | Yes (`unsubscribe()`) |
| Operators | `.then()`, `.catch()` | 100+ RxJS operators |

```ts
// Promise — starts immediately, can't cancel
const promise = fetch('/api/employees').then(r => r.json())

// Observable — lazy, cancellable
const ticks$ = interval(1000)               // not running yet
const sub = ticks$.subscribe(n => console.log(n))  // starts now
sub.unsubscribe()                            // stops it
```

Convention: Observable variable names end with `$` (`employees$`, `search$`).

### 8.2 Creating observables

```ts
const name$    = of('Alice', 'Bob', 'Carol')     // fixed values, then complete
const arr$     = from([1, 2, 3])                  // Array/Promise/Iterable → Observable
const tick$    = interval(1000)                   // 0,1,2,3... every N ms
const click$   = fromEvent<MouseEvent>(document, 'click')
```

### 8.3 Subscribing and unsubscribing

```ts
export class TimerComponent implements OnInit, OnDestroy {
  private sub = new Subscription()
  ngOnInit()    { this.sub.add(interval(1000).subscribe(n => console.log(n))) }
  ngOnDestroy() { this.sub.unsubscribe() }   // prevents memory leaks
}
```

**Best practice (Angular 16+):** `takeUntilDestroyed()` auto-unsubscribes on component destroy, no manual `ngOnDestroy` needed:

```ts
import { takeUntilDestroyed } from '@angular/core/rxjs-interop'
interval(1000).pipe(takeUntilDestroyed()).subscribe(n => console.log(n))
```

Forgetting to unsubscribe is a real memory leak — every mount/unmount cycle creates a new subscription without cleaning up the old one (`angular_discussion_qa.md` Q78).

### 8.4 Core operators

```ts
map(employees => employees.filter(e => e.isActive))       // transform
filter((e: MouseEvent) => e.button === 0)                  // pass matching values only

// switchMap — most-used operator; cancels previous inner Observable
searchTerm$.pipe(switchMap(term => this.employeeService.search(term)))

mergeMap(id => this.employeeService.delete$(id))            // run concurrently
concatMap(employee => this.employeeService.save$(employee)) // run sequentially, in order

debounceTime(300)            // wait for a pause before emitting
distinctUntilChanged()       // only emit on actual change

catchError(err => { console.error(err); return of([]) })    // handle errors gracefully
tap(employees => console.log('Received:', employees.length)) // side effects, doesn't change stream
combineLatest([employees$, filter$])                          // combine multiple streams
```

`switchMap` vs `mergeMap` vs `concatMap`, per the discussion Q&A: `switchMap` cancels the previous inner Observable on each emission (ideal for search-as-you-type — only the latest request matters); `mergeMap` runs all inner Observables concurrently (parallel independent operations); `concatMap` queues them and runs strictly in order (when order matters, e.g. sequential saves).

### 8.5 Subject and BehaviorSubject

```ts
const action$ = new Subject<string>()                 // no initial value, late subscribers miss past emissions
action$.subscribe(a => console.log('Action:', a))
action$.next('delete')

const filter$ = new BehaviorSubject<string>('All')     // has current value, late subscribers get it immediately
filter$.subscribe(f => console.log('Filter:', f))      // immediately logs 'All'
console.log(filter$.value)                              // read current value synchronously
```

### 8.6 Converting between Observables and Signals

```ts
import { toSignal, toObservable } from '@angular/core/rxjs-interop'

private searchTerm$ = toObservable(this.searchTerm)      // Signal → Observable
searchResults = toSignal(
  this.searchTerm$.pipe(debounceTime(300), distinctUntilChanged(), switchMap(term => this.employeeService.search$(term))),
  { initialValue: [] as Employee[] }                       // required — signal needs an initial value
)
```

### 8.7 The `async` pipe

```html
@if (employees$ | async; as employees) {
  @for (emp of employees; track emp.id) { <app-employee-card [employee]="emp" /> }
}
```

Advantages: auto-subscribes on render, auto-unsubscribes on destroy (no memory leak), triggers change detection on new value. With Signals, `async` is rarely needed since signals read directly in templates without it.

**React contrast:** Angular treats async data streams as first-class via RxJS Observables + `HttpClient` (which *returns* Observables, not Promises). React has no equivalent built-in stream primitive — the common pattern is `useEffect` + `fetch`/Axios returning Promises, or a data-fetching library (React Query, SWR). This is a genuine architectural difference, not just syntax: Observables are cancellable and can emit multiple times; a Promise resolves once and can't be cancelled.

---

## 9. Handling Forms in Angular (Module 08)

### 9.1 Reactive vs template-driven — quick comparison

| | Reactive Forms | Template-Driven Forms |
|--|---------------|----------------------|
| Form model | Defined in TypeScript class | Defined in HTML template |
| Validation | In TypeScript — testable | In template with directives |
| Dynamic forms | Easy (add/remove controls) | Complex |
| Async validators | First-class | Awkward |
| **Use for** | **Most forms — production default** | **Simple, 2-3 field forms** |

### 9.2 Building a reactive form

```ts
export class EmployeeFormComponent implements OnInit {
  private fb = inject(FormBuilder)
  form!: FormGroup

  ngOnInit() {
    this.form = this.fb.group({
      name:   ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email:  ['', [Validators.required, Validators.email]],
      salary: [70000, [Validators.required, Validators.min(10000), Validators.max(5_000_000)]],
      isActive: [true],
    })
    if (this.employeeToEdit()) this.form.patchValue(this.employeeToEdit()!)
  }

  get nameCtrl() { return this.form.get('name')! }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return }   // surfaces all errors at once
    this.formSubmit.emit(this.form.value)
  }
}
```

```html
<form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>
  <input id="name" formControlName="name" />
  @if (nameCtrl.touched && nameCtrl.errors) {
    @if (nameCtrl.errors['required'])  { <span class="error">Name is required</span> }
    @if (nameCtrl.errors['minlength']) { <span class="error">Too short</span> }
  }
  <button type="submit" [disabled]="form.invalid || form.pristine">Save</button>
</form>
```

### 9.3 Custom validators

```ts
// Synchronous cross-value validator
export function noFreeEmailValidator(): ValidatorFn {
  const freeProviders = ['gmail.com', 'yahoo.com', 'hotmail.com']
  return (control: AbstractControl): ValidationErrors | null => {
    const domain = (control.value ?? '').split('@')[1]?.toLowerCase()
    return domain && freeProviders.includes(domain) ? { freeEmail: { domain } } : null
  }
}

// Async validator — returns an Observable
export function uniqueEmailValidator(currentId?: number): AsyncValidatorFn {
  return (control: AbstractControl) => timer(400).pipe(
    switchMap(() => {
      const exists = inject(EmployeeService).employees().some(e => e.email === control.value && e.id !== currentId)
      return of(exists ? { emailTaken: true } : null)
    }),
  )
}
```

```ts
email: ['', [Validators.required, Validators.email, noFreeEmailValidator()], [uniqueEmailValidator()]],
//                sync validators here ^^^^^^^^^^^^^^^^^^                     ^^^^^^^^^^^^^^^^^^^ async validators — third array
```

A custom validator is a function taking an `AbstractControl` and returning `{ errorKey: true }` if invalid or `null` if valid. Async validators return an Observable/Promise of that same shape.

### 9.4 `FormArray` — dynamic fields

```ts
form = this.fb.group({
  projectName: ['', Validators.required],
  teamMembers: this.fb.array([this.fb.control('', Validators.required)]),
})
get teamMembers() { return this.form.get('teamMembers') as FormArray }
addMember()    { this.teamMembers.push(this.fb.control('', Validators.required)) }
removeMember(i: number) { this.teamMembers.removeAt(i) }
```

### 9.5 Template-driven forms

```ts
import { FormsModule } from '@angular/forms'
@Component({ imports: [FormsModule] })
export class QuickSearchComponent { searchTerm = ''; onSearch() {} }
```

```html
<form #searchForm="ngForm" (ngSubmit)="onSearch()">
  <input name="searchTerm" [(ngModel)]="searchTerm" #searchInput="ngModel" required minlength="2" />
  @if (searchInput.touched && searchInput.invalid) {
    @if (searchInput.errors?.['required']) { <span class="error">Required</span> }
  }
  <button type="submit" [disabled]="searchForm.invalid">Search</button>
</form>
```

Course rule: reactive for anything beyond 2-3 fields, or with validation/dynamic fields/async checks — template-driven only for quick search bars, filters, newsletter signups.

### 9.6 Angular form state CSS classes

`ng-pristine` / `ng-dirty`, `ng-untouched` / `ng-touched`, `ng-valid` / `ng-invalid`, `ng-pending` (async validator running) — Angular applies these automatically so error styling can be driven purely by CSS: `input.ng-invalid.ng-touched { border-color: red; }`.

`form.patchValue(data)` updates only the fields provided; `form.setValue(data)` requires **all** fields or throws.

---

## 10. Pipes (Module 09)

### 10.1 What pipes are

Pipes transform a value in the template without mutating the source data, applied with `|`: `{{ value | pipeName:arg1:arg2 }}`, chainable: `{{ value | pipe1 | pipe2 }}`.

### 10.2 Built-in pipes (all from `@angular/common`)

```html
{{ employee.joinDate | date:'mediumDate' }}                          <!-- Nov 15, 2021 -->
{{ employee.salary   | currency:'INR':'symbol':'1.0-0' }}            <!-- ₹95,000 -->
{{ 1234567.89        | number:'1.0-0' }}                             <!-- 1,234,568 -->
{{ 0.75               | percent }}                                   <!-- 75% -->
{{ employee.name      | uppercase }}
{{ 'hello world'      | titlecase }}                                 <!-- Hello World -->
{{ employees()        | slice:0:5 }}
<pre>{{ employee() | json }}</pre>                                    <!-- debug only -->
{{ employees$ | async | json }}
@for (entry of employee() | keyvalue; track entry.key) { {{ entry.key }}: {{ entry.value }} }
```

### 10.3 Custom pipes

```ts
@Pipe({ name: 'salaryRange', standalone: true, pure: true })   // pure = default, only recalcs when input reference changes
export class SalaryRangePipe implements PipeTransform {
  transform(salary: number): string {
    if (salary < 500_000)   return 'Junior (< ₹5L)'
    if (salary < 1_000_000) return 'Mid (₹5L–₹10L)'
    return 'Principal (₹20L+)'
  }
}
```

```html
<span>{{ employee().salary | salaryRange }}</span>
```

A search/filter pipe (impure example):

```ts
@Pipe({ name: 'filterEmployees', standalone: true, pure: false })   // impure — reruns every change-detection cycle
export class FilterEmployeesPipe implements PipeTransform {
  transform(employees: Employee[], term: string): Employee[] {
    if (!term.trim()) return employees
    return employees.filter(e => e.name.toLowerCase().includes(term.toLowerCase()))
  }
}
```

### 10.4 Pure vs impure — the rule

**Always start with a pure pipe** (`pure: true`, the default) — Angular can cache the result and only recalculates when the input reference changes. Switch to `pure: false` only if you observe it missing updates (e.g. filtering a mutated-in-place array). Performance note from the course: prefer filtering in the component with `computed()` signals over impure pipes, since impure pipes run on every change-detection cycle.

### 10.5 Pipe chaining and use in TypeScript

```html
{{ employee().joinDate | date:'dd MMM yyyy' | uppercase }}   <!-- "15 NOV 2021" -->
```

```ts
private currencyPipe = inject(CurrencyPipe)
formatSalary(salary: number): string { return this.currencyPipe.transform(salary, 'INR', 'symbol', '1.0-0') ?? '' }
// Component needs: providers: [CurrencyPipe]
```

---

## 11. Making HTTP Requests (Module 10)

### 11.1 Setup

```ts
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
  ],
}
```

`inject(HttpClient)` — **never `new HttpClient()`** — it must come from Angular's DI so interceptors and configuration apply.

### 11.2 Typed CRUD via a service

```ts
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http    = inject(HttpClient)
  private baseUrl = `${environment.apiBaseUrl}/users`

  loadAll(): void {
    this._loading.set(true)
    this.http.get<ApiUser[]>(this.baseUrl).pipe(
      map(users => users.map(mapApiUser)),
      catchError(err => { this._error.set(err.message); this._loading.set(false); return throwError(() => err) })
    ).subscribe(employees => { this._employees.set(employees); this._loading.set(false) })
  }

  create$(dto: Omit<Employee, 'id'>): Observable<Employee> {
    return this.http.post<ApiUser>(this.baseUrl, dto).pipe(
      map(user => ({ ...mapApiUser(user), ...dto, id: Date.now() })),
      tap(created => this._employees.update(list => [...list, created]))
    )
  }

  delete$(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this._employees.update(list => list.filter(e => e.id !== id)))
    )
  }
}
```

`tap()` updates local signal state after a successful call without altering the stream; `catchError()` handles failures without crashing the whole app.

### 11.3 All HTTP verbs

```ts
this.http.get<Employee[]>('/api/employees')
this.http.get<Employee[]>('/api/employees', { params: new HttpParams().set('department', 'Engineering') })
this.http.post<Employee>('/api/employees', newEmployeeData)
this.http.put<Employee>(`/api/employees/${id}`, fullEmployeeData)         // full replace
this.http.patch<Employee>(`/api/employees/${id}`, { isActive: false })    // partial update
this.http.delete<void>(`/api/employees/${id}`)
this.http.get<Employee[]>('/api/employees', { observe: 'response' })      // full response incl. status/headers
```

`HttpParams` builds URL query strings immutably and type-safely.

### 11.4 Interceptors — auth, error, loading

```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken()
  if (token) {
    const authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })  // immutable copy
    return next(authReq)
  }
  return next(req)
}
```

```ts
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401) inject(Router).navigate(['/login'])
    return throwError(() => error)
  }))
```

```ts
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService)
  loading.start()
  return next(req).pipe(finalize(() => loading.stop()))   // guaranteed on success OR error
}
```

`req.clone({ setHeaders })` creates a modified copy of the request — requests are immutable (tested, MCQ Q58). `finalize()` guarantees cleanup runs regardless of success/failure.

### 11.5 HTTP → Signal

```ts
departments = toSignal(
  this.http.get<Department[]>('/api/departments').pipe(catchError(() => of([] as Department[]))),
  { initialValue: [] as Department[] }
)
```

**React contrast:** Angular centralizes cross-cutting HTTP concerns (auth headers, global error handling, loading spinners) into interceptors registered once in `app.config.ts`. React/Axios equivalents exist (`axios.interceptors.request.use(...)`), which is exactly the pattern Aakash's real `Code/Angular` (`acme-react-demo`) project would need to hand-roll itself using `axios` — Angular provides the interceptor mechanism as a first-class framework feature instead of a library convention.

---

## 12. Authentication & Route Protection (Module 11)

### 12.1 Flow

```
User visits protected route → authGuard checks AuthService.isLoggedIn()
  Not logged in → redirect to /login?returnUrl=...
  Logged in     → check role if needed → render component
User logs in → AuthService stores token + user → redirect back to original destination
```

### 12.2 `AuthService` with Signals

```ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient)
  private _currentUser = signal<AuthUser | null>(this.loadUserFromStorage())

  readonly currentUser = this._currentUser.asReadonly()
  readonly isLoggedIn  = computed(() => this._currentUser() !== null)
  readonly isAdmin     = computed(() => this._currentUser()?.role === 'admin')

  private loadUserFromStorage(): AuthUser | null {
    try { const stored = localStorage.getItem(USER_KEY); return stored ? JSON.parse(stored) : null }
    catch { return null }
  }

  getToken(): string | null { return localStorage.getItem(TOKEN_KEY) }

  login(credentials: LoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials)
      .pipe(tap(response => this.saveSession(response.user)))
  }

  logout(): void { this.clearSession(); this.router.navigate(['/login']) }
  hasRole(roles: string[]): boolean { return roles.includes(this._currentUser()?.role ?? '') }
}
```

### 12.3 Guards

```ts
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService)
  if (auth.isLoggedIn()) return true
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } })
}

// Factory guard — parameterised by role
export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return () => inject(AuthService).hasRole(allowedRoles) || inject(Router).createUrlTree(['/unauthorized'])
}
```

```ts
{ path: 'admin', canActivate: [authGuard, roleGuard(['admin'])], loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes) }
```

### 12.4 Token attachment via interceptor

```ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken()
  return token ? next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })) : next(req)
}
```

### 12.5 Role-based UI

```html
@if (auth.isAdmin()) { <a routerLink="/admin">Admin</a> }
@if (auth.isLoggedIn()) {
  <span>{{ auth.userName() }}</span>
  <button (click)="auth.logout()">Sign Out</button>
} @else {
  <a routerLink="/login">Sign In</a>
}
```

### 12.6 Token storage — security notes (as taught)

| Storage | XSS Safe | CSRF Safe | Notes |
|---------|----------|-----------|-------|
| `localStorage` | No | Yes | Simple, common in SPAs — fine for training/demos |
| `sessionStorage` | No | Yes | Cleared when tab closes |
| `httpOnly` cookie | Yes | No (mitigated) | Safest — JS can't read it |
| Memory (signal) | Yes | Yes | Lost on refresh — needs refresh-token rotation |

Course guidance: `localStorage` is fine for training; production should use `httpOnly` cookies + CSRF tokens or in-memory tokens with refresh rotation.

**React contrast:** Aakash's real `Code/Angular` (`acme-react-demo`) project implements this same problem — auth state, login, protected routing — via `src/context/AuthProvider.tsx` + `AuthContextType.tsx` (React Context) and `src/pages/Login.tsx`/`src/routes/appRoutes.tsx` (React Router). Angular's guard functions (`CanActivateFn`) run *before* the router even renders the component, blocking navigation outright; React Router's typical pattern (a `<ProtectedRoute>` wrapper component checking context and redirecting) renders first and then redirects — a subtly different mechanism worth knowing for the assessment.

---

## 13. Dynamic Components, Angular Modules & Optimisation (Module 12)

### 13.1 Dynamic components at runtime

```ts
@Injectable({ providedIn: 'root' })
export class ModalService {
  private appRef   = inject(ApplicationRef)
  private injector = inject(EnvironmentInjector)

  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise(resolve => {
      const componentRef = createComponent(ConfirmDialogComponent, { environmentInjector: this.injector })
      componentRef.setInput('title', options.title)
      componentRef.instance.confirmed.subscribe(() => { resolve(true); this.destroy(componentRef, host) })
      this.appRef.attachView(componentRef.hostView)
      const host = document.createElement('div')
      document.body.appendChild(host)
      host.appendChild(componentRef.location.nativeElement)
    })
  }
}
```

`createComponent()` programmatically creates and inserts a component at runtime — used for modals, toasts, dialogs (tested, MCQ Q67) — distinct from `ng generate component`, which just scaffolds files at dev time.

### 13.2 `@defer` — declarative lazy template blocks (Angular 17+)

```html
@defer (on viewport) {
  <app-salary-chart [employees]="employees()" />
} @placeholder {
  <div class="chart-placeholder">Chart loading...</div>
} @loading (minimum 300ms) {
  <div class="spinner">Loading chart...</div>
} @error {
  <p>Could not load chart.</p>
}

@defer (on interaction) { <app-employee-audit-log [employeeId]="id()" /> } @placeholder { <button>Load Audit Log</button> }
@defer (on idle)        { <app-recommendations /> }
@defer (when showDetails()) { <app-employee-detail [employee]="employee()" /> }
```

`@placeholder` shows fallback content before the deferred block starts loading, replaced once the real content loads (tested, MCQ Q76). Triggers: `on viewport`, `on idle`, `on interaction`, `when condition()`.

### 13.3 NgModule — legacy context (still assessment-relevant)

```ts
@NgModule({
  declarations: [AppComponent, EmployeeCardComponent],   // components/pipes/directives in this module
  imports: [BrowserModule, HttpClientModule, AppRoutingModule],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
```

```ts
// main.ts (NgModule-based bootstrap)
platformBrowserDynamic().bootstrapModule(AppModule)
```

| | NgModule (legacy) | Standalone (modern) |
|--|------------------|---------------------|
| Component declares itself | No — declared in module | Yes — `standalone: true` |
| Import dependencies | In module's `imports` | In component's own `imports` |
| Providers | In module's `providers` | `provideXxx()` in `app.config.ts` |
| Lazy loading | `loadChildren: () => import(...Module)` | `loadComponent`/`loadChildren` with routes |

### 13.4 `OnPush` change detection

```ts
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeCardComponent {
  employee = input.required<Employee>()   // re-renders only when this signal reference changes
}
```

`OnPush` re-checks a component only when: its input reference changes, a signal/Observable it reads emits, a DOM event fires inside it, or `markForCheck()` is called manually. Course guidance: adopt Signals + `OnPush` on every leaf component.

### 13.5 Preloading and bundle analysis

```ts
provideRouter(routes, withPreloading(PreloadAllModules), withComponentInputBinding())
```

```bash
ng build --stats-json
npx webpack-bundle-analyzer dist/acme-ems-angular/stats.json
```

Common optimisations after analysis: split large vendor libraries, remove unused Angular locale data, use `@defer` for below-the-fold heavy components, import RxJS operators from `rxjs/operators` rather than the whole `rxjs` barrel for tree-shaking.

---

## 14. Deployment, Testing & CLI Roundup (Module 13)

### 14.1 Production build

```bash
ng build --configuration=production
```

Does: AOT compilation (templates → JS at build time, not runtime), tree-shaking, minification, content hashing (`main.abc123.js` for cache busting), differential loading.

### 14.2 Environments

```ts
// environment.ts (dev)
export const environment = { production: false, apiBaseUrl: 'http://localhost:3000/api' }
// environment.production.ts
export const environment = { production: true, apiBaseUrl: 'https://api.acme-ems.com/v1' }
```

`angular.json`'s `fileReplacements` swaps the file automatically at build time for the `production` configuration.

### 14.3 Deployment targets

- **Vercel** — `vercel --prod`, with a `vercel.json` rewrite (`"source": "/(.*)"` → `"/index.html"`) so client-side routes like `/employees/1` don't 404.
- **Netlify** — drag `dist/.../browser/` to the drop UI, or CLI `netlify deploy --dir=... --prod`; needs a `_redirects` file (`/*  /index.html  200`) for the same SPA-routing reason.
- **Docker** — multi-stage `Dockerfile` (Node build stage → nginx serve stage), with `nginx.conf`'s `try_files $uri $uri/ /index.html;` doing the SPA fallback.
- **GitHub Actions** — checkout → setup-node → `npm ci` → lint → test (`--watch=false --browsers=ChromeHeadless`) → build → deploy.

### 14.4 Unit testing (Jasmine + Karma, or Jest)

```bash
ng test                    # watch mode
ng test --watch=false       # once
ng test --code-coverage
```

**Service test:**

```ts
describe('EmployeeService', () => {
  let service: EmployeeService
  beforeEach(() => { TestBed.configureTestingModule({}); service = TestBed.inject(EmployeeService) })

  it('should add a new employee', () => {
    const initial = service.employees().length
    service.add({ ...mockEmployee, id: undefined as never })
    expect(service.employees().length).toBe(initial + 1)
  })
})
```

**Component test:**

```ts
beforeEach(async () => {
  await TestBed.configureTestingModule({ imports: [EmployeeCardComponent, CurrencyPipe, DatePipe] }).compileComponents()
  fixture   = TestBed.createComponent(EmployeeCardComponent)
  component = fixture.componentInstance
  fixture.componentRef.setInput('employee', mockEmployee)   // set a required input() signal in tests
  fixture.detectChanges()                                     // triggers change detection / re-render
})

it('should emit select event when card is clicked', () => {
  let emittedId: number | undefined
  component.select.subscribe((id: number) => emittedId = id)
  fixture.nativeElement.querySelector('.card').click()
  fixture.detectChanges()
  expect(emittedId).toBe(1)
})
```

**Mocking a service dependency:**

```ts
const mockEmployeeService = { employees: signal(mockEmployees), loadAll: jasmine.createSpy('loadAll') }
TestBed.configureTestingModule({
  imports:   [EmployeeListPageComponent],
  providers: [{ provide: EmployeeService, useValue: mockEmployeeService }],
})
```

`jasmine.createSpyObj`/`createSpy` produce mock functions that track calls — used for isolating the unit under test from its real dependencies, same idea as `jest.fn()`.

### 14.5 CLI deep dive

```bash
ng generate component path/component-name --skip-tests
ng g c my-comp --inline-template --inline-style --flat
ng build --stats-json
ng test --include='**/employee.service.spec.ts'
ng lint --fix
ng update @angular/core @angular/cli
ng add @angular/material
```

### 14.6 Course roundup — ecosystem map (as taught)

```
State Management:  Signals (primary, built-in) | NgRx (Redux pattern) | Akita/Elf
HTTP & Data:       HttpClient (built-in) | TanStack Query Angular
Forms:             Reactive Forms (primary) | Template-Driven | ngx-formly
UI Libraries:      Angular Material | PrimeNG | Ant Design for Angular
Testing:           Jasmine + Karma (default) | Jest | Playwright/Cypress (E2E)
Meta-frameworks:   Analog (SSR, file routing — Angular's Next.js equivalent)
Build:             Angular CLI + esbuild (17+) | Nx (monorepo)
```

Signals roadmap per the courseware: introduced in v16 (preview) → stable + `@if`/`@for`/`@defer` in v17 → `input()`/`toSignal()` in v18 → zoneless change detection (experimental) in v19 → Signals + standalone as the default pattern in v21 (the course's version) → full zoneless in the future, with NgModule and Zone.js on a long deprecation path.

---

## 15. Real Code Walkthrough — Honest Gap Statement (Case B)

**No genuine Angular code exists in the course repo's `Code/Angular` folder.** It was checked directly before writing any part of this guide, per the verification steps below.

### 15.1 What was checked

1. `grep -l "@Component\|@NgModule\|@Injectable\|from '@angular" -r "Code/Angular/src"` — returned no matches. Not a single file under `src/` contains an Angular decorator or an `@angular/...` import.
2. `Code/Angular/package.json` was read directly:

```json
{
  "name": "acme-react-demo",
  "dependencies": {
    "react": "^19.2.7",
    "react-dom": "^19.2.7",
    "axios": "1.18.1",
    "react-router": "8.1.0",
    "@reduxjs/toolkit": "2.12.0",
    "react-redux": "9.3.0"
  }
}
```

There is no `@angular/core`, `@angular/cli`, `@angular/router`, or any `@angular/*` package anywhere in `dependencies` or `devDependencies`. The build tool is `vite` (`"dev": "vite"`, `"build": "tsc -b && vite build"`), not the Angular CLI.

3. A directory listing of `Code/Angular/src/` confirms the file shapes are pure React/Vite conventions, not Angular's: `App.tsx`, `main.tsx`, `index.css`, `context/AuthProvider.tsx`, `context/AuthContextType.tsx`, `pages/Login.tsx`, `pages/Register.tsx`, `pages/Home.tsx`, `pages/EmployeeList.tsx`, `pages/EmployeeDetails.tsx`, `redux/store.ts`, `redux/empSlice.tsx`, `routes/appRoutes.tsx`, `services/api.service.ts`, `services/employee.service.ts`, `services/user.service.ts`, `models/employee.model.ts`. There is no `app.component.ts`, no `app.module.ts`, no `*.component.html`, no `angular.json` — none of the Angular-specific file conventions this course's Module 00 describes as the generated project shape.

### 15.2 What this folder actually is

This is the **same React + Redux Toolkit + React Router + Vite + Axios project** ("acme-react-demo") that appears — correctly labelled — elsewhere in the repo under the React course. Under `Code/Angular/`, it is simply mis-filed: same `package.json` name, same dependency set, same file layout (`context/`, `redux/`, `routes/`, `pages/`), same Jest + Testing Library test setup (`Login.test.tsx`, `AuthProvider.test.tsx`, `user.service.test.ts`). It happens to model a similar domain (employees, login/register, an API service layer) to the courseware's EMS example, which is likely why it was mistaken for or copied into the Angular folder — but structurally and technically it is 100% React, not Angular. No Angular code was fabricated to fill this gap.

### 15.3 Minimal illustrative snippets (NOT from the course repo)

These three snippets exist only to make the syntax concrete for the assessment. They are original, minimal illustrations — not reproductions of anything found in `Code/Angular` or any other file in the repo.

**Illustrative example (not from course repo) — a basic `@Component`:**

```typescript
1:  import { Component, signal } from '@angular/core'
2:
3:  @Component({
4:    selector: 'app-hello',
5:    standalone: true,
6:    template: `<h1>Hello, {{ name() }}</h1>`,
7:  })
8:  export class HelloComponent {
9:    name = signal('Aakash')
10: }
```

- **Line 1** — imports the `Component` decorator factory and the `signal()` function from Angular's core package.
- **Line 3–7** — `@Component(...)` is a **class decorator**: it attaches metadata Angular's compiler reads to turn a plain TypeScript class into a UI component. `selector: 'app-hello'` is the custom HTML tag other templates use to place this component (`<app-hello>`); there is no equivalent decorator step in React, where a component is just a function you import and call as JSX.
- **Line 5** — `standalone: true` means this component declares its own dependencies (via an `imports` array, omitted here since none are needed) instead of being registered inside an `@NgModule`.
- **Line 6** — `template` is an inline template string (equivalent to `templateUrl` pointing at a separate `.html` file); `{{ name() }}` is **interpolation**, calling the `name` signal to read its current value.
- **Line 9** — `name = signal('Aakash')` creates a writable reactive value. Unlike a plain class field, reading it inside a template (`name()`) registers that template as a dependent — Angular re-renders only this component when `name` changes.

**Illustrative example (not from course repo) — a service with dependency injection:**

```typescript
1:  import { Injectable, signal } from '@angular/core'
2:
3:  @Injectable({ providedIn: 'root' })
4:  export class CounterService {
5:    private _count = signal(0)
6:    readonly count = this._count.asReadonly()
7:
8:    increment(): void {
9:      this._count.update(n => n + 1)
10:   }
11: }
12:
13: // In a component:
14: // private counter = inject(CounterService)
15: // this.counter.increment()
```

- **Line 3** — `@Injectable({ providedIn: 'root' })` marks this class as available for Dependency Injection and tells Angular's injector to create exactly **one instance for the entire application** (a singleton) — the DI-container equivalent of a React Context provider or a Redux store, but wired automatically without wrapping any component tree.
- **Line 5–6** — the private-signal-plus-public-`asReadonly()` pattern: external code can read `count()` but cannot call `.set()`/`.update()` on it directly — all mutation must go through the service's own methods (`increment()`), keeping state changes centralized and predictable.
- **Line 9** — `.update(n => n + 1)` derives the new value from the current one, as opposed to `.set(value)` which replaces it outright.
- **Line 14** — `inject(CounterService)` is the modern (Angular 14+) way to retrieve the singleton instance inside another class's field initializer, without needing a constructor parameter.

**Illustrative example (not from course repo) — a template with `*ngIf`/`*ngFor`:**

```html
1:  <ul *ngIf="items.length > 0; else empty">
2:    <li *ngFor="let item of items; let i = index">
3:      {{ i + 1 }}. {{ item.name }}
4:    </li>
5:  </ul>
6:  <ng-template #empty>
7:    <p>No items.</p>
8:  </ng-template>
```

- **Line 1** — `*ngIf="items.length > 0; else empty"` is a **structural directive**: the `*` prefix is sugar that Angular expands to `<ng-template [ngIf]="...">` wrapping the `<ul>`. If the condition is false, the `<ul>` is never added to the DOM at all, and Angular renders the `#empty` template reference instead (declared on line 6).
- **Line 2** — `*ngFor="let item of items; let i = index"` repeats the `<li>` once per array element, exposing the current item as `item` and its position as `i` via Angular's built-in `index` local variable.
- **Line 3** — `{{ i + 1 }}. {{ item.name }}` — two interpolations in one text node; each is evaluated independently against the component class.
- **Line 6–8** — `<ng-template #empty>` defines a named template block that renders nothing by itself; it only appears when explicitly referenced (as it is by `else empty` on line 1). This is Angular's mechanism for defining alternate/fallback template content, unlike JSX's plain `{condition ? <A/> : <B/>}` ternary.

(Modern Angular 17+ code would prefer `@if`/`@for` block syntax over `*ngIf`/`*ngFor` — see Section 5.4 — but `*ngIf`/`*ngFor` remain common in existing/legacy Angular code and are directly assessed, so both are worth knowing.)

---

## Summary — What to Review Before the Assessment

- All fourteen modules (00–13) and the 80-question MCQ bank (Module 14) are genuine course content, reproduced and cross-referenced above.
- The heaviest-weighted assessment areas by MCQ count: Data Binding & Signals (12 Qs), Components & Directives (12 Qs), Routing (10 Qs), Observables/Forms/HTTP (15 Qs), Authentication/Performance/Testing (15 Qs).
- The `Code/Angular` folder in this repo is React, not Angular — do not reference it as a source of Angular examples; use the courseware snippets above instead.
