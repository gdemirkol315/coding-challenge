package com.elevator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("ELEVATOR SYSTEM");
            System.out.println("==============");
            System.out.println("Before proceeding, please open a new terminal and run:");
            System.out.println("java -cp target/classes com.elevator.ElevatorDisplay");
            System.out.println("\nPress Enter when you've started the display...");
            scanner.nextLine();
            
            System.out.print("\nEnter the number of elevators: ");
            int numElevators = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            System.out.println("\nCommand Interface");
            System.out.println("----------------");
            System.out.println("Format: fromFloor toFloor");
            System.out.println("Example: 3 20");
            System.out.println("Type 'exit' to quit\n");
            
            ElevatorSystem elevatorSystem = new ElevatorSystem(numElevators);
            
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("exit")) {
                    elevatorSystem.shutdown();
                    break;
                }
                
                try {
                    String[] parts = input.split("\\s+");
                    if (parts.length != 2) {
                        System.out.println("Invalid input format! Please enter two numbers.");
                        continue;
                    }
                    
                    int fromFloor = Integer.parseInt(parts[0]);
                    int toFloor = Integer.parseInt(parts[1]);
                    
                    if (fromFloor < 1 || toFloor < 1) {
                        System.out.println("Floor numbers must be positive!");
                        continue;
                    }
                    
                    elevatorSystem.submitRequest(fromFloor, toFloor);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter numbers.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Elevator system shut down.");
        }
    }
}
