package run

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS
import utils.COUNT_OF_ITERATIONS
import utils.ResultWriter

suspend fun runServer(type: String, serverRun: suspend () -> Long) = runBlocking {
    val time = serverRun()
    ResultWriter(
        listOf(time),
        COUNT_OF_CLIENTS,
        "results/1_${type}_server_${COUNT_OF_CLIENTS}_blocking_clients.txt",
        COUNT_OF_ITERATIONS
    ).write()
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
