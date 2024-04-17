package eventloop

import kotlinx.coroutines.CoroutineScope
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * All submitted tasks will be executed in the context of the event loop
 */
interface EventLoop {
    /**
     * Start event loop on the provided scope
     */
    fun runOn(scope: CoroutineScope)

    /**
     * Register server socket channel with OP_ACCEPT interest
     */
    suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel

    /**
     * Accept connection on the provided channel
     */
    suspend fun acceptConnection(channel: SocketChannel): Connection

    fun close(cause: Throwable? = null)
}

/**
 * Represents a server channel registered to an event loop with OP_ACCEPT interest
 */
interface RegisteredServerChannel {

    /**
     * Allows to accept connections on the server socket channel
     */
    suspend fun acceptConnection(): Connection
}

/**
 * Allows to perform read and write operations on the socket channel,
 * which will be submitted as tasks to the event loop and will be suspended until
 * they will be executed in the context of the event loop
 */
interface Connection {
    suspend fun performRead(body: (SocketChannel) -> Unit)

    suspend fun performWrite(body: (SocketChannel) -> Unit)

    fun close()
}