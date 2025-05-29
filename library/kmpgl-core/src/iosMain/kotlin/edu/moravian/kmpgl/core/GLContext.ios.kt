@file:Suppress("unused", "NOTHING_TO_INLINE", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalForeignApi::class)

package edu.moravian.kmpgl.core

import angle.*
import edu.moravian.kmpgl.util.Bufferable
import edu.moravian.kmpgl.util.Memory
import edu.moravian.kmpgl.util.asBuffer
import edu.moravian.kmpgl.util.of
import kotlinx.cinterop.*
import platform.CoreFoundation.CFRunLoopRun
import platform.CoreFoundation.CFRunLoopStop
import platform.Foundation.NSCondition
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.NSTimer
import platform.Foundation.performBlock
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.posix.CLOCK_UPTIME_RAW
import platform.posix.clock_gettime_nsec_np
import kotlin.math.roundToInt


actual typealias GLView = UIViewController
actual class GLPlatformContext


@OptIn(ExperimentalForeignApi::class)
fun tree(dir: String, indent: String = "") {
    val manager = NSFileManager.defaultManager
    val contents = manager.contentsOfDirectoryAtPath(dir, null)
    if (contents == null) {
        println("${indent}contents is null")
    } else {
        for (file in contents) {
            val attrs = manager.attributesOfItemAtPath("$dir/$file", null)
            if (attrs?.get("NSFileType") == NSFileTypeDirectory) {
                println("$indent$file/")
                tree("$dir/$file", "$indent  ")
            } else {
                println("$indent$file")
            }
        }
    }
}


