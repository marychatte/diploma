package eventgroup

import eventloop.Attachment
import eventloop.Task
import eventloop.attachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class SimpleEventLoopImpl : SimpleEventLoop {
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

    internal suspend fun <T> runOnLoopSuspend(body: suspend () -> T): T = suspendCancellableCoroutine { continuation ->
        taskQueue.addLast(Task(continuation, body))
    }

    internal fun addInterest(channel: SelectableChannel, interest: Int): SelectionKey {
        val result = channel.keyFor(selector)?.apply {
            interestOpsOr(interest)
        } ?: let {
            channel.register(selector, interest, Attachment())
        }
        selector.wakeup()
        return result
    }

    internal fun deleteInterest(selectionKey: SelectionKey, interest: Int) {
        selectionKey.interestOpsAnd(interest.inv())
    }

    companion object {
        private const val SELECTOR_TIMEOUT_MILLIS = 100L
    }
}
