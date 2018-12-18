package com.bookscomplexity

import com.kursx.parser.fb2.FictionBook
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path


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
                val param = call.receiveParameters()["id"]

                if (param != null) {
                    val book = backend.getBookInfo(param)
                    call.respondText(book, ContentType.Application.Json)
                }
            }

            post("/TopBooksServlet") {
                val topBooks = backend.getTopBooks()
                call.respondText(topBooks, ContentType.Application.Json)
            }

            post("/TopAuthServlet") {
                val topAuthors = backend.getTopAuthors()
                call.respondText(topAuthors, ContentType.Application.Json)
            }

            post("/AvgDifficultyServlet") {
                val avgDif = backend.getAvgDifficulty()
                call.respondText(avgDif, ContentType.Application.Json)
            }

            post("/bookUpload") {
                val multipart = call.receiveMultipart()
                val book = mutableMapOf<String, String>()

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            book +=
                                    when (part.name) {
                                        "bookTitle" -> "title" to part.value
                                        "bookAuthor" -> "author" to part.value
                                        "bookYear" -> "year" to part.value
                                        else -> return@forEachPart
                                    }
                        }
                        is PartData.FileItem -> {
                            if (part.originalFileName!!.endsWith("txt")) {
                                val bytes = part.streamProvider().readBytes()
                                book += "text" to bytes.toString(Charset.defaultCharset())
                            }

                            if (part.originalFileName!!.endsWith("fb2")) {
                                val pathOfBook = Path.of("src/book.fb2")
                                Files.copy(part.streamProvider(), pathOfBook)

                                val fb2 = FictionBook(File("src/book.fb2"))
                                Files.delete(pathOfBook)

                                var result = ""

                                for (chapter in fb2.body.sections) {
                                    for (element in chapter.elements) {
                                        result += element.text + " "
                                    }
                                }

                                book += "text" to result
                            }

                            if (part.originalFileName!!.endsWith("epub")) {
                                val pathOfBook = Path.of("src/book.epub")
                                Files.copy(part.streamProvider(), pathOfBook)

                                val epub = EpubReader().readEpub(FileInputStream(pathOfBook.toString()))
                                Files.delete(pathOfBook)

                                val spine = epub.spine
                                var result = ""
                                val coverIndex = spine.getResourceIndex("cover.xhtml")

                                for (i in (coverIndex + 1) until spine.size()) {
                                    result += spine.getResource(i).data.toString(Charset.defaultCharset())
                                }

                                val text = Jsoup.parse(result)
                                        .select("p")
                                        .eachText()
                                        .toString()
                                        .drop(1)
                                        .dropLast(1)

                                book += "text" to text
                            }
                        }
                    }
                    part.dispose()
                }

                if (book["text"] != null && book["title"] != null && book["author"] != null && book["year"] != null) {
                    backend.processBook(book["text"]!!, book["title"]!!, book["author"]!!, book["year"]!!)
                    call.respond(HttpStatusCode.Accepted)
                }
                else {
                    call.respond(HttpStatusCode.NotAcceptable)
                }


            }
        }
    }.start(wait = true)
}