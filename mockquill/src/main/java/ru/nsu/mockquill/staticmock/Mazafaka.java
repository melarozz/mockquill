package ru.nsu.mockquill.staticmock;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import ru.nsu.mockquill.MyMock;
import ru.nsu.mockquill.invocation.AbstractInvocationHandler;
import ru.nsu.mockquill.invocation.Invocation;
import ru.nsu.mockquill.invocation.MyInvocationHandler;
import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.stub.Stub;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.nsu.mockquill.matchers.Matchers.pullMatchers;

public class Mazafaka extends AbstractInvocationHandler {
    public static final Mazafaka INSTANCE = new Mazafaka();
    public Class<?> clazz;
    private byte[] originalBytecode;
    public static void storeOriginalBytecode(Class<?> targetClass) {
        ByteBuddyAgent.install();

        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(targetClass)
                .make();
        INSTANCE.clazz = targetClass;
        INSTANCE.originalBytecode = unloaded.getBytes();
        System.out.println("Original bytecode stored for " + targetClass.getName());
    }

    public static void restoreOriginal(Class<?> targetClass) throws IOException {
        if (INSTANCE.originalBytecode == null) {
            throw new IllegalStateException("No original bytecode stored for " + targetClass.getName());
        }
        ClassReloadingStrategy.fromInstalledAgent().reset(targetClass);
    }

    public static List<Stub> stubs = new ArrayList<>();

    public void addStub(Invocation invocation, Object value, boolean isException) {
        stubs.add(new Stub(invocation, value, isException));
    }

    public Stub findStub(Invocation currentInvocation) {
        System.out.println(currentInvocation.matchers + "MAZAFAKA3");
        for (Stub stub : stubs) {
            System.out.println(stub.invocation().matches(currentInvocation) + " sokmr;osmerkf;soemkrf;lskemrfl;skmerflmkserf");
            if (stub.invocation().matches(currentInvocation)) {
                return stub;
            }
        }
        return null;
    }

    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        Mazafaka.restoreOriginal(INSTANCE.clazz);
        return getDefaultValue(method.getReturnType());
    }

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.AllArguments Object[] args,
            @Advice.Origin Method method,
            @Advice.Origin Class<?> clazz
    ) {
        Mazafaka.storeOriginalBytecode(clazz);
        // Retrieve matchers and create an invocation object.
        List<ArgumentMatcher<?>> matchers = pullMatchers();
        System.out.println(matchers.size() + " MAZAFAKA228");
        Invocation invocation = new Invocation(null, method, args, matchers, INSTANCE);
        MyMock.clearLastInvocation();
        MyMock.setLastInvocation(invocation);
    }

    /**
     * OnMethodExit overrides the return value if a stub exists.
     * Using dynamic typing here allows ByteBuddy to perform boxing/unboxing
     * and assign the value even when the method returns a primitive.
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin Method method,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object ret)
            throws Throwable {

        // Find the stub for the recorded invocation.
        System.out.println(INSTANCE.stubs + " RMEOLRMFOERMF");
        Stub stub = INSTANCE.findStub(MyMock.getLastInvocation());
        if (stub != null) {
            if (stub.exception()) {
                throw (Throwable) stub.value();
            } else {
                // Override the return value with the stubbed value.
                ret = stub.value();
            }
        }

//
        // Otherwise, leave 'ret' as is (which may be the default value for primitives).
    }

    private static Object getDefaultValue(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return false;
            if (returnType == char.class) return '\0';
            if (returnType == byte.class || returnType == short.class || returnType == int.class)
                return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0.0f;
            if (returnType == double.class) return 0.0d;
        }
        return null;
    }
}
