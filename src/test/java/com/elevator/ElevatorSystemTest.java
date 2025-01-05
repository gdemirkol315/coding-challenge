package com.elevator;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class ElevatorSystemTest {
    private ElevatorSystem elevatorSystem;
    private List<String> displayMessages;

    @Before
    public void setUp() {
        displayMessages = new ArrayList<>();
        // Override the pipe writing functionality for testing
        elevatorSystem = new ElevatorSystem(2) {
            @Override
            protected void initializeDisplay(int numElevators) {
                // Skip pipe initialization for tests
            }

            @Override
            protected void writeToDisplay(String message) {
                displayMessages.add(message);
            }
        };
    }

    @After
    public void tearDown() {
        if (elevatorSystem != null) {
            elevatorSystem.shutdown();
        }
    }

    @Test
    public void testSystemInitialization() throws InterruptedException {
        Thread.sleep(2000); // Wait for request processing
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Elevator 1: Started at floor 1")));
    }

    @Test
    public void testRequestSubmission() throws InterruptedException {
        // Submit a request and verify it's assigned to the closest elevator
        elevatorSystem.submitRequest(3, 5);
        Thread.sleep(2000); // Wait for request processing

        // Verify that movement messages were logged
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 3")));
    }

    @Test
    public void testMultipleRequests() throws InterruptedException {
        // Submit multiple requests
        elevatorSystem.submitRequest(2, 4);
        elevatorSystem.submitRequest(6, 1);
        Thread.sleep(10000); // Wait for requests processing

        // Verify that both requests were handled
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 2")));
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 4")));
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 6")));
        assertTrue(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 1")));
    }

    @Test
    public void testShutdown() throws InterruptedException {
        elevatorSystem.submitRequest(2, 4);
        Thread.sleep(1000);
        elevatorSystem.shutdown();
        Thread.sleep(1000);

        // Submit another request after shutdown
        elevatorSystem.submitRequest(3, 5);
        Thread.sleep(1000);

        // Verify no new messages after shutdown
        assertFalse(displayMessages.stream()
            .anyMatch(msg -> msg.contains("Floor 3") || msg.contains("Floor 5")));
    }
}
