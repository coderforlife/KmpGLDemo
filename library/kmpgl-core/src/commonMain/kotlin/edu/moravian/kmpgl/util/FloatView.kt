@file:OptIn(ExperimentalUnsignedTypes::class)

package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.core.DataType

/**
 * A view of a list of floats. The underlying data could be in an array or memory chunk.
 * The view may have a layer of indirection to allow for data types, normalization, etc.
 */
sealed class FloatView: List<Float> {
    abstract override val size: Int // in elements
    override fun isEmpty() = size == 0
    abstract override operator fun get(index: Int): Float
    abstract operator fun set(index: Int, value: Float)
    override operator fun iterator() = object: FloatIterator() {
        private var index = 0
        override fun hasNext() = index < size
        override fun nextFloat() = get(index++)
    }
    override fun listIterator() = listIterator(0)
    override fun listIterator(index: Int) = object : ListIterator<Float> {
        private var idx = index
        override fun hasNext() = idx < size
        override fun hasPrevious() = idx > 0
        override fun next() = get(idx++)
        override fun nextIndex() = idx
        override fun previous() = get(--idx)
        override fun previousIndex() = idx - 1
    }
    abstract override fun subList(fromIndex: Int, toIndex: Int): FloatView

    // The rest of the methods are only implemented to complete the List interface
    // and are not intended to be used in performance-critical code
    override fun contains(element: Float) = indices.any { get(it) == element }
    override fun containsAll(elements: Collection<Float>) = elements.all { contains(it) }
    override fun indexOf(element: Float) = indices.first { get(it) == element }
    override fun lastIndexOf(element: Float) = indices.last { get(it) == element }
}

fun FloatView(
    data: Bufferable,
    type: DataType, // the data type of each component in the array
    offset: Int = 0, // the number of bytes before the first occurrence of a value; must be a multiple of the type size
    count: Int = (data.nbytes - offset) / type.size, // the number of elements in the buffer to use
    normalize: Boolean = false, // whether the data should be normalized to a range of [-1, 1] or [0, 1] when being cast to a float
): FloatView {
    require(offset >= 0 && count >= 0)
    when (data) {
        is Memory -> return floatViewFromMemory(data, type, offset, count, normalize)
        is PrimitiveArray -> {
            if (type != DataType.BYTE && type != DataType.UNSIGNED_BYTE) {
                val array =
                    if (data is BytePrimitiveArray) data.array else if (data is UBytePrimitiveArray) data.array.asByteArray() else null
                if (array != null)
                    return floatViewFromMemory(Memory.of(array), type, offset, count, normalize)
            }
            require(offset % type.size == 0)
            return floatViewFromArray(data, type, offset / type.size, count, normalize)
        }
        //else -> throw IllegalArgumentException("Unsupported data type: ${data::class}")
    }
}

private class FloatArrayView(
    val array: FloatArray,
    val offset: Int = 0, override val size: Int = array.size - offset,
): FloatView() {
    init { require(offset >= 0 && size >= 0 && offset + size <= array.size) }
    override fun get(index: Int): Float { return array[offset+index] }
    override fun set(index: Int, value: Float) { array[offset+index] = value }
    override fun subList(fromIndex: Int, toIndex: Int) =
        FloatArrayView(array, offset+fromIndex, toIndex-fromIndex)
}

private class FloatArrayWrapperView(
    val array: FloatArrayWrapper,
    val offset: Int = 0, override val size: Int = array.size - offset,
): FloatView() {
    init { require(offset >= 0 && size >= 0 && offset + size <= array.size) }
    override fun get(index: Int) = array[offset+index]
    override fun set(index: Int, value: Float) { array[offset+index] = value }
    override fun subList(fromIndex: Int, toIndex: Int) =
        FloatArrayWrapperView(array, offset+fromIndex, toIndex-fromIndex)
}

private class FloatMemoryView(val memory: Memory) : FloatView() {
    override val size = memory.size / Float.SIZE_BYTES
    override fun get(index: Int) = memory.getFloat(index * Float.SIZE_BYTES)
    override fun set(index: Int, value: Float) {
        memory[index * Float.SIZE_BYTES] = value
    }
    override fun subList(fromIndex: Int, toIndex: Int) =
        FloatMemoryView(memory[fromIndex * Float.SIZE_BYTES until toIndex * Float.SIZE_BYTES])
}

