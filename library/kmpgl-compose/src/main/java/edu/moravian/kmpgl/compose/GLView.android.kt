package edu.moravian.kmpgl.compose

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import edu.moravian.kmpgl.core.GLPlatformContext

@Composable
internal actual fun RealGLView(viewModel: GLViewModel, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = viewModel.getNativeView(GLPlatformContext(context)).apply {
                (parent as? ViewGroup?)?.removeView(this)
                // TODO: layoutParams = getLayoutParams(constraints)
            }
            FrameLayout(context).apply {
                addView(view)
                // TODO: layoutParams = getLayoutParams(constraints)
            }
        }
    )
}

//private fun getLayoutParams(constraints: Constraints) =
//    ViewGroup.LayoutParams(
//        matchOrWrap(constraints.hasFixedWidth),
//        matchOrWrap(constraints.hasFixedHeight)
//    )
//private fun matchOrWrap(match: Boolean) =
//    if (match) ViewGroup.LayoutParams.MATCH_PARENT
//    else ViewGroup.LayoutParams.WRAP_CONTENT
