@file:OptIn(ExperimentalUnsignedTypes::class)

package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.core.GLES3
import edu.moravian.kmpgl.core.IntDataType

/**
 * A view of a list of ints. The underlying data could be in an array or memory chunk.
 * The view may have a layer of indirection to allow for data types, etc.
 */
sealed class IntView: List<Int> {
    abstract override val size: Int // in elements
    override fun isEmpty() = size == 0
    abstract override operator fun get(index: Int): Int
    abstract operator fun set(index: Int, value: Int)
    override operator fun iterator() = object: IntIterator() {
        private var index = 0
        override fun hasNext() = index < this@IntView.size
        override fun nextInt() = get(index++)
    }
    override fun listIterator() = listIterator(0)
    override fun listIterator(index: Int) = object : ListIterator<Int> {
        private var idx = index
        override fun hasNext() = idx < this@IntView.size
        override fun hasPrevious() = idx > 0
        override fun next() = get(idx++)
        override fun nextIndex() = idx
        override fun previous() = get(--idx)
        override fun previousIndex() = idx - 1
    }
    abstract override fun subList(fromIndex: Int, toIndex: Int): IntView

    // The rest of the methods are only implemented to complete the List interface
    // and are not intended to be used in performance-critical code
    override fun contains(element: Int) = indices.any { get(it) == element }
    override fun containsAll(elements: Collection<Int>) = elements.all { contains(it) }
    override fun indexOf(element: Int) = indices.first { get(it) == element }
    override fun lastIndexOf(element: Int) = indices.last { get(it) == element }
}

fun IntView(
    data: Bufferable,
    type: IntDataType, // the data type of each component in the array
    offset: Int = 0, // the number of bytes before the first occurrence of a value; must be a multiple of the type size
    count: Int = (data.nbytes - offset) / type.size, // the number of elements in the buffer to use
): IntView {
    require(offset >= 0 && count >= 0)
    when (data) {
        is Memory -> return intViewFromMemory(data, type, offset, count)
        is PrimitiveArray -> {
            if (type != IntDataType.BYTE && type != IntDataType.UNSIGNED_BYTE) {
                val array = if (data is BytePrimitiveArray) data.array else if (data is UBytePrimitiveArray) data.array.asByteArray() else null
                if (array != null)
                    return intViewFromMemory(Memory.of(array), type, offset, count)
            }
            require(offset % type.size == 0)
            return intViewFromArray(data, type, offset / type.size, count)
        }
        //else -> { throw IllegalArgumentException("Unsupported data type: ${data::class}") }
    }
}

private class IntArrayView(
    val array: IntArray,
    val offset: Int = 0, override val size: Int = array.size - offset,
): IntView() {
    init { require(offset >= 0 && size >= 0 && offset + size <= array.size) }
    override fun get(index: Int) = array[offset+index]
    override fun set(index: Int, value: Int) { array[offset+index] = value }
    override fun subList(fromIndex: Int, toIndex: Int) =
        IntArrayView(array, offset+fromIndex, toIndex-fromIndex)
}

private class IntArrayWrapperView(
    val array: IntArrayWrapper,
    val offset: Int = 0, override val size: Int = array.size - offset,
): IntView() {
    init { require(offset >= 0 && size >= 0 && offset + size <= array.size) }
    override fun get(index: Int) = array[offset+index]
    override fun set(index: Int, value: Int) { array[offset+index] = value }
    override fun subList(fromIndex: Int, toIndex: Int) =
        IntArrayWrapperView(array, offset+fromIndex, toIndex-fromIndex)
}

private class IntMemoryView(val memory: Memory) : IntView() {
    override val size = memory.size / Int.SIZE_BYTES
    override fun get(index: Int) = memory.getInt(index * Int.SIZE_BYTES)
    override fun set(index: Int, value: Int) { memory[index * Int.SIZE_BYTES] = value }
    override fun subList(fromIndex: Int, toIndex: Int) =
        IntMemoryView(memory[fromIndex * Int.SIZE_BYTES until toIndex * Int.SIZE_BYTES])
}

private class IntMemoryViewFromByte(val memory: Memory): IntView() {
    override val size = memory.size / Byte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toInt()
    override operator fun set(index: Int, value: Int) { memory[index] = value.toByte() }
    override fun subList(fromIndex: Int, toIndex: Int) = IntMemoryViewFromByte(memory.slice(fromIndex, toIndex-fromIndex))
}
private class IntMemoryViewFromUByte(val memory: Memory): IntView() {
    override val size = memory.size / UByte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toUByte().toInt()
    override operator fun set(index: Int, value: Int) { memory[index] = value.toUByte().toByte() }
    override fun subList(fromIndex: Int, toIndex: Int) = IntMemoryViewFromUByte(memory.slice(fromIndex, toIndex-fromIndex))
}
private class IntMemoryViewFromShort(val memory: Memory): IntView() {
    override val size = memory.size / Short.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index*Short.SIZE_BYTES).toInt()
    override operator fun set(index: Int, value: Int) { memory[index*Short.SIZE_BYTES] = value.toShort() }
    override fun subList(fromIndex: Int, toIndex: Int) = IntMemoryViewFromShort(memory.slice(fromIndex*Short.SIZE_BYTES, (toIndex-fromIndex)*Short.SIZE_BYTES))
}
private class IntMemoryViewFromUShort(val memory: Memory): IntView() {
    override val size = memory.size / UShort.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index*UShort.SIZE_BYTES).toUShort().toInt()
    override operator fun set(index: Int, value: Int) { memory[index*UShort.SIZE_BYTES] = value.toUShort().toShort() }
    override fun subList(fromIndex: Int, toIndex: Int) = IntMemoryViewFromUShort(memory.slice(fromIndex*UShort.SIZE_BYTES, (toIndex-fromIndex)*UShort.SIZE_BYTES))
}
private class IntMemoryViewFromUInt(val memory: Memory): IntView() {
    override val size = memory.size / UInt.SIZE_BYTES
    override operator fun get(index: Int) = memory.getInt(index*UInt.SIZE_BYTES).toUInt().toInt()
    override operator fun set(index: Int, value: Int) { memory[index*UInt.SIZE_BYTES] = value.toUInt().toInt() }
    override fun subList(fromIndex: Int, toIndex: Int) = IntMemoryViewFromUInt(memory.slice(fromIndex*UInt.SIZE_BYTES, (toIndex-fromIndex)*UInt.SIZE_BYTES))
}

@OptIn(GLES3::class)
private fun intViewFromMemory(
    data: Memory,
    type: IntDataType,
    offset: Int, // in bytes
    count: Int, // the number of elements
): IntView {
    val mem = data.slice(offset, count * type.size)
    return when (type) {
        IntDataType.BYTE -> IntMemoryViewFromByte(mem)
        IntDataType.UNSIGNED_BYTE -> IntMemoryViewFromUByte(mem)
        IntDataType.SHORT -> IntMemoryViewFromShort(mem)
        IntDataType.UNSIGNED_SHORT -> IntMemoryViewFromUShort(mem)
        IntDataType.INT -> IntMemoryView(mem)
        IntDataType.UNSIGNED_INT -> IntMemoryViewFromUInt(mem)
    }
}

@OptIn(GLES3::class)
private fun intViewFromArray(
    data: PrimitiveArray,
    type: IntDataType,
    offset: Int, // in elements
    count: Int, // in elements
) = if (type == IntDataType.INT && data is IntPrimitiveArray) IntArrayView(data.array, offset, count)
    else IntArrayWrapperView(IntArrayWrapper(data, type), offset, count)
