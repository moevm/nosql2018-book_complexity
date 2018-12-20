package com.bookscomplexity

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Date
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.nio.charset.Charset


fun parseEPUB(epub: Book): MutableMap<String, String> {
    val bookInfo = mutableMapOf<String, String>()

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

    val metadata = epub.metadata

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

fun getEPUBCover(epub: Book): ByteArray? {
    if (epub.coverImage != null) {
        return epub.coverImage.data
    } else {
        for (resource in epub.resources.all) {
            if (resource.mediaType.name == "image/jpeg") {
                return resource.data
            }
        }
    }
    return null
}


// fun parseTXT(pathOfBook: Path): MutableMap<String, String> {}