package nonblocking

import kotlinx.coroutines.*
import java.io.Closeable
import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReactorSelectorManager : CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("ReactorSelectorManager")

    private val selector: Selector = Selector.open()

    private val job: Job = launch {
        while (true) {
            selector.select()
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
        }
    }

    suspend fun select(selectionKey: SelectionKey, interest: Int) {
        suspendCoroutine { continuation ->
            (selectionKey.attachment() as Attachment).apply {
                addContinuation(interest, continuation)
            }
        }
    }

    fun addInterest(channel: SelectableChannel, interest: Int): SelectionKey {
        val result = channel.keyFor(selector)?.apply {
            interestOpsOr(interest)
        } ?: let {
            channel.register(selector, interest, Attachment())
        }
        selector.wakeup()
        return result
    }

    fun deleteInterest(selectionKey: SelectionKey, interest: Int) {
        selectionKey.interestOpsAnd(interest.inv())
    }

    override fun close() {
        job.cancel()
        job.invokeOnCompletion { selector.close() }
        coroutineContext.cancel()
    }
}

private class Attachment {
    val interestToContinuationMap: ConcurrentHashMap<Int, Continuation<Unit>> = ConcurrentHashMap()

    fun addContinuation(interest: Int, continuation: Continuation<Unit>) {
        interestToContinuationMap[interest] = continuation
    }

    fun resumeContinuation(key: SelectionKey) {
        when {
            key.isReadable -> resumeContinuation(SelectionKey.OP_READ)
            key.isWritable -> resumeContinuation(SelectionKey.OP_WRITE)
            key.isAcceptable -> resumeContinuation(SelectionKey.OP_ACCEPT)
            key.isConnectable -> resumeContinuation(SelectionKey.OP_CONNECT)
        }
    }

    fun resumeContinuation(interest: Int) {
        val continuation = interestToContinuationMap[interest] ?: return
        interestToContinuationMap.remove(interest)
        continuation.resume(Unit)
    }
}