private class FloatMemoryViewFromByte(val memory: Memory): FloatView() {
    override val size = memory.size / Byte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toFloat()
    override operator fun set(index: Int, value: Float) {
        memory[index] = value.toInt().toByte()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromByte(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUByte(val memory: Memory): FloatView() {
    override val size = memory.size / UByte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toUByte().toFloat()
    override operator fun set(index: Int, value: Float) {
        memory[index] = value.toInt().toUByte().toByte()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUByte(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromShort(val memory: Memory): FloatView() {
    override val size = memory.size / Short.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index).toFloat()
    override operator fun set(index: Int, value: Float) {
        memory[index] = value.toInt().toShort()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromShort(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUShort(val memory: Memory): FloatView() {
    override val size = memory.size / UShort.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index).toUShort().toFloat()
    override operator fun set(index: Int, value: Float) {
        memory[index] = value.toInt().toUShort().toShort()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUShort(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromInt(val memory: Memory): FloatView() {
    override val size = memory.size / Int.SIZE_BYTES
    override operator fun get(index: Int) = memory.getInt(index).toFloat()
    override operator fun set(index: Int, value: Float) { memory[index] = value.toInt() }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromInt(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUInt(val memory: Memory): FloatView() {
    override val size = memory.size / UInt.SIZE_BYTES
    override operator fun get(index: Int) = memory.getInt(index).toUInt().toFloat()
    override operator fun set(index: Int, value: Float) { memory.set(index, value.toInt().toUInt().toInt()) }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUInt(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromByteNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / Byte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toFloat() / Byte.MAX_VALUE
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(-1f, 1f) * Byte.MAX_VALUE).toInt().toByte()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromByteNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUByteNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / UByte.SIZE_BYTES
    override operator fun get(index: Int) = memory[index].toUByte().toFloat() / UByte.MAX_VALUE.toInt()
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(0f, 1f) * UByte.MAX_VALUE.toInt()).toInt().toUByte().toByte()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUByteNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromShortNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / Short.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index).toFloat() / Short.MAX_VALUE
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromShortNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUShortNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / UShort.SIZE_BYTES
    override operator fun get(index: Int) = memory.getShort(index).toUShort().toFloat() / UShort.MAX_VALUE.toInt()
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(0f, 1f) * UShort.MAX_VALUE.toInt()).toInt().toUShort().toShort()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUShortNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromIntNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / Int.SIZE_BYTES
    override operator fun get(index: Int) = memory.getInt(index).toFloat() / Int.MAX_VALUE
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(-1f, 1f) * Int.MAX_VALUE).toInt()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromIntNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}
private class FloatMemoryViewFromUIntNormalized(val memory: Memory): FloatView() {
    override val size = memory.size / UInt.SIZE_BYTES
    override operator fun get(index: Int) = memory.getInt(index).toUInt().toFloat() / UInt.MAX_VALUE.toLong()
    override operator fun set(index: Int, value: Float) {
        memory[index] = (value.coerceIn(0f, 1f) * UInt.MAX_VALUE.toLong()).toUInt().toInt()
    }
    override fun subList(fromIndex: Int, toIndex: Int) = FloatMemoryViewFromUIntNormalized(memory.slice(fromIndex, toIndex-fromIndex))
}

private fun floatViewFromMemory(
    data: Memory,
    type: DataType,
    offset: Int, // in bytes
    count: Int, // the number of elements
    normalize: Boolean = false,
): FloatView {
    val mem = data.slice(offset, count * type.size)
    return when (type) {
        DataType.FLOAT -> FloatMemoryView(mem)
        DataType.BYTE -> if (normalize) FloatMemoryViewFromByteNormalized(mem) else FloatMemoryViewFromByte(mem)
        DataType.UNSIGNED_BYTE -> if (normalize) FloatMemoryViewFromUByteNormalized(mem) else FloatMemoryViewFromUByte(mem)
        DataType.SHORT -> if (normalize) FloatMemoryViewFromShortNormalized(mem) else FloatMemoryViewFromShort(mem)
        DataType.UNSIGNED_SHORT -> if (normalize) FloatMemoryViewFromUShortNormalized(mem) else FloatMemoryViewFromUShort(mem)
        DataType.INT -> if (normalize) FloatMemoryViewFromIntNormalized(mem) else FloatMemoryViewFromInt(mem)
        DataType.UNSIGNED_INT -> if (normalize) FloatMemoryViewFromUIntNormalized(mem) else FloatMemoryViewFromUInt(mem)
        else -> error("Unsupported data type")
    }
}

private fun floatViewFromArray(
    data: PrimitiveArray,
    type: DataType,
    offset: Int, // in elements
    count: Int, // in elements
    normalize: Boolean = false,
) = if (type == DataType.FLOAT && data is FloatPrimitiveArray) FloatArrayView(data.array, offset, count)
    else FloatArrayWrapperView(FloatArrayWrapper(data, type, normalize), offset, count)

class IndexedFloatView(
    val floats: FloatView,
    val indices: IntArray,
    val offset: Int = 0,
    val length: Int = indices.size - offset,
): FloatView() {
    init { require(offset >= 0 && length >= 0 && offset + length <= indices.size) }
    override val size = length
    override fun get(index: Int) = floats[indices[offset+index]]
    override fun set(index: Int, value: Float) { floats[indices[offset+index]] = value }
    override fun subList(fromIndex: Int, toIndex: Int): FloatView {
        require(fromIndex in 0..<size && toIndex in 0 .. size)
        return IndexedFloatView(floats, indices, offset+fromIndex, toIndex-fromIndex)
    }
}

fun FloatView.indexed(indices: IntArray, offset: Int = 0, length: Int = indices.size) =
    IndexedFloatView(this, indices, offset, length)
