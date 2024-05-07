package run

import reactor.ReactorServer

fun runReactorServer() = runServer {
    val server = ReactorServer()
    server.start()
    server.stop()
}
