@file:Suppress("ClassName", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import edu.moravian.kmpgl.core.GLContext.Companion.asPtr
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.refTo
import platform.gles2.*

// Only one class in each group has support on iOS, others all throw unsupported operation exception
// (except draw buffers - it doesn't support that at all as an extension)

internal actual class ANGLE_instanced_arrays_native actual constructor(gl: GLContext) {
    actual fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, instanceCount: Int) { throw UnsupportedOperationException() }
    actual fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) { throw UnsupportedOperationException() }
    actual fun vertexAttribDivisorANGLE(index: Int, divisor: Int) { throw UnsupportedOperationException() }
}
internal actual class EXT_instanced_arrays_native actual constructor(private val gl: GLContext) {
    actual fun drawArraysInstancedEXT(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { glDrawArraysInstancedEXT(mode.toUInt(), first, count, instanceCount) }
    actual fun drawElementsInstancedEXT(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { glDrawElementsInstancedEXT(mode.toUInt(), count, type.toUInt(), indicesOffset.asPtr<UByteVar>(), instanceCount) }
    actual fun vertexAttribDivisorEXT(index: Int, divisor: Int) = gl.checkGL { glVertexAttribDivisorEXT(index.toUInt(), divisor.toUInt()) }
}
internal actual class NV_instanced_arrays_native actual constructor(gl: GLContext) {
    actual fun drawArraysInstancedNV(mode: Int, first: Int, count: Int, instanceCount: Int) { throw UnsupportedOperationException() }
    actual fun drawElementsInstancedNV(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) { throw UnsupportedOperationException() }
    actual fun vertexAttribDivisorNV(index: Int, divisor: Int) { throw UnsupportedOperationException() }
}

@OptIn(ExperimentalForeignApi::class)
@GLES3OrExtension
internal actual class OES_vertex_array_object_native actual constructor(private val gl: GLContext) {
    actual fun genVertexArrayOES(): GLVertexArrayObject = gl.checkGL { glGenVertexArraysOES(1, gl.uintPtr); GLVertexArrayObject(gl.intTmp[0]) }
    actual fun genVertexArraysOES(n: Int): GLVertexArrayObjectArray = gl.checkGL { GLVertexArrayObjectArray(IntArray(n).also { glGenVertexArraysOES(n, it.asUIntArray().refTo(0)) }) }
    actual fun deleteVertexArrayOES(array: GLVertexArrayObject) = gl.checkGL { gl.intTmp[0] = array.id; glDeleteBuffers(1, gl.uintPtr) }
    actual fun deleteVertexArraysOES(arrays: GLVertexArrayObjectArray) = gl.checkGL { glDeleteVertexArraysOES(arrays.size, arrays.ids.asUIntArray().refTo(0)) }
    actual fun isVertexArrayOES(array: Int) = gl.checkGL { glIsVertexArrayOES(array.toUInt()).toInt() != GL_FALSE }
    actual fun bindVertexArrayOES(array: GLVertexArrayObject) = gl.checkGL { glBindVertexArrayOES(array.id.toUInt()) }
}

internal actual class EXT_draw_buffers_native actual constructor(gl: GLContext) {
    actual fun drawBuffersEXT(bufs: IntArray) { throw UnsupportedOperationException() }
}
internal actual class NV_draw_buffers_native actual constructor(gl: GLContext) {
    actual fun drawBuffersNV(bufs: IntArray) { throw UnsupportedOperationException() }
}

internal actual class ANGLE_framebuffer_multisample_native actual constructor(gl: GLContext) {
    actual fun renderbufferStorageMultisampleANGLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) { throw UnsupportedOperationException() }
}
internal actual class NV_framebuffer_multisample_native actual constructor(gl: GLContext) {
    actual fun renderbufferStorageMultisampleNV(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) { throw UnsupportedOperationException() }
}
internal actual class APPLE_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    // NOTE: these also exist in platform.gles but we are using the gles2 ones
    actual fun renderbufferStorageMultisampleAPPLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleAPPLE(target.toUInt(), samples, internalformat.toUInt(), width, height) }
    actual fun resolveMultisampleFramebufferAPPLE() = gl.checkGL { glResolveMultisampleFramebufferAPPLE() }
}
internal actual class EXT_multisampled_render_to_texture_native actual constructor(gl: GLContext) {
    actual fun renderbufferStorageMultisampleEXT(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) { throw UnsupportedOperationException() }
    actual fun framebufferTexture2DMultisampleEXT(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) { throw UnsupportedOperationException() }
}
internal actual class IMG_multisampled_render_to_texture_native actual constructor(gl: GLContext) {
    actual fun renderbufferStorageMultisampleIMG(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) { throw UnsupportedOperationException() }
    actual fun framebufferTexture2DMultisampleIMG(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) { throw UnsupportedOperationException() }
}
