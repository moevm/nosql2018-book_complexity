package com.bookscomplexity

import com.google.gson.Gson
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*
import java.io.IOException
import java.util.concurrent.TimeUnit

data class Book (val _id: ObjectId,
                 val title: String,
                 val author: String,
                 val words_count: Int,
                 val unique_words_count: Int,
                 val unique_stems_count: Int,
                 val lexicon_years: ByteArray,
                 val lexicon_rarity: Double,
                 val difficulty: Double)

data class Text (val _id: Int,
                 val text: String)

fun String.runCommand(): String? {
    try {
        val parts = arrayOf("/bin/sh", "-c", this)
        val proc = ProcessBuilder(*parts)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText() + proc.errorStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}

class ServerSide private constructor() {

    private val client = KMongo.createClient()
    private val database = client.getDatabase("nosql")
    private val col = database.getCollection<Book>("books_stats")


    private object Holder { val INSTANCE = ServerSide() }

    companion object {
        val instance: ServerSide by lazy { Holder.INSTANCE }
    }

    fun processBook(text: String, title: String, author: String) {
        val texts = database.getCollection<Text>("texts")
        texts.insertOne(Text(0, text))

        val log = "mongo < src/main/resources/mongo.js".runCommand()
        val log_splitted = log?.split("\n")
        val id_hash = log_splitted?.get(log_splitted.size - 3)?.substring(10, 34)
        val id = ObjectId(id_hash)

        val update = "{$set: {title: \"$title\", author: \"$author\"}}"
        col.updateOneById(id, update)
    }

    fun getBookInfo(bookId: String) =
            Gson().toJson(col.findOneById(ObjectId(bookId)))


    fun getBooksFromDB(order: String): String {

        col.createIndex("{ title: \"text\", author: \"text\" }")
        val result = col.find(" { \$text: { \$search: \"$order\" } }")

        return Gson().toJson(result.toMutableList())
    }

    fun getBooksCount() = col.countDocuments()

    
}