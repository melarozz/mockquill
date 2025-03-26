package ru.nsu.mockquill.staticmock;


import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.bind.annotation.Origin;
import ru.nsu.mockquill.MyMock;
import ru.nsu.mockquill.invocation.AbstractInvocationHandler;
import ru.nsu.mockquill.invocation.Invocation;
import ru.nsu.mockquill.invocation.MyInvocationHandler;
import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.stub.Stub;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static ru.nsu.mockquill.matchers.Matchers.pullMatchers;

public class StaticMock {
    private byte[] originalBytecode;

    public void storeOriginalBytecode(Class<?> targetClass) {
        ByteBuddyAgent.install();

        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(targetClass)
                .make();
        originalBytecode = unloaded.getBytes();
        System.out.println("Original bytecode stored for " + targetClass.getName());
    }

    public void mockStaticMethod(Class<?> targetClass, String methodName, Object mockResult) {
        ByteBuddyAgent.install();
//        Mazafaka.MOCK_RESULT = mockResult;

        new ByteBuddy()
                .redefine(targetClass)
                .visit(Advice.to(Mazafaka.class)
                        .on(isStatic()))
                .make()
                .load(targetClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        System.out.println("Static method '" + methodName + "' of "
                + targetClass.getName() + " has been mocked.");
    }

    public void restoreOriginal(Class<?> targetClass) throws IOException {
        if (originalBytecode == null) {
            throw new IllegalStateException("No original bytecode stored for " + targetClass.getName());
        }
        ClassReloadingStrategy.fromInstalledAgent().reset(targetClass);
    }

    public static void main(String[] args) throws IOException {
        class TargetClass {
            public static String staticMethod(String input) {
                return "Original: " + input;
            }
        }

        StaticMock helper = new StaticMock();
        helper.storeOriginalBytecode(TargetClass.class);

        helper.mockStaticMethod(TargetClass.class, "staticMethod", "Mocked Result");
        String result = TargetClass.staticMethod("Test");
        System.out.println("After mocking: " + result);

        helper.restoreOriginal(TargetClass.class);
        result = TargetClass.staticMethod("Test");
        System.out.println("After restoring: " + result);

        helper.mockStaticMethod(TargetClass.class, "staticMethod", "hola senor!");
        result = TargetClass.staticMethod("Test");
        System.out.println("After mocking: " + result);

        helper.restoreOriginal(TargetClass.class);
        result = TargetClass.staticMethod("Test");
        System.out.println("After restoring: " + result);
    }
}
