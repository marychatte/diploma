package run

import ktor.ActorSelectorManagerServer

suspend fun runKtorServer() = runServer {
    val server = ActorSelectorManagerServer()
    server.start()
}
