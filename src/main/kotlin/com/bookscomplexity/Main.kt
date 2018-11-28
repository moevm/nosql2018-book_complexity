package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText ( "Hello, world", ContentType.Text.Plain)
            }
        }
    }.start(wait = true)
}



