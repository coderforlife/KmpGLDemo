@file:Suppress("NOTHING_TO_INLINE", "unused")

package edu.moravian.kmpgl.core

import edu.moravian.kmpgl.math.BoolVector2
import edu.moravian.kmpgl.math.BoolVector3
import edu.moravian.kmpgl.math.BoolVector4
import edu.moravian.kmpgl.math.Color3
import edu.moravian.kmpgl.math.Color4
import edu.moravian.kmpgl.math.Euler
import edu.moravian.kmpgl.math.IntVector2
import edu.moravian.kmpgl.math.IntVector3
import edu.moravian.kmpgl.math.IntVector4
import edu.moravian.kmpgl.math.Matrix2
import edu.moravian.kmpgl.math.Matrix3
import edu.moravian.kmpgl.math.Matrix4
import edu.moravian.kmpgl.math.Quaternion
import edu.moravian.kmpgl.math.UIntVector2
import edu.moravian.kmpgl.math.UIntVector3
import edu.moravian.kmpgl.math.UIntVector4
import edu.moravian.kmpgl.math.Vector2
import edu.moravian.kmpgl.math.Vector3
import edu.moravian.kmpgl.math.Vector4
import edu.moravian.kmpgl.util.Bufferable
import edu.moravian.kmpgl.util.asBufferable

////////// Extension Function Wrappers //////////
inline val GLContext.hasFragmentShaderDerivatives get() = version >= 30 || extensions[OES_standard_derivatives::class] !== null

// Instanced Arrays
@OptIn(GLES3OrExtension::class)
inline val GLContext.hasInstancedArrays get() = version >= 30 || extensions[InstancedArraysExtension::class] !== null
@GLES3OrExtension
inline fun GLContext.drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) =
    if (version >= 30) drawArraysInstancedGLES3(mode, first, count, instanceCount)
    else extensions[InstancedArraysExtension::class]!!.drawArraysInstanced(mode, first, count, instanceCount)
@GLES3OrExtension
inline fun GLContext.drawElementsInstanced(mode: Int, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) =
    if (version >= 30) drawElementsInstancedGLES3(mode, count, type, indicesOffset, instanceCount)
    else extensions[InstancedArraysExtension::class]!!.drawElementsInstanced(mode, count, type, indicesOffset, instanceCount)
@GLES3OrExtension
inline fun GLContext.vertexAttribDivisor(index: Int, divisor: Int) =
    if (version >= 30) vertexAttribDivisorGLES3(index, divisor)
    else extensions[InstancedArraysExtension::class]!!.vertexAttribDivisor(index, divisor)

// Vertex Array Objects
@OptIn(GLES3OrExtension::class)
inline val GLContext.hasVertexArrayObjects get() = version >= 30 || extensions[OES_vertex_array_object::class] !== null
@GLES3OrExtension
inline fun GLContext.isVertexArray(array: Int) =
    if (version >= 30) isVertexArrayGLES3(array)
    else extensions[OES_vertex_array_object::class]!!.isVertexArrayOES(array)
@GLES3OrExtension
inline fun GLContext.genVertexArray() =
    if (version >= 30) genVertexArrayGLES3()
    else extensions[OES_vertex_array_object::class]!!.genVertexArrayOES()
@GLES3OrExtension
inline fun GLContext.genVertexArrays(n: Int) =
    if (version >= 30) genVertexArraysGLES3(n)
    else extensions[OES_vertex_array_object::class]!!.genVertexArraysOES(n)
@GLES3OrExtension
inline fun GLContext.deleteVertexArray(array: GLVertexArrayObject) =
    if (version >= 30) deleteVertexArrayGLES3(array) else extensions[OES_vertex_array_object::class]!!.deleteVertexArrayOES(array)
@GLES3OrExtension
inline fun GLContext.deleteVertexArrays(arrays: GLVertexArrayObjectArray) =
    if (version >= 30) deleteVertexArraysGLES3(arrays)
    else extensions[OES_vertex_array_object::class]!!.deleteVertexArraysOES(arrays)
@GLES3OrExtension
inline fun GLContext.bindVertexArray(array: GLVertexArrayObject) =
    if (version >= 30) bindVertexArrayGLES3(array)
    else extensions[OES_vertex_array_object::class]!!.bindVertexArrayOES(array)

