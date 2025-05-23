@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
@file:OptIn(ExperimentalUnsignedTypes::class)

package edu.moravian.kmpgl.util

import java.nio.*

internal class MemoryImplByteBuffer internal constructor(val buffer: ByteBuffer): MemoryImpl {
    override val size: Int get() = buffer.limit()
    override val isLittleEndian: Boolean get() = buffer.order() == ByteOrder.LITTLE_ENDIAN

    override inline fun get(index: Int) = buffer.get(index)
    override inline fun set(index: Int, value: Byte) { buffer.put(index, value) }

    override fun getShort(index: Int) = buffer.getShort(index)
    override fun getInt(index: Int) = buffer.getInt(index)
    override fun getLong(index: Int) = buffer.getLong(index)
    override fun getFloat(index: Int) = buffer.getFloat(index)
    override fun getDouble(index: Int) = buffer.getDouble(index)

    override fun set(index: Int, value: Short) { buffer.putShort(index, value) }
    override fun set(index: Int, value: Int) { buffer.putInt(index, value) }
    override fun set(index: Int, value: Long) { buffer.putLong(index, value) }
    override fun set(index: Int, value: Float) { buffer.putFloat(index, value) }
    override fun set(index: Int, value: Double) { buffer.putDouble(index, value) }

    override fun slice(offset: Int, length: Int) = MemoryImplByteBuffer(buffer.sub(offset, length).slice())
    override fun fill(offset: Int, count: Int, value: Byte) { for (index in offset until offset + count) { buffer.put(index, value) } }
    override fun copyTo(dest: MemoryImpl, destOffset: Int, length: Int, offset: Int) {
        if (dest !is MemoryImplByteBuffer) throw IllegalArgumentException("Destination must be MemoryImplByteBuffer")
        val buf = dest.buffer
        if (buffer.hasArray() && buf.hasArray() && !buffer.isReadOnly && !buf.isReadOnly) {
            System.arraycopy(
                buffer.array(), buffer.arrayOffset() + offset,
                buf.array(), buf.arrayOffset() + destOffset,
                length
            )
        } else { buf.off(destOffset).put(buffer.sub(offset, length)) }
    }

    override fun get(dest: ByteArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).get(dest, destOffset, count) }
    override fun get(dest: ShortArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).asShortBuffer().get(dest, destOffset, count) }
    override fun get(dest: IntArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).asIntBuffer().get(dest, destOffset, count) }
    override fun get(dest: LongArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).asLongBuffer().get(dest, destOffset, count) }
    override fun get(dest: FloatArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).asFloatBuffer().get(dest, destOffset, count) }
    override fun get(dest: DoubleArray, destOffset: Int, count: Int, offset: Int) { buffer.off(offset).asDoubleBuffer().get(dest, destOffset, count) }

    override fun set(src: ByteArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).put(src, srcOffset, count) }
    override fun set(src: ShortArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).asShortBuffer().put(src, srcOffset, count) }
    override fun set(src: IntArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).asIntBuffer().put(src, srcOffset, count) }
    override fun set(src: LongArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).asLongBuffer().put(src, srcOffset, count) }
    override fun set(src: FloatArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).asFloatBuffer().put(src, srcOffset, count) }
    override fun set(src: DoubleArray, srcOffset: Int, count: Int, offset: Int) { buffer.off(offset).asDoubleBuffer().put(src, srcOffset, count) }
}

fun Memory.Companion.of(buffer: ByteBuffer) = Memory(MemoryImplByteBuffer(buffer.slice()))
internal actual fun allocMemory(size: Int): MemoryImpl = MemoryImplByteBuffer(ByteBuffer.allocateDirect(size))
internal actual fun wrapMemory(bytes: ByteArray, offset: Int, count: Int): MemoryImpl = MemoryImplByteBuffer(ByteBuffer.wrap(bytes, offset, count))

private inline fun ByteBuffer.off(offset: Int): ByteBuffer = duplicate().apply { position(offset) }
private inline fun ByteBuffer.sub(offset: Int, length: Int): ByteBuffer = duplicate().apply { position(offset); limit(offset + length) }

internal inline fun Bufferable.asBuffer(): Buffer =
    when (this) {
        is Memory -> (this.impl as MemoryImplByteBuffer).buffer
        is BytePrimitiveArray -> ByteBuffer.wrap(array)
        is UBytePrimitiveArray -> ByteBuffer.wrap(array.asByteArray())
        is ShortPrimitiveArray -> ShortBuffer.wrap(array)
        is UShortPrimitiveArray -> ShortBuffer.wrap(array.asShortArray())
        is IntPrimitiveArray -> IntBuffer.wrap(array)
        is UIntPrimitiveArray -> IntBuffer.wrap(array.asIntArray())
        is LongPrimitiveArray -> LongBuffer.wrap(array)
        is ULongPrimitiveArray -> LongBuffer.wrap(array.asLongArray())
        is FloatPrimitiveArray -> FloatBuffer.wrap(array)
        is DoublePrimitiveArray -> DoubleBuffer.wrap(array)
        is HalfFloatPrimitiveArray -> ShortBuffer.wrap(array.raw)
        is Fixed32PrimitiveArray -> IntBuffer.wrap(array.raw)
    }
internal inline fun Bufferable.asBuffer(offset: Int, size: Int): Buffer =
    when (this) {
        is Memory -> (this.slice(offset, size).impl as MemoryImplByteBuffer).buffer
        is FloatPrimitiveArray -> FloatBuffer.wrap(array, offset, size)
        is HalfFloatPrimitiveArray -> ShortBuffer.wrap(array.raw, offset, size)
        is Fixed32PrimitiveArray -> IntBuffer.wrap(array.raw, offset, size)
        is IntPrimitiveArray -> IntBuffer.wrap(array, offset, size)
        is UIntPrimitiveArray -> IntBuffer.wrap(array.asIntArray(), offset, size)
        is ShortPrimitiveArray -> ShortBuffer.wrap(array, offset, size)
        is UShortPrimitiveArray -> ShortBuffer.wrap(array.asShortArray(), offset, size)
        is BytePrimitiveArray -> ByteBuffer.wrap(array, offset, size)
        is UBytePrimitiveArray -> ByteBuffer.wrap(array.asByteArray(), offset, size)
        is DoublePrimitiveArray -> DoubleBuffer.wrap(array, offset, size)
        is LongPrimitiveArray -> LongBuffer.wrap(array, offset, size)
        is ULongPrimitiveArray -> LongBuffer.wrap(array.asLongArray(), offset, size)
    }