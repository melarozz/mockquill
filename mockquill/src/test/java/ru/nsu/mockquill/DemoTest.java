package ru.nsu.mockquill;

import static org.junit.Assert.*;
import static ru.nsu.mockquill.MockFramework.*;
import static ru.nsu.mockquill.matchers.Matchers.*;

import org.junit.Before;
import org.junit.Test;

public class DemoTest {
    @Mock
    private Calculator calculator;

    @Mock
    private SomeService someService;

    @Before
    public void setUp() {
        MockFramework.initMocks(this);
    }

    @Test
    public void testInstanceMockingAndMatchers() {
        when(calculator.add(2, 3)).thenReturn(10);
        assertEquals(10, calculator.add(2, 3)); // есть заглушка - выдаем 10 всегда
        assertEquals(0, calculator.multiply(2, 3)); // нет заглушки - default value 0

        assertNull(someService.getData()); // еще нет заглушки
        when(someService.getData()).thenReturn("Mocked Data");
        assertEquals("Mocked Data", someService.getData()); // вернулось ожидаемое значение

        when(someService.complexMethod(eq("input"), anyInt(), matches("[a-z]")))
                .thenReturn("Complex Mocked Value"); // ставим заглушку с матчерами
        assertEquals("Complex Mocked Value",
                someService.complexMethod("input", 500, "a")); // вернули заглушку
        assertNotEquals("Complex Mocked Value",
                someService.complexMethod("NOTinput", 500, "a")); // аргументы не подходят
    }

    @Test
    public void testSpyBehavior() {
        Calculator realCalculator = new Calculator();
        Calculator spyCalculator = spy(realCalculator);

        // С заглушкой возвращает указанное значение
        when(spyCalculator.multiply(anyInt(), anyInt())).thenReturn(100);
        assertEquals(100, spyCalculator.multiply(2, 3));
        assertEquals(100, spyCalculator.multiply(23, 32));

        // Без заглушки вызывает реальные методы
        assertEquals(5, spyCalculator.add(2, 3));
        assertEquals(50, spyCalculator.subtract(100, 50));
    }

    @Test
    public void testStaticMocking() {
        createStaticClassMock(Calculator.class);
        when(Calculator.divide(24343434, 3)).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> Calculator.divide(24343434, 3));
        restoreOriginal(Calculator.class);

        // исходный метод работает после восстановления
        assertEquals(3, Calculator.divide(9, 3));

        createStaticClassMock(Calculator.class);
        when(Calculator.divide(customMatchInt(ctx -> ctx > 478), eq(5))).thenReturn(10);
        assertEquals(10, Calculator.divide(487, 5));
        assertNotEquals(10, Calculator.divide(478, 5)); // аргументы не подходят
    }

    @Test
    public void testSpyBehaviorConstructor() {
        Car realCar = new Car("Toyota", "Mark II", 1998);
        Car spyCar = spy(realCar);

        spyCar.accelerate(100);
        assertEquals(100, spyCar.getSpeed(), 0.01);
    }
}
