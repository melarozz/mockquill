package ru.nsu.mockquill.invocation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import ru.nsu.mockquill.stub.Stub;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StaticInvocationHandler extends AbstractInvocationHandler {
    public static final StaticInvocationHandler INSTANCE = new StaticInvocationHandler();
    public static final List<Stub> stubs = new ArrayList<>();
    private byte[] originalBytecode;

    public static void storeOriginalBytecode(Class<?> targetClass) {
        ByteBuddyAgent.install();

        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(targetClass)
                .make();
        INSTANCE.originalBytecode = unloaded.getBytes();
        System.out.println("Original bytecode stored for " + targetClass.getName());
    }

    public static void restoreOriginal(Class<?> targetClass) throws IOException {
        if (INSTANCE.originalBytecode == null) {
            throw new IllegalStateException("No original bytecode stored for " + targetClass.getName());
        }
        ClassReloadingStrategy.fromInstalledAgent().reset(targetClass);
    }

    public void addStub(Invocation invocation, Object value, boolean isException) {
        stubs.add(new Stub(invocation, value, isException));
    }

    public Stub findStub(Invocation currentInvocation) {
        for (Stub stub : stubs) {
            if (stub.invocation().matches(currentInvocation)) {
                return stub;
            }
        }
        return null;
    }

    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        return null;
    }
}
