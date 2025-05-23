package edu.moravian.kmpgl.compose

import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLListener
import edu.moravian.kmpgl.core.GLProgram
import edu.moravian.kmpgl.core.GLUniformLocation
import edu.moravian.kmpgl.core.currentProgram
import edu.moravian.kmpgl.core.isNull
import edu.moravian.kmpgl.core.uniform

abstract class Material: GLListener {
    protected var program = GLProgram.NULL

    protected abstract fun getUniformsInternal(gl: GLContext)
    protected abstract fun setInternal(gl: GLContext)
    fun set(gl: GLContext) {
        if (program.isNull || program != gl.currentProgram) {
            program = gl.currentProgram
            getUniformsInternal(gl)
        }
        setInternal(gl)
    }

    override fun dispose(gl: GLContext) {
        program = GLProgram.NULL
    }
}

data class BasicMaterial(
    val color: GLColor = GLColor(1f, 1f, 1f),
    val shininess: Float = 10f,
): Material() {
    private var uMaterialColor = GLUniformLocation.NULL
    private var uMaterialShininess = GLUniformLocation.NULL

    override fun setInternal(gl: GLContext) {
        gl.uniform(uMaterialColor, color)
        gl.uniform(uMaterialShininess, shininess)
    }

    override fun getUniformsInternal(gl: GLContext) {
        uMaterialColor = gl.getUniformLocation(program, "uMaterialColor")
        uMaterialShininess = gl.getUniformLocation(program, "uMaterialShininess")
    }

    override fun dispose(gl: GLContext) {
        super.dispose(gl)
        uMaterialColor = GLUniformLocation.NULL
        uMaterialShininess = GLUniformLocation.NULL
    }
}
