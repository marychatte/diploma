package reactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import utils.RESPONSE_BUFFER
import utils.checkRequest
import utils.isHttRequest
import java.net.InetSocketAddress
import java.net.StandardSocketOptions
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class ReactorServer(private val serverPort: Int) {
    private val serverSocketChannel: ServerSocketChannel = ServerSocketChannel.open().apply {
        configureBlocking(false)
        bind(InetSocketAddress(serverPort))
        setOption(StandardSocketOptions.SO_REUSEPORT, true)
        setOption(StandardSocketOptions.SO_REUSEADDR, true)
    }

    suspend fun start() {
        supervisorScope {
            val selectorManager = ReactorSelectorManager()
            selectorManager.runOn(this)

            val selectionKeyAccept = selectorManager.addInterest(serverSocketChannel, SelectionKey.OP_ACCEPT)

            var i = 1
            while (true) {
                try {
                    processClient(this, selectorManager, selectionKeyAccept, i)
                } catch (e: Throwable) {
                    println("processClient exception: ${e.message}")
                }
                i++
            }

            selectorManager.cancel()
        }
    }

    private suspend fun processClient(
        scope: CoroutineScope,
        selectorManager: ReactorSelectorManager,
        acceptSelectionKey: SelectionKey,
        i: Int,
    ) {
        selectorManager.select(acceptSelectionKey, SelectionKey.OP_ACCEPT)

        val clientChannel = serverSocketChannel.accept()
            ?.apply { configureBlocking(false) }
            ?: return

        scope.launch {
            val selectionKey = selectorManager.addInterest(
                clientChannel,
                SelectionKey.OP_READ or SelectionKey.OP_WRITE
            )
            try {
                while (true) {
                    val receivedBuffer = clientChannel.readFrom(selectionKey, selectorManager)

                    if (!receivedBuffer.isHttRequest()) break
                    receivedBuffer.checkRequest()

                    clientChannel.writeTo(selectionKey, selectorManager, RESPONSE_BUFFER.duplicate())
                }
            } catch (e: Throwable) {
                println("Exception: ${e.message}")
            }
            selectionKey.cancel()
            clientChannel.close()
        }
    }

    fun stop() {
        serverSocketChannel.close()
    }
}
