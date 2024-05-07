package run

import kotlinx.coroutines.delay
import netty.NettyServer

fun runNettyServer() = runServer {
    val server = NettyServer()
    server.start()
    delay(5000)
    server.stop()
}