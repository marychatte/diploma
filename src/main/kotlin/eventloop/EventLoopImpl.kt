package eventloop

import kotlinx.coroutines.*
import java.nio.channels.*
import java.nio.channels.spi.AbstractSelectableChannel

class EventLoopImpl : EventLoop {
    private val taskQueue = ArrayDeque<Task<*>>()

    private val selector = Selector.open()

    override fun close(cause: Throwable?) {
        taskQueue.forEach { it.continuation.cancel(cause) }
        selector.close()
    }

    override fun runOn(scope: CoroutineScope) {
        scope.launch {
            run()
        }
    }

    private suspend fun run() {
        while (true) {
            runAllPendingTasks()

            yield()
            selector.select(SELECTOR_TIMEOUT_MILLIS)

            val selectionKeys = selector.selectedKeys().iterator()
            while (selectionKeys.hasNext()) {
                val key = selectionKeys.next()
                selectionKeys.remove()

                try {
                    if (!key.isValid) continue
                    key.attachment.runTaskAndResumeContinuation(key)
                } catch (e: Throwable) {
                    key.channel().close()
                    key.attachment.cancel(e)
                }
            }
        }
    }

    private suspend fun runAllPendingTasks() {
        repeat(taskQueue.size) {
            taskQueue.removeFirst().runAndResume()
        }
    }

    private suspend fun <T> runOnLoopSuspend(body: suspend () -> T): T = suspendCancellableCoroutine { continuation ->
        taskQueue.addLast(Task(continuation, body))
    }

    override suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel {
        val key = registerKey(channel, SelectionKey.OP_ACCEPT)

        return RegisteredServerChannelImpl(channel, key)
    }

    override suspend fun acceptConnection(channel: SocketChannel): Connection {
        val key = registerKey(channel, SelectionKey.OP_CONNECT)

        key.attachment.runTask(SelectionKey.OP_CONNECT) {
            channel.finishConnect()
        }

        return ConnectionImpl(channel)
    }

    private suspend fun registerKey(channel: AbstractSelectableChannel, interest: Int) = runOnLoopSuspend {
        addInterest(channel, interest)
    }

    private fun addInterest(channel: SelectableChannel, interest: Int): SelectionKey {
        val result = channel.keyFor(selector)?.apply {
            interestOpsOr(interest)
        } ?: let {
            channel.register(selector, interest, Attachment())
        }
        selector.wakeup()
        return result
    }

    private fun deleteInterest(selectionKey: SelectionKey, interest: Int) {
        selectionKey.interestOpsAnd(interest.inv())
    }

    private inner class RegisteredServerChannelImpl(
        private val serverChannel: ServerSocketChannel,
        private val key: SelectionKey
    ) : RegisteredServerChannel {
        override suspend fun acceptConnection(): Connection {
            val result = key.attachment.runTask(SelectionKey.OP_ACCEPT) {
                withContext(Dispatchers.IO) {
                    serverChannel.accept().also {
                        it.configureBlocking(false)
                    }
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

    companion object {
        private const val SELECTOR_TIMEOUT_MILLIS = 100L
    }
}
