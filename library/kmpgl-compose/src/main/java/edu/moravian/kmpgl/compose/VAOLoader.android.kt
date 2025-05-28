package edu.moravian.kmpgl.compose

import io.ktor.util.GZipEncoder
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.currentCoroutineContext

internal actual suspend fun gzipDecodedChannel(source: ByteReadChannel) =
    GZipEncoder.decode(source, currentCoroutineContext())
