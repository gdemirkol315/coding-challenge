package com.elevator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ElevatorSystem {
    private static final String PIPE_PATH = "/tmp/elevator_pipe";
    private final List<Elevator> elevators;
    private final List<Thread> elevatorThreads;
    private final BlockingQueue<String> displayMessages;
    private volatile boolean running = true;
    private Thread displayThread;
    private PrintWriter pipeWriter;

    public interface DisplayWriter {
        void write(String message);
    }

    protected void initializeDisplay(int numElevators) throws IOException {
        pipeWriter = new PrintWriter(new FileWriter(PIPE_PATH));
        writeToDisplay("Starting elevator system with " + numElevators + " elevators...");
    }

    protected void writeToDisplay(String message) {
        if (pipeWriter != null) {
            pipeWriter.println(message);
            pipeWriter.flush();
        }
    }

    public ElevatorSystem(int numElevators) {
        elevators = new ArrayList<>();
        elevatorThreads = new ArrayList<>();
        displayMessages = new LinkedBlockingQueue<>();

        // Start display thread
        displayThread = new Thread(() -> {
            while (running) {
                try {
                    // Try to open pipe for writing
                    while (running && (pipeWriter == null || !new File(PIPE_PATH).exists())) {
                        try {
                            initializeDisplay(numElevators);
                            break;
                        } catch (IOException e) {
                            System.out.println("Waiting for display window to start...");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    String message = displayMessages.take();
                    try {
                        writeToDisplay(message);
                    } catch (Exception e) {
                        System.out.println("Lost connection to display. Attempting to reconnect...");
                        pipeWriter = null;
                        continue;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        displayThread.start();

        // Create a display writer that will be passed to elevators
        DisplayWriter display = message -> {
            displayMessages.offer(message);
        };
        
        // Create and start elevator threads
        for (int i = 1; i <= numElevators; i++) {
            Elevator elevator = new Elevator(i, display);
            elevators.add(elevator);
            Thread thread = new Thread(elevator);
            elevatorThreads.add(thread);
            thread.start();
        }
    }

    public void submitRequest(int fromFloor, int toFloor) {
        ElevatorRequest request = new ElevatorRequest(fromFloor, toFloor);
        
        // Simple elevator selection: choose the closest available elevator
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getCurrentFloor() - fromFloor);
            if (distance < minDistance) {
                minDistance = distance;
                bestElevator = elevator;
            }
        }
        
        if (bestElevator != null) {
            bestElevator.addRequest(request);
        }
    }

    public void shutdown() {
        running = false;
        for (Elevator elevator : elevators) {
            elevator.stop();
        }
        for (Thread thread : elevatorThreads) {
            thread.interrupt();
        }
        if (displayThread != null) {
            displayThread.interrupt();
        }
        if (pipeWriter != null) {
            pipeWriter.close();
        }
    }
}
