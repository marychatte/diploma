package run

import eventgroup.EventLoopGroupServer
import eventloop.EventLoopServer
import utils.SERVER_PORT

fun runEventLoopServer() = runServer {
    val server = EventLoopServer(SERVER_PORT)
    server.start()
    server.stop()
}

fun runEventLoopGroupServer() = runServer {
    val server = EventLoopGroupServer(SERVER_PORT)
    server.start()
    server.stop()
}