// Draw Buffers
@GLES3OrExtension
inline val GLContext.hasDrawBuffers get() = version >= 30 || extensions[DrawBuffersExtension::class] !== null
@GLES3OrExtension
inline fun GLContext.drawBuffers(bufs: IntArray) =
    if (version >= 30) drawBuffersGLES3(bufs)
    else extensions[DrawBuffersExtension::class]!!.drawBuffers(bufs)

// Framebuffer Multisample
@GLES3OrExtension
inline val GLContext.hasFramebufferMultisample get() = version >= 30 || extensions[FramebufferMultisampleExtension::class] !== null
@GLES3OrExtension
inline fun GLContext.renderbufferStorageMultisample(target: Int, samples: Int, internalFormat: Int, width: Int, height: Int) =
    if (version >= 30) renderbufferStorageMultisampleGLES3(target, samples, internalFormat, width, height)
    else extensions[FramebufferMultisampleExtension::class]!!.renderbufferStorageMultisample(target, samples, internalFormat, width, height)


////////// Uniform Wrappers //////////
inline fun GLContext.uniform(uniform: GLUniformLocation, s: Float) = this.uniform1f(uniform, s)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: Vector2) = this.uniform2f(uniform, v.x, v.y)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: Vector3) = this.uniform3f(uniform, v.x, v.y, v.z)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: Vector4) = this.uniform4f(uniform, v.x, v.y, v.z, v.w)
inline fun GLContext.uniform(uniform: GLUniformLocation, q: Quaternion) = this.uniform4f(uniform, q.x, q.y, q.z, q.w)
inline fun GLContext.uniform(uniform: GLUniformLocation, e: Euler) = this.uniform3f(uniform, e.x, e.y, e.z)
inline fun GLContext.uniform(uniform: GLUniformLocation, c: Color3) = this.uniform3f(uniform, c.r, c.g, c.b)
inline fun GLContext.uniform(uniform: GLUniformLocation, c: Color4) = this.uniform4f(uniform, c.r, c.g, c.b, c.a)
inline fun GLContext.uniform(uniform: GLUniformLocation, s: Int) = this.uniform1i(uniform, s)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: IntVector2) = this.uniform2i(uniform, v.x, v.y)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: IntVector3) = this.uniform3i(uniform, v.x, v.y, v.z)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: IntVector4) = this.uniform4i(uniform, v.x, v.y, v.z, v.w)
@GLES3 inline fun GLContext.uniform(uniform: GLUniformLocation, s: UInt) = this.uniform1ui(uniform, s)
@GLES3 inline fun GLContext.uniform(uniform: GLUniformLocation, v: UIntVector2) = this.uniform2ui(uniform, v.x, v.y)
@GLES3 inline fun GLContext.uniform(uniform: GLUniformLocation, v: UIntVector3) = this.uniform3ui(uniform, v.x, v.y, v.z)
@GLES3 inline fun GLContext.uniform(uniform: GLUniformLocation, v: UIntVector4) = this.uniform4ui(uniform, v.x, v.y, v.z, v.w)
inline fun GLContext.uniform(uniform: GLUniformLocation, s: Boolean) = this.uniform1i(uniform, if (s) 1 else 0)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: BoolVector2) = this.uniform2i(uniform, if (v.x) 1 else 0, if (v.y) 1 else 0)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: BoolVector3) = this.uniform3i(uniform, if (v.x) 1 else 0, if (v.y) 1 else 0, if (v.z) 1 else 0)
inline fun GLContext.uniform(uniform: GLUniformLocation, v: BoolVector4) = this.uniform4i(uniform, if (v.x) 1 else 0, if (v.y) 1 else 0, if (v.z) 1 else 0, if (v.w) 1 else 0)
inline fun GLContext.uniform(uniform: GLUniformLocation, m: Matrix2) = this.uniformMatrix2fv(uniform, m.elements)
inline fun GLContext.uniform(uniform: GLUniformLocation, m: Matrix3) = this.uniformMatrix3fv(uniform, m.elements)
inline fun GLContext.uniform(uniform: GLUniformLocation, m: Matrix4) = this.uniformMatrix4fv(uniform, m.elements)
// TODO: matrices: 2x3, 3x2, 2x4, 4x2, 3x4, 4x3

