# Java 25 Virtual Thread Assignments

These assignments are designed to help you master **High Concurrency** and **I/O Bound** architectures using Java 25's Virtual Threads.

## Prerequisites
- Java 25 (or Java 21+ with `--enable-preview` for some features if not fully finalized in your build).
- Understanding of `docs/THREADING_STRATEGY.md`.

---

## Assignment 1: The 10k Concurrent Sleepers
**Goal**: Demonstrate the massive scalability of Virtual Threads compared to Platform Threads.

### Requirements
1.  Create a class `VirtualThreadScaleTest` in `com.example.architecting.assignments`.
2.  Implement a method `runVirtualThreadTest(int taskCount)` that:
    - Uses `Executors.newVirtualThreadPerTaskExecutor()`.
    - Submits `taskCount` (e.g., 10,000 or 100,000) tasks.
    - Each task should simply sleep for 1 second (`Thread.sleep(1000)`).
    - Wait for all tasks to complete.
3.  Measure the total wall-clock time. It should be slightly over 1 second, regardless of the task count (up to a reasonable limit like 1M).
4.  **Challenge**: Try doing this with `Executors.newCachedThreadPool()` (Platform Threads) and observe the difference (or `OutOfMemoryError`).

### Key Concept
Virtual threads are cheap. You can have millions of them. They "unmount" from the carrier thread when blocking (sleeping), allowing the carrier to do other work.

---

## Assignment 2: Structured Concurrency (Fan-Out / Fan-In)
**Goal**: Coordinate multiple parallel I/O tasks safely using `StructuredTaskScope`.

### Requirements
1.  Create a class `StructuredConcurrencyDemo` in `com.example.architecting.assignments`.
2.  Simulate three I/O methods that return different data types (e.g., `fetchUser(id)`, `fetchOrders(id)`, `fetchSettings(id)`). Make them sleep for random durations (e.g., 100-500ms).
3.  Implement a method `buildUserDashboard(String userId)` that:
    - Opens a `StructuredTaskScope.ShutdownOnFailure()`.
    - Forks all three tasks.
    - Joins and throws if any failed.
    - Returns a combined result (e.g., a `Dashboard` record).
4.  **Error Handling**: Modify one task to throw an exception. Verify that the other tasks are cancelled (interrupted) immediately.

### Key Concept
`StructuredTaskScope` treats multiple threads as a single unit of work. If one fails, the scope shuts down, and siblings are cancelled, preventing "thread leaks".

---

## Assignment 3: Context Propagation with Scoped Values
**Goal**: Pass request-scoped data (like a Request ID or User Principal) deeply into the call stack without polluting method signatures.

### Requirements
1.  Create a class `ScopedValueDemo` in `com.example.architecting.assignments`.
2.  Define a `public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();`.
3.  Create a deep call hierarchy: `handleRequest()` -> `serviceA()` -> `repositoryB()` -> `logOperation()`.
4.  In `handleRequest()`, use `ScopedValue.where(REQUEST_ID, "req-123").run(() -> serviceA());`.
5.  In `logOperation()`, read the value using `REQUEST_ID.get()` and print it.
6.  Verify that `REQUEST_ID.get()` throws or is empty if called outside the scope.

### Key Concept
`ScopedValue` is a modern, immutable, and efficient alternative to `ThreadLocal`. It is designed for millions of virtual threads where `ThreadLocal` maps would consume too much memory.

---

## Assignment 4: The "Synchronized" Pinning Test (Java 25 Specific)
**Goal**: Verify that `synchronized` blocks no longer pin virtual threads in Java 25.

### Requirements
1.  Create a class `PinningTest` in `com.example.architecting.assignments`.
2.  Create a method that sleeps inside a `synchronized` block.
3.  Launch many of these tasks on Virtual Threads.
4.  If they run concurrently (finish in ~1s total), pinning is solved.
5.  If they run sequentially (finish in `N` seconds), pinning is occurring (which would happen in Java 20/19, but should be fixed in your Java 25 environment).

```java
synchronized(lock) {
    Thread.sleep(1000); // Should unmount in Java 25
}
```

---
