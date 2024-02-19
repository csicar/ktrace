package de.csicar.ktrace

import okio.ByteString
import okio.ByteString.Companion.toByteString
import kotlin.random.Random

@JvmInline value class SpanId(val id: ByteString)

fun Random.nextSpanId() = SpanId(nextBytes(8).toByteString())