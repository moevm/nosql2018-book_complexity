package com.bookscomplexity

import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

data class Book (val title: String,
                 val author: String,
                 val words_count: Int,
                 val unique_words_count: Int,
                 val unique_stems_count: Int,
                 val lexicon_years: Byte,
                 val lexicon_rarity: Double,
                 val difficulty: Double)

class ServerSide private constructor() {

    private val client = KMongo.createClient()
    private val database = client.getDatabase("nosql")
    private val col = database.getCollection<Book>()

    private object Holder { val INSTANCE = ServerSide() }

    companion object {
        val instance: ServerSide by lazy { Holder.INSTANCE }
    }

    fun insertBook() {

    }

    fun getBookFromDB(order: String) = col.findOne(Book::title eq order)
}