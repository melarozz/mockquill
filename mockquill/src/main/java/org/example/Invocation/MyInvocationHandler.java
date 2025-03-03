package org.example.Invocation;

import org.example.AllMatchers.ArgumentMatcher;
import org.example.AllMatchers.Matchers;
import org.example.MyMock;
import org.example.Stubbing.Stub;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Invocation handler for mocks.
 */
public class MyInvocationHandler extends AbstractInvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<ArgumentMatcher<?>> matchers = Matchers.pullMatchers();
        Invocation invocation = new Invocation(proxy, method, args, matchers, this);
        MyMock.setLastInvocation(invocation);
        Stub stub = findStub(invocation);
        if (stub != null) {
            if (stub.exception()) {
                throw (Throwable) stub.value();
            } else {
                return stub.value();
            }
        }
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
