package com.example.architecting.assignments;

public class PinningTest {

    private final Object lock = new Object();

    public static void main(String[] args) {
        // TODO: Run the test
    }

    public void runPinningTest() {
        // TODO: Launch many virtual threads that sleep inside a synchronized block
        // synchronized(lock) { Thread.sleep(1000); }
        // Measure if they run in parallel (good) or serial (bad/pinned)
    }
}
