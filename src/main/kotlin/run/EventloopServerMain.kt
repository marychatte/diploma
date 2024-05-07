package run

import eventgroup.EventLoopGroupServer
import eventloop.EventLoopServer

fun runEventLoopServer() = runServer {
    val server = EventLoopServer()
    server.start()
    server.stop()
}

fun runEventLoopGroupServer() = runServer {
    val server = EventLoopGroupServer()
    server.start()
    server.stop()
}
