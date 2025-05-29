package edu.moravian.kmpgl.compose

import edu.moravian.kmpgl.core.BufferTarget
import edu.moravian.kmpgl.core.DataType
import edu.moravian.kmpgl.core.DrawMode
import edu.moravian.kmpgl.core.ElementType
import edu.moravian.kmpgl.core.GL
import edu.moravian.kmpgl.core.GLAttributeLocation
import edu.moravian.kmpgl.core.GLBuffer
import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLListener
import edu.moravian.kmpgl.core.GLProgram
import edu.moravian.kmpgl.core.GLVertexArrayObject
import edu.moravian.kmpgl.core.IntDataType
import edu.moravian.kmpgl.core.bindVertexArray
import edu.moravian.kmpgl.core.currentProgram
import edu.moravian.kmpgl.core.deleteVertexArray
import edu.moravian.kmpgl.core.drawElements
import edu.moravian.kmpgl.core.genVertexArray
import edu.moravian.kmpgl.core.isNull
import edu.moravian.kmpgl.core.isSet
import edu.moravian.kmpgl.core.isVertexArray
import edu.moravian.kmpgl.math.Box3
import edu.moravian.kmpgl.math.Sphere
import edu.moravian.kmpgl.math.Vector3
import edu.moravian.kmpgl.util.Indices
import edu.moravian.kmpgl.util.IntView
import edu.moravian.kmpgl.util.Vector3View
import edu.moravian.kmpgl.util.asBufferable
import kotlin.math.sqrt

/** Create a Geometry from a float array of positions */
fun Geometry(positions: FloatArray, mode: DrawMode = DrawMode.TRIANGLES): Geometry = GeometryWithVAO(
    mapOf("position" to BufferAttribute(BufferData(positions.asBufferable()), 3, DataType.FLOAT)), mode,
)
/** Create a Geometry from a float array of positions and index array of (unsigned) shorts */
fun Geometry(positions: FloatArray, indices: ShortArray, mode: DrawMode = DrawMode.TRIANGLES): Geometry =
    GeometryWithVAOAndIBO(
        BufferData(indices.asBufferable(), target = BufferTarget.ELEMENT_ARRAY),
        attributes = mapOf(
            "position" to BufferAttribute(BufferData(positions.asBufferable()), 3, DataType.FLOAT)
        ),
        mode = mode,
    )

/**
 * A single geometry to be drawn on the GPU. The data isn't necessarily directly
 * available as it could be stored in several different ways (in Kotlin, in
 * native, with or without a VAO, with or without an IBO, ...).
 */
interface Geometry: Disposable, GLListener {
    val mode: DrawMode
    val elementCount: Int? // null means to compute it

    val boundingBox: Box3
    val boundingSphere: Sphere

    /** "Create" the geometry. This uploads whatever data is necessary to the GPU. */
    override fun create(gl: GLContext)

    /** Renders the geometry. Uploaded to GPU if needed (if never uploaded or data updated). */
    fun render(gl: GLContext)

    // We don't care about the render time
    override fun render(gl: GLContext, time: Long) { render(gl) }

    /** Free all GPU resources associated with the geometry. */
    override fun dispose(gl: GLContext)

    // common attribute lookups
    val positions: BufferAttribute?
    val normals: BufferAttribute?
    val tangents: BufferAttribute?
    val uvs: BufferAttribute?
}


private data class AttrInfo(
    val location: GLAttributeLocation,
    val attribute: BufferAttribute,
)

