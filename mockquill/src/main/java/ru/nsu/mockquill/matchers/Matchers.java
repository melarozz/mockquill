package ru.nsu.mockquill.matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides static matcher methods that can be used in stubbing.
 * For example:
 * when(mock.complexMethod(Matchers.eq("input"), Matchers.anyInt(), Matchers.matches("regex"))).thenReturn("value");
 */
public class Matchers {
    private static final ThreadLocal<List<ArgumentMatcher<?>>> matcherStack = ThreadLocal.withInitial(ArrayList::new);

    public static <T> T eq(T value) {
        matcherStack.get().add(new EqualsMatcher<>(value));
        return value;
    }

    public static int anyInt() {
        matcherStack.get().add(new AnyIntMatcher());
        return 1;
    }

    public static String matches(String regex) {
        matcherStack.get().add(new RegexMatcher(regex));
        return "";
    }

    public static <T> T customMatch(Predicate<T> callable) {
        matcherStack.get().add(new CustomLambdaMatcher<>(callable));
        return null;
    }

    public static int customMatchInt(Predicate<Integer> callable) {
        matcherStack.get().add(new CustomLambdaMatcher<>(callable));
        return 1;
    }

    public static double customMatchDouble(Predicate<Double> callable) {
        matcherStack.get().add(new CustomLambdaMatcher<>(callable));
        return 1;
    }

    public static float customMatchFloat(Predicate<Float> callable) {
        matcherStack.get().add(new CustomLambdaMatcher<>(callable));
        return 1;
    }

    public static List<ArgumentMatcher<?>> pullMatchers() {
        List<ArgumentMatcher<?>> matchers = new ArrayList<>(matcherStack.get());
        matcherStack.get().clear();
        return matchers;
    }
}
