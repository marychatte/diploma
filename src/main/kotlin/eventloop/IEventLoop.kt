package eventloop

import kotlinx.coroutines.Job

/**
 * All submitted tasks will be executed in the context of the event loop
 */
interface IEventLoop {
    /**
     * Start event loop
     */
    fun run(): Job

    fun close(cause: Throwable? = null)
}
