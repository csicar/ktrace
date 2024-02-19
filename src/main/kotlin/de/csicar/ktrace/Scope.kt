package de.csicar.ktrace

import io.opentelemetry.proto.common.v1.KeyValue
import java.lang.System.Logger.Level
import okio.ByteString

/**
 * Represents the state of a currently active Span with the id [spanId] in trace [traceId] [Scope]
 * is stored ThreadLocally and in the CoroutineScope. [Tracer]s have specific implementation of this
 * associated with them and use [Scope]s to append events to their history. [minLevel] is used to
 * decide if an event should be generated at all. It is usually
 */
class Scope(
    val traceId: ByteString,
    val spanId: SpanId,
    val minLevel: Level,
    val log: (eventName: String, level: Level, eventAttributes: List<KeyValue>) -> Unit
)

val currentScope: ThreadLocal<Scope?> =
    ThreadLocal.withInitial {
      // Is executed, when no scope is started on the current thread
      null
    }
