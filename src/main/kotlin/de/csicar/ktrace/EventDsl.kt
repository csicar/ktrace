package de.csicar.ktrace

import de.csicar.ktrace.tracer.getSourceCodeLocation
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
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
    builder: LogMessageBuilder.() -> Unit = {}
) {
  val oldCurrentScope = scope ?: currentScope.get() ?: return
  if (oldCurrentScope.minLevel > level) {
    return
  }
  val attributes = mutableListOf<KeyValue>()
  LogMessageBuilder(attributes).builder()
  oldCurrentScope.log(name, level, attributes)
}

inline fun trace(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit = {}) =
    log(System.Logger.Level.TRACE, name, scope, builder)

inline fun debug(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit = {}) =
    log(System.Logger.Level.DEBUG, name, scope, builder)

inline fun info(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit = {}) =
    log(System.Logger.Level.INFO, name, scope, builder)

inline fun warn(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit = {}) =
    log(System.Logger.Level.WARNING, name, scope, builder)

inline fun error(name: String, scope: Scope? = null, builder: LogMessageBuilder.() -> Unit = {}) =
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
