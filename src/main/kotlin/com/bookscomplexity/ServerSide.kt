package com.bookscomplexity

import com.google.gson.Gson
import org.bson.BsonDocument
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.GridFSBucket
import java.io.*


data class Text (val _id: Int,
                 val text: String)

class ServerSide private constructor() {

    private val client = KMongo.createClient()
    private val database = client.getDatabase("nosql")
    private val col = database.getCollection("books_stats")
    private val gridFSBucket = GridFSBuckets.create(database)


    private object Holder { val INSTANCE = ServerSide() }

    companion object {
        val instance: ServerSide by lazy { Holder.INSTANCE }
    }

    fun saveBook(text: String, title: String, author: String, year: String, coverHexId: String) {
        val texts = database.getCollection<Text>("texts")
        texts.insertOne(Text(0, text))

        val id = processBook()
        val coverId = ObjectId(coverHexId)
        val updateRequest = "{$set: {title: \"$title\", author: \"$author\", year: $year, cover: ${coverId.json}}}"
        col.updateOneById(id, updateRequest)
    }

    private fun processBook(): ObjectId {
        val log = "mongo < src/main/resources/mongo.js".runCommand()
        println(log)
        val log_splitted = log?.split("\n")
        val id_hash = log_splitted?.get(log_splitted.size - 3)?.substring(10, 34)

        return ObjectId(id_hash)
    }

    fun getBookInfo(bookId: String): String {
        val result = col.findOneById(ObjectId(bookId))

        try {
            result!!["_id"] = result["_id"].toString()
            result!!["cover"] = result["cover"].toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Gson().toJson(result)
    }

    fun saveCover(cover: ByteArray?): ObjectId {
        // save cover to db and return its name

        val fileId = gridFSBucket.uploadFromStream("mongodb-tutorial", ByteArrayInputStream(cover))
        return fileId
    }

    fun getCover(id: ObjectId): ByteArray {
        val streamToDownloadTo = ByteArrayOutputStream()
        gridFSBucket.downloadToStream(id, streamToDownloadTo);
        // streamToDownloadTo.close();
        return streamToDownloadTo.toByteArray()
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
                $match: {
                    year: { $exists: true, $ne: null }
                }
            },
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
            val index = (it["_id"]?.asDouble()?.intValue()!! - 1500) / 10
            difficultyArray[index] = it["difficulty"]?.asDouble()?.value!!
        }

        return Gson().toJson(difficultyArray)
    }
}