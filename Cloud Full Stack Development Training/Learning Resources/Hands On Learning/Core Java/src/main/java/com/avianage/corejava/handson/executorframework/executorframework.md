# Module 24 — Executor Framework (Condensed)

> Part F: Concurrency · Prerequisites: Module 23

## Why Not Raw Threads?
Manual `Thread` management doesn't scale: threads cost ~512KB stack each, no built-in limit (10,000 tasks = 10,000 threads = OOM), no easy result retrieval, no clean exception propagation, no scheduling/cancellation/timeouts.

**Executor Framework** (`java.util.concurrent`) separates the **task** from the **execution policy**.

```
Executor                 → submit, forget about threads
ExecutorService           → + lifecycle management + result retrieval
ScheduledExecutorService  → + scheduled/recurring execution
```
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(() -> processPayroll());
```

---

## 1. Thread Pool Types (`Executors` factory)

| Type | Behavior | Use For |
|---|---|---|
| `newFixedThreadPool(n)` | Fixed n threads, shared queue, tasks wait | Known, bounded workloads |
| `newCachedThreadPool()` | Creates threads as needed, reuses idle (60s), can grow large | Many short-lived tasks |
| `newSingleThreadExecutor()` | One thread, sequential order | Tasks that must not run concurrently |
| `newScheduledThreadPool(n)` | For delayed/periodic tasks | Scheduling |

---

## 2. `execute()` vs `submit()`

| Method | Returns | Exceptions | Use For |
|---|---|---|---|
| `execute(Runnable)` | void | Goes to UncaughtExceptionHandler | Fire-and-forget |
| `submit(Runnable)` | `Future<?>` | Stored in Future | Trackable/cancellable tasks |
| `submit(Callable<T>)` | `Future<T>` | Stored in Future | Tasks returning a result |

---

## 3. `Callable<T>` + `Future<T>`
`Runnable` can't return a result or throw checked exceptions — `Callable<T>` can.

```java
public class SalaryCalculationTask implements Callable<Double> {
    @Override public Double call() throws Exception {
        Thread.sleep(300);
        return employees.stream().filter(e -> e.getDepartment().equals(department))
                         .mapToDouble(Employee::getSalary).sum();
    }
}
```
```java
ExecutorService pool = Executors.newFixedThreadPool(3);
Future<Double> engFuture = pool.submit(new SalaryCalculationTask(all, "Engineering"));
Future<Double> hrFuture  = pool.submit(new SalaryCalculationTask(all, "HR"));

try {
    double total = engFuture.get() + hrFuture.get();   // blocks until each ready
} catch (ExecutionException e) {
    System.out.println("Task failed: " + e.getCause().getMessage());
} finally {
    pool.shutdown();
}
```

**`Future` methods:**
```java
future.get();                         // block until ready
future.get(5, TimeUnit.SECONDS);       // block with timeout — TimeoutException if exceeded
future.isDone(); future.isCancelled();
future.cancel(true);                    // true = interrupt if running
```

---

## 4. Shutting Down
Always shut down or the JVM won't exit.
```java
pool.shutdown();      // stop accepting new tasks, finish running ones
pool.shutdownNow();   // stop accepting, interrupt running (best-effort)
boolean finished = pool.awaitTermination(10, TimeUnit.SECONDS);
```
**Clean pattern:** submit + get results in `try`, `pool.shutdown()` in `finally`.

---

## 5. `invokeAll()` / `invokeAny()`

```java
List<Callable<Double>> tasks = List.of(task1, task2, task3, task4);

List<Future<Double>> futures = pool.invokeAll(tasks);   // submits all, blocks until ALL done
double total = 0;
for (Future<Double> f : futures) total += f.get();

Double fastest = pool.invokeAny(tasks);   // returns FIRST result, cancels the rest
```

---

## 6. `ScheduledExecutorService`
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

scheduler.schedule(() -> send(), 5, TimeUnit.SECONDS);            // run once, after delay

scheduler.scheduleAtFixedRate(() -> heartbeat(), 0, 10, TimeUnit.SECONDS);   // fixed rate — time between STARTS

scheduler.scheduleWithFixedDelay(() -> report(), 2, 30, TimeUnit.SECONDS);  // fixed delay — time between END and next START
```

---

