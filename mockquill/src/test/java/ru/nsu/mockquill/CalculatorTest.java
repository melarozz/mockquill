package ru.nsu.mockquill;

import static org.junit.Assert.assertEquals;
import static ru.nsu.mockquill.MockFramework.createStaticClassMock;
import static ru.nsu.mockquill.MockFramework.when;
import static ru.nsu.mockquill.matchers.Matchers.anyInt;
import static ru.nsu.mockquill.matchers.Matchers.customMatchInt;

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
        when(Calculator.divide(2, 3)).thenReturn(10);
        int result = Calculator.divide(2, 3);
        assertEquals(10, result);
    }
    @Test
    public void testAddMethodStubStaticLambda() {
        createStaticClassMock(Calculator.class);
        when(Calculator.divide(anyInt(), 3)).thenReturn(10);
        int result = Calculator.divide(224, 3);
        assertEquals(10, result);
    }
}