/** A GL geometry that uses a VAO. */
open class GeometryWithVAO(
    val attributes: Map<String, BufferAttribute> = mutableMapOf(),
    override val mode: DrawMode = DrawMode.TRIANGLES,
    override val elementCount: Int? = null,
): Geometry {
    protected var program = GLProgram.NULL
    protected var vao = GLVertexArrayObject.NULL
    private val attrs = mutableMapOf<String, AttrInfo>()
    override val positions get() = attributes["position"]
    override val normals get() = attributes["normal"]
    override val tangents get() = attributes["tangent"]
    override val uvs get() = attributes["uv"]

    override fun create(gl: GLContext) {
        dispose(gl)
        program = gl.currentProgram

        vao = gl.genVertexArray()
        gl.bindVertexArray(vao)
        for ((name, attribute) in attributes) { createBuffer(gl, name, attribute) }
        gl.bindVertexArray(GLVertexArrayObject.NULL)
        gl.bindBuffer(GL.ARRAY_BUFFER, GLBuffer.NULL)
    }

    private fun createBuffer(gl: GLContext, name: String, attribute: BufferAttribute) {
        attribute.buffer.acquire(gl)
        updateBuffer(gl, name, attribute)
    }

    private fun updateBuffer(gl: GLContext, name: String, attribute: BufferAttribute) {
        val location = gl.getAttribLocation(program, name)
        attrs[name] = AttrInfo(location, attribute)
        attribute.bufferData(gl)
        attribute.vertexAttribPointer(gl, location)
    }

    private fun deleteBuffer(gl: GLContext, name: String, info: AttrInfo) {
        gl.disableVertexAttribArray(info.location)
        info.attribute.buffer.release(gl)
        attrs.remove(name)
    }

    protected fun updateForRender(gl: GLContext) {
        if (vao.isNull) {
            create(gl)
            gl.bindVertexArray(vao)
        } else {
            gl.bindVertexArray(vao)

            val toDelete = attrs.filter { (name, info) -> info.attribute != attributes[name] }
            for ((name, info) in toDelete) { deleteBuffer(gl, name, info) }

            val toAdd = attributes.filterKeys { it !in attrs }
            for ((name, attribute) in toAdd) { createBuffer(gl, name, attribute) }

            val toUpdate = if (program == gl.currentProgram) attributes.filterValues { it.needsUpdate }
                           else attributes.filterKeys { it in attrs && it !in toAdd }
            for ((name, attribute) in toUpdate) { updateBuffer(gl, name, attribute) }
        }
    }

    override fun render(gl: GLContext) {
        updateForRender(gl)
        gl.drawArrays(mode.value, 0, numElements)
        gl.bindVertexArray(GLVertexArrayObject.NULL)
    }

    override fun dispose(gl: GLContext) {
        for (info in attrs.values) { info.attribute.buffer.release(gl) }
        attrs.clear()
        if (vao.isSet && gl.isVertexArray(vao)) { gl.deleteVertexArray(vao) }
        vao = GLVertexArrayObject.NULL
        program = GLProgram.NULL
    }

    open val numElements get() = elementCount ?: attrs.values.minOf { it.attribute.count }

    protected var _boundingBox: Box3? = null
    override val boundingBox: Box3 //  not accounting for model matrix
        get() =
            _boundingBox ?:
            positions?.let { boundingBox(it.vec3View(Indices(numElements))).also { _boundingBox = it } } ?:
            error("No positions to compute bounding box")

    protected var _boundingSphere: Sphere? = null
    override val boundingSphere: Sphere //  not accounting for model matrix
        get() =
            _boundingSphere ?:
            positions?.let { boundingSphere(it.vec3View(Indices(numElements)), boundingBox).also { _boundingSphere = it } } ?:
            error("No positions to compute bounding sphere")
}

