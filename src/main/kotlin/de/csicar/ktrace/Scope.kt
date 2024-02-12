package de.csicar.ktrace

import io.opentelemetry.proto.trace.v1.Span
import kotlin.random.Random
import okio.ByteString
import okio.ByteString.Companion.toByteString

class Scope(val traceId: ByteString, val spanId: ByteString) {
  val events: MutableList<Span.Event> = mutableListOf()
}

val currentScope: ThreadLocal<Scope?> =
    ThreadLocal.withInitial {
      // Is executed, when no scope is started on the current thread
      null
    }

/**
 * Gets the `currentScope` if one is already active. If not
 * 1. sets and returns the [explicitScope] (if provided)
 * 2. generates a new [Scope] with random `traceId` and `spanId`
 */
fun getCurrentScopeOrCreate(explicitScope: Scope? = null): Scope {
  val currentActiveScope = currentScope.get()
  if (currentActiveScope != null) {
      return currentActiveScope
  }

  val newScopeToOpen =
      explicitScope
          ?: Scope(Random.nextBytes(16).toByteString(), Random.nextBytes(8).toByteString())

  currentScope.set(newScopeToOpen)

  return newScopeToOpen
}
