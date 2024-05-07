package run

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS
import utils.SERVER_BACKLOG
import utils.SERVER_PORT
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

val selectorContext by lazy { newThreadContext(SELECTOR_THREADS_COUNT) }

val actorContext by lazy {
    when (ACTOR_THREADS_COUNT) {
        0 -> selectorContext // use same context for selector and actor
        else -> newThreadContext(ACTOR_THREADS_COUNT)
    }
}

fun runServer(context: CoroutineContext = actorContext, serverRun: suspend () -> Unit) =
    runBlocking(context) {
        serverRun()
    }

fun runClient(clientRun: () -> Unit) {
    (1..COUNT_OF_CLIENTS).map {
        Thread {
            try {
                clientRun()
            } catch (e: Exception) {
                println("Exception in client $it: $e")
            }
        }.apply { start() }
    }.forEach { it.join() }
}

fun newThreadContext(nThreads: Int): CoroutineContext {
    val pool = when (nThreads) {
        1 -> Executors.newSingleThreadExecutor()
        else -> Executors.newFixedThreadPool(nThreads)
    }

    return pool.asCoroutineDispatcher()
}

fun CoroutineContext.wrapInScope() = CoroutineScope(this)

fun serverChannels(config: ServerSocketChannel.() -> Unit = {}): List<ServerSocketChannel> {
    return ports().map { port ->
        ServerSocketChannel.open().apply {
            bind(InetSocketAddress(port), SERVER_BACKLOG)
            configureBlocking(false)

            config()
        }
    }
}

fun ports() = List(N_LISTENERS) { SERVER_PORT + it }
