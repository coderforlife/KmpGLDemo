package edu.moravian.kmpgl.demo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

import edu.moravian.kmpgl.compose.CUBE
import edu.moravian.kmpgl.compose.GLView
import edu.moravian.kmpgl.compose.GLViewModel
import edu.moravian.kmpgl.compose.ModelViewer
import edu.moravian.kmpgl.compose.getContext
import edu.moravian.kmpgl.compose.loadVAO
import edu.moravian.kmpgl.compose.loadVAOAsync
import edu.moravian.kmpgl.compose.loadVAOFromURL
import edu.moravian.kmpgl.compose.loadVAOModelsFromURLs
import edu.moravian.kmpgl.compose.orbitControls
import edu.moravian.kmpgl.compose.readAsset
import edu.moravian.kmpgl.compose.rememberOrbitControlsState

@Composable
@Preview
fun App() {
    MaterialTheme {
        var loaded by remember { mutableStateOf(false) }
        val controls = rememberOrbitControlsState()
        //val context = getContext()
        val modelViewer = remember { ModelViewer(controls) }
        val viewModel = viewModel { GLViewModel().also { it.context.addListener(modelViewer) } }

        val urls = listOf("https://test.3dadapt.xyz/files/output.vao")
        LaunchedEffect(null) {
            //modelViewer.addMesh(CUBE)
            //val data = readAsset("output.vao", context)
            //if (data != null) { modelViewer.addMesh(loadVAOAsync(data)) }
            modelViewer.loadVAOModelsFromURLs(urls)
            modelViewer.doneAddingMeshes()
            loaded = true
        }

        val baseMod = Modifier
            .size(350.dp, 400.dp)  //.fillMaxSize()
            .border(1.dp, Color.Green, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!loaded) {
                Text("Loading...", baseMod)
            } else {
                GLView(
                    viewModel = viewModel,
                    modifier = baseMod.orbitControls(controls, viewModel),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("Model Viewer", color = Color.Red)
        }
    }
}
