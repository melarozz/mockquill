package org.example.AllMatchers;

/**
 * Matches any int value.
 */
public class AnyIntMatcher implements ArgumentMatcher<Integer> {
    @Override
    public boolean matches(Integer argument) {
        return argument != null;
    }
}
