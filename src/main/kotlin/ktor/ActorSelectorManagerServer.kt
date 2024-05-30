package ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import run.ports
import run.selectorContext
import utils.*

class ActorSelectorManagerServer {
    private val sockets = ports().map { port ->
        aSocket(SelectorManager(selectorContext))
            .tcp()
            .bind(port = port) {
                backlogSize = SERVER_BACKLOG
            }
    }

    suspend fun start() {
        try {
            coroutineScope {
                val actorScope: CoroutineScope = this

                sockets.forEach { socket ->
                    launch {
                        while (true) {
                            socket.accept().launchConnection(actorScope, ::processClient)
                        }
                    }
                }
            }
        } finally {
            sockets.forEach { it.close() }
        }
    }

    private suspend fun processClient(connection: Connection) {
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
                break
            }
        }
    }

    private fun Socket.launchConnection(scope: CoroutineScope, block: suspend (Connection) -> Unit) {
        scope.launch {
            use {
                try {
                    block(it.connection())
                } catch (_: Throwable) {
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
