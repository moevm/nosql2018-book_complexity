package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.content.*
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File
import java.nio.charset.Charset


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
                static("js") {
                    files("js")
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

            post("/BookAnalysisServlet") {
                val bookAuthor = call.receiveParameters()["author"]
                val bookTitle = call.receiveParameters()["title"]

                if (bookAuthor != null && bookTitle != null) {
                    val book = backend.getBookInfo(bookTitle, bookAuthor)
                    call.respondText(book, ContentType.Application.Json)
                }
            }

            post("/bookUpload") {
                val multipart = call.receiveMultipart()
                val book = mutableMapOf<String, String>()

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            book += if (part.name == "bookTitle") {
                                "title" to part.value
                            } else {
                                "author" to part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.originalFileName!!.endsWith("txt")) {
                                val bytes = part.streamProvider().readBytes()
                                book += "text" to bytes.toString(Charset.defaultCharset())
                            }
                        }
                    }
                    part.dispose()
                }
            }
        }
    }.start(wait = true)
}