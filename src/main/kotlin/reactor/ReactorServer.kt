package reactor

import kotlinx.coroutines.*
import utils.RESPONSE_BUFFER
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class ReactorServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        configureBlocking(false)
        bind(InetSocketAddress(serverPort), numberOfClients + 1)
    }
    private val selectorManager = ReactorSelectorManager()

    var timeNano: Long = -1
        private set

    suspend fun start() {
        val selectionKeyAccept = selectorManager.addInterest(serverSocketChannel, SelectionKey.OP_ACCEPT)

        coroutineScope {
            for (i in 1..numberOfClients) {
                processClient(this, selectionKeyAccept)
            }
        }
        selectorManager.deleteInterest(selectionKeyAccept, SelectionKey.OP_ACCEPT)
        timeNano = System.nanoTime() - timeNano
    }

    private suspend fun processClient(scope: CoroutineScope, acceptSelectionKey: SelectionKey) {
        var clientAccepted = false
        while (!clientAccepted) {
            selectorManager.select(acceptSelectionKey, SelectionKey.OP_ACCEPT)
            val clientChannel = serverSocketChannel.accept()
                ?.apply { configureBlocking(false) }
                ?: continue
            clientAccepted = true

            if (timeNano == -1L) {
                timeNano = System.nanoTime()
            }

            val selectionKey = selectorManager.addInterest(clientChannel, SelectionKey.OP_READ or SelectionKey.OP_WRITE)
            val writeJob = scope.launch {
                clientChannel.writeTo(selectionKey, selectorManager, RESPONSE_BUFFER.duplicate())
            }
            val readJob = scope.launch {
                clientChannel.readFrom(selectionKey, selectorManager)
            }

            writeJob.invokeOnCompletion {
                readJob.invokeOnCompletion {
                    selectionKey.cancel()
                    clientChannel.close()
                }
            }
        }
    }

    fun stop() {
        selectorManager.close()
        serverSocketChannel.close()
    }
}
