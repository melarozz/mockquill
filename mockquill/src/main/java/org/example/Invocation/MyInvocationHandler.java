package org.example.Invocation;

import java.lang.reflect.Method;

/**
 * Invocation handler для моков.
 * Если для вызова не настроена заглушка, возвращаются значения по умолчанию,
 * соответствующие типу возвращаемого значения метода.
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
