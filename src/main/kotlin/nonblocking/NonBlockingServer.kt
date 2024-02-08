package nonblocking

import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class NonBlockingServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val scope = CoroutineScope(Dispatchers.Default) + CoroutineName("NonBlockingServer")

    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        configureBlocking(false)
        bind(InetSocketAddress(serverPort), numberOfClients + 1)
    }
    private val selectorManager = ReactorSelectorManager()

    var timeNano: Long = -1
        private set

    suspend fun start() {
        val selectionKeyAccept = selectorManager.addInterest(serverSocketChannel, SelectionKey.OP_ACCEPT)

        withContext(scope.coroutineContext) {
            for (i in 1..numberOfClients) {
                processClient(this, selectionKeyAccept)
            }
        }
        selectorManager.deleteInterest(selectionKeyAccept, SelectionKey.OP_ACCEPT)
        timeNano = System.nanoTime() - timeNano
    }

    private suspend fun processClient(innerScope: CoroutineScope, acceptSelectionKey: SelectionKey) {
        selectorManager.select(acceptSelectionKey, SelectionKey.OP_ACCEPT)
        val clientChannel = serverSocketChannel.accept().apply { configureBlocking(false) }

        if (timeNano == -1L) {
            timeNano = System.nanoTime()
        }

        val selectionKey = selectorManager.addInterest(clientChannel, SelectionKey.OP_READ or SelectionKey.OP_WRITE)
        innerScope.launch {
            clientChannel.writeTo(selectionKey, selectorManager)
        }
        innerScope.launch {
            clientChannel.readFrom(selectionKey, selectorManager)
        }
    }

    fun stop() {
        selectorManager.close()
        serverSocketChannel.close()
        scope.cancel()
    }
}
