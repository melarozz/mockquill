package ru.nsu.mockquill.matchers;

import java.util.Objects;

/**
 * Matches an argument that is equal to an expected value.
 */
public class EqualsMatcher<T> implements ArgumentMatcher<T> {
    private final T expected;

    public EqualsMatcher(T expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(T argument) {
        return Objects.equals(expected, argument);
    }
}
