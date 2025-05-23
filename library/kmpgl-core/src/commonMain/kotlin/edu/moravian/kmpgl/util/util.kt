@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package edu.moravian.kmpgl.util

inline fun Long.toIntOrFail(name: String): Int {
    require(this <= Int.MAX_VALUE) { "Long value $this of $name doesn't fit into 32-bit integer" }
    return toInt()
}

internal inline fun requirePositive(value: Int, name: String): Int {
    if (value < 0) throw IndexOutOfBoundsException("$name cannot be negative: $value")
    return value
}

inline fun requireIndex(index: Int, size: Int): Int {
    if (index !in 0 until size) { throw IndexOutOfBoundsException("index $index outside of [0; $size)") }
    return index
}

inline fun requireIndex(index: Int, size: Int, nbytes: Int): Int {
    if (index !in 0 .. (size-nbytes)) { throw IndexOutOfBoundsException("index $index of size $nbytes outside of [0; $size)") }
    return index
}

internal inline fun requireRange(offset: Int, length: Int, size: Int, name: String) {
    requirePositive(length, "length")
    if (offset !in 0 .. (size-length)) throw IndexOutOfBoundsException("range $name [$offset; ${offset+length}) outside of [0; $size)")
}
