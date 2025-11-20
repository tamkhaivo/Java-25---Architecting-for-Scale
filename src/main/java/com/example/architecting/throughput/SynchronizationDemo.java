package com.example.architecting.throughput;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates the interaction between Threading Models and Locking Strategies.
 * Specifically highlights:
 * 1. Platform Threads + Synchronized (Blocks OS Thread)
 * 2. Virtual Threads + Synchronized (Java 25: Unmounts / Java 21: Pins)
 * 3. Virtual Threads + ReentrantLock (Unmounts)
 */
public class SynchronizationDemo {

    // Semaphore to simulate "Pinning" behavior by limiting concurrency to core
    // count
    private static final Semaphore CARRIER_LIMIT = new Semaphore(Runtime.getRuntime().availableProcessors());

    public TaskSimulator.SimulationResult runPlatformSynchronized(int taskCount, Duration ioDuration) {
        return runDemo("Platform + Synchronized", Executors.newFixedThreadPool(200), taskCount, ioDuration, () -> {
            // Simulate locking a resource specific to this task (e.g., user session)
            Object localLock = new Object();
            synchronized (localLock) {
                Thread.sleep(ioDuration);
            }
            return null;
        });
    }

    public TaskSimulator.SimulationResult runVirtualSynchronized(int taskCount, Duration ioDuration) {
        // In Java 25, this will UNMOUNT. In Java 21, this would PIN.
        return runDemo("Virtual + Synchronized (Java 25)", Executors.newVirtualThreadPerTaskExecutor(), taskCount,
                ioDuration, () -> {
                    Object localLock = new Object();
                    synchronized (localLock) {
                        Thread.sleep(ioDuration);
                    }
                    return null;
                });
    }

    public TaskSimulator.SimulationResult runVirtualReentrantLock(int taskCount, Duration ioDuration) {
        return runDemo("Virtual + ReentrantLock", Executors.newVirtualThreadPerTaskExecutor(), taskCount, ioDuration,
                () -> {
                    ReentrantLock localLock = new ReentrantLock();
                    localLock.lock();
                    try {
                        Thread.sleep(ioDuration);
                    } finally {
                        localLock.unlock();
                    }
                    return null;
                });
    }

    public TaskSimulator.SimulationResult runVirtualPinnedSimulation(int taskCount, Duration ioDuration) {
        // Simulates Java 21 behavior where synchronized pins the carrier thread.
        // We enforce a limit equal to processor count to mimic the scarcity of carrier
        // threads.
        return runDemo("Virtual + Pinned (Simulated Java 21)", Executors.newVirtualThreadPerTaskExecutor(), taskCount,
                ioDuration, () -> {
                    CARRIER_LIMIT.acquire();
                    try {
                        // Even if we are technically virtual, we are holding a "permit" that represents
                        // a carrier thread.
                        // If we sleep here, we are "blocking" this permit, preventing others from
                        // running.
                        Thread.sleep(ioDuration);
                    } finally {
                        CARRIER_LIMIT.release();
                    }
                    return null;
                });
    }

    private TaskSimulator.SimulationResult runDemo(String name, ExecutorService executor, int taskCount,
            Duration ioDuration,
            Callable<Void> task) {
        System.out.println("Starting " + name + "...");
        try (executor) {
            long start = System.currentTimeMillis();
            List<Callable<Void>> tasks = new ArrayList<>(taskCount);
            for (int i = 0; i < taskCount; i++) {
                tasks.add(task);
            }

            try {
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
            return new TaskSimulator.SimulationResult(name, taskCount, totalTime, throughput);
        }
    }
}
