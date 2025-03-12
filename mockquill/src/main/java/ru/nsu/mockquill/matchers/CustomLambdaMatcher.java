package ru.nsu.mockquill.matchers;

import java.util.function.Supplier;


public class CustomLambdaMatcher<T> implements ArgumentMatcher<T> {
//@FunctionalInterface
//public interface ContextualFunction<T> {
//    R apply(T context);
//}
//    private final ContextualFunction<T, R> function;
//
//    public static <T, R> R executeWithContext(T context, ContextualFunction<T, R> function) {
//        return function.apply(context);
//    }
//
//    public CustomLambdaMatcher(ContextualFunction<T, R> function) {
//        this.function = function;
//    }

    private final Supplier<T> supplier;

    public CustomLambdaMatcher(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean matches(T argument) {
        return supplier.get() == argument;
    }
}
