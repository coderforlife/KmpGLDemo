@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class)

package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.core.GLES3
import edu.moravian.kmpgl.core.IntDataType

/**
 * Basic class that acts like an IntArray but is wrapping any typed array.
 */
sealed interface IntArrayWrapper: Iterable<Int> {
    val size: Int
    val elementSize: Int
    operator fun get(index: Int): Int
    operator fun set(index: Int, value: Int)
    override fun iterator() = object: IntIterator() {
        private var index = 0
        override fun hasNext() = index < size
        override fun nextInt() = get(index++)
    }
}

@OptIn(GLES3::class)
fun IntArrayWrapper(
    array: PrimitiveArray,
    type: IntDataType,
) = when (type) {
    IntDataType.BYTE -> array.array.let { arr -> when (arr) {
        is ByteArray -> IntsFromByteArray(arr)
        is UByteArray -> IntsFromByteArray(arr.asByteArray())
        else -> error("Unsupported data type")
    } }
    IntDataType.UNSIGNED_BYTE -> array.array.let { arr -> when (arr) {
        is UByteArray -> IntsFromUByteArray(arr)
        is ByteArray -> IntsFromUByteArray(arr.asUByteArray())
        else -> error("Unsupported data type")
    } }
    IntDataType.SHORT -> array.array.let { arr -> when (arr) {
        is ShortArray -> IntsFromShortArray(arr)
        is UShortArray -> IntsFromShortArray(arr.asShortArray())
        else -> error("Unsupported data type")
    } }
    IntDataType.UNSIGNED_SHORT -> array.array.let { arr -> when (arr) {
        is UShortArray -> IntsFromUShortArray(arr)
        is ShortArray -> IntsFromUShortArray(arr.asUShortArray())
        else -> error("Unsupported data type")
    } }
    IntDataType.INT -> array.array.let { arr -> when (arr) {
        is IntArray -> IntsFromIntArray(arr)
        is UIntArray -> IntsFromIntArray(arr.asIntArray())
        else -> error("Unsupported data type")
    } }
    IntDataType.UNSIGNED_INT -> array.array.let { arr -> when (arr) {
        is UIntArray -> IntsFromUIntArray(arr)
        is IntArray -> IntsFromUIntArray(arr.asUIntArray())
        else -> error("Unsupported data type")
    } }
}

private class IntsFromByteArray(val array: ByteArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = Byte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toInt()
    override operator fun set(index: Int, value: Int) { array[index] = value.toByte() }
}
private class IntsFromUByteArray(val array: UByteArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = UByte.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toInt()
    override operator fun set(index: Int, value: Int) { array[index] = value.toUByte() }
}
private class IntsFromShortArray(val array: ShortArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = Short.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toInt()
    override operator fun set(index: Int, value: Int) { array[index] = value.toShort() }
}
private class IntsFromUShortArray(val array: UShortArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = UShort.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toInt()
    override operator fun set(index: Int, value: Int) { array[index] = value.toUShort() }
}
private class IntsFromIntArray(val array: IntArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = Int.SIZE_BYTES
    override operator fun get(index: Int) = array[index]
    override operator fun set(index: Int, value: Int) { array[index] = value }
}
private class IntsFromUIntArray(val array: UIntArray): IntArrayWrapper {
    override val size = array.size
    override val elementSize = UInt.SIZE_BYTES
    override operator fun get(index: Int) = array[index].toInt()
    override operator fun set(index: Int, value: Int) { array[index] = value.toUInt() }
}
