# Module 23 — Multithreading (Condensed)

> Part F: Concurrency · Prerequisites: Module 08–13, 17

## Core Idea
A **thread** is the smallest unit of execution. Every program starts with one (main) thread; multithreading runs several simultaneously. Single-core CPUs context-switch to fake parallelism; multi-core CPUs run threads truly in parallel.

## Thread Lifecycle
```
NEW → RUNNABLE → RUNNING → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED
```
| State | Meaning |
|---|---|
| `NEW` | Created, `start()` not called |
| `RUNNABLE` | Ready, waiting for CPU |
| `RUNNING` | Executing |
| `BLOCKED` | Waiting for a lock |
| `WAITING` | Indefinite wait (`notify()`/`join()`) |
| `TIMED_WAITING` | Timed wait (`sleep()`, `join(timeout)`) |
| `TERMINATED` | Done |

---

## 1. Creating Threads

**Extend `Thread`:**
```java
public class PayrollTask extends Thread {
    public PayrollTask(String dept) { super("Payroll-" + dept); }
    @Override public void run() { /* work */ }
}
new PayrollTask("Engineering").start();   // NOT .run() — run() skips new-thread creation
```

**Implement `Runnable` (preferred)** — separates task from thread, allows extending another class:
```java
public class SalaryAuditTask implements Runnable {
    @Override public void run() { /* work */ }
}
new Thread(new SalaryAuditTask("Engineering", all), "Audit-Eng").start();
```

**Lambda (most concise)** — `Runnable` is a functional interface:
```java
new Thread(() -> System.out.println("in: " + Thread.currentThread().getName()), "PayrollThread").start();
```

`start()` creates a new OS thread and calls `run()` on it; calling `run()` directly runs on the *current* thread — no concurrency.

---

## 2. Thread Methods

**`sleep(ms)`** — pause; throws checked `InterruptedException`:
```java
try { Thread.sleep(2000); }
catch (InterruptedException e) { Thread.currentThread().interrupt(); }  // restore flag
```

**`join()`** — caller waits for the thread to finish:
```java
t1.start(); t2.start();
t1.join(); t2.join();          // main blocks here until both done
t1.join(5000);                  // with timeout
```

**`interrupt()`** — signal a thread to stop; it must cooperate by checking `isInterrupted()`:
```java
Thread worker = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) { /* work */ }
});
worker.start();
worker.interrupt();
```

**`setDaemon(true)`** — background thread, auto-killed when all user threads finish. Must be set **before** `start()`.

**`setPriority(int)`** — hint only (`MIN_PRIORITY`=1, `NORM_PRIORITY`=5, `MAX_PRIORITY`=10); OS not obligated to honor it.

---

## 3. Race Conditions
Two threads modifying shared state simultaneously → unpredictable results.
```java
public void increment() { count++; }   // NOT atomic: read-increment-write
```
Two threads each looping 1000 times → expect 2000, get some smaller unpredictable number — increments overwrite each other.

---

## 4. `synchronized` — Locking

**Method-level:**
```java
public synchronized void increment() { count++; }   // one thread at a time
```

**Block-level (more precise — smaller critical section = more concurrency):**
```java
public void processSalary(double salary) {
    double net = salary * 0.90;              // no shared state — no lock needed
    synchronized (lock) {
        processedSalaries.add(net);           // shared state — locked
        totalPayroll += net;
    }
}
```
Guarantees: **mutual exclusion** (one thread at a time) + **visibility** (changes visible to subsequent lock-holders).

---

## 5. `volatile` — Visibility Only
Ensures reads/writes go to main memory, not thread-local cache. **Visibility, not atomicity** — good for simple flags, not counters.
```java
private volatile boolean running = true;
public void stop() { running = false; }   // other threads see this immediately
```
For compound ops (increment, etc.), use `synchronized` or atomic classes instead.

---

## 6. `Atomic` Classes
`java.util.concurrent.atomic` — thread-safe single-variable ops without explicit locking.
```java
private AtomicInteger count = new AtomicInteger(0);
public void increment() { count.incrementAndGet(); }
public boolean tryReset(int expected) { return count.compareAndSet(expected, 0); }
```
Two threads × 1000 increments each → always exactly 2000.

---

## 7. Deadlock
Two threads each hold a lock the other needs → both wait forever.
```java
// T1: synchronized(lockA) { synchronized(lockB) { ... } }
// T2: synchronized(lockB) { synchronized(lockA) { ... } }
// → deadlock if they run concurrently
```
**Prevention:** always acquire locks in the same order across threads; use `tryLock()` with timeout (`java.util.concurrent.locks`); minimize simultaneous locks held.

---

## 8. Practical Pattern — Parallel Processing
```java
Thread[] threads = new Thread[departments.length];
for (int i = 0; i < departments.length; i++) {
    String dept = departments[i];
    threads[i] = new Thread(() -> processDepartment(dept, all), "Worker-" + dept);
    threads[i].start();
}
for (Thread t : threads) t.join();   // wait for all before continuing
```
Shared accumulation still needs a lock:
```java
synchronized (printLock) { totalPayroll += deptTotal; }
```
4 tasks × ~500ms each in parallel ≈ 500ms total, vs ~2000ms sequential.

---

## 9. `ThreadLocal<T>`
Gives each thread its own independent copy of a variable — no sharing, no locking needed.
```java
private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
public static void setUser(String u) { currentUser.set(u); }
public static String getUser() { return currentUser.get(); }
public static void clear() { currentUser.remove(); }
```
Widely used in web frameworks for per-request context (current user, transaction ID) without threading it through every method call. Always `remove()` when done to avoid leaks (especially in thread pools).

---

## Quick Reference

| Concept | Key Point |
|---|---|
| `Thread` vs `Runnable` | Prefer `Runnable` — separates task from thread |
| `start()` vs `run()` | Always `start()` — `run()` skips new-thread creation |
| `sleep(ms)` | Pauses current thread; handle `InterruptedException` |
| `join()` | Wait for another thread to finish |
| `interrupt()` | Signals stop — thread must cooperate |
| `synchronized` | Mutual exclusion, one thread at a time |
| `volatile` | Visibility for simple flags — not atomic |
| `AtomicInteger` etc. | Lock-free thread-safe counters/references |
| Race condition | Unsynchronized shared-state mutation — unpredictable |
| Deadlock | Circular lock-waiting — threads stuck forever |
| Daemon thread | Background thread, killed on JVM exit |
| `ThreadLocal` | Per-thread variable, no sharing |

## Next: Module 24 — Executor Framework (thread pools, futures, scheduled execution)