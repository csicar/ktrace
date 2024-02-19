import com.squareup.wire.GrpcClient
import com.squareup.wire.toHttpUrl
import de.csicar.ktrace.*
import de.csicar.ktrace.tracer.CompositeTracer
import de.csicar.ktrace.tracer.ConsoleTracer
import de.csicar.ktrace.tracer.OpenTelemetryTracer
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.resource.v1.Resource
import kotlin.random.Random
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.ByteString.Companion.toByteString

fun main() {

}
