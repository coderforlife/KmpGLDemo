@file:Suppress("ClassName", "PropertyName", "unused", "MemberVisibilityCanBePrivate",
    "ConstPropertyName", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"
)

package edu.moravian.kmpgl.core

import co.touchlab.stately.collections.IsoMutableMap
import kotlin.reflect.KClass

typealias ExtensionInit = (GLContext) -> Extension

@Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
class GLExtensions internal constructor(val gl: GLContext): Map<String, Extension> {
    val available = gl.getString(GL.EXTENSIONS).split(" ").toSet()
    private val extensions = mutableMapOf<String, Extension>()
    private val extByClass = mutableMapOf<KClass<*>, Extension?>()

    inline operator fun contains(name: String) = name in available
    override operator fun get(key: String): Extension? {
        if (key !in available) { return null }
        return extensions.getOrPut(key) { registry[key]?.invoke(gl) ?: GLExtensionUnknown(gl, key) }
    }

    // This get is typically going to be easier to use since it doesn't require a cast, use extensions[EXT::class]
    // It also supports getting one of many versions of the same extension by using the abstract class
    @Suppress("UNCHECKED_CAST")
    operator fun <T: Extension> get(clazz: KClass<T>): T? {
        return extByClass.getOrPut(clazz) {
            val group = groups[clazz]
            if (group !== null) { group.firstOrNull { it in available } } else { clazz.simpleName }?.let { this[it] }
        } as T?
    }
    operator fun <T: Extension> contains(clazz: KClass<T>) =
        clazz in extByClass || get(clazz) !== null

    override inline val size: Int get() = available.size
    override inline fun isEmpty() = size == 0
    override inline val keys: Set<String> get() = available
    override fun containsKey(key: String) = key in available
    override val entries: Set<Map.Entry<String, Extension>> get() {
        if (extensions.size == available.size) { return extensions.entries } // all extensions loaded, easy path
        return object : AbstractSet<Map.Entry<String, Extension>>() {
            override val size: Int get() = available.size
            override fun contains(element: Map.Entry<String, Extension>) = this@GLExtensions[element.key] == element.value
            override fun iterator(): Iterator<Map.Entry<String, Extension>> {
                return object : Iterator<Map.Entry<String, Extension>> {
                    var names = available.iterator()
                    override fun hasNext() = names.hasNext()
                    override fun next(): Map.Entry<String, Extension> {
                        val name = names.next()
                        return object : Map.Entry<String, Extension> {
                            override val key: String = name
                            override val value: Extension get() = this@GLExtensions[key]!!
                        }
                    }
                }
            }
        }
    }
    override val values: Collection<Extension> get() {
        if (extensions.size == available.size) { return extensions.values } // all extensions loaded, easy path
        return object : AbstractCollection<Extension>() {
            override val size: Int get() = available.size
            override fun contains(element: Extension) = element.gl == this@GLExtensions.gl
            override fun iterator(): Iterator<Extension> {
                return object : Iterator<Extension> {
                    var names = available.iterator()
                    override fun hasNext() = names.hasNext()
                    override fun next() = this@GLExtensions[names.next()]!!
                }
            }
        }
    }
    override fun containsValue(value: Extension) = value.gl == this.gl
        //if (extensions.size == available.size) { extensions.containsValue(value) } // all extensions loaded, easy path
        //else { extensions.containsValue(value) || available.firstOrNull { this[it] == value } !== null } // check loaded extensions then load them all one by one

    init {
        if (gl.version == 30) {
            get("EXT_color_buffer_float")
        } else if (gl.version == 20) {
            get("ANGLE_depth_texture")           // ?/21  - the closest to the WEBGL_depth_texture
            get("OES_depth_texture")             // 17/21 - a small portion of WEBGL_depth_texture
            get("OES_packed_depth_stencil")      // ?/21  - combined with the above gets much of WEBGL_depth_texture
            get("OES_texture_float")             // 18/21 - modifies texImage2D() and texSubImage2D() to support float type and array
            get("OES_texture_half_float")        // 18/21 - adds constant ext.HALF_FLOAT_OES (0x8D61) and changes texImage2D() and texSubImage2D() to allow it (data sent as a UShort16 array)
            get("OES_texture_half_float_linear") // 8/21  - adds support to texParameter() for half float types
            get("OES_standard_derivatives")      // 16/21 - adds constant ext.FRAGMENT_SHADER_DERIVATIVE_HINT_OES (0x8B8B) to be used with hint() and getParameter()
            get("OES_element_index_uint")        // 14/21 - adds support to drawElements() for type UNSIGNED_INT
            get("OES_vertex_array_object")       // 5/21  - adds support for VAOs (a constant and 4 functions)
            get("ANGLE_instanced_arrays") ?: get("EXT_instanced_arrays") ?: get("NV_instanced_arrays")
            get("EXT_blend_minmax")
        }
        get("OES_texture_float_linear")      // none     - adds support to texParameter() for float types
        get("EXT_color_buffer_half_float")   // none
        get("EXT_multisampled_render_to_texture") ?: get("IMG_multisampled_render_to_texture") // IMG_multisampled_render_to_texture: 1/21
    }

    companion object {
        private val registry = IsoMutableMap<String, ExtensionInit>()
        private val groups = IsoMutableMap<KClass<*>, Array<out String>>()
        fun addDefinition(name: String, init: ExtensionInit) { registry[name] = init }
        fun addGroup(clazz: KClass<*>, vararg exts: String) { groups[clazz] = exts }
        init {
            addDefinition(OES_standard_derivatives.name, ::OES_standard_derivatives)
            addDefinition(OES_element_index_uint.name, ::OES_element_index_uint)

            addDefinition(ANGLE_instanced_arrays.name, ::ANGLE_instanced_arrays)
            addDefinition(EXT_instanced_arrays.name, ::EXT_instanced_arrays)
            addDefinition(NV_instanced_arrays.name, ::NV_instanced_arrays)
            addGroup(InstancedArraysExtension::class, ANGLE_instanced_arrays.name, EXT_instanced_arrays.name, NV_instanced_arrays.name)

            addDefinition(OES_vertex_array_object.name, ::OES_vertex_array_object)

            addDefinition(EXT_blend_minmax.name, ::EXT_blend_minmax)

            addDefinition(EXT_shader_texture_lod.name, ::EXT_shader_texture_lod)

            addDefinition(EXT_color_buffer_half_float.name, ::EXT_color_buffer_half_float)
            addDefinition(EXT_color_buffer_float.name, ::EXT_color_buffer_float)

            addDefinition(EXT_draw_buffers.name, ::EXT_draw_buffers)
            addDefinition(NV_draw_buffers.name, ::NV_draw_buffers)
            addGroup(DrawBuffersExtension::class, EXT_draw_buffers.name, NV_draw_buffers.name)

            addDefinition(ANGLE_framebuffer_multisample.name, ::ANGLE_framebuffer_multisample)
            addDefinition(NV_framebuffer_multisample.name, ::NV_framebuffer_multisample)
            addDefinition(APPLE_framebuffer_multisample.name, ::APPLE_framebuffer_multisample)
            addGroup(FramebufferMultisampleExtension::class, EXT_multisampled_render_to_texture.name, IMG_multisampled_render_to_texture.name, ANGLE_framebuffer_multisample.name, NV_framebuffer_multisample.name, APPLE_framebuffer_multisample.name)

            addDefinition(EXT_multisampled_render_to_texture.name, ::EXT_multisampled_render_to_texture)
            addDefinition(IMG_multisampled_render_to_texture.name, ::IMG_multisampled_render_to_texture)
            addGroup(MultisampledRTTExtension::class, EXT_multisampled_render_to_texture.name, IMG_multisampled_render_to_texture.name)

            addDefinition(EXT_texture_filter_anisotropic.name, ::EXT_texture_filter_anisotropic)

            addDefinition(OES_depth_texture.name, ::OES_depth_texture)
            addDefinition(ANGLE_depth_texture.name, ::ANGLE_depth_texture)
            addDefinition(OES_packed_depth_stencil.name, ::OES_packed_depth_stencil)

            addDefinition(OES_texture_float.name, ::OES_texture_float)
            addDefinition(OES_texture_float_linear.name, ::OES_texture_float_linear)
            addDefinition(OES_texture_half_float.name, ::OES_texture_half_float)
            addDefinition(OES_texture_half_float_linear.name, ::OES_texture_half_float_linear)

            addDefinition(EXT_sRGB.name, ::EXT_sRGB)

            // Compressed Texture Formats
            addDefinition(EXT_texture_compression_s3tc_srgb.name, ::EXT_texture_compression_s3tc_srgb)
            addDefinition(EXT_texture_compression_dxt1.name, ::EXT_texture_compression_dxt1)
            addDefinition(EXT_texture_compression_s3tc.name, ::EXT_texture_compression_s3tc)
            addDefinition(IMG_texture_compression_pvrtc.name, ::IMG_texture_compression_pvrtc)
            addDefinition(OES_compressed_ETC1_RGB8_texture.name, ::OES_compressed_ETC1_RGB8_texture) // NOTE: ETC2 is built-in for OpenGL ES 3.0 and not available at all in 2.0
            addDefinition(KHR_texture_compression_astc_hdr.name, ::KHR_texture_compression_astc_hdr)
            addDefinition(KHR_texture_compression_astc_ldr.name, ::KHR_texture_compression_astc_ldr)
            addGroup(KHR_texture_compression_astc::class, KHR_texture_compression_astc_hdr.name, KHR_texture_compression_astc_ldr.name)
            addDefinition(EXT_texture_compression_bptc.name, ::EXT_texture_compression_bptc)
            addDefinition(NV_sRGB_formats.name, ::NV_sRGB_formats)
        }
    }}

