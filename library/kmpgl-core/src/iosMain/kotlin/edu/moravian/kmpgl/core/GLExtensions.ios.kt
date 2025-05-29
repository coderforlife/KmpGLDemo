@file:Suppress("ClassName", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.*
import edu.moravian.kmpgl.core.GLContext.Companion.asPtr
import edu.moravian.kmpgl.core.GLContext.Companion.ptr
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret

//private var lib: CPointer<out CPointed>? = null
//private val glLib get() = lib ?:  // may need "${NSBundle.mainBundle.resourcePath}/..."
//    dlopen("libGLESv2.framework/libGLESv2", 0)?.also { lib = it } ?:
//    throw RuntimeException("Failed to load libGLESv2: ${dlerror()?.toKStringFromUtf8()}")
//private fun tryGetGLFunc(name: String) = dlsym(glLib, name)
//private fun <T: Function<*>> getGLFunc(name: String): CPointer<CFunction<T>> =
//    tryGetGLFunc(name)?.reinterpret() ?: throw RuntimeException("Failed to find function $name in libGLESv2: ${dlerror()?.toKStringFromUtf8()}")

internal fun tryGetGLFunc(name: String) = eglGetProcAddress(name)
internal fun <T: Function<*>> getGLFunc(name: String): CPointer<CFunction<T>> =
    tryGetGLFunc(name)?.reinterpret() ?: throw RuntimeException("Failed to find function $name")

@GLES3OrExtension
internal actual class ANGLE_instanced_arrays_native actual constructor(private val gl: GLContext) {
    val drawArraysInstanced: PFNGLDRAWARRAYSINSTANCEDANGLEPROC by lazy { getGLFunc("glDrawArraysInstancedANGLE") }
    val drawElementsInstanced: PFNGLDRAWELEMENTSINSTANCEDANGLEPROC by lazy { getGLFunc("glDrawElementsInstancedANGLE") }
    val vertexAttribDivisor: PFNGLVERTEXATTRIBDIVISORANGLEPROC by lazy { getGLFunc("glVertexAttribDivisorANGLE") }
    actual fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { drawArraysInstanced(mode.convert(), first, count, instanceCount) }
    actual fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { drawElementsInstanced(mode.convert(), count, type.convert(), indicesOffset.asPtr<UByteVar>(), instanceCount) }
    actual fun vertexAttribDivisorANGLE(index: Int, divisor: Int) = gl.checkGL { vertexAttribDivisor(index.convert(), divisor.convert()) }
}
@GLES3OrExtension
internal actual class EXT_instanced_arrays_native actual constructor(private val gl: GLContext) {
    val drawArraysInstanced: PFNGLDRAWARRAYSINSTANCEDEXTPROC by lazy { getGLFunc("glDrawArraysInstancedEXT") }
    val drawElementsInstanced: PFNGLDRAWELEMENTSINSTANCEDEXTPROC by lazy { getGLFunc("glDrawElementsInstancedEXT") }
    val vertexAttribDivisor: PFNGLVERTEXATTRIBDIVISOREXTPROC by lazy { getGLFunc("glVertexAttribDivisorEXT") }
    actual fun drawArraysInstancedEXT(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { drawArraysInstanced(mode.convert(), first, count, instanceCount) }
    actual fun drawElementsInstancedEXT(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { drawElementsInstanced(mode.convert(), count, type.convert(), indicesOffset.asPtr<UByteVar>(), instanceCount) }
    actual fun vertexAttribDivisorEXT(index: Int, divisor: Int) = gl.checkGL { vertexAttribDivisor(index.convert(), divisor.convert()) }
}
@GLES3OrExtension
internal actual class NV_instanced_arrays_native actual constructor(private val gl: GLContext) {
    val drawArraysInstanced: PFNGLDRAWARRAYSINSTANCEDNVPROC by lazy { getGLFunc("glDrawArraysInstancedNV") }
    val drawElementsInstanced: PFNGLDRAWELEMENTSINSTANCEDNVPROC by lazy { getGLFunc("glDrawElementsInstancedNV") }
    val vertexAttribDivisor: PFNGLVERTEXATTRIBDIVISORNVPROC by lazy { getGLFunc("glVertexAttribDivisorNV") }
    actual fun drawArraysInstancedNV(mode: Int, first: Int, count: Int, instanceCount: Int) = gl.checkGL { drawArraysInstanced(mode.convert(), first, count, instanceCount) }
    actual fun drawElementsInstancedNV(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = gl.checkGL { drawElementsInstanced(mode.convert(), count, type.convert(), indicesOffset.asPtr<UByteVar>(), instanceCount) }
    actual fun vertexAttribDivisorNV(index: Int, divisor: Int) = gl.checkGL { vertexAttribDivisor(index.convert(), divisor.convert()) }
}

@OptIn(ExperimentalForeignApi::class)
@GLES3OrExtension
internal actual class OES_vertex_array_object_native actual constructor(private val gl: GLContext) {
    private val genVertexArrays: PFNGLGENVERTEXARRAYSOESPROC by lazy { getGLFunc("glGenVertexArraysOES") }
    private val deleteVertexArrays: PFNGLDELETEVERTEXARRAYSOESPROC by lazy { getGLFunc("glDeleteVertexArraysOES") }
    private val isVertexArray: PFNGLISVERTEXARRAYOESPROC by lazy { getGLFunc("glIsVertexArrayOES") }
    private val bindVertexArray: PFNGLBINDVERTEXARRAYOESPROC by lazy { getGLFunc("glBindVertexArrayOES") }
    actual fun genVertexArrayOES(): GLVertexArrayObject = gl.checkGLWithMem { genVertexArrays(1, ptr(gl.uintPtr)); GLVertexArrayObject(gl.intTmp[0]) }
    actual fun genVertexArraysOES(n: Int): GLVertexArrayObjectArray = gl.checkGLWithMem { GLVertexArrayObjectArray(IntArray(n).also { genVertexArrays(n, ptr(it.asUIntArray().refTo(0))) }) }
    actual fun deleteVertexArrayOES(array: GLVertexArrayObject) = gl.checkGLWithMem { gl.intTmp[0] = array.id; deleteVertexArrays(1, ptr(gl.uintPtr)) }
    actual fun deleteVertexArraysOES(arrays: GLVertexArrayObjectArray) = gl.checkGLWithMem { deleteVertexArrays(arrays.size, ptr(arrays.ids.asUIntArray().refTo(0))) }
    actual fun isVertexArrayOES(array: Int) = gl.checkGL { isVertexArray(array.toUInt()).toInt() != GL_FALSE }
    actual fun bindVertexArrayOES(array: GLVertexArrayObject) = gl.checkGL { bindVertexArray(array.id.toUInt()) }
}

@GLES3OrExtension
internal actual class EXT_draw_buffers_native actual constructor(private val gl: GLContext) {
    private val drawBuffers: PFNGLDRAWBUFFERSEXTPROC by lazy { getGLFunc("glDrawBuffersEXT") }
    actual fun drawBuffersEXT(bufs: IntArray) = gl.checkGLWithMem { drawBuffers(bufs.size, ptr(bufs.asUIntArray().refTo(0))) }
}
@GLES3OrExtension
internal actual class NV_draw_buffers_native actual constructor(private val gl: GLContext) {
    private val drawBuffers: PFNGLDRAWBUFFERSNVPROC by lazy { getGLFunc("glDrawBuffersNV") }
    actual fun drawBuffersNV(bufs: IntArray) = gl.checkGLWithMem { drawBuffers(bufs.size, ptr(bufs.asUIntArray().refTo(0))) }
}

@GLES3OrExtension
internal actual class ANGLE_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    private val renderbufferStorageMultisample: PFNGLRENDERBUFFERSTORAGEMULTISAMPLEANGLEPROC by lazy { getGLFunc("glRenderbufferStorageMultisampleANGLE") }
    actual fun renderbufferStorageMultisampleANGLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL {
        renderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height)
    }
}
@GLES3OrExtension
internal actual class NV_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    private val renderbufferStorageMultisample: PFNGLRENDERBUFFERSTORAGEMULTISAMPLENVPROC by lazy { getGLFunc("glRenderbufferStorageMultisampleNV") }
    actual fun renderbufferStorageMultisampleNV(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL {
        renderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height)
    }
}
@GLES3OrExtension
internal actual class APPLE_framebuffer_multisample_native actual constructor(private val gl: GLContext) {
    private val renderbufferStorageMultisample: PFNGLRENDERBUFFERSTORAGEMULTISAMPLEAPPLEPROC by lazy { getGLFunc("glRenderbufferStorageMultisampleAPPLE") }
    private val resolveMultisampleFramebuffer: PFNGLRESOLVEMULTISAMPLEFRAMEBUFFERAPPLEPROC by lazy { getGLFunc("glResolveMultisampleFramebufferAPPLE") }
    actual fun renderbufferStorageMultisampleAPPLE(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL { renderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height) }
    actual fun resolveMultisampleFramebufferAPPLE() = gl.checkGL { resolveMultisampleFramebuffer() }
}

internal actual class EXT_multisampled_render_to_texture_native actual constructor(private val gl: GLContext) {
    private val renderbufferStorageMultisample: PFNGLRENDERBUFFERSTORAGEMULTISAMPLEEXTPROC by lazy { getGLFunc("glRenderbufferStorageMultisampleEXT") }
    private val framebufferTexture2DMultisample: PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEEXTPROC by lazy { getGLFunc("glFramebufferTexture2DMultisampleEXT") }
    actual fun renderbufferStorageMultisampleEXT(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL {
        renderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height)
    }
    actual fun framebufferTexture2DMultisampleEXT(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = gl.checkGL {
        framebufferTexture2DMultisample(target.convert(), attachment.convert(), textarget.convert(), texture.convert(), level, samples)
    }
}
internal actual class IMG_multisampled_render_to_texture_native actual constructor(private val gl: GLContext) {
    private val renderbufferStorageMultisample: PFNGLRENDERBUFFERSTORAGEMULTISAMPLEIMGPROC by lazy { getGLFunc("glRenderbufferStorageMultisampleIMG") }
    private val framebufferTexture2DMultisample: PFNGLFRAMEBUFFERTEXTURE2DMULTISAMPLEIMGPROC by lazy { getGLFunc("glFramebufferTexture2DMultisampleIMG") }
    actual fun renderbufferStorageMultisampleIMG(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = gl.checkGL {
        renderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height)
    }
    actual fun framebufferTexture2DMultisampleIMG(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, samples: Int) = gl.checkGL {
        framebufferTexture2DMultisample(target.convert(), attachment.convert(), textarget.convert(), texture.convert(), level, samples)
    }
}
