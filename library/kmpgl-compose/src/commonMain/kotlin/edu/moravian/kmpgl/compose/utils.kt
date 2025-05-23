package edu.moravian.kmpgl.compose

import androidx.compose.ui.graphics.Color
import edu.moravian.kmpgl.core.GLContext

typealias GLColor = edu.moravian.kmpgl.math.Color3

fun Color.toGLColor(out: GLColor = GLColor()): GLColor {
    out.r = this.red
    out.g = this.green
    out.b = this.blue
    return out
}

fun GLColor.toColor(): Color = Color(this.r, this.g, this.b)

interface Disposable {
    fun dispose(gl: GLContext)
}

interface Versioned {
    var needsUpdate: Boolean
    val version: Int
}

abstract class VersionedImpl: Versioned {
    private var _needsUpdate: Boolean = true
    protected fun clearNeedsUpdate() { _needsUpdate = false }
    override var needsUpdate // when true, needs to be uploaded to the GPU
        get() = _needsUpdate
        set(value) { if (value && !_needsUpdate) {
            _needsUpdate = true
            version++
        } }
    final override var version: Int = 0; private set // incremented when the data changes
}
