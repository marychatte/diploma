package run

import blocking.BlockingServer

suspend fun runBlockingServer() = runServer("blocking") {
    val server = BlockingServer()
    server.start()
    server.stop()
    return@runServer server.timeNano
}