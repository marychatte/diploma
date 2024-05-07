package eventloop

/**
 * Represents a server channel registered to an event loop with OP_ACCEPT interest
 */
interface RegisteredServerChannel {

    /**
     * Allows to accept connections on the server socket channel
     */
    suspend fun acceptConnection(): Connection
}