////////// Vertex Attrib Wrappers //////////
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: Vector2) = this.vertexAttrib(index, v.x, v.y, 0f, 1f)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: Vector3) = this.vertexAttrib(index, v.x, v.y, v.x, 1f)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: Vector4) = this.vertexAttrib(index, v.x, v.y, v.x, v.z)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, q: Quaternion) = this.vertexAttrib(index, q.x, q.y, q.x, q.z)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, e: Euler) = this.vertexAttrib(index, e.x, e.y, e.x, 1f)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, c: Color3) = this.vertexAttrib(index, c.r, c.g, c.b, 1f)
inline fun GLContext.vertexAttrib(index: GLAttributeLocation, c: Color4) = this.vertexAttrib(index, c.r, c.g, c.b, c.a)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: IntVector2) = this.vertexAttrib(index, v.x, v.y, 0, 1)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: IntVector3) = this.vertexAttrib(index, v.x, v.y, v.x, 1)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: IntVector4) = this.vertexAttrib(index, v.x, v.y, v.x, v.z)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, s: Boolean) = this.vertexAttrib(index, if (s) 1 else 0, 0, 0, 1)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: BoolVector2) = this.vertexAttrib(index, if (v.x) 1 else 0, if (v.y) 1 else 0, 0, 1)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: BoolVector3) = this.vertexAttrib(index, if (v.x) 1 else 0, if (v.y) 1 else 0, if (v.z) 1 else 0, 1)
@GLES3 inline fun GLContext.vertexAttrib(index: GLAttributeLocation, v: BoolVector4) = this.vertexAttrib(index, if (v.x) 1 else 0, if (v.y) 1 else 0, if (v.z) 1 else 0, if (v.w) 1 else 0)
inline fun GLContext.currentVertexAttrib(loc: GLAttributeLocation, array: FloatArray = FloatArray(4)) = getVertexAttribFloatArray(loc, GL.CURRENT_VERTEX_ATTRIB, array)
@GLES3 inline fun GLContext.currentVertexAttrib(loc: GLAttributeLocation, array: IntArray = IntArray(4)) = getVertexAttribIntArray(loc, GL.CURRENT_VERTEX_ATTRIB, array)
@OptIn(ExperimentalUnsignedTypes::class)
@GLES3 inline fun GLContext.currentVertexAttrib(loc: GLAttributeLocation, array: UIntArray = UIntArray(4)) = getVertexAttribUIntArray(loc, GL.CURRENT_VERTEX_ATTRIB, array)

////////// Function Wrappers taking Enum Values //////////
inline fun GLContext.isEnabled(cap: Capability) = isEnabled(cap.value)
inline fun GLContext.enable(cap: Capability) = enable(cap.value)
inline fun GLContext.disable(cap: Capability) = disable(cap.value)

inline fun GLContext.getString(pname: ParamString) = getString(pname.value)
inline fun GLContext.getBool(pname: ParamBool) = getBool(pname.value)
inline fun GLContext.getInt(pname: ParamInt) = getInt(pname.value)
inline fun GLContext.getUInt(pname: ParamUInt) = getUInt(pname.value)
@GLES3 inline fun GLContext.getLong(pname: ParamLong) = getLong(pname.value)
inline fun GLContext.getFloat(pname: ParamFloat) = getFloat(pname.value)
inline fun GLContext.getBoolArray(pname: ParamBools, array: BooleanArray = BooleanArray(pname.count)) = getBoolArray(pname.value, array)
inline fun GLContext.getIntArray(pname: ParamInts, array: IntArray = IntArray(pname.count)) = getIntArray(pname.value, array)
@OptIn(ExperimentalUnsignedTypes::class)
inline fun GLContext.getUIntArray(pname: ParamUInts, array: UIntArray = UIntArray(getInt(pname.countField))) = getUIntArray(pname.value, array)
inline fun GLContext.getFloatArray(pname: ParamFloats, array: FloatArray = FloatArray(pname.count)) = getFloatArray(pname.value, array)
inline fun GLContext.getColor(pname: ParamColor) = Color4(getFloatArray(pname.value, 4))

inline val GLContext.currentProgram get() = GLProgram(getInt(GL.CURRENT_PROGRAM))
inline fun GLContext.getFramebufferBinding(target: FramebufferBinding) = GLFramebuffer(getInt(target.value))
inline fun GLContext.getTextureBinding(target: TextureBinding) = GLTexture(getInt(target.value))
inline fun GLContext.getBufferBinding(target: BufferBinding) = GLBuffer(getInt(target.value))

