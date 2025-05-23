package edu.moravian.kmpgl.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@Composable
actual fun getContext(): Any? = LocalContext.current

actual fun readAsset(path: String, context: Any?): ByteArray? {
    val assets = (context as? android.content.Context
        ?: throw IllegalArgumentException("Context must be of type android.content.Context")).assets
    try {
        assets.open(path).use { input ->
            val temp = ByteArray(4096)
            val output = ByteArrayOutputStream()
            var len: Int
            while ((input.read(temp).also { len = it }) != -1) {
                output.write(temp, 0, len)
            }
            return output.toByteArray()
        }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }
}
