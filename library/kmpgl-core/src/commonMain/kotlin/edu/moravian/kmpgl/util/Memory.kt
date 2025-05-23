@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.util

// TODO
//  Lots of things I don't like about this class, and for now it isn't really being used
//  anymore, instead just using a ByteArray.
//  The perfect solution would be somewhat close to this, using pointers on iOS/native
//  and ByteBuffer on JVM, but the interface is a bit clunky.
//  One idea is to make several subclasses that specialize for each data type and then
//  they can be used directly. Really want a 1D numpy ndarray, but also changeable between
//  types and endianess.

class Memory internal constructor(
    internal val impl: MemoryImpl,
    var defaultLE: Boolean = impl.isLittleEndian,
): Bufferable {
    override val size: Int = impl.size
    override val nbytes: Int = impl.size
    override val elementSize: Int = 1
    fun free() = impl.free()
    operator fun get(index: Int): Byte = impl.get(requireIndex(index, size))
    operator fun set(index: Int, value: Byte) = impl.set(requireIndex(index, size), value)

    fun getShort(index: Int, le: Boolean = defaultLE): Short = impl.getShort(requireIndex(index, size, 2)).revBytes(impl.isLittleEndian != le)
    fun getInt(index: Int, le: Boolean = defaultLE): Int = impl.getInt(requireIndex(index, size, 4)).revBytes(impl.isLittleEndian != le)
    fun getLong(index: Int, le: Boolean = defaultLE): Long = impl.getLong(requireIndex(index, size, 8)).revBytes(impl.isLittleEndian != le)
    fun getFloat(index: Int, le: Boolean = defaultLE): Float = impl.getFloat(requireIndex(index, size, 4)).revBytes(impl.isLittleEndian != le)
    fun getDouble(index: Int, le: Boolean = defaultLE): Double = impl.getDouble(requireIndex(index, size, 8)).revBytes(impl.isLittleEndian != le)
    fun set(index: Int, value: Short, le: Boolean = defaultLE) = impl.set(requireIndex(index, size, 2), value.revBytes(impl.isLittleEndian != le))
    fun set(index: Int, value: Int, le: Boolean = defaultLE) = impl.set(requireIndex(index, size, 4), value.revBytes(impl.isLittleEndian != le))
    fun set(index: Int, value: Long, le: Boolean = defaultLE) = impl.set(requireIndex(index, size, 8), value.revBytes(impl.isLittleEndian != le))
    fun set(index: Int, value: Float, le: Boolean = defaultLE) = impl.set(requireIndex(index, size, 4), value.revBytes(impl.isLittleEndian != le))
    fun set(index: Int, value: Double, le: Boolean = defaultLE) = impl.set(requireIndex(index, size, 8), value.revBytes(impl.isLittleEndian != le))

    fun slice(offset: Int = 0, length: Int = size - offset): Memory {
        requireRange(offset, length, size, "slice")
        if (length == 0) return Empty
        if (length == size) return this
        return Memory(impl.slice(offset, length), defaultLE)
    }
    fun fill(offset: Int = 0, count: Int = size - offset, value: Byte = 0) {
        requireRange(offset, count, size, "fill")
        if (count == 0) return
        impl.fill(offset, count, value)
    }
    fun copyTo(dest: Memory, destOffset: Int = 0, length: Int = size - destOffset, offset: Int = 0) {
        requireRange(offset, length, size, "copyTo")
        requireRange(destOffset, length, dest.size, "dest")
        if (length == 0) return
        impl.copyTo(dest.impl, destOffset, length, offset)
    }

    private inline fun checkGet(offset: Int, destSize: Int, destElemSize: Int, destOffset: Int, count: Int) {
        requireRange(destOffset, count, destSize, "dest")
        requireRange(offset, count*destElemSize, size, "get")
    }
    fun get(dest: ByteArray, destOffset: Int = 0, length: Int = size - destOffset, offset: Int = 0) {
        checkGet(offset, dest.size, 1, destOffset, length)
        if (length == 0) return
        impl.get(dest, destOffset, length, offset)
    }
    private inline fun checkSet(offset: Int, srcSize: Int, srcElemSize: Int, srcOffset: Int, count: Int) {
        requireRange(srcOffset, count, srcSize, "src")
        requireRange(offset, count*srcElemSize, size, "set")
    }
    fun set(src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset, offset: Int = 0) {
        checkSet(offset, src.size, 1, srcOffset, length)
        if (length == 0) return
        impl.set(src, srcOffset, length, offset)
    }

    fun get(dest: ShortArray, destOffset: Int = 0, count: Int = dest.size - destOffset, offset: Int, le: Boolean = defaultLE) {
        checkGet(offset, dest.size, 2, destOffset, count)
        if (count == 0) return
        if (impl.isLittleEndian != le) {
            for (index in 0 until count) { dest[index+destOffset] = getShort(index*2+offset, le) }
        } else {
            impl.get(dest, destOffset, count, offset)
        }
//        if (impl.isLittleEndian != le) {
//            for (index in destOffset until destOffset + count) { dest[index] = dest[index].revBytes() }
//        }
    }
    fun get(dest: ShortArray) { get(dest, 0, dest.size, 0) }
    fun get(dest: IntArray, destOffset: Int = 0, count: Int = dest.size - destOffset, offset: Int, le: Boolean = defaultLE) {
        checkGet(offset, dest.size, 4, destOffset, count)
        if (count == 0) return
        impl.get(dest, destOffset, count, offset)
        if (impl.isLittleEndian != le) {
            for (index in destOffset until destOffset + count) { dest[index] = dest[index].revBytes(true) }
        }
    }
    fun get(dest: IntArray) { get(dest, 0, dest.size, 0) }
    fun get(dest: LongArray, destOffset: Int = 0, count: Int = dest.size - destOffset, offset: Int, le: Boolean = defaultLE) {
        checkGet(offset, dest.size, 8, destOffset, count)
        if (count == 0) return
        impl.get(dest, destOffset, count, offset)
        if (impl.isLittleEndian != le) {
            for (index in destOffset until destOffset + count) { dest[index] = dest[index].revBytes(true) }
        }
    }
    fun get(dest: LongArray) { get(dest, 0, dest.size, 0) }
    fun get(dest: FloatArray, destOffset: Int = 0, count: Int = dest.size - destOffset, offset: Int, le: Boolean = defaultLE) {
        checkGet(offset, dest.size, 4, destOffset, count)
        if (count == 0) return
        impl.get(dest, destOffset, count, offset)
        if (impl.isLittleEndian != le) {
            for (index in destOffset until destOffset + count) { dest[index] = dest[index].revBytes() }
        }
    }
    fun get(dest: FloatArray) { get(dest, 0, dest.size, 0) }
    fun get(dest: DoubleArray, destOffset: Int = 0, count: Int = dest.size - destOffset, offset: Int, le: Boolean = defaultLE) {
        checkGet(offset, dest.size, 8, destOffset, count)
        if (count == 0) return
        impl.get(dest, destOffset, count, offset)
        if (impl.isLittleEndian != le) {
            for (index in destOffset until destOffset + count) { dest[index] = dest[index].revBytes() }
        }
    }
    fun get(dest: DoubleArray) { get(dest, 0, dest.size, 0) }

    fun set(src: ShortArray, srcOffset: Int = 0, count: Int = src.size - srcOffset, offset: Int, le: Boolean = defaultLE) {
        checkSet(offset, src.size, 2, srcOffset, count)
        if (count == 0) return
        if (le != impl.isLittleEndian) { // write each one individually
            for (index in 0 until count) { set(index+offset, src[index+srcOffset], le) }
        } else {
            impl.set(src, srcOffset, count, offset)
        }
    }
    fun set(src: IntArray, srcOffset: Int = 0, count: Int = src.size - srcOffset, offset: Int, le: Boolean = defaultLE) {
        checkSet(offset, src.size, 4, srcOffset, count)
        if (count == 0) return
        if (le != impl.isLittleEndian) { // write each one individually
            for (index in 0 until count) { set(index+offset, src[index+srcOffset], le) }
        } else {
            impl.set(src, srcOffset, count, offset)
        }
    }
    fun set(src: LongArray, srcOffset: Int = 0, count: Int = src.size - srcOffset, offset: Int, le: Boolean = defaultLE) {
        checkSet(offset, src.size, 8, srcOffset, count)
        if (count == 0) return
        if (le != impl.isLittleEndian) { // write each one individually
            for (index in 0 until count) { set(index+offset, src[index+srcOffset], le) }
        } else {
            impl.set(src, srcOffset, count, offset)
        }
    }
    fun set(src: FloatArray, srcOffset: Int = 0, count: Int = src.size - srcOffset, offset: Int, le: Boolean = defaultLE) {
        checkSet(offset, src.size, 4, srcOffset, count)
        if (count == 0) return
        if (le != impl.isLittleEndian) { // write each one individually
            for (index in 0 until count) { set(index+offset, src[index+srcOffset], le) }
        } else {
            impl.set(src, srcOffset, count, offset)
        }
    }
    fun set(src: DoubleArray, srcOffset: Int = 0, count: Int = src.size - srcOffset, offset: Int, le: Boolean = defaultLE) {
        checkSet(offset, src.size, 8, srcOffset, count)
        if (count == 0) return
        if (le != impl.isLittleEndian) { // write each one individually
            for (index in 0 until count) { set(index+offset, src[index+srcOffset], le) }
        } else {
            impl.set(src, srcOffset, count, offset)
        }
    }

    // mem[0..10]
    operator fun get(range: IntRange) = slice(range.first, range.last - range.first + 1)
    // mem[0..10] = 0
    operator fun set(range: IntRange, value: Byte) = fill(range.first, range.last - range.first + 1, value)
    // mem[0..10] = value  or  mem[0..10] = value[10..20]
    operator fun set(range: IntRange, value: Memory) {
        require(value.size == range.last - range.first + 1) { "Memory size ${value.size} doesn't match range size ${range.last - range.first + 1}" }
        value.copyTo(this, 0, value.size, range.first)
    }
    operator fun set(range: IntRange, value: ByteArray) {
        require(value.size == range.last - range.first + 1) { "ByteArray size ${value.size} doesn't match range size ${range.last - range.first + 1}" }
        set(value, 0, value.size, range.first)
    }
    inline operator fun set(index: Int, value: Short) = set(index, value, defaultLE)
    inline operator fun set(index: Int, value: Int) = set(index, value, defaultLE)
    inline operator fun set(index: Int, value: Long) = set(index, value, defaultLE)
    inline operator fun set(index: Int, value: Float) = set(index, value, defaultLE)
    inline operator fun set(index: Int, value: Double) = set(index, value, defaultLE)

    fun copy() = alloc(size).also { copyTo(it) }

    companion object {
        fun alloc(size: Int) = Memory(allocMemory(requirePositive(size, "size")))
        fun of(bytes: ByteArray) = of(bytes, 0, bytes.size)
        fun of(bytes: ByteArray, offset: Int, count: Int): Memory {
            requireRange(offset, count, bytes.size, "bytes")
            return Memory(wrapMemory(bytes, offset, count))
        }
        val Empty: Memory = Memory(object : MemoryImpl {
            override val size: Int = 0
            override val isLittleEndian: Boolean = true
            override fun get(index: Int): Byte = 0
            override fun set(index: Int, value: Byte) { }
            override fun get(dest: ByteArray, destOffset: Int, count: Int, offset: Int) { }
            override fun set(src: ByteArray, srcOffset: Int, count: Int, offset: Int) { }
            override fun slice(offset: Int, length: Int): MemoryImpl = this
            override fun fill(offset: Int, count: Int, value: Byte) { }
            override fun copyTo(dest: MemoryImpl, destOffset: Int, length: Int, offset: Int) { }

            override fun getShort(index: Int): Short = 0
            override fun getInt(index: Int): Int = 0
            override fun getLong(index: Int): Long = 0
            override fun getFloat(index: Int): Float = 0f
            override fun getDouble(index: Int): Double = 0.0
            override fun set(index: Int, value: Short) { }
            override fun set(index: Int, value: Int) { }
            override fun set(index: Int, value: Long) { }
            override fun set(index: Int, value: Float) { }
            override fun set(index: Int, value: Double) { }
            override fun get(dest: ShortArray, destOffset: Int, count: Int, offset: Int) { }
            override fun get(dest: IntArray, destOffset: Int, count: Int, offset: Int) { }
            override fun get(dest: LongArray, destOffset: Int, count: Int, offset: Int) { }
            override fun get(dest: FloatArray, destOffset: Int, count: Int, offset: Int) { }
            override fun get(dest: DoubleArray, destOffset: Int, count: Int, offset: Int) { }
            override fun set(src: ShortArray, srcOffset: Int, count: Int, offset: Int) { }
            override fun set(src: IntArray, srcOffset: Int, count: Int, offset: Int) { }
            override fun set(src: LongArray, srcOffset: Int, count: Int, offset: Int) { }
            override fun set(src: FloatArray, srcOffset: Int, count: Int, offset: Int) { }
            override fun set(src: DoubleArray, srcOffset: Int, count: Int, offset: Int) { }
        })
    }
}

