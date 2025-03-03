package org.example.AllMatchers;

/**
 * Interface for matching argument values.
 */
public interface ArgumentMatcher<T> {
    boolean matches(T argument);
}
