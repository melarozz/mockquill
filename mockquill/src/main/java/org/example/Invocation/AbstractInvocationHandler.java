package org.example.Invocation;

import org.example.Stubbing.Stub;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;

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
}
