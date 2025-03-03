package org.example;

import static org.example.MockFramework.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.example.AllMatchers.Matchers.eq;
import static org.example.AllMatchers.Matchers.anyInt;
import static org.example.AllMatchers.Matchers.matches;

import org.junit.Before;
import org.junit.Test;

public class ExampleTest {

    @Mock
    private SomeService someService;

    @Before
    public void setUp() {
        MockFramework.initMocks(this);
    }

    @Test
    public void testMock() {
        when(someService.getData()).thenReturn("Mocked Data");
        assertEquals("Mocked Data", someService.getData());
    }

    @Test(expected = RuntimeException.class)
    public void testThrow() {
        when(someService.getData()).thenThrow(new RuntimeException("Error"));
        someService.getData();
    }

    @Test
    public void testMatchers() {
        when(someService.complexMethod(eq("input"), anyInt(), matches("[a-z]")))
                .thenReturn("Complex Mocked Value");
        assertEquals("Complex Mocked Value", someService.complexMethod("input", 123, "b"));
        assertNotEquals("Complex Mocked Value", someService.complexMethod("input", 123, "1"));
    }
}