inline fun GLContext.hint(target: HintTarget, mode: HintMode) = hint(target.value, mode.value)
inline fun GLContext.clearColor(color: Color4) = clearColor(color.r, color.g, color.b, color.a)
inline fun GLContext.blendColor(color: Color4) = blendColor(color.r, color.g, color.b, color.a)
inline fun GLContext.blendEquation(mode: BlendEq) = blendEquation(mode.value)
inline fun GLContext.blendEquationSeparate(modeRGB: BlendEq, modeAlpha: BlendEq) = blendEquationSeparate(modeRGB.value, modeAlpha.value)
inline fun GLContext.blendFunc(sFactor: BlendFunc, dFactor: BlendFunc) = blendFunc(sFactor.value, dFactor.value)
inline fun GLContext.blendFuncSeparate(srcRGB: BlendFunc, dstRGB: BlendFunc, srcAlpha: BlendFunc, dstAlpha: BlendFunc) = blendFuncSeparate(srcRGB.value, dstRGB.value, srcAlpha.value, dstAlpha.value)
inline fun GLContext.cullFace(mode: Face) = cullFace(mode.value)
inline fun GLContext.depthFunc(func: ComparisonFunc) = depthFunc(func.value)
inline fun GLContext.frontFace(mode: FaceDir) = frontFace(mode.value)
inline fun GLContext.pixelStore(pname: PixelStoreParam, param: Int) = pixelStore(pname.value, param)
inline fun GLContext.stencilFunc(func: ComparisonFunc, ref: Int, mask: UInt) = stencilFunc(func.value, ref, mask)
inline fun GLContext.stencilFuncSeparate(face: Face, func: ComparisonFunc, ref: Int, mask: UInt) = stencilFuncSeparate(face.value, func.value, ref, mask)
inline fun GLContext.stencilMaskSeparate(face: Face, mask: UInt) = stencilMaskSeparate(face.value, mask)
inline fun GLContext.stencilOp(fail: StencilOp, zFail: StencilOp, zPass: StencilOp) = stencilOp(fail.value, zFail.value, zPass.value)
inline fun GLContext.stencilOpSeparate(face: Face, fail: StencilOp, zFail: StencilOp, zPass: StencilOp) = stencilOpSeparate(face.value, fail.value, zFail.value, zPass.value)

inline fun GLContext.clear(mask: BufferBit) = clear(mask.value)
inline fun GLContext.clear(mask: BufferBits) = clear(mask.value)

inline fun GLContext.drawArrays(mode: DrawMode, first: Int, count: Int) = drawArrays(mode.value, first, count)
inline fun GLContext.drawElements(mode: DrawMode, count: Int, type: ElementType, offset: Int) = drawElements(mode.value, count, type.value, offset)
@GLES3OrExtension inline fun GLContext.drawArraysInstanced(mode: DrawMode, first: Int, count: Int, instanceCount: Int) = drawArraysInstanced(mode.value, first, count, instanceCount)
@GLES3OrExtension inline fun GLContext.drawElementsInstanced(mode: DrawMode, count: Int, type: Int, indicesOffset: Int, instanceCount: Int) = drawElementsInstanced(mode.value, count, type, indicesOffset, instanceCount)
@GLES3 inline fun GLContext.drawRangeElements(mode: DrawMode, start: Int, end: Int, count: Int, type: ElementType, offset: Int) = drawRangeElements(mode.value, start, end, count, type.value, offset)

inline fun GLContext.getProgramParam(program: GLProgram, pname: ProgramParam) = getProgramInt(program, pname.value)

inline fun GLContext.createShader(type: ShaderType) = createShader(type.value)
inline fun GLContext.getShaderParam(shader: GLShader, pname: ShaderParam) = getShaderInt(shader, pname.value)
inline fun GLContext.getShaderPrecisionFormat(shaderType: ShaderType, precisionType: PrecisionType) = getShaderPrecisionFormat(shaderType.value, precisionType.value)

