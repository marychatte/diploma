package run

import blocking.BlockingServer
import utils.COUNT_OF_CLIENTS
import utils.SERVER_PORT

suspend fun runBlockingServer() = runServer("blocking") {
    val server = BlockingServer(COUNT_OF_CLIENTS, SERVER_PORT)
    server.start()
    server.stop()
    return@runServer server.timeNano
}