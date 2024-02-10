package run

import nonblocking.NonBlockingServer
import utils.COUNT_OF_CLIENTS
import utils.SERVER_PORT

suspend fun main() = runServer {
    val server = NonBlockingServer(COUNT_OF_CLIENTS, SERVER_PORT)
    server.start()
    server.stop()
    return@runServer server.timeNano
}
