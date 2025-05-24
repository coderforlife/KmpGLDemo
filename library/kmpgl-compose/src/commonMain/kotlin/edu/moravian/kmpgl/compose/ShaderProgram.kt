@file:Suppress("NOTHING_TO_INLINE")
package edu.moravian.kmpgl.compose

import co.touchlab.kermit.Logger
import edu.moravian.kmpgl.core.GL
import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLListener
import edu.moravian.kmpgl.core.GLProgram
import edu.moravian.kmpgl.core.GLShader
import edu.moravian.kmpgl.core.currentProgram
import edu.moravian.kmpgl.core.isProgram
import edu.moravian.kmpgl.core.isSet
import edu.moravian.kmpgl.core.isShader

private val logger = Logger.withTag("ShaderProgram")

class ShaderProgram(
    vertexShaderSource: String,
    fragmentShaderSource: String,
    vararg shaderSources: Pair<Int, String>,
): GLListener, Disposable {
    init {
        require(shaderSources.all { it.first != GL.VERTEX_SHADER && it.first != GL.FRAGMENT_SHADER }) {
            "Additional shader sources must not be vertex or fragment shaders"
        }
        require(shaderSources.map { it.first }.distinct().size == shaderSources.size) {
            "Additional shader sources must have unique types"
        }
    }

    var program = GLProgram.NULL; private set
    val shaders = listOf(
        Shader(GL.VERTEX_SHADER, vertexShaderSource),
        Shader(GL.FRAGMENT_SHADER, fragmentShaderSource),
    ) + shaderSources.map { Shader(it.first, it.second) }

    /** Compile and link the shader programs. */
    override fun create(gl: GLContext) {
        for (shader in shaders) { shader.compile(gl) }
        link(gl)
    }

    override fun recreate(gl: GLContext) {
        dispose(gl)
        create(gl)
    }

    private fun link(gl: GLContext) {
        if (isValid(gl)) {
            logger.w { "Program is already linked. Skipping..." }
            return
        }
        val program = gl.createProgram()
        for (s in shaders) { gl.attachShader(program, s.shader) }
        gl.linkProgram(program)
        if (gl.getProgramInt(program, GL.LINK_STATUS) == 0) {
            logger.e { "Program failed to link. The error log is:" }
            logger.e { gl.getProgramInfoLog(program) }
            dispose(gl)
            gl.deleteProgram(program)
            throw IllegalArgumentException("Program failed to link")
        }
        dispose(gl)
        this.program = program
    }

    /** Delete the program and all shaders. */
    override fun dispose(gl: GLContext) {
        delete(gl)
        for (shader in shaders) { shader.dispose(gl) }
    }

    /** Delete the program and set the current program to NULL if it is this program. */
    private fun delete(gl: GLContext) {
        println("Deleting program $program")
        if (isValid(gl)) {
            if (gl.currentProgram == program) {
                gl.useProgram(GLProgram.NULL)
            }
            gl.deleteProgram(program)
        }
        program = GLProgram.NULL
    }

    private inline fun isValid(gl: GLContext) = program.isSet && gl.isProgram(program)

    fun use(gl: GLContext) {
        if (!isValid(gl)) { create(gl) }
        gl.useProgram(program)
    }
    inline fun getAttribLocation(gl: GLContext, name: String) = gl.getAttribLocation(program, name)
    inline fun getUniformLocation(gl: GLContext, name: String) = gl.getUniformLocation(program, name)
}

inline fun GLContext.use(program: ShaderProgram) =
    program.use(this)
inline fun GLContext.getAttribLocation(program: ShaderProgram, name: String) =
    program.getAttribLocation(this, name)
inline fun GLContext.getUniformLocation(program: ShaderProgram, name: String) =
    program.getUniformLocation(this, name)

class Shader(val type: Int, val source: String): Disposable {
    var shader = GLShader.NULL; private set

    fun compile(gl: GLContext) {
        if (this.shader.isSet && gl.isShader(this.shader)) {
            logger.w { "Shader is already compiled. Skipping..." }
            return
        }
        val shader = gl.createShader(type)
        gl.shaderSource(shader, source)
        gl.compileShader(shader)
        if (gl.getShaderInt(shader, GL.COMPILE_STATUS) == 0) {
            logger.e { "Shader failed to compile. The error log is:" }
            logger.e { gl.getShaderInfoLog(shader) }
            gl.deleteShader(shader)
            throw IllegalArgumentException("Shader failed to compile")
        }
        dispose(gl)
        this.shader = shader
    }

    override fun dispose(gl: GLContext) {
        if (shader.isSet && gl.isShader(shader)) { gl.deleteShader(shader) }
        shader = GLShader.NULL
    }
}
