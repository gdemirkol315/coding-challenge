package com.elevator;

public class ElevatorRequest {
    private final int fromFloor;
    private final int toFloor;

    public ElevatorRequest(int fromFloor, int toFloor) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }
}