abstract class Extension protected constructor(val gl: GLContext)
class GLExtensionUnknown(gl: GLContext, val name: String): Extension(gl)

class OES_standard_derivatives internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_standard_derivatives"
        const val FRAGMENT_SHADER_DERIVATIVE_HINT_OES = 0x8B8B
    }
    val FRAGMENT_SHADER_DERIVATIVE_HINT_OES = Companion.FRAGMENT_SHADER_DERIVATIVE_HINT_OES
}
class OES_element_index_uint internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "OES_element_index_uint" }
}

@GLES3OrExtension
sealed class InstancedArraysExtension(gl: GLContext): Extension(gl) {
    val VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE // GL.VERTEX_ATTRIB_ARRAY_DIVISOR
    abstract fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int)
    abstract fun drawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int)
    abstract fun vertexAttribDivisor(index: Int, divisor: Int)
}
@GLES3OrExtension
class ANGLE_instanced_arrays internal constructor(gl: GLContext): InstancedArraysExtension(gl) {
    companion object {
        const val name = "ANGLE_instanced_arrays"
        const val VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE = 0x88FE // GL.VERTEX_ATTRIB_ARRAY_DIVISOR  // accepted by getVertexAttrib()
    }
    val VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE = Companion.VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE
    private val native = ANGLE_instanced_arrays_native(gl)
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) { native.drawArraysInstancedANGLE(mode, first, count, instanceCount) }
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) { native.drawElementsInstancedANGLE(mode, count, type, indicesOffset, instanceCount) }
    override fun vertexAttribDivisor(index: Int, divisor: Int) { native.vertexAttribDivisorANGLE(index, divisor) }
}
@GLES3OrExtension
internal expect class ANGLE_instanced_arrays_native(gl: GLContext) {
    fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, instanceCount: Int)
    fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int)
    fun vertexAttribDivisorANGLE(index: Int, divisor: Int)
}
@GLES3OrExtension
class EXT_instanced_arrays internal constructor(gl: GLContext): InstancedArraysExtension(gl) {
    companion object {
        const val name = "EXT_instanced_arrays"
        const val VERTEX_ATTRIB_ARRAY_DIVISOR_EXT = 0x88FE // GL.VERTEX_ATTRIB_ARRAY_DIVISOR  // accepted by getVertexAttrib()
    }
    val VERTEX_ATTRIB_ARRAY_DIVISOR_EXT = Companion.VERTEX_ATTRIB_ARRAY_DIVISOR_EXT
    private val native = EXT_instanced_arrays_native(gl)
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) { native.drawArraysInstancedEXT(mode, first, count, instanceCount) }
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) { native.drawElementsInstancedEXT(mode, count, type, indicesOffset, instanceCount) }
    override fun vertexAttribDivisor(index: Int, divisor: Int) { native.vertexAttribDivisorEXT(index, divisor) }
}
@GLES3OrExtension
internal expect class EXT_instanced_arrays_native(gl: GLContext) {
    fun drawArraysInstancedEXT(mode: Int, first: Int, count: Int, instanceCount: Int)
    fun drawElementsInstancedEXT(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int)
    fun vertexAttribDivisorEXT(index: Int, divisor: Int)
}
@GLES3OrExtension
class NV_instanced_arrays internal constructor(gl: GLContext): InstancedArraysExtension(gl) {
    companion object {
        const val name = "NV_instanced_arrays"
        const val VERTEX_ATTRIB_ARRAY_DIVISOR_NV = 0x88FE // GL.VERTEX_ATTRIB_ARRAY_DIVISOR  // accepted by getVertexAttrib()
    }
    val VERTEX_ATTRIB_ARRAY_DIVISOR_NV = Companion.VERTEX_ATTRIB_ARRAY_DIVISOR_NV
    private val native = NV_instanced_arrays_native(gl)
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) { native.drawArraysInstancedNV(mode, first, count, instanceCount) }
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) { native.drawElementsInstancedNV(mode, count, type, indicesOffset, instanceCount) }
    override fun vertexAttribDivisor(index: Int, divisor: Int) { native.vertexAttribDivisorNV(index, divisor) }
}
@GLES3OrExtension
internal expect class NV_instanced_arrays_native(gl: GLContext) {
    fun drawArraysInstancedNV(mode: Int, first: Int, count: Int, instanceCount: Int)
    fun drawElementsInstancedNV(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int)
    fun vertexAttribDivisorNV(index: Int, divisor: Int)
}

@GLES3OrExtension
class OES_vertex_array_object internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_vertex_array_object"
        const val VERTEX_ARRAY_BINDING_OES = 0x85B5 // GL.VERTEX_ARRAY_BINDING  // accepted by getInt()
    }
    val VERTEX_ARRAY_BINDING_OES = Companion.VERTEX_ARRAY_BINDING_OES
    private val native = OES_vertex_array_object_native(gl)
    fun isVertexArrayOES(array: Int): Boolean = native.isVertexArrayOES(array)
    fun genVertexArrayOES(): GLVertexArrayObject = native.genVertexArrayOES()
    fun genVertexArraysOES(n: Int): GLVertexArrayObjectArray = native.genVertexArraysOES(n)
    fun deleteVertexArrayOES(array: GLVertexArrayObject) = native.deleteVertexArrayOES(array)
    fun deleteVertexArraysOES(arrays: GLVertexArrayObjectArray) = native.deleteVertexArraysOES(arrays)
    fun bindVertexArrayOES(array: GLVertexArrayObject) = native.bindVertexArrayOES(array)
}
@GLES3OrExtension
internal expect class OES_vertex_array_object_native(gl: GLContext) {
    fun genVertexArrayOES(): GLVertexArrayObject
    fun genVertexArraysOES(n: Int): GLVertexArrayObjectArray
    fun deleteVertexArrayOES(array: GLVertexArrayObject)
    fun deleteVertexArraysOES(arrays: GLVertexArrayObjectArray)
    fun isVertexArrayOES(array: Int): Boolean
    fun bindVertexArrayOES(array: GLVertexArrayObject)
}

