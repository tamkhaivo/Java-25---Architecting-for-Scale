package com.example.architecting.cpu;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Demonstrates the "Offloading" pattern for mixed workloads (I/O + CPU).
 * 
 * Scenario:
 * 1. Receive Request (I/O)
 * 2. Process Data (CPU)
 * 3. Send Response (I/O)
 */
public class MixedWorkloadDemo {

    // Dedicated pool for CPU intensive parts
    private static final ExecutorService CPU_POOL = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int taskCount = 1000;
        Duration ioDuration = Duration.ofMillis(10);
        int fibNumber = 20; // Light CPU work, but enough to matter if multiplied

        System.out.println("==================================================");
        System.out.println("      Mixed Workload Comparison (Offloading)      ");
        System.out.println("==================================================");
        System.out.println("Task: 10ms I/O -> Fib(" + fibNumber + ") -> 10ms I/O");
        System.out.println("Task Count: " + taskCount);
        System.out.println("==================================================");

        // 1. Pure Virtual Threads (Everything on VT)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runDemo("Pure Virtual Threads", executor, taskCount, () -> {
                simulateIO(ioDuration);
                fibonacci(fibNumber); // CPU work on VT (Carrier)
                simulateIO(ioDuration);
                return null;
            });
        }

        // 2. Offloading Pattern (VT for I/O, Platform for CPU)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runDemo("Offloading Pattern", executor, taskCount, () -> {
                simulateIO(ioDuration);

                // Offload CPU work to dedicated pool
                Future<Long> cpuResult = CPU_POOL.submit(() -> fibonacci(fibNumber));
                cpuResult.get(); // Await result (blocks VT, not Carrier)

                simulateIO(ioDuration);
                return null;
            });
        }

        CPU_POOL.shutdown();
    }

    private static void runDemo(String name, ExecutorService executor, int taskCount, Callable<Void> task)
            throws InterruptedException, ExecutionException {
        System.out.println("\nStarting " + name + "...");

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(task);
        }

        Instant start = Instant.now();
        List<Future<Void>> futures = executor.invokeAll(tasks);

        for (Future<Void> f : futures) {
            f.get();
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // Calculate throughput
        double seconds = duration.toMillis() / 1000.0;
        double throughput = taskCount / seconds;

        System.out.printf("[%s] Time: %d ms, Throughput: %.2f tasks/sec%n", name, duration.toMillis(), throughput);
    }

    private static void simulateIO(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static long fibonacci(int n) {
        if (n <= 1)
            return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
