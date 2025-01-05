package com.elevator;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class ElevatorTest {
    private Elevator elevator;
    private TestDisplayWriter displayWriter;

    private class TestDisplayWriter implements ElevatorSystem.DisplayWriter {
        List<String> messages = new ArrayList<>();

        @Override
        public void write(String message) {
            messages.add(message);
        }

        public List<String> getMessages() {
            return messages;
        }

        public void clear() {
            messages.clear();
        }
    }

    @Before
    public void setUp() {
        displayWriter = new TestDisplayWriter();
        elevator = new Elevator(1, displayWriter);
    }

    @Test
    public void testInitialState() {
        assertEquals(1, elevator.getCurrentFloor());
    }

    @Test
    public void testAddRequest() {
        ElevatorRequest request = new ElevatorRequest(2, 5);
        elevator.addRequest(request);
        assertEquals(1, elevator.getCurrentFloor()); // Initial floor shouldn't change just by adding request
    }

    @Test
    public void testElevatorMovement() throws InterruptedException {
        // Start elevator thread
        Thread elevatorThread = new Thread(elevator);
        elevatorThread.start();

        // Add request and wait for movement
        elevator.addRequest(new ElevatorRequest(3, 5));
        Thread.sleep(5000); // Wait for elevator to move

        // Verify elevator reached destination
        assertEquals(5, elevator.getCurrentFloor());

        // Verify movement messages were logged
        List<String> messages = displayWriter.getMessages();
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Started at floor 1")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Picked up passenger at floor 3")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Dropped passenger at floor 5")));

        // Cleanup
        elevator.stop();
        elevatorThread.interrupt();
        elevatorThread.join(1000);
    }

    @Test
    public void testStopElevator() {
        elevator.stop();
        assertFalse(elevator.isRunning()); // Note: You'll need to add isRunning() method to Elevator class
    }

    @Test
    public void testMultipleRequests() throws InterruptedException {
        Thread elevatorThread = new Thread(elevator);
        elevatorThread.start();

        // Add multiple requests
        elevator.addRequest(new ElevatorRequest(2, 4));
        elevator.addRequest(new ElevatorRequest(6, 1));

        Thread.sleep(20000); // Wait for elevator to process both requests

        // Verify final position
        assertEquals(1, elevator.getCurrentFloor());

        // Verify all movements were logged
        List<String> messages = displayWriter.getMessages();
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Picked up passenger at floor 2")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Dropped passenger at floor 4")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Picked up passenger at floor 6")));
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("Dropped passenger at floor 1")));

        // Cleanup
        elevator.stop();
        elevatorThread.interrupt();
        elevatorThread.join(1000);
    }
}
