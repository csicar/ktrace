package de.csicar.ktrace

import com.squareup.wire.GrpcClient
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.collector.trace.v1.TraceServiceClient
import io.opentelemetry.proto.resource.v1.Resource
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans
import io.opentelemetry.proto.trace.v1.Span
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

// https://github.com/Kotlin/kotlinx.coroutines/issues/1302#issuecomment-1416493795
fun <T> Flow<T>.chunked(maxSize: Int, interval: Duration) = channelFlow {
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
        delay(interval)
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

class Tracer(grpcClient: GrpcClient, val resource: Resource) {
  private val sharedFlow = MutableSharedFlow<Span>()
  private val traceServiceClient = grpcClient.create(TraceServiceClient::class)

  suspend fun send(span: Span) {
    println("sending $span")
    sharedFlow.emit(span)
    println("done sending")
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
