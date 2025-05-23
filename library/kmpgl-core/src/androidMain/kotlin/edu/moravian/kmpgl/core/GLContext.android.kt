@file:Suppress("unused", "NOTHING_TO_INLINE", "PropertyName", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package edu.moravian.kmpgl.core

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.*
import android.opengl.GLUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import edu.moravian.kmpgl.util.Bufferable
import edu.moravian.kmpgl.util.Memory
import edu.moravian.kmpgl.util.asBuffer
import edu.moravian.kmpgl.util.of
import java.nio.*
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

actual typealias GLView = GLSurfaceView
actual class GLPlatformContext(
    val context: Context,
    val attrs: AttributeSet? = null
)

// OpenGL ES 2.0 first supported in API 8 (Android 2.2, <2010)
// OpenGL ES 3.0 first supported in API 18 (Android 4.3 Jelly Bean, 2013)
// OpenGL ES 3.1 first supported in API 21 (Android 5.0 Lollipop, 2014)

// EGL 1.4 first supported in API 17 (Android 4.2 Jelly Bean, 2012)
// EGL 1.5 first supported in API 29 (Android 10, 2019)

@OptIn(ExperimentalUnsignedTypes::class)
actual class GLContext: GLContextBase() {
    private inner class Renderer: GLSurfaceView.Renderer {
        // all of these run on the render thread automatically
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            this@GLContext.thread = Thread.currentThread()
            onCreate()
        }
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            onResize(width, height)
        }
        override fun onDrawFrame(gl: GL10) {
            // TODO: update this to use uptimeNanos() instead of elapsedRealtimeNanos()
            onRender(android.os.SystemClock.elapsedRealtimeNanos())
        }
    }

    inner class SurfaceView(context: Context, attrs: AttributeSet? = null): GLSurfaceView(context, attrs) {
        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            if (hasWindowFocus) { _view?.onResume() }
            else { _view?.onPause() }
        }
        override fun onVisibilityChanged(changedView: View, visibility: Int) {
            if (visibility == VISIBLE) { _view?.onResume() }
            else { _view?.onPause() }
        }
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            updateDrawSurfaceSize(w, h)
        }
        override fun onPause() {
            queueEvent(this@GLContext::onPause)
            super.onPause() // waits from the rendering to pause, may lose the GL resources at this point
        }
        override fun onResume() {
            super.onResume() // waits from the rendering to resume, reestablishes lost context
            queueEvent(this@GLContext::onResume)
        }
        override fun onDetachedFromWindow() {
            queueEvent(::onDispose) // doesn't do a full dispose so it can be re-used later
            super.onDetachedFromWindow() // waits from the rendering to detach, there isn't a rendering thread after this point
        }
    }

    private var _view: GLSurfaceView? = null
    actual val view: GLSurfaceView get() = checkNotNull(_view)
    private var thread: Thread? = null

    actual val isInitialized get() = _attributes != null
    private var _attributes: GLContextAttributes? = null
    actual val attributes get() = checkNotNull(_attributes)
    private var _version = 0
    actual val version; get() = _version

    @Throws(IllegalStateException::class)
    actual fun init(attributes: GLContextAttributes, context: GLPlatformContext): GLSurfaceView {
        if (isInitialized) { throw IllegalStateException("already initialized GL context") }
        this._attributes = attributes

        // Create the surface view
        _view = SurfaceView(context.context, context.attrs).apply {
            holder.setFormat(if (attributes.alpha) PixelFormat.RGBA_8888 else PixelFormat.RGB_888)
            setEGLContextFactory(ContextFactory())
            setEGLConfigChooser(ConfigChooser())
            setRenderer(Renderer())
        }

        return view
    }
    actual fun initIfNeeded(attributes: GLContextAttributes, context: GLPlatformContext): GLSurfaceView {
        return _view ?: init(attributes, context)
    }

    actual fun dispose() {
        _view?.queueEvent(::onDispose)
        _view = null
        clearListeners()
        thread = null
        _attributes = null
    }

    private fun <T> eglCheck(value: T): T {
        if (EGL14.eglGetError() != EGL10.EGL_SUCCESS) {
            throw RuntimeException(GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
        return value
    }
    private inner class ConfigChooser : EGLConfigChooser {
        // Only available in EGL15 (Android 10 / API 29)
        val EGL_OPENGL_ES3_BIT = 0x00000040
        private val configs = arrayOfNulls<EGLConfig>(1)
        private val output = IntArray(1)
        private fun chooseConfig(egl: EGL10, display: EGLDisplay, attributes: GLContextAttributes): EGLConfig? {
            val attribList = intArrayOf(
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_RENDERABLE_TYPE, if (attributes.version == GLVersion.GLES_2_0) EGL14.EGL_OPENGL_ES2_BIT else EGL_OPENGL_ES3_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT or if (attributes.preserveDrawingBuffer) EGL14.EGL_SWAP_BEHAVIOR_PRESERVED_BIT else 0,
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, if (attributes.alpha) 8 else 0,
                EGL10.EGL_DEPTH_SIZE, if (attributes.depth) 16 else 0,
                EGL10.EGL_STENCIL_SIZE, if (attributes.stencil) 8 else 0,
                EGL10.EGL_SAMPLE_BUFFERS, if (attributes.antialias) 1 else 0,
                EGL10.EGL_SAMPLES, if (attributes.antialias) 4 else 0,  // This is for 4x MSAA.
                EGL10.EGL_NONE
            )
            output[0] = 0
            configs[0] = null
            eglCheck(egl.eglChooseConfig(display, attribList, configs, 1, output))
            val config = configs[0]
            return if (output[0] == 0) { null }
                else if (!attributes.failIfMajorPerformanceCaveat) { config }
                else {
                    eglCheck(egl.eglGetConfigAttrib(display, config, EGL10.EGL_CONFIG_CAVEAT, output))
                    if (output[0] == EGL10.EGL_NONE) { config } else { null }
                }
        }
        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {
            // antialias, stencil, and depth are simply requests, not requirements
            // try without them if they don't work
            // alpha, on the other hand, is a requirement
            _version = if (attributes.version == GLVersion.GLES_2_0) 20 else 30
            var config = chooseConfig(egl, display, attributes)
            if (config !== null) { return config }
            if (attributes.antialias) {
                _attributes = attributes.copy(antialias = false)
                config = chooseConfig(egl, display, attributes)
                if (config !== null) { return config }
            }
            if (attributes.stencil) {
                _attributes = attributes.copy(stencil = false)
                config = chooseConfig(egl, display, attributes)
                if (config !== null) { return config }
            }
            if (attributes.depth) {
                _attributes = attributes.copy(depth = false)
                config = chooseConfig(egl, display, attributes)
                if (config !== null) { return config }
            }
            // TODO: try without combinations of antialias, stencil, and depth?
            return null
        }
    }
    private inner class ContextFactory : EGLContextFactory {
        // Only available in EGL15 (Android 10 / API 29)
        val EGL_CONTEXT_MAJOR_VERSION = 0x3098 // same as EGL14.EGL_CONTEXT_CLIENT_VERSION
        val EGL_CONTEXT_MINOR_VERSION = 0x30FB
        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
            val major = version / 10
            val minor = version % 10
            val attribList =
                if (/*eglVersion(egl, display) < 1.5 ||*/ minor == 0) {
                    intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, major, EGL10.EGL_NONE)
                } else {
                    intArrayOf(EGL_CONTEXT_MAJOR_VERSION, major, EGL_CONTEXT_MINOR_VERSION, minor, EGL10.EGL_NONE)
                }
            return eglCheck(egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attribList))
        }
        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) { eglCheck(egl.eglDestroyContext(display, context)) }
    }
    private inner class WindowSurfaceFactory : EGLWindowSurfaceFactory {
        override fun createWindowSurface(egl: EGL10, display: EGLDisplay, config: EGLConfig, nativeWindow: Any?): EGLSurface? {
            try {
                val surface = eglCheck(egl.eglCreateWindowSurface(display, config, nativeWindow, null))
                if (attributes.preserveDrawingBuffer) {
                    // TODO: nearly all of this code is for "casting" the surface between EGL10 and EGL14
                    // but it may not work since the current context is nothing at this point
                    // See https://github.com/google/mediapipe/blob/7e36a5e2ae8c66ef9717d399fa4004f448dde13f/mediapipe/java/com/google/mediapipe/glutil/EglManager.java#L251 for a more robust version
                    // In any case, I would like to avoid it. I can create an EGL14 version with:
                    //    android.opengl.EGLSurface(nativeHandle)
                    // but how to get the native handle from the EGL10 surface variable?
                    val curDisplay = eglCheck(egl.eglGetCurrentDisplay())
                    val curReadSurface = eglCheck(egl.eglGetCurrentSurface(EGL10.EGL_READ))
                    val curDrawSurface = eglCheck(egl.eglGetCurrentSurface(EGL10.EGL_DRAW))
                    val curContext = eglCheck(egl.eglGetCurrentContext())
                    eglCheck(egl.eglMakeCurrent(display, surface, surface, curContext))
                    eglCheck(EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL10.EGL_READ), EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED))
                    eglCheck(egl.eglMakeCurrent(curDisplay, curReadSurface, curDrawSurface, curContext))
                }
                return surface
            } catch (e: java.lang.IllegalArgumentException) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e("GLContext", "eglCreateWindowSurface", e)
            }
            return null
        }

        override fun destroySurface(egl: EGL10, display: EGLDisplay?, surface: EGLSurface?) {
            eglCheck(egl.eglDestroySurface(display, surface))
        }
    }

    actual val width: Int get() = lastWidth
    actual val height: Int get() = lastHeight
    actual val viewWidth: Int get() = view.width
    actual val viewHeight: Int get() = view.height

    actual var viewScale: Float = 1f
        set(value) {
            field = value
            if (value == 1f) { _view?.holder?.setSizeFromLayout() }
            else { updateDrawSurfaceSize(viewWidth, viewHeight) }
        }
    private fun updateDrawSurfaceSize(w: Int, h: Int) {
        if (viewScale != 1f) { // at 1 it is automatic, otherwise we need to do it ourselves
            val newW = (w * viewScale).roundToInt()
            val newH = (h * viewScale).roundToInt()
            if (newW != lastWidth || newH != lastHeight) {
                _view?.holder?.setFixedSize(newW, newH)
            }
        }
    }


    /////////////// Rendering ///////////////
    actual val isRunning: Boolean get() = rendering
    actual fun start() { if (!rendering) _view?.onResume() }
    actual fun stop() { if (rendering) _view?.onPause() }

    actual val renderingContinuously get() = _view?.renderMode == RENDERMODE_CONTINUOUSLY
    actual fun renderContinuously() { _view?.renderMode = RENDERMODE_CONTINUOUSLY }
    actual fun renderOnDemand() { _view?.renderMode = RENDERMODE_WHEN_DIRTY }
    actual fun renderFrame() { if (rendering) _view?.requestRender() }


    /////////////// Threading ///////////////
    actual fun isOnRenderThread() = this.thread == Thread.currentThread()
    actual fun runAsync(action: () -> Unit) { _view?.queueEvent(action) }
    actual fun <T: Any> runSync(action: () -> T): T {
        if (isOnRenderThread()) { return action() }
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        val comm = object: Runnable {
            lateinit var result: T
            override fun run() {
                result = action()
                lock.withLock { condition.signalAll() }
            }
        }
        lock.withLock { condition.await() }
        return comm.result
    }


    /////////////// Extensions ///////////////
    actual val extensions: GLExtensions by lazy { GLExtensions(this) }


    /////////////// Buffers ///////////////
    internal val boolTmp = BooleanArray(1)
    internal val intTmp = IntArray(2) // glGetShaderPrecisionFormat requires a pair of ints
    internal val intTmp2 = IntArray(1)
    internal val floatTmp = FloatArray(1)
    internal val longTmp = LongArray(1)
    internal companion object {
        inline fun Buffer.nbytes(): Int =
            limit()*(when (this) {
                is FloatBuffer -> 4
                is ByteBuffer -> 1
                is IntBuffer -> 4
                is ShortBuffer -> 2
                is DoubleBuffer -> 8
                is LongBuffer -> 8
                else -> throw IllegalArgumentException("Unknown buffer type ${this::class}")
            })
    }


    /////////////// Error Checking ///////////////
    internal inline fun <T> checkError(value: T): T {
        return when (val err = GLES20.glGetError()) {
            GLES20.GL_NO_ERROR -> { value }
            GLES20.GL_INVALID_ENUM -> throw IllegalArgumentException("GL: invalid enum value")
            GLES20.GL_INVALID_VALUE -> throw IllegalArgumentException("GL: invalid GL value")
            GLES20.GL_INVALID_OPERATION -> throw IllegalStateException("GL: invalid operation")
            GLES20.GL_OUT_OF_MEMORY -> throw IllegalStateException("GL: out of memory")
            else -> { throw RuntimeException("GL: error #${err.toString(16)}") }
        }
    }
    internal inline fun checkGLBase() {
        check(isInitialized) { "GL context not initialized" }
        check(isOnRenderThread()) { "GL functions must be called from render thread" }
    }
    internal inline fun checkGLBase(minVersion: Int) {
        if (_version < minVersion) { throw UnsupportedOperationException("GL version ${formatVersion(minVersion)} required (using ${formatVersion(version)})") }
        checkGLBase()
    }
    internal inline fun <T> checkGL(function: () -> T): T {
        checkGLBase()
        return checkError(function())
    }
    internal inline fun <T> checkGL(minVersion: Int, function: () -> T): T {
        checkGLBase(minVersion)
        return checkError(function())
    }
    private fun formatVersion(version: Int) = "${version / 10}.${version % 10}"

    actual fun getError(): Int { checkGLBase(); return GLES20.glGetError() }

    /////////////// Capabilities ///////////////
    actual fun isEnabled(cap: Int) = checkGL { GLES20.glIsEnabled(cap) }
    actual fun enable(cap: Int) = checkGL { GLES20.glEnable(cap) }
    actual fun disable(cap: Int) = checkGL { GLES20.glDisable(cap) }

    /////////////// Parameters ///////////////
    actual fun getBool(name: Int) = checkGL { GLES20.glGetBooleanv(name, boolTmp, 0); boolTmp[0] }
    actual fun getBoolArray(name: Int, output: BooleanArray): BooleanArray = checkGL { output.also { GLES20.glGetBooleanv(name, it, 0) } }
    actual fun getInt(name: Int) = checkGL { GLES20.glGetIntegerv(name, intTmp, 0); intTmp[0] }
    actual fun getIntArray(name: Int, output: IntArray) = checkGL { output.also { GLES20.glGetIntegerv(name, it, 0) } }
    actual fun getFloat(name: Int) = checkGL { GLES20.glGetFloatv(name, floatTmp, 0); floatTmp[0] }
    actual fun getFloatArray(name: Int, output: FloatArray) = checkGL { output.also { GLES20.glGetFloatv(name, it, 0) } }
    actual fun getString(name: Int) = checkGL { GLES20.glGetString(name)!! }
    @GLES3 actual fun getLong(name: Int) = checkGL(30) { GLES30.glGetInteger64v(name, longTmp, 0); longTmp[0] }
    @GLES3 actual fun getLong(target: Int, index: Int) = checkGL(30) { GLES30.glGetInteger64i_v(target, index, longTmp, 0); longTmp[0] }
    @GLES3 actual fun getLongArray(name: Int, output: LongArray) = checkGL(30) { output.also { GLES30.glGetInteger64v(name, it, 0) } }
    @GLES3 actual fun getString(name: Int, index: Int) = checkGL(30) { GLES30.glGetStringi(name, index)!! }
    @GLES3 actual fun getInt(target: Int, index: Int) = checkGL(30) { GLES30.glGetIntegeri_v(target, index, intTmp, 0); intTmp[0] }

    /////////////// Settings ///////////////
    actual fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) = checkGL { GLES20.glBlendColor(red, green, blue, alpha) }
    actual fun blendEquation(mode: Int) = checkGL { GLES20.glBlendEquation(mode) }
    actual fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) = checkGL { GLES20.glBlendEquationSeparate(modeRGB, modeAlpha) }
    actual fun blendFunc(sFactor: Int, dFactor: Int) = checkGL { GLES20.glBlendFunc(sFactor, dFactor) }
    actual fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) = checkGL { GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha) }
    actual fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = checkGL { GLES20.glClearColor(red, green, blue, alpha) }
    actual fun clearDepth(depth: Float) = checkGL { GLES20.glClearDepthf(depth) }
    actual fun clearStencil(s: Int) = checkGL { GLES20.glClearStencil(s) }
    actual fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) = checkGL { GLES20.glColorMask(red, green, blue, alpha) }
    actual fun cullFace(mode: Int) = checkGL { GLES20.glCullFace(mode) }
    actual fun depthFunc(func: Int) = checkGL { GLES20.glDepthFunc(func) }
    actual fun depthMask(flag: Boolean) = checkGL { GLES20.glDepthMask(flag) }
    actual fun depthRange(zNear: Float, zFar: Float) = checkGL { GLES20.glDepthRangef(zNear, zFar) }
    actual fun hint(target: Int, mode: Int) = checkGL { GLES20.glHint(target, mode) }
    actual fun frontFace(mode: Int) = checkGL { GLES20.glFrontFace(mode) }
    actual fun lineWidth(width: Float) = checkGL { GLES20.glLineWidth(width) }
    actual fun pixelStore(pname: Int, param: Int) = checkGL { GLES20.glPixelStorei(pname, param) }
    actual fun polygonOffset(factor: Float, units: Float) = checkGL { GLES20.glPolygonOffset(factor, units) }
    actual fun sampleCoverage(value: Float, invert: Boolean) = checkGL { GLES20.glSampleCoverage(value, invert) }
    actual fun scissor(x: Int, y: Int, width: Int, height: Int) = checkGL { GLES20.glScissor(x, y, width, height) }
    actual fun stencilFunc(func: Int, ref: Int, mask: UInt) = checkGL { GLES20.glStencilFunc(func, ref, mask.toInt()) }
    actual fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: UInt) = checkGL { GLES20.glStencilFuncSeparate(face, func, ref, mask.toInt()) }
    actual fun stencilMask(mask: UInt) = checkGL { GLES20.glStencilMask(mask.toInt()) }
    actual fun stencilMaskSeparate(face: Int, mask: UInt) = checkGL { GLES20.glStencilMaskSeparate(face, mask.toInt()) }
    actual fun stencilOp(fail: Int, zFail: Int, zPass: Int) = checkGL { GLES20.glStencilOp(fail, zFail, zPass) }
    actual fun stencilOpSeparate(face: Int, fail: Int, zFail: Int, zPass: Int) = checkGL { GLES20.glStencilOpSeparate(face, fail, zFail, zPass) }
    actual fun viewport(x: Int, y: Int, width: Int, height: Int) = checkGL { GLES20.glViewport(x, y, width, height) }

    /////////////// Basic Functions ///////////////
    actual fun clear(mask: UInt) = checkGL { GLES20.glClear(mask.toInt()) }
    actual fun drawArrays(mode: Int, first: Int, count: Int) = checkGL { GLES20.glDrawArrays(mode, first, count) }
    actual fun drawElements(mode: Int, count: Int, type: Int, offset: Int) = checkGL { GLES20.glDrawElements(mode, count, type, offset) }
    @GLES3 actual fun drawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) = checkGL(30) { GLES30.glDrawRangeElements(mode, start, end, count, type, offset) }
    actual fun finish() = checkGL { GLES20.glFinish() }
    actual fun flush() = checkGL { GLES20.glFlush() }

    /////////////// Program ///////////////
    actual fun isProgram(program: Int) = checkGL { GLES20.glIsProgram(program) }
    actual fun createProgram() = checkGL { GLProgram(GLES20.glCreateProgram()) }
    actual fun linkProgram(program: GLProgram) = checkGL { GLES20.glLinkProgram(program.id) }
    actual fun validateProgram(program: GLProgram) = checkGL { GLES20.glValidateProgram(program.id) }
    actual fun useProgram(program: GLProgram) = checkGL { GLES20.glUseProgram(program.id) }
    actual fun deleteProgram(program: GLProgram) = checkGL { GLES20.glDeleteProgram(program.id) }
    actual fun getProgramInt(program: GLProgram, pname: Int) = checkGL { GLES20.glGetProgramiv(program.id, pname, intTmp, 0); intTmp[0] }
    actual fun getProgramInfoLog(program: GLProgram): String = checkGL { GLES20.glGetProgramInfoLog(program.id) }
    @GLES3 actual fun programParameter(program: GLProgram, pname: Int, value: Int) = checkGL(30) { GLES30.glProgramParameteri(program.id, pname, value) }
    @GLES3 actual fun getFragDataLocation(program: GLProgram, name: String) = checkGL(30) { GLES30.glGetFragDataLocation(program.id, name) }
    @GLES3 actual fun getProgramBinary(program: GLProgram): ProgramBinary {
        checkGLBase(30)
        checkError(GLES30.glGetProgramiv(program.id, GLES30.GL_PROGRAM_BINARY_LENGTH, intTmp, 0))
        val size = intTmp[0]
        val binary = ByteArray(size)
        checkError(GLES30.glGetProgramBinary(program.id, size, intTmp, 0, intTmp, 1, ByteBuffer.wrap(binary)))
        return ProgramBinary(if (intTmp[0] == size) { binary } else { binary.copyOf(intTmp[0]) }, intTmp[1])
    }
    @GLES3 actual fun programBinary(program: GLProgram, binaryFormat: Int, binary: ByteArray) = checkGL(30) { GLES30.glProgramBinary(program.id, binaryFormat, ByteBuffer.wrap(binary), binary.size) }

    /////////////// Program + Attrib ///////////////
    actual fun getActiveAttrib(program: GLProgram, index: Int) = checkGL {
        val name = GLES20.glGetActiveAttrib(program.id, index, intTmp, 0, intTmp2, 0)
        NameTypeSize(name, intTmp2[0], intTmp[0])
    }
    actual fun getAttribLocation(program: GLProgram, name: String) = checkGL { GLAttributeLocation(GLES20.glGetAttribLocation(program.id, name)) }
    actual fun bindAttribLocation(program: GLProgram, index: GLAttributeLocation, name: String) = checkGL { GLES20.glBindAttribLocation(program.id, index.id, name) }

    /////////////// Program + Shader ///////////////
    actual fun attachShader(program: GLProgram, shader: GLShader) = checkGL { GLES20.glAttachShader(program.id, shader.id) }
    actual fun detachShader(program: GLProgram, shader: GLShader) = checkGL { GLES20.glDetachShader(program.id, shader.id) }
    actual fun getAttachedShaders(program: GLProgram): GLShaderArray {
        checkGLBase()
        checkError(GLES20.glGetProgramiv(program.id, GLES20.GL_ATTACHED_SHADERS, intTmp, 0))
        val count = intTmp[0]
        val shaders = IntArray(count)
        checkError(GLES20.glGetAttachedShaders(program.id, count, intTmp, 0, shaders, 0))
        return GLShaderArray(if (intTmp[0] == count) { shaders } else { shaders.copyOf(intTmp[0]) })
    }

    /////////////// Shader ///////////////
    actual fun isShader(shader: Int) = checkGL { GLES20.glIsShader(shader) }
    actual fun createShader(type: Int) = checkGL { GLShader(GLES20.glCreateShader(type)) }
    actual fun shaderSource(shader: GLShader, string: String) = checkGL { GLES20.glShaderSource(shader.id, string) }
    actual fun compileShader(shader: GLShader) = checkGL { GLES20.glCompileShader(shader.id) }
    actual fun deleteShader(shader: GLShader) = checkGL { GLES20.glDeleteShader(shader.id) }
    actual fun getShaderSource(shader: GLShader): String = checkGL { GLES20.glGetShaderSource(shader.id) }
    actual fun getShaderInt(shader: GLShader, pname: Int) = checkGL { GLES20.glGetShaderiv(shader.id, pname, intTmp, 0); intTmp[0] }
    actual fun getShaderInfoLog(shader: GLShader): String = checkGL { GLES20.glGetShaderInfoLog(shader.id) }
    actual fun getShaderPrecisionFormat(shaderType: Int, precisionType: Int) = checkGL {
        GLES20.glGetShaderPrecisionFormat(shaderType, precisionType, intTmp, 0, intTmp2, 0)
        ShaderPrecisionFormat(intTmp[0], intTmp[1], intTmp2[0])
    }
    actual fun releaseShaderCompiler() = checkGL { GLES20.glReleaseShaderCompiler() }

    /////////////// Program + Uniform ///////////////
    actual fun getActiveUniform(program: GLProgram, index: Int) = checkGL {
        val name = GLES20.glGetActiveUniform(program.id, index, intTmp, 0, intTmp2, 0)
        NameTypeSize(name, intTmp2[0], intTmp[0])
    }
    actual fun getUniformLocation(program: GLProgram, name: String) = checkGL { GLUniformLocation(GLES20.glGetUniformLocation(program.id, name)) }
    // TODO: does offset work?
    actual fun getUniformFloat(program: GLProgram, location: GLUniformLocation, params: FloatArray, offset: Int) = checkGL { GLES20.glGetUniformfv(program.id, location.id, params, offset) }
    actual fun getUniformInt(program: GLProgram, location: GLUniformLocation, params: IntArray, offset: Int) = checkGL { GLES20.glGetUniformiv(program.id, location.id, params, offset) }
    @GLES3 actual fun getUniformIndices(program: GLProgram, uniformNames: Array<String>, output: IntArray) = checkGL(30) {
        require(uniformNames.size <= output.size) { "uniformNames and output must have the same size" }
        output.also { GLES30.glGetUniformIndices(program.id, uniformNames, it, 0) }
    }
    @GLES3 actual fun getActiveUniforms(program: GLProgram, uniformIndices: IntArray, pname: Int, output: IntArray) = checkGL(30) {
        require(uniformIndices.size <= output.size) { "uniformIndices and output must have the same size" }
        output.also { GLES30.glGetActiveUniformsiv(program.id, uniformIndices.size, uniformIndices, 0, pname, it, 0) }
    }
    @GLES3 actual fun getUniformUInt(program: GLProgram, location: GLUniformLocation, params: UIntArray, offset: Int) = checkGL(30) { GLES30.glGetUniformuiv(program.id, location.id, params.asIntArray(), offset) }

    /////////////// Uniform ///////////////
    actual fun uniform1f(location: GLUniformLocation, x: Float) = checkGL { GLES20.glUniform1f(location.id, x) }
    actual fun uniform2f(location: GLUniformLocation, x: Float, y: Float) = checkGL { GLES20.glUniform2f(location.id, x, y) }
    actual fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float) = checkGL { GLES20.glUniform3f(location.id, x, y, z) }
    actual fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) = checkGL { GLES20.glUniform4f(location.id, x, y, z, w) }
    actual fun uniform1i(location: GLUniformLocation, x: Int) = checkGL { GLES20.glUniform1i(location.id, x) }
    actual fun uniform2i(location: GLUniformLocation, x: Int, y: Int) = checkGL { GLES20.glUniform2i(location.id, x, y) }
    actual fun uniform3i(location: GLUniformLocation, x: Int, y: Int, z: Int) = checkGL { GLES20.glUniform3i(location.id, x, y, z) }
    actual fun uniform4i(location: GLUniformLocation, x: Int, y: Int, z: Int, w: Int) = checkGL { GLES20.glUniform4i(location.id, x, y, z, w) }
    @GLES3 actual fun uniform1ui(location: GLUniformLocation, x: UInt) = checkGL(30) { GLES30.glUniform1ui(location.id, x.toInt()) }
    @GLES3 actual fun uniform2ui(location: GLUniformLocation, x: UInt, y: UInt) = checkGL(30) { GLES30.glUniform2ui(location.id, x.toInt(), y.toInt()) }
    @GLES3 actual fun uniform3ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt) = checkGL(30) { GLES30.glUniform3ui(location.id, x.toInt(), y.toInt(), z.toInt()) }
    @GLES3 actual fun uniform4ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt, w: UInt) = checkGL(30) { GLES30.glUniform4ui(location.id, x.toInt(), y.toInt(), z.toInt(), w.toInt()) }
    // TODO: does offset work?
    actual fun uniform1fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { GLES20.glUniform1fv(location.id, count, v, offset) }
    actual fun uniform2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { GLES20.glUniform2fv(location.id, count, v, offset) }
    actual fun uniform3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { GLES20.glUniform3fv(location.id, count, v, offset) }
    actual fun uniform4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { GLES20.glUniform4fv(location.id, count, v, offset) }
    actual fun uniform1iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { GLES20.glUniform1iv(location.id, count, v, offset) }
    actual fun uniform2iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { GLES20.glUniform2iv(location.id, count, v, offset) }
    actual fun uniform3iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { GLES20.glUniform3iv(location.id, count, v, offset) }
    actual fun uniform4iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { GLES20.glUniform4iv(location.id, count, v, offset) }
    @GLES3 actual fun uniform1uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { GLES30.glUniform1uiv(location.id, count, v.asIntArray(), offset) }
    @GLES3 actual fun uniform2uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { GLES30.glUniform2uiv(location.id, count, v.asIntArray(), offset) }
    @GLES3 actual fun uniform3uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { GLES30.glUniform3uiv(location.id, count, v.asIntArray(), offset) }
    @GLES3 actual fun uniform4uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { GLES30.glUniform4uiv(location.id, count, v.asIntArray(), offset) }
    actual fun uniformMatrix2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { GLES20.glUniformMatrix2fv(location.id, count, transpose, v, offset) }
    actual fun uniformMatrix3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { GLES20.glUniformMatrix3fv(location.id, count, transpose, v, offset) }
    actual fun uniformMatrix4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { GLES20.glUniformMatrix4fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix2x3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix2x3fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix3x2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix3x2fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix2x4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix2x4fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix4x2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix4x2fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix3x4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix3x4fv(location.id, count, transpose, v, offset) }
    @GLES3 actual fun uniformMatrix4x3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { GLES30.glUniformMatrix4x3fv(location.id, count, transpose, v, offset) }

    /////////////// Uniform Blocks ///////////////
    @GLES3 actual fun getUniformBlockIndex(program: GLProgram, uniformBlockName: String) = checkGL(30) { GLES30.glGetUniformBlockIndex(program.id, uniformBlockName) }
    @GLES3 actual fun getActiveUniformBlockInt(program: GLProgram, uniformBlockIndex: Int, pname: Int) = checkGL(30) { GLES30.glGetActiveUniformBlockiv(program.id, uniformBlockIndex, pname, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getActiveUniformBlockIntArray(program: GLProgram, uniformBlockIndex: Int, pname: Int, output: IntArray) = checkGL(30) { output.also { GLES30.glGetActiveUniformBlockiv(program.id, uniformBlockIndex, pname, it, 0) } }
    @GLES3 actual fun getActiveUniformBlockName(program: GLProgram, uniformBlockIndex: Int) = checkGL(30) { GLES30.glGetActiveUniformBlockName(program.id, uniformBlockIndex)!! }
    @GLES3 actual fun uniformBlockBinding(program: GLProgram, uniformBlockIndex: Int, uniformBlockBinding: Int) = checkGL(30) { GLES30.glUniformBlockBinding(program.id, uniformBlockIndex, uniformBlockBinding) }

    /////////////// Vertex Attributes ///////////////
    actual fun enableVertexAttribArray(index: GLAttributeLocation) = checkGL { GLES20.glEnableVertexAttribArray(index.id) }
    actual fun disableVertexAttribArray(index: GLAttributeLocation) = checkGL { GLES20.glDisableVertexAttribArray(index.id) }
    actual fun getVertexAttribFloatArray(index: GLAttributeLocation, pname: Int, output: FloatArray) = checkGL { output.also { GLES20.glGetVertexAttribfv(index.id, pname, it, 0) } }
    actual fun getVertexAttribInt(index: GLAttributeLocation, pname: Int) = checkGL { GLES20.glGetVertexAttribiv(index.id, pname, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getVertexAttribIntArray(index: GLAttributeLocation, pname: Int, output: IntArray) = checkGL(30) { output.also { GLES30.glGetVertexAttribIiv(index.id, pname, it, 0) } }
    @GLES3 actual fun getVertexAttribUIntArray(index: GLAttributeLocation, pname: Int, output: UIntArray) = checkGL(30) { output.also { GLES30.glGetVertexAttribIuiv(index.id, pname, it.asIntArray(), 0) } }

    actual fun vertexAttribPointer(index: GLAttributeLocation, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = checkGL { GLES20.glVertexAttribPointer(index.id, size, type, normalized, stride, offset) }
    @GLES3 actual fun vertexAttribIPointer(index: GLAttributeLocation, size: Int, type: Int, stride: Int, offset: Int) = checkGL(30) { GLES30.glVertexAttribIPointer(index.id, size, type, stride, offset) }

    actual fun vertexAttrib(index: GLAttributeLocation, x: Float, y: Float, z: Float, w: Float) = checkGL { GLES20.glVertexAttrib4f(index.id, x, y, z, w) }
    @GLES3 actual fun vertexAttrib(index: GLAttributeLocation, x: Int, y: Int, z: Int, w: Int) = checkGL(30) { GLES30.glVertexAttribI4i(index.id, x, y, z, w) }
    @GLES3 actual fun vertexAttrib(index: GLAttributeLocation, x: UInt, y: UInt, z: UInt, w: UInt) = checkGL(30) { GLES30.glVertexAttribI4ui(index.id, x.toInt(), y.toInt(), z.toInt(), w.toInt()) }
    // TODO: does offset work?
    actual fun vertexAttrib1fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { GLES20.glVertexAttrib1fv(index.id, values, offset) }
    actual fun vertexAttrib2fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { GLES20.glVertexAttrib2fv(index.id, values, offset) }
    actual fun vertexAttrib3fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { GLES20.glVertexAttrib3fv(index.id, values, offset) }
    actual fun vertexAttrib4fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { GLES20.glVertexAttrib4fv(index.id, values, offset) }
    @GLES3 actual fun vertexAttrib4iv(index: GLAttributeLocation, v: IntArray, offset: Int) = checkGL(30) { GLES30.glVertexAttribI4iv(index.id, v, offset) }
    @GLES3 actual fun vertexAttrib4uiv(index: GLAttributeLocation, v: UIntArray, offset: Int) = checkGL(30) { GLES30.glVertexAttribI4uiv(index.id, v.asIntArray(), offset) }

    /////////////// Vertex Array Objects ///////////////
    @GLES3OrExtension actual fun isVertexArrayGLES3(array: Int) = checkGL(30) { GLES30.glIsVertexArray(array) }
    @GLES3OrExtension actual fun genVertexArrayGLES3() = checkGL(30) { GLES30.glGenVertexArrays(1, intTmp, 0); GLVertexArrayObject(intTmp[0]) }
    @GLES3OrExtension actual fun genVertexArraysGLES3(n: Int) = checkGL(30) { GLVertexArrayObjectArray(IntArray(n).also { GLES30.glGenVertexArrays(n, it, 0) }) }
    @GLES3OrExtension actual fun bindVertexArrayGLES3(array: GLVertexArrayObject) = checkGL(30) { GLES30.glBindVertexArray(array.id) }
    @GLES3OrExtension actual fun deleteVertexArrayGLES3(array: GLVertexArrayObject) = checkGL(30) { intTmp[0] = array.id; GLES30.glDeleteVertexArrays(1, intTmp, 0) }
    @GLES3OrExtension actual fun deleteVertexArraysGLES3(arrays: GLVertexArrayObjectArray) = checkGL(30) { GLES30.glDeleteVertexArrays(arrays.size, arrays.ids, 0) }

    /////////////// Buffers ///////////////
    actual fun isBuffer(buffer: Int) = checkGL { GLES20.glIsBuffer(buffer) }
    actual fun genBuffer() = checkGL { GLES20.glGenBuffers(1, intTmp, 0); GLBuffer(intTmp[0]) }
    actual fun genBuffers(n: Int) = checkGL { GLBufferArray(IntArray(n).also { GLES20.glGenBuffers(n, it, 0) }) }
    actual fun bindBuffer(target: Int, buffer: GLBuffer) = checkGL { GLES20.glBindBuffer(target, buffer.id) }
    actual fun deleteBuffer(buffer: GLBuffer) = checkGL { intTmp[0] = buffer.id; GLES20.glDeleteBuffers(1, intTmp, 0) }
    actual fun deleteBuffers(buffers: GLBufferArray) = checkGL { GLES20.glDeleteBuffers(buffers.size, buffers.ids, 0) }
    actual fun getBufferInt(target: Int, pname: Int) = checkGL { GLES20.glGetBufferParameteriv(target, pname, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getBufferLong(target: Int, pname: Int) = checkGL(30) { GLES30.glGetBufferParameteri64v(target, pname, longTmp, 0); longTmp[0] }

    actual fun bufferData(target: Int, usage: Int, size: Int) = checkGL { GLES20.glBufferData(target, size, null, usage) }
    actual fun bufferData(target: Int, usage: Int, data: Bufferable) = checkGL {
        GLES20.glBufferData(target, data.nbytes, data.asBuffer(), usage)
    }
    fun bufferData(target: Int, usage: Int, data: Buffer) = checkGL {
        GLES20.glBufferData(target, data.nbytes(), data, usage)
    }
    actual fun bufferSubData(target: Int, offset: Int, data: Bufferable) = checkGL {
        GLES20.glBufferSubData(target, offset, data.nbytes, data.asBuffer())
    }
    fun bufferSubData(target: Int, offset: Int, data: Buffer) = checkGL {
        GLES20.glBufferSubData(target, offset, data.nbytes(), data)
    }
    actual fun bufferSubData(target: Int, offset: Int, data: Bufferable, srcOffset: Int, count: Int) = checkGL {
        GLES20.glBufferSubData(target, offset, count*data.elementSize, data.asBuffer(srcOffset, count))
    }

    @GLES3 actual fun copyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) = checkGL(30) { GLES30.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size) }

    @GLES3OrExtension actual fun drawBuffersGLES3(bufs: IntArray) = checkGL(30) { GLES30.glDrawBuffers(bufs.size, bufs, 0) }
    @GLES3 actual fun readBuffer(mode: Int) = checkGL(30) { GLES30.glReadBuffer(mode) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: IntArray) = checkGL(30) { GLES30.glClearBufferiv(buffer, drawbuffer, value, 0) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: UIntArray) = checkGL(30) { GLES30.glClearBufferuiv(buffer, drawbuffer, value.asIntArray(), 0) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: FloatArray) = checkGL(30) { GLES30.glClearBufferfv(buffer, drawbuffer, value, 0) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, stencil: Int) = checkGL(30) { intTmp[0] = stencil; GLES30.glClearBufferiv(buffer, drawbuffer, intTmp, 0) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float) = checkGL(30) { floatTmp[0] = depth; GLES30.glClearBufferfv(buffer, drawbuffer, floatTmp, 0) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) = checkGL(30) { GLES30.glClearBufferfi(buffer, drawbuffer, depth, stencil) }
    @GLES3 actual fun bindBufferRange(target: Int, index: Int, buffer: GLBuffer, offset: Int, size: Int) = checkGL(30) { GLES30.glBindBufferRange(target, index, buffer.id, offset, size) }
    @GLES3 actual fun bindBufferBase(target: Int, index: Int, buffer: GLBuffer) = checkGL(30) { GLES30.glBindBufferBase(target, index, buffer.id) }
    @GLES3 actual fun getBufferPointer(target: Int, pname: Int) = checkGL(30) {
        val buf = GLES30.glGetBufferPointerv(target, pname)
        if (buf === null) null else Memory.of(buf as ByteBuffer)
    }
    @GLES3 actual fun mapBufferRange(target: Int, offset: Int, length: Int, access: Int) = checkGL(30) {
        val buf = GLES30.glMapBufferRange(target, offset, length, access)
        if (buf === null) null else Memory.of(buf as ByteBuffer)
    }
    @GLES3 actual fun flushMappedBufferRange(target: Int, offset: Int, length: Int) = checkGL(30) { GLES30.glFlushMappedBufferRange(target, offset, length) }
    @GLES3 actual fun unmapBuffer(target: Int) = checkGL(30) { GLES30.glUnmapBuffer(target) }

    /////////////// Framebuffers ///////////////
    actual fun isFramebuffer(framebuffer: Int) = checkGL { GLES20.glIsFramebuffer(framebuffer) }
    actual fun genFramebuffer() = checkGL { GLES20.glGenFramebuffers(1, intTmp, 0); GLFramebuffer(intTmp[0]) }
    actual fun genFramebuffers(n: Int) = checkGL { GLFramebufferArray(IntArray(n).also { GLES20.glGenFramebuffers(n, it, 0) }) }
    actual fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer) = checkGL { GLES20.glBindFramebuffer(target, framebuffer.id) }
    actual fun deleteFramebuffer(framebuffer: GLFramebuffer) = checkGL { intTmp[0] = framebuffer.id; GLES20.glDeleteFramebuffers(1, intTmp, 0) }
    actual fun deleteFramebuffers(framebuffers: GLFramebufferArray) = checkGL { GLES20.glDeleteFramebuffers(framebuffers.size, framebuffers.ids, 0) }
    actual fun getFramebufferAttachmentInt(target: Int, attachment: Int, pname: Int) = checkGL { GLES20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, intTmp, 0); intTmp[0] }
    actual fun framebufferTexture2D(target: Int, attachment: Int, texTarget: Int, texture: GLTexture, level: Int) = checkGL { GLES20.glFramebufferTexture2D(target, attachment, texTarget, texture.id, level) }
    actual fun framebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: GLRenderbuffer) = checkGL { GLES20.glRenderbufferStorage(target, attachment, renderbufferTarget, renderbuffer.id) }
    actual fun checkFramebufferStatus(target: Int) = checkGL { GLES20.glCheckFramebufferStatus(target) }
    actual fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Bufferable) = checkGL { GLES20.glReadPixels(x, y, width, height, format, type, pixels.asBuffer()) }
    @GLES3 actual fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, offset: Int) = checkGL(30) { GLES30.glReadPixels(x, y, width, height, format, type, offset) }
    @GLES3 actual fun blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: UInt, filter: Int) = checkGL(30) { GLES30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask.toInt(), filter) }
    @GLES3 actual fun framebufferTextureLayer(target: Int, attachment: Int, texture: GLTexture, level: Int, layer: Int) = checkGL(30) { GLES30.glFramebufferTextureLayer(target, attachment, texture.id, level, layer) }
    @GLES3 actual fun invalidateFramebuffer(target: Int, attachments: IntArray) = checkGL(30) { GLES30.glInvalidateFramebuffer(target, attachments.size, attachments, 0) }
    @GLES3 actual fun invalidateSubFramebuffer(target: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) = checkGL(30) { GLES30.glInvalidateSubFramebuffer(target, attachments.size, attachments, 0, x, y, width, height) }

    /////////////// Renderbuffers ///////////////
    actual fun isRenderbuffer(renderbuffer: Int) = checkGL { GLES20.glIsRenderbuffer(renderbuffer) }
    actual fun genRenderbuffer() = checkGL { GLES20.glGenRenderbuffers(1, intTmp, 0); GLRenderbuffer(intTmp[0]) }
    actual fun genRenderbuffers(n: Int) = checkGL { GLRenderbufferArray(IntArray(n).also { GLES20.glGenRenderbuffers(n, it, 0) }) }
    actual fun bindRenderbuffer(target: Int, renderbuffer: GLRenderbuffer) = checkGL { GLES20.glBindRenderbuffer(target, renderbuffer.id) }
    actual fun deleteRenderbuffer(renderbuffer: GLRenderbuffer) = checkGL { intTmp[0] = renderbuffer.id; GLES20.glDeleteRenderbuffers(1, intTmp, 0) }
    actual fun deleteRenderbuffers(renderbuffers: GLRenderbufferArray) = checkGL { GLES20.glDeleteRenderbuffers(renderbuffers.size, renderbuffers.ids, 0) }
    actual fun renderbufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) = checkGL { GLES20.glRenderbufferStorage(target, internalFormat, width, height) }
    actual fun getRenderbufferInt(target: Int, pname: Int) = checkGL { GLES20.glGetRenderbufferParameteriv(target, pname, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getInternalformatInt(target: Int, internalformat: Int, pname: Int) = checkGL(30) { GLES30.glGetInternalformativ(target, internalformat, pname, 1, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getInternalformatIntArray(target: Int, internalformat: Int, pname: Int, output: IntArray) = checkGL(30) { output.also { GLES30.glGetInternalformativ(target, internalformat, pname, it.size, it, 0) } }
    @GLES3OrExtension actual fun renderbufferStorageMultisampleGLES3(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = checkGL(30) { GLES30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height) }

    /////////////// Texture Units ///////////////
    actual fun activeTexture(texture: Int) = checkGL { GLES20.glActiveTexture(texture) }

    /////////////// Textures ///////////////
    actual fun isTexture(texture: Int) = checkGL { GLES20.glIsTexture(texture) }
    actual fun genTexture() = checkGL { GLES20.glGenTextures(1, intTmp, 0); GLTexture(intTmp[0]) }
    actual fun genTextures(n: Int) = checkGL { GLTextureArray(IntArray(n).also { GLES20.glGenTextures(n, it, 0) }) }
    actual fun bindTexture(target: Int, texture: GLTexture) = checkGL { GLES20.glBindTexture(target, texture.id) }
    actual fun deleteTexture(texture: GLTexture) = checkGL { intTmp[0] = texture.id; GLES20.glDeleteTextures(1, intTmp, 0) }
    actual fun deleteTextures(textures: GLTextureArray) = checkGL { GLES20.glDeleteTextures(textures.size, textures.ids, 0) }
    actual fun texParameter(target: Int, pname: Int, param: Float) = checkGL { GLES20.glTexParameterf(target, pname, param) }
    actual fun texParameter(target: Int, pname: Int, param: Int) = checkGL { GLES20.glTexParameteri(target, pname, param) }
    actual fun getTexFloat(target: Int, pname: Int) = checkGL { GLES20.glGetTexParameterfv(target, pname, floatTmp, 0); floatTmp[0] }
    actual fun getTexInt(target: Int, pname: Int) = checkGL { GLES20.glGetTexParameteriv(target, pname, intTmp, 0); intTmp[0] }

    actual fun generateMipmap(target: Int) = checkGL { GLES20.glGenerateMipmap(target) }

    actual fun copyTexImage2D(target: Int, level: Int, internalFormat: Int, x: Int, y: Int,
                              width: Int, height: Int) = checkGL {
        GLES20.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, 0)
    }
    actual fun copyTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                                 x: Int, y: Int, width: Int, height: Int) = checkGL {
        GLES20.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height)
    }

    actual fun texImage2D(target: Int, level: Int, internalFormat: Int,
                          width: Int, height: Int, format: Int, type: Int, pixels: Bufferable?) = checkGL {
        GLES20.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, pixels?.asBuffer())
    }
    actual fun texSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                             width: Int, height: Int, format: Int, type: Int, pixels: Bufferable
    ) = checkGL {
        GLES20.glTexSubImage2D(target, level, xOffset, yOffset, width, height, format, type, pixels.asBuffer())
    }

    actual fun compressedTexImage2D(target: Int, level: Int, internalFormat: Int,
                                    width: Int, height: Int, data: Bufferable
    ) = checkGL {
        GLES20.glCompressedTexImage2D(target, level, internalFormat, width, height, 0, data.nbytes, data.asBuffer())
    }
    actual fun compressedTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                                       width: Int, height: Int, format: Int, data: Bufferable
    ) = checkGL {
        GLES20.glCompressedTexSubImage2D(target, level, xOffset, yOffset, width, height, format, data.nbytes, data.asBuffer())
    }

    @GLES3 actual fun texStorage2D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int) = checkGL(30) { GLES30.glTexStorage2D(target, levels, internalformat, width, height) }
    @GLES3 actual fun texStorage3D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int, depth: Int) = checkGL(30) { GLES30.glTexStorage3D(target, levels, internalformat, width, height, depth) }

    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int) = checkGL(30) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, 0, format, type, null)
    }
    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Bufferable?) = checkGL(30) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, 0, format, type, pixels?.asBuffer())
    }
    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) = checkGL(30) {
        GLES30.glTexImage3D(target, level, internalformat, width, height, depth, 0, format, type, offset)
    }
    @GLES3 actual fun texSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Bufferable) = checkGL(30) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels.asBuffer())
    }
    @GLES3 actual fun texSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) = checkGL(30) {
        GLES30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset)
    }
    @GLES3 actual fun copyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int, height: Int) = checkGL(30) {
        GLES30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)
    }
    @GLES3 actual fun compressedTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, data: Bufferable) = checkGL(30) {
        GLES30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, 0, data.nbytes, data.asBuffer())
    }
    @GLES3 actual fun compressedTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, imageSize: Int, offset: Int) = checkGL(30) {
        GLES30.glCompressedTexImage3D(target, level, internalformat, width, height, depth, 0, imageSize, offset)
    }
    @GLES3 actual fun compressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, data: Bufferable) = checkGL(30) {
        GLES30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data.nbytes, data.asBuffer())
    }
    @GLES3 actual fun compressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, imageSize: Int, offset: Int) = checkGL(30) {
        GLES30.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, offset)
    }

    /////////////// Samplers ///////////////
    @GLES3 actual fun isSampler(sampler: Int) = checkGL(30) { GLES30.glIsSampler(sampler) }
    @GLES3 actual fun genSampler() = checkGL(30) { GLES30.glGenSamplers(1, intTmp, 0); GLSampler(intTmp[0]) }
    @GLES3 actual fun genSamplers(count: Int) = checkGL(30) { GLSamplerArray(IntArray(count).also { GLES30.glGenSamplers(count, it, 0) }) }
    @GLES3 actual fun bindSampler(unit: Int, sampler: GLSampler) = checkGL(30) { GLES30.glBindSampler(unit, sampler.id) }
    @GLES3 actual fun deleteSampler(sampler: GLSampler) = checkGL(30) { intTmp[0] = sampler.id; GLES30.glDeleteSamplers(1, intTmp, 0) }
    @GLES3 actual fun deleteSamplers(samplers: GLSamplerArray) = checkGL(30) { GLES30.glDeleteSamplers(samplers.size, samplers.ids, 0) }
    @GLES3 actual fun samplerParameter(sampler: GLSampler, pname: Int, param: Float) = checkGL(30) { GLES30.glSamplerParameterf(sampler.id, pname, param) }
    @GLES3 actual fun samplerParameter(sampler: GLSampler, pname: Int, param: Int) = checkGL(30) { GLES30.glSamplerParameteri(sampler.id, pname, param) }
    @GLES3 actual fun getSamplerFloat(sampler: GLSampler, pname: Int) = checkGL(30) { GLES30.glGetSamplerParameterfv(sampler.id, pname, floatTmp, 0); floatTmp[0] }
    @GLES3 actual fun getSamplerInt(sampler: GLSampler, pname: Int) = checkGL(30) { GLES30.glGetSamplerParameteriv(sampler.id, pname, intTmp, 0); intTmp[0] }

    /////////////// Instanced Drawing ///////////////
    @GLES3OrExtension actual fun vertexAttribDivisorGLES3(index: Int, divisor: Int) = checkGL { GLES30.glVertexAttribDivisor(index, divisor) }
    @GLES3OrExtension actual fun drawArraysInstancedGLES3(mode: Int, first: Int, count: Int, instanceCount: Int) = checkGL { GLES30.glDrawArraysInstanced(mode, first, count, instanceCount) }
    @GLES3OrExtension actual fun drawElementsInstancedGLES3(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = checkGL { GLES30.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount) }

    /////////////// Query ///////////////
    @GLES3 actual fun isQuery(id: Int) = checkGL(30) { GLES30.glIsQuery(id) }
    @GLES3 actual fun genQuery() = checkGL(30) { GLES30.glGenQueries(1, intTmp, 0); GLQuery(intTmp[0]) }
    @GLES3 actual fun genQueries(n: Int) = checkGL(30) { GLQueryArray(IntArray(n).also { GLES30.glGenQueries(n, it, 0) }) }
    @GLES3 actual fun deleteQuery(id: GLQuery) = checkGL(30) { intTmp[0] = id.id; GLES30.glDeleteQueries(1, intTmp, 0) }
    @GLES3 actual fun deleteQueries(ids: GLQueryArray) = checkGL(30) { GLES30.glDeleteQueries(ids.size, ids.ids, 0) }
    @GLES3 actual fun beginQuery(target: Int, id: GLQuery) = checkGL(30) { GLES30.glBeginQuery(target, id.id) }
    @GLES3 actual fun endQuery(target: Int) = checkGL(30) { GLES30.glEndQuery(target) }
    @GLES3 actual fun getQueryInt(target: Int, pname: Int) = checkGL(30) { GLES30.glGetQueryiv(target, pname, intTmp, 0); intTmp[0] }
    @GLES3 actual fun getQueryObjectUInt(id: GLQuery, pname: Int) = checkGL(30) { GLES30.glGetQueryObjectuiv(id.id, pname, intTmp, 0); intTmp[0].toUInt() }

    /////////////// Syncs ///////////////
    @GLES3 actual fun isSync(sync: Long) = checkGL(30) { GLES30.glIsSync(sync) }
    @GLES3 actual fun fenceSync(condition: Int, flags: Int) = checkGL(30) { GLSync(GLES30.glFenceSync(condition, flags)) }
    @GLES3 actual fun deleteSync(sync: GLSync) = checkGL(30) { GLES30.glDeleteSync(sync.id) }
    @GLES3 actual fun clientWaitSync(sync: GLSync, flags: Int, timeout: Long) = checkGL(30) { GLES30.glClientWaitSync(sync.id, flags, timeout) }
    @GLES3 actual fun waitSync(sync: GLSync, flags: Int, timeout: Long) = checkGL(30) { GLES30.glWaitSync(sync.id, flags, timeout) }
    @GLES3 actual fun getSyncInt(sync: GLSync, pname: Int) = checkGL(30) { GLES30.glGetSynciv(sync.id, pname, 1, null, 0, intTmp, 0); intTmp[0] }

    /////////////// Transform Feedback ///////////////
    @GLES3 actual fun isTransformFeedback(id: Int) = checkGL(30) { GLES30.glIsTransformFeedback(id) }
    @GLES3 actual fun genTransformFeedback() = checkGL(30) { GLES30.glGenTransformFeedbacks(1, intTmp, 0); GLTransformFeedback(intTmp[0]) }
    @GLES3 actual fun genTransformFeedbacks(n: Int) = checkGL(30) { GLTransformFeedbackArray(IntArray(n).also { GLES30.glGenTransformFeedbacks(n, it, 0) }) }
    @GLES3 actual fun bindTransformFeedback(target: Int, id: GLTransformFeedback) = checkGL(30) { GLES30.glBindTransformFeedback(target, id.id) }
    @GLES3 actual fun deleteTransformFeedback(id: GLTransformFeedback) = checkGL(30) { intTmp[0] = id.id; GLES30.glDeleteTransformFeedbacks(1, intTmp, 0) }
    @GLES3 actual fun deleteTransformFeedbacks(ids: GLTransformFeedbackArray) = checkGL(30) { GLES30.glDeleteTransformFeedbacks(ids.size, ids.ids, 0) }
    @GLES3 actual fun beginTransformFeedback(primitiveMode: Int) = checkGL(30) { GLES30.glBeginTransformFeedback(primitiveMode) }
    @GLES3 actual fun endTransformFeedback() = checkGL(30) { GLES30.glEndTransformFeedback() }
    @GLES3 actual fun pauseTransformFeedback() = checkGL(30) { GLES30.glPauseTransformFeedback() }
    @GLES3 actual fun resumeTransformFeedback() = checkGL(30) { GLES30.glResumeTransformFeedback() }
    @GLES3 actual fun transformFeedbackVaryings(program: GLProgram, varyings: Array<String>, bufferMode: Int) = checkGL(30) { GLES30.glTransformFeedbackVaryings(program.id, varyings, bufferMode) }
    @GLES3 actual fun getTransformFeedbackVarying(program: GLProgram, index: Int) = checkGL(30) {
        val name = GLES30.glGetTransformFeedbackVarying(program.id, index, intTmp, 0, intTmp, 1)
        NameTypeSize(name, intTmp[1], intTmp[0])
    }
}


private inline fun eglVersion(egl: EGL10, display: EGLDisplay?) =
    egl.eglQueryString(display, EGL10.EGL_VERSION).split(' ')[0].toFloat()
