import kotlin.properties.Delegates

fun program(countOfClients: Int = 1): Long {
    var server: Server by Delegates.notNull()
    val serverThread = Thread {
        server = Server(countOfClients, SERVER_PORT)
        server.start()
    }
    serverThread.start()

    val threads = (1..countOfClients).map {
        Thread {
            val client = Client(SERVER_ADDRESS, SERVER_PORT)
            client.start()
        }.apply { start() }
    }

    serverThread.join()
    threads.forEach { it.join() }
    server.stop()

    return server.timeNano
}