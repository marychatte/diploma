package eventloop

import kotlinx.coroutines.CoroutineScope
import java.nio.channels.ServerSocketChannel

/**
 * All submitted tasks will be executed in the context of the event loop
 */
interface EventLoop : IEventLoop {
    /**
     * Register server socket channel with OP_ACCEPT interest
     */
    suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel
}
