package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.content.*
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

fun main(args: Array<String>) {

    val backend = ServerSide.instance

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

            post("/SearchResultServlet") {
                val searchedBook = call.receiveParameters()["searchedBook"]

                if (searchedBook != null) {
                    val book = backend.getBooksFromDB(searchedBook)
                    call.respondText(book, ContentType.Application.Json)
                }
            }
        }
    }.start(wait = true)
}