class EXT_blend_minmax internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_blend_minmax"
        const val MIN_EXT = 0x8007 // GL.MIN
        const val MAX_EXT = 0x8008 // GL.MAX
    }
    val MIN_EXT = Companion.MIN_EXT
    val MAX_EXT = Companion.MAX_EXT
}

class EXT_shader_texture_lod internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "EXT_shader_texture_lod" }
}

class EXT_color_buffer_half_float internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_color_buffer_half_float"
        const val RGBA16F_EXT = 0x881A // GL.RGBA16F
        const val RGB16F_EXT = 0x881B // GL.RGB16F
        const val RG16F_EXT = 0x822F // GL.RG16F
        const val R16F_EXT = 0x822D // GL.R16F
        const val FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE_EXT = 0x8211 // GL.FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE
        const val UNSIGNED_NORMALIZED_EXT = 0x8C17 // GL.UNSIGNED_NORMALIZED
    }
    val RGBA16F_EXT = Companion.RGBA16F_EXT
    val RGB16F_EXT = Companion.RGB16F_EXT
    val RG16F_EXT = Companion.RG16F_EXT
    val R16F_EXT = Companion.R16F_EXT
    val FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE_EXT =
        Companion.FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE_EXT
    val UNSIGNED_NORMALIZED_EXT = Companion.UNSIGNED_NORMALIZED_EXT
}
class EXT_color_buffer_float internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "EXT_color_buffer_float" }
}

