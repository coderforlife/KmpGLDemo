@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "FunctionName")

package edu.moravian.kmpgl.core

import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline
import kotlin.math.max

//@RequiresOptIn("Not all systems support GLES30.", level = RequiresOptIn.Level.WARNING) - 99%+ of devices support GLES30
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS,
    AnnotationTarget.LOCAL_VARIABLE)
annotation class GLES3

//@RequiresOptIn("Not all systems support GLES30 or have the proper extensions.", level = RequiresOptIn.Level.WARNING) - 99%+ of devices support GLES30
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS,
    AnnotationTarget.LOCAL_VARIABLE)
annotation class GLES3OrExtension


/////////////// Attributes for initializing GLContext ///////////////
// These are modelled after WebGL's context attributes, but not all are supported
data class GLContextAttributes(
    @JvmField val version: GLVersion = GLVersion.GLES_3_0,

    @JvmField val alpha: Boolean = true, // will have alpha channel
    @JvmField val depth: Boolean = true, // request depth buffer of at least 16 bits
    @JvmField val stencil: Boolean = false, // request stencil buffer of at least 8 bits
    @JvmField val antialias: Boolean = true, // enables 4x multisample rendering

    @JvmField val preserveDrawingBuffer: Boolean = false, // don't destroy drawing buffer on swap
    @JvmField val failIfMajorPerformanceCaveat: Boolean = false, // not used in iOS
)

enum class GLVersion(@JvmField val value: Int) {
    // OpenGL ES 2.0 ~= WebGL 1, OpenGL ES 3.0 ~= WebGL 2
    GLES_2_0(20), GLES_3_0(30), GLES_3_1(31), GLES_3_2(32);
}


/////////////// Special return types for some GL functions ///////////////
data class NameTypeSize(@JvmField val name: String, @JvmField val type: Int, @JvmField val size: Int) // same as WebGLActiveInfo from WebGL
data class ShaderPrecisionFormat(@JvmField val rangeMin: Int, @JvmField val rangeMax: Int, @JvmField val precision: Int) // same as WebGLShaderPrecisionFormat from WebGL
data class ProgramBinary(@JvmField val program: ByteArray, @JvmField val format: Int)


/////////////// GL Handle Types ///////////////
sealed interface GLValue {
    val id: Int
    companion object {
        @JvmInline private value class GLValueNull(override val id: Int = 0): GLValue
        val NULL: GLValue = GLValueNull()
    }
}
inline val GLValue.isSet get() = id != 0
inline val GLValue.isNull get() = id == 0
class GLValues<T: GLValue>(val ids: IntArray, val create: (Int) -> T): List<T> {
    override inline val size: Int get() = ids.size
    override inline fun get(index: Int) = create(ids[index])
    override inline fun contains(element: T) = element.id in ids
    override inline fun lastIndexOf(element: T) = ids.lastIndexOf(element.id)
    override inline fun indexOf(element: T) = ids.indexOf(element.id)
    override inline fun containsAll(elements: Collection<T>) = elements.all { it.id in ids }
    inline operator fun set(index: Int, id: T) { ids[index] = id.id }
    override inline fun isEmpty() = ids.isEmpty()
    override inline fun iterator() = object: Iterator<T> {
        var index = 0
        override fun hasNext() = index < size
        override fun next() = create(ids[index++])
    }
    override inline fun listIterator() = listIterator(0)
    override inline fun listIterator(index: Int) = object: ListIterator<T> {
        var i = index
        override fun hasNext() = i < size
        override fun next() = create(ids[i++])
        override fun nextIndex() = i
        override fun hasPrevious() = i >= 0
        override fun previous() = create(ids[--i])
        override fun previousIndex() = i-1
    }
    override inline fun subList(fromIndex: Int, toIndex: Int): List<T> {
        val from = max(0, fromIndex)
        val to = toIndex.coerceIn(from, size)
        return object : AbstractList<T>() {
            override val size get() = to - from
            override fun get(index: Int): T {
                if (index < 0 || index >= size) throw IndexOutOfBoundsException()
                return this@GLValues[from + index]
            }
        }
    }
}

@JvmInline value class GLProgram(override val id: Int): GLValue { companion object { val NULL = GLProgram(0) } }

