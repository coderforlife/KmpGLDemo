package edu.moravian.kmpgl.compose

import co.touchlab.kermit.Logger
import edu.moravian.kmpgl.core.GL
import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLListener
import edu.moravian.kmpgl.core.GLUniformLocation
import edu.moravian.kmpgl.core.uniform
import edu.moravian.kmpgl.math.Matrix4
import edu.moravian.kmpgl.math.Sphere
import edu.moravian.kmpgl.math.Vector3


// TODO: Things to adjust
//   - default shininess
// TODO: Things to add
//   - grid
//   - add shadows from SOME lights

private val logger = Logger.withTag("ModelViewer")
val ORIGIN = Vector3()
val WHITE = GLColor(1f, 1f, 1f)
val BLACK = GLColor(0f, 0f, 0f)

class ModelViewer(
    val controls: ViewControlsState,
    val numLights: Int = 3
): GLListener {
    private lateinit var program: ShaderProgram
    /**
     * Updates we need to apply during the next frame.
     * A set so that we register event update type only once.
     * Original insertion order is guaranteed as well.
     */
    private val updates = linkedSetOf<(GLContext) -> Unit>()

    /////////////// Basic Environment ///////////////
    /**
     * When rendering nothing, updates are still processed (like creating new
     * mesh VAOs on the GPU) and the background is still cleared, but nothing
     * else is rendered.
     */
    var renderNothing: Boolean = false

    var backgroundColor: GLColor = BLACK.clone()

    // Not helpful for the lighting style currently implemented
    private var uAmbientLight = GLUniformLocation.NULL
    var ambientLight = GLColor(0f, 0f, 0f)

    private var uLightPosition = GLUniformLocation.NULL
    private var uLightColor = GLUniformLocation.NULL
    private val lightPositions = FloatArray(numLights * 3)
    private val lightColors = FloatArray(numLights * 3)

    fun setLights(
        positions: List<Vector3>,
        colors: List<GLColor> = listOf()
    ) {
        if (positions.isEmpty()) { throw IllegalArgumentException("at least one light is required") }
        if (positions.size > numLights) { throw IllegalArgumentException("at most $numLights lights are supported") }
        lightPositions.fill(0f)
        lightColors.fill(0f)
        for (i in positions.indices) {
            positions[i].toArray(lightPositions, i * 3)
            colors.getOrElse(i) { WHITE }.toArray(lightColors, i * 3)
        }
    }

    init {
        // From http://www.3drender.com/light/3point.html
        setLights(
            listOf(
                Vector3(.4f, .6f, 3f), // Key Light
                Vector3(-.3f, .3f, 5f), // Fill Light
                Vector3(0f, .3f, -5f), // Rim/Back Light
            ),
            listOf(
                GLColor(1f, 1f, 1f), // Key Light // TODO: needs shadows
                GLColor(.5f, .5f, .5f), // Fill Light
                GLColor(.7f, .7f, .7f), // Rim/Back Light // TODO: needs shadows
            )
        )
    }

    /////////////// Camera Setup ///////////////
    // The view matrix is uploaded every render()
    private var uViewMatrix = GLUniformLocation.NULL
    fun resetView() { controls.reset() }
    private var uProjectionMatrix = GLUniformLocation.NULL
    private val projectionMatrix = Matrix4()
    var width: Int = 0; private set
    var height: Int = 0; private set


    /////////////// Meshes ///////////////
    private var uModelMatrix = GLUniformLocation.NULL
    val modelMatrix = Matrix4()
    private val _meshes = mutableListOf<Geometry>()
    val meshes : List<Geometry> = _meshes
    fun addMesh(mesh: Geometry) {
        _meshes.add(mesh)
        updates.add { gl -> mesh.create(gl) }
    }
    fun clearMeshes() {
        _meshes.forEach { updates.add { gl -> it.dispose(gl) } }
        _meshes.clear()
    }
    fun doneAddingMeshes() {
        if (_meshes.isEmpty()) { return }

        val sphere = Sphere()
        _meshes.forEach { sphere.union(it.boundingSphere) }
        val center = sphere.center
        val scale = 1f / sphere.radius
        // TODO: grid.updateTransformation(scale, 2*radius)

        //val bbox = Box3()
        //_meshes.forEach { bbox.union(it.boundingBox) }
        //val center = bbox.center
        //val depth = bbox.depth
        //// 2f = width of view coords
        //// 1.4 ~= sqrt(2) [corner-to-corner distance of box, slightly more to hit sides]
        //val scale = 2f / (1.4f * max(bbox.height, bbox.width, depth))
        //// TODO: grid.updateTransformation(scale, depth)

        modelMatrix.makeTranslation(
            center.multiplyScalar(-scale) // move it to the origin
        ).scale(Vector3(scale, scale, scale)) // scale it to a 1x1x1 box
    }


    /////////////// Mesh Appearance ///////////////
    private val _materials = mutableListOf(BasicMaterial())
    val materials: List<Material> = _materials
    private fun setMaterial(gl: GLContext, i: Int) =
        materials[i.coerceAtMost(materials.lastIndex)].set(gl)
    fun setMaterials(vararg colors: GLColor) {
        this._materials.clear()
        if (colors.isEmpty()) {
            this._materials.add(BasicMaterial())
        } else {
            this._materials.addAll(colors.map { BasicMaterial(it) })
        }
    }
    fun setMaterials(vararg materials: BasicMaterial) {
        this._materials.clear()
        if (materials.isEmpty()) {
            this._materials.add(BasicMaterial())
        } else {
            this._materials.addAll(materials)
        }
    }

    /////////////// Gl Render Cycle Events ///////////////
    override fun create(gl: GLContext) {
        logger.i { "create()" }

        // Set rendering settings
        gl.clearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)
        // on iOS this produces an error (GL_INVALID_FRAMEBUFFER_OPERATION)
        // probably not needed until the first render anyway
        //gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT or GL.STENCIL_BUFFER_BIT)
        gl.enable(GL.CULL_FACE)
        gl.enable(GL.DEPTH_TEST)

        // Compile and use program
        if (!::program.isInitialized) {
            program = FlatShaderProgram(gl, numLights)
        }
        program.create(gl)
        gl.use(program)

        // Get all attributes and uniforms
        uViewMatrix = gl.getUniformLocation(program, "uViewMatrix")
        uProjectionMatrix = gl.getUniformLocation(program, "uProjectionMatrix")
        uLightPosition = gl.getUniformLocation(program, "uLightPosition")
        uLightColor = gl.getUniformLocation(program, "uLightColor")
        uAmbientLight = gl.getUniformLocation(program, "uAmbientLight")
        uModelMatrix = gl.getUniformLocation(program, "uModelMatrix")

        // Create meshes
        meshes.forEach { it.create(gl) }

        // All updates are already applied above
        updates.clear()
    }

    override fun recreate(gl: GLContext) {
        logger.i { "recreate()" }
        create(gl)
    }

    override fun resize(gl: GLContext, width: Int, height: Int) {
        logger.i { "resize($width, $height)" }
        this.width = width
        this.height = height
        gl.viewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        if (ratio >= 1f) {
            projectionMatrix.makeOrthographic(-ratio, ratio, 1f, -1f, -1000f, 1000f)
        } else {
            projectionMatrix.makeOrthographic(-1f, 1f, 1f/ratio, -1f/ratio, -1000f, 1000f)
        }
        gl.uniformMatrix4fv(uProjectionMatrix, projectionMatrix.elements)
    }
    override fun render(gl: GLContext, time: Long) {
        //logger.i { "render()" }

        // Apply all pending updates
        for (update in updates) { update(gl) }
        updates.clear()

        // Clear the screen
        gl.clearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, 1f)
        gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)
        if (renderNothing) return

        // Setup the environment
        gl.uniform(uAmbientLight, ambientLight)
        gl.uniform3fv(uLightPosition, lightPositions, count = numLights)
        gl.uniform3fv(uLightColor, lightColors, count = numLights)

        // Draw the scene
        gl.uniformMatrix4fv(uViewMatrix, controls.update().elements)
        // TODO: grid.render()
        gl.uniformMatrix4fv(uModelMatrix, modelMatrix.elements)
        meshes.withIndex().forEach { (i, mesh) ->
            setMaterial(gl, i)
            mesh.render(gl)
        }
    }

    override fun pause(gl: GLContext) { logger.i { "pause()" } }
    override fun resume(gl: GLContext) { logger.i { "resume()" } }

    override fun dispose(gl: GLContext) {
        logger.i { "dispose()" }
        meshes.forEach { it.dispose(gl) }
        _meshes.clear()
        // TODO: grid.dispose()
        program.dispose(gl)
    }
}