@GLES3OrExtension
sealed class DrawBuffersExtension(gl: GLContext): Extension(gl) {
    val MAX_COLOR_ATTACHMENTS = 0x8CDF // GL.MAX_COLOR_ATTACHMENTS
    val MAX_DRAW_BUFFERS = 0x8824 // GL.MAX_DRAW_BUFFERS
    val DRAW_BUFFER0 = 0x8825 // GL.DRAW_BUFFER0
    val DRAW_BUFFER1 = 0x8826 // GL.DRAW_BUFFER1
    val DRAW_BUFFER2 = 0x8827 // GL.DRAW_BUFFER2
    val DRAW_BUFFER3 = 0x8828 // GL.DRAW_BUFFER3
    val DRAW_BUFFER4 = 0x8829 // GL.DRAW_BUFFER4
    val DRAW_BUFFER5 = 0x882A // GL.DRAW_BUFFER5
    val DRAW_BUFFER6 = 0x882B // GL.DRAW_BUFFER6
    val DRAW_BUFFER7 = 0x882C // GL.DRAW_BUFFER7
    val DRAW_BUFFER8 = 0x882D // GL.DRAW_BUFFER8
    val DRAW_BUFFER9 = 0x882E // GL.DRAW_BUFFER9
    val DRAW_BUFFER10 = 0x882F // GL.DRAW_BUFFER10
    val DRAW_BUFFER11 = 0x8830 // GL.DRAW_BUFFER11
    val DRAW_BUFFER12 = 0x8831 // GL.DRAW_BUFFER12
    val DRAW_BUFFER13 = 0x8832 // GL.DRAW_BUFFER13
    val DRAW_BUFFER14 = 0x8833 // GL.DRAW_BUFFER14
    val DRAW_BUFFER15 = 0x8834 // GL.DRAW_BUFFER15
    val COLOR_ATTACHMENT0 = GL.COLOR_ATTACHMENT0
    val COLOR_ATTACHMENT1 = 0x8CE1 // GL.COLOR_ATTACHMENT1
    val COLOR_ATTACHMENT2 = 0x8CE2 // GL.COLOR_ATTACHMENT2
    val COLOR_ATTACHMENT3 = 0x8CE3 // GL.COLOR_ATTACHMENT3
    val COLOR_ATTACHMENT4 = 0x8CE4 // GL.COLOR_ATTACHMENT4
    val COLOR_ATTACHMENT5 = 0x8CE5 // GL.COLOR_ATTACHMENT5
    val COLOR_ATTACHMENT6 = 0x8CE6 // GL.COLOR_ATTACHMENT6
    val COLOR_ATTACHMENT7 = 0x8CE7 // GL.COLOR_ATTACHMENT7
    val COLOR_ATTACHMENT8 = 0x8CE8 // GL.COLOR_ATTACHMENT8
    val COLOR_ATTACHMENT9 = 0x8CE9 // GL.COLOR_ATTACHMENT9
    val COLOR_ATTACHMENT10 = 0x8CEA // GL.COLOR_ATTACHMENT10
    val COLOR_ATTACHMENT11 = 0x8CEB // GL.COLOR_ATTACHMENT11
    val COLOR_ATTACHMENT12 = 0x8CEC // GL.COLOR_ATTACHMENT12
    val COLOR_ATTACHMENT13 = 0x8CED // GL.COLOR_ATTACHMENT13
    val COLOR_ATTACHMENT14 = 0x8CEE // GL.COLOR_ATTACHMENT14
    val COLOR_ATTACHMENT15 = 0x8CEF // GL.COLOR_ATTACHMENT15
    abstract fun drawBuffers(bufs: IntArray)
}
@GLES3OrExtension
class EXT_draw_buffers internal constructor(gl: GLContext): DrawBuffersExtension(gl) {
    companion object {
        const val name = "EXT_draw_buffers"
        const val MAX_COLOR_ATTACHMENTS_EXT = 0x8CDF // GL.MAX_COLOR_ATTACHMENTS
        const val MAX_DRAW_BUFFERS_EXT = 0x8824 // GL.MAX_DRAW_BUFFERS
        const val DRAW_BUFFER0_EXT = 0x8825 // GL.DRAW_BUFFER0
        const val DRAW_BUFFER1_EXT = 0x8826 // GL.DRAW_BUFFER1
        const val DRAW_BUFFER2_EXT = 0x8827 // GL.DRAW_BUFFER2
        const val DRAW_BUFFER3_EXT = 0x8828 // GL.DRAW_BUFFER3
        const val DRAW_BUFFER4_EXT = 0x8829 // GL.DRAW_BUFFER4
        const val DRAW_BUFFER5_EXT = 0x882A // GL.DRAW_BUFFER5
        const val DRAW_BUFFER6_EXT = 0x882B // GL.DRAW_BUFFER6
        const val DRAW_BUFFER7_EXT = 0x882C // GL.DRAW_BUFFER7
        const val DRAW_BUFFER8_EXT = 0x882D // GL.DRAW_BUFFER8
        const val DRAW_BUFFER9_EXT = 0x882E // GL.DRAW_BUFFER9
        const val DRAW_BUFFER10_EXT = 0x882F // GL.DRAW_BUFFER10
        const val DRAW_BUFFER11_EXT = 0x8830 // GL.DRAW_BUFFER11
        const val DRAW_BUFFER12_EXT = 0x8831 // GL.DRAW_BUFFER12
        const val DRAW_BUFFER13_EXT = 0x8832 // GL.DRAW_BUFFER13
        const val DRAW_BUFFER14_EXT = 0x8833 // GL.DRAW_BUFFER14
        const val DRAW_BUFFER15_EXT = 0x8834 // GL.DRAW_BUFFER15
        const val COLOR_ATTACHMENT0_EXT = GL.COLOR_ATTACHMENT0
        const val COLOR_ATTACHMENT1_EXT = 0x8CE1 // GL.COLOR_ATTACHMENT1
        const val COLOR_ATTACHMENT2_EXT = 0x8CE2 // GL.COLOR_ATTACHMENT2
        const val COLOR_ATTACHMENT3_EXT = 0x8CE3 // GL.COLOR_ATTACHMENT3
        const val COLOR_ATTACHMENT4_EXT = 0x8CE4 // GL.COLOR_ATTACHMENT4
        const val COLOR_ATTACHMENT5_EXT = 0x8CE5 // GL.COLOR_ATTACHMENT5
        const val COLOR_ATTACHMENT6_EXT = 0x8CE6 // GL.COLOR_ATTACHMENT6
        const val COLOR_ATTACHMENT7_EXT = 0x8CE7 // GL.COLOR_ATTACHMENT7
        const val COLOR_ATTACHMENT8_EXT = 0x8CE8 // GL.COLOR_ATTACHMENT8
        const val COLOR_ATTACHMENT9_EXT = 0x8CE9 // GL.COLOR_ATTACHMENT9
        const val COLOR_ATTACHMENT10_EXT = 0x8CEA // GL.COLOR_ATTACHMENT10
        const val COLOR_ATTACHMENT11_EXT = 0x8CEB // GL.COLOR_ATTACHMENT11
        const val COLOR_ATTACHMENT12_EXT = 0x8CEC // GL.COLOR_ATTACHMENT12
        const val COLOR_ATTACHMENT13_EXT = 0x8CED // GL.COLOR_ATTACHMENT13
        const val COLOR_ATTACHMENT14_EXT = 0x8CEE // GL.COLOR_ATTACHMENT14
        const val COLOR_ATTACHMENT15_EXT = 0x8CEF // GL.COLOR_ATTACHMENT15
    }
    val MAX_COLOR_ATTACHMENTS_EXT = Companion.MAX_COLOR_ATTACHMENTS_EXT
    val MAX_DRAW_BUFFERS_EXT = Companion.MAX_DRAW_BUFFERS_EXT
    val DRAW_BUFFER0_EXT = Companion.DRAW_BUFFER0_EXT
    val DRAW_BUFFER1_EXT = Companion.DRAW_BUFFER1_EXT
    val DRAW_BUFFER2_EXT = Companion.DRAW_BUFFER2_EXT
    val DRAW_BUFFER3_EXT = Companion.DRAW_BUFFER3_EXT
    val DRAW_BUFFER4_EXT = Companion.DRAW_BUFFER4_EXT
    val DRAW_BUFFER5_EXT = Companion.DRAW_BUFFER5_EXT
    val DRAW_BUFFER6_EXT = Companion.DRAW_BUFFER6_EXT
    val DRAW_BUFFER7_EXT = Companion.DRAW_BUFFER7_EXT
    val DRAW_BUFFER8_EXT = Companion.DRAW_BUFFER8_EXT
    val DRAW_BUFFER9_EXT = Companion.DRAW_BUFFER9_EXT
    val DRAW_BUFFER10_EXT = Companion.DRAW_BUFFER10_EXT
    val DRAW_BUFFER11_EXT = Companion.DRAW_BUFFER11_EXT
    val DRAW_BUFFER12_EXT = Companion.DRAW_BUFFER12_EXT
    val DRAW_BUFFER13_EXT = Companion.DRAW_BUFFER13_EXT
    val DRAW_BUFFER14_EXT = Companion.DRAW_BUFFER14_EXT
    val DRAW_BUFFER15_EXT = Companion.DRAW_BUFFER15_EXT
    val COLOR_ATTACHMENT0_EXT = Companion.COLOR_ATTACHMENT0_EXT
    val COLOR_ATTACHMENT1_EXT = Companion.COLOR_ATTACHMENT1_EXT
    val COLOR_ATTACHMENT2_EXT = Companion.COLOR_ATTACHMENT2_EXT
    val COLOR_ATTACHMENT3_EXT = Companion.COLOR_ATTACHMENT3_EXT
    val COLOR_ATTACHMENT4_EXT = Companion.COLOR_ATTACHMENT4_EXT
    val COLOR_ATTACHMENT5_EXT = Companion.COLOR_ATTACHMENT5_EXT
    val COLOR_ATTACHMENT6_EXT = Companion.COLOR_ATTACHMENT6_EXT
    val COLOR_ATTACHMENT7_EXT = Companion.COLOR_ATTACHMENT7_EXT
    val COLOR_ATTACHMENT8_EXT = Companion.COLOR_ATTACHMENT8_EXT
    val COLOR_ATTACHMENT9_EXT = Companion.COLOR_ATTACHMENT9_EXT
    val COLOR_ATTACHMENT10_EXT = Companion.COLOR_ATTACHMENT10_EXT
    val COLOR_ATTACHMENT11_EXT = Companion.COLOR_ATTACHMENT11_EXT
    val COLOR_ATTACHMENT12_EXT = Companion.COLOR_ATTACHMENT12_EXT
    val COLOR_ATTACHMENT13_EXT = Companion.COLOR_ATTACHMENT13_EXT
    val COLOR_ATTACHMENT14_EXT = Companion.COLOR_ATTACHMENT14_EXT
    val COLOR_ATTACHMENT15_EXT = Companion.COLOR_ATTACHMENT15_EXT
    private val native = EXT_draw_buffers_native(gl)
    override fun drawBuffers(bufs: IntArray) = native.drawBuffersEXT(bufs)
}
@GLES3OrExtension
internal expect class EXT_draw_buffers_native(gl: GLContext) {
    fun drawBuffersEXT(bufs: IntArray)
}
@GLES3OrExtension
class NV_draw_buffers internal constructor(gl: GLContext): DrawBuffersExtension(gl) {
    companion object {
        const val name = "NV_draw_buffers"
        const val MAX_DRAW_BUFFERS_NV = 0x8824 // GL.MAX_DRAW_BUFFERS
        const val DRAW_BUFFER0_NV = 0x8825 // GL.DRAW_BUFFER0
        const val DRAW_BUFFER1_NV = 0x8826 // GL.DRAW_BUFFER1
        const val DRAW_BUFFER2_NV = 0x8827 // GL.DRAW_BUFFER2
        const val DRAW_BUFFER3_NV = 0x8828 // GL.DRAW_BUFFER3
        const val DRAW_BUFFER4_NV = 0x8829 // GL.DRAW_BUFFER4
        const val DRAW_BUFFER5_NV = 0x882A // GL.DRAW_BUFFER5
        const val DRAW_BUFFER6_NV = 0x882B // GL.DRAW_BUFFER6
        const val DRAW_BUFFER7_NV = 0x882C // GL.DRAW_BUFFER7
        const val DRAW_BUFFER8_NV = 0x882D // GL.DRAW_BUFFER8
        const val DRAW_BUFFER9_NV = 0x882E // GL.DRAW_BUFFER9
        const val DRAW_BUFFER10_NV = 0x882F // GL.DRAW_BUFFER10
        const val DRAW_BUFFER11_NV = 0x8830 // GL.DRAW_BUFFER11
        const val DRAW_BUFFER12_NV = 0x8831 // GL.DRAW_BUFFER12
        const val DRAW_BUFFER13_NV = 0x8832 // GL.DRAW_BUFFER13
        const val DRAW_BUFFER14_NV = 0x8833 // GL.DRAW_BUFFER14
        const val DRAW_BUFFER15_NV = 0x8834 // GL.DRAW_BUFFER15
        const val COLOR_ATTACHMENT0_NV = GL.COLOR_ATTACHMENT0
        const val COLOR_ATTACHMENT1_NV = 0x8CE1 // GL.COLOR_ATTACHMENT1
        const val COLOR_ATTACHMENT2_NV = 0x8CE2 // GL.COLOR_ATTACHMENT2
        const val COLOR_ATTACHMENT3_NV = 0x8CE3 // GL.COLOR_ATTACHMENT3
        const val COLOR_ATTACHMENT4_NV = 0x8CE4 // GL.COLOR_ATTACHMENT4
        const val COLOR_ATTACHMENT5_NV = 0x8CE5 // GL.COLOR_ATTACHMENT5
        const val COLOR_ATTACHMENT6_NV = 0x8CE6 // GL.COLOR_ATTACHMENT6
        const val COLOR_ATTACHMENT7_NV = 0x8CE7 // GL.COLOR_ATTACHMENT7
        const val COLOR_ATTACHMENT8_NV = 0x8CE8 // GL.COLOR_ATTACHMENT8
        const val COLOR_ATTACHMENT9_NV = 0x8CE9 // GL.COLOR_ATTACHMENT9
        const val COLOR_ATTACHMENT10_NV = 0x8CEA // GL.COLOR_ATTACHMENT10
        const val COLOR_ATTACHMENT11_NV = 0x8CEB // GL.COLOR_ATTACHMENT11
        const val COLOR_ATTACHMENT12_NV = 0x8CEC // GL.COLOR_ATTACHMENT12
        const val COLOR_ATTACHMENT13_NV = 0x8CED // GL.COLOR_ATTACHMENT13
        const val COLOR_ATTACHMENT14_NV = 0x8CEE // GL.COLOR_ATTACHMENT14
        const val COLOR_ATTACHMENT15_NV = 0x8CEF // GL.COLOR_ATTACHMENT15
    }
    val MAX_DRAW_BUFFERS_NV = Companion.MAX_DRAW_BUFFERS_NV
    val DRAW_BUFFER0_NV = Companion.DRAW_BUFFER0_NV
    val DRAW_BUFFER1_NV = Companion.DRAW_BUFFER1_NV
    val DRAW_BUFFER2_NV = Companion.DRAW_BUFFER2_NV
    val DRAW_BUFFER3_NV = Companion.DRAW_BUFFER3_NV
    val DRAW_BUFFER4_NV = Companion.DRAW_BUFFER4_NV
    val DRAW_BUFFER5_NV = Companion.DRAW_BUFFER5_NV
    val DRAW_BUFFER6_NV = Companion.DRAW_BUFFER6_NV
    val DRAW_BUFFER7_NV = Companion.DRAW_BUFFER7_NV
    val DRAW_BUFFER8_NV = Companion.DRAW_BUFFER8_NV
    val DRAW_BUFFER9_NV = Companion.DRAW_BUFFER9_NV
    val DRAW_BUFFER10_NV = Companion.DRAW_BUFFER10_NV
    val DRAW_BUFFER11_NV = Companion.DRAW_BUFFER11_NV
    val DRAW_BUFFER12_NV = Companion.DRAW_BUFFER12_NV
    val DRAW_BUFFER13_NV = Companion.DRAW_BUFFER13_NV
    val DRAW_BUFFER14_NV = Companion.DRAW_BUFFER14_NV
    val DRAW_BUFFER15_NV = Companion.DRAW_BUFFER15_NV
    val COLOR_ATTACHMENT0_NV = Companion.COLOR_ATTACHMENT0_NV
    val COLOR_ATTACHMENT1_NV = Companion.COLOR_ATTACHMENT1_NV
    val COLOR_ATTACHMENT2_NV = Companion.COLOR_ATTACHMENT2_NV
    val COLOR_ATTACHMENT3_NV = Companion.COLOR_ATTACHMENT3_NV
    val COLOR_ATTACHMENT4_NV = Companion.COLOR_ATTACHMENT4_NV
    val COLOR_ATTACHMENT5_NV = Companion.COLOR_ATTACHMENT5_NV
    val COLOR_ATTACHMENT6_NV = Companion.COLOR_ATTACHMENT6_NV
    val COLOR_ATTACHMENT7_NV = Companion.COLOR_ATTACHMENT7_NV
    val COLOR_ATTACHMENT8_NV = Companion.COLOR_ATTACHMENT8_NV
    val COLOR_ATTACHMENT9_NV = Companion.COLOR_ATTACHMENT9_NV
    val COLOR_ATTACHMENT10_NV = Companion.COLOR_ATTACHMENT10_NV
    val COLOR_ATTACHMENT11_NV = Companion.COLOR_ATTACHMENT11_NV
    val COLOR_ATTACHMENT12_NV = Companion.COLOR_ATTACHMENT12_NV
    val COLOR_ATTACHMENT13_NV = Companion.COLOR_ATTACHMENT13_NV
    val COLOR_ATTACHMENT14_NV = Companion.COLOR_ATTACHMENT14_NV
    val COLOR_ATTACHMENT15_NV = Companion.COLOR_ATTACHMENT15_NV
    private val native = NV_draw_buffers_native(gl)
    override fun drawBuffers(bufs: IntArray) = native.drawBuffersNV(bufs)
}
@GLES3OrExtension
internal expect class NV_draw_buffers_native(gl: GLContext) {
    fun drawBuffersNV(bufs: IntArray)
}

