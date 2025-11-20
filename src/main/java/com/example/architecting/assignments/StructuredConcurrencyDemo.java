package com.example.architecting.assignments;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class StructuredConcurrencyDemo {

    public static void main(String[] args) {
        // TODO: Call buildUserDashboard and print the result
    }

    public Dashboard buildUserDashboard(String userId) {
        // TODO: Use StructuredTaskScope to fetch data in parallel
        return null;
    }

    // Simulated Services
    private String fetchUser(String id) throws InterruptedException {
        Thread.sleep(100);
        return "User-" + id;
    }

    private String fetchOrders(String id) throws InterruptedException {
        Thread.sleep(200);
        return "Orders-for-" + id;
    }

    private String fetchSettings(String id) throws InterruptedException {
        Thread.sleep(150);
        return "Settings-for-" + id;
    }

    public record Dashboard(String user, String orders, String settings) {
    }
}
