package de.csicar.ktrace

import io.opentelemetry.proto.trace.v1.Span
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

suspend fun <T> Tracer.span(name: String,  attributes: (LogMessageBuilder.() -> Unit)? = null, ctx: Scope? = null, block: suspend () -> T): T {
    val oldCurrentScope = currentScope.get()
    println("oldCurrentScope $oldCurrentScope")
    val ctxOrDefault = ctx ?: oldCurrentScope
    val innerCtx =
        Scope(
            ctxOrDefault?.traceId ?: Random.nextBytes(16).toByteString(),
            Random.nextBytes(8).toByteString())
    currentScope.set(innerCtx)
    val start = Date().toUnixEpochNanos()
    val result = withContext(coroutineContext + currentScope.asContextElement()) { block() }
    val end = Date().toUnixEpochNanos()
    currentScope.set(oldCurrentScope)

    this.send(
        Span(
            trace_id = innerCtx.traceId,
            span_id = innerCtx.spanId,
            name = name,
            parent_span_id = ctxOrDefault?.spanId ?: ByteString.EMPTY,
            start_time_unix_nano = start,
            end_time_unix_nano = end,
            events = innerCtx.events)
    )
    return result
}
