package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import utils.*
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel

class EventLoopServer(private val serverPort: Int) {
    private val serverChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        bind(InetSocketAddress(serverPort), SERVER_BACKLOG)
        configureBlocking(false)
    }

    suspend fun start() {
        val eventLoop = EventLoopImpl()
        coroutineScope {
            eventLoop.runOn(this)
            val channel = eventLoop.register(serverChannel)

            while (true) {
                processClient(channel, this)
            }
        }
    }

    fun stop() {
        serverChannel.close()
    }

    private suspend fun processClient(channel: RegisteredServerChannel, serverScope: CoroutineScope) {
        val connection = channel.acceptConnection()

        serverScope.launch {
            try {
                while (true) {
                    val receivedRequest = connection.read()

                    if (!DEBUG) {
                        if (!receivedRequest.isHttpRequest()) break
                    }
                    if (DEBUG) {
                        receivedRequest.checkRequest()
                    }

                    connection.write(RESPONSE_BUFFER.duplicate())

                    if (DEBUG) {
                        break
                    }
                }
            } catch (_: SocketException) {
            } catch (e: Throwable) {
                println("Exception: ${e.message} ")
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

