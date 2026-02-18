package com.hsmomo.lottogen.di

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            headers.append(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )
            headers.append(HttpHeaders.Accept, "*/*")
            headers.append(HttpHeaders.Referrer, "https://www.dhlottery.co.kr/gameResult.do?method=byWin")
            headers.append(HttpHeaders.Origin, "https://www.dhlottery.co.kr")
            headers.append(HttpHeaders.AcceptLanguage, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
}
