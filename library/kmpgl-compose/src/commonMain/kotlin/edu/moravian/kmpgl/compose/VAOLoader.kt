package edu.moravian.kmpgl.compose

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.discardExact
import io.ktor.utils.io.peek
import io.ktor.utils.io.readFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.ByteString
import kotlin.math.min

// VAO File Format:
//      2 32-bit integers: number of vertices and number of indices
//      followed by that many floats times 3
//      followed by that many shorts (technically unsigned shorts)
// Can be big-endian or little-endian (determined by the first 2 bytes being 0x00 0x00)
// Even though the number of vertices is an integer, the max number of vertices is
//      limited to 65536 because the indices are (unsigned) shorts.
// The big-endian format is generally less preferable as it requires extra work on most
//      platforms.


/** Load a VAO file from a byte array synchronously. This is a blocking call. */
fun loadVAO(data: ByteArray) = runBlocking { loadVAOFromChannel(ByteReadChannel(data)) }

/** Load a VAO file from a byte array asynchronously. */
suspend fun loadVAOAsync(data: ByteArray) = loadVAOFromChannel(ByteReadChannel(data))

/**
 * Load a VAO file from a URL asynchronously. Using the default client, data
 * that reports to be compressed at the HTTP level is transparently
 * decompressed.
 */
fun CoroutineScope.loadVAOFromURL(url: String) = loadVAOFromURL(url, defaultClient)

fun CoroutineScope.loadVAOFromURL(url: String, client: HttpClient): Deferred<Geometry?> = async {
    try {
        client.prepareGet(url).execute { httpResponse -> loadVAOFromChannel(httpResponse.body()) }
    } catch (e: Exception) { // if the error is not handled here, it will crash the app
        println("Error loading mesh from URL $url: ${e.message}")
        null
    }
}

suspend fun ModelViewer.loadVAOModelsFromURLs(urls: Collection<String>) =
    loadVAOModelsFromURLs(urls, defaultClient)

suspend fun ModelViewer.loadVAOModelsFromURLs(urls: Collection<String>, client: HttpClient) = coroutineScope {
    val deferred = urls.map { url -> loadVAOFromURL(url, client) }
    var n = 0
    for ((url, def) in urls.zip(deferred)) {
        try {
            def.await()?.let { addMesh(it); n++ }
        } catch (e: Exception) {
            println("Error loading mesh from URL $url: ${e.message}") // not actually reachable
        }
    }
    n
}


private suspend fun loadVAOFromChannel(channel: ByteReadChannel): Geometry {
    if (!channel.awaitContent(2*Int.SIZE_BYTES)) { throw RuntimeException("no data") }
    val header = channel.peek(2*Int.SIZE_BYTES) ?: throw RuntimeException("no data")
    if (header[0] == 0x1F.toByte() && header[1] == 0x8B.toByte() && header[2] == 0x08.toByte()) {
        // gzip compressed data - decompress it
        return loadVAOFromChannel(gzipDecodedChannel(channel))
    }
    channel.discardExact(2L*Int.SIZE_BYTES)

    // when there is 00 01 00 00 assume it is big endian (65536) instead of little endian (256)
    val isBigEndian = header[0] == 0x00.toByte() && header[1] == 0x00.toByte() || (
            header[0] == 0x00.toByte() && header[1] == 0x01.toByte() && header[2] == 0x00.toByte() && header[3] == 0x00.toByte()
            )
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    return if (isBigEndian) {
        val numVerts = header.readIntBE(0)
        val numIndices = header.readIntBE(Int.SIZE_BYTES)
        val positions = readPositionsBE(channel, numVerts * 3, buffer)
        val indices = readIndicesBE(channel, numIndices, numVerts, buffer)
        Geometry(positions, indices)
    } else {
        val numVerts = header.readIntLE(0)
        val numIndices = header.readIntLE(Int.SIZE_BYTES)
        val positions = readPositionsLE(channel, numVerts * 3, buffer)
        val indices = readIndicesLE(channel, numIndices, numVerts, buffer)
        Geometry(positions, indices)
    }
}

private suspend fun readPositionsBE(channel: ByteReadChannel, numPositions: Int, buffer: ByteArray): FloatArray {
    val positions = FloatArray(numPositions)
    var posIndex = 0
    while (posIndex != numPositions && !channel.isClosedForRead) {
        val n = min(numPositions - posIndex, buffer.size / Float.SIZE_BYTES)
        val nbytes = n * Float.SIZE_BYTES
        channel.readFully(buffer, 0, nbytes)
        for (i in 0 until nbytes step Float.SIZE_BYTES) {
            positions[posIndex++] = buffer.readFloatBE(i)
        }
    }
    if (posIndex < numPositions) { throw RuntimeException("not enough data") }
    return positions
}