## 7. `CompletableFuture` — Composable Async Pipelines (Java 8)

```java
CompletableFuture<Void> task = CompletableFuture.runAsync(() -> System.out.println("started"));
CompletableFuture<Double> salaryFuture = CompletableFuture.supplyAsync(() -> calculateTotalSalary("Engineering"));

CompletableFuture<String> report = salaryFuture.thenApply(total -> String.format("Payroll: %.2f", total));
CompletableFuture<String> emailFuture = report.thenApplyAsync(msg -> sendEmail("mgr@ems.com", msg));
System.out.println(emailFuture.get());
```

**Chaining:**
```java
CompletableFuture.supplyAsync(() -> loadEmployees("Engineering"))
    .thenApply(emps -> emps.stream().filter(e -> e.getSalary() > 70000).collect(Collectors.toList()))
    .thenAccept(list -> list.forEach(e -> System.out.println(e.getName())))
    .join();   // wait — doesn't throw checked exceptions
```

**Combining two futures:**
```java
CompletableFuture<Double> eng = CompletableFuture.supplyAsync(() -> calc("Engineering"));
CompletableFuture<Double> fin = CompletableFuture.supplyAsync(() -> calc("Finance"));
CompletableFuture<Double> combined = eng.thenCombine(fin, (e, f) -> e + f);
```

**Error handling:**
```java
CompletableFuture.supplyAsync(() -> { if (fails) throw new RuntimeException("DB down"); return emp; })
    .exceptionally(ex -> defaultEmployee());   // fallback on failure

CompletableFuture.supplyAsync(() -> loadEmployee(101))
    .whenComplete((emp, ex) -> {                // runs on both success and failure
        if (ex != null) System.out.println("Failed: " + ex.getMessage());
        else System.out.println("Loaded: " + emp.getName());
    });
```

---

## 8. Concurrent Collections

| Class | Use Case |
|---|---|
| `ConcurrentHashMap` | Thread-safe HashMap, high concurrency |
| `CopyOnWriteArrayList` | Lock-free reads, writes copy the whole list |
| `ConcurrentLinkedQueue` | Lock-free thread-safe queue |
| `BlockingQueue` | Producer-consumer queue, blocks on put/take |
| `ArrayBlockingQueue` | Bounded blocking queue |
| `LinkedBlockingQueue` | Unbounded blocking queue |

```java
ConcurrentHashMap<String, Double> deptPayroll = new ConcurrentHashMap<>();
// multiple threads can safely put/read simultaneously
```

**Producer-Consumer with `BlockingQueue`:**
```java
BlockingQueue<Employee> queue = new LinkedBlockingQueue<>(10);

Thread producer = new Thread(() -> { queue.put(emp); });   // blocks if full
Thread consumer = new Thread(() -> { Employee e = queue.take(); });  // blocks if empty
```

---

## 9. Practical Pattern
```java
ExecutorService pool = Executors.newFixedThreadPool(4);
List<Callable<Double>> tasks = new ArrayList<>();
for (String dept : departments) tasks.add(() -> generateReport(dept, all));

List<Future<Double>> futures = pool.invokeAll(tasks);      // run in parallel
double grandTotal = 0;
for (Future<Double> f : futures) grandTotal += f.get();
pool.shutdown();
```
4 tasks × ~400ms each in parallel ≈ 415ms total, vs ~1600ms sequential.

---

## Quick Reference

| Concept | Key Point |
|---|---|
| `ExecutorService` | Manages a thread pool — don't hand-roll raw threads in production |
| `newFixedThreadPool(n)` | Bounded, known workloads |
| `newCachedThreadPool()` | Grows as needed, many short tasks |
| `Callable<T>` | Returns a result, can throw checked exceptions |
| `Future<T>` | Async result handle, `get()` blocks |
| `invokeAll()` | All tasks, wait for all |
| `invokeAny()` | All tasks, return first result |
| `ScheduledExecutorService` | Delayed/periodic execution |
| `CompletableFuture` | Composable async pipelines — chain, combine, handle errors |
| `ConcurrentHashMap` | Thread-safe map replacement for `HashMap` |
| `BlockingQueue` | Thread-safe producer-consumer queue |
| `shutdown()` | Always call — prevents thread leaks |

## Next: Part G — I/O and Collections. Module 25: IO Streams