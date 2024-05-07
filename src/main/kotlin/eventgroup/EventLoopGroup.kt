package eventgroup

import eventloop.Connection
import eventloop.RegisteredServerChannel
import eventloop.attachment
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

/**
 * Contains a several event loops, every event loop is running on single thread
 */
class EventLoopGroup(
    private val maxLoops: Int,
    private val channels: List<ServerSocketChannel>,
    private val processConnection: suspend (channel: Connection, scope: CoroutineScope) -> Unit,
) {
    private val acceptLoop = SimpleEventLoopImpl()
    private val loopIndex = atomic(0)
    private val loops = mutableListOf<SimpleEventLoopImpl>()

    init {
        acceptLoop.run()

        repeat(maxLoops - 1) {
            val next = SimpleEventLoopImpl().apply { run() }
            loops.add(next)
        }
    }

    private suspend fun registerAcceptKey(channel: AbstractSelectableChannel, interest: Int) = acceptLoop.runOnLoopSuspend {
        acceptLoop.addInterest(channel, interest)
    }

    suspend fun start() {
        channels.map { channel ->
            val key = registerAcceptKey(channel, SelectionKey.OP_ACCEPT)

            RegisteredServerChannelImpl(channel, key)
        }.map { registered ->
            acceptLoop.scope.launch {
                while (true) {
                    val connection = registered.acceptConnection()

                    processConnection(connection, connection.loop.scope)
                }
            }
        }.joinAll()
    }

    private inner class RegisteredServerChannelImpl(
        private val serverChannel: ServerSocketChannel,
        private val key: SelectionKey,
    ) : RegisteredServerChannel {
        override suspend fun acceptConnection(): ConnectionImpl {
            val result = key.attachment.runTask(SelectionKey.OP_ACCEPT) {
                serverChannel.accept().also {
                    it.configureBlocking(false)
                }
            }

            val nextLoopIndex = loopIndex.getAndIncrement() % (maxLoops - 1)

            return ConnectionImpl(result, loops[nextLoopIndex])
        }
    }

    private class ConnectionImpl(
        private val channel: SocketChannel,
        val loop: SimpleEventLoopImpl,
    ) : Connection {
        override suspend fun performRead(body: (SocketChannel) -> Unit) {
            runTask(SelectionKey.OP_READ) { body(channel) }
        }

        override suspend fun performWrite(body: (SocketChannel) -> Unit) {
            runTask(SelectionKey.OP_WRITE) { body(channel) }
        }

        override fun close() {
            channel.close()
        }

        private suspend fun <T> runTask(interest: Int, body: suspend () -> T): T {
            val key = loop.addInterest(channel, interest)
            return key.attachment.runTask(interest, body).also {
                loop.deleteInterest(key, interest)
            }
        }
    }
}
