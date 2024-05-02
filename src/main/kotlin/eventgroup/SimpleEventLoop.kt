package eventgroup

import kotlinx.coroutines.CoroutineScope

/**
 * All submitted tasks will be executed in the context of the event loop
 */
interface SimpleEventLoop {
    /**
     * Start event loop on the provided scope
     */
    fun runOn(scope: CoroutineScope)

    fun close(cause: Throwable? = null)
}
