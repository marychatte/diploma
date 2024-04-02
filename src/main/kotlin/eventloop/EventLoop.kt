package eventloop

import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

interface EventLoop {
    suspend fun register(channel: ServerSocketChannel): RegisteredServerChannel

    suspend fun acceptConnection(channel: SocketChannel): Connection

    fun close()
}

interface RegisteredServerChannel {
    suspend fun acceptConnection(): Connection
}

interface Connection {
    suspend fun performRead(body: (SocketChannel) -> Unit)

    suspend fun performWrite(body: (SocketChannel) -> Unit)
}