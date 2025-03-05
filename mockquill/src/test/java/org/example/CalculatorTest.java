package org.example;

import static org.junit.Assert.assertEquals;
import static org.example.MockFramework.when;

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
    }
}
