package eventgroup

import eventloop.AEventLoopServer
import run.ACTOR_THREADS_COUNT
import run.SELECTOR_THREADS_COUNT

class EventLoopGroupServer : AEventLoopServer() {
    override suspend fun start() {
        if (ACTOR_THREADS_COUNT == 0) {
            error("EventGroup cannot share context with actors")
        }

        val group = EventLoopGroup(SELECTOR_THREADS_COUNT, serverChannels, ::processClient)

        group.start()
    }
}
