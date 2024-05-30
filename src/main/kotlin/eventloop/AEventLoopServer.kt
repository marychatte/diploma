package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import run.serverChannels
import utils.*
import java.nio.ByteBuffer

abstract class AEventLoopServer {
    protected val serverChannels = serverChannels()

    abstract suspend fun start()

    fun stop() {
        serverChannels.forEach { it.close() }
    }

    protected suspend fun processClient(connection: Connection, actorScope: CoroutineScope) {
        actorScope.launch {
            try {
                while (true) {
                    val receivedRequest = connection.read()

                    if (!DEBUG) {
                        if (!receivedRequest.isHttpRequest()) break
                    }

                    connection.write(RESPONSE_BUFFER.duplicate())
                }
            } catch (_: Throwable) {
            } finally {
                connection.close()
            }
        }
    }

    private suspend fun Connection.read(): ByteBuffer {
        val receivedRequest = ByteBuffer.allocate(REQUEST_SIZE)
        var readCount = 0
        while (true) {
            performRead {
                readCount += it.read(receivedRequest)
            }
            if (readCount == -1 || readCount >= REQUEST_SIZE) {
                break
            }
            yield()
        }

        return receivedRequest
    }

    private suspend fun Connection.write(response: ByteBuffer) {
        var writeCount = 0
        while (true) {
            performWrite {
                writeCount += it.write(response)
            }
            if (writeCount >= RESPONSE_SIZE) {
                break
            }
            yield()
        }
    }
}

class EventLoopServer : AEventLoopServer() {
    override suspend fun start() {
        val eventLoop = EventLoopImpl()

        eventLoop.run()
        val channels = serverChannels.map { eventLoop.register(it) }

        coroutineScope {
            val actorScope: CoroutineScope = this

            channels.forEach { channel ->
                launch {
                    while (true) {
                        processClient(channel.acceptConnection(), actorScope)
                    }
                }
            }
        }
    }
}
