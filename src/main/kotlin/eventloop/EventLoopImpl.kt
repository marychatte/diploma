package eventloop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.channels.*
import java.nio.channels.spi.AbstractSelectableChannel
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class EventLoopImpl : EventLoop, CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    private val taskChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    private val selector = Selector.open()

    init {
        launch {
            run()
        }
    }

    private suspend fun run() {
        while (true) {
            runAllPendingTasks()

            withContext(Dispatchers.IO) {
                selector.select(SELECTOR_TIMEOUT_MILLIS)
            }

            val selectionKeys = selector.selectedKeys().iterator()
            while (selectionKeys.hasNext()) {
                val key = selectionKeys.next()
                selectionKeys.remove()
                try {
                    if (!key.isValid) continue
                    val attachment = key.attachment() as Attachment
                    attachment.runTaskAndResumeContinuation(key)
                } catch (e: CancelledKeyException) {
                    withContext(Dispatchers.IO) {
                        key.channel().close()
                    }
                }
            }
        }
    }

    private suspend fun runAllPendingTasks() {
        while (true) {
            val task = taskChannel.tryReceive()

            if (!task.isSuccess) {
                break
            }

            task.getOrNull()?.invoke()
        }
    }

    override suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel {
        val key = registerKey(channel, SelectionKey.OP_ACCEPT)

        return RegisteredServerChannelImpl(channel, key)
    }

    override suspend fun acceptConnection(channel: SocketChannel): Connection {
        val key = registerKey(channel, SelectionKey.OP_CONNECT)

        key.runTask(SelectionKey.OP_CONNECT) {
            channel.finishConnect()
        }

        return ConnectionImpl(channel)
    }

    private suspend fun registerKey(
        channel: AbstractSelectableChannel,
        interest: Int
    ) = runOnLoopSuspend {
        withContext(Dispatchers.IO) {
            addInterest(channel, interest)
        }
    }

    private suspend fun <T> runOnLoopSuspend(body: suspend () -> T): T {
        val result = CompletableDeferred<T>()

        taskChannel.send { result.complete(body()) }

        return result.await()
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

    private suspend fun <T> SelectionKey.runTask(interest: Int, body: suspend () -> T): T {
        return (attachment() as Attachment).runTask(interest, body)
    }

    private inner class RegisteredServerChannelImpl(
        private val serverChannel: ServerSocketChannel,
        private val key: SelectionKey
    ) : RegisteredServerChannel {
        override suspend fun acceptConnection(): Connection {
            val result = key.runTask(SelectionKey.OP_ACCEPT) {
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

        private suspend fun <T> runTask(interest: Int, body: suspend () -> T): T {
            val key = addInterest(channel, interest)
            return (key.attachment() as Attachment).runTask(interest, body).also {
                deleteInterest(key, interest)
            }
        }
    }

    override fun close() {
        taskChannel.close()
        selector.close()
    }

    companion object {
        private const val SELECTOR_TIMEOUT_MILLIS = 100L
    }
}

private class Attachment {
    private var acceptTask: Task<Any?>? = null
    private var readTask: Task<Any?>? = null
    private var writeTask: Task<Any?>? = null
    private var connectTask: Task<Any?>? = null

    suspend fun <T> runTask(interest: Int, task: (suspend () -> T)): T {
        return suspendCoroutine {
            @Suppress("UNCHECKED_CAST")
            setContinuationByInterest(interest, Task(it, task) as Task<Any?>)
        }
    }

    suspend fun runTaskAndResumeContinuation(key: SelectionKey) {
        when {
            key.isAcceptable -> acceptTask.runAndResume(SelectionKey.OP_ACCEPT)
            key.isReadable -> readTask.runAndResume(SelectionKey.OP_READ)
            key.isWritable -> writeTask.runAndResume(SelectionKey.OP_WRITE)
            key.isConnectable -> connectTask.runAndResume(SelectionKey.OP_CONNECT)
        }
    }

    private suspend fun Task<Any?>?.runAndResume(interest: Int) {
        val tmp = this ?: return
        setContinuationByInterest(interest, null)
        tmp.runAndResume()
    }

    private fun setContinuationByInterest(interest: Int, task: Task<Any?>?) {
        when (interest) {
            SelectionKey.OP_ACCEPT -> acceptTask = task
            SelectionKey.OP_READ -> readTask = task
            SelectionKey.OP_WRITE -> writeTask = task
            SelectionKey.OP_CONNECT -> connectTask = task
        }
    }

    private data class Task<T>(
        val continuation: Continuation<T>,
        val task: suspend () -> T,
    ) {
        suspend fun runAndResume() {
            val result = task.invoke()
            continuation.resume(result)
        }
    }
}
