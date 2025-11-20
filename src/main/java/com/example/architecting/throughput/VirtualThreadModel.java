package com.example.architecting.throughput;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simulates throughput using Virtual Threads.
 * This model should demonstrate significantly higher throughput for I/O bound
 * tasks
 * as it is not limited by OS thread count.
 */
public class VirtualThreadModel implements TaskSimulator {

    @Override
    public SimulationResult run(int taskCount, Duration ioDuration, Duration cpuDuration) {
        System.out.println("Starting Virtual Thread simulation...");

        // In Java 25, this uses the ForkJoinPool as the scheduler.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
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
                    // In Virtual Threads, this unmounts the thread, releasing the carrier.
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
                // Submit all tasks.
                // Note: For very large taskCount, creating the list might be the bottleneck,
                // but for < 1 million it's usually fine.
                List<Future<Void>> futures = executor.invokeAll(tasks);
                for (Future<Void> f : futures) {
                    f.get();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            long end = System.currentTimeMillis();
            long totalTime = end - start;
            double throughput = (double) taskCount / (totalTime / 1000.0);

            return new SimulationResult("Virtual Threads (Java 25)", taskCount, totalTime, throughput);
        }
    }
}
