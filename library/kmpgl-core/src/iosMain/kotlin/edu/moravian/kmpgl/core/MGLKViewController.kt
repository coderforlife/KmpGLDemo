// Inspired by MetalANGLE's MGLKit: https://github.com/kakashidinho/metalangle/blob/master/ios/xcode/MGLKit/MGLKViewController.mm

@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package edu.moravian.kmpgl.core

import angle.eglSwapInterval
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreFoundation.CFTimeInterval
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSOperationQueue
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CACurrentMediaTime
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIViewControllerTransitionCoordinatorProtocol
import platform.darwin.NSObjectProtocol

interface MGLViewListener {
    fun onLoad(controller: MGLKViewController) {}
    fun onUnload(controller: MGLKViewController) {}
    fun onPause(controller: MGLKViewController) {}
    fun onResume(controller: MGLKViewController) {}
    fun onResize(controller: MGLKViewController, width: Int, height: Int) {}
    fun onRender(controller: MGLKViewController, rect: Rect, timeSinceLastUpdate: Double) {}
}

/**
 * A view controller used for rendering ANGLE-OpenGL content. It requires a
 * listener that receives events for performing the rendering.
 */
class MGLKViewController(
    val context: MGLContext = MGLContext(3),
    val listener: MGLViewListener
) : UIViewController(null, null) {
    var glView = MGLKView(this)
    override fun loadView() { setView(glView) }
    override fun setView(view: UIView?) {
        if (view != glView) { throw IllegalArgumentException("MGLKViewController can only use the built-in MGLKView as its view") }
        super.setView(view)
    }

    // Setting to 0 or 1 will sync the frame rate with display's refresh rate
    var preferredFramesPerSecond = 30
        set(value) {
            if (value < 0) { throw IllegalArgumentException("preferredFramesPerSecond must be >= 0") }
            field = if (value == 1) 0 else value
            displayLink.preferredFramesPerSecond = field.toLong()
            if (!displayLink.paused) {
                displayLink.paused = true
                displayLink.paused = false
            }
        }
    var config get() = context.config
        set(value) {
            context.config = value
            glView.glLayer.releaseSurface()
        }
    fun makeCurrent() { context.makeCurrent(glView.glLayer) }

    ///// Basic Listener Events /////
    internal fun onRender(rect: CValue<CGRect>) { listener.onRender(this, rect.kRect, timeSinceLastUpdate) }
    private fun onResize() {
        val (width, height) = glView.glLayer.drawableSize
        listener.onResize(this, width, height)
    }
    override fun viewWillTransitionToSize(size: CValue<CGSize>, withTransitionCoordinator: UIViewControllerTransitionCoordinatorProtocol) {
        super.viewWillTransitionToSize(size, withTransitionCoordinator)
        onResize()
    }
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        onResize()
    }
    override fun viewDidLoad() {
        listener.onLoad(this)
    }
    override fun viewDidUnload() { listener.onUnload(this) }


    ///// Frame Drawing /////
    private var timeSinceLastUpdate: CFTimeInterval = 0.0
    private var lastUpdateTime: CFTimeInterval = 0.0
    @Suppress("unused")
    @ObjCAction
    private fun draw() {
        val now = CACurrentMediaTime()

        if (appWasInBackground) {
            // To avoid time jump when the app goes to background for a long period of time
            lastUpdateTime = now
            appWasInBackground = false
            eglSwapInterval(context.display.egl, 0)
        }

        timeSinceLastUpdate = now - lastUpdateTime
        glView.display()
        lastUpdateTime = now
    }
    var runLoop = NSRunLoop.mainRunLoop
        set(value) {
            if (value != field) {
                displayLink.removeFromRunLoop(field, NSDefaultRunLoopMode)
                displayLink.addToRunLoop(value, NSDefaultRunLoopMode)
                if (!displayLink.paused) {
                    displayLink.paused = true
                    displayLink.paused = false
                }
            }
            field = value
        }
    private val displayLink = CADisplayLink.displayLinkWithTarget(this, NSSelectorFromString("draw")).apply {
        preferredFramesPerSecond = this@MGLKViewController.preferredFramesPerSecond.toLong()
        paused = true
        addToRunLoop(runLoop, NSDefaultRunLoopMode)
    }


    ///// Pausing and Resuming /////
    var paused get() = displayLink.paused
        set(value) { if (value) { this.pause() } else { this.resume() } }
    fun pause() {
        if (paused) { return }
        displayLink.paused = true
        listener.onPause(this)
    }
    fun resume() {
        if (!paused) { return }
        displayLink.paused = false
        onResize()
        listener.onResume(this)
    }
    private var appWasInBackground: Boolean = true
    private val willResignActive = Observer("UIApplicationWillResignActiveNotification") {
        appWasInBackground = true
        pause()
    }
    private val didBecomeActive = Observer("UIApplicationDidBecomeActiveNotification") { resume() }
    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        resume()
        willResignActive.enable()
        didBecomeActive.enable()
    }
    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        appWasInBackground = true
        pause()
        willResignActive.disable()
        didBecomeActive.disable()
    }
}

private class Observer(val name: String, val action: (NSNotification?) -> Unit) {
    private var observer: NSObjectProtocol? = null
    fun enable() {
        observer = NSNotificationCenter.defaultCenter.addObserverForName(name, null, NSOperationQueue.mainQueue, action)
    }
    fun disable() {
        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it, name, null) }
    }
}
