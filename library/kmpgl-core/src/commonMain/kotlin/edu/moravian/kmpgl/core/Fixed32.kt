@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.core

import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

//////////////////// Fixed32 and Array Types ////////////////////
// This tries to match primitive numbers and arrays as best as possible.
// Example numbers:
//      0.0 = 0x00000000
//      1.0 = 0x00010000
//     -1.0 = 0xFFFF0000
//      0.5 = 0x00008000
//     -0.5 = 0xFFFF8000
//      1.5 = 0x00018000
//     -1.5 = 0xFFFE8000
//  32767.0 = 0x7FFF0000
//  32768.0 ~= 0x7FFFFFFF
// -32768.0 = 0x80000000

@JvmInline
value class Fixed32 private constructor(val raw: Int): Comparable<Fixed32> {
    // Constructors use unsafe conversions
    constructor(value: Byte) : this(raw = value.toInt() shl SHIFT_BITS)
    constructor(value: Short) : this(raw = value.toInt() shl SHIFT_BITS)
    // TODO: constructor(value: Int) : this(value shl SHIFT_BITS)
    constructor(value: Long) : this(raw = (value shl SHIFT_BITS).toInt())
    constructor(value: Float) : this(raw =
        if (value >= MAX_VALUE_FLOAT) { MAX_VALUE_RAW }
        else if (value <= MIN_VALUE_FLOAT) { MIN_VALUE_RAW }
        else if (value >= 0) { (value * 65536f).roundToInt() }
        else { ((value * -65536f).roundToInt()).inv() + 1 }
    )
    constructor(value: Double) : this(raw =
        if (value >= MAX_VALUE_FLOAT) { MAX_VALUE_RAW }
        else if (value <= MIN_VALUE_FLOAT) { MIN_VALUE_RAW }
        else if (value >= 0) { (value * 65536.0).roundToInt() }
        else { ((value * -65536.0).roundToInt()).inv() + 1 }
    )

    companion object {
        const val SIZE_BYTES = 4
        const val SIZE_BITS = 32
        const val SHIFT_BITS = 16

        private const val MAX_VALUE_FLOAT = 32768f
        private const val MIN_VALUE_FLOAT = -32768f

        private const val MAX_VALUE_RAW = 0x7FFFFFFF
        private const val MIN_VALUE_RAW = 0x80000000.toInt()

        private const val ONE_RAW = 0x00010000
        private val ONE = Fixed32(raw = ONE_RAW)

        private val ZERO = Fixed32(raw = 0x00000000)

        val MAX_VALUE = Fixed32(raw = MAX_VALUE_RAW)
        val MIN_VALUE = Fixed32(raw = MIN_VALUE_RAW)

        fun fromRaw(value: Int) = Fixed32(raw = value)

        // Safe conversions
        fun from(value: Byte) = Fixed32(raw = value.toInt() shl SHIFT_BITS)
        fun from(value: Short) = Fixed32(raw = value.toInt() shl SHIFT_BITS)
        fun from(value: Int) = when {
            value > 0x7FFF || value < -0x8000 -> throw IllegalArgumentException("Invalid value for Fixed32")
            else -> Fixed32(raw = value shl SHIFT_BITS)
        }
        fun from(value: Long) = when {
            value > 0x7FFF || value < -0x8000 -> throw IllegalArgumentException("Invalid value for Fixed32")
            else -> Fixed32(raw = (value shl SHIFT_BITS).toInt())
        }
        fun from(value: Float) = when {
            value.isNaN() || value > MAX_VALUE_FLOAT || value < MIN_VALUE_FLOAT ->
                throw IllegalArgumentException("Invalid value for Fixed32")
            value >= 0 -> Fixed32(raw = (value * 65536f).roundToInt())
            value == MIN_VALUE_FLOAT -> MIN_VALUE
            else -> Fixed32(raw = ((value * -65536f).roundToInt()).inv() + 1)
        }
        fun from(value: Double) = when {
            value.isNaN() || value > MAX_VALUE_FLOAT || value < MIN_VALUE_FLOAT ->
                throw IllegalArgumentException("Invalid value for Fixed32")
            value >= 0 -> Fixed32(raw = (value * 65536.0).roundToInt())
            value.toFloat() == MIN_VALUE_FLOAT -> MIN_VALUE
            else -> Fixed32(raw = ((value * -65536.0).roundToInt()).inv() + 1)
        }
    }

    // Convert fixed32 to float32/float64 (there will be precision loss, even double would have some loss)
    val float: Float get() = when {
        this.raw >= 0 -> { this.raw / 65536f }
        this.raw == MIN_VALUE_RAW -> { MIN_VALUE_FLOAT } // doesn't convert right due to the -1 in the equation
        else -> { (this.raw - 1).inv() / -65536f }
    }
    val double: Double get() = when {
        this.raw >= 0 -> { this.raw / 65536.0 }
        this.raw == MIN_VALUE_RAW -> { MIN_VALUE_FLOAT.toDouble() }
        else -> { (this.raw - 1).inv() / -65536.0 }
    }

    inline fun toByte() = toInt().toByte()
    inline fun toShort() = toInt().toShort()
    inline fun toInt() = when {
        raw >= 0 || fractionPart == 0 -> (raw shr SHIFT_BITS)
        else -> (raw shr SHIFT_BITS) + 1
    }
    inline fun toLong() = toInt().toLong()
    inline fun toFloat() = float
    inline fun toDouble() = double

    override operator fun compareTo(other: Fixed32) = raw.compareTo(other.raw)
    operator fun plus(other: Fixed32) = Fixed32(raw = raw + other.raw)
    operator fun minus(other: Fixed32) = Fixed32(raw = raw - other.raw)
    operator fun times(other: Fixed32) = Fixed32(raw = ((raw.toLong() * other.raw) shr SHIFT_BITS).toInt())
    operator fun div(other: Fixed32) = Fixed32(raw = ((raw.toLong() shl SHIFT_BITS) / other.raw).toInt())
    //operator fun rem(other: Fixed32) = Fixed32(raw = raw % other.raw)

    // for positive numbers or when there is no fraction, this is what you would expect
    // for negative numbers with fractional parts, this is what you would expect minus 1
    inline val intPart get() = raw shr SHIFT_BITS
    inline val fractionPart get() = raw and 0xFFFF

    inline operator fun compareTo(other: Byte) = this.compareTo(other.toInt())
    inline operator fun compareTo(other: Short) = this.compareTo(other.toInt())
    inline operator fun compareTo(other: Int): Int {
        val int = this.intPart
        return when {
            this.fractionPart == 0 -> int.compareTo(other)
            (int >= 0 && int >= other) || (int < 0 && int+1 > other) -> 1
            else -> -1
        }
    }
    inline operator fun compareTo(other: Long) = when {
        other > 0x7FFF -> -1
        other < -0x8000 -> 1
        else -> this.compareTo(other.toInt())
    }
    inline operator fun compareTo(other: Float) = this.compareTo(Fixed32(other))
    inline operator fun compareTo(other: Double) = this.compareTo(Fixed32(other))

    // no overflow checks on any of these
    operator fun plus(other: Byte) = Fixed32(raw = raw + (other.toInt() shl SHIFT_BITS))
    operator fun plus(other: Short) = Fixed32(raw = raw + (other.toInt() shl SHIFT_BITS))
    operator fun plus(other: Int) = Fixed32(raw = raw + (other shl SHIFT_BITS))
    operator fun plus(other: Long) = Fixed32(raw = (raw + (other shl SHIFT_BITS)).toInt())
    operator fun plus(other: Float) = Fixed32(float + other)
    operator fun plus(other: Double) = Fixed32(double + other)
    operator fun minus(other: Byte) = Fixed32(raw = raw - (other.toInt() shl SHIFT_BITS))
    operator fun minus(other: Short) = Fixed32(raw = raw - (other.toInt() shl SHIFT_BITS))
    operator fun minus(other: Int) = Fixed32(raw = raw - (other shl SHIFT_BITS))
    operator fun minus(other: Long) = Fixed32(raw = (raw - (other shl SHIFT_BITS)).toInt())
    operator fun minus(other: Float) = Fixed32(float - other)
    operator fun minus(other: Double) = Fixed32(double - other)
    operator fun times(other: Byte) = Fixed32(raw = raw * other.toInt())
    operator fun times(other: Short) = Fixed32(raw = raw * other.toInt())
    operator fun times(other: Int) = Fixed32(raw = raw * other)
    operator fun times(other: Long) = Fixed32(raw = (raw * other).toInt())
    operator fun times(other: Float) = Fixed32(float * other)
    operator fun times(other: Double) = Fixed32(double * other)
    operator fun div(other: Byte) = Fixed32(raw = raw / other.toInt())
    operator fun div(other: Short) = Fixed32(raw = raw / other.toInt())
    operator fun div(other: Int) = Fixed32(raw = raw / other)
    operator fun div(other: Long) = Fixed32(raw = (raw / other).toInt())
    operator fun div(other: Float) = Fixed32(float / other)
    operator fun div(other: Double) = Fixed32(double / other)
    //operator fun rem(other: Byte) = Fixed32(raw = raw % other.toInt())
    //operator fun rem(other: Short) = Fixed32(raw = raw % other.toInt())
    //operator fun rem(other: Int) = Fixed32(raw = raw % other)
    //operator fun rem(other: Long) = Fixed32(raw = (raw % other).toInt())
    //operator fun rem(other: Float) = Fixed32(float % other)
    //operator fun rem(other: Double) = Fixed32(double % other)
    operator fun inc() = Fixed32(raw = raw + ONE.raw)
    operator fun dec() = Fixed32(raw = raw - ONE.raw)
    operator fun unaryPlus() = this
    operator fun unaryMinus() = Fixed32(raw = -raw)

    infix fun shl(bitCount: Int) = Fixed32(raw = raw shl bitCount)
    infix fun shr(bitCount: Int) = Fixed32(raw = raw shr bitCount)
    infix fun ushr(bitCount: Int) = Fixed32(raw = raw ushr bitCount)
    infix fun and(other: Int) = Fixed32(raw = raw and other)
    infix fun or(other: Int) = Fixed32(raw = raw or other)
    infix fun xor(other: Int) = Fixed32(raw = raw xor other)
    fun inv() = Fixed32(raw = raw.inv())

    override fun toString(): String {
        val int = this.intPart
        val fracStr = fractionString()
        return if (int >= 0) "$int.$fracStr"
            else "${int+1}.$fracStr"
    }
    private fun fractionString(): String {
        val fraction = this.fractionPart
        return if (fraction == 0) "0"
            else "${fraction/65536f}".substring(2) // TODO: not good...
    }
    fun toHexString(): String {
        val str = raw.toString(16).padStart(8, '0')
        return "${str.substring(0, 4)}.${str.substring(4)}"
    }

    //override fun equals(other: Any?) = when {
    //    other is Fixed32 -> raw == other.raw
    //    other is Byte -> raw == other.toInt() shl SHIFT_BITS
    //    other is Short -> raw == other.toInt() shl SHIFT_BITS
    //    other is Int -> raw == other shl SHIFT_BITS
    //    other is Long -> raw == (other shl SHIFT_BITS).toInt()
    //    other is Float -> raw == Fixed32(raw = other).raw
    //    other is Double -> raw == Fixed32(raw = other).raw
    //    else -> false
    //}
}

