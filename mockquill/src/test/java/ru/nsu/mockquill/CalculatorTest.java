package ru.nsu.mockquill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ru.nsu.mockquill.MockFramework.*;
import static ru.nsu.mockquill.matchers.Matchers.*;

import org.junit.Before;
import org.junit.Test;

public class CalculatorTest {

    @Mock
    private Calculator calculator;

    @Before
    public void setUp() {
        MockFramework.initMocks(this);
    }

    @Test
    public void testAddMethodStub() {
        when(calculator.add(2, 3)).thenReturn(10);
        int result = calculator.add(2, 3);
        assertEquals(10, result);
        result = calculator.multiply(2,3);
        assertEquals(0, result);
    }

    @Test
    public void testAddMethodStubStatic() {
        createStaticClassMock(Calculator.class);
        when(Calculator.divide(24343434, 3)).thenReturn(10);
        assertEquals(10, Calculator.divide(24343434, 3));
        restoreOriginal(Calculator.class);

        createStaticClassMock(Calculator.class);
        when(Calculator.divide(24343434, 3)).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> Calculator.divide(24343434, 3));
        restoreOriginal(Calculator.class);

        assertEquals(3, Calculator.divide(9, 3));
    }

    @Test
    public void testAddMethodStubStaticLambda() {
        createStaticClassMock(Calculator.class);
        when(Calculator.divide(customMatchInt((ctx) -> ctx > 478), eq(5))).thenReturn(10);
        int result = Calculator.divide(487, 5);
        assertEquals(10, result);

        restoreOriginal(Calculator.class);

        result = Calculator.divide(100, 5);
        assertEquals(20, result);
    }
}
