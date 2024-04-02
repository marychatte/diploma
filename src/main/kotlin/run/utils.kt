package run

import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS
import utils.COUNT_OF_ITERATIONS
import utils.FILE_NAME
import utils.ResultWriter

suspend fun runServer(serverRun: suspend () -> Long) = runBlocking {
    val time = serverRun()
    ResultWriter(
        listOf(time),
        COUNT_OF_CLIENTS,
        FILE_NAME,
        COUNT_OF_ITERATIONS
    ).write()
}

fun runClient(clientRun: () -> Unit)  {
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