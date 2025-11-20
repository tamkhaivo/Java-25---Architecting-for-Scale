# Strategic Threading Framework: First Principles & Decision Matrix

## 1. Core Philosophy
The goal of this framework is to maximize **System Throughput** while maintaining **High Observability**. We derive our decisions from first principles of computer science and the specific characteristics of the Java 25 runtime.

### Principle A: Little's Law ($\lambda = L / W$)
*   **Throughput ($\lambda$)**: Requests per second.
*   **Concurrency ($L$)**: Number of requests in flight.
*   **Latency ($W$)**: Time to process one request.

For I/O-bound applications, $W$ is dominated by waiting (network/disk). To increase $\lambda$, we must increase $L$.
*   **Platform Threads**: $L$ is limited by OS memory (~4,000 threads max). Throughput is capped.
*   **Virtual Threads**: $L$ is limited by Heap memory (~1,000,000+ threads). Throughput is maximized.

### Principle B: Resource Scarcity
*   **OS Threads**: Expensive. Heavy context switching (kernel mode). Large stack (2MB).
*   **Virtual Threads**: Cheap. User-mode scheduling. Resizable stack.

### Principle C: Observability & Debuggability
*   **Platform Threads**: 1:1 mapping to OS threads. Easy to trace with standard tools.
*   **Virtual Threads**: M:N mapping. require **Structured Concurrency** to maintain logical grouping and **Scoped Values** to propagate context without the memory overhead of `ThreadLocal`.

---

## 2. Decision Framework

### The "Pinning" Constraint
In Java 21+, `synchronized` blocks pinned the virtual thread to the carrier, blocking the OS thread.
**Java 25 Update**: `synchronized` now unmounts! Pinning is largely solved for Java monitors.
*   **Remaining Risk**: Native code (JNI) or legacy libraries that rely on thread identity or native locks can still pin.

### Selection Logic
1.  **CPU Intensive?** (e.g., Image processing, Crypto)
    *   **Use Platform Threads** (specifically `ForkJoinPool`).
    *   *Why?* Virtual threads add scheduling overhead with no benefit if the CPU is busy.
2.  **I/O Intensive?** (e.g., DB calls, REST APIs)
    *   **Use Virtual Threads**.
    *   *Why?* Maximizes $L$ in Little's Law.
3.  **Mixed Workload?**
    *   Default to **Virtual Threads** for the request handling.
    *   Offload CPU heavy parts to a dedicated Platform Thread pool.

---

## 3. Decision Flowchart

```mermaid
flowchart TD
    Start([New Component / Feature]) --> Type{Workload Type?}
    
    Type -- CPU Bound --> Platform[Platform Threads\n(ForkJoinPool)]
    Type -- I/O Bound --> Legacy{Uses Native/JNI\nLocks?}
    
    Legacy -- Yes --> PinRisk{Duration of Lock?}
    PinRisk -- Long --> Platform
    PinRisk -- Short --> Virtual[Virtual Threads]
    
    Legacy -- No --> Virtual
    
    subgraph Observability Strategy
    Virtual --> Struct{Complex Logic?}
    Struct -- Yes --> SC[Use StructuredTaskScope]
    Struct -- No --> Simple[Simple VThread]
    end
    
    Platform --> Standard[Standard Profiling]
```

## 4. Observability & Context Propagation

To maintain observability with millions of threads:

1.  **Avoid `ThreadLocal`**: It creates a map per thread. With 1M threads, this explodes heap usage.
2.  **Use `ScopedValue` (JEP 481)**: Immutable, shared data. Efficient for high-concurrency.
3.  **Structured Concurrency (JEP 480)**:
    *   Use `StructuredTaskScope` instead of `ExecutorService` when possible.
    *   Ensures errors propagate up.
    *   Ensures orphan threads are cancelled.
    *   Makes thread dumps readable (shows hierarchy).

## 5. Summary Table

| Feature | Platform Threads | Virtual Threads |
| :--- | :--- | :--- |
| **Best For** | CPU-bound, Long-running native locks | I/O-bound, High concurrency |
| **Cost** | High (OS Resource) | Low (Heap Object) |
| **Context** | `ThreadLocal` | `ScopedValue` |
| **Coordination** | `CompletableFuture` / `ExecutorService` | `StructuredTaskScope` |
| **Blocking** | Blocks OS Thread | Unmounts (Yields OS Thread) |

```java
// Recommended Pattern for Java 25
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var userTask = scope.fork(() -> fetchUser(id));
    var orderTask = scope.fork(() -> fetchOrders(id));

    scope.join().throwIfFailed();

    return new Response(userTask.get(), orderTask.get());
}
```
