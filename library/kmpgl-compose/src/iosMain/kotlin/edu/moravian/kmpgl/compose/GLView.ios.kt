package edu.moravian.kmpgl.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import edu.moravian.kmpgl.core.GLPlatformContext

@Composable
internal actual fun RealGLView(viewModel: GLViewModel, modifier: Modifier) {
    UIKitViewController(
        modifier = modifier,
        factory = { viewModel.getNativeView(GLPlatformContext()) }
    )
}
