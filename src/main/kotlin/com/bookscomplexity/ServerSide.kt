package com.bookscomplexity

import com.google.gson.Gson
import org.litote.kmongo.*

data class Books (val Books: MutableList<Book>)

data class Book (val title: String,
                 val author: String,
                 val words_count: Int,
                 val unique_words_count: Int,
                 val unique_stems_count: Int,
                 val lexicon_years: ByteArray,
                 val lexicon_rarity: Double,
                 val difficulty: Double)

class ServerSide private constructor() {

    private val client = KMongo.createClient()
    private val database = client.getDatabase("nosql")
    private val col = database.getCollection<Book>("books_stats")


    private object Holder { val INSTANCE = ServerSide() }

    companion object {
        val instance: ServerSide by lazy { Holder.INSTANCE }
    }

    fun insertBook() {

    }

    fun getBookInfo(title: String, author: String): String {
        val info = col.findOne(Book::title eq title, Book::author eq author)
        return Gson().toJson(info)
    }
           

    fun getBooksFromDB(order: String): String {
        col.createIndex("{ title: \"text\", author: \"text\" }")
        val books = col.find(" { \$text: { \$search: \"$order\" } }")

        val result = mutableListOf<Book>()
        result.addAll(books)

        return Gson().toJson(result)
    }
}