@GLES3OrExtension
sealed class FramebufferMultisampleExtension(gl: GLContext): Extension(gl) {
    open val MAX_SAMPLES = 0x8D57 // GL.MAX_SAMPLES
    open val RENDERBUFFER_SAMPLES = 0x8CAB // GL.RENDERBUFFER_SAMPLES
    open val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56 // GL.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE
    abstract fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
}
@GLES3OrExtension
class ANGLE_framebuffer_multisample internal constructor(gl: GLContext): FramebufferMultisampleExtension(gl) {
    companion object {
        const val name = "ANGLE_framebuffer_multisample"
        const val RENDERBUFFER_SAMPLES_ANGLE = 0x8CAB // GL.RENDERBUFFER_SAMPLES
        const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_ANGLE = 0x8D56 // GL.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE
        const val MAX_SAMPLES_ANGLE = 0x8D57 // GL.MAX_SAMPLES
    }
    val RENDERBUFFER_SAMPLES_ANGLE = Companion.RENDERBUFFER_SAMPLES_ANGLE
    val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_ANGLE =
        Companion.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_ANGLE
    val MAX_SAMPLES_ANGLE = Companion.MAX_SAMPLES_ANGLE
    private val native = ANGLE_framebuffer_multisample_native(gl)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = native.renderbufferStorageMultisampleANGLE(target, samples, internalformat, width, height)
}
@GLES3OrExtension
internal expect class ANGLE_framebuffer_multisample_native(gl: GLContext) {
    fun renderbufferStorageMultisampleANGLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
}
@GLES3OrExtension
class NV_framebuffer_multisample internal constructor(gl: GLContext): FramebufferMultisampleExtension(gl) {
    companion object {
        const val name = "NV_framebuffer_multisample"
        const val RENDERBUFFER_SAMPLES_NV = 0x8CAB // GL.RENDERBUFFER_SAMPLES
        const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_NV = 0x8D56 // GL.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE
        const val MAX_SAMPLES_NV = 0x8D57 // GL.MAX_SAMPLES
    }
    val RENDERBUFFER_SAMPLES_NV = Companion.RENDERBUFFER_SAMPLES_NV
    val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_NV = Companion.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_NV
    val MAX_SAMPLES_NV = Companion.MAX_SAMPLES_NV
    private val native = NV_framebuffer_multisample_native(gl)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = native.renderbufferStorageMultisampleNV(target, samples, internalformat, width, height)
}
@GLES3OrExtension
internal expect class NV_framebuffer_multisample_native(gl: GLContext) {
    fun renderbufferStorageMultisampleNV(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
}
@GLES3OrExtension
class APPLE_framebuffer_multisample internal constructor(gl: GLContext): FramebufferMultisampleExtension(gl) {
    companion object {
        const val name = "APPLE_framebuffer_multisample"
        const val RENDERBUFFER_SAMPLES_APPLE = 0x8CAB // GL.RENDERBUFFER_SAMPLES
        const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_APPLE = 0x8D56 // GL.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE
        const val MAX_SAMPLES_APPLE = 0x8D57 // GL.MAX_SAMPLES
        const val READ_FRAMEBUFFER_APPLE = 0x8CA8 // GL.READ_FRAMEBUFFER
        const val DRAW_FRAMEBUFFER_APPLE = 0x8CA9 // GL.DRAW_FRAMEBUFFER
        const val DRAW_FRAMEBUFFER_BINDING_APPLE = 0x8CA6 // GL.DRAW_FRAMEBUFFER_BINDING
        const val READ_FRAMEBUFFER_BINDING_APPLE = 0x8CAA // GL.READ_FRAMEBUFFER_BINDING
    }
    val RENDERBUFFER_SAMPLES_APPLE = Companion.RENDERBUFFER_SAMPLES_APPLE
    val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_APPLE =
        Companion.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_APPLE
    val MAX_SAMPLES_APPLE = Companion.MAX_SAMPLES_APPLE
    val READ_FRAMEBUFFER_APPLE = Companion.READ_FRAMEBUFFER_APPLE
    val DRAW_FRAMEBUFFER_APPLE = Companion.DRAW_FRAMEBUFFER_APPLE
    val DRAW_FRAMEBUFFER_BINDING_APPLE = Companion.DRAW_FRAMEBUFFER_BINDING_APPLE
    val READ_FRAMEBUFFER_BINDING_APPLE = Companion.READ_FRAMEBUFFER_BINDING_APPLE
    private val native = APPLE_framebuffer_multisample_native(gl)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = native.renderbufferStorageMultisampleAPPLE(target, samples, internalformat, width, height)
    fun resolveMultisampleFramebufferAPPLE() = native.resolveMultisampleFramebufferAPPLE()
}
@GLES3OrExtension
internal expect class APPLE_framebuffer_multisample_native(gl: GLContext) {
    fun renderbufferStorageMultisampleAPPLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    fun resolveMultisampleFramebufferAPPLE()
}

@GLES3OrExtension
sealed class MultisampledRTTExtension(gl: GLContext): FramebufferMultisampleExtension(gl) {
    abstract val TEXTURE_SAMPLES: Int
    abstract fun framebufferTexture2DMultisample(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int)
}
@GLES3OrExtension
class EXT_multisampled_render_to_texture internal constructor(gl: GLContext): MultisampledRTTExtension(gl) {
    companion object {
        const val name = "EXT_multisampled_render_to_texture"
        const val FRAMEBUFFER_ATTACHMENT_TEXTURE_SAMPLES_EXT = 0x8D6C
        const val RENDERBUFFER_SAMPLES_EXT = 0x8CAB
        const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT = 0x8D56
        const val MAX_SAMPLES_EXT = 0x8D57
    }
    val FRAMEBUFFER_ATTACHMENT_TEXTURE_SAMPLES_EXT =
        Companion.FRAMEBUFFER_ATTACHMENT_TEXTURE_SAMPLES_EXT
    val RENDERBUFFER_SAMPLES_EXT = Companion.RENDERBUFFER_SAMPLES_EXT
    val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT = Companion.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT
    val MAX_SAMPLES_EXT = Companion.MAX_SAMPLES_EXT
    private val native = EXT_multisampled_render_to_texture_native(gl)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = native.renderbufferStorageMultisampleEXT(target, samples, internalformat, width, height)
    override fun framebufferTexture2DMultisample(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = native.framebufferTexture2DMultisampleEXT(target, attachment, textarget, texture, level, samples)
    override val TEXTURE_SAMPLES = FRAMEBUFFER_ATTACHMENT_TEXTURE_SAMPLES_EXT
}
internal expect class EXT_multisampled_render_to_texture_native(gl: GLContext) {
    fun renderbufferStorageMultisampleEXT(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    fun framebufferTexture2DMultisampleEXT(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int)
}
@GLES3OrExtension
class IMG_multisampled_render_to_texture internal constructor(gl: GLContext): MultisampledRTTExtension(gl) {
    companion object {
        const val name = "IMG_multisampled_render_to_texture"
        // These values are not the ones from OpenGL EE 3.0 and are different than
        const val RENDERBUFFER_SAMPLES_IMG = 0x9133
        const val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_IMG = 0x9134
        const val MAX_SAMPLES_IMG = 0x9135
        const val TEXTURE_SAMPLES_IMG = 0x9136
    }
    val TEXTURE_SAMPLES_IMG = Companion.TEXTURE_SAMPLES_IMG
    val RENDERBUFFER_SAMPLES_IMG = Companion.RENDERBUFFER_SAMPLES_IMG
    val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_IMG = Companion.FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_IMG
    val MAX_SAMPLES_IMG = Companion.MAX_SAMPLES_IMG
    override val MAX_SAMPLES = MAX_SAMPLES_IMG
    override val RENDERBUFFER_SAMPLES = RENDERBUFFER_SAMPLES_IMG
    override val FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_IMG
    override val TEXTURE_SAMPLES = TEXTURE_SAMPLES_IMG
    private val native = IMG_multisampled_render_to_texture_native(gl)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = native.renderbufferStorageMultisampleIMG(target, samples, internalformat, width, height)
    override fun framebufferTexture2DMultisample(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = native.framebufferTexture2DMultisampleIMG(target, attachment, textarget, texture, level, samples)
}
internal expect class IMG_multisampled_render_to_texture_native(gl: GLContext) {
    fun renderbufferStorageMultisampleIMG(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    fun framebufferTexture2DMultisampleIMG(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int)
}

class EXT_texture_filter_anisotropic internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_texture_filter_anisotropic"
        const val TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE  // accepted by getTexParameter(), texParameterf(), and texParameteri()
        const val MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF  // accepted by getFloat()
    }
    val TEXTURE_MAX_ANISOTROPY_EXT = Companion.TEXTURE_MAX_ANISOTROPY_EXT
    val MAX_TEXTURE_MAX_ANISOTROPY_EXT = Companion.MAX_TEXTURE_MAX_ANISOTROPY_EXT
}

class ANGLE_depth_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "ANGLE_depth_texture"
        const val DEPTH_COMPONENT = 0x1902 // GL.DEPTH_COMPONENT // accepted by texImage2D()/texSubImage2D()
        const val DEPTH_STENCIL_OES = 0x84F9 // GL.DEPTH_STENCIL // accepted by texImage2D()/texSubImage2D()
        const val UNSIGNED_INT_24_8_OES = 0x84FA // GL.UNSIGNED_INT_24_8 // accepted by texImage2D()/texSubImage2D()
        const val DEPTH_COMPONENT32_OES = 0x81A7 // accepted by texImage2DExt() from EXT_texture_storage
        const val DEPTH24_STENCIL8_OES = 0x88F0 // GL.DEPTH24_STENCIL8 // accepted by renderbufferStorage()
    }
    val DEPTH_COMPONENT = Companion.DEPTH_COMPONENT
    val DEPTH_STENCIL_OES = Companion.DEPTH_STENCIL_OES
    val UNSIGNED_INT_24_8_OES = Companion.UNSIGNED_INT_24_8_OES
    val DEPTH_COMPONENT32_OES = Companion.DEPTH_COMPONENT32_OES
    val DEPTH24_STENCIL8_OES = Companion.DEPTH24_STENCIL8_OES
}
class OES_depth_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_depth_texture"
        const val DEPTH_COMPONENT = 0x1902 // GL.DEPTH_COMPONENT // accepted by texImage2D()/texSubImage2D()
    }
    val DEPTH_COMPONENT = Companion.DEPTH_COMPONENT
}
class OES_packed_depth_stencil internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_packed_depth_stencil"
        const val DEPTH_STENCIL_OES = 0x84F9 // GL.DEPTH_STENCIL // accepted by texImage2D()/texSubImage2D()
        const val UNSIGNED_INT_24_8_OES = 0x84FA // GL.UNSIGNED_INT_24_8 // accepted by texImage2D()/texSubImage2D()
        const val DEPTH24_STENCIL8_OES = 0x88F0 // GL.DEPTH24_STENCIL8 // accepted by renderbufferStorage()
    }
    val DEPTH_STENCIL_OES = Companion.DEPTH_STENCIL_OES
    val UNSIGNED_INT_24_8_OES = Companion.UNSIGNED_INT_24_8_OES
    val DEPTH24_STENCIL8_OES = Companion.DEPTH24_STENCIL8_OES
}

