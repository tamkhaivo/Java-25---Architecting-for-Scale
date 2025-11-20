package com.example.architecting.cpu;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Demonstrates that Virtual Threads offer no advantage (and potentially slight
 * overhead)
 * for CPU-bound tasks compared to Platform Threads.
 */
public class CpuIntensiveDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int taskCount = 100; // Number of CPU heavy tasks

        System.out.println("==================================================");
        System.out.println("      CPU-Intensive Workload Comparison           ");
        System.out.println("==================================================");
        System.out.println("Task: Calculate Fibonacci(45) recursively");
        System.out.println("Task Count: " + taskCount);
        System.out.println("==================================================");

        // 1. Platform Threads (ForkJoinPool - optimized for CPU)
        runDemo("Platform Threads (ForkJoinPool)", ForkJoinPool.commonPool(), taskCount);

        // 2. Virtual Threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runDemo("Virtual Threads", executor, taskCount);
        }

        // 3. Platform Threads (Fixed Pool - standard)
        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            runDemo("Platform Threads (Fixed Pool)", executor, taskCount);
        }
    }

    private static void runDemo(String name, ExecutorService executor, int taskCount)
            throws InterruptedException, ExecutionException {
        System.out.println("\nStarting " + name + "...");

        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> fibonacci(40)); // Adjusted to 40 to be reasonable but heavy
        }

        Instant start = Instant.now();
        List<Future<Long>> futures = executor.invokeAll(tasks);

        // Ensure all finished
        for (Future<Long> f : futures) {
            f.get();
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        System.out.printf("[%s] Completed in %d ms%n", name, duration.toMillis());
    }

    // Naive recursive implementation to burn CPU
    private static long fibonacci(int n) {
        if (n <= 1)
            return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
