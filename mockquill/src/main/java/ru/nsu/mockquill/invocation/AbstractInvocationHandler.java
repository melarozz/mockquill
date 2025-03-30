package ru.nsu.mockquill.invocation;

import ru.nsu.mockquill.InvocationStorage;
import ru.nsu.mockquill.matchers.ArgumentMatcher;
import ru.nsu.mockquill.stub.Stub;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static ru.nsu.mockquill.matchers.Matchers.pullMatchers;

/**
 * Base class for our invocation handlers (for both mocks and spies).
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {
    protected List<Stub> stubs = new ArrayList<>();

    public void addStub(Invocation invocation, Object value, boolean isException) {
        stubs.add(new Stub(invocation, value, isException));
    }

    protected Stub findStub(Invocation currentInvocation) {
        for (Stub stub : stubs) {
            if (stub.invocation().matches(currentInvocation)) {
                return stub;
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Извлекаем matchers
        List<ArgumentMatcher<?>> matchers = pullMatchers();
        // Создаем объект Invocation для текущего вызова
        Invocation invocation = new Invocation(new Object(), method, args, matchers, this);
        // Регистрируем вызов для stubbing
        InvocationStorage.setLastInvocation(invocation);

        // Если найден stub, возвращаем его значение (либо выбрасываем исключение)
        Stub stub = findStub(invocation);
        if (stub != null) {
            if (stub.exception()) {
                throw (Throwable) stub.value();
            } else {
                return stub.value();
            }
        }
        // Если stub не найден, делегируем выполнение конкретной реализации
        return proceed(method, args);
    }

    /**
     * Абстрактный метод, реализуемый подклассами для обработки вызова,
     * если для него не настроена заглушка.
     */
    protected abstract Object proceed(Method method, Object[] args) throws Throwable;
}
