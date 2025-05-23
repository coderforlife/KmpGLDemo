@file:OptIn(ExperimentalUnsignedTypes::class)

/**
 * Type safe wrappers for primitive arrays (or Memory).
 */

package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.core.Fixed32
import edu.moravian.kmpgl.core.Fixed32Array
import edu.moravian.kmpgl.core.HalfFloat
import edu.moravian.kmpgl.core.HalfFloatArray
import kotlin.jvm.JvmInline

/**
 * Any type that is bufferable including Memory and primitive arrays.
 * There are extension methods provided in OS-specific code.
 */
sealed interface Bufferable {
    val size: Int
    val nbytes: Int
    val elementSize: Int
}

sealed interface PrimitiveArray: Bufferable {
    val array: Any
    override val nbytes get() = size * elementSize
}

@JvmInline
value class BytePrimitiveArray(override val array: ByteArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Byte.SIZE_BYTES
}
fun ByteArray.asBufferable() = BytePrimitiveArray(this)

@JvmInline
value class UBytePrimitiveArray(override val array: UByteArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = UByte.SIZE_BYTES
}
fun UByteArray.asBufferable() = UBytePrimitiveArray(this)

@JvmInline
value class ShortPrimitiveArray(override val array: ShortArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Short.SIZE_BYTES
}
fun ShortArray.asBufferable() = ShortPrimitiveArray(this)

@JvmInline
value class UShortPrimitiveArray(override val array: UShortArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = UShort.SIZE_BYTES
}
fun UShortArray.asBufferable() = UShortPrimitiveArray(this)

@JvmInline
value class IntPrimitiveArray(override val array: IntArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Int.SIZE_BYTES
}
fun IntArray.asBufferable() = IntPrimitiveArray(this)

@JvmInline
value class UIntPrimitiveArray(override val array: UIntArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = UInt.SIZE_BYTES
}
fun UIntArray.asBufferable() = UIntPrimitiveArray(this)

@JvmInline
value class LongPrimitiveArray(override val array: LongArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Long.SIZE_BYTES
}
fun LongArray.asBufferable() = LongPrimitiveArray(this)

@JvmInline
value class ULongPrimitiveArray(override val array: ULongArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = ULong.SIZE_BYTES
}
fun ULongArray.asBufferable() = ULongPrimitiveArray(this)

@JvmInline
value class FloatPrimitiveArray(override val array: FloatArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Float.SIZE_BYTES
}
fun FloatArray.asBufferable() = FloatPrimitiveArray(this)

@JvmInline
value class DoublePrimitiveArray(override val array: DoubleArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Double.SIZE_BYTES
}
fun DoubleArray.asBufferable() = DoublePrimitiveArray(this)

@JvmInline
value class Fixed32PrimitiveArray(override val array: Fixed32Array): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = Fixed32.SIZE_BYTES
}
fun Fixed32Array.asBufferable() = Fixed32PrimitiveArray(this)

@JvmInline
value class HalfFloatPrimitiveArray(override val array: HalfFloatArray): PrimitiveArray {
    override val size get() = array.size
    override val elementSize get() = HalfFloat.SIZE_BYTES
}
fun HalfFloatArray.asBufferable() = HalfFloatPrimitiveArray(this)
