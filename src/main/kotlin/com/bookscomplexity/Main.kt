package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.cio.parseRequest
import io.ktor.http.content.*
import io.ktor.request.receiveParameters
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            static("/") {
                staticRootFolder = File("src/frontend")
                static("css") {
                    files("css")
                }
                static("html") {
                    files("html")
                }
                static("vendor") {
                    files("vendor")
                }
                static("img") {
                    files("img")
                }
                file("/index.html", "index.html")
                default("index.html")
            }

            get("/") {
                val alarm = call.request.queryParameters["searchedBook"]
                System.out.println("$alarm")
            }
        }
    }.start(wait = true)
}