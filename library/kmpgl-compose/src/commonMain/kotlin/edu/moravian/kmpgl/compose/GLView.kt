package edu.moravian.kmpgl.compose

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import edu.moravian.kmpgl.core.GLContext
import edu.moravian.kmpgl.core.GLContextAttributes
import edu.moravian.kmpgl.core.GLListener
import edu.moravian.kmpgl.core.GLPlatformContext

@Composable
fun GLView(
    viewModel: GLViewModel,
    modifier: Modifier = Modifier,
) { RealGLView(viewModel, modifier) }

@Composable
internal expect fun RealGLView(viewModel: GLViewModel, modifier: Modifier = Modifier)

/**
 * The view model for the GLView maintaining the GLContext.
 *
 * This keeps track of the rendering state and the size of the view.
 */
class GLViewModel(
    val context: GLContext = GLContext(),
    val attributes: GLContextAttributes = GLContextAttributes(),
): ViewModel() {
    var rendering by mutableStateOf(false); private set
    var width by mutableStateOf(0); private set
    var height by mutableStateOf(0); private set
    private val listener = object : GLListener {
        override fun create(gl: GLContext) { rendering = false }
        override fun recreate(gl: GLContext) { rendering = false }
        override fun resize(gl: GLContext, width: Int, height: Int) {
            this@GLViewModel.width = width
            this@GLViewModel.height = height
        }
        override fun render(gl: GLContext, time: Long) { rendering = true }
        override fun pause(gl: GLContext) { rendering = false }
        // override fun resume(gl: GLContext) { }
        override fun dispose(gl: GLContext) { rendering = false }
    }
    init { context.addListener(listener) }
    fun getNativeView(platformContext: GLPlatformContext) =
        context.initIfNeeded(attributes, platformContext)
    override fun onCleared() { context.dispose() }
}