@JvmInline value class GLShader(override val id: Int): GLValue { companion object { val NULL = GLShader(0) } }
typealias GLShaderArray = GLValues<GLShader>
inline fun GLShaderArray(ids: IntArray) = GLShaderArray(ids, ::GLShader)

@JvmInline value class GLAttributeLocation(override val id: Int): GLValue { companion object { val NULL = GLAttributeLocation(0) } }

@JvmInline value class GLUniformLocation(override val id: Int): GLValue { companion object { val NULL = GLUniformLocation(0) } }

@GLES3OrExtension @JvmInline value class GLVertexArrayObject(override val id: Int): GLValue { companion object { val NULL = GLVertexArrayObject(0) } }
@GLES3OrExtension typealias GLVertexArrayObjectArray = GLValues<GLVertexArrayObject>
@GLES3OrExtension inline fun GLVertexArrayObjectArray(ids: IntArray) = GLVertexArrayObjectArray(ids, ::GLVertexArrayObject)

@JvmInline value class GLBuffer(override val id: Int): GLValue { companion object { val NULL = GLBuffer(0) } }
typealias GLBufferArray = GLValues<GLBuffer>
inline fun GLBufferArray(ids: IntArray) = GLBufferArray(ids, ::GLBuffer)

@JvmInline value class GLFramebuffer(override val id: Int): GLValue { companion object { val NULL = GLFramebuffer(0) } }
typealias GLFramebufferArray = GLValues<GLFramebuffer>
inline fun GLFramebufferArray(ids: IntArray) = GLFramebufferArray(ids, ::GLFramebuffer)

@JvmInline value class GLRenderbuffer(override val id: Int): GLValue { companion object { val NULL = GLRenderbuffer(0) } }
typealias GLRenderbufferArray = GLValues<GLRenderbuffer>
inline fun GLRenderbufferArray(ids: IntArray) = GLRenderbufferArray(ids, ::GLRenderbuffer)

@JvmInline value class GLTexture(override val id: Int): GLValue { companion object { val NULL = GLTexture(0) } }
typealias GLTextureArray = GLValues<GLTexture>
inline fun GLTextureArray(ids: IntArray) = GLTextureArray(ids, ::GLTexture)

@GLES3 @JvmInline value class GLSampler(override val id: Int): GLValue { companion object { val NULL = GLSampler(0) } }
@GLES3 typealias GLSamplerArray = GLValues<GLSampler>
@GLES3 inline fun GLSamplerArray(ids: IntArray) = GLSamplerArray(ids, ::GLSampler)

@GLES3 @JvmInline value class GLQuery(override val id: Int): GLValue { companion object { val NULL = GLQuery(0) } }
@GLES3 typealias GLQueryArray = GLValues<GLQuery>
@GLES3 inline fun GLQueryArray(ids: IntArray) = GLQueryArray(ids, ::GLQuery)

@GLES3 @JvmInline value class GLSync(val id: Long)

@GLES3 @JvmInline value class GLTransformFeedback(override val id: Int): GLValue { companion object { val NULL = GLTransformFeedback(0) } }
@GLES3 typealias GLTransformFeedbackArray = GLValues<GLTransformFeedback>
@GLES3 inline fun GLTransformFeedbackArray(ids: IntArray) = GLTransformFeedbackArray(ids, ::GLTransformFeedback)

inline fun GLContext.isProgram(program: GLValue) = isProgram(program.id)
inline fun GLContext.isShader(shader: GLValue) = isShader(shader.id)
inline fun GLContext.isBuffer(buffer: GLValue) = isBuffer(buffer.id)
inline fun GLContext.isFramebuffer(framebuffer: GLValue) = isFramebuffer(framebuffer.id)
inline fun GLContext.isRenderbuffer(renderbuffer: GLValue) = isRenderbuffer(renderbuffer.id)
inline fun GLContext.isTexture(texture: GLValue) = isTexture(texture.id)

@GLES3OrExtension inline fun GLContext.isVertexArray(vao: GLValue) = isVertexArray(vao.id)

@GLES3 inline fun GLContext.isSampler(sampler: GLValue) = isSampler(sampler.id)
@GLES3 inline fun GLContext.isQuery(query: GLValue) = isQuery(query.id)
@GLES3 inline fun GLContext.isTransformFeedback(tf: GLValue) = isTransformFeedback(tf.id)
