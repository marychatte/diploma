package eventgroup

import eventloop.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import run.newThreadContext
import run.wrapInScope

class SimpleEventLoopImpl : AEventLoop(), IEventLoop {
    val scope = newThreadContext(nThreads = 1).wrapInScope()

    override fun run(): Job {
        return scope.launch { _run() }
    }
}
