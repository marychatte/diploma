package run

import blocking.BlockingServer

suspend fun runBlockingServer() = runServer {
    val server = BlockingServer()
    server.start()
    server.stop()
}