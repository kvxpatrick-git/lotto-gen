package com.hsmomo.lottogen.data.remote

import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.util.SyncLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProxyLatestResponse(
    @SerialName("latestDrawNo")
    val latestDrawNo: Int
)

@Serializable
data class ProxyDrawsResponse(
    @SerialName("draws")
    val draws: List<ProxyDraw>
)

@Serializable
data class ProxyDraw(
    @SerialName("drawNo")
    val drawNo: Int,
    @SerialName("drawDate")
    val drawDate: String,
    @SerialName("numbers")
    val numbers: List<Int>,
    @SerialName("bonus")
    val bonus: Int,
    @SerialName("firstPrizeAmount")
    val firstPrizeAmount: Long
)

class LottoApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    companion object {
        private const val TAG = "LottoApiService"
    }

    suspend fun getLatestAvailableDrawNo(): Result<Int> {
        return request("GET /api/lotto/latest") {
            val response: ProxyLatestResponse = httpClient.get("$baseUrl/api/lotto/latest").body()
            SyncLog.i(TAG, "proxy latestDrawNo=${response.latestDrawNo}")
            response.latestDrawNo
        }
    }

    suspend fun fetchBootstrapDraws(): Result<List<WinningDraw>> {
        val endpoint = "GET /api/lotto/bootstrap"
        SyncLog.i(TAG, "request start baseUrl=$baseUrl endpoint=$endpoint")
        return try {
            val response: ProxyDrawsResponse = httpClient.get("$baseUrl/api/lotto/bootstrap").body()
            SyncLog.i(TAG, "proxy bootstrap fetched count=${response.draws.size}")
            Result.success(response.draws.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                SyncLog.w(TAG, "proxy bootstrap unavailable: 404")
                return Result.success(emptyList())
            }
            val body = runCatching { e.response.bodyAsText().take(400) }.getOrDefault("<unavailable>")
            SyncLog.e(
                TAG,
                "request 4xx baseUrl=$baseUrl endpoint=$endpoint status=${e.response.status.value} body=$body",
                e
            )
            Result.failure(ApiResponseSyncException("Proxy 4xx error: ${e.message}", e))
        } catch (e: ServerResponseException) {
            val body = runCatching { e.response.bodyAsText().take(400) }.getOrDefault("<unavailable>")
            SyncLog.e(
                TAG,
                "request 5xx baseUrl=$baseUrl endpoint=$endpoint status=${e.response.status.value} body=$body",
                e
            )
            Result.failure(NetworkSyncException("Proxy 5xx error: ${e.message}", e))
        } catch (e: Exception) {
            SyncLog.e(TAG, "request failed baseUrl=$baseUrl endpoint=$endpoint", e)
            Result.failure(
                NetworkSyncException("Proxy request failed: ${e::class.simpleName} ${e.message}", e)
            )
        }
    }

    suspend fun fetchDraw(drawNo: Int): Result<WinningDraw> {
        return fetchDraws(drawNo, drawNo).mapCatching { draws ->
            draws.firstOrNull() ?: throw ApiResponseSyncException("Draw not found: $drawNo")
        }
    }

    suspend fun fetchDraws(startDrawNo: Int, endDrawNo: Int): Result<List<WinningDraw>> {
        if (startDrawNo > endDrawNo) {
            return Result.success(emptyList())
        }

        return request("GET /api/lotto/draws?start=$startDrawNo&end=$endDrawNo") {
            val response: ProxyDrawsResponse = httpClient.get("$baseUrl/api/lotto/draws") {
                parameter("start", startDrawNo)
                parameter("end", endDrawNo)
            }.body()
            SyncLog.i(
                TAG,
                "proxy draws fetched range=$startDrawNo..$endDrawNo count=${response.draws.size}"
            )
            response.draws.map { it.toDomain() }
        }
    }

    private suspend fun <T> request(endpoint: String, block: suspend () -> T): Result<T> {
        SyncLog.i(TAG, "request start baseUrl=$baseUrl endpoint=$endpoint")
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            val body = runCatching { e.response.bodyAsText().take(400) }.getOrDefault("<unavailable>")
            SyncLog.e(
                TAG,
                "request 4xx baseUrl=$baseUrl endpoint=$endpoint status=${e.response.status.value} body=$body",
                e
            )
            Result.failure(ApiResponseSyncException("Proxy 4xx error: ${e.message}", e))
        } catch (e: ServerResponseException) {
            val body = runCatching { e.response.bodyAsText().take(400) }.getOrDefault("<unavailable>")
            SyncLog.e(
                TAG,
                "request 5xx baseUrl=$baseUrl endpoint=$endpoint status=${e.response.status.value} body=$body",
                e
            )
            Result.failure(NetworkSyncException("Proxy 5xx error: ${e.message}", e))
        } catch (e: Exception) {
            SyncLog.e(TAG, "request failed baseUrl=$baseUrl endpoint=$endpoint", e)
            Result.failure(
                NetworkSyncException("Proxy request failed: ${e::class.simpleName} ${e.message}", e)
            )
        }
    }

    private fun ProxyDraw.toDomain(): WinningDraw {
        return WinningDraw(
            drawNo = drawNo,
            drawDate = drawDate,
            numbers = numbers.sorted(),
            bonus = bonus,
            firstPrizeAmount = firstPrizeAmount
        )
    }
}
