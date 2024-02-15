package de.csicar.ktrace

import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.Span
import java.lang.System.Logger.Level
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import okio.ByteString
import okio.ByteString.Companion.toByteString

interface Subscriber {
  fun enabled(level: Level): Boolean

  fun newSpan(span: OpenSpan): SpanId

  fun event(event: Span.Event, spanId: SpanId)

  fun enter(spanId: SpanId)

  fun exit(spanId: SpanId)
}

fun intToByteArray(value: Int): ByteString {
  val buffer = ByteBuffer.allocate(4)

  buffer.putInt(value)

  return buffer.array().toByteString()
}

@JvmInline value class SpanId(val id: Int)

data class OpenSpan(
    val name: String,
    val events: MutableList<Span.Event> = mutableListOf(),
    val attributes: MutableList<KeyValue> = mutableListOf(),
    val parentSpan: SpanId? = null,
)

data class IdGenerator(private val nextId: AtomicInteger = AtomicInteger(0)) {
  fun generate() = nextId.incrementAndGet().let { SpanId(it) }
}

class ConsoleSubscriber(val terminal: Terminal = Terminal(), val minLevel: Level = Level.ALL) :
    Subscriber {
  val openSpans = ConcurrentHashMap<SpanId, OpenSpan>()
  val idGenerator = IdGenerator()

  override fun enabled(level: Level): Boolean = level >= minLevel

  override fun newSpan(span: OpenSpan): SpanId {
    val freshId = idGenerator.generate()
    openSpans.set(freshId, span)
    return freshId
  }

  private tailrec fun getParents(spanId: SpanId, list: MutableList<OpenSpan>) {
    val openSpan = openSpans.get(spanId) ?: return
    list.add(openSpan)
    if (openSpan.parentSpan != null) {
      getParents(openSpan.parentSpan, list)
    }
  }

  override fun event(event: Span.Event, spanId: SpanId) {
    val span =
        openSpans.compute(spanId) { _, value ->
          value!!.events.add(event)
          value
        }

    val parents = mutableListOf<OpenSpan>()
    getParents(spanId, parents)

    terminal.println(
        table {
          body {

            row("name", "values")
            parents.forEach { row(it.name) }
            row("event", event)
          }

        })
  }

  override fun enter(spanId: SpanId) {
    terminal.println(grid { row("entering span $spanId") })
  }

  override fun exit(spanId: SpanId) {
    terminal.println(grid { row("exiting span $spanId") })
  }
}

inline fun test(block: () -> Unit) {
  try {
    println("in try")

    block()
    println("after block")
  } finally {

    println("in finally")
  }
}

fun main() {
  val res = test {
    println("in block")
    return
  }
  println("result from test $res")

  val asd = ConsoleSubscriber()
  val id = asd.newSpan(OpenSpan("top"))
  asd.enter(id)
  asd.event(Span.Event(name = "asd"), id)
  val id2 = asd.newSpan(OpenSpan("top"))
  asd.enter(id2)
  asd.event(Span.Event(name = "as2d"), id)
  asd.exit(id2)
  asd.exit(id)
}