class OES_texture_float internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "OES_texture_float" }
}
class OES_texture_float_linear internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "OES_texture_float_linear" }
}
class OES_texture_half_float internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_texture_half_float"
        const val HALF_FLOAT_OES = 0x8D61 // accepted by texImage2D()/texSubImage2D()
        // NOTE: not equal to GL.HALF_FLOAT
    }
    val HALF_FLOAT_OES = Companion.HALF_FLOAT_OES
}
class OES_texture_half_float_linear internal constructor(gl: GLContext): Extension(gl) {
    companion object { const val name = "OES_texture_half_float_linear" }
}

class EXT_sRGB internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_sRGB"
        const val SRGB_EXT = 0x8C40 // GL.SRGB accepted by texImage2D()/texSubImage2D()
        const val SRGB_ALPHA_EXT = 0x8C42 // accepted by texImage2D()/texSubImage2D()
        const val SRGB8_ALPHA8_EXT = 0x8C43 // GL.SRGB8_ALPHA8 // accepted by renderbufferStorage()
        const val FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING_EXT = 0x8210 // GL.FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING // accepted by getFramebufferAttachmentParameter()
    }
    val SRGB_EXT = Companion.SRGB_EXT
    val SRGB_ALPHA_EXT = Companion.SRGB_ALPHA_EXT
    val SRGB8_ALPHA8_EXT = Companion.SRGB8_ALPHA8_EXT
    val FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING_EXT =
        Companion.FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING_EXT
}


