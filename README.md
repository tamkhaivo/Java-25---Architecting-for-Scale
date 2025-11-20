# Java 25 Virtual Threads Example

This project demonstrates the use of virtual threads, a feature of Project Loom in Java 25.

## Prerequisites

*   Java 25 JDK
*   Apache Maven

## How to Run

1.  Compile the project:
    ```bash
    mvn compile
    ```

2.  Run the `VirtualThreadsExample`:
    ```bash
    mvn exec:java -Dexec.mainClass="com.example.loom.VirtualThreadsExample"
    ```

3.  Run the `ThreadComparison` example:
    ```bash
    mvn exec:java -Dexec.mainClass="com.example.loom.ThreadComparison"
    ```

The `VirtualThreadsExample` program will start 100,000 virtual threads, each of which will sleep for 1 second. The program will print a message when each task is complete and will print the total time taken to complete all tasks.

The `ThreadComparison` program will first run a test with 1,000 platform threads and then with 100,000 virtual threads, each performing a 1-second delay. It will then print the time taken for each, demonstrating the efficiency and scalability of virtual threads.