@GLES3 inline fun GLContext.getActiveUniform(program: GLProgram, uniformIndex: Int, pname: UniformParam) = getActiveUniforms(program, intArrayOf(uniformIndex), pname.value)[0]
@GLES3 inline fun GLContext.getActiveUniforms(program: GLProgram, uniformIndices: IntArray, pname: UniformParam) = getActiveUniforms(program, uniformIndices, pname.value)
@GLES3 inline fun GLContext.getActiveUniformBlockParam(program: GLProgram, uniformBlockIndex: Int, pname: UniformBlockParam) = getActiveUniformBlockInt(program, uniformBlockIndex, pname.value)
@GLES3 inline fun GLContext.getActiveUniformIndices(program: GLProgram, uniformBlockIndex: Int) =
    getActiveUniformBlockIntArray(program, uniformBlockIndex, GL.UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, getActiveUniformBlockInt(program, uniformBlockIndex, GL.UNIFORM_BLOCK_ACTIVE_UNIFORMS))

inline fun GLContext.getVertexAttribParam(index: GLAttributeLocation, pname: VertexAttribParam) = getVertexAttribInt(index, pname.value)
inline fun GLContext.vertexAttribPointer(index: GLAttributeLocation, size: Int, type: DataType, normalized: Boolean = false, stride: Int = 0, offset: Int = 0) = vertexAttribPointer(index, size, type.value, normalized, stride, offset)
@GLES3 inline fun GLContext.vertexAttribIPointer(index: GLAttributeLocation, size: Int, type: IntDataType, stride: Int = 0, offset: Int = 0) = vertexAttribIPointer(index, size, type.value, stride, offset)

inline fun GLContext.bindBuffer(target: BufferTarget, buffer: GLBuffer) = bindBuffer(target.value, buffer)
inline fun GLContext.getBufferParam(target: BufferTarget, pname: BufferParam) = getBufferInt(target.value, pname.value)
@GLES3 inline fun GLContext.getBufferParam(target: BufferTarget, pname: BufferParamLong) = getBufferLong(target.value, pname.value)
inline fun GLContext.bufferData(target: BufferTarget, usage: BufferUsage, size: Int) = bufferData(target.value, usage.value, size)
inline fun GLContext.bufferData(target: BufferTarget, usage: BufferUsage, data: Bufferable) = bufferData(target.value, usage.value, data)
inline fun GLContext.bufferSubData(target: BufferTarget, offset: Int, data: Bufferable) = bufferSubData(target.value, offset, data)
inline fun GLContext.bufferSubData(target: BufferTarget, offset: Int, data: Bufferable, srcOffset: Int, count: Int) = bufferSubData(target.value, offset, data, srcOffset, count)
@GLES3 inline fun GLContext.copyBufferSubData(readTarget: BufferTarget, writeTarget: BufferTarget, readOffset: Int, writeOffset: Int, size: Int) = copyBufferSubData(readTarget.value, writeTarget.value, readOffset, writeOffset, size)
// TODO: @GLES3 inline fun GLContext.drawBuffers(bufs: IntArray -> BufferColorTarget) // TODO: support extensions automatically
@GLES3 inline fun GLContext.readBuffer(source: BufferColorTarget) = readBuffer(source.value)
@GLES3 inline fun GLContext.bindBufferRange(target: BindBufferTarget, index: Int, buffer: GLBuffer, offset: Int, size: Int) = bindBufferRange(target.value, index, buffer, offset, size)
@GLES3 inline fun GLContext.bindBufferBase(target: BindBufferTarget, index: Int, buffer: GLBuffer) = bindBufferBase(target.value, index, buffer)
@GLES3 inline fun GLContext.mapBufferRange(target: BindBufferTarget, offset: Int, length: Int, access: MapAccessFlag) = mapBufferRange(target.value, offset, length, access.value)
@GLES3 inline fun GLContext.mapBufferRange(target: BindBufferTarget, offset: Int, length: Int, access: MapAccessFlags) = mapBufferRange(target.value, offset, length, access.value)
@GLES3 inline fun GLContext.flushMappedBufferRange(target: BindBufferTarget, offset: Int, length: Int) = flushMappedBufferRange(target.value, offset, length)
@GLES3 inline fun GLContext.unmapBuffer(target: BindBufferTarget) = unmapBuffer(target.value)

