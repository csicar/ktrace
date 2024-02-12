package de.csicar.ktrace

import java.util.*

fun Date.toUnixEpochNanos(): Long = this.time * 1000000