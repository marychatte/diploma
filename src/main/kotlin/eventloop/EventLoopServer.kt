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
    private val eventLoop: EventLoop = EventLoopImpl()

    var timeNano: Long = -1
        private set

    suspend fun start() {
        coroutineScope {
            val channel = eventLoop.register(serverChannel)
            for (i in 1..numberOfClients) {
                processClient(channel, this)
            }
        }
        timeNano = System.nanoTime() - timeNano
    }

    private suspend fun processClient(channel: RegisteredServerChannel, coroutineScope: CoroutineScope) {
        val connection = channel.acceptConnection()
        if (timeNano == -1L) {
            timeNano = System.nanoTime()
        }

        coroutineScope.launch {
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

