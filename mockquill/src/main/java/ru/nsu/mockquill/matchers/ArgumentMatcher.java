package ru.nsu.mockquill.matchers;

/**
 * Interface for matching argument values.
 */
public interface ArgumentMatcher<T> {
    boolean matches(T argument);
}
