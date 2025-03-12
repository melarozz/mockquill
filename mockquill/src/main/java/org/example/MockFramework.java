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
 * The main entry point for the mock framework.
 */
public class MockFramework {

    /**
     * Initializes fields annotated with @Mock in the given test instance.
     * Automatically injects mock instances into the fields.
     */
    public static void initMocks(Object testInstance) {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Mock.class)) {
                setMockToField(testInstance, field);
            }
        }
    }

    /**
     * Creates and assigns a mock instance to the given field.
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
     * Creates a mock for the given type.
     * - Uses Java Proxy if the class is an interface.
     * - Uses ByteBuddy for concrete classes.
     */
    public static <T> T mock(Class<T> clazz) {
        MyInvocationHandler handler = new MyInvocationHandler();

        return clazz.isInterface() ? createInterfaceMock(clazz, handler)
                : createClassMock(clazz, handler);
    }

    /**
     * Creates a mock for an interface using Java Proxy.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceMock(Class<T> clazz, MyInvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    /**
     * Creates a mock for a concrete class using ByteBuddy.
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
            throw new RuntimeException("Failed to create class mock for " + clazz, e);
        }
    }

    /**
     * Captures a method invocation on a mock for stubbing.
     * Usage: when(mock.method(...)).thenReturn(...);
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
     * Creates a spy that wraps a real object.
     * The spy delegates to the real object unless a method is stubbed.
     */
    public static <T> T spy(T realObject) {
        if (realObject == null) {
            throw new IllegalArgumentException("Cannot spy on a null object");
        }

        return realObject.getClass().isInterface() ? createInterfaceSpy(realObject)
                : createClassSpy(realObject);
    }

    /**
     * Creates a spy for an interface using Java Proxy.
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
     * Creates a spy for a concrete class using ByteBuddy.
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
            throw new RuntimeException("Failed to create spy for " + realObject.getClass(), e);
        }
    }

    /**
     * Copies all declared fields from the source to the target instance.
     * - This is a shallow copy.
     * - Fields declared as final may not be updated.
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
     * Copies a single field value from source to target.
     */
    private static void copyField(Object source, Object target, Field field) {
        field.setAccessible(true);
        try {
            field.set(target, field.get(source));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to copy field: " + field.getName(), e);
        }
    }
}
