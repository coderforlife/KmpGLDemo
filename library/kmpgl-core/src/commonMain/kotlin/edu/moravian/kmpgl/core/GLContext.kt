@file:Suppress("NOTHING_TO_INLINE", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package edu.moravian.kmpgl.core

import edu.moravian.kmpgl.util.Bufferable
import edu.moravian.kmpgl.util.Memory

expect class GLView // the platform-specific view to add to the layout
expect class GLPlatformContext // holds the platform-specific context for initialization

/**
 * Class for interfacing to OpenGL ES. This provides a cross-platform way to
 * initialize and manage a renderable view using OpenGL ES. This mostly mimics
 * WebGLRenderingContext from WebGL, including:
 *  - supports mostly the same context attributes during creation
 *  - functions drop the "gl" prefix
 * Some differences:
 *  - this class creates and manages the view (i.e. canvas) instead of it being
 *    created outside of the class (use the platform-specific init() method)
 *  - manages the rendering loop directly (although can also be managed
 *    externally) and you should register a listener
 *  - you must run things that interact with GL on the rendering thread (WebGL
 *    only has one thread), can use isOnRenderThread(), runAsync(), runSync();
 *    all of the functions in GLListener instances are automatically run on
 *    the rendering thread
 *  - no GL constants defined within the context, instead in the singleton
 *    object GL
 *  - some differences in functions (e.g. no getParameter() since it can return
 *    different types, but there are specific getInt(), ... methods);
 *    parameters like "border" that must always be a specific value have been
 *    removed; postfix argument types like "i" or "f" sometimes dropped from
 *    the names
 *
 * To select the version of GLES, use the GLContextAttributes class.
 *
 * On any system, the biggest task it to implement a GLListener instance.
 * This is where all the logic for the 3D program will be implemented
 * (primarily in create()/recreate() for loading resources and render() to draw
 * a frame).
 *
 * A GLContext object can only be initialized once, but can be reused (the view
 * can be moved to another layout for example). The context and view can only
 * be used once at a time in a particular layout though, so if additional views
 * are needed in the same layout, additional contexts will be required.
 *
 * Android
 * =======
 * In your Activity class, make a new variable for your GL context:
 *    private val glContext = GLContext()
 * In onCreate() or elsewhere:
 *    glContext.addListener(listener) // add first to make sure we get the initial create() event
 *    findViewById(R.id.rootView).addView(glContext.initIfNeeded(context = GLPlatformContext(context))) // can also take GLContextAttributes to adjust the surface created
 *
 * iOS - SwiftUI
 * =============
 * Add the following wrapper class:  (eventually this will be provided, but needed for now)
 *    import SwiftUI
 *    import UIKit
 *    import kmpgl_core
 *    struct GLView: UIViewControllerRepresentable {
 *        let glContext = GLContext()
 *        func makeUIViewController(context: Context) -> UIViewController {
 *            glContext.addListener(listener: listener)
 *            return glContext.doInitIfNeeded()
 *        }
 *        func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
 *            glContext.viewController.viewDidLoad()
 *        }
 *   }
 * Then you can use it in your other views, for example:
 *   GLView().frame(width: 375, height: 375)
 *
 * Compose
 * =======
 * See edu.moravian.kmpgl.compose for a multiplatform Compose wrapper.
 */
@OptIn(ExperimentalUnsignedTypes::class)
expect class GLContext(): GLContextBase {
    /**
     * Takes an optional GLContextAttributes and platform-specific context.
     * Returns the view to add to the layout.
     * Throws IllegalStateException if already initialized.
     */
    fun init(attributes: GLContextAttributes = GLContextAttributes(), context: GLPlatformContext): GLView

    /**
     * Like init() but accepts being already initialized, in this case all arguments are ignored.
     */
    fun initIfNeeded(attributes: GLContextAttributes = GLContextAttributes(), context: GLPlatformContext): GLView

        /** The view that is to be added to the layout. */
    val view: GLView

    /**
     * Free any resources associated with this object,
     * the context cannot be used after this.
     */
    fun dispose()

    /** True if the context has been initialized. */
    val isInitialized: Boolean

    /**
     * The attributes used to initialize this context. These may be different
     * than the ones passed to init() if some features were not available.
     */
    val attributes: GLContextAttributes

    /** The GL version initialized with, with 20 for 2.0, 30 for 3.0, and 31 for 3.1 */
    val version: Int

    /** The width of the drawing surface in pixels */
    val width: Int
    /** The height of the drawing surface in pixels */
    val height: Int

    /** The width of the view itself in pixels */
    val viewWidth: Int
    /** The height of the view itself in pixels */
    val viewHeight: Int

    /**
     * The content scaling differential between the drawing surface size
     * and the view scale. Default is 1. Lower values create pixelated results.
     */
    var viewScale: Float

    /**  Check if the rendering is currently running (i.e. not stopped). */
    val isRunning: Boolean

    /**
     * Start the rendering if currently stopped. This will cause
     * listener.resume() to be called followed by listener.render() to be
     * called continuously again.
     */
    fun start()

    /**
     * Stop the rendering if currently started. This will cause
     * listener.pause() to be called.
     */
    fun stop()

    // In super-class:
    //fun addListener(listener: GLListener) { listeners.add(listener) }
    //fun removeListener(listener: GLListener) { listeners.remove(listener) }
    //fun hasListener(listener: GLListener) { listener in listeners }

    val renderingContinuously: Boolean
    /** Only render frames continuously (default operation) */
    fun renderContinuously()
    /** Only render frames on-demand (with renderFrame() method) */
    fun renderOnDemand()
    /** Force a frame to be drawn if on-demand. Does nothing if rendering continuously. */
    fun renderFrame()

    /** Checks if the current thread is the render thread. */
    fun isOnRenderThread(): Boolean

    /**
     * Queues a runnable on the render thread. This function returns
     * immediately and does not wait for the runnable to finish or
     * even start executing.
     */
    fun runAsync(action: () -> Unit)

    /**
     * Queues a runnable on the render thread and waits for it to complete
     * before returning. This function returns whatever the action returns.
     */
    fun <T: Any> runSync(action: () -> T): T

    /** Gets the extensions for this context. */
    val extensions: GLExtensions

    /////////////// The rest of the functions are GL functions ///////////////
    // See https://registry.khronos.org/OpenGL-Refpages/es2.0/
    // Find documentation for them by looking for glXxxXxx online (or slight variations sometimes)
    // They must be called from the rendering thread and only after being initialized

    // Get the last error (although all functions actually throw exceptions when there is an error)
    fun getError(): Int

    // Capabilities
    fun isEnabled(cap: Int): Boolean
    fun enable(cap: Int)
    fun disable(cap: Int)

    // Parameters
    fun getBool(name: Int): Boolean
    fun getBoolArray(name: Int, output: BooleanArray): BooleanArray
    fun getInt(name: Int): Int
    fun getIntArray(name: Int, output: IntArray): IntArray
    @GLES3 fun getLong(name: Int): Long
    @GLES3 fun getLongArray(name: Int, output: LongArray): LongArray
    fun getFloat(name: Int): Float
    fun getFloatArray(name: Int, output: FloatArray): FloatArray
    fun getString(name: Int): String
    @GLES3 fun getString(name: Int = GL.EXTENSIONS, index: Int): String
    @GLES3 fun getInt(target: Int, index: Int): Int
    @GLES3 fun getLong(target: Int, index: Int): Long

    // Settings
    fun blendColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun blendEquation(mode: Int)
    fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int)
    fun blendFunc(sFactor: Int, dFactor: Int)
    fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int)
    fun clearColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun clearDepth(depth: Float)
    fun clearStencil(s: Int)
    fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean)
    fun cullFace(mode: Int)
    fun depthFunc(func: Int)
    fun depthMask(flag: Boolean)
    fun depthRange(zNear: Float, zFar: Float)
    fun hint(target: Int, mode: Int)
    fun frontFace(mode: Int)
    fun lineWidth(width: Float)
    fun pixelStore(pname: Int, param: Int)
    fun polygonOffset(factor: Float, units: Float)
    fun sampleCoverage(value: Float, invert: Boolean)
    fun scissor(x: Int, y: Int, width: Int, height: Int)
    fun stencilFunc(func: Int, ref: Int, mask: UInt)
    fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: UInt)
    fun stencilMask(mask: UInt)
    fun stencilMaskSeparate(face: Int, mask: UInt)
    fun stencilOp(fail: Int, zFail: Int, zPass: Int)
    fun stencilOpSeparate(face: Int, fail: Int, zFail: Int, zPass: Int)
    fun viewport(x: Int, y: Int, width: Int, height: Int)

    // Basic Functions
    fun clear(mask: UInt)
    fun drawArrays(mode: Int, first: Int, count: Int)
    fun drawElements(mode: Int, count: Int, type: Int, offset: Int)
    @GLES3 fun drawRangeElements(mode: Int, start: Int, end: Int, count: Int, type: Int, offset: Int)
    fun finish()
    fun flush()

    // Program
    fun isProgram(program: Int): Boolean
    fun createProgram(): GLProgram
    fun linkProgram(program: GLProgram)
    fun validateProgram(program: GLProgram)
    fun useProgram(program: GLProgram)
    fun deleteProgram(program: GLProgram)
    fun getProgramInt(program: GLProgram, pname: Int): Int
    fun getProgramInfoLog(program: GLProgram): String
    @GLES3 fun programParameter(program: GLProgram, pname: Int, value: Int)
    @GLES3 fun getFragDataLocation(program: GLProgram, name: String): Int
    @GLES3 fun getProgramBinary(program: GLProgram): ProgramBinary
    @GLES3 fun programBinary(program: GLProgram, binaryFormat: Int, binary: ByteArray)

    // Program + Attrib
    fun getActiveAttrib(program: GLProgram, index: Int): NameTypeSize
    fun getAttribLocation(program: GLProgram, name: String): GLAttributeLocation
    fun bindAttribLocation(program: GLProgram, index: GLAttributeLocation, name: String)

    // Program + Shader
    fun attachShader(program: GLProgram, shader: GLShader)
    fun detachShader(program: GLProgram, shader: GLShader)
    fun getAttachedShaders(program: GLProgram): GLShaderArray

    // Shader
    fun isShader(shader: Int): Boolean
    fun createShader(type: Int): GLShader
    fun shaderSource(shader: GLShader, string: String)
    fun compileShader(shader: GLShader)
    fun deleteShader(shader: GLShader)
    fun getShaderSource(shader: GLShader): String
    fun getShaderInt(shader: GLShader, pname: Int): Int
    fun getShaderInfoLog(shader: GLShader): String
    fun getShaderPrecisionFormat(shaderType: Int, precisionType: Int): ShaderPrecisionFormat
    fun releaseShaderCompiler()

    // Uniform
    fun getActiveUniform(program: GLProgram, index: Int): NameTypeSize
    fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation
    @GLES3 fun getUniformIndices(program: GLProgram, uniformNames: Array<String>, output: IntArray): IntArray
    @GLES3 fun getActiveUniforms(program: GLProgram, uniformIndices: IntArray, pname: Int, output: IntArray): IntArray
    fun getUniformFloat(program: GLProgram, location: GLUniformLocation, params: FloatArray, offset: Int = 0)
    fun getUniformInt(program: GLProgram, location: GLUniformLocation, params: IntArray, offset: Int = 0)
    @GLES3 fun getUniformUInt(program: GLProgram, location: GLUniformLocation, params: UIntArray, offset: Int = 0)
    fun uniform1f(location: GLUniformLocation, x: Float)
    fun uniform2f(location: GLUniformLocation, x: Float, y: Float)
    fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float)
    fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float)
    fun uniform1i(location: GLUniformLocation, x: Int)
    fun uniform2i(location: GLUniformLocation, x: Int, y: Int)
    fun uniform3i(location: GLUniformLocation, x: Int, y: Int, z: Int)
    fun uniform4i(location: GLUniformLocation, x: Int, y: Int, z: Int, w: Int)
    @GLES3 fun uniform1ui(location: GLUniformLocation, x: UInt)
    @GLES3 fun uniform2ui(location: GLUniformLocation, x: UInt, y: UInt)
    @GLES3 fun uniform3ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt)
    @GLES3 fun uniform4ui(location: GLUniformLocation, x: UInt, y: UInt, z: UInt, w: UInt)
    fun uniform1fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1)
    fun uniform2fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1)
    fun uniform3fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1)
    fun uniform4fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1)
    fun uniform1iv(location: GLUniformLocation, v: IntArray, offset: Int = 0, count: Int = 1)
    fun uniform2iv(location: GLUniformLocation, v: IntArray, offset: Int = 0, count: Int = 1)
    fun uniform3iv(location: GLUniformLocation, v: IntArray, offset: Int = 0, count: Int = 1)
    fun uniform4iv(location: GLUniformLocation, v: IntArray, offset: Int = 0, count: Int = 1)
    @GLES3 fun uniform1uiv(location: GLUniformLocation, v: UIntArray, offset: Int = 0, count: Int = 1)
    @GLES3 fun uniform2uiv(location: GLUniformLocation, v: UIntArray, offset: Int = 0, count: Int = 1)
    @GLES3 fun uniform3uiv(location: GLUniformLocation, v: UIntArray, offset: Int = 0, count: Int = 1)
    @GLES3 fun uniform4uiv(location: GLUniformLocation, v: UIntArray, offset: Int = 0, count: Int = 1)
    fun uniformMatrix2fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    fun uniformMatrix3fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    fun uniformMatrix4fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix2x3fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix3x2fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix2x4fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix4x2fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix3x4fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)
    @GLES3 fun uniformMatrix4x3fv(location: GLUniformLocation, v: FloatArray, offset: Int = 0, count: Int = 1, transpose: Boolean = false)

    // Uniform Blocks
    @GLES3 fun getUniformBlockIndex(program: GLProgram, uniformBlockName: String): Int
    @GLES3 fun getActiveUniformBlockInt(program: GLProgram, uniformBlockIndex: Int, pname: Int): Int
    @GLES3 fun getActiveUniformBlockIntArray(program: GLProgram, uniformBlockIndex: Int, pname: Int, output: IntArray): IntArray
    @GLES3 fun getActiveUniformBlockName(program: GLProgram, uniformBlockIndex: Int): String
    @GLES3 fun uniformBlockBinding(program: GLProgram, uniformBlockIndex: Int, uniformBlockBinding: Int)

    // Vertex Attributes
    fun enableVertexAttribArray(index: GLAttributeLocation)
    fun disableVertexAttribArray(index: GLAttributeLocation)
    fun getVertexAttribInt(index: GLAttributeLocation, pname: Int): Int
    fun getVertexAttribFloatArray(index: GLAttributeLocation, pname: Int, output: FloatArray): FloatArray
    @GLES3 fun getVertexAttribIntArray(index: GLAttributeLocation, pname: Int, output: IntArray): IntArray
    @GLES3 fun getVertexAttribUIntArray(index: GLAttributeLocation, pname: Int, output: UIntArray): UIntArray
    fun vertexAttribPointer(index: GLAttributeLocation, size: Int, type: Int, normalized: Boolean = false, stride: Int = 0, offset: Int = 0)
    @GLES3 fun vertexAttribIPointer(index: GLAttributeLocation, size: Int, type: Int, stride: Int = 0, offset: Int = 0)
    fun vertexAttrib(index: GLAttributeLocation, x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 1f)
    @GLES3 fun vertexAttrib(index: GLAttributeLocation, x: Int = 0, y: Int = 0, z: Int = 0, w: Int = 1)
    @GLES3 fun vertexAttrib(index: GLAttributeLocation, x: UInt = 0u, y: UInt = 0u, z: UInt = 0u, w: UInt = 1u)
    fun vertexAttrib1fv(index: GLAttributeLocation, values: FloatArray, offset: Int = 0)
    fun vertexAttrib2fv(index: GLAttributeLocation, values: FloatArray, offset: Int = 0)
    fun vertexAttrib3fv(index: GLAttributeLocation, values: FloatArray, offset: Int = 0)
    fun vertexAttrib4fv(index: GLAttributeLocation, values: FloatArray, offset: Int = 0)
    @GLES3 fun vertexAttrib4iv(index: GLAttributeLocation, v: IntArray, offset: Int = 0)
    @GLES3 fun vertexAttrib4uiv(index: GLAttributeLocation, v: UIntArray, offset: Int = 0)

    // Vertex Array Objects
    @GLES3OrExtension fun isVertexArrayGLES3(array: Int): Boolean
    @GLES3OrExtension fun genVertexArrayGLES3(): GLVertexArrayObject
    @GLES3OrExtension fun genVertexArraysGLES3(n: Int): GLVertexArrayObjectArray
    @GLES3OrExtension fun bindVertexArrayGLES3(array: GLVertexArrayObject)
    @GLES3OrExtension fun deleteVertexArrayGLES3(array: GLVertexArrayObject)
    @GLES3OrExtension fun deleteVertexArraysGLES3(arrays: GLVertexArrayObjectArray)

    // Buffers
    fun isBuffer(buffer: Int): Boolean
    fun genBuffer(): GLBuffer
    fun genBuffers(n: Int): GLBufferArray
    fun bindBuffer(target: Int, buffer: GLBuffer)
    fun deleteBuffer(buffer: GLBuffer)
    fun deleteBuffers(buffers: GLBufferArray)
    fun getBufferInt(target: Int, pname: Int): Int
    @GLES3 fun getBufferLong(target: Int, pname: Int): Long
    fun bufferData(target: Int, usage: Int, size: Int)
    fun bufferData(target: Int, usage: Int, data: Bufferable)
    fun bufferSubData(target: Int, offset: Int, data: Bufferable)
    fun bufferSubData(target: Int, offset: Int, data: Bufferable, srcOffset: Int, count: Int)
    @GLES3 fun copyBufferSubData(readTarget: Int, writeTarget: Int, readOffset: Int, writeOffset: Int, size: Int)
    @GLES3OrExtension fun drawBuffersGLES3(bufs: IntArray)
    @GLES3 fun readBuffer(mode: Int)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, value: IntArray)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, value: UIntArray)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, value: FloatArray)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, stencil: Int)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float)
    @GLES3 fun clearBuffer(buffer: Int, drawbuffer: Int, depth: Float, stencil: Int)
    @GLES3 fun bindBufferRange(target: Int, index: Int, buffer: GLBuffer, offset: Int, size: Int)
    @GLES3 fun bindBufferBase(target: Int, index: Int, buffer: GLBuffer)
    @GLES3 fun getBufferPointer(target: Int, pname: Int = GL.BUFFER_MAP_POINTER): Memory?
    @GLES3 fun mapBufferRange(target: Int, offset: Int, length: Int, access: Int): Memory?
    @GLES3 fun flushMappedBufferRange(target: Int, offset: Int, length: Int)
    @GLES3 fun unmapBuffer(target: Int): Boolean

    // Framebuffers
    fun isFramebuffer(framebuffer: Int): Boolean
    fun genFramebuffer(): GLFramebuffer
    fun genFramebuffers(n: Int): GLFramebufferArray
    fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer)
    fun deleteFramebuffer(framebuffer: GLFramebuffer)
    fun deleteFramebuffers(framebuffers: GLFramebufferArray)
    fun getFramebufferAttachmentInt(target: Int, attachment: Int, pname: Int): Int
    fun framebufferTexture2D(target: Int, attachment: Int, texTarget: Int, texture: GLTexture, level: Int = 0)
    fun checkFramebufferStatus(target: Int): Int
    fun framebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: GLRenderbuffer)
    fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Bufferable)
    @GLES3 fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, offset: Int)
    @GLES3 fun blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int,
                               dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: UInt, filter: Int)
    @GLES3 fun framebufferTextureLayer(target: Int, attachment: Int, texture: GLTexture, level: Int, layer: Int)
    @GLES3 fun invalidateFramebuffer(target: Int, attachments: IntArray)
    @GLES3 fun invalidateSubFramebuffer(target: Int, attachments: IntArray, x: Int, y: Int, width: Int, height: Int)

    // Renderbuffers
    fun isRenderbuffer(renderbuffer: Int): Boolean
    fun genRenderbuffer(): GLRenderbuffer
    fun genRenderbuffers(n: Int): GLRenderbufferArray
    fun bindRenderbuffer(target: Int, renderbuffer: GLRenderbuffer)
    fun deleteRenderbuffer(renderbuffer: GLRenderbuffer)
    fun deleteRenderbuffers(renderbuffers: GLRenderbufferArray)
    fun renderbufferStorage(target: Int, internalFormat: Int, width: Int, height: Int)
    fun getRenderbufferInt(target: Int, pname: Int): Int
    @GLES3 fun getInternalformatInt(target: Int, internalformat: Int, pname: Int): Int
    @GLES3 fun getInternalformatIntArray(target: Int, internalformat: Int, pname: Int, output: IntArray): IntArray
    @GLES3OrExtension fun renderbufferStorageMultisampleGLES3(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)

    // Textures
    fun isTexture(texture: Int): Boolean
    fun genTexture(): GLTexture
    fun genTextures(n: Int) : GLTextureArray
    fun activeTexture(texture: Int)
    fun bindTexture(target: Int, texture: GLTexture)
    fun deleteTexture(texture: GLTexture)
    fun deleteTextures(textures: GLTextureArray)
    fun texParameter(target: Int, pname: Int, param: Float)
    fun texParameter(target: Int, pname: Int, param: Int)
    fun getTexFloat(target: Int, pname: Int): Float
    fun getTexInt(target: Int, pname: Int): Int

    fun generateMipmap(target: Int)

    fun copyTexImage2D(target: Int, level: Int, internalFormat: Int, x: Int, y: Int,
                       width: Int, height: Int)
    fun copyTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                          x: Int, y: Int, width: Int, height: Int)

    fun texImage2D(target: Int, level: Int, internalFormat: Int,
                   width: Int, height: Int, format: Int, type: Int, pixels: Bufferable?)
    fun texSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                      width: Int, height: Int, format: Int, type: Int, pixels: Bufferable)
    fun compressedTexImage2D(target: Int, level: Int, internalFormat: Int,
                             width: Int, height: Int, data: Bufferable)
    fun compressedTexSubImage2D(target: Int, level: Int, xOffset: Int, yOffset: Int,
                                width: Int, height: Int, format: Int, data: Bufferable)

    @GLES3 fun texStorage2D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int)
    @GLES3 fun texStorage3D(target: Int, levels: Int, internalformat: Int, width: Int, height: Int, depth: Int)

    @GLES3 fun texImage3D(target: Int, level: Int, internalformat: Int,
                          width: Int, height: Int, depth: Int,
                          format: Int, type: Int)
    @GLES3 fun texImage3D(target: Int, level: Int, internalformat: Int,
                          width: Int, height: Int, depth: Int,
                          format: Int, type: Int, pixels: Bufferable?)
    @GLES3 fun texImage3D(target: Int, level: Int, internalformat: Int,
                          width: Int, height: Int, depth: Int,
                          format: Int, type: Int, offset: Int)

    @GLES3 fun texSubImage3D(target: Int, level: Int,
                             xoffset: Int, yoffset: Int, zoffset: Int,
                             width: Int, height: Int, depth: Int,
                             format: Int, type: Int, pixels: Bufferable)
    @GLES3 fun texSubImage3D(target: Int, level: Int,
                             xoffset: Int, yoffset: Int, zoffset: Int,
                             width: Int, height: Int, depth: Int,
                             format: Int, type: Int, offset: Int)

    @GLES3 fun copyTexSubImage3D(target: Int, level: Int,
                                 xoffset: Int, yoffset: Int, zoffset: Int,
                                 x: Int, y: Int, width: Int, height: Int)

    @GLES3 fun compressedTexImage3D(target: Int, level: Int, internalformat: Int,
                                    width: Int, height: Int, depth: Int, data: Bufferable)
    @GLES3 fun compressedTexImage3D(target: Int, level: Int, internalformat: Int,
                                    width: Int, height: Int, depth: Int,
                                    imageSize: Int, offset: Int)

    @GLES3 fun compressedTexSubImage3D(target: Int, level: Int,
                                       xoffset: Int, yoffset: Int, zoffset: Int,
                                       width: Int, height: Int, depth: Int,
                                       format: Int, data: Bufferable)
    @GLES3 fun compressedTexSubImage3D(target: Int, level: Int,
                                       xoffset: Int, yoffset: Int, zoffset: Int,
                                       width: Int, height: Int, depth: Int,
                                       format: Int, imageSize: Int, offset: Int)

    // Samplers
    @GLES3 fun isSampler(sampler: Int): Boolean
    @GLES3 fun genSampler(): GLSampler
    @GLES3 fun genSamplers(count: Int): GLSamplerArray
    @GLES3 fun bindSampler(unit: Int, sampler: GLSampler)
    @GLES3 fun deleteSampler(sampler: GLSampler)
    @GLES3 fun deleteSamplers(samplers: GLSamplerArray)
    @GLES3 fun samplerParameter(sampler: GLSampler, pname: Int, param: Float)
    @GLES3 fun samplerParameter(sampler: GLSampler, pname: Int, param: Int)
    @GLES3 fun getSamplerFloat(sampler: GLSampler, pname: Int): Float
    @GLES3 fun getSamplerInt(sampler: GLSampler, pname: Int): Int

    // Instanced Drawing
    @GLES3OrExtension fun vertexAttribDivisorGLES3(index: Int, divisor: Int)
    @GLES3OrExtension fun drawArraysInstancedGLES3(mode: Int, first: Int, count: Int, instanceCount: Int)
    @GLES3OrExtension fun drawElementsInstancedGLES3(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int)

    // Queries
    @GLES3 fun isQuery(id: Int): Boolean
    @GLES3 fun genQuery(): GLQuery
    @GLES3 fun genQueries(n: Int) : GLQueryArray
    @GLES3 fun deleteQuery(id: GLQuery)
    @GLES3 fun deleteQueries(ids: GLQueryArray)
    @GLES3 fun beginQuery(target: Int, id: GLQuery)
    @GLES3 fun endQuery(target: Int)
    @GLES3 fun getQueryInt(target: Int, pname: Int = GL.CURRENT_QUERY): Int
    @GLES3 fun getQueryObjectUInt(id: GLQuery, pname: Int): UInt

    // Syncs
    @GLES3 fun isSync(sync: Long): Boolean
    @GLES3 fun fenceSync(condition: Int = GL.SYNC_GPU_COMMANDS_COMPLETE, flags: Int = 0): GLSync
    @GLES3 fun deleteSync(sync: GLSync)
    @GLES3 fun clientWaitSync(sync: GLSync, flags: Int, timeout: Long): Int
    @GLES3 fun waitSync(sync: GLSync, flags: Int = 0, timeout: Long = GL.TIMEOUT_IGNORED)
    @GLES3 fun getSyncInt(sync: GLSync, pname: Int): Int

    // Transform Feedback
    @GLES3 fun isTransformFeedback(id: Int): Boolean
    @GLES3 fun genTransformFeedback(): GLTransformFeedback
    @GLES3 fun genTransformFeedbacks(n: Int): GLTransformFeedbackArray
    @GLES3 fun bindTransformFeedback(target: Int, id: GLTransformFeedback)
    @GLES3 fun deleteTransformFeedback(id: GLTransformFeedback)
    @GLES3 fun deleteTransformFeedbacks(ids: GLTransformFeedbackArray)
    @GLES3 fun beginTransformFeedback(primitiveMode: Int)
    @GLES3 fun endTransformFeedback()
    @GLES3 fun pauseTransformFeedback()
    @GLES3 fun resumeTransformFeedback()
    @GLES3 fun transformFeedbackVaryings(program: GLProgram, varyings: Array<String>, bufferMode: Int)
    @GLES3 fun getTransformFeedbackVarying(program: GLProgram, index: Int): NameTypeSize
}

