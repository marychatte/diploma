package eventloop

import java.nio.channels.SocketChannel

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