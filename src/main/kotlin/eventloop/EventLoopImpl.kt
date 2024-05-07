package eventloop

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import run.selectorContext
import run.wrapInScope
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

class EventLoopImpl : AEventLoop(), EventLoop {
    private val taskQueue = ArrayDeque<Task<*>>()

    private val selector = Selector.open()

    override fun close(cause: Throwable?) {
        taskQueue.forEach { it.continuation.cancel(cause) }
        selector.close()
    }

    override fun run(): Job {
        return selectorContext.wrapInScope().launch {
            _run()
        }
    }

    override suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel {
        val key = registerAcceptKey(channel, SelectionKey.OP_ACCEPT)

        return RegisteredServerChannelImpl(channel, key)
    }

    private suspend fun registerAcceptKey(channel: AbstractSelectableChannel, interest: Int) = runOnLoopSuspend {
        addInterest(channel, interest)
    }

    private inner class RegisteredServerChannelImpl(
        private val serverChannel: ServerSocketChannel,
        private val key: SelectionKey
    ) : RegisteredServerChannel {
        override suspend fun acceptConnection(): Connection {
            val result = key.attachment.runTask(SelectionKey.OP_ACCEPT) {
                serverChannel.accept().also {
                    it.configureBlocking(false)
                }
            }

            return ConnectionImpl(result)
        }
    }

    private inner class ConnectionImpl(private val channel: SocketChannel) : Connection {
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
            val key = addInterest(channel, interest)
            return key.attachment.runTask(interest, body).also {
                deleteInterest(key, interest)
            }
        }
    }
}
