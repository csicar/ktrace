package de.csicar.ktrace

import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.trace.v1.Span
import java.util.*

class LogMessageBuilder(private val keyValues: MutableList<KeyValue>) {

  operator fun String.minus(value: AnyValue) = keyValues.add(KeyValue(this, value))

  operator fun String.minus(value: String) = keyValues.add(KeyValue(this, AnyValue(value)))
}

/** @sample Sample.main */
inline fun log(
    level: System.Logger.Level,
    name: String,
    scope: Scope? = null,
    builder: LogMessageBuilder.() -> Unit
) {
  val oldCurrentScope = getCurrentScopeOrCreate(scope)
  val attributes = mutableListOf<KeyValue>(KeyValue("level", AnyValue(level.getName())))
  val logMessageName = LogMessageBuilder(attributes).builder()
  oldCurrentScope.events.add(
      Span.Event(time_unix_nano = Date().toUnixEpochNanos(), name = name, attributes = attributes))
}

inline fun log(level: System.Logger.Level, name: String, scope: Scope? = null) =
    log(level, name, scope) {}

inline fun trace(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit) =
    log(System.Logger.Level.TRACE, name, scope, builder)

inline fun debug(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit) =
    log(System.Logger.Level.DEBUG, name, scope, builder)

inline fun info(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit) =
    log(System.Logger.Level.INFO, name, scope, builder)

inline fun warn(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit) =
    log(System.Logger.Level.WARNING, name, scope, builder)

inline fun error(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit) =
    log(System.Logger.Level.ERROR, name, scope, builder)

private object Sample {
  fun LogMessageBuilder.callId(value: String) = "callId" - value

  fun main() {
    val callId = "asd"
    val gateId = "asdd"
    trace("asd") {
      callId(callId)
      "gateId" - gateId
    }
  }
}
