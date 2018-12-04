package com.bookscomplexity

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*

data class Book (val title: String,
                 val author: String,
                 val words_count: Int,
                 val unique_words_count: Int,
                 val unique_stems_count: Int,
                 val lexicon_years: ByteArray,
                 val lexicon_rarity: Double,
                 val difficulty: Double)

data class Text (val _id: ObjectId,
                 val text: String)

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

    fun doStemming(text: String, title: String, author: String) {

        // step 1 - insert text to collection
        val id = ObjectId()
        val doc = Text(id, text)
        val texts = database.getCollection<Text>("texts")
        texts.insertOne(doc)

        // step 2 - aggregate
        aggregateTexts(texts, id)
    }

    private fun aggregateTexts(texts: MongoCollection<Text>, id: ObjectId) {
        texts.aggregate<Any>("""[
	        { $match: { _id: $id } },
	        { $project: { words: { $ split: ["$ text", " "] } } },
	        { $unwind : "$ words" },
	        { $project: {_id: 0, w: "$ words"} },
	        { $out : "book_splited_text" }
            ]""".formatJson())
    }
}