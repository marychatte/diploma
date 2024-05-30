package reactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import run.serverChannels
import utils.DEBUG
import utils.RESPONSE_BUFFER
import utils.checkRequest
import utils.isHttpRequest
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class ReactorServer {
    private val serverSocketChannels: List<ServerSocketChannel> = serverChannels()

    private val selectorManager = ReactorSelectorManager().apply { runOn() }

    suspend fun start() {
        val selectionAcceptKeys = serverSocketChannels.associateWith {
            selectorManager.addInterest(it, SelectionKey.OP_ACCEPT)
        }

        coroutineScope {
            val actorScope: CoroutineScope = this

            selectionAcceptKeys.forEach { (channel, selectionKey) ->
                launch {
                    while (true) {
                        processClient(actorScope, selectorManager, channel, selectionKey)
                    }
                }
            }
        }
    }

    private suspend fun processClient(
        actorScope: CoroutineScope,
        selectorManager: ReactorSelectorManager,
        serverChannel: ServerSocketChannel,
        acceptSelectionKey: SelectionKey,
    ) {
        selectorManager.select(acceptSelectionKey, SelectionKey.OP_ACCEPT)
        val clientChannel = serverChannel.accept()
            ?.apply {
                socket().tcpNoDelay = true
                configureBlocking(false)
            }
            ?: return

        actorScope.launch {
            val selectionKey = selectorManager.addInterest(
                clientChannel,
                SelectionKey.OP_READ or SelectionKey.OP_WRITE
            )

            try {
                while (true) {
                    val receivedBuffer = clientChannel.readFrom(selectionKey, selectorManager)
                    if (!receivedBuffer.isHttpRequest()) {
                        break
                    }
                    if (DEBUG) {
                        receivedBuffer.checkRequest()
                    }

                    clientChannel.writeTo(selectionKey, selectorManager, RESPONSE_BUFFER.duplicate())

                    if (DEBUG) {
                        break
                    }
                }
            } catch (_: Throwable) {
            } finally {
                selectionKey.cancel()
                clientChannel.close()
            }
        }
    }

    fun stop() {
        serverSocketChannels.forEach { it.close() }
    }
}
