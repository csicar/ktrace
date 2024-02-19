import com.squareup.wire.GrpcClient
import com.squareup.wire.toHttpUrl
import de.csicar.ktrace.info
import de.csicar.ktrace.span
import de.csicar.ktrace.trace
import de.csicar.ktrace.tracer.CompositeTracer
import de.csicar.ktrace.tracer.ConsoleTracer
import de.csicar.ktrace.tracer.OpenTelemetryTracer
import io.kotest.core.spec.style.DescribeSpec
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.resource.v1.Resource
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Protocol

class IntegrationTest :
    DescribeSpec({
      xit("twörks") {
        val serverUrl = "http://localhost:4319".toHttpUrl()

        val grpcClient =
            GrpcClient.Builder()
                .client(
                    OkHttpClient.Builder().protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE)).build())
                .baseUrl(serverUrl)
                .build()

        val resource =
            Resource(
                listOf(
                    KeyValue("service.name", AnyValue("service asd")),
                    KeyValue("some_prop", AnyValue("prop_asd"))))

        runBlocking {
          val otelTracer =
              OpenTelemetryTracer(grpcClient, resource, System.Logger.Level.TRACE).also {
                launch { it.launch() }
              }
          val consoleTracer = ConsoleTracer(System.Logger.Level.TRACE)
          val tracer = CompositeTracer(otelTracer, consoleTracer)
          //              val tracer = otelTracer

          launch {
            tracer.span("asd span", System.Logger.Level.TRACE, { "asd" - "ads" }) {
              trace("some event") { "test" - "value" }
              delay(10)
              withContext(Dispatchers.IO) {
                tracer.span("inner", System.Logger.Level.DEBUG, { "asd" - "s" }) {
                  info("some inner thing with some stuff") { "test" - "asd" }
                }
              }
            }
          }
        }
      }
    })
