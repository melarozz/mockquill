package org.example.Stubbing;

import org.example.Invocation.Invocation;

/**
 * Represents a stubbed behavior.
 */
public record Stub(Invocation invocation, Object value, boolean exception) {
}
