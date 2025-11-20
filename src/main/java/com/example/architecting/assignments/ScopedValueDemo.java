package com.example.architecting.assignments;

public class ScopedValueDemo {

    // TODO: Define public static final ScopedValue<String> REQUEST_ID

    public static void main(String[] args) {
        // TODO: Start the chain with a bound ScopedValue
    }

    public void handleRequest() {
        // TODO: Bind value and call serviceA
    }

    public void serviceA() {
        repositoryB();
    }

    public void repositoryB() {
        logOperation();
    }

    public void logOperation() {
        // TODO: Read ScopedValue and print it
    }
}
