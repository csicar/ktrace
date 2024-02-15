import com.squareup.wire.GrpcClient
import com.squareup.wire.toHttpUrl
import de.csicar.ktrace.*
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.resource.v1.Resource
import kotlin.random.Random
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.ByteString.Companion.toByteString

fun main() {
  val serverUrl = "http://localhost:4319".toHttpUrl()

  val grpcClient =
      GrpcClient.Builder()
          .client(OkHttpClient.Builder().protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE)).build())
          .baseUrl(serverUrl)
          .build()

  val resource =
      Resource(
          listOf(
              KeyValue("service.name", AnyValue("service asd")),
              KeyValue("some_prop", AnyValue("prop_asd"))))
  val tracer = Tracer(grpcClient, resource)

  runBlocking {
    launch { tracer.launch() }

    launch {
      val traceId = Random.nextBytes(16).toByteString()
      val spanId = Random.nextBytes(8).toByteString()

      tracer.span("asd span", { "asd" - "ads" }) {
        trace("some event") { "test" - "value" }
        delay(10)
        withContext(Dispatchers.IO) {
          tracer.span("inner") { info("some inner thing with some stuff") { "test" - "asd" } }
        }
      }
    }
  }
}
