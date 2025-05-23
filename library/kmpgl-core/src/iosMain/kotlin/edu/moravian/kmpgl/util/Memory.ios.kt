@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

package edu.moravian.kmpgl.util

import kotlinx.cinterop.*
import platform.posix.memmove
import platform.posix.memset
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
internal open class MemoryImplPtr internal constructor(
    val pointer: CPointer<ByteVar>,
    final override val size: Int,
    private val owned: Boolean = true,
): MemoryImpl {
    init { requirePositive(size, "size") }
    override val isLittleEndian = Platform.isLittleEndian
    override fun free() { if (owned) nativeHeap.free(pointer) }
    @Suppress("unused", "LeakingThis")
    private val cleaner = createCleaner(this) { it.free() }

    private inline fun ptr(index: Int) = (pointer + index)!!

    final override inline fun get(index: Int) = pointer[index]
    final override inline fun set(index: Int, value: Byte) { pointer[index] = value }

    override fun getShort(index: Int) = ptr(index).reinterpret<ShortVar>().pointed.value
    override fun getInt(index: Int) = ptr(index).reinterpret<IntVar>().pointed.value
    override fun getLong(index: Int) = ptr(index).reinterpret<LongVar>().pointed.value
    override fun getFloat(index: Int) = Float.fromBits(ptr(index).reinterpret<IntVar>().pointed.value)
    override fun getDouble(index: Int) = Double.fromBits(ptr(index).reinterpret<LongVar>().pointed.value)

    override fun set(index: Int, value: Short) { ptr(index).reinterpret<ShortVar>().pointed.value = value }
    override fun set(index: Int, value: Int) { ptr(index).reinterpret<IntVar>().pointed.value = value }
    override fun set(index: Int, value: Long) { ptr(index).reinterpret<LongVar>().pointed.value = value }
    override fun set(index: Int, value: Float) { ptr(index).reinterpret<IntVar>().pointed.value = value.toBits() }
    override fun set(index: Int, value: Double) { ptr(index).reinterpret<LongVar>().pointed.value = value.toBits() }

    private inline fun get(offset: Int, dest: CPointer<*>, nbytes: Int) { memmove(dest, pointer + offset, nbytes.convert()) }
    override fun get(dest: ByteArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { set(offset, it.addressOf(destOffset), count) } }
    override fun get(dest: ShortArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { get(offset, it.addressOf(destOffset), count*2) } }
    override fun get(dest: IntArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { get(offset, it.addressOf(destOffset), count*4) } }
    override fun get(dest: LongArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { get(offset, it.addressOf(destOffset), count*8) } }
    override fun get(dest: FloatArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { get(offset, it.addressOf(destOffset), count*4) } }
    override fun get(dest: DoubleArray, destOffset: Int, count: Int, offset: Int) { dest.usePinned { get(offset, it.addressOf(destOffset), count*8) } }

    private inline fun set(offset: Int, src: CPointer<*>, nbytes: Int) { memmove(pointer + offset, src, nbytes.convert()) }
    override fun set(src: ByteArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { get(offset, it.addressOf(srcOffset), count) } }
    override fun set(src: ShortArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { set(offset, it.addressOf(srcOffset), count*2) } }
    override fun set(src: IntArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { set(offset, it.addressOf(srcOffset), count*4) } }
    override fun set(src: LongArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { set(offset, it.addressOf(srcOffset), count*8) } }
    override fun set(src: FloatArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { set(offset, it.addressOf(srcOffset), count*4) } }
    override fun set(src: DoubleArray, srcOffset: Int, count: Int, offset: Int) { src.usePinned { set(offset, it.addressOf(srcOffset), count*8) } }

    override fun slice(offset: Int, length: Int) = MemoryImplPtr((pointer + offset)!!, length, false)
    override fun fill(offset: Int, count: Int, value: Byte) { memset(pointer + offset, value.toInt(), count.convert()) }
    override fun copyTo(dest: MemoryImpl, destOffset: Int, length: Int, offset: Int) {
        if (dest !is MemoryImplPtr) throw IllegalArgumentException("Destination must be MemoryImplPtr")
        if (length != 0) memmove(dest.pointer + destOffset, pointer + offset, length.convert())
    }
}

internal actual fun allocMemory(size: Int): MemoryImpl = MemoryImplPtr(nativeHeap.allocArray(size), size, true)
internal actual fun wrapMemory(bytes: ByteArray, offset: Int, count: Int): MemoryImpl = MemoryImplByteArray(bytes, offset, count)
@OptIn(ExperimentalForeignApi::class)
fun Memory.Companion.of(pointer: CPointer<ByteVar>, size: Int, owned: Boolean = false) = Memory(MemoryImplPtr(pointer, size, owned))
@OptIn(ExperimentalForeignApi::class)
fun Memory.Companion.of(pointer: CPointer<*>, size: Int, owned: Boolean = false) = Memory(MemoryImplPtr(pointer.reinterpret(), size, owned))

internal class MemoryImplByteArray(
    val bytes: ByteArray,
    val offset: Int = 0,
    count: Int = bytes.size - offset,
    private val pin: Pinned<ByteArray> = bytes.pin(),
) : MemoryImplPtr(pin.addressOf(offset), count, false) {
    override fun free() { pin.unpin() }
}


@OptIn(ExperimentalForeignApi::class)
internal inline fun Bufferable.asBuffer() =
    when (this) {
        is Memory -> (this.impl as MemoryImplPtr).pointer
        is FloatPrimitiveArray -> array.refTo(0)
        is HalfFloatPrimitiveArray -> array.raw.refTo(0)
        is Fixed32PrimitiveArray -> array.raw.refTo(0)
        is IntPrimitiveArray -> array.refTo(0)
        is UIntPrimitiveArray -> array.asIntArray().refTo(0)
        is ShortPrimitiveArray -> array.refTo(0)
        is UShortPrimitiveArray -> array.asShortArray().refTo(0)
        is BytePrimitiveArray -> array.refTo(0)
        is UBytePrimitiveArray -> array.asByteArray().refTo(0)
        is DoublePrimitiveArray -> array.refTo(0)
        is LongPrimitiveArray -> array.refTo(0)
        is ULongPrimitiveArray -> array.asLongArray().refTo(0)
    }


@OptIn(ExperimentalForeignApi::class)
internal inline fun Bufferable.asBuffer(offset: Int, size: Int) =
    when (this) {
        is Memory -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); ((this.impl as MemoryImplPtr).pointer + offset)!! }
        is FloatPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is HalfFloatPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.raw.refTo(offset) }
        is Fixed32PrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.raw.refTo(offset) }
        is IntPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is UIntPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.asIntArray().refTo(offset) }
        is ShortPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is UShortPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.asShortArray().refTo(offset) }
        is BytePrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is UBytePrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.asByteArray().refTo(offset) }
        is DoublePrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is LongPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.refTo(offset) }
        is ULongPrimitiveArray -> { require(offset >= 0 && size >= 0 && offset+size <= this.size); array.asLongArray().refTo(offset) }
    }
