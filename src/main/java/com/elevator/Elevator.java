package com.elevator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Elevator implements Runnable {
    private final int id;
    private int currentFloor;
    private final BlockingQueue<ElevatorRequest> requests;
    private volatile boolean running = true;
    private final ElevatorSystem.DisplayWriter displayWriter;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Elevator(int id, ElevatorSystem.DisplayWriter displayWriter) {
        this.id = id;
        this.currentFloor = 1;
        this.requests = new LinkedBlockingQueue<>();
        this.displayWriter = displayWriter;
    }

    public void addRequest(ElevatorRequest request) {
        requests.offer(request);
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    private void writeStatus(String message) {
        String time = LocalTime.now().format(timeFormatter);
        displayWriter.write(String.format("[%s] Elevator %d: %s", time, id, message));
    }

    private void moveToFloor(int targetFloor) {
        int step = currentFloor < targetFloor ? 1 : -1;
        while (currentFloor != targetFloor) {
            currentFloor += step;
            writeStatus(String.format("Floor %d", currentFloor));
            try {
                Thread.sleep(1000); // Simulate movement time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void run() {
        writeStatus("Started at floor " + currentFloor);
        while (running) {
            try {
                ElevatorRequest request = requests.poll(1, TimeUnit.SECONDS);
                if (request != null) {
                    // First move to the floor where the request originated
                    moveToFloor(request.getFromFloor());
                    writeStatus("Picked up passenger at floor " + request.getFromFloor());
                    
                    // Then move to the destination floor
                    moveToFloor(request.getToFloor());
                    writeStatus("Dropped passenger at floor " + request.getToFloor());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
