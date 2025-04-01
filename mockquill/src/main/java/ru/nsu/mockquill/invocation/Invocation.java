package ru.nsu.mockquill.invocation;

import java.lang.reflect.Method;
import ru.nsu.mockquill.matchers.ArgumentMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Класс, записывающий вызов метода на mock-объекте.
 * Если при вызове использовались matchers (например, Matchers.eq(...)), они сохраняются в списке.
 */
public class Invocation {
    private final Method method;
    private final Object[] args;
    private final List<ArgumentMatcher<?>> matchers; // Может быть null, если matchers не использовались
    private final AbstractInvocationHandler handler;

    public Invocation(Object proxy, Method method, Object[] args,
                      List<ArgumentMatcher<?>> matchers, AbstractInvocationHandler handler) {
        this.method = method;
        this.args = args != null ? args : new Object[0];
        this.matchers = (matchers != null && matchers.size() == this.args.length) ? matchers : null;
        this.handler = handler;
    }

    public AbstractInvocationHandler getHandler() {
        return handler;
    }

    /**
     * Сравнивает ожидаемый вызов с фактическим.
     * Если для каждого параметра заданы matchers, используется их метод matches().
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
