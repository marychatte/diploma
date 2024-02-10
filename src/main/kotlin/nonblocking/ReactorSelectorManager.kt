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
    private val selectionKeysToContinuation: ConcurrentHashMap<SelectableChannel, ConcurrentHashMap<Int, Continuation<Unit>>> =
        ConcurrentHashMap()

    private val job: Job = launch {
        while (true) {
            selector.select()
            val selectionKeys = selector.selectedKeys().iterator()
            while (selectionKeys.hasNext()) {
                val key = selectionKeys.next()
                selectionKeys.remove()
                try {
                    if (!key.isValid) continue
                    processKey(key)
                } catch (e: CancelledKeyException) {
                    key.channel().close()
                }
            }
        }
    }

    private fun processKey(key: SelectionKey) {
        val keyMap = selectionKeysToContinuation[key.channel()] ?: return
        when {
            key.isReadable -> resumeContinuation(keyMap, SelectionKey.OP_READ)
            key.isWritable -> resumeContinuation(keyMap, SelectionKey.OP_WRITE)
            key.isAcceptable -> resumeContinuation(keyMap, SelectionKey.OP_ACCEPT)
            key.isConnectable -> resumeContinuation(keyMap, SelectionKey.OP_CONNECT)
        }
    }

    private fun resumeContinuation(keyMap: ConcurrentHashMap<Int, Continuation<Unit>>, interest: Int) {
        keyMap[interest]?.let { continuation ->
            keyMap.remove(interest)
            continuation.resume(Unit)
        }
    }

    suspend fun select(selectionKey: SelectionKey, interest: Int) {
        suspendCoroutine { continuation ->
            selectionKeysToContinuation.putIfAbsent(selectionKey.channel(), ConcurrentHashMap())
            selectionKeysToContinuation[selectionKey.channel()]?.set(interest, continuation)
        }
    }

    fun addInterest(channel: SelectableChannel, interest: Int): SelectionKey {
        val result = channel.keyFor(selector)?.apply {
            interestOpsOr(interest)
        } ?: let {
            channel.register(selector, interest)
        }
        selector.wakeup()
        return result
    }

    fun deleteInterest(selectionKey: SelectionKey, interest: Int) {
        selectionKey.interestOpsAnd(interest.inv())
    }

    override fun close() {
        selector.close()
        job.cancel()
        coroutineContext.cancel()
    }
}
