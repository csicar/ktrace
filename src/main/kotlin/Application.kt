import com.squareup.wire.GrpcClient
import com.squareup.wire.toHttpUrl
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.collector.trace.v1.TraceServiceClient
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.resource.v1.Resource
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans
import io.opentelemetry.proto.trace.v1.Span
import java.util.*
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.ByteString.Companion.toByteString

suspend fun main() {
  val serverUrl = "http://localhost:4319".toHttpUrl()

  val grpcClient =
      GrpcClient.Builder()
          .client(OkHttpClient.Builder().protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE)).build())
          .baseUrl(serverUrl)
          .build()
  val routeGuideClient = grpcClient.create(TraceServiceClient::class)

  val traceId = Random.nextBytes(16).toByteString()
  val spanId = Random.nextBytes(8).toByteString()

  routeGuideClient
      .Export()
      .execute(
          ExportTraceServiceRequest(
              listOf(
                  ResourceSpans(
                      Resource(listOf(KeyValue("service.name", AnyValue("service asd")))),
                      scope_spans =
                          listOf(
                              ScopeSpans(
                                  spans =
                                      listOf(
                                          Span(
                                              trace_id = traceId,
                                              span_id = spanId,
                                              start_time_unix_nano = Date().toUnixEpochNanos(),
                                              end_time_unix_nano =
                                                  Date().toUnixEpochNanos() + 100_000,
                                              events =
                                                  listOf(
                                                      Span.Event(
                                                          Date().toUnixEpochNanos(),
                                                          name = "event asd"))))))))))
}

fun Date.toUnixEpochNanos(): Long = this.time * 1000000
