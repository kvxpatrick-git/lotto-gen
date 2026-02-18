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
        val suffix = if (error == null) {
            ""
        } else {
            " | error=${error::class.simpleName}:${error.message}"
        }
        println("[${Clock.System.now()}][SYNC/$tag][ERROR] $message$suffix")
    }
}