@OptIn(ExperimentalUnsignedTypes::class)
actual open class GLContext: GLContextBase() {
    inner class Listener: MGLViewListener {
        override fun onLoad(controller: MGLKViewController) { runSyncOrAsync(::onCreate) }
        override fun onUnload(controller: MGLKViewController) { dispose() }
        override fun onPause(controller: MGLKViewController) { runSyncOrAsync(::onPause) }
        override fun onResume(controller: MGLKViewController) { runSyncOrAsync(::onResume) }
        override fun onResize(controller: MGLKViewController, width: Int, height: Int) { runSyncOrAsync { onResize(width, height) }}
        override fun onRender(controller: MGLKViewController, rect: Rect, timeSinceLastUpdate: Double) {
            val time = clock_gettime_nsec_np(CLOCK_UPTIME_RAW.toUInt()).toLong()
            runSync { onRender(time) }
        }
    }

    private var _viewController: MGLKViewController? = null
    actual val view: UIViewController get() = checkNotNull(_viewController)
    private val _view get() = _viewController?.glView

    actual val isInitialized get() = _viewController !== null
    private var _attributes: GLContextAttributes? = null
    actual val attributes get() = checkNotNull(_attributes)
    private var _version = 0
    actual val version; get() = _version

    @Throws(IllegalStateException::class)
    actual fun init(attributes: GLContextAttributes, context: GLPlatformContext) = doInit(attributes)

    // NOTE: in Swift/ObjC these are renamed to doInit()
    // Separate versions are required since Swift doesn't support default arg values
    @Throws(IllegalStateException::class)
    fun doInit() = doInit(GLContextAttributes())
    @Throws(IllegalStateException::class)
    fun doInit(attributes: GLContextAttributes): UIViewController {
        lock.withLock {
            if (isInitialized) { throw IllegalStateException("already initialized GL context") }
            _attributes = attributes
            _version = attributes.version.value
            threadCond.lockAndWait { NSThread.detachNewThreadWithBlock(::threadLoop) }
            return MGLKViewController(
                context = MGLContext(
                    attributes.version.value / 10,
                    attributes.version.value % 10,
                    Config(
                        colorFormat = ColorFormat.RGBA8888,
                        depthFormat = if (attributes.depth) DepthFormat.DF16 else DepthFormat.None,
                        stencilFormat = if (attributes.stencil) StencilFormat.SF8 else StencilFormat.None,
                        multisample = if (attributes.antialias) Multisample.X4 else Multisample.None,
                        retainedBacking = attributes.preserveDrawingBuffer,
                    )
                ),
                listener = Listener(),
            ).also {
                it.runLoop = runLoop!!
                _viewController = it
            }
        }
    }

    actual fun initIfNeeded(attributes: GLContextAttributes, context: GLPlatformContext) = doInitIfNeeded(attributes)

    // NOTE: in Swift/ObjC these are renamed to doInitIfNeeded()
    // Separate versions are required since Swift doesn't support default arg values
    fun doInitIfNeeded() = doInitIfNeeded(GLContextAttributes())
    fun doInitIfNeeded(attributes: GLContextAttributes) = _viewController ?: doInit(attributes)

    actual fun dispose() {
        lock.withLock {
            if (!isInitialized) { return }
            runSync(::onDispose)
            _viewController = null
            clearListeners()
            @Suppress("MISSING_DEPENDENCY_CLASS_IN_EXPRESSION_TYPE")
            runLoop?.let { CFRunLoopStop(it.getCFRunLoop()) }
            thread?.cancel()
            runLoop = null
            thread = null
            _attributes = null
        }
    }

    private val uiScale get() = (_view?.window?.screen ?: UIScreen.mainScreen).scale
    actual val width get() = _view!!.glLayer.drawableSize.width
    actual val height get() = _view!!.glLayer.drawableSize.height
    actual val viewWidth get() = _view!!.bounds.useContents { (size.width * uiScale).roundToInt() }
    actual val viewHeight get() = _view!!.bounds.useContents { (size.height * uiScale).roundToInt() }
    actual var viewScale: Float // TODO: these may be backwards * and /
        get() = (uiScale * _view!!.contentScaleFactor).toFloat()
        set(value) { _view!!.contentScaleFactor = uiScale / value }


    /////////////// Rendering ///////////////
    actual val isRunning get() = !_viewController!!.paused
    actual fun start() { _viewController?.resume() }
    actual fun stop() { _viewController?.pause() }

    // TODO: these may have the wrong meaning
    actual val renderingContinuously get() = !_view!!.enableSetNeedsDisplay
    actual fun renderContinuously() { _view?.enableSetNeedsDisplay = false }
    actual fun renderOnDemand() { _view?.enableSetNeedsDisplay = true }
    actual fun renderFrame() { if (_view!!.enableSetNeedsDisplay) _view?.display() }


    /////////////// Threading ///////////////
    private var thread: NSThread? = null
    private var runLoop: NSRunLoop? = null
    private val threadCond = NSCondition() // use to make sure the thread is started before we try to use it
    private val lock = NSLock()
    private fun threadLoop() {
        val runLoop = threadCond.lockAndSignal {
            thread = NSThread.currentThread.apply { name = "GL Render Thread" }
            NSRunLoop.currentRunLoop.also { runLoop = it }
        }
        // we need to add a timer that does nothing to keep the run loop alive indefinitely
        val timer = NSTimer.scheduledTimerWithTimeInterval(Double.MAX_VALUE, true) { }
        runLoop.addTimer(timer, NSDefaultRunLoopMode)
        CFRunLoopRun() // allows being stopped, unlike the below solution
        //while (!thread.cancelled && runLoop.runMode(NSDefaultRunLoopMode, NSDate.distantFuture));
    }
    actual fun isOnRenderThread() = NSThread.currentThread.isEqual(this.thread)
    actual fun runAsync(action: () -> Unit) {
        runLoop?.performBlock {
            _viewController?.makeCurrent()
            action()
        }
    }
    actual fun <T : Any> runSync(action: () -> T): T {
        if (isOnRenderThread()) {
            _viewController?.makeCurrent()
            return action()
        }

        // In general we don't want to be calling runSync except from the render thread
        // It will block the current thread until the action is completed
        println("GLContext::runSync() called from non-render thread, blocking until action is completed")
        return runLoop?.performSync(action) ?: throw IllegalStateException("GLContext runLoop is not initialized")
    }
    private fun runSyncOrAsync(action: () -> Unit) {
        if (isOnRenderThread()) { runSync(action) } else { runAsync(action) }
    }


    /////////////// Extensions ///////////////
    actual val extensions: GLExtensions by lazy { GLExtensions(this) }


    /////////////// Buffers and Type Helpers ///////////////
    internal val byteTmp = UByteArray(1)
    internal val bytePtr = byteTmp.refTo(0)
    internal var byteTmpBig = ByteArray(64)
    internal val intTmp = IntArray(4)
    internal val intPtr = intTmp.refTo(0)
    internal val intPtr1 = intTmp.refTo(1)
    internal val intPtr2 = intTmp.refTo(2)
    internal val uintPtr = intTmp.asUIntArray().refTo(0)
    internal val uintPtr1 = intTmp.asUIntArray().refTo(1)
    internal val uintPtr2 = intTmp.asUIntArray().refTo(2)
    internal val floatTmp = FloatArray(1)
    internal val floatPtr = floatTmp.refTo(0)
    internal val longTmp = LongArray(1)
    internal val longPtr = longTmp.refTo(0)
    @OptIn(ExperimentalForeignApi::class)
    internal companion object {
        inline fun Boolean.toGL(): GLboolean = if (this) GL_TRUE.toUByte() else GL_FALSE.toUByte()
        inline fun UByte.fromGL(): Boolean = this.toInt() != GL_FALSE
        inline fun Byte.fromGL(): Boolean = this.toInt() != GL_FALSE
        inline fun UByteArray.fromGL(output: BooleanArray) = output.also { this.forEachIndexed { i, v -> output[i] = v.fromGL() } }
        inline fun <T: CPointed> Int.asPtr() = this.toLong().toCPointer<T>()
        inline fun <T: CPointed> GLValue.asPtr() = this.id.toLong().toCPointer<T>()
        inline fun <T: CPointed> Long.asPtr() = this.toCPointer<T>()
        @GLES3 inline fun <T: CPointed> GLSync.asPtr() = this.id.toCPointer<T>()
        inline fun GLValue.toUInt() = id.toUInt()
        inline fun <T: GLValue> GLValues<T>.asUIntArray() = ids.asUIntArray()
        inline fun <T: CPointed> MemScope.ptr(x: CValuesRef<T>): CPointer<T> = x.getPointer(this)
    }
    internal inline fun checkByteTmpBigLength(len: Int) {
        if (len > byteTmpBig.size)
            byteTmpBig = ByteArray((len and (0x3F.inv())) + 0x40)  // next multiple of 64 bytes
    }
    internal inline fun getString(id: UInt, pname: Int, iv: (UInt, UInt, CValuesRef<IntVar>) -> Unit,
                                  get: (UInt, Int, CValuesRef<IntVar>, CValuesRef<ByteVar>) -> Unit): String {
        checkGLBase()
        checkError(iv(id, pname.toUInt(), intPtr))
        val len = intTmp[0]
        if (len == 0) { return "" }
        checkByteTmpBigLength(len)
        checkError(get(id, byteTmpBig.size, intPtr, byteTmpBig.refTo(0)))
        return byteTmpBig.toKString(endIndex = intTmp[0])
    }
    internal inline fun getNameTypeSize(id: UInt, index: Int, pname: Int, iv: (UInt, UInt, CValuesRef<IntVar>) -> Unit,
                                        get: (UInt, UInt, Int, CValuesRef<IntVar>, CValuesRef<IntVar>, CValuesRef<UIntVar>, CValuesRef<ByteVar>) -> Unit): NameTypeSize {
        checkGLBase()
        checkError(iv(id, pname.toUInt(), intPtr))
        checkByteTmpBigLength(intTmp[0])
        checkError(get(id, index.toUInt(), byteTmpBig.size, intPtr, intPtr1, uintPtr2, byteTmpBig.refTo(0)))
        return NameTypeSize(byteTmpBig.toKString(endIndex = intTmp[0]), intTmp[1], intTmp[2])
    }


    /////////////// Error Checking ///////////////
    internal inline fun <T> checkError(value: T): T =
        when (val err = glGetError().toInt()) {
            GL_NO_ERROR -> { value }
            GL_INVALID_ENUM -> throw IllegalArgumentException("GL: invalid enum value")
            GL_INVALID_VALUE -> throw IllegalArgumentException("GL: invalid GL value")
            GL_INVALID_OPERATION -> throw IllegalStateException("GL: invalid operation")
            GL_OUT_OF_MEMORY -> throw IllegalStateException("GL: out of memory")
            else -> throw RuntimeException("GL: error #${err.toString(16)}")
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
    internal inline fun <T> checkGLWithMem(function: MemScope.() -> T): T {
        checkGLBase()
        memScoped { return checkError(function()) }
    }
    private fun formatVersion(version: Int) = "${version / 10}.${version % 10}"

    actual fun getError(): Int { checkGLBase(); return glGetError().toInt() }

    /////////////// Capabilities ///////////////
    actual fun isEnabled(cap: Int) = checkGL { glIsEnabled(cap.toUInt()).toInt() != GL_FALSE }
    actual fun enable(cap: Int) = checkGL { glEnable(cap.toUInt()) }
    actual fun disable(cap: Int) = checkGL { glDisable(cap.toUInt()) }

    /////////////// Parameters ///////////////
    actual fun getBool(name: Int) = checkGL { glGetBooleanv(name.toUInt(), bytePtr); byteTmp[0].toInt() != GL_FALSE }
    actual fun getBoolArray(name: Int, output: BooleanArray): BooleanArray = checkGL {
        UByteArray(output.size).also { glGetBooleanv(name.toUInt(), it.refTo(0)) }.fromGL(output)
    }
    actual fun getInt(name: Int) = checkGL { glGetIntegerv(name.toUInt(), intPtr); intTmp[0] }
    actual fun getIntArray(name: Int, output: IntArray) = checkGL { output.also { glGetIntegerv(name.toUInt(), it.refTo(0)) } }
    actual fun getFloat(name: Int) = checkGL { glGetFloatv(name.toUInt(), floatPtr); floatTmp[0] }
    actual fun getFloatArray(name: Int, output: FloatArray) = checkGL { output.also { glGetFloatv(name.toUInt(), it.refTo(0)) } }
    actual fun getString(name: Int) = checkGL { glGetString(name.toUInt())!!.reinterpret<ByteVar>().toKString() }
    @GLES3 actual fun getLong(name: Int) = checkGL(30) { glGetInteger64v(name.toUInt(), longPtr); longTmp[0] }
    @GLES3 actual fun getLong(target: Int, index: Int) = checkGL(30) { glGetInteger64i_v(target.toUInt(), index.toUInt(), longPtr); longTmp[0] }
    @GLES3 actual fun getLongArray(name: Int, output: LongArray) = checkGL(30) { output.also { glGetInteger64v(name.toUInt(), it.refTo(0)) } }
    @GLES3 actual fun getString(name: Int, index: Int) = checkGL(30) { glGetStringi(name.toUInt(), index.toUInt())!!.reinterpret<ByteVar>().toKString() }
    @GLES3 actual fun getInt(target: Int, index: Int) = checkGL(30) { glGetIntegeri_v(target.toUInt(), index.toUInt(), intPtr); intTmp[0] }

    /////////////// Settings ///////////////
    actual fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) = checkGL { glBlendColor(red, green, blue, alpha) }
    actual fun blendEquation(mode: Int) = checkGL { glBlendEquation(mode.toUInt()) }
    actual fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) = checkGL { glBlendEquationSeparate(modeRGB.toUInt(), modeAlpha.toUInt()) }
    actual fun blendFunc(sFactor: Int, dFactor: Int) = checkGL { glBlendFunc(sFactor.toUInt(), dFactor.toUInt()) }
    actual fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) = checkGL { glBlendFuncSeparate(srcRGB.toUInt(), dstRGB.toUInt(), srcAlpha.toUInt(), dstAlpha.toUInt()) }
    actual fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = checkGL { glClearColor(red, green, blue, alpha) }
    actual fun clearDepth(depth: Float) = checkGL { glClearDepthf(depth) }
    actual fun clearStencil(s: Int) = checkGL { glClearStencil(s) }
    actual fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) = checkGL { glColorMask(red.toGL(), green.toGL(), blue.toGL(), alpha.toGL()) }
    actual fun cullFace(mode: Int) = checkGL { glCullFace(mode.toUInt()) }
    actual fun depthFunc(func: Int) = checkGL { glDepthFunc(func.toUInt()) }
    actual fun depthMask(flag: Boolean) = checkGL { glDepthMask(flag.toGL()) }
    actual fun depthRange(zNear: Float, zFar: Float) = checkGL { glDepthRangef(zNear, zFar) }
    actual fun hint(target: Int, mode: Int) = checkGL { glHint(target.toUInt(), mode.toUInt()) }
    actual fun frontFace(mode: Int) = checkGL { glFrontFace(mode.toUInt()) }
    actual fun lineWidth(width: Float) = checkGL { glLineWidth(width) }
    actual fun pixelStore(pname: Int, param: Int) = checkGL { glPixelStorei(pname.toUInt(), param) }
    actual fun polygonOffset(factor: Float, units: Float) = checkGL { glPolygonOffset(factor, units) }
    actual fun sampleCoverage(value: Float, invert: Boolean) = checkGL { glSampleCoverage(value, invert.toGL()) }
    actual fun scissor(x: Int, y: Int, width: Int, height: Int) = checkGL { glScissor(x, y, width, height) }
    actual fun stencilFunc(func: Int, ref: Int, mask: UInt) = checkGL { glStencilFunc(func.toUInt(), ref, mask) }
    actual fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: UInt) = checkGL { glStencilFuncSeparate(face.toUInt(), func.toUInt(), ref, mask) }
    actual fun stencilMask(mask: UInt) = checkGL { glStencilMask(mask) }
    actual fun stencilMaskSeparate(face: Int, mask: UInt) = checkGL { glStencilMaskSeparate(face.toUInt(), mask) }
    actual fun stencilOp(fail: Int, zFail: Int, zPass: Int) = checkGL { glStencilOp(fail.toUInt(), zFail.toUInt(), zPass.toUInt()) }
    actual fun stencilOpSeparate(face: Int, fail: Int, zFail: Int, zPass: Int) = checkGL { glStencilOpSeparate(face.toUInt(), fail.toUInt(), zFail.toUInt(), zPass.toUInt()) }
    actual fun viewport(x: Int, y: Int, width: Int, height: Int) = checkGL { glViewport(x, y, width, height) }

    /////////////// Basic Functions ///////////////
    actual fun clear(mask: UInt) = checkGL { glClear(mask) }
    actual fun drawArrays(mode: Int, first: Int, count: Int) = checkGL { glDrawArrays(mode.toUInt(), first, count) }
    actual fun drawElements(mode: Int, count: Int, type: Int, offset: Int) = checkGL { glDrawElements(mode.toUInt(), count, type.toUInt(), offset.toLong().asPtr<CPointed>()) }
    @GLES3 actual fun drawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int) = checkGL(30) { glDrawRangeElements(mode.toUInt(), start.toUInt(), end.toUInt(), count, type.toUInt(), offset.asPtr<CPointed>()) }
    actual fun finish() = checkGL { glFinish() }
    actual fun flush() = checkGL { glFlush() }

    /////////////// Program ///////////////
    actual fun isProgram(program: Int) = checkGL { glIsProgram(program.toUInt()).fromGL() }
    actual fun createProgram() = checkGL { GLProgram(glCreateProgram().toInt()) }
    actual fun linkProgram(program: GLProgram) = checkGL { glLinkProgram(program.toUInt()) }
    actual fun validateProgram(program: GLProgram) = checkGL { glValidateProgram(program.toUInt()) }
    actual fun useProgram(program: GLProgram) = checkGL { glUseProgram(program.toUInt()) }
    actual fun deleteProgram(program: GLProgram) = checkGL { glDeleteProgram(program.toUInt()) }
    actual fun getProgramInt(program: GLProgram, pname: Int) = checkGL { glGetProgramiv(program.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    actual fun getProgramInfoLog(program: GLProgram): String = getString(program.toUInt(), GL_INFO_LOG_LENGTH, ::glGetProgramiv,::glGetProgramInfoLog)
    @GLES3 actual fun programParameter(program: GLProgram, pname: Int, value: Int) = checkGL(30) { glProgramParameteri(program.toUInt(), pname.toUInt(), value) }
    @GLES3 actual fun getFragDataLocation(program: GLProgram, name: String) = checkGL(30) { glGetFragDataLocation(program.toUInt(), name) }
    @GLES3 actual fun getProgramBinary(program: GLProgram): ProgramBinary {
        checkGLBase(30)
        checkError(glGetProgramiv(program.toUInt(), GL_PROGRAM_BINARY_LENGTH.toUInt(), intPtr))
        val size = intTmp[0]
        val binary = ByteArray(size)
        checkError(glGetProgramBinary(program.toUInt(), size, intPtr, uintPtr1, binary.refTo(0)))
        return ProgramBinary(if (intTmp[0] == size) { binary } else { binary.copyOf(intTmp[0]) }, intTmp[1])
    }
    @GLES3 actual fun programBinary(program: GLProgram, binaryFormat: Int, binary: ByteArray) = checkGL(30) { glProgramBinary(program.toUInt(), binaryFormat.toUInt(), binary.refTo(0), binary.size) }

    /////////////// Program + Attrib ///////////////
    actual fun getActiveAttrib(program: GLProgram, index: Int) = getNameTypeSize(program.toUInt(), index, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, ::glGetProgramiv, ::glGetActiveAttrib)
    actual fun getAttribLocation(program: GLProgram, name: String) = checkGL { GLAttributeLocation(glGetAttribLocation(program.toUInt(), name)) }
    actual fun bindAttribLocation(program: GLProgram, index: GLAttributeLocation, name: String) = checkGL { glBindAttribLocation(program.toUInt(), index.toUInt(), name) }

    /////////////// Program + Shader ///////////////
    actual fun attachShader(program: GLProgram, shader: GLShader) = checkGL { glAttachShader(program.toUInt(), shader.toUInt()) }
    actual fun detachShader(program: GLProgram, shader: GLShader) = checkGL { glDetachShader(program.toUInt(), shader.toUInt()) }
    actual fun getAttachedShaders(program: GLProgram): GLShaderArray {
        checkGLBase()
        checkError(glGetProgramiv(program.toUInt(), GL_ATTACHED_SHADERS.toUInt(), intPtr))
        val count = intTmp[0]
        val shaders = IntArray(count)
        checkError(glGetAttachedShaders(program.toUInt(), count, intPtr, shaders.asUIntArray().refTo(0)))
        return GLShaderArray(if (intTmp[0] == count) { shaders } else { shaders.copyOf(intTmp[0]) })
    }

    /////////////// Shader ///////////////
    actual fun isShader(shader: Int) = checkGL { glIsShader(shader.toUInt()).fromGL() }
    actual fun createShader(type: Int) = checkGL { GLShader(glCreateShader(type.toUInt()).toInt()) }
    actual fun shaderSource(shader: GLShader, string: String) = checkGL { memScoped {
        val arrPtr = allocArrayOf<CPointer<ByteVar>>(null) // need a pointer to pointer to characters
        arrPtr[0] = string.cstr.ptr
        glShaderSource(shader.toUInt(), 1, arrPtr, null)
    } }
    actual fun compileShader(shader: GLShader) = checkGL { glCompileShader(shader.toUInt()) }
    actual fun deleteShader(shader: GLShader) = checkGL { glDeleteShader(shader.toUInt()) }
    actual fun getShaderSource(shader: GLShader): String = getString(shader.toUInt(), GL_SHADER_SOURCE_LENGTH, ::glGetShaderiv,::glGetShaderSource)
    actual fun getShaderInt(shader: GLShader, pname: Int) = checkGL { glGetShaderiv(shader.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    actual fun getShaderInfoLog(shader: GLShader): String = getString(shader.toUInt(), GL_INFO_LOG_LENGTH, ::glGetShaderiv,::glGetShaderInfoLog)
    actual fun getShaderPrecisionFormat(shaderType: Int, precisionType: Int) = checkGL {
        glGetShaderPrecisionFormat(shaderType.toUInt(), precisionType.toUInt(), intPtr, intPtr2)
        ShaderPrecisionFormat(intTmp[0], intTmp[1], intTmp[2])
    }
    actual fun releaseShaderCompiler() = checkGL { glReleaseShaderCompiler() }

    /////////////// Program + Uniform ///////////////
    actual fun getActiveUniform(program: GLProgram, index: Int) = getNameTypeSize(program.toUInt(), index, GL_ACTIVE_UNIFORM_MAX_LENGTH, ::glGetProgramiv, ::glGetActiveUniform)
    actual fun getUniformLocation(program: GLProgram, name: String) = checkGL { GLUniformLocation(glGetUniformLocation(program.toUInt(), name)) }
    actual fun getUniformFloat(program: GLProgram, location: GLUniformLocation, params: FloatArray, offset: Int) = checkGL { glGetUniformfv(program.toUInt(), location.id, params.refTo(offset)) }
    actual fun getUniformInt(program: GLProgram, location: GLUniformLocation, params: IntArray, offset: Int) = checkGL { glGetUniformiv(program.toUInt(), location.id, params.refTo(offset)) }
    @GLES3 actual fun getUniformIndices(program: GLProgram, uniformNames: Array<String>, output: IntArray) = checkGL(30) {
        require(uniformNames.size <= output.size) { "uniformNames and output must have the same size" }
        output.also { memScoped {
            glGetUniformIndices(program.toUInt(), uniformNames.size, uniformNames.toCStringArray(memScope), it.asUIntArray().refTo(0))
        } }
    }
    @GLES3 actual fun getActiveUniforms(program: GLProgram, uniformIndices: IntArray, pname: Int, output: IntArray) = checkGL(30) {
        require(uniformIndices.size <= output.size) { "uniformIndices and output must have the same size" }
        output.also { glGetActiveUniformsiv(program.toUInt(), uniformIndices.size, uniformIndices.asUIntArray().refTo(0), pname.toUInt(), it.refTo(0)) }
    }
    @GLES3 actual fun getUniformUInt(program: GLProgram, location: GLUniformLocation, params: UIntArray, offset: Int) = checkGL(30) { glGetUniformuiv(program.toUInt(), location.id, params.refTo(0)) }

    /////////////// Uniform ///////////////
    actual fun uniform1f(location: GLUniformLocation, x: Float) = checkGL { glUniform1f(location.id, x) }
    actual fun uniform2f(location: GLUniformLocation, x: Float, y: Float) = checkGL { glUniform2f(location.id, x, y) }
    actual fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float) = checkGL { glUniform3f(location.id, x, y, z) }
    actual fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) = checkGL { glUniform4f(location.id, x, y, z, w) }
    actual fun uniform1i(location: GLUniformLocation, x: Int) = checkGL { glUniform1i(location.id, x) }
    actual fun uniform2i(location: GLUniformLocation, x: Int, y: Int) = checkGL { glUniform2i(location.id, x, y) }
    actual fun uniform3i(location: GLUniformLocation, x: Int, y: Int, z: Int) = checkGL { glUniform3i(location.id, x, y, z) }
    actual fun uniform4i(location: GLUniformLocation, x: Int, y: Int, z: Int, w: Int) = checkGL { glUniform4i(location.id, x, y, z, w) }
    @GLES3 actual fun uniform1ui(location: GLUniformLocation, x: UInt) = checkGL(30) { glUniform1ui(location.id, x) }
    @GLES3 actual fun uniform2ui(location: GLUniformLocation, x: UInt, y: UInt) = checkGL(30) { glUniform2ui(location.id, x, y) }
    @GLES3 actual fun uniform3ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt) = checkGL(30) { glUniform3ui(location.id, x, y, z) }
    @GLES3 actual fun uniform4ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt, w: UInt) = checkGL(30) { glUniform4ui(location.id, x, y, z, w) }
    actual fun uniform1fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { glUniform1fv(location.id, count, v.refTo(offset)) }
    actual fun uniform2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { glUniform2fv(location.id, count, v.refTo(offset)) }
    actual fun uniform3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { glUniform3fv(location.id, count, v.refTo(offset)) }
    actual fun uniform4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int) = checkGL { glUniform4fv(location.id, count, v.refTo(offset)) }
    actual fun uniform1iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { glUniform1iv(location.id, count, v.refTo(offset)) }
    actual fun uniform2iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { glUniform2iv(location.id, count, v.refTo(offset)) }
    actual fun uniform3iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { glUniform3iv(location.id, count, v.refTo(offset)) }
    actual fun uniform4iv(location: GLUniformLocation, v: IntArray, offset: Int, count: Int) = checkGL { glUniform4iv(location.id, count, v.refTo(offset)) }
    @GLES3 actual fun uniform1uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { glUniform1uiv(location.id, count, v.refTo(offset)) }
    @GLES3 actual fun uniform2uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { glUniform2uiv(location.id, count, v.refTo(offset)) }
    @GLES3 actual fun uniform3uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { glUniform3uiv(location.id, count, v.refTo(offset)) }
    @GLES3 actual fun uniform4uiv(location: GLUniformLocation, v: UIntArray, offset: Int, count: Int) = checkGL(30) { glUniform4uiv(location.id, count, v.refTo(offset)) }
    actual fun uniformMatrix2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { glUniformMatrix2fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    actual fun uniformMatrix3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { glUniformMatrix3fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    actual fun uniformMatrix4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL { glUniformMatrix4fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix2x3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix2x3fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix3x2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix3x2fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix2x4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix2x4fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix4x2fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix4x2fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix3x4fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix3x4fv(location.id, count, transpose.toGL(), v.refTo(offset)) }
    @GLES3 actual fun uniformMatrix4x3fv(location: GLUniformLocation, v: FloatArray, offset: Int, count: Int, transpose: Boolean) = checkGL(30) { glUniformMatrix4x3fv(location.id, count, transpose.toGL(), v.refTo(offset)) }

    /////////////// Uniform Blocks ///////////////
    @GLES3 actual fun getUniformBlockIndex(program: GLProgram, uniformBlockName: String) = checkGL(30) { glGetUniformBlockIndex(program.toUInt(), uniformBlockName).toInt() }
    @GLES3 actual fun getActiveUniformBlockInt(program: GLProgram, uniformBlockIndex: Int, pname: Int) = checkGL(30) { glGetActiveUniformBlockiv(program.toUInt(), uniformBlockIndex.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    @GLES3 actual fun getActiveUniformBlockIntArray(program: GLProgram, uniformBlockIndex: Int, pname: Int, output: IntArray) = checkGL(30) { output.also { glGetActiveUniformBlockiv(program.toUInt(), uniformBlockIndex.toUInt(), pname.toUInt(), it.refTo(0)) } }
    @GLES3 actual fun getActiveUniformBlockName(program: GLProgram, uniformBlockIndex: Int): String {
        checkGLBase(30)
        checkError(glGetActiveUniformBlockName(program.toUInt(), uniformBlockIndex.toUInt(), byteTmpBig.size, intPtr, byteTmpBig.refTo(0)))
        return byteTmpBig.toKString(endIndex = intTmp[0])
    }
    @GLES3 actual fun uniformBlockBinding(program: GLProgram, uniformBlockIndex: Int, uniformBlockBinding: Int) = checkGL(30) { glUniformBlockBinding(program.toUInt(), uniformBlockIndex.toUInt(), uniformBlockBinding.toUInt()) }

    /////////////// Vertex Attributes ///////////////
    actual fun enableVertexAttribArray(index: GLAttributeLocation) = checkGL { glEnableVertexAttribArray(index.toUInt()) }
    actual fun disableVertexAttribArray(index: GLAttributeLocation) = checkGL { glDisableVertexAttribArray(index.toUInt()) }
    actual fun getVertexAttribFloatArray(index: GLAttributeLocation, pname: Int, output: FloatArray) = checkGL { output.also { glGetVertexAttribfv(index.toUInt(), pname.toUInt(), it.refTo(0)) } }
    actual fun getVertexAttribInt(index: GLAttributeLocation, pname: Int) = checkGL { glGetVertexAttribiv(index.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    @GLES3 actual fun getVertexAttribIntArray(index: GLAttributeLocation, pname: Int, output: IntArray) = checkGL(30) { output.also { glGetVertexAttribIiv(index.toUInt(), pname.toUInt(), it.refTo(0)) } }
    @GLES3 actual fun getVertexAttribUIntArray(index: GLAttributeLocation, pname: Int, output: UIntArray) = checkGL(30) { output.also { glGetVertexAttribIuiv(index.toUInt(), pname.toUInt(), it.refTo(0)) } }

    actual fun vertexAttribPointer(index: GLAttributeLocation, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = checkGL { glVertexAttribPointer(index.toUInt(), size, type.toUInt(), normalized.toGL(), stride, offset.asPtr<CPointed>()) }
    @GLES3 actual fun vertexAttribIPointer(index: GLAttributeLocation, size: Int, type: Int, stride: Int, offset: Int) = checkGL(30) { glVertexAttribIPointer(index.toUInt(), size, type.toUInt(), stride, offset.asPtr<CPointed>()) }

    actual fun vertexAttrib(index: GLAttributeLocation, x: Float, y: Float, z: Float, w: Float) = checkGL { glVertexAttrib4f(index.toUInt(), x, y, z, w) }
    @GLES3 actual fun vertexAttrib(index: GLAttributeLocation, x: Int, y: Int, z: Int, w: Int) = checkGL(30) { glVertexAttribI4i(index.toUInt(), x, y, z, w) }
    @GLES3 actual fun vertexAttrib(index: GLAttributeLocation, x: UInt, y: UInt, z: UInt, w: UInt) = checkGL(30) { glVertexAttribI4ui(index.toUInt(), x, y, z, w) }
    actual fun vertexAttrib1fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { glVertexAttrib1fv(index.toUInt(), values.refTo(offset)) }
    actual fun vertexAttrib2fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { glVertexAttrib2fv(index.toUInt(), values.refTo(offset)) }
    actual fun vertexAttrib3fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { glVertexAttrib3fv(index.toUInt(), values.refTo(offset)) }
    actual fun vertexAttrib4fv(index: GLAttributeLocation, values: FloatArray, offset: Int) = checkGL { glVertexAttrib4fv(index.toUInt(), values.refTo(offset)) }
    @GLES3 actual fun vertexAttrib4iv(index: GLAttributeLocation, v: IntArray, offset: Int) = checkGL(30) { glVertexAttribI4iv(index.toUInt(), v.refTo(offset)) }
    @GLES3 actual fun vertexAttrib4uiv(index: GLAttributeLocation, v: UIntArray, offset: Int) = checkGL(30) { glVertexAttribI4uiv(index.toUInt(), v.refTo(offset)) }

    /////////////// Vertex Array Objects ///////////////
    @GLES3OrExtension actual fun isVertexArrayGLES3(array: Int) = checkGL(30) { glIsVertexArray(array.toUInt()).fromGL() }
    @GLES3OrExtension actual fun genVertexArrayGLES3() = checkGL(30) { glGenVertexArrays(1, uintPtr); GLVertexArrayObject(intTmp[0]) }
    @GLES3OrExtension actual fun genVertexArraysGLES3(n: Int) = checkGL(30) { GLVertexArrayObjectArray(IntArray(n).also { glGenVertexArrays(n, it.asUIntArray().refTo(0)) }) }
    @GLES3OrExtension actual fun bindVertexArrayGLES3(array: GLVertexArrayObject) = checkGL(30) { glBindVertexArray(array.toUInt()) }
    @GLES3OrExtension actual fun deleteVertexArrayGLES3(array: GLVertexArrayObject) = checkGL(30) { intTmp[0] = array.id; glDeleteVertexArrays(1, uintPtr) }
    @GLES3OrExtension actual fun deleteVertexArraysGLES3(arrays: GLVertexArrayObjectArray) = checkGL(30) { glDeleteVertexArrays(1, arrays.asUIntArray().refTo(0)) }

    /////////////// Buffers ///////////////
    actual fun isBuffer(buffer: Int) = checkGL { glIsBuffer(buffer.toUInt()).fromGL() }
    actual fun genBuffer() = checkGL { glGenBuffers(1, uintPtr); GLBuffer(intTmp[0]) }
    actual fun genBuffers(n: Int) = checkGL { GLBufferArray(IntArray(n).also { glGenBuffers(n, it.asUIntArray().refTo(0)) }) }
    actual fun bindBuffer(target: Int, buffer: GLBuffer) = checkGL { glBindBuffer(target.toUInt(), buffer.toUInt()) }
    actual fun deleteBuffer(buffer: GLBuffer) = checkGL { intTmp[0] = buffer.id; glDeleteBuffers(1, uintPtr) }
    actual fun deleteBuffers(buffers: GLBufferArray) = checkGL { glDeleteBuffers(buffers.size, buffers.asUIntArray().refTo(0)) }
    actual fun getBufferInt(target: Int, pname: Int) = checkGL { glGetBufferParameteriv(target.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    @GLES3 actual fun getBufferLong(target: Int, pname: Int) = checkGL(30) { glGetBufferParameteri64v(target.toUInt(), pname.toUInt(), longPtr); longTmp[0] }

    actual fun bufferData(target: Int, usage: Int, size: Int) = checkGL { glBufferData(target.toUInt(), size.toLong(), null, usage.toUInt()) }
    actual fun bufferData(target: Int, usage: Int, data: Bufferable) = checkGL {
        glBufferData(target.toUInt(), data.nbytes.toLong(), data.asBuffer(), usage.toUInt())
    }
    actual fun bufferSubData(target: Int, offset: Int, data: Bufferable) = checkGL {
        glBufferSubData(target.toUInt(), offset.toLong(), data.nbytes.toLong(), data.asBuffer())
    }
    actual fun bufferSubData(target: Int, offset: Int, data: Bufferable, srcOffset: Int, count: Int) = checkGL {
        glBufferSubData(target.toUInt(), offset.toLong(), (count * data.elementSize).toLong(), data.asBuffer(srcOffset, count))
    }

    @GLES3 actual fun copyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int) = checkGL(30) { glCopyBufferSubData(readTarget.toUInt(), writeTarget.toUInt(), readOffset.toLong(), writeOffset.toLong(), size.toLong()) }

    @GLES3OrExtension actual fun drawBuffersGLES3(bufs: IntArray) = checkGL(30) { glDrawBuffers(bufs.size, bufs.asUIntArray().refTo(0)) }
    @GLES3 actual fun readBuffer(mode: Int) = checkGL(30) { glReadBuffer(mode.toUInt()) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: IntArray) = checkGL(30) { glClearBufferiv(buffer.toUInt(), drawbuffer, value.refTo(0)) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: UIntArray) = checkGL(30) { glClearBufferuiv(buffer.toUInt(), drawbuffer, value.refTo(0)) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, value: FloatArray) = checkGL(30) { glClearBufferfv(buffer.toUInt(), drawbuffer, value.refTo(0)) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, stencil: Int) = checkGL(30) { intTmp[0] = stencil; glClearBufferiv(buffer.toUInt(), drawbuffer, intPtr) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float) = checkGL(30) { floatTmp[0] = depth; glClearBufferfv(buffer.toUInt(), drawbuffer, floatPtr) }
    @GLES3 actual fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int) = checkGL(30) { glClearBufferfi(buffer.toUInt(), drawbuffer, depth, stencil) }
    @GLES3 actual fun bindBufferRange(target: Int, index: Int, buffer: GLBuffer, offset: Int, size: Int) = checkGL(30) { glBindBufferRange(target.toUInt(), index.toUInt(), buffer.toUInt(), offset.toLong(), size.toLong()) }
    @GLES3 actual fun bindBufferBase(target: Int, index: Int, buffer: GLBuffer) = checkGL(30) { glBindBufferBase(target.toUInt(), index.toUInt(), buffer.toUInt()) }
    @GLES3 actual fun getBufferPointer(target: Int, pname: Int): Memory? {
        checkGLBase(30)
        checkError(glGetBufferParameteriv(target.toUInt(), GL_BUFFER_MAPPED.toUInt(), intPtr))
        if (intTmp[0] == GL_FALSE) return null

        checkError(glGetBufferParameteri64v(target.toUInt(), GL_BUFFER_MAP_LENGTH.toUInt(), longPtr))
        val size = longTmp[0]
        if (size == 0L) { return Memory.Empty }

        longTmp[0] = 0
        memScoped { glGetBufferPointerv(target.toUInt(), pname.toUInt(), longPtr.getPointer(memScope).reinterpret()) }
        return if (longTmp[0] == 0L) null else Memory.of(longTmp[0].asPtr<CPointed>()!!, size.toInt())
    }
    @GLES3 actual fun mapBufferRange(target: Int, offset: Int, length: Int, access: Int) = checkGL(30) {
        val buf = glMapBufferRange(target.toUInt(), offset.toLong(), length.toLong(), access.toUInt())
        if (buf === null) null else Memory.of(buf, length)
    }
    @GLES3 actual fun flushMappedBufferRange(target: Int, offset: Int, length: Int) = checkGL(30) { glFlushMappedBufferRange(target.toUInt(), offset.toLong(), length.toLong()) }
    @GLES3 actual fun unmapBuffer(target: Int) = checkGL(30) { glUnmapBuffer(target.toUInt()).fromGL() }

    /////////////// Framebuffers ///////////////
    actual fun isFramebuffer(framebuffer: Int) = checkGL { glIsFramebuffer(framebuffer.toUInt()).fromGL() }
    actual fun genFramebuffer() = checkGL { glGenFramebuffers(1, uintPtr); GLFramebuffer(intTmp[0]) }
    actual fun genFramebuffers(n: Int) = checkGL { GLFramebufferArray(IntArray(n).also { glGenFramebuffers(n, it.asUIntArray().refTo(0)) }) }
    actual fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer) = checkGL { glBindFramebuffer(target.toUInt(), framebuffer.toUInt()) }
    actual fun deleteFramebuffer(framebuffer: GLFramebuffer) = checkGL { intTmp[0] = framebuffer.id; glDeleteFramebuffers(1, uintPtr) }
    actual fun deleteFramebuffers(framebuffers: GLFramebufferArray) = checkGL { glDeleteFramebuffers(framebuffers.size, framebuffers.asUIntArray().refTo(0)) }
    actual fun getFramebufferAttachmentInt(target: Int, attachment: Int, pname: Int) = checkGL { glGetFramebufferAttachmentParameteriv(target.toUInt(), attachment.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    actual fun framebufferTexture2D(target: Int, attachment: Int, texTarget: Int, texture: GLTexture, level: Int) = checkGL { glFramebufferTexture2D(target.toUInt(), attachment.toUInt(), texTarget.toUInt(), texture.toUInt(), level) }
    actual fun framebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: GLRenderbuffer) = checkGL { glRenderbufferStorage(target.toUInt(), attachment.toUInt(), renderbufferTarget, renderbuffer.id) }
    actual fun checkFramebufferStatus(target: Int) = checkGL { glCheckFramebufferStatus(target.toUInt()).toInt() }
    actual fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Bufferable) = checkGL { glReadPixels(x, y, width, height, format.toUInt(), type.toUInt(), pixels.asBuffer()) }
    @GLES3 actual fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, offset: Int) = checkGL(30) { glReadPixels(x, y, width, height, format.toUInt(), type.toUInt(), offset.asPtr<CPointed>()) }
    @GLES3 actual fun blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: UInt, filter: Int) = checkGL(30) { glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter.toUInt()) }
    @GLES3 actual fun framebufferTextureLayer(target: Int, attachment: Int, texture: GLTexture, level: Int, layer: Int) = checkGL(30) { glFramebufferTextureLayer(target.toUInt(), attachment.toUInt(), texture.toUInt(), level, layer) }
    @GLES3 actual fun invalidateFramebuffer(target: Int, attachments: IntArray) = checkGL(30) { glInvalidateFramebuffer(target.toUInt(), attachments.size, attachments.asUIntArray().refTo(0)) }
    @GLES3 actual fun invalidateSubFramebuffer(target: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int) = checkGL(30) { glInvalidateSubFramebuffer(target.toUInt(), attachments.size, attachments.asUIntArray().refTo(0), x, y, width, height) }

    /////////////// Renderbuffers ///////////////
    actual fun isRenderbuffer(renderbuffer: Int) = checkGL { glIsRenderbuffer(renderbuffer.toUInt()).fromGL() }
    actual fun genRenderbuffer() = checkGL { glGenRenderbuffers(1, uintPtr); GLRenderbuffer(intTmp[0]) }
    actual fun genRenderbuffers(n: Int) = checkGL { GLRenderbufferArray(IntArray(n).also { glGenRenderbuffers(n, it.asUIntArray().refTo(0)) }) }
    actual fun bindRenderbuffer(target: Int, renderbuffer: GLRenderbuffer) = checkGL { glBindRenderbuffer(target.toUInt(), renderbuffer.toUInt()) }
    actual fun deleteRenderbuffer(renderbuffer: GLRenderbuffer) = checkGL { intTmp[0] = renderbuffer.id; glDeleteRenderbuffers(1, uintPtr) }
    actual fun deleteRenderbuffers(renderbuffers: GLRenderbufferArray) = checkGL { glDeleteRenderbuffers(renderbuffers.size, renderbuffers.asUIntArray().refTo(0)) }
    actual fun renderbufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) = checkGL { glRenderbufferStorage(target.toUInt(), internalFormat.toUInt(), width, height) }
    actual fun getRenderbufferInt(target: Int, pname: Int) = checkGL { glGetRenderbufferParameteriv(target.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    @GLES3 actual fun getInternalformatInt(target: Int, internalformat: Int, pname: Int) = checkGL(30) { glGetInternalformativ(target.toUInt(), internalformat.toUInt(), pname.toUInt(), 1, intPtr); intTmp[0] }
    @GLES3 actual fun getInternalformatIntArray(target: Int, internalformat: Int, pname: Int, output: IntArray) = checkGL(30) { output.also { glGetInternalformativ(target.toUInt(), internalformat.toUInt(), pname.toUInt(), output.size, it.refTo(0)) } }
    @GLES3OrExtension actual fun renderbufferStorageMultisampleGLES3(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = checkGL(30) { glRenderbufferStorageMultisample(target.toUInt(), samples, internalformat.toUInt(), width, height) }

    /////////////// Texture Units ///////////////
    actual fun activeTexture(texture: Int) = checkGL { glActiveTexture(texture.toUInt()) }

    /////////////// Textures ///////////////
    actual fun isTexture(texture: Int) = checkGL { glIsTexture(texture.toUInt()).fromGL() }
    actual fun genTexture() = checkGL { glGenTextures(1, uintPtr); GLTexture(intTmp[0]) }
    actual fun genTextures(n: Int) = checkGL { GLTextureArray(IntArray(n).also { glGenTextures(n, it.asUIntArray().refTo(0)) }) }
    actual fun bindTexture(target: Int, texture: GLTexture) = checkGL { glBindTexture(target.toUInt(), texture.toUInt()) }
    actual fun deleteTexture(texture: GLTexture) = checkGL { intTmp[0] = texture.id; glDeleteTextures(1, uintPtr) }
    actual fun deleteTextures(textures: GLTextureArray) = checkGL { glDeleteTextures(textures.size, textures.asUIntArray().refTo(0)) }
    actual fun texParameter(target: Int, pname: Int, param: Float) = checkGL { glTexParameterf(target.toUInt(), pname.toUInt(), param) }
    actual fun texParameter(target: Int, pname: Int, param: Int) = checkGL { glTexParameteri(target.toUInt(), pname.toUInt(), param) }
    actual fun getTexFloat(target: Int, pname: Int) = checkGL { glGetTexParameterfv(target.toUInt(), pname.toUInt(), floatPtr); floatTmp[0] }
    actual fun getTexInt(target: Int, pname: Int) = checkGL { glGetTexParameteriv(target.toUInt(), pname.toUInt(), intPtr); intTmp[0] }

    actual fun generateMipmap(target: Int) = checkGL { glGenerateMipmap(target.toUInt()) }

    actual fun copyTexImage2D(target: Int, level: Int, internalFormat: Int, x: Int, y: Int,
                              width: Int, height: Int) = checkGL {
        glCopyTexImage2D(target.toUInt(), level, internalFormat.toUInt(), x, y, width, height, 0)
    }
    actual fun copyTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                                 x: Int, y: Int, width: Int, height: Int) = checkGL {
        glCopyTexSubImage2D(target.toUInt(), level, xOffset, yOffset, x, y, width, height)
    }

    actual fun texImage2D(target: Int, level: Int, internalFormat: Int,
                          width: Int, height: Int, format: Int, type: Int, pixels: Bufferable?) = checkGL {
        glTexImage2D(target.toUInt(), level, internalFormat, width, height, 0, format.toUInt(), type.toUInt(), pixels?.asBuffer())
    }
    actual fun texSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                             width: Int, height: Int, format: Int, type: Int, pixels: Bufferable
    ) = checkGL {
        glTexSubImage2D(target.toUInt(), level, xOffset, yOffset, width, height, format.toUInt(), type.toUInt(), pixels.asBuffer())
    }

    actual fun compressedTexImage2D(target: Int, level: Int, internalFormat: Int,
                                    width: Int, height: Int, data: Bufferable
    ) = checkGL {
        glCompressedTexImage2D(target.toUInt(), level, internalFormat.toUInt(), width, height, 0, data.nbytes, data.asBuffer())
    }
    actual fun compressedTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                                       width: Int, height: Int, format: Int, data: Bufferable
    ) = checkGL {
        glCompressedTexSubImage2D(target.toUInt(), level, xOffset, yOffset, width, height, format.toUInt(), data.nbytes, data.asBuffer())
    }

    @GLES3 actual fun texStorage2D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int) = checkGL(30) { glTexStorage2D(target.toUInt(), levels, internalformat.toUInt(), width, height) }
    @GLES3 actual fun texStorage3D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int, depth: Int) = checkGL(30) { glTexStorage3D(target.toUInt(), levels, internalformat.toUInt(), width, height, depth) }

    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int) = checkGL(30) {
        glTexImage3D(target.toUInt(), level, internalformat, width, height, depth, 0, format.toUInt(), type.toUInt(), null)
    }
    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Bufferable?) = checkGL(30) {
        glTexImage3D(target.toUInt(), level, internalformat, width, height, depth, 0, format.toUInt(), type.toUInt(), pixels?.asBuffer())
    }
    @GLES3 actual fun texImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) = checkGL(30) {
        glTexImage3D(target.toUInt(), level, internalformat, width, height, depth, 0, format.toUInt(), type.toUInt(), offset.asPtr<CPointed>())
    }
    @GLES3 actual fun texSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Bufferable) = checkGL(30) {
        glTexSubImage3D(target.toUInt(), level, xoffset, yoffset, zoffset, width, height, depth, format.toUInt(), type.toUInt(), pixels.asBuffer())
    }
    @GLES3 actual fun texSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, offset: Int) = checkGL(30) {
        glTexSubImage3D(target.toUInt(), level, xoffset, yoffset, zoffset, width, height, depth, format.toUInt(), type.toUInt(), offset.asPtr<CPointed>())
    }
    @GLES3 actual fun copyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, x: Int, y: Int, width: Int, height: Int) = checkGL(30) {
        glCopyTexSubImage3D(target.toUInt(), level, xoffset, yoffset, zoffset, x, y, width, height)
    }
    @GLES3 actual fun compressedTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, data: Bufferable) = checkGL(30) {
        glCompressedTexImage3D(target.toUInt(), level, internalformat.toUInt(), width, height, depth, 0, data.nbytes, data.asBuffer())
    }
    @GLES3 actual fun compressedTexImage3D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, depth: Int, imageSize: Int, offset: Int) = checkGL(30) {
        glCompressedTexImage3D(target.toUInt(), level, internalformat.toUInt(), width, height, depth, 0, imageSize, offset.asPtr<CPointed>())
    }
    @GLES3 actual fun compressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, data: Bufferable) = checkGL(30) {
        glCompressedTexSubImage3D(target.toUInt(), level, xoffset, yoffset, zoffset, width, height, depth, format.toUInt(), data.nbytes, data.asBuffer())
    }
    @GLES3 actual fun compressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, imageSize: Int, offset: Int) = checkGL(30) {
        glCompressedTexSubImage3D(target.toUInt(), level, xoffset, yoffset, zoffset, width, height, depth, format.toUInt(), imageSize, offset.asPtr<CPointed>())
    }

    /////////////// Samplers ///////////////
    @GLES3 actual fun isSampler(sampler: Int) = checkGL(30) { glIsSampler(sampler.toUInt()).fromGL() }
    @GLES3 actual fun genSampler() = checkGL(30) { glGenSamplers(1, uintPtr); GLSampler(intTmp[0]) }
    @GLES3 actual fun genSamplers(count: Int) = checkGL(30) { GLSamplerArray(IntArray(count).also { glGenSamplers(count, it.asUIntArray().refTo(0)) }) }
    @GLES3 actual fun bindSampler(unit: Int, sampler: GLSampler) = checkGL(30) { glBindSampler(unit.toUInt(), sampler.toUInt()) }
    @GLES3 actual fun deleteSampler(sampler: GLSampler) = checkGL(30) { intTmp[0] = sampler.id; glDeleteSamplers(1, uintPtr) }
    @GLES3 actual fun deleteSamplers(samplers: GLSamplerArray) = checkGL(30) { glDeleteSamplers(samplers.size, samplers.asUIntArray().refTo(0)) }
    @GLES3 actual fun samplerParameter(sampler: GLSampler, pname: Int, param: Float) = checkGL(30) { glSamplerParameterf(sampler.toUInt(), pname.toUInt(), param) }
    @GLES3 actual fun samplerParameter(sampler: GLSampler, pname: Int, param: Int) = checkGL(30) { glSamplerParameteri(sampler.toUInt(), pname.toUInt(), param) }
    @GLES3 actual fun getSamplerFloat(sampler: GLSampler, pname: Int) = checkGL(30) { glGetSamplerParameterfv(sampler.toUInt(), pname.toUInt(), floatPtr); floatTmp[0] }
    @GLES3 actual fun getSamplerInt(sampler: GLSampler, pname: Int) = checkGL(30) { glGetSamplerParameteriv(sampler.toUInt(), pname.toUInt(), intPtr); intTmp[0] }

    /////////////// Instanced Drawing ///////////////
    @GLES3OrExtension actual fun vertexAttribDivisorGLES3(index: Int, divisor: Int) = checkGL(30) { glVertexAttribDivisor(index.toUInt(), divisor.toUInt()) }
    @GLES3OrExtension actual fun drawArraysInstancedGLES3(mode: Int, first: Int, count: Int, instanceCount: Int) = checkGL(30) { glDrawArraysInstanced(mode.toUInt(), first, count, instanceCount) }
    @GLES3OrExtension actual fun drawElementsInstancedGLES3(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = checkGL(30) { glDrawElementsInstanced(mode.toUInt(), count, type.toUInt(), indicesOffset.asPtr<CPointed>(), instanceCount) }

    /////////////// Query ///////////////
    @GLES3 actual fun isQuery(id: Int) = checkGL(30) { glIsQuery(id.toUInt()).fromGL() }
    @GLES3 actual fun genQuery() = checkGL(30) { glGenQueries(1, uintPtr); GLQuery(intTmp[0]) }
    @GLES3 actual fun genQueries(n: Int) = checkGL(30) { GLQueryArray(IntArray(n).also { glGenQueries(n, it.asUIntArray().refTo(0)) }) }
    @GLES3 actual fun deleteQuery(id: GLQuery) = checkGL(30) { intTmp[0] = id.id; glDeleteQueries(1, uintPtr) }
    @GLES3 actual fun deleteQueries(ids: GLQueryArray) = checkGL(30) { glDeleteQueries(ids.size, ids.asUIntArray().refTo(0)) }
    @GLES3 actual fun beginQuery(target: Int, id: GLQuery) = checkGL(30) { glBeginQuery(target.toUInt(), id.toUInt()) }
    @GLES3 actual fun endQuery(target: Int) = checkGL(30) { glEndQuery(target.toUInt()) }
    @GLES3 actual fun getQueryInt(target: Int, pname: Int) = checkGL(30) { glGetQueryiv(target.toUInt(), pname.toUInt(), intPtr); intTmp[0] }
    @GLES3 actual fun getQueryObjectUInt(id: GLQuery, pname: Int) = checkGL(30) { glGetQueryObjectuiv(id.toUInt(), pname.toUInt(), uintPtr); intTmp[0].toUInt() }

    /////////////// Syncs ///////////////
    @GLES3 actual fun isSync(sync: Long) = checkGL(30) { glIsSync(sync.asPtr()).fromGL() }
    @GLES3 actual fun fenceSync(condition: Int, flags: Int) = checkGL(30) { GLSync(glFenceSync(condition.toUInt(), flags.toUInt()).toLong()) }
    @GLES3 actual fun deleteSync(sync: GLSync) = checkGL(30) { glDeleteSync(sync.asPtr()) }
    @GLES3 actual fun clientWaitSync(sync: GLSync, flags: Int, timeout: Long) = checkGL(30) { glClientWaitSync(sync.asPtr(), flags.toUInt(), timeout.toULong()).toInt() }
    @GLES3 actual fun waitSync(sync: GLSync, flags: Int, timeout: Long) = checkGL(30) { glWaitSync(sync.asPtr(), flags.toUInt(), timeout.toULong()) }
    @GLES3 actual fun getSyncInt(sync: GLSync, pname: Int) = checkGL(30) { glGetSynciv(sync.asPtr(), pname.toUInt(), 1, null, intPtr); intTmp[0] }

    /////////////// Transform Feedback ///////////////
    @GLES3 actual fun isTransformFeedback(id: Int) = checkGL(30) { glIsTransformFeedback(id.toUInt()).fromGL() }
    @GLES3 actual fun genTransformFeedback() = checkGL(30) { glGenTransformFeedbacks(1, uintPtr); GLTransformFeedback(intTmp[0]) }
    @GLES3 actual fun genTransformFeedbacks(n: Int) = checkGL(30) { GLTransformFeedbackArray(IntArray(n).also { glGenTransformFeedbacks(n, it.asUIntArray().refTo(0)) }) }
    @GLES3 actual fun bindTransformFeedback(target: Int, id: GLTransformFeedback) = checkGL(30) { glBindTransformFeedback(target.toUInt(), id.toUInt()) }
    @GLES3 actual fun deleteTransformFeedback(id: GLTransformFeedback) = checkGL(30) { intTmp[0] = id.id; glDeleteTransformFeedbacks(1, uintPtr) }
    @GLES3 actual fun deleteTransformFeedbacks(ids: GLTransformFeedbackArray) = checkGL(30) { glDeleteTransformFeedbacks(ids.size, ids.asUIntArray().refTo(0)) }
    @GLES3 actual fun beginTransformFeedback(primitiveMode: Int) = checkGL(30) { glBeginTransformFeedback(primitiveMode.toUInt()) }
    @GLES3 actual fun endTransformFeedback() = checkGL(30) { glEndTransformFeedback() }
    @GLES3 actual fun pauseTransformFeedback() = checkGL(30) { glPauseTransformFeedback() }
    @GLES3 actual fun resumeTransformFeedback() = checkGL(30) { glResumeTransformFeedback() }
    @GLES3 actual fun transformFeedbackVaryings(program: GLProgram, varyings: Array<String>, bufferMode: Int) {
        memScoped {
            glTransformFeedbackVaryings(program.toUInt(), varyings.size, varyings.toCStringArray(memScope), bufferMode.toUInt())
        }
    }
    @GLES3 actual fun getTransformFeedbackVarying(program: GLProgram, index: Int) = getNameTypeSize(program.toUInt(), index, GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH, ::glGetProgramiv, ::glGetTransformFeedbackVarying)
}
