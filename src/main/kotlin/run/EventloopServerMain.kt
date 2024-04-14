package run

import eventloop.EventLoopServer
import utils.COUNT_OF_CLIENTS
import utils.SERVER_PORT

suspend fun runEventLoopServer() = runServer("eventloop") {
    val server = EventLoopServer(COUNT_OF_CLIENTS, SERVER_PORT)
    server.start()
    return@runServer server.timeNano
}