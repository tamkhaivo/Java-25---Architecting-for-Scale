package com.example.architecting.throughput;

import java.time.Duration;

/**
 * Main entry point for the Throughput Demonstration.
 * Compares Platform Threads, Virtual Threads, and different Locking Strategies.
 */
public class ThroughputDemo {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      Java 25 Throughput & Threading Model Demo   ");
        System.out.println("==================================================");

        // Configuration
        // We use a relatively high task count to show the difference,
        // but low enough to run quickly for a demo.
        int taskCount = 10_000;
        Duration ioDuration = Duration.ofMillis(10); // 10ms latency
        Duration cpuDuration = Duration.ZERO; // Pure I/O bound for this test

        System.out.println("Configuration:");
        System.out.println("  Tasks: " + taskCount);
        System.out.println("  I/O Duration: " + ioDuration.toMillis() + "ms");
        System.out.println("  CPU Duration: " + cpuDuration.toMillis() + "ms");
        System.out.println("==================================================\n");

        // 1. Baseline: Platform Threads
        TaskSimulator platformModel = new PlatformThreadModel();
        TaskSimulator.SimulationResult platformResult = platformModel.run(taskCount, ioDuration, cpuDuration);
        System.out.println(platformResult);
        System.out.println();

        // 2. Target: Virtual Threads (Java 25)
        TaskSimulator virtualModel = new VirtualThreadModel();
        TaskSimulator.SimulationResult virtualResult = virtualModel.run(taskCount, ioDuration, cpuDuration);
        System.out.println(virtualResult);
        System.out.println();

        // 3. Locking Strategy Comparison
        System.out.println("--- Locking Strategy Comparison ---");
        SynchronizationDemo syncDemo = new SynchronizationDemo();

        // 3a. Platform + Synchronized
        // We reduce task count for platform threads to avoid exhaustion if we were to
        // scale up,
        // but for 10k it's fine.
        var syncPlatform = syncDemo.runPlatformSynchronized(taskCount, ioDuration);
        System.out.println(syncPlatform);

        // 3b. Virtual + Synchronized (Java 25 - Unmounts)
        var syncVirtual = syncDemo.runVirtualSynchronized(taskCount, ioDuration);
        System.out.println(syncVirtual);

        // 3c. Virtual + ReentrantLock (Always Unmounts)
        var lockVirtual = syncDemo.runVirtualReentrantLock(taskCount, ioDuration);
        System.out.println(lockVirtual);

        // 3d. Virtual + Pinned Simulation (Java 21 Behavior)
        var pinnedVirtual = syncDemo.runVirtualPinnedSimulation(taskCount, ioDuration);
        System.out.println(pinnedVirtual);

        System.out.println("\n==================================================");
        System.out.println("Summary of Results (Tasks/Sec):");
        System.out.printf("%-40s: %.2f\n", platformResult.modelName(), platformResult.throughputPerSecond());
        System.out.printf("%-40s: %.2f\n", virtualResult.modelName(), virtualResult.throughputPerSecond());
        System.out.printf("%-40s: %.2f\n", syncPlatform.modelName(), syncPlatform.throughputPerSecond());
        System.out.printf("%-40s: %.2f\n", syncVirtual.modelName(), syncVirtual.throughputPerSecond());
        System.out.printf("%-40s: %.2f\n", pinnedVirtual.modelName(), pinnedVirtual.throughputPerSecond());
        System.out.println("==================================================");
    }
}
