package de.csicar.ktrace.tracer

import de.csicar.ktrace.Scope
import de.csicar.ktrace.SpanId
import io.opentelemetry.proto.common.v1.KeyValue
import okio.ByteString

class CompositeTracer(
    private val first: Tracer,
    private val second: Tracer,
) : Tracer {

  override val minLevel = maxOf(first.minLevel, second.minLevel)

  override suspend fun <T> span(
      name: String,
      attributes: List<KeyValue>,
      traceId: ByteString,
      spanId: SpanId,
      parentSpan: SpanId?,
      block: suspend (Scope) -> T
  ): T {
    return first.span(name, attributes, traceId, spanId, parentSpan) {firstScope ->
      second.span(name, attributes, traceId, spanId, parentSpan) {secondScope ->
          val innerScope = Scope(traceId, spanId, minLevel) { eventName, level, eventAttributes ->
              firstScope.log(eventName, level, eventAttributes)
              secondScope.log(eventName, level, eventAttributes)
          }
          block(innerScope)
      }
    }
  }
}