inline fun GLContext.getBoolArray(name: Int, count: Int) = getBoolArray(name, BooleanArray(count))
inline fun GLContext.getIntArray(name: Int, count: Int) = getIntArray(name, IntArray(count))
inline fun GLContext.getFloatArray(name: Int, count: Int) = getFloatArray(name, FloatArray(count))
inline fun GLContext.getUInt(name: Int) = getInt(name).toUInt()
@OptIn(ExperimentalUnsignedTypes::class)
inline fun GLContext.getUIntArray(name: Int, output: UIntArray) = getIntArray(name, output.asIntArray()).asUIntArray()
@OptIn(ExperimentalUnsignedTypes::class)
inline fun GLContext.getUIntArray(name: Int, count: Int) = getIntArray(name, count).asUIntArray()
@GLES3
inline fun GLContext.getUniformIndices(program: GLProgram, uniformNames: Array<String>) = getUniformIndices(program, uniformNames, IntArray(uniformNames.size))
@GLES3
inline fun GLContext.getActiveUniforms(program: GLProgram, uniformIndices: IntArray, pname: Int) = getActiveUniforms(program, uniformIndices, pname, IntArray(uniformIndices.size))
@GLES3
inline fun GLContext.getActiveUniformBlockIntArray(program: GLProgram, uniformBlockIndex: Int, pname: Int, count: Int): IntArray = getActiveUniformBlockIntArray(program, uniformBlockIndex, pname, IntArray(count))
@GLES3
inline fun GLContext.getInternalformatIntArray(target: Int, internalformat: Int, pname: Int, count: Int) = getInternalformatIntArray(target, internalformat, pname, IntArray(count))

