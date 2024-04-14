package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import utils.REQUEST_SIZE
import utils.RESPONSE_BUFFER
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel

class EventLoopServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val serverChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        bind(InetSocketAddress(serverPort), numberOfClients + 1)
        configureBlocking(false)
    }

    private val eventLoop = EventLoopImpl()

    suspend fun start() {
        eventLoop.runOn(CoroutineScope(Dispatchers.IO))

        coroutineScope {
            val channel = eventLoop.register(serverChannel)

            for (i in 1..numberOfClients) {
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
            connection.performWrite { it.write(RESPONSE_BUFFER.duplicate()) }

            val readBuffer = ByteBuffer.allocate(REQUEST_SIZE)
            connection.performRead {
                it.read(readBuffer)
            }
        }
    }
}

