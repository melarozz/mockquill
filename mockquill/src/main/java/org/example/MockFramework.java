package org.example;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.example.Invocation.Invocation;
import org.example.Invocation.MyInvocationHandler;
import org.example.Stubbing.OngoingStubbing;
import org.example.Stubbing.OngoingStubbingImpl;

/**
 * The main entry–point for the framework.
 */
public class MockFramework {

    /**
     * Initializes fields annotated with @Mock in the given test instance.
     */
    public static void initMocks(Object testInstance) {
        Class<?> clazz = testInstance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Mock.class)) {
                field.setAccessible(true);
                try {
                    Object mock = mock(field.getType());
                    field.set(testInstance, mock);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Creates a mock for the given type.
     * If the type is an interface, uses Java Proxy.
     * Otherwise (a class), uses ByteBuddy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        if (clazz.isInterface()) {
            MyInvocationHandler handler = new MyInvocationHandler();
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                    new Class<?>[]{clazz}, handler);
        } else {
            try {
                MyInvocationHandler handler = new MyInvocationHandler();
                Class<? extends T> dynamicType = new ByteBuddy()
                        .subclass(clazz)
                        .method(ElementMatchers.not(ElementMatchers.named("clone")))
                        .intercept(InvocationHandlerAdapter.of(handler))
                        .make()
                        .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
                return dynamicType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create class mock for " + clazz, e);
            }
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

}
