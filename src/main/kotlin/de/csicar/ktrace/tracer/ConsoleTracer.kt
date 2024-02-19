package de.csicar.ktrace.tracer

import com.github.ajalt.colormath.model.Ansi16
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Text
import de.csicar.ktrace.Scope
import de.csicar.ktrace.SpanId
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import java.lang.System.Logger.Level
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import okio.ByteString

class ConsoleTracer(override val minLevel: Level, val terminal: Terminal = Terminal()) : Tracer {

  override suspend fun <T> span(
      name: String,
      attributes: List<KeyValue>,
      traceId: ByteString,
      spanId: SpanId,
      parentSpan: SpanId?,
      block: suspend (Scope) -> T
  ): T {
    val styleForLevel = style(Level.TRACE)
    terminal.println(
        grid {
          row(
              "  ",
              formatInstant(Instant.now()),
              styleForLevel(Level.TRACE.name),
              (TextStyles.bold + styleForLevel)("entering $name"),
              styleForLevel(printInlineAttributes(attributes)))
        })
    val innerScope =
        Scope(traceId, spanId, minLevel) { eventName, level, eventAttributes ->
          terminal.println(
              grid {
                row(
                    "  ",
                    formatInstant(Instant.now()),
                    Text(style(level)(level.name)),
                    (TextStyles.bold + style(level))(name),
                    style(level)(printInlineAttributes(eventAttributes)))
                row(TextStyles.dim("at "), white(getSourceCodeLocation().toString()))
              })
        }
    return block(innerScope)
  }

  fun printInlineAttributes(attributes: List<KeyValue>) =
      attributes.joinToString(", ") { TextStyles.bold(it.key + ": ") + printAnyValue(it.value_) }

  fun printAnyValue(value: AnyValue?): String {
    return if (value == null) {
      return "-"
    } else if (value.bool_value !== null) {
      value.bool_value.toString()
    } else if (value.string_value !== null) {
      value.string_value // TODO escape
    } else {
      TODO(value.toString())
    }
  }

  val purple = TextStyle(Ansi16(35))

  fun style(level: Level): TextStyle {
    // https://docs.rs/tracing-subscriber/latest/src/tracing_subscriber/fmt/format/pretty.rs.html#144
    return when (level) {
      Level.ALL -> green
      Level.TRACE -> purple
      Level.DEBUG -> blue
      Level.INFO -> green
      Level.WARNING -> yellow
      Level.ERROR -> red
      Level.OFF -> TextStyles.dim + purple
    }
  }

  private val dateFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

  private fun formatInstant(instant: Instant): String {
    return instant.atZone(ZoneId.systemDefault()).format(dateFormatter)
  }
}

fun getSourceCodeLocation(): StackTraceElement? {
  // Get the current stack trace
  val stackTrace = Thread.currentThread().stackTrace
  stackTrace.forEach { println(it) }
  // Find the first element in the stack trace that represents a line of code in the source files
  return stackTrace.drop(10).firstOrNull() { element ->
    val fileName = element.fileName
    fileName != null && !fileName.startsWith("Runtime") && !fileName.startsWith("Proxy")
  }
}
