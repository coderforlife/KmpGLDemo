package edu.moravian.kmpgl.compose

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import platform.zlib.Z_NEED_DICT
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_STREAM_ERROR
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import kotlin.experimental.ExperimentalNativeApi

const val IN_BUFFER_SIZE = 4*1024
const val OUT_BUFFER_SIZE = 8*1024

// The GzipEncoder class does nothing on iOS, so we instead we create it ourselves
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class, DelicateCoroutinesApi::class)
internal actual suspend fun gzipDecodedChannel(source: ByteReadChannel) =
    GlobalScope.writer(currentCoroutineContext()) { memScoped {
        val strm = alloc<platform.zlib.z_stream_s>()

        // Initialize the zlib stream
        strm.zalloc = null
        strm.zfree = null
        strm.opaque = null
        strm.avail_in = 0u
        strm.next_in = null
        if (inflateInit2(strm.ptr, 32+15) != Z_OK) { throw RuntimeException("Failed to initialize zlib stream") }

        try {
            // Allocate buffers for input and output
            val bufferIn = allocArray<ByteVar>(IN_BUFFER_SIZE)
            val bufferOut = allocArray<ByteVar>(OUT_BUFFER_SIZE)

            while (!source.isClosedForRead) {
                // Read data from the source channel
                val count = source.readAvailable(bufferIn, 0, IN_BUFFER_SIZE)
                if (count <= 0) { continue }
                strm.avail_in = count.toUInt()
                strm.next_in = bufferIn.reinterpret()

                do {
                    // Decompress the data
                    strm.avail_out = OUT_BUFFER_SIZE.toUInt()
                    strm.next_out = bufferOut.reinterpret()
                    val ret = inflate(strm.ptr, Z_NO_FLUSH)
                    assert(ret != Z_STREAM_ERROR)
                    if (ret < 0 || ret == Z_NEED_DICT) { throw RuntimeException("Error during inflation: $ret") }

                    // Write the decompressed data to the output channel
                    channel.writeFully(bufferOut, 0, OUT_BUFFER_SIZE - strm.avail_out.toInt())

                    if (ret == Z_STREAM_END) { break }
                } while (strm.avail_out == 0u)
            }
        } finally {
            inflateEnd(strm.ptr)
        }
    } }.channel
