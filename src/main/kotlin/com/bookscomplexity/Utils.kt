package com.bookscomplexity

import java.io.IOException
import java.util.concurrent.TimeUnit

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