inline fun GLContext.bindFramebuffer(target: FramebufferTarget, framebuffer: GLFramebuffer) = bindFramebuffer(target.value, framebuffer)
inline fun GLContext.getFramebufferAttachmentParam(target: FramebufferTarget, attachment: FramebufferAttachment, pname: FramebufferAttachmentParam) = getFramebufferAttachmentInt(target.value, attachment.value, pname.value)
inline fun GLContext.framebufferTexture2D(target: FramebufferTarget, attachment: FramebufferAttachment, texTarget: TextureTarget2D, texture: GLTexture, level: Int = 0) = framebufferTexture2D(target.value, attachment.value, texTarget.value, texture, level)
inline fun GLContext.checkFramebufferStatus(target: FramebufferTarget) = FramebufferStatus.from(checkFramebufferStatus(target.value))
inline fun GLContext.framebufferRenderbuffer(target: FramebufferTarget, attachment: FramebufferAttachment, renderbufferTarget: RenderbufferTarget, renderbuffer: GLRenderbuffer) = framebufferRenderbuffer(target.value, attachment.value, renderbufferTarget.value, renderbuffer)
inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, type: PixelType, pixels: Bufferable) = readPixels(x, y, width, height, format.value, type.value, pixels)
@GLES3 inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, type: PixelType, offset: Int) = readPixels(x, y, width, height, format.value, type.value, offset)
@GLES3 inline fun GLContext.blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: BufferBit, filter: MagFilter) = blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask.value, filter.value)
@GLES3 inline fun GLContext.blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: BufferBits, filter: MagFilter) = blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask.value, filter.value)
@GLES3 inline fun GLContext.framebufferTextureLayer(target: FramebufferTarget, attachment: FramebufferAttachment, texture: GLTexture, level: Int, layer: Int) = framebufferTextureLayer(target.value, attachment.value, texture, level, layer)
// TODO: @GLES3 inline fun GLContext.invalidateFramebuffer(target: FramebufferTarget, attachments: IntArray -> FramebufferAttachment) = invalidateFramebuffer(target.value, attachments)
// TODO: @GLES3 inline fun GLContext.invalidateSubFramebuffer(target: FramebufferTarget, attachments: IntArray -> FramebufferAttachment, x: Int, y: Int, width: Int, height: Int) = invalidateFramebuffer(target.value, attachments, x, y, width, height)

inline fun GLContext.bindRenderbuffer(renderbuffer: GLRenderbuffer) = bindRenderbuffer(GL.RENDERBUFFER, renderbuffer)
inline fun GLContext.renderbufferStorage(internalFormat: RenderbufferInternalFormat, width: Int, height: Int) = renderbufferStorage(GL.RENDERBUFFER, internalFormat.value, width, height)
inline fun GLContext.getRenderbufferInt(pname: RenderbufferParam) = getRenderbufferInt(GL.RENDERBUFFER, pname.value)
@GLES3 inline fun GLContext.getInternalformatSamples(internalFormat: RenderbufferInternalFormat) = getInternalformatIntArray(GL.RENDERBUFFER, internalFormat.value, GL.SAMPLES, getInternalformatInt(GL.RENDERBUFFER, internalFormat.value, GL.NUM_SAMPLE_COUNTS))
@GLES3OrExtension inline fun GLContext.renderbufferStorageMultisample(samples: Int, internalFormat: RenderbufferInternalFormat, width: Int, height: Int) = renderbufferStorageMultisample(GL.RENDERBUFFER, samples, internalFormat.value, width, height)

inline fun GLContext.activeTexture(texture: TextureUnit) = activeTexture(texture.value)
inline fun GLContext.bindTexture(target: TextureTarget, texture: GLTexture) = bindTexture(target.value, texture)
inline fun GLContext.texParameter(target: TextureTarget, pname: TextureFloatParam, param: Float) = texParameter(target.value, pname.value, param)
inline fun GLContext.texParameter(target: TextureTarget, pname: TextureIntParam, param: Int) = texParameter(target.value, pname.value, param)
inline fun GLContext.getTexParam(target: TextureTarget, pname: TextureFloatParam): Float = getTexFloat(target.value, pname.value)
inline fun GLContext.getTexParam(target: TextureTarget, pname: TextureIntParam): Int = getTexInt(target.value, pname.value)
inline fun GLContext.generateMipmap(target: TextureTarget) = generateMipmap(target.value)
@GLES3 inline fun GLContext.bindSampler(unit: TextureUnit, sampler: GLSampler) = bindSampler(unit.value, sampler)
@GLES3 inline fun GLContext.samplerParameter(sampler: GLSampler, pname: SamplerIntParam, param: Int) = samplerParameter(sampler, pname.value, param)
@GLES3 inline fun GLContext.samplerParameter(sampler: GLSampler, pname: SamplerFloatParam, param: Float) = samplerParameter(sampler, pname.value, param)
@GLES3 inline fun GLContext.getSamplerParam(sampler: GLSampler, pname: SamplerIntParam) = getSamplerInt(sampler, pname.value)
@GLES3 inline fun GLContext.getSamplerParam(sampler: GLSampler, pname: SamplerFloatParam) = getSamplerFloat(sampler, pname.value)

