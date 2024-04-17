package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

    private val eventLoop = EventLoopImpl()

    suspend fun start() {
        eventLoop.runOn(CoroutineScope(Dispatchers.IO))

        coroutineScope {
            val channel = eventLoop.register(serverChannel)

            while (true) {
                processClient(channel, this)
            }
        }
    }

    fun stop() {
        eventLoop.close()
        serverChannel.close()
    }

    private suspend fun processClient(channel: RegisteredServerChannel, serverScope: CoroutineScope) {
        val connection = channel.acceptConnection()

        serverScope.launch {
            try {
                while (true) {
                    val receivedRequest = ByteBuffer.allocate(REQUEST_SIZE)
                    connection.performRead {
                        it.read(receivedRequest)
                    }

                    if (!DEBUG) {
                        if (!receivedRequest.isHttRequest()) break
                    }
                    if (DEBUG) {
                        receivedRequest.checkRequest()
                    }

                    connection.performWrite { it.write(RESPONSE_BUFFER.duplicate()) }

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
}

