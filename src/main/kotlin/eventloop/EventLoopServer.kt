package eventloop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import utils.DATA_BUFFER
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class EventLoopServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
//        configureBlocking(false) todo
        bind(InetSocketAddress(serverPort), numberOfClients + 1)
    }
    private val eventLoopManager = EventLoopManager()

    var timeNano: Long = -1
        private set

    suspend fun start() {
        coroutineScope {
            for (i in 1..numberOfClients) {
                processClient(this)
            }
        }
        timeNano = System.nanoTime() - timeNano
    }

    private suspend fun processClient(scope: CoroutineScope) {
        var clientAccepted = false
        while (!clientAccepted) {
            val clientChannel = eventLoopManager.addEvent {
                serverSocketChannel.accept()
            } as SocketChannel?

            clientChannel?.apply { configureBlocking(false) } ?: continue
            clientAccepted = true


            if (timeNano == -1L) {
                timeNano = System.nanoTime()
            }

            val writeJob = scope.launch {
                clientChannel.writeTo(eventLoopManager, DATA_BUFFER.duplicate())
            }
            val readJob = scope.launch {
                clientChannel.readFrom(eventLoopManager)
            }

            writeJob.invokeOnCompletion {
                readJob.invokeOnCompletion {
                    clientChannel.close()
                }
            }
        }
    }

    fun stop() {
        eventLoopManager.close()
        serverSocketChannel.close()
    }
}
