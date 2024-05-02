package run

import ktor.ActorSelectorManagerServer
import ktor.KtorServer

fun runKtorServer() = runServer {
    KtorServer().start()
}

fun runAsmServer() = runServer {
    ActorSelectorManagerServer().start()
}
