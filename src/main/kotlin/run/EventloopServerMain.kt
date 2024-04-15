package run

import eventloop.EventLoopServer
import utils.SERVER_PORT

suspend fun runEventLoopServer() = runServer {
    val server = EventLoopServer(SERVER_PORT)
    server.start()
    server.stop()
}