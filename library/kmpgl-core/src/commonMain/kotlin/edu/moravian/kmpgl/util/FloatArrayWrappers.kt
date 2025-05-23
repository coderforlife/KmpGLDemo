@file:OptIn(ExperimentalUnsignedTypes::class)

package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.core.DataType
import edu.moravian.kmpgl.core.GLES3

/**
 * Basic class that acts like a FloatArray but is wrapping any typed array.
 * Also supports normalized values.
 */
sealed interface FloatArrayWrapper: Iterable<Float> {
    val size: Int
    val elementSize: Int
    operator fun get(index: Int): Float
    operator fun set(index: Int, value: Float)
    override fun iterator() = object: FloatIterator() {
        private var index = 0
        override fun hasNext() = index < size
        override fun nextFloat() = get(index++)
    }
}

@OptIn(GLES3::class)
fun FloatArrayWrapper(
    array: PrimitiveArray,
    type: DataType,
    normalize: Boolean = false
) = when (type) {
    DataType.FLOAT -> array.array.let { arr -> when (arr) {
        is FloatArray -> FloatsFromFloatArray(arr)
        is IntArray -> FloatsIntBitsArray(arr)
        is UIntArray -> FloatsIntBitsArray(arr.asIntArray())
        else -> error("Unsupported data type")
    } }
    DataType.BYTE -> array.array.let { arr -> when (arr) {
        is ByteArray -> if (normalize) FloatsFromByteArrayNormalized(arr) else FloatsFromByteArray(arr)
        is UByteArray -> if (normalize) FloatsFromByteArrayNormalized(arr.asByteArray()) else FloatsFromByteArray(arr.asByteArray())
        else -> error("Unsupported data type")
    } }
    DataType.UNSIGNED_BYTE -> array.array.let { arr -> when (arr) {
        is UByteArray -> if (normalize) FloatsFromUByteArrayNormalized(arr) else FloatsFromUByteArray(arr)
        is ByteArray -> if (normalize) FloatsFromUByteArrayNormalized(arr.asUByteArray()) else FloatsFromUByteArray(arr.asUByteArray())
        else -> error("Unsupported data type")
    } }
    DataType.SHORT -> array.array.let { arr -> when (arr) {
        is ShortArray -> if (normalize) FloatsFromShortArrayNormalized(arr) else FloatsFromShortArray(arr)
        is UShortArray -> if (normalize) FloatsFromShortArrayNormalized(arr.asShortArray()) else FloatsFromShortArray(arr.asShortArray())
        else -> error("Unsupported data type")
    } }
    DataType.UNSIGNED_SHORT -> array.array.let { arr -> when (arr) {
        is UShortArray -> if (normalize) FloatsFromUShortArrayNormalized(arr) else FloatsFromUShortArray(arr)
        is ShortArray -> if (normalize) FloatsFromUShortArrayNormalized(arr.asUShortArray()) else FloatsFromUShortArray(arr.asUShortArray())
        else -> error("Unsupported data type")
    } }
    DataType.INT -> array.array.let { arr -> when (arr) {
        is IntArray -> if (normalize) FloatsFromIntArrayNormalized(arr) else FloatsFromIntArray(arr)
        is UIntArray -> if (normalize) FloatsFromIntArrayNormalized(arr.asIntArray()) else FloatsFromIntArray(arr.asIntArray())
        else -> error("Unsupported data type")
    } }
    DataType.UNSIGNED_INT -> array.array.let { arr -> when (arr) {
        is UIntArray -> if (normalize) FloatsFromUIntArrayNormalized(arr) else FloatsFromUIntArray(arr)
        is IntArray -> if (normalize) FloatsFromUIntArrayNormalized(arr.asUIntArray()) else FloatsFromUIntArray(arr.asUIntArray())
        else -> error("Unsupported data type")
    } }
    else -> error("Unsupported data type")
}

private class FloatsFromFloatArray(val array: FloatArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Float.SIZE_BYTES
    override operator fun get(index: Int) = array[index]
    override operator fun set(index: Int, value: Float) { array[index] = value }
}

private class FloatsIntBitsArray(val array: IntArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Int.SIZE_BYTES
    override operator fun get(index: Int) = Float.fromBits(array[index])
    override operator fun set(index: Int, value: Float) { array[index] = value.toBits() }
}
private class FloatsFromByteArray(val array: ByteArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Byte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toInt().toByte() }
}
private class FloatsFromUByteArray(val array: UByteArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = UByte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toInt().toUByte() }
}
private class FloatsFromShortArray(val array: ShortArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Short.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toInt().toShort() }
}
private class FloatsFromUShortArray(val array: UShortArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = UShort.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toInt().toUShort() }
}
private class FloatsFromIntArray(val array: IntArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Int.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toInt() }
}
private class FloatsFromUIntArray(val array: UIntArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = UInt.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat()
    override operator fun set(index: Int, value: Float) { array[index] = value.toUInt() }
}
private class FloatsFromByteArrayNormalized(val array: ByteArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Byte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / Byte.MAX_VALUE
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(-1f, 1f) * Byte.MAX_VALUE).toInt().toByte() }
}
private class FloatsFromUByteArrayNormalized(val array: UByteArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Byte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / UByte.MAX_VALUE.toInt()
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(0f, 1f) * UByte.MAX_VALUE.toInt()).toInt().toUByte() }
}
private class FloatsFromShortArrayNormalized(val array: ShortArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Short.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / Short.MAX_VALUE
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort() }
}
private class FloatsFromUShortArrayNormalized(val array: UShortArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = UShort.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / UShort.MAX_VALUE.toInt()
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(0f, 1f) * UShort.MAX_VALUE.toInt()).toInt().toUShort() }
}
private class FloatsFromIntArrayNormalized(val array: IntArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = Int.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / Int.MAX_VALUE
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(-1f, 1f) * Int.MAX_VALUE).toInt() }
}
private class FloatsFromUIntArrayNormalized(val array: UIntArray): FloatArrayWrapper {
    override val size = array.size
    override val elementSize = UInt.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toFloat() / UInt.MAX_VALUE.toLong()
    override operator fun set(index: Int, value: Float) { array[index] = (value.coerceIn(0f, 1f) * UInt.MAX_VALUE.toLong()).toUInt() }
}
