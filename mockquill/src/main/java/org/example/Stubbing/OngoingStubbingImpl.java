package org.example.Stubbing;

import org.example.Invocation.Invocation;

/**
 * Implementation of the stubbing API.
 */
public class OngoingStubbingImpl<T> implements OngoingStubbing<T> {
    private final Invocation invocation;

    public OngoingStubbingImpl(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public void thenReturn(T value) {
        invocation.getHandler().addStub(invocation, value, false);
    }

    @Override
    public void thenThrow(Throwable throwable) {
        invocation.getHandler().addStub(invocation, throwable, true);
    }
}
