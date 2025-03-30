package ru.nsu.mockquill.invocation;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import ru.nsu.mockquill.InvocationStorage;
import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.stub.Stub;

import java.lang.reflect.Method;
import java.util.List;

import static ru.nsu.mockquill.matchers.Matchers.pullMatchers;

public class StaticInvocation extends AbstractInvocationHandler {
    @BindingPriority(0)
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin Method method,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object ret,
            @Advice.AllArguments Object[] args
    ) throws Throwable {
        List<ArgumentMatcher<?>> matchers = pullMatchers();
        Invocation invocation = new Invocation(null, method, args, matchers, InvocationStorage.staticInvocationHandler);
        InvocationStorage.setLastInvocation(invocation);
        Stub stub = StaticInvocationHandler.INSTANCE.findStub(InvocationStorage.getLastInvocation());
//        System.out.println("STUBBED VALUE: " + stub);
        if (stub != null) {
            if (stub.exception()) {
                throw (Throwable) stub.value();
            } else {
                ret = stub.value();
            }
        }

        ret = getDefaultValue(method.getReturnType());
    }

    public static Object getDefaultValue(Class<?> returnType) {
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

    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        return getDefaultValue(method.getReturnType());
    }
}
