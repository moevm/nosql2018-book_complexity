package com.bookscomplexity

import io.ktor.http.content.streamProvider
import nl.siegmann.epublib.domain.Date
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

fun parseEPUB(pathOfBook: Path): MutableMap<String, String> {
    val bookInfo = mutableMapOf<String, String>()

    val epub = EpubReader().readEpub(FileInputStream(pathOfBook.toString()))

    val spine = epub.spine
    var result = ""

    for (i in 0 until spine.size()) {
        result += spine.getResource(i).data.toString(Charset.defaultCharset())
    }

    val text = Jsoup.parse(result)
            .select("p")
            .eachText()
            .toString()
            .drop(1)
            .dropLast(1)

    bookInfo += "text" to text


    val metadata = epub.metadata;

    val authors = metadata.authors.joinToString()
    bookInfo += "author" to authors

    val title = metadata.titles[0]
    bookInfo += "title" to title


    for (date in metadata.dates) {
        if (date.event == Date.Event.PUBLICATION) {
            val publicationYear = date.getValue().substring(0, 4)
            bookInfo += "year" to publicationYear
        }
    }

    return bookInfo
}


// fun parseTXT(pathOfBook: Path): MutableMap<String, String> {}