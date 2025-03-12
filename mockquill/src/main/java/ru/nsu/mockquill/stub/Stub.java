package ru.nsu.mockquill.stub;

import ru.nsu.mockquill.invocation.Invocation;

/**
 * Represents a stubbed behavior.
 */
public record Stub(Invocation invocation, Object value, boolean exception) {
}
