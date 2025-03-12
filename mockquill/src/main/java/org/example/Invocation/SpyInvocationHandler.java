package org.example.Invocation;

import java.lang.reflect.Method;

/**
 * Invocation handler для спаев.
 * Если для вызова настроена заглушка, возвращается stub-значение;
 * в противном случае вызов делегируется обёрнутому (реальному) объекту.
 */
public class SpyInvocationHandler extends AbstractInvocationHandler {
    private final Object realObject;

    public SpyInvocationHandler(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        return method.invoke(realObject, args);
    }
}