private suspend fun readPositionsLE(channel: ByteReadChannel, numPositions: Int, buffer: ByteArray): FloatArray {
    val positions = FloatArray(numPositions)
    var posIndex = 0
    while (posIndex != numPositions && !channel.isClosedForRead) {
        val n = min(numPositions - posIndex, buffer.size / Float.SIZE_BYTES)
        val nbytes = n * Float.SIZE_BYTES
        channel.readFully(buffer, 0, nbytes)
        for (i in 0 until nbytes step Float.SIZE_BYTES) {
            positions[posIndex++] = buffer.readFloatLE(i)
        }
    }
    if (posIndex < numPositions) { throw RuntimeException("not enough data") }
    return positions
}

private suspend fun readIndicesBE(channel: ByteReadChannel, numIndices: Int, numVertices: Int, buffer: ByteArray): ShortArray {
    val indices = ShortArray(numIndices)
    var indIndex = 0
    while (indIndex != numIndices && !channel.isClosedForRead) {
        val n = min(numIndices - indIndex, buffer.size / Short.SIZE_BYTES)
        val nbytes = n * Short.SIZE_BYTES
        channel.readFully(buffer, 0, nbytes)
        for (i in 0 until nbytes step Short.SIZE_BYTES) {
            val ind = buffer.readShortBE(i)
            if (ind >= numVertices) { throw RuntimeException("invalid index (at index $indIndex: $ind >= $numVertices") }
            indices[indIndex++] = ind
        }
    }
    if (indIndex < numIndices) { throw RuntimeException("not enough data") }
    return indices
}

private suspend fun readIndicesLE(channel: ByteReadChannel, numIndices: Int, numVertices: Int, buffer: ByteArray): ShortArray {
    val indices = ShortArray(numIndices)
    var indIndex = 0
    while (indIndex != numIndices && !channel.isClosedForRead) {
        val n = min(numIndices - indIndex, buffer.size / Short.SIZE_BYTES)
        val nbytes = n * Short.SIZE_BYTES
        channel.readFully(buffer, 0, nbytes)
        for (i in 0 until nbytes step Short.SIZE_BYTES) {
            val ind = buffer.readShortLE(i)
            if (ind >= numVertices) { throw RuntimeException("invalid index (at index $indIndex: $ind >= $numVertices") }
            indices[indIndex++] = ind
        }
    }
    if (indIndex < numIndices) { throw RuntimeException("not enough data") }
    return indices
}

const val DEFAULT_BUFFER_SIZE = 8 * 1024

private var defaultClient = HttpClient {
    install(ContentEncoding) // allows receiving deflate and gzip-compressed content (no brotli though)
    expectSuccess = true
    //install(Logging) { logger = KtorLogger; level = LogLevel.HEADERS }
    install(HttpTimeout) {
        connectTimeoutMillis = 1000
        socketTimeoutMillis = 2000
        requestTimeoutMillis = 5000
    }
}

internal expect suspend fun gzipDecodedChannel(source: ByteReadChannel): ByteReadChannel

internal fun ByteArray.readShortBE(index: Int) = (this[index+1].toInt() and 0xFF or (this[index].toInt() and 0xFF shl 8)).toShort()
internal fun ByteArray.readShortLE(index: Int) = (this[index].toInt() and 0xFF or (this[index+1].toInt() and 0xFF shl 8)).toShort()
internal fun ByteArray.readIntBE(index: Int) = (this[index+3].toInt() and 0xFF) or (this[index+2].toInt() and 0xFF shl 8) or (this[index+1].toInt() and 0xFF shl 16) or (this[index].toInt() and 0xFF shl 24)
internal fun ByteArray.readIntLE(index: Int) = (this[index].toInt() and 0xFF) or (this[index+1].toInt() and 0xFF shl 8) or (this[index+2].toInt() and 0xFF shl 16) or (this[index+3].toInt() and 0xFF shl 24)
internal fun ByteString.readIntBE(index: Int) = (this[index+3].toInt() and 0xFF) or (this[index+2].toInt() and 0xFF shl 8) or (this[index+1].toInt() and 0xFF shl 16) or (this[index].toInt() and 0xFF shl 24)
internal fun ByteString.readIntLE(index: Int) = (this[index].toInt() and 0xFF) or (this[index+1].toInt() and 0xFF shl 8) or (this[index+2].toInt() and 0xFF shl 16) or (this[index+3].toInt() and 0xFF shl 24)
internal fun ByteArray.readFloatBE(index: Int) = Float.fromBits(readIntBE(index))
internal fun ByteArray.readFloatLE(index: Int) = Float.fromBits(readIntLE(index))
