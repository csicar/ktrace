package de.csicar.ktrace.tracer

import de.csicar.ktrace.Scope
import de.csicar.ktrace.SpanId
import io.opentelemetry.proto.common.v1.KeyValue
import java.lang.System.Logger.Level
import okio.ByteString

interface Tracer {
  val minLevel: Level

  suspend fun <T> span(
      name: String,
      attributes: List<KeyValue>,
      traceId: ByteString,
      spanId: SpanId,
      parentSpan: SpanId?,
      block: suspend (Scope) -> T,
  ): T

  fun enabled(level: Level) = level.severity >= minLevel.severity
}
