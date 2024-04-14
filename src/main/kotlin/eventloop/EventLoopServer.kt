package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import utils.DATA_ARRAY_SIZE
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel

class EventLoopServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val serverChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        bind(InetSocketAddress(serverPort), numberOfClients + 1)
        configureBlocking(false)
    }

    var timeNano: Long = -1
        private set

    suspend fun start() {
        val loop = EventLoopImpl()
        coroutineScope {
            loop.runOn(this)

            val channel = loop.register(serverChannel)

            for (i in 1..numberOfClients) {
                processClient(channel, this)
            }
        }
        loop.close()
        timeNano = System.nanoTime() - timeNano
    }

    private suspend fun processClient(channel: RegisteredServerChannel, serverScope: CoroutineScope) {
        val connection = channel.acceptConnection()
        if (timeNano == -1L) {
            timeNano = System.nanoTime()
        }

        serverScope.launch {
            val buffer = ByteBuffer.allocate(DATA_ARRAY_SIZE)
            connection.performWrite { it.write(buffer) }

            val readBuffer = ByteBuffer.allocate(DATA_ARRAY_SIZE)
            connection.performRead {
                it.read(readBuffer)
            }

            assert(readBuffer.array().contentEquals(buffer.array()))
        }
    }
}

