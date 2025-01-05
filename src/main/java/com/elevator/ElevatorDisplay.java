package com.elevator;

import java.io.*;

public class ElevatorDisplay {
    private static final String PIPE_PATH = "/tmp/elevator_pipe";

    public static void main(String[] args) {
        System.out.println("Starting Elevator Display...");
        
        // Create pipe if it doesn't exist
        try {
            File pipe = new File(PIPE_PATH);
            if (!pipe.exists()) {
                Runtime.getRuntime().exec("mkfifo " + PIPE_PATH).waitFor();
                System.out.println("Created named pipe: " + PIPE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error creating pipe: " + e.getMessage());
            return;
        }

        System.out.println("Waiting for elevator system to connect...");
        
        while (true) {
            try (BufferedReader reader = new BufferedReader(new FileReader(PIPE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Connection lost. Waiting for reconnection...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }
}
