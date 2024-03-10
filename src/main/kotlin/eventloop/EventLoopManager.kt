package eventloop

import kotlinx.coroutines.*
import nonblocking.Attachment
import java.io.Closeable
import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectableChannel
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EventLoopManager : CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName(this::class.simpleName!!)

    private val eventQueue: Queue<Event> = ConcurrentLinkedQueue()
    private val selector = Selector.open()

    private val job: Job = launch {
        while (true) {
            selector.select(TIMEOUT)
            val selectionKeys = selector.selectedKeys().iterator()
            while (selectionKeys.hasNext()) {
                val key = selectionKeys.next()
                selectionKeys.remove()
                try {
                    if (!key.isValid) continue

                    val attachment = key.attachment() as Attachment
                    attachment.resumeContinuation(key)
                } catch (e: CancelledKeyException) {
                    key.channel().close()
                }
            }

            val event = eventQueue.poll() ?: continue
            event.continuation.resume(event.action())
        }
    }

    suspend fun addEvent(channel: SelectableChannel,interestOps: Int, event: Event): Any {
        launch {
            channel.register(selector, interestOps)
        }
        return suspendCoroutine { continuation ->
            eventQueue.add(Event(action, continuation))
        }
    }

    override fun close() {
        job.cancel()
        coroutineContext.cancel()
    }

    companion object {
        private const val TIMEOUT = 100L
    }
}

class Event(val action: () -> Any, val continuation: Continuation<Any>)