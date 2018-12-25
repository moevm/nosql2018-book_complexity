package com.bookscomplexity

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import nl.siegmann.epublib.epub.EpubReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.File
import java.nio.file.Paths

val backend = ServerSide.instance


fun saveBook(bookPath: String) {
    val bookInfo = mutableMapOf<String, String>()
    var cover: ByteArray? = null

    val pathOfBook = Paths.get(bookPath)
    val epub = EpubReader().readEpub(FileInputStream(pathOfBook.toString()))
    bookInfo.putAll(parseEPUB(epub))
    cover = getEPUBCover(epub)

    if (cover != null) {
        val id = backend.saveCover(cover)
        bookInfo["cover"] = id.toHexString()
    } else {
        // standard cover
        bookInfo["cover"] = "5c1acd37de5f05148d0763db"
    }

    if (bookInfo["text"] != null && bookInfo["title"] != null && bookInfo["author"] != null && bookInfo["year"] != null) {
        backend.saveBook(bookInfo["text"]!!, bookInfo["title"]!!, bookInfo["author"]!!, bookInfo["year"]!!, bookInfo["cover"]!!)
    }
}

fun main() {

    saveBook("../python/texts/aleph.gutenberg.org/cache/epub/10003/pg10003-images.epub")

    for (line in File("../resources/books_paths.txt").readLines()) {
        println(line)
        saveBook(line)
        Thread.sleep(10_000)
    }

    // print("start")
    // Thread.sleep(4_000)
    // print("end")
    // saveBook("../python/texts/aleph.gutenberg.org/cache/epub/10003/pg10003-images.epub")
    // saveBook("../python/texts/aleph.gutenberg.org/cache/epub/1/pg1-images.epub")
}