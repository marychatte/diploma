package run

import reactor.ReactorServer
import utils.COUNT_OF_CLIENTS
import utils.SERVER_PORT

suspend fun runReactorServer() = runServer("reactor") {
    val server = ReactorServer(SERVER_PORT)
    server.start()
    server.stop()
    return@runServer server.timeNano
}
