package run

import kotlinx.coroutines.delay
import netty.NettyServer
import utils.SERVER_PORT

fun runNettyServer() = runServer {
    val server = NettyServer(SERVER_PORT)
    server.start()
    delay(5000)
    server.stop()
}