package eventloop

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

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