/**
 * Memory interface for the native platform. The native implementation can
 * assume that indices have already been checked and does not need to handle
 * endianess.
 */
internal interface MemoryImpl {
    val size: Int
    val isLittleEndian: Boolean
    fun free() {}

    fun get(index: Int): Byte
    fun set(index: Int, value: Byte)
    fun get(dest: ByteArray, destOffset: Int, count: Int, offset: Int)
    fun set(src: ByteArray, srcOffset: Int, count: Int, offset: Int)

    fun slice(offset: Int, length: Int): MemoryImpl
    fun fill(offset: Int, count: Int, value: Byte)
    fun copyTo(dest: MemoryImpl, destOffset: Int, length: Int, offset: Int)

    fun getShort(index: Int): Short
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getFloat(index: Int): Float
    fun getDouble(index: Int): Double
    fun set(index: Int, value: Short)
    fun set(index: Int, value: Int)
    fun set(index: Int, value: Long)
    fun set(index: Int, value: Float)
    fun set(index: Int, value: Double)
    fun get(dest: ShortArray, destOffset: Int, count: Int, offset: Int)
    fun get(dest: IntArray, destOffset: Int, count: Int, offset: Int)
    fun get(dest: LongArray, destOffset: Int, count: Int, offset: Int)
    fun get(dest: FloatArray, destOffset: Int, count: Int, offset: Int)
    fun get(dest: DoubleArray, destOffset: Int, count: Int, offset: Int)
    fun set(src: ShortArray, srcOffset: Int, count: Int, offset: Int)
    fun set(src: IntArray, srcOffset: Int, count: Int, offset: Int)
    fun set(src: LongArray, srcOffset: Int, count: Int, offset: Int)
    fun set(src: FloatArray, srcOffset: Int, count: Int, offset: Int)
    fun set(src: DoubleArray, srcOffset: Int, count: Int, offset: Int)
}
internal expect fun allocMemory(size: Int): MemoryImpl
internal expect fun wrapMemory(bytes: ByteArray, offset: Int, count: Int): MemoryImpl

