package ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import utils.BODY
import utils.SERVER_PORT

class KtorServer {
    fun start() {
        embeddedServer(CIO, port = SERVER_PORT) {
            routing {
                post("/") {
                    call.respond(HttpStatusCode.OK, BODY)
                }
            }
        }.start(wait = true)
    }
}