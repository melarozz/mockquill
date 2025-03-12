package ru.nsu.mockquill;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ru.nsu.mockquill.MockFramework.when;
import static ru.nsu.mockquill.matchers.Matchers.*;

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

    @Test
    public void testCustomMatch() {
        when(someService.complexMethod(eq("input"), customMatch(() -> 123), matches("[a-z]")))
                .thenReturn("Complex Mocked Value");
        assertEquals("Complex Mocked Value", someService.complexMethod("input", 123, "b"));
        assertNotEquals("Complex Mocked Value", someService.complexMethod("input", 123, "1"));
    }
}
