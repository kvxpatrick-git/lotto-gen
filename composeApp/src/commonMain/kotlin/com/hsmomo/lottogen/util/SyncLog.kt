package com.hsmomo.lottogen.util

import kotlinx.datetime.Clock

object SyncLog {
    fun d(tag: String, message: String) {
        println("[${Clock.System.now()}][SYNC/$tag][DEBUG] $message")
    }

    fun i(tag: String, message: String) {
        println("[${Clock.System.now()}][SYNC/$tag][INFO] $message")
    }

    fun w(tag: String, message: String) {
        println("[${Clock.System.now()}][SYNC/$tag][WARN] $message")
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        val suffix = if (error == null) "" else buildErrorSuffix(error)
        println("[${Clock.System.now()}][SYNC/$tag][ERROR] $message$suffix")
    }

    private fun buildErrorSuffix(error: Throwable): String {
        val chain = generateSequence(error) { it.cause }
            .take(4)
            .joinToString(" <- ") { "${it::class.simpleName}:${it.message}" }
        val topFrame = error.stackTrace.firstOrNull()?.let { " @ ${it.className}.${it.methodName}:${it.lineNumber}" } ?: ""
        return " | error=$chain$topFrame"
    }
}
