package run

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

suspend fun main(args: Array<String>) {
    val parser = ArgParser("run")
    val service by parser.argument(ArgType.Choice<Service>(), description = "server or client")
    val type by parser.argument(ArgType.Choice<TYPE>(), description = "blocking or nonblocking")
    parser.parse(args)

    when (service) {
        Service.SERVER -> {
            when (type) {
                TYPE.BLOCKING -> runBlockingServer()
                TYPE.NONBLOCKING -> runNonBlockingServer()
            }
        }

        Service.CLIENT -> {
            when (type) {
                TYPE.BLOCKING -> runBlockingClient()
                TYPE.NONBLOCKING -> TODO("not implemented")
            }
        }
    }
}

enum class Service {
    SERVER,
    CLIENT,
}

enum class TYPE {
    BLOCKING,
    NONBLOCKING,
}