@GLES3 inline fun GLContext.beginQuery(target: QueryTarget, id: GLQuery) = beginQuery(target.value, id)
@GLES3 inline fun GLContext.endQuery(target: QueryTarget) = endQuery(target.value)
@GLES3 inline fun GLContext.getQuery(target: QueryTarget) = GLQuery(getQueryInt(target.value, GL.CURRENT_QUERY))
@GLES3 inline fun GLContext.getQueryParam(id: GLQuery, pname: QueryParam) = getQueryObjectUInt(id, pname.value)

@GLES3 inline fun GLContext.clientWaitSync(sync: GLSync, flags: SyncWaitFlag, timeout: Long) = SyncWaitStatus.from(clientWaitSync(sync, flags.value, timeout))
@GLES3 inline fun GLContext.clientWaitSync(sync: GLSync, flags: SyncWaitFlags, timeout: Long) = SyncWaitStatus.from(clientWaitSync(sync, flags.value, timeout))
@GLES3 inline fun GLContext.getSyncParam(sync: GLSync, pname: SyncParam) = getSyncInt(sync, pname.value)

@GLES3 inline fun GLContext.bindTransformFeedback(id: GLTransformFeedback) = bindTransformFeedback(GL.TRANSFORM_FEEDBACK, id)
@GLES3 inline fun GLContext.beginTransformFeedback(primitiveMode: PrimitiveMode) = beginTransformFeedback(primitiveMode.value)
@GLES3 inline fun GLContext.transformFeedbackVaryings(program: GLProgram, varyings: Array<String>, bufferMode: TransformFeedbackMode) = transformFeedbackVaryings(program, varyings, bufferMode.value)

////////// Other Wrappers //////////
@GLES3 inline fun GLContext.clearBuffer(drawbuffer: Int, color: Color4) = clearBuffer(GL.COLOR, drawbuffer, color.toArray())
@GLES3 inline fun GLContext.clearBufferInt(drawbuffer: Int, color: Color4) = clearBuffer(GL.COLOR, drawbuffer, color.toIntVector().toArray())
@OptIn(ExperimentalUnsignedTypes::class)
@GLES3 inline fun GLContext.clearBufferUInt(drawbuffer: Int, color: Color4) = clearBuffer(GL.COLOR, drawbuffer, color.toUIntVector().toArray())
@GLES3 inline fun GLContext.clearBuffer(stencil: Int) = clearBuffer(GL.STENCIL, 0, stencil)
@GLES3 inline fun GLContext.clearBuffer(depth: Float) = clearBuffer(GL.DEPTH, 0, depth)
@GLES3 inline fun GLContext.clearBuffer(depth: Float, stencil: Int) = clearBuffer(GL.DEPTH_STENCIL, 0, depth, stencil)

@OptIn(ExperimentalUnsignedTypes::class)
inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, pixels: UByteArray) = readPixels(x, y, width, height, format.value, GL.UNSIGNED_BYTE, pixels.asBufferable())
@GLES3 inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, pixels: ByteArray) = readPixels(x, y, width, height, format.value, GL.BYTE, pixels.asBufferable())
@OptIn(ExperimentalUnsignedTypes::class)
@GLES3 inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, pixels: UIntArray) = readPixels(x, y, width, height, format.value, GL.UNSIGNED_INT, pixels.asBufferable())
@GLES3 inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, pixels: IntArray) = readPixels(x, y, width, height, format.value, GL.INT, pixels.asBufferable())
inline fun GLContext.readPixels(x: Int, y: Int, width: Int, height: Int, format: PixelFormat, pixels: FloatArray) = readPixels(x, y, width, height, format.value, GL.FLOAT, pixels.asBufferable())
