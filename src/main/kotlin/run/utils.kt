package run

import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS

suspend fun runServer(serverRun: suspend () -> Unit) = runBlocking {
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