//////////////////// Compressed Texture Formats ////////////////////
class EXT_texture_compression_s3tc_srgb internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_texture_compression_s3tc_srgb"
        const val COMPRESSED_SRGB_S3TC_DXT1_EXT = 0x8C4C
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT = 0x8C4D
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT = 0x8C4E
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT = 0x8C4F
    }
    val COMPRESSED_SRGB_S3TC_DXT1_EXT = Companion.COMPRESSED_SRGB_S3TC_DXT1_EXT
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT
}
class EXT_texture_compression_dxt1 internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_texture_compression_dxt1"
        const val COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0
        const val COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1
    }
    val COMPRESSED_RGB_S3TC_DXT1_EXT = Companion.COMPRESSED_RGB_S3TC_DXT1_EXT
    val COMPRESSED_RGBA_S3TC_DXT1_EXT = Companion.COMPRESSED_RGBA_S3TC_DXT1_EXT
}
class EXT_texture_compression_s3tc internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_texture_compression_s3tc"
        const val COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2
        const val COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3
    }
    val COMPRESSED_RGBA_S3TC_DXT3_EXT = Companion.COMPRESSED_RGBA_S3TC_DXT3_EXT
    val COMPRESSED_RGBA_S3TC_DXT5_EXT = Companion.COMPRESSED_RGBA_S3TC_DXT5_EXT
}
class IMG_texture_compression_pvrtc internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "IMG_texture_compression_pvrtc"
        const val COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00
        const val COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01
        const val COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02
        const val COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03
    }
    val COMPRESSED_RGB_PVRTC_4BPPV1_IMG = Companion.COMPRESSED_RGB_PVRTC_4BPPV1_IMG
    val COMPRESSED_RGB_PVRTC_2BPPV1_IMG = Companion.COMPRESSED_RGB_PVRTC_2BPPV1_IMG
    val COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = Companion.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG
    val COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = Companion.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG
}
class OES_compressed_ETC1_RGB8_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_compressed_ETC1_RGB8_texture"
        const val ETC1_RGB8_OES = 0x8D64
    }
    val ETC1_RGB8_OES = Companion.ETC1_RGB8_OES
}
abstract class KHR_texture_compression_astc internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93B0
        const val COMPRESSED_RGBA_ASTC_5x4_KHR = 0x93B1
        const val COMPRESSED_RGBA_ASTC_5x5_KHR = 0x93B2
        const val COMPRESSED_RGBA_ASTC_6x5_KHR = 0x93B3
        const val COMPRESSED_RGBA_ASTC_6x6_KHR = 0x93B4
        const val COMPRESSED_RGBA_ASTC_8x5_KHR = 0x93B5
        const val COMPRESSED_RGBA_ASTC_8x6_KHR = 0x93B6
        const val COMPRESSED_RGBA_ASTC_8x8_KHR = 0x93B7
        const val COMPRESSED_RGBA_ASTC_10x5_KHR = 0x93B8
        const val COMPRESSED_RGBA_ASTC_10x6_KHR = 0x93B9
        const val COMPRESSED_RGBA_ASTC_10x8_KHR = 0x93BA
        const val COMPRESSED_RGBA_ASTC_10x10_KHR = 0x93BB
        const val COMPRESSED_RGBA_ASTC_12x10_KHR = 0x93BC
        const val COMPRESSED_RGBA_ASTC_12x12_KHR = 0x93BD
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 0x93D0
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = 0x93D1
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = 0x93D2
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = 0x93D3
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = 0x93D4
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = 0x93D5
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = 0x93D6
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = 0x93D7
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = 0x93D8
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = 0x93D9
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = 0x93DA
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = 0x93DB
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = 0x93DC
        const val COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = 0x93DD
    }
    val COMPRESSED_RGBA_ASTC_4x4_KHR = Companion.COMPRESSED_RGBA_ASTC_4x4_KHR
    val COMPRESSED_RGBA_ASTC_5x4_KHR = Companion.COMPRESSED_RGBA_ASTC_5x4_KHR
    val COMPRESSED_RGBA_ASTC_5x5_KHR = Companion.COMPRESSED_RGBA_ASTC_5x5_KHR
    val COMPRESSED_RGBA_ASTC_6x5_KHR = Companion.COMPRESSED_RGBA_ASTC_6x5_KHR
    val COMPRESSED_RGBA_ASTC_6x6_KHR = Companion.COMPRESSED_RGBA_ASTC_6x6_KHR
    val COMPRESSED_RGBA_ASTC_8x5_KHR = Companion.COMPRESSED_RGBA_ASTC_8x5_KHR
    val COMPRESSED_RGBA_ASTC_8x6_KHR = Companion.COMPRESSED_RGBA_ASTC_8x6_KHR
    val COMPRESSED_RGBA_ASTC_8x8_KHR = Companion.COMPRESSED_RGBA_ASTC_8x8_KHR
    val COMPRESSED_RGBA_ASTC_10x5_KHR = Companion.COMPRESSED_RGBA_ASTC_10x5_KHR
    val COMPRESSED_RGBA_ASTC_10x6_KHR = Companion.COMPRESSED_RGBA_ASTC_10x6_KHR
    val COMPRESSED_RGBA_ASTC_10x8_KHR = Companion.COMPRESSED_RGBA_ASTC_10x8_KHR
    val COMPRESSED_RGBA_ASTC_10x10_KHR = Companion.COMPRESSED_RGBA_ASTC_10x10_KHR
    val COMPRESSED_RGBA_ASTC_12x10_KHR = Companion.COMPRESSED_RGBA_ASTC_12x10_KHR
    val COMPRESSED_RGBA_ASTC_12x12_KHR = Companion.COMPRESSED_RGBA_ASTC_12x12_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR
    val COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = Companion.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR
}
class KHR_texture_compression_astc_hdr internal constructor(gl: GLContext): KHR_texture_compression_astc(gl) {
    companion object { const val name = "KHR_texture_compression_astc_hdr" }
}
class KHR_texture_compression_astc_ldr internal constructor(gl: GLContext): KHR_texture_compression_astc(gl) {
    companion object { const val name = "KHR_texture_compression_astc_ldr" }
}
class EXT_texture_compression_bptc internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "EXT_texture_compression_bptc"
        const val COMPRESSED_RGBA_BPTC_UNORM_EXT = 0x8E8C
        const val COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT = 0x8E8D
        const val COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT = 0x8E8E
        const val COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT = 0x8E8F
    }
    val COMPRESSED_RGBA_BPTC_UNORM_EXT = Companion.COMPRESSED_RGBA_BPTC_UNORM_EXT
    val COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT = Companion.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT
    val COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT = Companion.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT
    val COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT = Companion.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT
}
class NV_sRGB_formats internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "NV_sRGB_formats"
        const val SLUMINANCE_NV = 0x8C46
        const val SLUMINANCE_ALPHA_NV = 0x8C44
        const val SLUMINANCE8_NV = 0x8C47
        const val SLUMINANCE8_ALPHA8_NV = 0x8C45
        const val ETC1_SRGB8_NV = 0x88EE

        // also in EXT_sRGB (but has a different value)
        const val SRGB8_NV = 0x8C41

        // same as EXT_texture_compression_s3tc_srgb
        const val COMPRESSED_SRGB_S3TC_DXT1_NV = 0x8C4C
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT1_NV = 0x8C4D
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT3_NV = 0x8C4E
        const val COMPRESSED_SRGB_ALPHA_S3TC_DXT5_NV = 0x8C4F
    }
    val SLUMINANCE_NV = Companion.SLUMINANCE_NV
    val SLUMINANCE_ALPHA_NV = Companion.SLUMINANCE_ALPHA_NV
    val SRGB8_NV = Companion.SRGB8_NV
    val SLUMINANCE8_NV = Companion.SLUMINANCE8_NV
    val SLUMINANCE8_ALPHA8_NV = Companion.SLUMINANCE8_ALPHA8_NV
    val COMPRESSED_SRGB_S3TC_DXT1_NV = Companion.COMPRESSED_SRGB_S3TC_DXT1_NV
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT1_NV = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_NV
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT3_NV = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_NV
    val COMPRESSED_SRGB_ALPHA_S3TC_DXT5_NV = Companion.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_NV
    val ETC1_SRGB8_NV = Companion.ETC1_SRGB8_NV
}

