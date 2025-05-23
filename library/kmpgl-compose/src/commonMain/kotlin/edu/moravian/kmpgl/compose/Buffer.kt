package edu.moravian.kmpgl.compose

import edu.moravian.kmpgl.core.BufferTarget
import edu.moravian.kmpgl.core.BufferUsage
import edu.moravian.kmpgl.core.DataType
import edu.moravian.kmpgl.core.GLAttributeLocation
import edu.moravian.kmpgl.core.GLBuffer
import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLES3
import edu.moravian.kmpgl.core.IntDataType
import edu.moravian.kmpgl.core.bindBuffer
import edu.moravian.kmpgl.core.bufferData
import edu.moravian.kmpgl.core.isNull
import edu.moravian.kmpgl.core.isSet
import edu.moravian.kmpgl.core.vertexAttribIPointer
import edu.moravian.kmpgl.core.vertexAttribPointer
import edu.moravian.kmpgl.util.Bufferable
import edu.moravian.kmpgl.util.FloatView
import edu.moravian.kmpgl.util.Indices
import edu.moravian.kmpgl.util.IntView
import edu.moravian.kmpgl.util.Vector2View
import edu.moravian.kmpgl.util.Vector3View
import edu.moravian.kmpgl.util.Vector4View

/**
 * A buffer that can be uploaded to the GPU. This can be used by one or more BufferAttributes to
 * describe the data in the buffer.
 */
data class BufferData(
    val data: Bufferable,
    val usage: BufferUsage = BufferUsage.STATIC_DRAW, // how the data is expected to be used
    val target: BufferTarget = BufferTarget.ARRAY,
): VersionedImpl(), Disposable {
    val numBytes: Int = data.nbytes
    private var buffer = GLBuffer.NULL
    private var usageCount = 0
    fun bufferData(gl: GLContext) {
        if (needsUpdate) {
            if (buffer.isNull) { buffer = gl.genBuffer() }
            gl.bindBuffer(target, buffer)
            gl.bufferData(target, usage, data)
            clearNeedsUpdate()
        }
    }
    fun acquire(gl: GLContext) { ++usageCount }
    fun release(gl: GLContext) { if (usageCount > 0 && --usageCount == 0) { dispose(gl) } }
    override fun dispose(gl: GLContext) {
        if (buffer.isSet) gl.deleteBuffer(buffer)
        buffer = GLBuffer.NULL
        needsUpdate = true
    }
}


/**
 * A an attribute that references a buffer of data. This is used to describe the data that is stored
 * in a buffer. A single buffer can back a single attribute or multiple attributes.
 */
open class BufferAttribute(
    val buffer: BufferData,
    val numComponents: Int, // 1-4
    val type: DataType, // the data type of each component in the array
    val stride: Int = 0, // the number of bytes between the start of one attribute and the next; 0 is special and means tightly packed
    val offset: Int = 0, // the number of bytes before the first occurrence of the attribute; must be a multiple of the type size
    val count: Int = computeCount(buffer.numBytes, numComponents, type.size, stride, offset), // the number of elements in the buffer to use
    val normalize: Boolean = false, // whether the data should be normalized to a range of [-1, 1] or [0, 1] when being cast to a float
): Versioned {
    init {
        require(buffer.target == BufferTarget.ARRAY) // probably too strict
        require(numComponents in 1..4)
        require(offset % type.size == 0)
        require(count >= 0)
    }

    fun bufferData(gl: GLContext) { buffer.bufferData(gl) }
    open fun vertexAttribPointer(gl: GLContext, location: GLAttributeLocation) {
        gl.vertexAttribPointer(location, numComponents, type, normalize, stride, offset)
        gl.enableVertexAttribArray(location)
    }

    /**
     * A view of the buffer as a sequence of floats. This is used to iterate over the data in the
     * buffer. The view does not take into account the number of components or the stride since
     * those relate to the vectors, not the floats.
     */
    val rawFloatView get() =
        FloatView(buffer.data, type, offset, numRawElems, normalize)

    protected val elemStride = if (stride == 0) numComponents else stride / type.size
    protected val numRawElems = count * elemStride

    fun vec2View() = Vector2View(rawFloatView, numComponents, elemStride)
    fun vec3View() = Vector3View(rawFloatView, numComponents, elemStride)
    fun vec4View() = Vector4View(rawFloatView, numComponents, elemStride)
    fun vec2View(indices: Indices) = Vector2View(rawFloatView, numComponents, elemStride, indices)
    fun vec3View(indices: Indices) = Vector3View(rawFloatView, numComponents, elemStride, indices)
    fun vec4View(indices: Indices) = Vector4View(rawFloatView, numComponents, elemStride, indices)

    companion object {
        fun computeCount(numBytes: Int, numComponents: Int, typeSize: Int = 1, stride: Int = 0, offset: Int = 0) =
            (numBytes - offset) / (if (stride == 0) (typeSize * numComponents) else stride)
    }

    override var needsUpdate: Boolean
        get() = buffer.needsUpdate
        set(value) { buffer.needsUpdate = value }
    override val version: Int
        get() = buffer.version

    override fun equals(other: Any?) =
        this === other || other is BufferAttribute &&
                buffer == other.buffer && numComponents == other.numComponents &&
                type == other.type && stride == other.stride &&
                offset == other.offset && count == other.count &&
                normalize == other.normalize

    override fun hashCode(): Int {
        var result = buffer.hashCode()
        result = 31 * result + numComponents
        result = 31 * result + type.hashCode()
        result = 31 * result + stride
        result = 31 * result + offset
        result = 31 * result + count
        result = 31 * result + normalize.hashCode()
        return result
    }
}

@GLES3
class IntBufferAttribute(
    buffer: BufferData,
    numComponents: Int = 1, // 1-4
    val intType: IntDataType = IntDataType.UNSIGNED_BYTE, // the data type of each component in the array
    stride: Int = 0, // the number of bytes between the start of one attribute and the next; 0 is special and means tightly packed
    offset: Int = 0, // the number of bytes before the first occurrence of the attribute; must be a multiple of the type size
    count: Int = computeCount(buffer.numBytes, numComponents, intType.size, stride, offset),
): BufferAttribute(buffer, numComponents, DataType.from(intType.value), stride, offset, count, false) {
    override fun vertexAttribPointer(gl: GLContext, location: GLAttributeLocation) {
        gl.vertexAttribIPointer(location, numComponents, intType, stride, offset)
        gl.enableVertexAttribArray(location)
    }
    val rawIntView get() = IntView(buffer.data, intType, offset, numRawElems)

    override fun equals(other: Any?) =
        this === other || other is IntBufferAttribute &&
                super.equals(other) && intType == other.intType

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + intType.hashCode()
        return result
    }
}
