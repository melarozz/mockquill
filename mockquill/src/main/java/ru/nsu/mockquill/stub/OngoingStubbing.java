package ru.nsu.mockquill.stub;

/**
 * API to set stubbed behavior.
 */
public interface OngoingStubbing<T> {
    /**
     * Stub a return value.
     */
    void thenReturn(T value);

    /**
     * Stub an exception to be thrown.
     */
    void thenThrow(Throwable throwable);
}
