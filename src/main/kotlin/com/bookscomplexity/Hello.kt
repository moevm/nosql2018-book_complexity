package com.bookscomplexity

import org.litote.kmongo.* //NEEDED! import KMongo extensions

data class Jedi(val name: String, val age: Int)


//here the name of the collection by convention is "jedi"
//you can use getCollection<Jedi>("otherjedi") if the collection name is different

fun main(args: Array<String>) {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("test") //normal java driver usage
    val col = database.getCollection<Jedi>() //KMongo extension method

    col.insertOne(Jedi("Luke Skywalker", 19))
    val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")
}