/**
 * The GLContext code that is not platform dependent
 */
abstract class GLContextBase {
    /**
     * Add a listener for all "GL" lifecycle events. The event methods area all
     * called from the renderer thread. Listeners that are already added are
     * safely ignored.
     */
    fun addListener(listener: GLListener) {
        listeners.add(listener)
        listenersList = listeners.toList()
    }
    /** Remove a listener. Does nothing if not registered. */
    fun removeListener(listener: GLListener) {
        listeners.remove(listener)
        listenersList = listeners.toList()
    }
    /** Check if a listener is registered. */
    fun hasListener(listener: GLListener) { listener in listeners }

    private val listeners = mutableSetOf<GLListener>()
    private var listenersList = emptyList<GLListener>()
    protected fun clearListeners() {
        listeners.clear()
        listenersList = emptyList()
    }

    private var lastEvent = ""
    protected var rendering = false
    protected var lastWidth = -1
    protected var lastHeight = -1
    private var lastSetWidth = -1
    private var lastSetHeight = -1
    protected fun onCreate() {
        if (lastEvent == "" || lastEvent == "dispose") {
            lastEvent = "create"
            listenersList.forEach { it.create(this as GLContext) }
        } else {
            if (rendering) { onPause() }
            lastEvent = "recreate"
            listenersList.forEach { it.recreate(this as GLContext) }
        }
    }
    protected fun onResize(width: Int, height: Int) {
        if (lastEvent == "") { return }
        lastHeight = height
        lastWidth = width
        if (lastEvent == "create" || lastEvent == "recreate" || lastEvent == "pause") {
            // defer these until resume() is called
        } else if (width != lastSetWidth || height != lastSetHeight) {
            //assert(lastEvent != "dispose")
            onResizeForce()
        }
    }
    private fun onResizeForce() {
        lastSetWidth = lastWidth
        lastSetHeight = lastHeight
        lastEvent = "resize"
        listenersList.forEach { it.resize(this as GLContext, lastSetWidth, lastSetHeight) }
    }
    protected fun onResume() {
        if (lastEvent == "resume" || lastEvent == "") { return }
        if (lastEvent == "create" || lastEvent == "recreate" || (lastEvent == "pause" && (lastWidth != lastSetWidth || lastHeight != lastSetHeight))) {
            onResizeForce()
        }
        //assert(lastEvent == "resize" || lastEvent == "pause")
        lastEvent = "resume"
        rendering = true
        listenersList.forEach { it.resume(this as GLContext) }
    }
    protected fun onRender(time: Long) {
        // Sometimes onResume() will never be called (at least not at the right time)
        // But we need to be at least created
        if (lastEvent == "create" || lastEvent == "recreate" || lastEvent == "pause") { onResume() }
        if (!rendering) { return } // ignore requests to render before we are ready
        lastEvent = "render"
        listenersList.forEach { it.render(this as GLContext, time) }
    }
    protected fun onPause() {
        //assert(rendering)
        if (lastEvent == "pause") { return }
        lastEvent = "pause"
        rendering = false
        listenersList.forEach { it.pause(this as GLContext) }
    }
    protected fun onDispose() {
        if (lastEvent == "dispose") { return }
        if (rendering) { onPause() }
        lastEvent = "dispose"
        listenersList.forEach { it.dispose(this as GLContext) }
    }
}
