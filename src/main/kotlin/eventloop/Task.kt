package eventloop

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 * A task for the event loop
 *
 * It contains a runnable, which perform an i/o operation,
 * and a continuation, that will be resumed with the result
 */
data class Task<T>(
    val continuation: CancellableContinuation<T>,
    val runnable: suspend () -> T,
) {
    suspend fun runAndResume() {
        try {
            val result = runnable.invoke()
            continuation.resume(result)
        } catch (e: Throwable) {
            continuation.cancel(e)
        }
    }
}
