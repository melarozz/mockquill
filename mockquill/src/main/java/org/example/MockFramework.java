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
import sun.misc.Unsafe;

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
    private static <T> T createClassSpy(T realObject) {
        try {
            T spyInstance = createSpyInstance(realObject);

            // Copy fields from the original object to the spy instance
            copyFields(realObject, spyInstance);

            return spyInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create spy for " + realObject.getClass(), e);
        }
    }

    /**
     * Creates a spy instance using ByteBuddy, with Unsafe to allocate memory.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createSpyInstance(T realObject) throws Exception {
        Class<? extends T> byteBuddyClass = new ByteBuddy()
                .subclass((Class<T>) realObject.getClass())
                .method(ElementMatchers.not(ElementMatchers.named("clone")))
                .intercept(InvocationHandlerAdapter.of(new SpyInvocationHandler(realObject)))
                .make()
                .load(realObject.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        return createInstanceWithoutConstructor(byteBuddyClass);
    }

    /**
     * Allocates an instance using Unsafe to avoid invoking the constructor.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInstanceWithoutConstructor(Class<? extends T> byteBuddyClass) throws Exception {
        Unsafe unsafe = getUnsafeInstance();
        return (T) unsafe.allocateInstance(byteBuddyClass);
    }

    /**
     * Retrieves the Unsafe instance via reflection.
     */
    private static Unsafe getUnsafeInstance() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (Unsafe) unsafeField.get(null);
    }

    /**
     * Copies fields from the original object to the spy instance.
     */
    private static <T> void copyFields(T originalObject, T spyInstance) throws Exception {
        Field[] fields = originalObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(originalObject);
                if (value != null) {
                    setFieldValue(spyInstance, field, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets a field value in the spy instance.
     */
    private static <T> void setFieldValue(T instance, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field fieldInstance = instance.getClass().getSuperclass().getDeclaredField(field.getName());
        fieldInstance.setAccessible(true);
        fieldInstance.set(instance, value);
    }
}
