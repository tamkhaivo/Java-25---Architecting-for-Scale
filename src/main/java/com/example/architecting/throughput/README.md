# Throughput and Threading Models in Java 25

This project demonstrates the concepts of throughput by comparing different threading models and locking strategies.

## Key Concepts

### 1. Platform Threads (Legacy)
- **OS Relationship**: 1:1 Mapping. Each Java thread maps directly to an OS thread.
- **Limitation**: OS threads are heavy (~1MB stack) and expensive to switch (kernel mode). The "Throughput Limit" is the number of OS threads the system can handle (typically a few thousand).
- **Behavior**: Blocking I/O blocks the underlying OS thread.

### 2. Virtual Threads (Java 21+)
- **OS Relationship**: M:N Mapping. Many virtual threads map to a few "carrier" OS threads.
- **Advantage**: Lightweight (~1KB stack), fast switching (user mode).
- **Behavior**: Blocking I/O "unmounts" the virtual thread, freeing the carrier to do other work.

### 3. The "Pinning" Problem (Java 21 vs Java 25)
- **Java 21**: Using `synchronized` inside a virtual thread "pinned" it to the carrier. If the code blocked while pinned, the carrier was blocked, killing throughput.
- **Java 25**: `synchronized` now unmounts properly (JEP 491). The limit shifts from CPU/OS to Heap/External Resources.

## Running the Demo

Run the `ThroughputDemo` class to see the comparison in action.

```bash
# Create output directory
mkdir -p target/classes

# Compile
javac -d target/classes src/main/java/com/example/architecting/throughput/*.java

# Run
java -cp target/classes com.example.architecting.throughput.ThroughputDemo
```

## Expected Results

1.  **Platform Threads**: Lower throughput. Limited by the thread pool size (e.g., 200).
2.  **Virtual Threads**: Extremely high throughput. Limited only by how fast tasks can be submitted and processed.
3.  **Virtual + Synchronized (Java 25)**: High throughput. Similar to ReentrantLock.
4.  **Virtual + Pinned (Simulated)**: Low throughput. Simulates the Java 21 behavior where concurrency is limited to the number of CPU cores.
