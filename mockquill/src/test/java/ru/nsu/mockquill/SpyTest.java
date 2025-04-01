package ru.nsu.mockquill;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static ru.nsu.mockquill.MockFramework.spy;
import static ru.nsu.mockquill.MockFramework.when;
import static ru.nsu.mockquill.matchers.Matchers.anyInt;
import static ru.nsu.mockquill.matchers.Matchers.eq;

public class SpyTest {

    @Test
    public void testSpyBehavior() {
        Calculator realCalculator = new Calculator();
        Calculator spyCalculator = spy(realCalculator);

        assertEquals(5, spyCalculator.add(2, 3));
        assertEquals(6, spyCalculator.multiply(2, 3));

        when(spyCalculator.multiply(anyInt(), anyInt())).thenReturn(100);
        assertEquals(100, spyCalculator.multiply(2, 3));
        assertEquals(100, spyCalculator.multiply(23, 32));

        assertEquals(10, spyCalculator.add(7, 3));
    }

    @Test
    public void testSpyWithDifferentStubs() {
        Calculator realCalculator = new Calculator();
        Calculator spyCalculator = spy(realCalculator);

        when(spyCalculator.add(10, 20)).thenReturn(500);
        assertEquals(500, spyCalculator.add(10, 20));
        assertEquals(30, spyCalculator.add(15, 15));

        // Stub subtract() method
        when(spyCalculator.subtract(anyInt(), eq(5))).thenReturn(-1);
        assertEquals(-1, spyCalculator.subtract(10, 5));
        assertEquals(5, spyCalculator.subtract(20, 15));
    }

    @Test
    public void testSpyDoesNotAffectOtherMethods() {
        Calculator realCalculator = new Calculator();
        Calculator spyCalculator = spy(realCalculator);

        when(spyCalculator.subtract(anyInt(), anyInt())).thenReturn(999);

        assertEquals(999, spyCalculator.subtract(100, 50));
        assertEquals(999, spyCalculator.subtract(10, 5));

        assertEquals(8, spyCalculator.add(5, 3));
        assertEquals(50, spyCalculator.multiply(5, 10));
    }

//    @Test
//    public void testSpyWithArgs() {
//        Car realCar = new Car("Toyota", "Mark II", 1998);
//        realCar.accelerate(100);
//        Car spyCar = spy(realCar);
//        assertEquals(spyCar.getSpeed(), realCar.getSpeed(), 0.0001);
//    }
}
