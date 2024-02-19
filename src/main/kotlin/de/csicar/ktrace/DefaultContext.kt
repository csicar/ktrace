package de.csicar.ktrace

import de.csicar.ktrace.tracer.ConsoleTracer
import de.csicar.ktrace.tracer.OpenTelemetryTracer
import de.csicar.ktrace.tracer.Tracer
import kotlin.random.Random

/**
 * Stores default configuration used by the tracing library in case some value is not provided
 * E.g. tracer, random, …
 */
object DefaultContext {
    var random = Random(Random.nextLong())

    var tracer : Tracer = ConsoleTracer(System.Logger.Level.TRACE)
}