class GeometryWithVAOAndIBO(
    val indexBuffer: BufferData,
    elementCount: Int? = null,
    attributes: Map<String, BufferAttribute> = mutableMapOf(),
    mode: DrawMode = DrawMode.TRIANGLES,
    val elementType: ElementType = ElementType.UNSIGNED_SHORT,
    val offset: Int = 0,
): GeometryWithVAO(attributes, mode, elementCount) {
    init {
        require(indexBuffer.target == BufferTarget.ELEMENT_ARRAY)
        require(offset % elementType.size == 0)
    }

    override val numElements get() = elementCount ?: ((indexBuffer.numBytes / elementType.size) - offset)
    private val intDataType = IntDataType.from(elementType.value)
    private val indexView = IntView(indexBuffer.data, intDataType, offset, numElements)
    val indices get() = Indices(indexView)

    override fun create(gl: GLContext) {
        super.create(gl)
        gl.bindVertexArray(vao)
        indexBuffer.acquire(gl)
        indexBuffer.bufferData(gl)
        gl.bindVertexArray(GLVertexArrayObject.NULL)
        gl.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, GLBuffer.NULL)
    }

    override fun render(gl: GLContext) {
        updateForRender(gl)
        gl.drawElements(mode, numElements, elementType, offset)
        gl.bindVertexArray(GLVertexArrayObject.NULL)
    }

    override fun dispose(gl: GLContext) {
        indexBuffer.release(gl)
        super.dispose(gl)
    }

    override val boundingBox: Box3 // not accounting for model matrix
        get() =
            _boundingBox ?:
            positions?.let { boundingBox(it.vec3View(indices)).also { _boundingBox = it } } ?:
            error("No positions to compute bounding box")

    override val boundingSphere: Sphere // not accounting for model matrix
        get() =
            _boundingSphere ?:
            positions?.let { boundingSphere(it.vec3View(indices), boundingBox).also { _boundingSphere = it } } ?:
            error("No positions to compute bounding sphere")
}


/** Testing model */
val CUBE = Geometry(
    floatArrayOf(
        /*A*/ 1f, 1f, 1f, /*B*/ -1f, 1f, 1f, /*C*/ -1f, -1f, 1f, /*D*/ 1f, -1f, 1f,
        /*E*/ 1f, -1f, -1f, /*F*/ -1f, -1f, -1f, /*G*/ -1f, 1f, -1f, /*H*/ 1f, 1f, -1f),
    shortArrayOf(
        /*ABC*/ 0, 1, 2, /*ACD*/ 0, 2, 3, /*ADH*/ 0, 3, 7, /*DEH*/ 3, 4, 7,
        /*FGH*/ 5, 6, 7, /*EFH*/ 4, 5, 7, /*BGC*/ 1, 6, 2, /*FCG*/ 5, 2, 6,
        /*BHG*/ 1, 7, 6, /*BAH*/ 1, 0, 7, /*CED*/ 2, 4, 3, /*CFE*/ 2, 5, 4,
    )
)

private fun boundingBox(vertices: Vector3View, output: Box3 = Box3()): Box3 {
    val iter = vertices.iterator()
    if (!iter.hasNext()) return output.makeEmpty()
    val vec3 = Vector3()
    val (x0, y0, z0) = iter.next(vec3)
    var minX = x0; var maxX = x0
    var minY = y0; var maxY = y0
    var minZ = z0; var maxZ = z0
    while (iter.hasNext()) {
        val (x, y, z) = iter.next(vec3)
        if (x < minX) { minX = x } else if (x > maxX) { maxX = x }
        if (y < minY) { minY = y } else if (y > maxY) { maxY = y }
        if (z < minZ) { minZ = z } else if (z > maxZ) { maxZ = z }
    }
    output.min.set(minX, minY, minZ)
    output.max.set(maxX, maxY, maxZ)
    return output
}
private fun boundingSphere(vertices: Vector3View, boundingBox: Box3, output: Sphere = Sphere()): Sphere {
    val iter = vertices.iterator()
    val center = output.center
    boundingBox.getCenter(center)
    val vec3 = Vector3()
    var radiusSq = 0f
    while (iter.hasNext()) {
        val (x, y, z) = iter.next(vec3)
        val dx = x-center.x; val dy = y-center.y; val dz = z-center.z
        val distSq = dx*dx + dy*dy + dz*dz
        if (distSq > radiusSq) { radiusSq = distSq }
    }
    output.radius = sqrt(radiusSq)
    return output
}
