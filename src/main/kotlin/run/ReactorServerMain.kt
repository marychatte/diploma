package run

import reactor.ReactorServer
import utils.SERVER_PORT

suspend fun runReactorServer() = runServer {
    val server = ReactorServer(SERVER_PORT)
    server.start()
    server.stop()
}
