package edu.moravian.kmpgl.compose

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingMappedIfSafe
import platform.Foundation.dataWithContentsOfFile

@Composable
actual fun getContext(): Any? = null

@OptIn(ExperimentalForeignApi::class)
actual fun readAsset(path: String, context: Any?): ByteArray? {
    val assetPath = "${NSBundle.mainBundle.resourcePath!!}/compose-resources/assets/$path"
    val data = NSData.dataWithContentsOfFile(assetPath, NSDataReadingMappedIfSafe, null) ?: return null
    return data.bytes?.readBytes(data.length.toInt())
}

// Alternative implementation using mmap
//
//@OptIn(ExperimentalForeignApi::class)
//fun read(path: String): ByteArray? {
//    val fd = open(path, O_RDONLY)
//    val length = lseek(fd, 0, SEEK_END)
//    lseek(fd, 0, SEEK_SET)
//    val ptr = mmap(null, length.toULong(), PROT_READ, MAP_SHARED, fd, 0)!!
//    close(fd)
//    return ptr?.readBytes(data.length.toInt())?.also {
//       munmap(ptr, length.toULong())
//    }
//}

// Tool for debugging the file system and seeing what files are available
//
//@OptIn(ExperimentalForeignApi::class)
//fun tree(dir: String, indent: String = "") {
//    val manager = NSFileManager.defaultManager
//    val contents = manager.contentsOfDirectoryAtPath(dir, null)
//    if (contents == null) {
//        println("${indent}contents is null")
//    } else {
//        for (file in contents) {
//            val attrs = manager.attributesOfItemAtPath("$dir/$file", null)
//            if (attrs?.get("NSFileType") == NSFileTypeDirectory) {
//                println("$indent$file/")
//                tree("$dir/$file", "$indent  ")
//            } else {
//                println("$indent$file")
//            }
//        }
//    }
//}
