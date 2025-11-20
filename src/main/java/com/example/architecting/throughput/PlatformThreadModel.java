package com.example.architecting.throughput;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simulates throughput using Platform Threads (OS threads).
 * This model is limited by the number of OS threads the system can handle
 * efficiently.
 */
public class PlatformThreadModel implements TaskSimulator {

    @Override
    public SimulationResult run(int taskCount, Duration ioDuration, Duration cpuDuration) {
        // Use a cached thread pool to simulate "one thread per task" behavior,
        // but realistically this will hit OS limits or become very slow with high task
        // counts.
        // For a fairer comparison to a bounded server, we could use a fixed pool,
        // but cached pool better demonstrates the resource exhaustion or context
        // switching overhead
        // if we try to match virtual threads 1:1.
        // However, to avoid crashing the machine with 100k threads, we'll cap it or use
        // a large fixed pool.
        // Let's use a fixed pool of 200 threads to simulate a typical web server limit.
        int maxThreads = 200;

        System.out.println("Starting Platform Thread simulation with " + maxThreads + " threads...");

        try (ExecutorService executor = Executors.newFixedThreadPool(maxThreads)) {
            long start = System.currentTimeMillis();

            List<Callable<Void>> tasks = new ArrayList<>(taskCount);
            for (int i = 0; i < taskCount; i++) {
                tasks.add(() -> {
                    // Simulate CPU work
                    if (!cpuDuration.isZero()) {
                        long endCpu = System.nanoTime() + cpuDuration.toNanos();
                        while (System.nanoTime() < endCpu) {
                            // busy spin
                        }
                    }

                    // Simulate Blocking I/O
                    if (!ioDuration.isZero()) {
                        try {
                            Thread.sleep(ioDuration);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    return null;
                });
            }

            try {
                List<Future<Void>> futures = executor.invokeAll(tasks);
                // Wait for all to complete (invokeAll does this, but good to be explicit about
                // intent)
                for (Future<Void> f : futures) {
                    f.get();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            long end = System.currentTimeMillis();
            long totalTime = end - start;
            double throughput = (double) taskCount / (totalTime / 1000.0);

            return new SimulationResult("Platform Threads (Fixed Pool " + maxThreads + ")", taskCount, totalTime,
                    throughput);
        }
    }
}
