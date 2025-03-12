package org.example;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.example.Invocation.Invocation;
import org.example.Invocation.MyInvocationHandler;
import org.example.Stubbing.OngoingStubbing;
import org.example.Stubbing.OngoingStubbingImpl;
import org.example.Invocation.SpyInvocationHandler;

/**
 * Основной класс для работы с mock и spy.
 */
public class MockFramework {

    /**
     * Инициализирует поля с аннотацией @Mock в переданном объекте теста.
     * Автоматически создаёт моки и присваивает их полям.
     */
    public static void initMocks(Object testInstance) {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Mock.class)) {
                setMockToField(testInstance, field);
            }
        }
    }

    /**
     * Создаёт мок и присваивает его указанному полю.
     */
    private static void setMockToField(Object testInstance, Field field) {
        field.setAccessible(true);
        try {
            Object mock = mock(field.getType());
            field.set(testInstance, mock);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject mock into field: " + field.getName(), e);
        }
    }

    /**
     * Создаёт мок-объект для переданного класса.
     * - Если это интерфейс, используется Java Proxy.
     * - Если это динамический класс, используется ByteBuddy.
     *   TODO: статические классы
     */
    public static <T> T mock(Class<T> clazz) {
        MyInvocationHandler handler = new MyInvocationHandler();

        return clazz.isInterface() ? createInterfaceMock(clazz, handler)
                : createClassMock(clazz, handler);
    }

    /**
     * Создаёт мок для интерфейса с помощью Java Proxy.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceMock(Class<T> clazz, MyInvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    /**
     * Создаёт мок для конкретного класса с использованием ByteBuddy.
     */
    private static <T> T createClassMock(Class<T> clazz, MyInvocationHandler handler) {
        try {
            return new ByteBuddy()
                    .subclass(clazz)
                    .method(ElementMatchers.not(ElementMatchers.named("clone")))
                    .intercept(InvocationHandlerAdapter.of(handler))
                    .make()
                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create classs mock for " + clazz, e);
        }
    }

    /**
     * Запоминает вызов метода для последующего задания заглушки.
     * Использование: when(mock.method(...)).thenReturn(...);
     */
    public static <T> OngoingStubbing<T> when(T methodCall) {
        Invocation invocation = MyMock.getLastInvocation();
        if (invocation == null) {
            throw new IllegalStateException("No invocation captured for stubbing.");
        }
        MyMock.clearLastInvocation();
        return new OngoingStubbingImpl<>(invocation);
    }

    /**
     * Создаёт шпион (spy), который оборачивает реальный объект.
     * Вызовы делегируются реальному объекту, если для них нет заглушки.
     */
    public static <T> T spy(T realObject) {
        if (realObject == null) {
            throw new IllegalArgumentException("Cannot spy on null-object");
        }

        return realObject.getClass().isInterface() ? createInterfaceSpy(realObject)
                : createClassSpy(realObject);
    }

    /**
     * Создаёт шпион для интерфейса с использованием Java Proxy.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceSpy(T realObject) {
        return (T) Proxy.newProxyInstance(
                realObject.getClass().getClassLoader(),
                realObject.getClass().getInterfaces(),
                new SpyInvocationHandler(realObject)
        );
    }

    /**
     * Создаёт шпион для конкретного класса с использованием ByteBuddy.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createClassSpy(T realObject) {
        try {
            T spyInstance = new ByteBuddy()
                    .subclass((Class<T>) realObject.getClass())
                    .method(ElementMatchers.not(ElementMatchers.named("clone")))
                    .intercept(InvocationHandlerAdapter.of(new SpyInvocationHandler(realObject)))
                    .make()
                    .load(realObject.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();

            copyFields(realObject, spyInstance);
            return spyInstance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot make spy on " + realObject.getClass(), e);
        }
    }

    /**
     * Копирует все объявленные поля из одного объекта в другой.
     * - Это shallow копирование.
     * - Финальные поля могут не обновляться.
     */
    private static void copyFields(Object source, Object target) {
        Class<?> clazz = source.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                copyField(source, target, field);
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Копирует значение одного поля из исходного объекта в целевой.
     */
    private static void copyField(Object source, Object target, Field field) {
        field.setAccessible(true);
        try {
            field.set(target, field.get(source));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot copy fields: " + field.getName(), e);
        }
    }
}
