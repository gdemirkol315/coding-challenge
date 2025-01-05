package com.elevator;

import org.junit.Test;
import static org.junit.Assert.*;

public class ElevatorRequestTest {
    
    @Test
    public void testElevatorRequestCreation() {
        ElevatorRequest request = new ElevatorRequest(2, 5);
        assertEquals(2, request.getFromFloor());
        assertEquals(5, request.getToFloor());
    }

    @Test
    public void testElevatorRequestWithSameFloors() {
        ElevatorRequest request = new ElevatorRequest(3, 3);
        assertEquals(3, request.getFromFloor());
        assertEquals(3, request.getToFloor());
    }
}
