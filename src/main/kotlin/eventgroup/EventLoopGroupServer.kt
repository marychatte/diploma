package eventgroup

import eventloop.AEventLoopServer
import kotlinx.coroutines.coroutineScope

class EventLoopGroupServer(serverPort: Int) : AEventLoopServer(serverPort) {
    override suspend fun start() {
        val eventLoop = EventLoopGroup(maxLoops = 3)
        coroutineScope {
            val channel = eventLoop.register(serverChannel)

            while (true) {
                processClient(channel, this)
            }
        }
    }
}
