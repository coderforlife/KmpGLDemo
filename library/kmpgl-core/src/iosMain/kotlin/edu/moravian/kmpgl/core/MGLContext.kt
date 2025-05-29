// Inspired by MetalANGLE's MGLKit: https://github.com/kakashidinho/metalangle/blob/master/ios/xcode/MGLKit/MGLContext.mm

@file:OptIn(ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.EGLContext
import angle.EGLDisplay
import angle.EGL_CONTEXT_MAJOR_VERSION
import angle.EGL_CONTEXT_MINOR_VERSION
import angle.EGL_DRAW
import angle.EGL_NONE
import angle.EGL_NO_CONTEXT
import angle.EGL_NO_SURFACE
import angle.EGL_READ
import angle.eglDestroyContext
import angle.eglGetCurrentContext
import angle.eglGetCurrentSurface
import angle.eglMakeCurrent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

/** An EGL context and surface */
class MGLContext(
    val apiMajorVersion: Int = 3,
    val apiMinorVersion: Int = 0,
    config: Config = Config(),
) {
    val display = MGLDisplay.default
    private val holder = EGLContextHolder(display.egl, apiMajorVersion, apiMinorVersion, config)
    @Suppress("unused")
    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(holder) { it.dispose() }
    val isValid get() = holder.isValid
    val egl get() = holder.context
    var config = config
        set(value) { holder.setConfig(apiMajorVersion, apiMinorVersion, config); field = value }
    val eglConfig get() = eglChooseConfig(display.egl, config)

    fun makeCurrent(layer: MGLLayer? = null): Boolean {
        layer?.context = this  // make sure the layer has the right context
        val surface = layer?.eglSurface ?: EGL_NO_SURFACE
        val display = display.egl
        return ((eglGetCurrentContext() == egl &&
                 eglGetCurrentSurface(EGL_READ) == surface && eglGetCurrentSurface(EGL_DRAW) == surface) ||
                 eglMakeCurrent(display, surface, surface, egl).bool)
    }
}

private class EGLContextHolder(private val display: EGLDisplay, major: Int, minor: Int, config: Config) {
    val isValid get() = _context != EGL_NO_CONTEXT
    var context: EGLContext
        get() {
            if (_context == EGL_NO_CONTEXT) { throw RuntimeException("EGLContext disposed") }
            return _context!!
        }
        set(value) {
            dispose()
            _context = value
        }
    private var _context: EGLContext? = init(major, minor, config)
    private fun init(major: Int, minor: Int, config: Config): EGLContext {
        val eglConfig = eglChooseConfig(display, config)

        val ctxAttribs = intArrayOf(
            EGL_CONTEXT_MAJOR_VERSION, major,
            EGL_CONTEXT_MINOR_VERSION, minor,
            EGL_NONE
        )
        return eglCreateContext(display, eglConfig, EGL_NO_CONTEXT, ctxAttribs)
    }
    fun setConfig(major: Int, minor: Int, config: Config) {
        dispose()
        _context = init(major, minor, config)
    }
    fun dispose() {
        if (_context != EGL_NO_CONTEXT) {
            if (eglGetCurrentContext() == context) {
                eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
            }
            eglDestroyContext(display, _context)
            _context = EGL_NO_CONTEXT
        }
    }
}
