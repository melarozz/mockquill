package org.example.Invocation;

import org.example.AllMatchers.ArgumentMatcher;
import org.example.Stubbing.Stub;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс для invocation handlers (как для моков, так и для спаев).
 * Реализует общий алгоритм:
 * 1. Извлечение matchers;
 * 2. Создание объекта Invocation и сохранение его для stubbing;
 * 3. Поиск подходящего stub;
 * 4. Если stub найден, возвращается его значение (или выбрасывается исключение);
 * 5. Если stub не найден, вызывается абстрактный метод proceed, реализуемый подклассами.
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
        List<ArgumentMatcher<?>> matchers = org.example.AllMatchers.Matchers.pullMatchers();
        // Создаем объект Invocation для текущего вызова
        Invocation invocation = new Invocation(proxy, method, args, matchers, this);
        // Регистрируем вызов для stubbing
        org.example.MyMock.setLastInvocation(invocation);

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
