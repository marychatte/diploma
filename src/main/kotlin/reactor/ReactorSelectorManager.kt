package reactor

import kotlinx.coroutines.*
import java.io.Closeable
import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReactorSelectorManager {
    private val selector: Selector = Selector.open()

    private var runJob: Job? = null

    fun cancel() {
        runJob?.cancel()
    }

    fun runOn(scope: CoroutineScope) {
        runJob = scope.launch {
            run()
        }
    }

    private fun run() {
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
                } catch (e: Throwable) {
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
}

private class Attachment {
    private var acceptContinuation: Continuation<Unit>? = null
    private var readContinuation: Continuation<Unit>? = null
    private var writeContinuation: Continuation<Unit>? = null
    private var connectContinuation: Continuation<Unit>? = null

    fun addContinuation(interest: Int, continuation: Continuation<Unit>) {
        setContinuationByInterest(interest, continuation)
    }

    fun resumeContinuation(key: SelectionKey) {
        when {
            key.isAcceptable -> acceptContinuation.resume(SelectionKey.OP_ACCEPT)
            key.isReadable -> readContinuation.resume(SelectionKey.OP_READ)
            key.isWritable -> writeContinuation.resume(SelectionKey.OP_WRITE)
            key.isConnectable -> connectContinuation.resume(SelectionKey.OP_CONNECT)
        }
    }

    private fun Continuation<Unit>?.resume(interest: Int) {
        val tmp = this ?: return
        setContinuationByInterest(interest, null)
        tmp.resume(Unit)
    }

    private fun setContinuationByInterest(interest: Int, continuation: Continuation<Unit>?) {
        when (interest) {
            SelectionKey.OP_ACCEPT -> acceptContinuation = continuation
            SelectionKey.OP_READ -> readContinuation = continuation
            SelectionKey.OP_WRITE -> writeContinuation = continuation
            SelectionKey.OP_CONNECT -> connectContinuation = continuation
        }
    }
}