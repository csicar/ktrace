package de.csicar.ktrace

import de.csicar.ktrace.tracer.Tracer
import io.opentelemetry.proto.common.v1.KeyValue
import java.lang.System.Logger.Level
import kotlin.random.Random
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString

suspend inline fun <T> Tracer.span(
    name: String,
    level: Level = Level.DEBUG,
    attributes: (LogMessageBuilder.() -> Unit) = {},
    enclosingScope: Scope? = null,
    crossinline block: suspend () -> T
): T {
  if (!enabled(level)) {
    return block()
  }

  val oldCurrentScope = currentScope.get()
  println("oldCurrentScope $oldCurrentScope")
  val ctxOrDefault = enclosingScope ?: oldCurrentScope
  val spanAttributes = mutableListOf<KeyValue>()
  LogMessageBuilder(spanAttributes).attributes()
  val traceId = ctxOrDefault?.traceId ?: DefaultContext.random.nextBytes(16).toByteString()
  val spanId = Random.nextSpanId()
  val parentId = ctxOrDefault?.spanId

  return this.span(name, spanAttributes, traceId, spanId, parentId) { innerScope ->
    currentScope.set(innerScope)
    val result = withContext(currentScope.asContextElement()) { block() }
    currentScope.set(oldCurrentScope)
    result
  }
}
