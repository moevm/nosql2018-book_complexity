package com.bookscomplexity

import com.google.gson.Gson
import org.bson.BsonDocument
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

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
    private val col = database.getCollection("books_stats")


    private object Holder { val INSTANCE = ServerSide() }

    companion object {
        val instance: ServerSide by lazy { Holder.INSTANCE }
    }

    fun processBook(text: String, title: String, author: String, year: String) {
        val texts = database.getCollection<Text>("texts")
        texts.insertOne(Text(0, text))

        val log = "mongo < src/main/resources/mongo.js".runCommand()
        val log_splitted = log?.split("\n")
        val id_hash = log_splitted?.get(log_splitted.size - 3)?.substring(10, 34)
        val id = ObjectId(id_hash)

        val update = "{$set: {title: \"$title\", author: \"$author\", year: \"$year\"}}"
        col.updateOneById(id, update)
    }

    fun getBookInfo(bookId: String): String {
        val result = col.findOneById(ObjectId(bookId))

        try {
            result!!["_id"] = result["_id"].toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Gson().toJson(result)
    }


    fun getBooksFromDB(order: String): String {
        col.createIndex("{ title: \"text\", author: \"text\" }")

        val result = col.find(" { \$text: { \$search: \"$order\" } }")
                .toMutableList()
        result.forEach { it["_id"] = it["_id"].toString() }

        return Gson().toJson(result)
    }

    fun getTopBooks(): String {
        val result = col.find().sort("{difficulty: -1}").limit(10)
                .toMutableList()
        result.forEach { it["_id"] = it["_id"].toString() }
        return Gson().toJson(result)
    }


    fun getTopAuthors(): String {

        val json = """
            [
                {
                    $group: {
                        _id: "$ author",
                        difficulty: { $avg: "$ difficulty"}
                    }
                },
                { $sort: { difficulty: -1 } },
                { $limit: 10 }
            ]
        """.formatJson()

        val topAuthors = col.aggregate<BsonDocument>(json).toMutableList()
        return topAuthors.toString().formatJson()
    }

    fun getAvgDifficulty(): String {

        val json = """[
            {
                $group: {
                    _id: {$multiply: [10, {$floor: {$divide: ["$ year", 10]}}]},
                    difficulty: { $avg: "$ difficulty"}
                }
            },
            { $sort: { _id: 1 } }
        ]""".formatJson()

        val avgDif = col.aggregate<BsonDocument>(json).toMutableList()
        val difficultyArray = DoubleArray(60)

        avgDif.forEach {
            val index = (it["_id"]?.asDouble()?.intValue()!! - 1500) / 60
            difficultyArray[index] = it["difficulty"]?.asDouble()?.value!!
        }

        return Gson().toJson(difficultyArray)
    }
}