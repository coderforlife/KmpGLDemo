@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.util

//interface Buffer<ArrayType>: Bufferable {
//    fun free()
//    fun slice(offset: Int = 0, length: Int = size - offset): Buffer<ArrayType>
//    //fun fill(offset: Int = 0, count: Int = size - offset, value: Byte = 0): Buffer
//    fun copyTo(dest: Buffer<*>, destOffset: Int = 0, length: Int = size - destOffset, offset: Int = 0): Buffer<ArrayType>
//
//    fun get(dest: ArrayType, destOffset: Int = 0, length: Int = size - destOffset, offset: Int = 0)
//    fun set(src: ArrayType, srcOffset: Int = 0, length: Int = src.size - srcOffset, offset: Int = 0)
//
//    // mem[0..10]
//    operator fun get(range: IntRange) = slice(range.first, range.last - range.first + 1)
//    // mem[0..10] = 0
//    operator fun set(range: IntRange, value: Byte) = fill(range.first, range.last - range.first + 1, value)
//    // mem[0..10] = value  or  mem[0..10] = value[10..20]
//    operator fun set(range: IntRange, value: Memory) {
//        require(value.size == range.last - range.first + 1) { "Memory size ${value.size} doesn't match range size ${range.last - range.first + 1}" }
//        value.copyTo(this, 0, value.size, range.first)
//    }
//    operator fun set(range: IntRange, value: ArrayType) {
//        require(value.size == range.last - range.first + 1) { "ByteArray size ${value.size} doesn't match range size ${range.last - range.first + 1}" }
//        set(value, 0, value.size, range.first)
//    }
//
//    fun copy() = alloc(size).also { copyTo(it) }
//
//    companion object {
//        fun alloc(size: Int) = Memory(allocMemory(requirePositive(size, "size")))
//        fun of(bytes: ByteArray) = of(bytes, 0, bytes.size)
//        fun of(bytes: ByteArray, offset: Int, count: Int): Memory {
//            requireRange(offset, count, bytes.size, "bytes")
//            return Memory(wrapMemory(bytes, offset, count))
//        }
//    }
//}
//
///**
// * Memory interface for the native platform. The native implementation can
// * assume that indices have already been checked and does not need to handle
// * endianess.
// */
//internal interface MemoryImpl {
//    val size: Int
//    val isLittleEndian: Boolean
//    fun free() {}
//
//    fun get(index: Int): Byte
//    fun set(index: Int, value: Byte)
//    fun get(dest: ByteArray, destOffset: Int, count: Int, offset: Int)
//    fun set(src: ByteArray, srcOffset: Int, count: Int, offset: Int)
//
//    fun slice(offset: Int, length: Int): MemoryImpl
//    fun fill(offset: Int, count: Int, value: Byte)
//    fun copyTo(dest: MemoryImpl, destOffset: Int, length: Int, offset: Int)
//
//    fun getShort(index: Int): Short
//    fun getInt(index: Int): Int
//    fun getLong(index: Int): Long
//    fun getFloat(index: Int): Float
//    fun getDouble(index: Int): Double
//    fun set(index: Int, value: Short)
//    fun set(index: Int, value: Int)
//    fun set(index: Int, value: Long)
//    fun set(index: Int, value: Float)
//    fun set(index: Int, value: Double)
//    fun get(dest: ShortArray, destOffset: Int, count: Int, offset: Int)
//    fun get(dest: IntArray, destOffset: Int, count: Int, offset: Int)
//    fun get(dest: LongArray, destOffset: Int, count: Int, offset: Int)
//    fun get(dest: FloatArray, destOffset: Int, count: Int, offset: Int)
//    fun get(dest: DoubleArray, destOffset: Int, count: Int, offset: Int)
//    fun set(src: ShortArray, srcOffset: Int, count: Int, offset: Int)
//    fun set(src: IntArray, srcOffset: Int, count: Int, offset: Int)
//    fun set(src: LongArray, srcOffset: Int, count: Int, offset: Int)
//    fun set(src: FloatArray, srcOffset: Int, count: Int, offset: Int)
//    fun set(src: DoubleArray, srcOffset: Int, count: Int, offset: Int)
//}
//internal expect fun allocMemory(size: Int): MemoryImpl
//internal expect fun wrapMemory(bytes: ByteArray, offset: Int, count: Int): MemoryImpl
//
//private inline fun Short.revBytes(reverse: Boolean) = if (reverse) toUShort().revBytes().toShort() else this
//private inline fun Int.revBytes(reverse: Boolean) = if (reverse) toUInt().revBytes().toInt() else this
//private inline fun Long.revBytes(reverse: Boolean) = if (reverse) toULong().revBytes().toLong() else this
//private inline fun Float.revBytes(reverse: Boolean) = if (reverse) revBytes() else this
//private inline fun Double.revBytes(reverse: Boolean) = if (reverse) revBytes() else this
//
//private inline fun UShort.revBytes() = ((this.toInt() and 0xff) shl 8 or (this.toInt() ushr 8)).toUShort()
//private inline fun UInt.revBytes() = ((this shl 24) or (this shr 24) or ((this and 0xff00u) shl 8) or ((this and 0xff0000u) shr 8))
//private inline fun ULong.revBytes() = ((this shl 56) or (this shr 56) or ((this and 0xff00u) shl 40) or ((this and 0xff0000u) shl 24) or ((this and 0xff000000u) shl 8) or ((this and 0xff00000000u) shr 8) or ((this and 0xff0000000000u) shr 24) or ((this and 0xff000000000000u) shr 40))
//private inline fun Float.revBytes() = Float.fromBits(toBits().toUInt().revBytes().toInt())
//private inline fun Double.revBytes() = Double.fromBits(toBits().toULong().revBytes().toLong())
