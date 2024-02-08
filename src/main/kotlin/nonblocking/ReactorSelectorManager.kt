package nonblocking

import kotlinx.coroutines.*
import java.io.Closeable
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
        while (isActive && selector.isOpen) {
            selector.select()
            val selectionKeys = selector.selectedKeys().iterator()
            while (selectionKeys.hasNext()) {
                val key = selectionKeys.next()
                selectionKeys.remove()
                if (!key.isValid) {
                    continue
                }

                val keyMap = selectionKeysToContinuation[key.channel()] ?: continue
                if (key.isReadable) {
                    keyMap[SelectionKey.OP_READ]?.let { continuation ->
                        keyMap.remove(SelectionKey.OP_READ)
                        continuation.resume(Unit)
                    }
                }
                if (key.isWritable) {
                    keyMap[SelectionKey.OP_WRITE]?.let { continuation ->
                        keyMap.remove(SelectionKey.OP_WRITE)
                        continuation.resume(Unit)
                    }
                }
                if (key.isAcceptable) {
                    keyMap[SelectionKey.OP_ACCEPT]?.let { continuation ->
                        keyMap.remove(SelectionKey.OP_ACCEPT)
                        continuation.resume(Unit)
                    }
                }
                if (key.isConnectable) {
                    keyMap[SelectionKey.OP_CONNECT]?.let { continuation ->
                        keyMap.remove(SelectionKey.OP_CONNECT)
                        continuation.resume(Unit)
                    }
                }
            }
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
        job.cancel()
        job.invokeOnCompletion { selector.close() }
        coroutineContext.cancel()
    }
}
