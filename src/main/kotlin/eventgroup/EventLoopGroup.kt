package eventgroup

import eventloop.Connection
import eventloop.RegisteredServerChannel
import eventloop.attachment
import kotlinx.atomicfu.atomic
import run.newSingleThreadScope
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

/**
 * Contains a several event loops, every event loop is running on single thread
 */
class EventLoopGroup(private val maxLoops: Int = 10) {
    private val loopIndex = atomic(0)
    private val acceptLoop = SimpleEventLoopImpl()
    private val loops = mutableListOf<SimpleEventLoopImpl>()

    init {
        acceptLoop.runOn(newSingleThreadScope())

        repeat(maxLoops - 1) {
            val next = SimpleEventLoopImpl().apply { runOn(newSingleThreadScope()) }
            loops.add(next)
        }
    }

    suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel {
        val key = registerKey(channel, SelectionKey.OP_ACCEPT)

        return RegisteredServerChannelImpl(channel, key)
    }

    private suspend fun registerKey(channel: AbstractSelectableChannel, interest: Int) = acceptLoop.runOnLoopSuspend {
        acceptLoop.addInterest(channel, interest)
    }

    private inner class RegisteredServerChannelImpl(
        private val serverChannel: ServerSocketChannel,
        private val key: SelectionKey,
    ) : RegisteredServerChannel {
        override suspend fun acceptConnection(): Connection {
            val result = key.attachment.runTask(SelectionKey.OP_ACCEPT) {
                serverChannel.accept().also {
                    it.configureBlocking(false)
                }
            }

            val index = loopIndex.getAndIncrement() % (maxLoops - 1)

            return ConnectionImpl(result, loops[index])
        }
    }

    private class ConnectionImpl(
        private val channel: SocketChannel,
        private val eventLoop: SimpleEventLoopImpl,
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
            val key = eventLoop.addInterest(channel, interest)
            return key.attachment.runTask(interest, body).also {
                eventLoop.deleteInterest(key, interest)
            }
        }
    }
}