// The remainder are not actually exposed anywhere
class AMD_compressed_3DC_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "AMD_compressed_3DC_texture"
        const val _3DC_X_AMD = 0x87F9
        const val _3DC_XY_AMD = 0x87FA
    }
    val _3DC_X_AMD = Companion._3DC_X_AMD
    val _3DC_XY_AMD = Companion._3DC_XY_AMD
}
class AMD_compressed_ATC_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "AMD_compressed_ATC_texture"
        const val ATC_RGB_AMD = 0x8C92
        const val ATC_RGBA_EXPLICIT_ALPHA_AMD = 0x8C93
        const val ATC_RGBA_INTERPOLATED_ALPHA_AMD = 0x87EE
    }
    val ATC_RGB_AMD = Companion.ATC_RGB_AMD
    val ATC_RGBA_EXPLICIT_ALPHA_AMD = Companion.ATC_RGBA_EXPLICIT_ALPHA_AMD
    val ATC_RGBA_INTERPOLATED_ALPHA_AMD = Companion.ATC_RGBA_INTERPOLATED_ALPHA_AMD
}
class OES_compressed_paletted_texture internal constructor(gl: GLContext): Extension(gl) {
    companion object {
        const val name = "OES_compressed_paletted_texture"
        const val PALETTE4_RGB8_OES = 0x8B90
        const val PALETTE4_RGBA8_OES = 0x8B91
        const val PALETTE4_R5_G6_B5_OES = 0x8B92
        const val PALETTE4_RGBA4_OES = 0x8B93
        const val PALETTE4_RGB5_A1_OES = 0x8B94
        const val PALETTE8_RGB8_OES = 0x8B95
        const val PALETTE8_RGBA8_OES = 0x8B96
        const val PALETTE8_R5_G6_B5_OES = 0x8B97
        const val PALETTE8_RGBA4_OES = 0x8B98
        const val PALETTE8_RGB5_A1_OES = 0x8B99
    }
    val PALETTE4_RGB8_OES = Companion.PALETTE4_RGB8_OES
    val PALETTE4_RGBA8_OES = Companion.PALETTE4_RGBA8_OES
    val PALETTE4_R5_G6_B5_OES = Companion.PALETTE4_R5_G6_B5_OES
    val PALETTE4_RGBA4_OES = Companion.PALETTE4_RGBA4_OES
    val PALETTE4_RGB5_A1_OES = Companion.PALETTE4_RGB5_A1_OES
    val PALETTE8_RGB8_OES = Companion.PALETTE8_RGB8_OES
    val PALETTE8_RGBA8_OES = Companion.PALETTE8_RGBA8_OES
    val PALETTE8_R5_G6_B5_OES = Companion.PALETTE8_R5_G6_B5_OES
    val PALETTE8_RGBA4_OES = Companion.PALETTE8_RGBA4_OES
    val PALETTE8_RGB5_A1_OES = Companion.PALETTE8_RGB5_A1_OES
}
