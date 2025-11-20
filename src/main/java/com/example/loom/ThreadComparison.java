package com.example.loom;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

public class ThreadComparison
{

    private static final int VIRTUAL_TASKS = 100_000;
    private static final int PLATFORM_TASKS = 1_000; // Reduced for platform threads to avoid OutOfMemoryError

    public static void main(String[] args)
    {
        System.out.println("Starting platform thread test with " + PLATFORM_TASKS + " tasks...");
        runWithPlatformThreads();
        System.out.println("Platform thread test finished.");

        System.out.println("\nStarting virtual thread test with " + VIRTUAL_TASKS + " tasks...");
        runWithVirtualThreads();
        System.out.println("Virtual thread test finished.");
    }

    private static void runWithVirtualThreads()
    {
        Instant start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor())
        {
            IntStream.range(0, VIRTUAL_TASKS).forEach(i ->
            {
                executor.submit(() ->
                {
                    try
                    {
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                    return i;
                });
            });
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Virtual threads took: " + timeElapsed + "ms");
    }

    private static void runWithPlatformThreads()
    {
        Instant start = Instant.now();
        // A ThreadFactory that creates platform threads
        ThreadFactory platformThreadFactory = Thread.ofPlatform().factory();
        try (var executor = Executors.newThreadPerTaskExecutor(platformThreadFactory))
        {
            IntStream.range(0, PLATFORM_TASKS).forEach(i ->
            {
                executor.submit(() ->
                {
                    try
                    {
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                    return i;
                });
            });
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Platform threads took: " + timeElapsed + "ms");
    }
}