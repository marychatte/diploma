package run

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import utils.BODY_SIZE
import kotlin.properties.Delegates

val MAX_THREADS = Runtime.getRuntime().availableProcessors() - 2

var SELECTOR_THREADS_COUNT by Delegates.notNull<Int>()
    private set

var ACTOR_THREADS_COUNT by Delegates.notNull<Int>()
    private set

var N_LISTENERS by Delegates.notNull<Int>()
    private set

fun main(args: Array<String>) {
    val parser = ArgParser("run")
    val type by parser.argument(
        ArgType.Choice<TYPE>(),
        description = "blocking, reactor, netty, eventloop, eventgroup, asm"
    )

    val selectorThreadCount by parser.option(
        type = ArgType.Int,
        description = "Number of threads used for selector pattern",
        shortName = "s",
    ).required()

    val actorThreadCount by parser.option(
        type = ArgType.Int,
        description = """
            Number of threads used for processing requests. 
            If not set (availableProcessors - 2 - selectorThreadCount) is used.
            If value is 0 - same pool as for selector is used.
        """.trimIndent(),
        shortName = "a",
    )

    val bodySize by parser.option(
        type = ArgType.Int,
        description = """
            Size of the body in bytes. Default is 3072.
        """.trimIndent(),
        shortName = "b",
    ).default(3072)

    val nListeners by parser.option(
        type = ArgType.Int,
        description = """
            Number of ports to listen on (starring from 12345). Default is 1.
        """.trimIndent(),
        shortName = "l",
    ).default(1)

    parser.parse(args)

    SELECTOR_THREADS_COUNT = selectorThreadCount
    ACTOR_THREADS_COUNT = actorThreadCount ?: (MAX_THREADS - selectorThreadCount)
    BODY_SIZE = bodySize
    N_LISTENERS = nListeners

    println(
        """
        Running ${type.name.lowercase()} with: 
          - $SELECTOR_THREADS_COUNT selector thread(s) 
          - $ACTOR_THREADS_COUNT actor thread(s) ${if (actorThreadCount == 0) "(same pool as for selector)" else ""}
          - Body size of ${BODY_SIZE}b
          - On port(s): ${(0..<N_LISTENERS).joinToString { (12345 + it).toString() }}
          
    """.trimIndent()
    )

    when (type) {
        TYPE.BLOCKING -> runBlockingServer()
        TYPE.REACTOR -> runReactorServer()
        TYPE.NETTY -> runNettyServer()
        TYPE.EVENTLOOP -> runEventLoopServer()
        TYPE.EVENTGROUP -> runEventLoopGroupServer()
        TYPE.KTOR -> runKtorServer()
        TYPE.ASM -> runAsmServer()
    }
}

enum class TYPE {
    BLOCKING,
    REACTOR,
    NETTY,
    EVENTLOOP,
    EVENTGROUP,
    KTOR,
    ASM,
}