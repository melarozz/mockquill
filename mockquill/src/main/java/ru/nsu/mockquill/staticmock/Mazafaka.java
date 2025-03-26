package ru.nsu.mockquill.staticmock;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import ru.nsu.mockquill.MyMock;
import ru.nsu.mockquill.invocation.AbstractInvocationHandler;
import ru.nsu.mockquill.invocation.Invocation;
import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.stub.Stub;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static ru.nsu.mockquill.matchers.Matchers.pullMatchers;

public class Mazafaka extends AbstractInvocationHandler {
    @Override
    protected Object proceed(Method method, Object[] args) throws Throwable {
        return getDefaultValue(method.getReturnType());
    }

    @BindingPriority(0)
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin Method method,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object ret,
            @Advice.AllArguments Object[] args
    ) throws Throwable {
        List<ArgumentMatcher<?>> matchers = pullMatchers();
        Invocation invocation = new Invocation(null, method, args, matchers, MyMock.sih);
        MyMock.setLastInvocation(invocation);

//        if () {
//            throw new RuntimeException("Mockquill internal exception! All arguments must be either matchers or values. To use values with matchers consider to use eq() matcher");
//        }

        Stub stub = MyMock.sih.findStub(MyMock.getLastInvocation());
        if (stub != null) {
            if (stub.exception()) {
                throw (Throwable) stub.value();
            } else {
                ret = stub.value();
            }
        }
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
}
