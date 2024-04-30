package run

import ktor.ActorSelectorManagerServer
import ktor.KtorServer

suspend fun runKtorServer() = runServer {
    KtorServer().start()
}

suspend fun runAsmServer() = runServer {
    ActorSelectorManagerServer().start()
}
