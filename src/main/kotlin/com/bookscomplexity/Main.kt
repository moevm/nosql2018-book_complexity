package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.post
import io.ktor.routing.get
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
import java.nio.file.Paths
import java.time.ZoneId
import nl.siegmann.epublib.domain.Date
import org.bson.types.ObjectId
import java.io.InputStream


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

            get("/cover") {
                val parameters = call.request.queryParameters
                val idHexString = parameters["id"]
                val cover = backend.getCover(ObjectId(idHexString))
                call.respondBytes(cover, ContentType.Image.JPEG, HttpStatusCode.OK)
            }

            post("/SearchResultServlet") {
                val searchedBook = call.receiveParameters()["searchedBook"]

                if (searchedBook != null) {
                    val book = backend.getBooksFromDB(searchedBook)
                    call.respondText(book, ContentType.Application.Json)
                }
            }

            get("/export") {
                val log = "mongoexport --db nosql --collection books_stats --out export.json".runCommand()
                println(log)
                call.respondFile(File("export.json"))
                Files.delete(Paths.get("export.json"))
            }

            post("/import") {
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val originalFileName = part.originalFileName!!.toLowerCase()
                            if (originalFileName.endsWith("json")) {
                                val path = Paths.get("import.json")
                                Files.copy(part.streamProvider(), path)

                                val log = "mongoimport --upsertFields _id --db nosql --collection books_stats --file import.json ".runCommand()
                                println(log)

                                Files.delete(path)

                                call.respond(HttpStatusCode.Accepted)
                            }

                        }
                    }
                }
                call.respond(HttpStatusCode.NotAcceptable)

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
                val bookInfoFromRequest = mutableMapOf<String, String>()
                val bookInfoFromEBook = mutableMapOf<String, String>()
                var cover: ByteArray? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.value != "") {
                                bookInfoFromRequest +=
                                    when (part.name) {
                                        "bookTitle" -> "title" to part.value
                                        "bookAuthor" -> "author" to part.value
                                        "bookYear" -> "year" to part.value
                                        else -> return@forEachPart
                                    }
                            }
                        }

                        is PartData.FileItem -> {
                            val originalFileName = part.originalFileName!!.toLowerCase()
                            if (originalFileName.endsWith("txt")) {
                                val bytes = part.streamProvider().readBytes()
                                bookInfoFromEBook.put("text", bytes.toString(Charset.defaultCharset()))
                            }

                            if (originalFileName.endsWith("epub")) {
                                val pathOfBook = Paths.get("src/book.epub")
                                Files.copy(part.streamProvider(), pathOfBook)
                                val epub = EpubReader().readEpub(FileInputStream(pathOfBook.toString()))
                                Files.delete(pathOfBook)

                                bookInfoFromEBook.putAll(parseEPUB(epub))
                                if (cover == null) cover = getEPUBCover(epub)
                            }

                            if (originalFileName.endsWith("jpg")) {
                                cover = part.streamProvider().readBytes()
                            }
                        }
                    }
                    part.dispose()
                }

                val bookInfo = bookInfoFromEBook
                bookInfo.putAll(bookInfoFromRequest) // values in bookInfoFromRequest rewrite values in bookInfo

                if (cover != null) {
                    val id = backend.saveCover(cover)
                    bookInfo["cover"] = id.toHexString()
                } else {
                    // standard cover
                    bookInfo["cover"] = "5c1acd37de5f05148d0763db"
                }

                if (bookInfo["text"] != null && bookInfo["title"] != null && bookInfo["author"] != null && bookInfo["year"] != null) {
                    backend.saveBook(bookInfo["text"]!!, bookInfo["title"]!!, bookInfo["author"]!!, bookInfo["year"]!!, bookInfo["cover"]!!)
                    call.respondRedirect("/nahi")
                }
                else {
                    call.respond(HttpStatusCode.NotAcceptable)
                }
            }
        }
    }.start(wait = true)
}