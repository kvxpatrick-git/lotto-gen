package com.hsmomo.lottogen.data.remote

import com.hsmomo.lottogen.domain.model.WinningDraw
import com.hsmomo.lottogen.util.SyncLog

class LottoRemoteDataSource(
    private val apiService: LottoApiService
) {
    companion object {
        private const val TAG = "LottoRemoteDataSource"
        private const val INITIAL_SYNC_FALLBACK_RANGE = 200
        private const val MAX_DRAWS_PER_REQUEST = 301
    }

    /**
     * 모든 회차 데이터 조회 (1회차부터 최신까지)
     */
    suspend fun fetchAllDraws(): Result<List<WinningDraw>> {
        SyncLog.i(TAG, "fetchAllDraws start")
        val bootstrapResult = apiService.fetchBootstrapDraws()
        if (bootstrapResult.isSuccess) {
            val bootstrapDraws = bootstrapResult.getOrThrow()
            if (bootstrapDraws.isNotEmpty()) {
                val bootstrapLatest = bootstrapDraws.maxOfOrNull { it.drawNo } ?: 0
                SyncLog.i(
                    TAG,
                    "fetchAllDraws bootstrap success count=${bootstrapDraws.size} latest=$bootstrapLatest"
                )

                val latestAvailable = apiService.getLatestAvailableDrawNo().getOrNull()
                if (latestAvailable == null || latestAvailable <= bootstrapLatest) {
                    return Result.success(bootstrapDraws)
                }

                SyncLog.i(
                    TAG,
                    "fetchAllDraws bootstrap gap detected; fetch tail=${bootstrapLatest + 1}..$latestAvailable"
                )
                val tailResult = fetchDrawsChunked(bootstrapLatest + 1, latestAvailable)
                if (tailResult.isFailure) {
                    SyncLog.w(
                        TAG,
                        "fetchAllDraws tail sync failed; keep bootstrap only: ${tailResult.exceptionOrNull()?.message}"
                    )
                    return Result.success(bootstrapDraws)
                }

                val merged = (bootstrapDraws + tailResult.getOrThrow())
                    .associateBy { it.drawNo }
                    .values
                    .sortedBy { it.drawNo }
                SyncLog.i(TAG, "fetchAllDraws merged bootstrap+tail count=${merged.size}")
                return Result.success(merged)
            }
            SyncLog.i(TAG, "fetchAllDraws bootstrap empty; switching to live sync")
        } else {
            SyncLog.w(
                TAG,
                "fetchAllDraws bootstrap failed; switching to live sync: ${bootstrapResult.exceptionOrNull()?.message}"
            )
        }

        return apiService.getLatestAvailableDrawNo().fold(
            onSuccess = { latestDrawNo ->
                SyncLog.i(TAG, "fetchAllDraws latestAvailable=$latestDrawNo")
                val fullSync = fetchDrawsChunked(1, latestDrawNo)
                if (fullSync.isSuccess) {
                    SyncLog.i(TAG, "fetchAllDraws full sync success count=${fullSync.getOrNull()?.size ?: 0}")
                    return@fold fullSync
                }
                SyncLog.w(TAG, "fetchAllDraws full sync failed; switching to fallback recent=$INITIAL_SYNC_FALLBACK_RANGE")

                // 첫 동기화 대량 요청이 실패할 경우 최신 구간이라도 확보해 앱 사용성을 유지한다.
                val fallbackStart = maxOf(1, latestDrawNo - INITIAL_SYNC_FALLBACK_RANGE + 1)
                SyncLog.i(TAG, "fetchAllDraws fallback range=$fallbackStart..$latestDrawNo")
                fetchDrawsChunked(fallbackStart, latestDrawNo)
            },
            onFailure = { error ->
                SyncLog.e(TAG, "fetchAllDraws failed while resolving latest", error)
                Result.failure(error)
            }
        )
    }

    /**
     * 특정 회차 이후의 데이터 조회
     */
    suspend fun fetchDrawsAfter(drawNo: Int): Result<List<WinningDraw>> {
        SyncLog.i(TAG, "fetchDrawsAfter start localLatest=$drawNo")
        return apiService.getLatestAvailableDrawNo().fold(
            onSuccess = { latestDrawNo ->
                SyncLog.i(TAG, "fetchDrawsAfter latestAvailable=$latestDrawNo")
                if (drawNo >= latestDrawNo) {
                    SyncLog.i(TAG, "fetchDrawsAfter no update required local=$drawNo latest=$latestDrawNo")
                    return@fold Result.success(emptyList())
                }
                SyncLog.i(TAG, "fetchDrawsAfter fetch range=${drawNo + 1}..$latestDrawNo")
                fetchDrawsChunked(drawNo + 1, latestDrawNo)
            },
            onFailure = { error ->
                SyncLog.e(TAG, "fetchDrawsAfter failed while resolving latest localLatest=$drawNo", error)
                Result.failure(error)
            }
        )
    }

    private suspend fun fetchDrawsChunked(startDrawNo: Int, endDrawNo: Int): Result<List<WinningDraw>> {
        if (startDrawNo > endDrawNo) {
            return Result.success(emptyList())
        }

        val result = mutableListOf<WinningDraw>()
        var chunkStart = startDrawNo
        while (chunkStart <= endDrawNo) {
            val chunkEnd = minOf(endDrawNo, chunkStart + MAX_DRAWS_PER_REQUEST - 1)
            SyncLog.i(TAG, "fetchDrawsChunked request range=$chunkStart..$chunkEnd")
            val chunkResult = apiService.fetchDraws(chunkStart, chunkEnd)
            if (chunkResult.isFailure) {
                return Result.failure(
                    chunkResult.exceptionOrNull()
                        ?: IllegalStateException("chunk fetch failed without exception")
                )
            }
            result += chunkResult.getOrThrow()
            chunkStart = chunkEnd + 1
        }
        return Result.success(result.distinctBy { it.drawNo }.sortedBy { it.drawNo })
    }

    /**
     * 최신 회차 번호 조회
     */
    suspend fun getLatestDrawNo(): Result<Int> {
        return apiService.getLatestAvailableDrawNo()
    }
}
