import blocking.BlockingServer
import kotlinx.coroutines.runBlocking
import nonblocking.NonBlockingServer
import utils.*

fun main() = runBlocking {
    runServer(COUNT_OF_CLIENTS)
}

private suspend fun runServer(countOfClients: Int) {
    val times = mutableListOf<Long>()
    repeat(COUNT_OF_ITERATIONS) {
        times.add(startServer(countOfClients))
    }

    ResultWriter(
        times,
        countOfClients,
        FILE_NAME,
        COUNT_OF_ITERATIONS
    ).write()
}


private suspend fun startServer(countOfClients: Int): Long {
    val server = NonBlockingServer(countOfClients, SERVER_PORT)
//    val server = BlockingServer(countOfClients, SERVER_PORT)

    server.start()
    server.stop()
    return server.timeNano
}
