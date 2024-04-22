package run

import ktor.KtorServer

suspend fun runKtorServer() = runServer {
    KtorServer().start()
}
