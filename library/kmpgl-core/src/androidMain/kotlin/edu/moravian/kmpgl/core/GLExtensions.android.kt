@file:Suppress("ClassName", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package edu.moravian.kmpgl.core

import android.opengl.GLES20.GL_FALSE

internal actual class ANGLE_instanced_arrays_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { glDrawArraysInstancedANGLE(mode, first, count, instanceCount) }
    private external fun glDrawArraysInstancedANGLE(mode: Int, first: Int, count: Int, instanceCount: Int)
    actual fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { glDrawElementsInstancedANGLE(mode, count, type, indicesOffset, instanceCount) }
    private external fun glDrawElementsInstancedANGLE(mode: Int, count: Int, type: Int, offset: Int, primcount: Int)
    actual fun vertexAttribDivisorANGLE(index: Int, divisor: Int) = gl.checkGL { glVertexAttribDivisorANGLE(index, divisor) }
    private external fun glVertexAttribDivisorANGLE(index: Int, divisor: Int)
}
internal actual class EXT_instanced_arrays_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun drawArraysInstancedEXT(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { glDrawArraysInstancedEXT(mode, first, count, instanceCount) }
    private external fun glDrawArraysInstancedEXT(mode: Int, first: Int, count: Int, instanceCount: Int)
    actual fun drawElementsInstancedEXT(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { glDrawElementsInstancedEXT(mode, count, type, indicesOffset, instanceCount) }
    private external fun glDrawElementsInstancedEXT(mode: Int, count: Int, type: Int, offset: Int, primcount: Int)
    actual fun vertexAttribDivisorEXT(index: Int, divisor: Int) = gl.checkGL { glVertexAttribDivisorEXT(index, divisor) }
    private external fun glVertexAttribDivisorEXT(index: Int, divisor: Int)
}
internal actual class NV_instanced_arrays_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun drawArraysInstancedNV(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { glDrawArraysInstancedNV(mode, first, count, instanceCount) }
    private external fun glDrawArraysInstancedNV(mode: Int, first: Int, count: Int, instanceCount: Int)
    actual fun drawElementsInstancedNV(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { glDrawElementsInstancedNV(mode, count, type, indicesOffset, instanceCount) }
    private external fun glDrawElementsInstancedNV(mode: Int, count: Int, type: Int, offset: Int, primcount: Int)
    actual fun vertexAttribDivisorNV(index: Int, divisor: Int) = gl.checkGL { glVertexAttribDivisorNV(index, divisor) }
    private external fun glVertexAttribDivisorNV(index: Int, divisor: Int)
}

internal actual class OES_vertex_array_object_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun genVertexArrayOES() = gl.checkGL { glGenVertexArraysOES(1, gl.intTmp); GLVertexArrayObject(gl.intTmp[0]) }
    actual fun genVertexArraysOES(n: Int) = gl.checkGL { GLVertexArrayObjectArray(IntArray(n).also { glGenVertexArraysOES(n, it) }) }
    private external fun glGenVertexArraysOES(n: Int, out: IntArray): IntArray
    actual fun deleteVertexArrayOES(array: GLVertexArrayObject) = gl.checkGL { gl.intTmp[0] = array.id; glDeleteVertexArraysOES(1, gl.intTmp) }
    actual fun deleteVertexArraysOES(arrays: GLVertexArrayObjectArray) = gl.checkGL { glDeleteVertexArraysOES(arrays.size, arrays.ids) }
    private external fun glDeleteVertexArraysOES(n: Int, arrays: IntArray)
    actual fun isVertexArrayOES(array: Int) = gl.checkGL { glIsVertexArrayOES(array) != GL_FALSE }
    private external fun glIsVertexArrayOES(array: Int): Int
    actual fun bindVertexArrayOES(array: GLVertexArrayObject) = gl.checkGL { glBindVertexArrayOES(array.id) }
    private external fun glBindVertexArrayOES(array: Int)
}

internal actual class EXT_draw_buffers_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun drawBuffersEXT(bufs: IntArray) = gl.checkGL { glDrawBuffersEXT(bufs) }
    private external fun glDrawBuffersEXT(bufs: IntArray)
}
internal actual class NV_draw_buffers_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun drawBuffersNV(bufs: IntArray) = gl.checkGL { glDrawBuffersNV(bufs) }
    private external fun glDrawBuffersNV(bufs: IntArray)
}

internal actual class ANGLE_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun renderbufferStorageMultisampleANGLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleANGLE(target, samples, internalformat, width, height) }
    private external fun glRenderbufferStorageMultisampleANGLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
}
internal actual class NV_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun renderbufferStorageMultisampleNV(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleNV(target, samples, internalformat, width, height) }
    private external fun glRenderbufferStorageMultisampleNV(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
}
internal actual class APPLE_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun renderbufferStorageMultisampleAPPLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleAPPLE(target, samples, internalformat, width, height) }
    private external fun glRenderbufferStorageMultisampleAPPLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    actual fun resolveMultisampleFramebufferAPPLE() = gl.checkGL { glResolveMultisampleFramebufferAPPLE() }
    private external fun glResolveMultisampleFramebufferAPPLE()
}
internal actual class EXT_multisampled_render_to_texture_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun renderbufferStorageMultisampleEXT(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleEXT(target, samples, internalformat, width, height) }
    private external fun glRenderbufferStorageMultisampleEXT(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    actual fun framebufferTexture2DMultisampleEXT(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = gl.checkGL { glFramebufferTexture2DMultisampleEXT(target, attachment, textarget, texture, level, samples) }
    private external fun glFramebufferTexture2DMultisampleEXT(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int)
}
internal actual class IMG_multisampled_render_to_texture_native actual constructor(private val gl: GLContext) {
    init { System.loadLibrary("opengl-extensions") }
    actual fun renderbufferStorageMultisampleIMG(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { glRenderbufferStorageMultisampleIMG(target, samples, internalformat, width, height) }
    private external fun glRenderbufferStorageMultisampleIMG(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
    actual fun framebufferTexture2DMultisampleIMG(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = gl.checkGL { glFramebufferTexture2DMultisampleIMG(target, attachment, textarget, texture, level, samples) }
    private external fun glFramebufferTexture2DMultisampleIMG(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int)
}
