package run

import blocking.BlockingServer

fun runBlockingServer() = runServer {
    val server = BlockingServer()
    server.start()
    server.stop()
}