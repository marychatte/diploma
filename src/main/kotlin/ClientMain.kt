import blocking.BlockingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.COUNT_OF_CLIENTS
import utils.COUNT_OF_ITERATIONS
import utils.SERVER_ADDRESS
import utils.SERVER_PORT

fun main() = runBlocking {
    repeat(COUNT_OF_ITERATIONS) {
        runClients(this, COUNT_OF_CLIENTS)
    }
}

private suspend fun runClients(scope: CoroutineScope, countOfClients: Int) {
    (1..countOfClients).map {
        Thread {
            try {
                scope.launch {
                    val client = BlockingClient(SERVER_ADDRESS, SERVER_PORT)
                    client.start()
                }
            } catch (e: Exception) {
                println("Exception in client $it: $e")
            }
        }.apply { start() }
    }.forEach { it.join() }
}
