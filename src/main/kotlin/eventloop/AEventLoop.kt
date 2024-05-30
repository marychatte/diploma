package eventloop

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import java.nio.channels.*

@Suppress("FunctionName")
abstract class AEventLoop : IEventLoop {
    private val taskQueue = ArrayDeque<Task<*>>()

    private val selector = Selector.open()

    override fun close(cause: Throwable?) {
        taskQueue.forEach { it.continuation.cancel(cause) }
        selector.close()
    }

    protected suspend fun _run() {
        while (true) {
            runAllPendingTasks()

            val n = selector.select(SELECTOR_TIMEOUT_MILLIS)
            yield()

            if (n == 0) {
                continue
            }

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
        return result
    }

    internal fun deleteInterest(selectionKey: SelectionKey, interest: Int) {
        selectionKey.interestOpsAnd(interest.inv())
    }

    companion object {
        private const val SELECTOR_TIMEOUT_MILLIS = 20L
    }
}
