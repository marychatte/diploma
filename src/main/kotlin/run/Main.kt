package run

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

suspend fun main(args: Array<String>) {
    val parser = ArgParser("run")
    val service by parser.argument(ArgType.Choice<Service>(), description = "server or client")
    val type by parser.argument(ArgType.Choice<TYPE>(), description = "blocking, reactor, netty, eventloop, ktor")
    parser.parse(args)

    when (service) {
        Service.SERVER -> {
            when (type) {
                TYPE.BLOCKING -> runBlockingServer()
                TYPE.REACTOR -> runReactorServer()
                TYPE.NETTY -> runNettyServer()
                TYPE.EVENTLOOP -> runEventLoopServer()
                TYPE.KTOR -> runKtorServer()
                TYPE.ASM -> runAsmServer()
            }
        }

        Service.CLIENT -> {
            runBlockingClient()
        }
    }
}

enum class Service {
    SERVER,
    CLIENT,
}

enum class TYPE {
    BLOCKING,
    REACTOR,
    NETTY,
    EVENTLOOP,
    KTOR,
    ASM,
}