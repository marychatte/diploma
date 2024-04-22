package run

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

val singleThreadContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

suspend fun runServer(context: CoroutineContext = singleThreadContext, serverRun: suspend () -> Unit) =
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
