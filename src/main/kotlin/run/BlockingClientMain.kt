package run

import blocking.BlockingClient
import utils.SERVER_ADDRESS
import utils.SERVER_PORT

fun runBlockingClient() = runClient {
    val client = BlockingClient(SERVER_ADDRESS, SERVER_PORT)
    client.start()
}