private inline fun Short.revBytes(reverse: Boolean) = if (reverse) toUShort().revBytes().toShort() else this
private inline fun Int.revBytes(reverse: Boolean) = if (reverse) toUInt().revBytes().toInt() else this
private inline fun Long.revBytes(reverse: Boolean) = if (reverse) toULong().revBytes().toLong() else this
private inline fun Float.revBytes(reverse: Boolean) = if (reverse) revBytes() else this
private inline fun Double.revBytes(reverse: Boolean) = if (reverse) revBytes() else this

private inline fun UShort.revBytes() = ((this.toInt() and 0xff) shl 8 or (this.toInt() ushr 8)).toUShort()
private inline fun UInt.revBytes() = ((this shl 24) or (this shr 24) or ((this and 0xff00u) shl 8) or ((this and 0xff0000u) shr 8))
private inline fun ULong.revBytes() = ((this shl 56) or (this shr 56) or ((this and 0xff00u) shl 40) or ((this and 0xff0000u) shl 24) or ((this and 0xff000000u) shl 8) or ((this and 0xff00000000u) shr 8) or ((this and 0xff0000000000u) shr 24) or ((this and 0xff000000000000u) shr 40))
private inline fun Float.revBytes() = Float.fromBits(toBits().toUInt().revBytes().toInt())
private inline fun Double.revBytes() = Double.fromBits(toBits().toULong().revBytes().toLong())
