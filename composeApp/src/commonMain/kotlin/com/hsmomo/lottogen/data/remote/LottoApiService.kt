package com.hsmomo.lottogen.data.remote

import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.util.SyncLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
        return request {
            val response: ProxyLatestResponse = httpClient.get("$baseUrl/api/lotto/latest").body()
            SyncLog.i(TAG, "proxy latestDrawNo=${response.latestDrawNo}")
            response.latestDrawNo
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

        return request {
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

    private suspend fun <T> request(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            Result.failure(ApiResponseSyncException("Proxy 4xx error: ${e.message}", e))
        } catch (e: ServerResponseException) {
            Result.failure(NetworkSyncException("Proxy 5xx error: ${e.message}", e))
        } catch (e: Exception) {
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
