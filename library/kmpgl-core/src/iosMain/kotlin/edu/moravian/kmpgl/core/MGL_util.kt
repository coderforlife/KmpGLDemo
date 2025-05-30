// Inspired by MetalANGLE's MGLKit: https://github.com/kakashidinho/metalangle/tree/master/ios/xcode/MGLKit

@file:OptIn(ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.EGLBoolean
import angle.EGLConfig
import angle.EGLConfigVar
import angle.EGLContext
import angle.EGLDisplay
import angle.EGLSurface
import angle.EGL_ALPHA_SIZE
import angle.EGL_BLUE_SIZE
import angle.EGL_DEPTH_SIZE
import angle.EGL_FALSE
import angle.EGL_GL_COLORSPACE_LINEAR_KHR
import angle.EGL_GL_COLORSPACE_SRGB_KHR
import angle.EGL_GREEN_SIZE
import angle.EGL_NONE
import angle.EGL_NO_CONTEXT
import angle.EGL_NO_SURFACE
import angle.EGL_RED_SIZE
import angle.EGL_SAMPLES
import angle.EGL_SAMPLE_BUFFERS
import angle.EGL_STENCIL_SIZE
import angle.EGLintVar
import angle.eglChooseConfig
import angle.eglCreateWindowSurface
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.QuartzCore.CALayer


///// Configuration /////

enum class ColorFormat(
    val red: Int = 8, val green: Int = 8, val blue: Int = 8, val alpha: Int = 8,
    val colorSpace: Int = EGL_GL_COLORSPACE_LINEAR_KHR
) { RGBA8888, SRGBA8888(colorSpace = EGL_GL_COLORSPACE_SRGB_KHR) }
enum class StencilFormat(val value: Int) { None(0), SF8(8) }
enum class DepthFormat(val value: Int) { None(0), DF16(16), DF24(24) }
enum class Multisample(val value: Int) { None(0), X4(4) }

data class Config(
    val colorFormat: ColorFormat = ColorFormat.RGBA8888,
    val depthFormat: DepthFormat = DepthFormat.None,
    val stencilFormat: StencilFormat = StencilFormat.None,
    val multisample: Multisample = Multisample.None,
    val retainedBacking: Boolean = false
)

internal fun Config.toAttribs() = intArrayOf(
    EGL_RED_SIZE,       colorFormat.red,
    EGL_GREEN_SIZE,     colorFormat.green,
    EGL_BLUE_SIZE,      colorFormat.blue,
    EGL_ALPHA_SIZE,     colorFormat.alpha,
    EGL_DEPTH_SIZE,     depthFormat.value,
    EGL_STENCIL_SIZE,   stencilFormat.value,
    EGL_SAMPLE_BUFFERS, 0,
    EGL_SAMPLES,        multisample.value,
    EGL_NONE
)


///// CGFSize/CGRect Kotlin Versions /////

data class Size(val width: Int, val height: Int)
data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

internal val CValue<CGSize>.kSize get() = useContents { Size(width.toInt(), height.toInt()) }
internal val CValue<CGRect>.kRect get() = useContents { Rect(origin.x.toInt(), origin.y.toInt(), size.width.toInt(), size.height.toInt()) }


///// Easier EGL Boolean Use /////

internal val EGLBoolean.bool get() = this != EGL_FALSE.toUInt()
internal operator fun EGLBoolean.not() = this == EGL_FALSE.toUInt()


///// EGL Function Wrappers /////

internal fun eglChooseConfig(display: EGLDisplay?, config: Config) =
    eglChooseConfig(display, config.toAttribs())

internal fun eglChooseConfig(display: EGLDisplay?, attribs: IntArray): EGLConfig {
    memScoped {
        val config = alloc<EGLConfigVar>()
        val numConfigs = alloc<EGLintVar>()
        attribs.usePinned { a ->
            if (!eglChooseConfig(display, a.addressOf(0), config.ptr, 1, numConfigs.ptr) || numConfigs.value < 1) {
                throw RuntimeException("eglChooseConfig() failed: ${angle.eglGetError().toString(16)}")
            }
        }
        return config.value!!
    }
}

internal fun eglCreateContext(display: EGLDisplay?, config: EGLConfig, sharedContext: EGLContext? = EGL_NO_CONTEXT, attribs: IntArray): EGLContext =
    attribs.usePinned { a ->
        check(angle.eglCreateContext(display, config, sharedContext, a.addressOf(0)), EGL_NO_CONTEXT, "eglCreateContext")
    }

internal fun eglCreateWindowSurface(display: EGLDisplay?, config: EGLConfig, window: CALayer, attribs: IntArray): EGLSurface =
    attribs.usePinned { a ->
        check(eglCreateWindowSurface(display, config, interpretCPointer(window.objcPtr()), a.addressOf(0)), EGL_NO_SURFACE, "eglCreateWindowSurface")
    }

internal fun <T> check(value: T?, bad: T?, name: String): T {
    if (value == bad) { throw RuntimeException("$name() failed: ${angle.eglGetError().toString(16)}") }
    return value!!
}
