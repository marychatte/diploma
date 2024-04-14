package run

import kotlinx.coroutines.delay
import netty.NettyServer
import utils.COUNT_OF_CLIENTS
import utils.SERVER_PORT

suspend fun runNettyServer() = runServer {
    val server = NettyServer(COUNT_OF_CLIENTS, SERVER_PORT)
    server.start()
    delay(5000)
    server.stop()
}