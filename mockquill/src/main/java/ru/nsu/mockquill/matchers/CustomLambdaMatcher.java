package ru.nsu.mockquill.matchers;

import java.util.function.Predicate;


public class CustomLambdaMatcher<T> implements ArgumentMatcher<T> {
    private final Predicate<T> supplier;

    public CustomLambdaMatcher(Predicate<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean matches(T argument) {
        return supplier.test(argument);
    }
}
