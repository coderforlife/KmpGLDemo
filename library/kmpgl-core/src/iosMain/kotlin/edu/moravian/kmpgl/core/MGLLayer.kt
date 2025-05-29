// Inspired by MetalANGLE's MGLKit: https://github.com/kakashidinho/metalangle/blob/master/ios/xcode/MGLKit/MGLLayer.mm

@file:OptIn(ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.EGLSurface
import angle.EGL_BUFFER_PRESERVED
import angle.EGL_DRAW
import angle.EGL_GL_COLORSPACE_KHR
import angle.EGL_NONE
import angle.EGL_NO_CONTEXT
import angle.EGL_NO_SURFACE
import angle.EGL_READ
import angle.EGL_SWAP_BEHAVIOR
import angle.eglDestroySurface
import angle.eglGetCurrentSurface
import angle.eglMakeCurrent
import angle.eglSurfaceAttrib
import angle.eglSwapBuffers
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSCoder
import platform.QuartzCore.CALayer
import platform.QuartzCore.CALayerMeta
import platform.QuartzCore.CAMetalLayer
import platform.QuartzCore.CATransaction
import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.max
import kotlin.native.ref.createCleaner


/** A view layer that can be used with for rendering ANGLE-OpenGL content. */
@OptIn(BetaInteropApi::class)
class MGLLayer : CALayer {
    @Suppress("unused")
    @OverrideInit // annotations needed for the UIView.layerClass handler to work correctly
    constructor() : super()

    @Suppress("unused")
    @OverrideInit
    constructor(coder: NSCoder): super(coder)

    @Suppress("unused")
    @OverrideInit
    constructor(layer: Any): super(layer) { if (layer is MGLLayer) { context = layer.context } }

    companion object : CALayerMeta() // needed for the UIView.layerClass handler

    internal var context: MGLContext? = null
        set(value) {
            if (field != value) {
                releaseSurface()
                surface.context = value
                surface.updateConfig()
                field = value
            }
        }
    private val _layer = CAMetalLayer().also {
        it.frame = this.bounds
        this.addSublayer(it)
    }

    override fun setContentsScale(contentsScale: CGFloat) {
        super.setContentsScale(contentsScale)
        _layer.contentsScale = contentsScale
    }

    // Size of the OpenGL framebuffer
    val drawableSize: Size get() {
        _layer.drawableSize.useContents { if (width == 0.0 && height == 0.0) { checkLayerSize() } }
        return _layer.drawableSize.kSize
    }

    // Present the contents of OpenGL backed framebuffer on screen as soon as possible.
    fun present() = context?.let {
        if (!eglSwapBuffers(it.display.egl, eglSurface)) { return false }
        checkLayerSize()
        return true
    } ?: false

    private var surface = EGLSurfaceHolder(null, _layer)
    @Suppress("unused")
    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(surface) { it.dispose() }

    internal val eglSurface: EGLSurface? get() {
        if (!surface.isValid) {
            checkLayerSize()
            surface.updateConfig()
        }
        return surface.surface
    }
    internal fun releaseSurface() { surface.dispose() }

    private fun checkLayerSize() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        _layer.frame = this.bounds
        _layer.bounds.useContents {
            _layer.drawableSize = CGSizeMake(
                max(size.width * _layer.contentsScale, 1.0),
                max(size.height * _layer.contentsScale, 1.0)
            )
        }
        CATransaction.commit()
    }
}

private class EGLSurfaceHolder(var context: MGLContext? = null, val layer: CALayer) {
    val isValid get() = surface != EGL_NO_SURFACE
    var surface: EGLSurface? = null
        set(value) { dispose(); field = value }
    fun updateConfig() {
        dispose()
        val context = context ?: return
        val config = context.config
        val attribs = intArrayOf(EGL_GL_COLORSPACE_KHR, config.colorFormat.colorSpace, EGL_NONE)
        surface = eglCreateWindowSurface(context.display.egl, context.eglConfig, layer, attribs)
        if (config.retainedBacking) {
            eglSurfaceAttrib(context.display.egl, surface, EGL_SWAP_BEHAVIOR, EGL_BUFFER_PRESERVED)
        }
    }
    fun dispose() {
        if (surface != EGL_NO_SURFACE) {
            if (surface == eglGetCurrentSurface(EGL_READ) || surface == eglGetCurrentSurface(EGL_DRAW)) {
                eglMakeCurrent(context?.display?.egl, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
            }
            eglDestroySurface(context?.display?.egl, surface)
            surface = EGL_NO_SURFACE
        }
    }
}
