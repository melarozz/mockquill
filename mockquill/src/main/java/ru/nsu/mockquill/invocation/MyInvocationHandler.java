package ru.nsu.mockquill.invocation;

import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.matchers.Matchers;
import ru.nsu.mockquill.MyMock;
import ru.nsu.mockquill.stub.Stub;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Invocation handler for mocks.
 */
public class MyInvocationHandler extends AbstractInvocationHandler {
    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        return getDefaultValue(method.getReturnType());
    }

    private Object getDefaultValue(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return false;
            if (returnType == char.class) return '\0';
            if (returnType == byte.class || returnType == short.class || returnType == int.class)
                return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0.0f;
            if (returnType == double.class) return 0.0d;
        }
        return null;
    }
}
