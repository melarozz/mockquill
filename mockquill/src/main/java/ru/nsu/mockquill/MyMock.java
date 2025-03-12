package ru.nsu.mockquill;

import ru.nsu.mockquill.invocation.Invocation;

/**
 * Maintains a thread–local record of the last intercepted invocation.
 */
public class MyMock {
    private static final ThreadLocal<Invocation> LAST_INVOCATION = new ThreadLocal<>();

    public static void setLastInvocation(Invocation invocation) {
        LAST_INVOCATION.set(invocation);
    }

    public static Invocation getLastInvocation() {
        return LAST_INVOCATION.get();
    }

    public static void clearLastInvocation() {
        LAST_INVOCATION.remove();
    }
}
