package org.example.Invocation;

import org.example.AllMatchers.ArgumentMatcher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Records a method call on a mock.
 * If matchers were used (via Matchers.eq, etc.) they are stored in the matchers list.
 */
public class Invocation {
    private final Method method;
    private final Object[] args;
    private final List<ArgumentMatcher<?>> matchers; // may be null if no matchers were used
    private final AbstractInvocationHandler handler;

    public Invocation(Object proxy, Method method, Object[] args,
                      List<ArgumentMatcher<?>> matchers, AbstractInvocationHandler handler) {
        this.method = method;
        this.args = args != null ? args : new Object[0];
        // if the number of matchers equals the number of arguments, we use them.
        this.matchers = (matchers != null && matchers.size() == this.args.length) ? matchers : null;
        this.handler = handler;
    }

    public AbstractInvocationHandler getHandler() {
        return handler;
    }

    /**
     * Checks whether this (expected) invocation matches an actual invocation.
     * If matchers are defined for each parameter, their match() method is used.
     */
    @SuppressWarnings("unchecked")
    public boolean matches(Invocation actual) {
        if (!method.equals(actual.method)) return false;
        if (args.length != actual.args.length) return false;
        for (int i = 0; i < args.length; i++) {
            Object expected = args[i];
            Object actualArg = actual.args[i];
            if (matchers != null) {
                ArgumentMatcher matcher = matchers.get(i);
                if (!matcher.matches(actualArg)) {
                    return false;
                }
            } else if (!Objects.equals(expected, actualArg)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "method=" + method.getName() +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
