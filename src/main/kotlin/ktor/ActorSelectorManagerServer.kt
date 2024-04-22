package ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import utils.*
import java.net.SocketException

class ActorSelectorManagerServer {

    suspend fun start() {
        coroutineScope {
            val socket = aSocket(SelectorManager(coroutineContext))
                .tcp()
                .bind(InetSocketAddress(SERVER_ADDRESS, SERVER_PORT)) {
                    backlogSize = SERVER_BACKLOG
                    reuseAddress = true
                    reusePort = true
                }

            socket.use { server ->
                while (true) {
                    server.accept().launchConnection(this) { connection ->
                        while (true) {
                            val receivedByteArray = connection.input.readByteArray()

                            if (!DEBUG) {
                                if (!receivedByteArray.isHttpRequest()) {
                                    break
                                }
                            }

                            if (DEBUG) {
                                receivedByteArray.checkRequest()
                            }

                            connection.output.writeByteArray()

                            if (DEBUG) {
                                return@launchConnection
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Socket.launchConnection(scope: CoroutineScope, block: suspend (Connection) -> Unit) {
        scope.launch {
            use {
                try {
                    block(connection())
                } catch (_: SocketException) {
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                }
            }
        }
    }

    private suspend fun ByteReadChannel.readByteArray(): ByteArray {
        val receivedByteArray = ByteArray(REQUEST_SIZE)
        var readBytes = 0
        while (readBytes != -1 && readBytes < REQUEST_SIZE) {
            readBytes += readAvailable(receivedByteArray, readBytes, REQUEST_SIZE - readBytes)
        }
        return receivedByteArray
    }

    private suspend fun ByteWriteChannel.writeByteArray() {
        var writtenBytes = 0
        while (writtenBytes < RESPONSE_SIZE) {
            writtenBytes += writeAvailable(RESPONSE, writtenBytes, RESPONSE_SIZE - writtenBytes)
        }
        flush()
    }
}
