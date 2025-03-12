package ru.nsu.mockquill.staticmock;


import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;

import java.io.IOException;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class StaticMock {
    // In-memory storage for the original bytecode of the target class.
    private byte[] originalBytecode;

    /**
     * Stores the original bytecode of the target class (including its static methods).
     */
    public void storeOriginalBytecode(Class<?> targetClass) {
        // Install the agent if not already installed.
        ByteBuddyAgent.install();

        // Redefine the target class (without modifications) to capture its current bytecode.
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .redefine(targetClass)
                .make();
        originalBytecode = unloaded.getBytes();
        System.out.println("Original bytecode stored for " + targetClass.getName());
    }

    /**
     * Mocks the static method of the target class by redefining it.
     * The mock result is stored in a static field accessible to the advice.
     */
    public void mockStaticMethod(Class<?> targetClass, String methodName, Object mockResult) {
        // Install the agent if not already installed.
        ByteBuddyAgent.install();

        // Set the mock result into the advice's static field.
        StaticMethodAdvice.MOCK_RESULT = mockResult;

        new ByteBuddy()
                .redefine(targetClass)
                .visit(Advice.to(StaticMethodAdvice.class)
                        .on(named(methodName).and(isStatic())))
                .make()
                .load(targetClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        System.out.println("Static method '" + methodName + "' of "
                + targetClass.getName() + " has been mocked.");
    }

    /**
     * Restores the original bytecode for the target class.
     */
    public void restoreOriginal(Class<?> targetClass) throws IOException {
        if (originalBytecode == null) {
            throw new IllegalStateException("No original bytecode stored for " + targetClass.getName());
        }
        // Create a mapping from the target class's TypeDescription to the original bytecode.
        ClassReloadingStrategy.fromInstalledAgent().reset(targetClass);
    }

    // Advice class used to intercept static method calls.
    public static class StaticMethodAdvice {
        // Static field to store the desired mock result.
        public static Object MOCK_RESULT;

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static String onEnter(@Advice.AllArguments Object[] args) {
            // Cast the mock result to String to match the target method's return type.
            return (String) MOCK_RESULT;
        }

        @Advice.OnMethodExit
        public static void onExit(
                @Advice.Enter String returned,
                @Advice.Return(readOnly = false) String ret) {
            // Set the return value from onEnter as the new return value.
            ret = returned;
        }
    }

    // --- Example usage ---
    public static void main(String[] args) throws IOException {
        // Example target class with a static method.
        class TargetClass {
            public static String staticMethod(String input) {
                return "Original: " + input;
            }
        }

        StaticMock helper = new StaticMock();
        // Store the original bytecode.
        helper.storeOriginalBytecode(TargetClass.class);

        // Mock the static method so it returns a mocked result.
        helper.mockStaticMethod(TargetClass.class, "staticMethod", "Mocked Result");

        // Test the mocked method.
        String result = TargetClass.staticMethod("Test");
        System.out.println("After mocking: " + result);  // Expected: "Mocked Result"

        // Restore the original implementation.
        helper.restoreOriginal(TargetClass.class);
        result = TargetClass.staticMethod("Test");
        System.out.println("After restoring: " + result); // Expected: "Original: Test"


        // Mock the static method so it returns a mocked result.
        helper.mockStaticMethod(TargetClass.class, "staticMethod", "MAZAFAKA");
        // Test the mocked method.
        result = TargetClass.staticMethod("Test");
        System.out.println("After mocking: " + result);  // Expected: "Mocked Result"


        // Restore the original implementation.
        helper.restoreOriginal(TargetClass.class);
        result = TargetClass.staticMethod("Test");
        System.out.println("After restoring: " + result); // Expected: "Original: Test"
    }
}
