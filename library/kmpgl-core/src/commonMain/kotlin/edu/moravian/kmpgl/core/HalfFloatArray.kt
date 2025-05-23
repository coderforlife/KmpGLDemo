@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.core

import kotlin.jvm.JvmField

// TODO: make this more robust like the Fixed32 type


//////////////////// Half Float Array Type ////////////////////
// We don't actually have a half float type, instead we use shorts that are
// converted as needed. This tries to match primitive arrays as best as possible.
class HalfFloatArray(@JvmField val raw: ShortArray): Iterable<Float> {
    constructor(size: Int) : this(ShortArray(size))
    constructor(size: Int, init: (Int) -> Float) : this(ShortArray(size) { init(it).toHalfFloat() })
    @JvmField
    val size = raw.size
    inline operator fun get(index: Int): Float = raw[index].fromHalfFloat()
    inline operator fun set(index: Int, value: Float) { raw[index] = value.toHalfFloat() }
    override fun iterator(): FloatIterator = object: FloatIterator() {
        var index = 0
        override fun hasNext() = index != size
        override fun nextFloat() = raw[index++].fromHalfFloat()
    }
}
inline fun HalfFloatArray.copyOf(newSize: Int = this.size) = HalfFloatArray(this.raw.copyOf(newSize))
inline fun HalfFloatArray.copyOfRange(fromIndex: Int, toIndex: Int) = HalfFloatArray(this.raw.copyOfRange(fromIndex, toIndex))
inline fun HalfFloatArray.copyInto(destination: HalfFloatArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.size): HalfFloatArray {
    this.raw.copyInto(destination.raw, destinationOffset, startIndex, endIndex)
    return destination
}


// No real Half-Float type, just used for constants
class HalfFloat private constructor() {
    companion object {
        const val SIZE_BYTES = 2
        const val SIZE_BITS = 16

        const val MAX_VALUE = 65504f
        const val MIN_VALUE = -65504f

        const val RAW_MAX_VALUE = 0b0_11110_1111111111.toShort()
        const val RAW_MIN_VALUE = 0b1_11110_1111111111.toShort()
        const val RAW_NEGATIVE_INFINITY = 0b1_11111_0000000000.toShort()
        const val RAW_POSITIVE_INFINITY = 0b0_11111_0000000000.toShort()
        const val RAW_NaN = 0b0_11111_0000000001.toShort() // one example
    }
}


// Convert float32 to float16
fun Float.toHalfFloat(): Short {
    if (this > HalfFloat.MAX_VALUE) {
        //Log.w("toHalfFloat(): value out of range.")
        return HalfFloat.RAW_MAX_VALUE
    } else if (this < HalfFloat.MIN_VALUE) {
        //Log.w("toHalfFloat(): value out of range.")
        return HalfFloat.RAW_MIN_VALUE
    }
    val f = this.toBits()
    val e = (f shr 23) and 0x1ff
    return (_tables.baseTable[e] + ((f and 0x7fffff) shr _tables.shiftTable[e])).toShort()
}

// Convert float16 to float32
inline fun Short.fromHalfFloat() = this.toInt().fromHalfFloat()
fun Int.fromHalfFloat(): Float {
    val m = this shr 10
    return Float.fromBits(_tables.mantissaTable[_tables.offsetTable[m] + (this and 0x3ff)] + _tables.exponentTable[m])
}

// Helper
private object _tables {
    val baseTable = IntArray(512)
    val shiftTable = IntArray(512)
    init {
        for (i in 0 until 256) {
            val e = i - 127
            if (e < -27) {
                // very small number (0, -0)
                baseTable[i] = 0x0000
                baseTable[i or 0x100] = 0x8000
                shiftTable[i] = 24
                shiftTable[i or 0x100] = 24
            } else if (e < -14) {
                // small number (denorm)
                baseTable[i] = 0x0400 shr (-e-14)
                baseTable[i or 0x100] = (0x0400 shr (-e-14)) or 0x8000
                shiftTable[i] = -e-1
                shiftTable[i or 0x100] = -e-1
            } else if (e <= 15) {
                // normal number
                baseTable[i] = (e+15) shl 10
                baseTable[i or 0x100] = ((e+15) shl 10) or 0x8000
                shiftTable[i] = 13
                shiftTable[i or 0x100] = 13
            } else if ( e < 128 ) {
                // large number (Infinity, -Infinity)
                baseTable[i] = 0x7c00
                baseTable[i or 0x100] = 0xfc00
                shiftTable[i] = 24
                shiftTable[i or 0x100] = 24
            } else {
                // stay (NaN, Infinity, -Infinity)
                baseTable[i] = 0x7c00
                baseTable[i or 0x100 ] = 0xfc00
                shiftTable[i] = 13
                shiftTable[i or 0x100 ] = 13
            }
        }
    }

    val offsetTable = IntArray(64) { if (it == 32) 0 else 1024 }

    val mantissaTable = IntArray(2048)
    init {
        for (i in 1 until 1024) {
            var m = i shl 13 // zero pad mantissa bits
            var e = 0 // zero exponent
            // normalized
            while ((m and 0x800000) == 0) { m = m shl 1; e -= 0x800000 } // decrement exponent
            m = m and 0x800000.inv() // clear leading 1 bit
            e += 0x38800000 // adjust bias
            mantissaTable[i] = m or e
        }

        for (i in 1024 until 2048) { mantissaTable[i] = 0x38000000 + ((i-1024) shl 13) }
    }

    val exponentTable = IntArray(64)
    init {
        for (i in 1 until 31) { exponentTable[i] = i shl 23 }
        exponentTable[31] = 0x47800000
        exponentTable[32] = 0x80000000.toInt()
        for (i in 33 until 63) { exponentTable[i] = 0x80000000.toInt() + ((i-32) shl 23) }
        exponentTable[63] = 0xc7800000.toInt()
    }
}

