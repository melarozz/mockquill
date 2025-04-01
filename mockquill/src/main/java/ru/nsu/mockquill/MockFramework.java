package ru.nsu.mockquill;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import ru.nsu.mockquill.invocation.Invocation;
import ru.nsu.mockquill.invocation.InvocationHandler;
import ru.nsu.mockquill.invocation.SpyInvocationHandler;
import ru.nsu.mockquill.invocation.StaticInvocation;
import ru.nsu.mockquill.invocation.StaticInvocationHandler;
import ru.nsu.mockquill.stub.OngoingStubbing;
import ru.nsu.mockquill.stub.OngoingStubbingImpl;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;

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
    private static <T> T createInterfaceMock(Class<T> clazz, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, handler);
    }

    /**
     * Creates a mock for the given type.
     * If the type is an interface, uses Java Proxy.
     * Otherwise (a class), uses ByteBuddy.
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> clazz) {
        InvocationHandler handler = new InvocationHandler();

        return clazz.isInterface() ? createInterfaceMock(clazz, handler)
                : createClassMock(clazz, handler);
    }

    private static <T> T createClassMock(Class<T> clazz, InvocationHandler handler) {
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

    static <T> void createStaticClassMock(Class<T> clazz) {
        StaticInvocationHandler.storeOriginalBytecode(clazz);
        InvocationStorage.staticInvocationHandler = new StaticInvocationHandler();
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(clazz)
                .visit(Advice.to(StaticInvocation.class)
                        .on(isStatic()))
                .make()
                .load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    static <T> void restoreOriginal(Class<T> clazz) {
        try {
            StaticInvocationHandler.restoreOriginal(clazz);
            StaticInvocationHandler.stubs.clear();
            System.out.println("Original bytecode restored for " + clazz.getName());
        } catch (java.io.IOException e) {
            System.out.println("Can't restore class " + clazz.getName());
        }
    }

    /**
     * Captures a method invocation on a mock for stubbing.
     * Usage: when(mock.method(...)).thenReturn(...);
     */
    public static <T> OngoingStubbing<T> when(T methodCall) {
        Invocation invocation = InvocationStorage.getLastInvocation();

        if (invocation == null) {
            throw new IllegalStateException("No invocation captured for stubbing.");
        }
        InvocationStorage.clearLastInvocation();
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
     * Retrieves the Unsafe instance via reflection.
     */
    private static Unsafe getUnsafeInstance() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (Unsafe) unsafeField.get(null);
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
     * Создаёт шпион для конкретного класса с использованием ByteBuddy.
     */
    @SuppressWarnings("unchecked")
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
     * Копирует все объявленные поля из одного объекта в другой.
     * - Это shallow копирование.
     * - Финальные поля могут не обновляться.
     */
    private static void copyFields(Object originalObject, Object spyInstance) {
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

    private static <T> void setFieldValue(T instance, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field fieldInstance = instance.getClass().getSuperclass().getDeclaredField(field.getName());
        fieldInstance.setAccessible(true);
        fieldInstance.set(instance, value);
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
