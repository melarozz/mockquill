package ru.nsu.mockquill;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import ru.nsu.mockquill.invocation.Invocation;
import ru.nsu.mockquill.invocation.MyInvocationHandler;
import ru.nsu.mockquill.invocation.SpyInvocationHandler;
import ru.nsu.mockquill.stub.OngoingStubbing;
import ru.nsu.mockquill.stub.OngoingStubbingImpl;

/**
 * The main entry–point for the framework.
 */
public class MockFramework {
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
     * Initializes fields annotated with @Mock in the given test instance.
     */
    public static void initMocks(Object testInstance) {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Mock.class)) {
                setMockToField(testInstance, field);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceMock(Class<T> clazz, MyInvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    /**
     * Creates a mock for the given type.
     * If the type is an interface, uses Java Proxy.
     * Otherwise (a class), uses ByteBuddy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        MyInvocationHandler handler = new MyInvocationHandler();

        return clazz.isInterface() ? createInterfaceMock(clazz, handler)
                : createClassMock(clazz, handler);
    }

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
     * Captures a method invocation on a mock for stubbing.
     * Usage: when(mock.method(...)).thenReturn(...);
     */
    public static <T> OngoingStubbing<T> when(T methodCall) {
        Invocation invocation = MyMock.getLastInvocation();
        return new OngoingStubbingImpl<>(invocation);
    }

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
