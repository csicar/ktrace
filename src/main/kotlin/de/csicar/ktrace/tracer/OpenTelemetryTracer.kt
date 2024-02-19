package de.csicar.ktrace.tracer

import com.squareup.wire.GrpcClient
import de.csicar.ktrace.*
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.collector.trace.v1.TraceServiceClient
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.resource.v1.Resource
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans
import io.opentelemetry.proto.trace.v1.Span
import java.lang.System.Logger.Level
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import okio.ByteString

class OpenTelemetryTracer(
    grpcClient: GrpcClient,
    val resource: Resource,
    override val minLevel: Level
) : Tracer {
  private val sharedFlow = MutableSharedFlow<Span>()
  private val traceServiceClient = grpcClient.create(TraceServiceClient::class)

  override suspend fun <T> span(
      name: String,
      attributes: List<KeyValue>,
      traceId: ByteString,
      spanId: SpanId,
      parentSpan: SpanId?,
      block: suspend (Scope) -> T,
  ): T {
    val events = mutableListOf<Span.Event>()
    val innerScope =
        Scope(traceId, spanId, minLevel) { eventName, level, eventAttributes ->
          val levelAttribute = KeyValue("level", AnyValue(level.name))
          events.add(Span.Event(Date().toUnixEpochNanos(), eventName, eventAttributes + listOf(levelAttribute)))
        }
    val start = Date().toUnixEpochNanos()
    val result = withContext(currentScope.asContextElement()) { block(innerScope) }
    val end = Date().toUnixEpochNanos()

    this.send(
        Span(
            trace_id = innerScope.traceId,
            span_id = innerScope.spanId.id,
            name = name,
            parent_span_id = parentSpan?.id ?: ByteString.EMPTY,
            start_time_unix_nano = start,
            end_time_unix_nano = end,
            events = events,
            attributes = attributes))
    return result
  }

  private suspend fun send(span: Span) {
    sharedFlow.emit(span)
  }

  suspend fun launch() {
    sharedFlow.chunked(256, 5.seconds).collect { span ->
      val request =
          ExportTraceServiceRequest(
              listOf(ResourceSpans(resource, scope_spans = listOf(ScopeSpans(spans = span)))))
      traceServiceClient.Export().execute(request)
    }
  }
}

/**
 * Implementation for buffering a flow with limits on both [maxDelay] and maximum number of buffered
 * elements [maxSize]. One either of these limits is reached, a `List<T>` with the buffered elements
 * is `emit`ted. Implementation based on
 * [GitHub issue](https://github.com/Kotlin/kotlinx.coroutines/issues/1302#issuecomment-1416493795)
 */
fun <T> Flow<T>.chunked(maxSize: Int, maxDelay: Duration) = channelFlow {
  val buffer = ArrayList<T>(maxSize)
  var flushJob: Job? = null

  collect { value ->
    flushJob?.cancelAndJoin()
    buffer.add(value)

    if (buffer.size >= maxSize) {
      send(buffer.toList())
      buffer.clear()
    } else {
      flushJob = launch {
        delay(maxDelay)
        if (buffer.isNotEmpty()) {
          send(buffer.toList())
          buffer.clear()
        }
      }
    }
  }

  flushJob?.cancelAndJoin()

  if (buffer.isNotEmpty()) {
    send(buffer.toList())
    buffer.clear()
  }
}
