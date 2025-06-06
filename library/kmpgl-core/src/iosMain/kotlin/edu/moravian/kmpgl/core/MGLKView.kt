// Inspired by MetalANGLE's MGLKit: https://github.com/kakashidinho/metalangle/blob/master/ios/xcode/MGLKit/MGLKView.mm

@file:OptIn(ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.eglGetError
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewMeta

private val blankRect = CGRectMake(0.0, 0.0, 1.0, 1.0)

/** A view used for rendering ANGLE-OpenGL content. */
class MGLKView(
    frame: CValue<CGRect>,
    private val controller: MGLKViewController,
) : UIView(frame) {
    constructor(controller: MGLKViewController) : this(blankRect, controller)
    init { autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight }

    // specify the layer class to use for this view
    companion object : UIViewMeta() {
        @Suppress("RemoveRedundantQualifierName")
        @OptIn(BetaInteropApi::class)
        override fun layerClass() = MGLLayer.`class`()!!
    }
    internal val glLayer get() = layer as MGLLayer

    override fun drawRect(rect: CValue<CGRect>) { controller.onRender(rect) }
    fun display() {
        val layer = glLayer
        if (!controller.context.makeCurrent(layer)) {
            throw RuntimeException("Failed to set current context: ${eglGetError().toString(16)}")
        }
        drawRect(bounds)
        if (!layer.present()) { throw RuntimeException("Failed to present framebuffer: ${eglGetError().toString(16)}") }
    }

    var enableSetNeedsDisplay = true
    override fun setNeedsDisplay() { if (enableSetNeedsDisplay) { super.setNeedsDisplay() } }
    override fun setNeedsDisplayInRect(rect: CValue<CGRect>) { if (enableSetNeedsDisplay) { super.setNeedsDisplayInRect(rect) } }
}
