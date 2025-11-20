package com.example.architecting.throughput;

import java.time.Duration;

/**
 * Interface for defining a throughput simulation task.
 */
public interface TaskSimulator {

    /**
     * Runs the simulation.
     *
     * @param taskCount   Number of tasks to submit.
     * @param ioDuration  Duration of simulated I/O blocking.
     * @param cpuDuration Duration of simulated CPU work.
     * @return The result of the simulation (e.g., total time taken).
     */
    SimulationResult run(int taskCount, Duration ioDuration, Duration cpuDuration);

    record SimulationResult(String modelName, int taskCount, long totalTimeMillis, double throughputPerSecond) {
        @Override
        public String toString() {
            return String.format("[%s] Tasks: %d, Time: %d ms, Throughput: %.2f tasks/sec",
                    modelName, taskCount, totalTimeMillis, throughputPerSecond);
        }
    }
}