@JvmInline
value class Fixed32Array(@JvmField val raw: IntArray): Iterable<Fixed32> {
    constructor(size: Int) : this(IntArray(size))
    constructor(size: Int, init: (Int) -> Fixed32) : this(IntArray(size) { init(it).raw })
    val size get() = raw.size
    inline operator fun get(index: Int): Fixed32 = Fixed32.fromRaw(raw[index])
    inline operator fun set(index: Int, value: Fixed32) { raw[index] = value.raw }
    override fun iterator(): Iterator<Fixed32> = object: Iterator<Fixed32> {
        var index = 0
        override fun hasNext() = index != size
        override fun next() = Fixed32.fromRaw(raw[index++])
    }
    fun floatIterator(): FloatIterator = object: FloatIterator() {
        var index = 0
        override fun hasNext() = index != size
        override fun nextFloat() = Fixed32.fromRaw(raw[index++]).float
    }
}
inline fun Fixed32Array.copyOf(newSize: Int = this.size) = Fixed32Array(this.raw.copyOf(newSize))
inline fun Fixed32Array.copyOfRange(fromIndex: Int, toIndex: Int) = Fixed32Array(this.raw.copyOfRange(fromIndex, toIndex))
inline fun Fixed32Array.copyInto(destination: Fixed32Array, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.size): Fixed32Array {
    this.raw.copyInto(destination.raw, destinationOffset, startIndex, endIndex)
    return destination
}
