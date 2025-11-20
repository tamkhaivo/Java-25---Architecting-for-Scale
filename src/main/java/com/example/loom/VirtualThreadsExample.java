package com.example.loom;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadsExample
{
    public static void main(String[] args)
    {
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor())
        {
            IntStream.range(0, 100_000).forEach(i ->
            {
                executor.submit(() ->
                {
                    Thread.sleep(1000);
                    System.out.println("Finished task " + i + " on thread " + Thread.currentThread());
                    return i;
                });
            });
        }

        long endTime = System.currentTimeMillis();
        System.out.println("All tasks finished in " + (endTime - startTime) + "ms");
    }
}
