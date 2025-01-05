package com.elevator;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Assert;

import java.io.*;
import java.util.concurrent.*;

public class ElevatorDisplayTest {
    private static final String TEST_PIPE_PATH = "/tmp/test_elevator_pipe";
    private File testPipe;
    private ExecutorService executorService;

    @Before
    public void setUp() {
        testPipe = new File(TEST_PIPE_PATH);
        executorService = Executors.newSingleThreadExecutor();
        // Clean up any existing pipe
        if (testPipe.exists()) {
            testPipe.delete();
        }
    }

    @After
    public void tearDown() {
        executorService.shutdownNow();
        if (testPipe.exists()) {
            testPipe.delete();
        }
    }

    @Test
    public void testPipeCreation() throws Exception {
        // Create pipe using same method as ElevatorDisplay
        Process process = Runtime.getRuntime().exec("mkfifo " + TEST_PIPE_PATH);
        process.waitFor();

        assertTrue("Pipe should be created", testPipe.exists());
        assertTrue("Pipe should be readable", testPipe.canRead());
        assertTrue("Pipe should be writable", testPipe.canWrite());
    }

    @Test
    public void testReadFromPipe() throws Exception {
        // Create pipe
        Process process = Runtime.getRuntime().exec("mkfifo " + TEST_PIPE_PATH);
        process.waitFor();

        // Set up a writer in a separate thread
        Future<?> writerFuture = executorService.submit(() -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_PIPE_PATH))) {
                writer.println("Elevator at floor 1");
                writer.println("Elevator moving to floor 2");
            } catch (IOException e) {
                Assert.fail("Failed to write to pipe: " + e.getMessage());
            }
        });

        // Read from pipe
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_PIPE_PATH))) {
            String line;
            int linesRead = 0;
            while ((line = reader.readLine()) != null && linesRead < 2) {
                output.append(line).append("\n");
                linesRead++;
            }
        }

        writerFuture.get(5, TimeUnit.SECONDS); // Wait for writer to complete

        String expectedOutput = "Elevator at floor 1\nElevator moving to floor 2\n";
        assertEquals("Should read correct messages from pipe", expectedOutput, output.toString());
    }

    @Test
    public void testReconnectionAfterDisconnect() throws Exception {
        // Create pipe
        Process process = Runtime.getRuntime().exec("mkfifo " + TEST_PIPE_PATH);
        process.waitFor();

        // First connection
        Future<?> writerFuture = executorService.submit(() -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_PIPE_PATH))) {
                writer.println("First message");
            } catch (IOException e) {
                Assert.fail("Failed to write to pipe: " + e.getMessage());
            }
        });

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_PIPE_PATH))) {
            String line = reader.readLine();
            output.append(line).append("\n");
        }

        writerFuture.get(5, TimeUnit.SECONDS);

        // Second connection after brief delay
        Thread.sleep(1000);

        Future<?> secondWriterFuture = executorService.submit(() -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_PIPE_PATH))) {
                writer.println("Second message");
            } catch (IOException e) {
                Assert.fail("Failed to write to pipe: " + e.getMessage());
            }
        });

        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_PIPE_PATH))) {
            String line = reader.readLine();
            output.append(line).append("\n");
        }

        secondWriterFuture.get(5, TimeUnit.SECONDS);

        String expectedOutput = "First message\nSecond message\n";
        assertEquals("Should handle reconnection and read all messages", expectedOutput, output.toString());
    }

    @Test(expected = FileNotFoundException.class)
    public void testErrorHandlingForNonExistentPipe() throws FileNotFoundException {
        try (BufferedReader reader = new BufferedReader(new FileReader("/nonexistent/pipe"))) {
            reader.readLine();
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException) e;
            }
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }
}
